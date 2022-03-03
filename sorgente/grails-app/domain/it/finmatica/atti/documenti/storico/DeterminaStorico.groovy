package it.finmatica.atti.documenti.storico

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.IDocumentoStorico
import it.finmatica.atti.IDocumentoStoricoEsterno
import it.finmatica.atti.commons.FileAllegatoStorico
import it.finmatica.atti.dizionari.Categoria
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione

class DeterminaStorico implements IDocumentoStoricoEsterno, IDocumentoStorico {
    // viene usato con accesso dinamico dal AttiFileDownloader
    public static final String TIPO_OGGETTO = Determina.TIPO_OGGETTO

    /**
     * Contiene un xml con i soggetti della determina al momento della storicizzazione:
     * <soggetti>
     * 	<soggetto tipo="UNITA/UTENTE" cognomeNome="COGNOME_NOME" descrizione="DESCR UNITA" utente="CODICE_UTENTE_AD4" progrUo="PROGRESSIVO_UO" ottica="CODICE_OTTICA" dal="DD/MM/YYYY" />
     *  ..
     * </soggetti>
     */
    String xmlSoggetti

    /**
     * Contiene un xml con le determine collegate:
     * <determine>
     * 	<determina id="" operazione="INTEGRAZIONE/ANNULLAMENTO">
     * 		<oggetto>OGGETTO_DETERMINA</oggetto>
     * 		<numeroProposta  numero="NUMERO" anno="ANNO" tipoRegistro="TIPO_REG" data="DD/MM/YYYY" />
     * 		<numeroDetermina numero="NUMERO" anno="ANNO" tipoRegistro="TIPO_REG" data="DD/MM/YYYY" />
     * 	</determina>
     *  ..
     * </determine>
     */
    String xmlDetermineCollegate

    /**
     * Contiene un xml con i dati aggiuntivi della determina:
     * <datiAggiuntivi>
     *     <dato id="" codice="" valore="" idTipoValore="" tipoValore=""/>
     * </datiAggiuntivi>
     */
    String xmlDatiAggiuntivi

    long idDetermina    // riferimento alla determina "originale"
    long revisione        // indice di revisione della determina storico
    WkfStep step        // lo step in cui è stata storicizzata

    WkfIter iter
    TipoDetermina tipologia

    // stati del documento
    StatoDocumento stato
    StatoFirma statoFirma
    StatoConservazione statoConservazione
    StatoOdg statoOdg        // stato della delibera per la sua gestione in odg

    // testi
    FileAllegatoStorico testo
    FileAllegatoStorico stampaUnica

    // modelli testo
    GestioneTestiModello modelloTesto

    // dati per odg
    Categoria categoria
    Commissione commissione        // commissione che dovrà discutere la determina
    OggettoSeduta oggettoSeduta

    // dati proposta
    String oggetto

    // data modificabile dall'utente sulla base della quale si sceglie l'anno del registro su cui numerare la determina (FIXME: o della proposta? o entrambe? o boh?)
    Date dataProposta

    Date dataNumeroProposta
    Integer numeroProposta
    Integer annoProposta
    TipoRegistro registroProposta
    Date dataNumeroDetermina
    Integer numeroDetermina
    Integer annoDetermina
    TipoRegistro registroDetermina
    Date dataNumeroProtocollo
    Integer numeroProtocollo
    Integer annoProtocollo
    TipoRegistro registroProtocollo

    boolean controlloFunzionario = false
    boolean riservato = false

    // note
    String note
    String noteTrasmissione
    String noteContabili

    // dati di protocollo
    String classificaCodice
    Date classificaDal
    String classificaDescrizione
    Integer fascicoloAnno
    String fascicoloNumero
    String fascicoloOggetto

    // dati di pubblicazione
    boolean pubblicaRevoca = false // resta in pubblicazione fino a revoca
    Integer giorniPubblicazione
    Date dataPubblicazione
    Date dataFinePubblicazione
    Date dataPubblicazione2
    Date dataFinePubblicazione2
    Date dataEsecutivita

    // campo che contiene la lista dei campi non modificabili
    String campiProtetti

    // indica i codici dei visti che ho già trattato (serve per gestire eventuali ciclicità sui visti nel flusso)
    String codiciVistiTrattati

    // indica il codice CIG
    String codiceGara

    // indica l'id del documento sul documentale esterno (ad es. GDM)
    Long idDocumentoEsterno
    Long versioneDocumentoEsterno

    // indica se il documento è valido o no, cioè se è stato "cancellato" oppure no
    boolean valido = true

    So4Amministrazione ente
    Date dateCreated
    Ad4Utente utenteIns
    Date lastUpdated
    Ad4Utente utenteUpd

    Date dataScadenza
    String motivazione
    Integer priorita

    static mapping = {
        table 'determine_storico'
        id column: 'id_determina_storico'
        idDetermina column: 'id_determina', index: 'detsto_det_fk'
        tipologia column: 'id_tipo_determina'
        iter column: 'id_engine_iter'
        step column: 'id_engine_step'
        testo column: 'id_file_allegato_testo'
        stampaUnica column: 'id_file_allegato_stampa_unica'
        categoria column: 'id_categoria'
        commissione column: 'id_commissione'
        oggettoSeduta column: 'id_oggetto_seduta'
        registroProposta column: 'registro_proposta'
        registroProtocollo column: 'registro_protocollo'
        registroDetermina column: 'registro_determina'
        codiceGara column: 'cig'

        controlloFunzionario type: 'yes_no'
        riservato type: 'yes_no'
        pubblicaRevoca type: 'yes_no'

        oggetto length: 4000
        note length: 4000
        noteTrasmissione length: 4000
        noteContabili length: 4000
        fascicoloOggetto length: 4000
        campiProtetti length: 4000
        classificaDescrizione length: 4000
        xmlSoggetti sqlType: 'clob'
        xmlDetermineCollegate sqlType: 'clob'
        xmlDatiAggiuntivi sqlType: 'clob'

        dataPubblicazione2 column: 'data_pubblicazione_2'
        dataFinePubblicazione2 column: 'data_fine_pubblicazione_2'

        modelloTesto column: 'id_modello_testo'

        valido type: 'yes_no'
        ente column: 'ente'
        dateCreated column: 'data_ins'
        utenteIns column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd column: 'utente_upd'

        dataScadenza column: 'data_scadenza'
        motivazione column: 'motivazione'
    }

    static constraints = {
        iter nullable: true
        step nullable: true
        tipologia nullable: true
        testo nullable: true
        stampaUnica nullable: true
        categoria nullable: true
        xmlDatiAggiuntivi nullable: true

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
        dataNumeroProtocollo nullable: true
        numeroProtocollo nullable: true
        annoProtocollo nullable: true
        registroProtocollo nullable: true

        note nullable: true
        noteTrasmissione nullable: true
        noteContabili nullable: true

        classificaCodice nullable: true
        classificaDal nullable: true
        classificaDescrizione nullable: true
        fascicoloAnno nullable: true
        fascicoloNumero nullable: true
        fascicoloOggetto nullable: true

        giorniPubblicazione nullable: true
        dataPubblicazione nullable: true
        dataFinePubblicazione nullable: true
        dataPubblicazione2 nullable: true
        dataFinePubblicazione2 nullable: true
        dataEsecutivita nullable: true

        codiciVistiTrattati nullable: true
        codiceGara nullable: true
        campiProtetti nullable: true
        idDocumentoEsterno nullable: true
        versioneDocumentoEsterno nullable: true
        stato nullable: true
        statoFirma nullable: true
        statoConservazione nullable: true
        statoOdg nullable: true
        modelloTesto nullable: true

        dataScadenza nullable: true
        motivazione nullable: true
        priorita nullable: true
    }

    private SpringSecurityService getSpringSecurityService () {
        return Holders.applicationContext.getBean("springSecurityService")
    }

    def beforeValidate () {
        utenteIns = utenteIns ?: springSecurityService.currentUser
        utenteUpd = utenteUpd ?: springSecurityService.currentUser
        ente = ente ?: springSecurityService.principal.amministrazione
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
        multiEnteFilter(condition: "ente = :enteCorrente", types: 'string')
    }

    @Override
    transient Object getDocumentoOriginale () {
        return Determina.get(idDetermina)
    }
}
