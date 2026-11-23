package odg

import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dizionari.Delega
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.StatoOdg
import it.finmatica.atti.dto.dizionari.DelegaDTO
import it.finmatica.atti.dto.dizionari.NotificaDTO
import it.finmatica.atti.dto.odg.*
import it.finmatica.atti.dto.odg.dizionari.EsitoDTO
import it.finmatica.atti.dto.odg.dizionari.EsitoDTOService
import it.finmatica.atti.dto.odg.dizionari.RuoloPartecipanteDTO
import it.finmatica.atti.dto.odg.dizionari.VotoDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.atti.odg.OggettoSedutaDelega
import it.finmatica.atti.odg.dizionari.EsitoStandard
import it.finmatica.atti.odg.dizionari.RuoloPartecipante
import it.finmatica.atti.odg.dizionari.Voto
import odg.seduta.PopupPartecipantiViewModel
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zhtml.Messagebox
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.ListModelList
import org.zkoss.zul.Listbox
import org.zkoss.zul.Window

import java.text.SimpleDateFormat

class OdgOggettoSedutaViewModel {

    // services
    EsitoDTOService esitoDTOService
    OggettoSedutaDTOService oggettoSedutaDTOService
    OggettoPartecipanteDTOService oggettoPartecipanteDTOService
    SedutaDTOService sedutaDTOService

    // componenti
    Window self

    // dati
    OggettoSedutaDTO oggetto
    List<EsitoDTO> listaEsito
    LinkedHashMap voti = [(Voto.VOTO_FAVOREVOLE): 0,
                          (Voto.VOTO_CONTRARIO) : 0,
                          (Voto.VOTO_ASTENUTO)  : 0,
                          (Voto.NON_VOTANTE)    : 0]
    List<VotoDTO> listaVoti
    List<DelegaDTO> listaDelega
    List<NotificaDTO> listaNotifiche
    List<RuoloPartecipanteDTO> listaRuoli
    OggettoPartecipanteDTO selectedPartecipante
//	List<OggettoPartecipanteDTO>	listaPartecipanti = []
    ListModelList<OggettoPartecipanteDTO> listaPartecipanti = new ListModelList<>()
    VotoDTO votoPredefinito
    String oggettoProposta = ""
    HashMap<OggettoSedutaDTO, List<As4SoggettoCorrenteDTO>> listaOggettoSeduta

    // stato
//	String testoDaCercare 	= ""
    boolean oggettoModificabile = true;
    boolean daNonVerbalizzare = false;
    boolean mostraAssentiNonGiustificati = true;
    boolean mostraEseguibilitaImmediata = true
    boolean odgNumeraDelibere = true;
    boolean abilitaVotoPresidente = true;
    boolean delegheMultipleAbilitato = false;
    boolean hasAltreDeleghe = false;

    @Init
    init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") long id) {
        this.self = w

        OggettoSeduta ogg = OggettoSeduta.createCriteria().get {
            eq("id", id)

            fetchMode("propostaDelibera", FetchMode.JOIN)
            fetchMode("propostaDelibera.tipologia", FetchMode.JOIN)
            fetchMode("seduta", FetchMode.JOIN)
            fetchMode("seduta.commissione", FetchMode.JOIN)
            fetchMode("seduta.tipoSeduta", FetchMode.JOIN)
            fetchMode("esito", FetchMode.JOIN)
        }

        oggetto = ogg.toDTO(["delega.assessore"])

        // http://svi-redmine/issues/22189 -> ODG: Impostare la data e l'ora di discussione della proposta solo in fase di conferma esito
        // if (oggetto.dataDiscussione == null) {
        //	 oggetto.dataDiscussione = oggetto.seduta.dataSeduta
        // }

        daNonVerbalizzare = StatoOdg.isInIstruttoria(ogg.propostaDelibera ?: ogg.determina);
        oggettoModificabile = Impostazioni.ODG_MODIFICA_OGGETTO_PROPOSTA.abilitato
        mostraAssentiNonGiustificati = Impostazioni.ODG_MOSTRA_ASSENTI_NON_GIUSTIFICATI.abilitato;
        mostraEseguibilitaImmediata = Impostazioni.ESEGUIBILITA_IMMEDIATA_ATTIVA.abilitato
        delegheMultipleAbilitato = Impostazioni.DELEGHE_MULTIPLE.abilitato
        hasAltreDeleghe = OggettoSedutaDelega.countByOggettoSeduta(ogg) > 0

        listaVoti = Voto.findAllByValido(true, [sort: 'sequenza', order: 'asc']).toDTO()

        // Carica il dal dizionazrio dei voti il valore predefinito se c'è
        votoPredefinito = Voto.findByPredefinito(true)?.toDTO()

        loadEsito()
        loadDelega()
        loadOggetto()

//		if (oggetto.esito != null)
//			loadPartecipanti()
//          ricalcolaVoti()

        odgNumeraDelibere = Impostazioni.ODG_NUMERA_DELIBERE.abilitato
        abilitaVotoPresidente = ogg.seduta.votoPresidente

        BindUtils.postNotifyChange(null, null, this, "oggetto")
        BindUtils.postNotifyChange(null, null, this, "abilitaVotoPresidente")
    }

    public boolean isNumeraDelibera() {
        return (oggetto.confermaEsito && oggetto.esito.esitoStandard.codice == EsitoStandard.ADOTTATO && Delibera.countByOggettoSeduta(oggetto.domainObject) == 0);
    }

    private void loadOggetto() {
        if (oggetto.propostaDelibera) {
            oggettoProposta = oggetto.propostaDelibera.oggetto
        } else {
            oggettoProposta = oggetto.determina.oggetto
        }
        BindUtils.postNotifyChange(null, null, this, "oggettoProposta")
    }

    private void loadEsito() {
        listaEsito = esitoDTOService.getListaEsiti(oggetto.propostaDelibera.domainObject, oggetto.seduta.commissione.id);
    }

    private void loadPartecipanti() {
        listaPartecipanti = sedutaDTOService.creaPartecipanti(oggetto.domainObject).toDTO(["sedutaPartecipante.commissioneComponente.componente", "sedutaPartecipante.commissioneComponente.incarico", "sedutaPartecipante.componenteEsterno", "sedutaPartecipante.incarico", "ruoloPartecipante", "sedutaPartecipanteDelegato.commissioneComponente.componente", "sedutaPartecipanteDelegato.componenteEsterno", "voto"]);

        // per ogni voto nella lista voti caricata inizialmente scorre la lista partecipanti e
        // calcola i risultati delle votazioni e costruisce lo stringone
        //ricalcolaVoti()
        BindUtils.postNotifyChange(null, null, this, "listaPartecipanti")
    }

    private void ricalcolaVoti() {
        boolean votoPres = oggetto.seduta.votoPresidente
        /*
        voti.each { voto ->
            voto.value = listaPartecipanti.count { partecipante ->
                partecipante.voto?.codice == voto.key &&
                        partecipante.ruoloPartecipante?.codice != RuoloPartecipante.CODICE_SEGRETARIO &&
                        (partecipante.ruoloPartecipante?.codice != RuoloPartecipante.CODICE_PRESIDENTE || votoPres)
            }
        }
        */
        def gruppoVoti = listaPartecipanti.findAll { partecipante ->
            partecipante.ruoloPartecipante?.codice != RuoloPartecipante.CODICE_SEGRETARIO &&
            (partecipante.ruoloPartecipante?.codice != RuoloPartecipante.CODICE_PRESIDENTE || votoPres)
        }.groupBy { it.voto?.codice }

        voti.each { k, v ->
            voti[k] = gruppoVoti[k]?.size() ?: 0
        }

        BindUtils.postNotifyChange(null, null, this, "voti")
    }

    private void loadDelega() {
        listaDelega = Delega.createCriteria().list {
            eq('valido', true)
            fetchMode("assessore", FetchMode.JOIN)
            order('sequenza', 'asc')
        }.toDTO()
    }

    @Command
    onCambiaVoto() {
        ricalcolaVoti();
    }

    @Command
    openPopupVoti() {
        Window w = Executions.createComponents("/odg/popupAssegnaVoti.zul", null, null)
        w.onClose { event ->
            VotoDTO voto = event.data ?: Voto.get(event.data)
            if (voto) {
                // assegno il voto a tutti i partecipanti
                for (def partecipante : listaPartecipanti) {
                    if (partecipante.presente &&    // assegno il voto solo se il partecipante è presente
                            partecipante.ruoloPartecipante?.codice != RuoloPartecipante.CODICE_SEGRETARIO && // se non è il segretario
                            partecipante.ruoloPartecipante?.codice != RuoloPartecipante.CODICE_INVITATO &&     // se non è invitato
                            (partecipante.ruoloPartecipante?.codice != RuoloPartecipante.CODICE_PRESIDENTE ||  // se non è presidente
                                    (partecipante.ruoloPartecipante?.codice == RuoloPartecipante.CODICE_PRESIDENTE && // oppure se è il presidente e il presidente ha diritto di voto
                                            oggetto.seduta.votoPresidente))) {
                        partecipante.voto = voto
                    }
                }
                ricalcolaVoti();
                BindUtils.postNotifyChange(null, null, this, "listaPartecipanti")
            }
        }
        w.doModal()
    }

    @NotifyChange(["listaNotifiche"])
    @Command
    calcolaListaNotifiche() {
        listaNotifiche = Notifica.createCriteria().list() {
            eq("valido", true)
            'in'("tipoNotifica", [TipoNotifica.DELIBERA_SEGRETARIO, TipoNotifica.VERBALIZZAZIONE_PROPOSTA])

            // FIXME: Perché devo fare questo if?
            if (oggetto.propostaDelibera == null) {
                ne("codice", TipoNotifica.DELIBERA_SEGRETARIO)
            }
        }?.toDTO()
    }

    @Command
    public boolean onSalva() {
        if (!checkOnSalva()) {
            return false;
        }

        // in caso di modifica dell'oggetto di una proposta per cui non è presente nessun esito questo viene aggiornato
        if (oggetto.esito == null && oggetto.propostaDelibera != null && oggettoProposta != oggetto.propostaDelibera.oggetto) {
            PropostaDelibera propostaDelibera = PropostaDelibera.get(oggetto.propostaDelibera.id)
            propostaDelibera.oggetto = oggettoProposta.toUpperCase()
            propostaDelibera.save()
        } else if (oggetto.esito == null && oggetto.determina && oggettoProposta != oggetto.determina.oggetto) {
            Determina determina = Determina.get(oggetto.determina.id)
            determina.oggetto = oggettoProposta.toUpperCase()
            determina.save()
        }

        oggettoSedutaDTOService.salva(oggetto, listaPartecipanti)
        Clients.showNotification("Proposta salvata.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true);

        BindUtils.postGlobalCommand(null, null, "onRefreshPartecipanti", [messages: []]);
        return true;
    }

    @Command
    onSalvaChiudi() {
        if (!onSalva()) {
            return
        }

        onChiudi();
    }

    @Command
    onChiudi() {
        BindUtils.postGlobalCommand(null, null, "abilitaTabFolders", null)
        BindUtils.postGlobalCommand(null, null, "onRefreshTesti", null)

        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @Command
    onModificaPartecipante(@BindingParam("partecipante") OggettoPartecipanteDTO partecipante) {
        Window w = Executions.createComponents("/odg/seduta/popupPartecipanti.zul", self, [id: partecipante.id, seduta: null, oggettoSeduta: oggetto, sezione: PopupPartecipantiViewModel.SEZIONE_OGGETTO_SEDUTA])
        w.onClose {
            loadPartecipanti()
            ricalcolaVoti()
        }
        w.doModal()
    }

    @Command
    onEliminaPartecipante(@BindingParam("partecipante") OggettoPartecipanteDTO partecipante) {
        Messagebox.show("Sei sicuro di voler cancellare questo partecipante?", "Conferma cancellazione del partecipante",
                Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    public void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            oggettoPartecipanteDTOService.elimina(partecipante)
                            loadPartecipanti()
                            ricalcolaVoti()
                        }
                    }
                }
        )
    }

    @Command
    onCreaPartecipante() {
        Window w = Executions.createComponents("/odg/seduta/popupPartecipanti.zul", self, [id: -1, seduta: null, oggettoSeduta: oggetto, sezione: PopupPartecipantiViewModel.SEZIONE_OGGETTO_SEDUTA])
        w.onClose {
            loadPartecipanti()
            ricalcolaVoti()
        }
        w.doModal()
    }

    @NotifyChange("listaPartecipanti")
    @Command
    onLoadPartecipanti() {
        loadPartecipanti()
        ricalcolaVoti()
    }

    @NotifyChange(["selectedPartecipante", "numeroPresenti", "numeroAssenti"])
    @Command
    onSettaPresenza(@BindingParam("valore") String valore, @BindingParam("partecipante") OggettoPartecipanteDTO partecipante) {
        partecipante.presente = (valore == "Presenti")
        partecipante.assenteNonGiustificato = (valore == "Assenti Non Giustificati")
        partecipante.voto = (valore == "Presenti" && partecipante.ruoloPartecipante?.codice != 'S') ? votoPredefinito : null;
        selectedPartecipante = partecipante;

        ricalcolaVoti();

        BindUtils.postNotifyChange(null, null, this, "selectedPartecipante")
    }

    @NotifyChange("selectedPartecipante")
    @Command
    onChangeRuolo(@BindingParam("ruolo") String ruolo, @BindingParam("partecipante") OggettoPartecipanteDTO partecipante) {
        if (ruolo == RuoloPartecipante.CODICE_SEGRETARIO || ruolo == RuoloPartecipante.CODICE_INVITATO) {
            partecipante.voto = null;
        } else {
            partecipante.voto = (partecipante.voto == null) ? votoPredefinito : partecipante.voto;
        }
        selectedPartecipante = partecipante
    }

    @Command
    prevOggetto() {
        Long idOggettoSeduta = OggettoSeduta.createCriteria().get {
            eq("sequenzaDiscussione", oggetto.sequenzaDiscussione - 1)
            eq("seduta.id", oggetto.seduta.id)
        }?.id

        if (idOggettoSeduta > 0) {
            apriOggettoSeduta(idOggettoSeduta)
        }
    }

    @Command
    nextOggetto() {
        Long idOggettoSeduta = OggettoSeduta.createCriteria().get {
            eq("sequenzaDiscussione", oggetto.sequenzaDiscussione + 1)
            eq("seduta.id", oggetto.seduta.id)
        }?.id

        if (idOggettoSeduta > 0) {
            apriOggettoSeduta(idOggettoSeduta)
        }
    }

    private void apriOggettoSeduta(long idOggettoSeduta) {
        Executions.createComponents("/odg/oggettoSeduta.zul", null, [id: idOggettoSeduta, wp: 'oggettoSeduta']).doModal()
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @Command
    onApriProposta() {
        if (oggetto.propostaDelibera != null) {
            Executions.createComponents("/atti/documenti/propostaDelibera.zul", self, [id: oggetto.propostaDelibera.id]).doModal()
        } else {
            Executions.createComponents("/atti/documenti/determina.zul", self, [id: oggetto.determina.id]).doModal()
        }
    }

    @Command
    onNotifica(@BindingParam("notifica") NotificaDTO notifica) {
        Window w = Executions.createComponents("/odg/popupNotificheMail.zul", self, [seduta         : oggetto.seduta
                                                                                     , oggettoSeduta: oggetto
                                                                                     , notifica     : notifica])
        w.doModal()
    }

    private boolean checkOnSalva() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy")
        List<String> messaggio = []

        if (oggetto.propostaDelibera && !oggetto.propostaDelibera.tipologia?.titolo) {
            messaggio += "Il valore TIPO PROPOSTA è obbligatorio"
        }

        if (oggetto.determina && !oggetto.determina.tipologia?.titolo) {
            messaggio += "Il valore TIPO PROPOSTA è obbligatorio\n"
        }

        if (oggetto.oggettoAggiuntivo == null) {
            messaggio += "Il valore OGGETTO AGGIUNTIVO è obbligatorio"
        }

        if (!oggettoProposta?.trim()) {
            messaggio += "Il valore OGGETTO è obbligatorio"
        }

        if (oggetto.dataDiscussione) {
            Date dataMin = oggetto.seduta?.dataInizioSeduta ?: oggetto.seduta?.dataSeduta

            if (oggetto.dataDiscussione < dataMin) {
                messaggio += "La DATA DISCUSSIONE non può essere precedente della data definita nella seduta (" + dateFormatter.format(dataMin) + ")"
            }

            if (oggetto.seduta.dataFineSeduta != null) {
                if (oggetto.dataDiscussione > oggetto.seduta.dataFineSeduta) {
                    messaggio += "La DATA DISCUSSIONE non può essere successiva alla data definita nella seduta (" + dateFormatter.format(oggetto.seduta.dataFineSeduta) + ")"
                }
            }

            def oraDiscussione = oggetto.oraDiscussione?.trim()

            if (!oraDiscussione) {
                messaggio += "Il valore ORA DISCUSSIONE è obbligatorio se viene inserita la DATA DISCUSSIONE"
            } else if (oraDiscussione && !(oraDiscussione ==~ /^([0-1]\d|2[0-3]):([0-5]\d)$/)) {
                messaggio += "Il valore ORA DISCUSSIONE non è corretto (HH:mm)"
            }
        }

        if (messaggio.size() > 0) {
            //Clients.showNotification(messaggio, Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 5000, true)
            Messagebox.show("${messaggio.join("\n\n")}.", "Attenzione", Messagebox.OK, Messagebox.EXCLAMATION)
            return false
        }

        return true
    }

    @Command
    onAggiungiDelega() {
        Window w = Executions.createComponents("/atti/documenti/popupSceltaDelega.zul", self, [propostaDelibera: oggetto?.propostaDelibera])
        w.onClose { event ->
            if (event.data != null) {
                if (event.data instanceof String && event.data == "eliminaDelega") {
                    oggetto.delega = null
                } else {
                    oggetto.delega = event.data
                }
                BindUtils.postNotifyChange(null, null, this, "oggetto")
            }
        }
        w.doModal()
    }

    @Command
    onGestisciDelegheOggettoSeduta () {
        Window w = Executions.createComponents("/odg/popupDelegheOggettoSeduta.zul", self, [oggettoSeduta: oggetto])
        w.onClose { event ->
            hasAltreDeleghe = OggettoSedutaDelega.countByOggettoSeduta(oggetto.domainObject) > 0
            BindUtils.postNotifyChange(null, null, this, "hasAltreDeleghe")
        }
        w.doModal()
    }

    @NotifyChange("numeraDelibera")
    @Command
    onNumeraDelibera() {

        String msg = odgNumeraDelibere ? "Sei sicuro di voler Numerare la Delibera?" : "Sei sicuro di voler Creare la Delibera?";
        Messagebox.show(msg, "Attenzione",
                Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    public void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            OdgOggettoSedutaViewModel.this.confermaEsitoENumeraDelibera();
                        }
                    }
                });
    }

    private void confermaEsitoENumeraDelibera() {
        if (sedutaDTOService.esisteSedutaPrecedenteConEsitoNonConfermato(oggetto.seduta)) {
            Clients.showNotification("Non è possibile numerare la delibera: esiste una seduta precedente con proposte senza esito confermato.", Clients.NOTIFICATION_TYPE_ERROR, null, "before_center", 5000, true);
            return
        }

        if (sedutaDTOService.esistonoDelibereNonNumerateInSedutePrecedenti(oggetto.seduta)) {
            Clients.showNotification("Non è possibile numerare la delibera: esiste una seduta precedente con proposte senza delibera numerata.", Clients.NOTIFICATION_TYPE_ERROR, null, "before_center", 5000, true);
            return
        }

        if (odgNumeraDelibere) {
            Delibera delibera = sedutaDTOService.confermaEsitoENumeraDelibera(oggetto);
            Clients.showNotification("Delibera creata con numero: ${delibera.numeroDelibera} / ${delibera.annoDelibera}", Clients.NOTIFICATION_TYPE_INFO, self, "middle_center", 3000, true);
        } else {
            Delibera delibera = sedutaDTOService.confermaEsitoECreaDelibera(oggetto);
            Clients.showNotification("Delibera creata", Clients.NOTIFICATION_TYPE_INFO, self, "middle_center", 3000, true);
        }
        oggetto.confermaEsito = true;

        BindUtils.postNotifyChange(null, null, this, "numeraDelibera")
    }

    @Command
    onSettaEsito() {
        oggettoSedutaDTOService.salva(oggetto)
        loadPartecipanti()
        ricalcolaVoti()

        BindUtils.postNotifyChange(null, null, this, "numeraDelibera")
    }

    /*
    @NotifyChange(["listaPartecipanti", "selectedPartecipante"])
    @Command onSuSequenza (@BindingParam("listaPartecipanti") Listbox listaPartecipanti) {
        int index = listaPartecipanti.getSelectedIndex()
        if(index>0 && index <= listaPartecipanti.getItems().size()-1) {
            def precedente = listaPartecipanti.getSelectedIndex() - 1
            oggettoPartecipanteDTOService.spostaPartecipanteSu(selectedPartecipante, listaPartecipanti.getItemAtIndex(precedente).value, index);
            def tmp = listaPartecipanti.find { it.id == selectedPartecipante.id }
            if (selectedPartecipante.oggettoSeduta.esito != null) {
                loadPartecipanti()
                ricalcolaVoti()
            }
            selectedPartecipante = tmp
        }
    }
    */

    @NotifyChange(["listaPartecipanti", "selectedPartecipante", "listaPresenze"])
    @Command
    onSuSequenza(@BindingParam("listaPresenze") Listbox listaPresenze) {
        if (listaPresenze.getSelectedIndex() > 0) {
            def corrente = listaPresenze.getItemAtIndex(listaPresenze.getSelectedIndex()).value
            def precedente = listaPresenze.getItemAtIndex(listaPresenze.getSelectedIndex() - 1).value

            Long sequenzaTmpCampo = precedente.sequenza
            precedente.sequenza = corrente.sequenza
            corrente.sequenza = sequenzaTmpCampo

            listaPartecipanti.sort { it.sequenza }

            selectedPartecipante = corrente

            oggettoPartecipanteDTOService.spostaPartecipanteSu(selectedPartecipante, precedente)
        }
    }

    /*
	@NotifyChange(["listaPartecipanti", "selectedPartecipante"])
	@Command onGiuSequenza (@BindingParam("listaPartecipanti") Listbox listaPartecipanti) {
		int index = listaPartecipanti.getSelectedIndex()
		if(index < listaPartecipanti.getItems().size()-1 && index>=0) {
			def successivo = listaPartecipanti.getSelectedIndex() + 1
			oggettoPartecipanteDTOService.spostaPartecipanteGiu(selectedPartecipante, listaPartecipanti.getItemAtIndex(successivo).value, index);
			def tmp = listaPartecipanti.find { it.id == selectedPartecipante.id }
			if (selectedPartecipante.oggettoSeduta.esito != null) {
				loadPartecipanti()
				ricalcolaVoti()
			}
			selectedPartecipante = tmp
		}
	}
    */

    @NotifyChange(["listaPartecipanti", "selectedPartecipante", "listaPresenze"])
    @Command
    onGiuSequenza(@BindingParam("listaPresenze") Listbox listaPresenze) {
        if (listaPresenze.getSelectedIndex() >= 0 && listaPresenze.getSelectedIndex() < listaPartecipanti.size() - 1) {
            def corrente = listaPresenze.getItemAtIndex(listaPresenze.getSelectedIndex()).value
            def successivo = listaPresenze.getItemAtIndex(listaPresenze.getSelectedIndex() + 1).value

            Long sequenzaTmpCampo = successivo.sequenza
            successivo.sequenza = corrente.sequenza
            corrente.sequenza = sequenzaTmpCampo

            listaPartecipanti.sort { it.sequenza }

            selectedPartecipante = corrente

            oggettoPartecipanteDTOService.spostaPartecipanteGiu(selectedPartecipante, successivo)
        }
    }
}
