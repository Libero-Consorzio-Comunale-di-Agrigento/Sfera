package it.finmatica.atti.dto.documenti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.VistoParereService
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.storico.VistoParereStorico
import it.finmatica.atti.dto.documenti.tipologie.TipoVistoParereDTO
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.util.resource.Labels

class VistoParereDTOService {

	SpringSecurityService 	springSecurityService
	VistoParereService    	vistoParereService
	AttiGestioneTesti		gestioneTesti

    public def salva (VistoParereDTO vistoParereDTO) {
		VistoParere vistoParere = vistoParereDTO.getDomainObject()?:new VistoParere()

		// rilascio il lock sul testo (se presente)
		gestioneTesti.uploadEUnlockTesto(vistoParere)

		vistoParere.unitaSo4			= vistoParereDTO.unitaSo4?.domainObject
		vistoParere.firmatario			= vistoParereDTO.firmatario?.domainObject
		vistoParere.note 				= vistoParereDTO.note
		vistoParere.noteTrasmissione	= vistoParereDTO.noteTrasmissione
		vistoParere.esito				= vistoParereDTO.esito
		vistoParere.stato				= vistoParereDTO.stato
		vistoParere.tipologia			= vistoParereDTO.tipologia?.domainObject
		vistoParere.modelloTesto		= vistoParereDTO.tipologia?.modelloTesto?.domainObject
		vistoParere.determina           = vistoParereDTO.determina?.domainObject
		vistoParere.propostaDelibera	= vistoParereDTO.propostaDelibera?.domainObject
		vistoParere.dataOrdinamento		= vistoParereDTO.dataOrdinamento

		vistoParere.save()

		// se sono in creazione del visto, copio le competenze che ha il documento principale e le assegno al visto:
		if (!(vistoParereDTO.id > 0)) {
			vistoParereService.copiaCompetenzeDocumentoPrincipale(vistoParere)
		}

		return vistoParere.toDTO();
    }

	public void elimina (VistoParereDTO vistoParereDTO) {
		vistoParereService.elimina(vistoParereDTO.domainObject);
	}

	public def caricaListaVisti (IProposta proposta) {
		VistoParere.createCriteria().list {
			createAlias("firmatario", "f", CriteriaSpecification.LEFT_JOIN)
			eq ("determina.id", determina.id)
			eq ("valido", true)
			tipologia { order ("codice", "asc") }
			not {
				eq ("stato", StatoDocumento.ANNULLATO)
			}
			fetchMode ("tipologia",  FetchMode.JOIN)
			fetchMode ("unitaSo4",   FetchMode.JOIN)
			fetchMode ("firmatario", FetchMode.JOIN)
		}.toDTO()
	}

	public def caricaStorico (VistoParereDTO vistoParere, DeterminaDTO determinaDto = null, PropostaDeliberaDTO propostaDeliberaDto = null, DeliberaDTO deliberaDto = null) {
		def v = VistoParereStorico.createCriteria().list {
			projections {
				groupProperty ("dateCreated")				     // 0
				utenteIns {
					groupProperty ("nominativoSoggetto")         // 1
				}

				step {
					cfgStep {
						attore {
							groupProperty ("nome")           	 // 2
						}
						groupProperty("titolo")                  // 3
					}
					attori {
						// faccio la "count" degli attori in questo modo
						// ottengo una riga sola per attori "multipli"
						// e nessuna riga se il nodo non ha attori (e quindi non va mostrato nello storico)
						// prima c'era: groupProperty("id") ma così mostrava tante righe per ogni attore nello step
						// (ad es. nel caso dell'attore che trova le unità figlie)
						// se invece tolgo del tutto non va bene perché si vedono anche step da cui il documento non è passato
						// Con la nuova versione del configuratoreiter infatti si possono mettere le condizioni di ingresso al nodo
						// e quello che si ottiene su db è che il passaggio dal nodo viene comunque registrato ma non ne vengono calcolati gli attori)
						count("id")                       		  // 5
					}
				}

				tipologia {
					caratteristicaTipologia {
						groupProperty ("titolo")                 // 6
					}
				}

				groupProperty ("testo.id")                       // 7
				groupProperty ("id")                             // 8
				groupProperty ("esito")                          // 9
				groupProperty ("note")                           // 10
			}

			if (vistoParere != null) {
				eq ("idVistoParere", vistoParere.id)
			}

			if (determinaDto != null) {
				eq ("determina.id", determinaDto.id)
			}

			if (propostaDeliberaDto != null) {
				eq ("propostaDelibera.id", propostaDeliberaDto.id)
			}

			if (deliberaDto != null) {
				eq("delibera.id", deliberaDto.id)
			}

			order ("dateCreated", "asc")
		}.collect { row ->
					[ id: 				row[7]
					, descrizione:		row[5]
					, data: 			row[0]
					, utente: 			row[1]
					, carico: 			row[2]
					, idFileAllegato:	row[6]
					, note:				row[9]
					, titolo: 			"${row[3]}, esito: ${Labels.getLabel("visto.esito."+row[8])}"
					, tipoOggetto:		VistoParere.TIPO_OGGETTO]
		}
	}

	public def aggiungiVistoContabile (def viewModel, TipoVistoParereDTO tipoVisto) {
		VistoParere visto = vistoParereService.creaVistoParere(tipoVisto.getDomainObject(), false)
		def documento = viewModel.getDocumentoIterabile(false)
		documento.addToVisti(visto)
		documento = documento.save()
		return documento
	}

	public def eliminaVistoContabile (def viewModel){
		def documento = viewModel.getDocumentoIterabile(false)

		def listaVistiDaCancellare = VistoParere.createCriteria().list(){
			eq ("valido", true)
			tipologia{
				eq("contabile", true)
			}
			or {
				eq("determina.id", documento.id)
				eq("propostaDelibera.id", documento.id)
			}
		}

		for (i in listaVistiDaCancellare){
			documento.removeFromVisti(i)
		}
		documento = documento.save()
		return documento
	}
}
