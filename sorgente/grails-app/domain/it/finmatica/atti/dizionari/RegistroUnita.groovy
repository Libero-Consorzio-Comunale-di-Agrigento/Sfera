package it.finmatica.atti.dizionari

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.impostazioni.CaratteristicaTipologia;
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
/**
 * Rappresenta i registri disponibili per un'unità
 * Ci deve essere un solo registro valido per unità
 * @author MFrancesconi
 *
 */
class RegistroUnita {
	TipoRegistro 	tipoRegistro
	So4UnitaPubb	unitaSo4
	CaratteristicaTipologia	caratteristica;

	boolean valido = true
	Date validoDal  // da valorizzare alla creazione del record
	Date validoAl   // deve essere valorizzato con la data di sistema quando valido = false
					// quando valido = true deve essere null

	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		table 			'tipi_registro_unita'
		id 				column: 'id_tipo_registro_unita'
		tipoRegistro	column: 'tipo_registro'
		caratteristica  column: 'id_caratteristica', index: 'FK_TIPREGUN_CARTIP'
		columns {
			unitaSo4 {
				column name: 'unita_progr'
				column name: 'unita_dal'
				column name: 'unita_ottica'
			}
		}

		valido 			type:   'yes_no'
		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'
	}

	static constraints = {
		validoAl 		nullable: true
		caratteristica	nullable: true
	}

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate() {
		validoDal 	= 	validoDal?:new Date()
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	def beforeInsert() {
		validoAl 	= 	valido?null:(validoAl?:new Date())
		validoDal 	= 	new Date()
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	def beforeUpdate() {
		validoAl 	= 	valido?null:(validoAl?:new Date())
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}
}
