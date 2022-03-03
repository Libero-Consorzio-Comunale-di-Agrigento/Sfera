package it.finmatica.atti.commons

import it.finmatica.atti.IFileAllegato

class FileAllegatoStorico implements IFileAllegato {

    String	nome
	byte[]	allegato
	String	contentType
	long 	dimensione = -1
	String 	testo
	boolean	firmato 	 = false
	boolean modificabile = true;

	String	nomeOriginale
	String	contentTypeOriginale

	Long idFileEsterno	// indica l'id del file se salvato su un repository esterno (ad es GDM)

	static constraints = {
		nome		( blank		: false
					, maxSize	: 200)
		allegato		nullable: true
		idFileEsterno 	nullable: true
		testo			nullable: true
		nomeOriginale			nullable: true
		contentTypeOriginale	nullable: true
	}

	static mapping = {
    	table 			'file_allegati_storico'
    	id 				column:  'id_file_allegato_storico'
		allegato		sqlType: 'Blob'
		testo 			sqlType: 'Clob'
		firmato			type: 	 'yes_no'
		modificabile	type: 	 'yes_no'
	}

	def beforeValidate () {
		if (firmato) {
			modificabile = false;
		}
	}

	public transient boolean isP7m () {
		return this.nome.toLowerCase().endsWith("p7m");
	}

	public transient boolean isPdf () {
		return this.nome.toLowerCase().endsWith("pdf");
	}

	public transient String getNomeFileSbustato () {
		return this.nome.replaceAll(/\.p7m/, "")
	}

	public transient String getNomePdf () {
		return this.nome.replaceAll(/\..+$/, ".pdf")
	}

	public transient String getNomeFileOriginale () {
		if (this.nome.endsWith(FileAllegato.ESTENSIONE_FILE_NASCOSTO)) {
			return this.nome.substring(0, this.nome.length() - FileAllegato.ESTENSIONE_FILE_NASCOSTO.length())
		}
		return this.nome
	}
}
