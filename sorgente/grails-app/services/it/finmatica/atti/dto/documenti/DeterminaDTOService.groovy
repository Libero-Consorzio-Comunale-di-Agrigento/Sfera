package it.finmatica.atti.dto.documenti

import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.NotificheService
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.VistoParereService
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.storico.DeterminaStorico
import it.finmatica.atti.documenti.tipologie.GestioneTestiModelloCompetenza
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAttoreService
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO

class DeterminaDTOService {

    WkfIterService wkfIterService
    VistoParereService vistoParereService
    VistoParereDTOService vistoParereDTOService
    NotificheService      notificheService

	public DeterminaDTO salva (DeterminaDTO determinaDto) {
		Determina determina = Determina.get(determinaDto.id)?:new Determina()
		if (determina.version != determinaDto.version) {
			throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
		}
		determina.statoOdg		= determinaDto.statoOdg
		determina.oggettoSeduta	= determinaDto.oggettoSeduta?.domainObject
		determina.oggettoRicorrente = determinaDto.oggettoRicorrente?.domainObject

		determina.save(failOnError: true)
		return determina.toDTO()
	}

    public List<GestioneTestiModelloDTO> getListaModelliTestoAbilitati (long idTipologiaDetermina, def utente) {
        List<Long> listaIdModelliTesto = TipoDetermina.createCriteria().list() {
            projections {
                modelliTesto {
                    property("id")
                }
            }
            eq("id", idTipologiaDetermina)
        }

        if (listaIdModelliTesto.size() <= 0) {
            return null
        }

		return GestioneTestiModelloCompetenza.createCriteria().list {
			projections {
				gestioneTestiModello {
					property ("id")
					property ("nome")
					property ("descrizione")
				}
			}
			gestioneTestiModello {
				'in'("id", listaIdModelliTesto)
				eq ("valido", true)
			}
			AttiGestoreCompetenze.controllaCompetenze(delegate)(utente)
		}.collect { row -> new GestioneTestiModelloDTO(id:row[0], nome:row[1], descrizione:row[2]) }
	}

    public def caricaStorico (DeterminaDTO determinaDto) {
        if (determinaDto.id > 0) {
            def storico = DeterminaStorico.createCriteria().list {
                projections {
                    groupProperty("dateCreated")                     // 0
                    utenteIns {                                       //
                        groupProperty("nominativoSoggetto")          // 1
                    }                                                 //
                    //
                    step {                                            //
                        cfgStep {                                     //
                            attore {                                  //
                                groupProperty("nome")                // 2
                            }                                         //
                            groupProperty("titolo")                   // 3
                        }                                             //
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
                        }                                             //
                    }                                                 //
                    //
                    groupProperty("testo.id")                        // 5
                    groupProperty("id")                              // 6
                    tipologia {                                       //
                        caratteristicaTipologia {                     //
                            groupProperty("titolo")                  // 7
                        }
                    }
                    groupProperty("note")                            // 8
                    groupProperty("noteContabili")                   // 9
                }

                eq("idDetermina", determinaDto.id)

                order("dateCreated", "asc")
            }.collect { row ->
                [id              : row[6]
                 , data          : row[0]
                 , utente        : row[1]
                 , carico        : row[2]
                 , idFileAllegato: row[5]
                 , titolo        : row[3]
                 , descrizione   : row[7]
                 , note          : row[8]
                 , noteContabili : row[9]
                 , tipoOggetto   : Determina.TIPO_OGGETTO]
            }

            def storicoVisto = vistoParereDTOService.caricaStorico(null, determinaDto, null, null)
            if (storicoVisto != null) {
                storico.addAll(storicoVisto)
            }

            // FIXME: per quando prima o poi una determina andrà all'odg, questo può servire per lo storico:
//			List<OggettoSeduta> oggettiSeduta = OggettoSeduta.findAllByPropostaDelibera(propostaDelibera.domainObject)
//			for (OggettoSeduta oggettoSeduta : oggettiSeduta) {
//				storico.add ([id:propostaDelibera.id, data:oggettoSeduta.seduta.dataSeduta, utente: oggettoSeduta.utenteIns.nominativoSoggetto, carico: null, idFileAllegato:null, titolo:"Inserito in seduta", tipoOggetto:PropostaDelibera.TIPO_OGGETTO]);
//
//				if (oggettoSeduta.esito != null) {
//					storico.add ([id:propostaDelibera.id, data:oggettoSeduta.seduta.dataSeduta, utente: null, carico: null, idFileAllegato:null, titolo:"Discusso con esito: ${oggettoSeduta.esito.titolo}", tipoOggetto:PropostaDelibera.TIPO_OGGETTO]);
//				}
//			}

            return storico.sort(true, { it.data })

        }

        return null
    }

    def cambiaTipologia (DeterminaDTO determinaDto) {
        Determina d = Determina.get(determinaDto.id)

        d.tipologia = determinaDto.tipologia.getDomainObject()
        d.controlloFunzionario = determinaDto.controlloFunzionario
        d.giorniPubblicazione = determinaDto.giorniPubblicazione
        d.pubblicaRevoca = determinaDto.pubblicaRevoca
        d.diventaEsecutiva = determinaDto.diventaEsecutiva

        //chiudo l'iter
        wkfIterService.terminaIter(d.iter)
        WkfCfgIter cfgIter = WkfCfgIter.getIterIstanziabile(d.tipologia.progressivoCfgIter).get()
        wkfIterService.istanziaIter(cfgIter, d)

        //rendo i visti precedenti non gestibili
        d.stato = StatoDocumento.PROPOSTA

        // resetto i visti
        d.codiciVistiTrattati = ""

        // resetto il CIG
        d.codiceGara = ""

        // invalido i visti presenti, e li ricreo nuovi.
        for (VistoParere visto : d.visti) {
            // salto quelli già invalidi:
            if (!visto.valido) {
                continue
            }

            visto.valido = false;
            visto.save(failOnError: true)

            if (visto.iter != null && visto.iter.dataFine == null) {
                wkfIterService.terminaIter(visto.iter)
            }

            // elimino tutte le notifiche di cambio step
            notificheService.eliminaNotifiche(visto, TipoNotifica.ASSEGNAZIONE)
            // elimino tutte le "altre" notifiche
            notificheService.eliminaNotifiche(visto)
        }
        // aggiungo i visti automatici presenti nella nuova tipologia
        d = vistoParereService.creaVistiAutomatici(d)
        d = d.save()
        return d.toDTO()
    }
}
