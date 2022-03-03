package it.finmatica.atti.documenti

import it.finmatica.atti.dizionari.EsitoControlloRegolarita;

class ControlloRegolaritaDocumento {
	ControlloRegolarita controlloRegolarita
	Determina 	determina
	Delibera	delibera
	EsitoControlloRegolarita esitoControlloRegolarita
	String note
	boolean notificata = false

	static mapping = {
		table 						'controllo_regolarita_doc'
		id 							column: 'id_controllo_regolarita_doc'
		controlloRegolarita 		column: 'id_controllo_regolarita', 	index: 'conregdoc_conreg_fk'
		determina					column: 'id_determina',				index: 'conregdoc_det_fk'
		delibera 					column: 'id_delibera',				index: 'conregdoc_del_fk'
		esitoControlloRegolarita 	column: 'id_esito_controllo_reg',	index: 'conregdoc_esito_fk'
		notificata					type: 	'yes_no'
	}

    static constraints = {
		determina 					nullable: true
		delibera 					nullable: true
		esitoControlloRegolarita 	nullable: true
		note						nullable: true
	}
}
