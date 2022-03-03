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
import org.zkoss.zk.ui.event.Events
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
	def documentoDto
	long idFileAllegato
	def risultatiVerifica
	String nomeFile
	DTO fileAllegatoDto
    boolean p7m = true


	@Init init (@ContextParam(ContextType.COMPONENT)Window 	w
			  , @QueryParam("file") 	String file
			  , @QueryParam("doc") 		String doc
			  , @QueryParam("tipo") 	String tipo) {

		self				= w
		FileAllegato fileAllegato = FileAllegato.get(file)
        documentoDto 		= DocumentoFactory.getDocumento(Long.parseLong(doc), tipo).toDTO()

		this.idFileAllegato = Long.parseLong(file)
		this.fileAllegatoDto= fileAllegato.toDTO()
		this.nomeFile 		= fileAllegato.nome
        this.p7m            = fileAllegato.isP7m()
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command onP7m () {
		def documento = documentoDto.getDomainObject()
		def fAllegato = fileAllegatoDto.getDomainObject()
		attiFileDownloader.downloadFile(documento, fAllegato, false, false)
	}

	@Command onSbusta () {
		def documento = documentoDto.getDomainObject()
		def fAllegato = fileAllegatoDto.getDomainObject()
		attiFileDownloader.downloadFile(documento, fAllegato, false, true)
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
