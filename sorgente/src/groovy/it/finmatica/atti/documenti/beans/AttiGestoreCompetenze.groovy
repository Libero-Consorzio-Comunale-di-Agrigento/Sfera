package it.finmatica.atti.documenti.beans

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.IDocumentoStorico
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.competenze.*
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.competenze.DocumentoCompetenze
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.IGestoreCompetenze
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgCompetenza
import it.finmatica.so4.login.So4UserDetail
import it.finmatica.so4.login.detail.UnitaOrganizzativa
import org.apache.log4j.Logger
import org.hibernate.FetchMode
import org.zkoss.zk.ui.util.Clients

class AttiGestoreCompetenze implements IGestoreCompetenze {

	private static final Logger log = Logger.getLogger(AttiGestoreCompetenze.class)

	SpringSecurityService 	springSecurityService
	TokenIntegrazioneService tokenIntegrazioneService

	void rimuoviCompetenze(IDocumentoIterabile domainObject,
		WkfTipoOggetto oggetto, Attore attore, boolean lettura,
		boolean modifica, boolean cancellazione, WkfCfgCompetenza comp) {

		if (log.isInfoEnabled()) {
			log.info("Rimuovo la competenza (idCfgComp: ${comp?.id}[${oggetto.codice},${domainObject.TIPO_OGGETTO}]) [${lettura?'lettura':''}, ${modifica?'modifica':''}, ${cancellazione?'cancellazione':''}] all'attore ${attore} sul documento [${domainObject}]");
		}

		if (oggetto.codice == domainObject.TIPO_OGGETTO) {
			// rimuovo le competenze all'oggetto che sta iterando
			rimuoviCompetenzeGenerico(domainObject, attore, lettura, modifica, cancellazione, comp)

			// propago la rimozione delle competenze agli oggetti collegati al documento principale (se questo l'oggetto che sta iterando è un documento principale)
			rimuoviCompetenzeDocumentiCollegati (domainObject, attore, lettura, modifica, cancellazione, comp)

		// qui di seguito invece rimuovo le competenze "collegate" (solo ai documenti validi)
		} else if (oggetto.codice == VistoParere.TIPO_OGGETTO) {
			for (VistoParere v : domainObject.visti) {
				if (v.valido) {
					rimuoviCompetenzeGenerico(v, attore, lettura, modifica, cancellazione, comp)
				}
			}
		} else if (oggetto.codice == Allegato.TIPO_OGGETTO) {
			for (Allegato a : domainObject.allegati) {
				if (a.valido) {
					rimuoviCompetenzeGenerico(a, attore, lettura, modifica, cancellazione, comp)
				}
			}
		} else if (oggetto.codice == Certificato.TIPO_OGGETTO) {
			for (Certificato c : domainObject.certificati) {
				if (c.valido) {
					rimuoviCompetenzeGenerico(c, attore, lettura, modifica, cancellazione, comp)
				}
			}
		} else if (oggetto.codice == Determina.TIPO_OGGETTO && domainObject.hasProperty("determina") &&  domainObject.determina != null) {
			rimuoviCompetenzeGenerico(domainObject.determina, attore, lettura, modifica, cancellazione, comp)

		} else if (oggetto.codice == PropostaDelibera.TIPO_OGGETTO && domainObject.hasProperty("propostaDelibera") && domainObject.propostaDelibera != null) {
			rimuoviCompetenzeGenerico(domainObject.propostaDelibera, attore, lettura, modifica, cancellazione, comp)

		} else if (oggetto.codice == Delibera.TIPO_OGGETTO && domainObject.hasProperty("delibera") && domainObject.delibera != null) {
			rimuoviCompetenzeGenerico(domainObject.delibera, attore, lettura, modifica, cancellazione, comp)
		}
	}

	private void rimuoviCompetenzeDocumentiCollegati (IDocumentoIterabile domainObject, Attore attore, boolean lettura, boolean modifica, boolean cancellazione, WkfCfgCompetenza comp) {
		if (domainObject instanceof VistoParere || domainObject instanceof Certificato) {
			// rimuovo le competenze in lettura al documento principale:
			rimuoviCompetenzeGenerico (domainObject.documentoPrincipale, attore, true, false, false, comp)
		}

		if (domainObject.hasProperty("allegati")) {
			for (Allegato a : domainObject.allegati) {
				rimuoviCompetenzeGenerico(a, attore, true, false, false, comp)
			}
		}

		if (domainObject.hasProperty("visti")) {
			for (VistoParere v : domainObject.visti) {
				rimuoviCompetenzeGenerico(v, attore, true, false, false, comp)
			}
		}

		if (domainObject.hasProperty("certificati")) {
			for (Certificato c : domainObject.certificati) {
				rimuoviCompetenzeGenerico(c, attore, true, false, false, comp)
			}
		}
	}

	void assegnaCompetenze(IDocumentoIterabile domainObject,
		WkfTipoOggetto oggetto, Attore attore, boolean lettura,
		boolean modifica, boolean cancellazione, WkfCfgCompetenza comp) {

		if (log.isInfoEnabled()) {
			log.info("Assegno la competenza (idCfgComp: ${comp?.id}[${oggetto.codice},${domainObject.TIPO_OGGETTO}]) [${lettura?'lettura':''}, ${modifica?'modifica':''}, ${cancellazione?'cancellazione':''}] all'attore ${attore} sul documento [${domainObject}]");
		}

		if (oggetto.codice == domainObject.TIPO_OGGETTO) {
			// do le competenze all'oggetto che sta iterando
			assegnaCompetenzeGenerico(domainObject, attore, lettura, modifica, cancellazione, comp)

			// propago le competenze agli oggetti collegati al documento principale (se questo l'oggetto che sta iterando è un documento principale)
			assegnaCompetenzeDocumentiCollegati (domainObject, attore, lettura, modifica, cancellazione, comp)

		// qui di seguito invece assegno le competenze "collegate" (solo ai documenti validi)
		} else if (oggetto.codice == VistoParere.TIPO_OGGETTO) {
			for (VistoParere v : domainObject.visti) {
				if (v.valido) {
					assegnaCompetenzeGenerico(v, attore, lettura, modifica, cancellazione, comp)
				}
			}
		} else if (oggetto.codice == Allegato.TIPO_OGGETTO) {
			for (Allegato a : domainObject.allegati) {
				if (a.valido) {
					assegnaCompetenzeGenerico(a, attore, lettura, modifica, cancellazione, comp)
				}
			}
		} else if (oggetto.codice == Certificato.TIPO_OGGETTO) {
			for (Certificato c : domainObject.certificati) {
				if (c.valido) {
					assegnaCompetenzeGenerico(c, attore, lettura, modifica, cancellazione, comp)
				}
			}
		} else if (oggetto.codice == Determina.TIPO_OGGETTO && domainObject.hasProperty("determina") &&  domainObject.determina != null) {
			assegnaCompetenzeGenerico(domainObject.determina, attore, lettura, modifica, cancellazione, comp)

		} else if (oggetto.codice == PropostaDelibera.TIPO_OGGETTO && domainObject.hasProperty("propostaDelibera") && domainObject.propostaDelibera != null) {
			assegnaCompetenzeGenerico(domainObject.propostaDelibera, attore, lettura, modifica, cancellazione, comp)

		} else if (oggetto.codice == Delibera.TIPO_OGGETTO && domainObject.hasProperty("delibera") && domainObject.delibera != null) {
			assegnaCompetenzeGenerico(domainObject.delibera, attore, lettura, modifica, cancellazione, comp)
		}
	}

	private void assegnaCompetenzeDocumentiCollegati (IDocumentoIterabile domainObject, Attore attore, boolean lettura, boolean modifica, boolean cancellazione, WkfCfgCompetenza comp) {
		if (domainObject instanceof VistoParere || domainObject instanceof Certificato) {
			// do le competenze in lettura al documento principale:
			assegnaCompetenzeGenerico(domainObject.documentoPrincipale, attore, true, false, false, comp)
			assegnaCompetenzeDocumentiCollegati (domainObject.documentoPrincipale, attore, true, false, false, comp)

		} else if (domainObject instanceof Delibera) {

			// come delibera do le competenze alla proposta
			assegnaCompetenzeGenerico(domainObject.proposta, attore, true, false, false, comp)
			assegnaCompetenzeDocumentiCollegati (domainObject.proposta, attore, true, false, false, comp)
		}

		if (domainObject.hasProperty("allegati")) {
			for (Allegato a : domainObject.allegati) {
				assegnaCompetenzeGenerico(a, attore, true, false, false, comp)
			}
		}

		if (domainObject.hasProperty("visti")) {
			for (VistoParere v : domainObject.visti) {
				assegnaCompetenzeGenerico(v, attore, true, false, false, comp)
			}
		}

		if (domainObject.hasProperty("certificati")) {
			for (Certificato c : domainObject.certificati) {
				assegnaCompetenzeGenerico(c, attore, true, false, false, comp)
			}
		}
	}

	private void rimuoviCompetenzeGenerico (Object d, Attore attore, boolean lettura, boolean modifica, boolean cancellazione, WkfCfgCompetenza comp) {
		if (log.isDebugEnabled()) {
			log.debug("Rimuovo le competenze (idCfgComp: ${comp?.id}) [${lettura?'lettura':''}, ${modifica?'modifica':''}, ${cancellazione?'cancellazione':''}] all'attore ${attore} sul documento ${d}");
		}

		// WARN: assumo di eliminare solo le competenze in modifica, che una volta che le ho in lettura non le posso mai eliminare.
		//		 questa è una condizione molto forte che verrà a cadere con una gestione migliore delle competenze.
		//getCompetenze(d, attore, comp, { eq("modifica", true) })*.delete()

		def competenzeInModifica = getCompetenze(d, attore, comp, { eq("modifica", true) })

		for (def competenza : competenzeInModifica) {
			competenza.modifica = false;
			competenza.save();
		}
	}

	def getCompetenze (Object d, Attore attore, WkfCfgCompetenza comp = null, Closure filtro = null) {
        String propertyName = getCompetenzePropertyName(d)
        Class<?> DomainClassCompetenze = getDomainClassCompetenze(d)

		return DomainClassCompetenze.createCriteria().list {
			eq (propertyName, d)

			if (attore.utenteAd4 != null) {
				eq("utenteAd4", attore.utenteAd4)
			} else {
				isNull("utenteAd4")
			}

			if (attore.ruoloAd4 != null) {
				eq("ruoloAd4", attore.ruoloAd4)
			} else {
				isNull("ruoloAd4")
			}

			if (attore.unitaSo4 != null) {
				eq ("unitaSo4.progr", 			attore.unitaSo4.progr)
				eq ("unitaSo4.ottica.codice", 	attore.unitaSo4.ottica.codice)
				
// RIMUOVO IL DAL (siccome è parte della chiave primaria)				
//						eq ("unitaSo4.dal", 			attore.unitaSo4.dal)
			} else {
				isNull("unitaSo4")
			}

			if (comp != null) {
				eq ("cfgCompetenza", comp) 	// WARN: assumo di eliminare solo le competenze assegnate dal flusso, ignoro le altre.
			}

			if (filtro != null) {
				filtro.delegate = delegate;
				filtro();
			}
		}
	}

	/**
	 * Assegna le competenze al documento per un dato attore.
	 *
	 * Segue la logica:
	 *      1) verifico se ho già una riga di competenza per documento-utente-ruolo-unità
	 *      2) inserisco se non c'è
	 *      3) aggiorno la riga se c'è
	 *      4) è un metodo che "AGGIUNGE" competenze, non "TOGLIE": se quindi viene invocato con lettura:true,modifica:false, e l'utente ha lettura:true,modifica:true, rimarrà uguale. (non verrà tolta modifica)
	 *
	 * @param d             documento su cui aggiornare la competenza
	 * @param attore        l'attore con la tupla documento-utente-ruolo-unità
	 * @param lettura       se true: da' le competenze in lettura
	 * @param modifica      se true: da' le competenze in modifica
	 * @param cancellazione se true: da' le competenze in cancellazione
	 * @param comp          la configurazione che ha generato la competenza da aggiornare
	 */
	private void assegnaCompetenzeGenerico (Object d, Attore attore, boolean lettura, boolean modifica, boolean cancellazione, WkfCfgCompetenza comp) {
        String propertyName = getCompetenzePropertyName(d)
        Class<?> DomainClassCompetenze = getDomainClassCompetenze(d)

		if (log.isDebugEnabled()) {
			log.debug("Assegno le competenze (idCfgComp: ${comp?.id}) [${lettura?'lettura':''}, ${modifica?'modifica':''}, ${cancellazione?'cancellazione':''}] all'attore ${attore} sul documento [${propertyName}:${d.id}]");
		}

		// ottengo tutte le competenze per l'attore
		def competenze = getCompetenze(d, attore);

		if (competenze == null || competenze.size() == 0) {
			competenze = [DomainClassCompetenze.newInstance()]
		}

		// in teoria ne becco una sola, faccio un for che non si sa mai:
		for (def competenza : competenze) {
			competenza."${propertyName}" = d
			competenza.cfgCompetenza 	 = comp?:competenza.cfgCompetenza; 	// se la competenza passata è null, riassegno quella esistente.
																			// faccio così perché quando assegno le competenze dalle notifiche al jwf,
																			// non ho una competenza e passo null. In questo modo, non asfalto quello che già c'è
																			// e che mi serve in fase di eliminazione delle competenze.

			// siccome qui sto solo "dando" delle competenze, do priorità a quelle che ho già dato:
			competenza.lettura       = (lettura 	 || competenza.lettura)
			competenza.modifica      = (modifica 	 || competenza.modifica)
			competenza.cancellazione = (cancellazione|| competenza.cancellazione)

			competenza.utenteAd4     = attore.utenteAd4
			competenza.ruoloAd4      = attore.ruoloAd4
			competenza.unitaSo4  	 = attore.unitaSo4

			competenza.save()
		}
	}

	def getListaCompetenze (def domainObject) {
        String propertyName = getCompetenzePropertyName(domainObject)
        Class<?> DomainClassCompetenze = getDomainClassCompetenze(domainObject)

		return DomainClassCompetenze.createCriteria().list {
			eq (propertyName, domainObject)

			fetchMode("unitaSo4",  FetchMode.JOIN)
			fetchMode("ruoloAd4",  FetchMode.JOIN)
			fetchMode("utenteAd4", FetchMode.JOIN)
		}
	}


	/**
	 * Copia le competenze di un documento su un altro.
	 * Se viene specificato "solaLettura=true" allora verranno copiate tutte le competenze ma verranno messe in sola lettura.
	 *
	 * @param daDocumento il documento di cui copiare le competenze
	 * @param aDocumento  il documento su cui copiare le competenze
	 * @param solaLettura se true le nuove competenze verranno create come sola lettura, altrimenti verranno copiate normalmente.
	 */
	void copiaCompetenze (def daDocumento, def aDocumento, boolean solaLettura) {
		def competenze = getListaCompetenze(daDocumento);
		for (def c : competenze) {
			Attore attore = new Attore(utenteAd4:c.utenteAd4, ruoloAd4:c.ruoloAd4, unitaSo4:c.unitaSo4);
			assegnaCompetenzeGenerico (aDocumento, attore, solaLettura?true:c.lettura, solaLettura?false:c.modifica, solaLettura?false:c.cancellazione, c.cfgCompetenza);
		}
	}

	/**
	 * ritorna una mappa  [lettura: (boolean), modifica: (boolean), cancellazione: (boolean)] con le competenze calcolate sul documento per l'utente corrente.
	 *
	 * @param domainObject oggetto di cui si vogliono controllare le competenze
	 * @return la mappa delle competenze.
	 */
	@Override
	Map<String, Boolean> getCompetenze (IDocumentoIterabile domainObject) {
		return this.internalGetCompetenze(domainObject);
	}

	/**
	 * ritorna una mappa  [lettura: (boolean), modifica: (boolean), cancellazione: (boolean)] con le competenze calcolate sul documento per l'utente corrente.
	 * Funzione utilizzata da oggetti non iterabili (come Allegato)
	 *
	 * @param domainObject oggetto di cui si vogliono controllare le competenze
	 * @return la mappa delle competenze.
	 */
	Map<String, Boolean> getCompetenze (def domainObject, boolean lock = false) {
		return this.internalGetCompetenze(domainObject, lock);
	}

	private Map<String, Boolean> internalGetCompetenze (def domainObject, boolean lock = false) {
		So4UserDetail utente = springSecurityService.principal
		def map = [lettura: false, modifica: false, cancellazione: false]

        String propertyName = getCompetenzePropertyName(domainObject)
        Class<?> DomainClassCompetenze = getDomainClassCompetenze(domainObject)

		def res = DomainClassCompetenze.createCriteria().get  {
			projections {
				"${propertyName}" {
					groupProperty ("id")
				}

				max ("lettura")
				max ("modifica")
				max ("cancellazione")
			}

			eq (propertyName, domainObject)

			AttiGestoreCompetenze.controllaCompetenze(delegate)(utente)
		}

		// l'utente amministratore ha competenze di lettura su tutti i record trovati
		// le altre competenze invece vengono lette da db
		if (AttiUtils.isUtenteAmministratore())
			map.lettura = true
		else
			map.lettura 	= (res != null && res[1])
		map.modifica 		= (res != null && res[2])
		map.cancellazione 	= (res != null && res[3])

		if (Impostazioni.CONCORRENZA_ACCESSO.abilitato && map.modifica) {
			if (tokenIntegrazioneService.isLocked(domainObject)) {
				map.modifica = false;
			} else if (lock) {
				tokenIntegrazioneService.lockDocumento(domainObject)
			}
		}

		return map
	}

	/**
	 * Ritorna l'elenco degli attori che hanno competenze sul documento.
	 * È possibile specificare se si vogliono solo le competenze in lettura, in modifica o tutte.
	 *
	 * @param 	domainObject 	oggetto di cui si vogliono ottenere le competenze
	 * @param 	lettura			se true, indica di selezionare solo le competenze in "lettura" sul documento.
	 * @param	modifica		se true, indica di selezionare solo le competenze in "modifica" sul documento.
	 * @return 	Elenco degli attori che hanno competenza sul documento.
	 */
	List<Attore> getAttoriCompetenze (def domainObject, boolean lettura = false, boolean modifica = false) {
		String propertyName = getCompetenzePropertyName(domainObject)
		Class<?> DomainClassCompetenze = getDomainClassCompetenze(domainObject)

		def competenze = DomainClassCompetenze.createCriteria().list  {
			eq (propertyName, domainObject)
		}

		List<Attore> attori = [];

		for (def competenza : competenze) {
			// aggiungo solo gli attori che hanno le competenze richieste
			if ((lettura  == false || (lettura  && competenza.lettura)) &&
				(modifica == false || (modifica && competenza.modifica))) {
				attori << new Attore(utenteAd4:competenza.utenteAd4, ruoloAd4:competenza.ruoloAd4, unitaSo4:competenza.unitaSo4);
			}
		}

		return attori;
	}

	/**
	 * Criterio di controllo delle competenze:
	 *
	 * 	1) per ogni uo di #delegate (successivamente indicata come #uoiesima) verifico che
	 * 		a) se l'utente non è nullo questo
	 * 				-> sia pari al mio
	 * 		b) non è indicato nè ruolo, nè unità nè utente
	 * 		c) se l'utente è nullo e ruolo o unità sono indicati verifico che
	 * 				-> l'unità indicata è #uoiesima e ho i ruoli indicati
	 * 				-> l'unità è nulla ma ho i ruoli indicati
	 * 				-> l'unità indicata è uoiesima e non ci sono ruoli indicati
	 *
	 * @param delegate
	 * @return
	 */
	static def controllaCompetenze (def delegate, String propertyUtente = "utenteAd4", String propertyUnita = "unitaSo4", String propertyRuolo = "ruoloAd4") {
		def c = { So4UserDetail userDetail ->
			or {
				// se sono l'utente
				eq ("${propertyUtente}.id", userDetail.id)

				// se ho il ruolo con unità null
				and {
					isNull(propertyUnita)
					or {
						for (String codice : userDetail.uo().ruoli.flatten().codice.unique()) {
							eq ("${propertyRuolo}.ruolo", codice)
						}
					}
				}

				for (UnitaOrganizzativa uo : userDetail.uo()) {

					// se ho il ruolo per l'unità
					and {
						and {
							eq ("${propertyUnita}.progr", 			uo.id)
							eq ("${propertyUnita}.ottica.codice", 	uo.ottica)
						}
						or {
							for (def r : uo.ruoli) {
								eq ("${propertyRuolo}.ruolo", 		r.codice)
							}

							// se ho l'unità ma con il ruolo null
							isNull(propertyRuolo)
						}
					}
				}
			}
		}

		c.delegate = delegate
		return c
	}

	/**
	 * @return true se l'utente corrente è abilitato alla visualizzazione dei documenti riservati.
	 */
	boolean utenteCorrenteVedeRiservato (IDocumento atto) {

		if (AttiUtils.isUtenteAmministratore()) {
			return true
		}

		So4UserDetail utente = springSecurityService.principal
		String codiceRuoloDocumentoRiservato = (atto instanceof Determina) ? Impostazioni.RUOLO_SO4_RISERVATO_DETE.valore : Impostazioni.RUOLO_SO4_RISERVATO_DELI.valore

        String propertyName = getCompetenzePropertyName(atto)
		Class<?> DomainClassCompetenze = getDomainClassCompetenze(atto)

		long rowCount = DomainClassCompetenze.createCriteria().get {
			projections {
				rowCount()
			}

			eq (propertyName, atto)
			eq ("lettura", 	  true)

			or {
				// se sono l'utente
				eq ("utenteAd4.id", utente.id)

				// se l'utente ha il ruolo riservato, allora controllo anche se ha gli altri ruoli richiesti
				if (utente.hasRuolo(codiceRuoloDocumentoRiservato)) {

					// se ho il ruolo con unità null
					and {
						isNull("unitaSo4")
						// salto il controllo sul ruolo AGD perché lo hanno tutti:
						// lasciarlo significherebbe dire che possono vedere i documenti riservati tutti coloro
						// che hanno il ruolo AGDRISER in qualsiasi punto della struttura siccome il ruolo AGD viene dato per
						// l'accesso all'applicativo e per dare le competenze a tutti.
						ne ("ruoloAd4.ruolo", Impostazioni.RUOLO_ACCESSO_APPLICATIVO.valore)
						or {
							for (String codice : utente.uo().ruoli.flatten().codice.unique()) {
								eq ("ruoloAd4.ruolo", codice)
							}
						}
					}
				}

				for (UnitaOrganizzativa uo : utente.uo()) {
					// se l'utente ha il ruolo riservato per l'unità, allora controllo anche se ha gli altri ruoli/unità richiesti
					if (utente.hasRuolo(codiceRuoloDocumentoRiservato, uo.id)) {

						// se ho il ruolo per l'unità
						and {
							and {
								eq ("unitaSo4.progr", 			uo.id)
								eq ("unitaSo4.ottica.codice", 	uo.ottica)
							}
							or {
								for (def r : uo.ruoli) {
									eq ("ruoloAd4.ruolo", r.codice)
								}

								// se ho l'unità ma con il ruolo null
								isNull("ruoloAd4")
							}
						}
					}
				}
			}
		}

		return (rowCount > 0);
	}

    /**
     * @return true se l'utente corrente ha il ruolo di visualizzare il file allegato come definito in tipologia
     */
    boolean utenteCorrenteHaRuoloRiservatoInTipologia (IDocumento atto) {

        if (atto instanceof IDocumentoCollegato){
            atto = atto.getDocumentoPrincipale()
        }

        So4UserDetail utente = springSecurityService.principal
        String codiceRuoloDocumentoRiservato = atto.tipologiaDocumento.hasProperty("ruoloRiservato") ? atto.tipologiaDocumento.ruoloRiservato : null;

        if (codiceRuoloDocumentoRiservato) {
            // se l'utente ha il ruolo riservato, allora controllo anche se ha gli altri ruoli richiesti
            if (utente.hasRuolo(codiceRuoloDocumentoRiservato)) {
                return true
            }
        }
        return false;
    }

	boolean controllaRiservato (def documento) {
		def documentoPrincipale = documento;

		if (documento instanceof IDocumentoStorico) {
			documentoPrincipale = documento.getDocumentoOriginale();
		} else if (documento instanceof Allegato) {
			documentoPrincipale = documento.getDocumentoPrincipale();
		}

		// controllo la proprietà "riservato" sul documento effettivo, mentre controllo la competenza sul documentoPrincipale.
		// questo serve in caso stia scaricando un allegato: l'allegato può essere riservato quindi devo valutare se verificarne la riservatezza.
		// però il suo documento principale potrebbe non essere riservato.
		if (documento.hasProperty("riservato") && documento.riservato && !utenteCorrenteVedeRiservato(documentoPrincipale) && !utenteCorrenteHaRuoloRiservatoInTipologia(documentoPrincipale)) {
			Clients.showNotification("Attenzione: l'utente corrente non è abilitato alla visione del testo: il documento è riservato.", Clients.NOTIFICATION_TYPE_ERROR, null, "middle_center", 5000);
			return false
		}

		return true
	}

    // FIXME: metodi come questo che fanno instanceof andrebbero refattorizzati con un metodo all'interno dell'interfaccia IDocumento o simili.
	private Class<?> getDomainClassCompetenze (def documento) {
		if (documento instanceof Determina) {
			return DeterminaCompetenze
		} else if (documento instanceof PropostaDelibera) {
            return PropostaDeliberaCompetenze
        } else if (documento instanceof Delibera) {
            return DeliberaCompetenze
        } else if (documento instanceof VistoParere) {
            return VistoParereCompetenze
        } else if (documento instanceof Allegato) {
            return AllegatoCompetenze
        } else if (documento instanceof Certificato) {
            return CertificatoCompetenze
        } else if (documento instanceof Documento) {
            return DocumentoCompetenze
        }

        throw new AttiRuntimeException("Non ho trovato la classe per la gestione delle competenze per il documento ${documento}")
	}

    // FIXME: metodi come questo che fanno instanceof andrebbero refattorizzati con un metodo all'interno dell'interfaccia IDocumento o simili.
    private String getCompetenzePropertyName (def documento) {
        if (documento instanceof Determina) {
            return "determina"
        } else if (documento instanceof PropostaDelibera) {
            return "propostaDelibera"
        } else if (documento instanceof Delibera) {
            return "delibera"
        } else if (documento instanceof VistoParere) {
            return "vistoParere"
        } else if (documento instanceof Allegato) {
            return "allegato"
        } else if (documento instanceof Certificato) {
            return "certificato"
        } else if (documento instanceof Documento) {
            return "documento"
        }

        throw new AttiRuntimeException("Non ho trovato la classe per la gestione delle competenze per il documento ${documento}")
    }
}
