package it.finmatica.atti.documenti.beans

import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IDocumentoStoricoEsterno
import it.finmatica.atti.IFileAllegato
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.impostazioni.Impostazioni
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware

class AttiImpostazioniGestoreFile implements IGestoreFile, GrailsApplicationAware {

	private static final transient Logger log = Logger.getLogger(AttiImpostazioniGestoreFile.class)

	GrailsApplication grailsApplication

	void addFile(IDocumentoEsterno documento, IFileAllegato fileAllegato, InputStream is) {
		getGestoreFile().addFile(documento, fileAllegato, is);
	}

	@Override
	void updateFile(IFileAllegato fileAllegato) {
		getGestoreFile().updateFile(fileAllegato)
	}

	@Override
	InputStream getFile(IDocumentoEsterno documento, IFileAllegato fileAllegato) {
		return getGestoreFile().getFile(documento, fileAllegato);
	}

	void removeFile (IDocumentoEsterno documento, IFileAllegato fileAllegato) {
		getGestoreFile().removeFile(documento, fileAllegato);
	}

	@Override
	void addFileStorico(IDocumentoStoricoEsterno documento, IFileAllegato fileAllegato, IFileAllegato fileAllegatoDaStoricizzare) {
		getGestoreFile().addFileStorico(documento, fileAllegato, fileAllegatoDaStoricizzare);
	}

	@Override
	InputStream getFileStorico(IDocumentoStoricoEsterno documento, IFileAllegato fileAllegato) {
		return getGestoreFile().getFileStorico(documento, fileAllegato);
	}

	@Override
	void removeFileStorico(IDocumentoStoricoEsterno documento, IFileAllegato fileAllegato) {
		getGestoreFile().removeFileStorico(documento, fileAllegato);
	}

	@Override
	boolean isFilePresente(IDocumentoEsterno documento, IFileAllegato fileAllegato) {
		getGestoreFile().isFilePresente(documento, fileAllegato);
	}

	private IGestoreFile getGestoreFile () {
		return grailsApplication.mainContext.getBean(Impostazioni.GESTORE_FILE.valore);
	}

	String verificaImpronta(IDocumentoEsterno documento, IFileAllegato fileAllegato){
		return getGestoreFile().verificaImpronta(documento, fileAllegato);
	}
}
