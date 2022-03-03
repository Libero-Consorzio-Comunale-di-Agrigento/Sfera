package it.finmatica.atti.documenti.tipologie

import it.finmatica.gestioneiter.configuratore.dizionari.WkfGruppoStep
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgStep

class ParametroTipologiaService {

	public def aggiornaParametri (def tipologia, def listaParametri) {
		// elimino i precedenti parametri:
		tipologia.parametri.collect { it }.each {
			tipologia.removeFromParametri (it)
			it.delete()
		}

		// ricreo i nuovi:
		for (Map p : listaParametri) {
			ParametroTipologia tp 	= new ParametroTipologia ()
			tp.gruppoStep			= WkfGruppoStep.get(p.idGruppoStep)
			tp.codice				= p.codice
			tp.valore				= p.valore
			tipologia.addToParametri (tp)
		}

		return tipologia;
	}

    public def getListaParametri (String tipologia, long idTipologia, long progressivoCfgIter) {
		// recupero i parametri esistenti
		def listaParametriEsistenti = ParametroTipologia.createCriteria().list {
			eq ("${tipologia}.id", idTipologia)
		}

		// recupero tutti i parametri che devo configurare
		def listaParametriStep = WkfCfgStep.createCriteria().list {		// parametri delle azioni in ingresso
			projections {
				gruppoStep {
					distinct ("id")
					property ("nome")
					property ("descrizione")
				}
				azioniIngresso {
					parametri {
						property ("codice")
						property ("descrizione")
					}
				}
			}

			cfgIter {
				eq ("progressivo", 	progressivoCfgIter)
				eq ("verificato",  	true)
				eq ("stato", 		WkfCfgIter.STATO_IN_USO)
			}
		} + WkfCfgStep.createCriteria().list {		// parametri delle azioni in ingresso
			projections {
				gruppoStep {
					distinct ("id")
					property ("nome")
					property ("descrizione")
				}
				azioniUscita {
					parametri {
						property ("codice")
						property ("descrizione")
					}
				}
			}

			cfgIter {
				eq ("progressivo", 	progressivoCfgIter)
				eq ("verificato",  	true)
				eq ("stato", 		WkfCfgIter.STATO_IN_USO)
			}
		} + WkfCfgStep.createCriteria().list {							// parametri delle azioni sulle condizioni

			projections {
				gruppoStep {
					distinct ("id")
					property ("nome")
					property ("descrizione")
				}
				condizione {
					parametri {
						property ("codice")
						property ("descrizione")
					}
				}
			}

			cfgIter {
				eq ("progressivo", 	progressivoCfgIter)
				eq ("verificato", 	true)
				eq ("stato", 		WkfCfgIter.STATO_IN_USO)
			}
		} + WkfCfgStep.createCriteria().list {							// parametri delle azioni sui pulsanti

			projections {
				gruppoStep {
					distinct ("id")
					property ("nome")
					property ("descrizione")
				}
				cfgPulsanti {
					pulsante {
						azioni {
							parametri {
								property ("codice")
								property ("descrizione")
							}
						}
					}
				}
			}

			cfgIter {
				eq ("progressivo", 	progressivoCfgIter)
				eq ("verificato", 	true)
				eq ("stato", 		WkfCfgIter.STATO_IN_USO)
			}
		}

		log.debug ("listaParametri: $listaParametriStep")

		def r = [];
		for (def row : listaParametriStep) {
            if (r.find { it.idGruppoStep == row[0] && it.codice == row[3] } == null) {
            	r <<  [ idGruppoStep: 			row[0]
            		  , descrizione:  			row[1]
            		  , descrizioneParametro: 	row[4]
            		  , codice: 				row[3]
            		  , valore: listaParametriEsistenti.find { p -> p.codice == row[3] && p.gruppoStep?.id == row[0] }?.valore]
            }
		}

		return r.sort { it.idGruppoStep }
	}
}
