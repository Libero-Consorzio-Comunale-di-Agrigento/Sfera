package it.finmatica.atti;

public interface IDocumentaleEsterno {

	void salvaDocumento (IDocumentoEsterno documento);

	void storicizzaDocumento (IDocumentoStoricoEsterno documento);

	String getUrlDocumento (IDocumentoEsterno documento);
}
