package commons

import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.DocumentoFactory
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.StampaUnicaService
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.dto.odg.SedutaStampaDTO
import it.finmatica.dto.DTO
import it.finmatica.grails.firmadigitale.FirmaDigitaleService
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Filedownload
import org.zkoss.zul.Window

class PopupFileFirmatoSmartDesktopViewModel {

	// services
	AttiFileDownloader 		attiFileDownloader
	FirmaDigitaleService 	firmaDigitaleService
	IGestoreFile 			gestoreFile

	// componenti
	Window self

	// dati
	Long idDoc
	Long idFile
	String tipo
	def documentoDto
	def fileAllegatoDto
	def risultatiVerifica
	String nomeFile
	boolean firmato = true

	@Init init (@ContextParam(ContextType.COMPONENT)Window 	w
			  , @QueryParam("file") 	String file
			  , @QueryParam("doc") 		String doc
			  , @QueryParam("tipo") 	String tipo) {

		self				= w
		this.idDoc = Long.parseLong(doc)
		this.idFile = Long.parseLong(file)
		this.tipo = tipo

		this.documentoDto = DocumentoFactory.getDocumento(idDoc, tipo).toDTO()
		FileAllegato fileAllegato = FileAllegato.get(idFile)
		this.fileAllegatoDto = fileAllegato.toDTO()
		this.nomeFile = fileAllegato.nome
		// retro compatibilità
		this.firmato = fileAllegato.firmato || fileAllegato.isP7m()
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command onP7m () {
		//def documento = documentoDto.getDomainObject()
		//def fAllegato = fileAllegatoDto.getDomainObject()
		//attiFileDownloader.downloadFile(documento, fAllegato, false, false)

		// Costruisco l'URL del controller
		String url = "${Executions.getCurrent().contextPath}/smartDesktop/downloadFile" +
				"?file=${idFile}" +
				"&doc=${idDoc}" +
				"&tipo=${tipo}" +
				"&trasformaInPdf=false" +
				"&sbusta=false"

		// Download diretto
		//Executions.getCurrent().sendRedirect(url)
		Clients.evalJavaScript("window.open('${url}', '_blank')")
	}

	@Command onSbusta () {
		//def documento = documentoDto.getDomainObject()
		//def fAllegato = fileAllegatoDto.getDomainObject()
		//attiFileDownloader.downloadFile(documento, fAllegato, false, true)

		// Costruisco l'URL del controller
		String url = "${Executions.getCurrent().contextPath}/smartDesktop/downloadFile" +
				"?file=${idFile}" +
				"&doc=${idDoc}" +
				"&tipo=${tipo}" +
				"&trasformaInPdf=false" +
				"&sbusta=true"

		// Download diretto
		//Executions.getCurrent().sendRedirect(url)
		Clients.evalJavaScript("window.open('${url}', '_blank')")
	}

	@Command onCopia () {
		IDocumento documento = documentoDto.getDomainObject()
		Filedownload.save(gestoreFile.getFile(documento, documento.testoOdt), documento.testoOdt.contentType, documento.testoOdt.nomeFileOdtOriginale)
	}

	@NotifyChange("risultatiVerifica")
	@Command onVerifica () {
		def documento = documentoDto.getDomainObject()
		def fAllegato = fileAllegatoDto.getDomainObject()

		risultatiVerifica = firmaDigitaleService.verificaFirma(gestoreFile.getFile(documento, fAllegato))
	}
}
