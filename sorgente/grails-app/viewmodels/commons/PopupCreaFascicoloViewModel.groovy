package commons

import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.IProtocolloEsterno.Classifica
import it.finmatica.atti.IProtocolloEsterno.Fascicolo
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class PopupCreaFascicoloViewModel {
	// beans
	IProtocolloEsterno protocolloEsterno

	// componenti
	Window self

	// dati
	String classificaCodice
	String parentProgressivo
	String anno
	String descrizione
	List<Classifica> listaClassificazioni
	List<Fascicolo> listaFascicoli
	String filtroClassificazioni= ""
	String filtroFascicoli= ""
	Classifica selectClassifica
	Fascicolo selectFascicolo

	@NotifyChange(['listaClassificazioni','listaFascicoli','selectClassifica','selectFascicolo'])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("classificaCodice") String classificaCodice,  @ExecutionArgParam("listaFascicoli") List<Fascicolo> lf) {
		this.self = w
		selectClassifica = null
		selectFascicolo = null
		caricaListaClassificazioni()
	    if(listaClassificazioni.size()>0){
			listaClassificazioni.each {
				if(it.codice == classificaCodice)
					selectClassifica = it
			}
	    }

		if(lf.size()>0)
			listaFascicoli = lf
	}

	// metodi per il calcolo delle combobox delle classificazioni
	private void caricaListaClassificazioni() {
		listaClassificazioni = protocolloEsterno.getListaClassificazioni(filtroClassificazioni, "")
	}

	// metodi per il calcolo delle combobox delle fascicolazioni
	private void caricaListaFascicoli(){
		listaFascicoli = protocolloEsterno.getListaFascicoli(filtroFascicoli, selectClassifica.codice)
	}

	@NotifyChange(['listaClassificazioni','listaFascicoli','selectClassifica','selectFascicolo'])
	@Command onChangeClassifica() {
		if(selectClassifica!=null){
			selectFascicolo = null
			listaFascicoli = []
			BindUtils.postNotifyChange(null, null, this, "selectFascicolo")
			BindUtils.postNotifyChange(null, null, this, "listaFascicoli")
			caricaListaFascicoli()
		}
	}

	private boolean checkOnSalva () {
		String messaggio = ""

		if (selectClassifica.codice == null) {
			messaggio+= "Il valore CLASSIFICA è obbligatorio\n"
		}

		if (anno == null) {
			messaggio+= "Il valore ANNO è obbligatorio\n"
		}

		if (descrizione == null) {
			messaggio+= "Il valore DESCRIZIONE è obbligatorio\n"
		}

		if (messaggio != "") {
			Messagebox.show(messaggio, "Attenzione", Messagebox.OK, Messagebox.INFORMATION);
			return false
		}
		return true
	}

	public String fnsubstring(String codice, String descrizione){
		   int size= 65
		   descrizione = (descrizione.length()>size)?descrizione.substring(0,size)+".....":descrizione
		   return codice + " - "+ descrizione
	}

	@NotifyChange('listaFascicoli')
	@Command onSalva() {
		if (checkOnSalva()) {
			protocolloEsterno.creaFascicolo (null,anno,descrizione,selectFascicolo?.numero,selectClassifica?.codice);
			caricaListaFascicoli()
		}
	}

	@Command onSalvaChiudi() {
		onSalva()
		onChiudi()
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
