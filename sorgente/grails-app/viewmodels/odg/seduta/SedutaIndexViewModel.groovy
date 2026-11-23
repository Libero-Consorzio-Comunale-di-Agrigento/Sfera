package odg.seduta

import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.dto.dizionari.NotificaDTO
import it.finmatica.atti.dto.odg.CommissioneStampaDTO
import it.finmatica.atti.dto.odg.SedutaDTO
import it.finmatica.atti.dto.odg.SedutaDTOService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.odg.*
import it.finmatica.atti.odg.dizionari.RuoloPartecipante
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.TipoFile
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Filedownload
import org.zkoss.zul.Include
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Tab
import org.zkoss.zul.Tabbox
import org.zkoss.zul.Tabpanel
import org.zkoss.zul.Window
import org.zkoss.zk.ui.event.EventListener

import java.text.SimpleDateFormat

class SedutaIndexViewModel {

    // services
    GestioneTestiService gestioneTestiService
    SedutaDTOService     sedutaDTOService
    SedutaService        sedutaService

    // componenti
    Window self

    // dati
    SedutaDTO                  seduta
    String                     descrizioneSeduta
    String                     statoSeduta
    String                     linkSedutaPrincipale
    int                        numOggettoSeduta
    int                        numSedutaPartecipante
    int                        numDelibere
    boolean                    modificaPartecipanti
    boolean                    abilitaLinkSeduta
    List<CommissioneStampaDTO> listaStampeCommissione
    List<NotificaDTO>          listaNotifiche
    boolean                    abilitaTabVerbalizzazione
    boolean                    abilitaTabCommissioniCollegate
    boolean                    abilitaVerbalizzazione
    boolean                    abilitaTesti
    boolean                    esportaSedutaXml
    boolean                    esportaSedutaCsv
    Date                       dataSedutaIniziale
    boolean                    abilitaOrdinamentoProposteSeparato

    GrailsApplication grailsApplication

    @Init
    void init (@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") long id) {
        this.self = w

        linkSedutaPrincipale = ""
        abilitaLinkSeduta = true
        abilitaTabVerbalizzazione = true
        abilitaTabCommissioniCollegate = false
        abilitaVerbalizzazione = true
        abilitaTesti = true
        esportaSedutaXml = false
        esportaSedutaCsv = false
        descrizioneSeduta = ""
        numOggettoSeduta = 0
        numSedutaPartecipante = 0

        abilitaOrdinamentoProposteSeparato=Impostazioni.ODG_ORDINAMENTO_PROPOSTE_SEPARATO.abilitato

        if (id > 0) {
            Seduta s = Seduta.findById(id,
                                       [fetch: [commissione: "eager", tipoSeduta: "eager", secondaSeduta: "eager", utenteIns: "eager", utenteUpd: "eager"]])
            seduta = s.toDTO()
            dataSedutaIniziale = seduta.dataSeduta

            // calcolo il testo del link per aprire la seconda seduta:
            Seduta sedutaPrincipale = Seduta.findBySecondaSeduta(s)
            if (sedutaPrincipale != null) {
                linkSedutaPrincipale =
                        "Seconda convocazione della seduta n° " + sedutaPrincipale.numero + " del  " + sedutaPrincipale.dataSeduta.format(
                                'dd/MM/yyyy')
            }

            calcolaListaStampeCommissione()
            calcolaListaNotifiche()

        } else {
            seduta = new SedutaDTO(id: -1)
            seduta.dataSeduta = new Date().clearTime()
        }

        calcolaNumSedutaPartecipante()
        calcolaNumDelibere()
        checkAbilitaTestiEVerbalizzazione()
        checkAbilitaTabVerbalizzazione()
        checkAbilitaTabCommissioniCollegate()
        checkAbilitaEsportazioneSedutaXml()
        checkAbilitaEsportazioneSedutaCsv()
    }
    /*
    @Command
    public void onSelectTab(@BindingParam("selectedTab") Tab selectedTab,
                            @ContextParam(ContextType.VIEW) Component view) {

        if (selectedTab != null && "verbalizzazioneOrdina".equals(selectedTab.getId())) {
            Include include = (Include) view.query("#includeOrdina");
            if (include != null) {
                String src = include.getSrc();
                include.setSrc(null);
                include.setSrc(src);
            }
        } else if (selectedTab != null && "verbalizzazioneModifica".equals(selectedTab.getId())) {
            Include include = (Include) view.query("#includeModifica");
            if (include != null) {
                // forza il reload dello zul e quindi re-inizializza il VM
                String src = include.getSrc();
                include.setSrc(null);
                include.setSrc(src);
            }
        } else if(selectedTab != null && selectedTab.getLastChild()!=null){
            selectedTab.getLastChild().invalidate()
        }

    }
    */
    @Command
    public void onSelectTab(@BindingParam("selectedTab") Tab selectedTab,
                            @ContextParam(ContextType.VIEW) Component view) {

        if (selectedTab == null) return

        switch (selectedTab.getId()) {
            case "verbalizzazioneOrdina":
                BindUtils.postGlobalCommand(null, null,
                        "onRefreshVerbalizzazioneOrdina",
                        null
                )
                break
            case "verbalizzazioneModifica":
                BindUtils.postGlobalCommand(null, null,
                        "onRefreshVerbalizzazioneModifica",
                        null
                )
                break
            default:
                return
        }
    }

    boolean isStampeAbilitate () {
        if (Impostazioni.STAMPE_SEDUTA_DOCUMENTI.abilitato) {
            return false
        }

        if (seduta.id < 0) {
            return false
        }

        return true
    }

    boolean isNotificheStampeAbilitate () {
        if (seduta.id < 0) {
            return false
        }

        return (isStampeAbilitate() && listaNotifiche.size() > 0 && Impostazioni.ODG_FORMATO_STAMPE.valore.equalsIgnoreCase(TipoFile.PDF.estensione))
    }

    String getDescrizioneSeduta () {
        if (seduta.id > 0) {
            return "Commissione " + seduta.commissione?.titolo + " - SEDUTA n° " + seduta?.numero + " del " + seduta.dataSeduta?.format('dd/MM/yyyy');
        } else {
            return "Nuova Seduta";
        }
    }

    @NotifyChange("numOggettoSeduta")
    String getNumOggettoSeduta () {
        numOggettoSeduta = OggettoSeduta.createCriteria().get() {
            projections { rowCount() }
            eq("seduta.id", seduta.id)
        }
    }

    @NotifyChange("numDelibere")
    private String calcolaNumDelibere () {
        numDelibere = Delibera.createCriteria().get() {
            projections { rowCount() }
            oggettoSeduta {
                eq("seduta.id", seduta.id)
            }
        }
    }

    @NotifyChange("modificaPartecipanti")
    String getModificaPartecipanti () {
        if (seduta.id > 0 && seduta.dataSeduta) {
            modificaPartecipanti = !(seduta.dataSeduta.clearTime().compareTo(new Date().clearTime()) <= 0)
        } else {
            modificaPartecipanti = true
        }
    }

    @Command
    void onApriSedutaPrincipale () {
        Seduta sedutaPrincipale = Seduta.findBySecondaSeduta(seduta.domainObject);
        Window w = Executions.createComponents("/odg/seduta/index.zul", null, [id: sedutaPrincipale.id])
        w.doModal()
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @GlobalCommand
    void abilitaTabFolders () {
        calcolaNumSedutaPartecipante()
        calcolaNumDelibere()
        checkAbilitaTestiEVerbalizzazione()
    }

    private void checkAbilitaTabVerbalizzazione () {
        Date dataSedutaFormatted = (new SimpleDateFormat("dd/MM/yyyy")).parse(seduta.dataSeduta.format("dd/MM/yyyy"))
        Date oggi = (new SimpleDateFormat("dd/MM/yyyy")).parse(new Date().format("dd/MM/yyyy"))

        abilitaTabVerbalizzazione = !(dataSedutaFormatted.compareTo(oggi) <= 0 && seduta.oraSeduta != null)

        BindUtils.postNotifyChange(null, null, this, "abilitaTabVerbalizzazione")
    }

    private void checkAbilitaTabCommissioniCollegate () {
        abilitaTabCommissioniCollegate = CommissioneCollegata.countByCommissioneAndValido(seduta?.commissione?.getDomainObject(), true) > 0
        BindUtils.postNotifyChange(null, null, this, "abilitaTabCommissioniCollegate")
    }

    private void checkAbilitaVerbalizzazione () {
        checkAbilitaTestiEVerbalizzazione()
    }

    private void checkAbilitaTesti () {
        checkAbilitaTestiEVerbalizzazione()
    }

    private void checkAbilitaTestiEVerbalizzazione () {
        boolean ruoliObbligatoriMancanti = !controlloRuoliObbligatori(RuoloPartecipante.CODICE_PRESIDENTE) && !controlloRuoliObbligatori(RuoloPartecipante.CODICE_SEGRETARIO)

        abilitaVerbalizzazione = ruoliObbligatoriMancanti ? (numSedutaPartecipante == 0) : true
        abilitaTesti           = ruoliObbligatoriMancanti ? (numDelibere == 0)           : true

        BindUtils.postNotifyChange(null, null, this, "abilitaVerbalizzazione")
        BindUtils.postNotifyChange(null, null, this, "abilitaTesti")
    }

    private void checkAbilitaEsportazioneSedutaXml () {
        if (grailsApplication.config.atti.esportaSedutaXml != null && grailsApplication.config.atti.esportaSedutaXml == true) {
            esportaSedutaXml = true
        } else {
            esportaSedutaXml = false
        }
    }

    private void checkAbilitaEsportazioneSedutaCsv () {
        if (grailsApplication.config.atti.esportaSedutaCsv != null && grailsApplication.config.atti.esportaSedutaCsv == true) {
            esportaSedutaCsv = true
        } else {
            esportaSedutaCsv = false
        }
    }

    private boolean controlloRuoliObbligatori (String ruolo) {
        if (seduta.commissione?.ruoliObbligatori) {
            return (SedutaPartecipante.createCriteria().count {
                eq('seduta.id', seduta.id)
                ruoloPartecipante { eq("codice", ruolo) }
            } == 0)
        }

        return false
    }

    private void calcolaNumSedutaPartecipante () {
        numSedutaPartecipante = SedutaPartecipante.countBySeduta(seduta.domainObject);
    }

    @NotifyChange(["listaStampeCommissione"])
    @Command
    void calcolaListaStampeCommissione () {
        listaStampeCommissione = CommissioneStampa.createCriteria().list() {
            eq('commissione.id', seduta.commissione.id)
            'in'('codice', [CommissioneStampa.CONVOCAZIONE, CommissioneStampa.VERBALE])

            fetchMode("commissione", FetchMode.JOIN)
            fetchMode("modelloTesto", FetchMode.JOIN)
        }?.toDTO()
    }

    void calcolaListaNotifiche () {
        listaNotifiche = Notifica.findAllByCommissioneAndValidoAndTipoNotificaInList(seduta.commissione.getDomainObject(), true, [TipoNotifica.CONVOCAZIONE_SEDUTA, TipoNotifica.NOTIFICHE_ODG], [fetch:[commissione:'eager']]).toDTO()

        if (seduta.dataSeduta.clearTime().compareTo(new Date().clearTime()) <= 0) {
            if (seduta.completa) {
                statoSeduta = "Completa"
            } else {
                statoSeduta = "Da Verbalizzare"
            }
        } else {
            statoSeduta = "Ancora da svolgere"
        }

        BindUtils.postNotifyChange(null, null, this, "notificheStampeAbilitate")
        BindUtils.postNotifyChange(null, null, this, "statoSeduta")
        BindUtils.postNotifyChange(null, null, this, "listaNotifiche")
    }

    private boolean checkOnSalva () {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy")
        List<String> messaggio = []

        if (!seduta.commissione) {
            messaggio << "Il valore COMMISSIONE è obbligatorio"
        }

        if (!seduta.tipoSeduta) {
            messaggio << "Il valore TIPO SEDUTA è obbligatorio"
        }

        def oraInizioSeduta = seduta.oraInizioSeduta?.trim()

        if (seduta.dataInizioSeduta && !oraInizioSeduta) {
            messaggio << "Il valore ORA INIZIO SEDUTA è obbligatorio se viene valorizzata la DATA INIZIO SEDUTA"
        } else if (oraInizioSeduta && !(oraInizioSeduta ==~ /^([0-1]\d|2[0-3]):([0-5]\d)$/)) {
            messaggio << "Il valore ORA INIZIO SEDUTA non è corretto (HH:mm)"
        }

        if (!seduta.dataSeduta) {
            messaggio << "Il valore DATA SEDUTA è obbligatorio"
        }

        def oraSeduta = seduta.oraSeduta?.trim()

        if (!oraSeduta) {
            messaggio << "Il valore ORA SEDUTA è obbligatorio"
        } else if (oraSeduta && !(oraSeduta ==~ /^([0-1]\d|2[0-3]):([0-5]\d)$/)) {
            messaggio << "Il valore ORA SEDUTA non è corretto (HH:mm)"
        }

        if (seduta.dataSeduta && seduta.dataSecondaConvocazione) {
            if (seduta.dataSecondaConvocazione < seduta.dataSeduta) {
                messaggio << "Il valore DATA SECONDA CONVOCAZIONE non può essere precedente alla DATA SEDUTA ("+dateFormatter.format(seduta.dataSeduta)+")"
            }
        }

        def oraSecondaConvocazione = seduta.oraSecondaConvocazione?.trim()

        if (seduta.dataSecondaConvocazione && !oraSecondaConvocazione) {
            messaggio << "Il valore ORA SECONDA CONVOCAZIONE è obbligatorio se viene valorizzata la DATA SECONDA CONVOCAZIONE"
        } else if (oraSecondaConvocazione && !(oraSecondaConvocazione ==~ /^([0-1]\d|2[0-3]):([0-5]\d)$/)) {
            messaggio << "Il valore ORA SECONDA CONVOCAZIONE non è corretto (HH:mm)"
        }

        if (seduta.dataSeduta && seduta.dataInizioSeduta) {
            if (seduta.dataInizioSeduta < seduta.dataSeduta) {
                messaggio << "Il valore DATA INIZIO SEDUTA non può essere precedente alla DATA SEDUTA ("+dateFormatter.format(seduta.dataSeduta)+")"
            }
        }

        if (!seduta.dataInizioSeduta && seduta.dataFineSeduta) {
            messaggio << "Il valore DATA INZIO SEDUTA è obbligatorio se viene valorizzata la DATA FINE SEDUTA"
        }

        if (seduta.dataInizioSeduta && seduta.dataFineSeduta) {
            if (seduta.dataFineSeduta < seduta.dataInizioSeduta) {
                messaggio << "Il valore DATA FINE SEDUTA non può essere precedente alla DATA INIZIO SEDUTA ("+dateFormatter.format(seduta.dataInizioSeduta)+")"
            }
        }

        def oraFineSeduta = seduta.oraFineSeduta?.trim()

        if (seduta.dataFineSeduta && !oraFineSeduta) {
            messaggio << "Il valore ORA FINE SEDUTA è obbligatorio se viene valorizzata la DATA FINE SEDUTA"
        } else if (oraFineSeduta && !(oraFineSeduta ==~ /^([0-1]\d|2[0-3]):([0-5]\d)$/)) {
            messaggio << "Il valore ORA FINE SEDUTA non è corretto (HH:mm)"
        }

        if (messaggio.size() > 0) {
            //Clients.showNotification("Attenzione:\n${messaggio.join("\n")}", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 5000, true)
            Messagebox.show("${messaggio.join("\n\n")}.", "Attenzione", Messagebox.OK, Messagebox.EXCLAMATION)
            return false
        }

        return true
    }

    @NotifyChange([
            "seduta",
            "descrizioneSeduta",
            "tipoSeduta",
            "modificaPartecipanti",
            "numSedutaPartecipante"
    ])
    @Command
    boolean onSalva () {
        if (!checkOnSalva()) {
            return false
        }

        seduta = sedutaDTOService.salva(seduta)
        Clients.showNotification("Seduta salvata.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true);
        calcolaNumSedutaPartecipante()
        checkAbilitaTabVerbalizzazione()
        checkAbilitaTabCommissioniCollegate()
        calcolaListaStampeCommissione()
        calcolaListaNotifiche()

        BindUtils.postGlobalCommand(null, null, "onSelectDatiSeduta", null)
        BindUtils.postGlobalCommand(null, null, "onRefreshCommissione", null)
        BindUtils.postGlobalCommand(null, null, "onRefreshPartecipantiAndShowNotify", null)

        return true
    }

    @Command
    void onElimina () {
        Messagebox.show("Attenzione.\nSei sicuro di eliminare la seduta corrente ?", "Richiesta Conferma",
                        Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                        new org.zkoss.zk.ui.event.EventListener() {
                            void onEvent (Event e) {
                                if (Messagebox.ON_OK.equals(e.getName())) {
                                    sedutaDTOService.elimina(seduta);
                                    onChiudi();
                                }
                            }
                        }
        )
    }

    @Command
    void onSalvaChiudi () {
        if (onSalva())
            onChiudi()
    }

    @Command
    void onChiudi () {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }



    @NotifyChange("seduta")
    @Command
    void onPubblica (@BindingParam("pubblica") boolean pubblica) {
        seduta.pubblicaWeb = pubblica
        onSalva()
    }

    @Command
    void onStampa (@BindingParam("stampa") CommissioneStampaDTO stampa) {
        GestioneTestiModello modello = stampa.modelloTesto.domainObject

        switch (stampa.modelloTesto.tipoModello.codice) {
            case ~/${Seduta.MODELLO_TESTO_CONVOCAZIONE}.*/ :

                // carico l'impostazione dell'elenco registri delle informative per distinguerli dai registri delle proposte
                String elenco_registri = "#" + Impostazioni.ODG_ELENCO_REGISTRI_INFORMATIVE.valore + "#"

                def mappaParametri = [:]
                mappaParametri["id"] = seduta.id
                mappaParametri["elenco_registri_informative"] = elenco_registri
                mappaParametri["id_seduta_stampa"] = -1

                InputStream testoPdf = gestioneTestiService.stampaUnione(modello, mappaParametri, Impostazioni.ODG_FORMATO_STAMPE.valore, true)
                Filedownload.save(testoPdf, TipoFile.getInstanceByEstensione(Impostazioni.ODG_FORMATO_STAMPE.valore).contentType, "Convocazione")
                break
            case Seduta.MODELLO_TESTO_VERBALE:
                InputStream testoPdf = gestioneTestiService.stampaUnione(modello, [id: seduta.id], Impostazioni.ODG_FORMATO_STAMPE.valore, true)
                Filedownload.save(testoPdf, TipoFile.getInstanceByEstensione(Impostazioni.ODG_FORMATO_STAMPE.valore).contentType, "Verbale")
                break
        }
    }

    @Command
    void onNotifica (@BindingParam("notifica") NotificaDTO notifica) {
        Window w = Executions.createComponents("/odg/popupNotificheMail.zul", self, [seduta         : seduta
                                                                                     , oggettoSeduta: null
                                                                                     , notifica     : notifica])
        w.doModal()
    }

    @Command
    void onExportSedutaXml () {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd")
        // Creo XML Seduta
        def xmlString = sedutaService.creaFileXml(seduta.domainObject).toString();

        InputStream testoXml = new ByteArrayInputStream(xmlString.getBytes())

        String outputFolder = Impostazioni.FILE_REPOSITORY_PATH.valore
        String fileName = seduta.commissione.id.toString() + "_" + dateFormatter.format(seduta.dataSeduta) + "_" + seduta.oraSeduta.replace(':',
                '') + ".xml"

        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }

        File file = new File(outputFolder + File.separator + fileName);
        file.createNewFile()

        FileUtils.writeStringToFile(file, xmlString);

        Clients.showNotification("Il file è stato salvato sul percorso: ${outputFolder + File.separator + fileName}", Clients.NOTIFICATION_TYPE_INFO,
                null, "top_center", 3000, true);

        Filedownload.save(testoXml, "text/xml", fileName)
    }

    @Command
    void onExportSedutaCsv () {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd")
        String fileName = seduta.commissione.id.toString() + "_" + dateFormatter.format(seduta.dataSeduta) + "_" + seduta.oraSeduta.replace(':',
                '') + ".csv"
        // Creo Csv della Seduta
        def map = sedutaService.creaFileCsv(seduta.domainObject);

        File file = File.createTempFile("temp", ".csv")

        map.each {
            file << '"'+it.ordine+'","'+it.categoria+'","'+it.data+'","'+it.oggetto.replaceAll('[,"]', "")+'","'+it.tipologia+'"\n'
        }

        FileInputStream is = new FileInputStream(file)
        Filedownload.save(is, "text/csv", fileName)
    }

    @Command
    void onStampaUnica () {
        sedutaService.creaStampaUnica(seduta.domainObject)
    }
}
