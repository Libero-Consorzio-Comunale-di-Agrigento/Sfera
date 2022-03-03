package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.tipologie.TipoVistoParere
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class VistoParere implements IDocumentoCollegato, IDocumento {
	public static final transient String TIPO_OGGETTO 			= "VISTO"
	public static final transient String TIPO_OGGETTO_PARERE 	= "PARERE"

    WkfIter 		iter
	TipoVistoParere tipologia

	// il firmatario di default
	Ad4Utente 		firmatario

	// unità di redazione
	So4UnitaPubb 	unitaSo4

	// testo
	FileAllegato	testo
	FileAllegato	testoOdt
	GestioneTestiModello modelloTesto

	// dati visto/parere
	String 			note
	String 			noteTrasmissione
	Date 			dataAdozione	// questo campo è la data di firma, proveniente dalla trascodifica di GS4
	Date			dataOrdinamento
	boolean automatico = false
	EsitoVisto esito

	// indica lo stato del Visto: se è da processare, processato o annullato (ad es per reinoltro della proposta)
	StatoDocumento 	stato
	StatoFirma		statoFirma
	StatoMarcatura statoMarcatura

	// campo che contiene la lista dei campi non modificabili
	String campiProtetti

	static belongsTo =  [ determina			: Determina
						, propostaDelibera 	: PropostaDelibera
						, delibera			: Delibera]	// aggiunto per gestire i pareri della delibera per san donato milanese. #6987

	// chi ha effettivamente firmato il visto e allegati
	static hasMany = 	[ firmatari			: Firmatario
						, allegati			: Allegato]

	// indica l'id del documento sul documentale esterno (ad es. GDM)
	Long idDocumentoEsterno

	// indica se il documento è valido o no, cioè se è stato "cancellato" oppure no
	boolean 	valido = true

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static constraints = {
		tipologia	nullable: true
		iter		nullable: true
		firmatario 	nullable: true
		unitaSo4	nullable: true
		testo		nullable: true
		testoOdt	nullable: true
		note		nullable: true
		noteTrasmissione 	nullable: true
		modelloTesto 		nullable: true
		campiProtetti		nullable: true
		determina		    nullable: true
		propostaDelibera	nullable: true
		delibera		    nullable: true
		idDocumentoEsterno	nullable: true
		stato				nullable: true
		statoFirma			nullable: true
		statoMarcatura		nullable: true
		dataAdozione		nullable: true
		dataOrdinamento		nullable: true
    }

	static mapping = {
		table		'visti_pareri'
		id			column: 'id_visto_parere'
		iter		column: 'id_engine_iter'
		tipologia 	column: 'id_tipologia'
		testo		column: 'id_file_allegato_testo'
		testoOdt	column: 'id_file_allegato_testo_odt'
		modelloTesto column:'id_modello_testo'
		automatico	type: 'yes_no'
		note 		length: 4000
		noteTrasmissione 		length: 4000
		campiProtetti			length: 4000

		determina			column: "id_determina", 		index: 'vispar_det_fk'
		propostaDelibera	column: "id_proposta_delibera", index: 'vispar_prodel_fk'
		delibera			column: "id_delibera", 			index: 'vispar_del_fk'

		firmatario	column: 'utente_firmatario'
		columns {
			unitaSo4 {
				column name: 'unita_progr'
				column name: 'unita_dal'
				column name: 'unita_ottica'
			}
		}

		valido			type: 	'yes_no'
		ente 			column: 'ente'
		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'
	}

    transient String getTipoOggetto () {
        if (determina != null) {
            return VistoParere.TIPO_OGGETTO
        } else {
            return VistoParere.TIPO_OGGETTO_PARERE
        }
    }

	private SpringSecurityService getSpringSecurityService () {
		return Holders.applicationContext.getBean("springSecurityService")
	}

	def beforeValidate () {
		utenteIns = utenteIns?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
		utenteUpd = utenteUpd?:springSecurityService.currentUser

		allegati*.beforeValidate()
		firmatari*.beforeValidate()
	}

	def beforeInsert () {
		utenteIns = springSecurityService.currentUser
		utenteUpd = springSecurityService.currentUser
		ente	  = springSecurityService.principal.amministrazione
	}

	def beforeUpdate () {
		utenteUpd = springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: "ente = :enteCorrente", types: 'string')
	}

	/* metodi per le interfacce */

    static transients = ['documentoPrincipale', 'soggetto']

	long getIdDocumento () {
		return id?:-1
	}

    transient So4UnitaPubb getUnitaProponente () {
        return this.documentoPrincipale.unitaProponente
    }

    transient ITipologia getTipologiaDocumento () {
		return this.tipologia;
	}

	transient IDocumento getDocumentoPrincipale () {
		return this.determina?:this.propostaDelibera?:this.delibera;
	}

	transient void setDocumentoPrincipale (IDocumento documentoPrincipale) {
		if (documentoPrincipale instanceof Determina) {
			this.determina = documentoPrincipale;
		} else if (documentoPrincipale instanceof Delibera) {
			this.delibera = documentoPrincipale;
		} else if (documentoPrincipale instanceof PropostaDelibera) {
			this.propostaDelibera = documentoPrincipale;
		}
	}

	transient String getNomeFileTestoPdf () {
		return getNomeFile()+".pdf";
	}
	
	transient String getNomeFile () {
		return "${tipologia.codice}_${getDocumentoPrincipale().getNomeFile()}"
	}

	transient IProposta getProposta () {
		return propostaDelibera?:determina?.getProposta()?:delibera?.getProposta();
	}

	transient IAtto getAtto () {
		return propostaDelibera?.getAtto()?:determina?:delibera;
	}

	/**
	 * Setta il soggetto con del tipo richiesto con l'utente e/o l'unità so4
	 * @param tipoSoggetto 	il tipo soggetto da settare
	 * @param utenteAd4		l'utente del soggetto
	 * @param unitaSo4		l'unità del soggetto
	 */
	transient void setSoggetto (String tipoSoggetto, Ad4Utente utenteAd4, So4UnitaPubb unitaSo4) {
		setSoggetto(tipoSoggetto, utenteAd4, unitaSo4, 0);
	}

	/**
	 * Setta il soggetto con del tipo richiesto con l'utente e/o l'unità so4
	 * @param tipoSoggetto 	il tipo soggetto da settare
	 * @param utenteAd4		l'utente del soggetto
	 * @param unitaSo4		l'unità del soggetto
	 */
	transient void setSoggetto (String tipoSoggetto, Ad4Utente utenteAd4, So4UnitaPubb unitaSo4, int sequenza) {
		switch (tipoSoggetto) {
			case TipoSoggetto.FIRMATARIO:
				this.firmatario = utenteAd4
			break;
			case TipoSoggetto.UO_DESTINATARIA:
				this.unitaSo4 = unitaSo4
			break;
		}
	}

	/**
	 * Ritorna il soggetto della delibera
	 *
	 * @param tipoSoggetto	il codice del soggetto da ritornare
	 * @return	il soggetto trovato, null altrimenti.
	 */
	transient ISoggettoDocumento getSoggetto (String tipoSoggetto) {
		switch (tipoSoggetto) {
			case TipoSoggetto.FIRMATARIO:
				return new SoggettoDocumento (tipoSoggetto:TipoSoggetto.get(tipoSoggetto), utenteAd4:this.firmatario, unitaSo4:null, sequenza:0, attivo:true, documentoPrincipale:this)
			break

			case TipoSoggetto.UO_DESTINATARIA:
				return new SoggettoDocumento (tipoSoggetto:TipoSoggetto.get(tipoSoggetto), utenteAd4:null, unitaSo4:this.unitaSo4, sequenza:0, attivo:true, documentoPrincipale:this)
			break
		}

		return  null
	}

	transient List<ISoggettoDocumento> getSoggetti () {
		List<ISoggettoDocumento> sogg = []
		ISoggettoDocumento s = getSoggetto (TipoSoggetto.FIRMATARIO)
		if (s != null) {
			sogg << s
		}

		s = getSoggetto (TipoSoggetto.UO_DESTINATARIA)
		if (s != null) {
			sogg << s
		}

		return sogg
	}

	transient boolean isRiservato () {
		return false
	}
}
