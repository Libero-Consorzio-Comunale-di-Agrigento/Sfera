package commons

import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupScansioneViewModel {

	Window self

	String urlScansione


	@NotifyChange("urlScansione")
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("urlScansione") String urlScansione) {
		this.self = w
		this.urlScansione = urlScansione
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
