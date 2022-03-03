package commons

import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.IProtocolloEsterno.Classifica
import it.finmatica.atti.IProtocolloEsterno.Documento
import it.finmatica.atti.dto.documenti.AllegatoDTO
import it.finmatica.atti.dto.documenti.AllegatoDTOService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.docer.DocErConfig
import it.finmatica.docer.atti.anagrafiche.DatiRicercaDocumento
import it.finmatica.gestionetesti.TipoFile
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupRicercaDocumentiViewModel {

    // beans
    IProtocolloEsterno protocolloEsterno
    IGestoreFile gestoreFile
    AllegatoDTOService allegatoDTOService
    DocErConfig docErConfig

    // componenti
    Window self

    // dati
    Documento selectedDocumento
    AllegatoDTO allegato
    DatiRicercaDocumento datiRicerca
    List<Documento> listaDocumenti
    List<Classifica> listaClassificazioni
    Classifica selectClassifica

    List listaStatoArchivistico
    List listaStatoBusiness
    List listaTipoComponente
    List listaStatoConservazione
    List listaTipoConservazione

    Date dataAcquisizioneDal, dataAcquisizioneAl

    String codEnte, codAoo, keyWords, docNum, typeId, docName, descrizione, dataAcquisizione
    String classifica, progrFascicolo, annoFascicolo, numProtocollo, annoProtocollo
    String oggettoProtocollo, registroProtocollo, numRegistrazione, annoRegistrazione
    String oggettoRegistrazione, idRegistrazione, numPubblicazione, annoPubblicazione
    String oggettoPubblicazione, registroPubblicazione

    def statoArchivistico, statoBusinness, tipoComponente, statoConservazione, tipoConservazione

    @NotifyChange(['listaClassificazioni', 'selectClassifica'])
    @Init
    init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("allegato") AllegatoDTO allegato) {
        this.self = w
        this.allegato = allegato
        datiRicerca = new DatiRicercaDocumento()
        selectClassifica = null
        listaStatoArchivistico = []
        listaStatoBusiness = []
        listaTipoComponente = []
        listaStatoConservazione = []
        listaTipoConservazione = []
        typeId = "DETERMINA DIRIGENZIALE"
        codAoo = docErConfig.getCodiceAoo()
        codEnte = docErConfig.getCodiceEnte()
        caricaListaStatoArchivistico()
        caricaListaStatoBusiness()
        caricaListaTipoComponente()
        caricaListaClassificazioni()
        caricaListaStatoConservazione()
        caricaListaTipoConservazione()
    }

    private void caricaListaStatoArchivistico() {
        listaStatoArchivistico << [id: null, value: ""]
        listaStatoArchivistico << [id: "-1", value: "Non Definito"]
        listaStatoArchivistico << [id: "0", value: "Generico"]
        listaStatoArchivistico << [id: "1", value: "Generico (Definitivo)"]
        listaStatoArchivistico << [id: "2", value: "Registrato"]
        listaStatoArchivistico << [id: "3", value: "Protocollato"]
        listaStatoArchivistico << [id: "4", value: "Classificato"]
        listaStatoArchivistico << [id: "5", value: "Fascicolato"]
        listaStatoArchivistico << [id: "6", value: "Pubblicato"]
        listaStatoArchivistico << [id: "7", value: "In Archivio di Deposito"]
    }

    private void caricaListaStatoBusiness() {
        listaStatoBusiness << [id: null, value: ""]
        listaStatoBusiness << [id: "0", value: "Non Definito"]
        listaStatoBusiness << [id: "1", value: "Da Protocollare"]
        listaStatoBusiness << [id: "2", value: "Da Fascicolare"]
        listaStatoBusiness << [id: "3", value: "Da Registrare"]
        listaStatoBusiness << [id: "4", value: "Da Firmare"]
    }

    private void caricaListaTipoComponente() {
        listaTipoComponente << [id: null, value: ""]
        listaTipoComponente << [id: "PRINCIPALE", value: "Principale"]
        listaTipoComponente << [id: "ALLEGATO", value: "Allegato"]
        listaTipoComponente << [id: "ANNOTAZIONE", value: "Annotazione"]
        listaTipoComponente << [id: "ANNESSO", value: "Annesso"]
    }

    private void caricaListaStatoConservazione() {
        listaStatoConservazione << [id: null, value: ""]
        listaStatoConservazione << [id: "0", value: "Da non conservare"]
        listaStatoConservazione << [id: "1", value: "Da conservare"]
        listaStatoConservazione << [id: "2", value: "Inviato a conservazione"]
        listaStatoConservazione << [id: "3", value: "Conservato"]
        listaStatoConservazione << [id: "4", value: "In errore"]
    }

    private void caricaListaTipoConservazione() {
        listaTipoConservazione << [id: null, value: ""]
        listaTipoConservazione << [id: "SOSTITUTIVA", value: "Sostitutiva"]
        listaTipoConservazione << [id: "FISCALE", value: "Fiscale"]
    }

    private void caricaListaClassificazioni() {
        listaClassificazioni = protocolloEsterno.getListaClassificazioni("", "").sort { it.codice }
    }

    public String fnsubstring(String codice, String descrizione) {
        int size = 65
        descrizione = (descrizione.length() > size) ? descrizione.substring(0, size) + "....." : descrizione
        return codice + " - " + descrizione
    }

    private void creaDatiRicercaDocumento() {
        if (docNum != null)
            datiRicerca.setDocNum(docNum)
        if (typeId != null)
            datiRicerca.setTypeId(typeId)
        if (codEnte != null)
            datiRicerca.setCodEnte(codEnte)
        if (codAoo != null)
            datiRicerca.setCodAoo(codAoo)
        if (statoArchivistico != null)
            datiRicerca.setStatoArchivistico(statoArchivistico.id)
        if (statoBusinness != null)
            datiRicerca.setStatoBusinness(statoBusinness.id)
        if (docName != null)
            datiRicerca.setDocName(docName)
        if (descrizione != null)
            datiRicerca.setDescrizione(descrizione)

        if (dataAcquisizioneDal != null && dataAcquisizioneAl != null) {
            dataAcquisizione = dataAcquisizioneDal.format("yyyy-MM-dd") + "T00:00:00.000+01:00 TO " + dataAcquisizioneAl.format("yyyy-MM-dd") + "T00:00:00.000+01:00"
            datiRicerca.setDataAcquisizione(dataAcquisizione)
        }
        if (tipoComponente != null)
            datiRicerca.setTipoComponente(tipoComponente.id)
        if (selectClassifica != null)
            datiRicerca.setClassifica(selectClassifica.codice)
        if (progrFascicolo != null)
            datiRicerca.setProgrFascicolo(progrFascicolo)
        if (annoFascicolo != null)
            datiRicerca.setAnnoFascicolo(annoFascicolo)
        if (numProtocollo != null)
            datiRicerca.setNumProtocollo(numProtocollo)
        if (annoProtocollo != null)
            datiRicerca.setAnnoProtocollo(annoProtocollo)
        if (oggettoProtocollo != null)
            datiRicerca.setOggettoProtocollo(oggettoProtocollo)
        if (registroProtocollo != null)
            datiRicerca.setRegistroProtocollo(registroProtocollo)
        if (numRegistrazione != null)
            datiRicerca.setNumRegistrazione(numRegistrazione)
        if (annoRegistrazione != null)
            datiRicerca.setAnnoRegistrazione(annoRegistrazione)
        if (oggettoRegistrazione != null)
            datiRicerca.setOggettoRegistrazione(oggettoRegistrazione)
        if (idRegistrazione != null)
            datiRicerca.setIdRegistrazione(idRegistrazione)
        if (numPubblicazione != null)
            datiRicerca.setNumPubblicazione(numPubblicazione)
        if (annoPubblicazione != null)
            datiRicerca.setAnnoPubblicazione(annoPubblicazione)
        if (oggettoPubblicazione != null)
            datiRicerca.setOggettoPubblicazione(oggettoPubblicazione)
        if (registroPubblicazione != null)
            datiRicerca.setRegistroPubblicazione(registroPubblicazione)
        if (statoConservazione != null)
            datiRicerca.setStatoConservazione(statoConservazione.id)
        if (tipoConservazione != null)
            datiRicerca.setTipoConservazione(tipoConservazione.id)
        if (keyWords != null)
            datiRicerca.setKeyWords(keyWords)
    }

    @NotifyChange(['listaDocumenti'])
    @Command
    onCerca() {
        creaDatiRicercaDocumento()
        listaDocumenti = protocolloEsterno.getListaDocumenti(datiRicerca)
        BindUtils.postNotifyChange(null, null, this, "listaDocumenti")
    }

    @Command
    onChiudi() {
        Events.postEvent(Events.ON_CLOSE, self, allegato)
    }

    @Command
    onSalva() {
        caricaFileAllegato()
        onChiudi()
    }

    private void caricaFileAllegato() {
        InputStream is = protocolloEsterno.downloadFile(selectedDocumento.getDocNum());
        if (is != null) {
            allegato.titolo = selectedDocumento.getDocName()
            allegatoDTOService.uploadFile(allegato, selectedDocumento.getDocName(), TipoFile.P7M.contentType, is)
        }
    }
}
