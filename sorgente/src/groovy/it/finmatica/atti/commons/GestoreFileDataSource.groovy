package it.finmatica.atti.commons

import grails.util.Holders
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IFileAllegato
import it.finmatica.atti.IGestoreFile
import org.apache.commons.lang.NotImplementedException

import javax.activation.DataSource

/**
 * Created by esasdelli on 07/11/2017.
 */
class GestoreFileDataSource implements DataSource {
    IDocumentoEsterno documentoEsterno
    IFileAllegato fileAllegato

    GestoreFileDataSource (IDocumentoEsterno documentoEsterno, IFileAllegato fileAllegato) {
        this.documentoEsterno = documentoEsterno
        this.fileAllegato = fileAllegato
    }

    private IGestoreFile getGestoreFile () {
        return Holders.applicationContext.getBean ("gestoreFile")
    }

    @Override
    InputStream getInputStream () throws IOException {
        return getGestoreFile().getFile(documentoEsterno, fileAllegato)
    }

    @Override
    OutputStream getOutputStream () throws IOException {
        throw new NotImplementedException("Non implementato.")
    }

    @Override
    String getContentType () {
        return fileAllegato.contentType
    }

    @Override
    String getName () {
        return fileAllegato.nome
    }
}
