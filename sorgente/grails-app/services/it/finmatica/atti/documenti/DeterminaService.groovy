package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.IntegrazioneAlbo
import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.RegistroService
import it.finmatica.atti.dizionari.RegistroUnita
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.competenze.VistoParereCompetenze
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CaratteristicaTipologiaService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.CasaDiVetroService
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.so4.login.detail.UnitaOrganizzativa
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import it.finmatica.zkutils.SuccessHandler
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import static it.finmatica.zkutils.LabelUtils.getLabel as l

class DeterminaService {

	SpringSecurityService springSecurityService

	IntegrazioneAlbo		integrazioneAlbo
	RegistroService 		registroService
	WkfIterService 			wkfIterService
	IGestoreFile 			gestoreFile
	IDocumentaleEsterno 	gestoreDocumentaleEsterno
	SuccessHandler 			successHandler
	CasaDiVetroService		casaDiVetroService
	AttiGestoreCompetenze	gestoreCompetenze
	IntegrazioneContabilita integrazioneContabilita
	NotificheService		notificheService
	VistoParereService		vistoParereService
	CertificatoService 		certificatoService
	CaratteristicaTipologiaService caratteristicaTipologiaService
    BudgetService           budgetService

	Allegato getFrontespizio (Determina dete) {
		return Allegato.findByDeterminaAndValidoAndCodice (dete, true, Allegato.ALLEGATO_FRONTESPIZIO)
	}

	List<So4UnitaPubb> getUnitaProponentiDetermine(String codiceAmministrazione) {
		List<So4UnitaPubb> unita = DeterminaSoggetto.createCriteria().list() {
			projections {
				distinct("unitaSo4")
			}
			unitaSo4 {
				eq("amministrazione.codice", codiceAmministrazione)
			}
			eq ("tipoSoggetto.codice", TipoSoggetto.UO_PROPONENTE)
			fetchMode ("unitaSo4", FetchMode.JOIN)
		}
		return unita
	}

	List<Determina> getDetermineFinePubblicazione () {
		return Determina.createCriteria ().list {
			createAlias('iter', 'it', CriteriaSpecification.INNER_JOIN)
			eq ("pubblicaRevoca", false)
			or {
				lt ("dataFinePubblicazione",  new Date())
				lt ("dataFinePubblicazione2", new Date())
			}
			//eq ("stato", StatoDocumento.PUBBLICATO)
			isNull ("it.dataFine")
		}
	}

	List<Determina> getDetermineDaRendereEsecutive () {
		// cerco tutte le determine che hanno la data di pubblicazione diversa da null (cioè sono in pubblicazione).
		// non uso lo "stato del documento" perché in caso di errore questo codice potrebbe essere rilanciato successivamente
		// alla fine della pubblicazione.

		// Il riferimento dei giorni di pubblicazione potrebbe essere dalla data di inizio o fine della pubblicazione
		String proprietaDataPubblicazione = ("INIZIO".equals(Impostazioni.INIZIO_ESECUTIVITA.valore) ? 'dataPubblicazione' : 'dataFinePubblicazione')

		return Determina.executeQuery (
				"""select d
				 from Determina d
				where trunc(d."""+proprietaDataPubblicazione+""" + :giorniEsec) <= current_date()
				  and d."""+proprietaDataPubblicazione+""" is not null
				  and d.valido = true
				  and d.dataEsecutivita is null
				  and d.diventaEsecutiva = true""", [ giorniEsec:		(double)Impostazioni.PUBBLICAZIONE_GIORNI_ESECUTIVITA.valoreInt])
	}

	/**
	 * Rende esecutiva la determina, annulla o integra le determine collegate
	 */
	Determina rendiEsecutiva (Determina determina, Date dataEsecutivita = new Date()) {
		// se la data di esecutività richiesta è successiva ad oggi, non devo rendere esecutiva la determina. Ci penserà il job notturno.
		if (new Date().clearTime().before(dataEsecutivita.clone().clearTime())) {
			return determina
		}

        // se per qualche ragione, la determina è già esecutiva, non faccio niente.
        if (determina.dataEsecutivita != null) {
            return determina
        }

		determina.dataEsecutivita = dataEsecutivita
		determina.stato = StatoDocumento.ESECUTIVO

		// Rendo esecutivi i movimenti contabili
		integrazioneContabilita.rendiEsecutivoAtto (determina)

		// prendo le determine collegate:
		for (DocumentoCollegato documentoCollegato : determina.documentiCollegati) {
			if (documentoCollegato.operazione == DocumentoCollegato.OPERAZIONE_ANNULLA) {
				annullaDetermina (documentoCollegato.determinaCollegata, determina)
				successHandler.addMessage("Atto ${documentoCollegato.determinaCollegata.numeroDetermina} / ${documentoCollegato.determinaCollegata.annoDetermina} ANNULLATO.")

			} else if (documentoCollegato.operazione == DocumentoCollegato.OPERAZIONE_INTEGRA) {
				integraDetermina (documentoCollegato.determinaCollegata)
				successHandler.addMessage("Atto ${documentoCollegato.determinaCollegata.numeroDetermina} / ${documentoCollegato.determinaCollegata.annoDetermina} INTEGRATO.")
			}
		}

        if (determina.eseguibilitaImmediata && determina.tipologiaDocumento.tipoCertImmEseg != null){
            certificatoService.creaCertificato(determina, determina.tipologiaDocumento.tipoCertImmEseg, Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA, false)
        }
		else if (determina.tipologiaDocumento.tipoCertEsec != null) {
			certificatoService.creaCertificato(determina, determina.tipologiaDocumento.tipoCertEsec, Certificato.CERTIFICATO_ESECUTIVITA, false)
		}

		if (!Impostazioni.INTEGRAZIONE_ALBO.isDisabilitato() && determina.idDocumentoAlbo > 0) {
			integrazioneAlbo.aggiornaDataEsecutivita(determina)
		}

        if (Impostazioni.GESTIONE_BUDGET.abilitato){
            budgetService.autorizzaBudget(determina)
        }
		successHandler.addMessage(l("message.determina.esecutiva", determina.dataEsecutivita))
		notificheService.notifica(TipoNotifica.ESECUTIVITA, determina);

		return determina
	}

	/**
	 * Rende la determina NON esecutiva.
	 */
	Determina rendiNonEsecutiva (Determina determina) {
		determina.stato = StatoDocumento.NON_ESECUTIVO
		determina.save()
		notificheService.notifica (TipoNotifica.NON_ESECUTIVITA, determina)
		integrazioneContabilita.annullaProposta (determina)
		return determina
	}

	/**
	 * Numera la Proposta di Determina
	 */
    Determina numeraProposta (Determina determina) {
		if (determina.annoProposta > 0 && determina.numeroProposta > 0) {
			throw new AttiRuntimeException(l("message.determina.propostaConNumeroPresente", determina.id, determina.numeroProposta+" / "+determina.annoProposta))
		}

		if (determina.tipologia.codiceGara && determina.tipologia.codiceGaraObbligatorio && (determina.codiceGara == null || determina.codiceGara?.length() == 0)) {
			throw new AttiRuntimeException("Attenzione, il Codice Identificativo Gara è obbligatorio.")
		}

		String codice = Impostazioni.REGISTRO_PROPOSTE.valore

		registroService.numera (TipoRegistro.findByCodice(codice), { numero, anno, data, registro ->
			determina.numeroProposta 		= numero
			determina.annoProposta	 		= anno
			determina.dataNumeroProposta 	= data
			determina.registroProposta 		= registro.tipoRegistro
		})
		determina.save()

		// informo la contabilità del numero della proposta
		integrazioneContabilita.salvaProposta(determina)

		return determina
	}

	/**
	 * Numera la Determina
	 */
	Determina numeraDetermina (Determina determina) {
		if (determina.annoDetermina > 0 && determina.numeroDetermina > 0) {
            throw new AttiRuntimeException(l("message.determina.determinaConNumeroPresente", determina.id, determina.numeroDetermina+" / "+determina.annoDetermina))
		}

		if (!(determina.annoProposta > 0 && determina.numeroProposta > 0)) {
			throw new AttiRuntimeException(l("message.determina.numerareDeterminaSenzaNumeroProposta"))
		}

		if (determina.tipologia.codiceGara && determina.tipologia.codiceGaraObbligatorio && (determina.codiceGara == null || determina.codiceGara.trim().length() == 0)) {
			throw new AttiRuntimeException("Attenzione, il Codice Identificativo Gara è obbligatorio.")
		}

		TipoRegistro tipoRegistro = null
		if (determina.tipologia.registroUnita) {
			So4UnitaPubb uo = determina.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4

			// prima cerco il registro unità con la caratteristica della tipologia
			tipoRegistro = RegistroUnita.createCriteria().get {
				eq ("unitaSo4", uo)
				eq ("valido", true)
				eq ("caratteristica", determina.tipologia.caratteristicaTipologia)
				delegate.tipoRegistro {
					eq ("valido", 	 true)
					eq ("determina", true)
				}
			}?.tipoRegistro

			// non ho trovato nessun tipo registro per la caratteristica di questa tipologia, rifaccio la ricerca senza caratteristica:
			if (tipoRegistro == null) {
				tipoRegistro = RegistroUnita.createCriteria().get {
					eq ("unitaSo4", uo)
					eq ("valido", true)
					isNull("caratteristica")

					delegate.tipoRegistro {
						eq ("valido", 	 true)
						eq ("determina", true)
					}
				}?.tipoRegistro
			}

		} else {
			tipoRegistro = determina.tipologia.tipoRegistro
		}

		if (tipoRegistro == null) {
			throw new AttiRuntimeException ("Attenzione! Non è stato trovato il registro su cui numerare l'atto! Controllare le impostazioni di tipologia.")
		}

		registroService.numera (tipoRegistro, { numero, anno, data, registro ->
			determina.numeroDetermina 		= numero
			determina.annoDetermina	 		= anno
			determina.dataNumeroDetermina 	= data
			determina.registroDetermina 	= registro.tipoRegistro
		})

		determina.save()

		// informo la contabilità del numero della determina
		integrazioneContabilita.salvaAtto(determina)

		return determina
	}

	/**
	 * Seconda numerazione della Determina
	 */
	Determina numeraDetermina2 (Determina determina) {
		if (determina.annoDetermina2 > 0 && determina.numeroDetermina2 > 0) {
			throw new AttiRuntimeException(l("message.determina.determinaConSecondoNumeroPresente", determina.id, determina.numeroDetermina2 + " / " + determina.annoDetermina2))
		}

		if (!(determina.annoProposta > 0 && determina.numeroProposta > 0)) {
			throw new AttiRuntimeException(l("message.determina.numerareDeterminaSenzaNumeroProposta"))
		}

		if (determina.tipologia.tipoRegistro2 == null) {
			throw new AttiRuntimeException (l("message.determina.mancaSecondoRegistro", determina.tipologia.titolo))
		}

		registroService.numera (determina.tipologia.tipoRegistro2, { numero, anno, data, registro ->
			determina.numeroDetermina2 		= numero
			determina.annoDetermina2	 	= anno
			determina.dataNumeroDetermina2 	= data
			determina.registroDetermina2 	= registro.tipoRegistro
		})

		determina.save()

		return determina
	}

	void annullaProposta (Determina proposta) {
		for (VistoParere v : proposta.visti) {
			if (v.iter != null && v.iter?.dataFine != null && v.valido) {
				wkfIterService.terminaIter(v.iter)
			}
		}

		proposta.stato = StatoDocumento.ANNULLATO
		proposta.save()

		if (Impostazioni.INTEGRAZIONE_GDM.abilitato) {
			gestoreDocumentaleEsterno.salvaDocumento (proposta)
		}

		if (Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato) {
			casaDiVetroService.elimina(proposta)
		}

		if (proposta.numeroProposta > 0) {
		    integrazioneContabilita.annullaProposta (proposta)
		}

        if (Impostazioni.GESTIONE_BUDGET.abilitato){
            budgetService.annullaBudget(proposta);
        }

		// elimino tutte le notifiche di cambio step
        notificheService.eliminaNotifiche(proposta, TipoNotifica.ASSEGNAZIONE)
        // elimino tutte le "altre" notifiche
        notificheService.eliminaNotifiche(proposta)

        // notifico l'annullamento della proposta
        notificheService.notifica(TipoNotifica.PROPOSTA_ANNULLATA, proposta)
	}

	boolean conVistoContabile (Determina dete) {
		// ritorno true se la determina ha un visto contabile valido
		return vistoParereService.esisteAlmenoUnVisto (dete.visti, null, null, null, true)
	}

	/**
	 * Annulla la determina
	 */
	Determina annullaDetermina (Determina determina, Determina determinaPrincipale) {
        //se la determina è già esecutiva allora deve diventare NON ESECUTIVA, altrimenti ANNULLATA
		if (determina.dataEsecutivita == null)
			determina.stato = StatoDocumento.ANNULLATO;
		else
			determina.stato = StatoDocumento.NON_ESECUTIVO;

		// termino l'iter della determina e dei suoi documenti collegati.

		// TODO: se il documento è in pubblicazione, devo annullare tutto ma non devo chiudere il suo iter.
		// oppure lo devo chiudere? se lo chiudo (come ora) le attività in scrivania "presa visione" rimarranno per sempre
		// perché il pulsante sarà nascosto. Mentre se no lo annullo, il flusso dopo la pubblicazione potrebbe prevedere qualsiasi cosa. QUINDI CHE FACCIO???
		if (determina.iter != null) {
			wkfIterService.terminaIter(determina.iter);
		}

		for (VistoParere v : determina.visti) {
			if (v.iter != null) {
				wkfIterService.terminaIter(v.iter)

				// elimino tutte le notifiche di cambio step
				notificheService.eliminaNotifiche(v, TipoNotifica.ASSEGNAZIONE)
				// elimino tutte le "altre" notifiche
				notificheService.eliminaNotifiche(v)
			}
		}

		for (Certificato c : determina.certificati) {
			if (c.iter != null) {
				wkfIterService.terminaIter(c.iter)

				// elimino tutte le notifiche di cambio step
				notificheService.eliminaNotifiche(c, TipoNotifica.ASSEGNAZIONE)
				// elimino tutte le "altre" notifiche
				notificheService.eliminaNotifiche(c)
			}
		}

		determina.save()

        if (!"N".equals(Impostazioni.INTEGRAZIONE_ALBO.valore) && determinaPrincipale!=null) {
			integrazioneAlbo.annullaAtto(determina, determinaPrincipale)
		}

		if (Impostazioni.INTEGRAZIONE_GDM.abilitato) {
			gestoreDocumentaleEsterno.salvaDocumento (determina)
		}

		if (Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato) {
			casaDiVetroService.elimina(determina);
		}

		// Posso annullare la determina in due momenti differenti:
		// 1) con un visto contabile negativo (e quindi la determina non diventa esecutiva)
		// 2) con un'altra determina dopo che la prima è diventata esecutiva.
		// Elimino i movimenti della proposta sulla contabilità solo se la determina non è esecutiva.
		// In caso di annullamento la determina non devono essere eliminati i movimenti contabili
		//if (conVistoContabile(determina)) {
		//	integrazioneContabilita.annullaProposta (determina);
		//}
		if (Impostazioni.CONTABILITA.valore == "integrazioneContabilitaCe4") {
			integrazioneContabilita.annullaProposta (determina);
		} else if (determina.dataEsecutivita == null) {
			integrazioneContabilita.annullaProposta (determina);
		}

        // elimino tutte le notifiche di cambio step
        notificheService.eliminaNotifiche(determina, TipoNotifica.ASSEGNAZIONE)
        // elimino tutte le "altre" notifiche
        notificheService.eliminaNotifiche(determina)

        // notifico l'annullamento o la non esecutività della determina
		if (determina.dataEsecutivita == null)
        	notificheService.notifica(TipoNotifica.ATTO_ANNULLATO, determina)
		else
			notificheService.notifica(TipoNotifica.NON_ESECUTIVITA, determina)

		return determina
	}

	/**
	 * Integra la determina
	 * @param determina
	 * @return
	 */
	Determina integraDetermina (Determina determina) {
		determina.stato = StatoDocumento.INTEGRATO;

		if (Impostazioni.INTEGRAZIONE_GDM.abilitato) {
			gestoreDocumentaleEsterno.salvaDocumento (determina)
		}

		return determina
	}

	Determina duplica (Determina dete, boolean testo, boolean fileAllegato, boolean visti) {
		Determina copia = new Determina()

		copia.tipologia = dete.tipologia
		copia.statoOdg  = StatoOdg.INIZIALE

		// TODO Da testare - copia dei file allegati se deciso in fase di duplicazione
		if (fileAllegato == true) {
			if (dete.testo != null) {
				FileAllegato testoDetermina = dete.testo
				FileAllegato copiaTesto 	= new FileAllegato()
				copiaTesto.nome            	= testoDetermina.nome
				copiaTesto.contentType     	= testoDetermina.contentType
				copiaTesto.dimensione      	= testoDetermina.dimensione
				copiaTesto.testo           	= testoDetermina.testo

				gestoreFile.addFile(copia, copiaTesto, gestoreFile.getFile(dete, dete.testo))
				copia.testo = copiaTesto
			}

			if (dete.stampaUnica != null) {
				FileAllegato stampaUnica 		= dete.stampaUnica
				FileAllegato copiaStampaUnica	= new FileAllegato()
				copiaStampaUnica.nome           = stampaUnica.nome
				copiaStampaUnica.contentType    = stampaUnica.contentType
				copiaStampaUnica.dimensione     = stampaUnica.dimensione
				copiaStampaUnica.testo          = stampaUnica.testo

				gestoreFile.addFile(copia, copiaStampaUnica, gestoreFile.getFile(dete, dete.stampaUnica))
				copia.stampaUnica = copiaStampaUnica
			}
		}

		copia.modelloTesto 				= dete.modelloTesto
		copia.categoria 				= dete.categoria
		copia.commissione 				= dete.commissione
		copia.oggettoSeduta 			= dete.oggettoSeduta

		copia.dataProposta 				= new Date()
		copia.oggetto 					= dete.oggetto

		copia.controlloFunzionario 		= dete.controlloFunzionario
		copia.riservato 				= dete.riservato

		//copia.note 					= dete.note
		//copia.noteTrasmissione 		= dete.noteTrasmissione
		//copia.noteContabili 			= dete.noteContabili

		copia.classificaCodice 			= dete.classificaCodice
		copia.classificaDal 			= dete.classificaDal
		copia.classificaDescrizione 	= dete.classificaDescrizione
		copia.fascicoloAnno 			= dete.fascicoloAnno
		copia.fascicoloNumero 			= dete.fascicoloNumero
		copia.fascicoloOggetto 			= dete.fascicoloOggetto

		copia.pubblicaRevoca 			= dete.pubblicaRevoca
		copia.giorniPubblicazione 		= dete.giorniPubblicazione
		copia.diventaEsecutiva			= dete.diventaEsecutiva

		// TODO: copia dei file allegati da testare.
		if (fileAllegato == true) {
			for (Allegato i in dete.allegati) {
				Allegato temp = new Allegato()

				temp.titolo           =  i.titolo
				temp.descrizione      =  i.descrizione
				temp.tipoAllegato     =  i.tipoAllegato
				temp.statoFirma       =  i.statoFirma
				temp.ubicazione       =  i.ubicazione
				temp.origine          =  i.origine
				temp.quantita	      =  i.quantita
				temp.numPagine	      =  i.numPagine
				temp.sequenza 	      =  i.sequenza
				temp.stampaUnica      =  i.stampaUnica
				temp.riservato	      =  i.riservato
				temp.valido           =  i.valido

				for (FileAllegato al in i.fileAllegati) {
					FileAllegato fileAllegatoIesimo = al
					FileAllegato copiaTesto 	= new FileAllegato()
					copiaTesto.nome            	= fileAllegatoIesimo.nome

					copiaTesto.contentType     	= fileAllegatoIesimo.contentType
					copiaTesto.dimensione      	= fileAllegatoIesimo.dimensione
					copiaTesto.testo           	= fileAllegatoIesimo.testo

					gestoreFile.addFile(temp, copiaTesto, gestoreFile.getFile(i, al))
					temp.addToFileAllegati(copiaTesto)
				}

				copia.addToAllegati(temp)
			}
		}

		/*
		// Al momento non è necessario effettuare la copia dei Destinatari delle notifiche e dei documenti collegati
		// TODO Da testare
		for (DestinatarioNotifica i in dete.destinatariNotifiche) {
			DestinatarioNotifica temp = new DestinatarioNotifica()

			temp.email            = i.email
			temp.utente           = i.utente
			temp.unitaSo4         = i.unitaSo4
			temp.tipoNotifica     = i.tipoNotifica
			temp.tipoDestinatario = i.tipoDestinatario

			copia.addToDestinatariNotifiche(temp)
		}

		for (DocumentoCollegato i in dete.documentiCollegati) {
			DocumentoCollegato detCol	= new DocumentoCollegato()
			detCol.operazione 	        = i.operazione
			detCol.determinaCollegata	= i.determinaCollegata
			detCol.deliberaCollegata	= i.deliberaCollegata

			copia.addToDocumentiCollegati(detCol)
		}
		*/
		copia = copia.save()

		if (visti == true) {
			for (VistoParere i in dete.visti) {
				if (!i.valido) continue;
				VistoParere temp = vistoParereService.creaVistoParere(copia, i.tipologia, i.automatico, i.firmatario, i.unitaSo4)

				// leggo le competenze dei visti parere
				List<VistoParereCompetenze> competenze = VistoParereCompetenze.createCriteria().list() {
					eq("vistoParere.id", i.id)
				}

				for (VistoParereCompetenze j in competenze) {
					VistoParereCompetenze tempComp = new VistoParereCompetenze()
					tempComp.vistoParere			= temp
					tempComp.lettura			    = j.lettura
					tempComp.modifica		    	= j.modifica
					tempComp.cancellazione			= j.cancellazione
					tempComp.utenteAd4          	= j.utenteAd4
					tempComp.ruoloAd4            	= j.ruoloAd4
					tempComp.unitaSo4            	= j.unitaSo4
					tempComp.save()
				}
			}
		}

		// il redattore è l'utente corrente
		As4SoggettoCorrente s = springSecurityService.principal.soggetto
		UnitaOrganizzativa uo = springSecurityService.principal.uo()[0]

		def soggetti = caratteristicaTipologiaService.calcolaSoggetti(copia, copia?.tipologia?.caratteristicaTipologia);
		for (def soggetto : soggetti) {
			if (soggetto.key == TipoSoggetto.REDATTORE) {
				copia.setSoggetto(TipoSoggetto.REDATTORE, s.utenteAd4, So4UnitaPubb.getUnita(uo.id, uo.ottica, uo.dal).get());
			} else {
				copia.setSoggetto(soggetto.key, soggetto.value.utente?.domainObject, soggetto.value.unita?.domainObject)
			}
		}

		// viene ricreato un iter della stesso tipo dell'originale
		def id_iter = WkfCfgIter.getIterIstanziabile(copia?.tipologia?.progressivoCfgIter?:((long)-1)).get()
		wkfIterService.istanziaIter(id_iter, copia);

		//assegna le competenze
		gestoreCompetenze.assegnaCompetenze(copia, WkfTipoOggetto.get(Determina.TIPO_OGGETTO), new Attore(utenteAd4:s.utenteAd4), true, true, false, null)

		return copia
	}

	/**
	 * Recupera la lista di determine da pubblicare
	 */
	List<Determina> getDetermineDaPubblicare () {
		return Determina.createCriteria ().list {
			isNull ("dataPubblicazione")
			eq ("daPubblicare", true)
			le ("dataMinimaPubblicazione",  new Date())
		}
	}
}