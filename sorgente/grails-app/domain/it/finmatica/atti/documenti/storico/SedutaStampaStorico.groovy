package it.finmatica.atti.documenti.storico

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.IDocumentoStorico
import it.finmatica.atti.IDocumentoStoricoEsterno
import it.finmatica.atti.commons.FileAllegatoStorico
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.StatoConservazione
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.odg.CommissioneStampa
import it.finmatica.atti.odg.Seduta
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione

class SedutaStampaStorico implements IDocumentoStoricoEsterno, IDocumentoStorico {

    // viene usato con accesso dinamico dal AttiFileDownloader
    public static final String TIPO_OGGETTO = SedutaStampa.TIPO_OGGETTO

    /**
     * Contiene un xml con i soggetti della determina al momento della storicizzazione:
     * <soggetti>
     * 	<soggetto tipo="UNITA/UTENTE" cognomeNome="COGNOME_NOME" descrizione="DESCR UNITA" utente="CODICE_UTENTE_AD4" progrUo="PROGRESSIVO_UO" ottica="CODICE_OTTICA" dal="DD/MM/YYYY" />
     *  ..
     * </soggetti>
     */
    String xmlSoggetti

    long idSedutaStampa    // riferimento alla seduta stampa "originale"
    long revisione        // indice di revisione della determina storico

    // indica l'id del documento sul documentale esterno (ad es. GDM)
    Long idDocumentoEsterno
    Long versioneDocumentoEsterno

    // indica se il documento è valido o no, cioè se è stato "cancellato" oppure no
    boolean valido = true

    boolean riservato

    boolean pubblicaVisualizzatore
    String  note

    So4Amministrazione ente
    Date               dateCreated
    Ad4Utente          utenteIns
    Date               lastUpdated
    Ad4Utente          utenteUpd

    // lo step in cui è stata storicizzata
    WkfStep step

    WkfIter           iter
    CommissioneStampa commissioneStampa

    // stati del documento
    StatoDocumento     stato
    StatoFirma         statoFirma
    StatoConservazione statoConservazione

    // testi
    FileAllegatoStorico testo

    // modelli testo
    GestioneTestiModello modelloTesto

    Seduta seduta

    // Dati dell'albo
    Long    idDocumentoAlbo
    Integer numeroAlbo
    Integer annoAlbo

    // dati di protocollo
    String  classificaCodice
    Date    classificaDal
    String  classificaDescrizione
    Integer fascicoloAnno
    String  fascicoloNumero
    String  fascicoloOggetto

    // numero di protocollo
    Date         dataNumeroProtocollo
    Integer      numeroProtocollo
    Integer      annoProtocollo
    TipoRegistro registroProtocollo
    Long         idDocumentoLettera

    // dati di pubblicazione
    boolean pubblicaRevoca = false
    boolean daPubblicare   = false
    Integer giorniPubblicazione
    Date    dataPubblicazione
    Date    dataFinePubblicazione
    Date    dataPubblicazione2
    Date    dataFinePubblicazione2
    Date    dataMinimaPubblicazione

    static mapping = {
        table 'odg_sedute_stampe_storico'
        id column: 'id_seduta_stampa_storico'
        idSedutaStampa column: 'id_seduta_stampa'
        commissioneStampa column: 'id_commissione_stampa'
        iter column: 'id_engine_iter'
        step column: 'id_engine_step'
        testo column: 'id_file_allegato_testo'
        registroProtocollo column: 'registro_protocollo'
        seduta column: 'id_seduta'

        riservato type: 'yes_no'
        fascicoloOggetto length: 4000
        note length: 4000
        classificaDescrizione length: 4000
        xmlSoggetti sqlType: 'clob'

        modelloTesto column: 'id_modello_testo'

        valido type: 'yes_no'
        ente column: 'ente'
        dateCreated column: 'data_ins'
        utenteIns column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd column: 'utente_upd'

        // dati pubblicazione
        dataPubblicazione2 column: 'data_pubblicazione_2'
        dataFinePubblicazione2 column: 'data_fine_pubblicazione_2'
        dataMinimaPubblicazione column: 'data_min_pubblicazione'
        daPubblicare column: 'da_pubblicare', type: 'yes_no'
        pubblicaRevoca type: 'yes_no'
        pubblicaVisualizzatore type: 'yes_no'
    }

    static constraints = {
        note nullable: true
        iter nullable: true
        step nullable: true
        commissioneStampa nullable: true
        seduta nullable: true
        testo nullable: true

        dataNumeroProtocollo nullable: true
        numeroProtocollo nullable: true
        annoProtocollo nullable: true
        registroProtocollo nullable: true
        idDocumentoLettera nullable: true

        classificaCodice nullable: true
        classificaDal nullable: true
        classificaDescrizione nullable: true
        fascicoloAnno nullable: true
        fascicoloNumero nullable: true
        fascicoloOggetto nullable: true

        idDocumentoEsterno nullable: true
        versioneDocumentoEsterno nullable: true
        stato nullable: true
        statoFirma nullable: true
        statoConservazione nullable: true
        modelloTesto nullable: true

        // dati di pubblicazione
        giorniPubblicazione nullable: true
        dataPubblicazione nullable: true
        dataFinePubblicazione nullable: true
        dataPubblicazione2 nullable: true
        dataFinePubblicazione2 nullable: true
        dataMinimaPubblicazione nullable: true

        // Dati dell'albo
        idDocumentoAlbo nullable: true
        numeroAlbo nullable: true
        annoAlbo nullable: true
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
        return SedutaStampa.get(idSedutaStampa)
    }
}
