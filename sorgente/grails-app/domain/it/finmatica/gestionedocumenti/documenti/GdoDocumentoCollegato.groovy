package it.finmatica.gestionedocumenti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente

class GdoDocumentoCollegato {

    TipoCollegamento tipoCollegamento

    Documento documento
    Documento collegato

    static belongsTo = [documento : Documento]

    boolean   valido = true
    Date      dateCreated
    Ad4Utente utenteIns
    Date      lastUpdated
    Ad4Utente utenteUpd

	static mapping = {
		table		'gdo_documenti_collegati'
		id 			        column: 'id_documento_collegato'
		tipoCollegamento	column: 'id_tipo_collegamento', index: 'tipcoll_fk'
		documento			column: 'id_documento', 		index: 'docprinc_fk'
		collegato			column: 'id_collegato', 		index: 'doccoll_fk'

        dateCreated 	column: 'data_ins'
        utenteIns 		column: 'utente_ins'
        lastUpdated 	column: 'data_upd'
        utenteUpd 		column: 'utente_upd'
        valido          type: 'yes_no'
	}

    static constraints = {

    }

    static namedQueries = {
        /**
         * Ritorna l'elenco dei collegamenti in cui il documento passato risulta come "collegato"
         */
        collegamentiInversi { Documento documentoCollegato, def tipoColl = null ->

            if (tipoColl instanceof String) {
                tipoCollegamento {
                    eq ("codice", tipoColl)
                }
            } else if (tipoColl instanceof TipoCollegamento) {
                eq ("tipoCollegamento", tipoColl)
            }

            eq ("collegato", documentoCollegato)
        }

        /**
         * Ritorna l'elenco dei collegamenti in cui il documento passato risulta come "documento"
         */
        collegamenti { Documento documento, def tipoColl = null ->

            if (tipoColl instanceof String) {
                tipoCollegamento {
                    eq ("codice", tipoColl)
                }
            } else if (tipoColl instanceof TipoCollegamento) {
                eq ("tipoCollegamento", tipoColl)
            }

            eq ("documento", documento)
        }

        /**
         * Ritorna il n. di collegamenti in cui il documento passato risulta come "documento"
         */
        numeroCollegamenti { Documento documento, def tipoColl = null ->
            projections {
                count ("id")
            }

            collegamenti (documento, tipoColl)
        }

        /**
         * Ritorna il n. di collegamenti in cui il documento passato risulta come "collegato"
         */
        numeroCollegamentiInversi { Documento collegato, def tipoColl = null ->
            projections {
                count ("id")
            }

            collegamentiInversi (collegato, tipoColl)
        }
    }

    SpringSecurityService getSpringSecurityService () {
        return Holders.getApplicationContext().getBean("springSecurityService")
    }

    def beforeValidate () {
        utenteIns	= utenteIns?:getSpringSecurityService().currentUser
        utenteUpd	= utenteUpd?:getSpringSecurityService().currentUser
    }

    def beforeInsert () {
        utenteIns	= utenteIns?:getSpringSecurityService().currentUser
        utenteUpd	= utenteUpd?:getSpringSecurityService().currentUser
    }

    def beforeUpdate () {
        utenteUpd	=	utenteUpd?:getSpringSecurityService().currentUser
    }
}
