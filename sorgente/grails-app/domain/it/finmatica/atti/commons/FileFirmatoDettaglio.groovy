package it.finmatica.atti.commons

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente

/**
 * Rappresenta i Dati Aggiuntivi ai documenti.
 * @author esasdelli
 *
 */
class FileFirmatoDettaglio {

    public static String CALCOLATO  = 'CALCOLATO'
    public static String VERIFICATO = 'VERIFICATO'


    FileAllegato fileAllegato
    String nominativo
    Date dataFirma
    Date dataVerifica
    String stato
    Long idDocumento

    Date dateCreated
    Ad4Utente utenteIns
    Date lastUpdated
    Ad4Utente utenteUpd

    static belongsTo = [  fileAllegato: FileAllegato ]

    static mapping = {
        table 'file_firmati'
        id column: 'id_file_firmato'
        dateCreated column: 'data_ins'
        utenteIns column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd column: 'utente_upd'
        fileAllegato column: 'id_file_allegato'
    }

    static constraints = {
        dataFirma  nullable: true
        nominativo nullable: true
        stato      nullable: true
    }

    private SpringSecurityService getSpringSecurityService () {
        return Holders.getApplicationContext().getBean("springSecurityService")
    }

    def beforeValidate () {
        utenteIns = utenteIns ?: springSecurityService.currentUser
        utenteUpd = springSecurityService.currentUser
    }

    def beforeInsert () {
        utenteIns = springSecurityService.currentUser
        utenteUpd = springSecurityService.currentUser
    }

    def beforeUpdate () {
        utenteUpd = springSecurityService.currentUser
    }
}
