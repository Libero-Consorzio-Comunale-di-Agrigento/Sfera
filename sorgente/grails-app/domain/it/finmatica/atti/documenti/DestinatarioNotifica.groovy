package it.finmatica.atti.documenti

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente;
import it.finmatica.atti.dizionari.Email
import it.finmatica.atti.odg.SedutaStampa;
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb;

/**
 * Contiene i destinatari interni o esterni associati a un documento.
 */
class DestinatarioNotifica {

	public static final transient String TIPO_DESTINATARIO_INTERNO	= "INTERNO"
	public static final transient String TIPO_DESTINATARIO_ESTERNO	= "ESTERNO"

	public static final transient String TIPO_NOTIFICA_COMPETENZA 	= 'COMPETENZA'
	public static final transient String TIPO_NOTIFICA_CONOSCENZA 	= 'CONOSCENZA'

	static belongsTo = [determina : Determina, delibera : Delibera, propostaDelibera : PropostaDelibera, sedutaStampa : SedutaStampa]

	Email 			email
	Ad4Utente 		utente
	So4UnitaPubb 	unitaSo4
	String 			tipoNotifica
	String 			tipoDestinatario

	static mapping = {
		table 				'destinatari_notifiche'
		id 					column: 'id_destinatario_notifica'
		determina			column: 'id_determina', 			index: 'desnot_det_fk'
		delibera			column: 'id_delibera', 				index: 'desnot_del_fk'
		propostaDelibera	column: 'id_proposta_delibera', 	index: 'desnot_prodel_fk'
		sedutaStampa    	column: 'id_seduta_stampa', 	    index: 'desnot_sedsta_fk'
		email				column: 'id_email'
		utente				column: 'utente_ad4'

		columns {
			unitaSo4 {
				column name: 'unita_progr'
				column name: 'unita_dal'
				column name: 'unita_ottica'
			}
		}
	}

    static constraints = {
        sedutaStampa        nullable: true
		determina			nullable: true
		delibera			nullable: true
		propostaDelibera	nullable: true
		email 				nullable: true
		utente				nullable: true
		unitaSo4			nullable: true
		tipoNotifica 		inList: [TIPO_NOTIFICA_COMPETENZA, TIPO_NOTIFICA_CONOSCENZA]
		tipoDestinatario	inList: [TIPO_DESTINATARIO_INTERNO, TIPO_DESTINATARIO_ESTERNO]
    }

	DestinatarioNotifica duplica () {
		DestinatarioNotifica duplica = new DestinatarioNotifica()
		duplica.email = this.email
		duplica.utente = this.utente
		duplica.unitaSo4 = this.unitaSo4
		duplica.tipoNotifica = this.tipoNotifica
		duplica.tipoDestinatario = this.tipoDestinatario
		return duplica
	}

    As4SoggettoCorrente getSoggettoCorrente () {
        if (this.utente == null) {
            return null
        }

        return As4SoggettoCorrente.findByUtenteAd4(this.utente)
    }

    DestinatarioNotificaAttivita getAttivita () {
        return DestinatarioNotificaAttivita.findByDestinatarioNotifica(this)
    }
}
