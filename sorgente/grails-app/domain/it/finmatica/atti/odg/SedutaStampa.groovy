package it.finmatica.atti.odg

import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.*
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.FileDocumento
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.GestioneTestiTipoModello
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

/**
 * Rappresenta la stampa creata per una commissione. A seconda del caso può contenere o meno il file allegato
 *
 * @author mfrancesconi
 *
 */
class SedutaStampa extends Documento implements IProtocollabile, IPubblicabile {

    public static final String TIPO_OGGETTO = "SEDUTA_STAMPA"
    public static final String CODICE_FILE_TESTO_MODIFICABILE = "TESTO_MODIFICABILE"

    // la seduta a cui appartiene questa stampa
    Seduta seduta

    // la "tipologia" di stampa
    CommissioneStampa commissioneStampa

    String note

    // dati di classificazione
    String  classificaCodice
    Date    classificaDal
    String  classificaDescrizione
    Integer fascicoloAnno
    String  fascicoloNumero
    String  fascicoloOggetto

    // dati di protocollo
    Date         dataNumeroProtocollo
    Integer      numeroProtocollo
    Integer      annoProtocollo
    TipoRegistro registroProtocollo
    Long         idDocumentoLettera     // id del documento della lettera su AGSPR

    // Dati dell'albo
    Long    idDocumentoAlbo
    Integer numeroAlbo
    Integer annoAlbo

    // indica se la stampa è accessibile dal visualizzatore.
    boolean pubblicaVisualizzatore = false

    // dati di pubblicazione
    boolean pubblicaRevoca = false
    boolean daPubblicare   = false
    Integer giorniPubblicazione
    Date    dataPubblicazione
    Date    dataFinePubblicazione
    Date    dataPubblicazione2
    Date    dataFinePubblicazione2
    Date    dataMinimaPubblicazione

    static belongsTo = [seduta: Seduta]
    static hasMany = [destinatariNotifiche: DestinatarioNotifica]

    static mapping = {
        table 'odg_sedute_stampe'
        id column: 'id_seduta_stampa'
        seduta column: 'id_seduta'
        commissioneStampa column: 'id_commissione_stampa'
        fascicoloOggetto length: 4000
        note length: 4000
        classificaDescrizione length: 4000
        registroProtocollo column: 'registro_protocollo'

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

        // dati di classificazione
        classificaCodice nullable: true
        classificaDal nullable: true
        classificaDescrizione nullable: true
        fascicoloAnno nullable: true
        fascicoloNumero nullable: true
        fascicoloOggetto nullable: true

        // dati di protocollo
        dataNumeroProtocollo nullable: true
        numeroProtocollo nullable: true
        annoProtocollo nullable: true
        registroProtocollo nullable: true

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

        // dati della lettera
        idDocumentoLettera nullable: true
    }

    static transients = ['modelloTesto', 'testo', 'testoOdt']

    @Override
    ITipologia getTipologiaDocumento () {
        return commissioneStampa
    }

    @Override
    FileAllegato getTesto () {
        return getFilePrincipale()
    }

    @Override
    void setTesto (FileAllegato testo) {
        if (testo == null) {
            FileDocumento testoPrincipale = getFilePrincipale()
            if (testoPrincipale != null) {
                removeFromFileDocumenti(testoPrincipale)
            }
        } else {
            throw new UnsupportedOperationException(
                    "non è possibile utilizzare setTesto su SedutaStampa, bisogna utilizzare invece .addToFileDocumenti")
        }
    }

    @Override
    FileAllegato getTestoOdt () {
        return getFile(CODICE_FILE_TESTO_MODIFICABILE)
    }

    @Override
    void setTestoOdt (FileAllegato testoOdt) {
        if (testoOdt == null) {
            FileDocumento file = getTestoOdt()
            if (file != null) {
                removeFromFileDocumenti(file)
            }
        } else {
            FileDocumento fileDocumento = new FileDocumento(nome: testoOdt.nome, contentType: testoOdt.contentType, codice: CODICE_FILE_TESTO_MODIFICABILE)
            addToFileDocumenti(fileDocumento)
        }
    }

    @Override
    GestioneTestiModello getModelloTesto () {
        return getFilePrincipale()?.modelloTesto ?: commissioneStampa.modelloTesto
    }

    @Override
    void setModelloTesto (GestioneTestiModello modelloTesto) {
        getFilePrincipale()?.modelloTesto = modelloTesto
    }

    @Override
    So4UnitaPubb getUnitaProponente () {
        return getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4
    }

    @Override
    String getNomeFileTestoPdf () {
        return "${getNomeFile()}.pdf"
    }

    @Override
    String getNomeFile () {
        GestioneTestiTipoModello tipoModello = getModelloTesto().tipoModello
        if (tipoModello.codice == Seduta.MODELLO_TESTO_VERBALE) {
            return "Verbale_${seduta.commissione.tipoRegistroSeduta.codice}_${seduta.numero}_${seduta.anno}"
        } else {
            return "Convocazione_${seduta.commissione.tipoRegistroSeduta.codice}_${seduta.numero}_${seduta.anno}"
        }
    }

    @Override
    transient Set<Allegato> getAllegati () {
        return new HashSet<Allegato>()
    }

    String getOggetto () {
        return "${commissioneStampa.titolo} ${seduta.commissione.titolo} ${seduta.numero} / ${seduta.anno} del ${seduta.dataOraSeduta.format("dd/MM/yyyy 'ore' HH:mm")}"
    }

    @Override
    ITipologiaPubblicazione getTipologiaPubblicazione () {
        return commissioneStampa
    }

    @Override
    IProtocollabile.Movimento getMovimento () {
        return IProtocollabile.Movimento.PARTENZA
    }

    @Override
    List<DestinatarioNotifica> getDestinatari () {
        if (this.destinatariNotifiche == null) {
            return []
        }

        return this.destinatariNotifiche as List
    }
}