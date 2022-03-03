package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.Categoria
import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class Determina implements IAtto, IProposta {

    public static final transient String TIPO_OGGETTO = "DETERMINA"
    public static final transient String SEPARATORE = "#"    // separatore per i codici dei visti

    // indica l'id del documento sul sistema Docer per il Provider di Registrazione
    transient Long idDocumentoDocer

    WkfIter iter
    TipoDetermina tipologia

    // stati del documento
    StatoDocumento stato
    StatoFirma statoFirma
    StatoConservazione statoConservazione
    StatoOdg statoOdg        // stato della delibera per la sua gestione in odg
    StatoMarcatura statoMarcatura

    // testi
    FileAllegato testo
    FileAllegato testoOdt
    FileAllegato stampaUnica

    // modelli testo
    GestioneTestiModello modelloTesto

    // dati per odg
    Categoria categoria
    Commissione commissione        // commissione che dovrà discutere la determina
    OggettoSeduta oggettoSeduta

    // dati proposta
    String oggetto

    // data modificabile dall'utente sulla base della
    // quale si sceglie l'anno del registro su cui numerare la determina (FIXME: o della proposta? o entrambe? o boh?)
    Date dataProposta

    Date dataNumeroProposta
    Integer numeroProposta
    Integer annoProposta
    TipoRegistro registroProposta

    // dati determina
    Date dataNumeroDetermina
    Integer numeroDetermina
    Integer annoDetermina
    TipoRegistro registroDetermina

    // dati per il secondo numero della determina: http://svi-redmine/issues/22205
    Date dataNumeroDetermina2
    Integer numeroDetermina2
    Integer annoDetermina2
    TipoRegistro registroDetermina2

    boolean controlloFunzionario = false
    boolean riservato = false
    boolean eseguibilitaImmediata = false

    // gestione corte dei conti
    boolean daInviareCorteConti
    Date dataInvioCorteConti

    // note
    String note
    String noteTrasmissione
    String noteContabili
    String motivazioniEseguibilita

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

    // Dati dell'albo
    Long idDocumentoAlbo
    Integer numeroAlbo
    Integer annoAlbo

	// dati di pubblicazione
	boolean pubblicaRevoca 				= false // resta in pubblicazione fino a revoca
	Integer	giorniPubblicazione
	Date 	dataPubblicazione
	Date 	dataFinePubblicazione
	Date 	dataPubblicazione2
	Date 	dataFinePubblicazione2
	Date	dataEsecutivita
	Date 	dataMinimaPubblicazione
	boolean daPubblicare				= false
    boolean pubblicaVisualizzatore      = false
	Date 	dataScadenza
	String  motivazione
	Integer priorita
    Date    dataOrdinamento

    // campo che contiene la lista dei campi non modificabili
    String campiProtetti

    // indica i codici dei visti che ho già trattato (serve per gestire eventuali ciclicità sui visti nel flusso)
    String codiciVistiTrattati

    // indica il codice CIG
    String codiceGara

    // indica l'id del documento sul documentale esterno (ad es. GDM)
    Long idDocumentoEsterno

    // indica se il documento è valido o no, cioè se è stato "cancellato" oppure no
    boolean valido = true

    So4Amministrazione ente
    Date dateCreated
    Ad4Utente utenteIns
    Date lastUpdated
    Ad4Utente utenteUpd

    // indica se l'atto deve diventare esecutivo oppure no
    boolean diventaEsecutiva

    // campi relativi all'invio dei dati al Tesoriere
    boolean inviatoTesoriere = false
    String noteTesoriere
    Integer numeroProtTesoriere
    Date dataProtTesoriere
    boolean controllaDestinatari

	OggettoRicorrente oggettoRicorrente

	static transients = ['idDocumentoDocer']

    Set<DeterminaSoggetto> soggetti
    static hasMany = [soggetti              : DeterminaSoggetto
                      , visti               : VistoParere
                      , allegati            : Allegato
                      , certificati         : Certificato
                      , destinatariNotifiche: DestinatarioNotifica
                      , documentiCollegati  : DocumentoCollegato
                      , firmatari           : Firmatario
                      , datiAggiuntivi      : DatoAggiuntivo
                      , budgets             : Budget]

    static mappedBy = [documentiCollegati: 'determinaPrincipale']

	static mapping = {
		table 				'determine'
		id 					column: 'id_determina'
		tipologia			column: 'id_tipo_determina', 			index: 'det_tipdet_fk'
		iter				column: 'id_engine_iter', 				index: 'det_wkfengite_fk'
		testo				column: 'id_file_allegato_testo'
		testoOdt			column: 'id_file_allegato_testo_odt'
		stampaUnica			column: 'id_file_allegato_stampa_unica'
		categoria			column: 'id_categoria'
		commissione 		column: 'id_commissione'
		oggettoSeduta		column: 'id_oggetto_seduta'
		oggettoRicorrente	column:	'id_oggetto_ricorrente'
		registroProposta    column: 'registro_proposta'
		registroProtocollo  column: 'registro_protocollo'
		registroDetermina   column: 'registro_determina'
		registroDetermina2  column: 'registro_determina_2'
		annoDetermina2		column: 'anno_determina_2'
		numeroDetermina2	column: 'numero_determina_2'
		dataNumeroDetermina2 column: 'data_numero_determina_2'
		codiceGara          column: 'cig'

        controlloFunzionario type: 'yes_no'
        riservato type: 'yes_no'
        eseguibilitaImmediata type: 'yes_no'
        daInviareCorteConti type: 'yes_no'
        diventaEsecutiva type: 'yes_no'
        inviatoTesoriere type: 'yes_no'
        controllaDestinatari type: 'yes_no'

        oggetto length: 4000
        note length: 4000
        noteTrasmissione length: 4000
        noteContabili length: 4000
        fascicoloOggetto length: 4000
        campiProtetti length: 4000
        noteTesoriere length: 4000
        classificaDescrizione length: 4000

        dataPubblicazione2 column: 'data_pubblicazione_2'
        dataFinePubblicazione2 column: 'data_fine_pubblicazione_2'
		dataMinimaPubblicazione	column: 'data_min_pubblicazione'
		daPubblicare			column: 'da_pubblicare', type: 'yes_no'
        pubblicaRevoca type: 'yes_no'
        pubblicaVisualizzatore type: 'yes_no'

		dataScadenza 		    column: 'data_scadenza'
		motivazione			    column: 'motivazione'
		
		modelloTesto	        column: 'id_modello_testo'

		valido					type: 	'yes_no'
        ente 					column: 'ente'
		dateCreated 			column: 'data_ins'
		utenteIns 				column: 'utente_ins'
		lastUpdated 			column: 'data_upd'
		utenteUpd 				column: 'utente_upd'
        motivazioniEseguibilita column: 'motivazioni_eseguibilita'
	}

    static constraints = {

        iter nullable: true
        tipologia nullable: true
        testo nullable: true
        testoOdt nullable: true
        stampaUnica nullable: true
        categoria nullable: true

        commissione nullable: true
        oggettoSeduta nullable: true

        dataNumeroProposta nullable: true
        numeroProposta nullable: true
        annoProposta nullable: true
        registroProposta nullable: true

        dataNumeroDetermina nullable: true
        numeroDetermina nullable: true
        annoDetermina nullable: true
        registroDetermina nullable: true

        dataNumeroDetermina2 nullable: true
        numeroDetermina2 nullable: true
        annoDetermina2 nullable: true
        registroDetermina2 nullable: true

        dataNumeroProtocollo nullable: true
        numeroProtocollo nullable: true
        annoProtocollo nullable: true
        registroProtocollo nullable: true

        dataInvioCorteConti nullable: true

        note nullable: true
        noteTrasmissione nullable: true
        noteContabili nullable: true

        classificaCodice nullable: true
        classificaDal nullable: true
        classificaDescrizione nullable: true
        fascicoloAnno nullable: true
        fascicoloNumero nullable: true
        fascicoloOggetto nullable: true

        idDocumentoAlbo nullable: true
        numeroAlbo nullable: true
        annoAlbo nullable: true

		giorniPubblicazione     nullable: true
		dataPubblicazione       nullable: true
		dataFinePubblicazione   nullable: true
		dataPubblicazione2      nullable: true
		dataFinePubblicazione2  nullable: true
		dataEsecutivita			nullable: true
		dataScadenza  			nullable: true
		dataMinimaPubblicazione nullable: true
		motivazione				nullable: true
		priorita				nullable: true
        dataOrdinamento         nullable: true

        codiciVistiTrattati nullable: true
        codiceGara nullable: true
        campiProtetti nullable: true
        idDocumentoEsterno nullable: true
        stato nullable: true
        statoFirma nullable: true
        statoConservazione nullable: true
        statoOdg nullable: true
        statoMarcatura nullable: true
        modelloTesto nullable: true

		numeroProtTesoriere		nullable: true
		dataProtTesoriere		nullable: true
		noteTesoriere			nullable: true

		oggettoRicorrente		nullable: true
        motivazioniEseguibilita nullable: true
    }

	long getIdDocumento () {
		return id?:-1
	}

    transient String getTipoOggetto () {
        return Determina.TIPO_OGGETTO
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
        setSoggetto(tipoSoggetto, utenteAd4, unitaSo4, 0)
    }

    /**
     * Setta il soggetto con del tipo richiesto con l'utente e/o l'unità so4
     *
     * Se utenteAd4 e unitaSo4 sono nulli, allora significa che voglio "eliminare" quel soggetto. Quindi se trovato, il soggetto viene eliminato.
     *
     * @param tipoSoggetto il tipo soggetto da settare
     * @param utenteAd4 l'utente del soggetto
     * @param unitaSo4 l'unità del soggetto
     */
    transient void setSoggetto (String tipoSoggetto, Ad4Utente utenteAd4, So4UnitaPubb unitaSo4, int sequenza) {
        DeterminaSoggetto determinaSoggetto = this.getSoggetto(tipoSoggetto)

        if (determinaSoggetto == null) {
            // http://svi-redmine/issues/14559
            // se non ho trovato il soggetto e i valori sono pure null, esco
            if (utenteAd4 == null && unitaSo4 == null) {
                return
            }

            determinaSoggetto = new DeterminaSoggetto()
            determinaSoggetto.tipoSoggetto = TipoSoggetto.get(tipoSoggetto)
            determinaSoggetto.sequenza = sequenza

            // quando aggiungo un nuovo soggetto, lo imposto subito come "attivo".
            determinaSoggetto.attivo = true
            addToSoggetti(determinaSoggetto)
        }

        // http://svi-redmine/issues/14559
        // se ho trovato il soggetto ma ne voglio "svuotare" i campi, allora lo elimino:
        if (determinaSoggetto != null &&
                utenteAd4 == null && unitaSo4 == null) {
            removeFromSoggetti(determinaSoggetto)
            determinaSoggetto.delete()
            // TODO: qui vanno gestiti i soggetti "multipli": attivo/non-attivo, sequenza.
            return
        }

        // infine, aggiorno i valori del soggetto.
        determinaSoggetto.utenteAd4 = utenteAd4
        determinaSoggetto.unitaSo4 = unitaSo4
    }

    /**
     * Ritorna il soggetto della determina
     *
     * @param tipoSoggetto il codice del soggetto da ritornare
     * @return il soggetto trovato, null altrimenti.
     */
    transient DeterminaSoggetto getSoggetto (String tipoSoggetto) {
        for (DeterminaSoggetto s in soggetti) {
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
        certificati*.beforeValidate()
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

    transient ITipologia getTipologiaDocumento () {
        return this.tipologia
    }

    transient String getEstremiAtto () {
        if (numeroDetermina > 0) {
            return "${numeroDetermina} / ${annoDetermina} (${registroDetermina.descrizione})"
        }

        if (numeroProposta > 0) {
            return "${numeroProposta} / ${annoProposta} (${registroProposta.descrizione})"
        }

        return "Proposta non numerata con oggetto: $oggetto."
    }

    transient String getNomeFileTestoPdf () {
        return getNomeFile() + ".pdf"
    }

    transient String getNomeFile () {
        if (numeroDetermina > 0) {
            return "DET_${registroDetermina.codice}_${numeroDetermina}_${annoDetermina}"
        } else if (numeroProposta > 0) {
            return "PR_DET_${registroProposta.codice}_${numeroProposta}_${annoProposta}"
        } else {
            return "PR_DET_${id}"
        }
    }

    // Getters per visualizzatore

    transient Integer getAnno () {
        return annoDetermina
    }

    transient Integer getNumero () {
        return numeroDetermina
    }

    transient TipoRegistro getRegistro () {
        return registroDetermina
    }

    transient boolean isAttoRiservato () {
        return riservato
    }

    transient Date getDataNumero () {
        return dataNumeroDetermina
    }

    transient So4UnitaPubb getUnitaProponente () {
        return getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4
    }

    // Metodi di interfaccia:
    transient IAtto getAtto () {
        return this
    }

    transient IProposta getProposta () {
        return this
    }

    transient Integer getAnnoAtto () {
        return this.annoDetermina
    }

    transient Integer getNumeroAtto () {
        return this.numeroDetermina
    }

    // utilizzata nel Visualizzatore
    transient Date getDataAtto () {
        return this.dataNumeroDetermina
    }

    transient TipoRegistro getRegistroAtto () {
        return this.registroDetermina
    }

    @Override
    ITipologiaPubblicazione getTipologiaPubblicazione () {
        return this.tipologia
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
