package commons

import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestionetesti.AppletEditaTesto
import it.finmatica.gestionetesti.CorrettoreTesto
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.webdav.WebdavClient
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Window

class PopupCorreggiTestoViewModel {

	// services
	IGestoreFile 			gestoreFile;
	GestioneTestiService 	gestioneTestiService;
	WebdavClient			webdavClient;
	AppletEditaTesto		appletEditaTesto;

	// componenti
	Window self

	// dati
	def documentoDto
	String nomeDocumentoWebdav
	String urlDocumentoWebdav
	String stacktrace

	@Init init (@ContextParam(ContextType.COMPONENT) Window w
			  , @ExecutionArgParam("documento") def documento
			  , @ExecutionArgParam("eccezione") Throwable eccezione) {

		if (eccezione.getMessage() == null && eccezione.getCause() != null) {
			eccezione = eccezione.getCause();
		}

		self 				= w
		documentoDto 		= documento
		nomeDocumentoWebdav = "TEMPORANEO-${new Date().time}.${Impostazioni.FORMATO_DEFAULT.valore}";
		stacktrace = eccezione.message+"\n"+eccezione.getStackTrace().toString().replace(')', ')\n');
	}

	@Command onChiudi () {
		if (urlDocumentoWebdav != null) {
			webdavClient.deleteFile(urlDocumentoWebdav)
		}
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command onCorreggiManualmente () {
		// devo ottenere il testo originale, trasformarlo se necessario nel formato richiesto, poi metterlo su webdav ed aprirlo.
		def documento = documentoDto.domainObject;
		InputStream is = gestoreFile.getFile(documento, documento.testo);
		urlDocumentoWebdav = webdavClient.salvaFile(nomeDocumentoWebdav, is);
		appletEditaTesto.editaTesto(urlDocumentoWebdav, { onSalvaTesto(); });
	}

	@Command onCorreggiAutomaticamente () {
		// devo ottenere il testo originale, trasformarlo in ODT, correggerlo, poi salvarlo su webdav ed eventualmente aprirlo.
		def documento = documentoDto.domainObject;
		InputStream is = gestoreFile.getFile(documento, documento.testo);
		InputStream odt = gestioneTestiService.converti(is, GestioneTestiService.FORMATO_ODT);
		CorrettoreTesto correttore = new CorrettoreTesto();
		InputStream odtCorretto = correttore.correggiTesto (odt, GestioneTestiService.FORMATO_ODT);
		InputStream testoCorretto = gestioneTestiService.converti(odtCorretto, Impostazioni.FORMATO_DEFAULT.valore);
		urlDocumentoWebdav = webdavClient.salvaFile(nomeDocumentoWebdav, testoCorretto);
		appletEditaTesto.editaTesto(urlDocumentoWebdav, { onSalvaTesto(); });
	}

	@Command onSalvaTesto () {
		// recupero il file da webdav e lo carico sul testo.
		Determina.withTransaction {

			def documento = documentoDto.domainObject;
			// carico l'eventuale testo che ho su webdav e lo sblocco
			gestioneTestiService.uploadEUnlockTesto(AttiGestioneTesti.creaIdRiferimento(documento))

			// ottengo il file su cui sto lavorando e lo salvo su db.
			InputStream is = webdavClient.getFileStream(urlDocumentoWebdav)
			gestoreFile.addFile(documento, documento.testo, is);
		}
		Clients.showNotification("Testo Salvato", Clients.NOTIFICATION_TYPE_INFO, self, "before_center", 3000, true);
	}
}
