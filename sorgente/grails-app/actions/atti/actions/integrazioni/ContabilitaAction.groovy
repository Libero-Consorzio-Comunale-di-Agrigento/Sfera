package atti.actions.integrazioni

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.dto.documenti.AllegatoDTOService
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioni.CasaDiVetroService
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaAscotWeb
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaCfa
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaComuneModena
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaSfera
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.zkutils.SuccessHandler
import org.apache.commons.io.FileUtils
import org.zkoss.zhtml.Filedownload
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox

import java.lang.reflect.Proxy
import static it.finmatica.zkutils.LabelUtils.getLabel as l

class ContabilitaAction {

    // services
    IntegrazioneContabilitaComuneModena integrazioneContabilitaComuneModena
    IntegrazioneContabilita             integrazioneContabilita
    SpringSecurityService               springSecurityService
    GestioneTestiService                gestioneTestiService
    AllegatoDTOService                  allegatoDTOService
    AllegatoService                     allegatoService
    CasaDiVetroService                  casaDiVetroService
    SuccessHandler                      successHandler
    AttiGestioneTesti                   gestioneTesti

    // beans
    IGestoreFile          gestoreFile
    AttiGestoreCompetenze gestoreCompetenze

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Contabilità: Salva la Proposta in contabilità",
            descrizione = "Salva la Proposta sulla contabilità solo se la proposta è numerata.")
    IDocumentoIterabile aggiornaProposta (IProposta documentoIterabile) {
        if (documentoIterabile.numeroProposta > 0) {
            integrazioneContabilita.salvaProposta(documentoIterabile)
        }

        return documentoIterabile
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "Contabilità: Aggiorna i movimenti contabili",
            descrizione = "Aggiorna i movimenti contabili su sfera.")
    IDocumentoIterabile aggiornaMovimentiContabili (def doc) {
        integrazioneContabilita.aggiornaMovimentiContabili(doc)
        return doc
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "Contabilità Sfera: Controlla i dati dei movimenti contabili",
            descrizione = "Interrompe il processo se i movimenti contabili associati non hanno i campi di impegno o accertamento completi.")
    IDocumentoIterabile controllaDatiMovimentiContabili (def doc) {

        def contabilitaSfera = Proxy.getInvocationHandler(integrazioneContabilita).getTargetBean();
        if (contabilitaSfera instanceof IntegrazioneContabilitaSfera) {
            def movimenti = contabilitaSfera.getMovimentiContabili(doc);
            for (MovimentoContabile m : movimenti) {
                if ((m.annoImpegno > 0 && m.numeroImpegno > 0) ||
                        (m.annoAccertamento > 0 && m.numeroAccertamento > 0)) {
                    continue;
                } else {
                    throw new AttiRuntimeException(l("message.contabilita.erroreDatiMovimentiContabili"));
                }
            }
        }

        return doc
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "CFA: Controlla che l'atto non abbia liquidazioni senza mandati",
            descrizione = "Interrompe il processo se esistono liquidazioni senza mandati.")
    IDocumentoIterabile controllaMandatiObbligatori (def doc) {
        if (!isLiquidazioneInteramentePagata(doc)) {
            throw new AttiRuntimeException(l("message.contabilita.erroreMandatiObbligatori"))
        }

        return doc
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "CFA: la liquidazione è interamente pagata?",
            descrizione = "Ritorna TRUE se la liquidazione è interamente pagata su CFA. FALSE altrimenti")
    boolean isLiquidazioneInteramentePagata (def doc) {
        def contabilitaCfa = Proxy.getInvocationHandler(integrazioneContabilita).getTargetBean();
        if (contabilitaCfa instanceof IntegrazioneContabilitaCfa) {
            return (contabilitaCfa.isLiquidazioneInteramentePagata(doc))
        }
        return false
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "CFA: non esistono liquidazioni emesse?",
            descrizione = "Ritorna TRUE se non sono presenti liquidazioni contabili emesse su CFA. FALSE altrimenti")
    boolean esistonoLiquidazioniDistinta (def doc) {
        def contabilitaCfa = Proxy.getInvocationHandler(integrazioneContabilita).getTargetBean();
        if (contabilitaCfa instanceof IntegrazioneContabilitaCfa) {
            return (!contabilitaCfa.esistonoLiquidazioniDistinta(doc))
        }
        return false
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "CFA: imposta lo stato della distinta come Inclusa in Atto [25]",
            descrizione = "Imposta lo stato della distinta come Inclusa in Atto [25].")
    IDocumentoIterabile impostaStatoDistintaInclusaInAtto (IProposta documentoIterabile) {
        def contabilitaCfa = Proxy.getInvocationHandler(integrazioneContabilita).getTargetBean();
        if (contabilitaCfa instanceof IntegrazioneContabilitaCfa) {
            if (!contabilitaCfa.esistonoLiquidazioniDistinta(documentoIterabile)){
                contabilitaCfa.impostaStatoDistintaInclusaInAtto(documentoIterabile)
            }
            else {
                throw new AttiRuntimeException(l("message.contabilita.erroreImpostaStatoInclusa"))

            }
        }
        return documentoIterabile
    }
    @Action(tipo = TipoAzione.CLIENT,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "CFA: Apre link Contabilità",
            descrizione = "Apre la pagina di emissione delle liquidazioni di CFA.")
    void apriUrlContabilita (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {

        def contabilitaCfa = Proxy.getInvocationHandler(integrazioneContabilita).getTargetBean();
        if (contabilitaCfa instanceof IntegrazioneContabilitaCfa) {
            String url = contabilitaCfa.getUrlControlloContabileDistintaLiquidazione(
                    viewModel.getDocumentoIterabile(false));

            // salto l'invalidate della maschera:
            successHandler.saltaInvalidate();

            // apro l'url
            Executions.getCurrent().sendRedirect(url, "_blank");
        }
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Contabilità: Annulla i Movimenti contabili della Proposta",
            descrizione = "Annulla gli eventuali documenti contabili della Proposta sulla contabilità (solo se la proposta è numerata).")
    IDocumentoIterabile annullaMovimentiProposta (IProposta documentoIterabile) {
        if (documentoIterabile.numeroProposta > 0) {
            integrazioneContabilita.annullaProposta(documentoIterabile)
        }

        return documentoIterabile
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "Contabilità: Salva l'atto in contabilità",
            descrizione = "Salva l'atto in contabilità")
    IDocumentoIterabile creaAggiornaAtto (IDocumentoIterabile documentoIterabile) {
        integrazioneContabilita.salvaAtto(documentoIterabile)
        return documentoIterabile
    }

	@Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
                nome = "Contabilità: Rende esecutivo l'atto in contabilità",
            descrizione = "Rende esecutivi i documenti contabili associati sulla contabilità")
    IDocumentoIterabile esecutivitaAtto (IDocumentoIterabile documentoIterabile) {
        integrazioneContabilita.rendiEsecutivoAtto(documentoIterabile)
        return documentoIterabile
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
            nome = "Contabilità: Crea la scheda Contabile",
            descrizione = "Recupero della stampa dei documenti contabili associati all'atto sulla contabilità.")
    IDocumentoIterabile creaSchedaContabile (IDocumentoIterabile documentoIterabile) {
        allegatoService.creaAllegatoSchedaContabile(documentoIterabile)
        return documentoIterabile
    }

    @Action(tipo = TipoAzione.CLIENT,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Contabilità: Mostra popup di conferma in caso non ci siano dati contabili",
            descrizione = "Verifica la presenza dei dati contabili e mostra popup di conferma in caso questi non siano presenti")
    void controllaDatiContabiliPresenti (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
        IDocumento doc = viewModel.getDocumentoIterabile(false)

        if (integrazioneContabilita.isAbilitata(doc) && !integrazioneContabilita.isConDocumentiContabili(doc)) {
            Messagebox.show(
                    "Attenzione: non sono presenti i dati contabili sul documento. Si è sicuri di voler proseguire?",
                    "Attenzione: non sono presenti i dati contabili",
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                    new org.zkoss.zk.ui.event.EventListener() {
                        void onEvent (Event e) {
                            if (Messagebox.ON_OK.equals(e.getName())) {
                                viewModel.eseguiPulsante(idCfgPulsante, idAzioneClient)
                            }
                        }
                    }
            )
        } else {
            viewModel.eseguiPulsante(idCfgPulsante, idAzioneClient);
        }
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Contabilità Modena: Invia i movimenti contabili al SIB",
            descrizione = "Invia i movimenti contabili al SIB. Blocca l'esecuzione se almeno un invio ha dato errore.")
    IDocumentoIterabile inviaMovimentiContabili (IProposta proposta) {
        integrazioneContabilitaComuneModena.inviaMovimentiContabili(proposta)
        String errori = integrazioneContabilitaComuneModena.getErroriMovimentiContabili(proposta)
        if (errori.length() > 0) {
            throw new AttiRuntimeException(
                    "Si sono verificati degli errori nell'inviare i movimenti contabili al SIB. Gli errori sono:\n${errori}")
        }
        return proposta
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "Contabilità: verifica che sia presente un allegato scheda contabile",
            descrizione = "Ritorna TRUE se è presente un allegato scheda contabile")
    boolean isSchedaContabilePresente (def doc) {
        return (allegatoService.getAllegato(doc, Allegato.ALLEGATO_SCHEDA_CONTABILE) != null)
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "Contabilità: verifica che non sia presente un allegato scheda contabile",
            descrizione = "Ritorna TRUE se NON è presente un allegato scheda contabile")
    boolean isSchedaContabileAssente (IDocumento doc) {
        return !isSchedaContabilePresente(doc)
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
            nome = "Contabilità: Crea la scheda Contabile senza salvarla",
            descrizione = "Recupero della stampa dei documenti contabili associati all'atto sulla contabilità senza effettuare il salvataggio.")
    void creaSchedaContabileScaricabile (IDocumento documentoIterabile) {
        // FIXME: il metodo integrazioneContabilit.getSchedaContabile potrebbe ritornare un pdf o un odt. Va cambiato il valore di ritorno.
        InputStream stampaSchedaContabile = integrazioneContabilita.getSchedaContabile(documentoIterabile)
        if (stampaSchedaContabile == null) {
            throw new AttiRuntimeException("Impossibile creare la scheda contabile, modello testo non trovato");
        }
        File tempFile = File.createTempFile("SchedaContabile", "tmp");
        try  {
            FileUtils.copyInputStreamToFile(stampaSchedaContabile, tempFile)
            if (!gestioneTesti.isPdf(tempFile)){
                stampaSchedaContabile = FileUtils.openInputStream(tempFile)
                stampaSchedaContabile = gestioneTesti.convertiStreamInPdf(stampaSchedaContabile, tempFile.name, documentoIterabile)
            }
            else {
                stampaSchedaContabile = FileUtils.openInputStream(tempFile)
            }
            Filedownload.save(stampaSchedaContabile, GestioneTestiService.getContentType(GestioneTestiService.FORMATO_PDF),
                    "SchedaContabile.pdf");
        } finally {
            tempFile.delete()
        }
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Verifica che tutti i movimenti contabili abbiano un cig",
            descrizione = "Verifica che tutti i movimenti contabili abbiano un cig. Blocca l'esecuzione se almeno un movimento ne è privo.")
    IDocumentoIterabile controllaCigObbligatorio (IProposta proposta) {
        //Il controllo va fatto in base al flag degli oggetti ricorrenti (se l'impostazione prevede la gestione degli oggetti ricorrenti)
        Boolean flag = proposta.oggettoRicorrente?.cigObbligatorio?:false

        if (flag) {
            if (!integrazioneContabilita.isMovimentiCigCompleti(proposta)) {
                throw new AttiRuntimeException(l("message.contabilita.erroreCigObbligatorio"));
            }
        }
        return proposta
   }


    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Verifica che tutti i movimenti contabili abbiano un cig associato ad un contratto in Casa di Vetro",
            descrizione = "Verifica che tutti i movimenti contabili abbiano un cig associato ad un contratto in Casa di Vetro. Blocca l'esecuzione in caso contrario.")
    IDocumentoIterabile controllaContratti (IProposta proposta) {
        def movimenti = integrazioneContabilita.getMovimentiContabili(proposta)

        for (def movimento : movimenti) {
            if (movimento.cig != null && !movimento.codiceTipo.startsWith("VAR_") && !casaDiVetroService.esisteContratto(movimento.cig, proposta.numeroProposta, proposta.annoProposta)) {
                throw new AttiRuntimeException(l("message.contabilita.erroreCigCasaDiVetro"))
            }
        }
        return proposta
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Verifica tutti i contratti in Casa di Vetro abbiano la data inizio valorizzata",
            descrizione = "Verifica tutti i contratti in Casa di Vetro abbiano la data inizio valorizzata")
    IDocumentoIterabile controllaDataInizioLavoriContratti (IProposta proposta) {
        def movimenti = integrazioneContabilita.getMovimentiContabili(proposta)

        for (def movimento : movimenti) {
            //FIXME: Al momento il controllo NON funziona se manca il contratto in casa di vetro (va bene per la partenza, ma andrà sistemato quando verrà importato il pregresso in CDV
            if (movimento.cig != null && !movimento.codiceTipo.startsWith("VAR_") && !movimento.cig.equals("0000000000") && casaDiVetroService.esisteContrattoSenzaDataInizio(movimento.cig, proposta.numeroProposta, proposta.annoProposta)) {
                throw new AttiRuntimeException(l("message.contabilita.erroreContrattoSenzaDataInizio"))
            }
        }
        return proposta
    }


    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Verifica tutti i CIG di una proposta con un oggetto ricorrente di tipo AFFIDAMENTO IN HOUSE",
            descrizione = "Verifica che tutti i CIG di una proposta con un oggetto ricorrente di tipo AFFIDAMENTO IN HOUSE siano valorizzati a 0000000000")
    IDocumentoIterabile controllaCigAffidamento(IProposta proposta) {
        controllaCigObbligatorio(proposta)

        def movimenti = integrazioneContabilita.getMovimentiContabili(proposta)

        for (def movimento : movimenti) {
            if (movimento.cig != null && !movimento.cig.equals("0000000000") && proposta.oggettoRicorrente?.tipo?.equals(OggettoRicorrente.TIPO_AFFIDAMENTO_IN_HOUSE)) {
                throw new AttiRuntimeException(l("message.contabilita.erroreCigAffidamento"))
            }
        }
        return proposta
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
            nome = "Contabilità: Inserisce la proposta (Contabilita AscotWeb)",
            descrizione = "Contabilità: Inserisce la proposta (Contabilita AscotWeb)")
    IDocumentoIterabile inserisciPropostaAscot(IDocumentoIterabile documentoIterabile) {
        def contabilitaAscotWeb = Proxy.getInvocationHandler(integrazioneContabilita).getTargetBean();
        if (contabilitaAscotWeb instanceof IntegrazioneContabilitaAscotWeb) {
            contabilitaAscotWeb.inserisciProposta(documentoIterabile)
        }
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
            nome = "Contabilità: Rende esecutivo l'atto in contabilità (Contabilita AscotWeb)",
            descrizione = "Contabilità: Rende esecutivo l'atto in contabilità (Contabilita AscotWeb)")
    IDocumentoIterabile rendiEsecutivoAtto(IDocumentoIterabile documentoIterabile) {
        def contabilitaAscotWeb = Proxy.getInvocationHandler(integrazioneContabilita).getTargetBean();
        if (contabilitaAscotWeb instanceof IntegrazioneContabilitaAscotWeb) {
            def documento = documentoIterabile
            if (documento instanceof IDocumentoCollegato) {
                documento = documento.documentoPrincipale
            }
            contabilitaAscotWeb.esecutivitaAtto(documento)
        }
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
            nome = "Contabilità: Adotta atto in contabilità (Contabilita AscotWeb)",
            descrizione = "Contabilità: Adotta atto l'atto in contabilità (Contabilita AscotWeb)")
    IDocumentoIterabile adottaAtto(IDocumentoIterabile documentoIterabile) {
        def contabilitaAscotWeb = Proxy.getInvocationHandler(integrazioneContabilita).getTargetBean();
        if (contabilitaAscotWeb instanceof IntegrazioneContabilitaAscotWeb) {
            def documento = documentoIterabile
            if (documento instanceof IDocumentoCollegato) {
                documento = documento.documentoPrincipale
            }
            contabilitaAscotWeb.adozioneAtto(documento)
        }
    }
}
