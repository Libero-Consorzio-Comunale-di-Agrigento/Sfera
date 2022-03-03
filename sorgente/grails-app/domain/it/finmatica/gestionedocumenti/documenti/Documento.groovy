package it.finmatica.gestionedocumenti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.Firmatario
import it.finmatica.atti.documenti.StatoConservazione
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.StatoMarcatura
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.gestionedocumenti.soggetti.DocumentoSoggetto
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class Documento implements IDocumentoIterabile {

    boolean riservato = false

    String  tipoOggetto
    WkfIter iter

    // stati del documento
    StatoDocumento     stato
    StatoFirma         statoFirma
    StatoConservazione statoConservazione
    StatoMarcatura     statoMarcatura

    // indica l'id del documento sul documentale esterno (ad es. GDM)
    Long idDocumentoEsterno

    List<FileDocumento> fileDocumenti

    Set<DocumentoSoggetto> soggetti
    static hasMany = [fileDocumenti     : FileDocumento,
                      soggetti          : DocumentoSoggetto,
                      documentiCollegati: GdoDocumentoCollegato,
                      firmatari: Firmatario]

    static mappedBy = [documentiCollegati: 'documento', soggetti: 'documento', firmatari: 'documento']

    So4Amministrazione ente
    boolean            valido = true
    Date               dateCreated
    Ad4Utente          utenteIns
    Date               lastUpdated
    Ad4Utente          utenteUpd

    static mapping = {
        table 'gdo_documenti'
        tablePerHierarchy(false)
        id column: 'id_documento'
        iter column: 'id_engine_iter'
        tipoOggetto column: 'tipo_oggetto'
        riservato type: 'yes_no'
        fileDocumenti indexColumn: [name: "sequenza", type: Integer]

        ente column: 'ente'
        dateCreated column: 'data_ins'
        utenteIns column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd column: 'utente_upd'
        valido type: 'yes_no'
    }

    static constraints = {
        iter nullable: true
        idDocumentoEsterno nullable: true
        stato nullable: true
        statoFirma nullable: true
        statoConservazione nullable: true
        statoMarcatura     nullable: true
        tipoOggetto nullable: true
    }

    SpringSecurityService getSpringSecurityService () {
        return Holders.getApplicationContext().getBean("springSecurityService")
    }

    def beforeValidate () {
        fileDocumenti*.beforeValidate()
        documentiCollegati*.beforeValidate()
        firmatari*.beforeValidate()

        utenteIns = utenteIns ?: getSpringSecurityService().currentUser
        utenteUpd = utenteUpd ?: getSpringSecurityService().currentUser
        ente = ente ?: springSecurityService.principal.amministrazione
    }

    def beforeInsert () {
        utenteIns = utenteIns ?: getSpringSecurityService().currentUser
        utenteUpd = utenteUpd ?: getSpringSecurityService().currentUser
        ente = ente ?: springSecurityService.principal.amministrazione
    }

    def beforeUpdate () {
        utenteUpd = utenteUpd ?: getSpringSecurityService().currentUser
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
        DocumentoSoggetto documentoSoggetto = this.getSoggetto(tipoSoggetto)

        if (documentoSoggetto == null) {
            // http://svi-redmine/issues/14559
            // se non ho trovato il soggetto e i valori sono pure null, esco
            if (utenteAd4 == null && unitaSo4 == null) {
                return
            }

            documentoSoggetto = new DocumentoSoggetto()
            documentoSoggetto.tipoSoggetto = TipoSoggetto.get(tipoSoggetto)
            documentoSoggetto.sequenza = sequenza

            // quando aggiungo un nuovo soggetto, lo imposto subito come "attivo".
            documentoSoggetto.attivo = true
            addToSoggetti(documentoSoggetto)
        }

        // http://svi-redmine/issues/14559
        // se ho trovato il soggetto ma ne voglio "svuotare" i campi, allora lo elimino:
        if (documentoSoggetto != null &&
                utenteAd4 == null && unitaSo4 == null) {
            removeFromSoggetti(documentoSoggetto)
            documentoSoggetto.delete()
            // TODO: qui vanno gestiti i soggetti "multipli": attivo/non-attivo, sequenza.
            return
        }

        // infine, aggiorno i valori del soggetto.
        documentoSoggetto.utenteAd4 = utenteAd4
        documentoSoggetto.unitaSo4 = unitaSo4
    }

    transient DocumentoSoggetto getSoggetto (String tipoSoggetto) {
        for (DocumentoSoggetto s in soggetti) {
            if (s.tipoSoggetto.codice == tipoSoggetto && s.attivo) {
                return s
            }
        }
        return null
    }

    /**
     * Metodi per la gestione dei documenti collegati
     */
    void addDocumentoCollegato (Documento collegato, String codiceCollegamento) {
        TipoCollegamento tipoCollegamento = TipoCollegamento.findByCodice(codiceCollegamento)
        if (tipoCollegamento == null) {
            throw new AttiRuntimeException(
                    "Non è stato possibile collegare il documento ${collegato} al documento ${this} perché il tipo di collegamento richiesto (codice: ${codiceCollegamento}) non esiste.")
        }

        addDocumentoCollegato(collegato, tipoCollegamento)
    }

    /**
     * Metodi per la gestione dei documenti allegati
     */
    void addDocumentoAllegato (Documento collegato) {
        addDocumentoCollegato(collegato, "ALLEGATO")
    }

    void addDocumentoCollegato (Documento collegato, TipoCollegamento tipoCollegamento) {
        // controllo se ho già un documento per questo tipo di collegamento
        GdoDocumentoCollegato documentoCollegato = getDocumentoCollegato(collegato, tipoCollegamento)
        if (documentoCollegato != null) {
            return
        }

        addToDocumentiCollegati(new GdoDocumentoCollegato(documento: this, collegato: collegato, tipoCollegamento: tipoCollegamento))
    }

    GdoDocumentoCollegato removeDocumentoCollegato (Documento collegato, String codiceCollegamento) {
        TipoCollegamento tipoCollegamento = TipoCollegamento.findByCodice(codiceCollegamento)
        if (tipoCollegamento == null) {
            throw new AttiRuntimeException(
                    "Non è stato possibile collegare il documento ${collegato} al documento ${this} perché il tipo di collegamento richiesto (codice: ${codiceCollegamento}) non esiste.")
        }
        return removeDocumentoCollegato(collegato, tipoCollegamento)
    }

    GdoDocumentoCollegato removeDocumentoCollegato (Documento collegato, TipoCollegamento tipoCollegamento) {
        GdoDocumentoCollegato documentoCollegato = getDocumentoCollegato(collegato, tipoCollegamento)
        if (documentoCollegato == null) {
            return documentoCollegato
        }

        removeFromDocumentiCollegati(documentoCollegato)
        documentoCollegato.delete()
        return documentoCollegato
    }

    List<Documento> getDocumentiCollegati (String codiceCollegamento) {
        TipoCollegamento tipoCollegamento = TipoCollegamento.findByCodice(codiceCollegamento)
        if (tipoCollegamento == null) {
            throw new AttiRuntimeException("Il tipo di collegamento richiesto (codice: ${codiceCollegamento}) non esiste.")
        }
        return getDocumentiCollegati(tipoCollegamento)
    }

    List<Documento> getDocumentiCollegati (TipoCollegamento tipoCollegamento) {
        return GdoDocumentoCollegato.findAllByDocumentoAndTipoCollegamento(this, tipoCollegamento).collegato
    }

    GdoDocumentoCollegato getDocumentoCollegato (Documento collegato, String codiceTipoCollegamento) {
        TipoCollegamento tipoCollegamento = TipoCollegamento.findByCodice(codiceTipoCollegamento)
        if (tipoCollegamento == null) {
            throw new AttiRuntimeException("Il tipo di collegamento richiesto (codice: ${codiceTipoCollegamento}) non esiste.")
        }
        return getDocumentoCollegato(collegato, tipoCollegamento)
    }

    GdoDocumentoCollegato getDocumentoCollegato (Documento collegato, TipoCollegamento tipoCollegamento) {
        return GdoDocumentoCollegato.findByDocumentoAndCollegatoAndTipoCollegamento(this, collegato, tipoCollegamento)
    }

    Documento getDocumentoCollegato (String codiceTipoCollegamento) {
        TipoCollegamento tipoCollegamento = TipoCollegamento.findByCodice(codiceTipoCollegamento)
        if (tipoCollegamento == null) {
            throw new AttiRuntimeException("Il tipo di collegamento richiesto (codice: ${codiceTipoCollegamento}) non esiste.")
        }
        return getDocumentoCollegato(tipoCollegamento)
    }

    Documento getDocumentoCollegato (TipoCollegamento tipoCollegamento) {
        return GdoDocumentoCollegato.findByDocumentoAndTipoCollegamento(this, tipoCollegamento)?.collegato
    }

    long getNumeroDocumentiCollegati (TipoCollegamento tipoCollegamento) {
        return GdoDocumentoCollegato.countByDocumentoAndTipoCollegamento(this, tipoCollegamento)
    }

    long getNumeroDocumentiCollegati (String codiceTipoCollegamento) {
        TipoCollegamento tipoCollegamento = TipoCollegamento.findByCodice(codiceTipoCollegamento)
        if (tipoCollegamento == null) {
            throw new AttiRuntimeException("Il tipo di collegamento richiesto (codice: ${codiceTipoCollegamento}) non esiste.")
        }
        return getNumeroDocumentiCollegati(tipoCollegamento)
    }

    FileDocumento getFile (String codice) {
        return fileDocumenti.find { it.codice == codice }
    }

    List<FileDocumento> getListaFile (String codice) {
        return fileDocumenti.findAll { it.codice == codice }
    }

    FileDocumento getFilePrincipale () {
        return getFile(FileDocumento.CODICE_FILE_PRINCIPALE)
    }

    FileDocumento getFileOriginale () {
        return getFile(FileDocumento.CODICE_FILE_ORIGINALE)
    }

    FileDocumento getFileFrontespizio () {
        return getFile(FileDocumento.CODICE_FILE_FRONTESPIZIO)
    }

    long getIdDocumento () {
        if (id == null) {
            return -1
        }
        return id
    }

    /*
     * Gestione dei file
     */

    static namedQueries = {
        numeroFilePerNome { Long idDocumento, String nomeFile ->
            projections {
                count("id")
            }

            eq("id", idDocumento)

            fileDocumenti {
                eq("nome", nomeFile)
            }
        }
    }
}
