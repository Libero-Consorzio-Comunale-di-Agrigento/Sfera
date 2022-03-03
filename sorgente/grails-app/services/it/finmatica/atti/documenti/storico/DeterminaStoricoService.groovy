package it.finmatica.atti.documenti.storico

import groovy.xml.StreamingMarkupBuilder
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.DeterminaSoggetto
import it.finmatica.atti.documenti.DocumentoCollegato
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestioneiter.motore.WkfStep

class DeterminaStoricoService {

	DatiAggiuntiviService datiAggiuntiviService
	IDocumentaleEsterno gestoreDocumentaleEsterno
	IGestoreFile gestoreFile

	public DeterminaStorico storicizza (Determina determina, WkfStep stepPrecedente, WkfStep stepSuccessivo) {
		DeterminaStorico storico = new DeterminaStorico ()
		storico.idDetermina = determina.id
		storico.revisione	= getNextRevisione(storico.idDetermina)
		storico.iter        = determina.iter
		storico.step		= stepPrecedente
		storico.xmlSoggetti = creaXmlSoggetti (determina)
		storico.xmlDetermineCollegate  = creaXmlDocumentiCollegati (determina)

		storico.iter                   = determina.iter
		storico.tipologia              = determina.tipologia
		storico.stato                  = determina.stato
		storico.statoFirma             = determina.statoFirma
		storico.statoConservazione     = determina.statoConservazione
		storico.statoOdg               = determina.statoOdg
		storico.modelloTesto           = determina.modelloTesto
		storico.categoria              = determina.categoria
		storico.xmlDatiAggiuntivi	   = datiAggiuntiviService.creaXmlDatiAggiuntivi(determina.datiAggiuntivi)
		storico.commissione            = determina.commissione
		storico.oggettoSeduta          = determina.oggettoSeduta
		storico.oggetto                = determina.oggetto
		storico.dataProposta           = determina.dataProposta
		storico.dataNumeroProposta     = determina.dataNumeroProposta
		storico.numeroProposta         = determina.numeroProposta
		storico.annoProposta           = determina.annoProposta
		storico.registroProposta       = determina.registroProposta
		storico.dataNumeroDetermina    = determina.dataNumeroDetermina
		storico.numeroDetermina        = determina.numeroDetermina
		storico.annoDetermina          = determina.annoDetermina
		storico.registroDetermina      = determina.registroDetermina
		storico.dataNumeroProtocollo   = determina.dataNumeroProtocollo
		storico.numeroProtocollo       = determina.numeroProtocollo
		storico.annoProtocollo         = determina.annoProtocollo
		storico.registroProtocollo     = determina.registroProtocollo
		storico.controlloFunzionario   = determina.controlloFunzionario
		storico.riservato              = determina.riservato
		storico.note                   = determina.note
		storico.noteTrasmissione       = determina.noteTrasmissione
		storico.noteContabili          = determina.noteContabili
		storico.classificaCodice       = determina.classificaCodice
		storico.classificaDal          = determina.classificaDal
		storico.classificaDescrizione  = determina.classificaDescrizione
		storico.fascicoloAnno          = determina.fascicoloAnno
		storico.fascicoloNumero        = determina.fascicoloNumero
		storico.fascicoloOggetto       = determina.fascicoloOggetto
		storico.pubblicaRevoca         = determina.pubblicaRevoca
		storico.giorniPubblicazione    = determina.giorniPubblicazione
		storico.dataPubblicazione      = determina.dataPubblicazione
		storico.dataFinePubblicazione  = determina.dataFinePubblicazione
		storico.dataPubblicazione2     = determina.dataPubblicazione2
		storico.dataFinePubblicazione2 = determina.dataFinePubblicazione2
		storico.dataEsecutivita        = determina.dataEsecutivita
		storico.campiProtetti          = determina.campiProtetti
		storico.codiciVistiTrattati    = determina.codiciVistiTrattati
		storico.codiceGara             = determina.codiceGara
		storico.idDocumentoEsterno     = determina.idDocumentoEsterno
		storico.valido                 = determina.valido
		storico.ente                   = determina.ente
		storico.dateCreated            = determina.dateCreated
		storico.utenteIns              = determina.utenteIns
		storico.dataScadenza		   = determina.dataScadenza
		storico.motivazione			   = determina.motivazione
		storico.priorita			   = determina.priorita

		storico.save()

		if (Impostazioni.INTEGRAZIONE_GDM.abilitato) {
			// se presente il gestore documentale esterno, storicizzo i file.
			gestoreDocumentaleEsterno?.storicizzaDocumento(storico)
		}

		if (determina.testo != null) {
			storico.testo = determina.testo.creaFileAllegatoStorico()

			gestoreFile.addFileStorico (storico, storico.testo, determina.testo)
		}

		if (determina.stampaUnica != null) {
			storico.stampaUnica = determina.stampaUnica.creaFileAllegatoStorico()

			gestoreFile.addFileStorico (storico, storico.stampaUnica, determina.stampaUnica)
		}

		return storico
	}

	/**
	 * Contiene un xml con i soggetti della determina al momento della storicizzazione:
	 * <soggetti>
	 * 	<soggetto tipo="UNITA/COMPONENTE" cognomeNome="COGNOME_NOME" descrizione="DESCR UNITA" utente="CODICE_UTENTE_AD4" progrUo="PROGRESSIVO_UO" ottica="CODICE_OTTICA" dal="DD/MM/YYYY" />
	 *  ..
	 * </soggetti>
	 */
	private String creaXmlSoggetti (Determina determina) {
		StreamingMarkupBuilder xml = new StreamingMarkupBuilder()
		return xml.bind { builder ->
			soggetti {
				for (DeterminaSoggetto ds : determina.soggetti) {
					if (ds.utenteAd4 != null) {
						As4SoggettoCorrente s = As4SoggettoCorrente.findByUtenteAd4(ds.utenteAd4)
						soggetto(tipo: ds.tipoSoggetto.categoria
								, cognomeNome: s.cognome + " " + s.nome
								, utente: ds.utenteAd4.id
								, descrizione: ds.unitaSo4?.descrizione
								, progrUo: ds.unitaSo4?.progr
								, ottica: ds.unitaSo4?.ottica?.codice
								, dal: ds.unitaSo4?.dal?.format("dd/MM/yyyy"))
					} else {
						soggetto(tipo: ds.tipoSoggetto?.categoria
								, descrizione: ds.unitaSo4?.descrizione
								, progrUo: ds.unitaSo4?.progr
								, ottica: ds.unitaSo4?.ottica?.codice
								, dal: ds.unitaSo4?.dal?.format("dd/MM/yyyy"))
					}
				}
			}
		}.toString()
	}

	/**
	 * Contiene un xml con i documenti collegati:
	 * <documentiCollegati>
	 * 	<determina id="" operazione="INTEGRAZIONE/ANNULLAMENTO">
	 * 		<oggetto>OGGETTO_DETERMINA</oggetto>
	 * 		<numeroProposta  numero="NUMERO" anno="ANNO" tipoRegistro="TIPO_REG" data="DD/MM/YYYY" />
	 * 		<numeroDetermina numero="NUMERO" anno="ANNO" tipoRegistro="TIPO_REG" data="DD/MM/YYYY" />
	 * 	</determina>
	 *
	 *  <delibera id="" operazione="INTEGRAZIONE/ANNULLAMENTO">
	 *   ...
	 *  </delibera>
	 *
	 *  ...
	 * </determine>
	 */
	private String creaXmlDocumentiCollegati (def dete) {
		StreamingMarkupBuilder xml 	= new StreamingMarkupBuilder()
		return xml.bind { builder ->
			documentiCollegati {
				for (DocumentoCollegato dc : dete.documentiCollegati) {

					if (dc.determinaCollegata != null) {
						determina (id: dc.id, operazione: dc.operazione) {
							oggetto (dc.determinaCollegata.oggetto)
							numeroProposta(numero: 			dc.determinaCollegata.numeroProposta
							, anno: 			dc.determinaCollegata.annoProposta
							, tipoRegistro: 	dc.determinaCollegata.registroProposta.codice
							, data:			dc.determinaCollegata.dataNumeroProposta?.format("dd/MM/yyyy"))

							numeroDetermina(numero: 		dc.determinaCollegata.numeroDetermina
							, anno: 			dc.determinaCollegata.annoDetermina
							, tipoRegistro: 	dc.determinaCollegata.registroDetermina.codice
							, data:			dc.determinaCollegata.dataNumeroDetermina?.format("dd/MM/yyyy"))
						}
					} else if (dc.deliberaCollegata != null) {
						delibera (id: dc.id, operazione: dc.operazione) {
							oggetto (dc.deliberaCollegata.oggetto)
							numeroProposta(numero: 			dc.deliberaCollegata.propostaDelibera.numeroProposta
							, anno: 			dc.deliberaCollegata.propostaDelibera.annoProposta
							, tipoRegistro: 	dc.deliberaCollegata.propostaDelibera.registroProposta.codice
							, data:			dc.deliberaCollegata.propostaDelibera.dataNumeroProposta?.format("dd/MM/yyyy"))

							numeroDelibera(numero: 			dc.deliberaCollegata.numeroDelibera
							, anno: 			dc.deliberaCollegata.annoDelibera
							, tipoRegistro: 	dc.deliberaCollegata.registroDelibera.codice
							, data:			dc.deliberaCollegata.dataNumeroDelibera?.format("dd/MM/yyyy"))
						}
					}
				}
			}
		}.toString()
	}

	public long getNextRevisione (long idDetermina) {
		return ((DeterminaStorico.createCriteria().get {
			projections { max("revisione") }

			eq ("idDetermina", idDetermina)

			//lock (true)
		}?:0) + 1)
	}
}
