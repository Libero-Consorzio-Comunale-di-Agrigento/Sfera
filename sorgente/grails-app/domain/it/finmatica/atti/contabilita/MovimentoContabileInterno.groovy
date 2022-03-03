package it.finmatica.atti.contabilita

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.so4.struttura.So4Amministrazione
import org.zkoss.zhtml.Strong

class MovimentoContabileInterno {

    Determina        determina
    PropostaDelibera propostaDelibera
    String           esercizio
    String           capitolo
    String           descrizioneCapitolo
    String           articolo
    String           epf
    String           pdcf
    String           eos
    BigDecimal       importo
    String           codiceMissione
    String           codiceProgramma
    String           progressivoSoggetto
    String           descrizioneSoggetto
    String           cognome
    String           nome
    String           cf
    String           piva
    String           cfEstero
    String           pivaEstero
    String           indirizzo
    String           localita
    String           comune
    String           provincia
    String           cap
    String           stato
    String           telefono
    String           email
    String           pec
    String           note

    So4Amministrazione ente
    Date               dateCreated
    Ad4Utente          utenteIns
    Date               lastUpdated
    Ad4Utente          utenteUpd

    static mapping = {
        table 'movimenti_contabili_interni'
        id column: 'id_movimento_contabile'

        determina column: 'id_determina'
        propostaDelibera column: 'id_proposta_delibera'

        capitolo column: 'CAPITOLO'
        descrizioneCapitolo column: 'DESCRIZIONE_CAPITOLO'
        esercizio column: 'ESERCIZIO'
        importo column: 'IMPORTO', scale: 2
        articolo column: 'ARTICOLO'
        epf column: 'EPF'
        pdcf column: 'PDCF'
        eos column: 'EOS'
        progressivoSoggetto column: 'PROGRESSIVO_SOGGETTO'
        descrizioneSoggetto column: 'DESCRIZIONE_SOGGETTO'
        codiceMissione column: 'CODICE_MISSIONE'
        codiceProgramma column: 'CODICE_PROGRAMMA'
        note column: 'NOTE'
        cognome column: 'COGNOME'
        nome column: 'NOME'
        cf column: 'CF'
        piva column: 'PIVA'
        cfEstero column: 'CF_ESTERO'
        pivaEstero column: 'PIVA_ESTERO'
        indirizzo column: 'INDIRIZZO'
        localita column: 'LOCALITA'
        comune column: 'COMUNE'
        provincia column: 'PROVINCIA'
        cap column: 'CAP'
        stato column: 'STATO'
        telefono column: 'TELEFONO'
        email column: 'EMAIL'
        pec column: 'PEC'

        ente column: 'ente'
        dateCreated column: 'data_ins'
        utenteIns column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd column: 'utente_upd'
    }

    static constraints = {
        determina nullable: true
        propostaDelibera nullable: true
        capitolo nullable: true
        descrizioneCapitolo nullable: true
        importo nullable: true
        esercizio nullable: true
        articolo nullable: true
        epf nullable: true
        pdcf nullable: true
        eos nullable: true
        progressivoSoggetto nullable: true
        descrizioneSoggetto nullable: true
        codiceMissione nullable: true
        codiceProgramma nullable: true
        note nullable: true
        cognome nullable: true
        nome nullable: true
        cf nullable: true
        piva nullable: true
        cfEstero nullable: true
        pivaEstero nullable: true
        indirizzo nullable: true
        localita nullable: true
        comune nullable: true
        provincia nullable: true
        cap nullable: true
        stato nullable: true
        telefono nullable: true
        email nullable: true
        pec nullable: true
    }

    private SpringSecurityService getSpringSecurityService() {
        return Holders.getApplicationContext().getBean("springSecurityService")
    }

    def beforeValidate() {
        utenteIns = utenteIns ?: springSecurityService.currentUser
        ente = ente ?: springSecurityService.principal.amministrazione
        utenteUpd = utenteUpd ?: springSecurityService.currentUser
    }

    def beforeInsert() {
        utenteIns = springSecurityService.currentUser
        utenteUpd = springSecurityService.currentUser
        ente = springSecurityService.principal.amministrazione
    }

    def beforeUpdate() {
        utenteUpd = springSecurityService.currentUser
    }

    static hibernateFilters = {
        multiEnteFilter(condition: "ente = :enteCorrente", types: 'string')
    }

    public IProposta getProposta() {
        determina ?: propostaDelibera
    }

    public IAtto getAtto() {
        determina ?: Delibera.findByPropostaDelibera(propostaDelibera)
    }
}
