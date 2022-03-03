package it.finmatica.atti.commons

import atti.ricerca.MascheraRicercaDocumento
import grails.orm.PagedResultList
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
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
@ComponentAnnotation("unita:@ZKBIND(ACCESS=both, SAVE_EVENT=onSelectUnita)")
class BandboxUnitaSo4 extends Bandbox implements EventListener {

	@Wire("listbox")
	private Listbox listaUnitaSo4;
	@Wire("paging")
	private Paging paging;

	private So4UnitaPubbDTO unita;
	private MascheraRicercaDocumento ricerca;

	public BandboxUnitaSo4 () {
		Executions.createComponents("/commons/bandboxUnitaSo4.zul", this, null);

		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);

		setAutodrop (true);
		addEventListener("onChange", 	this);
		addEventListener("onChanging", 	this);
		addEventListener("onOpen", 		this);
	}

	public void setUnita(So4UnitaPubbDTO unita) {
		this.unita = unita;
		setValue(unita?.descrizione);
	}
	
	public So4UnitaPubbDTO getUnita () {
		return this.unita;
	}
	
	public void svuotaUnita() {
		this.unita = null;
		close();
		Events.postEvent("onSelectUnita", this, null);
	}

	public void setMascheraRicerca (MascheraRicercaDocumento ricerca) {
		this.ricerca = ricerca
	}

	public MascheraRicercaDocumento getMascheraRicerca () {
		this.ricerca
	}

	@Listen("onPaging = paging")
	public void onPaging () {
		loadUnita(getValue())
	}

	@Listen("onSelect = listbox")
	public void doSelectUnita () {
		setUnita(listaUnitaSo4.getSelectedItem()?.getValue());
		close();
		paging.setTotalSize(1)
		listaUnitaSo4.setModel(new ListModelList<?>([unita]))
		Events.postEvent("onSelectUnita", this, null);
	}

	private void loadUnita (String filtro) {
		def results = executeQuery (filtro)
		paging.setTotalSize(results.totalCount);
		listaUnitaSo4.setModel(new ListModelList<?>(results.lista));
	}
	
	public def executeQuery (String filtro) {
		if (ricerca != null) {
			return executeQueryMaschera (filtro)
		}

		Date data = new Date();
		PagedResultList elencoUnita = So4UnitaPubb.createCriteria().list(max:paging.getPageSize(), offset: paging.getPageSize() * paging.getActivePage()) {
			or {
				ilike ("descrizione", "%"+filtro+"%")
				ilike ("codice", "%"+filtro+"%")
			}

			le("dal", data)
			or {
				isNull("al")
				ge("al",  data)
			}

			order ("descrizione", 	"asc")
			order ("codice", 		"asc")
		}

		return [totalCount: elencoUnita.totalCount, lista: elencoUnita.toDTO()]
	}

	private def executeQueryMaschera (String filtro) {
		if (filtro == null || filtro.trim().length() == 0) {
			filtro = "%"
		}
		filtro = filtro.replace (" ", "%")

		// se ho la maschera di ricerca E il tipo di soggetto da cercare, uso quello:
		return ricerca.ricercaUoProponente (filtro, paging.getPageSize(), paging.getActivePage())
	}
	
	public void onEvent(Event event) throws Exception {
		switch (event.name) {
    		case "onOpen":
				loadUnita(event.value);
			break;
			
			case "onChange":
			case "onChanging":
				if (getValue()?.length() > 0 && !(event.value?.trim()?.length() > 0)) {
					svuotaUnita()
				} else {
					loadUnita(event.value);
				}
				break;
			default:
				break;
		}
	}
}
