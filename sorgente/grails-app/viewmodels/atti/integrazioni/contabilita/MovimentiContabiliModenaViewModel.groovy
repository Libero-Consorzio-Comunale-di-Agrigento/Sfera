package atti.integrazioni.contabilita

import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.documenti.DocumentoFactory
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaComuneModena
import it.finmatica.atti.integrazioniws.comunemodena.contabilita.*
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class MovimentiContabiliModenaViewModel {

	IntegrazioneContabilitaComuneModena integrazioneContabilitaComuneModena

    // dati maschera
	def listaMovimenti
	boolean competenzaInModifica = false
    List<RecordVoceEconomica> listaVociEconomiche
    List<RecordSiope> listaCodiciSiope
	List<RecordPdc> listaPianoDeiConti
    List<String> listaCodiciIdEuropei
	List<?> listaCodiciStatistici
	def listaTipiCodiciStatistici
	MovimentoContabile movimento
    RecordCapitolo capitolo

    // dati di stato
	private long   idDocumento
	private String tipoDocumento
    private String codiceEnte
    private String codiceUoProponente

	// componenti
	@Wire ("#popupModificaMovimentoContabile")
	Window popupModificaMovimentoContabile

    @Wire ("#popupRicercaSoggetti")
    Window popupRicercaSoggetti

    // dati per la popup di ricerca soggetti
    List<RecordSoggetti> listaSoggetti
    RecordSoggetti soggetto
    String denominazioneSoggetto = ""
    String localitaSoggetto = ""
    String codiceSoggetto   = ""
    String codiceFiscale 	= ""
    String partitaIva    	= ""
    int activePage          = 0
    int pageSize            = 30
    int totalSize           = 0

	/*
	 * Funzioni per pagina "master"
	 */

	@Init init () {
		movimento = new MovimentoContabile(tipo:MovimentoContabile.TIPO_USCITA, annoEsercizio: Calendar.getInstance().get(Calendar.YEAR), numero: 0)

        // al massimo si possono impostare 5 codici statistici, predispongo le liste per la selezione
        listaCodiciStatistici = [[], [], [], [], []]

        // dati di test
        codiceEnte 			= "FINMATICA"
        codiceUoProponente 	= "A_PRES"

        listaTipiCodiciStatistici = integrazioneContabilitaComuneModena.getTipiCodiciStatistici()
	}

	@AfterCompose
	void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
	}

	private void aggiornaMovimentiContabili () {
		aggiornaMovimentiContabili(DocumentoFactory.getDocumento(idDocumento, tipoDocumento))
	}

	private void aggiornaMovimentiContabili (IDocumento atto) {
		listaMovimenti = integrazioneContabilitaComuneModena.getMovimentiContabili(atto);

		BindUtils.postNotifyChange ("movimentiContabiliQueue", null, this, "listaMovimenti");
	}

	@NotifyChange(["movimento", "listaMovimenti"])
	@Command onAggiungiMovimentoContabile () {
		movimento = new MovimentoContabile();
		// imposto come default il valore di spesa.
		// questo lo faccio per forzare l'aggiornamento del campo "tipo" di cui "entrata" è un alias.
		// se non lo facessi e l'utente da interfaccia non scegliesse nulla, allora il campo "tipo" avrebbe ancora valore null.
		// in questo modo invece lo forzo ad avere il campo "USCITA" di default.
		movimento.entrata = false;
		movimento.discard();
		popupModificaMovimentoContabile.doModal();
	}

	@NotifyChange(["movimento", "listaMovimenti"])
	@Command onModificaMovimentoContabile (@BindingParam("idMovimentoContabile") long idMovimentoContabile) {
		movimento = MovimentoContabile.get(idMovimentoContabile)
		movimento.discard()
        if (movimento.codiceSoggetto != null) {
            soggetto = integrazioneContabilitaComuneModena.getSoggetto (codiceEnte, codiceUoProponente, movimento.codiceSoggetto)
        }
        onCaricaCapitolo()
		popupModificaMovimentoContabile.doModal()
    }

	@Command onEliminaMovimentoContabile (@BindingParam("idMovimentoContabile") long idMovimentoContabile) {
		Messagebox.show("Eliminare il movimento contabile selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					void onEvent (Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							MovimentoContabile.get(idMovimentoContabile).delete ();
							MovimentiContabiliModenaViewModel.this.aggiornaMovimentiContabili();
						}
					}
				})
	}

	/*
	 * Funzione per Interfaccia IntegrazioneContabilita implementata da IntegrazioneContabilitaComuneModena
	 */
	@GlobalCommand("aggiornaAtto")
	@NotifyChange(["competenzaInModifica", "listaMovimenti"])
	void aggiornaAtto(@BindingParam("atto") IDocumento atto, @BindingParam("competenza") String competenza) {
		idDocumento 	    = atto.id
		tipoDocumento 	    = atto.TIPO_OGGETTO
        codiceEnte          = atto.ente.codice
        codiceUoProponente  = atto.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.codice

		this.competenzaInModifica 	= "W".equals (competenza);
		aggiornaMovimentiContabili(atto)
	}

	/*
	 * Funzioni per la popup del Movimento Contabile.
	 */

    @NotifyChange(["listaPianoDeiConti", "capitolo", "vociMinisteriali", "movimento"])
    @Command onCaricaCapitolo () {
        capitolo = integrazioneContabilitaComuneModena.getDettagliCapitolo (codiceEnte, codiceUoProponente, movimento.annoEsercizio, movimento.capitolo, movimento.articolo, movimento.numero)
		if (!(capitolo.liv5Pf?.trim()?.length() > 0)) {
			listaPianoDeiConti = integrazioneContabilitaComuneModena.getListaPianoDeiConti(codiceEnte, codiceUoProponente, capitolo, movimento.tipo)
		} else {
			listaPianoDeiConti = null
		}

        // TODO: da gestire il campo codIdEuropeo:
        // - va aggiunto alla tabella MOVIMENTI_CONTABILI
        // - va gestito il flag che indica se è modificabile o no.
		if (!(capitolo.codIdEuropeo?.trim()?.length() > 0)) {
            // se il campo non è presente allora lo faccio scegliere all'utente
            listaCodiciIdEuropei = integrazioneContabilitaComuneModena.getListaCodiciIdEuropei()
        }

        // TODO: da gestire il campo voceEconmica
        // - va aggiunto alla tabella MOVIMENTI_CONTABILI
        // - bisogna chiarire se questo valore è presente nel capitolo, non è modificabile dall'utente. Forse il comportamento è uguale al SIOPE ?
		if (!(capitolo.voceEconomica?.trim()?.length() > 0)) {
            // se il campo non è presente allora lo faccio scegliere all'utente
            listaVociEconomiche = integrazioneContabilitaComuneModena.getListaVociEconomiche (codiceEnte, codiceUoProponente, movimento.tipo, capitolo)
        }

		if (!(capitolo.siope?.trim()?.length() > 0)) {
            // se il campo non è presente, allora lo faccio scegliere all'utente
            // TODO: il campo capitolo.voceEconomica in realtà non va bene e va usato quanto eventualmente inserito dall'utente.
            listaCodiciSiope = integrazioneContabilitaComuneModena.getListaCodiciSiope (codiceEnte, codiceUoProponente, movimento.tipo, capitolo, capitolo.voceEconomica)
        }
    }

	@NotifyChange(["movimento", "listaMovimenti"])
	@Command onSalvaMovimentoContabile () {

		if (movimento.importo == null ||
            movimento.codice  == null ||
            movimento.azione  == null ||
            movimento.capitolo == null) {

			Clients.showNotification("Non è possibile salvare il movimento: non tutti i campi obbligatori sono compilati.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 3000, true)
			return;
		}

		movimento.idDocumento   = idDocumento
		movimento.tipoDocumento = tipoDocumento
		movimento.save()
		movimento = null
		popupModificaMovimentoContabile.visible = false

		aggiornaMovimentiContabili ()

		Clients.showNotification("Movimento salvato con successo.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
	}

	@Command onChiudiMovimentoContabile () {
		popupModificaMovimentoContabile.visible = false
	}

    @NotifyChange(["listaCodiciStatistici"])
	@Command onSelectTipoCodiceStatistico(@BindingParam("index") int index) {
        listaCodiciStatistici[index].clear()
        listaCodiciStatistici[index].addAll(integrazioneContabilitaComuneModena.getListaCodiciStatistici(codiceEnte, codiceUoProponente, movimento."tipoCodiceStatistico${index+1}"))
	}

    /*
     * Funzioni per la gestione della popup di ricerca soggetti
     */

    @Command onOpenPopupCercaSoggetti () {
        popupRicercaSoggetti.setVisible(true)
        popupRicercaSoggetti.doModal()
    }

    @NotifyChange(["listaSoggetti", "totalSize", "activePage"])
    @Command onCerca (@BindingParam("paginate") boolean paginate) {
        if (paginate == false) {
            activePage = 0
        }

        listaSoggetti = integrazioneContabilitaComuneModena.getListaSoggetti(codiceEnte, codiceUoProponente, movimento.tipo, codiceSoggetto, denominazioneSoggetto, codiceFiscale, partitaIva, localitaSoggetto)
    }

    @Command onAnnulla () {
        Events.postEvent(Events.ON_CLOSE, popupRicercaSoggetti, null)
    }

    @NotifyChange(["soggetto"])
    @Command onSeleziona() {
        movimento.codiceSoggetto = soggetto.codice
        Events.postEvent(Events.ON_CLOSE, popupRicercaSoggetti, soggetto)
    }
}