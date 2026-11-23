package commons

import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.IProtocolloEsterno.Fascicolo
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupFascicoliAurigaViewModel {
    // beans
    IProtocolloEsterno protocolloEsterno

    // componenti
    Window self

    // dati
    List<Fascicolo>	listaFascicoli
    Fascicolo		selectedFascicolo
    String 			classificaCodice
    String 			classificaDescrizione
    String			codiceUoProponente
    Date 			classificaDal

    // stato
    String filtro 	= ""
    //anno
    int anno        = Calendar.getInstance().get(Calendar.YEAR)

    @NotifyChange(['listaFascicoli','abilitaCreaFascicolo'])
    @Init init(@ContextParam(ContextType.COMPONENT) 		Window w
               , @ExecutionArgParam("classificaCodice") 		String classificaCodice
               , @ExecutionArgParam("classificaDescrizione") 	String classificaDescrizione
               , @ExecutionArgParam("classificaDal") 			Date classificaDal
               , @ExecutionArgParam("codiceUoProponente") 	String codiceUoProponente) {
        this.self = w
        this.classificaDescrizione 	= classificaDescrizione
        this.classificaCodice 		= classificaCodice
        this.codiceUoProponente 	= codiceUoProponente
        this.classificaDal			= classificaDal
        listaFascicoli 				= protocolloEsterno.getListaFascicoliAnno(anno, filtro, classificaCodice, classificaDal, codiceUoProponente)
    }

    @NotifyChange('listaFascicoli')
    @Command onCerca() {
        listaFascicoli = protocolloEsterno.getListaFascicoliAnno(anno, filtro, classificaCodice, classificaDal, codiceUoProponente)
    }

    @Command onSalva() {
        Events.postEvent(Events.ON_CLOSE, self, selectedFascicolo)
    }

    @Command onAnnulla () {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @NotifyChange(["listaFascicoli", "classificaCodice", "classificaDescrizione"])
    @Command onSvuotaClassifica () {
        classificaCodice 		= "";
        classificaDescrizione 	= "";
        classificaDal			= null;
        onCerca();
    }

    @NotifyChange(["classificaCodice", "classificaDescrizione"])
    @Command onAggiornaClassifica () {
        if (selectedFascicolo == null) {
            return;
        }

        classificaCodice 		= selectedFascicolo.classifica.codice;
        classificaDescrizione 	= selectedFascicolo.classifica.descrizione;
        classificaDal			= selectedFascicolo.classifica.dal
    }
}
