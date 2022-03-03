package it.finmatica.atti.dto.documenti.tipologie

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.impostazioni.CaratteristicaTipologiaDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class TipoDeliberaDTO implements it.finmatica.dto.DTO<TipoDelibera> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    boolean adottabile;
    CaratteristicaTipologiaDTO caratteristicaTipologia;
    CaratteristicaTipologiaDTO caratteristicaTipologiaDelibera;
    CaratteristicaTipologiaDTO caratteristicaTipologiaFuoriSacco;
    boolean categoriaObbligatoria;
    String codiceEsterno;
    CommissioneDTO commissione;
    boolean conservazioneSostitutiva;
    boolean copiaTestoProposta;
    boolean allegatoTestoProposta;
    Date dateCreated;
    boolean delega;
    boolean delegaObbligatoria;
    String descrizione;
    boolean diventaEsecutiva;
    So4AmministrazioneDTO ente;
    boolean eseguibilitaImmediata;
    boolean funzionarioObbligatorio;
    Integer giorniPubblicazione;
    boolean giorniPubblicazioneModificabile;
    boolean pubblicazioneFutura;
    Date lastUpdated;
    boolean manuale;
    Set<GestioneTestiModelloDTO> modelliTesto;
    GestioneTestiModelloDTO modelloTesto;
    GestioneTestiModelloDTO modelloTestoDelibera;
    GestioneTestiModelloDTO modelloTestoFrontespizio;
    boolean movimentiContabili;
    boolean queryMovimenti;
    boolean notificaOrganiControllo;
    Set<ParametroTipologiaDTO> parametri;
    Long progressivoCfgIter;
    Long progressivoCfgIterDelibera;
    Long progressivoCfgIterFuoriSacco;
    Long progressivoCfgIterPubblicazione;
    boolean pubblicaAllegati;
    boolean pubblicazione;
    boolean pubblicazioneFinoARevoca;
    boolean pubblicazioneTrasparenza;
    boolean scritturaMovimentiContabili;
    boolean secondaPubblicazione;
    boolean testoObbligatorio;
    boolean pubblicaAllegatiDefault;
    boolean pubblicaVisualizzatore;
    boolean pubblicaAllegatiVisualizzatore;
    Set<TipoVistoParereDTO> tipiVisto;
    TipoCertificatoDTO tipoCertAvvPubb;
    TipoCertificatoDTO tipoCertAvvPubb2;
    TipoCertificatoDTO tipoCertEsec;
    TipoCertificatoDTO tipoCertImmEseg;
    TipoCertificatoDTO tipoCertPubb;
    TipoCertificatoDTO tipoCertPubb2;
    String tipoPubblicazioneAlbo;
    TipoRegistroDTO tipoRegistroDelibera;
    String titolo;
    String titoloNotifica;
    String descrizioneNotifica;
    String descrizioneNotificaDelibera;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;
    boolean vistiPareri;
    Set<OggettoRicorrenteDTO> oggettiRicorrenti;
    boolean esecutivitaMovimenti;
    boolean incaricatoObbligatorio;
    Long sequenza;
    String ruoloRiservato;

    public void addToModelliTesto (GestioneTestiModelloDTO gestioneTestiModello) {
        if (this.modelliTesto == null)
            this.modelliTesto = new HashSet<GestioneTestiModelloDTO>()
        this.modelliTesto.add (gestioneTestiModello);
    }

    public void removeFromModelliTesto (GestioneTestiModelloDTO gestioneTestiModello) {
        if (this.modelliTesto == null)
            this.modelliTesto = new HashSet<GestioneTestiModelloDTO>()
        this.modelliTesto.remove (gestioneTestiModello);
    }
    public void addToParametri (ParametroTipologiaDTO parametroTipologia) {
        if (this.parametri == null)
            this.parametri = new HashSet<ParametroTipologiaDTO>()
        this.parametri.add (parametroTipologia);
        parametroTipologia.tipoDelibera = this
    }

    public void removeFromParametri (ParametroTipologiaDTO parametroTipologia) {
        if (this.parametri == null)
            this.parametri = new HashSet<ParametroTipologiaDTO>()
        this.parametri.remove (parametroTipologia);
        parametroTipologia.tipoDelibera = null
    }
    public void addToTipiVisto (TipoVistoParereDTO tipoVistoParere) {
        if (this.tipiVisto == null)
            this.tipiVisto = new HashSet<TipoVistoParereDTO>()
        this.tipiVisto.add (tipoVistoParere);
    }

    public void removeFromTipiVisto (TipoVistoParereDTO tipoVistoParere) {
        if (this.tipiVisto == null)
            this.tipiVisto = new HashSet<TipoVistoParereDTO>()
        this.tipiVisto.remove (tipoVistoParere);
    }

    public void addToOggettiRicorrenti (OggettoRicorrenteDTO oggettoRicorrente) {
        if (this.oggettiRicorrenti == null)
            this.oggettiRicorrenti = new HashSet<OggettoRicorrenteDTO>()
        this.oggettiRicorrenti.add (oggettoRicorrente);
    }

    public void removeFromOggettiRicorrenti (OggettoRicorrenteDTO oggettoRicorrente) {
        if (this.oggettiRicorrenti == null)
            this.oggettiRicorrenti = new HashSet<OggettoRicorrenteDTO>()
        this.oggettiRicorrenti.remove (oggettoRicorrente);
    }

    public TipoDelibera getDomainObject () {
        return TipoDelibera.get(this.id)
    }

    public TipoDelibera copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
