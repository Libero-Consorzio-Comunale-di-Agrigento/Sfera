import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestionetesti.GestioneTestiService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.Page

class IndexViewModel {

	// services
	def springSecurityService
	def grailsApplication
	def pluginManager
	StrutturaOrganizzativaService	strutturaOrganizzativaService
	GestioneTestiService			gestioneTestiService
	
	// componenti

	// sezioni (referenziate anche dai bottoni)
	def sezioni = [ atti: 	 		"/atti/index.zul"
				  , odg:			"/odg/index.zul"
				  , dizionari: 		"/dizionari/index.zul"
				  , conservazione: 	"/conservazione/index.zul"]
	String selectedSezione = "atti"
	String urlSezione

	boolean gestioneSeduteVisible = false
	boolean conservazioneVisible  = false
	boolean dizionariVisible  	  = false

	@NotifyChange("urlSezione")
    @org.zkoss.bind.annotation.Init
    init (@ContextParam(ContextType.PAGE) Page page) {
		gestioneTestiService.setDefaultEditor(Impostazioni.EDITOR_DEFAULT.valore, Impostazioni.EDITOR_DEFAULT_PATH.valore);
	
		urlSezione = sezioni.atti
		boolean isUtenteAmministratore = AttiUtils.isUtenteAmministratore();
		gestioneSeduteVisible  = isUtenteAmministratore || springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_ODG.valore);
		conservazioneVisible   = isUtenteAmministratore || springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_CONSERVAZIONE.valore);
		boolean dizAttiVisible = isUtenteAmministratore || springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_DIZIONARI_ATTI.valore);
		boolean dizOdgVisible  = isUtenteAmministratore || springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_DIZIONARI_ODG.valore);
        boolean dizImpVisible = isUtenteAmministratore || springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_DIZIONARI_IMPOSTAZIONI.valore);
        boolean dizBudVisible = isUtenteAmministratore || springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_DIZIONARI_BUDGET.valore);
		dizionariVisible	   = (dizAttiVisible || dizOdgVisible || dizImpVisible || dizBudVisible);
    }

	public List<String> getPatterns () {
		return sezioni.collect { it.key }
	}

    @Command
    onOpenInformazioniUtente () {
		Executions.createComponents("/system/informazioniUtente.zul", null, null).doModal();
	}

    @Command
    apriSezione (@BindingParam("sezione") String sezione) {
		if ((sezione == "odg" && !gestioneSeduteVisible) || (sezione == "conservazione" && !conservazioneVisible) || (sezione == "dizionari" && !dizionariVisible)){
			selectedSezione = "atti"
		} else {
			selectedSezione = sezione
		}
		urlSezione 		= sezioni[selectedSezione]

		BindUtils.postNotifyChange(null, null, this, "urlSezione")
		BindUtils.postNotifyChange(null, null, this, "selectedSezione")
	}

    @Command
    doLogout () {
		Executions.sendRedirect("/logout")
	}

    public String getUtenteCollegato () {
		return springSecurityService.principal.cognomeNome
	}

	public String getVersioneApplicazione () {
		if (AttiUtils.isUtenteAmministratore()) {
			String versione 	= grailsApplication.metadata['app.version'];
			String attiCf		= springSecurityService.currentUser.nominativo?.equals("AGSDE2") && Impostazioni.CONTABILITA.valore.equals("integrazioneContabilitaCfa") ? pluginManager.plugins.attiCF?.version : null
			return "© Gruppo Finmatica - AGSDE v$versione " + (attiCf ? " - AttiCF v$attiCf" : "");
		} else {
			String versione = grailsApplication.metadata['app.versione.istanza'];
			return "© Gruppo Finmatica - AGSDE v$versione";
		}
	}

	public String getNomeApplicazione () {
		return grailsApplication.metadata['app.name']
	}

	public String getAmministrazione () {
		return springSecurityService.principal.amm().descrizione
	}
}
