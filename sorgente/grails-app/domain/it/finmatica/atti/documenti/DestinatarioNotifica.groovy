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
	public static final transient String TIPO_DESTINATARIO_PITRE	= "PITRE"

	public static final transient String TIPO_NOTIFICA_COMPETENZA 	= 'COMPETENZA'
	public static final transient String TIPO_NOTIFICA_CONOSCENZA 	= 'CONOSCENZA'
	public static final transient String TIPO_NOTIFICA_PND 			= 'PND'

	static belongsTo = [determina : Determina, delibera : Delibera, propostaDelibera : PropostaDelibera, sedutaStampa : SedutaStampa]

	Email 				email
	Ad4Utente 			utente
	As4SoggettoCorrente	soggetto
	So4UnitaPubb 		unitaSo4
	String 				tipoNotifica
	String 				tipoDestinatario
	BigDecimal			importo
	Integer			 	giorniScadenza
	Date				dataScadenza
	String				codiceEsterno
	Long 				idRecapito
	Long 				idContatto
	String 				cognome
	String 				nome
	String 				codiceFiscale
	String 				indirizzo
	String 				comune
	String 				provincia
	String 				cap
	String 				indirizzoEmail

	//aggiunti per PiTre
	String				idSoggettoEsterno
	String 				tipoSoggettoEsterno
	String 				tipoCorrispondenteEsterno
	String				tipoRubricaEsterna  //TODO

	static mapping = {
		table 				'destinatari_notifiche'
		id 					column: 'id_destinatario_notifica'
		determina			column: 'id_determina', 			index: 'desnot_det_fk'
		delibera			column: 'id_delibera', 				index: 'desnot_del_fk'
		propostaDelibera	column: 'id_proposta_delibera', 	index: 'desnot_prodel_fk'
		sedutaStampa    	column: 'id_seduta_stampa', 	    index: 'desnot_sedsta_fk'
		email				column: 'id_email'
		utente				column: 'utente_ad4'
		soggetto 			column: 'ni_soggetto'
		importo				column: 'importo'
		giorniScadenza		column: 'giorni_scadenza'
		dataScadenza		column: 'data_scadenza'
		codiceEsterno		column: 'codice_esterno'

		idSoggettoEsterno	column: 'id_soggetto_esterno'
		tipoSoggettoEsterno	column: 'tipo_soggetto_esterno'
		tipoCorrispondenteEsterno	column: 'tipo_corrispondente_esterno'
		tipoRubricaEsterna	column: 'tipo_rubrica_esterna'

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
		soggetto			nullable: true
		importo				nullable: true
		giorniScadenza		nullable: true
		dataScadenza		nullable: true
		codiceEsterno		nullable: true
		idRecapito			nullable: true
		idContatto			nullable: true
		cognome				nullable: true
		nome				nullable: true
		codiceFiscale		nullable: true
		indirizzo			nullable: true
		comune				nullable: true
		provincia			nullable: true
		cap					nullable: true
		indirizzoEmail		nullable: true

		idSoggettoEsterno	nullable: true
		tipoSoggettoEsterno	nullable: true
		tipoCorrispondenteEsterno	nullable: true
		tipoRubricaEsterna	nullable: true

		tipoNotifica 		inList: [TIPO_NOTIFICA_COMPETENZA, TIPO_NOTIFICA_CONOSCENZA, TIPO_NOTIFICA_PND]
		tipoDestinatario	inList: [TIPO_DESTINATARIO_INTERNO, TIPO_DESTINATARIO_ESTERNO, TIPO_DESTINATARIO_PITRE]
    }

	DestinatarioNotifica duplica () {
		DestinatarioNotifica duplica = new DestinatarioNotifica()
		duplica.email = this.email
		duplica.utente = this.utente
		duplica.unitaSo4 = this.unitaSo4
		duplica.tipoNotifica = this.tipoNotifica
		duplica.tipoDestinatario = this.tipoDestinatario
		duplica.soggetto = this.soggetto
		duplica.importo = this.importo
		duplica.giorniScadenza = this.giorniScadenza
		duplica.dataScadenza = this.dataScadenza
		duplica.codiceEsterno = this.codiceEsterno
		duplica.idRecapito = this.idRecapito
		duplica.idContatto = this.idContatto
		duplica.cognome = this.cognome
		duplica.nome = this.nome
		duplica.codiceFiscale = this.codiceFiscale
		duplica.indirizzo = this.indirizzo
		duplica.comune = this.comune
		duplica.provincia = this.provincia
		duplica.cap = this.cap
		duplica.indirizzoEmail = this.indirizzoEmail

		duplica.idSoggettoEsterno = this.idSoggettoEsterno
		duplica.tipoSoggettoEsterno = this.tipoSoggettoEsterno
		duplica.tipoCorrispondenteEsterno = this.tipoCorrispondenteEsterno
		duplica.tipoRubricaEsterna = this.tipoRubricaEsterna

		return duplica
	}

	//restituisce il soggetto legato all'utente ad4 destinatario della notifica
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
