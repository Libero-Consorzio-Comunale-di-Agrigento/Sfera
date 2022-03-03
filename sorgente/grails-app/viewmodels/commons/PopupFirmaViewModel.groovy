package commons

import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupFirmaViewModel {

	// componenti
	Window self

	// dati
	String urlPopupFirma

	@NotifyChange("urlPopupFirma")
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("urlPopupFirma") String urlPopupFirma) {
		this.self = w

		// ZK aggiunge il contextPath agli url relativi negli iframe. Questo significa che se urlPopupFirma è '/UploadDownload', l'iframe punterà a: '/Atti/UploadDownload'.
		// ovviamente questo non va bene perché deve puntare al contesto 'UploadDownload'.
		// Per ovviare a questo problema, se l'url passato inizia con '/', aggiungo un '..' così da scendere di un livello.
		// Questa soluzione fa un po' schifo ma è la più semplice che ho trovato: ci sono alcuni clienti che
		// sono installati con percorsi "strani" ad es USL Napoli che ha i contesti installati sotto /webapps/jsuite con il risultato
		// che il contextPath sarebbe /jsuite/Atti e la UploadDownload si trova sotto /jsuite/UploadDownload.
		// Per questa ragione, non si può neanche prendere il banale "urlserver" come arriva dalla request, ma è necessario fare questo trucco.
		// Va sempre ricordato poi che è possibile configurare nel plugin di firma, l'url assoluto a cui puntare la pagina di firma, per quei casi
		// complicati in cui proprio non se ne esce.
		if (urlPopupFirma.startsWith("/")) {
			urlPopupFirma = "/.."+urlPopupFirma
		}

		this.urlPopupFirma = urlPopupFirma
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
