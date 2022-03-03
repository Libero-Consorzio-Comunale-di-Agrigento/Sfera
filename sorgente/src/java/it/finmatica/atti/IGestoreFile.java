package it.finmatica.atti;

import java.io.InputStream;

public interface IGestoreFile {

	void addFile (IDocumentoEsterno documento, IFileAllegato fileAllegato, InputStream is);

	void updateFile(IFileAllegato fileAllegato);

	InputStream getFile (IDocumentoEsterno documento, IFileAllegato fileAllegato);

	void removeFile (IDocumentoEsterno documento, IFileAllegato fileAllegato);

	void addFileStorico (IDocumentoStoricoEsterno documento, IFileAllegato fileAllegato, IFileAllegato fileAllegatoDaStoricizzare);

	InputStream getFileStorico (IDocumentoStoricoEsterno documento, IFileAllegato fileAllegato);

	void removeFileStorico (IDocumentoStoricoEsterno documento, IFileAllegato fileAllegato);

	boolean isFilePresente (IDocumentoEsterno documento, IFileAllegato fileAllegato);

	String verificaImpronta(IDocumentoEsterno documento, IFileAllegato fileAllegato);
}
