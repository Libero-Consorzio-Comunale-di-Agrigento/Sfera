package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.competenze.AllegatoCompetenze
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.TipoFile
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.zkoss.zhtml.Filedownload

import java.text.SimpleDateFormat

class AllegatoService {

    IntegrazioneContabilita integrazioneContabilita
    GestioneTestiService    gestioneTestiService
    AttiGestoreCompetenze   gestoreCompetenze
    AttiGestioneTesti       gestioneTesti
    IGestoreFile            gestoreFile
    SpringSecurityService   springSecurityService

    /**
     * Crea o recupera l'allegato con il testo modificabile per la gestione degli Omissis.
     * Se non esiste, crea un Allegato con codice=OMISSIS, lo allega alla delibera e gli
     * aggiunge il testo modificabile del documento principale.
     *
     * @param documento il documento a cui aggiungere il testo per gli omissis.
     */
    Allegato creaAllegatoOmissis (IDocumento documento) {
        // cerco l'allegato omissis, se non c'è ne creo uno nuovo:
        Allegato allegato = getAllegato(documento, Allegato.ALLEGATO_OMISSIS)
        if (allegato == null) {
            allegato = creaAllegato(documento, Allegato.ALLEGATO_OMISSIS)
        }

        aggiornaFileOmissis(documento, allegato)

        return allegato
    }

    /**
     * Copia il testo del documento principale nell'allegato, rinomina il file copiato con OMISSIS.
     * La copia avviene solo se il documento principale ha il testo ancora modificabile e non p7m o pdf.
     *
     * Se l'allegato non ha già un file, viene creato.
     *
     * @param documento il documento da cui copiare il testo
     * @param allegato l'allegato su cui copiare il file.
     */
    void aggiornaFileOmissis (IDocumento documento, Allegato allegato) {
        FileAllegato testoRiservato

        if (documento.testo != null && documento.testo.modificabile) {
            testoRiservato = documento.testo
        }

        if (testoRiservato == null) {
            throw new AttiRuntimeException(
                    "Non posso creare il testo con gli OMISSIS perché il documento principale non ha più un testo modificabile.")
        }

        FileAllegato omissis = allegato.getTesto()
        if (omissis == null) {
            omissis = creaFileOmissis(testoRiservato, documento.nomeFile)
            allegato.setTesto(omissis)
        }

        // aggiorno il testo del file omissis copiandolo da quello del documento principale
        gestioneTesti.copiaFileAllegato(documento, testoRiservato, allegato, omissis)
    }

    void aggiornaFileOmissisOscurato (IDocumento documento) {
        Allegato allegato = getAllegato(documento, Allegato.ALLEGATO_OMISSIS)
        if (allegato == null) {
            return
        }

        aggiornaFileOmissisOscurato(documento, allegato)
    }

    void aggiornaFileOmissisOscurato (IDocumento documento, Allegato allegato) {

        if (allegato.testo.firmato) {
            throw new AttiRuntimeException("Non posso sostituire un file OMISSIS già firmato.")
        }

        if (!documento.testo.modificabile) {
            // #35743: se il testo non è modificabile (pdf) allora non posso più modificare nemmeno l'omissis
            return;
        }

        // rigenero il testo del documento principale
        InputStream testoDocumentoPrincipale = gestioneTesti.generaStreamTestoDocumento(documento, Impostazioni.FORMATO_DEFAULT.valore, true)

        // applico le "pecette" nere sugli omissis
        FileAllegato fileOmissis = allegato.testo
        if (fileOmissis == null) {
            fileOmissis = creaFileOmissis(documento.testo, documento.nomeFile)
            allegato.setTesto(fileOmissis)
        }

        File tempFile = File.createTempFile("omissis", "tmp")
        FileOutputStream outputStream = new FileOutputStream(tempFile)
        gestioneTesti.applicaOmissis(testoDocumentoPrincipale, outputStream, TipoFile.PDF.estensione)

        fileOmissis.nome = fileOmissis.nomePdf
        fileOmissis.modificabile = false
        fileOmissis.contentType = TipoFile.PDF.contentType
        gestoreFile.addFile(allegato, fileOmissis, tempFile.newInputStream())

        FileUtils.deleteQuietly(tempFile)
        IOUtils.closeQuietly((FileOutputStream) outputStream)
    }

    FileAllegato creaFileOmissis (FileAllegato testoDocumentoPrincipale, String nomeFile) {
        FileAllegato omissis = new FileAllegato()
        omissis.nome = nomeFile + "_OMISSIS." + testoDocumentoPrincipale.estensione
        omissis.contentType = testoDocumentoPrincipale.contentType
        omissis.modificabile = true
        omissis.firmato = false
        return omissis
    }

    /**
     * Crea o rigenera la stampa del frontespizio per la Delibera.
     * Se non esiste, crea un Allegato con codice=FRONTESPIZIO, lo allega alla delibera, crea la stampa ottenuta dalla tipologia: modelloTestoFrontespizio.
     * Altrimenti, aggiorna il frontespizio già esistente ricreando la stampa.
     * Il frontespizio generato è un PDF, lo statoFirma sarà quello della delibera, verranno date le competenze in lettura all'utente corrente, verrà aggiunto in stampa unica.
     *
     * @param delibera la delibera a cui aggiungere il frontespizio.
     */
    void creaAllegatoFrontespizio (IDocumento documento) {

        // se non ho il modello testo del frontespizio, non faccio nulla ed esco.
        if (documento.tipologiaDocumento.modelloTestoFrontespizio == null) {
            return
        }

        // faccio flush per essere sicuro di ottenere i valori giusti:
        documento.save()

        Allegato allegato = getAllegato(documento, Allegato.ALLEGATO_FRONTESPIZIO)

        // se ho già l'allegato ed è firmato, non faccio niente
        if (allegato?.statoFirma == StatoFirma.FIRMATO) {
            return
        }

        if (allegato == null) {
            allegato = creaAllegato(documento, Allegato.ALLEGATO_FRONTESPIZIO)
            allegato.riservato = false
        }

        int numeroPagine = gestioneTesti.getNumeroPagine(documento, documento.testo)

        FileAllegato fileAllegato = allegato.fileAllegati?.size() > 0 ? allegato.fileAllegati?.first() : null;
        if (fileAllegato == null) {
            fileAllegato = new FileAllegato()
            fileAllegato.nome = "Frontespizio.pdf"
            fileAllegato.contentType = "application/pdf"
            fileAllegato.modificabile = false
            allegato.addToFileAllegati(fileAllegato)
            allegato.save()
        }

        // genero la stampa con i dati di runtime:
        InputStream frontespizio = gestioneTestiService.stampaUnione(
                (GestioneTestiModello) documento.tipologiaDocumento.modelloTestoFrontespizio,
                [id: documento.id, numero_pagine: (numeroPagine < 0 ? "n.d." : Integer.toString(numeroPagine))],
                TipoFile.PDF.estensione, true)

        // aggiorno il file
        gestoreFile.addFile(allegato, fileAllegato, frontespizio)
    }


    /**
     * Crea un allegato modificabile.
     * Se non esiste, crea un Allegato con codice=ALLEGATO_MODIFICABILE, lo allega alla documento.
     * Altrimenti, se l'allegato esiste già non fa nulla.
     *
     * @param documento
     */
    void creaAllegatoModificabile (VistoParere vistoParere) {
        Allegato allegato = getAllegato(vistoParere, Allegato.ALLEGATO_MODIFICABILE)

        // se ho già l'allegato ed è firmato, non faccio niente
        if (allegato?.statoFirma == StatoFirma.FIRMATO) {
            return
        }

        if (allegato == null) {
            allegato = creaAllegato(vistoParere, Allegato.ALLEGATO_MODIFICABILE)
        }

        allegato.riservato = false

        generaTestoAllegato(vistoParere, allegato)
    }

    /**
     * Crea un allegato riassuntivo delle firme.
     * Se non esiste, crea un Allegato con codice=ALLEGATO_RIASSUNTIVO_FIRME, lo allega alla documento.
     * Altrimenti, se l'allegato esiste già non fa nulla.
     *
     * @param documento
     */
    void creaAllegatoRiassuntivoFirme (IDocumento documento) {
        Allegato allegato = getAllegato(documento, Allegato.ALLEGATO_RIASSUNTIVO_FIRME)

        // se ho già l'allegato ed è firmato, non faccio niente
        if (allegato?.statoFirma == StatoFirma.FIRMATO) {
            return
        }

        if (allegato == null) {
            allegato = creaAllegato(documento, Allegato.ALLEGATO_RIASSUNTIVO_FIRME)
        }

        allegato.riservato = false

        generaTestoAllegato(documento, allegato)
    }

    void creaAllegatoSchedaContabile (IDocumento documento) {
        Allegato allegatoSchedaContabile = getAllegato(documento, Allegato.ALLEGATO_SCHEDA_CONTABILE)

        // se ho già l'allegato ed è firmato, non rigenero la scheda contabile.
        if (allegatoSchedaContabile?.statoFirma == StatoFirma.FIRMATO) {
            return
        }

        if (allegatoSchedaContabile == null) {
            allegatoSchedaContabile = creaAllegato(documento, Allegato.ALLEGATO_SCHEDA_CONTABILE)

            // se il documento principale è riservato, allora metto come riservato anche la scheda contabile.
            // altrimenti non faccio niente (do' quindi la possibilità all'utente di impostare la scheda contabile come riservata quando il suo documento principale non lo è)
            // questa modifica è richiesta da Vigevano (2/11/2016, sentire Laura Trabucchi in proposito)
            IDocumento documentoPrincipale = (documento instanceof IDocumentoCollegato ? ((IDocumentoCollegato) documento).documentoPrincipale : documento)
            allegatoSchedaContabile.riservato = documentoPrincipale.riservato
            allegatoSchedaContabile.save()
        }

        // genero la stampa contabile
        // FIXME: qua va fatto un altro refactor: getSchedaContabile deve ritornare anche il nome e il tipo di file.
        InputStream schedaContabile = integrazioneContabilita.getSchedaContabile(documento)

        // se la scheda contabile è vuota, significa che non ci sono movimenti e che quindi va eliminata.
        if (schedaContabile == null) {
            elimina(allegatoSchedaContabile, documento)
            return
        }

        FileAllegato fileAllegato = allegatoSchedaContabile.fileAllegati?.size() > 0 ? allegatoSchedaContabile.fileAllegati?.first() : null
        if (fileAllegato == null) {
            fileAllegato = new FileAllegato()

            if (allegatoSchedaContabile.tipoAllegato?.modelloTesto != null) {
                fileAllegato.nome = "SchedaContabile." + allegatoSchedaContabile.tipoAllegato.modelloTesto.tipo
                fileAllegato.contentType = allegatoSchedaContabile.tipoAllegato.modelloTesto.contentType
                fileAllegato.modificabile = allegatoSchedaContabile.tipoAllegato.modificabile
            } else {
                fileAllegato.nome = "SchedaContabile.pdf"
                fileAllegato.contentType = "application/pdf"
                fileAllegato.modificabile = false
            }
            allegatoSchedaContabile.addToFileAllegati(fileAllegato)
        }

        // aggiorno / aggiungo il file della scheda contabile
        gestoreFile.addFile(allegatoSchedaContabile, fileAllegato, schedaContabile)

        allegatoSchedaContabile.save()
    }

    void elimina (Allegato allegato, IDocumento documentoPrincipale) {
        Allegato daEliminare = documentoPrincipale.allegati.find { it.id == allegato.id }
        documentoPrincipale.removeFromAllegati(daEliminare)

        // se dopo questa modifica, ho ancora un collegamento attivo, salvo l'allegato ed esco:
        if (allegato.determina != null || allegato.delibera != null || allegato.propostaDelibera != null || allegato.vistoParere != null) {
            allegato.save()
            return;
        }

        // elimino tutti i suoi fileAllegati
        def toDelete = allegato.fileAllegati.collect { it }
        for (i in toDelete) {
            allegato.removeFromFileAllegati(i)
            gestoreFile.removeFile(allegato, i)
        }

        // elimino le competenze
        def listaCompetenze = AllegatoCompetenze.findAllByAllegato(allegato);
        for (i in listaCompetenze) {
            i.delete()
        }

        // elimino l'allegato
        allegato.delete()

        riordinaAllegati(documentoPrincipale)
    }

    void riordinaAllegati (IDocumento documentoPrincipale) {
        // ripristino la sequenza
        int i = 1
        for (Allegato a : documentoPrincipale.allegati.sort { it.sequenza }) {
            a.sequenza = (i++)
            a.save()
        }
    }

    Allegato creaAllegato (IDocumento documento, String codiceTipoAllegato) {
        TipoAllegato tipoAllegato = getTipoAllegato(codiceTipoAllegato, documento.tipoOggetto)
        if (tipoAllegato == null) {
            throw new AttiRuntimeException(
                    "Errore in configurazione: non è possibile creare un allegato di tipo '${codiceTipoAllegato}' per il documento di tipo '${documento.tipoOggetto}'. Creare un nuovo tipo di allegato nei dizionari.")
        }

        return creaAllegato(documento, tipoAllegato)
    }

    Allegato creaAllegato (IDocumento documento, TipoAllegato tipoAllegato) {
        Allegato allegato = new Allegato()
        allegato.titolo = tipoAllegato.titolo
        allegato.codice = tipoAllegato.codice

        allegato.tipoAllegato = tipoAllegato
        allegato.statoFirma = tipoAllegato.statoFirma
        allegato.stampaUnica = tipoAllegato.stampaUnica
        allegato.pubblicaAlbo = tipoAllegato.pubblicaAlbo
        allegato.pubblicaCasaDiVetro = tipoAllegato.pubblicaCasaDiVetro
        allegato.pubblicaVisualizzatore = tipoAllegato.pubblicaVisualizzatore
        allegato.sequenza = documento.allegati?.size() > 0 ? documento.allegati?.size() + 1 : 1

        documento.addToAllegati(allegato)
        allegato.save()
        documento.save()

        // assegno le competenze al nuovo allegato:
        gestoreCompetenze.copiaCompetenze(documento, allegato, false)

        return allegato
    }

    void generaTestoAllegato (IDocumento documento, Allegato allegato) {
        if (allegato.tipoAllegato.modelloTesto == null) {
            return
        }

        FileAllegato fileAllegato = allegato.fileAllegati?.size() > 0 ? allegato.fileAllegati?.first() : null

        String estensioneFile = (allegato.tipoAllegato.modificabile ? allegato.tipoAllegato.modelloTesto.tipo : TipoFile.PDF.estensione)
        if (fileAllegato == null) {
            fileAllegato = new FileAllegato()
            fileAllegato.nome = allegato.tipoAllegato.titolo + "." + estensioneFile
            fileAllegato.contentType = TipoFile.getInstanceByEstensione(estensioneFile).contentType
            fileAllegato.modificabile = allegato.tipoAllegato.modificabile
            allegato.addToFileAllegati(fileAllegato)
        }

        // genero la stampa con i dati di runtime:
        InputStream allegatoModificabile = gestioneTestiService.stampaUnione(allegato.tipoAllegato.modelloTesto,
                                                                             [id: documento.id],
                                                                             estensioneFile, true)

        // aggiorno il file
        gestoreFile.addFile(allegato, fileAllegato, allegatoModificabile)
    }

    TipoAllegato getTipoAllegato (String tipoAllegato, String tipoDocumento) {
        return TipoAllegato.findByValidoAndCodiceAndTipologia(true, tipoAllegato, tipoDocumento)
    }

    Allegato getAllegato (IDocumento documento, String tipoAllegato) {
        if (documento.allegati == null) {
            return null
        }

        for (Allegato allegato : documento.allegati) {
            if (!allegato.valido) {
                continue
            }
            if (allegato.codice == tipoAllegato) {
                return allegato
            }
        }
        return null
    }

    public void anteprimaAllegatoPdf(Allegato allegato, FileAllegato fileAllegato){
        InputStream anteprimaPdf
        try {
            anteprimaPdf = converti(allegato, fileAllegato)
            org.zkoss.zul.Filedownload.save(anteprimaPdf, TipoFile.PDF.contentType, fileAllegato.nomePdf)
        } catch (Exception e){
            log.error(e)
            throw new AttiRuntimeException("Impossibile convertire il file \"${fileAllegato.nome}\" contenuto dall'allegato \"${allegato.titolo}\"")
        }  finally {
            IOUtils.closeQuietly((InputStream) anteprimaPdf)
        }
    }

    public InputStream converti(Allegato allegato, FileAllegato fileAllegato){
        InputStream daConvertire
        try {
            daConvertire = gestoreFile.getFile(allegato, fileAllegato)
            return gestioneTesti.convertiStreamInPdf(daConvertire, fileAllegato.nome, allegato.documentoPrincipale)
        } finally {
            IOUtils.closeQuietly((InputStream) daConvertire)
        }
    }

    public void convertiAllegatoPdf(Allegato allegato){
        if (allegato.codice.equals(Allegato.ALLEGATO_OMISSIS)) return;

        for (FileAllegato fileAllegato: allegato.fileAllegati?.findAll { it.valido == true }) {
            if (!Impostazioni.ALLEGATO_CONVERTI_PDF_FORMATO.valori.contains(FilenameUtils.getExtension(fileAllegato.nome).toLowerCase())) {
                continue;
            }

            String fileName = fileAllegato.nome
            InputStream inputStreamPdf
            try {
                inputStreamPdf = converti(allegato, fileAllegato)

                FileAllegato file = FileAllegato.get(fileAllegato.id)
                Allegato all = Allegato.get(allegato.id)

                file.nome = file.nomePdf
                file.contentType = TipoFile.PDF.contentType
                file.modificabile = false
                gestoreFile.addFile(all, file, inputStreamPdf)
                file.save(flush: true)

            } catch (Exception e) {
                log.error("Errore durante la conversione del fileAllegato ${fileAllegato.id} dell'allegato ${allegato.id}", e);
            } finally {
                IOUtils.closeQuietly((InputStream) inputStreamPdf)
            }
        }
    }

    public void convertiAllegatiPdf(IDocumento documento){
        for (Allegato allegato : documento.allegati?.findAll { it.valido == true }) {
            if (allegato.codice.equals(Allegato.ALLEGATO_OMISSIS)) {
                continue;
            }
            if (!allegato.statoFirma.equals(StatoFirma.DA_FIRMARE)) {
                continue
            }

            convertiAllegatoPdf(allegato)
        }
    }

    public boolean esistonoAllegatiNonPdf(IDocumento documento){
        for (Allegato allegato : documento.allegati?.findAll { it.valido == true }) {
            if (allegato.codice.equals(Allegato.ALLEGATO_OMISSIS)) {
                continue;
            }
            if (!allegato.statoFirma.equals(StatoFirma.DA_FIRMARE)) {
                continue
            }

            for (FileAllegato fileAllegato : allegato.fileAllegati?.findAll { it.valido == true }) {
                if (Impostazioni.ALLEGATO_CONVERTI_PDF_FORMATO.valori.contains(FilenameUtils.getExtension(fileAllegato.nome).toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean esistonoAllegatiDaFirmareNonConvertibili(IDocumento documento){
        for (Allegato allegato : documento.allegati?.findAll { it.valido == true }) {
            if (!allegato.statoFirma.equals(StatoFirma.DA_FIRMARE)) {
                continue
            }

            for (FileAllegato fileAllegato : allegato.fileAllegati?.findAll { it.valido == true }) {
                String estensione = FilenameUtils.getExtension(fileAllegato.nome).toLowerCase();
                if (!fileAllegato.isPdf() && !fileAllegato?.nome.toLowerCase().endsWith(".pdf.p7m") && !Impostazioni.ALLEGATO_CONVERTI_PDF_FORMATO.valori.contains(estensione) && Impostazioni.ALLEGATO_FORMATI_POSSIBILI.valori.contains(estensione))
                    return true
            }
        }
        return false;
    }

    /**
     * Crea un allegato riassuntivo delle firme.
     * Se non esiste, crea un Allegato con codice=ALLEGATO_RIASSUNTIVO_FIRME, lo allega alla documento.
     * Altrimenti, se l'allegato esiste già non fa nulla.
     *
     * @param documento
     */
    void creaAllegatoTestoProposta (Delibera delibera) {
        Allegato allegato = getAllegato(delibera, Allegato.ALLEGATO_TESTO_PROPOSTA)

        // se ho già l'allegato, non faccio niente
        if (allegato!= null) {
            return
        }

        if (allegato == null) {
            allegato = creaAllegato(delibera, Allegato.ALLEGATO_TESTO_PROPOSTA)
        }
        allegato.sequenza = 0
        allegato.riservato = false

        FileAllegato fileAllegato = new FileAllegato()
        fileAllegato.nome = delibera.propostaDelibera.testo.nome
        fileAllegato.contentType = delibera.propostaDelibera.testo.contentType
        fileAllegato.modificabile = false
        allegato.addToFileAllegati(fileAllegato)
        allegato.save()

        delibera.addToAllegati(allegato)
        gestioneTesti.copiaFileAllegato(delibera.propostaDelibera, delibera.propostaDelibera.testo, allegato, fileAllegato)

        delibera.save()
    }

    /**
     * Crea un allegato con la copia del testo.
     * Se non esiste, crea un Allegato con codice=ALLEGATO_COPIA_TESTO, lo allega alla documento ed aggiunge un file allegato con la copia del testo.
     * Altrimenti, se l'allegato esiste aggiunge un file allegato con la copia del testo.
     *
     * @param documento
     */
    void creaAllegatoCopiaTesto (IProposta proposta) {
        Allegato allegato = getAllegato(proposta, Allegato.ALLEGATO_COPIA_TESTO)

        if (allegato == null) {
            allegato = creaAllegato(proposta, Allegato.ALLEGATO_COPIA_TESTO)
            allegato.sequenza = 0
            allegato.riservato = false
        }

        FileAllegato fileAllegato = new FileAllegato()

        fileAllegato.nome = new Date().format("yyyyMMddHHmmss") + "_" +(springSecurityService.currentUser.nominativoSoggetto?:"")+"_"+proposta.testo.nomePdf
        fileAllegato.contentType = TipoFile.PDF.contentType
        fileAllegato.modificabile = false
        allegato.addToFileAllegati(fileAllegato)
        allegato.save()

        InputStream daConvertire
        InputStream pdf
        try {
            daConvertire = gestoreFile.getFile(proposta, proposta.testo)
            pdf = gestioneTesti.convertiStreamInPdf(daConvertire, fileAllegato.nome, proposta)
            gestoreFile.addFile(allegato, fileAllegato, pdf)
        } finally {
            IOUtils.closeQuietly((InputStream) daConvertire)
            IOUtils.closeQuietly((InputStream) pdf)
        }

        proposta.addToAllegati(allegato)

        proposta.save()
    }
}
