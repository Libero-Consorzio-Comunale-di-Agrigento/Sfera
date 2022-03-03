package assistenza

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.commons.AttiUtils
import org.zkoss.bind.annotation.AfterCompose
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Window

class AssistenzaViewModel {

	// services
	SpringSecurityService springSecurityService
	Window self;

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		if (!AttiUtils.isUtenteAmministratore() && !springSecurityService.principal.hasRuolo("AGDASSI")) {
			Clients.showNotification("Non è possibile accedere alla pagina: non si dispone dei necessari diritti di accesso!", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 5000, false);
			Events.postEvent(Events.ON_CLOSE, self, null)
		}
	}

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
	}
}
