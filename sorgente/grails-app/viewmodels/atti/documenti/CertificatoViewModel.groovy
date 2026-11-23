package atti.documenti

import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.dto.documenti.CertificatoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CaratteristicaTipologiaService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.zk.SoggettoDocumento
import it.finmatica.dto.DTO
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dto.documenti.viste.So4DelegaService
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class CertificatoViewModel extends AbstractViewModel<Certificato> {

	// services
	CaratteristicaTipologiaService	caratteristicaTipologiaService
	AttiFileDownloader 				attiFileDownloader
	AttiGestoreCompetenze 			gestoreCompetenze
	TokenIntegrazioneService		tokenIntegrazioneService
	So4DelegaService				so4DelegaService

	// dati
	CertificatoDTO                 certificato
	String                         oggettoDocumentoPrincipale
	String                         proponente
	String                         documentoPrincipale
	Date                           dataInizioPubblicazione
	Date                           dataFinePubblicazione
	Integer                        giorniPubblicazione
	boolean                        pubblicaRevoca
	String                         titolo
	Map<String, SoggettoDocumento> soggetti = [:]
	boolean                        testoLockato
	boolean                        lockPermanente

	// stato
	boolean certificatoFirmato = false
	boolean firmaRemotaAbilitata

	long idModelloTesto = -1

	Map<String, Boolean> competenze

	// indica se è bloccato da un altro utente
	boolean isLocked

	// indica se il documento deve essere comunque aperto in lettura (delegato)
	boolean forzaCompetenzeLettura

	// utente per conto del quale si sta operando (delega So4)
	Ad4Utente delegante

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") long idCertificato, @ExecutionArgParam("idDocumentoEsterno") Long idDocumentoEsterno, @ExecutionArgParam("competenzeLettura") Boolean competenzeLettura, @ExecutionArgParam("delegante") Ad4Utente delegante) {
		this.self 		= w
		firmaRemotaAbilitata 			= Impostazioni.FIRMA_REMOTA.abilitato
		forzaCompetenzeLettura			= competenzeLettura
		this.delegante					= delegante
		Certificato cert = null
		if (idCertificato > 0) {
			cert = Certificato.get(idCertificato)
		} else if (idDocumentoEsterno > 0) {
			cert = Certificato.findByIdDocumentoEsterno(idDocumentoEsterno)
		} else {
			throw new AttiRuntimeException("Impossibile aprire il certificato: nessun identificativo valido fornito (idCertificato=${idCertificato}, idDocumentoEsterno=${idDocumentoEsterno})")
		}
		if (cert == null) {
			throw new AttiRuntimeException("Certificato non trovato (idCertificato=${idCertificato}, idDocumentoEsterno=${idDocumentoEsterno})")
		}
		aggiornaMaschera(cert)

		if (cert.iter != null) {
			aggiornaPulsanti()
		}
    }

	@Command apriDocumentoPrincipale () {
		Window w = Executions.createComponents("/atti/documenti/${(certificato.determina?'determina':'delibera')}.zul", self, [id:(certificato.determina?:certificato.delibera).id, competenzeLettura: forzaCompetenzeLettura])
		w.onClose {
			aggiornaMaschera(certificato.domainObject)
		}
		w.doModal()
	}

	/*
	 * Gestione Applet Testo
	 */
	@Command onDownloadTesto () {
		Certificato d 	= certificato.domainObject
		attiFileDownloader.downloadFileAllegato(d, d.testo)
	}

	@Command onApriTestoAtto () {
		def documento = certificato.getDomainObject().getDocumentoPrincipale()
		attiFileDownloader.downloadFileAllegato(documento, documento.testo)
	}

	@NotifyChange(["testoLockato", "certificato"])
	@Command
	editaTesto () {
		testoLockato = gestioneTesti.editaTesto(certificato)
		certificato = certificato.domainObject.toDTO(["determina", "delibera", "firmatario", "tipologia", "testo", "modelloTesto", "delibera.testo", "determina.testo"])
	}

	@Command
	onEliminaTesto () {
		gestioneTesti.eliminaTesto(certificato, this)
	}

	/*
	 * 	Metodi per il calcolo dei Soggetti della determina
	 *
	 */
	@Command onSceltaSoggetto (@BindingParam("tipoSoggetto") String tipoSoggetto, @BindingParam("categoriaSoggetto") String categoriaSoggetto) {
		Window w = Executions.createComponents ("/atti/documenti/popupSceltaSoggetto.zul", self, [idCaratteristicaTipologia: certificato.tipologia.caratteristicaTipologia.id
			, documento: certificato
			, soggetti: [FIRMATARIO: certificato.firmatario]
			, tipoSoggetto: 	tipoSoggetto
			, categoriaSoggetto:categoriaSoggetto])
		w.onClose { event ->
			// se ho annullato la modifica, non faccio niente:
			if (event.data == null)
				return

			// altrimenti aggiorno i soggetti.
			BindUtils.postNotifyChange(null, null, this, "soggetti")
            self.invalidate()
        }
		w.doModal()
	}

	/*
	 *  Gestione Chiusura Maschera
	 */
	@Command
	@NotifyChange("pubblicaRevoca")
	void setPubblicarevoca (@BindingParam("val") boolean val) {
		pubblicaRevoca = val
	}

	@Command onChiudi () {
		try {
			gestioneTesti.uploadEUnlockTesto(certificato, lockPermanente)
			tokenIntegrazioneService.unlockDocumento(certificato.domainObject)
		} catch (Exception e) {
			Clients.showNotification("Impossibile rilasciare il lock sul documento: ${e.message}", Clients.NOTIFICATION_TYPE_WARNING, null, "top_center", 5000, false)
		}
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	/*
	 * Implementazione dei Metodi per AbstractViewModel
	 */
	DTO<Certificato> getDocumentoDTO () {
		return certificato
	}

	@Override
	WkfCfgIter getCfgIter() {
		return WkfCfgIter.getIterIstanziabile(certificato.tipologia.progressivoCfgIter).get()
	}

	Certificato getDocumentoIterabile (boolean controllaConcorrenza) {
		if (certificato?.id > 0) {
			Certificato domainObject = certificato.getDomainObject()
			if (controllaConcorrenza && certificato?.version >= 0 && domainObject.version != certificato?.version) {
				throw new AttiRuntimeException("Attenzione: un altro utente ha modificato il documento su cui si sta lavorando. Impossibile continuare. \n (dto.version=${certificato.version}!=domain.version=${domainObject.version})")
			}

			return domainObject
		}

		return new Certificato()
	}

	Collection<String> validaMaschera () {
		if (!pubblicaRevoca && certificato?.tipo == Certificato.CERTIFICATO_PUBBLICAZIONE) {
			if (!giorniPubblicazione || giorniPubblicazione <= 0) {
				return ["Il numero di giorni di pubblicazione deve essere maggiore di zero."]
			}
		}
		return []
	}

	void aggiornaDocumentoIterabile (Certificato c) {
		if (!competenze?.modifica) {
			throw new AttiRuntimeException("L'utente ${springSecurityService.principal.username} non ha i diritti di modifica sul certificato con id ${c.id}")
		}

		// salvo e sblocco il testo
		gestioneTesti.uploadEUnlockTesto (c)

		caratteristicaTipologiaService.salvaSoggettiModificati(c, soggetti)

		c.documentoPrincipale.giorniPubblicazione 	= giorniPubblicazione
		c.documentoPrincipale.pubblicaRevoca	 	= pubblicaRevoca
	}

	void aggiornaMaschera (Certificato c) {
		// per prima cosa controllo che l'utente abbia le competenze in lettura sul documento
		competenze = gestoreCompetenze.getCompetenze(c, true)
		competenze.lettura = competenze.lettura ?: forzaCompetenzeLettura
		if (!competenze.lettura) {
			certificato = null
			throw new AttiRuntimeException("L'utente ${springSecurityService.principal.username} non ha i diritti di lettura sul certificato con id ${c.id}")
		}

		if (hasDelegaValida()) {
			if (c.statoFirma == StatoFirma.DA_FIRMARE) {
				gestoreCompetenze.assegnaCompetenze(c, WkfTipoOggetto.get(Certificato.TIPO_OGGETTO), new Attore(utenteAd4: springSecurityService.principal.soggetto.utenteAd4), true, true, false, null)
				competenze.modifica = true
			}
		}

		if (c.statoFirma == StatoFirma.IN_FIRMA || c.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE) {
			competenze.modifica 	 = false
			competenze.cancellazione = false
		}

        isLocked = tokenIntegrazioneService.isLocked(c)

		soggetti = caratteristicaTipologiaService.calcolaSoggettiDto(c)

		if (soggetti[TipoSoggetto.FIRMATARIO] == null) {
			soggetti = caratteristicaTipologiaService.calcolaSoggetti(c, c.tipologia.caratteristicaTipologia)
		}

		certificato = c.toDTO(["determina", "delibera", "firmatario", "tipologia", "testo", "modelloTesto", "delibera.testo", "determina.testo"])

		titolo = "Certificato di ${certificato.tipo == Certificato.CERTIFICATO_ESECUTIVITA? "Esecutività" : certificato.tipo == Certificato.CERTIFICATO_PUBBLICAZIONE? "Pubblicazione" : certificato.tipo == Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE? "Avvenuta Pubblicazione" : "Immediata eseguibilita"}"

		try {
			proponente 					= c.documentoPrincipale.getSoggetto(TipoSoggetto.REDATTORE)?.utenteAd4?.nominativoSoggetto
			dataInizioPubblicazione 	= c.documentoPrincipale.dataPubblicazione
			dataFinePubblicazione   	= c.documentoPrincipale.dataFinePubblicazione
			documentoPrincipale 		= c.documentoPrincipale.estremiAtto
			oggettoDocumentoPrincipale 	= c.documentoPrincipale.oggetto
			pubblicaRevoca				= c.documentoPrincipale.pubblicaRevoca
			giorniPubblicazione 		= c.documentoPrincipale.giorniPubblicazione
		} catch (Exception ignored) {
			// Il lazy loading su documentoPrincipale può fallire nei callback Messagebox
			// (request /zkau senza sessione OSIV). In quel caso conserva i valori esistenti:
			// l'importante è che postNotifyChange venga sempre eseguito.
		}

		BindUtils.postNotifyChange(null, null, this, "certificato")
		BindUtils.postNotifyChange(null, null, this, "titolo")
		BindUtils.postNotifyChange(null, null, this, "documentoPrincipale")
		BindUtils.postNotifyChange(null, null, this, "oggettoDocumentoPrincipale")
		BindUtils.postNotifyChange(null, null, this, "proponente")
		BindUtils.postNotifyChange(null, null, this, "pubblicaRevoca")
		BindUtils.postNotifyChange(null, null, this, "giorniPubblicazione")
		BindUtils.postNotifyChange(null, null, this, "soggetti")
		BindUtils.postNotifyChange(null, null, this, "isLocked")
		BindUtils.postNotifyChange(null, null, this, "competenze")
		BindUtils.postNotifyChange(null, null, this, "testoLockato")
	}

	private boolean hasDelegaValida() {
		return delegante != null && !so4DelegaService.getDeleghe(springSecurityService.currentUser, delegante).isEmpty()
	}
}
