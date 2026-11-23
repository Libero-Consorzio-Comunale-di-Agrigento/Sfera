package commons

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.IProtocolloEsterno.Fascicolo
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioni.protocollo.ProtocolloPiTre
import it.finmatica.atti.integrazioniws.pitre.classes.CreateProjectResponse
import it.finmatica.atti.integrazioniws.pitre.classes.Project
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Window

class PopupFascicoliPiTreViewModel {
    // beans
    IProtocolloEsterno protocolloEsterno

    ProtocolloPiTre protocolloPiTre
    StrutturaOrganizzativaService strutturaOrganizzativaService
    SpringSecurityService springSecurityService
    // componenti
    Window self

    @Wire("#popupCreaFascicoloPiTre")
    Window  popupCreaFascicoloPiTre

    // dati
    List<Fascicolo>	listaFascicoli =new ArrayList<Fascicolo>()
    Fascicolo		selectedFascicolo


    So4UnitaPubb unitaProponente
    List<So4UnitaPubb> listaUnitaProponente = new ArrayList<So4UnitaPubb>()

    String codice = ""
    String descrizione = ""
    String tipologia =""

    String codiceNuovoFascicolo = ""
    String descrizioneNuovoFascicolo = ""
    Ad4Utente utenteRedattore =null
    boolean riservatoNuovoFascicolo
    boolean sceltaUnitaProponente

    //anno
    int anno        = Calendar.getInstance().get(Calendar.YEAR)

    @NotifyChange(['listaFascicoli','abilitaCreaFascicolo'])
    @Init init(@ContextParam(ContextType.COMPONENT) 		Window w
                , @ExecutionArgParam("unitaProponente") 	So4UnitaPubb unitaProponente
                , @ExecutionArgParam("tipologia")           String tipologia
               , @ExecutionArgParam("sceltaUnitaProponente")           boolean sceltaUnitaProponente,
               @ExecutionArgParam("utenteRedattore")  Ad4Utente utenteRedattore
        ) {
        this.self = w
        this.unitaProponente=unitaProponente
        this.tipologia=tipologia
        this.sceltaUnitaProponente=sceltaUnitaProponente
        this.utenteRedattore=utenteRedattore

            listaUnitaProponente = strutturaOrganizzativaService.getUnitaInOttica(springSecurityService.principal.ottica().codice)
            listaUnitaProponente.sort { it.descrizione ?: '' }

    }

    So4UnitaPubb getUnitaProponente() {
        return unitaProponente
    }

    void setUnitaProponente(So4UnitaPubb unitaProponente) {
        this.unitaProponente = unitaProponente
    }


    @NotifyChange('listaFascicoli')
    @Command onCerca() {
        listaFascicoli = protocolloEsterno.getListaFascicoli(codice, descrizione, anno, unitaProponente, tipologia, utenteRedattore)
    }

    @Command onSalva() {
        if(selectedFascicolo!=null && selectedFascicolo.riservato){
            selectedFascicolo.oggetto="*descrizione non visualizzata*"
        }
        Events.postEvent(Events.ON_CLOSE, self, selectedFascicolo)
    }

    @Command onAnnulla () {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }



    //Comeandi PopupCreaFascicolo
    @Command
    void onChiudiCreaFascicolo () {
        popupCreaFascicoloPiTre.visible = false;
    }

    @Command
    void onOpenPopupCreaFascicolo(){
        popupCreaFascicoloPiTre.doModal()
    }

    @NotifyChange('listaFascicoli')
    @Command
    void onCreaFascicolo(){
        CreateProjectResponse response
        Project nuovoFascicolo
        try {
            response = protocolloPiTre.creaFascicolo(codiceNuovoFascicolo, descrizioneNuovoFascicolo, riservatoNuovoFascicolo, unitaProponente,utenteRedattore)
            if(response.Code==0){
                nuovoFascicolo=response.Project
                if(nuovoFascicolo!=null){
                    Fascicolo nuovo = new Fascicolo()
                    nuovo.numero=nuovoFascicolo.Code
                    nuovo.oggetto=nuovoFascicolo.Description
                    nuovo.sub=nuovoFascicolo.Id
                    nuovo.riservato=nuovoFascicolo.Private
                    listaFascicoli.add(nuovo)
                    BindUtils.postNotifyChange(null, null, this, "listaFascicoli")
                }
            } else {
                throw new Exception("[P4] "+response.Code + ":"+ response.ErrorMessage)
            }

        }catch (Exception ex){
            throw new AttiRuntimeException("[P4] Errore in fase di creazione fascicolo: ${ex.getMessage()}.", ex);
        }
        if(nuovoFascicolo!=null)
            popupCreaFascicoloPiTre.visible = false
    }
}
