package it.finmatica.atti.dto.documenti.tipologie

import it.finmatica.atti.documenti.tipologie.TipoVistoParere
import it.finmatica.atti.dto.dizionari.DelegaDTOService
import it.finmatica.atti.impostazioni.Impostazioni

class TipoVistoParereDTOService {
	DelegaDTOService delegaDTOService

    public TipoVistoParereDTO salva (TipoVistoParereDTO tipoVistoParereDTO) {
		TipoVistoParere tipoVistoParere = tipoVistoParereDTO.getDomainObject()?:new TipoVistoParere()
		boolean update = tipoVistoParereDTO.id > 0

		tipoVistoParere.valido		 			= tipoVistoParereDTO.valido
		tipoVistoParere.titolo		 			= tipoVistoParereDTO.titolo
		tipoVistoParere.codice		 			= tipoVistoParereDTO.codice
		tipoVistoParere.descrizione 			= tipoVistoParereDTO.descrizione
        tipoVistoParere.descrizioneNotifica     = tipoVistoParereDTO.descrizioneNotifica
		tipoVistoParere.progressivoCfgIter		= tipoVistoParereDTO.progressivoCfgIter
		tipoVistoParere.progressivoCfgIterDelibera	= tipoVistoParereDTO.progressivoCfgIterDelibera
		tipoVistoParere.caratteristicaTipologia = tipoVistoParereDTO.caratteristicaTipologia?.domainObject
		tipoVistoParere.modelloTesto			= tipoVistoParereDTO.modelloTesto?.domainObject
		tipoVistoParere.contabile               = tipoVistoParereDTO.contabile
		tipoVistoParere.conFirma                = tipoVistoParereDTO.conFirma
		tipoVistoParere.conRedazioneUnita       = tipoVistoParereDTO.conRedazioneUnita
		tipoVistoParere.conRedazioneDirigente   = tipoVistoParereDTO.conRedazioneDirigente
		tipoVistoParere.stampaUnica             = tipoVistoParereDTO.stampaUnica
		tipoVistoParere.pubblicazione           = tipoVistoParereDTO.pubblicazione
		tipoVistoParere.unitaDestinatarie		= tipoVistoParereDTO.unitaDestinatarie
		tipoVistoParere.sequenzaStampaUnica     = tipoVistoParereDTO.sequenzaStampaUnica
		tipoVistoParere.testoObbligatorio     	= tipoVistoParereDTO.testoObbligatorio
		tipoVistoParere.pubblicaAllegati		= tipoVistoParereDTO.pubblicaAllegati
		tipoVistoParere.pubblicaAllegatiDefault = tipoVistoParereDTO.pubblicaAllegatiDefault && tipoVistoParereDTO.pubblicaAllegati
        tipoVistoParere.queryMovimenti          = tipoVistoParereDTO.queryMovimenti

		tipoVistoParere.save(failOnError: true)

		if (Impostazioni.DELEGHE.abilitato) delegaDTOService.inserisciTipologiaDelega(tipoVistoParere, update)
		return tipoVistoParere.toDTO()
    }

	public void elimina (TipoVistoParereDTO tipoVistoParereDTO) {
		if (Impostazioni.DELEGHE.abilitato) delegaDTOService.eliminaTipologiaDelega(tipoVistoParereDTO.domainObject)
		tipoVistoParereDTO.domainObject.delete ();
	}

	public TipoVistoParereDTO duplica (TipoVistoParereDTO tipoVistoParereDTO) {
		TipoVistoParere tipoVistoParere = tipoVistoParereDTO.domainObject;
		tipoVistoParereDTO.id = -1;
		tipoVistoParereDTO.version = 0;
		tipoVistoParereDTO.titolo += " (duplica)";
		TipoVistoParere duplica = salva(tipoVistoParereDTO).domainObject;
		return duplica.toDTO();
	}
}
