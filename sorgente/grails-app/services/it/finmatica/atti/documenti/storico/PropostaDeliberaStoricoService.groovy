package it.finmatica.atti.documenti.storico

import groovy.xml.StreamingMarkupBuilder
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.documenti.DocumentoCollegato
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.PropostaDeliberaSoggetto
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestioneiter.motore.WkfStep

class PropostaDeliberaStoricoService {

	DatiAggiuntiviService datiAggiuntiviService
	IDocumentaleEsterno   gestoreDocumentaleEsterno
	IGestoreFile          gestoreFile

    public PropostaDeliberaStorico storicizza (PropostaDelibera propostaDelibera, WkfStep stepPrecedente, WkfStep stepSuccessivo) {
		PropostaDeliberaStorico storico = new PropostaDeliberaStorico ()
		storico.idPropostaDelibera = propostaDelibera.id
		storico.revisione	= getNextRevisione(storico.idPropostaDelibera)
		storico.iter        = propostaDelibera.iter
		storico.step		= stepPrecedente
		storico.xmlSoggetti = creaXmlSoggetti (propostaDelibera)
		storico.xmlDelibereCollegate  = creaXmlDocumentiCollegati (propostaDelibera)

		storico.iter                     = propostaDelibera.iter
		storico.tipologia                = propostaDelibera.tipologia
		storico.stato                    = propostaDelibera.stato
		storico.statoFirma               = propostaDelibera.statoFirma
		storico.statoOdg 		         = propostaDelibera.statoOdg
		storico.categoria                = propostaDelibera.categoria
		storico.commissione	             = propostaDelibera.commissione
		storico.delega                   = propostaDelibera.delega
		storico.oggettoSeduta            = propostaDelibera.oggettoSeduta
		storico.modelloTesto			 = propostaDelibera.modelloTesto
		storico.modelloTestoAnnullamento = propostaDelibera.modelloTestoAnnullamento
		storico.oggetto                  = propostaDelibera.oggetto
		storico.indirizzo                = propostaDelibera.indirizzo
		storico.dataProposta             = propostaDelibera.dataProposta
		storico.dataNumeroProposta       = propostaDelibera.dataNumeroProposta
		storico.numeroProposta           = propostaDelibera.numeroProposta
		storico.annoProposta             = propostaDelibera.annoProposta
		storico.registroProposta         = propostaDelibera.registroProposta
		storico.controlloFunzionario     = propostaDelibera.controlloFunzionario
		storico.riservato 			     = propostaDelibera.riservato
		storico.fuoriSacco 			     = propostaDelibera.fuoriSacco
		storico.note                     = propostaDelibera.note
		storico.noteTrasmissione         = propostaDelibera.noteTrasmissione
		storico.noteContabili            = propostaDelibera.noteContabili
		storico.noteCommissione          = propostaDelibera.noteCommissione
		storico.classificaCodice         = propostaDelibera.classificaCodice
		storico.classificaDal            = propostaDelibera.classificaDal
		storico.classificaDescrizione    = propostaDelibera.classificaDescrizione
		storico.fascicoloAnno            = propostaDelibera.fascicoloAnno
		storico.fascicoloNumero          = propostaDelibera.fascicoloNumero
		storico.fascicoloOggetto         = propostaDelibera.fascicoloOggetto
		storico.pubblicaRevoca           = propostaDelibera.pubblicaRevoca
		storico.giorniPubblicazione      = propostaDelibera.giorniPubblicazione
		storico.campiProtetti            = propostaDelibera.campiProtetti
		storico.codiciVistiTrattati      = propostaDelibera.codiciVistiTrattati
		storico.idDocumentoEsterno       = propostaDelibera.idDocumentoEsterno
		storico.valido                   = propostaDelibera.valido
		storico.ente                     = propostaDelibera.ente
		storico.dateCreated              = propostaDelibera.dateCreated
		storico.utenteIns                = propostaDelibera.utenteIns
		storico.parereRevisoriConti		 = propostaDelibera.parereRevisoriConti
		storico.motivazione				 = propostaDelibera.motivazione
		storico.priorita				 = propostaDelibera.priorita
        storico.xmlDatiAggiuntivi	     = datiAggiuntiviService.creaXmlDatiAggiuntivi(propostaDelibera.datiAggiuntivi)

		storico.save()

		if (Impostazioni.INTEGRAZIONE_GDM.abilitato) {
			// se presente il gestore documentale esterno, storicizzo i file.
			gestoreDocumentaleEsterno?.storicizzaDocumento(storico);
		}

		if (propostaDelibera.testo != null) {
			storico.testo = propostaDelibera.testo.creaFileAllegatoStorico()

			gestoreFile.addFileStorico (storico, storico.testo, propostaDelibera.testo)
		}

		if (propostaDelibera.stampaUnica != null) {
			storico.stampaUnica	= propostaDelibera.stampaUnica.creaFileAllegatoStorico()

			gestoreFile.addFileStorico (storico, storico.stampaUnica, propostaDelibera.stampaUnica)
		}

		return storico
    }

	/**
	 * Contiene un xml con i soggetti della delibera al momento della storicizzazione:
	 * <soggetti>
	 * 	<soggetto tipo="UNITA/COMPONENTE" cognomeNome="COGNOME_NOME" descrizione="DESCR UNITA" utente="CODICE_UTENTE_AD4" progrUo="PROGRESSIVO_UO" ottica="CODICE_OTTICA" dal="DD/MM/YYYY" />
	 *  ..
	 * </soggetti>
	 */
	private String creaXmlSoggetti (PropostaDelibera propostaDelibera) {
		StreamingMarkupBuilder xml 	= new StreamingMarkupBuilder()
		return xml.bind { builder ->
			soggetti {
				for (PropostaDeliberaSoggetto ds : propostaDelibera.soggetti) {
					if (ds.utenteAd4 != null) {
						As4SoggettoCorrente s = As4SoggettoCorrente.findByUtenteAd4(ds.utenteAd4)
						soggetto(tipo: 			ds.tipoSoggetto.categoria
							   , cognomeNome: 	s.cognome+" "+s.nome
							   , utente: 		ds.utenteAd4.id
							   , descrizione: 	ds.unitaSo4?.descrizione
							   , progrUo: 		ds.unitaSo4?.progr
							   , ottica: 		ds.unitaSo4?.ottica?.codice
							   , dal: 			ds.unitaSo4?.dal?.format("dd/MM/yyyy"))
					} else if (ds.unitaSo4 != null) {
						soggetto(tipo: 		ds.tipoSoggetto.categoria
							, descrizione: 	ds.unitaSo4.descrizione
							, progrUo: 		ds.unitaSo4.progr
						    , ottica: 		ds.unitaSo4.ottica.codice
						    , dal: 			ds.unitaSo4.dal.format("dd/MM/yyyy"))
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

	public long getNextRevisione (long idPropostaDelibera) {
		return ((PropostaDeliberaStorico.createCriteria().get {
			projections {
				max("revisione")
			}

			eq ("idPropostaDelibera", idPropostaDelibera)

			//lock (true)
		}?:0) + 1)
	}
}
