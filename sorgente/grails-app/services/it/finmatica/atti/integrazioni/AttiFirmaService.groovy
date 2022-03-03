package it.finmatica.atti.integrazioni

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.GrailsNameUtils
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.beans.AttiGestoreTransazioneFirma
import it.finmatica.atti.documenti.beans.GdmFirmaDigitaleFileStorage
import it.finmatica.atti.dto.documenti.viste.DocumentoStepDTO
import it.finmatica.atti.dto.documenti.viste.So4DelegaService
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzione
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzioneService
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.grails.firmadigitale.FirmaDigitaleFile
import it.finmatica.grails.firmadigitale.FirmaDigitaleService
import it.finmatica.grails.firmadigitale.FirmaDigitaleTransazione
import it.finmatica.jsign.api.PKCS7Builder
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

class AttiFirmaService {

	// servizi
	AttiGestoreTransazioneFirma attiGestoreTransazioneFirma
	SpringSecurityService		springSecurityService
	FirmaDigitaleService 		firmaDigitaleService
	GestioneTestiService 		gestioneTestiService
	GrailsApplication 			grailsApplication
	So4DelegaService			so4DelegaService
	WkfAzioneService 			wkfAzioneService
	WkfIterService 				wkfIterService
	AttiGestioneTesti 			gestioneTesti
	IGestoreFile 				gestoreFile
	NotificheService			notificheService
	FileFirmatoDettaglioService fileFirmatoDettaglioService

	String getUrlPopupFirma () {
		return attiGestoreTransazioneFirma.urlFirma
	}

	void fineFirmaSalvaFileFirmati (long idTransazioneFirma) {
		fineFirmaSalvaFileFirmati(FirmaDigitaleTransazione.get(idTransazioneFirma))
	}

	void fineFirmaSalvaFileFirmati (FirmaDigitaleTransazione transazione) {
		// recupero i file che sono stati correttamente firmati (o tutti, o nessuno)
		List<FirmaDigitaleFile> listaFileFirmati = firmaDigitaleService.getFileTransazione(transazione.id)

		// se la lista è vuota, significa che c'è stato un errore in firma
		if (listaFileFirmati == null || listaFileFirmati.size() == 0) {
			throw new AttiRuntimeException ("Attenzione: la transazione non contiene alcun file firmato!")
		}

		// processo i file firmati:
		for (FirmaDigitaleFile fileFirmato : listaFileFirmati) {

			// questo if serve per correggere un errore di sparkserver (Issue: #46749 ) in cui non venivano caricati i file firmati.
			// Siccome i file transitano direttamente su gdm, sfera non ha un modo per "assicurasi" che siano effettivamente firmati
			// ha quindi comunque senso (a prescindere dall'errore su sparkserver) assicurarsi che i file siano firmati.
			// per farlo, controlliamo il campo "nomeFirmato" che viene impostato al caricamento dei file firmati. Se questo è null, significa che i file
			// non sono stati caricati e pertanto bisogna bloccare il processo e restituire errore.
			if (fileFirmato.nomeFirmato == null || fileFirmato.nomeFirmato.trim().length() == 0) {
				throw new AttiRuntimeException("Errore nel processo di firma: per risolvere è necessario riavviare il Computer oppure il Processo JNLP.")
			}

			processaFileFirmato(fileFirmato)
		}
	}

	void fineFirmaSbloccaFlusso (long idTransazione, long idFile) {
        fineFirmaSbloccaFlusso(FirmaDigitaleTransazione.get(idTransazione), FirmaDigitaleFile.get(idFile))
    }

	void fineFirmaSbloccaFlusso (FirmaDigitaleTransazione transazione, FirmaDigitaleFile fileFirmato) {
		// scompongo il nome del file e capisco cosa devo fare
		def documento = getDocumentoIdRiferimento(fileFirmato.idRiferimentoFile)

		if (documento instanceof Allegato) {
			// se sto processando un allegato, allora mi assicuro di sbloccare il suo documento "padre"
			documento = documento.documentoPrincipale
		}

		if (documento instanceof IDocumentoIterabile) {
			sbloccaDocumentoFirmato (documento, transazione.utente)
		}
	}

	String multiFirma (Collection<DocumentoStepDTO> documentiDaFirmare) {
		String finalizzaTransazioneFirma

		// ciclo su ogni documento e per ciascuno prendo il pulsante che contiene l'azione di firma:
		for (DocumentoStepDTO d : documentiDaFirmare) {
			IDocumento documento = DocumentoFactory.getDocumento(d.idDocumento, d.tipoOggetto)

			List<WkfAzione> azioni = getAzioniPulsanteFirma(documento.iter.stepCorrente.id).findAll{azione -> azione.nomeMetodo.startsWith("finalizzaTransazioneFirma")}
			for (WkfAzione azione : azioni) {
				String finalizzaTransazioneFirmaDocumento = azione.nomeMetodo
				if (finalizzaTransazioneFirma == null) {
					finalizzaTransazioneFirma = finalizzaTransazioneFirmaDocumento
				} else if (!finalizzaTransazioneFirma.equals(finalizzaTransazioneFirmaDocumento)) {
					throw new AttiRuntimeException("Non è possibile effettuare la firma multipla con tipi diversi di firma.")
				}
			}
		}
		if (finalizzaTransazioneFirma == null){
			throw new AttiRuntimeException ("Errore di configurazione: non ho trovato l'azione di finalizzazione della firma. Verificare la configurazione del flusso.");
		}
		// ciclo su ogni documento e per ciascuno prendo il pulsante che contiene l'azione di firma:
		for (DocumentoStepDTO d : documentiDaFirmare) {
			IDocumento documento = DocumentoFactory.getDocumento(d.idDocumento, d.tipoOggetto)
			firmaDocumento (documento)
		}

		return finalizzaFirma(finalizzaTransazioneFirma)
	}

	private String finalizzaFirma (String azioneFirma) {
		switch (azioneFirma) {
			case "finalizzaTransazioneFirma":
				return attiGestoreTransazioneFirma.finalizzaTransazioneFirma()

			case "finalizzaTransazioneFirmaAutografa":
				return attiGestoreTransazioneFirma.finalizzaTransazioneFirmaAutografa()

			case "finalizzaTransazioneFirmaRemota":
				return attiGestoreTransazioneFirma.finalizzaTransazioneFirmaRemota()

			case "finalizzaTransazioneFirmaRemotaPdf":
				return attiGestoreTransazioneFirma.finalizzaTransazioneFirmaRemotaPdf()
		}

		throw new AttiRuntimeException ("Errore di configurazione: non ho trovato l'azione di finalizzazione della firma con nome: '${azioneFirma}'. Verificare la configurazione del flusso.")
	}

	/**
	 * Esegue le azioni del pulsante di firma.
	 * FIXME: questa è una approssimazione fintanto che gestione-iter non gestirà i pulsanti in multi-selezione.
	 *
	 * @param documento
	 * @return	ritorna l'azione di finalizzazione della firma (autografa o digitale) supponendo che sia l'ultima azione del pulsante.
	 */
	private String firmaDocumento (IDocumento documento) {
		// ottengo l'elenco delle azioni AUTOMATICHE che sono presenti nel pulsante di firma.
		List<WkfAzione> azioni = getAzioniPulsanteFirma (documento.iter.stepCorrente.id)

		// eseguo le azioni del pulsante:
		for (WkfAzione azione : azioni) {
			if (azione.automatica) {
				wkfAzioneService.eseguiAzioneAutomatica(azione, documento)
			}
		}
	}

	/**
	 * Ritorna tutte le azioni di tipo automatico/automatico_calcolo_attore
	 */
	private List<WkfAzione> getAzioniPulsanteFirma (long idStep) {
		//filtriamo le azioni dei pulsanti che hanno il metodo 'apriPopupFirma' per non incorrere in casi di pulsanti di firma di prova (con rollback alla fine)
		return WkfStep.executeQuery ("""select azione
									   from WkfStep s join s.cfgStep as cfgStep
											join cfgStep.cfgPulsanti as cfgPulsanti
											join cfgPulsanti.pulsante as pulsante
											join pulsante.azioni as azione
								 	  where exists (select a.id
													  from WkfPulsante p
													  join p.azioni as a
													 where p = pulsante
													   and a.nomeMetodo = :nomeMetodo
													   and a.nomeBean 	= :nomeBean)
										and azione.tipo in :tipiAzione
									    and s.id 		= :idStep
									order by index(azione) asc
							""", [tipiAzione:[TipoAzione.AUTOMATICA, TipoAzione.AUTOMATICA_CALCOLO_ATTORE], idStep: idStep, nomeMetodo:'apriPopupFirma', nomeBean:'firmaAction'])
	}

	/**
	 * Sblocca un documento solo se in stato "FIRMATO_DA_SBLOCCARE"
	 * Processa il firmatario impostandolo
	 *
	 * @param documento
	 * @param utenteFirmatario
	 * @return
	 */
	void sbloccaDocumentoFirmato (IDocumento documento, String utenteFirmatario) {
		// proseguo con lo sblocco del documento solo se questo è in stato "FIRMATO_DA_SBLOCCARE".
		// questo controllo serve perché siccome la firma di un Allegato sblocca il suo documento padre, il documento padre potrebbe essere
		// sbloccato due volte (una per la firma dell'allegato ed una per la firma del documento stesso)
		if (documento.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE) {
			processaFirmatario(documento, utenteFirmatario)

			// aggiorno lo stato del documento
			documento.statoFirma = StatoFirma.FIRMATO
			documento.statoMarcatura = StatoMarcatura.DA_MARCARE
			documento.save()

			log.debug("Sblocco il documento firmato: ${documento} con l'utente firmatario: ${utenteFirmatario}")
			try {
				wkfIterService.sbloccaDocumento(documento)
			} catch (Throwable e) {
				log.error(e);
				documento.statoFirma = StatoFirma.FIRMATO_DA_SBLOCCARE
				documento.save()
				Notifica.withNewTransaction {
					notificheService.aggiorna(documento)
				}
				throw e;
			}
		}
	}

	/**
	 * Sblocca i documenti solo se in stato "FIRMATO_DA_SBLOCCARE"
	 * Processa il firmatario impostandolo
	 *
	 * @param documento
	 * @param utenteFirmatario
	 * @return
	 */
	void sbloccaDocumentiFirmati (Collection<DocumentoStepDTO> documentiDaSbloccare, String utenteFirmatario) {
		for (DocumentoStepDTO d : documentiDaSbloccare) {
			IDocumento documento = DocumentoFactory.getDocumento(d.idDocumento, d.tipoOggetto)
			sbloccaDocumentoFirmato(documento, utenteFirmatario)
		}
	}

	private void processaFirmatario (IDocumentoIterabile documento, String utenteFirmatario) {
		// Per poter gestire la firma da parte di soggetti "terzi", non faccio il controllo sul firmatario.
		// In particolare serve per Rivoli (gestione del TSO). Mi accontento delle competenze del documento.
			Firmatario firmatario = Firmatario.cheStaFirmando(documento).get();

		if (firmatario == null) {
			throw new AttiRuntimeException ("Non ho trovato il firmatario per il documento: ${documento}")
		}

		// imposto il firmatario come che abbia firmato.
		firmatario.firmato = true
		firmatario.save()
	}
	
	/**
	 * Predispone il firmatario in coda per la firma.
	 */
	void preparaFirmatarioPerFirma (IDocumento documento, boolean controllaFirmatarioUtenteCorrente = true) {
		// cerco nella coda dei firmatari se ho già selezionato un firmatario per il documento e la transazione corrente.
		Firmatario firmatario = Firmatario.cheStaFirmando(documento).get();
		log.debug ("Firmatario che sta firmando: ${firmatario}")

		// se trovo un firmatario, esco.
		if (firmatario != null) {
			firmatario.dataFirma 		= new Date();
			firmatario.firmatarioEffettivo = springSecurityService.currentUser
			firmatario.save()
			return;
		}

		// se non trovo un firmatario per questo documento, dovrò selezionare il primo:
		firmatario = Firmatario.prossimoCheDeveFirmare(documento).get();
		log.debug ("Firmatario che deve firmare: ${firmatario}")

		// se ancora non trovo alcun firmatario, vuol dire che nessun firmatario è stato aggiunto alla coda, quindi do' errore.
		if (firmatario == null) {
			throw new AttiRuntimeException ("Non è possibile proseguire la firma: non c'è nessun firmatario predisposto sul documento ${documento.id}!");
		}

		// se invece trovo un firmatario, verifico che vada bene:
		if (controllaFirmatarioUtenteCorrente && (firmatario.firmatario.id != springSecurityService.currentUser.id && !so4DelegaService.hasDelega(springSecurityService.currentUser, firmatario.firmatario))) {
			throw new AttiRuntimeException ("Non è possibile proseguire la firma: il firmatario predisposto per il documento con id ${documento.id} è ${firmatario.firmatario?.nominativo}!")
		}

		// se invece arrivo qui, allora ho trovato il firmatario giusto, imposto data di firma e transazione, così che dopo lo possa ritrovare più facilmente.
		firmatario.dataFirma 		= new Date();
		firmatario.firmatarioEffettivo = springSecurityService.currentUser
		firmatario.save()
	}

	/**
	 * Prepara il testo per la firma.
	 *
	 * Se il testo esiste ed è un PDF, non fa nulla e ritorna le informazioni sul file per quello che sono già.
	 * Questo è necessario perché possono verificarsi errori nella prima fase di firma (ad es. l'utente apre la maschera di firma ma non firma).
	 *
	 * Come prima cosa, questo metodo verifica che l'editor del testo sia correttamente chiuso.
	 *
	 * Se il testo non esiste, viene creato dal modello "standard" usando i dati "live" della transazione oracle corrente.
	 * Se il testo esiste ed è ancora modificabile, viene riaggiornato con i dati "live" della transazione oracle corrente. (per intenderci: fa la stampa unione con il n. della determina/delibera appena ottenuto nella transazione).
	 * Se il testo è ancora modificabile (quindi è odt/doc/docx), ed è richiesta la trasformazione in pdf, viene convertito.
	 *
	 * @param documento					il documento di cui preparare il testo.
	 * @param trasformaInPdf	se true, converte il testo in pdf (solo se questo non lo è già).
	 * @return	l'oggetto-firma con tutte le informazioni del file da firmare.
	 */
	void preparaTestoPerFirma (IDocumento documento, boolean trasformaInPdf = true) {
		// se il testo non esiste o è ancora modificabile, lo creo:
		if (documento.testo == null || documento.testo.isModificabile()) {

			// verifico che l'editor del testo sia correttamente chiuso.
			if (gestioneTesti.isEditorAperto()) {
				throw new AttiRuntimeException ("Non è possibile proseguire con la firma del testo: il documento è ancora aperto nell'editor di testo. Chiudere l'editor di testo.");
			}

			String idRiferimentoTesto = AttiGestioneTesti.creaIdRiferimento(documento)
			if (gestioneTestiService.isLocked(idRiferimentoTesto)) {
				throw new AttiRuntimeException ("Non è possibile proseguire con la firma del testo: il documento è ancora aperto da un altro utente.");
			}

			// per prima cosa, eseguo l'unlock del testo se necessario
			gestioneTesti.uploadEUnlockTesto (documento)

			// genero/aggiorno il testo.
			gestioneTesti.generaTestoDocumento(documento, true)

			// salvo subito il testo generato nel testoOdt per future elaborazioni
			gestioneTesti.salvaTestoOdt(documento)
		}

		// se richiesto e se non lo è già, trasformo in pdf:
		// serve perché alcuni utilizzano la "firma autografa" che non trasforma il file in pdf
		// in un primo momento ma in un secondo. Ad es. Trezzano: prima "firma" il dirigente, poi la Ragioneria.
		// alla firma del Dirigente il testo deve rimanere modificabile (perché ancora non tutti i tag sono riempiti)
		if (trasformaInPdf && documento.testo.isModificabile()) {
            gestioneTesti.convertiTestoPdf(documento)
        }

		documento.statoFirma = StatoFirma.IN_FIRMA
		documento.save()

		attiGestoreTransazioneFirma.addFileDaFirmare(AttiFirmaService.creaIdRiferimento(documento, documento.testo), documento, documento.testo)
	}

	void preparaAllegati (IDocumento documento) {
        boolean allegatiDaFirmare = false

		impostaAllegatiDaFirmare(documento)

		// processo gli allegati:
		for (Allegato allegato : documento.allegati?.findAll { it.valido == true }) {
			if (allegato.statoFirma == StatoFirma.DA_FIRMARE || allegato.statoFirma == StatoFirma.IN_FIRMA) {
				for (FileAllegato fileAllegato : allegato.fileAllegati) {
					allegato.statoFirma = StatoFirma.IN_FIRMA
					attiGestoreTransazioneFirma.addFileDaFirmare(AttiFirmaService.creaIdRiferimento(allegato, fileAllegato), allegato, fileAllegato)
					allegatiDaFirmare = true
				}
			}
		}

		if (allegatiDaFirmare) {
			//quando firmo un allegato, imposto anche il suo documento principale come "da firmare" in modo da avere coerenza in interfaccia, siccome la firma parte dal documento principale
			documento.statoFirma = StatoFirma.IN_FIRMA
		}
	}

	/**
	 * Prepare l'allegato di tipo SCHEDA_CONTABILE per la firma, se questo allegato ha come stato firma DA_FIRMARE o IN_FIRMA
	 * @param documento Documento che contiene l'allegato di tipo SCHEDA_CONTABILE
	 */
	void preparaAllegatoSchedaContabile (IDocumento documento, String codice = Allegato.ALLEGATO_SCHEDA_CONTABILE, boolean soloNonFirmati = false) {
		boolean allegatiDaFirmare = false

		impostaAllegatiDaFirmare(documento)

		// processo gli allegati:
		for (Allegato allegato : documento.allegati) {
			if (allegato.valido && (allegato.statoFirma == StatoFirma.DA_FIRMARE || allegato.statoFirma == StatoFirma.IN_FIRMA) &&  codice == allegato.codice) {
				for (FileAllegato fileAllegato : allegato.fileAllegati) {
					if (soloNonFirmati && fileAllegato.firmato){
                        continue
                    }
                    allegato.statoFirma = StatoFirma.IN_FIRMA
					attiGestoreTransazioneFirma.addFileDaFirmare(AttiFirmaService.creaIdRiferimento(allegato, fileAllegato), allegato, fileAllegato)
					allegatiDaFirmare = true
				}
			}
		}

		if (allegatiDaFirmare) {
			//quando firmo un allegato, imposto anche il suo documento principale come "da firmare" in modo da avere coerenza in interfaccia, siccome la firma parte dal documento principale
			documento.statoFirma = StatoFirma.IN_FIRMA
		}
	}

	/**
	 * Elimina i firmatari (default: che non hanno già firmato il documento, altrimenti tutti).
	 */
	void eliminaFirmatari (def documento, boolean tutti = false) {
		def firmatari = Firmatario.createCriteria().list {
			if (!tutti) {
                eq ("firmato", false)
            }
			eq (GrailsNameUtils.getPropertyName(GrailsHibernateUtil.unwrapIfProxy(documento).class), documento)
		}

		for (Firmatario f : firmatari) {
			documento.removeFromFirmatari (f)
		}

		firmatari*.delete(failOnError: true)
		documento.save()
	}
	
	void eliminaPrimoFirmatario (def documento) {
		// ottengo il primo firmatario che deve ancora firmare
		List<Firmatario> firmatari = Firmatario.inCodaPerFirmare(documento).list();
		
		if (firmatari.size() == 0) {
			return;
		}
		
		// elimino il primo firmatario:
		firmatari[0].delete()
		firmatari[0] = null;
		
		// ciclo sui restanti e reimposto la sequenza.
		for (int sequenza = 1; sequenza < firmatari.size(); sequenza++) {
			// nota che la sequenza di firma è 1-based, quindi il valore è così già corretto.
			firmatari[sequenza].sequenza = sequenza;
			firmatari[sequenza].save()
		}
	}

	private void processaFileFirmato(FirmaDigitaleFile fileFirmato) {
		// istanzio il documento:
		def doc = getDocumentoIdRiferimento(fileFirmato.idRiferimentoFile)

		// se sto processando un allegato, allora mi assicuro di cambiare anche lo statoFirma sul suo documento padre.
		// questo viene fatto perché ci possono essere casi in cui si vuole firmare solo gli allegati ma non il testo del documento principale.
		if (doc instanceof Allegato) {
			// siccome l'allegato non deve essere sbloccato, lo imposto subito come FIRMATO
			doc.statoFirma = StatoFirma.FIRMATO

			// il controllo su doc.documentoPrincipale != null viene fatto perché dalla pagina di test viene firmato un Allegato che non ha il documentoPrincipale.
            // il controllo sullo stato_firma invece avviene perché voglio essere sicuro di stare firmando questo documento principale.
            // può succedere infatti di stare firmando un visto e gli allegati del documento principale. In questo caso, voglio che il visto sia sbloccato, ma il documento principale no perché altrimenti darebbe errore
            // la funzione "processaFirmatario" perché si aspetta una riga sulla Firmatari per la determina e non la trova.
			if (doc.documentoPrincipale != null && doc.documentoPrincipale.statoFirma == StatoFirma.IN_FIRMA) {
				doc.documentoPrincipale.statoFirma = StatoFirma.FIRMATO_DA_SBLOCCARE
				doc.documentoPrincipale.save()
			}
		} else {
			doc.statoFirma = StatoFirma.FIRMATO_DA_SBLOCCARE
		}

		// FIXME: questo "if" è bruttissimo ma è il modo più rapido per risolvere la situazione.
		// Per poter gestire la firma direttamente da GDM senza fare tutto il "su e giù" dei BLOB, viene utilizzata la classe GdmFirmaDigitaleFileStorage.
		// Questo codice è quello "legacy" per gestire la firma con il "vecchio modo". La cosa è brutta perché bisognerebbe incastrare tutto meglio nel giro di firma.
		if (fileFirmato.idDocumentoGdm == null) {
			new GdmFirmaDigitaleFileStorage(gestioneTesti, gestoreFile, fileFirmatoDettaglioService, springSecurityService).salvaFileFirmato(fileFirmato, fileFirmato.fileFirmato.binaryStream)
		}

		// salvo il documento e il file allegato
		doc.save()
	}

	/**
	 * Aggiunge l'utente alla coda dei firmatari per il documento richiesto.
	 * Siccome la firma può essere interrotta e poi ripresa, questa funzione potrebbe essere invocata più volte.
	 * Perciò questa funzione non crea un nuovo firmatario se esiste già un record con firmato = 'N' per il documento e utente richiesti.
	 *
	 * @param documento
	 * @param utenteAd4
	 */
	void addFirmatario (IDocumento documento, Ad4Utente utenteAd4) {
		if (utenteAd4 == null) {
			throw new AttiRuntimeException ("Attenzione! Manca il Firmatario per il documento ${documento}. Non è possibile continuare.")
		}

		log.debug ("addFirmatario: ${documento.id} ${utenteAd4.id}")
		def firmatario = Firmatario.perDocumento(documento).findByFirmatarioAndFirmato(utenteAd4, false);

		if (firmatario != null) {
			log.warn ("addFirmatario: esiste già un firmatario (${firmatario.id}) per l'utente ${utenteAd4.id} e il documento con id ${documento.id}. Non lo riaggiungo e proseguo.")
			return;
		}

		// conto i firmatari presenti in modo da settare correttamente la sequenza:
		long nFirmatari = Firmatario.ultimoCheDeveFirmare(documento).get()?.sequenza?:0;
		log.debug ("addFirmatario: ho contato ${nFirmatari} per il documento con id: ${documento.id}")

		// aggiungo il firmatario e forzo l'update
		documento.addToFirmatari (new Firmatario (firmatario: utenteAd4, sequenza: (nFirmatari+1)))
		documento.save()
	}

	def impostaAllegatiDaFirmare (def documento) {

		// questa funzione può essere invocata in due fasi:
		// - prima della prima firma
		// - prima delle firme successive alla prima
		// nel primo caso, non bisogna fare niente. Nel secondo caso, bisogna reimpostare come "DA FIRMARE" i file allegati "FIRMATI"

		def allegati = Allegato.createCriteria().listDistinct {
			// tutti gli allegati che appartengono a questo documento
			eq (GrailsNameUtils.getPropertyName(GrailsHibernateUtil.unwrapIfProxy(documento).class)+".id", documento.id)

			// tutti gli allegati che sono in stato "FIRMATO" e che hanno i file al loro interno che sono firmati:
			eq ("statoFirma", StatoFirma.FIRMATO)
			fileAllegati {
				eq ("firmato", true)
			}
		}

		allegati*.statoFirma = StatoFirma.DA_FIRMARE
		allegati*.save()

		return documento;
	}
	
	/**
	 * Elimina le transazioni di firma più vecchie di un giorno.
	 * Funzione richiamata dal job notturno.
	 */
	void eliminaTransazioniVecchie () {
		Date ieri = (new Date() - 1)
		// elimino le transazioni di firma più vecchie di un giorno
		def transazioni = FirmaDigitaleTransazione.findAllByDateCreatedLessThan (ieri)

		log.info ("Elimino ogni transazione di firma troppo vecchia.")
		for (FirmaDigitaleTransazione t : transazioni) {
			firmaDigitaleService.eliminaTransazione(t.id)
		}
	}

	/**
	 * Parsa l'idRiferimento costruito da creaIdRiferimento
	 *
	 * @param idRiferimento	l'id riferimento da parsare
	 * @return	la mappa con i valori trovati nell'idRiferimento, la mappa è della forma: [tipoOggetto:'${documento.TIPO_OGGETTO}', id:${documento.id}, idFile: ${idFile}, sbloccaFlusso:${documento instanceof IDocumentoIterabile}]
	 */
	static def parseIdRiferimento (String idRiferimento) {
		return Eval.me(idRiferimento)
	}

    static IDocumentoEsterno getDocumentoIdRiferimento (String idRiferimento) {
        def map = parseIdRiferimento(idRiferimento)
        return DocumentoFactory.getDocumento(map.id, map.tipoOggetto)
    }

    static FileAllegato getFileAllegatoIdRiferimento (String idRiferimento) {
        def map = parseIdRiferimento(idRiferimento)
        return FileAllegato.get(map.idFile)
    }

	/**
	 * Costruisce l'idRiferimento da passare alla firma per un certo file
	 *
	 * @param documento		la domain a cui appartiene il file da firmare. Questa domain DEVE avere la proprietà statica TIPO_OGGETTO e la proprietà "id".
	 * @param propertyName	il nome della proprietà della domain in cui trovare il file da firmare
	 * @param file			l'istanza del FileAllegato da firmare
	 * @return una stringa della forma: [tipoOggetto:'${documento.TIPO_OGGETTO}', id:${documento.id}, idFile: ${idFile}, sbloccaFlusso:${documento instanceof IDocumentoIterabile}]
	 */
	static String creaIdRiferimento (def documento, FileAllegato file) {
		return "[tipoOggetto:'${documento.TIPO_OGGETTO}', id:${documento.id}, idFile: ${file.id}, sbloccaFlusso:${documento instanceof IDocumentoIterabile}]"
	}
	
	/**
	 * Restituisce TRUE se il documento è firmato, FALSE altrimenti
	 * @param documento
	 * @return
	 */
	boolean isDocumentoFirmato(def documento){
		return Firmatario.cheHannoFirmato(documento).count () > 0 
	}

	public void aggiungiMarcaturaAllegati(def atto, List<String> listaAllegati) {
		String tsUrl = grailsApplication.config.grails.firmaTS.url
		String tsUser = grailsApplication.config.grails.firmaTS.user
		String tsPassword = grailsApplication.config.grails.firmaTS.password

		def documento = atto.domainObject
		for (def file : listaAllegati) {

			def fileAllegato = FileAllegato.get(file.idFileAllegato);
			def doc = DocumentoFactory.getDocumento(file.idDocumento, file.tipoDocumento)

			if (fileAllegato.firmato && fileAllegato.statoMarcatura == StatoMarcatura.DA_MARCARE) {
				OutputStream outputStream
				InputStream is
				try {
					File tempFileMarcato = File.createTempFile("signed", "tmp")
					outputStream = tempFileMarcato.newOutputStream()

					is = gestoreFile.getFile(doc, fileAllegato)
					PKCS7Builder.appendTS(is, outputStream, tsUrl, tsUser, tsPassword)

					gestoreFile.addFile(doc, fileAllegato, new FileInputStream(tempFileMarcato))
					fileAllegato.statoMarcatura = StatoMarcatura.MARCATO
					fileAllegato.save()
				} catch (Exception e) {
					log.error(e, e)
					throw new AttiRuntimeException("Errore durante la marcatura temporale del file " + fileAllegato.nome)
				}
				finally {
					outputStream.close()
					is?.close()
				}
			}
		}
		if (atto.statoMarcatura == StatoMarcatura.DA_MARCARE) {
			documento.statoMarcatura = StatoMarcatura.MARCATO
			documento.save()
		}
	}

	public void rimuoviMarcaturaAllegati(def atto, List<String> listaAllegati, boolean smarcaDocumento) {
		def documento = atto.domainObject
		for (def file : listaAllegati) {
			def fileAllegato = FileAllegato.get(file.idFileAllegato);
			def doc = DocumentoFactory.getDocumento(file.idDocumento, file.tipoDocumento)

			if (fileAllegato.firmato && fileAllegato.statoMarcatura == StatoMarcatura.MARCATO) {
				OutputStream outputStream
				InputStream is
				try {
					File tempFileMarcato = File.createTempFile("signed", "tmp")
					outputStream = tempFileMarcato.newOutputStream()

					is = gestoreFile.getFile(doc, fileAllegato)
					PKCS7Builder.removeTS(is, outputStream)

					gestoreFile.addFile(doc, fileAllegato, new FileInputStream(tempFileMarcato))
					fileAllegato.statoMarcatura = StatoMarcatura.DA_MARCARE
					fileAllegato.save()
				} catch (Exception e) {
					log.error(e, e)
					throw new AttiRuntimeException("Errore durante la rimozione della marcatura temporale del file " + fileAllegato.nome)
				}
				finally {
					outputStream.close()
					is?.close()
				}
			}
		}
		if (atto.statoMarcatura == StatoMarcatura.MARCATO && smarcaDocumento) {
			documento.statoMarcatura = StatoMarcatura.DA_MARCARE
			documento.save()
		}
	}

	public String getTipoFirma(IDocumentoIterabile documento){
		List<WkfAzione> azioni = getAzioniPulsanteFirma(documento.iter.stepCorrente.id).findAll{azione -> azione.nomeMetodo.startsWith("finalizzaTransazioneFirma")}
		String finalizzaTransazioneFirma = null
		for (WkfAzione azione : azioni) {
			finalizzaTransazioneFirma = azione.nomeMetodo
		}
		return finalizzaTransazioneFirma != null ? tipologiaFirma(finalizzaTransazioneFirma): null

	}

	private String tipologiaFirma (String azioneFirma) {
		switch (azioneFirma) {
			case "finalizzaTransazioneFirma":
				return "firma"

			case "finalizzaTransazioneFirmaAutografa":
				return "firmaAutografa";

			case "finalizzaTransazioneFirmaRemota":
				return "firmaRemota"

			case "finalizzaTransazioneFirmaRemotaPdf":
				return "firmaRemotaPdf"
		}
	}



}
