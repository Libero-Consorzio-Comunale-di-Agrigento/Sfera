package it.finmatica.gestionedocumenti.documenti

import it.finmatica.atti.commons.FileAllegato
import it.finmatica.gestionetesti.reporter.GestioneTestiModello

/**
 * Rappresenta un FILE allegato ad un documento.
 * Il BLOB del file non è presente perché viene salvato direttamente su GDM nella OGGETTI_FILE collegato al documento GDM.
 * Il campo "idFileEsterno" contiene l'id_oggetto_file della tabella gdm.oggetti_file.
 * Questo file può essere generato da un template, in tal caso, il campo modelloTesto è pieno.
 * Infine, è possibile che esista una riga di questa domain senza che esista un corrispettivo file su GDM. Si tratta di quei casi in cui
 * è stato scelto il modelloTesto con cui generare il file, ma non è ancora stato generato.
 */
class FileDocumento extends FileAllegato {

    // allegato "generico"
    public static final String CODICE_FILE_ALLEGATO     = "FILE_ALLEGATO"   // un file allegato generico
    public static final String CODICE_FILE_PRINCIPALE   = "FILE_PRINCIPALE" // il file principale del documento
    public static final String CODICE_FILE_ORIGINALE    = "FILE_ORIGINALE"  // il file principale del documento prima della firma
    public static final String CODICE_FILE_FRONTESPIZIO = "FILE_FRONTESPIZIO"   // il frontespizio del documento

    // identifica il "tipo di collegamento" che c'è tra il file documento ed il Documento. Ad es: ALLEGATO, STAMPA_UNICA, FILE_PRINCIPALE
    // in caso di FileDocumento "storico" (cioè con revisione > 0) questo campo indica il tipo di "snapshot" del file
    String codice = CODICE_FILE_ALLEGATO

    // è l'ordinamento con cui viene caricato dal Documento
    Integer sequenza

    GestioneTestiModello modelloTesto

    // indica la revisione dello storico su gdm a cui è possibile recuperare questo file. La revisione è riferita al documento GDM principale (quindi al documento.idDocumentoEsterno)
    Long revisione

    Documento documento
    static    belongsTo = [documento: Documento]
//    static    hasMany   = [fileStorici: FileDocumento]

    static mapping = {
        discriminator column: 'tipo', value: 'FILE_DOCUMENTO'
        modelloTesto column: 'id_modello_testo'
        sequenza updateable: false, insertable: false
        revisione column: 'revisione_storico'
        documento column: 'id_documento'
    }

    static constraints = {
        sequenza nullable: true
        revisione nullable: true
        modelloTesto nullable: true
    }

    static namedQueries = {
        fileStorico { FileDocumento filePadre, String codice ->
            eq("fileOriginale", filePadre)
            eq("codice", codice)
        }
    }

    transient FileDocumento getFileStorico (String codice) {
        return FileDocumento.fileStorico(this, codice).get()
    }

    def beforeValidate () {
        super.beforeValidate()
//        fileStorici*.beforeValidate()
    }
}
