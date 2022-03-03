package it.finmatica.gestionedocumenti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione

/**
 * Definisce la tipologia di documento
 *
 */
class TipoDocumento {

	// codice univoco che identifica il tipo documento nell'applicativo
	String codice
	String descrizione
	String commento
	String acronimo

    Long              progressivoCfgIter
    CaratteristicaTipologia caratteristicaTipologia

	boolean conservazioneSostitutiva	= false // se il documento deve andare in conservazione
	boolean testoObbligatorio			= true  // indica se il testo deve essere presente tra uno step e l'altro.

	GestioneTestiModello modelloTesto

	So4Amministrazione ente
	boolean            valido = true
	Date               dateCreated
	Ad4Utente          utenteIns
	Date               lastUpdated
	Ad4Utente          utenteUpd

	static mapping = {
		table 					'gdo_tipi_documento'
		tablePerHierarchy   	false
		id 						column: 'id_tipo_documento'
		caratteristicaTipologia column: 'id_caratteristica_tipologia'
        modelloTesto            column: 'id_modello_testo'
		descrizione 			length: 255
		commento 				length: 4000
		progressivoCfgIter		    column: 'progressivo_cfg_iter'
		conservazioneSostitutiva	type: 'yes_no'
		testoObbligatorio			type: 'yes_no'

        ente  column: 'ente'
        dateCreated 	column: 'data_ins'
        utenteIns 		column: 'utente_ins'
        lastUpdated 	column: 'data_upd'
        utenteUpd 		column: 'utente_upd'
        valido          type: 'yes_no'
	}

	static constraints = {
		progressivoCfgIter	nullable: true
		commento			nullable: true
		codice				nullable: true
		acronimo			nullable: true
	}

	transient WkfCfgIter getCfgIter () {
		return WkfCfgIter.getIterIstanziabile (progressivoCfgIter).get()
	}

	static namedQueries = {
		inUsoPerModelloTesto { long idModelloTesto ->
			or {
				modelliAssociati {
					eq ("modelloTesto.id", idModelloTesto)
				}
			}
			eq ("valido", true)
		}

		inUsoPerTipologiaSoggetto { long idTipologia ->
			eq ("tipologiaSoggetto.id", idTipologia)
			eq ("valido", true)
		}
	}

    SpringSecurityService getSpringSecurityService () {
        return Holders.getApplicationContext().getBean("springSecurityService")
    }

    def beforeValidate () {
        utenteIns	= utenteIns?:getSpringSecurityService().currentUser
        utenteUpd	= utenteUpd?:getSpringSecurityService().currentUser
        ente        = ente?:springSecurityService.principal.amministrazione
    }

    def beforeInsert () {
        utenteIns	= utenteIns?:getSpringSecurityService().currentUser
        utenteUpd	= utenteUpd?:getSpringSecurityService().currentUser
        ente        = ente?:springSecurityService.principal.amministrazione
    }

    def beforeUpdate () {
        utenteUpd	=	utenteUpd?:getSpringSecurityService().currentUser
    }
}
