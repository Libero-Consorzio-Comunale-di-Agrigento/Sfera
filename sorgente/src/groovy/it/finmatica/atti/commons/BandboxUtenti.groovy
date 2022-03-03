package it.finmatica.atti.commons

import atti.ricerca.MascheraRicercaDocumento
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
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
@ComponentAnnotation("utente:@ZKBIND(ACCESS=both, SAVE_EVENT=onSelectUtente)")
class BandboxUtenti extends Bandbox implements EventListener {

	@Wire("listbox")
	private Listbox listbox;
	@Wire("paging")
	private Paging paging;

    private MascheraRicercaDocumento ricerca;
	private Ad4UtenteDTO utente;
    private String tipoSoggetto;

	public BandboxUtenti () {
		Executions.createComponents("/commons/bandboxUtenti.zul", this, null);

		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);

		setAutodrop (true);
		addEventListener("onChange", 	this);
		addEventListener("onChanging", 	this);
		addEventListener("onOpen", 		this);
	}

	public void setUtente (Ad4UtenteDTO utente) {
		this.utente = utente;
		setValue(utente?.nominativoSoggetto);
	}

	public Ad4UtenteDTO getUtente () {
		return this.utente;
	}

	public void setMascheraRicerca (MascheraRicercaDocumento ricerca) {
        this.ricerca = ricerca
	}

	public MascheraRicercaDocumento getMascheraRicerca () {
        this.ricerca
	}

    public void setTipoSoggetto (String tipoSoggetto) {
        this.tipoSoggetto = tipoSoggetto
    }

    public String getTipoSoggetto () {
        return this.tipoSoggetto
    }

	@Listen("onPaging = paging")
	public void onPaging () {
		loadUtente(getValue())
	}

	@Listen("onSelect = listbox")
	public void doSelectUtente () {
		setUtente(listbox.getSelectedItem()?.getValue());
		paging.setTotalSize(1)
		listbox.setModel(new ListModelList<?>([utente]))
		close();
		Events.postEvent("onSelectUtente", this, null);
	}
	
	public void svuotaUtente() {
		this.utente = null;
		close();
		Events.postEvent("onSelectUtente", this, null);
	}

	private void loadUtente (String filtro) {
		def results = executeQuery (filtro)
		
		paging.setTotalSize(results.totalCount);
		listbox.setModel(new ListModelList<?>(results.lista));
	}
	
	public def executeQuery (String filtro) {
        if (filtro == null || filtro.trim().length() == 0) {
            filtro = "%"
        }
		filtro = filtro.replace (" ", "%")

        // se ho la maschera di ricerca E il tipo di soggetto da cercare, uso quello:
        if (ricerca != null && tipoSoggetto != null) {
            return ricerca.ricercaSoggetti (filtro, tipoSoggetto, paging.getPageSize(), paging.getActivePage())
        }

		def elenco = Ad4Utente.findAllByNominativoSoggettoIlike("%${filtro}%", [sort: "nominativoSoggetto", order: "asc", offset: paging.getPageSize() * paging.getActivePage(), max: paging.getPageSize()])
		int count  = Ad4Utente.countByNominativoSoggettoIlike("%${filtro}%")
		return [totalCount: count, lista: elenco.toDTO()]
	}
	
	public void onEvent(Event event) throws Exception {
		switch (event.name) {
    		case "onOpen":
				loadUtente(event.value);
			break;
			
			case "onChange":
			case "onChanging":
				if (getValue()?.length() > 0 && !(event.value?.trim()?.length() > 0)) {
					svuotaUtente()
				} else {
					loadUtente(event.value);
				}
				break;
			default:
				break;
		}
	}
}
