package it.finmatica.atti.dto.documenti.tipologie

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.impostazioni.CaratteristicaTipologiaDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class TipoDeterminaDTO implements it.finmatica.dto.DTO<TipoDetermina> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    CaratteristicaTipologiaDTO caratteristicaTipologia;
    boolean categoriaObbligatoria;
    String codiceEsterno;
    boolean codiceGara;
    boolean codiceGaraObbligatorio;
    boolean cupObbligatorio;
    boolean cupVisibile;
    boolean conservazioneSostitutiva;
    Date dateCreated;
    String descrizione;
    boolean diventaEsecutiva;
    So4AmministrazioneDTO ente;
    boolean esecutivitaMovimenti;
    boolean queryMovimenti;
    boolean funzionarioObbligatorio;
    Integer giorniPubblicazione;
    boolean giorniPubblicazioneModificabile;
    boolean pubblicazioneFutura;
    Date lastUpdated;
    boolean manuale;
    Set<GestioneTestiModelloDTO> modelliTesto;
    GestioneTestiModelloDTO modelloTesto;
    GestioneTestiModelloDTO modelloTestoAnnullamento;
    GestioneTestiModelloDTO modelloTestoFrontespizio;
    boolean movimentiContabili;
    boolean notificaOrganiControllo;
    Set<ParametroTipologiaDTO> parametri;
    Long progressivoCfgIter;
    Long progressivoCfgIterPubblicazione;
    boolean pubblicaAllegati;
    boolean pubblicazione;
    boolean pubblicazioneFinoARevoca;
    boolean pubblicazioneTrasparenza;
    boolean pubblicaAllegatiDefault;
    boolean pubblicaVisualizzatore;
    boolean registroUnita;
    boolean scritturaMovimentiContabili;
    boolean secondaPubblicazione;
    boolean testoObbligatorio;
    Set<TipoVistoParereDTO> tipiVisto;
    TipoCertificatoDTO tipoCertAvvPubb;
    TipoCertificatoDTO tipoCertAvvPubb2;
    TipoCertificatoDTO tipoCertEsec;
    TipoCertificatoDTO tipoCertPubb;
    TipoCertificatoDTO tipoCertPubb2;
    TipoCertificatoDTO tipoCertImmEseg;
    String tipoPubblicazioneAlbo;
    TipoRegistroDTO tipoRegistro;
    String titolo;
    String titoloNotifica;
    String descrizioneNotifica;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;
    boolean vistiPareri;
    TipoRegistroDTO tipoRegistro2;
    Set<OggettoRicorrenteDTO> oggettiRicorrenti;
    boolean incaricatoObbligatorio;
    String ruoloRiservato;
    boolean eseguibilitaImmediata;
    boolean pubblicaAllegatiVisualizzatore;

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
        parametroTipologia.tipoDetermina = this
    }

    public void removeFromParametri (ParametroTipologiaDTO parametroTipologia) {
        if (this.parametri == null)
            this.parametri = new HashSet<ParametroTipologiaDTO>()
        this.parametri.remove (parametroTipologia);
        parametroTipologia.tipoDetermina = null
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

    public TipoDetermina getDomainObject () {
        return TipoDetermina.get(this.id)
    }

    public TipoDetermina copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
