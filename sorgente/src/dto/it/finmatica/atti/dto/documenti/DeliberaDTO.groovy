package it.finmatica.atti.dto.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.StatoConservazione
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.StatoMarcatura
import it.finmatica.atti.dto.commons.FileAllegatoDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.motore.WkfIterDTO
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class DeliberaDTO implements it.finmatica.dto.DTO<Delibera> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Set<AllegatoDTO> allegati;
    Integer annoAlbo;
    Integer annoDelibera;
    Integer annoProtocollo;
    String campiProtetti;
    Set<CertificatoDTO> certificati;
    String classificaCodice;
    Date classificaDal;
    String classificaDescrizione;
    String codiciVistiTrattati;
    boolean daInviareCorteConti;
    Date dataAdozione;
    Date dataEsecutivita;
    Date dataEsecutivitaManuale;
    Date dataFinePubblicazione;
    Date dataFinePubblicazione2;
    Date dataInvioCorteConti;
    Date dataNumeroDelibera;
    Date dataNumeroProtocollo;
    Date dataPubblicazione;
    Date dataPubblicazione2;
    Date dateCreated;
    boolean diventaEsecutiva;
    So4AmministrazioneDTO ente;
    boolean eseguibilitaImmediata;
    String motivazioniEseguibilita;
    Integer fascicoloAnno;
    String fascicoloNumero;
    String fascicoloOggetto;
    Set<FirmatarioDTO> firmatari;
    Integer giorniPubblicazione;
    Long idDocumentoAlbo;
    Long idDocumentoEsterno;
    WkfIterDTO iter;
    Date lastUpdated;
    GestioneTestiModelloDTO modelloTesto;
    String note;
    String noteTrasmissione;
    Integer numeroAlbo;
    Integer numeroDelibera;
    Integer numeroProtocollo;
    String oggetto;
    OggettoSedutaDTO oggettoSeduta;
    PropostaDeliberaDTO propostaDelibera;
    boolean pubblicaRevoca;
    TipoRegistroDTO registroDelibera;
    TipoRegistroDTO registroProtocollo;
    boolean riservato;
    Set<DeliberaSoggettoDTO> soggetti;
    FileAllegatoDTO stampaUnica;
    StatoDocumento stato;
    StatoConservazione statoConservazione;
    StatoFirma statoFirma;
    StatoMarcatura statoMarcatura
    FileAllegatoDTO testo;
    FileAllegatoDTO testoOdt;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Set<VistoParereDTO> visti;
    boolean daPubblicare;
    boolean pubblicaVisualizzatore;
    Set<DatoAggiuntivoDTO> datiAggiuntivi;
    Set<DocumentoCollegatoDTO> documentiCollegati;

    public void addToAllegati (AllegatoDTO allegato) {
        if (this.allegati == null)
            this.allegati = new HashSet<AllegatoDTO>()
        this.allegati.add (allegato);
        allegato.delibera = this
    }

    public void removeFromAllegati (AllegatoDTO allegato) {
        if (this.allegati == null)
            this.allegati = new HashSet<AllegatoDTO>()
        this.allegati.remove (allegato);
        allegato.delibera = null
    }
    public void addToCertificati (CertificatoDTO certificato) {
        if (this.certificati == null)
            this.certificati = new HashSet<CertificatoDTO>()
        this.certificati.add (certificato);
        certificato.delibera = this
    }

    public void removeFromCertificati (CertificatoDTO certificato) {
        if (this.certificati == null)
            this.certificati = new HashSet<CertificatoDTO>()
        this.certificati.remove (certificato);
        certificato.delibera = null
    }
    public void addToFirmatari (FirmatarioDTO firmatario) {
        if (this.firmatari == null)
            this.firmatari = new HashSet<FirmatarioDTO>()
        this.firmatari.add (firmatario);
        firmatario.delibera = this
    }

    public void removeFromFirmatari (FirmatarioDTO firmatario) {
        if (this.firmatari == null)
            this.firmatari = new HashSet<FirmatarioDTO>()
        this.firmatari.remove (firmatario);
        firmatario.delibera = null
    }
    public void addToSoggetti (DeliberaSoggettoDTO deliberaSoggetto) {
        if (this.soggetti == null)
            this.soggetti = new HashSet<DeliberaSoggettoDTO>()
        this.soggetti.add (deliberaSoggetto);
        deliberaSoggetto.delibera = this
    }

    public void removeFromSoggetti (DeliberaSoggettoDTO deliberaSoggetto) {
        if (this.soggetti == null)
            this.soggetti = new HashSet<DeliberaSoggettoDTO>()
        this.soggetti.remove (deliberaSoggetto);
        deliberaSoggetto.delibera = null
    }
    public void addToVisti (VistoParereDTO vistoParere) {
        if (this.visti == null)
            this.visti = new HashSet<VistoParereDTO>()
        this.visti.add (vistoParere);
        vistoParere.delibera = this
    }

    public void removeFromVisti (VistoParereDTO vistoParere) {
        if (this.visti == null)
            this.visti = new HashSet<VistoParereDTO>()
        this.visti.remove (vistoParere);
        vistoParere.delibera = null
    }

    public void addToDatiAggiuntivi (DatoAggiuntivoDTO datoAggiuntivo) {
        if (this.datiAggiuntivi == null)
            this.datiAggiuntivi = new HashSet<DatoAggiuntivoDTO>()
        this.datiAggiuntivi.add (datoAggiuntivo);
        datoAggiuntivo.delibera = this
    }

    public void removeFromDatiAggiuntivi (DatoAggiuntivoDTO datoAggiuntivo) {
        if (this.datiAggiuntivi == null)
            this.datiAggiuntivi = new HashSet<DatoAggiuntivoDTO>()
        this.datiAggiuntivi.remove (datoAggiuntivo);
        datoAggiuntivo.delibera = null
    }

    public Delibera getDomainObject () {
        return Delibera.get(this.id)
    }

    public Delibera copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

	String firmatariOggetto;
	
	// Questo è solo un alias per la propostaDelibera siccome ne ho cambiato il nome perché andava in conflitto con l'interfaccia IAtto
	public PropostaDeliberaDTO getProposta() {
		return this.propostaDelibera;
	}
	
	// Questo è solo un alias per la propostaDelibera siccome ne ho cambiato il nome perché andava in conflitto con l'interfaccia IAtto
	public void setProposta(PropostaDeliberaDTO propostaDelibera) {
		this.propostaDelibera = propostaDelibera
	}
	
	public String getEstremiDelibera () {
		if (numeroDelibera > 0) {
			return "Delibera n. ${numeroDelibera} / ${annoDelibera}"
		}
		
		return "Delibera"
	}

	public String getEstremiAtto () {
		if (numeroDelibera > 0 ) {
			return "${numeroDelibera} / ${annoDelibera} (${registroDelibera.codice})";
		}
		return "";
	}

	/**
	 * Aggiunge un CODICE_VISTO alla lista di quelli già trattati (concatena codiciVistiTrattati += #CODICE_VISTO)
	 *
	 * @param codice	il codice della tipologia di visto trattata
	 */
	public void addCodiceVistoTrattato (String codice) {
		if (codiciVistiTrattati == null)
			codiciVistiTrattati = ""
		codiciVistiTrattati = (codiciVistiTrattati.tokenize(Delibera.SEPARATORE) << codice).unique().join(Delibera.SEPARATORE)
	}

	/**
	 * Rimuove un CODICE_VISTO alla lista di quelli già trattati (rimuove da codiciVistiTrattati #CODICE_VISTO)
	 *
	 * @param codice	il codice della tipologia di visto trattata
	 */
	public void removeCodiceVistoTrattato (String codice) {
		if (codiciVistiTrattati == null)
			codiciVistiTrattati = ""
		def v = codiciVistiTrattati.tokenize(Delibera.SEPARATORE)
		v.remove(codice)
		codiciVistiTrattati = v.join(Delibera.SEPARATORE)
	}

	/**
	 * @return la lista dei codici visti trattati
	 */
	public String[] getListaCodiciVistiTrattati () {
		if (codiciVistiTrattati == null)
			codiciVistiTrattati = ""
		return codiciVistiTrattati.tokenize(Delibera.SEPARATORE).toArray()
	}

	/**
	 * @param codice	il codice da verificare
	 * @return	true se il codice è presente nei visti trattati
	 */
	public boolean isVistoTrattato(String codice) {
		return getListaCodiciVistiTrattati().contains(codice);
	}

    public void addToDocumentiCollegati (DocumentoCollegatoDTO documentoCollegato) {
        if (this.documentiCollegati == null) {
            this.documentiCollegati = new HashSet<DocumentoCollegatoDTO>()
        }
        this.documentiCollegati.add(documentoCollegato);
        documentoCollegato.deliberaPrincipale = this
    }

    public void removeFromDocumentiCollegati (DocumentoCollegatoDTO documentoCollegato) {
        if (this.documentiCollegati == null) {
            this.documentiCollegati = new HashSet<DocumentoCollegatoDTO>()
        }
        this.documentiCollegati.remove(documentoCollegato);
        documentoCollegato.deliberaPrincipale = null
    }
}
