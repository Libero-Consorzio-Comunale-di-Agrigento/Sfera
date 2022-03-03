package atti.actions.commons

import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.documenti.DatoAggiuntivo
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.TipoDatoAggiuntivo
import it.finmatica.atti.documenti.viste.RicercaSiav
import it.finmatica.atti.dto.documenti.DatoAggiuntivoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.gestioneiter.annotations.Action

class DatiAggiuntiviAction {

    DatiAggiuntiviService datiAggiuntiviService

    @Action(tipo = Action.TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO],
            nome = "Verifica la presenza dei RIFLESSI_CONTABILI",
            descrizione = "Verifica che per la Determina sia valorizzato il dato aggiuntivo Riflessi Contabili. Interrompe l'esecuzione in caso il campo non sia valorizzato.")
    def verificaRiflessiContabili(Determina documento) {
        if (!datiAggiuntiviService.isDatoPresente(documento, TipoDatoAggiuntivo.RIFLESSI_CONTABILI)) {
            throw new AttiRuntimeException("Non è possibile proseguire. Il campo '${TipoDatoAggiuntivo.getDescrizione(TipoDatoAggiuntivo.RIFLESSI_CONTABILI)}' non valorizzato.")
        }
        return documento
    }


    @Action(tipo = Action.TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Verifica la presenza del codice protocollante SIAV",
            descrizione = "Verifica che per la Determina sia valorizzato il dato aggiuntivo Codice Protocollante Siav. Interrompe l'esecuzione in caso il campo non sia valorizzato.")
    def verificaCodiceProtocollazioneSiav (IDocumento documento) {
        if (!datiAggiuntiviService.isDatoPresente(documento, TipoDatoAggiuntivo.PROTOCOLLO_SIAV_STRUTTURA)) {
            throw new AttiRuntimeException("Non è possibile proseguire. Il campo '${TipoDatoAggiuntivo.getDescrizione(TipoDatoAggiuntivo.PROTOCOLLO_SIAV_STRUTTURA)}' non valorizzato.")
        }
        return documento
    }

    @Action(tipo = Action.TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Controlla il Settore Protocollo",
            descrizione = "Controlla che sulla proposta sia riportato il Settore Protocollo. Se non presente, interrompe l'esecuzione.")
    def controllaSettoreProtocollo(IProposta documento) {
        //viene verificato che sia abilitata la gestione dei aggiuntivi per il protocollo siav
        if (TipoDatoAggiuntivo.isAbilitato(TipoDatoAggiuntivo.PROTOCOLLO_SIAV_STRUTTURA)) {
            //nel caso non sia presente il dato aggiuntivo proviamo a calcolarlo
            salvaSettoreProtocollo(documento)

            DatoAggiuntivo datoAggiuntivo = datiAggiuntiviService.getDatoAggiuntivo(documento, TipoDatoAggiuntivo.PROTOCOLLO_SIAV_STRUTTURA)

            if (datoAggiuntivo == null || datoAggiuntivo.valore?.equals("-1")) {
                // se il dato aggiuntivo non è presente o ha un valore non valido allora restituiamo un errore
                throw new AttiRuntimeException("Non è possibile procedere: Settore Protocollo obbligatorio")
            }
        }
        return documento
    }

    @Action(tipo = Action.TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Salva il Settore Protocollo se univoco",
            descrizione = "Calcola il Settore Protocollo in base alla struttura proponente e lo salva solo nel caso in cui sia univoco.")
    IProposta salvaSettoreProtocollo(IProposta documento) {
        if (TipoDatoAggiuntivo.isAbilitato(TipoDatoAggiuntivo.PROTOCOLLO_SIAV_STRUTTURA)) {
            DatoAggiuntivo datoAggiuntivo = datiAggiuntiviService.getDatoAggiuntivo(documento, TipoDatoAggiuntivo.PROTOCOLLO_SIAV_STRUTTURA)
            if (datoAggiuntivo != null) {
                //verifichiamo che il valore sia ancora valido, non sarà valido nel caso di cambio dell'unità proponente
                RicercaSiav elemento = RicercaSiav.findByCodiceStrutturaAndCodiceSiav(documento.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.progr, datoAggiuntivo?.valore)
                if (elemento == null) {
                    //nel caso in cui il valore non sia più valido eliminiamo il vecchio valore e ricalcoliamo il nuovo
                    documento.toDTO().removeFromDatiAggiuntivi(datoAggiuntivo.toDTO())
                    datoAggiuntivo.delete()
                    datoAggiuntivo = null
                }
            }
            if (datoAggiuntivo == null) {
                //calcoliamo la lista dei codici siav collegati all'unità proponente
                List<RicercaSiav> elementi = RicercaSiav.findAllByCodiceStruttura(documento.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.progr)
                if (elementi?.size() == 1) {
                    // se è presente un solo codice allora creiamo il record nei dati aggiuntivi
                    RicercaSiav ricercaSiav = elementi.get(0)
                    DatoAggiuntivoDTO datoAggiuntivoDTO = new DatoAggiuntivoDTO()
                    datoAggiuntivoDTO.codice = TipoDatoAggiuntivo.PROTOCOLLO_SIAV_STRUTTURA
                    datoAggiuntivoDTO.valore = ricercaSiav.codiceSiav
                    def dto = documento.toDTO()
                    dto.addToDatiAggiuntivi(datoAggiuntivoDTO)
                    datiAggiuntiviService.salvaDatiAggiuntivi(documento, dto)
                    documento.save()
                }
            }
        }
        return documento
    }

    @Action(tipo = Action.TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "Verifica la presenza dell'estratto",
            descrizione = "Verifica che sia valorizzato il dato aggiuntivo Estratto. Interrompe l'esecuzione in caso il campo non sia valorizzato.")
    def verificaEstratto(IDocumento documento) {
        if (TipoDatoAggiuntivo.isAbilitato(TipoDatoAggiuntivo.ESTRATTO) && !datiAggiuntiviService.isDatoPresente(documento, TipoDatoAggiuntivo.ESTRATTO)) {
            throw new AttiRuntimeException("Non è possibile proseguire. Il campo '${TipoDatoAggiuntivo.getDescrizione(TipoDatoAggiuntivo.ESTRATTO)}' non valorizzato.")
        }
        return documento
    }

    @Action(tipo = Action.TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome = "Ritorna TRUE se il campo ESTRATTO è pieno",
            descrizione = "Ritorna TRUE se il campo ESTRATTO è pieno.")
    public boolean estrattoPresente(def d) {
        if (TipoDatoAggiuntivo.isAbilitato(TipoDatoAggiuntivo.ESTRATTO) && datiAggiuntiviService.isDatoPresente(d, TipoDatoAggiuntivo.ESTRATTO)) {
            return true;
        }
        return false;

    }
}