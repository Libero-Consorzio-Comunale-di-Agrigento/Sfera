package it.finmatica.atti.documenti

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

/**
 * Contiene i riferimenti alle attività di jworklist create per le notifiche.
 * La tabella contiene solo le attività in uso (quando un'attività viene tolta dalla jworklist, viene tolta anche da questa tabella.
 *
 *
 * @author mfrancesconi
 */
class DestinatarioNotificaAttivita {

    public static final transient String TIPO_NOTIFICA_PEC       = "PEC"
    public static final transient String TIPO_NOTIFICA_JWORKLIST = "JWORKLIST"

	public static final transient String NOTIFICA_UTENTE= "UTENTE"
	public static final transient String NOTIFICA_UNITA	= "UNITA"

	DestinatarioNotifica	destinatarioNotifica
	Notifica 				notifica
	Ad4Utente 				utente              // utente a cui è stata inviata la notifica
	So4UnitaPubb 			unitaSo4
	String					idRiferimento		// identifica il documento per cui è stata inviata la notifica
	String					idAttivita		    // id di riferimento della notifica inviata (può essere per la jworklist o per la PEC)
	String 					soggettoNotifica    // indica se la notifica è per "unità" o per "singolo utente"
    String					modalitaInvio		// "JWORKLIST" oppure "PEC"

	static mapping = {
		table 					'destinatari_notifiche_attivita'
		id 						column: 'id_dest_notifica_attivita'
		destinatarioNotifica	column: 'id_destinatario_notifica'
		utente					column: 'utente_ad4'
		notifica				column: 'id_notifica'

		columns {
			unitaSo4 {
				column name: 'unita_progr'
				column name: 'unita_dal'
				column name: 'unita_ottica'
			}
		}
	}

    static constraints = {
        utente                  nullable: true  // può essere null perché la notifica ora può essere inviata anche a utenti esterni (via PEC) che non hanno un utente ad4.
		unitaSo4				nullable: true
		destinatarioNotifica 	nullable: true
		notifica				nullable: true
		soggettoNotifica		inList: [NOTIFICA_UTENTE, NOTIFICA_UNITA]
        modalitaInvio           inList: [TIPO_NOTIFICA_JWORKLIST, TIPO_NOTIFICA_PEC]
    }
}
