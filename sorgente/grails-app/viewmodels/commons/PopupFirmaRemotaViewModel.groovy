package commons

import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.grails.firmadigitale.FirmaDigitaleTransazione
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupFirmaRemotaViewModel {

	// service
	AttiFirmaService attiFirmaService

	// componenti
	Window self

	// dati
	FirmaDigitaleTransazione transazioneFirma
	String username		//test_collaudo3
	String password		//
	String pin			//
	String firmaTipo
	String firmaVisibile
	String pagina
	String posizione

	// stato

	@NotifyChange("urlPopupFirma")
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("transazioneFirma") FirmaDigitaleTransazione transazioneFirma,
			   @ExecutionArgParam("firmaTipo") String firmaTipo,
			   @ExecutionArgParam("firmaVisibile") String firmaVisibile,
			   @ExecutionArgParam("pagina") String pagina,
			   @ExecutionArgParam("posizione") String posizione) {
		this.self = w
		this.transazioneFirma = transazioneFirma
		this.firmaTipo = firmaTipo
		this.firmaVisibile = firmaVisibile
		this.pagina = pagina
		this.posizione = posizione

	}

//	@Command onChiudi () {
//		Events.postEvent(Events.ON_CLOSE, self, null)
//	}

	@Command
	def	onFirma() {
		attiFirmaService.firmaRemota(transazioneFirma, username, password, pin, firmaTipo, firmaVisibile, pagina, posizione)

		Events.postEvent(Events.ON_CLOSE, self, null)
	}

}
