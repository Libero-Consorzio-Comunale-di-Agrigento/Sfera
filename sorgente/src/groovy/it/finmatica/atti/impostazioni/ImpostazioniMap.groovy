package it.finmatica.atti.impostazioni

import grails.plugin.springsecurity.SpringSecurityService
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.util.WebUtils

import java.util.concurrent.ConcurrentHashMap

public class ImpostazioniMap {

	private static final Logger log = Logger.getLogger (ImpostazioniMap.class)

	public static final String ENTE_FALLBACK 		= "*"
	public static final String ENTE_SENZA_LOGIN 	= "ENTE_SENZA_LOGIN"

	SpringSecurityService springSecurityService

	private final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> map
	private final Object lock
	private boolean initialized = false

	public ImpostazioniMap () {
		lock = new Object();
		map  = new ConcurrentHashMap<String, ConcurrentHashMap<String,String>>();
	}

	public void init () {
		Impostazioni.map = this;
	}

	public void clear () {
		synchronized (lock) {
			map.clear();
		}
	}

	public void refresh () {
		synchronized (lock) {
			// svuoto la mappa:
			//Impostazione.disableHibernateFilter('multiEnteFilter')
			Collection<Impostazione> impostazioni = getListaImpostazioni()
			
			for (Impostazione i : impostazioni) {
				log.debug ("Impostazione: ${i.ente}.${i.codice}->${i.valore}")
				ConcurrentHashMap<String, String> ente = map.get(i.ente);
				if (ente == null) {
					ente = new ConcurrentHashMap<String, String>();
					map.put(i.ente, ente);
				}

				log.debug "codice=${i.codice}, valore=${i.valore}"
				ente.put(i.codice, i.valore);
			}
			initialized = true;
		}
	}
	
	/**
	 * Ritorna l'elenco delle impostazioni.
	 * Metto questo metodo pubblico solo per esigenze di test (ahimè), siccome negli unit-test non c'è verso di fare override di Impostazioni.list ed Impostazioni è una domain con id composite non gestito in unit-test.
	 * @return l'elenco delle impostazioni
	 */
	public Collection<Impostazione> getListaImpostazioni () {
		return Impostazione.list ([order: "asc", sort:"ente"])
	}

	/**
	 * Questo metodo imposta il codice ente come valore di request.
	 * È necessario nel caso in cui l'utente non abbia fatto login e quindi non ci sia lo springSecurityService.principal
	 *
	 * @param ente
	 * @return
	 */
	public void setCodiceEnteSenzaLogin (String ente) {
		WebUtils.retrieveGrailsWebRequest().getSession()[ENTE_SENZA_LOGIN] = ente;
	}

	public String getCodiceEnteSenzaLogin () {
		return WebUtils.retrieveGrailsWebRequest().getSession()[ENTE_SENZA_LOGIN];
	}

	public String getValore (String impostazione, String valoreDefault, String ente) {
		if (!initialized) {
			refresh();
		}

		ConcurrentHashMap<String, String> enteMap = map.get(ente);
		
		// se l'ente richiesto non ha l'impostazione richiesta, vado sull'ente di fallback
		if (enteMap == null || !enteMap.containsKey(impostazione)) {
			enteMap = map.get(ENTE_FALLBACK);
		}
		
		// se ancora non ho l'ente oppure non ho l'impostazione, ritorno il valore di default dell'impostazione
		if (enteMap == null || !enteMap.containsKey(impostazione)) {
			return valoreDefault;
		}
		
		return enteMap.get(impostazione);
	}

	public String getValore (String impostazione, String valoreDefault) {
		return getValore (impostazione, valoreDefault, getCodiceEnte());
	}

	private String getCodiceEnte () {
		if (springSecurityService.isLoggedIn()) {
			return springSecurityService.principal.amm()?.codice?:ENTE_FALLBACK;
		}

		if (getCodiceEnteSenzaLogin()?.length() > 0) {
			return getCodiceEnteSenzaLogin();
		}

		return ENTE_FALLBACK;
	}

	public byte[] getRisorsa (String impostazione) {
		if (!initialized) {
			refresh();
		}
		return Impostazione.getImpostazione (impostazione, getCodiceEnte()).get()?.risorsa;
	}
}
