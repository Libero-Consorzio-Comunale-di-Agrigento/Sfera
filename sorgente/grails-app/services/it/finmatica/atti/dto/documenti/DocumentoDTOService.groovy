package it.finmatica.atti.dto.documenti

import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.storico.DeliberaStorico
import it.finmatica.atti.documenti.storico.DeterminaStorico
import it.finmatica.atti.documenti.storico.PropostaDeliberaStorico
import it.finmatica.atti.documenti.storico.VistoParereStorico
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.dto.DTO
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAttoreService
import it.finmatica.gestioneiter.motore.WkfIterService
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

class DocumentoDTOService {

    WkfIterService wkfIterService
    WkfAttoreService wkfAttoreService
    DatiAggiuntiviService datiAggiuntiviService

    void salvaDatiAggiuntivi (def documento, DTO<?> documentoDto) {
        datiAggiuntiviService.salvaDatiAggiuntivi(documento, documentoDto)
    }

    def getNoteTrasmissionePrecedenti (def documentoDto) {

        def domainObject = (documentoDto instanceof DTO) ? documentoDto.domainObject : documentoDto
        domainObject = GrailsHibernateUtil.unwrapIfProxy(domainObject)

        def storicoClass = null;
        if (domainObject instanceof Delibera) {
            storicoClass = DeliberaStorico
        } else if (domainObject instanceof Determina) {
            storicoClass = DeterminaStorico
        } else if (domainObject instanceof PropostaDelibera) {
            storicoClass = PropostaDeliberaStorico
        } else if (domainObject instanceof VistoParere) {
            storicoClass = VistoParereStorico
        }

        if (storicoClass == null) {
            return [noteTrasmissionePrecedenti        : []
                    , attorePrecedente                : false
                    , mostraNoteTrasmissionePrecedenti: false]
        }

        // ottengo le note di trasmissione precedenti (se ci sono):
        long idStorico = storicoClass.createCriteria().get {
            projections {
                property("id")
            }
            eq("id${domainObject.class.simpleName}", domainObject.id)
            step {
                attori {
                    or {
                        isNotNull("utenteAd4")
                        isNotNull("ruoloAd4")
                        isNotNull("unitaSo4")
                    }
                }
            }

            order("revisione", "desc")
            maxResults(1)
        }

        if (idStorico == null) {
            return [noteTrasmissionePrecedenti        : []
                    , attorePrecedente                : false
                    , mostraNoteTrasmissionePrecedenti: false]
        }

        // ottengo le note di trasmissione precedenti (se ci sono):
        def result = storicoClass.createCriteria().list {
            projections {
                property("id")
                property("noteTrasmissione")
                property("dateCreated")
                utenteIns {
                    property("nominativoSoggetto")
                }
                step {
                    cfgStep {
                        property("titolo")
                    }
                }
            }
            eq("id${domainObject.class.simpleName}", domainObject.id)
            step {
                attori {
                    or {
                        isNotNull("utenteAd4")
                        isNotNull("ruoloAd4")
                        isNotNull("unitaSo4")
                    }
                }
            }
            isNotNull("noteTrasmissione")

            order("revisione", "desc")
            if (!Impostazioni.NOTE_TRASMISSIONE_PUBBLICHE.abilitato) {
                maxResults(1)
            }
        }.collect { row ->
            [id              : row[0],
             data            : row[2],
             utente          : row[3],
             titolo          : row[4],
             noteTrasmissione: row[1]]
        }

        // se invece ho trovato le note di trasmissione, valorizzo la mappa di ritorno:
        def ret = [mostraNoteTrasmissionePrecedenti: Impostazioni.NOTE_TRASMISSIONE_PUBBLICHE.abilitato
                   , attorePrecedente              : false
                   , noteTrasmissionePrecedenti    : result]

        // ora cerco i vari attori e determino se devono vedere o meno le note di trasmissione:
        if (!result.isEmpty() && idStorico == result?.first()?.id) {
            def attori = storicoClass.createCriteria().list {
                projections {
                    step {
                        // attori dello step precedente
                        attori {
                            property("utenteAd4.id")              // 0
                            property("ruoloAd4.ruolo")            // 1
                            property("unitaSo4.progr")            // 2
                            property("unitaSo4.ottica.codice")    // 3
                        }
                    }

                    iter {
                        // attori dello step corrente legati all'iter.
                        stepCorrente {
                            attori {
                                property("utenteAd4.id")            // 4
                                property("ruoloAd4.ruolo")          // 5
                                property("unitaSo4.progr")          // 6
                                property("unitaSo4.ottica.codice")  // 7
                            }
                        }
                    }
                }
                eq("id", idStorico)

                order("revisione", "desc")
            }

            for (def row : attori) {
                if (wkfAttoreService.utenteCorrenteCorrispondeAttore(row[0], row[1], row[2], row[3]) ||
                        wkfAttoreService.utenteCorrenteCorrispondeAttore(row[4], row[5], row[6], row[7])) {
                    ret.attorePrecedente = true
                    ret.mostraNoteTrasmissionePrecedenti = true
                    break
                }
            }
        }

        return ret
    }
}

