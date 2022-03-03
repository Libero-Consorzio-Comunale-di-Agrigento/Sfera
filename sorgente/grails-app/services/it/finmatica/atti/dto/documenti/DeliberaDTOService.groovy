package it.finmatica.atti.dto.documenti

import atti.documenti.DeliberaViewModel
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.storico.DeliberaStorico
import org.zkoss.util.resource.Labels

class DeliberaDTOService {

	AttiGestioneTesti 		gestioneTesti;
	VistoParereDTOService 	vistoParereDTOService

	public void salva (DeliberaViewModel deliberaViewModel) {
		// se devo rilasciare il lock sul testo, lo rilascio.
		Delibera d = deliberaViewModel.getDocumentoIterabile(false);
		gestioneTesti.uploadTesto(d);
		deliberaViewModel.aggiornaDocumentoIterabile(d)
		d.save()
		deliberaViewModel.aggiornaMaschera(d);
	}

    public def caricaStorico (DeliberaDTO deliberaDto) {
		if (!(deliberaDto.id > 0)) {
			return null;
		}

		String descrizione = Labels.getLabel("dettaglio-delibera-titolo");

		def storico = DeliberaStorico.createCriteria().list {
			projections {
				groupProperty ("dateCreated")				// 0
				utenteIns {
					groupProperty ("nominativoSoggetto") 	// 1
				}

				step {
					cfgStep {
						attore {
							groupProperty ("nome")			// 2
						}
						groupProperty("titolo")				// 3
					}
					attori {
						// faccio la "count" degli attori in questo modo
						// ottengo una riga sola per attori "multipli"
						// e nessuna riga se il nodo non ha attori (e quindi non va mostrato nello storico)
						// prima c'era: groupProperty("id") ma così mostrava tante righe per ogni attore nello step
						// (ad es. nel caso dell'attore che trova le unità figlie)
						// se invece tolgo del tutto non va bene perché si vedono anche step da cui il documento non è passato
						// Con la nuova versione del configuratoreiter infatti si possono mettere le condizioni di ingresso al nodo
						// e quello che si ottiene su db è che il passaggio dal nodo viene comunque registrato ma non ne vengono calcolati gli attori)
						count("id")                       		  // 4
					}
				}

				groupProperty ("testo.id")					// 5
				groupProperty ("id")						// 6
//				proposta {
//					tipologia {
//						caratteristicaTipologia {
//							property ("titolo")				// 7
//						}
//					}
//				}
				groupProperty ("note")                      // 7
			}

			eq ("idDelibera", deliberaDto.id)

			order ("dateCreated", "asc")
		}.collect { row -> [id: 			row[6], 
							data: 			row[0], 
							utente: 		row[1], 
							carico: 		row[2], 
							idFileAllegato:	row[5], 
							titolo:			row[3],
							note: 			row[7],
							descrizione: 	descrizione, 
							tipoOggetto:	Delibera.TIPO_OGGETTO] }

		def storicoVisto = vistoParereDTOService.caricaStorico (null, null, null, deliberaDto)

		if (storicoVisto != null) {
			storico.addAll (storicoVisto)
		}
		return  storico.sort(true, { it.data });
	}
}
