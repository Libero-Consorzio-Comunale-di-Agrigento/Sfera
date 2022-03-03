package it.finmatica.atti.integrazioni.ricercadocumenti

import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IFileAllegato
import it.finmatica.atti.documenti.DocumentoFactory
import it.finmatica.dto.DTO
import it.finmatica.gestionetesti.TipoFile
import org.apache.commons.io.FilenameUtils

/**
 * Created by dscandurra on 16/11/2017.
 */
class AllegatoEsterno implements IFileAllegato, DTO<AllegatoEsterno> {

    long id

    // dati relativi al documento principale che contiene il file allegato
    String tipoDocumento
    Long idDocumentoPrincipale

    Long idFileEsterno
    Long idFileAllegato
    String nome
    String formatoFile
    String contentType

    String estremi
    String oggetto

    IDocumentoEsterno getDocumentoEsterno () {
        // nel caso di allegati provenienti da una ricerca protocolli GDM
        if (tipoDocumento == "PROTOCOLLO") {
            return new DocumentoEsterno(idDocumentoEsterno: idDocumentoPrincipale)
        }

        // nel caso di allegati provenienti da ricerche determine e delibere
        return DocumentoFactory.getDocumento(idDocumentoPrincipale, tipoDocumento)
    }

    @Override
    String getNome() {
        return nome
    }

    @Override
    String getContentType() {
        return contentType
    }

    @Override
    long getDimensione() {
        return 0
    }

    @Override
    String getTesto() {
        return null
    }

    @Override
    boolean isPdf() {
        return nome.toLowerCase().endsWith(TipoFile.PDF.estensione.toLowerCase())
    }

    @Override
    boolean isP7m() {
        return nome.toLowerCase().endsWith(TipoFile.P7M.estensione.toLowerCase())
    }

    @Override
    boolean isFirmato() {
        return isP7m()
    }

    @Override
    boolean isModificabile() {
        return isP7m() || isPdf()
    }

    transient String getNomeFileSbustato () {
        return this.nome.replaceAll(/(\.[pP]7[mM])+$/, "")
    }

    transient String getNomePdf () {
        return this.nome.replaceAll(/\..+$/, ".pdf")
    }

    @Override
    AllegatoEsterno getDomainObject() {
        return this
    }

    @Override
    AllegatoEsterno copyToDomainObject() {
        return this
    }

    AllegatoEsterno toDTO () {
        return this
    }

    @Override
    public String getNomeFileOriginale(){
        return this.nome
    }
}
