package it.finmatica.atti.commons

import groovy.transform.CompileStatic
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.ISoggettoDocumento
import it.finmatica.atti.documenti.ITipologia
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.StatoMarcatura
import it.finmatica.dto.DTO
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

/**
 * Created by czappavigna on 19/09/2017.
 */
@CompileStatic
class DocumentoGenerico implements IDocumento, DTO<DocumentoGenerico> {

    String TIPO_OGGETTO
    Long idDocumentoEsterno
    Long id
    int annoProtocollo
    int numeroProtocollo
    Date dataNumeroProtocollo
    So4Amministrazione ente

    @Override
    Long getIdDocumentoEsterno() {
        return idDocumentoEsterno
    }

    @Override
    Long getId () {
        return id
    }

    String getTipoOggetto () {
        return TIPO_OGGETTO
    }

    @Override
    long getIdDocumento () {
        return 0
    }

    @Override
    void setIter(WkfIter iter) {}

    @Override
    WkfIter getIter() { return null }

    @Override
    ITipologia getTipologiaDocumento() { return null }

    @Override
    StatoDocumento getStato() { return null }

    @Override
    void setStato(StatoDocumento stato) { }

    @Override
    StatoFirma getStatoFirma() { return null }

    @Override
    void setStatoFirma(StatoFirma statoFirma) { }

    @Override
    FileAllegato getTesto() { return null }

    @Override
    void setTesto(FileAllegato testo) { }

    @Override
    FileAllegato getTestoOdt() { return null }

    @Override
    void setTestoOdt(FileAllegato testoOdt) { }

    @Override
    GestioneTestiModello getModelloTesto() { return null }

    @Override
    void setModelloTesto(GestioneTestiModello modelloTesto) { }

    Collection<ISoggettoDocumento> getSoggetti() { return [] }

    @Override
    ISoggettoDocumento getSoggetto(String tipoSoggetto) { return null }

    @Override
    void setSoggetto(String tipoSoggetto, Ad4Utente utenteAd4, So4UnitaPubb unitaSo4) { }

    @Override
    void setSoggetto(String tipoSoggetto, Ad4Utente utenteAd4, So4UnitaPubb unitaSo4, int sequenza) { }

    @Override
    So4UnitaPubb getUnitaProponente() { return null }

    @Override
    Set<Allegato> getAllegati () {
        return new HashSet<Allegato>()
    }

    @Override
    String getNomeFileTestoPdf() { return null }

    @Override
    String getNomeFile() { return null }

    @Override
    boolean isRiservato () {
        return false
    }

    DTO toDTO() {
        return this
    }

    @Override
    DocumentoGenerico getDomainObject() {
        return this
    }

    @Override
    DocumentoGenerico copyToDomainObject() {
        return null
    }

    @Override
    StatoMarcatura getStatoMarcatura() { return null }

    @Override
    void setStatoMarcatura(StatoMarcatura statoMarcatura) { }


}
