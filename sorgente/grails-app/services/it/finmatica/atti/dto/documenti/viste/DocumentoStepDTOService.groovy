package it.finmatica.atti.dto.documenti.viste

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.viste.DocumentoStep
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.so4.login.So4UserDetail
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

import static it.finmatica.zkutils.LabelUtils.getLabel as l

class DocumentoStepDTOService {

	SpringSecurityService springSecurityService
	UserDetailsService    userDetailsService
	AttiFileDownloader    attiFileDownloader
	AttiGestoreCompetenze gestoreCompetenze
	So4DelegaService	  so4DelegaService

	// il campo sort contrario serve ad ordinare i documenti dal più vecchio al più recente
    def inCarico (String search, def tipiOggetto, def tipoRegistro, def statiFirma, int pageSize, int activePage, def orderMap, boolean sortContrario, boolean tutti = false, String unitaProponente = null, Ad4Utente delegante = null) {

		So4UserDetail so4User = null
		// gestiamo le tipologie nel caso in cui ci sia una delega
		def deleghe

		if (delegante != null) {
			so4User = userDetailsService.loadUserByUsername(delegante.nominativo);
			so4User.setAmministrazioneOtticaCorrente(springSecurityService.principal.amm().codice, springSecurityService.principal.ottica().codice)
			deleghe = so4DelegaService.getDeleghe(springSecurityService.currentUser, delegante)?.toDTO()
		}
		// numeri contenuti nella stringa di ricerca
		Integer searchNumbers = search.replaceAll("\\D+", "") != "" ? new Integer(search.replaceAll("\\D+", "")) : null

		// risultato query
		def result = DocumentoStep.createCriteria().list {

			projections {
				groupProperty ("idDocumento")            // 0
				groupProperty ("idPadre")                // 1
				groupProperty ("stato")                  // 2
				groupProperty ("statoFirma")             // 3
				groupProperty ("statoConservazione")     // 4
				groupProperty ("statoOdg")               // 5
				groupProperty ("stepNome")               // 6
				groupProperty ("stepDescrizione")        // 7
				groupProperty ("stepTitolo")             // 8
				groupProperty ("tipoOggetto")            // 9
				groupProperty ("tipoRegistro")           // 10
				groupProperty ("riservato")              // 11
				groupProperty ("oggetto")                // 12
				groupProperty ("unitaProponente")        // 13
				groupProperty ("anno")                   // 14
				groupProperty ("annoProposta")           // 15
				groupProperty ("numero")                 // 16
				groupProperty ("numeroProposta")         // 17
				groupProperty ("idTipologia")            // 18
				groupProperty ("titoloTipologia")        // 19
				groupProperty ("descrizioneTipologia")   // 20
				groupProperty ("dataAdozione")   		 // 21
				groupProperty ("statoVistiPareri")		 // 22
				groupProperty ("dataScadenza")			 // 23
				groupProperty ("priorita")			 	 // 24
				groupProperty ("dataOrdinamento")
			}

			and {
				controllaCompetenze(delegate)(so4User?:springSecurityService.principal)

				if (tipiOggetto != null && tipiOggetto.size() > 0) {
					'in' ("tipoOggetto", tipiOggetto)
				}

				if (tipoRegistro != null && tipoRegistro.size() > 0) {
					'in' ("tipoRegistro", tipoRegistro)
				}

				if (statiFirma != null) {
					'in' ("statoFirma", statiFirma*.toString())
				}

				or {
					isNull ("statoOdg")
					not {
						// escludo i documenti che sono in gestione all'ODG:
						'in' ("statoOdg", [StatoOdg.COMPLETO.toString(), StatoOdg.DA_COMPLETARE.toString(), StatoOdg.INSERITO.toString()])
					}
				}

				or {
					ilike ("oggetto", "%" + search + "%")
					ilike ("titoloTipologia", "%" + search + "%")
					if (searchNumbers != null) {
						eq ("numeroProposta", searchNumbers)
						eq ("numero", searchNumbers)
					}
				}
				
				if (unitaProponente != null){
					eq ("unitaProponente", unitaProponente)
				}

				if (deleghe) {
					or {
						for (So4DelegaDTO delega: deleghe){
							and {
								if (delega.tipologia != null){
									eq ("idTipologia", Long.parseLong(delega.tipologia))
								}
								if (delega.progressivoUnita != null && delega.codiceOttica != null){
									def unita = So4UnitaPubb.getUnita(delega.progressivoUnita, delega.codiceOttica)?.get()?.descrizione;
									if (unita){
										eq ("unitaProponente", unita)
									}

								}
							}
						}
					}

				}
			}
			if (orderMap != null){
				orderMap.each{ k, v -> order k, v }
			} else {
				order("anno",   		sortContrario ? "asc" : "desc")
				order("numero", 		sortContrario ? "asc" : "desc")
				order("annoProposta",   sortContrario ? "asc" : "desc")
				order("numeroProposta", sortContrario ? "asc" : "desc")
			}

			if (! tutti){
				firstResult (pageSize * activePage)
				maxResults  (pageSize)
			}
		}.collect { row -> new DocumentoStepDTO (
				  idDocumento            :row[0]
				, idPadre                :row[1]
				, stato                  :row[2]
				, statoFirma             :row[3]
				, statoConservazione     :row[4]
				, statoOdg               :row[5]
				, stepNome               :row[6]
				, stepDescrizione        :row[7]
				, stepTitolo             :row[8]
				, tipoOggetto            :row[9]
				, tipoRegistro           :row[10]
				, riservato              :row[11]
				, oggetto                :row[12]
				, unitaProponente        :row[13]
				, anno                   :row[14]
				, annoProposta           :row[15]
				, numero                 :row[16]
				, numeroProposta         :row[17]
				, idTipologia            :row[18]
				, titoloTipologia        :row[19]
				, descrizioneTipologia   :row[20]
				, dataAdozione			 :row[21]
				, statoVistiPareri		 :row[22]
				, dataScadenza	 		 :row[23]
				, priorita				 :row[24]
				, dataOrdinamento		 :row[25]
			)
		}
		def exportOptions =   [   idDocumento 			: [label: 'ID', 							index: -1, columnType: 'NUMBER']
								, idPadre				: [label: 'ID Padre', 						index: -1, columnType: 'TEXT']
								, stato 				: [label: 'Stato', 							index: -1, columnType: 'TEXT']
								, statoFirma			: [label: 'Stato Firma',					index: -1, columnType: 'TEXT']
								, statoConservazione	: [label: 'Stato Conservazione',			index: -1, columnType: 'TEXT']
								, statoOdg				: [label: 'Stato Odg',						index: -1, columnType: 'TEXT']
								, stepNome				: [label: 'Step Nome',						index: -1, columnType: 'TEXT']
								, stepDescrizione		: [label: 'Step Descrizione',				index: -1, columnType: 'TEXT']
								, stepTitolo			: [label: 'Step Titolo',					index:  7, columnType: 'TEXT']
								, tipoOggetto			: [label: 'Tipo Oggetto', 					index: -1, columnType: 'TEXT']
								, tipoRegistro 			: [label: 'Tipo Registro',					index: -1, columnType: 'TEXT']
								, riservato 			: [label: 'Riservato',			 			index: -1, columnType: 'TEXT']
								, oggetto				: [label: 'Oggetto', 						index:  5, columnType: 'TEXT']
								, unitaProponente		: [label: 'Unita Proponente', 				index:  6, columnType: 'TEXT']
								, anno					: [label: 'Anno Atto', 						index:  2, columnType: 'NUMBER']
								, annoProposta			: [label:l("label.ricerca.annoProposta"), 	index:  4, columnType: 'NUMBER']
								, numero				: [label: 'Numero Atto', 					index:  1, columnType: 'NUMBER']
								, numeroProposta		: [label:l("label.ricerca.numeroProposta"), index:  3, columnType: 'NUMBER']
								, idTipologia			: [label: 'ID Tipologia', 					index: -1, columnType: 'TEXT']
								, titoloTipologia		: [label: 'Tipologia', 						index:  0, columnType: 'TEXT']
								, descrizioneTipologia	: [label: 'Tipologia', 						index: -1, columnType: 'TEXT']
								, dataAdozione			: [label: 'Data Adozione',					index: -1, columnType: 'TEXT']
								, statoVistiPareri		: [label: 'Stato Visti Pareri',				index: -1, columnType: 'TEXT']
								, dataScadenza			: [label: Impostazioni.RICHIESTA_ESECUTIVITA_LABEL.valore, index: Impostazioni.RICHIESTA_ESECUTIVITA_COLONNA.abilitato?8:-1, columnType: 'DATE', formato:'dd/MM/yyyy']
								, priorita 				: [label: 'Priorita',						index: -1, columnType: 'NUMBER']
								, dataOrdinamento		: [label: 'Data Ordinamento', 				index: -1, columnType: 'TEXT']]
		// totale di righe
		def total = DocumentoStep.createCriteria().count() {

			projections {
				groupProperty ("idDocumento")
			}

			and {
				controllaCompetenze(delegate)(so4User?:springSecurityService.principal)

				if (tipiOggetto != null && tipiOggetto.size() > 0) {
					'in' ("tipoOggetto", tipiOggetto)
				}

				if (tipoRegistro != null && tipoRegistro.size() > 0) {
					'in' ("tipoRegistro", tipoRegistro)
				}

				if (statiFirma != null) {
					'in' ("statoFirma", statiFirma*.toString())
				}

				or {
					isNull ("statoOdg")
					not {
						// escludo i documenti che sono in gestione all'ODG:
						'in' ("statoOdg", [StatoOdg.COMPLETO.toString(), StatoOdg.DA_COMPLETARE.toString(), StatoOdg.INSERITO.toString()])
					}
				}

				or {
					ilike ("oggetto", "%" + search + "%")
					ilike ("titoloTipologia", "%" + search + "%")
					if (searchNumbers != null) {
						eq ("numeroProposta", searchNumbers)
						eq ("numero", searchNumbers)
					}
				}
				
				if (unitaProponente != null){
					eq ("unitaProponente", unitaProponente)
				}

				if (deleghe) {
					or {
						for (So4DelegaDTO delega: deleghe){
							and {
								if (delega.tipologia != null){
									eq ("idTipologia", Long.parseLong(delega.tipologia))
								}
								if (delega.progressivoUnita != null && delega.codiceOttica != null){
									def unita = So4UnitaPubb.getUnita(delega.progressivoUnita, delega.codiceOttica)?.get()?.descrizione;
									if (unita){
										eq ("unitaProponente", unita)
									}

								}
							}
						}
					}

				}
			}
		}

		return [total: total, result: result, exportOptions: exportOptions]
	}


	/**
	 * Criterio di controllo dell'incarico nella wkf_engine_step (appiattita nella vista documenti step):
	 *
	 *  1) utente indicato pari all'utente loggato (chiamato successivamente #delegate)
	 *  2) per ogni uo di #delegate (successivamente indicata come #uoiesima) verifico che
	 * 		a) l'unità indicata è #uoiesima e il ruolo è nullo
	 * 		b) per ogni ruolo che #delegate ha nella #uoiesima (successivamente chiamato #ruoloiesimo) verifico che
	 * 			->  l'unità è nulla o pari a #uoiesima e il ruolo sia pari a #ruoloiesimo
	 *
	 *  NB1: non è consentito avere uo, ruolo, utente tutti nulli per indicare con competenze a tutti altrimenti la query è lentissima
	 *	NB2: query ottimizzata grazie agli indici!
	 *
	 * @param delegate
	 * @return
	 */
	public static def controllaCompetenze (def delegate) {
		return AttiGestoreCompetenze.controllaCompetenze(delegate, "stepUtente", "stepUnita", "stepRuolo");
	}


	/*
	 * METODI PER POPOLARE I MENU POPUP DEGLI ALLEGATI E VISTI DI UN DOCUMENTO
	 */

	public def caricaAllegatiDocumento (long idDocumento, String tipoOggetto) {
		def listaAllegati
		switch (tipoOggetto) {
			case Determina.TIPO_OGGETTO:
				listaAllegati = caricaAllegatiDetermina (idDocumento)
			break
			case Delibera.TIPO_OGGETTO:
				listaAllegati = caricaAllegatiDelibera (idDocumento)
			break
			case PropostaDelibera.TIPO_OGGETTO:
				listaAllegati = caricaAllegatiPropostaDelibera (idDocumento)
			break
			case VistoParere.TIPO_OGGETTO:
			case VistoParere.TIPO_OGGETTO_PARERE:
				listaAllegati = caricaAllegatiVistoParere (idDocumento)
			break
			case Certificato.TIPO_OGGETTO:
				listaAllegati = caricaAllegatiCertificato (idDocumento)
			break
			default:
				throw new AttiRuntimeException ("Attenzione: tipo di documento ${tipoOggetto} non riconosciuto.")
			break
		}

		if (listaAllegati == null) {
			listaAllegati = []
		}

		if (listaAllegati.size() == 0) {
			listaAllegati.add([titolo: "Nessun File", idFileAllegato:-1, idDocumento: -1, classeDoc: null])
		}

		return listaAllegati
	}

	private def caricaAllegatiPropostaDelibera (long idDocumento) {
		def listaAllegati = []
		// ottengo la proposta delibera.
		PropostaDelibera propostaDelibera = PropostaDelibera.get(idDocumento)

		if (propostaDelibera.testo != null && (!propostaDelibera.riservato || gestoreCompetenze.utenteCorrenteVedeRiservato(propostaDelibera))) {
			listaAllegati.add([titolo: "Testo Proposta", idFileAllegato:propostaDelibera.testo.id, idDocumento: propostaDelibera.id, classeDoc: PropostaDelibera])
		}
		
		for (VistoParere vistoParere in propostaDelibera.visti) {
			if (vistoParere.valido && vistoParere.testo != null) {
				listaAllegati.add([titolo:vistoParere.tipologia.titolo, idFileAllegato:vistoParere.testo.id, idDocumento: vistoParere.id, classeDoc: VistoParere])
			}
		}

		return listaAllegati
	}

	private def caricaAllegatiDetermina (long idDocumento) {
		def listaAllegati = []
		
		Determina determina = Determina.get(idDocumento)

		if (determina.testo != null && (!determina.riservato || gestoreCompetenze.utenteCorrenteVedeRiservato(determina))) {
			listaAllegati.add([titolo: "Testo", idFileAllegato:determina.testo.id, idDocumento: determina.id, classeDoc: Determina])
		}

		if (determina.stampaUnica != null) {
			listaAllegati.add([titolo: "Stampa Unica", idFileAllegato:determina.stampaUnica.id, idDocumento: idDocumento, classeDoc: Determina])
		}

		for (VistoParere visto in determina.visti) {
			if (visto.valido && visto.testo != null) {
				listaAllegati.add([titolo:visto.tipologia.titolo, idFileAllegato:visto.testo.id, idDocumento: visto.id, classeDoc: VistoParere])
			}
		}

		return listaAllegati
	}

	private def caricaAllegatiDelibera (long idDocumento) {
		
		// in questo caso particolare idDocumento può essere l'id della delibera o della proposta di delibera.
		// questo avviene perché le query di ricerca di delibera e proposta di delibera, devono mostrare una riga sola per proposta e delibera (se presente)
		
		def listaAllegati = []
		long idPropostaDelibera
		Delibera delibera
		PropostaDelibera propostaDelibera = PropostaDelibera.get(idDocumento)
		if (propostaDelibera) {
    		delibera = Delibera.findByPropostaDelibera(propostaDelibera)
    		idPropostaDelibera = idDocumento
		} else {
    		delibera = Delibera.get(idDocumento)
    		idPropostaDelibera = delibera.propostaDelibera.id
		}
				
		// può succedere che l'utente corrente non abbia competenze in lettura sulla delibera perché questa è stata creata
		// e numerata ma non è ancora pubblicata.
		boolean competenzeInLetturaDelibera = gestoreCompetenze.getCompetenze(delibera).lettura
		if (delibera.testo != null && competenzeInLetturaDelibera && (!delibera.riservato || gestoreCompetenze.utenteCorrenteVedeRiservato(delibera))) {
 			listaAllegati.add([titolo: "Testo", idFileAllegato:delibera.testo.id, idDocumento: delibera.id, classeDoc: Delibera])
 		}
		 
		if (delibera.stampaUnica != null && competenzeInLetturaDelibera) {
			listaAllegati.add([titolo: "Stampa Unica", idFileAllegato:delibera.stampaUnica.id, idDocumento: delibera.id, classeDoc: Delibera])
		}

		listaAllegati.addAll(caricaAllegatiPropostaDelibera(idPropostaDelibera))

		if (competenzeInLetturaDelibera) {
    		for (Certificato c in delibera.certificati) {
    			if (c.valido && c.testo != null) {
    				listaAllegati.add([titolo:c.tipologia.titolo, idFileAllegato:c.testo.id, idDocumento: c.id, classeDoc: Certificato])
    			}
    		}
		}

		return listaAllegati
	}

	private def caricaAllegatiVistoParere (long idDocumento) {
		def listaAllegati = []
		VistoParere visto = VistoParere.get(idDocumento)
		
		if (visto.testo != null) {
			listaAllegati.add([titolo:visto.tipologia.titolo, idFileAllegato:visto.testo.id, idDocumento: visto.id, classeDoc: VistoParere])
		}

		return listaAllegati
	}

	private def caricaAllegatiCertificato (long idDocumento) {
		def listaAllegati = []
		Certificato certificato = Certificato.get(idDocumento)
		if (certificato.testo != null) {
			listaAllegati.add([titolo:certificato.tipologia.titolo, idFileAllegato:certificato.testo.id, idDocumento: idDocumento, classeDoc: Certificato])
		}
		return listaAllegati
	}

	public void downloadFileAllegato (def value) {
		// leggo la classe del documento
		if (value.idFileAllegato == -1) {
			// se viene cliccato nessun file allora non viene restituito nulla
			return
		}
		Class classeDocumento = value.classeDoc
		def documento = classeDocumento.createCriteria().get() {
			eq("id", value.idDocumento)
		}

		FileAllegato fAllegato = FileAllegato.createCriteria().get() {
			eq("id", value.idFileAllegato)
		}

		attiFileDownloader.downloadFileAllegato (documento, fAllegato);
	}
}
