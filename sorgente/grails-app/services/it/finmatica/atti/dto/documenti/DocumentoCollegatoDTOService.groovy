package it.finmatica.atti.dto.documenti

import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.documenti.*
import it.finmatica.atti.integrazioni.lettera.IntegrazioneLetteraAgspr
import it.finmatica.dto.DTO
import org.zkoss.zk.ui.Executions

class DocumentoCollegatoDTOService {

    IDocumentaleEsterno gestoreDocumentaleEsterno
    IntegrazioneLetteraAgspr integrazioneLetteraAgspr

    DTO<?> aggiungiDocumentiCollegati (DTO<?> documentoPrincipaleDto, List<DocumentoCollegatoDTO> documentiCollegatiDto) {
        IDocumento documentoPrincipale = documentoPrincipaleDto.domainObject

        for (DocumentoCollegatoDTO documentoCollegatoDTO : documentiCollegatiDto) {
            DocumentoCollegato documentoCollegato = creaDocumentoCollegato(documentoCollegatoDTO)
            documentoPrincipale.addToDocumentiCollegati(documentoCollegato)
        }

        documentoPrincipale.save()
        return documentoPrincipale.toDTO()
    }

    DocumentoCollegato creaDocumentoCollegato (DocumentoCollegatoDTO documentoCollegatoDto) {
        DocumentoCollegato documentoCollegato = new DocumentoCollegato()

        documentoCollegato.determinaCollegata = documentoCollegatoDto.determinaCollegata?.domainObject
        documentoCollegato.deliberaCollegata = documentoCollegatoDto.deliberaCollegata?.domainObject
        documentoCollegato.riferimentoEsternoCollegato = getRiferimentoEsterno(documentoCollegatoDto.riferimentoEsternoCollegato)
        documentoCollegato.operazione = documentoCollegatoDto.operazione

        return documentoCollegato
    }

    RiferimentoEsterno getRiferimentoEsterno (RiferimentoEsternoDTO riferimentoEsternoDTO) {
        if (riferimentoEsternoDTO == null) {
            return null
        }

        RiferimentoEsterno riferimentoEsterno = riferimentoEsternoDTO.domainObject
        if (riferimentoEsterno == null) {
            riferimentoEsterno = new RiferimentoEsterno()
        }
        riferimentoEsterno.titolo = riferimentoEsternoDTO.titolo
        riferimentoEsterno.tipoDocumento = riferimentoEsternoDTO.tipoDocumento
        riferimentoEsterno.idDocumentoEsterno = riferimentoEsternoDTO.idDocumentoEsterno
        riferimentoEsterno.codiceDocumentaleEsterno = riferimentoEsternoDTO.codiceDocumentaleEsterno
        riferimentoEsterno.save()
        return riferimentoEsterno
    }

    def eliminaDocumentoCollegato (def documentoPrincipaleDto, long idDocumentoCollegato) {
        DocumentoCollegato delCol = DocumentoCollegato.get(idDocumentoCollegato)
        def documentoPrincipale = documentoPrincipaleDto.domainObject

        documentoPrincipale.removeFromDocumentiCollegati(delCol)
        documentoPrincipale.save()

        if (delCol.riferimentoEsternoCollegato != null) {
            RiferimentoEsterno riferimentoEsterno = delCol.riferimentoEsternoCollegato
            delCol.riferimentoEsternoCollegato = null
            riferimentoEsterno.delete()
        }

        delCol.delete()
        return documentoPrincipale.toDTO()
    }

    void apriDocumento (DocumentoCollegatoDTO documentoCollegato) {
        if (documentoCollegato.tipoDocumento == Determina.TIPO_OGGETTO) {
            Executions.createComponents("/atti/documenti/determina.zul", null, [id: documentoCollegato.documento.id]).doModal()
        } else if (documentoCollegato.tipoDocumento == Delibera.TIPO_OGGETTO) {
            Executions.createComponents("/atti/documenti/delibera.zul", null, [id: documentoCollegato.documento.id]).doModal()
        } else if (documentoCollegato.tipoDocumento == PropostaDelibera.TIPO_OGGETTO) {
            Executions.createComponents("/atti/documenti/propostaDelibera.zul", null, [id: documentoCollegato.documento.id]).doModal()
        } else if ("LETTERA".equals(documentoCollegato?.riferimentoEsternoCollegato?.tipoDocumento)) {
            Executions.getCurrent().sendRedirect(integrazioneLetteraAgspr.getUrlLettera(documentoCollegato.riferimentoEsternoCollegato.idDocumentoEsterno), "_blank")
        } else {
            Executions.getCurrent().sendRedirect(gestoreDocumentaleEsterno.getUrlDocumento(documentoCollegato.riferimentoEsternoCollegato), "_blank")
        }
   }

    List<DocumentoCollegatoDTO> getListaDocumentiCollegati (IDocumento documento) {
        List<DocumentoCollegatoDTO> documentiCollegati = getDocumentiCollegatiDiretti(documento).toDTO(
                ["determinaCollegata", "deliberaCollegata", "riferimentoEsternoCollegato"])
        documentiCollegati*.collegamentoInverso = false

        List<DocumentoCollegatoDTO> documentiCollegatiInversi = getDocumentiCollegatiInversi(documento).toDTO(
                ["determinaPrincipale", "propostaDeliberaPrincipale", "deliberaPrincipale", "riferimentoEsternoCollegato"])
        documentiCollegatiInversi*.collegamentoInverso = true

        documentiCollegati.addAll(documentiCollegatiInversi)
        return documentiCollegati
    }

    List<DocumentoCollegato> getDocumentiCollegatiDiretti (Determina determina) {
        return determina.documentiCollegati?.toList() ?: new ArrayList<DocumentoCollegato>()
    }

    List<DocumentoCollegato> getDocumentiCollegatiDiretti (PropostaDelibera propostaDelibera) {
        return propostaDelibera.documentiCollegati?.toList() ?: new ArrayList<DocumentoCollegato>()
    }

    List<DocumentoCollegato> getDocumentiCollegatiDiretti (Delibera delibera) {
        return getDocumentiCollegatiDiretti(delibera.proposta) + (delibera?.documentiCollegati?.toList() ?: new ArrayList<DocumentoCollegato>())
    }

    List<DocumentoCollegatoDTO> getDocumentiCollegatiInversi (Determina determina) {
        return DocumentoCollegato.findAllByDeterminaCollegata(determina)
    }

    // per la proposta di delibera non ho documenti "inversi"
    List<DocumentoCollegatoDTO> getDocumentiCollegatiInversi (PropostaDelibera propostaDelibera) {
        return new ArrayList<DocumentoCollegatoDTO>()
    }

    List<DocumentoCollegatoDTO> getDocumentiCollegatiInversi (Delibera delibera) {
        return DocumentoCollegato.findAllByDeliberaCollegata(delibera)
    }
}
