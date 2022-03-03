package it.finmatica.atti.documenti

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.IntegrazioneAlbo
import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.dizionari.CalendarioFestivitaService
import it.finmatica.atti.dizionari.RegistroService
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.tipologie.GestioneTestiModelloCompetenza
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CaratteristicaTipologiaService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.atti.integrazioni.CasaDiVetroService
import it.finmatica.atti.odg.CommissioneComponente
import it.finmatica.atti.odg.CommissioneStampa
import it.finmatica.atti.odg.OggettoPartecipante
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.atti.odg.dizionari.RuoloPartecipante
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.IGestoreCompetenze
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.zkutils.SuccessHandler
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification

import static it.finmatica.zkutils.LabelUtils.getLabel as l

class DeliberaService {

    CaratteristicaTipologiaService caratteristicaTipologiaService
    CalendarioFestivitaService     calendarioFestivitaService
    IntegrazioneContabilita        integrazioneContabilita
    DatiAggiuntiviService          datiAggiuntiviService
    GestioneTestiService           gestioneTestiService
    IDocumentaleEsterno            gestoreDocumentaleEsterno
    CertificatoService             certificatoService
    IGestoreCompetenze             gestoreCompetenze
    CasaDiVetroService             casaDiVetroService
    VistoParereService             vistoParereService
    AttiGestioneTesti              gestioneTesti
    NotificheService               notificheService
    AttiFirmaService               attiFirmaService
    RegistroService                registroService
    SuccessHandler                 successHandler
    WkfIterService                 wkfIterService
    IGestoreFile                   gestoreFile
    IntegrazioneAlbo               integrazioneAlbo
    AllegatoService                allegatoService
    BudgetService                  budgetService

    void rigeneraDelibera (Delibera delibera) {
        // resetto lo stato della proposta
        delibera.stato = null
        delibera.statoFirma = StatoFirma.DA_FIRMARE

        // resetto i visti
        delibera.codiciVistiTrattati = ""

        attiFirmaService.eliminaFirmatari(delibera, true)

        // per qualche ragione a me ignota, non posso fare il for direttamente su d.visti (che in questo punto è un org.hibernate.collection.PersistentSet)
        // perché da un errore poco comprensibile. Con questo trucco invece viene riportato ad un più canonico ArrayList e funziona.
        List<VistoParere> vistiDaEliminare = []
        vistiDaEliminare.addAll(delibera.visti)

        // invalido i visti presenti, e li ricreo nuovi.
        for (VistoParere visto : vistiDaEliminare) {

            // salto quelli già invalidi:
            if (!visto.valido) {
                continue;
            }

            visto.valido = false
            visto.save()

            if (visto.iter != null && visto.iter.dataFine == null) {
                wkfIterService.terminaIter(visto.iter)
            }

            // elimino tutte le notifiche di cambio step
            notificheService.eliminaNotifiche(visto, TipoNotifica.ASSEGNAZIONE)
            // elimino tutte le "altre" notifiche
            notificheService.eliminaNotifiche(visto)

            // ricreo il visto partendo da quello eliminato.
            vistoParereService.creaVistoParere(delibera, visto.tipologia, visto.automatico, visto.firmatario, visto.unitaSo4)
        }

        // se il file è p7m o pdf, lo elimino e lo sostituisco con quello che ho in d.testoOdt
        if (delibera.testo != null && !delibera.testo.isModificabile() && delibera.testoOdt != null) {
            gestioneTesti.ripristinaTestoOdt(delibera)
        }

        // ripristino gli eventuali file allegati firmati:
        for (Allegato allegato : delibera.allegati.findAll { it.statoFirma == StatoFirma.FIRMATO || it.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE }) {
            // per ogni allegato, se è firmato, lo metto come "DA_FIRMARE"
            for (FileAllegato fileAllegato : allegato.fileAllegati) {
                if (fileAllegato.firmato) {
                    // se ho almeno un file firmato da applicativo, allora metto lo stato dell'allegato a "DA_FIRMARE"
                    allegato.statoFirma = StatoFirma.DA_FIRMARE;

                    // "elimino" il file firmato e ci rimetto quello originale
                    gestioneTesti.ripristinaFileOriginale(allegato, fileAllegato)
                }
            }
        }

        delibera.save()
    }

    List<Delibera> getDelibereFinePubblicazione () {
        return Delibera.createCriteria().list {
            createAlias('iter', 'it', CriteriaSpecification.INNER_JOIN)
            eq("pubblicaRevoca", false)
            isNull("it.dataFine")

            or {
                lt("dataFinePubblicazione", new Date())
                lt("dataFinePubblicazione2", new Date())
            }
        }
    }

    List<Delibera> getDelibereDaRendereEsecutive () {
        // cerco tutte le delibere che hanno la data di pubblicazione diversa da null (cioè sono in pubblicazione).
        // non uso lo "stato del documento" perché in caso di errore questo codice potrebbe essere rilanciato successivamente
        // alla fine della pubblicazione.

        // http://svi-redmine/issues/12632
        // Il riferimento dei giorni di pubblicazione potrebbe essere dalla data di inizio o fine della pubblicazione
        String proprietaDataPubblicazione = ("INIZIO".equals(Impostazioni.INIZIO_ESECUTIVITA.valore) ? 'dataPubblicazione' : 'dataFinePubblicazione')

        // TODO: va calcolata la data di esecutivà correttamente usando la funzione calcolaDataEsecutivita

        return Delibera.executeQuery(
                """select d
				 from Delibera d
				where trunc(d.""" + proprietaDataPubblicazione + """ + :giorniEsec) <= current_date()
				  and d.""" + proprietaDataPubblicazione + """ is not null
				  and d.valido = true
				  and d.dataEsecutivita is null
				  and d.diventaEsecutiva = true"""
                , [giorniEsec: (double) Impostazioni.PUBBLICAZIONE_GIORNI_ESECUTIVITA.valoreInt])
    }

    /**
     * Rende esecutiva la Delibera (cioè appone la data di esecutività).
     * Se la delibera è già esecutiva (cioè ha dataEsecutivita != null), non fa nulla.
     *
     * Annulla le eventuali delibere collegate (con operazione di annullamento).
     * Crea il certificato di esecutività (se configurato in tipologia).
     * Invia la notifica di esecutività (se configurata)
     *
     * Se la Data di Esecutività viene passata null, allora verrà calcolata in base alle impostazioni come N giorni dall'inizio o dalla fine della pubblicazione.
     *
     * @param delibera la delibera da rendere esecutiva
     * @param dataEsecutivita la data di esecutività da utilizzare. Se null, verrà calcolata in base alle impostazioni
     * @return la delibera esecutiva.
     */
    Delibera rendiEsecutiva (Delibera delibera, Date dataEsecutivita = null) {
        // se per qualche ragione, la delibera è già esecutiva, non faccio niente.
        if (delibera.dataEsecutivita != null) {
            return delibera
        }

        if (dataEsecutivita == null) {
            dataEsecutivita = calcolaDataEsecutivita(delibera)
        }

        // se la data di esecutività richiesta è successiva ad oggi, non devo rendere esecutiva la delibera. Ci penserà il job notturno.
        if (new Date().clearTime().before(dataEsecutivita.clone().clearTime())) {
            return delibera
        }

        delibera.dataEsecutivita = dataEsecutivita
        delibera.stato = StatoDocumento.ESECUTIVO

        delibera.save()

        // prendo le delibere collegate:
        for (DocumentoCollegato documentoCollegato : delibera.propostaDelibera.documentiCollegati) {
            if (documentoCollegato.operazione == DocumentoCollegato.OPERAZIONE_ANNULLA) {
                annullaDelibera(documentoCollegato.deliberaCollegata, delibera)
                successHandler.addMessage(l("message.delibera.annullata", documentoCollegato.deliberaCollegata.estremiAtto))
            }
        }

        // creo il certificato solo se effettivamente è configurato:
        if (delibera.tipologiaDocumento.tipoCertEsec != null) {
            certificatoService.creaCertificato(delibera, delibera.tipologiaDocumento.tipoCertEsec, Certificato.CERTIFICATO_ESECUTIVITA, false)
        }

        if (!Impostazioni.INTEGRAZIONE_ALBO.isDisabilitato() && delibera.idDocumentoAlbo > 0) {
            integrazioneAlbo.aggiornaDataEsecutivita(delibera)
        }

        // rendo esecutivi i movimenti sulla delibera
        integrazioneContabilita.rendiEsecutivoAtto(delibera)

        if (Impostazioni.GESTIONE_BUDGET.abilitato){
            budgetService.autorizzaBudget(delibera.proposta)
        }

        // invio le notifiche di esecutività configurate.
        notificheService.notifica(TipoNotifica.ESECUTIVITA, delibera)

        return delibera
    }

    /**
     * Rende immediatamente esecutiva la Delibera (cioè appone la data di esecutività).
     * Se la delibera è già esecutiva (cioè ha dataEsecutivita != null), non fa nulla.
     * Se la delibera non è immediatamente eseguibile, non fa nulla.
     *
     * Annulla le eventuali delibere collegate (con operazione di annullamento).
     * Crea il certificato di esecutività (se configurato in tipologia).
     * Invia la notifica di esecutività (se configurata)
     *
     * Come Data di Esecutività verrà utilizzata la data della seduta (se presente) altrimenti la data odierna.
     *
     * @param delibera la delibera da rendere esecutiva
     */
    void rendiImmediatamenteEsecutiva (Delibera delibera) {
        if (!delibera.eseguibilitaImmediata) {
            return
        }

        rendiEsecutiva(delibera, calcolaDataImmediataEsecutivita(delibera))
    }

    Date calcolaDataImmediataEsecutivita (Delibera delibera) {
        return esecutivitaGiornoFeriale(delibera.oggettoSeduta?.seduta?.dataSeduta ?: new Date())
    }

    /**
     * Calcola la data di esecutività della delibera.
     *
     * Il calcolo avviene in base alle impostazioni INIZIO_ESECUTIVITA, PUBBLICAZIONE_GIORNI_ESECUTIVITA.
     * Se INIZIO_ESECUTIVITA = "INIZIO", allora la data sarà la dataPubblicazione più il n. di giorni specificati in PUBBLICAZIONE_GIORNI_ESECUTIVITA.
     * Altrimenti, verrà preso come riferimento la data di fine pubblicazione (cui verranno sommati i giorni in PUBBLICAZIONE_GIORNI_ESECUTIVITA).
     *
     * @param delibera la delibera di cui bisogna calcolare la data di esecutività
     * @throws AttiRuntimeException se la data di riferimento per il calcolo è nulla (sia essa data di pubblicazione o fine pubblicazione)
     * @return la data calcolata
     */
    Date calcolaDataEsecutivita (Delibera delibera) {
        if (delibera.stato == StatoDocumento.ATTESA_ESECUTIVITA_MANUALE) {
            if (delibera.dataEsecutivitaManuale == null) {
                throw new AttiRuntimeException(l("message.delibera.esecutivitaRichiestaDataManuale"))
            }

            return delibera.dataEsecutivitaManuale
        }

        // http://svi-redmine/issues/12632
        // devo calcolare la data di esecutività dall'inizio della pubblicazione:
        if ("INIZIO".equals(Impostazioni.INIZIO_ESECUTIVITA.valore)) {
            // siccome il calcolo della data di esecutività si basa sulle date di pubblicazione, devo verificare di avere questi dati:
            if (delibera.dataPubblicazione == null) {
                throw new AttiRuntimeException(l("message.delibera.esecutivitaRichiestaDataInizioPubblicazione", delibera.estremiAtto))
            }

            return esecutivitaGiornoFeriale(delibera.dataPubblicazione + Impostazioni.PUBBLICAZIONE_GIORNI_ESECUTIVITA.valoreInt)
        }

        // siccome il calcolo della data di esecutività si basa sulle date di pubblicazione, devo verificare di avere questi dati:
        if (delibera.dataFinePubblicazione == null) {
            throw new AttiRuntimeException(l("message.delibera.esecutivitaRichiestaDataFinePubblicazione", delibera.estremiAtto))
        }

        return esecutivitaGiornoFeriale(delibera.dataFinePubblicazione + Impostazioni.PUBBLICAZIONE_GIORNI_ESECUTIVITA.valoreInt)
    }

    Date esecutivitaGiornoFeriale (Date dataEsecutivita) {
        if (Impostazioni.ESECUTIVITA_SOLO_GIORNI_FERIALI.abilitato) {
            return calendarioFestivitaService.getProssimoGiornoFeriale(dataEsecutivita)
        }

        return dataEsecutivita
    }

    /**
     * Adotta la delibera: imposta la data di adozione, numera la delibera ed invia la notifica di adozione.
     *
     * @param delibera
     * @param dataAdozione
     */
    void adottaDelibera (Delibera delibera) {

        if (delibera.oggettoSeduta != null && Impostazioni.DATA_ADOZIONE_DELIBERE.abilitato) {
            delibera.dataAdozione = delibera.oggettoSeduta.dataDiscussione

            if (delibera.dataAdozione == null) {
                delibera.dataAdozione = delibera.oggettoSeduta.seduta.dataInizioSeduta

                if (delibera.dataAdozione == null) {
                    delibera.dataAdozione = delibera.oggettoSeduta.seduta.dataSeduta
                }
            }

        } else if (delibera.dataAdozione == null) {
            delibera.dataAdozione = new Date()
        }

        if (delibera.dataAdozione == null) {
            throw new AttiRuntimeException(l("message.delibera.mancaDataAdozione"))
        }

        // assegno la data di adozione e numero la delibera
        numeraDelibera(delibera)

        // invio la notifica che la proposta è stata adottata.
        notificheService.notifica(TipoNotifica.ADOZIONE, delibera)
    }

    /**
     * Crea la delibera. La numera se richiesto dalle impostazioni.
     *
     * @param oggettoSeduta oggetto discusso in seduta
     * @return la delibera creata e numerata.
     */
    Delibera creaDelibera (PropostaDelibera propostaDelibera, OggettoSeduta oggettoSeduta = null) {

        // controllo di non creare la delibera due volte per la stessa proposta:
        propostaDelibera.lock()

        if (propostaDelibera.atto != null) {
            throw new AttiRuntimeException(l("message.delibera.propostaConAtto"))
        }

        Delibera delibera = new Delibera()

        delibera.proposta = propostaDelibera
        delibera.oggetto = propostaDelibera.oggetto
        delibera.registroDelibera = propostaDelibera.tipologia.tipoRegistroDelibera
        delibera.pubblicaRevoca = propostaDelibera.pubblicaRevoca
        delibera.giorniPubblicazione = propostaDelibera.giorniPubblicazione
        delibera.riservato = propostaDelibera.riservato
        delibera.daInviareCorteConti = propostaDelibera.daInviareCorteConti
        delibera.eseguibilitaImmediata = propostaDelibera.eseguibilitaImmediata
        delibera.motivazioniEseguibilita = propostaDelibera.motivazioniEseguibilita
        delibera.diventaEsecutiva = propostaDelibera.tipologia.diventaEsecutiva

        delibera.classificaCodice = propostaDelibera.classificaCodice
        delibera.classificaDal = propostaDelibera.classificaDal
        delibera.classificaDescrizione = propostaDelibera.classificaDescrizione
        delibera.fascicoloAnno = propostaDelibera.fascicoloAnno
        delibera.fascicoloNumero = propostaDelibera.fascicoloNumero
        delibera.fascicoloOggetto = propostaDelibera.fascicoloOggetto

        // prima di gestire le competenze devo salvare la delibera altrimenti nella "assegnaCompetenze" fa una query e da' errore "object references an unsaved transient instance"
        delibera.save()

        // copio tutti i dati aggiuntivi della proposta di delibera.
        // Questo serve per il SIAR: http://svi-redmine/issues/24722
        // TODO: migliorare la gestione dei dati aggiuntivi creando un dizionario degli stessi da cui attivarli ed impostare varie proprietà
        // come "copia questo dato dalla proposta all'atto"
        datiAggiuntiviService.copiaDatiAggiuntivi(propostaDelibera, delibera)

        // se provengo da un oggetto seduta allora lo associo subito.
        if (oggettoSeduta != null) {
            delibera.oggettoSeduta = oggettoSeduta
            delibera.eseguibilitaImmediata = oggettoSeduta.eseguibilitaImmediata
            delibera.motivazioniEseguibilita = oggettoSeduta.motivazioniEseguibilita

            // creo le competenze per chi ha ruolo di gestione nella commissione
            gestoreCompetenze.assegnaCompetenze(delibera, WkfTipoOggetto.get(Delibera.TIPO_OGGETTO),
                                                new Attore(ruoloAd4: oggettoSeduta.seduta.commissione.ruoloCompetenze), true, true, false, null)
        }

        // calcolo il modello di testo dopo aver associato l'eventuale oggettoSeduta (questo perché il metodo di calcoloModelloTesto prima verifica la presenza di oggettoSeduta siccome è il comportamento "standard")
        delibera.modelloTesto = calcolaModelloTesto(delibera)
        delibera.save()

        // calcolo i soggetti della delibera.
        // il calcolo dei soggetti avviene qui per dare la possibilità alle funzioni scritte nella caratteristica di usare l'oggetto seduta (se impostato)
        def soggetti = caratteristicaTipologiaService.calcolaSoggetti(delibera, delibera.tipologiaDocumento.caratteristicaTipologiaDelibera)

        // assegno i vari soggetti calcolati
        for (def soggetto : soggetti) {
            delibera.setSoggetto(soggetto.key, soggetto.value.utente?.domainObject, soggetto.value.unita?.domainObject)
        }

        // aggiungo i soggetti dei firmatari "aggiuntivi" non previsti dalla commissione.
        // per ogni soggetto predisposto per la firma, verifico se è già presente come soggetto per la delibera. se non lo è, lo aggiungo come "FIRMATARIO".
        def firmatariAggiuntivi = getUtentiFirmatari(delibera)

        // conto i firmatari già presenti. Questo lo faccio giusto per "sicurezza": al 99,999% delle volte il risultato sarà zero perché è molto difficile che si vada a impostare un FIRMATARIO in caratteristica soggetti.
        // in ogni caso, se anche lo si facesse, con questa "count", considero anche quello.
        int countFirmatari = DeliberaSoggetto.countByDeliberaAndTipoSoggetto(delibera, TipoSoggetto.get(TipoSoggetto.FIRMATARIO))

        // per ogni firmatario segnato in commissione e/o seduta non previsto dalla caratteristica soggetti, creo un soggetto FIRMATARIO.
        // faccio questo per "censire" tutti i firmatari e poter fare dei controlli più accurati in fase di attivazione della delibera.
        for (Ad4Utente firmatario : firmatariAggiuntivi) {
            if (DeliberaSoggetto.countByUtenteAd4AndDelibera(firmatario, delibera) == 0) {
                delibera.setSoggetto(TipoSoggetto.FIRMATARIO, firmatario, null, countFirmatari)
                countFirmatari++
            }
        }

        delibera.save()

        // copio i movimenti contabili dalla proposta di delibera alla delibera
        integrazioneContabilita.copiaMovimentiContabili(delibera.proposta, delibera)

        if (delibera.propostaDelibera.tipologia.allegatoTestoProposta) {
            allegatoService.creaAllegatoTestoProposta(delibera)
        }

        // collego gli allegati anche sulla delibera e li imposto come non fossero mai stati firmati
        // da applicativo, in questo modo al rigenera delibera, posso ripristinare l'esatto file come firmato nella proposta.
        for (Allegato a : delibera.proposta.allegati) {
            if (a.statoFirma == StatoFirma.FIRMATO) {
                a.statoFirma = StatoFirma.DA_FIRMARE
            }

            if (delibera.propostaDelibera.tipologia.copiaTestoProposta){
                a.sequenza = a.sequenza + 1
            }

            // imposto ogni file allegato come se non fosse mai stato firmato da applicativo.
            List fileAllegatiOriginali = a.fileAllegati*.fileOriginale.findAll{it != null}
            if (fileAllegatiOriginali != null) {
                for (FileAllegato fileOriginale : fileAllegatiOriginali) {
                    fileOriginale.nome = "PROP_" + fileOriginale.nome
                    gestoreFile.updateFile(fileOriginale)
                    fileOriginale.save()
                }
            }
            a.fileAllegati*.fileOriginale = null
            delibera.addToAllegati(a)
            a.save()
        }

        // copio il testo della proposta di delibera se richiesto dalla tipologia:
        if (delibera.tipologiaDocumento.copiaTestoProposta) {
            copiaTestoProposta(delibera)
        }

        delibera.save()

        // adotto subito la delibera se proviene da una seduta e se l'impostazione lo richiede
        // serve soprattutto per le Provincie
        if (delibera.oggettoSeduta != null && Impostazioni.ODG_NUMERA_DELIBERE.abilitato) {
            adottaDelibera(delibera)
        }

        return delibera
    }

    void copiaTestoProposta (Delibera delibera) {
        // Non svuoto il modello testo della delibera perché ad esempio all'AREU questo viene poi utilizzato per
        // ricreare il testo della delibera con un mezzo accrocchio: la configurazione dice:
        // il testo della delibera è la copia del testo della proposta
        // la delibera però può essere rigenerata e il suo testo modificato rimanendo nel flusso della delibera.
        // a questo punto, per fare edita testo ho bisogno del modello per poter proseguire (con la nuova versione del plugin di gestione testi questo non servirà più)
        // inoltre, se faccio "elimina testo" ho bisogno di un modello da cui ripartire e quel modello sarà costituito dal solo tag del testo della proposta

        String nomeTestoDelibera = delibera.nomeFile + "." + delibera.propostaDelibera.testo.nome.substring(
                delibera.propostaDelibera.testo.nome.indexOf(".") + 1)

        FileAllegato fileAllegato = new FileAllegato(nome: nomeTestoDelibera)
        delibera.testo = fileAllegato

        gestioneTesti.copiaFileAllegato(delibera.propostaDelibera, delibera.propostaDelibera.testo, delibera, delibera.testo)

        // copio anche il testo modificabile della proposta (se presente):
        if (delibera.propostaDelibera.testoOdt != null) {
            String nomeTestoModificabile = delibera.nomeFile + "." + delibera.propostaDelibera.testoOdt.estensione
            FileAllegato testoModificabile = new FileAllegato(nome: nomeTestoModificabile)
            delibera.testoOdt = testoModificabile

            gestioneTesti.copiaFileAllegato(delibera.propostaDelibera, delibera.propostaDelibera.testoOdt, delibera, delibera.testoOdt)
        }
    }

    /**
     * Numera la delibera in base al registro e alla data di adozione.
     * Il registro usato è nell'ordine:
     * - il registro scritto in tipologia-delibera
     * - il registro scritto nella commissione della proposta di delibera
     *
     * @param delibera
     * @return
     */
    Delibera numeraDelibera (Delibera delibera) {
        if (delibera.numeroDelibera > 0) {
            return delibera
        }

        TipoRegistro tipoRegistro = delibera.proposta.oggettoSeduta?.esito?.registroDelibera ?: delibera.proposta.tipologia.tipoRegistroDelibera ?: delibera.proposta.commissione.tipoRegistro;

        // come data per recuperare il registro uso la data di adozione perché c'è la possibilità di numerare una delibera
        // nell'anno successivo alla discussione della stessa (ad esempio, in caso di ODG alla fine dell'anno).
        registroService.numera(tipoRegistro, delibera.dataAdozione, { numero, anno, data, registro ->
            delibera.numeroDelibera = numero
            delibera.annoDelibera = anno
            delibera.dataNumeroDelibera = data
            delibera.registroDelibera = registro.tipoRegistro
        })

        delibera.save()

        // informo la contabilità del numero della delibera
        integrazioneContabilita.salvaAtto(delibera)

        return delibera
    }

    /**
     * Attiva il flusso della delibera e sblocca quello della sua proposta.
     * @param delibera
     */
    void attivaDelibera (Delibera delibera) {
        // se una delibera ha già un iter associato, non faccio nulla
        if (delibera.iter != null) {
            return;
        }

        // aggiungo i firmatari alla coda per la firma
        aggiungiFirmatari(delibera)

        // verifico che i firmatari aggiunti siano dei soggetti presenti sulla delibera:
        verificaFirmatari(delibera)

        // se la delibera è passata dall'odg, allora imposto le varie cose necessarie
        if (delibera.oggettoSeduta != null) {

            // Imposto la proposta di delibera come conclusa e ne sblocco il flusso
            StatoOdg.concludiOdg(delibera.proposta);

            // elimino le competenze in scrittura dei soggetti in odg, lasciando solo quelle in lettura.
            gestoreCompetenze.rimuoviCompetenze(delibera, WkfTipoOggetto.get(Delibera.TIPO_OGGETTO),
                                                new Attore(ruoloAd4: delibera.oggettoSeduta.seduta.commissione.ruoloCompetenze), true, true, false,
                                                null)
            gestoreCompetenze.assegnaCompetenze(delibera, WkfTipoOggetto.get(Delibera.TIPO_OGGETTO),
                                                new Attore(ruoloAd4: delibera.oggettoSeduta.seduta.commissione.ruoloCompetenze), true, false, false,
                                                null)

            // sblocco la proposta che è ancora in attesa sull'odg.
            wkfIterService.sbloccaDocumento(delibera.proposta)
        }

        // ottengo il flusso da istanziare, la priorità è questa:
        // - se è presente nell'esito dell'oggetto seduta, uso quello
        // - altrimenti uso quello scritto in commissione
        // - altrimenti uso quello scritto in tipologia
        long progIter = ((delibera.oggettoSeduta?.esito?.progressivoCfgIter) ?: delibera.proposta.commissione?.progressivoCfgIter) ?: delibera.tipologiaDocumento.progressivoCfgIterDelibera;

        // attivo l'iter.
        wkfIterService.istanziaIter(WkfCfgIter.getIterIstanziabile(progIter).get(), delibera)
        delibera.save()
    }

    /**
     * Crea il testo della delibera
     *
     * @param delibera
     * @param modelloTesto
     */
    void creaTesto (Delibera delibera, GestioneTestiModello modelloTesto = null) {

        if (modelloTesto == null) {
            modelloTesto = calcolaModelloTesto(delibera);
        }

        if (modelloTesto == null) {
            throw new AttiRuntimeException(l("message.delibera.mancaModelloTestoTipologia", delibera.estremiAtto, delibera.tipologiaDocumento.titolo))
        }

        // associo il modello testo
        delibera.modelloTesto = modelloTesto;

        // se ho il testo docx della delibera, lo converto in odt
        // (questo è necessario per poter consentire a reporter di includere il testo della proposta nel testo della delibera).
        // #31150: eliminiamo la trasformazione in odt per problemi in caso di proposte firmate
        /*
        if (Impostazioni.FORMATO_DEFAULT.valore == GestioneTestiService.FORMATO_DOCX || Impostazioni.FORMATO_DEFAULT.valore == GestioneTestiService.FORMATO_DOC) {
            gestioneTesti.salvaTestoOdt(delibera.propostaDelibera)
        }
        */
        // genero il testo della delibera
        gestioneTesti.generaTestoDocumento(delibera, true)

        // se richiesto dall'esito della seduta, allora converto subito in pdf.
        // nota che non è detto che ci sia l'oggetto seduta a causa delle delibere che non passano da ODG.
        if (delibera.oggettoSeduta?.esito?.testoAutomatico) {

            // salvo il testo originale prima
            gestioneTesti.salvaTestoOdt(delibera)

            // poi lo converto in pdf.
            gestioneTesti.convertiTestoPdf(delibera)
        }
    }

    /**
     * Calcola quale modello testo usare per la delibera.
     * Questo metodo si è reso necessario da quando le delibere possono esistere senza commissione.
     *
     * Il modello della delibera può essere specificato in tipologia o in commissione.
     * Il testo in commissione ha comunque la precedenza, quindi se è stata specificata una commissione, allora cerco il modello lì,
     * se non trovo nessun modello usabile, allora uso quello scritto in tipologia. Ma se anche questo non è presente, allora do' errore.
     *
     * @param delibera
     * @return
     */
    GestioneTestiModello calcolaModelloTesto (Delibera delibera) {

        // se ho la commissione, cerco il modello al suo interno:
        if (delibera.oggettoSeduta?.seduta?.commissione != null) {

            GestioneTestiModello modelloTesto = delibera.oggettoSeduta.seduta.commissione.stampe.find {
                it.codice == CommissioneStampa.DELIBERA && it.modelloTesto.tipoModello.codice.startsWith("DELIBERA")
            }?.modelloTesto

            if (modelloTesto == null) {
                throw new AttiRuntimeException(l("message.delibera.mancaModelloTestoCommissione", delibera.tipologiaDocumento.titolo,
                                                 delibera.oggettoSeduta.seduta.commissione.titolo))
            }

            return modelloTesto
        }

        if (delibera.tipologiaDocumento.modelloTestoDelibera == null) {
            throw new AttiRuntimeException(l("message.delibera.mancaModelloTestoTipologiaCommissione", delibera.tipologiaDocumento.titolo))
        }

        return delibera.tipologiaDocumento.modelloTestoDelibera
    }

    List<GestioneTestiModelloDTO> getListaModelliTestoAbilitati (def utente) {
        return GestioneTestiModelloCompetenza.createCriteria().list {
            projections {
                gestioneTestiModello {
                    groupProperty("id")
                    groupProperty("nome")
                    groupProperty("descrizione")
                }
            }

            gestioneTestiModello {
                like('tipoModello.codice', Delibera.TIPO_OGGETTO + "%")
                eq("valido", true)
            }

            AttiGestoreCompetenze.controllaCompetenze(delegate)(utente)

            fetchMode("gestioneTestiModello", FetchMode.JOIN)

            gestioneTestiModello {
                order("nome", "asc")
            }

        }.collect { row -> new GestioneTestiModelloDTO(id: row[0], nome: row[1], descrizione: row[2]) }
    }

    void aggiungiFirmatari (Delibera delibera) {

        def commissione = delibera.proposta.commissione ?: delibera.tipologiaDocumento.commissione;
        // se non ho l'oggetto seduta, significa che la delibera non è passata o "ancora" passata dall'odg.
        // Aggiungo quindi i firmatari come scritti in commissione.
        if (delibera.oggettoSeduta == null) {
            // se non ho la commissione, significa che aggiungerò i firmatari mano a mano nel flusso e non qui.
            if (commissione == null) {
                return;
            }

            // se invece ho la commissione, allora preparo i firmatari nell'ordine richiesto
            def firmatari = CommissioneComponente.findAllByCommissioneAndValidoAndFirmatario(commissione, true, true,
                                                                                             [sort: 'sequenzaFirma', order: 'asc'])
            for (CommissioneComponente firmatario : firmatari) {
                attiFirmaService.addFirmatario(delibera, firmatario.componente.utenteAd4);
            }
            return;
        }

        if (!isNumeroFirmatariUguale(delibera) && commissione.controlloFirmatari) {
            throw new AttiRuntimeException(
                    "Attenzione: il numero dei firmatari non è corretto: il numero di firmatari previsto dalla commissione è diverso dal numero dei firmatari specificato")
        }

        // verifico la sequenza dei firmatari
        List<OggettoPartecipante> firmatari = getListaFirmatariOggetto(delibera.oggettoSeduta.id)
        long indiceSequenza = 0
        for (OggettoPartecipante f : firmatari) {
            if (f.sequenzaFirma > indiceSequenza) {
                indiceSequenza = f.sequenzaFirma
            } else {
                throw new AttiRuntimeException("Attenzione: ci sono due firmatari con la stessa sequenza di firma: ${f.sequenzaFirma}.");
            }
        }

        for (OggettoPartecipante f : firmatari) {
            Ad4Utente utenteFirmatario = f.sedutaPartecipante.componenteEsterno?.utenteAd4 ?: f.sedutaPartecipante.commissioneComponente?.componente?.utenteAd4;
            if (utenteFirmatario == null) {
                throw new AttiRuntimeException(
                        "Attenzione: il firmatario con sequenza ${f.sequenzaFirma} non ha un utente collegato pertanto non può essere un firmatario.")
            }
            attiFirmaService.addFirmatario(delibera, utenteFirmatario);
        }
    }

    void aggiungiFirmatariDelibera (Delibera delibera) {

        def commissione = delibera.proposta.commissione ?: delibera.tipologiaDocumento.commissione;
        // Aggiungo i firmatari nell'ordine presente in commissione.
        // se non ho la commissione, significa che aggiungerò i firmatari mano a mano nel flusso e non qui.
        if (commissione == null) {
            return;
        }

        // se invece ho la commissione, allora preparo i firmatari nell'ordine richiesto
        def firmatari = CommissioneComponente.findAllByCommissioneAndValidoAndFirmatario(commissione, true, true,
                                                                                         [sort: 'sequenzaFirma', order: 'asc'])
        for (CommissioneComponente componente : firmatari) {
            DeliberaSoggetto firmatario = delibera.soggetti.find {
                it.tipoSoggetto.codice == getTipoSoggettoComponente(componente.ruoloPartecipante.codice)
            }
            if (firmatario == null) {
                throw new AttiRuntimeException(
                        "Attenzione: il firmatario con ruolo ${componente.ruoloPartecipante.descrizione} non è presente come soggetto della delibera e pertanto non può essere un firmatario.")
            }
            attiFirmaService.addFirmatario(delibera, firmatario.utenteAd4);
        }
    }

    /**
     * Elimina tutti i firmatari della delibera e aggiunge i firmatari nell'ordine presente in commissione
     * @param delibera
     */
    void ricalcolaFirmatariDelibera (Delibera delibera) {
        attiFirmaService.eliminaFirmatari(delibera, true)
        aggiungiFirmatariDelibera(delibera)
    }


    /**
     * Ritorna un elenco di utenti che dovranno firmare, prendendoli dall'oggetto seduta, se presente o dalla commissione.
     *
     * @param delibera
     * @return
     */
    def getUtentiFirmatari (Delibera delibera) {
        // se la delibera ha un oggetto seduta, prendo i firmatari da lì.
        if (delibera.oggettoSeduta != null) {
            def utentiFirmatari = []
            def partecipanti = getListaFirmatariOggetto(delibera.oggettoSeduta)

            for (OggettoPartecipante p : partecipanti) {
                if (p.sedutaPartecipante.componenteEsterno != null) {
                    if (p.sedutaPartecipante.componenteEsterno.utenteAd4 == null) {
                        throw new AttiRuntimeException(
                                "Non è possibile aggiungere il soggetto '${p.sedutaPartecipante.componenteEsterno.denominazione}' come firmatario perché non ha un utente ad4 collegato.")
                    }

                    utentiFirmatari << p.sedutaPartecipante.componenteEsterno.utenteAd4;
                }

                if (p.sedutaPartecipante.commissioneComponente != null) {
                    if (p.sedutaPartecipante.commissioneComponente.componente.utenteAd4 == null) {
                        throw new AttiRuntimeException(
                                "Non è possibile aggiungere il soggetto '${p.sedutaPartecipante.commissioneComponente.componente.denominazione}' come firmatario perché non ha un utente ad4 collegato.")
                    }

                    utentiFirmatari << p.sedutaPartecipante.commissioneComponente.componente.utenteAd4;
                }
            }

            return utentiFirmatari;
        }

        // se la delibera non ha oggetto seduta, recupero i soggetti dalla commissione
        def commissione = delibera.proposta.commissione ?: delibera.tipologiaDocumento.commissione;

        // se non ho la commissione, significa che aggiungerò i firmatari mano a mano nel flusso e non qui.
        if (commissione == null) {
            return [];
        }

        // se invece ho la commissione, allora preparo i firmatari nell'ordine richiesto
        def firmatari = CommissioneComponente.findAllByCommissioneAndValidoAndFirmatario(commissione, true, true,
                                                                                         [sort: 'sequenzaFirma', order: 'asc'])
        def utentiFirmatari = []
        for (CommissioneComponente firmatario : firmatari) {
            if (firmatario.componente.utenteAd4 == null) {
                throw new AttiRuntimeException(
                        "Non è possibile aggiungere il soggetto '${firmatario.componente}' come firmatario perché non ha un utente ad4 collegato.")
            }

            utentiFirmatari << firmatario.componente.utenteAd4;
        }

        return utentiFirmatari;
    }

    // Questo controllo non ha più senso ahimé...
    void verificaFirmatari (Delibera delibera) {
        // per ogni firmatario della delibera, verifico che sia presente un soggetto con lo stesso utente:
        for (Firmatario f : delibera.firmatari) {
            if (DeliberaSoggetto.countByUtenteAd4AndDelibera(f.firmatario, delibera) == 0) {
                throw new AttiRuntimeException(
                        "Non è possibile proseguire: c'è discordanza tra i soggetti e i firmatari sull'atto ${delibera.numeroDelibera} / ${delibera.annoDelibera}. Il Firmatario ${f.firmatario.nominativo} non è presente tra i soggetti dell'atto.")
            }
        }
    }

    List<OggettoPartecipante> getListaFirmatariOggetto (long idOggettoSeduta) {
        return getListaFirmatariOggetto(OggettoSeduta.get(idOggettoSeduta))
    }

    List<OggettoPartecipante> getListaFirmatariOggetto (OggettoSeduta oggettoSeduta) {
        return OggettoPartecipante.findAllByOggettoSedutaAndFirmatarioAndPresente(oggettoSeduta, true, true, [sort: 'sequenzaFirma', order: 'asc'])
    }

    /**
     * Annulla la delibera
     */
    Delibera annullaDelibera (Delibera delibera, Delibera deliberaPrincipale) {
        delibera.stato = StatoDocumento.ANNULLATO
        // termino l'iter della delibera e dei suoi documenti collegati.

        if (delibera.iter != null) {
            wkfIterService.terminaIter(delibera.iter)
        }

        for (VistoParere v : delibera.visti) {
            if (v.iter != null) {
                wkfIterService.terminaIter(v.iter)
            }
        }

        for (Certificato c : delibera.certificati) {
            if (c.iter != null) {
                wkfIterService.terminaIter(c.iter)
            }
        }

        delibera.save()

        if (!Impostazioni.INTEGRAZIONE_ALBO.isDisabilitato()) {
            integrazioneAlbo.annullaAtto(delibera, deliberaPrincipale)
        }

        if (Impostazioni.INTEGRAZIONE_GDM.abilitato) {
            gestoreDocumentaleEsterno.salvaDocumento(delibera)
        }

        if (Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato) {
            casaDiVetroService.elimina(delibera);
        }

        // Elimino i movimenti della proposta sulla contabilità solo se la delibera non è esecutiva.
        if (Impostazioni.CONTABILITA.valore == "integrazioneContabilitaCe4") {
            integrazioneContabilita.annullaProposta(delibera.propostaDelibera)
        } else if (delibera.eseguibilitaImmediata || delibera.dataEsecutivita == null) {
            integrazioneContabilita.annullaProposta(delibera.propostaDelibera)
        }

        // elimino tutte le notifiche di cambio step
        notificheService.eliminaNotifiche(delibera, TipoNotifica.ASSEGNAZIONE)
        // elimino tutte le "altre" notifiche
        notificheService.eliminaNotifiche(delibera)

        // notifico l'annullamento della delibera
        notificheService.notifica(TipoNotifica.ATTO_ANNULLATO, delibera)
    }

    boolean numeroFirmatariDaVerificare (Delibera delibera) {
        def commissione = delibera.proposta.commissione ?: delibera.tipologiaDocumento.commissione;
        if (!isNumeroFirmatariUguale(delibera) && !commissione?.controlloFirmatari) {
            return true;
        }
        return false;
    }

    private boolean isNumeroFirmatariUguale (Delibera delibera) {
        if (delibera.oggettoSeduta != null) {
            // conto i partecipanti firmatari:
            long nFirmatari = OggettoPartecipante.countByOggettoSedutaAndPresenteAndFirmatario(delibera.oggettoSeduta, true, true)

            // conto i firmatari commissione
            long nFirmatariCommissione = CommissioneComponente.countByCommissioneAndValidoAndFirmatario(delibera.oggettoSeduta.seduta.commissione,
                                                                                                        true, true)
            if (nFirmatari != nFirmatariCommissione) {
                return false;
            }
        }
        return true;
    }

    /**
     * Recupera la lista di determine da pubblicare
     */
    List<Delibera> getDeliberaDaPubblicare () {
        return Delibera.createCriteria().list {
            fetchMode("propostaDelibera", FetchMode.JOIN)
            isNull("dataPubblicazione")
            eq("daPubblicare", true)
            propostaDelibera {
                le("dataMinimaPubblicazione", new Date())
            }

        }
    }

    private String getTipoSoggettoComponente (String codice) {
        switch (codice) {
            case RuoloPartecipante.CODICE_PRESIDENTE:
                return TipoSoggetto.PRESIDENTE;
                break;
            case RuoloPartecipante.CODICE_SEGRETARIO:
                return TipoSoggetto.SEGRETARIO;
                break;
            default: return codice;
        }
    }
}
