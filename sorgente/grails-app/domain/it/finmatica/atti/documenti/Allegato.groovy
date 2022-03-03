package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.so4.struttura.So4Amministrazione

class Allegato implements IDocumentoEsterno, IDocumentoCollegato {

    public static final transient String TIPO_OGGETTO = "ALLEGATO"

    public static final transient String ALLEGATO_GENERICO                 = "ALLEGATO_GENERICO"
    public static final transient String ALLEGATO_SCHEDA_CONTABILE         = "SCHEDA_CONTABILE"
    public static final transient String ALLEGATO_SCHEDA_CONTABILE_ENTRATA = "SCHEDA_CONTABILE_ENTRATA"
    public static final transient String ALLEGATO_FRONTESPIZIO             = "FRONTESPIZIO"
    public static final transient String ALLEGATO_OMISSIS                  = "OMISSIS"
    public static final transient String ALLEGATO_MODIFICABILE             = "ALLEGATO_MODIFICABILE"
    public static final transient String ALLEGATO_RIASSUNTIVO_FIRME        = "ALLEGATO_RIASSUNTIVO_FIRME"
    public static final transient String ALLEGATO_TESTO_PROPOSTA           = "ALLEGATO_TESTO_PROPOSTA"
    public static final transient String ALLEGATO_COPIA_TESTO              = "ALLEGATO_COPIA_TESTO"

    Long idDocumentoEsterno    // indica l'id del documento sul documentale esterno (ad es. GDM)

    String titolo
    String descrizione

    TipoAllegato tipoAllegato

    StatoFirma statoFirma

    String ubicazione
    String origine

    int     quantita  = 1
    Integer numPagine = 1
    int     sequenza  = 1        // sequenza in stampa unica.

    boolean riservato           = false
    boolean stampaUnica         = false    // indica se va in stampa unica. Se è riservato, allora NON può andare in stampa unica.
    boolean pubblicaCasaDiVetro = true        // indica se va pubblicato alla casa di vetro. Se è riservato, allora NON può andare in casa di vetro
    boolean pubblicaAlbo        = true        // indica se va pubblicato all'albo. Se è riservato, allora NON può andare all'albo.
    boolean pubblicaVisualizzatore = true     // indica se va pubblicato nel visualizzatore.

    // indica se il documento è valido o no, cioè se è stato "cancellato" oppure no
    boolean valido = true

    // Se valorizzato, indica che l'allegato è generato automaticamente e quindi non eliminabile. Può assumere i valori:
    // SCHEDA_CONTABILE per la gestione Contabilità finanziaria, altrimenti nullo.
    // FRONTESPIZIO		per la gestione del frontespizio della determina.
    // OMISSIS			per la gestione del testo dell'atto con gli omissis.
    String codice

    So4Amministrazione ente
    Date               dateCreated
    Ad4Utente          utenteIns
    Date               lastUpdated
    Ad4Utente          utenteUpd

    static belongsTo = [determina: Determina, propostaDelibera: PropostaDelibera, delibera: Delibera, vistoParere: VistoParere]
    static hasMany   = [fileAllegati: FileAllegato]

    static mapping = {
        table 'allegati'
        id column: 'id_allegato'

        fileAllegati joinTable: [name: "allegati_file", key: "id_allegato", column: "id_file"]

        determina column: 'id_determina', index: 'all_det_fk'
        delibera column: 'id_delibera', index: 'all_del_fk'
        propostaDelibera column: 'id_proposta_delibera', index: 'all_prodel_fk'
        vistoParere column: 'id_visto_parere', index: 'all_vispar_fk'

        tipoAllegato column: 'id_tipo_allegato'
        stampaUnica type: 'yes_no'
        riservato type: 'yes_no'

        valido type: 'yes_no'
        ente column: 'ente'
        dateCreated column: 'data_ins'
        utenteIns column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd column: 'utente_upd'

        pubblicaCasaDiVetro type: 'yes_no'
        pubblicaAlbo type: 'yes_no'
        pubblicaVisualizzatore type: 'yes_no'
    }

    static constraints = {
        delibera nullable: true
        determina nullable: true
        propostaDelibera nullable: true
        vistoParere nullable: true
        idDocumentoEsterno nullable: true
        tipoAllegato nullable: true
        descrizione nullable: true
        ubicazione nullable: true
        origine nullable: true
        quantita nullable: true
        numPagine nullable: true
        statoFirma nullable: true
        codice nullable: true
    }

    private SpringSecurityService getSpringSecurityService () {
        return Holders.applicationContext.getBean("springSecurityService")
    }

    def beforeValidate () {
        utenteIns = utenteIns ?: springSecurityService.currentUser
        ente = ente ?: springSecurityService.principal.amministrazione
        utenteUpd = utenteUpd ?: springSecurityService.currentUser

        // mi assicuro che non possa essere in stampa unica se riservato:
        if (riservato) {
            stampaUnica = false;
        }
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

    static transients = ['documentoPrincipale', 'testo']

    static namedQueries = {
        numeroFilePerNome { long idAllegato, String nomeFile ->
            projections {
                count("id")
            }

            eq("id", idAllegato)

            fileAllegati {
                eq("nome", nomeFile)
            }
        }
    }

    transient IDocumento getDocumentoPrincipale () {
        return (((delibera) ?: propostaDelibera) ?: determina) ?: vistoParere
    }

    transient void setDocumentoPrincipale (IDocumento documentoPrincipale) {
        if (documentoPrincipale instanceof Determina) {
            this.determina = documentoPrincipale;
        } else if (documentoPrincipale instanceof Delibera) {
            this.delibera = documentoPrincipale;
        } else if (documentoPrincipale instanceof PropostaDelibera) {
            this.propostaDelibera = documentoPrincipale;
        } else if (documentoPrincipale instanceof VistoParere) {
            this.vistoParere = documentoPrincipale;
        }
    }

    FileAllegato getTesto () {
        if (fileAllegati?.size() > 0) {
            return fileAllegati.first()
        }
        return null
    }

    void setTesto (FileAllegato fileAllegato) {
        if (fileAllegato == null) {
            if (fileAllegati?.size() > 0) {
                removeFromFileAllegati(fileAllegato)
            }
        } else if (fileAllegati == null || fileAllegati.size() == 0) {
            addToFileAllegati(fileAllegato)
        } else {
            // posso avere solo un "testo", quindi rimuovo quello vecchio e imposto quello nuovo
            removeFromFileAllegati(getTesto())
            addToFileAllegati(fileAllegato)
        }
    }

    transient String getNomeFile () {
        return ((tipoAllegato.titolo) ?: codice) ?: TIPO_OGGETTO
    }
}
