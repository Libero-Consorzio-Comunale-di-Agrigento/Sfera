package it.finmatica.atti.odg

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.odg.dizionari.TipoSeduta
import it.finmatica.so4.login.So4UserDetail

import org.hibernate.criterion.CriteriaSpecification

/**
 * Rappresenta la seduta di una commissione
 *
 * @author mfrancesconi
 *
 */
class Seduta {

	public static final transient String MODELLO_TESTO_VERBALE 	 = 'ODG_VERBALE_SEDUTA'
	public static final transient String MODELLO_TESTO_CONVOCAZIONE = 'ODG_CONVOCAZIONE_SEDUTA'

	static hasMany = [partecipanti: SedutaPartecipante, stampe: SedutaStampa]

	Commissione commissione
	TipoSeduta  tipoSeduta
	String sede
	String note

	Date 	dataSeduta
	String 	oraSeduta
	Date 	dataInizioSeduta
	String 	oraInizioSeduta
	Date 	dataFineSeduta
	String 	oraFineSeduta
	Date 	dataSecondaConvocazione
	String 	oraSecondaConvocazione

	Integer	anno
	Integer numero
	boolean secondaConvocazione
	Seduta 	secondaSeduta

	// indica che la seduta è pubblica (anche utenti non autenticati possono vederla)
	boolean pubblica = true
	
	// indica che la seduta è pubblicata sul visualizzatore (solo quelle con pubblicaWeb = true possono essere viste dal visualizzatore)
	boolean pubblicaWeb = false
	
	boolean completa
	boolean valido = true
	boolean votoPresidente

	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd
	String 		link

	static mapping = {
		table 					'odg_sedute'
		id 						column: 'id_seduta'
		commissione 			column: 'id_commissione', 		index: 'odgsed_odgcom_fk'
		tipoSeduta 				column: 'id_tipo_seduta'
		secondaSeduta 			column: 'id_seconda_seduta'
		note 					length: 4000
		oraSeduta 				length: 5
		oraInizioSeduta 		length: 5
		oraFineSeduta 	  	    length: 5
		oraSecondaConvocazione 	length: 5
		secondaConvocazione 	type: 'yes_no'
		valido 					type: 'yes_no'
		completa 				type: 'yes_no'
		pubblica	 			type: 'yes_no'
		pubblicaWeb 			type: 'yes_no'
		votoPresidente			type: 'yes_no'
		dateCreated 			column: 'data_ins'
		utenteIns 				column: 'utente_ins'
		lastUpdated 			column: 'data_upd'
		utenteUpd 				column: 'utente_upd'
	}

	static constraints = {
		secondaSeduta 			nullable: true
		dataInizioSeduta 		nullable: true
		oraInizioSeduta 		nullable: true
		dataFineSeduta 		    nullable: true
		oraFineSeduta 		    nullable: true
		dataSecondaConvocazione nullable: true
		oraSecondaConvocazione 	nullable: true
		sede 					nullable: true
		note 					nullable: true
		anno 					nullable: true
		numero 					nullable: true
		link					nullable: true
	}

	static transients = ['utenteVisualizzatore', 'dataOraSeduta', 'dataOraInizioSeduta', 'dataOraSecondaConvocazione']
	private SpringSecurityService getSpringSecurityService () {
		return Holders.applicationContext.getBean("springSecurityService")
	}

	boolean isUtenteVisualizzatore() {
		boolean visualizza = false
		if (springSecurityService.isLoggedIn() && springSecurityService.getPrincipal() instanceof So4UserDetail){
			// Se autenticato e in ODG_SEDUTE_PARTECIPANTI o ruolo = RUOLO_VISUALIZZA per la commissione della seduta
			if (commissione.ruoloVisualizza != null && springSecurityService.getPrincipal().hasRuolo(commissione.ruoloVisualizza?.ruolo)){
				visualizza = true
			} else {
				// TODO: considero sia i componenti della commissione che gli esterni?
				SedutaPartecipante partecipante = SedutaPartecipante.createCriteria().get {
					createAlias('commissioneComponente', 'commissioneComponente', CriteriaSpecification.LEFT_JOIN)
					eq('seduta', this)
					or{
						eq('commissioneComponente.componente.id', springSecurityService.getPrincipal().getSoggetto().id)
						eq('componenteEsterno.id', springSecurityService.getPrincipal().getSoggetto().id)
					}
				}
				visualizza = (partecipante!=null)
			}
		}
		return visualizza
	}

	/**
	 * @return true se la seduta è iniziata (confrontando con sysdate)
	 */
	boolean isIniziata () {
		return (new Date().after(dataSeduta.clearTime()+1))
	}

	Date getDataOraSeduta () {
		return AttiUtils.dataOra(dataSeduta, oraSeduta);
	}

	Date getDataOraInizioSeduta () {
		return AttiUtils.dataOra(dataInizioSeduta, oraInizioSeduta);
	}

	Date getDataOraFineSeduta () {
		return AttiUtils.dataOra(dataFineSeduta, oraFineSeduta);
	}

	Date getDataOraSecondaConvocazione () {
		return AttiUtils.dataOra(dataSecondaConvocazione, oraSecondaConvocazione);
	}

	void setDataOraSeduta (Date data, String ora) {
		this.dataSeduta = AttiUtils.dataOra(data, ora);
		this.oraSeduta	= ora;
	}

	void setDataOraInizioSeduta (Date data, String ora) {
		this.dataInizioSeduta = AttiUtils.dataOra(data, ora);
		this.oraInizioSeduta	= ora;
	}

	void setDataOraFineSeduta (Date data, String ora) {
		this.dataFineSeduta = AttiUtils.dataOra(data, ora);
		this.oraFineSeduta	= ora;
	}

	void setDataOraSecondaConvocazione (Date data, String ora) {
		this.dataSecondaConvocazione = AttiUtils.dataOra(data, ora);
		this.oraSecondaConvocazione  = ora;
	}

	def beforeValidate() {
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	def beforeInsert() {
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	def beforeUpdate() {
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}
}
