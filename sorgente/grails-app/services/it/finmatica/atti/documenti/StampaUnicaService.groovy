package it.finmatica.atti.documenti

import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfImportedPage
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfWriter
import grails.util.GrailsNameUtils
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IFileAllegato
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.DocumentoGenerico
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.FileAllegatoStorico
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.grails.firmadigitale.FirmaDigitaleService
import it.finmatica.jsign.api.PKCS7ReaderStream
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.zul.Filedownload

import javax.servlet.http.HttpServletResponse
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class StampaUnicaService {

	IGestoreFile 		 gestoreFile
	GestioneTestiService gestioneTestiService
	AttiGestioneTesti 	 gestioneTesti
	FirmaDigitaleService firmaDigitaleService

	long sommaDimensioneAllegati (IDocumento documento) {
		IDocumento docPrincipale

		if (documento instanceof VistoParere)
			docPrincipale = documento.documentoPrincipale
		else
			docPrincipale = documento

		long somma = sommaFileAllegatiDocumento(docPrincipale)
		for (VistoParere v : docPrincipale.visti) {
			if (!v.valido) {
				continue
			}

			// sommo solo gli allegati dei visti validi
			somma += sommaFileAllegatiDocumento(v)
		}
		return somma
	}

	long sommaFileAllegatiDocumento (IDocumento documento) {
		// dato un documento, devo recuperare tutti gli allegati caricati dall'utente impostati per andare in stampa unica:
		return (long) (Allegato.createCriteria().get {
			createAlias('fileAllegati', 'fa', CriteriaSpecification.LEFT_JOIN)
			projections {
				sum ("fa.dimensione")
			}

			eq (GrailsNameUtils.getPropertyName(GrailsHibernateUtil.unwrapIfProxy(documento).class), documento)
			eq ("valido", 		true)
			eq ("stampaUnica", 	true)
			eq ("riservato", 	false)

			isNull ("codice")
		}?:0)
	}

	void stampaUnica (IDocumento atto) {
   		ArrayList<File> files = new ArrayList<File>();
		   
		boolean eliminaFile = true
		
		try {
			log.debug ("stampaUnica: aggiungo il frontespizio")
			aggiungiAllegati(files, Allegato.ALLEGATO_FRONTESPIZIO, atto);
			
    		File filePdf = null;
			if (!atto.isRiservato()) {
				if (atto.testo) {
					log.debug("stampaUnica: aggiungo il testo del documento")
					filePdf = gestioneTesti.convertiPdf(atto, atto.testo)
					if (filePdf != null)
						files.add(filePdf)
				}
			} else {
				log.debug("stampaUnica: aggiungo l'allegato omissis")
				aggiungiAllegati(files, Allegato.ALLEGATO_OMISSIS, atto)
			}

			log.debug ("stampaUnica: aggiungo il documento riassuntivo delle firme")
			aggiungiAllegati(files, Allegato.ALLEGATO_RIASSUNTIVO_FIRME, atto);

			log.debug ("stampaUnica: aggiungo la scheda contabile")
			aggiungiAllegati(files, Allegato.ALLEGATO_SCHEDA_CONTABILE, atto);

    		log.debug ("stampaUnica: cerco gli allegati del documento")
    		List<Allegato> allegati = Allegato.createCriteria().list {
				if (atto instanceof Determina) {
    				eq ("determina", atto)
    			} else if (atto instanceof Delibera) {
    				eq ("delibera",  atto)
    			}
				else {
					eq ("propostaDelibera",  atto)
				}
    			eq ("valido", 		true)
    			eq ("stampaUnica", 	true)
    			eq ("riservato", 	false)
				or {
					isNull("codice")
					not {
						'in' ( "codice", [Allegato.ALLEGATO_FRONTESPIZIO, Allegato.ALLEGATO_OMISSIS, Allegato.ALLEGATO_SCHEDA_CONTABILE, Allegato.ALLEGATO_RIASSUNTIVO_FIRME])
	    			}
    			}
    			order ("sequenza", 	"asc")
    		}
    
    		log.debug ("stampaUnica: aggiungo i vari allegati")
    		for (Allegato a : allegati) {
    			for (FileAllegato f : a.fileAllegati?.sort { it.id }) {
    				filePdf = gestioneTesti.convertiPdf(a, f)
    				if (filePdf != null) {
						log.debug("stampaUnica: aggiungo allegato: "+a.id)
						files.add(filePdf);
    				}
    			}
    		}
    
    		log.debug ("stampaUnica: cerco i visti del documento")
    		List<VistoParere> visti = VistoParere.createCriteria().list {
    			if (atto instanceof Determina) {
    				eq ("determina", atto)
				} else if (atto instanceof Delibera) {
					or {
						eq("propostaDelibera", atto.proposta)
						eq("delibera", atto)
					}
				} else if (atto instanceof PropostaDelibera) {
					eq ("propostaDelibera", atto)
				}

    			// Dopo attente discussioni, si è deciso che in stampa unica ci vanno SOLO i visti che sono stati FIRMATI (digitalmente o no)
//    			eq ("statoFirma", StatoFirma.FIRMATO)
				
				// Le discussioni di cui sopra non erano poi così attente...
				// Non ci ricordiamo perché avevamo preso quella decisione, comunque, dato che il 99% dei visti vengono firmati, ci sembra più logico mandare in stampa unica
				// quelli che hanno il flag in tipologia e che sono validi e conclusi. Questo in particolare ci torna comodo perché a Faenza hanno un visto "Permesso di Costruire" che viene
				// editato ma non firmato. Tale visto deve finire in stampa unica.
				
				eq ("stato", StatoDocumento.CONCLUSO)
				eq ("valido", true)

				// isNotNull("testo") // http://svi-redmine/issues/22765

				tipologia {
    				eq ("stampaUnica", true)
    				order ("sequenzaStampaUnica", "asc")
    			}
    		}
    
    		// se il documento è una delibera, allora verifico se ha dei pareri. Se ne ha, metto quelli in stampa unica.
			def vistiPareriAtto = visti.findAll{it.propostaDelibera == null && it.valido == true}
			def vistiPareriProposta = (atto instanceof Delibera || atto instanceof PropostaDelibera) ? visti.findAll{it.propostaDelibera != null && it.valido == true} : []
			def vistiPareri = (vistiPareriProposta ?: []) + (vistiPareriAtto ?: [])

			for (def vistoParere : vistiPareriProposta){
				if (vistiPareriAtto.findAll{ it.tipologia.codice == vistoParere.tipologia.codice}.size() > 0){
					vistiPareri.remove(vistoParere)
				}
			}

    		log.debug ("stampaUnica: aggiungo solo i visti firmati (digitalmente o no)")
    		for (VistoParere v : vistiPareri) {

				if (v.testo != null) {
					//aggiungo il testo del visto in stampa unica
					filePdf = gestioneTesti.convertiPdf(v, v.testo)
					if (filePdf != null) {
						files.add(filePdf)
					}
				}

				//aggiungo gli allegati del visto in stampa unica
				List<Allegato> allegatiVisto = Allegato.createCriteria().list {
					eq ("vistoParere", v)
					eq ("valido", 		true)
					eq ("stampaUnica", 	true)
					eq ("riservato", 	false)
					order ("sequenza", 	"asc")
				}
				log.debug ("stampaUnica: aggiungo i vari allegati del visto "+ v.tipologia.codice)
				for (Allegato a : allegatiVisto) {
					for (FileAllegato f : a.fileAllegati?.sort { it.id }) {
						filePdf = gestioneTesti.convertiPdf(a, f)
						if (filePdf != null) {
							log.debug("stampaUnica: aggiungo allegato: "+a.id)
							files.add(filePdf);
						}
					}
				}
    		}

			if (atto instanceof IAtto) {
				log.debug("stampaUnica: ordino i certificati")
				// l'ordinamento è:
				def ordineCertificati = [[tipo: Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA, secondaPubblicazione: false]
										 , [tipo: Certificato.CERTIFICATO_PUBBLICAZIONE, secondaPubblicazione: false]
										 , [tipo: Certificato.CERTIFICATO_ESECUTIVITA, secondaPubblicazione: false]
										 , [tipo: Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE, secondaPubblicazione: false]
										 , [tipo: Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA, secondaPubblicazione: true]
										 , [tipo: Certificato.CERTIFICATO_PUBBLICAZIONE, secondaPubblicazione: true]
										 , [tipo: Certificato.CERTIFICATO_ESECUTIVITA, secondaPubblicazione: true]
										 , [tipo: Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE, secondaPubblicazione: true]];

				// seleziono solo quelli validi.
				for (Certificato c : atto.certificati) {
					if (!c.valido) {
						continue;
					}

					// uso il tipo di certificato e la seconda pubblicazione come "chiave" di ordineCertificati e ad esso assegno il certificato corrente.
					ordineCertificati.find({c.tipo == it.tipo && c.secondaPubblicazione == it.secondaPubblicazione})?.certificato = c;
				}

				def certificati = ordineCertificati.findAll { it.certificato != null }.certificato

				log.debug("stampaUnica: aggiungo i certificati")
				for (Certificato c : certificati) {
					if (c.testo != null) {
						filePdf = gestioneTesti.convertiPdf(c, c.testo)
						if (filePdf != null) {
							files.add(filePdf)
						}
					}
				}
			}
    
    		// se non ho file da mettere in stampa unica, non la genero:
    		if (files.size() == 0) {
    			log.warn ("stampaUnica: Non ho pdf da mettere in stampa unica, non la creo.")
    			return;
    		}
    
    		log.debug ("stampaUnica: eseguo l'unione dei vari pdf (scrivo sul filesystem).")
			File fileTemporaneoSU = File.createTempFile("STAMPA_UNICA", "tmp")
			FileOutputStream out
			try {
				out = new FileOutputStream(fileTemporaneoSU)
				unisciFilePdf (out, files)

				log.debug ("stampaUnica: creo il nome del file.")
				String nomeFile = "SU_" + atto.getNomeFileTestoPdf()

				log.debug ("stampaUnica: salvo la stampa unica.")
				// se ho già il file di stampa unica, non lo ricreo
				if (atto.stampaUnica == null) {
					atto.stampaUnica = new FileAllegato(nome: nomeFile, contentType: "application/pdf", dimensione: -1, modificabile:false)
				}
				gestoreFile.addFile (atto, atto.stampaUnica, new FileInputStream(fileTemporaneoSU))
				atto.save()

			} finally {
				try { out?.close() } catch (Exception e) { }
				fileTemporaneoSU.delete()
			}
		} catch (Throwable e) {
			// in caso di eccezione non eliminare i file (per evenutale debug)
			eliminaFile = false
			throw e
		} finally {
			if (eliminaFile) {
				files*.delete()
			}
		}
	}



	/**
	 * Provvede alla stampa unica di più file pdf.
	 *
	 * @param nomeFileStampaUnica	il file della stampa unica da creare
	 * @param listaFiles			elenco dei file da aggiungere alla stampa unica.
	 * @param fraseFooter			la frase da aggiungere al footer di ogni pagina
	 * @return						il file della stampa unica creato
	 */
	private void unisciFilePdf (OutputStream outputStream, ArrayList<File> listaFiles) {
		unisciFilePdf (outputStream, listaFiles, Impostazioni.STAMPA_UNICA_FRASE_FOOTER.valore)
	}

	/**
	 * Provvede alla stampa unica di più file pdf.
	 *
	 * @param nomeFileStampaUnica	il file della stampa unica da creare
	 * @param listaFiles			elenco dei file da aggiungere alla stampa unica.
	 * @param fraseFooter			la frase da aggiungere al footer di ogni pagina
	 * @return						il file della stampa unica creato
	 */
	private void unisciFilePdf (OutputStream outputStream, ArrayList<File> listaFiles, String fraseFooter) {
		// preparo il file di output
		Document document 	= new Document()
		PdfCopy stampaUnica = new PdfCopy(document, outputStream)
		document.open()

		int totale = 0;
		int numero_pagina = 0;

		// per ogni file pdf, lo apro, ne conto le pagine
		if (Impostazioni.STAMPA_UNICA_NUMERO_PAGINE.abilitato){
			for (File file : listaFiles) {
				FileInputStream pdfFile = null;
				try {
					pdfFile = new FileInputStream(file);
					PdfReader reader = new PdfReader(pdfFile);
					PdfReader.unethicalreading = true;
					totale += reader.getNumberOfPages();
				} catch (Throwable t) {
				} finally {
					if (pdfFile != null) {
						pdfFile.close();
					}
				}
			}
		}
		// per ogni file pdf, lo apro, ne conto le pagine e ogni pagina l'aggiungo alla stampa unica.
		for (File file : listaFiles) {
			FileInputStream pdfFile = null;
			try {
				pdfFile = new FileInputStream(file);
				PdfReader reader = new PdfReader(pdfFile);
				PdfReader.unethicalreading = true;
				int n = reader.getNumberOfPages();

				// ciclo su ogni pagina
				for (int page=1; page<=n; page++) {
					// leggo la pagina
					PdfImportedPage pdfPage = stampaUnica.getImportedPage(reader, page);

					// aggiungo il footer
					PdfCopy.PageStamp footer = stampaUnica.createPageStamp(pdfPage);
					String[] array = fraseFooter.split("#");
					int y = 5 + (12*(array.length-1))
					for (String part : array) {
						ColumnText.showTextAligned(footer.getOverContent(), Element.ALIGN_LEFT, new Phrase(part), 5, y, 0);
						y -= 12
					}
					if (Impostazioni.STAMPA_UNICA_NUMERO_PAGINE.abilitato) {
						ColumnText.showTextAligned(footer.getOverContent(), Element.ALIGN_RIGHT, new Phrase(String.format(Impostazioni.STAMPA_UNICA_FORMATO_NUMERO_PAGINE.valore, ++numero_pagina, totale)), 589, 5, 0);
					}
					footer.alterContents();

					// aggiungo la pagina al pdf finale
					stampaUnica.addPage(pdfPage);
				}
			} catch (Throwable t) {
				log.warn ("ATTENZIONE!!! Errore nell'aggiunta del file PDF: ${file?.getAbsolutePath()} alla stampa unica!: ${t?.message}", t);
			} finally {
				if (pdfFile != null) {
					pdfFile.close();
				}
			}
		}
		document.close();
	}

	/**
	 * Aggiunge ad un array di files gli allegati con il codice specificato
	 *
	 * @param files Elenco dei files
	 * @param codice Codice da utilizzare per la ricerca degli allegati
	 * @param atto Atto su cui effettuare la stampa unica
	 */
	private void aggiungiAllegati(ArrayList<File> files, String codice, IDocumento atto) {
		List<Allegato> allegati = Allegato.createCriteria().list {
			if (atto instanceof Determina) {
				eq("determina", atto)
			} else if (atto instanceof Delibera) {
				eq ("delibera",  atto)
			} else {
				eq ("propostaDelibera",  atto)
			}
			eq ("valido", 		true)
			eq ("stampaUnica", 	true)
			eq ("riservato", 	false)
			eq ("codice", 		codice)
			order ("sequenza", 	"asc")
		}
		for (Allegato a : allegati) {
			for (FileAllegato f : a.fileAllegati?.sort { it.id }) {
				File filePdf = gestioneTesti.convertiPdf(a, f)
				if (filePdf != null) {
					log.debug("aggiungiAllegati: aggiungo allegato: "+a.id)
					files.add(filePdf);
				}
			}
		}
	}

	void stampaUnicaSeduta(ArrayList<PropostaDelibera> proposte){
		log.debug ("stampaUnicaSeduta: merge delle stampe uniche delle proposte")
		ArrayList<FileAllegato> files = new ArrayList<FileAllegato>();

		for ( def proposta : proposte ){
			File filePdf = gestioneTesti.convertiPdf(proposta, proposta.stampaUnica)
			if (filePdf != null) {
				files.add(filePdf);
			}
		}

		if (files.size() == 0) {
			log.warn ("stampaUnicaSeduta: Non ho pdf da mettere in stampa unica, non la creo.")
			return;
		}

		File fileTemporaneoSU = File.createTempFile("STAMPA_UNICA", "tmp")
		FileOutputStream out
		try {
			out = new FileOutputStream(fileTemporaneoSU)
			log.debug ("stampaUnicaSeduta: creo la stampa unica nel file ${fileTemporaneoSU.absolutePath}")
			unisciFilePdf (out, files)

			Filedownload.save(new FileInputStream(fileTemporaneoSU), "application/pdf" , "StampaUnicaSeduta.pdf")

		} finally {
			try { out?.close() } catch (Exception e) { log.error(e) }
			fileTemporaneoSU.delete()
		}
	}

    /**
     * Dato un atto crea un file zip con tutti i documenti collegati.
     *
     * @param atto
     */
    File creaZipAllegati (IAtto atto) {
        File tempZipFile = File.createTempFile("temp", "zip")
        OutputStream outputStream = tempZipFile.newOutputStream()
        try {
            // ottengo tutti i file, li zippo e li ritorno
            ZipOutputStream zipFile = new ZipOutputStream(outputStream)

            addDocumentoToZip(zipFile, "", atto)

            // aggiungo i visti/pareri (li prendo dalla proposta siccome la delibera normalmente non li ha e per la determina coincidono)
            int index = 1
            for (VistoParere vp : atto.proposta.visti) {
                if (!vp.valido) {
                    continue
                }

                // creo la directory per il visto / parere
                String zipDirectory = sanitize("${index}_${vp.tipologia.codice}")+"/"
                addDocumentoToZip (zipFile, zipDirectory, vp)
                index++
            }

            // creo la directory per i certificati
            index = 1
            for (Certificato certificato : atto.certificati) {
                if (!certificato.valido || certificato.testo == null) {
                    continue
                }

                // creo la directory per il visto / parere
                String entryName = sanitize("${index}_${certificato.tipo}")+"/"+certificato.testo.nome
                addFileToZip (zipFile, entryName, certificato, certificato.testo)
                index++
            }
            zipFile.close()

            return tempZipFile
        } finally {
            IOUtils.closeQuietly((OutputStream)outputStream)
        }
    }

    /**
     * Dato un atto crea un file zip con tutti i documenti collegati.
     *
     * @param propostaDelibera
     */
    File creaZipAllegati (PropostaDelibera propostaDelibera) {
        File tempZipFile = File.createTempFile("temp", "zip")
        OutputStream outputStream = tempZipFile.newOutputStream()
        try {
            // ottengo tutti i file, li zippo e li ritorno
            ZipOutputStream zipFile = new ZipOutputStream(outputStream)

            addDocumentoToZip(zipFile, "", propostaDelibera)

            // aggiungo i visti/pareri
            int index = 1
            for (VistoParere vp : propostaDelibera.visti) {
                if (!vp.valido) {
                    continue
                }

                // creo la directory per il visto / parere
                String zipDirectory = sanitize("${index}_${vp.tipologia.codice}")+"/"
                addDocumentoToZip (zipFile, zipDirectory, vp)
                index++
            }

            zipFile.close()

            return tempZipFile
        } finally {
            IOUtils.closeQuietly((OutputStream)outputStream)
        }
    }

    String sanitize (String name) {
        return name.replaceAll("[^a-zA-Z0-9-_\\.]", "_")
    }

    void addDocumentoToZip (ZipOutputStream zipFile, String zipDirectory, IDocumento documento) {
        // aggiungo il testo principale:
        if (documento.testo != null) {
            String entryName = zipDirectory+sanitize(documento.testo.nome)
            addFileToZip (zipFile, entryName, documento, documento.testo)
        }

        // per ogni allegato, creo la relativa directory
        addAllegatiToZip(zipFile, zipDirectory, documento)
    }

    void addAllegatiToZip (ZipOutputStream zipFile, String zipDirectory, IDocumento documento) {
        if (documento.allegati == null) {
            return
        }

        List<Allegato> allegati = documento.allegati.sort { it.sequenza }.sort { it.titolo }
        int index = 1
        for (Allegato allegato : allegati) {

            if (!allegato.valido) {
                continue
            }

            String zipAllegatoDirectory = zipDirectory+sanitize("${index}_${allegato.titolo}")+"/"
            int fileIndex = 1
            List<FileAllegato> fileAllegati = allegato.fileAllegati.sort { it.id }
            for (FileAllegato file : fileAllegati) {
                String entryName = zipAllegatoDirectory+sanitize("${fileIndex}_${file.nome}")
                addFileToZip (zipFile, entryName, allegato, file)
                fileIndex++
            }
            index++
        }
    }

    void addFileToZip (ZipOutputStream zipFile, String entryName, IDocumentoEsterno documento, IFileAllegato fileAllegato) {
        InputStream inputStream = null
        try {
            inputStream = gestoreFile.getFile(documento, fileAllegato)
            zipFile.putNextEntry(new ZipEntry(entryName))
            IOUtils.copy(inputStream, zipFile)
            zipFile.closeEntry()
        } finally {
            IOUtils.closeQuietly((InputStream)inputStream)
        }
    }

	public void copiaConforme (def documento, IFileAllegato fileAllegato, boolean trasformaInPdf=true, boolean sbusta=false, HttpServletResponse response = null) {
		boolean storico = (fileAllegato instanceof FileAllegatoStorico)
		InputStream is
		String nomeFile
		String contentType
		def listaFirmatari = []

		// ottengo l'input stream:
		is = (storico ? gestoreFile.getFileStorico(documento, fileAllegato) : gestoreFile.getFile(documento, fileAllegato))

		if (is == null) {
			throw new AttiRuntimeException("Attenzione! File Storico non trovato!")
		}

		nomeFile 	= fileAllegato.nome
		contentType = fileAllegato.contentType

		//Recupero la lista dei firmatari
		try {
			listaFirmatari = firmaDigitaleService.verificaFirma(is)
		}
		catch (Exception e) {
			log.error ("Si è verificato un errore nella estrapolazione della lista dei firmatari", e)
		}

		//Rileggo l'input stream perchè già letto dalla funzione verificaFirma
		is = (storico ? gestoreFile.getFileStorico(documento, fileAllegato) : gestoreFile.getFile(documento, fileAllegato))
		try {
			// se devo sbustare, sbusto e scarico il file
			if (sbusta && fileAllegato.isP7m()) {
				PKCS7ReaderStream reader = new PKCS7ReaderStream (is)
				is 			= reader.getOriginalContent()
				nomeFile 	= fileAllegato.getNomeFileSbustato()
				contentType = GestioneTestiService.getContentType(nomeFile.substring(nomeFile.lastIndexOf(".")+1))

				if (! nomeFile?.toLowerCase().endsWith("pdf")) {
					nomeFile 	= fileAllegato.getNomePdf()
					contentType = GestioneTestiService.getContentType(GestioneTestiService.FORMATO_PDF)
					is 			= gestioneTesti.convertiStreamInPdf (is, nomeFile, documento)
				}

			} else if (trasformaInPdf && !fileAllegato.isPdf() && !fileAllegato.isP7m()) {
				// se devo scaricare il pdf, eseguo la conversione solo se il file non è già PDF o P7M.
				nomeFile 	= fileAllegato.getNomePdf()
				contentType = GestioneTestiService.getContentType(GestioneTestiService.FORMATO_PDF)
				is 			= gestioneTesti.convertiStreamInPdf (is, nomeFile, documento)
			}
		} catch (Exception e) {
			throw new AttiRuntimeException ("Non è possibile creare la copia conforme per il file "+nomeFile)
		}

		File temp = File.createTempFile("COPIA_CONFORME", "tmp");
		FileOutputStream out;
		try {
			out = creaCopiaConforme(documento, is, new FileOutputStream(temp), listaFirmatari);
			is = new FileInputStream(temp);
		} finally {
			FileUtils.deleteQuietly(temp);
			IOUtils.closeQuietly((OutputStream)out);
		}

		if (response == null) {
			Filedownload.save (is, contentType, nomeFile);
		} else {
			response.contentType = contentType
			response.setHeader("Content-disposition", "attachment filename=${nomeFile}")
			IOUtils.copy(is, response.getOutputStream())
			response.outputStream.flush()
		}
	}

	private FileOutputStream creaCopiaConforme(def documento, InputStream is,FileOutputStream fos, ArrayList listaFirmatari) {
		PdfReader pdfreader = new PdfReader(is);
		PdfStamper pdfStamp = new PdfStamper(pdfreader, fos);
		int numPages = pdfreader.getNumberOfPages();
		PdfContentByte over;
		BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.EMBEDDED);
		Font fontTesto = new Font(bf);
		fontTesto.setSize(8);

		String estremiNumerazione = getInfoDatiNumerazione(documento)
		String estremiProtocollo = getInfoDatiProtocollazione(documento)

		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, fos);
		document.open();

		float posx = (document.right() - document.left()) / 2 + document.leftMargin();
		float posy;
		float incy = 10;
		int i = 0;
		while (i < numPages) {
			posy = document.bottom()+10;
			i++;
			over = pdfStamp.getOverContent(i);
			over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
			over.setFontAndSize(bf, 8);

			Phrase frase = new Phrase("Riproduzione cartacea del documento informatico sottoscritto digitalmente da", fontTesto);
			ColumnText.showTextAligned(over, Element.ALIGN_CENTER, frase, posx, posy, 0);

			//Aggiungo la dicitura per i firmatari
			String testo = ""

			if(listaFirmatari!=null) {
				if (listaFirmatari.size() <= 2) {
					for (utenteFirmatario in listaFirmatari) {
						if (utenteFirmatario.firmatario)
							testo = utenteFirmatario.firmatario.replaceAll("Documento firmato da: ", "").toUpperCase()
						if (utenteFirmatario.data)
							testo = testo + " il " + utenteFirmatario.data?.format("dd/MM/yyyy HH:mm:ss");

						//solo all'ultimo firmatario inserisco i riferimenti di legge
						if (utenteFirmatario == listaFirmatari.last())
							testo = testo + " ai sensi dell'art. 20 e 23 del D.lgs 82/2005";
						posy = posy - incy;
						frase = new Phrase(testo, fontTesto);
						ColumnText.showTextAligned(over, Element.ALIGN_CENTER, frase, posx, posy, 0);
					}
				}
				else {
					ListIterator iter = listaFirmatari.listIterator();
					while (iter.hasNext()) {
						def  utenteFirmatario = iter.next()
						if (utenteFirmatario.firmatario)
							testo = utenteFirmatario.firmatario.replaceAll("Documento firmato da: ", "").toUpperCase()
						if (utenteFirmatario.data)
							testo = testo + " il " + utenteFirmatario.data?.format("dd/MM/yyyy HH:mm:ss");

						if (iter.hasNext()) {
							utenteFirmatario = iter.next()
							if (utenteFirmatario.firmatario)
								testo = testo + ", " + utenteFirmatario.firmatario.replaceAll("Documento firmato da: ", "").toUpperCase()
							if (utenteFirmatario.data)
								testo = testo + " il " + utenteFirmatario.data?.format("dd/MM/yyyy HH:mm:ss");
						}
						//solo all'ultimo firmatario inserisco i riferimenti di legge
						if (utenteFirmatario == listaFirmatari.last())
							testo = testo + " " + Impostazioni.COPIA_CONFORME_FRASE_FIRMATARI.valore;
						posy = posy - incy;
						frase = new Phrase(testo, fontTesto);
						ColumnText.showTextAligned(over, Element.ALIGN_CENTER, frase, posx, posy, 0);
					}
				}

			}

			if (listaFirmatari.size() > 4 && estremiNumerazione?.size() > 0 && estremiProtocollo.size() > 0 ) {
				posy = posy - incy;
				frase = new Phrase(estremiNumerazione+ " - "+estremiProtocollo, fontTesto);
				ColumnText.showTextAligned(over, Element.ALIGN_CENTER, frase, posx, posy, 0);
			}
			else {
				//Aggiungo la dicitura per le informazioni sugli estremi dell'atto
				if (estremiNumerazione?.size() > 0) {
					posy = posy - incy;
					frase = new Phrase(estremiNumerazione, fontTesto);
					ColumnText.showTextAligned(over, Element.ALIGN_CENTER, frase, posx, posy, 0);
				}

				//Aggiungo la dicitura per le informazioni sugli estremi di protocollazione
				if (estremiProtocollo.size() > 0) {
					posy = posy - incy;
					frase = new Phrase(estremiProtocollo, fontTesto);
				}
			}

			ColumnText.showTextAligned(over, Element.ALIGN_CENTER, frase, posx, posy, 0);
		}
		try { pdfStamp.close(); pdfreader.close(); } catch (Exception e) { }
		return fos;
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

	private String getInfoDatiProtocollazione(def doc) {
		if(doc instanceof IDocumentoCollegato)
			doc = doc.documentoPrincipale;

		if(doc instanceof PropostaDelibera)
			return "";

		if (doc instanceof DocumentoGenerico) {
			return (doc.numeroProtocollo != null && doc.numeroProtocollo > 0) ? "Prot.: "+doc.annoProtocollo + " / " + doc.numeroProtocollo + " del " + doc.dataNumeroProtocollo?.format("dd/MM/yyyy") : "";
		}

		return (doc.numeroProtocollo != null && doc.numeroProtocollo != "") ? "Prot.: "+doc.annoProtocollo + " / " + doc.numeroProtocollo + " del " + doc.dataNumeroProtocollo?.format("dd/MM/yyyy") : "";
	}
}
