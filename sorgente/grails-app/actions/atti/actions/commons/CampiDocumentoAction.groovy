package atti.actions.commons

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.impostazioni.CampiDocumento
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzione
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzioneService

class CampiDocumentoAction {

	public static final String METODO_ABILITA_BLOCCO 	= "abilitaBlocco_";
	public static final String METODO_PROTEGGI_BLOCCO 	= "proteggiBlocco_";
	public static final String METODO_ABILITA_CAMPO		= "abilitaCampo_";
	public static final String METODO_PROTEGGI_CAMPO 	= "proteggiCampo_";

	WkfAzioneService 		wkfAzioneService
	SpringSecurityService 	springSecurityService

	/**
	 * Ritorna l'elenco dei campi da proteggere.
	 * Aggiunge all'elenco campiProtetti i campi del blocco del tipo oggetto.
	 *
	 * @param codiceTipoOggetto	il codice del tipo oggetto
	 * @param blocco			il codice del blocco dei campi da proteggere
	 * @param campiProtetti		la stringa dei campi già protetti concatenati
	 * @return					la stringa dei campi da proteggere concatenati
	 */
	private String proteggiCampiBlocco (String codiceTipoOggetto, String blocco, String campiProtetti) {
		// prendo i campi che questo blocco deve proteggere e li unisco all'elenco dei campi già protetti
		def listaCampi = CampiDocumento.splitListaCampi(campiProtetti)
		listaCampi.addAll(CampiDocumento.getCampiBlocco (codiceTipoOggetto, blocco))
		return CampiDocumento.joinListaCampi(listaCampi.unique())
	}

	/**
	 * Ritorna l'elenco dei campi da proteggere.
	 * Rimuove dall'elenco campiProtetti i campi del blocco del tipo oggetto.
	 *
	 * @param codiceTipoOggetto	il codice del tipo oggetto
	 * @param blocco			il codice del blocco dei campi da abilitare
	 * @param campiProtetti		la stringa dei campi già protetti concatenati
	 * @return					la stringa dei campi da proteggere concatenati
	 */
	private String abilitaCampiBlocco (String codiceTipoOggetto, String blocco, String campiProtetti) {
		// prendo i campi che questo blocco deve abilitare e li toglie da quelli già protetti
		def listaCampi = CampiDocumento.splitListaCampi(campiProtetti)
		listaCampi.removeAll(CampiDocumento.getCampiBlocco (codiceTipoOggetto, blocco))
		return CampiDocumento.joinListaCampi(listaCampi.unique())
	}

	def methodMissing(String name, args) {
		// i nomi sono della forma:
		// abilitaBlocco_NOME_BLOCCO
		// proteggiBlocco_NOME_BLOCCO

		if (name.startsWith (METODO_ABILITA_BLOCCO)) {
			String codiceBlocco = name.substring(METODO_ABILITA_BLOCCO.length())
			args[0].campiProtetti = abilitaCampiBlocco (args[0].TIPO_OGGETTO, codiceBlocco, args[0].campiProtetti)
		} else if (name.startsWith(METODO_PROTEGGI_BLOCCO)) {
			String codiceBlocco = name.substring(METODO_PROTEGGI_BLOCCO.length())
			args[0].campiProtetti = proteggiCampiBlocco(args[0].TIPO_OGGETTO, codiceBlocco, args[0].campiProtetti)
		} else if (name.startsWith(METODO_PROTEGGI_CAMPO)) {
			String codiceCampo = name.substring(METODO_PROTEGGI_CAMPO.length())
			args[0].campiProtetti = CampiDocumento.proteggiCampo(args[0].campiProtetti, codiceCampo);
		} else if (name.startsWith(METODO_ABILITA_CAMPO)) {
			String codiceCampo = name.substring(METODO_ABILITA_CAMPO.length())
			args[0].campiProtetti = CampiDocumento.abilitaCampo(args[0].campiProtetti, codiceCampo);
		} else {
            throw new MissingMethodException(name, args)
        }

		args[0].save();
	}

	public void aggiornaAzioni () {
		CampiDocumento.list().each { CampiDocumento c ->

			wkfAzioneService.insertOrUpdate(new WkfAzione([ nome: 		 "Abilita il blocco ${c.blocco}"
														  , descrizione: "Abilita i campi del blocco ${c.blocco}"
														  , nomeBean:    "campiDocumentoAction"
														  , nomeMetodo:  "${CampiDocumentoAction.METODO_ABILITA_BLOCCO}${c.blocco}"
														  , tipoOggetto: c.tipoOggetto
														  , tipo: 		 TipoAzione.AUTOMATICA
														  , ente: 		 springSecurityService.principal.amministrazione]), c.tipoOggetto.codice)

			wkfAzioneService.insertOrUpdate(new WkfAzione([ nome: 		 "Protegge il blocco ${c.blocco}"
														  , descrizione: "Protegge i campi del blocco ${c.blocco}"
														  , nomeBean:    "campiDocumentoAction"
														  , nomeMetodo:  "${CampiDocumentoAction.METODO_PROTEGGI_BLOCCO}${c.blocco}"
														  , tipoOggetto: c.tipoOggetto
														  , tipo: 		 TipoAzione.AUTOMATICA
														  , ente: 		 springSecurityService.principal.amministrazione]), c.tipoOggetto.codice)

			wkfAzioneService.insertOrUpdate(new WkfAzione([ nome: 		 "Abilita il campo ${c.campo}"
				, descrizione: "Abilita il ${c.campo}"
				, nomeBean:    "campiDocumentoAction"
				, nomeMetodo:  "${CampiDocumentoAction.METODO_ABILITA_CAMPO}${c.campo}"
				, tipoOggetto: c.tipoOggetto
				, tipo: 		 TipoAzione.AUTOMATICA
				, ente: 		 springSecurityService.principal.amministrazione]), c.tipoOggetto.codice)

			wkfAzioneService.insertOrUpdate(new WkfAzione([ nome: 		 "Protegge il campo ${c.campo}"
							, descrizione: "Protegge il campo ${c.campo}"
							, nomeBean:    "campiDocumentoAction"
							, nomeMetodo:  "${CampiDocumentoAction.METODO_PROTEGGI_CAMPO}${c.campo}"
							, tipoOggetto: c.tipoOggetto
							, tipo: 		 TipoAzione.AUTOMATICA
							, ente: 		 springSecurityService.principal.amministrazione]), c.tipoOggetto.codice)
		}
	}

	/* 			********************
	 * 			**** ATTENZIONE ****
	 * 			********************
	 * Queste funzioni specifiche per il TESTO_MANUALE, _invertono_ il normale funzionamento.
	 * Questo viene fatto per evitare di dover andare a sporcare il db di TUTTI solo per ovviare a una richiesta di Rivoli.
	 * Quindi la funzione abilitaCampo_ di fatto NASCONDE il campo di caricamento testo, viceversa, la funzione proteggiCampo_ lo visualizza.
	 */

	public def abilitaCampo_TESTO_MANUALE (def documento) {
		documento.campiProtetti = CampiDocumento.proteggiCampo (documento.campiProtetti, "TESTO_MANUALE");
		documento.save();
		return documento;
	}

	public def proteggiCampo_TESTO_MANUALE (def documento) {
		documento.campiProtetti = CampiDocumento.abilitaCampo (documento.campiProtetti, "TESTO_MANUALE");
		documento.save();
		return documento;
	}

	public def abilitaBlocco_TESTO_MANUALE (def documento) {
		documento.campiProtetti = proteggiCampiBlocco (documento.TIPO_OGGETTO, "TESTO_MANUALE", documento.campiProtetti);
		documento.save();
		return documento;
	}

	public def proteggiBlocco_TESTO_MANUALE (def documento) {
		documento.campiProtetti = abilitaCampiBlocco (documento.TIPO_OGGETTO, "TESTO_MANUALE", documento.campiProtetti);
		documento.save();
		return documento;
	}
}
