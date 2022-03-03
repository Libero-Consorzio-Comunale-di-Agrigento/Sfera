package it.finmatica.atti.dto.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.StatoMarcatura
import it.finmatica.atti.dto.commons.FileAllegatoDTO
import it.finmatica.atti.dto.dizionari.TipoAllegatoDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

public class AllegatoDTO implements it.finmatica.dto.DTO<Allegato> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String codice;
    Date dateCreated;
    DeliberaDTO delibera;
    String descrizione;
    DeterminaDTO determina;
    So4AmministrazioneDTO ente;
    Set<FileAllegatoDTO> fileAllegati;
    Long idDocumentoEsterno;
    Date lastUpdated;
    Integer numPagine;
    String origine;
    PropostaDeliberaDTO propostaDelibera;
    boolean pubblicaAlbo;
    boolean pubblicaCasaDiVetro;
    boolean pubblicaVisualizzatore;
    int quantita;
    boolean riservato;
    int sequenza;
    boolean stampaUnica;
    StatoFirma statoFirma;
    TipoAllegatoDTO tipoAllegato;
    String titolo;
    String ubicazione;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    VistoParereDTO vistoParere;

    public void addToFileAllegati (FileAllegatoDTO fileAllegato) {
        if (this.fileAllegati == null)
            this.fileAllegati = new HashSet<FileAllegatoDTO>()
        this.fileAllegati.add (fileAllegato);
    }

    public void removeFromFileAllegati (FileAllegatoDTO fileAllegato) {
        if (this.fileAllegati == null)
            this.fileAllegati = new HashSet<FileAllegatoDTO>()
        this.fileAllegati.remove (fileAllegato);
    }

    public Allegato getDomainObject () {
        return Allegato.get(this.id)
    }

    public Allegato copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

	public def getDocumentoPrincipale () {
		return ((delibera)?:propostaDelibera)?:determina;
	}

    public boolean isMarcato() {
        return (Allegato.createCriteria().count {
            createAlias("fileAllegati", "fa")
            eq ("id",  this.id)
            eq ("fa.statoMarcatura", StatoMarcatura.MARCATO)
        } > 0)
    }
}
