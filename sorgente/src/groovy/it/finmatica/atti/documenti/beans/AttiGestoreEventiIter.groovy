package it.finmatica.atti.documenti.beans

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.storico.*
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.IGestoreEventiIter
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.zkutils.SuccessHandler
import org.apache.log4j.Logger

class AttiGestoreEventiIter implements IGestoreEventiIter {

    private final static Logger log = Logger.getLogger(AttiGestoreEventiIter.class)

    PropostaDeliberaStoricoService  propostaDeliberaStoricoService
    SedutaStampaStoricoService		sedutaStampaStoricoService
    IDocumentaleEsterno				gestoreDocumentaleEsterno
    VistoParereStoricoService       vistoParereStoricoService
	TokenIntegrazioneService		tokenIntegrazioneService
    DeterminaStoricoService         determinaStoricoService
    DeliberaStoricoService          deliberaStoricoService
    SpringSecurityService 			springSecurityService
    NotificheService				notificheService
    SuccessHandler 					successHandler
    AttiGestioneTesti				gestioneTesti

    @Override
    void cambioStep (IDocumentoIterabile documentoIterabile, WkfIter iter, WkfStep stepPrecedente, WkfStep stepSuccessivo) {
        log.debug("Sto cambiando lo step dell'iter '${iter.cfgIter.nome}'(${iter.id}) da '${stepPrecedente?.cfgStep?.nome}'(${stepPrecedente?.id}) -> '${stepSuccessivo?.cfgStep?.nome}'(${stepSuccessivo.id}) per il documento ${documentoIterabile.id}")

        // se lo step da cui sto uscendo non è null e non è uno nodo condizionale, storicizzo.
        if (stepPrecedente != null && stepPrecedente.cfgStep.attore != null) {
            controllaTestoObbligatorio(documentoIterabile, iter)

            gestioneTesti.uploadEUnlockTesto(documentoIterabile)

            if (Impostazioni.INTEGRAZIONE_GDM.abilitato) {
                // se presente il documentale esterno, ci salvo il documento.
                gestoreDocumentaleEsterno?.salvaDocumento(documentoIterabile);
            }

            // storicizzo il documento
            storicizza(documentoIterabile, iter, stepPrecedente, stepSuccessivo)
        }

        // elimino tutte le notifiche di cambio step del nodo corrente (solo se l'ho)
        if (stepPrecedente != null) {
            notificheService.eliminaNotifiche(documentoIterabile, TipoNotifica.ASSEGNAZIONE);
        }

        // eseguo le notifiche solo se il nodo in cui vado ha degli attori
        if (stepSuccessivo?.attori?.size() > 0) {
            notificheService.notifica(TipoNotifica.ASSEGNAZIONE, documentoIterabile)
        }

		tokenIntegrazioneService.unlockDocumento(documentoIterabile)
	}

    private void controllaTestoObbligatorio (IDocumentoIterabile documentoIterabile, WkfIter iter) {
        // Non controllo il testo obbligatorio se ho usato l'azione "salta controllo testo"
        if (successHandler.idIterSaltaControlloTesto == iter.id) {
            return
        }

        // Non controllo il testo obbligatorio se il documento è un visto l'esito è NON_APPOSTO o DA_VALUTARE:
        if (documentoIterabile instanceof VistoParere &&
                (documentoIterabile.esito == EsitoVisto.NON_APPOSTO ||  // FEATURE per casalecchio: non processare il testo se il visto è non apposto.
                        documentoIterabile.esito == EsitoVisto.DA_VALUTARE)) {
            // FEATURE per gestire i visti al funzionario. Così da poterli "sbloccare" quando non hanno il testo
            return
        }

		// Non controllo il testo se in tipologia c'è scritto di non controllarlo:
		if ((documentoIterabile instanceof IProposta 	&& !documentoIterabile.tipologiaDocumento.testoObbligatorio) ||
			(documentoIterabile instanceof VistoParere 	&& !documentoIterabile.tipologia.testoObbligatorio)) {
			return
		}

        // Se il documento è un Certificato, NON controllo il testo obbligatorio:
        if (documentoIterabile instanceof Certificato) {
            return
        }

        // Controllo il testo obbligatorio solo per Determine, Proposte di Delibera e Visti
        if (documentoIterabile.testo == null) {
            throw new AttiRuntimeException("Non è possibile proseguire senza aver prima editato il testo.")
        }
    }

    private void storicizza (IDocumentoIterabile documentoIterabile, WkfIter iter, WkfStep stepPrecedente, WkfStep stepSuccessivo) {
        if (documentoIterabile instanceof Determina) {
            log.debug("Storicizzo la determina con id: ${documentoIterabile.id}")
            determinaStoricoService.storicizza(documentoIterabile, stepPrecedente, stepSuccessivo)

            // devo eliminare le note di trasmissione tra uno step e l'altro:
            documentoIterabile.noteTrasmissione = ""

        } else if (documentoIterabile instanceof Delibera) {
            log.debug("Storicizzo la delibera con id: ${documentoIterabile.id}")
            deliberaStoricoService.storicizza(documentoIterabile, stepPrecedente, stepSuccessivo)

            // devo eliminare le note di trasmissione tra uno step e l'altro:
            documentoIterabile.noteTrasmissione = ""

        } else if (documentoIterabile instanceof PropostaDelibera) {
            log.debug("Storicizzo la proposta di delibera con id: ${documentoIterabile.id}")
            propostaDeliberaStoricoService.storicizza(documentoIterabile, stepPrecedente, stepSuccessivo)

            // devo eliminare le note di trasmissione tra uno step e l'altro:
            documentoIterabile.noteTrasmissione = ""

        } else if (documentoIterabile instanceof VistoParere) {
            log.debug("Storicizzo il visto/parere con id: ${documentoIterabile.id}")
            vistoParereStoricoService.storicizza(documentoIterabile, stepPrecedente, stepSuccessivo)

            // devo eliminare le note di trasmissione tra uno step e l'altro:
            documentoIterabile.noteTrasmissione = ""
        } else if (documentoIterabile instanceof SedutaStampa) {
            log.debug("Storicizzo il visto/parere con id: ${documentoIterabile.id}")
            sedutaStampaStoricoService.storicizza(documentoIterabile, stepPrecedente, stepSuccessivo)
        }
    }
}

