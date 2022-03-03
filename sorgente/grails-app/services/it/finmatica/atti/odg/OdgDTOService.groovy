package it.finmatica.atti.odg

import it.finmatica.atti.documenti.DocumentoFactory
import it.finmatica.atti.documenti.StatoOdg
import it.finmatica.atti.exceptions.AttiRuntimeException

class OdgDTOService {

	void mandaInOdg (def documentiDto) {
		for (def documentoDto : documentiDto) {
			def domainObject = DocumentoFactory.getDocumento(documentoDto.idDocumento, documentoDto.tipoOggetto)
			StatoOdg.mandaInOdg(domainObject)
			domainObject.save()
		}
	}

	void togliDaOdg (def  documentiDto) {
		for (def documentoDto : documentiDto) {
			def domainObject = DocumentoFactory.getDocumento(documentoDto.idDocumento, documentoDto.tipoOggetto)

			if (domainObject?.oggettoSeduta != null){
				throw new AttiRuntimeException("Impossibile modificare lo stato OdG della proposta ${domainObject.numeroProposta}/${domainObject.annoProposta} poichè risulta inserita in una seduta.");
			}
			StatoOdg.togliDaOdg(domainObject)
			domainObject.save()
		}
	}
}
