package it.finmatica.atti.documenti.storico

import groovy.xml.StreamingMarkupBuilder
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.DeliberaSoggetto
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestioneiter.motore.WkfStep

class DeliberaStoricoService {

	DatiAggiuntiviService datiAggiuntiviService
	IDocumentaleEsterno   gestoreDocumentaleEsterno
	IGestoreFile          gestoreFile

    public DeliberaStorico storicizza (Delibera delibera, WkfStep stepPrecedente, WkfStep stepSuccessivo) {
		DeliberaStorico storico = new DeliberaStorico ()
		storico.idDelibera  = delibera.id
		storico.revisione	= getNextRevisione(storico.idDelibera)
		storico.iter        = delibera.iter
		storico.step		= stepPrecedente
		storico.xmlSoggetti = creaXmlSoggetti (delibera)

		storico.iter                    = delibera.iter
		storico.proposta                = delibera.proposta
		storico.oggettoSeduta           = delibera.oggettoSeduta
		storico.stato                   = delibera.stato
		storico.statoFirma              = delibera.statoFirma
		storico.statoConservazione      = delibera.statoConservazione
		storico.modelloTesto            = delibera.modelloTesto
		storico.oggetto                 = delibera.oggetto
		storico.dataNumeroDelibera      = delibera.dataNumeroDelibera
		storico.numeroDelibera          = delibera.numeroDelibera
		storico.annoDelibera            = delibera.annoDelibera
		storico.registroDelibera        = delibera.registroDelibera
		storico.dataNumeroProtocollo    = delibera.dataNumeroProtocollo
		storico.numeroProtocollo        = delibera.numeroProtocollo
		storico.annoProtocollo          = delibera.annoProtocollo
		storico.registroProtocollo      = delibera.registroProtocollo
		storico.eseguibilitaImmediata   = delibera.eseguibilitaImmediata
		storico.motivazioniEseguibilita = delibera.motivazioniEseguibilita
		storico.dataEsecutivita         = delibera.dataEsecutivita
		storico.dataAdozione		    = delibera.dataAdozione
		storico.idDocumentoEsterno      = delibera.idDocumentoEsterno
		storico.pubblicaRevoca          = delibera.pubblicaRevoca
		storico.giorniPubblicazione     = delibera.giorniPubblicazione
		storico.dataPubblicazione       = delibera.dataPubblicazione
		storico.dataFinePubblicazione   = delibera.dataFinePubblicazione
		storico.dataPubblicazione2      = delibera.dataPubblicazione2
		storico.dataFinePubblicazione2  = delibera.dataFinePubblicazione2
		storico.valido                  = delibera.valido
		storico.ente                    = delibera.ente
		storico.dateCreated             = delibera.dateCreated
		storico.utenteIns               = delibera.utenteIns
		storico.note                    = delibera.note
		storico.noteTrasmissione        = delibera.noteTrasmissione
        storico.xmlDatiAggiuntivi	    = datiAggiuntiviService.creaXmlDatiAggiuntivi(delibera.datiAggiuntivi)

		storico.save()

		if (Impostazioni.INTEGRAZIONE_GDM.abilitato) {
			// se presente il gestore documentale esterno, storicizzo i file.
			gestoreDocumentaleEsterno?.storicizzaDocumento(storico);
		}

		if (delibera.testo != null) {
			storico.testo = delibera.testo.creaFileAllegatoStorico()

			gestoreFile.addFileStorico (storico, storico.testo, delibera.testo)
		}

		if (delibera.stampaUnica != null) {
			storico.stampaUnica	= delibera.stampaUnica.creaFileAllegatoStorico()

			gestoreFile.addFileStorico (storico, storico.stampaUnica, delibera.stampaUnica)
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
	private String creaXmlSoggetti (Delibera delibera) {
		StreamingMarkupBuilder xml 	= new StreamingMarkupBuilder()
		return xml.bind { builder ->
			soggetti {
				for (DeliberaSoggetto ds : delibera.soggetti) {
					if (ds.utenteAd4 != null) {
						As4SoggettoCorrente s = As4SoggettoCorrente.findByUtenteAd4(ds.utenteAd4)
						soggetto(tipo: 			ds.tipoSoggetto.categoria
							   , cognomeNome: 	s.cognome+" "+s.nome
							   , utente: 		ds.utenteAd4.id
							   , descrizione: 	ds.unitaSo4?.descrizione
							   , progrUo: 		ds.unitaSo4?.progr
							   , ottica: 		ds.unitaSo4?.ottica?.codice
							   , dal: 			ds.unitaSo4?.dal?.format("dd/MM/yyyy"))
					} else {
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

	public long getNextRevisione (long idDelibera) {
		return ((DeliberaStorico.createCriteria().get {
			projections {
				max("revisione")
			}

			eq ("idDelibera", idDelibera)

			//lock (true)
		}?:0) + 1)
	}
}
