package it.finmatica.atti.documenti

import grails.util.GrailsNameUtils
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.documenti.Documento
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

/**
 * Questa domain ha molteplici scopi:
 *
 * 1) PRIMA DELLA FIRMA: Viene usata per aggiungere alla coda dei firmatari, i soggetti che dovranno firmare il documento.
 *
 * 2) DURANTE LA FIRMA: Mantiene le informazioni della transazione di firma iniziata. In questo modo è possibile recuperarla immediatamente.
 *
 * 3) DOPO LA FIRMA: Una volta conclusa la firma, contiene i vari soggetti che hanno firmato i documenti.
 *
 * @author esasdelli
 */
class Firmatario {

	Ad4Utente 	firmatario
	Ad4Utente 	firmatarioEffettivo
	boolean 	firmato = false
	int 		sequenza		// sequenza di firma. 1-based.
	Date 		dataFirma

	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static belongsTo = [  determina 		: Determina
						, delibera 			: Delibera
						, propostaDelibera 	: PropostaDelibera
						, vistoParere 		: VistoParere
						, certificato 		: Certificato
						, documento			: Documento]

	static mapping = {
		table				'firmatari'
		id					column: 'id_firmatario'

		determina			column: 'id_determina',  		index: 'firm_det_fk'
		delibera 			column: 'id_delibera',  		index: 'firm_del_fk'
		propostaDelibera 	column: 'id_proposta_delibera', index: 'firm_prodel_fk'
		vistoParere 		column: 'id_visto_parere',  	index: 'firm_vispar_fk'
		certificato 		column: 'id_certificato',  		index: 'firm_cer_fk'
		documento 			column: 'id_documento',  		index: 'firm_doc_fk'

		firmatario 			column: 'utente_firmatario'
		firmatarioEffettivo	column: 'utente_firmatario_effettivo'
		firmato 			type: 	'yes_no'

		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'
	}

    static constraints = {
		dataFirma			nullable: true
		determina 		    nullable: true
		delibera 			nullable: true
		propostaDelibera 	nullable: true
		vistoParere 		nullable: true
		certificato 		nullable: true
		documento			nullable: true
		firmatarioEffettivo nullable: true
    }

	static namedQueries = {
		
		perDocumento { IDocumento documento ->
			if (documento instanceof Documento) {
				eq ("documento", documento)
			} else {
				eq (GrailsNameUtils.getPropertyName(GrailsHibernateUtil.unwrapIfProxy(documento).class), documento)
			}
		}
		
		prossimoCheDeveFirmare { IDocumento documento ->
			inCodaPerFirmare(documento, null)
			order ("sequenza", "asc")
			maxResults (1)
		}
		
		ultimoCheDeveFirmare { IDocumento documento ->
			inCodaPerFirmare(documento, null)
			order ("sequenza", "desc")
			maxResults (1)
		}
		
		inCodaPerFirmare { IDocumento documento, Ad4Utente utente = null ->
			perDocumento (documento)
			isNull ("dataFirma")
			eq ("firmato", false)
			
			if (utente != null) {
				or {
					eq("firmatario", utente)
					eq("firmatarioEffettivo", utente)
				}
			}
		}

		cheStaFirmando { IDocumento documento ->
			perDocumento (documento)
			isNotNull ("dataFirma")
			eq ("firmato", false)
			order ("sequenza", "asc")
			maxResults (1)
		}

		cheHannoFirmato { IDocumento documento ->
			perDocumento (documento)
			isNotNull ("dataFirma")
			eq ("firmato", true)
			order ("sequenza", "desc")
		}

		ultimoCheHaFirmato { IDocumento documento ->
			perDocumento (documento)
			isNotNull ("dataFirma")
			eq ("firmato", true)
			order ("sequenza", "desc")
			maxResults (1)
		}
	}

	private SpringSecurityService getSpringSecurityService () {
        return Holders.applicationContext.getBean("springSecurityService")
    }

	def beforeValidate () {
		utenteIns = utenteIns?:springSecurityService.currentUser
		utenteUpd = utenteUpd?:springSecurityService.currentUser
	}

	def beforeInsert () {
		utenteIns = springSecurityService.currentUser
		utenteUpd = springSecurityService.currentUser
	}

	def beforeUpdate () {
		utenteUpd = springSecurityService.currentUser
	}
}