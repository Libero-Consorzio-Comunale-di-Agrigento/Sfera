package atti.actions.integrazioni

import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.documenti.Certificato;
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IFascicolabile
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.documenti.IProtocollabile
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.TipoDatoAggiuntivo
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.lettera.IntegrazioneLetteraAgspr
import it.finmatica.atti.integrazioni.protocollo.ProtocolloTreviso
import it.finmatica.atti.integrazioni.protocollo.ProtocolloTrevisoTest
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.zkutils.SuccessHandler
import org.zkoss.zk.ui.Executions

import java.lang.reflect.Proxy

class ProtocolloAction {

    IntegrazioneLetteraAgspr integrazioneLetteraAgspr
    IProtocolloEsterno       protocolloEsterno
    SuccessHandler           successHandler

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
		nome		= "Protocolla il documento",
		descrizione	= "Protocollo il documento sul protocollo definito dalle impostazioni. Protocolla solo se sono soddisfatti i criteri (cioè se il documento no è già protocollato e se sono presenti classifica e fascicolo quando richiesti)")
	def protocolla (IProtocollabile documento) {
		
		if (Impostazioni.PROTOCOLLO_ATTIVO.abilitato) {
			
			// se il documento è già protocollato, esco:
			if (documento.numeroProtocollo > 0) {
				return documento;
			}
			
			// se classifica e fascicolo sono obbligatori ma non li ho, devo interrompere il processo e dare errore
			controllaFascicoloEClassifica(documento)
			
			// eseguo la protocollazione
			protocolloEsterno.protocolla(documento)
			
			// segnalo la protocollazione effettuata
			successHandler.addMessage("Protocollazione effettuata con n. ${documento.numeroProtocollo} / ${documento.annoProtocollo}")
		}

		return documento;
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
		nome		= "Controlla Fascicolo e Classifica",
		descrizione	= "Controlla che sulla proposta siano riportati classifica e fascicolo di protocollo se sono richiesti dall'impostazione PROTOCOLLO_CLASSIFICA_OBBL e PROTOCOLLO_FASCICOLO_OBBL. Se non presenti, interrompe l'esecuzione.")
	def controllaFascicoloEClassifica (IFascicolabile documento) {
		boolean classificaObbligatoria 	= (Impostazioni.PROTOCOLLO_CLASSIFICA_OBBL.abilitato)
		boolean fascicoloObbligatorio 	= (Impostazioni.PROTOCOLLO_FASCICOLO_OBBL.abilitato)

		if (classificaObbligatoria && documento.classificaCodice == null && fascicoloObbligatorio && documento.fascicoloNumero == null) {
			throw new AttiRuntimeException("Non è possibile procedere: Classifica e Fascicolo sono obbligatori")
		} else {
			if (classificaObbligatoria && documento.classificaCodice == null) {
				throw new AttiRuntimeException("Non è possibile procedere: la Classifica è obbligatoria")
			}
			if (fascicoloObbligatorio && documento.fascicoloNumero == null) {
				throw new AttiRuntimeException("Non è possibile procedere: il Fascicolo è obbligatorio")
			}
		}
		
		return documento
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
		nome		= "Fascicola il documento sul Protocollo",
		descrizione	= "Fascicola il documento sul protocollo definito dalle impostazioni.")
	def fascicola (IFascicolabile documento) {
		if (documento.classificaCodice != null && documento.fascicoloAnno > 0  && documento.fascicoloNumero != null) {
			protocolloEsterno.fascicola(documento);
			successHandler.addMessage("Fasciolazione effettuata con Classifica ${documento.classificaCodice} - Anno Fascicolo ${documento.fascicoloAnno} - Numero Fascicolo ${documento.fascicoloNumero}")
		}

		return documento;
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
			nome		= "Archivia il documento protocollato",
			descrizione	= "Dopo la protocollazione e la firma, rimanda i file firmati al sistema di protocollazione per la successiva archiviazione.")
	def archivia (IAtto documento) {
		// Azione realizzata per l'integrazione con il protocollo di Treviso.

		// se il documento non è già protocollato, esco:
		if (!(documento.numeroProtocollo > 0)) {
			return documento;
		}

		def protocolloTreviso = Proxy.getInvocationHandler(protocolloEsterno).getTargetBean();
		if (protocolloTreviso instanceof ProtocolloTreviso || protocolloTreviso instanceof ProtocolloTrevisoTest) {
			protocolloTreviso.archiviazioneAtto(documento);
		}

		return documento;
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
			nome		= "Archivia i visti del documento protocollato",
			descrizione	= "Dopo la protocollazione del documento principale, manda il testo firmato dei visti al sistema di protocollazione.")
	def archiviaVisti (IAtto documento) {
		// Azione realizzata per l'integrazione con il protocollo di Treviso.

		// se il documento non è già protocollato, esco:
		if (!(documento.numeroProtocollo > 0)) {
			return documento;
		}

		def protocolloTreviso = Proxy.getInvocationHandler(protocolloEsterno).getTargetBean();
		if (protocolloTreviso instanceof ProtocolloTreviso || protocolloTreviso instanceof ProtocolloTrevisoTest) {
			protocolloTreviso.archiviazioneVisti(documento)
		}

		return documento;
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Archivia il testo del visto contabile firmato",
		descrizione	= "Dopo la protocollazione del documento principale, manda il testo firmato del visto contabile al sistema di protocollazione.")
	def archiviaVistoContabile (VistoParere documento) {
		// Azione realizzata per l'integrazione con il protocollo di Treviso.

		// se il documento non è già protocollato, esco:
		if (!(documento.atto.numeroProtocollo > 0)) {
			return documento;
		}

		// se il visto non è contabile, esco:
		if (!(documento.tipologia.contabile)) {
			return documento;
		}

		def protocolloTreviso = Proxy.getInvocationHandler(protocolloEsterno).getTargetBean();
		if (protocolloTreviso instanceof ProtocolloTreviso || protocolloTreviso instanceof ProtocolloTrevisoTest) {
			protocolloTreviso.archiviazioneDocumentoCollegato(documento)
		}

		return documento;
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto = [Certificato.TIPO_OGGETTO],
			nome		= "Archivia il certificato",
			descrizione	= "Dopo la protocollazione del documento principale, manda il certificato al sistema di protocollazione.")
	public def archiviaCertificato (Certificato certificato) {
		// Azione realizzata per l'integrazione con il protocollo di Treviso.

		// se il documento non è già protocollato, esco:
		if (!(certificato.documentoPrincipale.numeroProtocollo > 0)) {
			return certificato;
		}

		def protocolloTreviso = Proxy.getInvocationHandler(protocolloEsterno).getTargetBean();
		if (protocolloTreviso instanceof ProtocolloTreviso || protocolloTreviso instanceof ProtocolloTrevisoTest) {
			protocolloTreviso.archiviazioneDocumentoCollegato(certificato)
		}

		return certificato;
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto = [Certificato.TIPO_OGGETTO],
			nome		= "Archivia il certificato e finalizza l'invio",
			descrizione	= "Dopo la protocollazione del documento principale, manda il certificato al sistema di protocollazione e chiude il documento nel sistema di protocollo.")
	public def archiviaCertificatoEFinalizza (Certificato certificato) {
		// Azione realizzata per l'integrazione con il protocollo di Treviso.

		// se il documento non è già protocollato, esco:
		if (!(certificato.documentoPrincipale.numeroProtocollo > 0)) {
			return certificato;
		}

		def protocolloTreviso = Proxy.getInvocationHandler(protocolloEsterno).getTargetBean();
		if (protocolloTreviso instanceof ProtocolloTreviso || protocolloTreviso instanceof ProtocolloTrevisoTest) {
			protocolloTreviso.archiviazioneDocumentoCollegato(certificato, "2")
		}

		return certificato;
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [Certificato.TIPO_OGGETTO],
		nome		= "Protocolla l'atto principale",
		descrizione	= "Protocollo l'atto principale sul protocollo definito dalle impostazioni. Protocolla solo se sono soddisfatti i criteri (cioè se il documento no è già protocollato e se sono presenti classifica e fascicolo quando richiesti)")
	Certificato protocollaAttoPrincipale (Certificato certificato) {
		protocolla(certificato.documentoPrincipale)
		return certificato
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto = [SedutaStampa.TIPO_OGGETTO],
			nome		= "Crea la Lettera su AGSPR",
			descrizione	= "Crea la lettera su agspr")
	SedutaStampa creaLettera (SedutaStampa sedutaStampa) {
		integrazioneLetteraAgspr.creaLettera(sedutaStampa)
		successHandler.addMessage("Lettera Creata su AGSPR")
		return sedutaStampa
	}

	@Action(tipo	= TipoAzione.CLIENT,
		tipiOggetto = [SedutaStampa.TIPO_OGGETTO],
		nome		= "Apre la lettera su AGSPR",
		descrizione	= "Apre la lettera su AGSPR")
	void apriLettera (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
        SedutaStampa sedutaStampa = viewModel.getDocumentoIterabile(false)
		String url = integrazioneLetteraAgspr.getUrlLettera(sedutaStampa.idDocumentoLettera)
        successHandler.saltaInvalidate()
        Executions.getCurrent().sendRedirect(url, "_blank")
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto = [Delibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO],
			nome		= "Crea la Lettera su AGSPR",
			descrizione	= "Crea la lettera su agspr")
	IAtto creaLettera (IAtto atto) {
		integrazioneLetteraAgspr.creaLettera(atto)
		successHandler.addMessage("Lettera Creata su AGSPR")
		return atto
	}

	@Action(tipo	=  TipoAzione.CLIENT,
			tipiOggetto = [Delibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO],
			nome		= "Crea la Lettera su AGSPR e la apre",
			descrizione	= "Crea la lettera su agspr e la apre")
	IAtto creaLetteraEApri (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
		IAtto atto = viewModel.getDocumentoIterabile(false)
		Long idLettera = integrazioneLetteraAgspr.creaLettera(atto)
		String url = integrazioneLetteraAgspr.getUrlLettera(idLettera)
		successHandler.saltaInvalidate()
		Executions.getCurrent().sendRedirect(url, "_blank")
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto = [SedutaStampa.TIPO_OGGETTO],
		nome		= "Ritorna TRUE se è presente una Lettera per questo documento",
		descrizione	= "Ritorna TRUE se è presente una Lettera per questo documento")
    boolean isLetteraPresente (SedutaStampa sedutaStampa) {
		return (integrazioneLetteraAgspr.isLetteraPresente(sedutaStampa.idDocumentoLettera))
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto = [SedutaStampa.TIPO_OGGETTO],
		nome		= "Ritorna TRUE se NON è presente una Lettera per questo documento",
		descrizione	= "Ritorna TRUE se NON è presente una Lettera per questo documento")
	boolean isLetteraNonPresente (SedutaStampa sedutaStampa) {
		return !isLetteraPresente(sedutaStampa)
	}
}
