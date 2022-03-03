package it.finmatica.atti.integrazioni.ricercadocumenti

import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.dto.DTO

/**
 * Created by esasdelli on 02/10/2017.
 */
class DocumentoEsterno implements IDocumentoEsterno, DTO<DocumentoEsterno>
{

    Long idDocumentoEsterno

    long idDocumento
    String tipoDocumento

    String estremi
    String oggetto


    @Override
    DocumentoEsterno getDomainObject() {
        return this
    }

    @Override
    DocumentoEsterno copyToDomainObject() {
        return this
    }

    DocumentoEsterno toDTO () {
        return this
    }
}
