package it.finmatica.atti.impostazioni

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Firmatario
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.ISoggettoDocumento
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.CommissioneComponente
import it.finmatica.atti.odg.OggettoPartecipante
import it.finmatica.atti.odg.SedutaPartecipante
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.atti.odg.dizionari.RuoloPartecipante
import it.finmatica.atti.zk.SoggettoDocumento
import it.finmatica.so4.login.detail.UnitaOrganizzativa
import it.finmatica.so4.struttura.So4SuddivisioneStruttura
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubbService
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class RegolaCalcoloService {

	SpringSecurityService 			springSecurityService
	StrutturaOrganizzativaService 	strutturaOrganizzativaService
	So4ComponentePubbService 		so4ComponentePubbService
	PreferenzaUtenteService			preferenzaUtenteService
	
	So4UnitaPubb getUnitaDestinatariaDocumentoPrincipale (VistoParere vistoParere, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return vistoParere.documentoPrincipale.getSoggetto(TipoSoggetto.UO_DESTINATARIA).unitaSo4
	}
	
	def getComponentePresidente (Delibera delibera, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return new So4ComponentePubb(soggetto: getSoggettoPerRuoloPartecipanteInSedutaOCommissione(delibera, RuoloPartecipante.CODICE_PRESIDENTE))
	}
	
	def getComponenteSegretario (Delibera delibera, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return new So4ComponentePubb(soggetto: getSoggettoPerRuoloPartecipanteInSedutaOCommissione(delibera, RuoloPartecipante.CODICE_SEGRETARIO))
	}
	
	def getComponenteDirettoreAmministrativo (Delibera delibera, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return new So4ComponentePubb(soggetto: getSoggettoPerRuoloPartecipanteInSedutaOCommissione(delibera, RuoloPartecipante.CODICE_DIRETTORE_AMMINISTRATIVO))
	}

    def getComponenteDirettoreSanitario (Delibera delibera, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
        return new So4ComponentePubb(soggetto: getSoggettoPerRuoloPartecipanteInSedutaOCommissione(delibera, RuoloPartecipante.CODICE_DIRETTORE_SANITARIO))
    }
	
	def getComponenteDirettoreGenerale (Delibera delibera, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return new So4ComponentePubb(soggetto: getSoggettoPerRuoloPartecipanteInSedutaOCommissione(delibera, RuoloPartecipante.CODICE_DIRETTORE_GENERALE))
	}
	
	def getComponenteDirettoreAmministrativo (VistoParere vistoParere, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return getComponenteDirettoreAmministrativo(vistoParere.documentoPrincipale, soggetti, codiceRuolo, tipoSoggettoPartenza)
	}

	def getComponenteDirettoreSanitario (VistoParere vistoParere, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return getComponenteDirettoreSanitario(vistoParere.documentoPrincipale, soggetti, codiceRuolo, tipoSoggettoPartenza)
	}

	def getComponenteDirettoreGenerale (VistoParere vistoParere, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return getComponenteDirettoreGenerale(vistoParere.documentoPrincipale, soggetti, codiceRuolo, tipoSoggettoPartenza)
	}

    def getComponenteDirettoreSocioSanitario (Delibera delibera, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
        return new So4ComponentePubb(soggetto: getSoggettoPerRuoloPartecipanteInSedutaOCommissione(delibera, RuoloPartecipante.CODICE_DIRETTORE_SOCIO_SANITARIO))
    }

	private As4SoggettoCorrente getSoggettoPerRuoloPartecipanteInSedutaOCommissione (Delibera delibera, String codiceRuoloPartecipante) {
		if (delibera.oggettoSeduta != null) {
			OggettoPartecipante partecipante = OggettoPartecipante.findByOggettoSedutaAndRuoloPartecipanteAndPresente(delibera.oggettoSeduta, RuoloPartecipante.get(codiceRuoloPartecipante), true)
			return partecipante.sedutaPartecipante.componenteEsterno?:partecipante.sedutaPartecipante.commissioneComponente.componente
		}
		
		// se invece non ho l'oggettoSeduta perché la delibera non è passata dall'Ordine del Giorno, recupero il soggetto dalla commissione:
		Commissione commissione = delibera.propostaDelibera.commissione
		if (commissione == null) {
			commissione = delibera.tipologiaDocumento.commissione
		}
		CommissioneComponente componente = CommissioneComponente.findByCommissioneAndRuoloPartecipanteAndValido(commissione, RuoloPartecipante.get(codiceRuoloPartecipante), true)
		return componente.componente
	}

	So4ComponentePubb getComponentePerUtenteCorrente (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def componenti = getListaComponentiPerUtenteConRuoloInOttica(documento, soggetti, codiceRuolo, tipoSoggettoPartenza)

		if (componenti?.size() > 0) {
			return componenti[0]
		} else {
			return null
		}
	}

	List<So4ComponentePubb> getListaComponentiPerUtenteConRuoloInOttica (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return strutturaOrganizzativaService.getComponentiPerUtenteConRuoloInOttica(springSecurityService.principal.id, codiceRuolo, springSecurityService.principal.ottica().codice)
	}

	So4ComponentePubb getComponenteConRuoloInOttica (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def componenti = getListaComponentiConRuoloInOttica(documento, soggetti, codiceRuolo, tipoSoggettoPartenza)

		if (componenti?.size() > 0) {
			componenti.sort { it.nominativoSoggetto }
			return componenti[0]
		} else {
			return null
		}
	}

	List<So4ComponentePubb> getListaComponentiConRuoloInOttica (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		String codiceOttica = springSecurityService.principal.ottica().codice

		return strutturaOrganizzativaService.getComponentiConRuoloInOttica(codiceRuolo, codiceOttica)
	}

	So4ComponentePubb getComponenteConRuoloInUnita (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def componenti = getListaComponentiConRuoloInUnita(documento, soggetti, codiceRuolo, tipoSoggettoPartenza)

		if (componenti?.size() > 0) {
			return componenti[0]
		} else {
			return null
		}
	}

	List<So4ComponentePubb> getListaComponentiConRuoloInUnita (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		if (soggetti[tipoSoggettoPartenza.codice]?.unita == null) {
			return null
		}

		def uo = soggetti[tipoSoggettoPartenza.codice].unita
		return strutturaOrganizzativaService.getComponentiConRuoloInUnita(codiceRuolo, uo.progr, uo.ottica.codice)
	}

	public So4ComponentePubb getComponenteConRuoloInUnitaEUnitaPadri (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def uo = soggetti[tipoSoggettoPartenza.codice]?.unita
		if (uo == null) {
			return null
		}

		def ruoli = [codiceRuolo];
		So4UnitaPubb unita = So4UnitaPubb.getUnita(uo.progr, uo.ottica.codice, uo.dal).get();

		while (unita != null) {
			def componenti = so4ComponentePubbService.getComponentiUnitaPubbConRuoli(ruoli, unita, new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE)
			if (componenti?.size() > 0) {
				return componenti[0];
			}

			unita = unita.getUnitaPubbPadre()
		}

		return null;
	}

	So4ComponentePubb getComponenteConRuoloInUnitaPadri (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def componenti = getListaComponentiConRuoloInUnitaPadri(documento, soggetti, codiceRuolo, tipoSoggettoPartenza)

		if (componenti?.size() > 0) {
			return componenti[0]
		} else {
			return null
		}
	}

	List<So4ComponentePubb> getListaComponentiConRuoloInUnitaPadri (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		if (soggetti[tipoSoggettoPartenza.codice]?.unita == null) {
			return null
		}

		def uo = soggetti[tipoSoggettoPartenza.codice].unita
		return strutturaOrganizzativaService.getComponentiConRuoloInUnitaPadri(codiceRuolo, uo.progr, uo.ottica.codice, uo.dal)
	}

	So4ComponentePubb getComponenteConRuoloInArea  (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def componenti = getListaComponentiConRuoloInArea(documento, soggetti, codiceRuolo, tipoSoggettoPartenza)

		if (componenti?.size() > 0) {
			return componenti[0]
		} else {
			return null
		}
	}

	List<So4ComponentePubb> getListaComponentiConRuoloInArea  (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		if (soggetti[tipoSoggettoPartenza.codice]?.unita == null) {
			return null
		}

		def uo = soggetti[tipoSoggettoPartenza.codice].unita
		So4SuddivisioneStruttura suddivisione = So4SuddivisioneStruttura.getSuddivisione(Impostazioni.SO4_SUDDIVISIONE_AREA.valore, uo.ottica.codice).get()
		
		if (suddivisione == null) {
			throw new AttiRuntimeException ("Non è stato possibile trovare la suddivisione con codice '${Impostazioni.SO4_SUDDIVISIONE_AREA.valore}' per l'ottica ${uo.ottica?.descrizione}")
		}
		
		return strutturaOrganizzativaService.getComponentiConRuoloInSuddivisione([codiceRuolo], uo.progr, uo.ottica.codice, uo.dal, suddivisione.id)
	}

	So4ComponentePubb getResponsabileConRuoloInUnita (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def uo = soggetti[tipoSoggettoPartenza.codice]?.unita
		if (uo == null) {
			return null
		}

		def ruoli = [Impostazioni.RUOLO_SO4_RESPONSABILE.valore, codiceRuolo];
		So4UnitaPubb unita = So4UnitaPubb.getUnita(uo.progr, uo.ottica.codice, uo.dal).get();

		while (unita != null) {
			def componenti = so4ComponentePubbService.getComponentiUnitaPubbConRuoli(ruoli, unita, new Date(), StrutturaOrganizzativaService.ASSEGNAZIONE_PREVALENTE, StrutturaOrganizzativaService.TIPO_ASSEGNAZIONE)
			if (componenti?.size() > 0) {
				return componenti[0];
			}

			unita = unita.getUnitaPubbPadre()
		}

		return null;
	}

	So4ComponentePubb getResponsabileConRuoloInOttica (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		String ruolo = Impostazioni.RUOLO_SO4_RESPONSABILE.valore;
		String codiceOttica = springSecurityService.principal.ottica().codice;
		List<So4ComponentePubb> c = strutturaOrganizzativaService.getComponentiConRuoliInOttica([ruolo, codiceRuolo], codiceOttica);
		if (c?.size() > 0) {
			return c[0];
		}

		return null;
	}

	So4ComponentePubb getComponenteCertificatoEsecutivita (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		String ruolo = Impostazioni.RUOLO_SO4_FIRMATARIO_CERT_ESEC.valore;
		String codiceOttica = springSecurityService.principal.ottica().codice;
		List<So4ComponentePubb> c = strutturaOrganizzativaService.getComponentiConRuoloInOttica(ruolo, codiceOttica);
		if (c?.size() > 0) {
			return c[0];
		}

		return null;
	}

	So4ComponentePubb getFirmatarioDecretiInOttica (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		String ruolo = Impostazioni.RUOLO_SO4_FIRMATARIO_DECRETI.valore;
		String codiceOttica = springSecurityService.principal.ottica().codice;
		List<So4ComponentePubb> c = strutturaOrganizzativaService.getComponentiConRuoloInOttica(ruolo, codiceOttica);
		if (c?.size() > 0) {
			return c[0];
		}

		return null;
	}

	So4ComponentePubb getComponenteConRuoloInUnitaFiglie (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def componenti = getListaComponentiConRuoloInUnitaFiglie(documento, soggetti, codiceRuolo, tipoSoggettoPartenza)

		if (componenti?.size() > 0) {
			return componenti[0]
		} else {
			return null
		}
	}

	List<So4ComponentePubb> getListaComponentiConRuoloInUnitaFiglie  (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		if (soggetti[tipoSoggettoPartenza.codice]?.unita == null) {
			return null
		}

		def uo = soggetti[tipoSoggettoPartenza.codice].unita
		return strutturaOrganizzativaService.getComponentiConRuoloInUnitaFiglie(codiceRuolo, uo.progr, uo.ottica.codice, uo.dal)
	}

	So4UnitaPubb getUnitaOttica (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def unita = getListaUnitaOttica(documento, soggetti, codiceRuolo, tipoSoggettoPartenza)

		if (unita?.size() > 0) {
			return unita[0]
		} else {
			return null
		}
	}

	List<So4UnitaPubb> getListaUnitaOttica (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return strutturaOrganizzativaService.getUnitaInOttica(springSecurityService.principal.ottica().codice)
	}

	So4UnitaPubb getUnitaSoggetto (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return soggetti[tipoSoggettoPartenza.codice]?.unita?.domainObject
	}

	So4UnitaPubb getUnitaPreferitaSoggetto (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		if (soggetti[tipoSoggettoPartenza.codice]?.utente == null) {
			return null
		}
		String preferita = preferenzaUtenteService.getPreferenzaUtente(Preferenza.UNITA_DEFAULT, soggetti[tipoSoggettoPartenza.codice]?.utente.domainObject)
		if (preferita != null) {
			So4UnitaPubb unita = So4UnitaPubb.getUnita(Long.parseLong(preferita), springSecurityService.principal.ottica().codice).get()
			if (unita != null) {
				return unita
			}
		}
		return soggetti[tipoSoggettoPartenza.codice]?.unita?.domainObject
	}

	List<So4UnitaPubb> getListaUnitaSoggetto (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		if (soggetti[tipoSoggettoPartenza.codice]?.utente == null) {
			return null
		}
		return strutturaOrganizzativaService.getUnitaUtente(soggetti[tipoSoggettoPartenza.codice].utente.id, springSecurityService.principal.ottica().codice)
	}

	So4UnitaPubb getUnitaSoggettoConRuolo (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def componenti = getListaUnitaSoggettoConRuolo(documento, soggetti, codiceRuolo, tipoSoggettoPartenza)

		if (componenti?.size() > 0) {
			return componenti[0]
		} else {
			return null
		}
	}

	List<So4UnitaPubb> getListaUnitaSoggettoConRuolo (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		if (soggetti[tipoSoggettoPartenza.codice]?.utente == null) {
			return null
		}
		return strutturaOrganizzativaService.getUnitaUtenteConRuolo(soggetti[tipoSoggettoPartenza.codice].utente.id, codiceRuolo, springSecurityService.principal.ottica().codice)
	}

	List<So4UnitaPubb> getListaUnitaFiglie (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def uo = soggetti[tipoSoggettoPartenza.codice]?.unita
		if (uo == null) {
			return null
		}

		// come prima unità ci metto l'unità da cui parto così che l'utente possa tornare indietro nel caso scegliesse prima una unità figlia.
		List<So4UnitaPubb> listaUnita = [So4UnitaPubb.getUnita(uo.progr, uo.ottica.codice, uo.dal).get()]
		listaUnita.addAll(strutturaOrganizzativaService.getUnitaFiglieNLivello (uo.progr, uo.ottica.codice, uo.dal));
		return listaUnita;
	}

	So4UnitaPubb getUnitaArea (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def uo = soggetti[tipoSoggettoPartenza.codice]?.unita
		if (uo == null) {
			return null
		}

		So4SuddivisioneStruttura suddivisione = So4SuddivisioneStruttura.findByCodiceAndOttica(Impostazioni.SO4_SUDDIVISIONE_AREA.valore, springSecurityService.principal.ottica)

		So4UnitaPubb unita = So4UnitaPubb.getUnita(uo.progr, uo.ottica.codice, uo.dal).get();
		while (unita != null && unita.suddivisione.id != suddivisione.id) {
			unita = unita.getUnitaPubbPadre()
		}

		return unita;
	}

	So4UnitaPubb getUnitaPreferitaArea (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def uo = getUnitaPreferitaSoggetto(documento, soggetti, codiceRuolo, tipoSoggettoPartenza)
		if (uo == null) {
			return null
		}

		So4SuddivisioneStruttura suddivisione = So4SuddivisioneStruttura.findByCodiceAndOttica(Impostazioni.SO4_SUDDIVISIONE_AREA.valore, springSecurityService.principal.ottica)

		So4UnitaPubb unita = So4UnitaPubb.getUnita(uo.progr, uo.ottica.codice, uo.dal).get();
		while (unita != null && unita.suddivisione.id != suddivisione.id) {
			unita = unita.getUnitaPubbPadre()
		}

		return unita;
	}

	List<So4UnitaPubb> getUnitaPadriUtenteCorrente (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		List<UnitaOrganizzativa> unitaUtente = springSecurityService.principal.uo()
		List<So4UnitaPubb> unitaPadri = [];
		for (UnitaOrganizzativa uo : unitaUtente) {
			unitaPadri.addAll(strutturaOrganizzativaService.getUnitaPadri(uo.id, uo.ottica, uo.dal))
		}

		return unitaPadri.unique(true) { it.id }
	}

	/**
	 * Ritorna l'unità proponente del documento padre.
	 *
	 * ATTENZIONE: il nome di questo metodo è importante ed è usato nella vistoParereService.allineaUnitaDocumentoPrincipale
	 *
	 * @param documento
	 * @param soggetti
	 * @param codiceRuolo
	 * @param tipoSoggettoPartenza
	 * @return
	 */
	So4UnitaPubb getUnitaProponenteDocumentoPrincipale (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def documentoPrincipale = documento.documentoPrincipale

		// L'unità Proponente è della "proposta" non dell'"atto". Quindi se il documento che ho è un atto, accedo alla sua proposta.
		// Questo serve in particolare per la Delibera che non ha l'UO_PROPONENTE tra i suoi soggetti.
		if (documentoPrincipale instanceof IAtto) {
			documentoPrincipale = documentoPrincipale.proposta;
		}
		
		return documentoPrincipale.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4;
	}

	/**
	 * Ritorna l'unità proponente del documento padre.
	 *
	 * @param documento
	 * @param soggetti
	 * @param codiceRuolo
	 * @param tipoSoggettoPartenza
	 * @return
	 */
	So4UnitaPubb getUnitaFunzionarioDocumentoPrincipale (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return documento.documentoPrincipale.getSoggetto(TipoSoggetto.FUNZIONARIO)?.unitaSo4
	}

	/**
	 * Ritorna il dirigente del documento padre.
     *
     * ATTENZIONE: il nome di questo metodo è importante perché usato nella vistoParereService.allineaFirmatarioDocumentoPrincipale
	 *
	 * @param documento
	 * @param soggetti
	 * @param codiceRuolo
	 * @param tipoSoggettoPartenza
	 * @return
	 */
	So4ComponentePubb getDirigenteDocumentoPrincipale (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return getSoggettoDocumentoPrincipale(documento, soggetti, codiceRuolo, tipoSoggettoPartenza, TipoSoggetto.DIRIGENTE)
	}

	/**
	 * Ritorna il funzionario del documento padre.
	 *
	 * @param documento
	 * @param soggetti
	 * @param codiceRuolo
	 * @param tipoSoggettoPartenza
	 * @return
	 */
	So4ComponentePubb getFunzionarioDocumentoPrincipale (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return getSoggettoDocumentoPrincipale(documento, soggetti, codiceRuolo, tipoSoggettoPartenza, TipoSoggetto.FUNZIONARIO)
	}

	/**
	 * Ritorna l'incaricato del documento padre.
	 *
	 * @param documento
	 * @param soggetti
	 * @param codiceRuolo
	 * @param tipoSoggettoPartenza
	 * @return
	 */
	So4ComponentePubb getIncaricatoDocumentoPrincipale (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		return getSoggettoDocumentoPrincipale(documento, soggetti, codiceRuolo, tipoSoggettoPartenza, TipoSoggetto.INCARICATO)
	}

	private So4ComponentePubb getSoggettoDocumentoPrincipale (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza, String tipoSoggettoDocumentoPrincipale) {
		ISoggettoDocumento dirigente = documento.documentoPrincipale.getSoggetto(tipoSoggettoDocumentoPrincipale)

		if (dirigente == null) {
			return null;
		}

		As4SoggettoCorrente sogg = As4SoggettoCorrente.findByUtenteAd4(dirigente.utenteAd4)

		if (dirigente.unitaSo4 == null) {
			// cerco il componente associato al soggetto (ritorno il primo che trovo a caso, sperando non mi interessi l'unità...)
			return So4ComponentePubb.allaData(new Date()).perOttica(springSecurityService.principal.ottica().codice).findBySoggetto(sogg);
		} else {
			return So4ComponentePubb.allaData(new Date()).perOttica(springSecurityService.principal.ottica().codice).findByProgrUnitaAndSoggetto(dirigente.unitaSo4.progr, sogg);
		}
	}

	List<So4ComponentePubb> getFirmatariSeduta (SedutaStampa sedutaStampa, Map<String, SoggettoDocumento> soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
        List<SedutaPartecipante> firmatari = sedutaStampa.seduta.partecipanti.findAll { it.firmatario }.sort { it.sequenzaFirma }
        List<So4ComponentePubb> utentiFirmatari = []
        for (SedutaPartecipante firmatario : firmatari) {
            So4ComponentePubb componentePubb = So4ComponentePubb.findBySoggettoAndOttica(firmatario.componenteEsterno?:firmatario.commissioneComponente.componente, springSecurityService.principal.ottica)
            utentiFirmatari << componentePubb
        }
        return utentiFirmatari
	}

	/**
	 * Ritorna il relatore della proposta
	 *
	 * @param documento				il documento
	 * @param soggetti				i soggetti
	 * @param codiceRuolo			il codice del ruolo che il componente deve avere
	 * @param tipoSoggettoPartenza	il soggetto di partenza (ne viene usata l'unità)
	 * @return						il componente con il ruolo richiesto
	 */
	So4ComponentePubb getComponenteRelatore (PropostaDelibera proposta, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		// il relatore dev'essere inserito in struttura.
		As4SoggettoCorrente assessore = proposta.delega?.assessore
		So4ComponentePubb componente = So4ComponentePubb.findBySoggetto(assessore);
		return componente;
	}

	/**
	 * Ritorna il componente con il ruolo specificato all'interno della sola unità "AREA" del soggetto di partenza.
	 *
	 * @param documento				il documento
	 * @param soggetti				i soggetti
	 * @param codiceRuolo			il codice del ruolo che il componente deve avere
	 * @param tipoSoggettoPartenza	il soggetto di partenza (ne viene usata l'unità)
	 * @return						il componente con il ruolo richiesto
	 */
	So4ComponentePubb getComponenteConRuoloInAreaPadre (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def uo = soggetti[tipoSoggettoPartenza.codice]?.unita
		if (uo == null) {
			return null
		}

		So4UnitaPubb unita = So4UnitaPubb.getUnita(uo.progr, uo.ottica.codice, uo.dal).get();
		So4UnitaPubb area  = strutturaOrganizzativaService.getUnitaVertice(unita);

		List<So4ComponentePubb> componenti = strutturaOrganizzativaService.getComponentiConRuoloInUnita(codiceRuolo, area.progr, area.ottica.codice)
		return (componenti.size() > 0)? componenti[0] : null
	}


	/**
	 * Ritorna l'unità "SERVIZIO" che fa capo all'unità con assegnazione prevalente dell'utente di riferimento.
	 *
	 * @param documento				il documento
	 * @param soggetti				i soggetti
	 * @param codiceRuolo			il codice del ruolo
	 * @param tipoSoggettoPartenza	il soggetto di partenza (ne viene utilizzato l'utente)
	 * @return	l'unità "SERVIZIO"
	 */
	So4UnitaPubb getUnitaServizio (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def utente = soggetti[tipoSoggettoPartenza.codice]?.utente;
		if (utente == null) {
			return null
		}

		// in teoria dovrebbe trovarne una sola, ma so4 potrebbe non essere super-corretto.
		List<So4UnitaPubb> listaUnita = strutturaOrganizzativaService.getUnitaUtenteConRuolo(utente.id, codiceRuolo, springSecurityService.principal.ottica().codice, new Date(), "1")

		if (listaUnita.size() == 0) {
			return null;
		}

		// altrimenti prendo la prima e risalgo la catena fino a trovare il servizio.
		So4UnitaPubb unita = listaUnita[0]
		String codiceSuddivisione = Impostazioni.SO4_SUDDIVISIONE_SERVIZIO.valore;
		while (unita != null && unita.suddivisione.codice != codiceSuddivisione) {
			unita = unita.getUnitaPubbPadre();
		}

		return unita;
	}

	/**
	 * Ritorna l'unità "SERVIZIO" che fa capo all'unità preferita con assegnazione prevalente dell'utente di riferimento.
	 *
	 * @param documento				il documento
	 * @param soggetti				i soggetti
	 * @param codiceRuolo			il codice del ruolo
	 * @param tipoSoggettoPartenza	il soggetto di partenza (ne viene utilizzato l'utente)
	 * @return	l'unità "SERVIZIO"
	 */
	So4UnitaPubb getUnitaPreferitaServizio (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def utente = soggetti[tipoSoggettoPartenza.codice]?.utente;
		if (utente == null) {
			return null
		}

		// altrimenti prendo la prima e risalgo la catena fino a trovare il servizio.
		So4UnitaPubb unita = getUnitaPreferitaSoggetto(documento, soggetti, codiceRuolo, tipoSoggettoPartenza)
		if (unita == null){
			return null
		}
		String codiceSuddivisione = Impostazioni.SO4_SUDDIVISIONE_SERVIZIO.valore;
		while (unita != null && unita.suddivisione.codice != codiceSuddivisione) {
			unita = unita.getUnitaPubbPadre();
		}

		return unita;
	}

	/**
	 * Ritorna le unità "SERVIZIO" che fanno capo all'unità con assegnazione prevalente dell'utente di riferimento.
	 *
	 * @param documento				il documento
	 * @param soggetti				i soggetti
	 * @param codiceRuolo			il codice del ruolo
	 * @param tipoSoggettoPartenza	il soggetto di partenza (ne viene utilizzato l'utente)
	 * @return	l'unità "SERVIZIO"
	 */
	List<So4UnitaPubb> getListaUnitaServizio (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def utente = soggetti[tipoSoggettoPartenza.codice]?.utente;
		if (utente == null) {
			return null
		}

		// cerco tutte le unità per cui l'utente ha il ruolo richiesto
		List<So4UnitaPubb> listaUnita = strutturaOrganizzativaService.getUnitaUtenteConRuolo(utente.id, codiceRuolo, springSecurityService.principal.ottica().codice, new Date())
		List<So4UnitaPubb> listaServizi = [];

		// altrimenti prendo la prima e risalgo la catena fino a trovare il servizio.
		String codiceSuddivisione = Impostazioni.SO4_SUDDIVISIONE_SERVIZIO.valore;
		String codiceArea = Impostazioni.SO4_SUDDIVISIONE_AREA.valore;

		// per ogni unità che ho trovato, cerco il relativo servizio
		for (So4UnitaPubb uo : listaUnita) {
			So4UnitaPubb unita = uo;

			// se appartengo ad un'area, la aggiungo e passo ad una nuova unità.
			if (unita.suddivisione.codice == codiceArea) {
				listaServizi.add(unita);
				continue;
			}

			// cerco il servizio
			while (unita != null && unita.suddivisione.codice != codiceSuddivisione ) {
				unita = unita.getUnitaPubbPadre();
			}

			// se lo trovo, aggiungo il servizio
			if (unita != null) {
				listaServizi.add(unita);
			}
		}

		// elimino eventuali doppi:
		return listaServizi.unique { it.progr };
	}

	/**
	 * Ritorna tutte le unità che appartengono all'ottica che hanno almeno un componente diretto con il ruolo indicato.
	 *
	 * @param documento				il documento
	 * @param soggetti				i soggetti	(non viene usato)
	 * @param codiceRuolo			il codice del ruolo
	 * @param tipoSoggettoPartenza	il soggetto di partenza (non viene usato)
	 * @return elenco di unità che hanno almeno un componente diretto con un certo ruolo.
	 */
	List<So4UnitaPubb> getListaUnitaConRuolo (def documento, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		List<So4UnitaPubb> listaUnita = strutturaOrganizzativaService.getUnitaConRuolo(codiceRuolo, springSecurityService.principal.ottica().codice, new Date())

		return listaUnita;
	}
}
