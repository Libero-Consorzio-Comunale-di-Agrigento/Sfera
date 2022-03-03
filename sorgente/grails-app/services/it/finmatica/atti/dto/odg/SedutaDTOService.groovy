package it.finmatica.atti.dto.odg

import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.DeliberaService
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.StatoOdg
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.storico.PropostaDeliberaStorico
import it.finmatica.atti.dto.documenti.DeliberaDTO
import it.finmatica.atti.dto.odg.dizionari.EsitoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.odg.*
import it.finmatica.atti.odg.dizionari.Esito
import it.finmatica.atti.odg.dizionari.EsitoStandard
import it.finmatica.atti.odg.dizionari.RuoloPartecipante
import it.finmatica.atti.odg.dizionari.Voto
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.zhtml.Messagebox

class SedutaDTOService {

    DeliberaService   deliberaService
    WkfIterService    wkfIterService
    SedutaService     sedutaService
    AttiGestioneTesti gestioneTesti
    IGestoreFile      gestoreFile

    SedutaDTO salva (SedutaDTO sedutaDto) {
        Seduta seduta = sedutaDto.domainObject ?: new Seduta()

        // controllo se sto cercando di spostare la data della seduta: non posso farlo se ho almeno un partecipante
        if (seduta.id > 0 && seduta.dataSeduta != sedutaDto.dataSeduta && esistonoPartecipantiPresenti(seduta)) {
            throw new AttiRuntimeException("Non è possibile spostare la seduta: esistono partecipanti presenti.")
        }

        // controllo le date della seduta: prevengo che l'utente inserisca date a caso (ad es per errore date nel 0218 invece che 2018)
        controllaDateSeduta(seduta, sedutaDto, 2)

        // creo una nuova seduta.
        seduta.commissione = sedutaDto.commissione.domainObject
        seduta.tipoSeduta = sedutaDto.tipoSeduta.domainObject
        seduta.dataSeduta = sedutaDto.dataSeduta
        seduta.oraSeduta = sedutaDto.oraSeduta
        seduta.dataSecondaConvocazione = sedutaDto.dataSecondaConvocazione
        seduta.oraSecondaConvocazione = sedutaDto.oraSecondaConvocazione
        seduta.sede = sedutaDto.sede
        seduta.note = sedutaDto.note
        seduta.completa = sedutaDto.completa
        seduta.dataInizioSeduta = sedutaDto.dataInizioSeduta
        seduta.oraInizioSeduta = sedutaDto.oraInizioSeduta
        seduta.dataFineSeduta = sedutaDto.dataFineSeduta
        seduta.oraFineSeduta = sedutaDto.oraFineSeduta
        seduta.secondaConvocazione = sedutaDto.secondaConvocazione
        seduta.pubblica = sedutaDto.pubblica
        seduta.pubblicaWeb = sedutaDto.pubblicaWeb
        seduta.votoPresidente = sedutaDto.votoPresidente
        seduta.secondaSeduta = sedutaDto.secondaSeduta?.domainObject
        seduta.link = sedutaDto.link

        // se sono in creazione, numero la seduta e creo i convocati della commissione
        if (!(seduta.id > 0)) {
            sedutaService.numeraSeduta(seduta)
            sedutaDto.numero = seduta.numero
            sedutaDto.anno = seduta.anno

            seduta.save()

            List<CommissioneComponente> componentiCommissione = CommissioneComponente.createCriteria().list() {
                eq('valido', true)
                eq('commissione.id', seduta.commissione.id)
                order('sequenza', 'asc')

                fetchMode("componente", FetchMode.JOIN)
                fetchMode("ruoloPartecipante", FetchMode.JOIN)
            }

            for (CommissioneComponente c : componentiCommissione) {
                SedutaPartecipante p = new SedutaPartecipante()
                p.seduta = seduta
                p.firmatario = c.firmatario
                p.sequenzaFirma = c.sequenzaFirma
                p.sequenza = c.sequenza
                p.sequenzaPartecipante = c.sequenza
                p.convocato = true
                p.presente = false
                p.assenteNonGiustificato = false
                p.commissioneComponente = c
                p.ruoloPartecipante = c.ruoloPartecipante
                p.componenteEsterno = null
                p.save()
            }
        }

        seduta.save()

        sedutaDto.id = seduta.id
        sedutaDto.version = seduta.version

        return sedutaDto
    }

    void elimina (SedutaDTO sedutaDto) {
        Seduta seduta = Seduta.get(sedutaDto.id)
        seduta.valido = false
        seduta.save()

        //rimuovo gli oggetti seduta
        def listaOggettiSeduta = OggettoSeduta.findAllBySeduta(seduta);
        for (oggetto in listaOggettiSeduta) {
            rimuoviOggettoSeduta(oggetto)
        }
    }

    Seduta creaSecondaSeduta (SedutaDTO sedutaDto) {
        Seduta seduta = sedutaDto.domainObject

        if (seduta.secondaSeduta != null) {
            throw new AttiRuntimeException("La Seduta ha già una seconda convocazione. Non è possibile crearne un'altra.");
        }

        Seduta secondaConvocazione = new Seduta()
        secondaConvocazione.commissione = seduta.commissione
        secondaConvocazione.tipoSeduta = seduta.tipoSeduta
        secondaConvocazione.dataSeduta = seduta.dataSecondaConvocazione
        secondaConvocazione.oraSeduta = seduta.oraSecondaConvocazione
        secondaConvocazione.dataSecondaConvocazione = null
        secondaConvocazione.oraSecondaConvocazione = null
        secondaConvocazione.sede = seduta.sede
        secondaConvocazione.note = seduta.note
        secondaConvocazione.completa = false
        secondaConvocazione.dataInizioSeduta = null
        secondaConvocazione.oraInizioSeduta = null
        secondaConvocazione.dataFineSeduta = null
        secondaConvocazione.oraFineSeduta = null
        secondaConvocazione.secondaConvocazione = true
        secondaConvocazione.secondaSeduta = null
        secondaConvocazione.votoPresidente = seduta.votoPresidente
        secondaConvocazione.pubblica = seduta.pubblica

        sedutaService.numeraSeduta(secondaConvocazione)

        secondaConvocazione.save()

        seduta.completa = true
        seduta.secondaSeduta = secondaConvocazione
        seduta.save()

        List<SedutaPartecipante> partecipanti = SedutaPartecipante.findAllBySeduta(seduta)
        for (SedutaPartecipante partecipante : partecipanti) {
            SedutaPartecipante partecipante2 = new SedutaPartecipante()
            partecipante2.seduta = secondaConvocazione

            partecipante2.commissioneComponente = partecipante.commissioneComponente
            partecipante2.ruoloPartecipante = partecipante.ruoloPartecipante
            partecipante2.componenteEsterno = partecipante.componenteEsterno
            partecipante2.convocato = partecipante.convocato
            partecipante2.presente = partecipante.presente
            partecipante2.assenteNonGiustificato = partecipante.assenteNonGiustificato
            partecipante2.firmatario = partecipante.firmatario
            partecipante2.sequenza = partecipante.sequenza
            partecipante2.sequenzaPartecipante = partecipante.sequenzaPartecipante
            partecipante2.sequenzaFirma = partecipante.sequenzaFirma

            partecipante2.save()
        }

        List<OggettoSeduta> oggettiSeduta = OggettoSeduta.findAllBySeduta(seduta, [sort: "sequenzaConvocazione", order: "asc"])
        for (OggettoSeduta oggettoSeduta : oggettiSeduta) {
            OggettoSeduta obj = new OggettoSeduta()
            obj.seduta = secondaConvocazione
            obj.propostaDelibera = oggettoSeduta.propostaDelibera
            obj.determina = oggettoSeduta.determina
            obj.esito = oggettoSeduta.esito
            obj.delega = oggettoSeduta.delega
            obj.dataDiscussione = oggettoSeduta.dataDiscussione
            obj.oraDiscussione = oggettoSeduta.oraDiscussione
            obj.note = oggettoSeduta.note
            obj.sequenzaConvocazione = oggettoSeduta.sequenzaConvocazione
            obj.sequenzaDiscussione = oggettoSeduta.sequenzaDiscussione
            obj.oggettoAggiuntivo = oggettoSeduta.oggettoAggiuntivo
            obj.eseguibilitaImmediata = oggettoSeduta.eseguibilitaImmediata
            obj.save()

            // aggiorno la proposta collegata alla nuova seduta:
            PropostaDelibera proposta = oggettoSeduta.propostaDelibera
            proposta.oggettoSeduta = obj
            proposta.save()
        }

        return seduta
    }

    void rimuoviProposta (OggettoSedutaDTO selezionato) {
        rimuoviOggettoSeduta(selezionato.domainObject)
        riordinaProposteOdg(selezionato.seduta.id)
    }

    void rimuoviOggettoSeduta (OggettoSeduta oggettoSeduta) {
        List<OggettoPartecipante> partecipanti = OggettoPartecipante.findAllByOggettoSeduta(oggettoSeduta)
        partecipanti*.delete()

        PropostaDelibera proposta = oggettoSeduta.propostaDelibera

        // elimino l'oggetto seduta dallo storico
        // (FIXME: questo non ha molto senso: la cosa migliore sarebbe mettere non valido l'oggettoSeduta e lasciare il riferimento sullo storico)
        List<PropostaDeliberaStorico> proposteStorico = PropostaDeliberaStorico.findAllByIdPropostaDelibera(proposta.id)
        proposteStorico*.oggettoSeduta = null
        proposteStorico*.save()

        proposta.oggettoSeduta = null
        StatoOdg.togliDaSeduta(proposta)
        proposta.save()
        oggettoSeduta.delete()
    }

    void riordinaProposteOdg (long idSeduta) {
        def listaProposteOdg = OggettoSeduta.createCriteria().list {
            eq("seduta.id", idSeduta)
            order("sequenzaConvocazione", "asc")
        }

        int sequenza = 1
        for (OggettoSeduta o : listaProposteOdg) {
            o.sequenzaConvocazione = sequenza++
            o.sequenzaDiscussione = o.sequenzaConvocazione
            o.save()
        }
    }

    OggettoSeduta inserisciProposta (SedutaDTO sedutaDto, def propostaDto, int index) {
        OggettoSeduta presente = OggettoSeduta.findBySedutaAndPropostaDelibera(sedutaDto.domainObject, propostaDto.domainObject)
        if (presente != null) {
            if (presente.esito?.esitoStandard?.codice != EsitoStandard.RINVIO_UFFICIO || !presente.confermaEsito) {
                return presente
            }
        }

        OggettoSeduta oggettoSeduta = new OggettoSeduta()
        oggettoSeduta.seduta = sedutaDto.domainObject
        oggettoSeduta.sequenzaConvocazione = OggettoSeduta.countBySeduta(sedutaDto.domainObject) + 1
        oggettoSeduta.sequenzaDiscussione = oggettoSeduta.sequenzaConvocazione
        // http://svi-redmine/issues/22189 -> ODG: Impostare la data di discussione della proposta in fase di conferma esito
        //oggettoSeduta.dataDiscussione = sedutaDto.dataSeduta?.clearTime();

        def proposta = propostaDto.domainObject;

        if (proposta instanceof PropostaDelibera) {
            oggettoSeduta.propostaDelibera = proposta
            oggettoSeduta.delega = proposta.delega
            oggettoSeduta.eseguibilitaImmediata = proposta.eseguibilitaImmediata
            oggettoSeduta.motivazioniEseguibilita = proposta.motivazioniEseguibilita
        } else {
            oggettoSeduta.determina = proposta
        }

        oggettoSeduta.save()

        StatoOdg.inserisciInSeduta(proposta);
        proposta.oggettoSeduta = oggettoSeduta
        proposta.save()

        spostaOggettoSeduta(oggettoSeduta, index)
        return oggettoSeduta;
    }

    void spostaOggettoSeduta (OggettoSeduta oggettoSeduta, int index) {
        // parto da 1.
        if (index < 1) {
            index = 1;
        }

        // il numero massimo di sequenza è il numero di oggetti appartenenti alla stessa seduta:
        int maxsequenza = OggettoSeduta.countBySeduta(oggettoSeduta.seduta);
        if (index > maxsequenza) {
            index = maxsequenza;
        }

        int oldsequenza = oggettoSeduta.sequenzaConvocazione;

        if (oldsequenza == index) {
            return;
        }

        oggettoSeduta.sequenzaConvocazione = index
        oggettoSeduta.sequenzaDiscussione = oggettoSeduta.sequenzaConvocazione
        oggettoSeduta.save()

        def oggettiSeduta = OggettoSeduta.createCriteria().list {
            if (index > oldsequenza) {
                le("sequenzaConvocazione", index)
                ge("sequenzaConvocazione", oldsequenza)
            } else {
                le("sequenzaConvocazione", oldsequenza)
                ge("sequenzaConvocazione", index)
            }
            ne("id", oggettoSeduta.id)
            eq("seduta.id", oggettoSeduta.seduta.id)
            order("sequenzaConvocazione", "asc")
        }

        for (OggettoSeduta o : oggettiSeduta) {
            if (o.id == oggettoSeduta.id) {
                continue;
            }
            o.sequenzaConvocazione += ((oldsequenza > index) ? +1 : -1);
            o.sequenzaDiscussione = o.sequenzaConvocazione
            o.save()
        }
    }

    void inserisciTutteProposte (SedutaDTO seduta, def listaProposte) {
        int nOggettiSeduta = OggettoSeduta.countBySeduta(seduta.domainObject)
        for (def proposta : listaProposte) {
            inserisciProposta(seduta, proposta, nOggettiSeduta + 1);
            nOggettiSeduta++;
        }
    }

    void rimuoviTuttiOggettiSeduta (SedutaDTO seduta) {
        // cerco tutti gli oggetti seduta di questa seduta e tento di eliminarli:
        boolean tutti = true
        List<OggettoSeduta> oggettiSeduta = OggettoSeduta.findAllBySeduta(seduta.domainObject)
        for (OggettoSeduta oggettoSeduta : oggettiSeduta) {
            if (oggettoSeduta.confermaEsito) {
                tutti = false
            } else {
                rimuoviOggettoSeduta(oggettoSeduta)
            };
        }
        if (!tutti) {
            Messagebox.show("Impossibile rimuovere le proposte con un esito confermato!!")
        }
        riordinaProposteOdg(seduta.id)
    }

    void spostaOggettoSedutaSu (OggettoSedutaDTO oggettoSeduta) {
        spostaOggettoSeduta(oggettoSeduta.domainObject, oggettoSeduta.sequenzaConvocazione - 1)
    }

    void spostaOggettoSedutaGiu (OggettoSedutaDTO oggettoSeduta) {
        spostaOggettoSeduta(oggettoSeduta.domainObject, oggettoSeduta.sequenzaConvocazione + 1)
    }

    void assegnaEsiti (Collection<OggettoSedutaDTO> listaOggettiSedutaDto, EsitoDTO esitoDto, String note) {
        Esito esito = esitoDto.domainObject
        for (OggettoSedutaDTO oggetto : listaOggettiSedutaDto) {
            OggettoSeduta oggettoSeduta = oggetto.domainObject

            // sto per creare una delibera su un oggetto seduta non adottabile?
            if (!oggettoSeduta.propostaDelibera.tipologia.adottabile && esito.esitoStandard.creaDelibera) {
                throw new AttiRuntimeException(
                        "Non è possibile assegnare l'esito '${esito.titolo}' alla proposta n. ${oggettoSeduta.propostaDelibera.numeroProposta} perché la tipologia della proposta indica che non è adottabile.")
            }

            oggettoSeduta.esito = esito
            if (note) {
                oggettoSeduta.note = note
            }
            oggettoSeduta.save()

            // aggiungo i partecipanti all'oggetto
            creaPartecipanti(oggettoSeduta)
        }
    }

    List<OggettoPartecipante> creaPartecipanti (OggettoSeduta oggetto) {
        // se oggetto è il primo della discussione e non ha partecipanti, gli aggiungo quelli di default della seduta.
        if (oggetto.sequenzaDiscussione == 1) {
            List<OggettoPartecipante> listaPartecipanti = OggettoPartecipante.findAllByOggettoSeduta(oggetto)
            if (listaPartecipanti.size() == 0) {
                listaPartecipanti = creaOggettoPartecipanteFromSeduta(SedutaPartecipante.findAllBySeduta(oggetto.seduta), oggetto)
            }

            return listaPartecipanti.sort { it.sequenza }
        }

        // altrimenti, gli aggiungo i partecipanti dell'oggetto discusso precedentemente (se questo non ha già dei partecipanti)
        List<OggettoPartecipante> listaPartecipanti = OggettoPartecipante.findAllByOggettoSeduta(oggetto)
        if (listaPartecipanti.size() == 0) {
            OggettoSeduta precedente = OggettoSeduta.createCriteria().get {
                eq("sequenzaDiscussione", oggetto.sequenzaDiscussione - 1)
                eq("seduta.id", oggetto.seduta.id)
            }

            List<OggettoPartecipante> lista_ogg = OggettoPartecipante.findAllByOggettoSeduta(precedente)
            if (lista_ogg.size() > 0) {
                listaPartecipanti = creaOggettoPartecipanteFromOggetti(OggettoPartecipante.findAllByOggettoSeduta(precedente), oggetto)
            } else {
                listaPartecipanti = creaOggettoPartecipanteFromSeduta(SedutaPartecipante.findAllBySeduta(oggetto.seduta), oggetto)
            }
        }

        return listaPartecipanti.sort { it.sequenza }
    }

    private List<OggettoPartecipante> creaOggettoPartecipanteFromSeduta (List<SedutaPartecipante> sedutaPartecipanti, OggettoSeduta oggetto) {
        List<OggettoPartecipante> oggPart = []
        for (SedutaPartecipante sedutaPartecipante : sedutaPartecipanti) {
            OggettoPartecipante p = new OggettoPartecipante()
            p.oggettoSeduta = oggetto
            p.sedutaPartecipante = sedutaPartecipante
            p.ruoloPartecipante = sedutaPartecipante.ruoloPartecipante
            p.assenteNonGiustificato = sedutaPartecipante.assenteNonGiustificato
            p.firmatario = sedutaPartecipante.firmatario
            p.sequenzaFirma = sedutaPartecipante.sequenzaFirma
            p.presente = sedutaPartecipante.presente
            p.sequenza = sedutaPartecipante.sequenzaPartecipante
            if (p.presente && p.ruoloPartecipante?.codice != RuoloPartecipante.CODICE_SEGRETARIO) {
                p.voto = Voto.findByPredefinito(true)
            }
            p.save()
            oggPart.add(p)
        }
        return oggPart
    }

    private List<OggettoPartecipante> creaOggettoPartecipanteFromOggetti (List<OggettoPartecipante> origine, OggettoSeduta oggetto) {
        List<OggettoPartecipante> oggPart = []
        for (OggettoPartecipante oggettoPartecipante : origine) {
            OggettoPartecipante p = new OggettoPartecipante()
            p.oggettoSeduta = oggetto
            p.sedutaPartecipante = oggettoPartecipante.sedutaPartecipante
            p.ruoloPartecipante = oggettoPartecipante.ruoloPartecipante
            p.assenteNonGiustificato = oggettoPartecipante.assenteNonGiustificato
            p.presente = oggettoPartecipante.presente
            p.firmatario = oggettoPartecipante.firmatario
            p.sequenzaFirma = oggettoPartecipante.sequenzaFirma
            p.sequenza = oggettoPartecipante.sequenza
            if (p.presente && p.ruoloPartecipante?.codice != RuoloPartecipante.CODICE_SEGRETARIO) {
                p.voto = Voto.findByPredefinito(true)
            }
            p.save()
            oggPart.add(p)
        }
        return oggPart
    }

    private List<OggettoPartecipante> aggiungiOggettoPartecipanteFromSeduta (List<SedutaPartecipante> sedutaPartecipanti, OggettoSeduta oggetto, int posizione) {
        List<OggettoPartecipante> oggPart = []
        int elem = posizione + 1
        for (SedutaPartecipante sedutaPartecipante : sedutaPartecipanti) {
            OggettoPartecipante p = new OggettoPartecipante()
            p.oggettoSeduta = oggetto
            p.sedutaPartecipante = sedutaPartecipante
            p.ruoloPartecipante = sedutaPartecipante.ruoloPartecipante
            p.assenteNonGiustificato = sedutaPartecipante.assenteNonGiustificato
            p.firmatario = sedutaPartecipante.firmatario
            p.sequenzaFirma = sedutaPartecipante.sequenzaFirma
            p.presente = sedutaPartecipante.presente
            p.sequenza = elem
            if (p.presente && p.ruoloPartecipante?.codice != RuoloPartecipante.CODICE_SEGRETARIO) {
                p.voto = Voto.findByPredefinito(true)
            }
            elem++
            p.save()
            oggPart.add(p)
        }
        return oggPart
    }

    boolean esistonoPartecipantiPresenti (Seduta seduta) {
        return (SedutaPartecipante.countBySedutaAndPresente(seduta, true) > 0)
    }

    boolean esistonoOggettiSedutaConfermatiConPartecipante (long idSeduta, long idSedutaPartecipante) {
        long count = OggettoPartecipante.createCriteria().count {
            eq("sedutaPartecipante.id", idSedutaPartecipante)

            oggettoSeduta {
                eq("seduta.id", idSeduta)
                eq("confermaEsito", true)
            }
        }
        return (count > 0)
    }

    boolean esistonoOggettiSedutaConfermati (Seduta seduta) {
        return (OggettoSeduta.countBySedutaAndConfermaEsito(seduta, true) > 0)
    }

    void confermaEsiti (List<OggettoSedutaDTO> oggettiSeduta) {
        for (OggettoSedutaDTO oggetto : oggettiSeduta) {
            sedutaService.confermaEsito(oggetto.domainObject)
        }
    }

    Delibera confermaEsitoENumeraDelibera (OggettoSedutaDTO o) {
        OggettoSeduta oggettoSeduta = o.domainObject
        sedutaService.confermaEsito(oggettoSeduta)
        Delibera del = deliberaService.creaDelibera(oggettoSeduta.propostaDelibera, oggettoSeduta)
        return deliberaService.numeraDelibera(del)
    }

    Delibera confermaEsitoECreaDelibera (OggettoSedutaDTO o) {
        OggettoSeduta oggettoSeduta = o.domainObject
        sedutaService.confermaEsito(oggettoSeduta)
        return deliberaService.creaDelibera(oggettoSeduta.propostaDelibera, oggettoSeduta)
    }

    void creaDelibera (OggettoSedutaDTO oggettoSedutaDto) {
        OggettoSeduta oggettoSeduta = oggettoSedutaDto.domainObject
        deliberaService.creaDelibera(oggettoSeduta.propostaDelibera, oggettoSeduta)
    }

    void generaTestoDelibera (DeliberaDTO delibera, GestioneTestiModelloDTO modelloDTO) {
        // il modelloDTO può essere nullo se in popup di scelta testi l'utente lascia "utilizza modello testo scritto in tipologia"
        deliberaService.creaTesto(delibera.domainObject, modelloDTO?.domainObject)
    }

    /**
     * Issue: http://svi-redmine/issues/10320
     *
     * @param seduta
     * @return TRUE se ci sono proposte adottabili con esito confermato nelle sedute successive
     */
    boolean esisteSedutaSuccessivaConEsitoConfermato (SedutaDTO sedutaDto) {
        int count = OggettoSeduta.createCriteria().get() {

            projections {
                rowCount()
            }

            eq('confermaEsito', true)

            seduta {
                gt('dataSeduta', sedutaDto.dataSeduta)
                eq('anno', sedutaDto.anno)
                eq('commissione.id', sedutaDto.commissione.id)
                eq('valido', true)
            }

            propostaDelibera {
                tipologia {
                    eq("adottabile", true)
                }
            }
        }

        return (count > 0)
    }

    /**
     * Issue: http://svi-redmine/issues/10320
     * @return TRUE se ci sono proposte adottabili con esito NON confermato nelle sedute precedenti
     */
    boolean esisteSedutaPrecedenteConEsitoNonConfermato (SedutaDTO sedutaDto) {
        int count = OggettoSeduta.createCriteria().get() {

            projections {
                rowCount()
            }

            eq('confermaEsito', false)

            seduta {
                lt('dataSeduta', sedutaDto.dataSeduta)
                eq('anno', sedutaDto.anno)
                eq('commissione.id', sedutaDto.commissione.id)
                eq('valido', true)
                isNull('dataSecondaConvocazione')
            }

            propostaDelibera {
                tipologia {
                    eq("adottabile", true)
                }
            }
        }

        return (count > 0)
    }

    /**
     * Issue: http://svi-redmine/issues/10320
     *
     * @return TRUE se esiste almeno una delibera non numerata (ma che deve esserlo) in una seduta precedente a quella di riferimento.
     */
    boolean esistonoDelibereNonNumerateInSedutePrecedenti (SedutaDTO sedutaDto) {
        int count = OggettoSeduta.createCriteria().get() {
            createAlias("delibera", "deli", CriteriaSpecification.LEFT_JOIN)

            projections {
                rowCount()
            }

            isNull("deli.id")

            esito {
                esitoStandard {
                    eq('creaDelibera', true)
                }
            }

            seduta {
                lt('dataSeduta', sedutaDto.dataSeduta)
                eq('anno', sedutaDto.anno)
                eq('commissione.id', sedutaDto.commissione.id)
                eq('valido', true)
            }
        }

        return (count > 0)
    }

    void controllaDateSeduta (Seduta seduta, SedutaDTO sedutaDTO, int anni) {
        controllaData("data della seduta", seduta.dataSeduta, sedutaDTO.dataSeduta, anni)
        controllaData("data della seconda convocazione", seduta.dataSecondaConvocazione, sedutaDTO.dataSecondaConvocazione, anni)
        controllaData("data di inzio seduta", seduta.dataInizioSeduta, sedutaDTO.dataInizioSeduta, anni)
        controllaData("data di fine seduta", seduta.dataFineSeduta, sedutaDTO.dataFineSeduta, anni)
    }

    void controllaData (String descrizioneData, Date data, Date dataDto, int anni) {
        if (data == dataDto) {
            return
        }

        if (dataDto != null && !isDataInRange(dataDto, anni)) {
            throw new AttiRuntimeException("La ${descrizioneData} non è coerente con il periodo di svolgimento")
        }
    }

    boolean isDataBetween (Date toCheck, Date before, Date after) {
        return toCheck.before(after) && toCheck.after(before)
    }

    boolean isDataInRange (Date toCheck, int anni) {
        return isDataBetween(toCheck, getDataSedutaMinima(anni), getDataSedutaMassima(anni))
    }

    Date getDataSedutaMinima (int anni) {
        Calendar pastDate = new GregorianCalendar()
        pastDate.setTime(new Date())
        pastDate.roll(Calendar.YEAR, -anni)
        pastDate.clearTime()
        return pastDate.getTime()
    }

    Date getDataSedutaMassima (int anni) {
        Calendar futureDate = new GregorianCalendar()
        futureDate.setTime(new Date())
        futureDate.add(Calendar.YEAR, anni)
        futureDate.clearTime()
        return futureDate.getTime()
    }
}
