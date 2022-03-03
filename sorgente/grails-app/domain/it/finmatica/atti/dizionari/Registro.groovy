package it.finmatica.atti.dizionari

import java.util.Date;

import it.finmatica.so4.struttura.So4Amministrazione;
import it.finmatica.so4.struttura.So4Ottica;

class Registro {
	TipoRegistro tipoRegistro
	int anno
	int ultimoNumero
	Date dataUltimoNumero

	boolean valido = true
	Date validoDal  // da valorizzare alla creazione del record
	Date validoAl   // deve essere valorizzato con la data di sistema quando valido = false
					// quando valido = true deve essere null

    static mapping = {
		table 						'registri'
		id 			 		column: "id_registro"
		tipoRegistro 		column: 'tipo_registro',  	index: 'reg_tipreg_fk'
		dataUltimoNumero 	column: 'ultima_data'
		valido 				type:   'yes_no'
    }

	static constraints = {
		validoAl     nullable: true
	}


	def beforeValidate() {
		validoDal 	= 	validoDal?:new Date()
	}

	def beforeInsert() {
		validoAl 	= 	valido?null:(validoAl?:new Date())
		validoDal 	= 	new Date()
	}

	def beforeUpdate() {
		validoAl 	= 	valido?null:(validoAl?:new Date())
	}
}
