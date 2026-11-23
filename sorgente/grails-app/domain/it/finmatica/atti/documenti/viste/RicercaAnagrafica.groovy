package it.finmatica.atti.documenti.viste

class RicercaAnagrafica {
	String  idRicercaAnagrafica
	String 	ni
	Long 	idRecapito
	Long 	idContatto
	String 	denominazione
	String 	cognome
	String 	nome
	String 	partitaIva
	String 	codiceFiscale
	String 	indirizzoCompleto
	String 	email
	String 	fax
	String 	indirizzo
	String 	cap
	String 	comune
	String 	provincia
	String 	telefono
	String 	sesso
	String 	comuneNascita
	String 	dataNascita
	String 	tipo
	String 	codiceFiscaleEstero
	Long 	idTipoRecapito
	String 	descrizioneTipoRecapito
	String 	tipoSpedizione
	Long 	tipoSoggetto
	Date 	dal
	Date 	al

	static mapping = {
		// mapping per questa domain
		table 			'ricerca_anagrafica'

		version false
		id 		generator: 'assigned', name: 'idRicercaAnagrafica', column: 'id_ricerca_anagrafica'

	}
}
