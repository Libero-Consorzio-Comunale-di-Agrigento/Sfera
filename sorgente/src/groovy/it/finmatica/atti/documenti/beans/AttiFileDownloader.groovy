package it.finmatica.atti.documenti.beans

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.IFileAllegato
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.AttiAd4Service
import it.finmatica.atti.commons.FileAllegatoStorico
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.TipoFile
import it.finmatica.jsign.api.PKCS7ReaderStream
import org.apache.commons.io.IOUtils
import org.apache.log4j.Logger
import org.zkoss.util.media.AMedia
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Filedownload
import org.zkoss.zul.Window

import javax.servlet.http.HttpServletResponse

class AttiFileDownloader {

    private static final Logger log = Logger.getLogger(AttiFileDownloader.class)

    SpringSecurityService springSecurityService
    GestioneTestiService  gestioneTestiService
    AttiGestoreCompetenze gestoreCompetenze
    AttiFirmaService      attiFirmaService
    IGestoreFile          gestoreFile
    AttiGestioneTesti     gestioneTesti
    AttiAd4Service        attiAd4Service

    void downloadFileAllegato (def documento, IFileAllegato fileAllegato, boolean solalettura = true, boolean controllaRiservato = true) {

        if (documento.hasProperty("TIPO_OGGETTO") && documento.hasProperty("id")) {
            attiAd4Service.logAd4("Download file allegato " + documento?.TIPO_OGGETTO, "Download file allegato con id=" + documento?.id.toString())
        }

        // controllo la riservatezza SOLO SE NON sto scaricando la stampa unica:
        if (!(documento.hasProperty(
                "stampaUnica") && documento.stampaUnica instanceof IFileAllegato && documento.stampaUnica?.id == fileAllegato.id)) {

            // controllo la riservatezza: se ritorna TRUE allora l'utente vede il riservato e può procedere con il download
            // altrimenti esce (il messaggio di errore è già stato dato dalla funzione stessa)
            if (!gestoreCompetenze.controllaRiservato(documento)) {
                return;
            }
        }

        // se il file allegato è un p7m, apro la popup di download/verifica.
        if (fileAllegato.isP7m() || fileAllegato.isFirmato()) {
            Window w = Executions.createComponents("/commons/popupFileFirmato.zul", null,
                                                   [documento: documento?.toDTO(), nomeFile: fileAllegato.nome, fileAllegato: fileAllegato.toDTO(), storico: (fileAllegato instanceof FileAllegatoStorico), solalettura: solalettura])
            w.doModal()
            return
        }

        downloadFile(documento, fileAllegato, solalettura)
    }

    void downloadFile (
            def documento, IFileAllegato fileAllegato, boolean trasformaInPdf = true, boolean sbusta = false, HttpServletResponse response = null) {
        boolean storico = (fileAllegato instanceof FileAllegatoStorico)
        InputStream is
        String nomeFile
        String contentType
        long dimensione

        // ottengo l'input stream:
        is = (storico ? gestoreFile.getFileStorico(documento, fileAllegato) : gestoreFile.getFile(documento, fileAllegato))

        if (is == null) {
            throw new AttiRuntimeException("Attenzione! File Storico non trovato!")
        }

        nomeFile = fileAllegato.getNomeFileOriginale()
        contentType = fileAllegato.contentType
        dimensione = fileAllegato.dimensione

        try {
            // se devo sbustare, sbusto e scarico il file
            if (sbusta && fileAllegato.isP7m()) {
                PKCS7ReaderStream reader = new PKCS7ReaderStream(is)
                is = reader.getOriginalContent()
                nomeFile = fileAllegato.getNomeFileSbustato()
                contentType = TipoFile.getInstanceByEstensione(nomeFile.substring(nomeFile.lastIndexOf(".") + 1)).contentType
            } else if (trasformaInPdf && !fileAllegato.isPdf() && !fileAllegato.isP7m()) {
                // se devo scaricare il pdf, eseguo la conversione solo se il file non è già PDF o P7M.
                // faccio il controllo sulle estensioni piuttosto che sul flag "modificabile" perché ad es. a Trezzano
                // usano la firma autografa che imposta il documento come "non modificabile" ma non viene trasformato in pdf (almeno in prima battuta)
                nomeFile = fileAllegato.getNomePdf()
                contentType = TipoFile.PDF.contentType
                is = gestioneTesti.convertiStreamInPdf(is, nomeFile, documento)
            }
        } catch (Exception e) {
            log.error("Si è verificato un errore nello sbustare o convertire il file allegato con id ${fileAllegato.id}. Scarico il file originale.",
                      e)

            // se la trasformazione in pdf dà errore, scarico il file "normale"
            nomeFile = fileAllegato.nome
            contentType = fileAllegato.contentType

            // rileggo il file originale siccome lo stream potrebbe essere stato "consumato" anche solo parzialmente.
            is = (storico ? gestoreFile.getFileStorico(documento, fileAllegato) : gestoreFile.getFile(documento, fileAllegato))
        }

        if (response == null) {
            AMedia media = new AMedia(nomeFile, null, contentType, is);
            Filedownload.save(media);
//            Filedownload.save(is, contentType, java.net.URLEncoder.encode(nomeFile, "UTF-8"));
        } else {
            response.setContentLength((int) dimensione)
            response.setContentType(contentType)
            //response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=${nomeFile}")
            IOUtils.copy(is, response.getOutputStream())
            //response.outputStream.flush()
        }
    }

}
