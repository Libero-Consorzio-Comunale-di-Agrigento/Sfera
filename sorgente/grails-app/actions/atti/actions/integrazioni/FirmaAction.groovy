package atti.actions.integrazioni

import atti.actions.commons.AttoriAction
import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.DeleteOnCloseFileInputStream
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.AllegatoService
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.EsitoVisto
import it.finmatica.atti.documenti.Firmatario
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.ISoggettoDocumento
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.beans.AttiGestoreTransazioneFirma
import it.finmatica.atti.documenti.tipologie.ParametroTipologia
import it.finmatica.atti.dto.documenti.viste.So4DelegaService
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.TipoFile
import it.finmatica.jsign.verify.api.SimpleVerifier
import it.finmatica.jsign.verify.result.CriticityLevel
import it.finmatica.jsign.verify.result.VerifyResult
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.springframework.transaction.interceptor.TransactionAspectSupport
import org.zkoss.zul.Filedownload

/**
 * Contiene le azioni per la firma dei documenti
 */
class FirmaAction {

    AttiGestoreTransazioneFirma attiGestoreTransazioneFirma
    SpringSecurityService       springSecurityService
    GestioneTestiService        gestioneTestiService
    AttiFirmaService            attiFirmaService
    AllegatoService             allegatoService
    AttiGestioneTesti           gestioneTesti
    AttoriAction                attoriAction
    IGestoreFile                gestoreFile
    So4DelegaService            so4DelegaService

    @Action(tipo = TipoAzione.CLIENT,
            tipiOggetto = [Determina.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Apre popup Firma",
            descrizione = "Apre la popup di firma")
    void apriPopupFirma (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
        // Attenzione! Il nome di questo metodo è importante! Viene usato come stringa nell'AbstractViewModel!
        // se lo si modifica, va modificato anche quel riferimento!
        viewModel.apriPopupFirma()
    }

    @Action(tipo = TipoAzione.PULSANTE,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Anteprima del Testo",
            descrizione = "Scarica l'anteprima del testo con tutti i campi valorizzati.")
    def scaricaAnteprimaTesto (def documento, AbstractViewModel<? extends IDocumentoIterabile> viewModel) {
        InputStream anteprimaIn
        InputStream anteprimaPdfIn
        OutputStream anteprimaOut
        File fileAnteprima
        try {
            anteprimaIn = gestoreFile.getFile(documento, documento.testo)
            anteprimaPdfIn = gestioneTesti.convertiStreamInPdf(anteprimaIn, documento.testo.nome, documento)
            fileAnteprima = File.createTempFile("anteprima", "tmp")
            anteprimaOut = new FileOutputStream(fileAnteprima)
            IOUtils.copy(anteprimaPdfIn, anteprimaOut)
        } finally {
            IOUtils.closeQuietly((InputStream) anteprimaIn)
            IOUtils.closeQuietly((InputStream) anteprimaPdfIn)
            IOUtils.closeQuietly((OutputStream) anteprimaOut)
        }

        OutputStream watermarkedOut
        File fileWatermarked
        try {
            fileWatermarked = File.createTempFile("watermarked", "tmp")
            anteprimaIn = new FileInputStream(fileAnteprima)
            watermarkedOut = new FileOutputStream(fileWatermarked)
            gestioneTesti.applicaWatermarkFacsimile(anteprimaIn, watermarkedOut)

            DeleteOnCloseFileInputStream deleteOnCloseFile = new DeleteOnCloseFileInputStream(fileWatermarked)
            Filedownload.save(deleteOnCloseFile, TipoFile.PDF.contentType, "anteprima.pdf")

        } finally {
            IOUtils.closeQuietly((InputStream) anteprimaIn)
            IOUtils.closeQuietly((OutputStream) watermarkedOut)
            FileUtils.deleteQuietly(fileAnteprima)
        }

        // interrompo la transazione e faccio rollback di tutto
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()

        return documento
    }

    /*
     * Azioni di firma
     */

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Verifica che il testo sia firmato digitalmente",
            descrizione = "Se il testo non è firmato digitalmente, interrompe l'operazione con un errore.")
    def verificaTestoFirmato (def documento) {
        try {
            SimpleVerifier v = new SimpleVerifier()
            VerifyResult res = v.isSigned(gestoreFile.getFile(documento, documento.testo));

            // se non è un file firmato correttamente do errore
            if (!res.isValid(CriticityLevel.HIGH)) {
                throw new AttiRuntimeException("Il testo non è firmato correttamente.")
            }
        } catch (Exception e) {
            throw new AttiRuntimeException("Non è possibile verificare la firma del testo. Il testo è firmato correttamente?");
        }

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Predispone la Firma",
            descrizione = "Conclude la transazione di firma e costruisce l'url della popup di firma.")
    def finalizzaTransazioneFirma (def documento) {
        attiGestoreTransazioneFirma.finalizzaTransazioneFirma()
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Predispone la Firma Remota",
            descrizione = "Conclude la transazione di firma remota e costruisce l'url della popup di firma.")
    def finalizzaTransazioneFirmaRemota (def documento) {
        attiGestoreTransazioneFirma.finalizzaTransazioneFirmaRemota();
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Predispone la Firma Remota PDF",
            descrizione = "Conclude la transazione di firma remota pdf e costruisce l'url della popup di firma.")
    def finalizzaTransazioneFirmaRemotaPdf (def documento) {
        attiGestoreTransazioneFirma.finalizzaTransazioneFirmaRemotaPdf();
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Predispone la Firma Autografa",
            descrizione = "Conclude la transazione di firma autografa e costruisce l'url della popup di fine firma (non passa dalla firma con smartcard).")
    def finalizzaTransazioneFirmaAutografa (def documento) {
        attiGestoreTransazioneFirma.finalizzaTransazioneFirmaAutografa();
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
            nome = "Prepara il documento per la firma con allegati.",
            descrizione = "Prepara il firmatario, aggiunge il testo del documento alla firma, aggiunge gli allegati del documento in stato DA_FIRMARE alla firma.")
    def predisponiDocumento (IDocumento documento) {

        // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
        attiFirmaService.preparaFirmatarioPerFirma(documento)

        // preparo il documento da firmare
        attiFirmaService.preparaTestoPerFirma(documento)

        // preparo gli allegati del documento che sono in stato DA_FIRMARE
        attiFirmaService.preparaAllegati(documento)

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Prepara il documento con OMISSIS per la firma con allegati.",
            descrizione = "Prepara il firmatario, prepara il testo allegato con OMISSIS, rimuove il testo barrato, aggiunge il testo del documento alla firma, aggiunge gli allegati del documento in stato DA_FIRMARE alla firma.")
    def predisponiDocumentoConOmissis (IDocumento documento) {

        // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
        attiFirmaService.preparaFirmatarioPerFirma(documento)

        // rimuovo il testo -barrato- dal testo del documento (solo se ho l'allegato omissis)
        if (documento.testo.modificabile && allegatoService.getAllegato(documento, Allegato.ALLEGATO_OMISSIS) != null) {
            // aggiorna il testo dell'allegato con omissis
            allegatoService.aggiornaFileOmissisOscurato(documento)
            gestioneTesti.rimuoviOmissis(documento, documento.testo)
        }

        // preparo il documento da firmare
        attiFirmaService.preparaTestoPerFirma(documento)

        // preparo gli allegati del documento che sono in stato DA_FIRMARE
        attiFirmaService.preparaAllegati(documento)

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Prepara documento per la firma senza trasformare il testo in pdf.",
            descrizione = "Prepara il firmatario, aggiunge il testo del documento alla firma (non lo trasforma in pdf), aggiunge gli allegati del documento in stato DA_FIRMARE alla firma.")
    def predisponiTestoNonPdf (IDocumento documento) {

        // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
        attiFirmaService.preparaFirmatarioPerFirma(documento)

        // preparo il documento da firmare
        attiFirmaService.preparaTestoPerFirma(documento, false)

        // preparo gli allegati del documento che sono in stato DA_FIRMARE
        attiFirmaService.preparaAllegati(documento)

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
        tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
        nome = "Prepara documento per la firma senza trasformare il testo in pdf e senza firmare gli allegati.",
        descrizione = "Prepara il firmatario, aggiunge il testo del documento alla firma (non lo trasforma in pdf), senza gli allegati")
    def predisponiTestoNonPdfSenzaAllegati (IDocumento documento) {

        // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
        attiFirmaService.preparaFirmatarioPerFirma(documento)

        // preparo il documento da firmare
        attiFirmaService.preparaTestoPerFirma(documento, false)

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [VistoParere.TIPO_OGGETTO],
            nome = "Prepara il documento per la firma con anche gli allegati del documento principale",
            descrizione = "Prepara il firmatario, aggiunge il testo del documento alla firma, aggiunge gli allegati del documento principale.")
    def predisponiDocumentoConAllegatiDocumentoPrincipale (VistoParere documento) {

        // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
        attiFirmaService.preparaFirmatarioPerFirma(documento)

        // preparo il documento da firmare
        attiFirmaService.preparaTestoPerFirma(documento)

        // preparo gli allegati del documento principale:
        // - la preparazione dei file allegati imposta lo stato firma = IN_FIRMA sul documento principale (perché in caso di firma dei soli allegati del documento principale, è giusto che questo diventi "IN_FIRMA")
        // - però allo sblocco dei documenti dopo la firma, si riceverà un errore perché non c'è un firmatario predisposto per il documento principale (siccome sarà in stato FIRMATO_DA_SBLOCCARE si tenterà di sbloccarlo)
        // - per questa ragione, salvo lo statoFirma corrente del documento principale, poi lo ripristino dopo averne preparato gli allegati.
        // questa modifica è la meno invasiva per gestire l'errore accaduto a Vigevano. Bisognerà poi ripensare alla gestione della firma per questi casi particolari.
        StatoFirma statoFirmaDocumentoPrincipale = documento.documentoPrincipale.statoFirma
        attiFirmaService.preparaAllegati(documento.documentoPrincipale)
        documento.documentoPrincipale.statoFirma = statoFirmaDocumentoPrincipale
        documento.documentoPrincipale.save()

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
        tipiOggetto = [VistoParere.TIPO_OGGETTO],
        nome = "Prepara il documento per la firma con anche il testo del documento principale",
        descrizione = "Prepara il firmatario, aggiunge il testo del documento alla firma, aggiunge il testo del documento principale.")
    def predisponiDocumentoConTestoDocumentoPrincipale (VistoParere documento) {

        // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
        attiFirmaService.preparaFirmatarioPerFirma(documento)
        attiFirmaService.addFirmatario(documento.documentoPrincipale, Firmatario.cheStaFirmando(documento).get().firmatario)
        attiFirmaService.preparaFirmatarioPerFirma(documento.documentoPrincipale)

        // preparo il documento da firmare
        attiFirmaService.preparaTestoPerFirma(documento)

        // preparo il testo del documento principale
        attiFirmaService.preparaTestoPerFirma(documento.documentoPrincipale)

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Prepara il documento per la firma senza allegati.",
            descrizione = "Prepara il firmatario, aggiunge il testo del documento alla firma, non aggiunge gli allegati.")
    def predisponiDocumentoSenzaAllegati (IDocumento documento) {

        // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
        attiFirmaService.preparaFirmatarioPerFirma(documento)

        // preparo il documento da firmare
        attiFirmaService.preparaTestoPerFirma(documento)

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Prepara il documento per la firma del testo da fuori applicativo.",
            descrizione = "Prepara il firmatario esterno (l'utente corrente può non corrispondere all'utente firmatario predisposto). Inoltre si prevede che il testo sia già stato firmato esternamente.")
    def predisponiDocumentoFirmaEsterno (IDocumento documento) {

        // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
        attiFirmaService.preparaFirmatarioPerFirma(documento, false)

        // preparo il documento da firmare
        attiFirmaService.preparaTestoPerFirma(documento, false)

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
            nome = "Prepara gli allegati per la firma.",
            descrizione = "Aggiunge gli allegati del documento contrassegnati come DA_FIRMARE alla firma.")
    def predisponiAllegati (IDocumento documento) {

        // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
        attiFirmaService.preparaFirmatarioPerFirma(documento)

        // preparo gli allegati del documento principale
        attiFirmaService.preparaAllegati(documento)

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [VistoParere.TIPO_OGGETTO],
            nome = "Prepara per la firma l'allegato SCHEDA_CONTABILE del documento principale",
            descrizione = "Prepara il firmatario, aggiunge l'allegato SCHEDA_CONTABILE del documento principale.")
    def predisponiAllegatoSchedaContabileDocumentoPrincipale (VistoParere documento) {

        // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
        attiFirmaService.preparaFirmatarioPerFirma(documento)

        // preparo l'allegato scheda contabile del documento principale:
        // - la preparazione dei file allegati imposta lo stato firma = IN_FIRMA sul documento principale (perché in caso di firma dei soli allegati del documento principale, è giusto che questo diventi "IN_FIRMA")
        // - però allo sblocco dei documenti dopo la firma, si riceverà un errore perché non c'è un firmatario predisposto per il documento principale (siccome sarà in stato FIRMATO_DA_SBLOCCARE si tenterà di sbloccarlo)
        // - per questa ragione, salvo lo statoFirma corrente del documento principale, poi lo ripristino dopo averne preparato gli allegati.
        // questa modifica è la meno invasiva per gestire l'errore accaduto a Vigevano. Bisognerà poi ripensare alla gestione della firma per questi casi particolari.
        StatoFirma statoFirmaDocumentoPrincipale = documento.documentoPrincipale.statoFirma
        attiFirmaService.preparaAllegatoSchedaContabile(documento.documentoPrincipale)
        documento.documentoPrincipale.statoFirma = statoFirmaDocumentoPrincipale
        documento.documentoPrincipale.save()

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [VistoParere.TIPO_OGGETTO],
            nome = "Prepara per la firma l'allegato SCHEDA_CONTABILE_ENTRATA del documento principale",
            descrizione = "Prepara il firmatario, aggiunge l'allegato SCHEDA_CONTABILE_ENTRATA del documento principale.")
    def predisponiAllegatoSchedaContabileEntrataDocumentoPrincipale (VistoParere documento) {

        // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
        attiFirmaService.preparaFirmatarioPerFirma(documento)

        // preparo l'allegato scheda contabile del documento principale:
        // - la preparazione dei file allegati imposta lo stato firma = IN_FIRMA sul documento principale (perché in caso di firma dei soli allegati del documento principale, è giusto che questo diventi "IN_FIRMA")
        // - però allo sblocco dei documenti dopo la firma, si riceverà un errore perché non c'è un firmatario predisposto per il documento principale (siccome sarà in stato FIRMATO_DA_SBLOCCARE si tenterà di sbloccarlo)
        // - per questa ragione, salvo lo statoFirma corrente del documento principale, poi lo ripristino dopo averne preparato gli allegati.
        // questa modifica è la meno invasiva per gestire l'errore accaduto a Vigevano. Bisognerà poi ripensare alla gestione della firma per questi casi particolari.
        StatoFirma statoFirmaDocumentoPrincipale = documento.documentoPrincipale.statoFirma
        attiFirmaService.preparaAllegatoSchedaContabile(documento.documentoPrincipale, Allegato.ALLEGATO_SCHEDA_CONTABILE_ENTRATA)
        documento.documentoPrincipale.statoFirma = statoFirmaDocumentoPrincipale
        documento.documentoPrincipale.save()

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
            nome = "Prepara l'allegato SCHEDA_CONTABILE per la firma.",
            descrizione = "Aggiunge gli allegati SCHEDA_CONTABILE contrassegnati come DA_FIRMARE alla firma.")
    def predisponiAllegatoSchedaContabile (IDocumento documento) {

        // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
        attiFirmaService.preparaFirmatarioPerFirma(documento)

        // preparo l'allegato scheda contabile per la firma
        attiFirmaService.preparaAllegatoSchedaContabile(documento)

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
            nome = "Prepara l'allegato SCHEDA_CONTABILE_ENTRATA per la firma.",
            descrizione = "Aggiunge gli allegati SCHEDA_CONTABILE_ENTRATA contrassegnati come DA_FIRMARE alla firma.")
    def predisponiAllegatoSchedaContabileEntrata (IDocumento documento) {

        // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
        attiFirmaService.preparaFirmatarioPerFirma(documento)

        // preparo l'allegato scheda contabile per la firma
        attiFirmaService.preparaAllegatoSchedaContabile(documento, Allegato.ALLEGATO_SCHEDA_CONTABILE_ENTRATA)

        return documento
    }


    @Action(tipo = TipoAzione.AUTOMATICA,
        tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
        nome = "Prepara l'allegato ALLEGATO_COPIA_TESTO per la firma.",
        descrizione = "Aggiunge gli allegati ALLEGATO_COPIA_TESTO contrassegnati come DA_FIRMARE alla firma.")
    def predisponiAllegatoCopiaTesto (IDocumento documento) {

        // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
        attiFirmaService.preparaFirmatarioPerFirma(documento)

        // preparo l'allegato scheda contabile per la firma
        attiFirmaService.preparaAllegatoSchedaContabile(documento, Allegato.ALLEGATO_COPIA_TESTO, true)

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
        tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
        nome = "Imposta l'allegato ALLEGATO_COPIA_TESTO come DA_NON_FIRMARE",
        descrizione = "Imposta l'allegato ALLEGATO_COPIA_TESTO come DA_NON_FIRMARE. Se non esiste non viene creato.")
    def predisponiCopiaTestoDaNonFirmare (IDocumento documento) {
        Allegato copiaTesto = allegatoService.getAllegato(documento, Allegato.ALLEGATO_COPIA_TESTO);

        // se non ho il la copia del testo, non faccio nulla ed esco
        if (copiaTesto == null) {
            return documento
        }

        copiaTesto.statoFirma = StatoFirma.DA_NON_FIRMARE;
        copiaTesto.save()
        return documento
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
        tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
        nome = "L'allegato ALLEGATO_COPIA_TESTO è firmato?",
        descrizione = "Ritorna TRUE se l'allegato ALLEGATO_COPIA_TESTO ha stato_firma = FIRMATO, false altrimenti")
    boolean isStatoFirmaAllegatoCopiaTestoFirmato (IDocumento d) {
        Allegato copiaTesto = allegatoService.getAllegato(documento, Allegato.ALLEGATO_COPIA_TESTO);

        // se non ho il la copia del testo, non faccio nulla ed esco
        if (copiaTesto == null) {
            return true
        }
        else {
            return copiaTesto.statoFirma == StatoFirma.FIRMATO
        }
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "Imposta il frontespizio come DA_FIRMARE",
            descrizione = "Imposta il frontespizio come DA_FIRMARE. Se non esiste non viene creato. Dopo questa azione va invocata la 'Predisponi gli allegati per firma'.")
    def predisponiFrontespizio (IDocumento documento) {
        Allegato frontespizio = allegatoService.getAllegato(documento, Allegato.ALLEGATO_FRONTESPIZIO);

        // se non ho il frontespizio, non faccio nulla ed esco
        if (frontespizio == null) {
            return documento
        }

        // imposto il frontespizio come DA_FIRMARE.
        frontespizio.statoFirma = StatoFirma.DA_FIRMARE;
        frontespizio.save()
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "Prepara i visti/pareri collegati per la firma.",
            descrizione = """Aggiunge il testo dei visti/pareri con il CODICE_VISTO specificato in tipologia alla firma.
Se il visto/parere non ha esito, viene assegnato l'esito FAVOREVOLE.
Vengono considerati solo i visti il cui firmatario è l'utente corrente.""",
            codiciParametri = ["CODICE_VISTO"],
            descrizioniParametri = ["Codice del visto/parere da firmare"])
    def predisponiVisti (IDocumento documento) {
        // verifico se devo firmare un visto separatamente:
        // Recupero del parametro dalla tipologia
        String codiceVisto = ParametroTipologia.getValoreParametro(documento.tipologiaDocumento, documento.iter.stepCorrente.cfgStep, "CODICE_VISTO")
        if (codiceVisto != null && codiceVisto.length() > 0) {

            // cerco il visto con il codice richiesto:
            for (VistoParere vp : documento.visti) {
                if (vp.valido && vp.tipologia.codice == codiceVisto) {

                    if (vp.firmatario == null) {
                        throw new AttiRuntimeException(
                                "Non è possibile proseguire: non è stato specificato il firmatario del visto/parere ${vp.tipologia.titolo}");
                    }

                    // prendo solo i visti di cui io sono il firmatario:
                    if (vp.firmatario.id != springSecurityService.currentUser.id && !so4DelegaService.hasDelega(springSecurityService.currentUser, vp.firmatario)) {
                        log.warn(
                                "Il firmatario del visto ${vp.id} è l'utente ${vp.firmatario.id} mentre l'utente corrente è: ${springSecurityService.currentUser.id}")
                        continue;
                    }

                    // se il visto non ha un esito, metto automaticamente favorevole.
                    if (vp.esito == null || vp.esito == EsitoVisto.DA_VALUTARE) {
                        vp.esito = EsitoVisto.FAVOREVOLE;
                    }

                    // aggiungo il firmatario alla coda dei soggetti che dovranno firmare
                    attiFirmaService.addFirmatario(vp, vp.firmatario)

                    // per dare la possibilità di calcolare correttamente la data di firma sul testo, preparo (se non lo è già), il firmatario:
                    attiFirmaService.preparaFirmatarioPerFirma(vp)
                    attiFirmaService.preparaTestoPerFirma(vp)
                }
            }
        }

        return documento
    }

    /*
     * Azioni sui firmatari
     */

    // http://svi-redmine/issues/12620 Proposta di delibera firmata dal relatore
    @Action(tipo = TipoAzione.AUTOMATICA_CALCOLO_ATTORE,
            tipiOggetto = [PropostaDelibera.TIPO_OGGETTO],
            nome = "Aggiunge il Relatore per la firma.",
            descrizione = "Aggiunge il Relatore alla coda dei firmatari per la firma. Se il relatore è un soggetto senza utente AD4, quest'azione ritornerà un errore bloccante.")
    def addFirmatarioRelatore (PropostaDelibera documento) {

        if (documento.delega == null) {
            throw new AttiRuntimeException("Non è possibile aggiungere il Relatore come firmatario siccome non è stato impostato sul documento.")
        }

        Ad4Utente utenteAd4 = documento.delega.assessore?.utenteAd4;
        if (utenteAd4 == null) {
            throw new AttiRuntimeException(
                    "Non è possibile aggiungere il Relatore come firmatario perché il soggetto richiesto non possiede un utente applicativo.")
        }

        attiFirmaService.addFirmatario(documento, utenteAd4)
        return documento
    }

    @Action(tipo = TipoAzione.CALCOLO_ATTORE,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Ritorna il prossimo utente firmatario",
            descrizione = "Ritorna l'attore firmatario che è prossimo alla firma")
    def getProssimoFirmatario (def documento, boolean ingresso) {
        // se sto invocando questa funzione per il calcolo delle competenze "in ingresso", allora devo prendere il firmatario che non ha ancora firmato.
        // la competenza sarà "in ingresso" anche in caso di valutazione del pulsante di firma che usa questo attore come competenza.
        // se invece sto invocando questa funzione per il calcolo delle competenze "in uscita", allora devo prendere gli attori che hanno in carico il documento
        if (ingresso) {

            Firmatario firmatario;
            // se il documento è in fase di firma interrotta, devo ritornare l'utente che "sta firmando"
            if (documento.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE || documento.statoFirma == StatoFirma.IN_FIRMA) {
                firmatario = Firmatario.cheStaFirmando(documento).get();
            } else {
                firmatario = Firmatario.prossimoCheDeveFirmare(documento).get();
            }

            if (firmatario == null) {
                throw new AttiRuntimeException(
                        "Non c'è nessun firmatario predisposto per la firma del documento ${documento.class.name} (${documento.id})")
            }

            return new Attore(utenteAd4: firmatario.firmatario)
        } else {
            // se sono in "uscita" devo togliere le competenze a chi ha in carico il documento.
            // è un sistema un po' grossolano ma funziona.
            // faccio così perché per le ASL, è possibile sbloccare il documento principale partendo da un parere, senza firmare il documento principale.
            // non firmando il documento principale, la funzione "ultimoCheHaFirmato" non trova nulla (nel caso del primo soggetto che firma) oppure troverebbe un soggetto sbagliato (nel caso del secondo o successivo firmatario)
            // pertanto, ritorno tutti i soggetti che hanno in carico il documento (che in fase di firma "dovrebbe" essere uno solo) e tolgo a loro le competenze.
            return attoriAction.getAttoriInCarico(documento)
        }
    }

    @Action(tipo = TipoAzione.CALCOLO_ATTORE,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Ritorna l'ultimo utente firmatario",
            descrizione = "Ritorna l'attore firmatario che ha firmato per ultimo")
    def getUltimoFirmatario (def documento) {
        Firmatario firmatario = Firmatario.ultimoCheHaFirmato(documento).get();

        if (firmatario == null) {
            throw new AttiRuntimeException("Non c'è nessun firmatario per il documento ${documento.class.name} (${documento.id})")
        }

        return new Attore(utenteAd4: firmatario.firmatario)
    }

    @Action(tipo = TipoAzione.CALCOLO_ATTORE,
            tipiOggetto = [VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
            nome = "Ritorna l'ultimo utente firmatario del Documento Principale",
            descrizione = "Ritorna l'attore firmatario che ha firmato per ultimo il Documento Principale")
    def getUltimoFirmatarioDocumentoPrincipale (def documento) {
        return getUltimoFirmatario(documento.documentoPrincipale)
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
            nome = "Elimina il primo firmatario del Documento Principale",
            descrizione = "Elimina il primo firmatario del Documento Principale che non ha ancora firmato.")
    def eliminaPrimoFirmatarioDocumentoPrincipale (def documento) {
        attiFirmaService.eliminaPrimoFirmatario(documento.documentoPrincipale);
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA_CALCOLO_ATTORE,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Elimina i firmatari che non hanno firmato",
            descrizione = "Elimina i firmatari in coda per la firma che non hanno ancora firmato. Utile per quando il documento \"torna indietro\".")
    def eliminaFirmatari (def documento) {
        attiFirmaService.eliminaFirmatari(documento);
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA_CALCOLO_ATTORE,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Aggiunge il Dirigente per la firma.",
            descrizione = "Aggiunge il Dirigente alla coda dei firmatari per la firma.")
    def addFirmatarioDirigente (def documento) {
        Ad4Utente utenteAd4 = documento.getSoggetto(TipoSoggetto.DIRIGENTE).utenteAd4
        attiFirmaService.addFirmatario(documento, utenteAd4)
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA_CALCOLO_ATTORE,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Aggiunge il Funzionario per la firma.",
            descrizione = "Aggiunge il Funzionario alla coda dei firmatari per la firma.")
    def addFirmatarioFunzionario (def documento) {
        Ad4Utente utenteAd4 = documento.getSoggetto(TipoSoggetto.FUNZIONARIO)?.utenteAd4

        if (utenteAd4 == null) {
            throw new AttiRuntimeException("Non è possibile aggiungere il funzionario alla firma perché non è presente sul documento!");
        }

        attiFirmaService.addFirmatario(documento, utenteAd4)
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA_CALCOLO_ATTORE,
            tipiOggetto = [Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Aggiunge il Segretario per la firma.",
            descrizione = "Aggiunge il Segretario alla coda dei firmatari per la firma.")
    def addFirmatarioSegretario (def documento) {
        Ad4Utente utenteAd4 = documento.getSoggetto(TipoSoggetto.SEGRETARIO).utenteAd4
        attiFirmaService.addFirmatario(documento, utenteAd4)
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA_CALCOLO_ATTORE,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Aggiunge tutti soggetti FIRMATARIO per la firma.",
            descrizione = "Aggiunge tutti soggetti FIRMATARIO alla coda dei firmatari per la firma.")
    def addFirmatari (IDocumento documento) {
        List<ISoggettoDocumento> firmatari = documento.soggetti.findAll { it.tipoSoggetto.codice == TipoSoggetto.FIRMATARIO }.sort { it.sequenza }
        for (ISoggettoDocumento firmatario : firmatari) {
            attiFirmaService.addFirmatario(documento, firmatario.utenteAd4)
        }
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA_CALCOLO_ATTORE,
            tipiOggetto = [VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
            nome = "Aggiunge il Firmatario di default per la firma.",
            descrizione = "Aggiunge il Firmatario di default alla coda dei firmatari per la firma, se il firmatario non è valorizzato, utilizza l'utente corrente che viene anche impostato sul documento.")
    def addFirmatarioDefault (def documento) {
        if (documento.firmatario == null) {
            documento.firmatario = springSecurityService.currentUser
            documento.save()
        }

        attiFirmaService.addFirmatario(documento, documento.firmatario)

        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA_CALCOLO_ATTORE,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Aggiunge l'utente corrente come firmatario.",
            descrizione = "Aggiunge l'utente corrente come firmatario alla coda dei firmatari per la firma.")
    def addFirmatarioUtenteCorrente (def documento) {
        Ad4Utente utenteAd4 = springSecurityService.currentUser
        attiFirmaService.addFirmatario(documento, utenteAd4)
        return documento
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "C'è un firmatario che deve firmare?",
            descrizione = "Ritorna TRUE se il documento ha un firmatario che ancora non ha firmato.")
    boolean existsFirmatario (IDocumento d) {
        return (Firmatario.inCodaPerFirmare(d).count() > 0);
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Il Firmatario predisposto è l'ultimo?",
            descrizione = "Ritorna TRUE se il firmatario che deve firmare è l'ultimo predisposto.")
    boolean isProssimoFirmatarioUltimo (IDocumento d) {
        Firmatario prossimo = Firmatario.prossimoCheDeveFirmare(d).get();
        if (prossimo == null) {
            return false;
        }

        Firmatario ultimo = Firmatario.ultimoCheDeveFirmare(d).get();
        if (ultimo == null) {
            return false;
        }

        return (prossimo.firmatario.id == ultimo.firmatario.id)
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Delibera.TIPO_OGGETTO],
            nome = "Il Firmatario predisposto è il DIRETTORE_GENERALE?",
            descrizione = "Ritorna TRUE se il firmatario che deve firmare è il DIRETTORE_GENERALE.")
    boolean isProssimoFirmatarioDirettoreGenerale (Delibera delibera) {
        Firmatario prossimo = Firmatario.prossimoCheDeveFirmare(delibera).get();
        if (prossimo == null) {
            return false;
        }

        Ad4Utente direttoreGenerale = delibera.getSoggetto(TipoSoggetto.DIRETTORE_GENERALE)?.utenteAd4;
        if (direttoreGenerale == null) {
            return false;
        }

        return (prossimo.firmatario.id == direttoreGenerale.id)
    }

    /*
     * Azioni sugli stati di firma
     */

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Imposta il documento come DA FIRMARE",
            descrizione = "Imposta il campo stato_firma = DA_FIRMARE")
    def setStatoFirmaDaFirmare (IDocumento d) {
        d.statoFirma = StatoFirma.DA_FIRMARE
        return d
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Il documento da firmare?",
            descrizione = "Ritorna TRUE se il documento ha stato_firma = DA_FIRMARE, false altrimenti")
    boolean isStatoFirmaDaFirmare (IDocumento d) {
        return (d.statoFirma == StatoFirma.DA_FIRMARE)
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Imposta il documento come FIRMATO",
            descrizione = "Imposta il campo stato_firma = FIRMATO")
    def setStatoFirmaFirmato (IDocumento d) {
        d.statoFirma = StatoFirma.FIRMATO
        return d
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Il documento è in stato firmato?",
            descrizione = "Ritorna TRUE se il documento ha stato_firma = FIRMATO, false altrimenti")
    boolean isStatoFirmaFirmato (IDocumento d) {
        return (d.statoFirma == StatoFirma.FIRMATO)
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Il documento è in firma?",
            descrizione = "Ritorna TRUE se il documento ha stato_firma = IN_FIRMA, false altrimenti")
    boolean isStatoFirmaInFirma (IDocumento d) {
        return (d.statoFirma == StatoFirma.IN_FIRMA)
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Imposta il documento come DA NON FIRMARE",
            descrizione = "Imposta il campo stato_firma = DA_NON_FIRMARE")
    def setStatoFirmaDaNonFirmare (IDocumento d) {
        d.statoFirma = StatoFirma.DA_NON_FIRMARE
        return d
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Il documento è da non firmare?",
            descrizione = "Ritorna TRUE se il documento ha stato_firma = DA_NON_FIRMARE, false altrimenti")
    boolean isStatoFirmaDaNonFirmare (IDocumento d) {
        return (d.statoFirma == StatoFirma.DA_NON_FIRMARE)
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
            nome = "Il documento non è già stato firmato?",
            descrizione = "Ritorna TRUE se nessun firmatario ha firmato il documento, FALSE altrimenti.")
    boolean isDocumentoNonFirmato (IDocumento d) {
        return !attiFirmaService.isDocumentoFirmato(d)
    }
}
