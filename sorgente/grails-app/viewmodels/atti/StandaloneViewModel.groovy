package atti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.PropostaDeliberaSoggetto
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestionetesti.GestioneTestiService
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.bind.annotation.QueryParam
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Window

class StandaloneViewModel {

	// services
	SpringSecurityService springSecurityService
	GestioneTestiService gestioneTestiService

	// componenti
	Window self

	// dati
	def tipiOggetto 	=  [(Delibera.TIPO_OGGETTO)          : '/atti/documenti/delibera.zul'
						  ,	(Determina.TIPO_OGGETTO)      	 : '/atti/documenti/determina.zul'
						  , (VistoParere.TIPO_OGGETTO)       : '/atti/documenti/visto.zul'
						  , (VistoParere.TIPO_OGGETTO_PARERE): '/atti/documenti/parere.zul'
						  , (Certificato.TIPO_OGGETTO)       : '/atti/documenti/certificato.zul'
						  , (PropostaDelibera.TIPO_OGGETTO)  : '/atti/documenti/propostaDelibera.zul'
						  , (SedutaStampa.TIPO_OGGETTO)      : '/odg/seduta/sedutaStampa.zul' ]

	@Init init (@ContextParam(ContextType.COMPONENT) Window w,
				@QueryParam("operazione") 			 String operazione,
				@QueryParam("id") 					 String id,
				@QueryParam("tipoDocumento") 		 String tipoDocumento,
                @QueryParam("idDocumentoEsterno") 	 String idDocumentoEsterno,
                @QueryParam("refreshAPP") 	         String refreshAPP) {
		this.self = w
		gestioneTestiService.setDefaultEditor(Impostazioni.EDITOR_DEFAULT.valore, Impostazioni.EDITOR_DEFAULT_PATH.valore)

        boolean refresh = refreshAPP != null ? Boolean.parseBoolean(refreshAPP) : true;

		// switch case di selezione dell'operazione
		switch (operazione) {
			case "APRI_DOCUMENTO":
				Long idDoc = id != null ? Long.parseLong(id) : null;
				Long idDocEst = idDocumentoEsterno!= null ? Long.parseLong(idDocumentoEsterno) : null;

				// FIXME: questo è brutto. forse vanno divisi in scrivania o vanno creati url diversi per il link della scrivania?
				if (tipoDocumento == VistoParere.TIPO_OGGETTO) {
					VistoParere vp = VistoParere.get(idDoc);
					if ((vp.documentoPrincipale instanceof PropostaDelibera) || (vp.documentoPrincipale instanceof Delibera)) {
						tipoDocumento = VistoParere.TIPO_OGGETTO_PARERE;
					}
				}

				creaPopup(tipiOggetto[tipoDocumento], [id: idDoc, idDocumentoEsterno : idDocEst], refresh).doModal()
				break

			case "DA_FIRMARE":
				creaPopup("/atti/index.zul", [codiceTab: 'da_firmare'], refresh)
				break

			default:
				break
		}
	}

	private Window creaPopup (String zul, def parametri, boolean refresh) {
		Window window = Executions.createComponents(zul, self, parametri)
		window.onClose() {
            if (refresh) {
                Clients.evalJavaScript('''jq(window).unbind(\'beforeunload\');
                                            window.close();
                                            if (window.opener) {
                                                if (window.opener.refreshAPP) {
                                                    window.opener.refreshAPP();
                                                } else {
                                                    window.opener.location.reload(false);
                                                }
                                            }''')
            }
            else {
                Clients.evalJavaScript("jq(window).unbind('beforeunload'); window.close();")
            }
        }
		return window
	}
}
