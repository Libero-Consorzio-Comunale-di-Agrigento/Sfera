package it.finmatica.atti.commons

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.IFileAllegato
import it.finmatica.atti.documenti.StatoMarcatura
import org.apache.commons.io.FilenameUtils

class FileAllegato implements IFileAllegato {

    // questa estensione serve a GDM per nascondere i file
    public static final String ESTENSIONE_FILE_NASCOSTO = ".HD"

    String nome
    byte[] allegato
    String contentType
    long   dimensione = -1
    String testo

    // indica se il file è stato firmato dall'applicativo. Questo è sufficiente fintanto che si passa dalla domain Allegato che invece mantiene
    // lo statoFirma. Quindi se allegato.statoFirma = FIRMATO ma file.firmato = false, allora il file è stato caricato già firmato.
    boolean firmato = false

    // indica se il documento è un testo ancora modificabile dall'utente o no.
    boolean modificabile = false

    Long idFileEsterno    // indica l'id del file se salvato su un repository esterno (ad es GDM)

    FileAllegato fileOriginale


    boolean            valido = true
    Date               dateCreated
    Ad4Utente          utenteIns
    Date               lastUpdated
    Ad4Utente          utenteUpd
    StatoMarcatura      statoMarcatura

    static hasMany   = [fileFirmatiDettagli: FileFirmatoDettaglio]

    static constraints = {
        nome(blank: false, maxSize: 200)
        allegato nullable: true
        idFileEsterno nullable: true
        testo nullable: true
        fileOriginale nullable: true
        statoMarcatura nullable: true
    }

    static mapping = {
        discriminator column: 'tipo', value: 'FILE_ALLEGATO'
        table 'file_allegati'
        id column: 'id_file_allegato'
        allegato sqlType: 'Blob'
        fileOriginale column: 'id_file_allegato_originale'
        testo sqlType: 'Clob'
        firmato type: 'yes_no'
        modificabile type: 'yes_no'

        dateCreated column: 'data_ins'
        utenteIns column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd column: 'utente_upd'
        valido type: 'yes_no'
    }

    SpringSecurityService getSpringSecurityService () {
        return Holders.getApplicationContext().getBean("springSecurityService")
    }

    def beforeValidate () {
        if ((firmato && isP7m())|| statoMarcatura?.equals(StatoMarcatura.MARCATO)) {
            modificabile = false
        }

        utenteIns = utenteIns ?: getSpringSecurityService().currentUser
        utenteUpd = utenteUpd ?: getSpringSecurityService().currentUser
    }

    def beforeInsert () {
        utenteIns = utenteIns ?: getSpringSecurityService().currentUser
        utenteUpd = utenteUpd ?: getSpringSecurityService().currentUser
    }

    def beforeUpdate () {
        utenteUpd = utenteUpd ?: getSpringSecurityService().currentUser
    }

    transient boolean isPdf () {
        return this.nome.toLowerCase().endsWith("pdf");
    }

    transient boolean isP7m () {
        return this.nome.toLowerCase().endsWith("p7m");
    }

    transient String getNomeFileSbustato () {
        return this.nome.replaceAll(/(\.[pP]7[mM])+$/, "")
    }

    transient String getNomePdf () {
        return this.nome.replaceAll(/\..+$/, ".pdf")
    }

	transient String getEstensione () {
		return FilenameUtils.getExtension(this.nome)
	}

    FileAllegatoStorico creaFileAllegatoStorico () {
        return new FileAllegatoStorico(nome: nome, contentType: contentType, dimensione: dimensione, testo: testo, firmato: firmato,
                                       modificabile: modificabile)
    }

    /**
     * Il nome del file nascosto ha in più l'estensione per essere nascosto su gdm
     * @return
     */
    String getNomeFileNascosto () {
        return nome + ESTENSIONE_FILE_NASCOSTO
    }

    /**
     * Il nome del file originale senza l'estensione per essere nascosto su gdm.
     * @return
     */
    String getNomeFileOriginale () {
        return togliEstensioneGdm(nome)
    }

    /**
     * Il nome del file da usare per il testo_odt dei documenti.
     * Questo nome è formato da "ODT" + il nome del file nascosto, questo sempre per evitare i nomi doppi dei file su GDM.
     * @return
     */
    String getNomeFileOdt () {
        // il nome del file "odt" ha come prefisso "ODT" perché non deve essere uguale al nome del fileOriginale
        // che viene salvato prima della firma. Il nome del file non deve essere uguale perché altrimenti succedono casini con GDM.
        return "ODT" + getNomeFileNascosto()
    }

    /**
     * Il nome del file odt senza il prefisso "ODT" ed il prefisso ".HD"
     * @return
     */
    String getNomeFileOdtOriginale () {
        // in teoria questo "if" non dovrebbe servire siccome il nome del file odt deve iniziare sempre per "ODT" per evitare
        // file con nomi doppi su GDM. In pratica, siccome al Comune di Modena è successo un mezzo marone, è possibile che ci siano dei testi_odt
        // senza il prefisso "ODT".
        // FIXME: dati di modena così da togliere questo "if"
        if (nome.startsWith("ODT")) {
            return togliEstensioneGdm(nome.substring(3))
        }

        return togliEstensioneGdm(nome)
    }

    String togliEstensioneGdm (String nome) {
        if (nome.endsWith(ESTENSIONE_FILE_NASCOSTO)) {
            return nome.substring(0, nome.length() - ESTENSIONE_FILE_NASCOSTO.length())
        }
        return nome
    }
}
