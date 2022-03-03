package it.finmatica.atti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.dto.DTO
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.IMaschera
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgPulsante
import it.finmatica.gestioneiter.dto.configuratore.iter.WkfCfgPulsanteDTO
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.zkutils.SuccessHandler
import org.apache.commons.lang.StringUtils
import org.springframework.transaction.TransactionStatus
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

abstract class AbstractViewModel<T extends IDocumentoIterabile> implements IMaschera<T> {

	// servizi.
	// Questi sono Bean Iniettati. Sono 'private' e hanno i relativi getter/setter
	// perché per qualche ragione non vengono iniettati quando questa classe viene ereditata dagli altri ViewModel.
	private AttiGestioneTesti		gestioneTesti
	private SuccessHandler 			successHandler
	private WkfIterService 			wkfIterService
	private AttiFirmaService 		attiFirmaService
	private SpringSecurityService	springSecurityService

	// riverimento alla Window di questo viewModel.
	Window self;

	// elenco dei pulsanti dell'iter da mostrare
	private List<WkfCfgPulsanteDTO> pulsanti;

	abstract Collection<String> validaMaschera ()

	abstract DTO<T> getDocumentoDTO ()

	/**
	 * Può essere chiamata dal viewModel o dallo zul quando in maschera vengono eseguite azioni che possono cambiare i pulsanti
	 */
	@Command
	void aggiornaPulsanti () {
		aggiornaPulsanti (getDocumentoIterabile(false));
	}

	void aggiornaPulsanti (IDocumento documento) {
		def pulsanti = wkfIterService.getPulsanti (documento, this);

		// se sono in stato IN_FIRMA o FIRMATO_DA_SBLOCCARE, mostro solo i pulsanti di firma:
		if (documento.statoFirma == StatoFirma.IN_FIRMA || documento.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE) {
			pulsanti = pulsanti.findAll { cfgPulsante -> cfgPulsante.pulsante.azioni.find { azione -> azione.nomeMetodo == "apriPopupFirma" } != null }
			pulsanti = pulsanti.toDTO()
			for (def p : pulsanti) {
				p.etichetta = "Completa ${p.etichetta}"
			}
		} else {
			pulsanti = pulsanti.toDTO()
		}

		// imposto i pulsanti sulla maschera e aggiorno il viewModel
		setPulsanti(pulsanti);
		BindUtils.postNotifyChange(null, null, this, "pulsanti");

		// questo serve per riallineare la maschera con i pulsanti, senza questo, i pulsanti non si vedranno.
		invalidate();
	}

	@Command
	void clickPulsanteIter (@BindingParam("idPulsante") long idCfgPulsante) {
		def documentoIterabile = getDocumentoIterabile(true);

		// se sono in fase di completa firma
		switch (documentoIterabile.statoFirma) {
			case StatoFirma.FIRMATO_DA_SBLOCCARE:
				// sblocco il documento firmato
				attiFirmaService.sbloccaDocumentoFirmato(documentoIterabile, springSecurityService.currentUser.id)

				// aggiorno la maschera e i pulsanti al solito modo
				aggiornaMaschera(documentoIterabile)
				aggiornaPulsanti()
			break;

			default:
				// eseguo il pulsante normalmente
				clickPulsanteIter(idCfgPulsante, -1);
			break;
		}
	}

	private void apriPopupFirma () {
    		Window w = Executions.createComponents("/commons/popupFirma.zul", self, [urlPopupFirma: attiFirmaService.urlPopupFirma])
		w.onClose { event ->
			aggiornaMaschera(getDocumentoIterabile(false))
			aggiornaPulsanti()
		}
		w.doModal()
	}

	void clickPulsanteIter (long idCfgPulsante, long idAzioneClient) {
		WkfCfgPulsante cfgPulsante = WkfCfgPulsante.get(idCfgPulsante)
		if (cfgPulsante.pulsante.competenzaInModifica) {
			// prima di eseguire il pulsante, valido la maschera:
			Collection<String> messaggiValidazione = validaMaschera()
			if (messaggiValidazione != null && messaggiValidazione.size() > 0) {
				Clients.showNotification(StringUtils.join(messaggiValidazione, "\n"), Clients.NOTIFICATION_TYPE_ERROR, self, "middle_center", -1, true);
				return
			}
		}

		// blocco l'operazione se l'utente sta ancora editando il testo e se il pulsante prevede un cambio-step:
		if (gestioneTesti.isEditorAperto() && cfgPulsante.cfgStepSuccessivo != null) {
			Clients.showNotification("Per poter inoltrare il documento, è necessario chiudere l'editor di testo.", Clients.NOTIFICATION_TYPE_WARNING, null, "middle_center", 3000, true);
			return;
		}

		// controllo se il pulsante richiede conferma (e non ho già fatto l'azione client)
		String messaggioConferma = cfgPulsante.pulsante.messaggioConferma;
		if (messaggioConferma != null && messaggioConferma.length() > 0 && idAzioneClient <= 0) {
			// chiedi conferma
			Messagebox.show(messaggioConferma, "Attenzione - Richiesta Conferma",
				Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							AbstractViewModel.this.eseguiPulsante(idCfgPulsante, idAzioneClient);
						}
					}
				}
			)
		} else {
			eseguiPulsante(idCfgPulsante, idAzioneClient)
		}
	}

	void eseguiPulsante (long idCfgPulsante, long idAzioneClient) {
		def documento
		boolean committed = false
        boolean pulsanteConSalvataggio = true

			Determina.withTransaction { TransactionStatus status ->

			// ottengo il documento iterabile:
			documento = getDocumentoIterabile(true)

			// aggiorno subito il documento solo se lo sto creando.
			// altrimenti, se ne occuperà il wkfIterService.eseguiPulsante ad eseguire l'aggiornaDocumentoIterabile.
			if (!(documento.id > 0)) {
				aggiornaDocumentoIterabile(documento)
			}

			// lo salvo
			documento.save()

			// creo l'iter se non l'ho:
			if (documento.iter == null) {
				this.wkfIterService.istanziaIter(getCfgIter(), documento)
			}

			WkfCfgPulsante cfgPulsante = WkfCfgPulsante.get(idCfgPulsante)
			pulsanteConSalvataggio = cfgPulsante.competenzaInModifica

			this.wkfIterService.eseguiPulsante (documento, idCfgPulsante, this, idAzioneClient)

			committed = !status.isRollbackOnly()
		}

        // alcune azioni (come ad esempio la "FirmaAction.scaricaAnteprimaTesto") eseguono un Rollback
        // senza lanciare eccezione. In tal caso, non c'è bisogno di refreshare la maschera siccome non è successo niente,
        // inoltre darebbe errore di sessione hibernate non trovata.
		if (committed) {
			// aggiorno l'interfaccia:
			aggiornaMaschera(documento)

			// fuori dalla transazione calcolo i pulsanti
			aggiornaPulsanti(documento)

            if (pulsanteConSalvataggio) {
			    successHandler.showMessages("Documento salvato.")
            }
		} else {
            successHandler.clearMessages()
        }
	}

	@Command
	void invalidate () {
		if (!successHandler.saltaInvalidate) {
			self.invalidate()
		}
	}

	List<WkfCfgPulsante> getPulsanti() {
		return pulsanti
	}

	void setPulsanti (List<WkfCfgPulsante> pulsanti) {
		this.pulsanti = pulsanti
	}

	void setWkfIterService(WkfIterService wkfIterService) {
		this.wkfIterService = wkfIterService;
	}

	WkfIterService getWkfIterService() {
		return this.wkfIterService;
	}

	SuccessHandler getSuccessHandler() {
		return successHandler;
	}

	void setSuccessHandler(SuccessHandler successHandler) {
		this.successHandler = successHandler;
	}

	AttiFirmaService getAttiFirmaService() {
		return attiFirmaService;
	}

	void setAttiFirmaService(AttiFirmaService attiFirmaService) {
		this.attiFirmaService = attiFirmaService;
	}

	SpringSecurityService getSpringSecurityService() {
		return springSecurityService;
	}

	void setSpringSecurityService(SpringSecurityService springSecurityService) {
		this.springSecurityService = springSecurityService;
	}
	
	AttiGestioneTesti getGestioneTesti() {
		return gestioneTesti;
	}

	void setGestioneTesti(AttiGestioneTesti gestioneTesti) {
		this.gestioneTesti = gestioneTesti;
	}
}
