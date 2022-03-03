package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.dizionari.Esito
import it.finmatica.atti.odg.dizionari.EsitoStandard

class EsitoDTOService {

    public EsitoDTO salva(EsitoDTO esitoDto) {
		Esito esito 				= Esito.get(esitoDto.id)?:new Esito()
    	if(esito.version != esitoDto.version) {
			throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
		}
		esito.esitoStandard				= EsitoStandard.get(esitoDto.esitoStandard.codice)
    	esito.commissione				= Commissione.get(esitoDto.commissione?.id)
    	esito.commissioneArrivo			= Commissione.get(esitoDto.commissioneArrivo?.id)
    	esito.titolo					= esitoDto.titolo
		esito.descrizione				= esitoDto.descrizione
		esito.valido					= esitoDto.valido
		esito.notificaVerbalizzazione	= esitoDto.notificaVerbalizzazione
		esito.gestioneEsecutivita 		= esitoDto.gestioneEsecutivita
		esito.testoAutomatico 			= esitoDto.testoAutomatico
		esito.progressivoCfgIter		= (esitoDto.progressivoCfgIter > 0 ? esitoDto.progressivoCfgIter : null)
		esito.registroDelibera			= TipoRegistro.get(esitoDto.registroDelibera?.codice)
		esito.save()
		return esito.toDTO()
	}

	public void elimina(EsitoDTO esitoDTO) {
		Esito.get(esitoDTO.id).delete()
	}

	public List<EsitoDTO> getListaEsiti (PropostaDelibera proposta = null, long idCommissione = -1) {
		return Esito.createCriteria().list() {
			if (idCommissione > 0) {
				or {
					isNull("commissione")
					eq ("commissione.id", idCommissione)
				}
			}

			// se ho la proposta e la sua tipologia non è adottabile, allora mostro solo gli esiti che NON creano una delibera.
			if (!(proposta?.tipologia?.adottabile?:true)) {
				esitoStandard {
					eq ("creaDelibera", false)
				}
			}

			eq ("valido", true)

			order('titolo', 	 'asc')
			order('descrizione', 'asc')
		}.toDTO()
	}
}

