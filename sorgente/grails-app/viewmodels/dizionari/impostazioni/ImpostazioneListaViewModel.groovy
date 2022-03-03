package dizionari.impostazioni

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.dto.impostazioni.ImpostazioneDTO
import it.finmatica.atti.impostazioni.Impostazione
import it.finmatica.atti.impostazioni.ImpostazioneService
import it.finmatica.atti.impostazioni.ImpostazioniMap
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Combobox
import org.zkoss.zul.Comboitem
import org.zkoss.zul.Window

class ImpostazioneListaViewModel {

	ImpostazioniMap impostazioniMap

	// Paginazione
	int pageSize 	= AfcAbstractGrid.PAGE_SIZE_DEFAULT
	int activePage 	= 0
	int	totalSize	= 0

	// componenti
	Window self

	// dati
	HashMap 					selectedRecord
	ArrayList 					listaImpostazioniModificabili

	// services
	ImpostazioneService			impostazioneService
	String filtro=""

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		activePage 	= 0
		totalSize	= 0
		this.self = w
		caricaListaImpostazioni()
	}

	@NotifyChange(["listaImpostazioniModificabili", "totalSize"])
	private void caricaListaImpostazioni() {
		listaImpostazioniModificabili = new ArrayList()
		PagedResultList lista = Impostazione.createCriteria().list(max: pageSize, offset: pageSize * activePage) {

			// se non sono l'utente amministratore, mostro solo quelli modificabili.
			if (!AttiUtils.isUtenteAmministratore()) {
				eq ("modificabile", true)
			}

			order ("etichetta", "asc")

			if (filtro != "" && filtro != null){
				or {
					ilike("etichetta", "%"+filtro+"%")
					ilike("descrizione", "%"+filtro+"%")
				}
			}
		}
		totalSize  = lista.totalCount
		List<ImpostazioneDTO> listaImpostazioni = lista.toDTO()
		listaImpostazioni.each{
			HashMap hashMap = new HashMap()
			hashMap.put("impostazione", it)
			hashMap.put("modificato", new Boolean(false))
			listaImpostazioniModificabili.add(hashMap)
		}
	}

	@Command onChangeTxt(@BindingParam("content")HashMap content){
		content.modificato = true
	}

	@Command onCreateCbx(@BindingParam("target")Component target, @BindingParam("content")HashMap content){
		if (content?.impostazione?.caratteristiche != null) {
			def xml = new XmlSlurper().parseText(content.impostazione.caratteristiche)
			def selectedItem
			xml.children()?.each { item ->
				Comboitem ci = new Comboitem(item.@label.text())
				ci.value = item.@value.text()
				if (content.impostazione.valore == ci.value)	selectedItem = ci
				target.appendChild(ci)
			}
			((Combobox)target).setSelectedItem(selectedItem)
		}
	}

	@Command onSelectCbx(@BindingParam("target")Component target, @BindingParam("content")HashMap content){
		content.impostazione.valore = ((Combobox)target).getSelectedItem().value
		content.modificato = true
	}

	@NotifyChange(["listaImpostazioniModificabili", "totalSize"])
	@Command onPagina() {
		caricaListaImpostazioni()
	}

	@NotifyChange(["listaImpostazioniModificabili", "selectedRecord", "activePage", "totalSize"])
	@Command onRefresh () {
		selectedRecord = null
		activePage = 0
		caricaListaImpostazioni()
	}

	@Command onSalva () {
		listaImpostazioniModificabili.findAll { it.modificato == true }.each { imp ->
			impostazioneService.salva(imp.impostazione);
			imp.impostazione = (Impostazione.getImpostazione(imp.impostazione.codice, imp.impostazione.ente).get()).toDTO()
			imp.modificato   = false
		}

		impostazioniMap.refresh();

		Clients.showNotification("Impostazioni salvate.", Clients.NOTIFICATION_TYPE_INFO, self, "before_center", 3000, true);
	}

	@NotifyChange(["listaImpostazioniModificabili", "totalSize"])
	@Command onFiltro () {
		selectedRecord = null
		activePage = 0
		caricaListaImpostazioni()
	}
}
