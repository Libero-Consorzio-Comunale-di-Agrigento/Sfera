package it.finmatica.atti.integrazioni.contabilita

import grails.compiler.GrailsCompileStatic
import groovy.transform.CompileDynamic
import it.finmatica.atti.cf.integrazione.AttoCf
import it.finmatica.atti.cf.integrazione.IAttiIntegrazioneServiceCf
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.tipologie.TipoDocumentoCf
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.zkoss.bind.BindUtils
import org.zkoss.zk.ui.event.EventQueues

@Component("integrazioneContabilitaCfa")
@Lazy
@GrailsCompileStatic
class IntegrazioneContabilitaCfa extends AbstractIntegrazioneContabilita {

    @Autowired(required = false) IAttiIntegrazioneServiceCf attiCfIntegrazioneService
    @Autowired IntegrazioneContabilitaCf4 integrazioneContabilitaCf4

    boolean isLiquidazioneInteramentePagata (IDocumento documento) {
        return (attiCfIntegrazioneService.isLiquidazioneInteramentePagata(getAttoCf(documento)))
    }

    String getUrlControlloContabileDistintaLiquidazione (IDocumento documento) {
        return attiCfIntegrazioneService.getUrlControlloContabileDistintaLiquidazione(getAttoCf(documento))
    }

    boolean esistonoLiquidazioniDistinta(IDocumento documento){
        return attiCfIntegrazioneService.getCountLiqDistinta(getAttoCf(documento)) > 0
    }

    void impostaStatoDistintaInclusaInAtto(IDocumento documento){
        attiCfIntegrazioneService.aggiornaStatoDistinta(getAttoCf(documento), attiCfIntegrazioneService.STATO_DISTINTA_INCLUSA_IN_ATTO)
    }

    List<IDocumento> getDocumentiDaSbloccare () {
        def documentiDaSbloccare = []
        documentiDaSbloccare.addAll(getDocumentiInAttesa(Determina))
        documentiDaSbloccare.addAll(getDocumentiInAttesa(Delibera))
        documentiDaSbloccare.addAll(getDocumentiInAttesa(PropostaDelibera))
        return documentiDaSbloccare
    }

    @CompileDynamic
    private List<IDocumento> getDocumentiInAttesa (Class<?> DomainClass) {
        return DomainClass.createCriteria().list {
            iter {
                stepCorrente {
                    cfgStep {
                        condizioneSblocco {
                            eq "nomeBean", "contabilitaAction"
                            eq "nomeMetodo", "isLiquidazioneInteramentePagata"
                        }
                    }
                }
            }
        }
    }

    @Override
    String getZul (IDocumento documento) {
        if (isContabilitaSolaLettura(documento)) {
            return integrazioneContabilitaCf4.getZul(documento);
        } else {
            return "/atti/cf/index.zul";
        }
    }

    @Override
    boolean isConDocumentiContabili (IDocumento documento) {
        if (isContabilitaSolaLettura(documento)) {
            return (integrazioneContabilitaCf4.getMovimentiContabili(documento)?.size() > 0)
        } else {
            return (getMovimentiContabili(documento)?.size() > 0)
        }
    }

    void aggiornaMaschera (IDocumento documento, boolean modifica) {
        if (isContabilitaSolaLettura(documento)) {
            integrazioneContabilitaCf4.aggiornaMaschera(documento, modifica)
        } else {
            BindUtils.postGlobalCommand("cfQueue", EventQueues.DESKTOP, "aggiornaAtto",
                                        [atto: getAttoCf(documento), competenza: modifica ? "W" : "R"])
        }
    }

    List<?> getMovimentiContabili (IDocumento documento) {
        if (isContabilitaSolaLettura(documento)) {
            return integrazioneContabilitaCf4.getMovimentiContabili(documento);
        } else {
            return attiCfIntegrazioneService.getDocumentiContabili(getAttoCf(documento));
        }
    }

    void salvaProposta (IProposta proposta) {
        attiCfIntegrazioneService.creaAggiornaProposta(getAttoCf(proposta));
    }

    void annullaProposta (IProposta proposta) {
        attiCfIntegrazioneService.annullaMovimentiProposta(getAttoCf(proposta));
    }

    void salvaAtto (IAtto atto) {
        attiCfIntegrazioneService.creaAggiornaAtto(getAttoCf(atto));
    }

    @CompileDynamic
    void rendiEsecutivoAtto (IAtto atto) {
        // in contabilità lasciare il flag di prenotazione significa che gli impegni rimangono "prenotati" e quindi non diventano "esecutivi"
        // quindi devo invertire il flag "esecutivitaMovimenti"
        boolean lasciaFlagPrenotazione = !atto.tipologiaDocumento.esecutivitaMovimenti
        attiCfIntegrazioneService.esecutivitaAtto(getAttoCf(atto), lasciaFlagPrenotazione)
    }

    InputStream getSchedaContabile (IDocumento documento) {
        return attiCfIntegrazioneService.getSchedaContabile(getAttoCf(documento));
    }

    private AttoCf getAttoCf (IDocumento documento, boolean aggiungiDocumentiCollegati = true) {
        if (documento instanceof Determina) {
            return getAttoCf((Determina) documento, aggiungiDocumentiCollegati)
        } else if (documento instanceof Delibera) {
            return getAttoCf((Delibera) documento, aggiungiDocumentiCollegati)
        } else if (documento instanceof PropostaDelibera) {
            return getAttoCf((PropostaDelibera) documento, aggiungiDocumentiCollegati)
        } else if (documento instanceof VistoParere) {
            return getAttoCf((VistoParere) documento, aggiungiDocumentiCollegati)
        }

        throw new AttiRuntimeException("Documento non gestito in contabilità: ${documento}")
    }

    private AttoCf getAttoCf (Determina determina, boolean aggiungiDocumentiCollegati = true) {
        AttoCf attoCf = new AttoCf(AttoCf.TIPO_DETERMINA, determina.id, determina.ente?.codice)
        attoCf.setDatiProposta(determina.dataNumeroProposta
                               , determina.annoProposta
                               , determina.numeroProposta
                               , determina.registroProposta?.registroEsterno ?: determina.registroProposta?.codice
                               , determina.tipologia.titolo
                               , determina.oggetto
                               , determina.getUnitaProponente().progr
                               , null
                               , determina.getUnitaProponente().ottica.codice)

        if (determina.numeroDetermina > 0) {
            attoCf.setDatiAtto(determina.dataNumeroDetermina
                               , determina.annoDetermina
                               , determina.numeroDetermina
                               , determina.registroDetermina?.registroEsterno ?: determina.registroDetermina?.codice
                               , determina.registroDetermina?.descrizione)
            attoCf.dataEsecutivita = determina.dataEsecutivita
            attoCf.immediatamenteEseguibile = true
        }

        // Questo viene usato a Treviso per poter aggiungere i movimenti contabili dall'unità di competenza
        So4UnitaPubb s = determina.getSoggetto(TipoSoggetto.UO_DESTINATARIA)?.unitaSo4

        // Questo viene usato a Pistoia per poter aggiungere i movimenti contabili dall'unità di competenza dell'unità del firmatario
        if (s == null) {
            s = determina.getSoggetto(TipoSoggetto.UO_FIRMATARIO)?.unitaSo4
        }

        if (s != null) {
            attoCf.setUnitaCompetenzaProgressivo(s.progr)
        }

        attoCf.tipiDocumento = TipoDocumentoCf.getTipiDocumento(determina.tipologia)
        attoCf.documentiCollegati = []
        // se ho dei documenti collegati, li aggiungo all'attocf:
        if (aggiungiDocumentiCollegati && determina.documentiCollegati != null) {
            for (DocumentoCollegato documentoCollegato : determina.documentiCollegati) {
                if (documentoCollegato.operazione == DocumentoCollegato.OPERAZIONE_COLLEGA) {
                    // impedisco la ricorsione passando "false" per non rischiare di andare in loop infinito.
                    // inoltre gestisco i soli documenti "determina" e "delibera"
                    if (documentoCollegato.determinaCollegata != null) {
                        attoCf.documentiCollegati.add(getAttoCf(documentoCollegato.determinaCollegata, false))
                    } else if (documentoCollegato.deliberaCollegata) {
                        attoCf.documentiCollegati.add(getAttoCf(documentoCollegato.deliberaCollegata, false))
                    }
                }
            }
        }

        return attoCf
    }

    private AttoCf getAttoCf (VistoParere vistoParere, boolean aggiungiDocumentiCollegati = true) {
        return getAttoCf(vistoParere.documentoPrincipale, aggiungiDocumentiCollegati)
    }

    private AttoCf getAttoCf (Delibera delibera, boolean aggiungiDocumentiCollegati = true) {
        return getAttoCf((PropostaDelibera) delibera.proposta, aggiungiDocumentiCollegati)
    }

    private AttoCf getAttoCf (PropostaDelibera propostaDelibera, boolean aggiungiDocumentiCollegati = true) {
        AttoCf attoCf = new AttoCf(AttoCf.TIPO_DELIBERA, propostaDelibera.id, propostaDelibera.ente?.codice)
        attoCf.setDatiProposta(propostaDelibera.dataNumeroProposta
                               , propostaDelibera.annoProposta
                               , propostaDelibera.numeroProposta
                               , propostaDelibera.registroProposta?.registroEsterno ?: propostaDelibera.registroProposta?.codice
                               , propostaDelibera.tipologia.titolo
                               , propostaDelibera.oggetto
                               , propostaDelibera.getUnitaProponente().progr
                               , null
                               , propostaDelibera.getUnitaProponente().ottica.codice)

        Delibera delibera = propostaDelibera.getAtto();
        if (delibera != null) {
            attoCf.setDatiAtto(delibera.dataAdozione
                               , delibera.annoDelibera
                               , delibera.numeroDelibera
                               , delibera.registroDelibera?.registroEsterno ?: delibera.registroDelibera.codice
                               , delibera.registroDelibera?.descrizione)
            attoCf.dataEsecutivita = delibera.dataEsecutivita
            attoCf.immediatamenteEseguibile = delibera.eseguibilitaImmediata
        }

        attoCf.tipiDocumento = TipoDocumentoCf.getTipiDocumento(propostaDelibera.tipologia)
        attoCf.documentiCollegati = []
        // se ho dei documenti collegati, li aggiungo all'attocf:
        if (aggiungiDocumentiCollegati && propostaDelibera.documentiCollegati != null) {
            for (DocumentoCollegato documentoCollegato : propostaDelibera.documentiCollegati) {
                if (documentoCollegato.operazione == DocumentoCollegato.OPERAZIONE_COLLEGA) {
                    // impedisco la ricorsione passando "false" per non rischiare di andare in loop infinito.
                    // inoltre gestisco i soli documenti "delibera"
                    if (documentoCollegato.deliberaCollegata) {
                        attoCf.documentiCollegati.add(getAttoCf(documentoCollegato.deliberaCollegata, false))
                    }
                }
            }
        }

        return attoCf
    }

    @Override
    boolean isTipiDocumentoAbilitati () {
        return true;
    }

    @CompileDynamic
    boolean isContabilitaSolaLettura (IDocumento documento) {
        if (documento instanceof IDocumentoCollegato) {
            documento = ((IDocumentoCollegato) documento).documentoPrincipale
        }

        return !documento.tipologiaDocumento.scritturaMovimentiContabili
    }

    boolean isMovimentiCigCompleti(IDocumento documento) {
        return attiCfIntegrazioneService.isMovimentiCigCompleti(getAttoCf(documento))
    }
}
