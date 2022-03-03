package it.finmatica.atti.commons

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.so4.struttura.So4SuddivisioneStruttura
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubbService
import it.finmatica.so4.strutturaPubblicazione.So4RuoloComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class StrutturaOrganizzativaService {

	public static final String TIPO_ASSEGNAZIONE 		= "%";
	public static final String ASSEGNAZIONE_PREVALENTE 	= "%";

	So4ComponentePubbService		so4ComponentePubbService

	/**
	 * Ritorna TRUE se il soggetto ha il ruolo richiesto per l'unità richiesta alla data odierna.
	 * Ritorna TRUE se il soggetto ha un qualunque ruolo (parametro ruolo = null) per l'unità richiesta in data odierna.
	 * Ritorna FALSE in tutti gli altri casi.
	 *
	 * @param niSoggetto	NI del soggetto da cercare
	 * @param ruolo			il codice del ruolo, può essere NULL in tal caso sarà sufficiente che l'utente abbia un ruolo qualunque per l'unità
	 * @param progr			il progressivo dell'unità. Può essere -1, in tal caso sarà sufficiente che l'utente abbia il ruolo specificato in una qualunque unità.
	 * @return
	 */
	public boolean soggettoHaRuoloPerUnita (long niSoggetto, String ruolo, long progr, String codiceOttica) {
		Date date = new Date();
		int count = So4RuoloComponentePubb.createCriteria().count {
			componente {
				eq ("soggetto.id", niSoggetto)

				if (progr > 0) {
					eq ("progrUnita", progr)
					le ("dal", date)
					or {
						isNull ("al")
						ge ("al",  date)
					}
				}

				eq ("ottica.codice", codiceOttica)
			}

			if (ruolo != null) {
				eq ("ruolo.ruolo", ruolo)
			}
		}

		return (count > 0);
	}

	/**
	 * Ritorna il componente dato l'utenteAd4 e l'unità di appartenenza.
	 *
	 * @param utenteAd4	utente del componente
	 * @param unitaSo4	unità di appartenenza
	 * @return	il componente trovato oppure null
	 */
	So4ComponentePubb getComponente (Ad4Utente utenteAd4, So4UnitaPubb unitaSo4) {
		def componenti = So4ComponentePubb.componentiUnitaPubb (unitaSo4.progr, unitaSo4.ottica.codice, unitaSo4.al?:new Date()) {
			soggetto {
				eq ("utenteAd4.id", utenteAd4.id)
			}
		}

		if (componenti.size() == 0)
			return null

		return componenti[0]
	}

	/**
	 * Ritorna tutte le UO valide alla data richiesta (default sysdate) a cui appartiene un soggetto.
	 *
	 * @param soggettoAs4	l'id del soggetto da cercare
	 * @param codiceOttica	il codice dell'ottica
	 * @return	ritorna l'elenco delle So4UnitaPubb trovate.
	 */
	public List<So4UnitaPubb> getUnitaSoggetto (long idSoggetto, String codiceOttica, Date date = new Date()) {
		return So4ComponentePubb.executeQuery ("""
			select uo
			  from So4UnitaPubb uo
			     , So4ComponentePubb c
			 where c.progrUnita = uo.progr
			   and uo.ottica.codice = c.ottica.codice
			   and c.dal <= :dataRif
			   and (c.al is null or c.al >= :dataRif)
			   and uo.dal <= :dataRif
			   and (uo.al is null or uo.al >= :dataRif)
			   and c.soggetto.id = :idSoggetto
			   and c.ottica.codice = :codiceOttica
			 order by uo.descrizione asc
			""", [dataRif: org.apache.commons.lang.time.DateUtils.truncate(date, Calendar.DATE), idSoggetto: idSoggetto, codiceOttica: codiceOttica])
	}

	/**
	 * Ritorna tutte le UO padri fino alla radice di una certa UO
	 *
	 * @param soggettoAs4	l'id del soggetto da cercare
	 * @param codiceOttica	il codice dell'ottica
	 * @return	ritorna l'unità di partenza e tutte le sue unità padri fino al vertice
	 */
	public List<So4UnitaPubb> getUnitaPadri (long progrUo, String codiceOttica, Date date = new Date()) {
		So4UnitaPubb uo = So4UnitaPubb.getUnita(progrUo, codiceOttica, date).get()
		def listaUo = []
		while (uo != null) {
			listaUo << uo
			uo = uo.unitaPubbPadre
		}

		return listaUo
	}

	/**
	 * Ritorna tutte le UO  di una certa UO
	 *
	 * @param codiceOttica	il codice dell'ottica
	 * @return				ritorna tutte le unità che appartengono all'ottica
	 */
	public List<So4UnitaPubb> getUnitaInOttica (String codiceOttica, Date date = new Date()) {
		return So4UnitaPubb.allaData(date).perOttica(codiceOttica).list()
	}

	/**
	 * Ritorna i componenti che hanno il ruolo specificato per un certo utente all'interno dell'ottica ordinati per assegnazione prevalente ASC.
	 *
	 * @param utenteAd4		il codice dell'utente ad4
	 * @param codiceRuolo	il codice del ruolo
	 * @param codiceOttica	il codice dell'ottica
	 * @return				i componenti relativi all'utente per l'ottica con il ruolo richiesto Ordinate per Assegnazione Prevalente ASC.
	 */
	public List<So4ComponentePubb> getComponentiPerUtenteConRuoloInOttica (String utenteAd4, String codiceRuolo, String codiceOttica, Date date = new Date()) {
		return So4ComponentePubb.executeQuery("""
			select c
			  from So4ComponentePubb 		c
				 , So4RuoloComponentePubb 	rc
				 , So4AttrComponente 		a
			 where rc.ruolo.ruolo = :ruolo
			   and rc.dal <= :dataRif
			   and (rc.al is null or rc.al >= :dataRif)
			   and rc.componente  = c
			   and c.dal <= :dataRif
			   and (c.al is null or c.al >= :dataRif)
			   and c.soggetto.utenteAd4.id = :utenteAd4
 			   and a.componente = c
			   and (a.al is null or a.al >= :dataRif)
   			   and a.dal <= :dataRif
			   and c.ottica.codice = :ottica
		  order by a.assegnazionePrevalente asc
		 """, [dataRif: org.apache.commons.lang.time.DateUtils.truncate(date, Calendar.DATE), ruolo:codiceRuolo, ottica:codiceOttica, utenteAd4:utenteAd4])
	}

	/**
	 * Ritorna tutte le UO valide alla data richiesta (default sysdate) a cui appartiene un utente.
	 *
	 * @param utenteAd4		il codice dell'utente ad4
	 * @param codiceOttica	il codice dell'ottica
	 * @return	ritorna l'elenco delle So4UnitaPubb trovate.
	 */
	public List<So4UnitaPubb> getUnitaUtente (String utenteAd4, String codiceOttica, Date date = new Date()) {
		return So4ComponentePubb.executeQuery ("""
			select uo
			  from So4UnitaPubb uo
			     , So4ComponentePubb c
			 where c.progrUnita = uo.progr
			   and uo.ottica.codice = c.ottica.codice
			   and c.dal <= :dataRif
			   and (c.al is null or c.al >= :dataRif)
			   and uo.dal <= :dataRif
			   and (uo.al is null or uo.al >= :dataRif)
			   and c.soggetto.utenteAd4.id = :utenteAd4
			   and c.ottica.codice = :codiceOttica
			 order by uo.descrizione asc
			""", [dataRif: org.apache.commons.lang.time.DateUtils.truncate(date, Calendar.DATE), utenteAd4: utenteAd4, codiceOttica: codiceOttica])
	}

	/**
	 * Ritorna tutte le Unità di un'ottica a cui l'utente appartiene e in cui ha un certo ruolo.
	 *
	 * @param utenteAd4		il codice dell'utenteAd4
	 * @param codiceRuolo	il codice del ruolo
	 * @param codiceOttica	il codice dell'ottica
	 * @return				tutte le Unità di un'ottica a cui l'utente appartiene e in cui ha un certo ruolo.
	 */
	public List<So4UnitaPubb> getUnitaUtenteConRuolo (String utenteAd4, String codiceRuolo, String codiceOttica, Date date = new Date(), String assegnazionePrevalente = StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE, String tipoAssegnazione = StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE) {
		return So4ComponentePubb.executeQuery ("""
			select distinct uo
			  from So4UnitaPubb uo
			     , So4ComponentePubb c
				 , So4RuoloComponentePubb 	rc
				 , So4AttrComponentePubb 	att
			 where uo.ottica.codice = :codiceOttica
			   and uo.dal <= :dataRif
			   and (uo.al is null or uo.al >= :dataRif)

			   and c.progrUnita = uo.progr
			   and c.dal <= :dataRif
			   and (c.al is null or c.al >= :dataRif)
   			   and c.soggetto.utenteAd4.id = :utenteAd4
			   and c.ottica = uo.ottica

			   and att.componente 						= c
			   and att.assegnazionePrevalente			like :assegnazionePrevalente
			   and att.tipoAssegnazione		    		like :tipoAssegnazione
			   and att.dal <= :dataRif
			   and (att.al is null or att.al >= :dataRif)

			   and rc.ruolo.ruolo = :ruolo
			   and rc.dal <= :dataRif
			   and (rc.al is null or rc.al >= :dataRif)
			   and rc.componente = c
			 order by uo.descrizione asc
			""", [dataRif: org.apache.commons.lang.time.DateUtils.truncate(date, Calendar.DATE), ruolo: codiceRuolo, utenteAd4: utenteAd4, codiceOttica: codiceOttica, assegnazionePrevalente:assegnazionePrevalente, tipoAssegnazione:tipoAssegnazione])
	}

	/**
	 * Ritorna tutte le Unità di un'ottica che contiengono almeno un utente con il ruolo richiesto
	 *
	 * @param codiceRuolo	il codice del ruolo
	 * @param codiceOttica	il codice dell'ottica
	 * @return				tutte le Unità di un'ottica a cui l'utente appartiene e in cui ha un certo ruolo.
	 */
	public List<So4UnitaPubb> getUnitaConRuolo (String codiceRuolo, String codiceOttica, Date date = new Date(), String assegnazionePrevalente = StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE, String tipoAssegnazione = StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE) {
		return So4ComponentePubb.executeQuery ("""
			select distinct uo
			  from So4UnitaPubb uo
			     , So4ComponentePubb c
				 , So4RuoloComponentePubb 	rc
				 , So4AttrComponentePubb 	att
			 where uo.ottica.codice = :codiceOttica
			   and uo.dal <= :dataRif
			   and (uo.al is null or uo.al >= :dataRif)

			   and c.progrUnita = uo.progr
			   and c.dal <= :dataRif
			   and (c.al is null or c.al >= :dataRif)
			   and c.ottica = uo.ottica

			   and att.componente 						= c
			   and att.assegnazionePrevalente			like :assegnazionePrevalente
			   and att.tipoAssegnazione		    		like :tipoAssegnazione
			   and att.dal <= :dataRif
			   and (att.al is null or att.al >= :dataRif)

			   and rc.ruolo.ruolo = :ruolo
			   and rc.dal <= :dataRif
			   and (rc.al is null or rc.al >= :dataRif)
			   and rc.componente = c
			 order by uo.descrizione asc
			""", [dataRif: org.apache.commons.lang.time.DateUtils.truncate(date, Calendar.DATE), ruolo: codiceRuolo, codiceOttica: codiceOttica, assegnazionePrevalente:assegnazionePrevalente, tipoAssegnazione:tipoAssegnazione])
	}

	/**
	 *  Restituisce true o false a seconda che l'utente in input abbia il ruolo definito nella tabella Impostazioni per il codice in input
	 *  su almeno un'unità dell'ottica di default definita sempre nella tabella delle Impostazioni
	 *
	 * @param utenteAd4
	 * @param codiceXOtticaSuImpostazioni
	 * @param codiceXRuoloSuImpostazioni
	 * @return
	 */
	public boolean utenteHasRuoloDaImpostazioni (String utenteAd4, String codiceXOtticaSuImpostazioni, String codiceXRuoloSuImpostazioni, Date date = new Date()) {
		def tot = So4UnitaPubb.executeQuery ("""
			select count(*)
			  from So4UnitaPubb uo
			     , So4ComponentePubb c
				 , So4RuoloComponentePubb 	rc
                 , Impostazione i1
				 , Impostazione i2
			 where i1.codice = :codiceXOtticaSuImpostazioni
               and i2.codice = :codiceXRuoloSuImpostazioni

               and uo.ottica.codice = i1.valore
			   and uo.dal <= :dataRif
			   and (uo.al is null or uo.al >= :dataRif)

			   and c.progrUnita = uo.progr
			   and c.dal <= :dataRif
			   and (c.al is null or c.al >= :dataRif)
   			   and c.soggetto.utenteAd4.id = :utenteAd4
			   and c.ottica = uo.ottica

			   and rc.ruolo.ruolo = i2.valore
			   and rc.dal <= :dataRif
			   and (rc.al is null or rc.al >= :dataRif)
			   and rc.componente = c
			""", [dataRif: org.apache.commons.lang.time.DateUtils.truncate(date, Calendar.DATE), codiceXOtticaSuImpostazioni: codiceXOtticaSuImpostazioni, utenteAd4: utenteAd4, codiceXRuoloSuImpostazioni: codiceXRuoloSuImpostazioni])
	    return (tot[0] > 0)
	}

	/**
	 * Ritorna tutti i componenti con un certo ruolo all'interno dell'ottica richiesta.
	 *
	 * Documentazione Filippo:
	 * a) Visualizzare tutti i funzionari dell'ente (cioè i componenti con il ruolo in input
	 *    appartenenti all'ottica passata come parametro)
	 *
	 * @param codiceRuolo	il codice del ruolo
	 * @param codiceOttica	il codice dell'ottica
	 * @param dataRif 		data di riferimento (default: sysdate)
	 * @return 				Elenco dei componenti con il ruolo richiesto che appartengono all'ottica
	 */
	public List<So4ComponentePubb> getComponentiConRuoloInOttica (def ruolo, String codiceOttica, Date dataRif = new Date()) {
		return so4ComponentePubbService.getComponentiPubbOtticaConRuolo(ruolo, codiceOttica, dataRif, StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE)
	}

	/**
	 * Ritorna tutti i componenti con un certo ruolo all'interno dell'ottica richiesta.
	 *
	 * Documentazione Filippo:
	 * a) Visualizzare tutti i funzionari dell'ente (cioè i componenti con il ruolo in input
	 *    appartenenti all'ottica passata come parametro)
	 *
	 * @param codiceRuolo	il codice del ruolo
	 * @param codiceOttica	il codice dell'ottica
	 * @param dataRif 		data di riferimento (default: sysdate)
	 * @return 				Elenco dei componenti con il ruolo richiesto che appartengono all'ottica
	 */
	public List<So4ComponentePubb> getComponentiConRuoliInOttica (Collection ruoli, String codiceOttica, Date dataRif = new Date()) {
		return so4ComponentePubbService.getComponentiPubbOtticaConRuoli(ruoli, codiceOttica, dataRif, StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE)
	}

	/**
	 * Ritorna tutti i componenti con un certo ruolo all'interno di una data unità.
	 *
	 * Documentazione Filippo:
	 * b) Visualizzare solo funzionari presenti nella uo del redattore ( cioè i componenti con un determinato ruolo
	 *    presenti nella uo del del componente passato in input )
	 *
	 * @param codiceRuolo	il codice del ruolo
	 * @param progrUo		il progressivo della uo
	 * @param codiceOttica	il codice dell'ottica
	 * @param dataRif		data di riferimento (default: sysdate) per estrarre componente e ruoli
	 * @return 				elenco di componenti con il ruolo richiesto che appartengono alla uo.
	 */
	public List<So4ComponentePubb> getComponentiConRuoloInUnita (String codiceRuolo, long progrUo, String codiceOttica, Date date = new Date()) {
		return So4ComponentePubb.executeQuery("""
			select c
			  from So4ComponentePubb 		c
				 , So4RuoloComponentePubb 	rc
				 , So4UnitaPubb				uo
			 where rc.ruolo.ruolo = :ruolo
			   and rc.dal <= :dataRif
			   and (rc.al is null or rc.al >= :dataRif)
			   and rc.componente = c
			   and c.dal <= :dataRif
			   and (c.al is null or c.al >= :dataRif)
			   and c.ottica.codice = :ottica
			   and c.progrUnita = :progrUnita
			   and uo.progr = c.progrUnita
			   and uo.dal <= :dataRif
			   and uo.ottica.codice = :ottica
               and (uo.al is null or uo.al >= :dataRif)""", [dataRif: org.apache.commons.lang.time.DateUtils.truncate(date, Calendar.DATE), ruolo:codiceRuolo, ottica:codiceOttica, progrUnita:progrUo])
	}

	/**
	 * Ritorna tutti i componenti che hanno un certo ruolo nell'unità richiesta e in tutta la catena dei suoi "padri" fino al vertice.
	 *
	 * Documentazione Filippo:
	 * c) Visualizzare funzionari ( cioè i componenti con il ruolo in input ) presenti nella uo del redattore
	 *    ( componente in input ) e nella stessa area del redattore (navigando la struttura SOLO verso l'alto fino
	 *    ad arrivare al vertice)
	 *
	 * @param codiceRuolo	il codice del ruolo
	 * @param progrUo		il progressivo della uo
	 * @param codiceOttica	il codice dell'ottica
	 * @param dataDal		data dell'unità
	 * @param fermatiAlPrimoRisultato	se true, la ricerca si ferma al primo componente con il ruolo richiesto che viene trovato.
	 * @return 				elenco di componenti con il ruolo richiesto che appartengono alla uo e a tutti i padri della uo.
	 */
	public List<So4ComponentePubb> getComponentiConRuoloInUnitaPadri(String codiceRuolo, long progrUo, String codiceOttica, Date dataDal, boolean fermatiAlPrimoRisultato = false) {
		So4UnitaPubb uo = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

		List<So4ComponentePubb> elencoComponenti = []
		while (uo != null) {
			elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuolo(codiceRuolo, uo, uo.al?:new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))
			if (fermatiAlPrimoRisultato && elencoComponenti.size() > 0)
				return elencoComponenti

			uo = uo.getUnitaPubbPadre(uo.al?:new Date())
		}
		return elencoComponenti
	}

	/**
	 * Ritorna tutti i componenti che hanno un certo ruolo nell'unità richiesta e in tutta la catena dei suoi "padri" fino ad arrivare a una certa suddivisione.
	 *
	 * Documentazione Filippo:
	 * d) Visualizzare funzionari ( cioè i componenti con il ruolo in input ) presenti nella uo del redattore
	 * ( componente in input ) e nella stessa area del redattore (navigando la struttura SOLO verso l'alto ma
	 * fino ad arrivare ad una suddivisione della struttura che non sia necessariamente il vertice)
	 *
	 * @param codiceRuolo	il codice del ruolo
	 * @param progrUo		il progressivo della uo
	 * @param codiceOttica	il codice dell'ottica
	 * @param dataDal		data dell'unità
	 * @param idSuddivisione l'id della suddivisione a cui fermarsi
	 * @param fermatiAlPrimoRisultato	se true, la ricerca si ferma al primo componente con il ruolo richiesto che viene trovato.
	 * @return 				elenco di componenti con il ruolo richiesto che appartengono alla uo e a tutti i padri della uo fino ad arrivare a una certa suddivisione.
	 */
	public List<So4ComponentePubb> getComponentiConRuoloInPadriFinoASuddivisione(String codiceRuolo, long progrUo, String codiceOttica, Date dataDal, long idSuddivisione, boolean fermatiAlPrimoRisultato = false) {
		So4UnitaPubb uo = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

		List<So4ComponentePubb> elencoComponenti = []
		while (uo != null) {
			elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuolo(codiceRuolo, uo, uo.al?:new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))
			if ((fermatiAlPrimoRisultato && elencoComponenti.size() > 0) || uo.suddivisione.id == idSuddivisione)
				return elencoComponenti

			uo = uo.getUnitaPubbPadre(uo.al?:new Date())
		}
		return elencoComponenti
	}

	public List<So4ComponentePubb> getComponentiConRuoloInSuddivisione(def ruoli, long progrUo, String codiceOttica, Date dataDal, long idSuddivisione, boolean fermatiAlPrimoRisultato = false){
		So4UnitaPubb uo = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()
		def codiciRuoli = [];


		// ottengo l'unità suddivisione "padre":
		while (uo != null && uo.suddivisione.id != idSuddivisione) {
			uo = uo.getUnitaPubbPadre(uo.al?:new Date())
		}

		// se non ho trovato la suddivisone, esco e ritorno una lista vuota.
		if (uo == null) {
			return [];
		}

		List<So4ComponentePubb> elencoComponenti = []
		// prendo tutti i componenti con i ruoli richiesti.
		visitDepthFirst(uo, -1) { So4UnitaPubb unita ->
			elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuoli(ruoli, unita, unita.al?:new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))
		}

		return elencoComponenti;
	}
	/**
	 * Ritorna tutti i componenti che hanno un certo ruolo nell'unità richiesta, nelle sue sorelle e in tutta la catena dei suoi "padri" fino ad arrivare a una certa suddivisione.
	 *
	 * Documentazione Filippo:
	 * e) Visualizzare funzionari ( cioè i componenti con il ruolo in input ) presenti nella uo del redattore
	 * ( componente in input ) e nella stessa area del redattore ( presentando tutti i componenti con ruolo
	 * funzionario dell'area di appartenenza del redattore quindi anche in unità di pari livello del redattore
	 * ed in generale di altri rami dell'albero dell'area)
	 * A posteriori si è deciso di implementare anche la possibiltà di ricercare fino ad un certa suddivisione
	 *
	 * @param codiceRuolo	il codice del ruolo
	 * @param progrUo		il progressivo della uo
	 * @param codiceOttica	il codice dell'ottica
	 * @param dataDal		data dell'unità
	 * @param idSuddivisione l'id della suddivisione a cui fermarsi, default: -1 (indica di ricercare per tutta l'area e non fermarsi alla suddivisione)
	 * @return 				elenco di componenti con il ruolo richiesto che appartengono alla uo e a tutti i padri della uo fino ad arrivare a una certa suddivisione.
	 */
	public List<So4ComponentePubb> getComponentiConRuoloInUnitaEPadri (String codiceRuolo, long progrUo, String codiceOttica, Date dataDal, long idSuddivisione = -1) {
		List<So4ComponentePubb> elencoComponenti = []

		// per prima cosa ottengo l'uo di partenza:
		So4UnitaPubb uoPartenza = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

		// ne prendo i componenti:
		elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuolo(codiceRuolo, uoPartenza, uoPartenza.al?:new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))

		// quindi prendo l'uo padre e i suoi componenti con ruolo richiesto:
		So4UnitaPubb uoPadre = uoPartenza.getUnitaPubbPadre(uoPartenza.al?:new Date())

		if (uoPadre != null) {
			// prendo le unità sorelle dell'unità di partenza:
			List<So4UnitaPubb> uoSorelle = uoPadre.getUnitaPubbFiglie(uoPadre.al?:new Date())

			// e prendo i componenti per ogni uo figlia (e quindi sorella di quella di partenza)
			for (So4UnitaPubb uo : uoSorelle) {
				// salto l'uo da cui sono partito.
				if (uo.progr == uoPartenza.progr)
					continue;

				elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuolo(codiceRuolo, uo, uo.al?:new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))
			}
		}

		// infine, risalgo la catena dei padri fintanto che non arrivo al vertice o incontro la suddivisione richiesta:
		while (uoPadre != null) {
			elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuolo(codiceRuolo, uoPadre, uoPadre.al?:new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))

			if (uoPadre.suddivisione.id == idSuddivisione) {
				break;
			}
			uoPadre = uoPadre.getUnitaPubbPadre(uoPadre.al?:new Date())
		}

		return elencoComponenti
	}

	public List<So4ComponentePubb> getComponentiInUnita (So4UnitaPubb unitaSo4) {
		return So4ComponentePubb.componentiUnitaPubb (unitaSo4.progr, unitaSo4.ottica.codice, unitaSo4.al?:new Date()).list();
	}

	/**
	 * Ritorna tutti i componenti che hanno un certo ruolo a partire dalle unità figlie dell'unità richiesta fino in fondo alla struttura.
	 *
	 * Documentazione Filippo:
	 * f) Consentire la selezione dei funzionari ( componenti con il ruolo in input ) di tutta la struttura organizzativa
	 * tenendo come riferimento l'unità di appartenenza del firmatario ( componente in input ) e gestendo la selezione
	 * dei funzionari dalle unità di 1 livello inferiore rispetto al firmatario fino all'ultimo livello della struttura
	 * (in questo caso, essendo la gestione del funzionario strettamente legata al firmatario, non potrà essere consentita
	 * la selezione del funzionario fino a quando il campo del firmatario non è stato riempito).
	 *
	 * @param codiceRuolo	il codice del ruolo
	 * @param progrUo		il progressivo della uo
	 * @param codiceOttica	il codice dell'ottica
	 * @param dataDal		data dell'unità
	 * @return 				elenco dei componenti che hanno un certo ruolo a partire dalle unità figlie dell'unità richiesta fino in fondo alla struttura.
	 */
	public List<So4ComponentePubb> getComponentiConRuoloInUnitaFiglie (String codiceRuolo, long progrUo, String codiceOttica, Date dataDal) {
		// per prima cosa ottengo l'uo di partenza:
		So4UnitaPubb uoPartenza = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

		// ottengo i componenti dell'unità su cui sono
		List<So4ComponentePubb> elencoComponenti = []
		elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuolo(codiceRuolo, uoPartenza, uoPartenza.al?:new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))

		// ciclo sulle unità figlie e le "visito" per ottenere tutti i relativi componenti e unità figlie
		List<So4UnitaPubb> unitaFiglie = uoPartenza.getUnitaPubbFiglie(uoPartenza.al?:new Date())
		for (So4UnitaPubb uo : unitaFiglie) {
			visitDepthFirst(uo, -1) { So4UnitaPubb unita ->
				elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuolo(codiceRuolo, unita, unita.al?:new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))
			}
		}
		return elencoComponenti
	}

	/**
	 * Ritorna tutti i componenti nell'unità scelta e nelle unità figlie.
	 *
	 * @param progrUo		il progressivo della uo
	 * @param codiceOttica	il codice dell'ottica
	 * @param dataDal		data dell'unità
	 * @return 				elenco dei componenti che hanno un certo ruolo a partire dalle unità figlie dell'unità richiesta fino in fondo alla struttura.
	 */
	public List<So4ComponentePubb> getComponentiInUnitaEFiglie (long progrUo, String codiceOttica, Date dataDal) {
		// per prima cosa ottengo l'uo di partenza:
		So4UnitaPubb uoPartenza = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

		// ottengo i componenti dell'unità su cui sono
		List<So4ComponentePubb> elencoComponenti = []
		elencoComponenti.addAll(So4ComponentePubb.componentiUnitaPubb(uoPartenza.progr, codiceOttica, uoPartenza.al?:new Date()).list())

		// ciclo sulle unità figlie e le "visito" per ottenere tutti i relativi componenti e unità figlie
		List<So4UnitaPubb> unitaFiglie = uoPartenza.getUnitaPubbFiglie(uoPartenza.al?:new Date())
		for (So4UnitaPubb uo : unitaFiglie) {
			visitDepthFirst(uo, -1) { So4UnitaPubb unita ->
				elencoComponenti.addAll(So4ComponentePubb.componentiUnitaPubb(unita.progr, codiceOttica, unita.al?:new Date()).list())
			}
		}
		return elencoComponenti
	}

	/**
	 * Ritorna tutti i componenti che hanno un certo ruolo a partire dalle unità figlie dell'unità richiesta fino in fondo alla struttura.
	 *
	 * Documentazione Filippo:
	 * f) Consentire la selezione dei funzionari ( componenti con il ruolo in input ) di tutta la struttura organizzativa
	 * tenendo come riferimento l'unità di appartenenza del firmatario ( componente in input ) e gestendo la selezione
	 * dei funzionari dalle unità di 1 livello inferiore rispetto al firmatario fino all'ultimo livello della struttura
	 * (in questo caso, essendo la gestione del funzionario strettamente legata al firmatario, non potrà essere consentita
	 * la selezione del funzionario fino a quando il campo del firmatario non è stato riempito).
	 *
	 * @param codiceRuolo	il codice del ruolo
	 * @param progrUo		il progressivo della uo
	 * @param codiceOttica	il codice dell'ottica
	 * @param dataDal		data dell'unità
	 * @return 				elenco dei componenti che hanno un certo ruolo a partire dalle unità figlie dell'unità richiesta fino in fondo alla struttura.
	 */
	public List<So4ComponentePubb> getComponentiConRuoliInUnitaFiglie (def ruoli, long progrUo, String codiceOttica, Date dataDal) {
		// per prima cosa ottengo l'uo di partenza:
		So4UnitaPubb uoPartenza = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

		List<So4ComponentePubb> elencoComponenti = []

		visitDepthFirst(uoPartenza, -1) { So4UnitaPubb unita ->
			elencoComponenti.addAll(so4ComponentePubbService.getComponentiUnitaPubbConRuoli(ruoli, unita, unita.al?:new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE))
		}

		return elencoComponenti
	}

	/**
	 * Ritorna l'elenco delle unità figlie a partire dall'unità richiesta.
	 * Data l'unità, il codice dell'ottica, la data dell'unità ed  il livello si risale il ramo della struttura partendo dall'unità data fino al livello.
	 * Una volta raggiunta, si ritornano tutte le unità figlie.
	 *
	 * @param progrUo		il progressivo della uo
	 * @param codiceOttica	il codice dell'ottica
	 * @param dataDal		data dell'unità
	 * @return				elenco delle unità figlie della unità richiesta.
	 */
	public List<So4UnitaPubb> getUnitaFiglieNLivello (long progrUo, String codiceOttica, Date dataDal, int livello = -1) {
		// per prima cosa ottengo l'uo di partenza:
		So4UnitaPubb uoPartenza = So4UnitaPubb.getUnita(progrUo, codiceOttica, dataDal).get()

		List<So4UnitaPubb> elencoUnita = []

		List<So4UnitaPubb> unitaFiglie = uoPartenza.getUnitaPubbFiglie(uoPartenza.al?:new Date())
		for (So4UnitaPubb uo : unitaFiglie) {
			if (livello == 0) {
				return elencoUnita;
			}
			visitDepthFirst(uo, livello-1) { So4UnitaPubb unita ->
				elencoUnita.add(unita)
			}
		}
		return elencoUnita
	}

	/**
	 * Ritorna l'elenco delle unità figlie del ramo della suddivisione a partire dall'unità richiesta.
	 * Data l'unità e il codice suddivisione, si risale il ramo della struttura partendo dall'unità data fino alla suddivisione.
	 * Una volta raggiunta, si ritornano tutte le unità figlie.
	 *
	 * @param uo					unità di partenza
	 * @param codiceSuddivisione	codice della suddivisione
	 * @return						elenco delle unità figlie della suddivisione.
	 */
	List<So4UnitaPubb> getUnitaFiglieSuddivisione (So4UnitaPubb uo, String codiceSuddivisione) {
		// ottengo la suddivisione
		So4SuddivisioneStruttura suddivisione = So4SuddivisioneStruttura.getSuddivisione(codiceSuddivisione, Impostazioni.OTTICA_SO4.valore).get();

		// ottengo l'unità vertice della suddivisione richiesta.
		So4UnitaPubb uoServizio = getUnitaVertice(uo, suddivisione.id)

		// ottengo le unità figlie dell'unità vertice
		List<So4UnitaPubb> uoFiglie = getUnitaFiglieNLivello (uoServizio.progr, uoServizio.ottica.codice, uoServizio.dal)

		return uoFiglie;
	}

	private void visitDepthFirst (So4UnitaPubb unita, int livello, Closure visitor) {
		// prima visito l'unità su cui sono
		visitor (unita)

		// se sono arrivato al livello, esco.
		if (livello == 0) {
			return;
		}

		// poi ricorsivamente agisco sulle figlie, livello per livello.
		List<So4UnitaPubb> unitaFiglie = unita.getUnitaPubbFiglie(unita.al?:new Date())
		for (So4UnitaPubb uo : unitaFiglie) {
			visitDepthFirst(uo, livello-1, visitor)
		}
	}

	So4UnitaPubb getUnitaVertice (So4UnitaPubb unita, long idSuddivisione = -1) {
		while (unita != null && unita.suddivisione.id != idSuddivisione && unita.progrPadre > 0) {
			unita = unita.getUnitaPubbPadre(unita.al?:new Date());
		}
		return unita
	}
}
