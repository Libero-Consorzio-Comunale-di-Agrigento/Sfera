package it.finmatica.atti.dto.documenti

import grails.transaction.Transactional

import grails.transaction.Transactional
import it.finmatica.atti.documenti.GruppoPoliticoDocumentoService
import it.finmatica.atti.dto.documenti.GruppoPoliticoDocumentoDTO

@Transactional
class GruppoPoliticoDocumentoDTOService {

    GruppoPoliticoDocumentoService gruppoPoliticoDocumentoService

    public void elimina(GruppoPoliticoDocumentoDTO gruppoPoliticoDocumentoDto, def documentoPrincipaleDto) {
        gruppoPoliticoDocumentoService.elimina(gruppoPoliticoDocumentoDto.domainObject, documentoPrincipaleDto.domainObject);
    }

    public void salva(GruppoPoliticoDocumentoDTO gruppoPoliticoDocumentoDto, def documentoPrincipaleDto) {
        gruppoPoliticoDocumentoService.salva(gruppoPoliticoDocumentoDto.domainObject, documentoPrincipaleDto.domainObject);
    }

}
