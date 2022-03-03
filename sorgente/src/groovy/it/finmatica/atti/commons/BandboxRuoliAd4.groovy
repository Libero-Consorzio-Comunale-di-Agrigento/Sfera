package it.finmatica.atti.commons

import grails.orm.PagedResultList
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
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
@ComponentAnnotation("ruolo:@ZKBIND(ACCESS=both, SAVE_EVENT=onSelectRuolo)")
class BandboxRuoliAd4 extends Bandbox implements EventListener {

	@Wire("listbox")
	private Listbox listaRuoliAd4;
	@Wire("paging")
	private Paging paging;

	private Ad4RuoloDTO ruolo;

	public BandboxRuoliAd4 () {
		Executions.createComponents("/commons/bandboxRuoliAd4.zul", this, null);

		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);

		setAutodrop (true);
		addEventListener("onChange", this);
		addEventListener("onChanging", this);

		loadRuoli(getValue());
	}

	public void setRuolo (Ad4RuoloDTO ruolo) {
		this.ruolo = ruolo;
		setValue(ruolo?.descrizione);
	}

	public Ad4RuoloDTO getRuolo () {
		return this.ruolo;
	}

	@Listen("onPaging = paging")
	public void onPaging () {
		loadRuoli(getValue())
	}

	@Listen("onSelect = listbox")
	public void doSelectRuolo () {
		setRuolo(listaRuoliAd4.getSelectedItem()?.getValue());
		paging.setTotalSize(1)
		listaRuoliAd4.setModel(new ListModelList<?>([ruolo]))
		close();
		Events.postEvent("onSelectRuolo", this, null);
	}

	private void loadRuoli (String filtroRuoli) {
		PagedResultList elencoRuoli = Ad4Ruolo.createCriteria().list(max:paging.getPageSize(), offset: paging.getPageSize() * paging.getActivePage()) {
			or {
				ilike ("ruolo", "%"+filtroRuoli+"%")
				ilike ("descrizione", "%"+filtroRuoli+"%")
			}

			order ("descrizione", 	"asc")
			order ("ruolo", 		"asc")
		}

		paging.setTotalSize(elencoRuoli.totalCount);
		listaRuoliAd4.setModel(new ListModelList<?>(elencoRuoli.toDTO()));
	}

	public void onEvent(Event event) throws Exception {
		switch (event.name) {
			case "onChange":
				loadRuoli(getValue());
				break;
			case "onChanging":
				loadRuoli(event.getValue());
				break;
			default:
				break;
		}
	}
}
