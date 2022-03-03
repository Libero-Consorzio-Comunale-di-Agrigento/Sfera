package it.finmatica.atti.dto.documenti

import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.storico.PropostaDeliberaStorico
import it.finmatica.atti.documenti.tipologie.GestioneTestiModelloCompetenza
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import org.hibernate.FetchMode

class PropostaDeliberaDTOService {

    WkfIterService        wkfIterService
    VistoParereService    vistoParereService
    VistoParereDTOService vistoParereDTOService
    NotificheService      notificheService

    PropostaDeliberaDTO salva (PropostaDeliberaDTO propostaDto) {
        PropostaDelibera proposta = PropostaDelibera.get(propostaDto.id) ?: new PropostaDelibera()
        if (proposta.version != propostaDto.version) {
            throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
        }
        proposta.numeroProposta = propostaDto.numeroProposta
        proposta.annoProposta = propostaDto.annoProposta
        proposta.tipologia = propostaDto.tipologia?.domainObject
        proposta.oggetto = propostaDto.oggetto
        proposta.statoOdg = propostaDto.statoOdg
        proposta.fuoriSacco = propostaDto.fuoriSacco
        proposta.eseguibilitaImmediata = propostaDto.eseguibilitaImmediata
        proposta.motivazioniEseguibilita = propostaDto.motivazioniEseguibilita
        proposta.commissione = propostaDto.commissione?.domainObject
        proposta.oggettoSeduta = propostaDto.oggettoSeduta?.domainObject
        proposta.indirizzo = propostaDto.indirizzo?.domainObject
        proposta.parereRevisoriConti = propostaDto.parereRevisoriConti
        proposta.oggettoRicorrente = propostaDto.oggettoRicorrente?.domainObject
        proposta.motivazione = propostaDto.motivazione
        proposta.priorita = propostaDto.priorita
		proposta.dataOrdinamento		= propostaDto.dataOrdinamento
        proposta.dataMinimaPubblicazione = propostaDto.dataMinimaPubblicazione

        proposta.save(failOnError: true)
        return proposta.toDTO()
    }

    void elimina (PropostaDeliberaDTO proposta) {
        PropostaDelibera.get(proposta.id).delete()
    }

    void sbloccaFlusso (PropostaDeliberaDTO propostaDTO) {
        PropostaDelibera proposta = propostaDTO.domainObject
        wkfIterService.sbloccaDocumento(proposta)
        proposta.save(failOnError: true)
    }

    List<GestioneTestiModelloDTO> getListaModelliTestoAbilitati (long idTipologiaDelibera, def utente) {
        List<Long> listaIdModelliTesto = TipoDelibera.createCriteria().list() {
            projections {
                modelliTesto {
                    property("id")
                }
            }
            eq("id", idTipologiaDelibera)
            modelliTesto {
                like("tipoModello.codice", "PROPOSTA_DELIBERA%")
            }
        }

        if (listaIdModelliTesto.size() <= 0) {
            return null
        }

        return GestioneTestiModelloCompetenza.createCriteria().list {
            projections {
                gestioneTestiModello {
                    property("id")
                    property("nome")
                    property("descrizione")
                }
            }
            gestioneTestiModello {
                'in'("id", listaIdModelliTesto)
                eq("valido", true)
            }
            AttiGestoreCompetenze.controllaCompetenze(delegate)(utente)
        }.collect { row -> new GestioneTestiModelloDTO(id: row[0], nome: row[1], descrizione: row[2]) }
    }

    def caricaStorico (PropostaDeliberaDTO propostaDelibera) {
        if (!(propostaDelibera.id > 0)) {
            return null;
        }

        def storico = PropostaDeliberaStorico.createCriteria().list {
            projections {
                groupProperty("dateCreated")                                 // 0
                utenteIns {                                                   //
                    groupProperty("nominativoSoggetto")                      // 1
                }                                                             //
                //
                step {                                                        //
                    cfgStep {                                                 //
                        attore {                                              //
                            groupProperty("nome")                            // 2
                        }                                                     //
                        groupProperty("titolo")                               // 3
                    }                                                         //
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
                }                                                             //
                //
                groupProperty("testo.id")                                    // 5
                groupProperty("id")                                          // 6
                tipologia {                                                   //
                    caratteristicaTipologia {                                 //
                        groupProperty("titolo")                              // 7
                    }
                }
                groupProperty("note")                                          // 8
                groupProperty("noteContabili")                                  // 9
            }

            eq("idPropostaDelibera", propostaDelibera.id)

            order("dateCreated", "asc")
        }.collect { row ->
            [id            : row[6],
             data          : row[0],
             utente        : row[1],
             carico        : row[2],
             idFileAllegato: row[5],
             titolo        : row[3],
             descrizione   : row[7],
             note          : row[8],
             noteContabili : row[9],
             tipoOggetto   : PropostaDelibera.TIPO_OGGETTO]
        }

        def storicoVisto = vistoParereDTOService.caricaStorico(null, null, propostaDelibera, null)

        if (storicoVisto != null) {
            storico.addAll(storicoVisto)
        }

        // controllo per l'inserimento in seduta e suo esito
        PropostaDelibera p = propostaDelibera.domainObject;
        List<OggettoSeduta> oggettiSeduta = OggettoSeduta.findAllByPropostaDelibera(p)
        for (OggettoSeduta oggettoSeduta : oggettiSeduta) {
            storico.add(
                    [id: propostaDelibera.id, data: oggettoSeduta.dateCreated, utente: oggettoSeduta.utenteIns.nominativoSoggetto, carico: null, idFileAllegato: null, titolo: "Inserito in seduta", descrizione: p.tipologia.caratteristicaTipologia.titolo, tipoOggetto: PropostaDelibera.TIPO_OGGETTO]);

            if (oggettoSeduta.esito != null) {
                storico.add(
                        [id: propostaDelibera.id, data: oggettoSeduta.dataOraDiscussione ?: oggettoSeduta.seduta.dataOraInizioSeduta ?: oggettoSeduta.seduta.dataOraSeduta, utente: null, carico: null, idFileAllegato: null, titolo: "Discusso con esito: ${oggettoSeduta.esito.titolo}", descrizione: p.tipologia.caratteristicaTipologia.titolo, tipoOggetto: PropostaDelibera.TIPO_OGGETTO, note: oggettoSeduta.note]);
            }
        }

        return storico.sort(true, { it.data })
    }

    def cambiaTipologia (PropostaDeliberaDTO propostaDeliberaDTO) {
        PropostaDelibera d = PropostaDelibera.get(propostaDeliberaDTO.id)

        d.tipologia = propostaDeliberaDTO.tipologia.getDomainObject()
        d.controlloFunzionario = propostaDeliberaDTO.controlloFunzionario
        d.giorniPubblicazione = propostaDeliberaDTO.giorniPubblicazione
        d.pubblicaRevoca = propostaDeliberaDTO.pubblicaRevoca

        //chiudo l'iter
        wkfIterService.terminaIter(d.iter)
        WkfCfgIter cfgIter = WkfCfgIter.getIterIstanziabile(d.tipologia.progressivoCfgIter).get()
        wkfIterService.istanziaIter(cfgIter, d)

        //rendo i visti precedenti non gestibili
        d.stato = StatoDocumento.PROPOSTA

        // resetto i visti
        d.codiciVistiTrattati = ""

        // invalido i visti presenti, e li ricreo nuovi.
        for (VistoParere visto : d.visti) {
            // salto quelli già invalidi:
            if (!visto.valido) {
                continue
            };

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

    void addSoggettoFirmatario (PropostaDeliberaDTO propostaDeliberaDTO, it.finmatica.atti.zk.SoggettoDocumento firmatario, int sequenza) {
        PropostaDeliberaSoggetto soggetto = new PropostaDeliberaSoggetto()
        soggetto.propostaDelibera = propostaDeliberaDTO.domainObject
        soggetto.sequenza = sequenza
        soggetto.unitaSo4 = firmatario.unita.domainObject
        soggetto.utenteAd4 = firmatario.utente.domainObject
        soggetto.tipoSoggetto = firmatario.tipoSoggetto.domainObject
        soggetto.attivo = true
        soggetto.save()
    }

    void rimuoviFirmatario (def soggettiFirmatari, PropostaDeliberaSoggettoDTO soggettoDTO) {
        soggettoDTO.domainObject.delete()
        // FIXME: questa assegnazione non ha effetto, forse si può fare un refactor di questi soggetti usando la "nuova" struttura di SoggettoDocumento
//        soggettiFirmatari = caricaSoggettiFirmatari(soggettoDTO.propostaDelibera)
    }

    private void rinumeraSoggettiFirmatari (def dto) {
        def firmatari = PropostaDeliberaSoggetto.createCriteria().list() {
            tipoSoggetto {
                eq("categoria", TipoSoggetto.CATEGORIA_COMPONENTE)
                eq("codice", TipoSoggetto.FIRMATARIO)
            }
            propostaDelibera {
                eq("id", dto.id)
            }
            eq("attivo", true)

            fetchMode("utenteAd4", FetchMode.JOIN)
            fetchMode("propostaDelibera", FetchMode.JOIN)
            fetchMode("unitaSo4", FetchMode.JOIN)
            fetchMode("tipoSoggetto", FetchMode.JOIN)
            order("sequenza", "asc")
        }.eachWithIndex { item, index ->
            item.sequenza = index
            item.save(failOnError: true, flush: true)
        }
    }

    def caricaSoggettiFirmatari (def dto) {
        return PropostaDeliberaSoggetto.createCriteria().list() {
            tipoSoggetto {
                eq("categoria", TipoSoggetto.CATEGORIA_COMPONENTE)
                eq("codice", TipoSoggetto.FIRMATARIO)
            }
            propostaDelibera {
                eq("id", dto.id)
            }
            eq("attivo", true)

            fetchMode("utenteAd4", FetchMode.JOIN)
            order("sequenza", "asc")
        }.toDTO()
    }
}

