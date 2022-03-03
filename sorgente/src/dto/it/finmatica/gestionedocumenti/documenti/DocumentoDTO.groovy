package it.finmatica.gestionedocumenti.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.StatoConservazione
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.StatoMarcatura
import it.finmatica.atti.dto.documenti.FirmatarioDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionedocumenti.soggetti.DocumentoSoggettoDTO
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfTipoOggettoDTO
import it.finmatica.gestioneiter.dto.motore.WkfIterDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

public class DocumentoDTO implements it.finmatica.dto.DTO<Documento> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    Set<GdoDocumentoCollegatoDTO> documentiCollegati;
    So4AmministrazioneDTO ente;
    List<FileDocumentoDTO> fileDocumenti;
    Long idDocumentoEsterno;
    WkfIterDTO iter;
    Date lastUpdated;
    boolean riservato;
    Set<DocumentoSoggettoDTO> soggetti;
    StatoDocumento stato;
    StatoConservazione statoConservazione;
    StatoFirma statoFirma;
    StatoMarcatura statoMarcatura;
    WkfTipoOggettoDTO tipoOggetto;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Set<FirmatarioDTO> firmatari;

    public void addToFirmatari (FirmatarioDTO firmatario) {
        if (this.firmatari == null) {
            this.firmatari = new HashSet<FirmatarioDTO>()
        }
        this.firmatari.add(firmatario);
        firmatario.documento = this
    }

    public void removeFromFirmatari (FirmatarioDTO firmatario) {
        if (this.firmatari == null) {
            this.firmatari = new HashSet<FirmatarioDTO>()
        }
        this.firmatari.remove(firmatario);
        firmatario.documento = null
    }

    public void addToDocumentiCollegati (GdoDocumentoCollegatoDTO documentoCollegato) {
        if (this.documentiCollegati == null)
            this.documentiCollegati = new HashSet<GdoDocumentoCollegatoDTO>()
        this.documentiCollegati.add (documentoCollegato);
        documentoCollegato.documento = this
    }

    public void removeFromDocumentiCollegati (GdoDocumentoCollegatoDTO documentoCollegato) {
        if (this.documentiCollegati == null)
            this.documentiCollegati = new HashSet<GdoDocumentoCollegatoDTO>()
        this.documentiCollegati.remove (documentoCollegato);
        documentoCollegato.documento = null
    }

    public void addToFileDocumenti (FileDocumentoDTO fileDocumento) {
        if (this.fileDocumenti == null)
            this.fileDocumenti = new ArrayList<FileDocumentoDTO>()
        this.fileDocumenti.add (fileDocumento);
        fileDocumento.documento = this
    }

    public void removeFromFileDocumenti (FileDocumentoDTO fileDocumento) {
        if (this.fileDocumenti == null)
            this.fileDocumenti = new ArrayList<FileDocumentoDTO>()
        this.fileDocumenti.remove (fileDocumento);
        fileDocumento.documento = null
    }

    public void addToSoggetti (DocumentoSoggettoDTO documentoSoggetto) {
        if (this.soggetti == null)
            this.soggetti = new HashSet<DocumentoSoggettoDTO>()
        this.soggetti.add (documentoSoggetto);
        documentoSoggetto.documento = this
    }

    public void removeFromSoggetti (DocumentoSoggettoDTO documentoSoggetto) {
        if (this.soggetti == null)
            this.soggetti = new HashSet<DocumentoSoggettoDTO>()
        this.soggetti.remove (documentoSoggetto);
        documentoSoggetto.documento = null
    }

    public Documento getDomainObject () {
        return Documento.get(this.id)
    }

    public Documento copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

}
