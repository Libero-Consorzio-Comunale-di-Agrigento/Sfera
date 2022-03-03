package it.finmatica.atti.documenti.beans

import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IDocumentoStoricoEsterno
import it.finmatica.atti.IFileAllegato
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.MarkableFileInputStream
import it.finmatica.jsign.api.PKCS7Reader
import org.apache.commons.io.IOUtils
import org.apache.log4j.Logger
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import org.apache.tika.sax.BodyContentHandler
import org.springframework.transaction.annotation.Transactional

class AttiGestoreFile implements IGestoreFile {

	private static final transient Logger log = Logger.getLogger(AttiGestoreFile.class)
	
	@Transactional
	void addFile (IDocumentoEsterno documento, IFileAllegato fileAllegato, InputStream is) {
		
		MarkableFileInputStream inputStream
		try {
			inputStream = new MarkableFileInputStream (is)
				
			if (inputStream.markSupported()) {
				inputStream.mark(0)
			}

			// carico il file su gdm:
			addFileToSfera (documento, fileAllegato, inputStream)
			
			// ora che ho letto tutto lo stream, ne ho la dimensione e la scrivo:
			fileAllegato.dimensione = inputStream.byteCount
			
			// questo è normalmente true per:
			// - MarkableFileInputStream	<- questo arriva quando si fa un upload di file più grosso di qualche centinaio di KB
			// - ByteArrayInputStream		<- questo arriva quando si fa un edita/testo o upload di file "piccoli"
			// - OracleBlobInputStream 		<- questo non so se arriverà mai
			if (inputStream.markSupported()) {
				inputStream.reset()
				
				// tento di leggere il testo ocr del file:
				fileAllegato.testo = getTestoOCR (inputStream, fileAllegato.nome, fileAllegato.p7m)
				fileAllegato.save()
			}
			
		} finally {
			// chiudo qui l'input stream siccome nell'upload alla profilo, anch'essa tenta di chiuderlo
			// ma avendo sovrascritto il metodo close() per poter rileggere l'inputstream,
			// c'è bisogno di chiuderlo qui.
			inputStream.chiudiMeglio()
		}
	}

	@Override
	void updateFile(IFileAllegato fileAllegato) {
		fileAllegato.save()
	}

	void addFileToSfera (IDocumentoEsterno documento, IFileAllegato fileAllegato, InputStream is) {
		fileAllegato.idFileEsterno	= null
		fileAllegato.allegato 		= IOUtils.getBytes(is)
		fileAllegato.save()
	}
	
	String getTestoOCR (InputStream inputStream, String nomefile, boolean p7m) {
		try {
			// una volta letto il file verifico se è un p7m e nel caso lo sbusto
			if (p7m) {
				PKCS7Reader reader = new PKCS7Reader(inputStream)
				inputStream = reader.getOriginalContent()
			}
	
			Metadata metadata = new Metadata()
			metadata.set(Metadata.RESOURCE_NAME_KEY, nomefile)
			ParseContext context = new ParseContext()
			BodyContentHandler handler = new BodyContentHandler(-1)
			Parser parser = new AutoDetectParser()
			parser.parse(inputStream, handler, metadata, new ParseContext())
			return handler.toString()
		} catch (Throwable e) {
			log.info ("Non sono riuscito a leggere il testo OCR del file: ${nomefile}.")
			return null
		}
	}
	
	@Transactional(readOnly=true)
	InputStream getFile(IDocumentoEsterno documento, IFileAllegato fileAllegato) {
		return new ByteArrayInputStream (fileAllegato.allegato)
	}

	@Transactional
	void removeFile (IDocumentoEsterno documento, IFileAllegato fileAllegato) {
		fileAllegato.delete (failOnError: true)
	}

	@Transactional
	void addFileStorico(IDocumentoStoricoEsterno documento, IFileAllegato fileAllegato, IFileAllegato fileAllegatoDaStoricizzare) {
		fileAllegato.allegato = fileAllegatoDaStoricizzare.allegato
		fileAllegato.testo    = fileAllegatoDaStoricizzare.testo
		fileAllegato.save()
	}

	@Transactional(readOnly=true)
	InputStream getFileStorico(IDocumentoStoricoEsterno documento, IFileAllegato fileAllegato) {
		return new ByteArrayInputStream (fileAllegato.allegato)
	}

	@Transactional
	void removeFileStorico(IDocumentoStoricoEsterno documento, IFileAllegato fileAllegato) {
		fileAllegato.delete()
	}

	@Transactional(readOnly=true)
	boolean isFilePresente (IDocumentoEsterno documento, IFileAllegato fileAllegato) {
		return true
	}

	@Transactional
	public String verificaImpronta(IDocumentoEsterno documento, IFileAllegato fileAllegato){
		return "La verifica è andata a buon fine";
	}
}
