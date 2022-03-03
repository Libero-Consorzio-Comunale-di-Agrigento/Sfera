package it.finmatica.atti.dto.commons

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.commons.FileAllegato
import grails.compiler.GrailsCompileStatic
import it.finmatica.atti.documenti.StatoMarcatura
import it.finmatica.atti.impostazioni.Impostazioni
import org.apache.commons.io.FilenameUtils

@GrailsCompileStatic
public class FileAllegatoDTO implements it.finmatica.dto.DTO<FileAllegato> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    byte[] allegato;
    byte[] allegatoOriginale;
    String contentType;
    String contentTypeOriginale;
    long dimensione;
    boolean firmato;
    Long idFileEsterno;
    boolean modificabile;
    String nome;
    String nomeOriginale;
    String testo;
    FileAllegatoDTO fileOriginale;
    Date dateCreated;
    Date lastUpdated;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    StatoMarcatura statoMarcatura;
    Set<FileFirmatoDettaglioDTO> fileFirmatiDettagli;

    public FileAllegato getDomainObject () {
        return FileAllegato.get(this.id)
    }

    public FileAllegato copyToDomainObject () {
        return null
    }

    public void addToFileFirmati (FileFirmatoDettaglioDTO fileFirmatoDettaglioDTO) {
        if (this.fileFirmatiDettagli == null)
            this.fileFirmatiDettagli = new HashSet<FileFirmatoDettaglioDTO>()
        this.fileFirmatiDettagli.add (fileFirmatoDettaglioDTO);
        fileFirmatoDettaglioDTO.fileAllegato = this
    }

    public void removeFromFileAllegati (FileFirmatoDettaglioDTO fileFirmatoDettaglioDTO) {
        if (this.fileFirmatiDettagli == null)
            this.fileFirmatiDettagli = new HashSet<FileFirmatoDettaglioDTO>()
        this.fileFirmatiDettagli.remove (fileFirmatoDettaglioDTO);
        fileFirmatoDettaglioDTO.fileAllegato = null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

	public String getDimensioneMB () {
        return new Double(((dimensione) / 1_000_000)).round(2) + " MB"
    }

    public boolean isConvertibilePdf () {
        return (Impostazioni.ALLEGATO_CONVERTI_PDF_FORMATO.valori.contains(FilenameUtils.getExtension(nome).toLowerCase()));
    }
}
