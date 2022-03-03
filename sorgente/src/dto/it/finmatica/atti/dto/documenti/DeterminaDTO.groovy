package it.finmatica.atti.dto.documenti

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.*
import it.finmatica.atti.dto.commons.FileAllegatoDTO
import it.finmatica.atti.dto.dizionari.CategoriaDTO
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoDeterminaDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDTO
import it.finmatica.gestioneiter.dto.motore.WkfIterDTO
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

@GrailsCompileStatic
public class DeterminaDTO implements it.finmatica.dto.DTO<Determina> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Set<AllegatoDTO> allegati;
    Integer annoAlbo;
    Integer annoDetermina;
    Integer annoProposta;
    Integer annoProtocollo;
    String campiProtetti;
    CategoriaDTO categoria;
    Set<CertificatoDTO> certificati;
    String classificaCodice;
    Date classificaDal;
    String classificaDescrizione;
    String codiceGara;
    String codiciVistiTrattati;
    CommissioneDTO commissione;
    boolean controlloFunzionario;
    boolean daInviareCorteConti;
    Date dataEsecutivita;
    Date dataFinePubblicazione;
    Date dataFinePubblicazione2;
    Date dataInvioCorteConti;
    Date dataNumeroDetermina;
    Date dataNumeroProposta;
    Date dataNumeroProtocollo;
    Date dataProposta;
    Date dataProtTesoriere;
    Date dataPubblicazione;
    Date dataPubblicazione2;
    Date dataScadenza;
    Date dateCreated;
    Date dataOrdinamento;
    Set<DestinatarioNotificaDTO> destinatariNotifiche;
    boolean diventaEsecutiva;
    Set<DocumentoCollegatoDTO> documentiCollegati;
    So4AmministrazioneDTO ente;
    Integer fascicoloAnno;
    String fascicoloNumero;
    String fascicoloOggetto;
    Set<FirmatarioDTO> firmatari;
    Integer giorniPubblicazione;
    Long idDocumentoAlbo;
    Long idDocumentoEsterno;
    boolean inviatoTesoriere;
    WkfIterDTO iter;
    Date lastUpdated;
    GestioneTestiModelloDTO modelloTesto;
    String motivazione;
    Integer priorita;
    String note;
    String noteContabili;
    String noteTesoriere;
    String noteTrasmissione;
    Integer numeroAlbo;
    Integer numeroDetermina;
    Integer numeroProposta;
    Integer numeroProtTesoriere;
    Integer numeroProtocollo;
    String oggetto;
    OggettoSedutaDTO oggettoSeduta;
    boolean pubblicaRevoca;
    TipoRegistroDTO registroDetermina;
    TipoRegistroDTO registroProposta;
    TipoRegistroDTO registroProtocollo;
    boolean riservato;
    Set<DeterminaSoggettoDTO> soggetti;
    FileAllegatoDTO stampaUnica;
    StatoDocumento stato;
    StatoConservazione statoConservazione;
    StatoFirma statoFirma;
    StatoOdg statoOdg;
    StatoMarcatura statoMarcatura
    FileAllegatoDTO testo;
    FileAllegatoDTO testoOdt;
    TipoDeterminaDTO tipologia;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Set<VistoParereDTO> visti;
    Date dataMinimaPubblicazione;
    boolean daPubblicare;
    boolean pubblicaVisualizzatore;
    Set<DatoAggiuntivoDTO> datiAggiuntivi;
    boolean eseguibilitaImmediata;
    String motivazioniEseguibilita;
    Set<BudgetDTO> budgets;

    Date dataNumeroDetermina2
    Integer numeroDetermina2
    Integer annoDetermina2
    TipoRegistroDTO registroDetermina2
    OggettoRicorrenteDTO oggettoRicorrente;
    boolean controllaDestinatari;

    public void addToAllegati (AllegatoDTO allegato) {
        if (this.allegati == null) {
            this.allegati = new HashSet<AllegatoDTO>()
        }
        this.allegati.add(allegato);
        allegato.determina = this
    }

    public void removeFromAllegati (AllegatoDTO allegato) {
        if (this.allegati == null) {
            this.allegati = new HashSet<AllegatoDTO>()
        }
        this.allegati.remove(allegato);
        allegato.determina = null
    }

    public void addToCertificati (CertificatoDTO certificato) {
        if (this.certificati == null) {
            this.certificati = new HashSet<CertificatoDTO>()
        }
        this.certificati.add(certificato);
        certificato.determina = this
    }

    public void removeFromCertificati (CertificatoDTO certificato) {
        if (this.certificati == null) {
            this.certificati = new HashSet<CertificatoDTO>()
        }
        this.certificati.remove(certificato);
        certificato.determina = null
    }

    public void addToDestinatariNotifiche (DestinatarioNotificaDTO destinatarioNotifica) {
        if (this.destinatariNotifiche == null) {
            this.destinatariNotifiche = new HashSet<DestinatarioNotificaDTO>()
        }
        this.destinatariNotifiche.add(destinatarioNotifica);
        destinatarioNotifica.determina = this
    }

    public void removeFromDestinatariNotifiche (DestinatarioNotificaDTO destinatarioNotifica) {
        if (this.destinatariNotifiche == null) {
            this.destinatariNotifiche = new HashSet<DestinatarioNotificaDTO>()
        }
        this.destinatariNotifiche.remove(destinatarioNotifica);
        destinatarioNotifica.determina = null
    }

    public void addToDocumentiCollegati (DocumentoCollegatoDTO documentoCollegato) {
        if (this.documentiCollegati == null) {
            this.documentiCollegati = new HashSet<DocumentoCollegatoDTO>()
        }
        this.documentiCollegati.add(documentoCollegato);
        documentoCollegato.determinaPrincipale = this
    }

    public void removeFromDocumentiCollegati (DocumentoCollegatoDTO documentoCollegato) {
        if (this.documentiCollegati == null) {
            this.documentiCollegati = new HashSet<DocumentoCollegatoDTO>()
        }
        this.documentiCollegati.remove(documentoCollegato);
        documentoCollegato.determinaPrincipale = null
    }

    public void addToFirmatari (FirmatarioDTO firmatario) {
        if (this.firmatari == null) {
            this.firmatari = new HashSet<FirmatarioDTO>()
        }
        this.firmatari.add(firmatario);
        firmatario.determina = this
    }

    public void removeFromFirmatari (FirmatarioDTO firmatario) {
        if (this.firmatari == null) {
            this.firmatari = new HashSet<FirmatarioDTO>()
        }
        this.firmatari.remove(firmatario);
        firmatario.determina = null
    }

    public void addToSoggetti (DeterminaSoggettoDTO determinaSoggetto) {
        if (this.soggetti == null) {
            this.soggetti = new HashSet<DeterminaSoggettoDTO>()
        }
        this.soggetti.add(determinaSoggetto);
        determinaSoggetto.determina = this
    }

    public void removeFromSoggetti (DeterminaSoggettoDTO determinaSoggetto) {
        if (this.soggetti == null) {
            this.soggetti = new HashSet<DeterminaSoggettoDTO>()
        }
        this.soggetti.remove(determinaSoggetto);
        determinaSoggetto.determina = null
    }

    public void addToVisti (VistoParereDTO vistoParere) {
        if (this.visti == null) {
            this.visti = new HashSet<VistoParereDTO>()
        }
        this.visti.add(vistoParere);
        vistoParere.determina = this
    }

    public void removeFromVisti (VistoParereDTO vistoParere) {
        if (this.visti == null) {
            this.visti = new HashSet<VistoParereDTO>()
        }
        this.visti.remove(vistoParere);
        vistoParere.determina = null
    }

    public void addToDatiAggiuntivi (DatoAggiuntivoDTO datoAggiuntivo) {
        if (this.datiAggiuntivi == null) {
            this.datiAggiuntivi = new HashSet<DatoAggiuntivoDTO>()
        }
        this.datiAggiuntivi.add(datoAggiuntivo);
        datoAggiuntivo.determina = this
    }

    public void removeFromDatiAggiuntivi (DatoAggiuntivoDTO datoAggiuntivo) {
        if (this.datiAggiuntivi == null) {
            this.datiAggiuntivi = new HashSet<DatoAggiuntivoDTO>()
        }
        this.datiAggiuntivi.remove(datoAggiuntivo);
        datoAggiuntivo.determina = null
    }

    public Determina getDomainObject () {
        return Determina.get(this.id)
    }

    public Determina copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */
    // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    public String getEstremiAtto () {
        if (numeroDetermina > 0) {
            return "${numeroDetermina} / ${annoDetermina} (${registroDetermina.codice})";
        }
        return "${numeroProposta} / ${annoProposta} (${registroProposta.codice})";
    }

    /**
     * Aggiunge un CODICE_VISTO alla lista di quelli già trattati (concatena codiciVistiTrattati += #CODICE_VISTO)
     *
     * @param codice il codice della tipologia di visto trattata
     */
    public void addCodiceVistoTrattato (String codice) {
        if (codiciVistiTrattati == null) {
            codiciVistiTrattati = ""
        }
        codiciVistiTrattati = (codiciVistiTrattati.tokenize(Determina.SEPARATORE) << codice).unique().join(Determina.SEPARATORE)
    }

    /**
     * Rimuove un CODICE_VISTO alla lista di quelli già trattati (rimuove da codiciVistiTrattati #CODICE_VISTO)
     *
     * @param codice il codice della tipologia di visto trattata
     */
    public void removeCodiceVistoTrattato (String codice) {
        if (codiciVistiTrattati == null) {
            codiciVistiTrattati = ""
        }
        def v = codiciVistiTrattati.tokenize(Determina.SEPARATORE)
        v.remove(codice)
        codiciVistiTrattati = v.join(Determina.SEPARATORE)
    }

    /**
     * @return la lista dei codici visti trattati
     */
    public String[] getListaCodiciVistiTrattati () {
        if (codiciVistiTrattati == null) {
            codiciVistiTrattati = ""
        }
        return codiciVistiTrattati.tokenize(Determina.SEPARATORE).toArray()
    }

    /**
     * @param codice il codice da verificare
     * @return true se il codice è presente nei visti trattati
     */
    public boolean isVistoTrattato (String codice) {
        return getListaCodiciVistiTrattati().contains(codice);
    }

    /**
     * @return l'elenco dei destinatari interni
     */
    public List<it.finmatica.atti.documenti.DestinatarioNotifica> getDestinatariInterni () {
        return destinatariNotifiche?.toList()?.findAll { destinatario -> destinatario.tipoDestinatario == it.finmatica.atti.documenti.DestinatarioNotifica.TIPO_DESTINATARIO_INTERNO }
    }

    /**
     * @return l'elenco dei destinatari esterni
     */
    public List<it.finmatica.atti.documenti.DestinatarioNotifica> getDestinatariEsterni () {
        return destinatariNotifiche?.toList()?.findAll { destinatario -> destinatario.tipoDestinatario == it.finmatica.atti.documenti.DestinatarioNotifica.TIPO_DESTINATARIO_ESTERNO }
    }
}
