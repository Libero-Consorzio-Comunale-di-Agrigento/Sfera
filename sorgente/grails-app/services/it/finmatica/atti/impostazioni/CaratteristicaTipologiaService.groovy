package it.finmatica.atti.impostazioni

import it.finmatica.atti.documenti.*
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.zk.SoggettoDocumento
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.soggetti.DocumentoSoggetto
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.FetchMode

class CaratteristicaTipologiaService {

    GrailsApplication grailsApplication

    /**
     * Salva i soggetti modificati in interfaccia sul documento
     *
     * @param documento
     * @param soggetti
     * @param aggiungiNuovo indica se va sostituito il soggetto oppure se ne va aggiunto uno nuovo.
     */
    void salvaSoggettiModificati (IDocumento documento, Map<String, SoggettoDocumento> soggetti, boolean aggiungiNuovo = false) {
        for (def soggetto : soggetti) {
            if (soggetto.value.modificato && soggetto.value.lista.size() == 0) {
                int sequenza = 0
                if (aggiungiNuovo) {
                    ISoggettoDocumento soggettoDocumento = documento.getSoggetto(soggetto.key)
                    if (soggettoDocumento != null) {
                        soggettoDocumento.attivo = false
                        soggettoDocumento.save()
                        sequenza = soggettoDocumento.sequenza + 1
                    }
                }

                documento.setSoggetto(soggetto.key, soggetto.value.utente?.domainObject, soggetto.value.unita?.domainObject, sequenza)
            }

            for (SoggettoDocumento s : soggetto.value.lista) {
                ISoggettoDocumento sogg = null
                if (s.id > 0) {
                    sogg = documento.soggetti.find { it.id == s.id }
                }

                if (sogg == null) {
                    sogg = creaSoggettoDocumento(documento)
                    documento.addToSoggetti(sogg)
                }

                sogg.tipoSoggetto = s.tipoSoggetto.domainObject
                sogg.sequenza = s.sequenza
                sogg.attivo = s.attivo
                sogg.utenteAd4 = s.utente?.domainObject
                sogg.unitaSo4 = s.unita?.domainObject
                sogg.save()

                // assegno l'id del soggetto appena salvato così da non incorrere nel rischio di salvare due volte gli stessi soggetti con chiamate successive a questa funzione.
                s.id = sogg.id
            }
        }

        documento.save()
    }

    private ISoggettoDocumento creaSoggettoDocumento (IDocumento documento) {
        if (documento instanceof Determina) {
            return new DeterminaSoggetto()
        } else if (documento instanceof Delibera) {
            return new DeliberaSoggetto()
        } else if (documento instanceof PropostaDelibera) {
            return new PropostaDeliberaSoggetto()
        } else if (documento instanceof Documento) {
            return new DocumentoSoggetto()
        } else {
            throw new AttiRuntimeException("Il tipo di documento ${documento} non supporta i soggetti multipli per lo stesso tipo-soggetto")
        }
    }

    /**
     * Ottiene la map dei soggetti dal documento già creato
     *
     * @param documento il documento di cui ottenere i soggetti.
     * @return la mappa dei soggetti.
     */
    Map<String, SoggettoDocumento> calcolaSoggettiDto (IDocumento documento) {
        return documento.soggetti.groupBy { it.tipoSoggetto.codice }.collectEntries { [(it.key): creaSoggetto(it.value)]}
    }

    /**
     * Calcola tutti i soggetti del documento sulla base della sua caratteristica.
     *
     * Viene usato per il "primo" calcolo dei soggetti quando un documento viene creato.
     * In questa fase normalmente il documento non è ancora salvato su db.
     *
     * @param documento il documento di cui effettuare il calcolo dei soggetti
     * @param caratteristica la caratteristica da calcolare.
     * @return i soggetti calcolati precedentemente.
     */
    Map<String, SoggettoDocumento> calcolaSoggetti (IDocumento documento, CaratteristicaTipologia caratteristica, Map<String, SoggettoDocumento> soggetti = [:]) {
        // per calcolare tutti i soggetti, inizio da quelli senza dipendenze
        for (CaratteristicaTipoSoggetto tipoSoggetto : caratteristica.caratteristicheTipiSoggetto) {
            if (tipoSoggetto.tipoSoggettoPartenza == null && soggetti[tipoSoggetto.tipoSoggetto.codice] == null) {
                SoggettoDocumento sogg = creaSoggetto(documento, tipoSoggetto, soggetti)
                if (sogg != null) {
                    soggetti[tipoSoggetto.tipoSoggetto.codice] = sogg
                }
            }
        }
        // FIXME: gestisco solo un livello di profondità (in teoria bisognerebbe andare in ricorsione etc...)
        // poi quelli con dipendenze
        for (CaratteristicaTipoSoggetto tipoSoggetto : caratteristica.caratteristicheTipiSoggetto) {
            if (tipoSoggetto.tipoSoggettoPartenza != null && soggetti[tipoSoggetto.tipoSoggetto.codice] == null) {
                SoggettoDocumento sogg = creaSoggetto(documento, tipoSoggetto, soggetti)
                if (sogg != null) {
                    soggetti[tipoSoggetto.tipoSoggetto.codice] = sogg
                }
            }
        }
        return soggetti
    }

    /**
     * Aggiorna i soggetti sulla base del soggetto modificato.
     *
     * Viene usato quando l'utente modifica un soggetto da interfaccia (ad esempio sceglie l'unità proponente dalla popup di scelta dei soggetti) e
     * bisogna quindi ricalcolare i soggetti "dipendenti" dal soggetto modificato (ad esempio il firmatario di solito viene calcolato in base all'unità proponente)
     *
     * TODO: gestisco solo un livello di profondità (in teoria bisognerebbe andare in ricorsione etc...)
     *
     * @param idCaratteristica id della caratteristica che descrive i soggetti da ricalcolare
     * @param documento il documento proprietario dei soggetti.
     * @param soggetti i soggetti da ricalcolare
     * @param tipoSoggetto il tipo soggetto che è stato modificato e da cui partire per il ricalcolo.
     */
    void aggiornaSoggetti (long idCaratteristica, IDocumento documento, Map<String, SoggettoDocumento> soggetti, String tipoSoggetto) {
        CaratteristicaTipologia caratteristica = CaratteristicaTipologia.get(idCaratteristica)

        def tipiSoggetto = [tipoSoggetto]
        for (CaratteristicaTipoSoggetto caratteristicaTipoSoggetto : caratteristica.caratteristicheTipiSoggetto) {
            if (tipiSoggetto.contains(caratteristicaTipoSoggetto.tipoSoggettoPartenza?.codice)) {
                SoggettoDocumento sogg = creaSoggetto(documento, caratteristicaTipoSoggetto, soggetti)

                if (sogg != null) {
                    // siccome questa regola di calcolo dipende dal tipoSoggetto iniziale, mi segno anche il suo tipoSoggetto,
                    // così da valutare anche le prossime regole di calcolo dipendenti da questo soggetto
                    soggetti[caratteristicaTipoSoggetto.tipoSoggetto.codice] = sogg
                    tipiSoggetto << caratteristicaTipoSoggetto.tipoSoggetto.codice
                }
            }
        }
    }

    /**
     * Esegue la regola di calcolo "LISTA" per il tipo soggetto specificato.
     *
     * @param idCaratteristica
     * @param documento
     * @param soggetti
     * @param tipoSoggetto
     * @return
     */
    List<SoggettoDocumento> calcolaListaSoggetti (long idCaratteristica, IDocumento documento, Map<String, SoggettoDocumento> soggetti, String tipoSoggetto) {
        CaratteristicaTipoSoggetto caratteristicaTipoSoggetto = CaratteristicaTipoSoggetto.createCriteria().get {
            eq("caratteristicaTipologia.id", idCaratteristica)
            eq("tipoSoggetto.codice", tipoSoggetto)

            order("sequenza", "asc")

            fetchMode("tipoSoggetto", FetchMode.JOIN)
            fetchMode("tipoSoggettoPartenza", FetchMode.JOIN)
            fetchMode("regolaCalcoloLista", FetchMode.JOIN)
        }

        List listaSoggetti = eseguiRegola(caratteristicaTipoSoggetto.regolaCalcoloLista, documento, soggetti, caratteristicaTipoSoggetto.ruolo?.ruolo,
                                          caratteristicaTipoSoggetto.tipoSoggettoPartenza)
        return creaSoggetti(caratteristicaTipoSoggetto.tipoSoggetto, listaSoggetti)
    }

    private List<SoggettoDocumento> creaSoggetti (TipoSoggetto tipoSoggetto, List soggettiCalcolati) {
        List<SoggettoDocumento> soggetti = []
        for (def soggetto : soggettiCalcolati) {
            soggetti << creaSoggetto(tipoSoggetto, soggetto)
        }
        return soggetti
    }

    private SoggettoDocumento creaSoggetto (IDocumento documento, CaratteristicaTipoSoggetto caratteristicaTipoSoggetto, Map<String, SoggettoDocumento> soggetti) {
        // se non ho la regola di default, allora non creo il nuovo soggetto.
        // Questo mi serve per poter gestire quei soggetti "opzionali" del documento, ad esempio, per le determine congiunte di Treviso, non è detto che ci sia una Unità o un firmatario in più.
        if (caratteristicaTipoSoggetto.regolaCalcoloDefault == null) {
            return null;
        }

        // un soggetto calcolato da una regola di calcolo può essere:
        // - So4UnitaPubb
        // - So4ComponentePubb
        // - List<So4UnitaPubb>
        // - List<So4ComponentePubb>
        def soggettoCalcolato = eseguiRegola(caratteristicaTipoSoggetto.regolaCalcoloDefault, documento, soggetti,
                                             caratteristicaTipoSoggetto.ruolo?.ruolo,
                                             caratteristicaTipoSoggetto.tipoSoggettoPartenza)

        if (soggettoCalcolato == null) {
            return null
        }

        return creaSoggetto(caratteristicaTipoSoggetto.tipoSoggetto, soggettoCalcolato)
    }

    private SoggettoDocumento creaSoggetto (TipoSoggetto tipoSoggetto, List soggettiCalcolati) {
        return new SoggettoDocumento(tipoSoggetto, soggettiCalcolati)
    }

    private SoggettoDocumento creaSoggetto (TipoSoggetto tipoSoggetto, So4ComponentePubb componente) {
        return new SoggettoDocumento(tipoSoggetto, componente)
    }

    private SoggettoDocumento creaSoggetto (TipoSoggetto tipoSoggetto, So4UnitaPubb unita) {
        return new SoggettoDocumento(tipoSoggetto, unita)
    }

    private SoggettoDocumento creaSoggetto (Collection<ISoggettoDocumento> soggetti) {
        return new SoggettoDocumento(soggetti)
    }

    private SoggettoDocumento creaSoggetto (ISoggettoDocumento soggetto) {
        return new SoggettoDocumento(soggetto)
    }

    private def eseguiRegola (RegolaCalcolo regola, IDocumento documento, Map<String, SoggettoDocumento> soggetti
                              , String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
        return grailsApplication.mainContext.getBean(regola.nomeBean)."${regola.nomeMetodo}"(documento, soggetti, codiceRuolo, tipoSoggettoPartenza)
    }
}