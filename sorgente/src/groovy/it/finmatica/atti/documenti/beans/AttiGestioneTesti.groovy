package it.finmatica.atti.documenti.beans

import com.aspose.words.*
import com.itextpdf.text.Element
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.*
import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IFileAllegato
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.AttiAd4Service
import it.finmatica.atti.commons.DocumentoGenerico
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.GestioneTestiLog
import it.finmatica.atti.commons.GestioneTestiLogService
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.IDocumentoCollegato
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.dto.commons.FileAllegatoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.dto.DTO
import it.finmatica.gestionedocumenti.documenti.FileDocumento
import it.finmatica.gestionetesti.EsitoRichiestaLock
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.TipoFile
import it.finmatica.gestionetesti.lock.GestioneTestiDettaglioLock
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.jsign.api.PKCS7Reader
import it.finmatica.reporter2.mailmerge.services.FinmaticaMailMerge
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.transaction.annotation.Transactional
import org.zkoss.util.media.Media
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import java.awt.*

@Transactional
class AttiGestioneTesti {

	private static final Logger log = Logger.getLogger(AttiGestioneTesti.class)

	SpringSecurityService 	springSecurityService
	GestioneTestiService 	gestioneTestiService
	AttiGestoreCompetenze 	gestoreCompetenze
	GrailsApplication 		grailsApplication
	AttiAd4Service 			attiAd4Service
	IGestoreFile 			gestoreFile
	GestioneTestiLogService gestioneTestiLogService

	private AttiFileDownloader   attiFileDownloader

	static String creaIdRiferimento (def documento) {
		return "${documento.id}"
	}

    private void caricaLicenzaAspose () {
        License license = new License()
        // if (!license.isLicensed) {
            license.setLicense(FinmaticaMailMerge.class.getClassLoader().getResourceAsStream("Aspose.Words.lic"))
        //}
    }

    /**
     * Sostituisce lo stile "barrato" con spazi evidenziati in nero.
     *
     * @param inputStream
     * @param outputStream
     * @param formato
     */
	void applicaOmissis (InputStream inputStream, OutputStream outputStream, String formato) {
        caricaLicenzaAspose ()

		Document doc = new Document(inputStream)
		doc.joinRunsWithSameFormatting()

		for (Run run : doc.getChildNodes (NodeType.RUN, true)) {
			if (run.font.strikeThrough) {
                run.font.setStrikeThrough(false)
                run.setText(run.text.replaceAll(".", "*"))
                // questa riga sotto è commentata perché con l'highlight e i testi ODT gli asterischi che sostituiscono il testo barrato
                // prendono il colore bianco. Potrebbe forse servire per i modelli word. Forse.
//                run.font.setHighlightColor(Color.BLACK)
                run.font.shading.setBackgroundPatternColor(Color.BLACK)
				run.font.setColor(Color.BLACK)
			}
		}

		doc.save(outputStream, SaveFormat.fromName(formato.toUpperCase()))
	}

    /**
     * Rimuove lo stile "barrato"
     *
     * @param inputStream
     * @param outputStream
     * @param formato
     */
    void rimuoviOmissis (InputStream inputStream, OutputStream outputStream, String formato) {
        caricaLicenzaAspose ()

        Document doc = new Document(inputStream)
        doc.joinRunsWithSameFormatting()

        for (Run run : doc.getChildNodes (NodeType.RUN, true)) {
            if (run.font.strikeThrough) {
                run.font.setStrikeThrough(false)
            }
        }

        doc.save(outputStream, SaveFormat.fromName(formato.toUpperCase()))
    }

    /**
     * Conta quante occorrenze di testo "barrato" sono presenti nel documento.
     *
     * @param inputStream
     * @return
     */
	int contaOmissis (InputStream inputStream) {
        Document doc = new Document(inputStream)
        doc.joinRunsWithSameFormatting()

        int count = 0
        for (Run run : doc.getChildNodes(NodeType.RUN, true)) {
            if (run.font.strikeThrough) {
                count++
            }
        }

        return count
    }

    void applicaWatermarkFacsimile (InputStream inputStream, OutputStream outputStream) {
        PdfReader reader   = new PdfReader(inputStream)
        PdfStamper stamper = new PdfStamper(reader, outputStream)
        com.itextpdf.text.Font f = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 100)
        for (int page = 1; page <= reader.getNumberOfPages(); page ++) {
            PdfContentByte over = stamper.getOverContent(page)
            Phrase p = new Phrase("FACSIMILE", f)
            over.saveState()
            PdfGState gs1 = new PdfGState()
            gs1.setFillOpacity(0.1f)
            over.setGState(gs1)
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, p, 297, 450, 60)
            over.restoreState()
        }
        stamper.close()
        reader.close()
    }

	@Transactional
	void applicaOmissis (IDocumentoEsterno documento, IFileAllegato fileAllegato) {
        InputStream testo = gestoreFile.getFile(documento, fileAllegato)
        File omissis = File.createTempFile("omissis", ".temp")
        try {
            applicaOmissis (testo, omissis.newOutputStream(), Impostazioni.FORMATO_DEFAULT.valore)
            gestoreFile.addFile(documento, fileAllegato, omissis.newInputStream())
        } finally {
            IOUtils.closeQuietly((InputStream)testo)
            FileUtils.deleteQuietly(omissis)
        }
	}

	@Transactional
	void rimuoviOmissis (IDocumentoEsterno documento, IFileAllegato fileAllegato) {
		InputStream testo = gestoreFile.getFile(documento, fileAllegato)
        File omissis = File.createTempFile("omissis", ".temp")
        try {
            rimuoviOmissis (testo, omissis.newOutputStream(), Impostazioni.FORMATO_DEFAULT.valore)
            gestoreFile.addFile(documento, fileAllegato, omissis.newInputStream())
        } finally {
            IOUtils.closeQuietly((InputStream)testo)
            FileUtils.deleteQuietly(omissis)
        }
	}

	@Transactional(readOnly=true)
	void eliminaTesto (def dto, def viewModel) {
        // TODO: Questo metodo va spostato in AbstractViewModel

		// se ho già un documento aperto non faccio nulla.
		if (gestioneTestiService.isEditorAperto()) {
			log.info("Ho già un documento aperto. Non faccio nulla.");
			Clients.showNotification("Per poter eliminare il testo, bisogna prima chiudere l'editor.", Clients.NOTIFICATION_TYPE_WARNING, null, "middle_center", 3000, true);
			return;
		}

		Messagebox.show("Sei sicuro di voler eliminare il testo?", "Elimina Testo",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
			new org.zkoss.zk.ui.event.EventListener() {
				void onEvent(org.zkoss.zk.ui.event.Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						// per eliminare il testo, passo dal viewModel così guadagno la transazionalità dell'operazione che non avrei
						// facendo this.eliminaTesto()
						def d = viewModel.gestioneTesti.eliminaTesto(dto)
						viewModel.aggiornaMaschera (d)
					}
				}
			}
		)
	}

    @Transactional
    void eliminaTesto (IDocumento documento) {
        if (documento.testo != null && documento.testo.isModificabile()) {
            IFileAllegato testo = documento.testo
            documento.testo = null
            gestoreFile.removeFile(documento, testo)
            documento.save()
        }
    }

	@Transactional
	def eliminaTesto (DTO dto) {
		def domainObject = dto.domainObject

		attiAd4Service.logAd4("Elimina testo " +  domainObject.TIPO_OGGETTO, "Elimina testo con id="+ domainObject.id.toString())

		// prima elimino il lock se presente, poi elimino il testo:
		gestioneTestiService.eliminaLock(AttiGestioneTesti.creaIdRiferimento(dto), springSecurityService.currentUser)

        eliminaTesto(domainObject)

		return domainObject
	}

	@Transactional
	void convertiTestoPdf(IDocumento documento) {
		InputStream inputStream

		try {
			inputStream = convertiStreamInPdf(gestoreFile.getFile(documento, documento.testo), documento.testo.nome, documento)

			documento.testo.nome = documento.getNomeFileTestoPdf()
			documento.testo.contentType = TipoFile.PDF.contentType
			documento.testo.modificabile = false
			documento.save()

			gestoreFile.addFile(documento, documento.testo, inputStream)
		} finally {
			IOUtils.closeQuietly((InputStream) inputStream)
		}
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	InputStream convertiStreamInPdf(InputStream is, String nomeFile, IDocumento documento) {

		GestioneTestiLog testiLog = saveLogBeforeConversion(nomeFile, documento)
		return convertAndLog(is, testiLog)
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	InputStream convertAndLog(InputStream is, GestioneTestiLog testiLog) {
		try {
			InputStream inputStream = gestioneTestiService.converti(is, TipoFile.PDF.estensione)
			testiLog.stato = GestioneTestiLog.Stato.SUCCESSO
			gestioneTestiLogService.log(testiLog)
			return inputStream
		}
		catch (RuntimeException e) {

			testiLog.errore = e.localizedMessage
			testiLog.stato = GestioneTestiLog.Stato.ERRORE
			gestioneTestiLogService.log(testiLog)

			throw e
		}
		finally {
			testiLog.dataFineElaborazione = new Date()
			gestioneTestiLogService.log(testiLog)
		}
	}

	GestioneTestiLog saveLogBeforeConversion(String nomeFile, IDocumento documento) {
		GestioneTestiLog testiLog = new GestioneTestiLog()
		testiLog.nomeFile = nomeFile
		testiLog.idDocumento = documento.id
		testiLog.estremiDocumento = getInfoDatiNumerazione(documento)
		testiLog.operazione = GestioneTestiLog.Operazione.CONVERSIONE_PDF
		testiLog.applicativo = "SFERA"
		gestioneTestiLogService.log(testiLog)
		return testiLog
	}


	@Transactional
	void salvaTestoOdt (IDocumento documento) {
		salvaTestoOdt (documento, documento.testo)
	}

	@Transactional
	void salvaTestoOdt (IDocumento documento, FileAllegato fileAllegato) {
        FileAllegato testoOdt = documento.testoOdt
        if (testoOdt == null) {
            testoOdt = new FileAllegato()
            documento.testoOdt = testoOdt

            documento.testoOdt.nome = fileAllegato.nomeFileOdt
        }

        copiaFileAllegato(documento, fileAllegato, documento.testoOdt)
	}

    @Transactional
    void ripristinaTestoOdt(IDocumento documento) {
        ripristinaTestoOdt(documento, documento.testo)
    }

	@Transactional
	void ripristinaTestoOdt(IDocumento documento, FileAllegato fileAllegato) {
        if (documento.testoOdt == null) {
            return
        }

        // elimino il file originale presente perché altrimenti fa casino con i nomi di gdm
        if (documento.testo.fileOriginale != null) {
            FileAllegato fileOriginale = documento.testo.fileOriginale
            documento.testo.fileOriginale = null
            gestoreFile.removeFile(documento, fileOriginale)
        }

        fileAllegato.nome = documento.testoOdt.nomeFileOdtOriginale

        copiaFileAllegato(documento, documento.testoOdt, fileAllegato)
        FileAllegato testoOdt = documento.testoOdt
        documento.testoOdt = null
        gestoreFile.removeFile(documento, testoOdt)
	}

	@Transactional
	void salvaFileOriginale (FileAllegato fileAllegato) {
		FileAllegato fileOriginale = new FileAllegato()
		fileOriginale.nome          = fileAllegato.nomeFileNascosto
		fileOriginale.idFileEsterno = fileAllegato.idFileEsterno
		fileOriginale.dimensione	= fileAllegato.dimensione
		fileOriginale.contentType	= fileAllegato.contentType
		fileOriginale.firmato		= fileAllegato.firmato
		fileOriginale.modificabile	= fileAllegato.modificabile
		fileOriginale.save()

		fileAllegato.fileOriginale = fileOriginale
		fileAllegato.idFileEsterno = null
		fileAllegato.save()

        gestoreFile.updateFile(fileOriginale)
	}

    @Transactional
    void ripristinaFileOriginale (Allegato documento, FileAllegato fileAllegato) {
        if (fileAllegato.fileOriginale == null) {
            // nessun file originale da ripristinare. Devo dare errore?
            return
        }

        FileAllegato fileOriginale = fileAllegato.fileOriginale

        FileAllegato swap = new FileAllegato()
        swap.nome           = fileAllegato.nomeFileOriginale
        swap.idFileEsterno  = fileAllegato.idFileEsterno
        swap.dimensione	    = fileAllegato.dimensione
        swap.contentType	= fileAllegato.contentType
        swap.firmato		= fileAllegato.firmato
        swap.modificabile	= fileAllegato.modificabile

        fileAllegato.nome           = fileOriginale.nomeFileOriginale
        fileAllegato.idFileEsterno  = fileOriginale.idFileEsterno
        fileAllegato.dimensione	    = fileOriginale.dimensione
        fileAllegato.contentType	= fileOriginale.contentType
        fileAllegato.firmato		= fileOriginale.firmato
        fileAllegato.modificabile	= fileOriginale.modificabile

        fileOriginale.nome           = swap.nomeFileOriginale
        fileOriginale.idFileEsterno  = swap.idFileEsterno
        fileOriginale.dimensione	 = swap.dimensione
        fileOriginale.contentType	 = swap.contentType
        fileOriginale.firmato		 = swap.firmato
        fileOriginale.modificabile	 = swap.modificabile

        fileAllegato.fileOriginale = null
        fileAllegato.save()

        gestoreFile.removeFile(documento, fileOriginale)
		gestoreFile.updateFile(fileAllegato)
    }

	/**
	 * Copia un file allegato sullo stesso documento.
	 * Copia tutti i dati ECCETTO il nome del file che deve essere già presente sul file destinatario.
	 *
	 * @param documentoSrc
	 * @param fileAllegatoSrc
	 * @param documentoDest
	 * @param fileAllegatoDest
	 */
    @Transactional
    void copiaFileAllegato (IDocumentoEsterno documentoSrc, FileAllegato fileAllegatoSrc, FileAllegato fileAllegatoDest) {
        copiaFileAllegato(documentoSrc, fileAllegatoSrc, documentoSrc, fileAllegatoDest)
    }

	/**
	 * Copia un file allegato da un documento ad un altro.
     * Copia tutti i dati ECCETTO il nome del file che deve essere già presente sul file destinatario.
	 *
	 * @param documentoSrc
	 * @param fileAllegatoSrc
	 * @param documentoDest
	 * @param fileAllegatoDest
	 */
    @Transactional
    void copiaFileAllegato (IDocumentoEsterno documentoSrc, FileAllegato fileAllegatoSrc, IDocumentoEsterno documentoDest, FileAllegato fileAllegatoDest) {
        fileAllegatoDest.modificabile   = fileAllegatoSrc.modificabile
        fileAllegatoDest.firmato        = fileAllegatoSrc.firmato
        fileAllegatoDest.contentType 	= fileAllegatoSrc.contentType
        fileAllegatoDest.save()

        InputStream inputStream
        try {
            inputStream = gestoreFile.getFile(documentoSrc, fileAllegatoSrc)
            gestoreFile.addFile(documentoDest, fileAllegatoDest, inputStream)
        } finally {
            IOUtils.closeQuietly((InputStream)inputStream)
        }
    }

	@Transactional
	void generaTestoDocumento (def documento, boolean liveData = true) {
		if (documento instanceof SedutaStampa) {
			generaTestoDocumento ((SedutaStampa) documento, Impostazioni.FORMATO_DEFAULT.valore, liveData)
		} else if (documento instanceof Allegato) {
			generaTestoDocumento ((Allegato) documento, Impostazioni.FORMATO_DEFAULT.valore, liveData)
        } else {
            // caso base è un IDocumento
            generaTestoDocumento ((IDocumento) documento, Impostazioni.FORMATO_DEFAULT.valore, liveData)
        }
	}

	@Transactional
	void generaTestoDocumento (IDocumento documento, String formato, boolean liveData = true) {
		ByteArrayInputStream testoOdt = generaStreamTestoDocumento(documento, formato, liveData)

		if (documento.testo == null) {
			FileAllegato fileAllegato 	= new FileAllegato()
			fileAllegato.nome 			= "${documento.nomeFile}.${formato}"
			fileAllegato.dimensione		= -1
			fileAllegato.contentType	= TipoFile.getInstanceByEstensione(formato).contentType
			fileAllegato.modificabile	= true
			documento.testo 			= fileAllegato
		}

		gestoreFile.addFile(documento, documento.testo, testoOdt)
		documento.save()
	}

	@Transactional
	void generaTestoDocumento (Allegato allegato, String formato, boolean liveData = true) {
		ByteArrayInputStream testoOdt = generaStreamTestoDocumento(allegato, formato, liveData)
		FileAllegato fileAllegato = allegato.testo

		if (fileAllegato == null) {
            fileAllegato = new FileAllegato()
			fileAllegato.nome 			= "${allegato.nomeFile}.${formato}"
			fileAllegato.dimensione		= -1
			fileAllegato.contentType	= TipoFile.getInstanceByEstensione(formato).contentType
			fileAllegato.modificabile	= true
			allegato.setTesto(fileAllegato)
		}

		gestoreFile.addFile(allegato, fileAllegato, testoOdt)
		allegato.save()
	}

	@Transactional
	void generaTestoDocumento (SedutaStampa documento, String formato, boolean liveData = true) {
		ByteArrayInputStream testoOdt = generaStreamTestoDocumento(documento, formato, liveData)

        if (documento.filePrincipale == null) {
			FileDocumento fileAllegato 	= new FileDocumento()
			fileAllegato.nome 			= "${documento.nomeFile}.${formato}"
			fileAllegato.dimensione		= -1
			fileAllegato.contentType	= TipoFile.getInstanceByEstensione(formato).contentType
			fileAllegato.modificabile	= true
            fileAllegato.modelloTesto   = documento.modelloTesto
            fileAllegato.codice         = FileDocumento.CODICE_FILE_PRINCIPALE
            fileAllegato.sequenza       = 0

			documento.addToFileDocumenti(fileAllegato)
			documento.save()
		}

        gestoreFile.addFile(documento, documento.filePrincipale, testoOdt)

		documento.save()
	}

	@Transactional
	ByteArrayInputStream generaStreamTestoDocumento (IDocumento documento, String formato, boolean liveData = true) {
		return generaStreamTestoDocumento(documento, [id: documento.id], documento.modelloTesto, formato, liveData)
	}

	@Transactional
	ByteArrayInputStream generaStreamTestoDocumento (Allegato documento, String formato, boolean liveData = true) {
		def modelloTesto =  documento.tipoAllegato?.modelloTesto ?: documento.documentoPrincipale.modelloTesto
		return generaStreamTestoDocumento(documento, [id: documento.documentoPrincipale.id], modelloTesto, formato, liveData)
	}

	@Transactional
	ByteArrayInputStream generaStreamTestoDocumento (SedutaStampa documento, String formato, boolean liveData = true) {
		return generaStreamTestoDocumento(documento, [id: documento.seduta.id, id_seduta_stampa: documento.id], documento.modelloTesto, formato, liveData)
	}

	@Transactional
	ByteArrayInputStream generaStreamTestoDocumento (def documento, Map parametriQuery, GestioneTestiModello modelloTesto, String formato, boolean liveData = true) {
		log.debug ("Genero il testo per il documento ${documento}, con il modello: ${modelloTesto.tipoModello.codice} e il formato: ${formato}")
		InputStream template = null

		try {

			// se ho già il testo, allora devo fare la "seconda passata", quindi riparto da quello e non dal template originale
			if (documento.testo != null) {
				template = gestoreFile.getFile(documento, documento.testo)
			} else {
				template = new ByteArrayInputStream(modelloTesto.fileTemplate)
			}

			// per la query live mi assicuro che il db sia allineato
			if (liveData) {
				documento.save()
			}

			return gestioneTestiService.stampaUnione(template, new String(modelloTesto.tipoModello.query), parametriQuery, formato, liveData)
		} finally {
			try { template?.close() } catch (Exception e) {}
		}
	}

	@Transactional
	boolean editaTesto (DTO documentoDto) {
		def documento 	 = documentoDto.domainObject
        FileAllegato fileAllegato = documento.testo

		attiAd4Service.logAd4("Edita testo "+documento.TIPO_OGGETTO, "Edita testo con id="+documento.id.toString())
		
		// controllo il riservato:
		if (!gestoreCompetenze.controllaRiservato(documento)) {
			return false
		}

		// #23078 controllo che il documento non sia in una fase di firma (è possibile se più utenti sono sullo stesso documento ed uno di questi clicca "firma", l'altro ancora vede il pulsante "Edita Testo" cliccabile)
		if (documento.statoFirma == StatoFirma.IN_FIRMA || documento.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE) {
			Clients.showNotification("Non è possibile editare il testo: è in corso un'operazione di firma.", Clients.NOTIFICATION_TYPE_WARNING, null, "middle_center", 3000, true)
			return false
		}

		// se il testo non è modificabile, do' errore:
		if (fileAllegato != null && !fileAllegato.modificabile) {
			Clients.showNotification("Il testo risulta non più modificabile.", Clients.NOTIFICATION_TYPE_WARNING, null, "middle_center", 3000, true)
			return false
		}
		
		// se ho già un documento aperto non faccio nulla.
		if (gestioneTestiService.isEditorAperto()) {
			Clients.showNotification("Il testo risulta già aperto nell'editor.", Clients.NOTIFICATION_TYPE_WARNING, null, "middle_center", 3000, true);
			return true // ritorno true perché ho comunque già il lock
		}

		String idRiferimentoTesto = AttiGestioneTesti.creaIdRiferimento(documentoDto)

        InputStream inputStreamTesto
        // se non ho il testo, devo generarlo e salvarlo su db.
        if (fileAllegato == null) {
            generaTestoDocumento(documento)
            fileAllegato = documento.testo
            inputStreamTesto = gestoreFile.getFile(documento, fileAllegato)
        } else {
            // se invece ho il testo, ne rigenero solo lo stream per salvarlo su webdav:
            inputStreamTesto = generaStreamTestoDocumento(documento, Impostazioni.FORMATO_DEFAULT.valore)
        }

        FileAllegatoDTO testoDto = fileAllegato.toDTO()

		EsitoRichiestaLock esito = gestioneTestiService.apriEditorTesto(idRiferimentoTesto, springSecurityService.currentUser, inputStreamTesto, fileAllegato.nome, { InputStream inputStream ->
			def doc  = documentoDto.domainObject
            def file = testoDto.domainObject

            // #23078 Questo controllo serve per intercettare quegli utenti che fanno "edita-testo" di un documento che un altro utente sta "firmando".
            // siccome la procedura di firma modifica il file (sia subito prima della firma che subito dopo), con questo controllo si evita che un altro utente oltre al firmatario possa sovrascrivere il testo da firmare.
            if (testoDto.version != file.version) {
                throw new AttiRuntimeException("Un altro utente ha modificato il testo, non è possibile sovrascriverlo.")
            }

			gestoreFile.addFile(doc, file, inputStream)
			
			documentoDto.version = doc.version
			Clients.showNotification("Testo Salvato", Clients.NOTIFICATION_TYPE_INFO, null, "before_center", 3000, true)
		})
		
		if (!esito.esitoLock) {
			Messagebox.show("Non è possibile aprire il documento: ${esito.messaggio}. Vuoi scaricare il testo in sola lettura?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
    				void onEvent(org.zkoss.zk.ui.event.Event e) {
    					if (Messagebox.ON_OK.equals(e.getName())) {
    						def d = documentoDto.domainObject
    						getAttiFileDownloader().downloadFileAllegato(d, d.testo, true)
    					}
    				}
    			})
		}

		// allineo la versione del DTO per non avere problemi in interfaccia
		documentoDto.version = documento.version
		
		return esito.esitoLock
	}

	@Transactional
	void uploadEUnlockTesto (DTO documento, boolean lockPermanente = false, boolean controllaCompetenze = true) {
		if (documento.domainObject == null) {
			return
		}

		String idRiferimentoTesto = AttiGestioneTesti.creaIdRiferimento(documento)
		GestioneTestiDettaglioLock lock = gestioneTestiService.getDettaglioLock(idRiferimentoTesto)
		if (lock?.utenteInizioLock?.id == springSecurityService.currentUser.id) {
			uploadEUnlockTesto (documento.domainObject, lockPermanente, controllaCompetenze)
		}
	}

	@Transactional
	void uploadEUnlockTesto (def documento, boolean lockPermanente = false, boolean controllaCompetenze = true) {
		if (!(documento.id > 0)) {
			return
		}

		String idRiferimentoTesto = AttiGestioneTesti.creaIdRiferimento(documento)
		InputStream file = gestioneTestiService.unlock (idRiferimentoTesto, controllaCompetenze?springSecurityService.currentUser:null)
		try {
			// carico il documento solo se effettivamente l'ho.
			if (file != null) {
				gestoreFile.addFile(documento, documento.testo, file)
			}
		} finally {
			try { file?.close() } catch (Exception e) {}
		}
	}

	@Transactional
	void uploadTestoManuale (IDocumento documento, Media uploadedFile) {
		// se non ho il testo, ne creo uno nuovo
		if (documento.testo == null) {
			FileAllegato fileAllegato 	= new FileAllegato()
			fileAllegato.dimensione		= -1
			fileAllegato.modificabile	= true
			documento.testo 			= fileAllegato
		}

		documento.testo.nome 		= documento.getNomeFileTestoPdf().replaceAll(/\..+$/, uploadedFile.name.substring(uploadedFile.name.indexOf(".")))
		documento.testo.contentType	= uploadedFile.contentType

		// se il documento caricato è un pdf o un p7m, lo imposto come non modificabile:
		documento.testo.modificabile = !(documento.testo.isPdf() || documento.testo.isP7m())

		InputStream uploadedStream = uploadedFile.streamData
		try {
			gestoreFile.addFile(documento, documento.testo, uploadedStream)
			documento.save()
		} finally {
			try { uploadedStream?.close() } catch (Exception e) {}
		}
	}
	
	boolean isEditorAperto () {
		return gestioneTestiService.isEditorAperto()
	}

	@Transactional(readOnly=true)
	boolean isTestoLockato (def documento) {
		return (gestioneTestiService.getDettaglioLock(AttiGestioneTesti.creaIdRiferimento(documento)) != null)
	}

	@Transactional
	void uploadTesto (def documento) {
		// Quando faccio upload di un testo, questo deve essere già presente sulla domain.
		// se non lo è, esco e non faccio nulla
		if (documento.testo == null) {
			return
		}
		
		if (gestioneTestiService.isEditorAperto()) {
			// se ho un documento ancora aperto, do errore:
			throw new AttiRuntimeException ("Per proseguire è necessario chiudere il documento aperto.")
		}

		String idRiferimentoTesto = AttiGestioneTesti.creaIdRiferimento(documento)
		InputStream file = gestioneTestiService.getFile (idRiferimentoTesto)

		try {
			// carico il documento solo se effettivamente l'ho.
			if (file != null) {
				gestoreFile.addFile(documento, documento.testo, file)
			}
		} finally {
			try { file?.close() } catch (Exception e) {}
		}
	}

	// metodi per la gestione delle dipendenze circolari..
	private void setAttiFileDownloader (AttiFileDownloader attiFileDownloader) {
		this.attiFileDownloader = attiFileDownloader
	}

	AttiFileDownloader getAttiFileDownloader () {
		if (this.attiFileDownloader == null) {
			setAttiFileDownloader(grailsApplication.getMainContext().getBean("attiFileDownloader"));
		}

		return this.attiFileDownloader;
	}

	File convertiPdf (IDocumentoEsterno documentoEsterno, FileAllegato fileAllegato) {
		FileInputStream fis = null

		try {
			// se odt va trasformato in pdf, se pdf bene, se p7m va estratto pdf.
			InputStream inputStream = gestoreFile.getFile(documentoEsterno, fileAllegato);

			if (fileAllegato.isP7m()) {
				log.debug ("convertiPdf: ${fileAllegato.nome} è un p7m, lo sbusto")
				PKCS7Reader reader = new PKCS7Reader(inputStream)

				log.debug ("convertiPdf: ${fileAllegato.nome} è un p7m, converto il file sbustato in pdf:")
				inputStream = reader.getOriginalContent();
			}

			File temp = salvaFileTemporaneo (fileAllegato.nome, inputStream)
			if (isPdf(temp)) {
				return temp
			}

			try {
				fis = new FileInputStream (temp)
				log.debug("convertiPdf: tento di convertire il file ${fileAllegato.nome} in pdf")
				inputStream = convertiStreamInPdf(fis, fileAllegato.nome, documentoEsterno)
			} catch (Throwable e) {
				log.warn ("Non sono riuscito a convertire l'allegato con id=${fileAllegato.id} nomefile=${fileAllegato.nome}.", e)
				return null;
			}

			File file = salvaFileTemporaneo(fileAllegato.nome, inputStream)
			return file
		} finally {
			fis?.close()
		}
	}

	boolean isPdf (File file) {
		FileInputStream fis
		try {
			fis = new FileInputStream (file)
			byte[] buffer = new byte[4];
			if (fis.read(buffer) != buffer.length) {
				return false
			}
			return ("%PDF".equals(new String(buffer)))
		} finally {
			fis?.close()
		}
	}

	int getNumeroPagine (IDocumento documento, FileAllegato fileAllegato) {
        File file = convertiPdf(documento, fileAllegato)
        InputStream input = null
        try {
            input = new FileInputStream(file)
            PdfReader reader = new PdfReader(input)
            return reader.getNumberOfPages()
        } catch (Exception e) {
            log.warn("Errore in fase di lettura del testo del documento ${documento.id}", e)
            return -1
        } finally {
            input?.close()
            FileUtils.deleteQuietly(file)
        }
	}

	private File salvaFileTemporaneo (String filename, InputStream is) {
		File temp = File.createTempFile(filename, "tmp")
		FileOutputStream os = new FileOutputStream(temp)
		try {
			IOUtils.copy(is, os)
		} finally {
			if (os != null) {
				os.close()
			}
		}
		return temp
	}

	private String getInfoDatiNumerazione(def doc) {

		if(doc instanceof IDocumentoCollegato)
			doc = doc.documentoPrincipale;

		if (doc instanceof Determina) {
			return (doc.numeroDetermina != null && doc.numeroDetermina != "") ? doc.tipologia.titoloNotifica+": "+doc.annoDetermina + " / " + doc.numeroDetermina + " del " + doc.dataNumeroDetermina?.format("dd/MM/yyyy") : "";
		}

		if (doc instanceof Delibera) {
			return (doc.numeroDelibera !=null && doc.numeroDelibera != "") ? doc.proposta.tipologia.titoloNotifica+": "+doc.annoDelibera + " / " + doc.numeroDelibera + ((doc.dataAdozione != null) ? (" del " + doc.dataAdozione?.format("dd/MM/yyyy") ) : "") : "";
		}

		if (doc instanceof PropostaDelibera) {
			return (doc.numeroProposta != null && doc.numeroProposta != "") ? "Proposta: "+doc.annoProposta + " / " + doc.numeroProposta + " del " + doc.dataNumeroProposta?.format("dd/MM/yyyy") : "";
		}

		if (doc instanceof DocumentoGenerico) {
			return "";
		}
	}
}
