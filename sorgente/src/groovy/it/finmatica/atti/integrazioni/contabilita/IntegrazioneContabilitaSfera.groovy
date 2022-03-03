package it.finmatica.atti.integrazioni.contabilita

import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.IProposta
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.TipoFile
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.GestioneTestiTipoModello
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.zkoss.bind.BindUtils
import org.zkoss.zk.ui.event.EventQueues

@Component("integrazioneContabilitaSfera")
@Lazy
class IntegrazioneContabilitaSfera extends AbstractIntegrazioneContabilita {
	
	public static final String TIPO_MODELLO_SCHEDA_CONTABILE_DETERMINA 			= "DETERMINA_SCHEDA_CONTABILE_SFERA"
	public static final String TIPO_MODELLO_SCHEDA_CONTABILE_PROPOSTA_DELIBERA 	= "PROPOSTA_DELIBERA_SCHEDA_CONTABILE_SFERA"
	
	@Autowired GestioneTestiService gestioneTestiService
	
	@Override
	String getZul(IDocumento documento) {
		return "/atti/integrazioni/contabilita/movimentiContabili.zul";
	}

	@Override
	boolean isConDocumentiContabili(IDocumento documento) {
		def documentoPrincipale = getProposta(documento);
		return (MovimentoContabile.countByIdDocumentoAndTipoDocumento (documentoPrincipale.id, documentoPrincipale.TIPO_OGGETTO) > 0)
	}
	
	@Override
	boolean isTipiDocumentoAbilitati () {
		return false;
	}
	
	void aggiornaMaschera (IDocumento documento, boolean modifica) {
		BindUtils.postGlobalCommand("movimentiContabiliQueue", EventQueues.DESKTOP, "aggiornaAtto", [atto:documento, competenza:modifica?"W":"R"])
	}
	
	List<?> getMovimentiContabili (IDocumento documento) {
		def documentoPrincipale = getProposta(documento);
		return MovimentoContabile.findAllByIdDocumentoAndTipoDocumento (documentoPrincipale.id, documentoPrincipale.TIPO_OGGETTO, [sort:'tipo', order:'asc']);
	}

	/**
	 * Implementato solamente per la delibera che deve copiare i dati dalla proposta:
	 */
	void copiaMovimentiContabili (IProposta proposta, IAtto atto) {
		// copio i movimenti dalla proposta di delibera
		def movimentiContabili = getMovimentiContabili (proposta)
		
		for (MovimentoContabile m : movimentiContabili) {
			def movimento 			= m.clone()
			movimento.idDocumento 	= atto.id
			movimento.tipoDocumento	= atto.TIPO_OGGETTO
			movimento.save()
		}
	}
	
	InputStream getSchedaContabile (IDocumento documento) {
		IProposta proposta = getProposta (documento)
		
		def modello = null
		if (proposta instanceof Determina) {
			modello = GestioneTestiModello.findByTipoModello(GestioneTestiTipoModello.findByCodice(TIPO_MODELLO_SCHEDA_CONTABILE_DETERMINA))
		} else {
    		modello = GestioneTestiModello.findByTipoModello(GestioneTestiTipoModello.findByCodice(TIPO_MODELLO_SCHEDA_CONTABILE_PROPOSTA_DELIBERA))
		}
		
		// se non ho il modello, do' errore?
		if (modello == null) {
			return null
		}
		
		return gestioneTestiService.stampaUnione(modello, [id: documento.id], TipoFile.PDF.estensione, true)
	}
}
