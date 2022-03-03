package it.finmatica.atti.documenti.beans

import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.Sql
import it.finmatica.atti.*
import it.finmatica.atti.commons.MarkableFileInputStream
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.dmServer.util.Global
import it.finmatica.jdmsutil.data.ProfiloExtend
import it.finmatica.jdmsutil.data.ProfiloVersionExtend
import it.finmatica.jsign.api.PKCS7Reader
import org.apache.log4j.Logger
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import org.apache.tika.sax.BodyContentHandler
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource
import java.sql.Connection

class GdmGestoreFile implements IGestoreFile {

	private static final transient Logger log = Logger.getLogger(GdmGestoreFile.class)

	IDocumentaleEsterno   gestoreDocumentaleEsterno
	SpringSecurityService springSecurityService
	DataSource            dataSource_gdm

	@Transactional
	void addFile (IDocumentoEsterno documento, IFileAllegato fileAllegato, InputStream is) {
		
		MarkableFileInputStream inputStream
		try { 
    		inputStream = new MarkableFileInputStream (is) 
    			
    		if (inputStream.markSupported()) {
    			inputStream.mark(Integer.MAX_VALUE)
    		}

    		// carico il file su gdm:
    		addFileToGdm (documento, fileAllegato, inputStream)
			
			// ora che ho letto tutto lo stream, ne ho la dimensione e la scrivo:
			fileAllegato.dimensione = inputStream.byteCount
    		
    		// questo è normalmente true per:
    		// - MarkableFileInputStream	<- questo arriva quando si fa un upload di file più grosso di qualche centinaio di KB
    		// - ByteArrayInputStream		<- questo arriva quando si fa un edita/testo o upload di file "piccoli"
    		// - OracleBlobInputStream 		<- questo arriva wrappato in un BufferedInputStream quando l'InputStream arriva da una .getFile() da gdm.
    		if (inputStream.markSupported()) {
    			inputStream.reset()
    			
    			// tento di leggere il testo ocr del file:
    			fileAllegato.testo = getTestoOCR (inputStream, fileAllegato.nome, fileAllegato.p7m)
    		}

			fileAllegato.save()
		} finally {
			// chiudo qui l'input stream siccome nell'upload alla profilo, anch'essa tenta di chiuderlo 
			// ma avendo sovrascritto il metodo close() per poter rileggere l'inputstream,
			// c'è bisogno di chiuderlo qui.
			inputStream.chiudiMeglio()
		}	
	}

	@Override
	void updateFile(IFileAllegato fileAllegato) {

		// rinomino il file su gdm.
		Sql sql = new Sql(dataSource_gdm)
		sql.execute("update impronte_file set filename = :filename where ID_DOCUMENTO = (select id_documento from oggetti_file where id_oggetto_file = :idOggettoFile) and FILENAME = (select filename from oggetti_file where id_oggetto_file = :idOggettoFile)" , [filename:fileAllegato.nome, idOggettoFile: fileAllegato.idFileEsterno])
		sql.execute("update oggetti_file set filename = :filename where id_oggetto_file = :idOggettoFile", [filename:fileAllegato.nome, idOggettoFile: fileAllegato.idFileEsterno])
	}

	@Transactional
	void addFileToGdm (IDocumentoEsterno documento, IFileAllegato fileAllegato, InputStream is) {
		Connection conn = dataSource_gdm.connection

		// per prima cosa salvo il documento se non è già salvato:
		if (!(documento.idDocumentoEsterno > 0)) {
			fileAllegato.save()
			documento.save()
			gestoreDocumentaleEsterno.salvaDocumento (documento)
		}

		// poi carico il file:
		log.debug ("new ProfiloExtend (${Long.toString(documento.idDocumentoEsterno)}, ${GdmDocumentaleEsterno.GDM_USER}, ${null}, ${conn}, false)")
		ProfiloExtend p = new ProfiloExtend (Long.toString(documento.idDocumentoEsterno), GdmDocumentaleEsterno.GDM_USER, null, conn, false);

		boolean creatoNuovoFileGdm = false;
		// se ho l'idFileEsterno, devo sovrascrivere o rinominare il file:
		if (fileAllegato.idFileEsterno > 0) {
			String gdmFileName = p.getFileName(fileAllegato.idFileEsterno);
			log.debug ("Ho il file su gdm con id: ${fileAllegato.idFileEsterno} e nome: ${gdmFileName}")
			if (!fileAllegato.nome.equals(gdmFileName)) {
				log.debug ("Rinomino il file su gdm (${fileAllegato.idFileEsterno}: ${gdmFileName} -> ${fileAllegato.nome}")
				p.renameFileName(gdmFileName, fileAllegato.nome, is);
			} else {
				log.debug ("Aggiorno il file su gdm (${fileAllegato.idFileEsterno}: ${fileAllegato.nome}")
				p.setFileName(fileAllegato.nome, is);
			}
		} else {
			log.debug ("Creo il file su gdm: ${fileAllegato.nome}")
			p.setFileName(fileAllegato.nome, is);
			creatoNuovoFileGdm = true;
		}

		if (!p.salva().booleanValue()) {
			throw new AttiRuntimeException ("Errore in salvataggio: "+p.getError())
		}

		if (!p.accedi().booleanValue()) {
			throw new AttiRuntimeException ("Errore in accedi dopo salvataggio: "+p.getError())
		}

		if (creatoNuovoFileGdm) {
			String gdmIdOggettoFile = p.getIdFile(fileAllegato.nome)
			log.debug ("File su gdm creato con id: ${gdmIdOggettoFile}")
			fileAllegato.idFileEsterno	= Long.parseLong(p.getIdFile(fileAllegato.nome))
		}

		// salvo il file allegato
		fileAllegato.save()
		documento.save()
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
	boolean isFilePresente (IDocumentoEsterno documento, IFileAllegato fileAllegato) {
		Connection conn = dataSource_gdm.connection
		ProfiloExtend p = new ProfiloExtend (Long.toString(documento.idDocumentoEsterno), GdmDocumentaleEsterno.GDM_USER, null, conn, false);

		String nomeFile = p.getFileName(fileAllegato.idFileEsterno);
		return (nomeFile != null && nomeFile.trim().length() > 0);
	}

	@Transactional(readOnly=true)
	InputStream getFile(IDocumentoEsterno documento, IFileAllegato fileAllegato) {
		if (!(documento.idDocumentoEsterno > 0)) {
			throw new AttiRuntimeException ("Non è possibile continuare: il documento non è ancora stato salvato sul documentale esterno (il testo è stato editato?)")
		}

		Connection conn = dataSource_gdm.connection
		ProfiloExtend p = new ProfiloExtend (Long.toString(documento.idDocumentoEsterno), GdmDocumentaleEsterno.GDM_USER, null, conn, false);

		return p.getFileStream(p.getFileName(fileAllegato.idFileEsterno))
	}

	@Transactional
	void removeFile (IDocumentoEsterno documento, IFileAllegato fileAllegato) {
		Connection conn = dataSource_gdm.connection
		if (documento.idDocumentoEsterno > 0) {
			ProfiloExtend p = new ProfiloExtend (Long.toString(documento.idDocumentoEsterno), GdmDocumentaleEsterno.GDM_USER, null, conn, false);
			p.setDeleteFileName(fileAllegato.nome);
			if (!p.salva().booleanValue()) {
				throw new AttiRuntimeException ("Errore in salvataggio: "+p.getError())
			}
		}

		fileAllegato.delete()
		documento.save()
	}

	@Override
	void addFileStorico(IDocumentoStoricoEsterno documento, IFileAllegato fileAllegato, IFileAllegato fileAllegatoDaStoricizzare) {
		fileAllegato.save ()
	}

	@Override
	@Transactional(readOnly = true)
	InputStream getFileStorico(IDocumentoStoricoEsterno documento, IFileAllegato fileAllegato) {
		Connection conn = dataSource_gdm.connection
		ProfiloVersionExtend profiloDocumentoEsterno = new ProfiloVersionExtend(documento.idDocumentoEsterno, documento.getVersioneDocumentoEsterno(), GdmDocumentaleEsterno.GDM_USER, null, conn);

		if (!profiloDocumentoEsterno.accedi ().booleanValue()) {
			throw new AttiRuntimeException (profiloDocumentoEsterno.getError());
		}

		return profiloDocumentoEsterno.getFileStream(fileAllegato.nome);
	}

	@Override
	void removeFileStorico(IDocumentoStoricoEsterno documento, IFileAllegato fileAllegato) {
		// TODO Auto-generated method stub

	}

	public String verificaImpronta(IDocumentoEsterno documento, IFileAllegato fileAllegato){
		Connection conn = dataSource_gdm.connection
		ProfiloExtend p = new ProfiloExtend (Long.toString(documento.idDocumentoEsterno), GdmDocumentaleEsterno.GDM_USER, null, conn, false);
		String result = p.verificaImpronta512(p.getFileName(fileAllegato.idFileEsterno))
		if (result.equals(Global.CODERROR_IA_NESSUN_ERRORE)){
			return "La verifica è andata a buon fine";
		} else if (result.equals(Global.CODERROR_IA_IMPRONTA_ASSENTE)){
  			return "La profilo fà riferimento ad un documento non ancora memorizzato";
		} else if (result.equals(Global.CODERROR_IA_DOCUMENTO_INESISTENTE)){
		  return "L'impronta dell'allegato non è stata generata";
		} else if (result.equals(Global.CODERROR_IA_ALLEGATO_MODIFICATO)){
		  return "L'allegato risulta modificato";
		} else if (result.equals(Global.CODERROR_IA_ALLEGATO_CANCELLATO)){
		  return "L'allegato risulta cancellato"
		} else { return "Errore durante la verifica dell'impronta"}

	}

}
