package commons

import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.OrganoControlloNotifica
import it.finmatica.atti.dto.dizionari.NotificaEmailDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.documenti.OrganoControlloNotificaDTO
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloDTO
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloDTOService
import it.finmatica.atti.dto.odg.dizionari.TipoOrganoControlloDTO
import it.finmatica.atti.odg.dizionari.OrganoControllo
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.*

class PopupNotificheOrganiDiControlloViewModel {

	// service
	OrganoControlloDTOService organoControlloDTOService

	// componenti
	Window self
	@Wire("#popupAzioni") Window popupAzioni

	// dati
	OrganoControlloNotificaDTO 	organoControlloNotifica;
	OrganoControlloDTO			organoControllo;
	GestioneTestiModelloDTO 	selectedStampa

	List							lista
	List<GestioneTestiModelloDTO> 	listaStampe
	List<TipoRegistroDTO> 			listaTipiRegistro
	List<NotificaEmailDTO>			listaNotificaEmail
	List<TipoOrganoControlloDTO> 	listaTipiOrganoDiControllo
	List<String> 					listaAmbiti = [OrganoControlloNotifica.AMBITO_DETERMINA, OrganoControlloNotifica.AMBITO_DELIBERA];

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long idNotificaOrganoDiControllo) {
		this.self = w

		if (idNotificaOrganoDiControllo > 0) {
			this.organoControlloNotifica = OrganoControlloNotifica.get(idNotificaOrganoDiControllo).toDTO();
		} else {
			this.organoControlloNotifica = new OrganoControlloNotificaDTO();
		}

		listaTipiOrganoDiControllo = OrganoControllo.createCriteria().list() {
			projections {
				property ("tipo")
			}
			eq('valido', true)
			tipo {
				eq('valido', true)
			}
			order ("sequenza", "asc")
			fetchMode("tipo", FetchMode.JOIN)
		}.toDTO();

		listaStampe = GestioneTestiModello.createCriteria().list() {
			projections {
				property ("id")
				property ("nome")
			}
			eq ("valido", true)
			eq ("tipoModello.codice", OrganoControlloNotifica.MODELLO_TESTO)
		}.collect { row -> new GestioneTestiModelloDTO(id:row[0], nome:row[1]) }
		selectedStampa = listaStampe[0]
		caricaListaDocumenti ();
	}

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
	}

	@Command onChangeAmbito() {
		listaTipiRegistro = TipoRegistro.createCriteria().list() {
			if (organoControlloNotifica.ambito == OrganoControlloNotifica.AMBITO_DETERMINA) {
				eq ("determina", true)
			}

			if (organoControlloNotifica.ambito == OrganoControlloNotifica.AMBITO_DELIBERA) {
				eq ("delibera", true)
			}
			eq ("valido", true)
		}.toDTO();
		listaTipiRegistro.add(0, new TipoRegistroDTO(codice:"", descrizione:"--seleziona--"))
		organoControlloNotifica.tipoRegistro = listaTipiRegistro[0]

		lista = [];

		BindUtils.postNotifyChange(null, null, this, "lista")
		BindUtils.postNotifyChange(null, null, this, "listaTipiRegistro")
		BindUtils.postNotifyChange(null, null, this, "organoControlloNotifica")
	}

	@Command onChiudi() {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command onVisualizza () {
		caricaListaDocumenti();
	}

	private void caricaListaDocumenti () {
		if (OrganoControlloNotifica.STATO_INVIATA.equals(organoControlloNotifica.stato)) {
			lista = organoControlloDTOService.getListaDocumentiInviati(organoControlloNotifica).toDTO();
		} else {
			lista = organoControlloDTOService.cercaDocumenti (organoControlloNotifica).toDTO();
		}
		BindUtils.postNotifyChange(null, null, this, "lista")
	}

	@Command onNotifica(@BindingParam("lista") Listbox lista) {
		organoControlloNotifica = organoControlloDTOService.preparaNotifiche(organoControlloNotifica, this.lista, selectedStampa)
		popupAzioni.doModal();
		BindUtils.postNotifyChange(null, null, this, "organoControlloNotifica")
	}

	@Command onInvia () {
		organoControlloNotifica = organoControlloDTOService.inviaNotifiche(organoControlloNotifica).toDTO();
		onVisualizza();
		popupAzioni.setVisible(false);
		BindUtils.postNotifyChange(null, null, this, "organoControlloNotifica")
		Clients.showNotification("Notifiche inviate.", Clients.NOTIFICATION_TYPE_INFO, self, "before_center", 3000, true);
	}

	@Command onStampa () {
		Filedownload.save(new ByteArrayInputStream(organoControlloNotifica.domainObject.testo.allegato), organoControlloNotifica.domainObject.testo.contentType, "Notifica")
	}

	@Command onAnnulla () {
		if (!OrganoControlloNotifica.STATO_INVIATA.equals(organoControlloNotifica.stato)) {
			organoControlloDTOService.eliminaNotifica(organoControlloNotifica)
		}
		popupAzioni.setVisible(false);
	}

	@Command onApriDocumento (@ContextParam(ContextType.COMPONENT) Listitem l) {
		if (organoControlloNotifica.ambito.equals(OrganoControlloNotifica.AMBITO_DETERMINA)) {
			Executions.createComponents("/atti/documenti/determina.zul", self, [id : l.value.id]).doModal()
		}

		if (organoControlloNotifica.ambito.equals(OrganoControlloNotifica.AMBITO_DELIBERA)) {
			Executions.createComponents("/atti/documenti/delibera.zul", self, [id : l.value.id]).doModal()
		}
	}
	
	@Command onCancellaNotifica () {
		Messagebox.show("Eliminare la notifica selezionata?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						organoControlloDTOService.eliminaNotifica(organoControlloNotifica)
						onChiudi();
					}
				}
		});
	}
}
