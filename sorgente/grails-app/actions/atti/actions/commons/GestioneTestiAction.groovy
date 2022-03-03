package atti.actions.commons

import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.DeleteOnCloseFileInputStream
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.zkutils.SuccessHandler
import org.zkoss.zhtml.Filedownload

/**
 * Contiene le azioni client comuni a tutti i documenti
 * @author NDiFabio
 */
class GestioneTestiAction {

    // services
    GestioneTestiService gestioneTestiService
    StampaUnicaService   stampaUnicaService
    AllegatoService      allegatoService
    SuccessHandler       successHandler
    AttiGestioneTesti    gestioneTesti

    // beans
    IGestoreFile gestoreFile

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "Salta il controllo sul testo obbligatorio",
            descrizione = "Evita che il flusso si blocchi tra uno step e l'altro se manca il testo.")
    def saltaControlloTesto (IDocumento documento) {
        successHandler.idIterSaltaControlloTesto = documento.iter.id
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Crea la stampa unica del documento",
            descrizione = "Crea la stampa unica in pdf del documento includendo il testo principale, gli allegati, i visti e i certificati")
    def stampaUnica (IDocumento documento) {
        // prima eseguo il flush del documento:
        documento.save()
        stampaUnicaService.stampaUnica(documento)
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
            nome = "Crea la stampa unica del documento principale",
            descrizione = "Crea la stampa unica in pdf del documento principale includendo il testo principale, gli allegati, i visti e i certificati")
    def stampaUnicaDocumentoPrincipale (IDocumentoCollegato documento) {
        // prima eseguo il flush del documento:
        documento.save()
        stampaUnicaService.stampaUnica(documento.documentoPrincipale)
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "Crea il frontespizio",
            descrizione = "Crea o rigenera il frontespizio")
    def generaFrontespizio (IDocumento documento) {
        allegatoService.creaAllegatoFrontespizio(documento)
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Crea o aggiorna l'allegato Omissis",
            descrizione = "Crea l'allegato omissis copia del testo del documento principale.")
    def creaAllegatoOmissis (IDocumento documento) {
        allegatoService.creaAllegatoOmissis(documento)
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Aggiorna il testo OMISSIS con il testo oscurato",
            descrizione = "Aggiorna il testo dell'allegato OMISSIS (se presente) con il testo del documento principale con il testo barrato sostituito con spazi evidenziati di nero. Se non è già presente un allegato omissis, questo NON verrà creato e questa azione non farà nulla.")
    def aggiornaAllegatoOmissisOscurato (IDocumento documento) {
        Allegato allegato = allegatoService.getAllegato(documento, Allegato.ALLEGATO_OMISSIS)
        if (allegato == null) {
            return documento
        }
        allegatoService.aggiornaFileOmissisOscurato(documento, allegato)

        return documento
    }

    @Action(tipo = TipoAzione.CLIENT,
            tipiOggetto = [PropostaDelibera.TIPO_OGGETTO],
            nome = "Crea la Stampa della Camicia",
            descrizione = "Crea e scarica la Stampa della camicia della Proposta di Delibera.")
    void downloadStampaCamicia (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
        def propostaDelibera = viewModel.getDocumentoIterabile(false)
        GestioneTestiModello modello = GestioneTestiModello.findWhere('tipoModello.codice': "CAMICIA_PROPOSTA_DELIBERA", valido: true)
        InputStream is = gestioneTestiService.stampaUnione(modello, [id: propostaDelibera.id], Impostazioni.FORMATO_DEFAULT.valore, true)
        Filedownload.save(is, GestioneTestiService.getContentType(Impostazioni.FORMATO_DEFAULT.valore),
                          "CAMICIA_${propostaDelibera.nomeFile}.${Impostazioni.FORMATO_DEFAULT.valore}");
    }

    @Action(tipo = TipoAzione.CLIENT,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Crea Zip File",
            descrizione = "Crea e scarica un file zip con tutti i testi e gli allegati del documento.")
    void downloadZipAllegati (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
        def documento = viewModel.getDocumentoIterabile(false)
        File zipAllegati = stampaUnicaService.creaZipAllegati(documento)
        Filedownload.save(new DeleteOnCloseFileInputStream(zipAllegati), "application/zip", "${documento.nomeFile}.zip");
    }

    @Action(tipo = TipoAzione.CLIENT,
            tipiOggetto = [VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
            nome = "Crea Zip File",
            descrizione = "Crea e scarica un file zip con tutti i testi e gli allegati del documento principale.")
    void downloadZipAllegatiDocumentoPrincipale (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
        def documento = viewModel.getDocumentoIterabile(false)
        File zipAllegati = stampaUnicaService.creaZipAllegati(documento.documentoPrincipale)
        Filedownload.save(new DeleteOnCloseFileInputStream(zipAllegati), "application/zip", "${documento.nomeFile}.zip");
    }

    /*
     * Operazioni sui testi
     */

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Upload del testo",
            descrizione = "Carica il testo presente su webdav nel database.")
    def uploadTesto (def d) {
        gestioneTesti.uploadTesto(d)
        return d
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Upload e unlock del testo",
            descrizione = "Carica il testo presente su webdav nel database, quindi rilascia il lock per l'utente in sessione.")
    def uploadEUnlockTesto (def d) {
        gestioneTesti.uploadEUnlockTesto(d)
        return d
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Crea Testo PDF",
            descrizione = "Crea il Testo PDF con i dati runtime (utile per fare la stampa dopo la numerazione).")
    def creaPdfTesto (IDocumento d) {
        // creo il pdf solo se il testo solo se non è già un pdf o p7m.
        // faccio l'if così anziché verificare se modificabile o no perché (ad es. a Trezzano),
        // c'è la firma autografa che imposta il documento come firmato e non modificabile:
        // il test su modificabile quindi fallirebbe e il testo non verrebbe generato/convertito in pdf.
        if (d.testo.isPdf() || d.testo.isP7m()) {
            return d
        }

        // aggiorno il testo del documento: siccome sto trasformando in PDF, verifico anche che ci siano tutti i campi nel modello.
        gestioneTesti.generaTestoDocumento(d, true)

        // prima di convertire il testo, salvo l'originale
        gestioneTesti.salvaTestoOdt(d)

        // converto in pdf e salvo l'odt
        gestioneTesti.convertiTestoPdf(d)

        return d
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Elimina il testo",
            descrizione = "Elimina il testo del documento (solo se il testo è ancora modificabile).")
    def eliminaTesto (IDocumento d) {
        gestioneTesti.eliminaTesto (d)
        return d
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Il testo è presente?",
            descrizione = "Indica se il testo è presente")
    def isTestoPresente (IDocumento d) {
        return d.testo != null
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Il testo è modificabile?",
            descrizione = "Indica se il testo è modificabile (ritorna TRUE anche se il testo non è presente)")
    def isTestoModificabile (IDocumento d) {
        return d.testo == null || d.testo.isModificabile();
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Crea Testo (dati live)",
            descrizione = "Crea il Testo (solo se il testo è nullo) con i dati della transazione corrente (ad es. il numero della determina appena numerata). Non gestisce l'eventuale BLOB ritornato da un campo della query del modello.")
    def creaTesto (IDocumento d) {
        if (d.testo == null) {
            // genero il testo con i dati "live"
            gestioneTesti.generaTestoDocumento(d, true)
        }

        return d
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Crea Testo",
            descrizione = "Crea il Testo (solo se il testo è nullo). Questa funzione NON usa i dati della transazione corrente. Utile soprattutto per inserire il BLOB nel testo della Delibera")
    def creaTestoDatiNonLive (IDocumento d) {
        if (d.testo == null) {
            // genero il testo con i dati non "live"
            gestioneTesti.generaTestoDocumento(d, false)
        }

        return d
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Rigenera Testo",
            descrizione = "Elimina il testo e lo ricrea con i dati runtime (solo se il testo è ancora modificabile).")
    def rigeneraTesto (IDocumento d) {
        if (d.testo == null || d.testo.isModificabile()) {
            eliminaTesto(d);
            creaTesto(d);
        }

        return d
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Delibera.TIPO_OGGETTO],
            nome = "Crea l'allegato riassuntivo delle firme",
            descrizione = "Crea l'allegato riassuntivo delle firme")
    def generaAllegatoRiassuntivoFirme (IDocumento documento) {
        allegatoService.creaAllegatoRiassuntivoFirme(documento)
        return documento
    }

    @Action(tipo = TipoAzione.AUTOMATICA,
        tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
        nome = "Crea la copia del testo della proposta",
        descrizione = "Crea la copia del testo della proposta")
    def generaAllegatoCopiaTesto (IDocumento documento) {
        if (documento.testo != null) {
            allegatoService.creaAllegatoCopiaTesto(documento)
        }
        return documento
    }
}
