package it.finmatica.atti.dto.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.StatoMarcatura
import it.finmatica.atti.documenti.StatoOdg
import it.finmatica.atti.dto.commons.FileAllegatoDTO
import it.finmatica.atti.dto.dizionari.CategoriaDTO
import it.finmatica.atti.dto.dizionari.DelegaDTO
import it.finmatica.atti.dto.dizionari.IndirizzoDeliberaDTO
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoDeliberaDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.motore.WkfIterDTO
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class PropostaDeliberaDTO implements it.finmatica.dto.DTO<PropostaDelibera> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Set<AllegatoDTO> allegati;
    Integer annoProposta;
    String campiProtetti;
    CategoriaDTO categoria;
    String classificaCodice;
    Date classificaDal;
    String classificaDescrizione;
    String codiciVistiTrattati;
    CommissioneDTO commissione;
    boolean controlloFunzionario;
    boolean daInviareCorteConti;
    Date dataNumeroProposta;
    Date dataProposta;
    Date dateCreated;
    DelegaDTO delega;
    Set<DestinatarioNotificaDTO> destinatariNotifiche;
    Set<DocumentoCollegatoDTO> documentiCollegati;
    So4AmministrazioneDTO ente;
    boolean eseguibilitaImmediata;
    String motivazioniEseguibilita
    Integer fascicoloAnno;
    String fascicoloNumero;
    String fascicoloOggetto;
    Set<FirmatarioDTO> firmatari;
    boolean fuoriSacco;
    Integer giorniPubblicazione;
    Long idDocumentoEsterno;
    IndirizzoDeliberaDTO indirizzo;
    WkfIterDTO iter;
    Date lastUpdated;
    GestioneTestiModelloDTO modelloTesto;
    GestioneTestiModelloDTO modelloTestoAnnullamento;
    String note;
    String noteCommissione;
    String noteContabili;
    String noteTrasmissione;
    Integer numeroProposta;
    String oggetto;
    OggettoSedutaDTO oggettoSeduta;
    boolean parereRevisoriConti;
    boolean pubblicaRevoca;
    TipoRegistroDTO registroProposta;
    boolean riservato;
    Set<PropostaDeliberaSoggettoDTO> soggetti;
    FileAllegatoDTO stampaUnica;
    StatoDocumento stato;
    StatoFirma statoFirma;
    StatoOdg statoOdg;
    StatoMarcatura statoMarcatura;
    FileAllegatoDTO testo;
    FileAllegatoDTO testoOdt;
    TipoDeliberaDTO tipologia;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Set<VistoParereDTO> visti;
    OggettoRicorrenteDTO oggettoRicorrente;
    Date dataMinimaPubblicazione;
    Date dataScadenza;
    String motivazione;
    Integer priorita;
    Set<DatoAggiuntivoDTO> datiAggiuntivi;
    Set<BudgetDTO> budgets;
    Date dataOrdinamento;
    boolean controllaDestinatari;

    public void addToAllegati (AllegatoDTO allegato) {
        if (this.allegati == null) {
            this.allegati = new HashSet<AllegatoDTO>()
        }
        this.allegati.add(allegato);
        allegato.propostaDelibera = this
    }

    public void removeFromAllegati (AllegatoDTO allegato) {
        if (this.allegati == null) {
            this.allegati = new HashSet<AllegatoDTO>()
        }
        this.allegati.remove(allegato);
        allegato.propostaDelibera = null
    }

    public void addToDestinatariNotifiche (DestinatarioNotificaDTO destinatarioNotifica) {
        if (this.destinatariNotifiche == null) {
            this.destinatariNotifiche = new HashSet<DestinatarioNotificaDTO>()
        }
        this.destinatariNotifiche.add(destinatarioNotifica);
        destinatarioNotifica.propostaDelibera = this
    }

    public void removeFromDestinatariNotifiche (DestinatarioNotificaDTO destinatarioNotifica) {
        if (this.destinatariNotifiche == null) {
            this.destinatariNotifiche = new HashSet<DestinatarioNotificaDTO>()
        }
        this.destinatariNotifiche.remove(destinatarioNotifica);
        destinatarioNotifica.propostaDelibera = null
    }

    public void addToDocumentiCollegati (DocumentoCollegatoDTO documentoCollegato) {
        if (this.documentiCollegati == null) {
            this.documentiCollegati = new HashSet<DocumentoCollegatoDTO>()
        }
        this.documentiCollegati.add(documentoCollegato);
        documentoCollegato.propostaDeliberaPrincipale = this
    }

    public void removeFromDocumentiCollegati (DocumentoCollegatoDTO documentoCollegato) {
        if (this.documentiCollegati == null) {
            this.documentiCollegati = new HashSet<DocumentoCollegatoDTO>()
        }
        this.documentiCollegati.remove(documentoCollegato);
        documentoCollegato.propostaDeliberaPrincipale = null
    }

    public void addToFirmatari (FirmatarioDTO firmatario) {
        if (this.firmatari == null) {
            this.firmatari = new HashSet<FirmatarioDTO>()
        }
        this.firmatari.add(firmatario);
        firmatario.propostaDelibera = this
    }

    public void removeFromFirmatari (FirmatarioDTO firmatario) {
        if (this.firmatari == null) {
            this.firmatari = new HashSet<FirmatarioDTO>()
        }
        this.firmatari.remove(firmatario);
        firmatario.propostaDelibera = null
    }

    public void addToSoggetti (PropostaDeliberaSoggettoDTO propostaDeliberaSoggetto) {
        if (this.soggetti == null) {
            this.soggetti = new HashSet<PropostaDeliberaSoggettoDTO>()
        }
        this.soggetti.add(propostaDeliberaSoggetto);
        propostaDeliberaSoggetto.propostaDelibera = this
    }

    public void removeFromSoggetti (PropostaDeliberaSoggettoDTO propostaDeliberaSoggetto) {
        if (this.soggetti == null) {
            this.soggetti = new HashSet<PropostaDeliberaSoggettoDTO>()
        }
        this.soggetti.remove(propostaDeliberaSoggetto);
        propostaDeliberaSoggetto.propostaDelibera = null
    }

    public void addToVisti (VistoParereDTO vistoParere) {
        if (this.visti == null) {
            this.visti = new HashSet<VistoParereDTO>()
        }
        this.visti.add(vistoParere);
        vistoParere.propostaDelibera = this
    }

    public void removeFromVisti (VistoParereDTO vistoParere) {
        if (this.visti == null) {
            this.visti = new HashSet<VistoParereDTO>()
        }
        this.visti.remove(vistoParere);
        vistoParere.propostaDelibera = null
    }

    public void addToDatiAggiuntivi (DatoAggiuntivoDTO datoAggiuntivo) {
        if (this.datiAggiuntivi == null) {
            this.datiAggiuntivi = new HashSet<DatoAggiuntivoDTO>()
        }
        this.datiAggiuntivi.add(datoAggiuntivo);
        datoAggiuntivo.propostaDelibera = this
    }

    public void removeFromDatiAggiuntivi (DatoAggiuntivoDTO datoAggiuntivo) {
        if (this.datiAggiuntivi == null) {
            this.datiAggiuntivi = new HashSet<DatoAggiuntivoDTO>()
        }
        this.datiAggiuntivi.remove(datoAggiuntivo);
        datoAggiuntivo.propostaDelibera = null
    }

    public PropostaDelibera getDomainObject () {
        return PropostaDelibera.get(this.id)
    }

    public PropostaDelibera copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */
    // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    /**
     * Aggiunge un CODICE_VISTO alla lista di quelli già trattati (concatena codiciVistiTrattati += #CODICE_VISTO)
     *
     * @param codice il codice della tipologia di visto trattata
     */
    public void addCodiceVistoTrattato (String codice) {
        if (codiciVistiTrattati == null) {
            codiciVistiTrattati = ""
        }
        codiciVistiTrattati = (codiciVistiTrattati.tokenize(PropostaDelibera.SEPARATORE) << codice).unique().join(PropostaDelibera.SEPARATORE)
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
        def v = codiciVistiTrattati.tokenize(PropostaDelibera.SEPARATORE)
        v.remove(codice)
        codiciVistiTrattati = v.join(PropostaDelibera.SEPARATORE)
    }

    /**
     * @return la lista dei codici visti trattati
     */
    public String[] getListaCodiciVistiTrattati () {
        if (codiciVistiTrattati == null) {
            codiciVistiTrattati = ""
        }
        return codiciVistiTrattati.tokenize(PropostaDelibera.SEPARATORE).toArray()
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

    String getEstremiAtto () {
        if (numeroProposta > 0) {
            return "${numeroProposta} / ${annoProposta} (${registroProposta.descrizione})"
        }

        return "Proposta non numerata con oggetto: $oggetto."
    }

}
