package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.CalendarioFestivita
import it.finmatica.atti.dizionari.CalendarioFestivitaService
import it.finmatica.atti.dto.dizionari.CalendarioFestivitaDTO
import org.hibernate.FetchMode
import org.springframework.core.env.PropertyResolver
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class CalendarioFestivitaDettaglioViewModel extends AfcAbstractRecord {

    int pageSize   = 10
    int activePage = 0
    int totalSize  = 0

    // services
    CalendarioFestivitaService calendarioFestivitaService

    @NotifyChange(["selectedRecord", "totalSize"])
    @Init
    init (@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
        this.self = w

        // Inizializo le variabili di classe
        activePage = 0

        if (id != null) {
            selectedRecord = caricaCalendarioFestivitaDto(id)
            aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
            aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
        } else {
            selectedRecord = new CalendarioFestivitaDTO(valido: true, giorno: 1, mese: 1)
        }
    }

    private CalendarioFestivitaDTO caricaCalendarioFestivitaDto (Long idCalendarioFestivita) {
        CalendarioFestivita calendarioFestivita = CalendarioFestivita.createCriteria().get {
            eq("id", idCalendarioFestivita)
            fetchMode("utenteIns", FetchMode.JOIN)
            fetchMode("utenteUpd", FetchMode.JOIN)
        }
        return calendarioFestivita.toDTO()
    }

    //////////////////////////////////////////
    //				SALVATAGGIO				//
    //////////////////////////////////////////

    @NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
    @Command
    onSalva () {
        boolean isNuovoCalendarioFestivita = (selectedRecord.id == null)
        def id = calendarioFestivitaService.salva(selectedRecord).id
        selectedRecord = caricaCalendarioFestivitaDto(id)
        if (isNuovoCalendarioFestivita) {
            aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
        }
        aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
    }

    @NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
    @Command
    onSalvaChiudi () {
        onSalva()
        onChiudi()
    }

    @Command
    onSettaValido (@BindingParam("valido") boolean valido) {
        Messagebox.show("Modificare la validità della voce calendario?", "Modifica validità",
                        Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                        new org.zkoss.zk.ui.event.EventListener() {
                            public void onEvent (Event e) {
                                if (Messagebox.ON_OK.equals(e.getName())) {
                                    super.getSelectedRecord().valido = valido
                                    onSalva()
                                    BindUtils.postNotifyChange(null, null, CalendarioFestivitaDettaglioViewModel.this, "selectedRecord")
                                    BindUtils.postNotifyChange(null, null, CalendarioFestivitaDettaglioViewModel.this, "datiCreazione")
                                    BindUtils.postNotifyChange(null, null, CalendarioFestivitaDettaglioViewModel.this, "datiModifica")
                                }
                            }
                        }
        )
    }

}
