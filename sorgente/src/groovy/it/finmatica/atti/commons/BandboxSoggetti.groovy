package it.finmatica.atti.commons

import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.annotation.ComponentAnnotation
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.EventListener
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Listen
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Bandbox
import org.zkoss.zul.ListModelList
import org.zkoss.zul.Listbox
import org.zkoss.zul.Paging

// documentazione ZK: http://books.zkoss.org/wiki/ZK_Developer%27s_Reference/UI_Composing/Composite_Component
// esempio ZK: http://www.zkoss.org/zkdemo/listbox/dual_listbox

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver)
@ComponentAnnotation("soggetto:@ZKBIND(ACCESS=both, SAVE_EVENT=onSelectSoggetto)")
class BandboxSoggetti extends Bandbox implements EventListener {

	@Wire("listbox")
	private Listbox listbox;
	@Wire("paging")
	private Paging paging;

	private As4SoggettoCorrenteDTO soggetto;

	public BandboxSoggetti () {
		Executions.createComponents("/commons/bandboxSoggetti.zul", this, null);

		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);

		setAutodrop (true);
		addEventListener("onChange", 	this);
		addEventListener("onChanging", 	this);
		addEventListener("onOpen", 		this);
	}

	public void setSoggetto (As4SoggettoCorrenteDTO soggetto) {
		this.soggetto = soggetto;
		setValue(soggetto?.denominazione);
	}

	public As4SoggettoCorrenteDTO getSoggetto () {
		return this.soggetto;
	}

	@Listen("onPaging = paging")
	public void onPaging () {
		loadSoggetto(getValue())
	}

	@Listen("onSelect = listbox")
	public void doSelectSoggetto () {
		setSoggetto(listbox.getSelectedItem()?.getValue());
		paging.setTotalSize(1)
		listbox.setModel(new ListModelList<?>([soggetto]))
		close();
		Events.postEvent("onSelectSoggetto", this, null);
	}
	
	public void svuotaSoggetto() {
		this.soggetto = null;
		close();
		Events.postEvent("onSelectSoggetto", this, null);
	}

	private void loadSoggetto (String filtro) {
		def results = executeQuery (filtro)
		
		paging.setTotalSize(results.totalCount);
		listbox.setModel(new ListModelList<?>(results.lista));
	}
	
	public def executeQuery (String filtro) {
		filtro = filtro.replace (" ", "%")
		def elenco = As4SoggettoCorrente.findAllByDenominazioneIlike("%${filtro}%", [sort: "denominazione", order: "asc", offset: paging.getPageSize() * paging.getActivePage(), max: paging.getPageSize()])
		int count  = As4SoggettoCorrente.countByDenominazioneIlike("%${filtro}%")
		return [totalCount: count, lista: elenco.toDTO()]
	}
	
	public void onEvent(Event event) throws Exception {
		switch (event.name) {
    		case "onOpen":
				loadSoggetto(event.value);
			break;
			
			case "onChange":
			case "onChanging":
				if (getValue()?.length() > 0 && !(event.value?.trim()?.length() > 0)) {
					svuotaSoggetto()
				} else {
					loadSoggetto(event.value);
				}
				break;
			default:
				break;
		}
	}
}
