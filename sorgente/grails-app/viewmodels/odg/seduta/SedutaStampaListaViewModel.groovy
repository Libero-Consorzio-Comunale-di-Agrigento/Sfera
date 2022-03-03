package odg.seduta

import it.finmatica.atti.dto.odg.SedutaDTO
import it.finmatica.atti.dto.odg.SedutaStampaDTO
import it.finmatica.atti.integrazioni.lettera.IntegrazioneLetteraAgspr
import it.finmatica.atti.integrazioni.pec.IntegrazionePecDucd
import it.finmatica.atti.odg.CommissioneStampa
import it.finmatica.atti.odg.Seduta
import it.finmatica.atti.odg.SedutaService
import it.finmatica.atti.odg.SedutaStampa
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Listbox
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class SedutaStampaListaViewModel {

    private Listbox self

    IntegrazioneLetteraAgspr integrazioneLetteraAgspr
    IntegrazionePecDucd integrazionePecDucd
    SedutaService sedutaService

    String                tipoStampa
    List<SedutaStampaDTO> listaStampe = []
    SedutaDTO             seduta

    @Init
    void init (
            @ContextParam(ContextType.COMPONENT) Listbox w,
            @ExecutionArgParam("seduta") SedutaDTO seduta, @ExecutionArgParam("tipo") String tipoStampa) {
        this.self = w
        this.tipoStampa = tipoStampa
        this.seduta = seduta
        caricaListaStampe(seduta.id, tipoStampa)
    }

    @Command
    void onApriStampa (@BindingParam("stampa") SedutaStampaDTO stampa) {
        Window w = Executions.createComponents("/odg/seduta/sedutaStampa.zul", null, [seduta: seduta, tipo: tipoStampa, id: stampa?.id])
        w.onClose {
            caricaListaStampe(this.seduta.id, this.tipoStampa)
        }
        w.doModal()
    }

    @GlobalCommand
    @NotifyChange(["nuovaStampaAbilitato", "messaggioListaVuota"])
    void onSelectDatiSeduta () {
        // lasciato intenzionalmente vuoto. serve solo per consentire il refresh dei due campi messi nella @NotifyChange
    }

    @Command
    boolean isNuovaStampaAbilitato () {
        if (tipoStampa == CommissioneStampa.CONVOCAZIONE && seduta.id > 0) {
            return true
        }

        if (tipoStampa == CommissioneStampa.VERBALE && sedutaService.isTutteDelibereConEsitoConfermato(seduta.domainObject)) {
            return true
        }

        return false
    }

    @Command
    void onEliminaStampa (@BindingParam("stampa") SedutaStampaDTO sedutaStampa) {
        Messagebox.show("Eliminare la stampa?", "Attenzione", Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
                        new org.zkoss.zk.ui.event.EventListener() {
                            void onEvent (Event e) {
                                if (Messagebox.ON_OK.equals(e.getName())) {
                                    SedutaStampa stampa = sedutaStampa.domainObject
                                    stampa.valido = false
                                    stampa.save()

                                    caricaListaStampe(seduta.id, tipoStampa)
                                }
                            }
                        }
        )
    }

    @Command
    void apriRicevutaPec (@BindingParam("sedutaStampa") SedutaStampaDTO sedutaStampa) {
        String urlRicevuta = integrazionePecDucd.getUrlRicevuta(sedutaStampa.domainObject)
        Executions.getCurrent().sendRedirect(urlRicevuta, "_blank")
    }

    @Command
    void apriDocumentoLettera (@BindingParam("sedutaStampa") SedutaStampaDTO sedutaStampa) {
        if (integrazioneLetteraAgspr.isLetteraPresente(sedutaStampa.idDocumentoLettera)) {
            String urlDocumentoLettera = integrazioneLetteraAgspr.getUrlLettera(sedutaStampa.idDocumentoLettera)
            Executions.getCurrent().sendRedirect(urlDocumentoLettera, "_blank")
        }
        else {
            Clients.showNotification("Documento non presente.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 5000, true);
        }
    }

    String getMessaggioListaVuota () {
        if (tipoStampa == CommissioneStampa.VERBALE) {
            if (!isNuovaStampaAbilitato()) {
                return "Sarà possibile inserire nuovi Verbali solo quando tutte le proposte discusse avranno un esito confermato."
            }
        }

        if (tipoStampa == CommissioneStampa.CONVOCAZIONE) {
            if (!(seduta.id > 0)) {
                return "Sarà possibile inserire nuove Convocazioni solo dopo aver salvato la seduta."
            }
        }

        return "Non è presente alcuna stampa."
    }

    private void caricaListaStampe (long idSeduta, String tipoStampa) {
        listaStampe = SedutaStampa.createCriteria().list {
            eq("valido", true)
            eq("seduta.id", idSeduta)
            commissioneStampa {
                eq("codice", tipoStampa)
            }
        }.toDTO()

        BindUtils.postNotifyChange(null, null, this, "listaStampe")
    }
}
