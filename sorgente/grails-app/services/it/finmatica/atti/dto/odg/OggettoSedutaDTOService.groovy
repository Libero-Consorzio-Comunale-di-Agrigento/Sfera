package it.finmatica.atti.dto.odg

import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dto.dizionari.DelegaDTO
import it.finmatica.atti.dto.odg.dizionari.EsitoDTO
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import org.apache.commons.collections.comparators.NullComparator
import org.hibernate.FetchMode

class OggettoSedutaDTOService {

	OggettoPartecipanteDTOService oggettoPartecipanteDTOService;

	public OggettoSedutaDTO salva (OggettoSedutaDTO oggettoSedutaDTO, List<OggettoPartecipanteDTO> partecipanti) {
		salva(oggettoSedutaDTO);
		for (def p : partecipanti) {
			oggettoPartecipanteDTOService.salva(p);
		}
	}

	public OggettoSedutaDTO salva (OggettoSedutaDTO oggettoSedutaDTO) {
		OggettoSeduta oggettoSeduta 		= oggettoSedutaDTO.domainObject?:new OggettoSeduta()
		oggettoSeduta.propostaDelibera		= oggettoSedutaDTO.propostaDelibera?.domainObject
		oggettoSeduta.determina				= oggettoSedutaDTO.determina?.domainObject
		oggettoSeduta.seduta 				= oggettoSedutaDTO.seduta.domainObject
		oggettoSeduta.sequenzaConvocazione  = oggettoSedutaDTO.sequenzaConvocazione
		oggettoSeduta.sequenzaDiscussione 	= oggettoSedutaDTO.sequenzaDiscussione
		oggettoSeduta.esito 				= oggettoSedutaDTO.esito?.domainObject
		oggettoSeduta.delega 				= oggettoSedutaDTO.delega?.domainObject

		oggettoSeduta.oggettoAggiuntivo 	= oggettoSedutaDTO.oggettoAggiuntivo
		oggettoSeduta.dataDiscussione 		= oggettoSedutaDTO.dataDiscussione
		oggettoSeduta.oraDiscussione 		= oggettoSedutaDTO.oraDiscussione
		oggettoSeduta.eseguibilitaImmediata = oggettoSedutaDTO.eseguibilitaImmediata
		oggettoSeduta.motivazioniEseguibilita = oggettoSedutaDTO.motivazioniEseguibilita
		oggettoSeduta.confermaEsito		 	= oggettoSedutaDTO.confermaEsito
		oggettoSeduta.note 					= oggettoSedutaDTO.note

		oggettoSeduta.save ()

		return oggettoSeduta.toDTO()
	}

	public OggettoSedutaDTO elimina (OggettoSedutaDTO oggettoSedutaDTO) {
		OggettoSeduta.get(oggettoSedutaDTO.id).delete()
		oggettoSedutaDTO.id 	 = -1
		oggettoSedutaDTO.version = 0
		return oggettoSedutaDTO
	}

    public List<OggettoSedutaDTO> ricerca(CommissioneDTO commissione, EsitoDTO esito, Date dal, Date al, Integer numero, String oggetto, DelegaDTO delega, As4SoggettoCorrenteDTO utenteProponente, So4UnitaPubbDTO unitaProponente)  {
		def listaProposteDelibera = ricercaProposteDelibera(commissione, esito, dal, al, numero, oggetto, delega, utenteProponente, unitaProponente)
		def nullHigh = new NullComparator(true)
		listaProposteDelibera.sort{a, b-> nullHigh.compare(a.seduta.dataSeduta, b.seduta.dataSeduta)}
		return listaProposteDelibera
	}

	public List<OggettoSedutaDTO> ricercaProposteDelibera(CommissioneDTO commissione, EsitoDTO esito, Date dal, Date al, Integer numero, String oggetto, DelegaDTO _delega, As4SoggettoCorrenteDTO utenteProponente, So4UnitaPubbDTO unitaProponente) {
		return OggettoSeduta.createCriteria().list() {
			fetchMode("esito", 								FetchMode.JOIN)
			fetchMode("delega", 							FetchMode.JOIN)
			fetchMode("delega.assessore", 					FetchMode.JOIN)
			fetchMode("seduta", 							FetchMode.JOIN)
			fetchMode("seduta.commissione", 				FetchMode.JOIN)
			fetchMode("propostaDelibera", 					FetchMode.JOIN)
			fetchMode("propostaDelibera.delega", 			FetchMode.JOIN)
			fetchMode("propostaDelibera.delega.assessore", 	FetchMode.JOIN)
			
			propostaDelibera {
				isNotNull("id")
			}

			if (numero) {
				propostaDelibera {
					eq ("numeroProposta", numero)
				}
			}

			if (oggetto) {
				propostaDelibera {
					ilike ("oggetto", "%"+oggetto+"%")
				}
			}

			if (dal) {
				seduta {
					ge ("dataSeduta", dal)
				}
			}

			if (al) {
				seduta {
					le ("dataSeduta", al)
				}
			}

			if (_delega)
				eq ("delega.id", _delega.id)

			if (esito)
				eq ("esito.id", esito.id)

			if (commissione) {
				seduta {
					eq ("commissione.id", commissione.id)
				}
			}

			if (utenteProponente) {
				delega {
					eq ("assessore.id", utenteProponente.id)
				}
			}

			if (unitaProponente) {
				propostaDelibera {
					soggetti {
						eq ("unitaSo4.progr", unitaProponente.progr)
					}
				}
			}

		}.toDTO()
	}
}
