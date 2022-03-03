package it.finmatica.atti.documenti.storico

import groovy.xml.StreamingMarkupBuilder
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.dto.odg.SedutaStampaDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestionedocumenti.soggetti.DocumentoSoggetto
import it.finmatica.gestioneiter.motore.WkfStep

class SedutaStampaStoricoService {

	IDocumentaleEsterno gestoreDocumentaleEsterno
	IGestoreFile gestoreFile

	SedutaStampaStorico storicizza (SedutaStampa sedutaStampa, WkfStep stepPrecedente, WkfStep stepSuccessivo) {
		SedutaStampaStorico storico = new SedutaStampaStorico ()
		storico.idSedutaStampa = sedutaStampa.id
		storico.revisione	= getNextRevisione(storico.idSedutaStampa)
		storico.iter        = sedutaStampa.iter
		storico.step		= stepPrecedente
		storico.xmlSoggetti = creaXmlSoggetti (sedutaStampa)

		storico.iter                   = sedutaStampa.iter
		storico.commissioneStampa      = sedutaStampa.commissioneStampa
		storico.stato                  = sedutaStampa.stato
		storico.statoFirma             = sedutaStampa.statoFirma
		storico.statoConservazione     = sedutaStampa.statoConservazione
		storico.note                   = sedutaStampa.note
		storico.modelloTesto           = sedutaStampa.modelloTesto
		storico.dataNumeroProtocollo   = sedutaStampa.dataNumeroProtocollo
		storico.numeroProtocollo       = sedutaStampa.numeroProtocollo
		storico.annoProtocollo         = sedutaStampa.annoProtocollo
		storico.registroProtocollo     = sedutaStampa.registroProtocollo
		storico.riservato              = sedutaStampa.riservato
		storico.classificaCodice       = sedutaStampa.classificaCodice
		storico.classificaDal          = sedutaStampa.classificaDal
		storico.classificaDescrizione  = sedutaStampa.classificaDescrizione
		storico.fascicoloAnno          = sedutaStampa.fascicoloAnno
		storico.fascicoloNumero        = sedutaStampa.fascicoloNumero
		storico.fascicoloOggetto       = sedutaStampa.fascicoloOggetto
		storico.idDocumentoEsterno     = sedutaStampa.idDocumentoEsterno
		storico.idDocumentoLettera     = sedutaStampa.idDocumentoLettera
		storico.valido                 = sedutaStampa.valido
		storico.ente                   = sedutaStampa.ente
		storico.dateCreated            = sedutaStampa.dateCreated
		storico.utenteIns              = sedutaStampa.utenteIns

        // Dati dell'albo
        storico.idDocumentoAlbo = sedutaStampa.idDocumentoAlbo
        storico.numeroAlbo      = sedutaStampa.numeroAlbo
        storico.annoAlbo        = sedutaStampa.annoAlbo

		// dati di pubblicazione
		storico.pubblicaRevoca 			= sedutaStampa.pubblicaRevoca
		storico.daPubblicare			= sedutaStampa.daPubblicare
		storico.giorniPubblicazione 	= sedutaStampa.giorniPubblicazione
		storico.dataPubblicazione 		= sedutaStampa.dataPubblicazione
		storico.dataFinePubblicazione 	= sedutaStampa.dataFinePubblicazione
		storico.dataPubblicazione2 		= sedutaStampa.dataPubblicazione2
		storico.dataFinePubblicazione2 	= sedutaStampa.dataFinePubblicazione2
		storico.dataMinimaPubblicazione = sedutaStampa.dataMinimaPubblicazione

		storico.save()

        gestoreDocumentaleEsterno.storicizzaDocumento(storico)

		if (sedutaStampa.testo != null) {
			storico.testo = sedutaStampa.testo.creaFileAllegatoStorico()

			gestoreFile.addFileStorico (storico, storico.testo, sedutaStampa.testo)
		}

		return storico
	}

	/**
	 * Contiene un xml con i soggetti della stampa al momento della storicizzazione:
	 * <soggetti>
	 * 	<soggetto tipo="UNITA/COMPONENTE" cognomeNome="COGNOME_NOME" descrizione="DESCR UNITA" utente="CODICE_UTENTE_AD4" progrUo="PROGRESSIVO_UO" ottica="CODICE_OTTICA" dal="DD/MM/YYYY" />
	 *  ..
	 * </soggetti>
	 */
	private String creaXmlSoggetti (SedutaStampa sedutaStampa) {
		StreamingMarkupBuilder xml = new StreamingMarkupBuilder()
		return xml.bind { builder ->
			soggetti {
				for (DocumentoSoggetto ds : sedutaStampa.soggetti) {
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

	long getNextRevisione (long idSedutaStampa) {
		return ((SedutaStampaStorico.createCriteria().get {
			projections { max("revisione") }

			eq ("idSedutaStampa", idSedutaStampa)

			//lock (true)
		}?:0) + 1)
	}

    public def caricaStorico (SedutaStampaDTO sedutaStampaDto) {
        if (!(sedutaStampaDto.id > 0)) {
            return []
        }

        def storico = SedutaStampaStorico.createCriteria().list {
            projections {
                groupProperty("dateCreated")                     // 0
                utenteIns {
                    groupProperty("nominativoSoggetto")          // 1
                }
                step {
                    cfgStep {
                        attore {
                            groupProperty("nome")                 // 2
                        }
                        groupProperty("titolo")                   // 3
                    }
                    //
                    attori {
                        // faccio la "count" degli attori in questo modo
                        // ottengo una riga sola per attori "multipli"
                        // e nessuna riga se il nodo non ha attori (e quindi non va mostrato nello storico)
                        // prima c'era: groupProperty("id") ma così mostrava tante righe per ogni attore nello step
                        // (ad es. nel caso dell'attore che trova le unità figlie)
                        // se invece tolgo del tutto non va bene perché si vedono anche step da cui il documento non è passato
                        // Con la nuova versione del configuratoreiter infatti si possono mettere le condizioni di ingresso al nodo
                        // e quello che si ottiene su db è che il passaggio dal nodo viene comunque registrato ma non ne vengono calcolati gli attori)
                        count("id")                              // 4
                    }
                }
                groupProperty("testo.id")                        // 5
                groupProperty("id")                              // 6
                commissioneStampa {
                    caratteristicaTipologia {
                        groupProperty("titolo")                  // 7
                    }
                }
            }

            eq("idSedutaStampa", sedutaStampaDto.id)

            order("dateCreated", "asc")
        }.collect { row ->
            [id              : row[6]
             , data          : row[0]
             , utente        : row[1]
             , carico        : row[2]
             , idFileAllegato: row[5]
             , titolo        : row[3]
             , descrizione   : row[7]
             , note          : ""
             , noteContabili : ""
             , tipoOggetto   : SedutaStampa.TIPO_OGGETTO]
        }

        return storico.sort(true, { it.data })
    }

}
