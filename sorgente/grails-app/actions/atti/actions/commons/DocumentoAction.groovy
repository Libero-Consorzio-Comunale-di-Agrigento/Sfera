package atti.actions.commons

import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.AllegatoService
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.NotificheService
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.StatoOdg
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.VistoParereService
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.tipologie.ParametroTipologia
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.atti.integrazioni.documenti.AllegatiObbligatori
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.zkutils.SuccessHandler

class DocumentoAction {

	VistoParereService 	vistoParereService
	AttiFirmaService 	attiFirmaService
	AllegatoService 	allegatoService
	WkfIterService	 	wkfIterService
    AttiGestioneTesti   gestioneTesti
	IGestoreFile 		gestoreFile
	SuccessHandler 		successHandler
	NotificheService 	notificheService

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
		nome		= "Il documento è già passato da questo nodo?",
		descrizione	= "Ritorna TRUE se il documento è già passato dal nodo corrente almeno una volta. FALSE altrimenti.")
	public boolean isGiaPassato (def d) {
		return (WkfStep.countByIterAndCfgStep(d.iter, d.iter.stepCorrente.cfgStep) > 1);
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
		nome		= "Genera una eccezione a runtime.",
		descrizione	= "Genera sempre un'eccezione a runtime. Risulta utile in fase di sviluppo per testare meglio le commit su più connessioni diverse.")
	public def sempreEccezione (def d) {
		throw new Exception("Eccezione per verificare la commit su più connessioni.");
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
		nome		= "Ritorna sempre TRUE",
		descrizione	= "Ritorna sempre TRUE. Può risultare utile per costrurire nodi di attesa automatici che non aspettano ma vanno avanti subito.")
	public boolean sempreVero (def d) {
		return true;
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
		nome		= "Ritorna sempre FALSE",
		descrizione	= "Ritorna sempre FALSE. Può risultare utile per nascondere pulsanti per mantenere valido un flusso senza doverlo rifare.")
	public boolean sempreFalso (def d) {
		return false;
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Imposta come RISERVATO",
		descrizione	= "Imposta il documento come RISERVATO")
	public IDocumentoIterabile impostaRiservato (IDocumentoIterabile d) {
		d.riservato = true
		d.save()
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Imposta come NON RISERVATO",
		descrizione	= "Imposta il documento come NON RISERVATO")
	public IDocumentoIterabile impostaNonRiservato (IDocumentoIterabile d) {
		d.riservato = false
		d.save()
		return d
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Il documento è RISERVATO?",
		descrizione	= "Ritorna TRUE se il documento ha il campo RISERVATO = true")
	public boolean isRiservato (IDocumentoIterabile d) {
		return d.riservato;
	}
	
	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Il documento è RISERVATO ed è senza un allegato OMISSIS?",
		descrizione	= "Ritorna TRUE se il documento ha il campo RISERVATO = true E se NON è presente un allegato OMISSIS.")
	public boolean isRiservatoESenzaOmissis (IDocumentoIterabile d) {
		if (!d.riservato) {
			return false;
		}
		
		Allegato allegato = allegatoService.getAllegato(d, Allegato.ALLEGATO_OMISSIS);
		return (allegato == null);
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Il documento è NON RISERVATO?",
		descrizione	= "Ritorna TRUE se il documento ha il campo RISERVATO = false")
	public boolean isNotRiservato (IDocumentoIterabile d) {
		return !(d.riservato);
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Il documento contiene note contabili?",
		descrizione = "Ritorna TRUE se il documento ha il campo NOTE_CONTABILI valorizzato")
	public boolean isNoteContabiliValorizzato (IDocumentoIterabile d) {
		return (d.noteContabili != null && d.noteContabili.trim().length() > 0)
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Elimina il documento",
		descrizione	= "Elimina il documento (lo mette in stato non valido)")
	public IDocumentoIterabile eliminaDetermina (IDocumentoIterabile d) {
		d.valido = false
		d.save()
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO],
		nome		= "Rigenera Proposta",
		descrizione	= "Chiude i visti presenti e li ricrea come da gestire. Resetta il campo di codiciVistiTrattati. Elimina i firmatari che non hanno firmato. Ripristina il Testo della proposta come prima della firma. Ripristina gli allegati come prima della firma.")
	public def rigeneraProposta (IDocumento documento) {
		// resetto lo stato della proposta
		documento.stato	 	 	= StatoDocumento.PROPOSTA
		documento.statoFirma 	= null
		documento.statoOdg   	= StatoOdg.INIZIALE
		documento.oggettoSeduta = null

		attiFirmaService.eliminaFirmatari(documento, true)
		
		rigeneraVisti(documento)

		// se il file è p7m o pdf, lo elimino e lo sostituisco con quello che ho in d.testoOdt
		if (documento.testo != null && !documento.testo.isModificabile() && documento.testoOdt != null) {
			gestioneTesti.ripristinaTestoOdt (documento)
		}

		// ripristino gli eventuali file allegati firmati:
		for (Allegato allegato : documento.allegati.findAll { it.statoFirma == StatoFirma.FIRMATO || it.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE }) {
			// per ogni allegato, se è firmato, lo metto come "DA_FIRMARE"
			for (FileAllegato fileAllegato : allegato.fileAllegati) {
				if (fileAllegato.firmato) {
					// se ho almeno un file firmato da applicativo, allora metto lo stato dell'allegato a "DA_FIRMARE"
					allegato.statoFirma = StatoFirma.DA_FIRMARE
                    gestioneTesti.ripristinaFileOriginale(allegato, fileAllegato)
				}
			}
		}

		documento.save()
		return documento
	}
	
	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Rigenera i Visti del documento",
		descrizione	= "Chiude i visti presenti e li ricrea come da gestire. Resetta il campo di codiciVistiTrattati.")
	public def rigeneraVisti (IDocumento d) {
		// resetto i visti
		d.codiciVistiTrattati = ""

		// per qualche ragione a me ignota, non posso fare il for direttamente su d.visti (che in questo punto è un org.hibernate.collection.PersistentSet)
		// perché da un errore poco comprensibile. Con questo trucco invece viene riportato ad un più canonico ArrayList e funziona.
		List<VistoParere> vistiDaEliminare = []
		vistiDaEliminare.addAll(d.visti?:[]) // se d.visti è null, da' errore di ambiguous method overlapping

		// invalido i visti presenti, e li ricreo nuovi.
		for (VistoParere visto : vistiDaEliminare) {

			// salto quelli già invalidi:
			if (!visto.valido) {
				continue
			}

			visto.valido = false
			visto.save()

			if (visto.iter != null && visto.iter.dataFine == null) {
				wkfIterService.terminaIter (visto.iter)
			}

			// elimino tutte le notifiche di cambio step
			notificheService.eliminaNotifiche(visto, TipoNotifica.ASSEGNAZIONE)
			// elimino tutte le "altre" notifiche
			notificheService.eliminaNotifiche(visto)

			vistoParereService.creaVistoParere (d, visto.tipologia, visto.automatico, visto.firmatario, visto.unitaSo4)
		}

		d.save()

		return d
	}
	
	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Rigenera i Visti del documento con il codice specificato in tipologia",
		descrizione	= "Rigenera solo i visti con il codice specificato in tipologia. Chiude i visti presenti e li ricrea come da gestire. Resetta il campo di codiciVistiTrattati.",
		codiciParametri 	 = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto da rigenerare."])
	public def rigeneraVistiConCodice (IDocumento d) {
		// Recupero del parametro dalla tipologia
		String codiceVisto = ParametroTipologia.getValoreParametro (d.tipologia, d.iter.stepCorrente.cfgStep, "CODICE_VISTO")
		
		// resetto i visti
		d.removeCodiceVistoTrattato(codiceVisto)

		// per qualche ragione a me ignota, non posso fare il for direttamente su d.visti (che in questo punto è un org.hibernate.collection.PersistentSet)
		// perché da un errore poco comprensibile. Con questo trucco invece viene riportato ad un più canonico ArrayList e funziona.
		List<VistoParere> vistiDaEliminare = []
		vistiDaEliminare.addAll(d.visti)

		// invalido i visti presenti, e li ricreo nuovi.
		for (VistoParere visto : vistiDaEliminare) {

			// salto quelli già invalidi e quelli con il codice diverso da quello richiesto:
			if (!visto.valido ||
				visto.tipologia.codice != codiceVisto) {
				continue
			}

			visto.valido = false
			visto.save()

			if (visto.iter != null && visto.iter.dataFine == null) {
				wkfIterService.terminaIter (visto.iter)
			}

			// elimino tutte le notifiche di cambio step
			notificheService.eliminaNotifiche(visto, TipoNotifica.ASSEGNAZIONE)
			// elimino tutte le "altre" notifiche
			notificheService.eliminaNotifiche(visto)

			vistoParereService.creaVistoParere (d, visto.tipologia, visto.automatico, visto.firmatario, visto.unitaSo4)
		}

		d.save()

		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
			nome		= "Imposta come NON PRIORITARIO",
			descrizione	= "Imposta il documento come NON PRIORITARIO")
	public IDocumentoIterabile impostaNotPrioritario (IDocumentoIterabile d) {
		d.priorita = 0
		d.save()
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
			nome		= "Imposta come PRIORITARIO",
			descrizione	= "Imposta il documento come PRIORITARIO")
	public IDocumentoIterabile impostaPrioritario (IDocumentoIterabile d) {
		d.priorita = 1
		d.save()
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
			nome		= "Verifica la presenza dell'allegato SCHEDA_CONTABILE_ENTRATA",
			descrizione	= "Verifica che per l'atto sia presente l'allegato SCHEDA_CONTABILE_ENTRATA. Interrompe l'esecuzione nel caso in cui non sia presente.")
	public def controllaAllegatoSchedaContabileEntrata (IDocumento d) {
		for (Allegato allegato : d.allegati) {
			if (allegato.valido && allegato.codice == Allegato.ALLEGATO_SCHEDA_CONTABILE_ENTRATA) {
				return d;
			}
		}
		throw new AttiRuntimeException("L'allegato Scheda Contabile Entrata non è presente.")
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
			nome		= "Verifica la presenza dell'allegato SCHEDA_CONTABILE",
			descrizione	= "Verifica che per l'atto sia presente l'allegato SCHEDA_CONTABILE. Interrompe l'esecuzione nel caso in cui non sia presente.")
	public def controllaAllegatoSchedaContabile (IDocumento d) {
		for (Allegato allegato : d.allegati) {
			if (allegato.valido && allegato.codice == Allegato.ALLEGATO_SCHEDA_CONTABILE) {
				return d;
			}
		}
		throw new AttiRuntimeException("L'allegato Scheda Contabile non è presente.")
	}


	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
			nome		= "Inserisce la data ordinamento",
			descrizione	= "Inserisce la data ordinamento")
	public IDocumentoIterabile inserisciDataOrdinamento (IDocumentoIterabile d) {
		d.dataOrdinamento = new Date()
		d.save()
		successHandler.addMessage("Documento salvato")
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
			nome		= "Svuota la data ordinamento",
			descrizione	= "Svuota la data ordinamento")
	public IDocumentoIterabile eliminaDataOrdinamento (IDocumentoIterabile d) {
		d.dataOrdinamento = null
		d.save()
		successHandler.addMessage("Documento salvato")
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
			nome		= "Converti in pdf tutti gli allegati",
			descrizione	= "Converti in pdf tutti gli allegati")
	public IDocumentoIterabile convertiAllegatiPdf (IDocumentoIterabile d) {
		allegatoService.convertiAllegatiPdf(d)
		d.save()
		successHandler.addMessage("Conversione terminata correttamente")
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
			nome		= "Controlla che non esistano allegati da firmare di formati consentiti ma di cui non è prevista la conversione in PDF",
			descrizione	= "Controlla che non esistano allegati da firmare di formati consentiti ma di cui non è prevista la conversione in PDF")
	void controllaAllegatiNonPdfPresenti (IDocumento doc) {
		// conto quanti allegati ci sono da firmare di formati consetiti ma di cui non è prevista la conversione in PDF
		if (allegatoService.esistonoAllegatiDaFirmareNonConvertibili(doc)) {
			throw new AttiRuntimeException("Esistono allegati da firmare di formati consentiti ma di cui non è prevista la conversione in PDF.")
		}
	}


	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
			nome		= "Controlla che siano presenti tutti gli allegati obbligatori in tipologia e categoria",
			descrizione	= "Controlla che siano presenti tutti gli allegati obbligatori in tipologia e categoria")
	void controllaAllegatiObbligatori (IDocumento doc) {
		def proposta = doc instanceof Delibera ? doc.proposta : doc
		def listaAllegatiObbligatori = MappingIntegrazione.findAllByCategoriaAndCodiceAndValoreInterno(AllegatiObbligatori.MAPPING_CATEGORIA, AllegatiObbligatori.MAPPING_CODICE_TIPOLOGIA, proposta.tipologiaDocumento.id.toString())
		if (doc.categoria != null){
			listaAllegatiObbligatori += MappingIntegrazione.findAllByCategoriaAndCodiceAndValoreInterno(AllegatiObbligatori.MAPPING_CATEGORIA, AllegatiObbligatori.MAPPING_CODICE_CATEGORIA, proposta.categoria.id.toString())
		}
		for (def integrazione: listaAllegatiObbligatori) {
			TipoAllegato tipoAllegato = TipoAllegato.get(Long.parseLong(integrazione.valoreEsterno))
			if (doc.allegati.findAll { it.tipoAllegato == tipoAllegato }.size() == 0) {
				throw new AttiRuntimeException("Esistono allegati obbligatori non presenti: \"${tipoAllegato.titolo}\"")
			}
		}
	}
}
