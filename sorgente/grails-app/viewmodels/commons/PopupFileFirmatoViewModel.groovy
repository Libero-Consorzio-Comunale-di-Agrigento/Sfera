package commons

import it.finmatica.atti.IGestoreFile
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

class PopupFileFirmatoViewModel {

	// services
	AttiFileDownloader 		attiFileDownloader
	FirmaDigitaleService 	firmaDigitaleService
	IGestoreFile 			gestoreFile
	StampaUnicaService		stampaUnicaService

	// componenti
	Window self

	// dati
	def documentoDto
	long idFileAllegato
	def risultatiVerifica
	String nomeFile
	DTO fileAllegatoDto
    boolean p7m = true

	// stato
	boolean solalettura = true
	boolean storico     = false
	boolean copiaTesto  = false

	@Init init (@ContextParam(ContextType.COMPONENT)Window 	w
			  , @ExecutionArgParam("documento") 	def 	doc
			  , @ExecutionArgParam("nomeFile") 		String 	nomeFile
			  , @ExecutionArgParam("fileAllegato") 	DTO fileAllegatoDto
			  , @ExecutionArgParam("storico") 		boolean storico
			  , @ExecutionArgParam("solalettura") 	boolean solalettura) {

		self				= w
		documentoDto 		= doc
		this.idFileAllegato = idFileAllegato
		this.fileAllegatoDto= fileAllegatoDto
		this.solalettura 	= solalettura
		this.storico 		= storico
		this.nomeFile 		= nomeFile
        this.p7m            = fileAllegatoDto.getDomainObject().isP7m()

        // FIXME: ho aggiunto doc instanceof SedutaStampaDTO per fare in fretta ma bisognerà verificare meglio come capire se il documento ha o no un testoOdt.
		this.copiaTesto		= !storico && ((doc.hasProperty("testoOdt") && doc.testoOdt != null) || doc instanceof SedutaStampaDTO)
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

		risultatiVerifica = firmaDigitaleService.verificaFirma((storico ? gestoreFile.getFileStorico(documento, fAllegato) : gestoreFile.getFile(documento, fAllegato)))
	}

	@Command onCopiaConforme () {
		def documento = documentoDto.getDomainObject()
		def fAllegato = fileAllegatoDto.getDomainObject()

		stampaUnicaService.copiaConforme(documento, fAllegato, true, true)
	}
}
