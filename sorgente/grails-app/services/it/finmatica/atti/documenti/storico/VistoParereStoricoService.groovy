package it.finmatica.atti.documenti.storico

import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestioneiter.motore.WkfStep

class VistoParereStoricoService {

	IDocumentaleEsterno gestoreDocumentaleEsterno
	IGestoreFile gestoreFile

    public VistoParereStorico storicizza (VistoParere vistoParere, WkfStep stepPrecedente, WkfStep stepSuccessivo) {
		VistoParereStorico storico = new VistoParereStorico ()
		storico.idVistoParere = vistoParere.id
		storico.revisione	= getNextRevisione(storico.idVistoParere)
		storico.iter        = vistoParere.iter
		storico.step		= stepPrecedente

		storico.iter                = vistoParere.iter
		storico.tipologia           = vistoParere.tipologia
		storico.firmatario          = vistoParere.firmatario
		storico.unitaSo4            = vistoParere.unitaSo4
		storico.note                = vistoParere.note
		storico.dataAdozione        = vistoParere.dataAdozione
		storico.automatico          = vistoParere.automatico
		storico.esito               = vistoParere.esito
		storico.stato               = vistoParere.stato
		storico.statoFirma          = vistoParere.statoFirma
		storico.campiProtetti       = vistoParere.campiProtetti
		storico.determina	        = vistoParere.determina
		storico.propostaDelibera    = vistoParere.propostaDelibera
		storico.delibera		    = vistoParere.delibera
		storico.idDocumentoEsterno  = vistoParere.idDocumentoEsterno
		storico.valido              = vistoParere.valido
		storico.ente                = vistoParere.ente
		storico.dateCreated         = vistoParere.dateCreated
		storico.utenteIns           = vistoParere.utenteIns
		storico.noteTrasmissione	= vistoParere.noteTrasmissione

		storico.save()

		if (Impostazioni.INTEGRAZIONE_GDM.abilitato) {
			// se presente il gestore documentale esterno, storicizzo i file.
			gestoreDocumentaleEsterno?.storicizzaDocumento(storico);
		}

		if (vistoParere.testo != null) {
			storico.testo = vistoParere.testo.creaFileAllegatoStorico()
			gestoreFile.addFileStorico (storico, storico.testo, vistoParere.testo)
		}

		return storico
    }

	public long getNextRevisione (long idVistoParere) {
		return ((VistoParereStorico.createCriteria().get {
			projections {
				max("revisione")
			}

			eq ("idVistoParere", idVistoParere)

			//lock (true)
		}?:0) + 1)
	}
}
