package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.*
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class PropostaDelibera implements IProposta {

    public static final transient String TIPO_OGGETTO = "PROPOSTA_DELIBERA"
    public static final transient String SEPARATORE = "#"    // separatore per i codici dei visti

    WkfIter      iter
    TipoDelibera tipologia

    // stati del documento
    StatoDocumento stato
    StatoFirma     statoFirma
    StatoOdg       statoOdg        // stato della delibera per la sua gestione in odg
    StatoMarcatura statoMarcatura

    // dati odg
    Categoria     categoria
    Commissione   commissione            // commissione che dovrà discutere la delibera
    Delega        delega
    OggettoSeduta oggettoSeduta

    // testi
    FileAllegato testo
    FileAllegato testoOdt    // il testo in odt o doc che va incluso nella stampa della Delibera. Non è modificabile.
    FileAllegato stampaUnica

    // modelli testo
    GestioneTestiModello modelloTesto                // modello di stampa predefinito
    GestioneTestiModello modelloTestoAnnullamento
    // modello di stampa predefinito per l'annullamento TODO da gestire!

    // dati proposta
    String            oggetto
    IndirizzoDelibera indirizzo

    // data modificabile dall'utente sulla base della quale si sceglie l'anno del registro su cui numerare la delibera (FIXME: o della proposta? o entrambe? o boh?)
    Date dataProposta

    // Indica la data prevista della seduta - attività n. #22294
    Date dataScadenza

    Date         dataNumeroProposta
    Integer      numeroProposta
    Integer      annoProposta
    TipoRegistro registroProposta

    boolean controlloFunzionario = false
    boolean riservato            = false
    boolean fuoriSacco           = false
    boolean parereRevisoriConti  = false

    // Eseguibilità immediata
    boolean eseguibilitaImmediata = false
    String  motivazioniEseguibilita

    // note
    String note
    String noteTrasmissione
    String noteContabili
    String noteCommissione

	// dati di protocollo
    String classificaCodice
    Date classificaDal
    String classificaDescrizione
    Integer fascicoloAnno
    String  fascicoloNumero
    String  fascicoloOggetto

    // dati di pubblicazione
    boolean pubblicaRevoca = false // resta in pubblicazione fino a revoca
    Integer giorniPubblicazione
    Date    dataMinimaPubblicazione

    // gestione della corte dei conti:
    boolean daInviareCorteConti

    // campo che contiene la lista dei campi non modificabili
    String campiProtetti

    // indica i codici dei visti che ho già trattato (serve per gestire eventuali ciclicità sui visti nel flusso)
    String codiciVistiTrattati

    // indica l'id del documento sul documentale esterno (ad es. GDM)
    Long idDocumentoEsterno

    // indica se il documento è valido o no, cioè se è stato "cancellato" oppure no
    boolean valido = true

    So4Amministrazione ente
    Date               dateCreated
    Ad4Utente          utenteIns
    Date               lastUpdated
    Ad4Utente          utenteUpd
    String             motivazione
    Integer            priorita
    Date        dataOrdinamento
    boolean     controllaDestinatari

    OggettoRicorrente oggettoRicorrente

    Set<PropostaDeliberaSoggetto> soggetti
    static hasMany = [soggetti              : PropostaDeliberaSoggetto
                      , visti               : VistoParere
                      , allegati            : Allegato
                      , destinatariNotifiche: DestinatarioNotifica
                      , documentiCollegati  : DocumentoCollegato
                      , firmatari           : Firmatario
                      , datiAggiuntivi      : DatoAggiuntivo
                      , budgets             : Budget]

    static mapping = {
        table 'proposte_delibera'
        id column: 'id_proposta_delibera'
        tipologia column: 'id_tipo_delibera', index: 'prodel_tipdel_fk'
        iter column: 'id_engine_iter', index: 'prodel_wkfengite_fk'
        testo column: 'id_file_allegato_testo'
        testoOdt column: 'id_file_allegato_testo_odt'
        stampaUnica column: 'id_file_allegato_stampa_unica'
        categoria column: 'id_categoria'
        commissione column: 'id_commissione'
        oggettoSeduta column: 'id_oggetto_seduta'
        delega column: 'id_delega'
        indirizzo column: 'id_indirizzo_delibera'
        oggettoRicorrente column: 'id_oggetto_ricorrente'
        registroProposta column: 'registro_proposta'
        dataMinimaPubblicazione column: 'data_min_pubblicazione'
        motivazioniEseguibilita column: 'motivazioni_eseguibilita'
        dataScadenza column: 'data_scadenza'

        controlloFunzionario type: 'yes_no'
        riservato type: 'yes_no'
        pubblicaRevoca type: 'yes_no'
        fuoriSacco type: 'yes_no'
        eseguibilitaImmediata type: 'yes_no'
        daInviareCorteConti type: 'yes_no'
        parereRevisoriConti type: 'yes_no'
        controllaDestinatari type: 'yes_no'

        oggetto length: 4000
        note length: 4000
        noteTrasmissione length: 4000
        noteContabili length: 4000
        noteCommissione length: 4000
        campiProtetti length: 4000
        motivazione length: 4000
        classificaDescrizione length: 4000

        modelloTesto column: 'id_modello_testo'
        modelloTestoAnnullamento column: 'id_modello_testo_annullamento'

        valido type: 'yes_no'
        ente column: 'ente'
        dateCreated column: 'data_ins'
        utenteIns column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd column: 'utente_upd'
    }

    static constraints = {
        iter nullable: true
        tipologia nullable: true
        testo nullable: true
        testoOdt nullable: true
        stampaUnica nullable: true
        categoria nullable: true
        delega nullable: true
        indirizzo nullable: true

        commissione nullable: true
        oggettoSeduta nullable: true

        dataNumeroProposta nullable: true
        numeroProposta nullable: true
        annoProposta nullable: true
        registroProposta nullable: true

        dataScadenza nullable: true

        note nullable: true
        noteTrasmissione nullable: true
        noteContabili nullable: true
        noteCommissione nullable: true

        classificaCodice nullable: true
        classificaDal nullable: true
        classificaDescrizione nullable: true
        fascicoloAnno nullable: true
        fascicoloNumero nullable: true
        fascicoloOggetto nullable: true

        giorniPubblicazione nullable: true
        modelloTesto nullable: true
        modelloTestoAnnullamento nullable: true
        dataMinimaPubblicazione nullable: true

        codiciVistiTrattati nullable: true
        campiProtetti nullable: true
        idDocumentoEsterno nullable: true

        stato nullable: true
        statoFirma nullable: true
        statoOdg nullable: true
        statoMarcatura nullable: true

        dataNumeroProposta nullable: true
        numeroProposta nullable: true
        annoProposta nullable: true
        registroProposta nullable: true

        oggettoRicorrente nullable: true

        motivazioniEseguibilita nullable: true
        motivazione nullable: true
        priorita nullable: true
        dataOrdinamento         nullable: true
    }

    long getIdDocumento () {
        return id ?: -1
    }

    transient String getTipoOggetto () {
        return PropostaDelibera.TIPO_OGGETTO
    }

    /**
     * Aggiunge un CODICE_VISTO alla lista di quelli già trattati (concatena codiciVistiTrattati += #CODICE_VISTO)
     *
     * @param codice il codice della tipologia di visto trattata
     */
    void addCodiceVistoTrattato (String codice) {
        if (codiciVistiTrattati == null) {
            codiciVistiTrattati = ""
        }
        codiciVistiTrattati = (codiciVistiTrattati.tokenize(SEPARATORE) << codice).unique().join(SEPARATORE)
    }

    /**
     * Rimuove un CODICE_VISTO alla lista di quelli già trattati (rimuove da codiciVistiTrattati #CODICE_VISTO)
     *
     * @param codice il codice della tipologia di visto trattata
     */
    void removeCodiceVistoTrattato (String codice) {
        if (codiciVistiTrattati == null) {
            codiciVistiTrattati = ""
        }
        def v = codiciVistiTrattati.tokenize(SEPARATORE)
        v.remove(codice)
        codiciVistiTrattati = v.join(SEPARATORE)
    }

    /**
     * @return la lista dei codici visti trattati
     */
    String[] getListaCodiciVistiTrattati () {
        if (codiciVistiTrattati == null) {
            codiciVistiTrattati = ""
        }
        return codiciVistiTrattati.tokenize(SEPARATORE).toArray()
    }

    /**
     * Setta il soggetto con del tipo richiesto con l'utente e/o l'unità so4
     * @param tipoSoggetto il tipo soggetto da settare
     * @param utenteAd4 l'utente del soggetto
     * @param unitaSo4 l'unità del soggetto
     */
    transient void setSoggetto (String tipoSoggetto, Ad4Utente utenteAd4, So4UnitaPubb unitaSo4) {
        setSoggetto(tipoSoggetto, utenteAd4, unitaSo4, 0);
    }

    /**
     * Setta il soggetto con del tipo richiesto con l'utente e/o l'unità so4
     * @param tipoSoggetto il tipo soggetto da settare
     * @param utenteAd4 l'utente del soggetto
     * @param unitaSo4 l'unità del soggetto
     */
    transient void setSoggetto (String tipoSoggetto, Ad4Utente utenteAd4, So4UnitaPubb unitaSo4, int sequenza) {
        PropostaDeliberaSoggetto deliberaSoggetto = this.getSoggetto(tipoSoggetto);

        if (deliberaSoggetto == null) {
            // http://svi-redmine/issues/14559
            // se non ho trovato il soggetto e i valori sono pure null, esco
            if (utenteAd4 == null && unitaSo4 == null) {
                return;
            }

            deliberaSoggetto = new PropostaDeliberaSoggetto()
            deliberaSoggetto.tipoSoggetto = TipoSoggetto.get(tipoSoggetto)
            deliberaSoggetto.sequenza = sequenza;

            // quando aggiungo un nuovo soggetto, lo imposto subito come "attivo".
            deliberaSoggetto.attivo = true;
            addToSoggetti(deliberaSoggetto)
        }

        // http://svi-redmine/issues/14559
        // se ho trovato il soggetto ma ne voglio "svuotare" i campi, allora lo elimino:
        if (deliberaSoggetto != null &&
                utenteAd4 == null && unitaSo4 == null) {
            removeFromSoggetti(deliberaSoggetto)
            deliberaSoggetto.delete()
            // TODO: qui vanno gestiti i soggetti "multipli": attivo/non-attivo, sequenza.
            return;
        }

        deliberaSoggetto.utenteAd4 = utenteAd4;
        deliberaSoggetto.unitaSo4 = unitaSo4;
    }

    /**
     * Ritorna il soggetto attivo della delibera
     *
     * @param tipoSoggetto il codice del soggetto da ritornare
     * @return il soggetto trovato, null altrimenti.
     */
    transient PropostaDeliberaSoggetto getSoggetto (String tipoSoggetto) {
        for (PropostaDeliberaSoggetto s : soggetti) {
            if (s.tipoSoggetto.codice == tipoSoggetto && s.attivo) {
                return s
            }
        }
        return null
    }

    private SpringSecurityService getSpringSecurityService () {
        return Holders.applicationContext.getBean("springSecurityService")
    }

    def beforeValidate () {
        utenteIns = utenteIns ?: springSecurityService.currentUser
        utenteUpd = utenteUpd ?: springSecurityService.currentUser
        ente = ente ?: springSecurityService.principal.amministrazione

        visti*.beforeValidate()
        allegati*.beforeValidate()
        documentiCollegati*.beforeValidate()
        firmatari*.beforeValidate()
    }

    def beforeInsert () {
        utenteIns = springSecurityService.currentUser
        utenteUpd = springSecurityService.currentUser
        ente = springSecurityService.principal.amministrazione
    }

    def beforeUpdate () {
        utenteUpd = springSecurityService.currentUser
    }

    static hibernateFilters = {
        multiEnteFilter(condition: "ente = :enteCorrente and valido = 'Y'", types: 'string')
    }

    transient So4UnitaPubb getUnitaProponente () {
        return getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4
    }

    /* metodi per interfaccia */

    transient ITipologia getTipologiaDocumento () {
        return this.tipologia;
    }

    transient String getNomeFileTestoPdf () {
        return getNomeFile() + ".pdf";
    }

    transient String getNomeFile () {
        if (numeroProposta > 0) {
            return "PR_DEL_${registroProposta.codice}_${numeroProposta}_${annoProposta}";
        } else {
            return "PR_DEL_${id}";
        }
    }

    transient Delibera getAtto () {
        // metto che l'atto deve essere anche valido:
        // può succedere che la delibera collegata sia presente ma non valida, per lo più in caso di assistenze.
        return Delibera.findByPropostaDeliberaAndValido(this, true)
    }
}
