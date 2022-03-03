package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.RegistroService
import it.finmatica.atti.dizionari.TipoDatoAggiuntivoValore
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.competenze.PropostaDeliberaCompetenze
import it.finmatica.atti.documenti.competenze.VistoParereCompetenze
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CaratteristicaTipologiaService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.so4.login.detail.UnitaOrganizzativa
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

import static it.finmatica.zkutils.LabelUtils.getLabel as l

class PropostaDeliberaService {

	IntegrazioneContabilita integrazioneContabilita
	RegistroService         registroService
	WkfIterService          wkfIterService
	IGestoreFile            gestoreFile
	NotificheService        notificheService
	BudgetService           budgetService
	AttiGestoreCompetenze   gestoreCompetenze
	SpringSecurityService   springSecurityService
	CaratteristicaTipologiaService caratteristicaTipologiaService

    public PropostaDelibera numeraProposta (PropostaDelibera propostaDelibera) {
		if (propostaDelibera.annoProposta > 0 && propostaDelibera.numeroProposta > 0) {
			throw new AttiRuntimeException("Attenzione, documento (${propostaDelibera.id}) con numero già assegnato: ${propostaDelibera.numeroProposta}/${propostaDelibera.annoProposta}.")
		}

		String codice = Impostazioni.REGISTRO_PROPOSTE.valore

		registroService.numera (TipoRegistro.findByCodice(codice), { numero, anno, data, registro ->
			propostaDelibera.numeroProposta 		= numero
			propostaDelibera.annoProposta	 		= anno
			propostaDelibera.dataNumeroProposta 	= data
			propostaDelibera.registroProposta 		= registro.tipoRegistro
		})
		propostaDelibera.save ()
		
		// informo la contabilità del numero della proposta
		integrazioneContabilita.salvaProposta(propostaDelibera)

		return propostaDelibera
	}

	void annullaProposta (PropostaDelibera proposta) {
		if (proposta.oggettoSeduta != null && proposta.oggettoSeduta.esito == null){
			throw new AttiRuntimeException(l("label.annullaPropostaInSeduta.errore"))
		}

		for (VistoParere v : proposta.visti) {
			if (v.iter != null && v.iter?.dataFine != null && v.valido) {
				wkfIterService.terminaIter(v.iter)
			}
		}

		proposta.stato = StatoDocumento.ANNULLATO;
		proposta.save()

		if (proposta.numeroProposta > 0) {
			integrazioneContabilita.annullaProposta(proposta)
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

	PropostaDelibera duplica(PropostaDelibera propDeli, boolean testo, boolean fileAllegato, boolean visti) {
		Ad4Utente utenteSessione = springSecurityService.currentUser // utente di sessione
		PropostaDelibera copia = new PropostaDelibera()

		copia.tipologia = propDeli.tipologia

		copia.stato = propDeli.stato
		copia.statoFirma = propDeli.statoFirma
		copia.statoOdg = propDeli.statoOdg

		// copia dei file allegati se deciso in fase di duplicazione
		if (fileAllegato == true) {
			if (propDeli.testo != null) {
				FileAllegato testoPropostaDelibera = propDeli.testo
				FileAllegato copiaTesto = new FileAllegato()
				copiaTesto.nome            = testoPropostaDelibera.nome

				copiaTesto.contentType     = testoPropostaDelibera.contentType
				copiaTesto.dimensione      = testoPropostaDelibera.dimensione
				copiaTesto.testo           = testoPropostaDelibera.testo

				gestoreFile.addFile(copia, copiaTesto, gestoreFile.getFile(propDeli, propDeli.testo))
				copia.testo = copiaTesto
			}

			if (propDeli.testoOdt != null) {
				FileAllegato testoOdt = propDeli.testoOdt
				FileAllegato copiaStampaUnica 	 = new FileAllegato()
				copiaStampaUnica.nome            = testoOdt.nome
				copiaStampaUnica.contentType     = testoOdt.contentType
				copiaStampaUnica.dimensione      = testoOdt.dimensione
				copiaStampaUnica.testo           = testoOdt.testo
				gestoreFile.addFile(copia, copiaStampaUnica, gestoreFile.getFile(propDeli, propDeli.stampaUnica))
				copia.testoOdt = copiaStampaUnica
			}
		}

		copia.modelloTesto 	= propDeli.modelloTesto

		copia.categoria 	= propDeli.categoria
		copia.commissione 	= propDeli.commissione
		copia.oggettoSeduta = propDeli.oggettoSeduta
		copia.dataProposta 	= new Date()
		copia.oggetto 		= propDeli.oggetto
		copia.dataScadenza  = propDeli.dataScadenza
		copia.motivazione   = propDeli.motivazione
		copia.indirizzo		= propDeli.indirizzo
		copia.daInviareCorteConti = propDeli.daInviareCorteConti

		copia.controlloFunzionario 	= propDeli.controlloFunzionario
		copia.riservato 			= propDeli.riservato
		copia.fuoriSacco 			= propDeli.fuoriSacco
		copia.eseguibilitaImmediata = propDeli.eseguibilitaImmediata
		copia.motivazioniEseguibilita = propDeli.motivazioniEseguibilita

		copia.note 				= propDeli.note
		copia.noteTrasmissione 	= propDeli.noteTrasmissione
		copia.noteContabili 	= propDeli.noteContabili

		copia.classificaCodice 		= propDeli.classificaCodice
		copia.classificaDal 		= propDeli.classificaDal
		copia.classificaDescrizione = propDeli.classificaDescrizione
		copia.fascicoloAnno 		= propDeli.fascicoloAnno
		copia.fascicoloNumero 		= propDeli.fascicoloNumero
		copia.fascicoloOggetto 		= propDeli.fascicoloOggetto

		copia.pubblicaRevoca = propDeli.pubblicaRevoca
		copia.giorniPubblicazione = propDeli.giorniPubblicazione
		copia.dataMinimaPubblicazione = propDeli.dataMinimaPubblicazione

		copia.campiProtetti = propDeli.campiProtetti
		copia.parereRevisoriConti = propDeli.parereRevisoriConti
		copia.delega = propDeli.delega
		copia.codiciVistiTrattati = propDeli.codiciVistiTrattati

		copia.valido = propDeli.valido

		for (PropostaDeliberaSoggetto i in propDeli.soggetti) {
			PropostaDeliberaSoggetto temp = new PropostaDeliberaSoggetto()

			if (i.tipoSoggetto.codice == TipoSoggetto.REDATTORE) {
				temp.tipoSoggetto       = i.tipoSoggetto
				temp.utenteAd4          = utenteSessione
				temp.unitaSo4           = i.unitaSo4
				temp.attivo 			= true
			} else {
				temp.tipoSoggetto       = i.tipoSoggetto
				temp.utenteAd4          = i.utenteAd4
				temp.unitaSo4           = i.unitaSo4
				temp.attivo 			= true
			}

			copia.addToSoggetti(temp)
		}

		if (fileAllegato == true) {

			for (Allegato i in propDeli.allegati) {
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
					FileAllegato copiaTesto = new FileAllegato()
					copiaTesto.nome            = fileAllegatoIesimo.nome

					copiaTesto.contentType     = fileAllegatoIesimo.contentType
					copiaTesto.dimensione      = fileAllegatoIesimo.dimensione
					copiaTesto.testo           = fileAllegatoIesimo.testo

					gestoreFile.addFile(temp, copiaTesto, gestoreFile.getFile(i, al))
					temp.addToFileAllegati(copiaTesto)
				}

				copia.addToAllegati(temp)
			}
		}

		for (DestinatarioNotifica i in propDeli.destinatariNotifiche) {
			DestinatarioNotifica temp = new DestinatarioNotifica()

			temp.email                   = i.email
			temp.utente                  = i.utente
			temp.unitaSo4                = i.unitaSo4
			temp.tipoNotifica            = i.tipoNotifica
			temp.tipoDestinatario        = i.tipoDestinatario

			temp.id = null
			copia.addToDestinatariNotifiche(temp)
		}

		if (visti == true) {
			for (VistoParere i in propDeli.visti) {
				VistoParere temp = new VistoParere()
				temp.tipologia 		= i.tipologia
				temp.firmatario 	= i.firmatario
				temp.unitaSo4 		= i.unitaSo4
				temp.note 			= i.note
				temp.dataAdozione 	= i.dataAdozione
				temp.automatico 	= i.automatico
				temp.esito 			= i.esito
				temp.modelloTesto	= i.modelloTesto

				temp.stato = i.stato
				temp.statoFirma = i.statoFirma

				temp.id = null
				copia.addToVisti(temp)
				copia.save()

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

		for (DatoAggiuntivo d in propDeli.datiAggiuntivi) {
			DatoAggiuntivo temp = new DatoAggiuntivo()
			temp.codice			= d.codice
			temp.valore			= d.valore
			temp.valoreTipoDato = d.valoreTipoDato
			copia.addToDatiAggiuntivi(temp)
		}

		copia = copia.save()

		// viene ricreato un iter della stesso tipo dell'originale
		def id_iter = WkfCfgIter.getIterIstanziabile(copia?.tipologia?.progressivoCfgIter?:((long)-1)).get()
		wkfIterService.istanziaIter(id_iter, copia);

		//assegna le competenze
		gestoreCompetenze.assegnaCompetenze(copia, WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), new Attore(utenteAd4:utenteSessione), true, true, false, null)

		return copia
	}
}
