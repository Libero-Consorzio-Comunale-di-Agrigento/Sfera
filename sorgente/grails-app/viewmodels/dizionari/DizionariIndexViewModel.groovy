package dizionari

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.impostazioni.Impostazioni
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Window

class DizionariIndexViewModel {

    // componenti
    Window self

    // stato
    String selectedSezione
	String urlSezione
	def pagineDizionarioAtti = [
	  "tipoDetermina" 				: "/dizionari/atti/tipoDeterminaLista.zul"
	, "tipoDelibera"  				: "/dizionari/atti/tipoDeliberaLista.zul"
	, "tipoCertificato"  			: "/dizionari/atti/tipoCertificatoLista.zul"
	, "tipoVistoParere" 			: "/dizionari/atti/tipoVistoParereLista.zul"
	, "email"              			: "/dizionari/atti/emailLista.zul"
	, "delega"              		: "/dizionari/atti/delegaLista.zul"
	, "notifica"           			: "/dizionari/atti/notificaLista.zul"
	, "tipoRegistro"           		: "/dizionari/atti/tipoRegistroLista.zul"
	, "registroUnita"           	: "/dizionari/atti/registroUnitaLista.zul"
	, "tipoAllegato"         		: "/dizionari/atti/tipoAllegatoLista.zul"
	, "oggettoRicorrente"          	: "/dizionari/atti/oggettoRicorrenteLista.zul"
	, "indirizzoDelibera"          	: "/dizionari/atti/indirizzoDeliberaLista.zul"
	, "gestioneTestiModello"        : "/dizionari/impostazioni/gestioneTestiModelloLista.zul"
	, "calendarioFestivita"         : "/dizionari/atti/calendarioFestivitaLista.zul"
    , "datiAggiuntivi"              : "/dizionari/atti/tipoDatoAggiuntivoValoreLista.zul"
	]

    def pagineDizionarioControlloRegolarita = [
            "tipiControlloRegolarita"   : "/dizionari/atti/tipoControlloRegolaritaLista.zul"
            , "esitiControlloRegolarita": "/dizionari/atti/esitoControlloRegolaritaLista.zul"
    ]

    def pagineDizionarioOdg = [
            "commissione"      : "/dizionari/odg/commissioneLista.zul"
            , "esito"          : "/dizionari/odg/esitoLista.zul"
            , "voto"           : "/dizionari/odg/votoLista.zul"
            , "organoControllo": "/dizionari/odg/organoControlloLista.zul"
            , "tipoSeduta"     : "/dizionari/odg/tipoSedutaLista.zul"
            , "incarichi"      : "/dizionari/odg/incaricoLista.zul"
            , "importoGettone" : "/dizionari/odg/importoGettoneLista.zul"
    ]

    def pagineDizionarioImpostazioni = [
            "caratteristica"            : "/dizionari/atti/caratteristicaTipologiaLista.zul"
            , "impostazione"            : "/dizionari/impostazioni/impostazioneLista.zul"
            , "mappingIntegrazioni"     : "/dizionari/impostazioni/mappingIntegrazioniLista.zul"
            , "gestioneTestiTipoModello": "/dizionari/impostazioni/gestioneTestiTipoModelloLista.zul"
            , "lockTesti"               : "/gestionetesti/ui/funzionalita/lockDocumentiLista.zul"
            , "lockDocumenti"           : "/dizionari/impostazioni/lockAttiLista.zul"
            , "gestioneUnita"           : "/dizionari/impostazioni/cambioUnitaLista.zul"
            , "cambioUtente"            : "/dizionari/impostazioni/cambioUtenteLista.zul"
            , "gestioneDocumenti"       : "/dizionari/impostazioni/gestioneDocumenti.zul"
            , "regoleCampi"             : "/dizionari/impostazioni/regolaCampoLista.zul"
    ]

    def pagineDizionarioBudget = [
            "budget"                    : "/dizionari/atti/tipoBudgetLista.zul"
    ]

    def pagineDizionari = new HashMap()

    boolean attiDisabled = true
    boolean odgDisabled = true
    boolean budgetDisabled = true
    boolean impostazioniDisabled = true
    boolean categoriaDeterminaDisabled = true
    boolean datoAggiuntivoDisabled = true
    boolean categoriaPropostaDeliberaDisabled = true
    boolean gettonePresenzaDisabled = true
    boolean calendarioFestivitaDisabled = true
    boolean configuratoreIterDisabled = true

    StrutturaOrganizzativaService strutturaOrganizzativaService
    SpringSecurityService springSecurityService

    @Init
    init (@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w

        boolean isUtenteAmministratore = AttiUtils.isUtenteAmministratore();
        attiDisabled = !(springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_DIZIONARI_ATTI.valore) || isUtenteAmministratore);
        odgDisabled = !(springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_DIZIONARI_ODG.valore) || isUtenteAmministratore);
        impostazioniDisabled = !(springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_DIZIONARI_IMPOSTAZIONI.valore) || isUtenteAmministratore);
        configuratoreIterDisabled = !(isUtenteAmministratore);

        if (!Impostazioni.GESTIONE_BUDGET.abilitato) {
            budgetDisabled = true;
        } else {
            budgetDisabled = !(springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_DIZIONARI_BUDGET.valore) || isUtenteAmministratore);
        }

        if (!Impostazioni.CATEGORIA_DETERMINA.abilitato) {
            categoriaDeterminaDisabled = true;
        } else {
            categoriaDeterminaDisabled = false;
            pagineDizionarioAtti["categoriaDetermina"] = "/dizionari/atti/categoriaDeterminaLista.zul"
        }

        gettonePresenzaDisabled = !Impostazioni.ODG_GETTONE_PRESENZA_ATTIVO.abilitato

        if (!Impostazioni.CATEGORIA_PROPOSTA_DELIBERA.abilitato) {
            categoriaPropostaDeliberaDisabled = true
        } else {
            categoriaPropostaDeliberaDisabled = false
            pagineDizionarioAtti["categoriaPropostaDelibera"] = "/dizionari/atti/categoriaPropostaDeliberaLista.zul"
        }

        if (!attiDisabled) {
            pagineDizionari.putAll(pagineDizionarioAtti)
        }

        if (!odgDisabled) {
            pagineDizionari.putAll(pagineDizionarioOdg)
        }

        if (!impostazioniDisabled) {
            pagineDizionari.putAll(pagineDizionarioImpostazioni)
        }

        if (!impostazioniDisabled) {
            pagineDizionari.putAll(pagineDizionarioControlloRegolarita)
        }

        if (!budgetDisabled) {
            pagineDizionari.putAll(pagineDizionarioBudget)
        }

        if (Impostazioni.DATI_AGGIUNTIVI.valore != "N") {
            datoAggiuntivoDisabled = true
        }

        if (Impostazioni.ESECUTIVITA_SOLO_GIORNI_FERIALI.abilitato) {
            calendarioFestivitaDisabled = false
        }
    }

    public List<String> getPatterns () {
        return pagineDizionari.collect { it.key }
    }

    public void setSelectedSezione (String value) {
        if (value == null || value.length() == 0) {
            urlSezione = null
        }

        selectedSezione = value
        urlSezione = pagineDizionari[selectedSezione]
        BindUtils.postNotifyChange(null, null, this, "urlSezione")
    }

    @Command
    apriConfiguratoreIter () {
        Executions.getCurrent().sendRedirect("/configuratoreiter/index.zul","_blank")
    }
}
