package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class Delibera implements IAtto {

    public static final transient String TIPO_OGGETTO = "DELIBERA"
    public static final transient String SEPARATORE = "#"    // separatore per i codici dei visti

    WkfIter iter
    PropostaDelibera propostaDelibera
    OggettoSeduta oggettoSeduta

    // stati del documento
    StatoDocumento stato
    StatoFirma statoFirma
    StatoConservazione statoConservazione
    StatoMarcatura statoMarcatura

    // testi
    FileAllegato testo
    FileAllegato testoOdt
    FileAllegato stampaUnica
    GestioneTestiModello modelloTesto

    // dati della delibera
    String oggetto

    Date dataNumeroDelibera
    Integer numeroDelibera
    Integer annoDelibera
    TipoRegistro registroDelibera

	// dati di protocollo
    String classificaCodice
    Date classificaDal
    String classificaDescrizione
    Integer fascicoloAnno
    String fascicoloNumero
    String fascicoloOggetto
    Date dataNumeroProtocollo
    Integer numeroProtocollo
    Integer annoProtocollo
    TipoRegistro registroProtocollo

    // esecutività
    Date dataEsecutivita
    Date dataAdozione

    // data che l'utente può inserire manualmente per indicare con quale data la delibera deve diventare esecutiva.
    // questo campo serve per dare la possibilità all'utente di scriverlo e salvare il documento senza rendere esecutiva la delibera
    // cosa che avverrebbe se scrivesse direttamente nel campo dataEsecutivita. Vedi attività: http://svi-redmine/issues/17326
    Date dataEsecutivitaManuale

    // indica l'id del documento sul documentale esterno (ad es. GDM)
    Long idDocumentoEsterno

    // Dati dell'albo
    Long idDocumentoAlbo
    Integer numeroAlbo
    Integer annoAlbo

    // gestione corte dei conti
    boolean daInviareCorteConti
    Date dataInvioCorteConti

	//Eseguibilità immediata
	boolean eseguibilitaImmediata = false
	String motivazioniEseguibilita

	// dati di pubblicazione
	boolean riservato				= false
	boolean pubblicaRevoca 			= false // resta in pubblicazione fino a revoca
	boolean daPubblicare			= false
	Integer	giorniPubblicazione
	Date 	dataPubblicazione
	Date 	dataFinePubblicazione
	Date 	dataPubblicazione2
	Date 	dataFinePubblicazione2
    boolean pubblicaVisualizzatore = false

    // indica i codici dei visti che ho già trattato (serve per gestire eventuali ciclicità sui visti nel flusso)
    String codiciVistiTrattati

    // campo che contiene la lista dei campi non modificabili
    String campiProtetti

    // note
    String note
    String noteTrasmissione

    // indica se il documento è valido o no, cioè se è stato "cancellato" oppure no
    boolean valido = true

    So4Amministrazione ente
    Date dateCreated
    Ad4Utente utenteIns
    Date lastUpdated
    Ad4Utente utenteUpd

    // indica se l'atto deve diventare esecutivo oppure no
    boolean diventaEsecutiva

    static belongsTo = [oggettoSeduta: OggettoSeduta]

    Set<DeliberaSoggetto> soggetti
    static hasMany = [allegati        : Allegato
                      , visti         : VistoParere // aggiunto per gestire i pareri della delibera per san donato milanese. #6987
                      , certificati   : Certificato
                      , soggetti      : DeliberaSoggetto
                      , firmatari     : Firmatario
                      , datiAggiuntivi: DatoAggiuntivo
                      , documentiCollegati  : DocumentoCollegato]

    static mappedBy = [documentiCollegati: 'deliberaPrincipale']

    static constraints = {

        // ESasdelli:
        // commentato perché a causa di un bug di grails falliscono gli unit-test.
        // questa costraint viene comunque verificata a livello di db dalla costraint oracle
        //propostaDelibera unique: true

		iter					nullable: true
		oggetto 				nullable: true
		oggettoSeduta			nullable: true
		registroDelibera		nullable: true
		annoDelibera			nullable: true
		numeroDelibera 	 		nullable: true
		dataNumeroDelibera		nullable: true

		classificaCodice        nullable: true
		classificaDal           nullable: true
		classificaDescrizione   nullable: true
		fascicoloAnno           nullable: true
		fascicoloNumero         nullable: true
		fascicoloOggetto        nullable: true
		numeroProtocollo 		nullable: true
		annoProtocollo			nullable: true
		dataNumeroProtocollo	nullable: true

		registroProtocollo		nullable: true
		idDocumentoAlbo         nullable: true
		numeroAlbo              nullable: true
		annoAlbo                nullable: true
		giorniPubblicazione		nullable: true
		dataEsecutivita			nullable: true
		dataAdozione			nullable: true
        dataEsecutivitaManuale  nullable: true
		giorniPubblicazione     nullable: true
		dataPubblicazione       nullable: true
		dataFinePubblicazione   nullable: true
		dataPubblicazione2      nullable: true
		dataFinePubblicazione2  nullable: true
		idDocumentoEsterno		nullable: true
		stato					nullable: true
		statoFirma				nullable: true
		statoConservazione		nullable: true
        statoMarcatura          nullable: true
		testo					nullable: true
		testoOdt				nullable: true
		stampaUnica				nullable: true
		modelloTesto			nullable: true
		note 					nullable: true
		noteTrasmissione 		nullable: true
		codiciVistiTrattati		nullable: true
		dataInvioCorteConti		nullable: true
		campiProtetti			nullable: true
		motivazioniEseguibilita nullable: true
    }

	static mapping = {
		table 		 	'delibere'
		id 	 					column: 'id_delibera'
		iter					column: 'id_engine_iter',			index: 'del_wkfengite_fk'
		propostaDelibera 		column: 'id_proposta_delibera',		index: 'del_tipdel_fk'
		oggettoSeduta 			column: 'id_oggetto_seduta',		index: 'del_odgoggsed_fk'
		testo					column: 'id_file_allegato_testo', 	index: 'del_filall_fk'
		testoOdt				column: 'id_file_allegato_testo_odt'
		stampaUnica				column: 'id_file_allegato_stampa_unica'
		modelloTesto			column: 'id_modello_testo'
		motivazioniEseguibilita column: 'motivazioni_eseguibilita'

		registroDelibera    	column: 'registro_delibera'
		registroProtocollo  	column: 'registro_protocollo'

        pubblicaRevoca type: 'yes_no'
        riservato type: 'yes_no'
        eseguibilitaImmediata type: 'yes_no'
        daInviareCorteConti type: 'yes_no'
        diventaEsecutiva type: 'yes_no'
        pubblicaVisualizzatore type: 'yes_no'

		dataPubblicazione2		column: 'data_pubblicazione_2'
		dataFinePubblicazione2	column: 'data_fine_pubblicazione_2'
		daPubblicare			column: 'da_pubblicare'

		campiProtetti			length: 4000
		oggetto					length: 4000
		note 					length: 4000
		noteTrasmissione 		length: 4000
        classificaDescrizione   length: 4000

		valido			type: 	'yes_no'
		ente 			column: 'ente'
		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'
		daPubblicare			type: 'yes_no'
	}

	long getIdDocumento () {
		return id?:-1
	}

    transient String getTipoOggetto () {
        return Delibera.TIPO_OGGETTO
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
        DeliberaSoggetto deliberaSoggetto = this.getSoggetto(tipoSoggetto);

        if (deliberaSoggetto == null) {
            // http://svi-redmine/issues/14559
            // se non ho trovato il soggetto e i valori sono pure null, esco
            if (utenteAd4 == null && unitaSo4 == null) {
                return;
            }

            deliberaSoggetto = new DeliberaSoggetto()
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

        deliberaSoggetto.utenteAd4 = utenteAd4
        deliberaSoggetto.unitaSo4 = unitaSo4
    }

    /**
     * Ritorna il soggetto della delibera
     *
     * @param tipoSoggetto il codice del soggetto da ritornare
     * @return il soggetto trovato, null altrimenti.
     */
    transient ISoggettoDocumento getSoggetto (String tipoSoggetto) {
        for (DeliberaSoggetto s in soggetti) {
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
        utenteUpd = springSecurityService.currentUser
        ente = ente ?: springSecurityService.principal.amministrazione

        documentiCollegati*.beforeValidate()
        allegati*.beforeValidate()
        certificati*.beforeValidate()
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

    /* metodi di interfaccia */
    static transients = ['proposta', 'dataMinimaPubblicazione']

    transient ITipologia getTipologiaDocumento () {
        return this.propostaDelibera.tipologia;
    }

    transient String getEstremiAtto () {
        if (numeroDelibera > 0) {
            return "${numeroDelibera} / ${annoDelibera} (${registroDelibera.descrizione})";
        }

        return "${propostaDelibera.numeroProposta} / ${propostaDelibera.annoProposta} (${propostaDelibera.registroProposta.descrizione})";
    }

    transient String getNomeFileTestoPdf () {
        return "${getNomeFile()}.pdf";
    }

    transient String getNomeFile () {
        if (numeroDelibera > 0) {
            return "DEL_${registroDelibera.codice}_${numeroDelibera}_${annoDelibera}";
        }
        return "DEL_${id}";
    }

    // Getters per visualizzatore

    transient Integer getNumero () {
        return numeroDelibera
    }

    transient TipoRegistro getRegistro () {
        return registroDelibera
    }

    // per analogia con le determine
    transient boolean isAttoRiservato () {
        return riservato;
    }

    transient Date getDataNumero () {
        return dataNumeroDelibera
    }

    transient So4UnitaPubb getUnitaProponente () {
        return proposta?.unitaProponente
    }

    // metodi di interfaccia

    transient Integer getAnnoAtto () {
        return this.annoDelibera;
    }

    transient Integer getNumeroAtto () {
        return this.numeroDelibera;
    }

    transient Date getDataAtto () {
        return this.dataAdozione;
    }

    transient TipoRegistro getRegistroAtto () {
        return this.registroDelibera;
    }

    transient IProposta getProposta () {
        return this.propostaDelibera;
    }

    transient void setProposta (IProposta proposta) {
        this.propostaDelibera = proposta;
    }

    @Override
    ITipologiaPubblicazione getTipologiaPubblicazione () {
        return this.propostaDelibera.tipologia
    }

    transient void setDataMinimaPubblicazione (Date date) {
        this.propostaDelibera.dataMinimaPubblicazione = date
    }

    transient Date getDataMinimaPubblicazione () {
        return this.propostaDelibera.dataMinimaPubblicazione
    }

    @Override
    IProtocollabile.Movimento getMovimento () {
        return IProtocollabile.Movimento.INTERNO
    }

    @Override
    List<DestinatarioNotifica> getDestinatari () {
        return []
    }
}
