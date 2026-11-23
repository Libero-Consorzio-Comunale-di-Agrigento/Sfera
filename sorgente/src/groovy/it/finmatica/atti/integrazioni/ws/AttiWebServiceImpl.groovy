package it.finmatica.atti.integrazioni.ws

import atti.ricerca.MascheraRicercaDocumento
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.dizionari.Categoria
import it.finmatica.atti.dizionari.DizionarioContabile
import it.finmatica.atti.dizionari.DizionarioContabileService
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.documenti.AllegatoService
import it.finmatica.atti.documenti.DatoAggiuntivo
import it.finmatica.atti.documenti.TipoDatoAggiuntivo
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.PropostaDeliberaEsterna
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaAtsPavia
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.Esito
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.GetComponentiCommissioneResp
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.GetProposteSedutaResp
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.GetProposteSedutaRespExt
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.GetSeduteResp
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.OggettoPartecipanteVoto
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.OggettoSedutaProposta
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.SedutaProposta
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.SedutaPropostaExt
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.SedutaPropostaRiferimento
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.SedutaVotazione
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.SetPropostaVotoResp
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.CommissioneComponente
import it.finmatica.atti.odg.OggettoPartecipante
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.atti.odg.Seduta
import it.finmatica.atti.odg.SedutaPartecipante
import it.finmatica.atti.odg.SedutaService
import it.finmatica.atti.odg.dizionari.RuoloPartecipante
import it.finmatica.atti.odg.dizionari.Voto
import it.finmatica.gestionetesti.TipoFile
import org.springframework.transaction.annotation.Transactional
import groovy.transform.CompileDynamic
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IFileAllegato
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.GestoreFileDataSource
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.DeliberaService
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.DeterminaService
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.ISoggettoDocumento
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.PropostaDeliberaService
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.DeterminaEsterna
import it.finmatica.atti.integrazioni.DocumentiEsterniService
import it.finmatica.atti.integrazioni.ws.dati.*
import it.finmatica.gestioneiter.IGestoreCompetenze
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzione
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgPulsante
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.so4.login.So4UserDetail
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.w3c.dom.Node
import org.xml.sax.InputSource

import javax.activation.DataHandler
import javax.jws.WebParam
import javax.xml.bind.JAXB
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import java.text.SimpleDateFormat

@Transactional
@Component("attiWebService")
@GrailsCompileStatic
class AttiWebServiceImpl implements AttiWebService {

    private static final Logger log = Logger.getLogger(AttiWebServiceImpl.class)

    @Autowired
    PropostaDeliberaService propostaDeliberaService
    @Autowired
    DocumentiEsterniService documentiEsterniService
    @Autowired
    IDocumentaleEsterno     gestoreDocumentaleEsterno
    @Autowired
    SpringSecurityService   springSecurityService
    @Autowired
    IGestoreCompetenze      gestoreCompetenze
    @Autowired
    AttiGestoreCompetenze attiGestoreCompetenze
    @Autowired
    DeterminaService        determinaService
    @Autowired
    DeliberaService         deliberaService
    @Autowired
    WkfIterService          wkfIterService
    @Autowired
    SedutaService           sedutaService
    @Autowired
    IGestoreFile            gestoreFile
    @Autowired
    AllegatoService         allegatoService
    @Autowired
    DizionarioContabileService dizionarioContabileService
    @Autowired
    IntegrazioneContabilitaAtsPavia integrazioneContabilitaAtsPavia
    @Autowired
    AttiGestioneTesti attiGestioneTesti
    /*
     * Metodi web-service esposti:
     */


    String ricercaDocumentiEstesa (Soggetto operatore, String ente, String campiRicerca, Integer pagina, Integer max) {
        log.info("ricercaDocumentiEstesa")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("campiRicerca="+ObjToStringXml(campiRicerca))
        log.info("pagina="+ObjToStringXml(pagina))
        log.info("max="+ObjToStringXml(max))
        try {
            //verificaValiditaTipoDocumento("ricercaDocumentiEstesa", dati, [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO])

            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            CampiRicercaEstesa campiRicercaEstesa= getCampiRicerca(campiRicerca)

            return ricercaEstesa(campiRicercaEstesa,pagina,max)
        } catch (Exception e) {
            log.error("Problemi durante l'operazione ricercaDocumentiEstesa: ${e.getMessage()}", e);
            throw new AttiRuntimeException("Problemi durante l'operazione ricercaDocumentiEstesa: ${e.message}", e)
        }
    }


    RispostaGenerica setDizionariContabili(
            @WebParam(name = "operatore") Soggetto operatore,
            @WebParam(name = "ente") String ente,
            @WebParam(name = "tipo") String tipo,
            @WebParam(name = "operazione") String operazione, @WebParam(name = "dati") String dati) {
        RispostaGenerica ret=new RispostaGenerica()
        log.info("setDizionariContabili")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("tipo="+ObjToStringXml(tipo))
        log.info("operazione="+ObjToStringXml(operazione))
        if(operazione?.equals("UPDATE"))
            log.info("dati="+ObjToStringXml(dati))

        ret.esito="OK"
        ret.codiceErrore=0
        try{
            login(operatore, ente)
        }catch (Throwable th){
            return new RispostaGenerica("KO",-2,"Errore in fase di login")
        }

        Ad4Utente utenteCorrente =(Ad4Utente)springSecurityService.currentUser

        //check campi

        if(!operazione?.equals("UPDATE") && !operazione?.equals("REBUILD")){
            return new RispostaGenerica("KO",-4, "Operazione non consentita, possibili UPDATE, REBUILD")
        }

        if(!tipo.equals(DizionarioContabile.BILANCIO) &&
        !tipo.equals(DizionarioContabile.CONTO) &&
        !tipo.equals(DizionarioContabile.SOTTOBUDGET)){
            return new RispostaGenerica("KO",-3, "Tipo non consentito, possibili BILANCIO, CONTO, SOTTOBUDGET")
        }
        try{


            if(operazione.equals("UPDATE")){
                List<DizionarioContabile> datiDizionariContabili= getDatiDizionariContabili(dati,operatore,tipo)
                dizionarioContabileService.update(datiDizionariContabili)
            }
            if(operazione.equals("REBUILD")){
                String xmlDati= getXmlDatiDizionariContabili(dati);
                dizionarioContabileService.rebuild(tipo,ente,utenteCorrente.utente,xmlDati)
            }
        }
        catch (Throwable ex){
            ret.esito="KO"
            ret.codiceErrore=-1
            ret.errore=ex.getMessage()
        }
        return ret
    }

    Documento numeraAtto (Soggetto operatore, String ente, Documento documento) {
        log.info("numeraAtto")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("documento="+ObjToStringXml(documento))
        try {
            // verifico che questo metodo sia invocabile per il tipo di documento passato
            verificaValiditaTipoDocumento("numera", documento.tipo, [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO])

            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            // eseguo le operazioni
            switch (documento.tipo) {
                case Determina.TIPO_OGGETTO:
                    return numeraDetermina(documento)

                case Delibera.TIPO_OGGETTO:
                    return numeraDelibera(documento)
            }
        } catch (Exception e) {
            log.error("Problemi durante l'operazione numeraAtto: ${e.getMessage()}", e);
            throw new AttiRuntimeException("Problemi durante l'operazione numeraAtto: ${e.message}", e)
        }
    }

    Documento creaProposta (Soggetto operatore, String ente, Documento proposta) {
        log.info("creaProposta")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("proposta="+ObjToStringXml(proposta))
        try {
            verificaValiditaTipoDocumento("creaProposta", proposta.tipo, [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO])

            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            return creaPropostaAtto(proposta, null)

        } catch (Exception e) {
            log.error("Problemi durante l'operazione creaProposta: ${e.getMessage()}", e);
            throw new AttiRuntimeException("Problemi durante l'operazione creaProposta: ${e.message}", e)
        }
    }

    DocumentoEsteso creaPropostaEstesa (Soggetto operatore, String ente, DocumentoEsteso propostaEstesa) {
        log.info("creaProposta")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("proposta="+ObjToStringXml(propostaEstesa))
        try {
            verificaValiditaTipoDocumento("creaProposta", propostaEstesa.tipo, [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO])

            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            Documento proposta = toDocumentoFormEsteso(propostaEstesa)
            Documento documento = creaPropostaAtto(proposta, propostaEstesa)

            //creo il documento esteso che è una copia del documento
            DocumentoEsteso documentoEsteso = toEstesoFormDocumento(documento)
            // Aggiungo i campi non presenti nel documento
            switch (proposta.tipo) {
                case Determina.TIPO_OGGETTO:
                    Determina determina = Determina.get(documento.id)

                    documentoEsteso.tipologiaPubblicazione = determina.tipologia.tipoPubblicazioneAlbo
                    documentoEsteso.titoloTipologia = determina.tipologia.titolo
                    documentoEsteso.nominativoDirigente = determina.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.nominativoSoggetto
                    documentoEsteso.uoProponenteDescrizione = determina.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.descrizione
                    documentoEsteso.cig = determina.codiceGara
                    documentoEsteso.cup = propostaEstesa.cup

                    documentoEsteso.daInviareCorteConti = determina.daInviareCorteConti
                    documentoEsteso.dataInvioCorteConti = determina.dataInvioCorteConti
                    documentoEsteso.note = determina.note
                    documentoEsteso.eseguibilitaImmediata = determina.eseguibilitaImmediata
                    documentoEsteso.motivazioniEseguibilita = determina.motivazioniEseguibilita
                    documentoEsteso.dataScadenza = determina.dataScadenza
                    documentoEsteso.priorita = determina.priorita
                    documentoEsteso.motivazione = determina.motivazione
                    documentoEsteso.codiceCategoria = determina.categoria?.codice
                    break;
                case Delibera.TIPO_OGGETTO:
                    PropostaDelibera propostaDelibera = PropostaDelibera.get(documento.id)

                    documentoEsteso.tipologiaPubblicazione = propostaDelibera.tipologia.tipoPubblicazioneAlbo
                    documentoEsteso.titoloTipologia = propostaDelibera.tipologia.titolo
                    documentoEsteso.nominativoDirigente = propostaDelibera.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.nominativoSoggetto
                    documentoEsteso.uoProponenteDescrizione = propostaDelibera.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.descrizione
                    documentoEsteso.daInviareCorteConti = propostaDelibera.daInviareCorteConti
                    documentoEsteso.note = propostaDelibera.note
                    documentoEsteso.eseguibilitaImmediata = propostaDelibera.eseguibilitaImmediata
                    documentoEsteso.motivazioniEseguibilita = propostaDelibera.motivazioniEseguibilita
                    documentoEsteso.dataScadenza = propostaDelibera.dataScadenza
                    documentoEsteso.priorita = propostaDelibera.priorita
                    documentoEsteso.motivazione = propostaDelibera.motivazione
                    documentoEsteso.codiceCategoria = propostaDelibera.categoria?.codice
                default:
                    break;
            }

            return documentoEsteso
        } catch (Exception e) {
            log.error("Problemi durante l'operazione creaProposta: ${e.getMessage()}", e);
            throw new AttiRuntimeException("Problemi durante l'operazione creaProposta: ${e.message}", e)
        }
    }

    Long addAllegato (Soggetto operatore, String ente,
                           String titolo,
                           String tipoAllegato,
                           String statoFirma,
                           boolean stampaUnica,
                           boolean pubblicaAlbo,
                           boolean pubblicaCasaDiVetro,
                           boolean pubblicaVisualizzatore,
                           boolean riservato,
                           long idDocumento,
                           RiferimentoFile riferimentoFile) {
        log.info("addAllegato")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("titolo="+ObjToStringXml(titolo))
        log.info("tipoAllegato="+ObjToStringXml(tipoAllegato))
        log.info("statoFirma="+ObjToStringXml(statoFirma))
        log.info("stampaUnica="+ObjToStringXml(stampaUnica))
        log.info("pubblicaAlbo="+ObjToStringXml(pubblicaAlbo))
        log.info("pubblicaCasaDiVetro="+ObjToStringXml(pubblicaCasaDiVetro))
        log.info("pubblicaVisualizzatore="+ObjToStringXml(pubblicaVisualizzatore))
        log.info("riservato="+ObjToStringXml(riservato))
        log.info("idDocumento="+ObjToStringXml(idDocumento))
        log.info("file="+ObjToStringXml(riferimentoFile))

        try {
            String tipo = getTipo(idDocumento)

            verificaValiditaTipoDocumento("addAllegato", tipo, [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, it.finmatica.atti.documenti.VistoParere.TIPO_OGGETTO])

            if(titolo==null || titolo.length()==0)
                throw new AttiRuntimeException("Campo obbligatorio mancante: titolo")

            if(riferimentoFile==null )
                throw new AttiRuntimeException("Campo obbligatorio mancante: riferimentoFile")

            if(riferimentoFile.nome== null || riferimentoFile.nome.length()==0 )
                throw new AttiRuntimeException("Campo obbligatorio mancante: riferimentoFile.nome")

            if(riferimentoFile.file== null)
                throw new AttiRuntimeException("Campo obbligatorio mancante: riferimentoFile.file")

            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            return addAllegatoAtto(idDocumento,tipo, titolo,tipoAllegato,statoFirma,stampaUnica,pubblicaAlbo,pubblicaCasaDiVetro,pubblicaVisualizzatore,riservato,riferimentoFile)
        } catch (Exception e) {
            log.error("Problemi durante l'operazione addAllegato: ${e.getMessage()}", e);
            throw new AttiRuntimeException("Problemi durante l'operazione addAllegato: ${e.message}", e)
        }
    }

    RisultatoRicerca ricercaDocumenti (Soggetto operatore, String ente, CampiRicerca campiRicerca, Integer pagina, Integer max) {
        log.info("ricercaDocumenti")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("campiRicerca="+ObjToStringXml(campiRicerca))
        log.info("pagina="+ObjToStringXml(pagina))
        log.info("max="+ObjToStringXml(max))

        try {
            // verifico che questo metodo sia invocabile per il tipo di documento passato
            verificaValiditaTipoDocumento("ricercaAtto", campiRicerca.tipo, [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO])

            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            return ricerca(campiRicerca, pagina, max)
        } catch (Exception e) {
            log.error("Problemi durante l'operazione ricercaDocumenti: ${e.getMessage()}", e);
            throw new AttiRuntimeException("Problemi durante l'operazione ricercaDocumenti: ${e.message}", e)
        }
    }

    Documento annullaAtto (Soggetto operatore, String ente, Documento documento) {
        log.info("annullaAtto")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("documento="+ObjToStringXml(documento))

        try {
            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            // eseguo l'operazione
            return annullaDetermina(documento)
        } catch (Exception e) {
            log.error("Problemi durante l'operazione annullaAtto: ${e.getMessage()}", e)
            throw new AttiRuntimeException("Problemi durante l'operazione annullaAtto: ${e.message}", e)
        }
    }

    Documento getDocumentoPubblico (Soggetto operatore, String ente, long idDocumento ) {
        log.info("getDocumentoPubblico")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("idDocumento="+ObjToStringXml(idDocumento))
        try {
            String tipo = getTipo(idDocumento)

            // verifico che questo metodo sia invocabile per il tipo di documento passato
            verificaValiditaTipoDocumento("ricercaDocumento", tipo, [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO])

            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            return ricercaDocumento(idDocumento, tipo,false)
        } catch (Exception e) {
            log.error("Problemi durante l'operazione getDocumento: ${e.getMessage()}", e)
            throw new AttiRuntimeException("Problemi durante l'operazione getDocumento: ${e.message}", e)
        }
    }

    Documento getDocumento (Soggetto operatore, String ente, long idDocumento) {
        log.info("getDocumento")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("idDocumento="+ObjToStringXml(idDocumento))
        try {
            String tipo = getTipo(idDocumento)

            // verifico che questo metodo sia invocabile per il tipo di documento passato
            verificaValiditaTipoDocumento("ricercaDocumento", tipo, [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO])

            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            return ricercaDocumento(idDocumento, tipo,true)
        } catch (Exception e) {
            log.error("Problemi durante l'operazione getDocumento: ${e.getMessage()}", e)
            throw new AttiRuntimeException("Problemi durante l'operazione getDocumento: ${e.message}", e)
        }
    }

    boolean verificaLetturaAllegato(Soggetto operatore, String ente,String idDocumento, Long idOggettoFile) {
        boolean ret = false

        log.info("verificaLetturaAllegato")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("idDocumento="+ObjToStringXml(idDocumento))
        log.info("idOggettoFile="+ObjToStringXml(idOggettoFile))

        try {
            login(operatore, ente)

            String idDocumentoFiltrato = idDocumento.toString().contains("#") ? idDocumento.toString().substring(0, idDocumento.toString().indexOf("#")) : idDocumento.toString()
            Long idDocumentoAttuale = Long.valueOf(idDocumentoFiltrato)

            it.finmatica.atti.integrazioni.ws.dati.Documento documento = getDocumento(operatore, ente, idDocumentoAttuale)

            IAtto documentoAttuale = null
            if (documento.tipo.equals(it.finmatica.atti.integrazioni.ws.dati.Documento.TIPO_DETERMINA)) {
                documentoAttuale = Determina.get(idDocumentoAttuale)
            } else if (documento.tipo.equals(it.finmatica.atti.integrazioni.ws.dati.Documento.TIPO_DELIBERA)) {
                documentoAttuale = Delibera.get(idDocumentoAttuale)
            }

            FileAllegato testo = documentoAttuale.getTesto()
            if (testo != null && testo.idFileEsterno == idOggettoFile) {
                ret = attiGestioneTesti.gestoreCompetenze.controllaRiservato(documentoAttuale, false)
            } else {
                boolean trovato = false
                for (it.finmatica.atti.documenti.Allegato allegato : documentoAttuale.getAllegati()) {
                    if (trovato) {
                        break
                    }
                    for (FileAllegato fileAllegato : allegato.fileAllegati) {
                        if (fileAllegato.idFileEsterno == idOggettoFile) {
                            ret = attiGestioneTesti.gestoreCompetenze.controllaRiservato(allegato, false)
                            trovato = true
                            break
                        }
                    }
                }
            }
            return ret
        }catch(Exception e){
            log.error("Problemi durante l'operazione verificaLetturaAllegato: ${e.getMessage()}", e)
            throw new AttiRuntimeException("Problemi durante l'operazione verificaLetturaAllegato: ${e.message}", e)
        }
    }

    RiferimentoFile getFile (Soggetto operatore, String ente, RiferimentoFile riferimentoFile) {
        log.info("getFile")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("riferimentoFile="+ObjToStringXml(riferimentoFile))
        try {
            if (riferimentoFile.tipoDocumento?.trim()?.length() == 0) {
                throw new AttiRuntimeException("Il campo 'tipoDocumento' non può essere vuoto ma deve contenere il tipo di documento di cui si vuole ottenere il file, ad esempio: 'DETERMINA', 'DELIBERA', 'ALLEGATO' etc.")
            }

            if (!(riferimentoFile.idDocumento > 0)) {
                throw new AttiRuntimeException("Il campo 'idDocumento' è obbligatorio e deve essere un numero intero positivo.")
            }

            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            return getFilePrincipale(riferimentoFile)
        } catch (Exception e) {
            log.error("Problemi durante l'operazione getFile: ${e.getMessage()}", e)
            throw new AttiRuntimeException("Problemi durante l'operazione getFile: ${e.message}", e)
        }
    }

    /*
     * Metodi privati "che fanno il lavoro"
     */

    @CompileDynamic
    Long addAllegatoAtto(long idDocumento, String tipo, String titolo,
                         String tipoAllegato,
                         String statoFirma,
                         boolean stampaUnica,
                         boolean pubblicaAlbo,
                         boolean pubblicaCasaDiVetro,
                         boolean pubblicaVisualizzatore,
                         boolean riservato,
                         RiferimentoFile file) {

        it.finmatica.atti.documenti.Allegato allegato = new it.finmatica.atti.documenti.Allegato()
        allegato.stampaUnica=stampaUnica

        if (tipoAllegato) {
            TipoAllegato tipologiaAllegato = allegatoService.getTipoAllegato(tipoAllegato , tipo)
            if (tipologiaAllegato == null) {
                tipologiaAllegato = allegatoService.getTipoAllegatoByCodiceEsterno(tipoAllegato , tipo)
            }
            allegato.tipoAllegato = tipologiaAllegato
        }
        allegato.titolo = titolo
        allegato.statoFirma = statoFirma
        allegato.stampaUnica = stampaUnica
        allegato.pubblicaAlbo = pubblicaAlbo
        allegato.pubblicaCasaDiVetro = pubblicaCasaDiVetro
        allegato.pubblicaVisualizzatore = pubblicaVisualizzatore
        allegato.riservato= riservato

        if(file!=null && file.file!=null) {
            FileAllegato fileAllegato = new FileAllegato()
            fileAllegato.nome = file.nome
            //fileAllegato.allegato = file.file.decodeBase64()
            String estensione=""
            try{
                estensione=file.nome.toLowerCase().substring(file.nome.lastIndexOf(".")+1)
                fileAllegato.contentType=TipoFile.getInstanceByEstensione(estensioneFile).contentType
            }catch (Exception ex){
                fileAllegato.contentType=TipoFile.DEFAULT.contentType
            }
            gestoreFile.addFile(allegato, fileAllegato, new ByteArrayInputStream(toBytes(file.file)))
            allegato.addToFileAllegati(fileAllegato)
        }

        switch (tipo) {
            case Determina.TIPO_OGGETTO:
                Determina determina = Determina.get(idDocumento)
                if (determina == null) {
                    throw new AttiRuntimeException("Non ho trovato la Determina con id: ${idDocumento}")
                }
                return addAllegatoToDetermina(allegato,determina)
            case Delibera.TIPO_OGGETTO:
                Delibera delibera = Delibera.get(idDocumento)

                if (delibera) {
                    return addAllegatoToDelibera(allegato,delibera)
                }
                else {
                    PropostaDelibera propostaDelibera = PropostaDelibera.get(idDocumento)
                    if (propostaDelibera == null) {
                        throw new AttiRuntimeException("Non ho trovato la Delibera con id: ${idDocumento}")
                    }

                    delibera = propostaDelibera.atto
                    if (delibera) {
                        return addAllegatoToDelibera(allegato, delibera)
                    }
                    else {
                        return addAllegatoToPropostaDelibera(allegato, propostaDelibera)
                    }
                }

            case PropostaDelibera.TIPO_OGGETTO:
                //in realtà qui non entro mai perchè la getTipo non restituisce mai PROPOSTA_DELIBERA
                PropostaDelibera propostaDelibera = PropostaDelibera.get(idDocumento)
                if (propostaDelibera == null) {
                    throw new AttiRuntimeException("Non ho trovato la PropostaDelibera con id: ${idDocumento}")
                }
                return addAllegatoToPropostaDelibera(allegato,propostaDelibera)
            case it.finmatica.atti.documenti.VistoParere.TIPO_OGGETTO:
                VistoParere vistoParere = null
                if (vistoParere == null) {
                    throw new AttiRuntimeException("Non ho trovato i VistoParere con id: ${idDocumento}")
                }
                return addAllegatoToVistoParere(allegato,vistoParere)
        }

        return null

    }

    /*long addAllegatoToDocumento(it.finmatica.atti.documenti.Allegato allegato, IDocumento documento){
        allegato.sequenza = documento.allegati?.size() > 0 ? documento.allegati?.size() + 1 : 1
        documento.addToAllegati(allegato)
        allegato.save()
        documento.save()
        // assegno le competenze al nuovo allegato:
        gestoreCompetenze.assegnaCompetenze(documento, allegato, false)
        return allegato.id
    }*/

    Long addAllegatoToDetermina(it.finmatica.atti.documenti.Allegato allegato,Determina determina){
        allegato.sequenza = determina.allegati?.size() > 0 ? determina.allegati?.size() + 1 : 1
        determina.addToAllegati(allegato)
        allegato.save()
        determina.save()
        // assegno le competenze al nuovo allegato:
        attiGestoreCompetenze.copiaCompetenze(determina, allegato, false)
        return allegato.id
    }

    Long addAllegatoToDelibera(it.finmatica.atti.documenti.Allegato allegato,Delibera delibera){
        allegato.sequenza = delibera.allegati?.size() > 0 ? delibera.allegati?.size() + 1 : 1
        delibera.addToAllegati(allegato)
        allegato.save()
        delibera.save()
        // assegno le competenze al nuovo allegato:
        attiGestoreCompetenze.copiaCompetenze(delibera, allegato, false)
        return allegato.id
    }

    Long addAllegatoToPropostaDelibera(it.finmatica.atti.documenti.Allegato allegato,PropostaDelibera propostaDelibera){
        allegato.sequenza = propostaDelibera.allegati?.size() > 0 ? propostaDelibera.allegati?.size() + 1 : 1
        propostaDelibera.addToAllegati(allegato)
        allegato.save()
        propostaDelibera.save()
        // assegno le competenze al nuovo allegato:
        attiGestoreCompetenze.copiaCompetenze(propostaDelibera, allegato, false)
        return allegato.id
    }

    Long addAllegatoToVistoParere(it.finmatica.atti.documenti.Allegato allegato,it.finmatica.atti.documenti.VistoParere vistoParere){
        allegato.sequenza = vistoParere.allegati?.size() > 0 ? vistoParere.allegati?.size() + 1 : 1
        vistoParere.addToAllegati(allegato)
        allegato.save()
        vistoParere.save()
        // assegno le competenze al nuovo allegato:
        attiGestoreCompetenze.copiaCompetenze(vistoParere, allegato, false)
        return allegato.id
    }

    private String ricercaEstesa ( CampiRicercaEstesa campiRicercaEstesa,Integer pagina,Integer max) throws Exception {
        MascheraRicercaDocumento ricerca = new MascheraRicercaDocumento()
        ricerca.pageSize = max ?: Integer.MAX_VALUE
        ricerca.activePage = pagina ?: 0

        // ricerca sul singolo numero atto o dal al
        ricerca.annoAtto = (Long) campiRicercaEstesa.anno
        ricerca.numeroAtto = (Long) campiRicercaEstesa.numero
        ricerca.numeroAttoDal = campiRicercaEstesa.numeroAttoDal
        ricerca.numeroAttoAl = campiRicercaEstesa.numeroAttoAl
        ricerca.dataAdozioneDal = campiRicercaEstesa.dataAttoDal
        ricerca.dataAdozioneAl = campiRicercaEstesa.dataAttoAl

        ricerca.registroAtto = campiRicercaEstesa.registro


        ricerca.oggetto = campiRicercaEstesa.oggetto
        ricerca.tipoDocumento = campiRicercaEstesa.tipo

        ricerca.codiceEsternoTipologia = campiRicercaEstesa.codiceTipologiaAtto
        ricerca.uoProponenteCodice = campiRicercaEstesa.codiceUnitaProponente
        ricerca.tipoPubblicazioneAlbo = campiRicercaEstesa.tipoPubblicazioneAlbo

        ricerca.numeroProposta = campiRicercaEstesa.numeroProposta
        ricerca.numeroPropostaDal = campiRicercaEstesa.numeroPropostaDal
        ricerca.numeroPropostaAl = campiRicercaEstesa.numeroPropostaAl
        ricerca.annoProposta = campiRicercaEstesa.annoProposta

        ricerca.dataPubblicazioneDal = campiRicercaEstesa.dataPubblicazioneDal
        ricerca.dataPubblicazioneAl = campiRicercaEstesa.dataPubblicazioneAl

        ricerca.dataEsecutivitaDal = campiRicercaEstesa.dataEsecutivitaDal
        ricerca.dataEsecutivitaAl = campiRicercaEstesa.dataEsecutivitaAl

        ricerca.annoProtocollo = campiRicercaEstesa.annoProtocollo
        if (campiRicercaEstesa.numeroProtocollo) {
            ricerca.numeroProtocolloDal = campiRicercaEstesa.numeroProtocollo
            ricerca.numeroProtocolloAl = campiRicercaEstesa.numeroProtocollo
        } else {
            ricerca.numeroProtocolloDal = campiRicercaEstesa.numeroProtocolloDal
            ricerca.numeroProtocolloAl = campiRicercaEstesa.numeroProtocolloAl
        }
        ricerca.classificaCodice = campiRicercaEstesa.codiceClassifica
        ricerca.fascicoloNumero = campiRicercaEstesa.numeroFascicolo
        ricerca.fascicoloAnno = campiRicercaEstesa.annoFascicolo

        if (campiRicercaEstesa.categoria) {
            ricerca.setCategoriaDaCodice(campiRicercaEstesa.tipo, campiRicercaEstesa.categoria)
        }

        ricerca.inEvidenza=campiRicercaEstesa.inEvidenza
        ricerca.dataInEvidenzaDa=campiRicercaEstesa.dataInEvidenzaDa
        ricerca.dataInEvidenzaA=campiRicercaEstesa.dataInEvidenzaA

        ricerca.pagina((So4UserDetail) springSecurityService.principal)

        List<DocumentoEsteso> documenti = ricerca.listaDocumenti.collect { Map doc ->
            String nominativoDirigente
            String tipologiaPubblicazione
            Date dataInizioPubblicazione
            Date dataFinePubblicazione
            String codiceTipologia
            String cig
            String cup
            boolean daInviareCorteConti
            Date dataInvioCorteConti
            String note
            boolean eseguibilitaImmediata
            String motivazioniEseguibilita
            Date dataScadenza
            Integer priorita
            String motivazione
            String codiceCategoria
            List<MovimentoContabile> listaMovimenti
            Boolean inEvidenza
            Date dataInEvidenzaDa
            Date dataInEvidenzaA
            BigDecimal importo
   //         Date dataProposta

            switch (doc.tipoDocumento) {
                case Determina.TIPO_OGGETTO:
                    Determina determina = Determina.get((Long) doc.idDocumento)
                    nominativoDirigente = determina.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.nominativoSoggetto
                    tipologiaPubblicazione = determina.tipologia?.tipoPubblicazioneAlbo
                    dataInizioPubblicazione = determina.dataPubblicazione
                    dataFinePubblicazione = determina.dataFinePubblicazione
                    codiceTipologia = determina.tipologia.codiceEsterno
                    cig =  determina.codiceGara
                  //  cup = TipoDatoAggiuntivo.getValore(determina.toDTO().datiAggiuntivi, TipoDatoAggiuntivo.CUP)
                    cup = ""
                    daInviareCorteConti = determina.daInviareCorteConti
                    dataInvioCorteConti = determina.dataInvioCorteConti
                    note = determina.note
                    eseguibilitaImmediata = determina.eseguibilitaImmediata
                    motivazioniEseguibilita = determina.motivazioniEseguibilita
                    dataScadenza = determina.dataScadenza
                    priorita = determina.priorita
                    motivazione = determina.motivazione
                    codiceCategoria = determina.categoria?.codice
                    listaMovimenti = (List<MovimentoContabile>)integrazioneContabilitaAtsPavia.getMovimentiContabili(determina)
                    inEvidenza=determina.inEvidenza
                    dataInEvidenzaDa=determina.dataInEvidenzaDa
                    dataInEvidenzaA=determina.dataInEvidenzaA
                    importo =determina.importo
      //              dataProposta = determina.dataProposta
                    break;

                case Delibera.TIPO_OGGETTO:
                    Delibera delibera = Delibera.get((Long) doc.idDocumento)
                    if (delibera) {
                        nominativoDirigente = delibera.propostaDelibera?.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.nominativoSoggetto
                        tipologiaPubblicazione = delibera.propostaDelibera?.tipologia?.tipoPubblicazioneAlbo
                        dataInizioPubblicazione = delibera.dataPubblicazione
                        dataFinePubblicazione = delibera.dataFinePubblicazione
                        codiceTipologia = delibera.propostaDelibera?.tipologia?.codiceEsterno
                        daInviareCorteConti = delibera.daInviareCorteConti
                        dataInvioCorteConti = delibera.dataInvioCorteConti
                        note = delibera.note
                        eseguibilitaImmediata = delibera.eseguibilitaImmediata
                        motivazioniEseguibilita = delibera.motivazioniEseguibilita
                        dataScadenza = delibera.propostaDelibera?.dataScadenza
                        priorita = delibera.propostaDelibera?.priorita
                        motivazione = delibera.propostaDelibera?.motivazione
                        codiceCategoria = delibera.propostaDelibera?.categoria?.codice
                        listaMovimenti = (List<MovimentoContabile>)integrazioneContabilitaAtsPavia.getMovimentiContabili(delibera)
                        inEvidenza=delibera.propostaDelibera?.inEvidenza
                        dataInEvidenzaDa=delibera.propostaDelibera?.dataInEvidenzaDa
                        dataInEvidenzaA=delibera.propostaDelibera?.dataInEvidenzaA
                        importo =delibera.propostaDelibera?.importo
     //                   dataProposta = delibera.propostaDelibera?.dataProposta
                    }
                    else {
                        PropostaDelibera propostaDelibera = PropostaDelibera.get((Long) doc.idDocumento)
                        nominativoDirigente = propostaDelibera.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.nominativoSoggetto
                        tipologiaPubblicazione = propostaDelibera.tipologia?.tipoPubblicazioneAlbo
                        codiceTipologia = propostaDelibera.tipologia?.codiceEsterno
                        daInviareCorteConti = propostaDelibera.daInviareCorteConti
                        note = propostaDelibera.note
                        eseguibilitaImmediata = propostaDelibera.eseguibilitaImmediata
                        motivazioniEseguibilita = propostaDelibera.motivazioniEseguibilita
                        dataScadenza = propostaDelibera.dataScadenza
                        priorita = propostaDelibera.priorita
                        motivazione = propostaDelibera.motivazione
                        codiceCategoria = propostaDelibera.categoria?.codice
                        listaMovimenti = (List<MovimentoContabile>)integrazioneContabilitaAtsPavia.getMovimentiContabili(propostaDelibera)
                        inEvidenza=propostaDelibera.inEvidenza
                        dataInEvidenzaDa=propostaDelibera.dataInEvidenzaDa
                        dataInEvidenzaA=propostaDelibera.dataInEvidenzaA
                        importo =propostaDelibera.importo
  //                      dataProposta = propostaDelibera.dataProposta
                        //leggo l'eventuale delibera
                        delibera = propostaDelibera.getAtto()
                        if (delibera) {
                            dataInizioPubblicazione = delibera.dataPubblicazione
                            dataFinePubblicazione = delibera.dataFinePubblicazione
                            dataInvioCorteConti = delibera.dataInvioCorteConti
                        }
                    }
                    break;

                case PropostaDelibera.TIPO_OGGETTO:
                    PropostaDelibera propostaDelibera = PropostaDelibera.get((Long) doc.idDocumento)
                    nominativoDirigente = propostaDelibera.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.nominativoSoggetto
                    tipologiaPubblicazione = propostaDelibera.tipologia?.tipoPubblicazioneAlbo
                    codiceTipologia = propostaDelibera.tipologia?.codiceEsterno
                    daInviareCorteConti = propostaDelibera.daInviareCorteConti
                    note = propostaDelibera.note
                    eseguibilitaImmediata = propostaDelibera.eseguibilitaImmediata
                    motivazioniEseguibilita = propostaDelibera.motivazioniEseguibilita
                    dataScadenza = propostaDelibera.dataScadenza
                    priorita = propostaDelibera.priorita
                    motivazione = propostaDelibera.motivazione
                    codiceCategoria = propostaDelibera.categoria?.codice
    //                dataProposta = propostaDelibera.dataProposta

                    //leggo l'eventuale delibera
                    Delibera delibera = propostaDelibera.getAtto()
                    if (delibera) {
                        dataInizioPubblicazione = delibera.dataPubblicazione
                        dataFinePubblicazione = delibera.dataFinePubblicazione
                        dataInvioCorteConti = delibera.dataInvioCorteConti
                    }
                    break;
            }

            new DocumentoEsteso(numero: (Integer) doc.numeroAtto,
                    anno: (Integer) doc.annoAtto,
                    codiceRegistro: (String) doc.registroAtto,
                    numero2: (Integer) doc.numeroAtto2,
                    anno2: (Integer) doc.annoAtto2,
                    codiceRegistro2: (String) doc.registroAtto2,
                    dataNumero2: (Date) doc.dataNumero2,
                    annoProposta: (Integer) doc.annoProposta,
                    numeroProposta: (Integer) doc.numeroProposta,
//                    dataProposta: dataProposta,
                    oggetto: (String) doc.oggetto,
                    tipo: (String) doc.tipoDocumento,
                    stato: (String) doc.stato,
                    dataEsecutivita: (Date) doc.dataEsecutivita,
                    data: (Date) doc.dataAdozione,
                    titoloTipologia: (String) doc.titoloTipologia,
                    codiceTipologia: (String) codiceTipologia,
                    nominativoDirigente: (String) nominativoDirigente,
                    tipologiaPubblicazione: (String) tipologiaPubblicazione,
                    dataInizioPubblicazione: (Date) dataInizioPubblicazione,
                    dataFinePubblicazione: (Date) dataFinePubblicazione,
                    uoProponenteDescrizione: (String) doc.uoProponenteDescrizione,
                    cig: cig,
                    cup: cup,
                    daInviareCorteConti: daInviareCorteConti,
                    dataInvioCorteConti: dataInvioCorteConti,
                    note: note,
                    eseguibilitaImmediata: eseguibilitaImmediata,
                    motivazioniEseguibilita: motivazioniEseguibilita,
                    dataScadenza: dataScadenza,
                    priorita: priorita,
                    motivazione: motivazione,
                    codiceCategoria: codiceCategoria,
                    inEvidenza       :(Boolean)inEvidenza,
                    dataInEvidenzaDa :(Date)dataInEvidenzaDa,
                    dataInEvidenzaA  :(Date)dataInEvidenzaA,
                    importo          :(BigDecimal)importo,
                    movimentiContabili: toMovimentiContabiliWS(listaMovimenti),
                    id: (Long) ((doc.tipoDocumento == Delibera.TIPO_OGGETTO) ? doc.idDocumentoPrincipale : doc.idDocumento)
            )
        }

        DocumentiLista ret = new DocumentiLista()
        ret.documento = documenti
        ret.totaleDocumenti = ricerca.totalSize
        return doc2Xml(ret)
    }

    List<MovimentoContabileWS> toMovimentiContabiliWS(List<MovimentoContabile> movimenti){
        if (!movimenti) {
            return []
        }
        List<MovimentoContabileWS> ret = new ArrayList<MovimentoContabileWS>()
        for (MovimentoContabile movimentoContabile: movimenti){
            MovimentoContabileWS movimentoContabileWS = new MovimentoContabileWS()
            movimentoContabileWS.annoEsercizio          =  movimentoContabile.annoEsercizio
            movimentoContabileWS.conto                  =  movimentoContabile.conto
            movimentoContabileWS.contoDescrizione       =  movimentoContabile.contoDescrizione
            movimentoContabileWS.bilancio               =  movimentoContabile.bilancio
            movimentoContabileWS.bilancioDescrizione    =  movimentoContabile.bilancioDescrizione
            movimentoContabileWS.sottoBudget            =  movimentoContabile.sottoBudget
            movimentoContabileWS.sottoBudgetDescrizione =  movimentoContabile.sottoBudgetDescrizione
            movimentoContabileWS.note                   =  movimentoContabile.note
            movimentoContabileWS.importoIniziale        =  movimentoContabile.importoIniziale
            movimentoContabileWS.importoVariazione      =  movimentoContabile.importoVariazione
            movimentoContabileWS.importoResiduo         =  movimentoContabile.importoResiduo
            movimentoContabileWS.importoProvvedimento   =  movimentoContabile.importoProvvedimento
            ret.add(movimentoContabileWS)
        }
        return ret
    }

    String doc2Xml(DocumentoEsteso documento){
        JAXBContext jaxbContext = JAXBContext.newInstance(DocumentoEsteso.class)
        Marshaller marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true)
        StringWriter stringWriter= new StringWriter()
        marshaller.marshal(documento,stringWriter)
        String xmlContest=stringWriter.toString()
        return xmlContest
    }

    String doc2Xml(DocumentiLista documenti){
        JAXBContext jaxbContext = JAXBContext.newInstance(DocumentiLista.class)
        Marshaller marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true)
        StringWriter stringWriter= new StringWriter()
        marshaller.marshal(documenti,stringWriter)
        String xmlContest=stringWriter.toString()
        xmlContest=xmlContest.replace("\n","").replace("xsi:nil=\"true\"","").replace("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"","")
        return xmlContest
    }


    private RisultatoRicerca ricerca (CampiRicerca cp, Integer pagina, Integer max) {
        MascheraRicercaDocumento ricerca = new MascheraRicercaDocumento()
        ricerca.pageSize = max ?: Integer.MAX_VALUE
        ricerca.activePage = pagina ?: 0

        // ricerca sul singolo numero atto
        ricerca.annoAtto = (Long) cp.anno
        ricerca.numeroAtto = (Long) cp.numero
        ricerca.registroAtto = cp.registro

        ricerca.oggetto = cp.oggetto
        ricerca.tipoDocumento = cp.tipo
        ricerca.pagina((So4UserDetail) springSecurityService.principal)

        List<Documento> documenti = ricerca.listaDocumenti.collect { Map doc ->
            new Documento(numero: (Integer) doc.numeroAtto,
                    anno: (Integer) doc.annoAtto,
                    codiceRegistro: (String) doc.registroAtto,
                    numero2: (Integer) doc.numeroAtto2,
                    anno2: (Integer) doc.annoAtto2,
                    codiceRegistro2: (String) doc.registroAtto2,
                    dataNumero2: (Date) doc.dataNumero2,
                    annoProposta: (Integer) doc.annoProposta,
                    numeroProposta: (Integer) doc.numeroProposta,
                    oggetto: (String) doc.oggetto,
                    tipo: (String) doc.tipoDocumento,
                    stato: (String) doc.stato,
                    dataEsecutivita: (Date) doc.dataEsecutivita,
                    data: (Date) doc.dataAdozione,
                    id: (Long) ((doc.tipoDocumento == Delibera.TIPO_OGGETTO) ? doc.idDocumentoPrincipale : doc.idDocumento)
            )
        }

        return new RisultatoRicerca(documenti: documenti, totaleDocumenti: ricerca.totalSize)
    }

    private Documento numeraDetermina (Documento documento) {
        // controllo che ci sia l'azione di numerazione:
        Determina determina = Determina.get(documento.id)

        if (determina == null) {
            throw new AttiRuntimeException("La determina con id '${documento.id}' non esiste.")
        }

        So4UserDetail userDetail = (So4UserDetail) springSecurityService.principal

        if (determina.ente.codice != userDetail.amm().codice) {
            throw new AttiRuntimeException(
                    "Non è possibile modificare la determina con id '${documento.id}' associata all'ente ${determina.ente.codice} con il login effettuato sull'ente ${userDetail.amm().codice}")
        }

        // se è già numerata, esco:
        if (determina.numeroDetermina > 0) {
            return toDocumento(determina)
        }

        // se l'utente non ha le competenze di modifica, do' errore:
        def competenze = gestoreCompetenze.getCompetenze(determina)
        if (!competenze.modifica) {
            throw new AttiRuntimeException(
                    "L'utente ${userDetail.username} non ha i diritti di modifica sulla Proposta di Determina ${determina.estremiAtto}.")
        }

        WkfAzione azione = getAzione(determina.iter, "determinaAction", "numeraDetermina")
        if (azione == null) {
            throw new AttiRuntimeException("Non è prevista la numerazione della determina nel nodo in cui si trova la proposta.")
        }

        determina = determinaService.numeraDetermina(determina)
        gestoreDocumentaleEsterno.salvaDocumento(determina)

        return toDocumento(determina)
    }

    private WkfAzione getAzione (WkfIter iter, String nomeBean, String nomeMetodo) {
        if (iter == null) {
            return null
        }

        for (WkfCfgPulsante pulsante : iter.stepCorrente.cfgStep.cfgPulsanti) {
            WkfAzione azione = pulsante.azioni.find { it.nomeBean == nomeBean && it.nomeMetodo == nomeMetodo }
            if (azione != null) {
                return azione
            }
        }

        return null
    }

    @CompileDynamic
    private Documento annullaDetermina (Documento documento) {

        Determina determina

        if (documento.id != null) {
            determina = Determina.get(documento.id)
        } else {
            determina = Determina.createCriteria().get() {
                eq("annoDetermina", documento.anno)
                eq("numeroDetermina", documento.numero)
                eq("registroDetermina.codice", documento.codiceRegistro)
            }?.toDTO()
        }

        if (determina == null) {
            if (documento.id != null) {
                throw new AttiRuntimeException("La determina con id '${documento.id}' non esiste.")
            } else {
                throw new AttiRuntimeException(
                        "La determina con '${documento.anno}' - '${documento.numero}'' - '${documento.codiceRegistro}' non esiste.")
            }
        }

        if (determina.ente.codice != springSecurityService.principal.amm().codice) {
            throw new AttiRuntimeException(
                    "Non è possibile modificare la determina con id '${documento.id}' associata all'ente ${determina.ente.codice} con il login effettuato sull'ente ${springSecurityService.principal.amm().codice}")
        }

        // se l'utente non ha le competenze di modifica, do' errore:
        def competenze = gestoreCompetenze.getCompetenze(determina)
        if (!competenze.modifica) {
            throw new AttiRuntimeException(
                    "L'utente ${springSecurityService.currentUser.nominativo} non ha i diritti di modifica sulla Proposta di Determina ${determina.estremiAtto}.")
        }

        determinaService.annullaDetermina(determina, null)

        return toDocumento(determina)
    }

    private Documento ricercaDocumento (long idDocumento, String tipo, boolean attoCompleto) {
        switch (tipo) {

            case Determina.TIPO_OGGETTO:
                Determina determina = Determina.get(idDocumento)
                if (determina == null) {
                    throw new AttiRuntimeException("Non ho trovato la Determina con id: ${idDocumento}")
                }
                return toDocumento(determina,attoCompleto)

            case Delibera.TIPO_OGGETTO:
                Delibera delibera = Delibera.get(idDocumento)
                if (delibera == null) {
                    PropostaDelibera propostaDelibera = PropostaDelibera.get(idDocumento)
                    if (propostaDelibera == null) {
                        throw new AttiRuntimeException("Non ho trovato la Delibera con id: ${idDocumento}")
                    }

                    return toDocumento(propostaDelibera,attoCompleto)
                }
                return toDocumento(delibera,attoCompleto)
        }
    }

    private Documento numeraDelibera (Documento documento) {
        // controllo che ci sia l'azione di numerazione:
        Delibera delibera = Delibera.get(documento.id)

        if (delibera == null) {
            throw new AttiRuntimeException("La delibera con id '${documento.id}' non esiste.")
        }


        So4UserDetail userDetail = (So4UserDetail) springSecurityService.principal

        if (delibera.ente.codice != userDetail.amm().codice) {
            throw new AttiRuntimeException(
                    "Non è possibile modificare la delibera con id '${documento.id}' associata all'ente ${delibera.ente.codice} con il login effettuato sull'ente ${userDetail.amm().codice}")
        }

        // se è già numerata, esco:
        if (delibera.numeroDelibera > 0) {
            return toDocumento(delibera)
        }

        // se l'utente non ha le competenze di modifica, do' errore:
        def competenze = gestoreCompetenze.getCompetenze(delibera)
        if (!competenze.modifica) {
            throw new AttiRuntimeException(
                    "L'utente ${userDetail.username} non ha i diritti di modifica sulla Delibera ${delibera.estremiAtto}.")
        }

        WkfAzione azione = getAzione(delibera.iter, "deliberaAction", "numeraDelibera")
        if (azione == null) {
            throw new AttiRuntimeException("Non è prevista la numerazione della delibera nel nodo in cui si trova la proposta.")
        }

        delibera = deliberaService.numeraDelibera(delibera)
        gestoreDocumentaleEsterno.salvaDocumento(delibera)

        return toDocumento(delibera)
    }

    private void login (Soggetto operatore, String ente) {
        AttiUtils.eseguiAutenticazione(operatore.utenteAd4, ente)
    }

    private void verificaValiditaTipoDocumento (String nomeMetodo, def tipo, List<String> tipiDocumentoValidi) {
        if (!tipiDocumentoValidi.contains(tipo)) {
            throw new AttiRuntimeException(
                    "Non è possibile eseguire il metodo ${nomeMetodo} su un documento di tipo: ${tipo}, i tipi documento accettati sono: ${tipiDocumentoValidi}")
        }
    }

    private String getTipo (long id) {
        if (Determina.get(id) != null) {
            return Determina.TIPO_OGGETTO
        } else if (Delibera.get(id) != null) {
            return Delibera.TIPO_OGGETTO
        } else if (it.finmatica.atti.documenti.Allegato.get(id) != null) {
            return it.finmatica.atti.documenti.Allegato.TIPO_OGGETTO
        } else if (it.finmatica.atti.documenti.Certificato.get(id) != null) {
            return it.finmatica.atti.documenti.Certificato.TIPO_OGGETTO
        } else if (PropostaDelibera.get(id) != null) {
            return Delibera.TIPO_OGGETTO
        } else if (it.finmatica.atti.documenti.VistoParere.get(id)) {
            return it.finmatica.atti.documenti.VistoParere.TIPO_OGGETTO
        }

        throw new AttiRuntimeException("Non ho trovato il tipo documento con id ${id}")
    }

    private Documento toDocumento ( Determina determina) {
        return toDocumento(determina,true)
    }

    private Documento toDocumento (Determina determina, boolean attoCompleto) {
        Documento documento = new Documento(id: determina.id, oggetto: determina.oggetto)

        documento.codiceTipologia = determina.tipologia.codiceEsterno

        if (determina.numeroProposta > 0) {
            documento.tipo = Determina.TIPO_OGGETTO
            documento.numeroProposta = determina.numeroProposta
            documento.annoProposta = determina.annoProposta
            documento.dataProposta = determina.dataNumeroProposta
        }

        if (determina.numeroDetermina > 0) {
            documento.tipo = Determina.TIPO_OGGETTO
            documento.numero = determina.numeroDetermina
            documento.anno = determina.annoDetermina
            documento.codiceRegistro = determina.registroDetermina.codice
            documento.data = determina.dataNumeroDetermina
            documento.dataEsecutivita = determina.dataEsecutivita
        }

        if (determina.numeroDetermina2) {
            documento.numero2 = determina.numeroDetermina2
            documento.anno2 = determina.annoDetermina2
            documento.codiceRegistro2 = determina.registroDetermina2.codice
            documento.dataNumero2 = determina.dataNumeroDetermina2
        }

        if (determina.numeroProtocollo > 0) {
            documento.numeroProtocollo = determina.numeroProtocollo
            documento.dataProtocollo = determina.dataNumeroProtocollo
            documento.annoProtocollo = determina.annoProtocollo
        }

        documento.dataInizioPubblicazione = determina.dataPubblicazione
        documento.dataFinePubblicazione = determina.dataFinePubblicazione
        documento.numeroAlbo = determina.numeroAlbo
        documento.annoAlbo = determina.annoAlbo
        documento.stato = determina.stato
        documento.idRiferimento = determina.idDocumentoEsterno


        documento.redattore = creaSoggetto(determina.getSoggetto(TipoSoggetto.REDATTORE))
        documento.dirigente = creaSoggetto(determina.getSoggetto(TipoSoggetto.DIRIGENTE))
        documento.funzionario = creaSoggetto(determina.getSoggetto(TipoSoggetto.FUNZIONARIO))
        documento.unitaProponente = creaSoggetto(determina.getSoggetto(TipoSoggetto.UO_PROPONENTE))?.unita

        if (determina.classificaCodice?.trim()?.length() > 0) {
            documento.classificazione = new Classificazione(codice: determina.classificaCodice, descrizione: determina.classificaDescrizione, dataValidita: determina.classificaDal)
        }

        if (determina.fascicoloNumero?.trim()?.length() > 0) {
            documento.fascicolo = new Fascicolo(numero: determina.fascicoloNumero, anno: (determina.fascicoloAnno!=null?determina.fascicoloAnno:0), oggetto: determina.fascicoloOggetto)
        }

        // Testo
        documento.testo = creaRiferimentoFile(Determina.TIPO_OGGETTO, determina, determina.testo)

        // Allegati
        documento.allegati = getAllegati(determina,attoCompleto)

        // Stampa Unica
        Allegato stampaUnica = creaAllegatoStampaUnica(determina, determina.stampaUnica, determina.TIPO_OGGETTO)
        if (stampaUnica != null){
            documento.allegati.add(stampaUnica)
        }

        // VistiPareri
        documento.vistiPareri = getVistiPareri(determina,attoCompleto)
        // Certificati
        documento.certificati = getListaCertificati(determina)
        // Soggetti
        documento.soggetti = getListaSoggetti(determina)

        return documento
    }

    @CompileDynamic
    private RiferimentoFile creaRiferimentoFile (String tipoDocumento, def documento, IFileAllegato fileAllegato) {
        if (fileAllegato == null) {
            return null
        }

        return new RiferimentoFile(idDocumento: documento.id, tipoDocumento: tipoDocumento, nome: fileAllegato.nome, idFile: fileAllegato.id)
    }

    private Documento toDocumento ( Delibera delibera) {
        return toDocumento(delibera,true)
    }

    private Documento toDocumento (Delibera delibera, boolean attoCompleto) {
        Documento documento = new Documento(id: delibera.id, oggetto: delibera.oggetto)

        if (delibera.propostaDelibera != null) {
            documento.tipo = Delibera.TIPO_OGGETTO
            documento.numeroProposta = delibera.propostaDelibera.numeroProposta
            documento.annoProposta = delibera.propostaDelibera.annoProposta
            documento.dataProposta = delibera.propostaDelibera.dataNumeroProposta
            documento.codiceTipologia = delibera.propostaDelibera.tipologia.codiceEsterno
        }

        if (delibera.numeroDelibera > 0) {
            documento.tipo = Delibera.TIPO_OGGETTO
            documento.numero = delibera.numeroDelibera
            documento.anno = delibera.annoDelibera
            documento.data = delibera.dataAdozione
            documento.dataEsecutivita = delibera.dataEsecutivita
        }

        if (delibera.numeroProtocollo > 0) {
            documento.numeroProtocollo = delibera.numeroProtocollo
            documento.dataProtocollo = delibera.dataNumeroProtocollo
            documento.annoProtocollo = delibera.annoProtocollo
        }

        documento.dataInizioPubblicazione = delibera.dataPubblicazione
        documento.dataFinePubblicazione = delibera.dataFinePubblicazione
        documento.numeroAlbo = delibera.numeroAlbo
        documento.annoAlbo = delibera.annoAlbo
        documento.stato = delibera.stato

        documento.redattore = creaSoggetto(delibera.propostaDelibera.getSoggetto(TipoSoggetto.REDATTORE))
        documento.dirigente = creaSoggetto(delibera.propostaDelibera.getSoggetto(TipoSoggetto.DIRIGENTE))
        documento.funzionario = creaSoggetto(delibera.propostaDelibera.getSoggetto(TipoSoggetto.FUNZIONARIO))
        documento.unitaProponente = creaSoggetto(delibera.propostaDelibera.getSoggetto(TipoSoggetto.UO_PROPONENTE))?.unita

        if (delibera.classificaCodice?.trim()?.length() > 0) {
            documento.classificazione = new Classificazione(codice: delibera.classificaCodice, descrizione: delibera.classificaDescrizione, dataValidita: delibera.classificaDal)
        }

        if (delibera.fascicoloNumero?.trim()?.length() > 0) {
            documento.fascicolo = new Fascicolo(numero: delibera.fascicoloNumero, anno: (delibera.fascicoloAnno!=null?delibera.fascicoloAnno:0), oggetto: delibera.fascicoloOggetto)
        }

        // Testo
        documento.testo = creaRiferimentoFile(Delibera.TIPO_OGGETTO, delibera, delibera.testo)


        // Allegati
        documento.allegati = getAllegati(delibera,attoCompleto)
        // Stampa Unica
        Allegato stampaUnica = creaAllegatoStampaUnica(delibera, delibera.stampaUnica, delibera.TIPO_OGGETTO)
        if (stampaUnica != null){
            documento.allegati.add(stampaUnica)
        }
        // VistiPareri
        documento.vistiPareri = getVistiPareri(delibera,attoCompleto)
        // Certificati
        documento.certificati = getListaCertificati(delibera)
        // Soggetti
        documento.soggetti = getListaSoggetti(delibera)

        return documento
    }

    private Documento toDocumento ( PropostaDelibera propostaDelibera) {
        return toDocumento(propostaDelibera,true)
    }

    private Documento toDocumento (PropostaDelibera propostaDelibera, boolean attoCompleto) {
        Documento documento = new Documento(id: propostaDelibera.id, oggetto: propostaDelibera.oggetto)

        documento.tipo = Delibera.TIPO_OGGETTO
        documento.numeroProposta = propostaDelibera.numeroProposta
        documento.annoProposta = propostaDelibera.annoProposta
        documento.dataProposta = propostaDelibera.dataNumeroProposta
        documento.codiceTipologia = propostaDelibera.tipologia.codiceEsterno

        documento.stato = propostaDelibera.stato

        documento.redattore = creaSoggetto(propostaDelibera.getSoggetto(TipoSoggetto.REDATTORE))
        documento.dirigente = creaSoggetto(propostaDelibera.getSoggetto(TipoSoggetto.DIRIGENTE))
        documento.funzionario = creaSoggetto(propostaDelibera.getSoggetto(TipoSoggetto.FUNZIONARIO))
        documento.unitaProponente = creaSoggetto(propostaDelibera.getSoggetto(TipoSoggetto.UO_PROPONENTE))?.unita

        // Testo
        documento.testo = creaRiferimentoFile(Delibera.TIPO_OGGETTO, propostaDelibera, propostaDelibera.testo)

        // Allegati
        documento.allegati = getAllegati(propostaDelibera,attoCompleto)

        // Stampa Unica
        Allegato stampaUnica = creaAllegatoStampaUnica(propostaDelibera, propostaDelibera.stampaUnica, propostaDelibera.TIPO_OGGETTO)
        if (stampaUnica != null){
            documento.allegati.add(stampaUnica)
        }

        // VistiPareri
        documento.vistiPareri = getVistiPareri(propostaDelibera,attoCompleto)
        // Soggetti
        documento.soggetti = getListaSoggetti(propostaDelibera)

        return documento
    }

    private RiferimentoFile getFilePrincipale (RiferimentoFile riferimentoFile) {
        FileAllegato fileAllegato = FileAllegato.get(riferimentoFile.idFile)
        if (fileAllegato == null) {
            throw new AttiRuntimeException("Non ho trovato il file con id ${riferimentoFile.idFile}")
        }

        def documento = getDoc(riferimentoFile.idDocumento)
        RiferimentoFile file = new RiferimentoFile()
        file.idDocumento = riferimentoFile.idDocumento
        file.idFile = fileAllegato.id.longValue()
        file.nome = fileAllegato.nome
        file.tipoDocumento = getTipo(riferimentoFile.idDocumento)
        file.file = new DataHandler(new GestoreFileDataSource(documento, fileAllegato))
        return file
    }

    private IDocumentoEsterno getDoc (long idDocumento) {
        Determina dete = Determina.get(idDocumento)
        if (dete != null) {
            return dete
        }

        Delibera deli = Delibera.get(idDocumento)
        if (deli != null) {
            return deli
        }

        PropostaDelibera propostaDelibera = PropostaDelibera.get(idDocumento)
        if (propostaDelibera != null) {
            return propostaDelibera
        }

        it.finmatica.atti.documenti.Allegato alle = it.finmatica.atti.documenti.Allegato.get(idDocumento)
        if (alle != null) {
            return alle
        }

        it.finmatica.atti.documenti.Certificato cert = it.finmatica.atti.documenti.Certificato.get(idDocumento)
        if (cert != null) {
            return cert
        }

        it.finmatica.atti.documenti.VistoParere visto = it.finmatica.atti.documenti.VistoParere.get(idDocumento)
        if (visto != null) {
            return visto
        }

        throw new AttiRuntimeException("Documento con id ${idDocumento} non trovato.")
    }

    private List<Allegato> getAllegati (IDocumento documento) {
        return getAllegati(documento,true)
    }

    private List<Allegato> getAllegati (IDocumento documento,boolean attoCompleto) {
        if (documento.allegati == null) {
            return []
        }

        List<Allegato> lista = []
        for (it.finmatica.atti.documenti.Allegato a : documento.allegati) {
            if (!a.valido) {
                continue
            }
            if (!attoCompleto && allegatoService.isRiservato(a)) {
                continue
            }

            Allegato allegato = new Allegato()
            allegato.tipo = a.tipoAllegato?.codiceEsterno
            allegato.titoloTipologia = a.tipoAllegato?.titolo

            allegato.titolo = a.titolo
            allegato.id = (Integer) a.id
            allegato.riferimentiFile = []

            if (a.fileAllegati != null) {
                for (FileAllegato f : a.fileAllegati) {
                    if (!a.valido) {
                        continue
                    }

                    allegato.riferimentiFile << creaRiferimentoFile(it.finmatica.atti.documenti.Allegato.TIPO_OGGETTO, a, f)
                }
            }

            if (allegato.riferimentiFile.size() > 0) {
                lista << allegato
            }
        }
        return lista
    }

    private Allegato creaAllegatoStampaUnica(IDocumento documento, FileAllegato stampaUnica, String tipoOggetto) {
        if (stampaUnica != null) {
            it.finmatica.atti.integrazioni.ws.dati.Allegato allegato = new it.finmatica.atti.integrazioni.ws.dati.Allegato()
            allegato.tipo = "STAMPA_UNICA"
            allegato.titoloTipologia = "STAMPA_UNICA"

            allegato.titolo = "Stampa Unica"
            allegato.id = (Integer) stampaUnica.id
            allegato.riferimentiFile = []

            allegato.riferimentiFile << creaRiferimentoFile(tipoOggetto, documento, stampaUnica)

            if (allegato.riferimentiFile.size() > 0) {
                return allegato
            }
        }
        return null
    }

    @CompileDynamic
    private List<VistoParere> getVistiPareri (IDocumento atto, boolean attoCompleto) {
        List<VistoParere> lista = []
        List<it.finmatica.atti.documenti.VistoParere> visti = it.finmatica.atti.documenti.VistoParere.createCriteria().list {
            if (atto instanceof Determina) {
                eq("determina", atto)
            } else if (atto instanceof Delibera) {
                or {
                    eq("propostaDelibera", atto.proposta)
                    eq("delibera", atto)
                }
            } else if (atto instanceof PropostaDelibera) {
                eq("propostaDelibera", atto)
            } else {
                eq("id", -1)
            }

            eq("valido", true)
            eq("stato", StatoDocumento.CONCLUSO)

            isNotNull("testo")
            tipologia {
                order("sequenzaStampaUnica", "asc")
            }
        }

        // se il documento è una delibera, allora verifico se ha dei pareri. Se ne ha, metto quelli in stampa unica.
        List<it.finmatica.atti.documenti.VistoParere> pareriDelibera = visti.findAll { it.delibera != null }
        if (pareriDelibera.size() > 0) {
            visti = pareriDelibera
        }

        for (it.finmatica.atti.documenti.VistoParere v : visti) {
            if(!attoCompleto && v.tipologia && isCodiceinListaRiservati(v.tipologia?.codice)) {
                continue
            }
            lista << getVistoParere(v)
        }

        return lista
    }

    boolean isCodiceinListaRiservati(String codice){
        List<String> lista = Arrays.asList(Impostazioni.WS_ELENCO_VISTIPARERI_RISERVATI.valore.split("#"));
        return lista.contains(codice)
    }

    private VistoParere getVistoParere (it.finmatica.atti.documenti.VistoParere vistoParere) {
        VistoParere visto = new VistoParere()
        visto.allegati = []

        visto.firmatario = creaSoggetto(vistoParere.getSoggetto(TipoSoggetto.FIRMATARIO))
        visto.unita = creaSoggetto(vistoParere.getSoggetto(TipoSoggetto.UO_DESTINATARIA))

        if (vistoParere.testo != null) {
            visto.testo = new RiferimentoFile(nome: vistoParere.testo.nome, idDocumento: vistoParere.id, idFile: vistoParere.testo.id)
        }

        visto.allegati = getAllegati(vistoParere)

        return visto
    }

    @CompileDynamic
    private List<Certificato> getListaCertificati (IAtto atto) {
        def ordineCertificati = [[tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA, secondaPubblicazione: false]
                                 , [tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_PUBBLICAZIONE, secondaPubblicazione: false]
                                 , [tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_ESECUTIVITA, secondaPubblicazione: false]
                                 , [tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE, secondaPubblicazione: false]
                                 , [tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA, secondaPubblicazione: true]
                                 , [tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_PUBBLICAZIONE, secondaPubblicazione: true]
                                 , [tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_ESECUTIVITA, secondaPubblicazione: true]
                                 , [tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE, secondaPubblicazione: true]]

        // seleziono solo quelli validi.
        for (it.finmatica.atti.documenti.Certificato certificato : atto.certificati) {
            if (!certificato.valido) {
                continue
            }

            // uso il tipo di certificato e la seconda pubblicazione come "chiave" di ordineCertificati e ad esso assegno il certificato corrente.
            ordineCertificati.find({ certificato.tipo == it.tipo && certificato.secondaPubblicazione == it.secondaPubblicazione })?.certificato = certificato
        }

        def certificati = ordineCertificati.findAll { it.certificato != null }.certificato

        List<Certificato> listaCertificati = []
        for (it.finmatica.atti.documenti.Certificato c : certificati) {
            if (c.testo != null && c.stato == StatoDocumento.CONCLUSO) {
                RiferimentoFile rf = new RiferimentoFile(nome: c.testo.nome, idDocumento: c.id, idFile: c.testo.id)
                listaCertificati.add(new Certificato(firmatario: creaSoggetto(c.getSoggetto(TipoSoggetto.FIRMATARIO)), testo: rf, tipo: c.tipo))
            }
        }
        return listaCertificati
    }

    private List<Soggetto> getListaSoggetti (IDocumento documento) {
        List<Soggetto> lista = []
        for (ISoggettoDocumento soggettoDocumento : documento.getSoggetti()) {
            lista.add(creaSoggetto(soggettoDocumento))
        }

        if (documento instanceof Delibera) {
            lista.addAll(getListaSoggetti(((Delibera) documento).propostaDelibera))
        }

        return lista
    }

    private Soggetto creaSoggetto (ISoggettoDocumento soggettoDocumento) {
        if (soggettoDocumento == null) {
            return null
        }

        UnitaOrganizzativa uo = null
        if (soggettoDocumento.unitaSo4 != null) {
            uo = new UnitaOrganizzativa(progressivo: soggettoDocumento.unitaSo4.progr
                    , codiceOttica: soggettoDocumento.unitaSo4.ottica.codice
                    , dal: soggettoDocumento.unitaSo4.dal
                    , codice: soggettoDocumento.unitaSo4.codice
                    , descrizione: soggettoDocumento.unitaSo4.descrizione)
        }

        if (soggettoDocumento.utenteAd4 != null) {
            As4SoggettoCorrente s = As4SoggettoCorrente.findByUtenteAd4(soggettoDocumento.utenteAd4)
            return new Soggetto(tipo: soggettoDocumento.tipoSoggetto.codice
                    , niAs4: s.id.toString()
                    , nome: s.nome
                    , cognome: s.cognome
                    , utenteAd4: soggettoDocumento.utenteAd4.nominativo
                    , codiceFiscale: s.codiceFiscale
                    , unita: uo)
        } else {
            return new Soggetto(tipo: soggettoDocumento.tipoSoggetto.codice, unita: uo)
        }
    }

    @CompileDynamic
    private Documento creaPropostaAtto (Documento proposta, DocumentoEsteso propostaEstesa) {
        if (!(proposta.codiceTipologia?.length() > 0)) {
            throw new AttiRuntimeException("Non è possibile creare una proposta senza aver specificato il 'codiceTipologia'.")
        }

        switch (proposta.tipo) {
            case Determina.TIPO_OGGETTO:
                Determina determina = documentiEsterniService.creaDetermina(proposta)
                if (propostaEstesa) {
                    determina.codiceGara = propostaEstesa.cig
                    if (propostaEstesa.cup) {
                        DatoAggiuntivo datoAggiuntivo = new DatoAggiuntivo()
                        datoAggiuntivo.codice = TipoDatoAggiuntivo.CUP
                        datoAggiuntivo.valore = propostaEstesa.cup
                        determina.addToDatiAggiuntivi(datoAggiuntivo)
                    }

                    determina.daInviareCorteConti = propostaEstesa.daInviareCorteConti
                    determina.dataInvioCorteConti = propostaEstesa.dataInvioCorteConti
                    determina.note = propostaEstesa.note
                    determina.eseguibilitaImmediata = propostaEstesa.eseguibilitaImmediata
                    determina.motivazioniEseguibilita = propostaEstesa.motivazioniEseguibilita
                    determina.dataScadenza = propostaEstesa.dataScadenza
                    determina.priorita = propostaEstesa.priorita
                    determina.motivazione = propostaEstesa.motivazione

                    if (propostaEstesa.codiceCategoria) {
                        //Aggancio la determina ad una categoria solo se la trovo già codificata e valida
                        Categoria categoria = Categoria.findByValidoAndCodice(true, propostaEstesa.codiceCategoria)
                        if (categoria) {
                            determina.categoria = categoria
                        }
                    }
                }
                DeterminaEsterna determinaEsterna = documentiEsterniService.creaDeterminaEsterna(DocumentiEsterniService.APPLICATIVO_ESTERNO_WEBSERVICE, proposta)
                determinaEsterna.idDetermina = determina.id
                determinaEsterna.statoAcquisizione = DocumentiEsterniService.ELABORATO
                determinaEsterna.save()

                WkfCfgIter cfgIter = WkfCfgIter.getIterIstanziabile(determina.tipologia.progressivoCfgIter).get()
                wkfIterService.istanziaIter(cfgIter, determina)
                determinaService.numeraProposta(determina)
                wkfIterService.sbloccaDocumento(determina)
                return toDocumento(determina)

            case Delibera.TIPO_OGGETTO:
                PropostaDelibera propostaDelibera = documentiEsterniService.creaPropostaDelibera(proposta)
                PropostaDeliberaEsterna propostaDeliberaEsterna = documentiEsterniService.creaPropostaDeliberaEsterna(DocumentiEsterniService.APPLICATIVO_ESTERNO_WEBSERVICE, proposta)
                propostaDeliberaEsterna.idPropostaDelibera = propostaDelibera.id
                propostaDeliberaEsterna.statoAcquisizione = DocumentiEsterniService.ELABORATO
                propostaDeliberaEsterna.save()

                WkfCfgIter cfgIter = (WkfCfgIter)WkfCfgIter.getIterIstanziabile(propostaDelibera.tipologia.progressivoCfgIter).get()
                wkfIterService.istanziaIter(cfgIter, propostaDelibera)
                propostaDeliberaService.numeraProposta(propostaDelibera)
                wkfIterService.sbloccaDocumento(propostaDelibera)
                return toDocumento(propostaDelibera)
        }
    }

    GetComponentiCommissioneResp getComponentiCommissione(Soggetto operatore
                                                          , String ente
                                                          , String titoloCommissione) {
        log.info("getComponentiCommissione")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("titoloCommissione="+ObjToStringXml(titoloCommissione))
        try {
            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            if(titoloCommissione==null || titoloCommissione.length()==0){
                throw new AttiRuntimeException("campo commissione obbligatorio:")
            }

            GetComponentiCommissioneResp response = new GetComponentiCommissioneResp()

            Commissione commissione = Commissione.findByTitoloAndValido(titoloCommissione, true)

            if(commissione!=null && commissione.id>0){
                response.commissione=commissione.titolo
                response.descrizione=commissione.descrizione

                if(commissione.getComponenti().size()>0){

                    response.componenti = new ArrayList<ComponenteCommissione>()
                    int contaComponenti=0
                    for(CommissioneComponente commissioneComponente : commissione.getComponenti()){
                        if(commissioneComponente.valido) {
                            ComponenteCommissione componenteCommissione = new ComponenteCommissione()
                            componenteCommissione.nome = commissioneComponente.componente.nome
                            componenteCommissione.cognome = commissioneComponente.componente.cognome
                            componenteCommissione.codiceFiscale = commissioneComponente.componente.codiceFiscale
                            componenteCommissione.sequenza = commissioneComponente.sequenza
                            componenteCommissione.firmatario = commissioneComponente.firmatario
                            componenteCommissione.ni = commissioneComponente.componente.id.toString()
                            componenteCommissione.ruolo = commissioneComponente.ruoloPartecipante?.descrizione
                            response.componenti.add(componenteCommissione)
                            contaComponenti++
                        }
                    }
                    response.totaleComponenti=contaComponenti
                    Collections.sort(response.componenti, new Comparator<ComponenteCommissione>() {
                        @Override
                        int compare(ComponenteCommissione o1, ComponenteCommissione o2) {
                            return Integer.compare(o1.getSequenza(), o2.getSequenza())
                        }
                    })
                }

            }


            return response
        }catch (Exception ex){
            log.error("Problemi durante l'operazione getComponentiCommissione: ${ex.getMessage()}", ex)
            throw new AttiRuntimeException("Problemi durante l'operazione getComponentiCommissione: ${ex.message}", ex)
        }

    }

    GetSeduteResp getSedute(Soggetto operatore
                            , String ente
                            , String titoloCommissione
                            , String dataDal
                            , String dataAl
                            , String pubblicate){
        log.info("getSedute")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("titoloCommissione="+ObjToStringXml(titoloCommissione))
        log.info("dataDal="+ObjToStringXml(dataDal))
        log.info("dataAl="+ObjToStringXml(dataAl))
        log.info("pubblicate="+ObjToStringXml(pubblicate))
        try {
            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            if(titoloCommissione==null || titoloCommissione.length()==0){
                throw new AttiRuntimeException("campo commissione obbligatorio:")
            }

            if(dataDal!=null &&  dataDal.length()==10){
                text2Date(dataDal)
            }

            if(dataAl!=null &&  dataAl.length()==10){
                text2Date(dataAl)
            }

            GetSeduteResp response = new GetSeduteResp()

            List<Seduta> elenco = sedutaService.elencoSedute(titoloCommissione,dataDal,dataAl,pubblicate)
            if(elenco!=null && elenco.size()>0){
                response.totaleSedute=elenco.size()
                response.sedute = new ArrayList<SedutaPropostaRiferimento>()
                for(Seduta seduta : elenco){
                    SedutaPropostaRiferimento sedutaPropostaRiferimento = new SedutaPropostaRiferimento()
                    sedutaPropostaRiferimento.idSeduta = seduta.id
                    sedutaPropostaRiferimento.commissione=seduta.commissione.titolo
                    sedutaPropostaRiferimento.anno=seduta.anno
                    sedutaPropostaRiferimento.numero=seduta.numero
                    if(seduta.dataSeduta!=null){
                        sedutaPropostaRiferimento.dataSeduta=date2Text(seduta.dataSeduta)
                        sedutaPropostaRiferimento.oraSeduta=seduta.oraSeduta
                    }
                    response.sedute.add(sedutaPropostaRiferimento)
                }
            }

            return response
        }catch (Exception ex){
            log.error("Problemi durante l'operazione getSedute: ${ex.getMessage()}", ex)
            throw new AttiRuntimeException("Problemi durante l'operazione getSedute: ${ex.message}", ex)
        }
    }

    GetProposteSedutaResp getProposteSeduta (  Soggetto operatore
                                               , String ente
                                               , Long   idSeduta
                                               , Long  idRiferimento
                                               , String titoloCommissione
                                               , Integer anno
                                               , Integer numero) {
        log.info("getProposteSeduta")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("idSeduta="+ObjToStringXml(idSeduta))
        log.info("idRiferimento="+ObjToStringXml(idRiferimento))
        log.info("titoloCommissione="+ObjToStringXml(titoloCommissione))
        log.info("anno="+ObjToStringXml(anno))
        log.info("numero="+ObjToStringXml(numero))
        try {
            GetProposteSedutaResp response = new GetProposteSedutaResp()
            Esito esitoWs =  new Esito()
            esitoWs.codice = 0
            esitoWs.messaggioErrore = ''

            Seduta seduta
            Commissione commissione
            if ((idSeduta == null) && (idRiferimento == null) && (anno == null) && (numero == null)) {
                esitoWs.codice = 1
                esitoWs.messaggioErrore = "Specificare i parametri di input"
            }
            else {
                if (idSeduta != null) {
                    seduta = Seduta.get(idSeduta)

                    if (!(seduta)) {
                        esitoWs.codice = 2
                        esitoWs.messaggioErrore = "Non ho trovato la seduta con id " + idSeduta
                    } else
                        commissione = seduta.commissione
                } else {
                    if (titoloCommissione != null) {
                        commissione = Commissione.findByTitoloAndValido(titoloCommissione, true)
                        if ((anno != null) && (numero != null)) {
                            seduta = Seduta.findByAnnoAndNumeroAndCommissioneAndValido(anno, numero, commissione, true)
                        } else {
                            if (!(idRiferimento.toString().length() == 4)) {
                                esitoWs.codice = 3
                                esitoWs.messaggioErrore = "Il parametro idRiferimento deve essere un numero di 4 cifre"
                            } else {
                                Integer vAnno = ('20' + idRiferimento.toString().substring(0, 2)).toInteger()
                                Integer vNumero = idRiferimento.toString()[-2..-1].toInteger()
                                seduta = Seduta.findByAnnoAndNumeroAndCommissioneAndValido(vAnno, vNumero, commissione, true)
                            }
                        }
                    } else {
                        esitoWs.codice = 4
                        esitoWs.messaggioErrore = "Quando viene cercata una seduta tramite anno e numero oppure idRiferimento è necessario indicare la commissione"
                    }
                }
            }

            //solo nel caso in cui non abbia già gestito l'errore provo a leggere la seduta da db
            if (esitoWs.codice == 0) {
                SedutaProposta wsSeduta
                if (seduta) {
                    wsSeduta = new SedutaProposta()
                    wsSeduta.idSeduta =  seduta.id
                    wsSeduta.anno = seduta.anno
                    wsSeduta.numero = seduta.numero
                    if (idRiferimento > 0)
                        wsSeduta.idRiferimento = idRiferimento
                    wsSeduta.commissione = commissione.titolo

                    wsSeduta.odg = []
                    List<OggettoSeduta> listaOggettiSeduta = sedutaService.getElencoOggettiSeduta(seduta)
                    for (OggettoSeduta o : listaOggettiSeduta) {
                        OggettoSedutaProposta oggettoSeduta = new OggettoSedutaProposta()
                        oggettoSeduta.idOggetto = o.id
                        oggettoSeduta.idRiferimento = o.propostaDelibera.numeroProposta+"/"+o.propostaDelibera.annoProposta
                        oggettoSeduta.annoProposta = o.propostaDelibera.annoProposta
                        oggettoSeduta.numeroProposta = o.propostaDelibera.numeroProposta
                        oggettoSeduta.idProposta = o.propostaDelibera.id
                        oggettoSeduta.oggettoEsteso = o.propostaDelibera.oggetto
                        wsSeduta.odg.add(oggettoSeduta)
                    }
                    response.seduta = wsSeduta
                }
                else {
                    esitoWs.codice = 5
                    esitoWs.messaggioErrore = "Non ho trovato nessuna seduta con i parametri specificati"
                }
            }
            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            // login(operatore, ente)
            response.esito = esitoWs

            return response
        } catch (Exception e) {
            log.error("Problemi durante l'operazione getDocumento: ${e.getMessage()}", e)
            throw new AttiRuntimeException("Problemi durante l'operazione getDocumento: ${e.message}", e)
        }
    }

    SetPropostaVotoResp setPropostaVoto (Soggetto operatore,
                                         String ente,
                                         SedutaVotazione sedutaVotazione) {
        log.info("setPropostaVoto")
        log.info("operatore="+ObjToStringXml(operatore))
        log.info("ente="+ObjToStringXml(ente))
        log.info("sedutaVotazione="+ObjToStringXml(sedutaVotazione))

        try {
            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            SetPropostaVotoResp response = new SetPropostaVotoResp()
            Esito esitoWs = new Esito()
            String messaggioErrore

            //cerco di leggere gli oggetti da db....in caso di errore effettuo la return con il messaggio di errore
            if ((sedutaVotazione.idSeduta == null) && (sedutaVotazione.idRiferimentoSeduta == null) && (sedutaVotazione.anno == null) && (sedutaVotazione.numero == null)) {
                messaggioErrore = "Specificare i parametri di input per la seduta"
                esitoWs.codice = 1
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 1 - "+messaggioErrore)
                response.esito = esitoWs
                return response
            }

            Seduta seduta
            //Leggo la seduta tramite l'id specificato
            if (sedutaVotazione.idSeduta != null) {
                seduta = Seduta.get(sedutaVotazione.idSeduta)

                if (!(seduta)) {
                    messaggioErrore = "Non ho trovato la seduta con id " + sedutaVotazione.idSeduta
                    esitoWs.codice = 2
                    esitoWs.messaggioErrore = messaggioErrore
                    log.error("Errore ws setPropostaVoto: Codice 2 - "+messaggioErrore)
                    response.esito = esitoWs
                    return response
                }
            }

            if (!(seduta)) {
                //Non ho trovato ancora la seduta, quindi la cerco per anno - numero - titolo commissione
                if (!sedutaVotazione.commissione) {
                    messaggioErrore = "Quando viene cercata una seduta tramite anno e numero oppure idRiferimento è necessario indicare la commissione"
                    esitoWs.codice = 3
                    esitoWs.messaggioErrore = messaggioErrore
                    log.error("Errore ws setPropostaVoto: Codice 3 - "+messaggioErrore)
                    response.esito = esitoWs
                    return response
                }

                Commissione commissione = Commissione.findByTitoloAndValido(sedutaVotazione.commissione, true)
                if (!commissione) {
                    messaggioErrore = "Non ho trovato la commissione "+sedutaVotazione.commissione
                    esitoWs.codice = 4
                    esitoWs.messaggioErrore = messaggioErrore
                    log.error("Errore ws setPropostaVoto: Codice 4 - "+messaggioErrore)
                    response.esito = esitoWs
                    return response
                }

                if ((sedutaVotazione.anno != null) && (sedutaVotazione.numero != null)) {
                    seduta = Seduta.findByAnnoAndNumeroAndCommissioneAndValido(sedutaVotazione.anno, sedutaVotazione.numero, commissione, true)
                } else {
                    if (!(sedutaVotazione.idRiferimentoSeduta.toString().length() == 4)) {
                        messaggioErrore = "Il parametro idRiferimento deve essere un numero di 4 cifre"
                        esitoWs.codice = 5
                        esitoWs.messaggioErrore = messaggioErrore
                        log.error("Errore ws setPropostaVoto: Codice 5 - "+messaggioErrore)
                        response.esito = esitoWs
                        return response
                    }
                    Integer vAnno = ('20' + sedutaVotazione.idRiferimentoSeduta.toString().substring(0, 2)).toInteger()
                    Integer vNumero = sedutaVotazione.idRiferimentoSeduta.toString()[-2..-1].toInteger()
                    seduta = Seduta.findByAnnoAndNumeroAndCommissioneAndValido(vAnno, vNumero, commissione, true)
                }
            }

            //Se non ho trovato la seduta restutuisco un messaggio di errore
            if (!(seduta)) {
                messaggioErrore =  "Non ho trovato la seduta con i parametri specificati"
                esitoWs.codice = 6
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 6 - "+messaggioErrore)
                response.esito = esitoWs
                return response
            }

            //tipo di voto....i voti previsti sono:
            // 1 SEMPLICE - 2 NOMINALE - 3 NUMERO LEGALE [APPELLO] - 4 SEGRETA - 5 o 0 IMMEDIATA ESEGUIBITLITA'
            if (!(sedutaVotazione.oggettoSedutaVotazione.tipoVoto)) {
                messaggioErrore =  "Il tipoVoto è un parametro obbligatorio"
                esitoWs.codice = 7
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 7 - "+messaggioErrore)
                response.esito = esitoWs
                return response
            }

            //se tipo voto =  5 o 0 IMMEDIATA ESEGUIBITLITA' allora nella proposta devo impostare solo il flag di immediata eseguibilità
            if ((sedutaVotazione.oggettoSedutaVotazione.tipoVoto < 1) || (sedutaVotazione.oggettoSedutaVotazione.tipoVoto > 5)) {
                messaggioErrore =  "Il tipoVoto deve essere un numero compreso tra 1 e 5"
                esitoWs.codice = 8
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 8 - "+messaggioErrore)
                response.esito = esitoWs
                return response
            }

            //leggo presidente e segretario
            As4SoggettoCorrente presidente
            if (sedutaVotazione.oggettoSedutaVotazione.niPresidente != null) {
                presidente = As4SoggettoCorrente.get(sedutaVotazione.oggettoSedutaVotazione.niPresidente)
            } else if (sedutaVotazione.oggettoSedutaVotazione.cfPresidente != null && sedutaVotazione.oggettoSedutaVotazione.cfPresidente.length()>0) {
                presidente = getSoggettoBySedutaCF(seduta, sedutaVotazione.oggettoSedutaVotazione.cfPresidente)
            }
            if (!(presidente)) {
                messaggioErrore = "Presidente non specificato (è necessario inserire NI o CODICE FISCALE)"

                if (sedutaVotazione.oggettoSedutaVotazione.niPresidente != null) {
                    messaggioErrore = "Presidente non trovato con id " + sedutaVotazione.oggettoSedutaVotazione.niPresidente
                }
                if (sedutaVotazione.oggettoSedutaVotazione.cfPresidente != null && sedutaVotazione.oggettoSedutaVotazione.cfPresidente.length()>0) {
                    messaggioErrore = "Presidente non trovato con cf " + sedutaVotazione.oggettoSedutaVotazione.cfPresidente
                }

                esitoWs.codice = 9
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 9 - "+messaggioErrore)
                response.esito = esitoWs
                return response
            }

            SedutaPartecipante sedutaPresidente = sedutaService.getPartecipanteSeduta(seduta, presidente)
            if (!(sedutaPresidente)) {
                messaggioErrore =  "Il presidente non risulta presente in seduta"
                esitoWs.codice = 10
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 10 - "+messaggioErrore)
                response.esito = esitoWs
                return response
            }

//            Questo controllo non serve più in quanto il presidente può essere diverso da quello della seduta
//            if (sedutaPresidente.ruoloPartecipante?.codice != RuoloPartecipante.CODICE_PRESIDENTE) {
//                messaggioErrore =  "Il soggetto con id "+sedutaVotazione.oggettoSedutaVotazione.niPresidente+" non risulta presidente della seduta"
//                esitoWs.codice = 11
//                esitoWs.messaggioErrore = messaggioErrore
//                log.error("Errore ws setPropostaVoto: Codice 11 - "+messaggioErrore)
//                response.esito = esitoWs
//                return response
//            }
            //todo devo verificare che sia presente in seduta ??? oppure lo devo aggiungere in automatico se manca ??
            //per ora diamo errore se non è già presente in seduta
            //devo controllare che sia in seduta quando faccio l'appello.....oppure nell'oggetto_seduta se sto dando un voto

            As4SoggettoCorrente segretario
            if (sedutaVotazione.oggettoSedutaVotazione.niSegretario != null) {
                segretario = As4SoggettoCorrente.get(sedutaVotazione.oggettoSedutaVotazione.niSegretario)
            }
            if (sedutaVotazione.oggettoSedutaVotazione.cfSegretario != null && sedutaVotazione.oggettoSedutaVotazione.cfSegretario.length()>0)  {
                segretario =  getSoggettoBySedutaCF(seduta,sedutaVotazione.oggettoSedutaVotazione.cfSegretario)
            }

            if (!(segretario)) {
                messaggioErrore = "Segretario non specificato (è necessario inserire NI o CODICE FISCALE)"

                if (sedutaVotazione.oggettoSedutaVotazione.niSegretario != null) {
                    messaggioErrore =  "Segretario non trovato con id "+sedutaVotazione.oggettoSedutaVotazione.niSegretario
                }
                if (sedutaVotazione.oggettoSedutaVotazione.cfSegretario != null && sedutaVotazione.oggettoSedutaVotazione.cfSegretario.length()>0)  {
                    messaggioErrore =  "Segretario non trovato con cf "+sedutaVotazione.oggettoSedutaVotazione.cfSegretario
                }

                esitoWs.codice = 12
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 12 - "+messaggioErrore)
                response.esito = esitoWs
                return response
            }

            SedutaPartecipante sedutaSegretario = sedutaService.getPartecipanteSeduta(seduta, segretario)
            if (!(sedutaSegretario)) {
                messaggioErrore =  "Il segretario non risulta presente in seduta"
                esitoWs.codice = 13
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 13 - "+messaggioErrore)
                response.esito = esitoWs
                return response
            }

//            Questo controllo non serve più in quanto il segretario può essere diverso da quello della seduta
//            if (sedutaSegretario.ruoloPartecipante?.codice != RuoloPartecipante.CODICE_SEGRETARIO) {
//                messaggioErrore =  "Il soggetto con id "+sedutaVotazione.oggettoSedutaVotazione.niSegretario+" non risulta segretario della seduta"
//                esitoWs.codice = 14
//                esitoWs.messaggioErrore = messaggioErrore
//                log.error("Errore ws setPropostaVoto: Codice 14 - "+messaggioErrore)
//                response.esito = esitoWs
//                return response
//            }

            //Se la votazione ha codice 3 indica che viene gestita solo l'appello iniziale (non devo scorrere le proposte)
            if (sedutaVotazione.oggettoSedutaVotazione.tipoVoto == 3) {
                esitoWs = appelloIniziale(esitoWs, seduta, sedutaVotazione)
                //dopo la gestione dell'appello iniziale esco dalla procedura
                response.esito = esitoWs
                return response
            }
            //todo: dizionario esiti...aggiungere codice per trovare l'esito - togliere il presente

            OggettoSeduta oggettoSeduta
            if (sedutaVotazione.oggettoSedutaVotazione.idOggetto != null) {
                oggettoSeduta = OggettoSeduta.get(sedutaVotazione.oggettoSedutaVotazione.idOggetto)
                if (!oggettoSeduta) {
                    messaggioErrore =  "Non ho trovato la proposta con id " + sedutaVotazione.oggettoSedutaVotazione.idOggetto
                    esitoWs.codice = 21
                    esitoWs.messaggioErrore = messaggioErrore
                    log.error("Errore ws setPropostaVoto: Codice 21 - "+messaggioErrore)
                    response.esito = esitoWs
                    return response
                }
            }
            else {
                String annoNumero = sedutaVotazione.oggettoSedutaVotazione.idRiferimentoOggetto
                String annoProposta = annoNumero.substring(annoNumero.indexOf("/") + 1);
                String numeroProposta = annoNumero.substring(0, annoNumero.indexOf("/"));

                PropostaDelibera propostaDelibera = PropostaDelibera.findByAnnoPropostaAndNumeroProposta(annoProposta.toInteger(), numeroProposta.toInteger())
                if (!propostaDelibera) {
                    messaggioErrore = "Non ho trovato la proposta con idRiferimentoOggetto " + sedutaVotazione.oggettoSedutaVotazione.idRiferimentoOggetto
                    esitoWs.codice = 22
                    esitoWs.messaggioErrore = messaggioErrore
                    log.error("Errore ws setPropostaVoto: Codice 22 - "+messaggioErrore)
                    response.esito = esitoWs
                    return response
                }

                oggettoSeduta = OggettoSeduta.findBySedutaAndPropostaDelibera(seduta, propostaDelibera)
                if (!oggettoSeduta) {
                    messaggioErrore = "Non ho trovato la proposta con idRiferimentoOggetto " + sedutaVotazione.oggettoSedutaVotazione.idRiferimentoOggetto+" nella seduta indicata"
                    esitoWs.codice = 23
                    esitoWs.messaggioErrore = messaggioErrore
                    log.error("Errore ws setPropostaVoto: Codice 23 - "+messaggioErrore)
                    response.esito = esitoWs
                    return response
                }
            }

            //Se non ho trovato la proposta in seduta restituisco un messaggio di errore
            if (!(oggettoSeduta)) {
                messaggioErrore = "Non ho trovato la proposta con i parametri specificati"
                esitoWs.codice = 24
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 24 - "+messaggioErrore)
                response.esito = esitoWs
                return response
            }

            if (oggettoSeduta.confermaEsito) {
                messaggioErrore = "Impossibile modificare la proposta in quanto ha l'esito già confermato"
                esitoWs.codice = 25
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 25 - "+messaggioErrore)
                response.esito = esitoWs
                return response
            }

            //viene votata solo l'immediata eseguibilità
            if (sedutaVotazione.oggettoSedutaVotazione.tipoVoto == 5) {
                if (sedutaVotazione.oggettoSedutaVotazione.immediataEseguibilita == null) {
                    messaggioErrore = "Non è stata specificata l'immediata eseguibilità per la proposta"
                    esitoWs.codice = 26
                    esitoWs.messaggioErrore = messaggioErrore
                    log.error("Errore ws setPropostaVoto: Codice 26 - "+messaggioErrore)
                    response.esito = esitoWs
                    return response
                }

                oggettoSeduta.eseguibilitaImmediata = sedutaVotazione.oggettoSedutaVotazione.immediataEseguibilita
                try {
                    oggettoSeduta.save()
                } catch (Exception e) {
                    log.error("Errore ws setPropostaVoto: Codice 27 - Errore nel salvare la proposta: ${e.getMessage()}", e)
                    esitoWs.codice = 27
                    esitoWs.messaggioErrore = "Errore nel salvare la proposta"
                    response.esito = esitoWs
                    return response
                }

                esitoWs.codice = 0
                response.esito = esitoWs
                return response
            } //if (sedutaVotazione.oggettoSedutaVotazione.tipoVoto == 5)

            if (!sedutaVotazione.oggettoSedutaVotazione.esito) {
                messaggioErrore = "Inserire l'esito per la proposta"
                esitoWs.codice = 33
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 33 - "+messaggioErrore)
                response.esito = esitoWs
                return response
            }

            it.finmatica.atti.odg.dizionari.Esito esito = it.finmatica.atti.odg.dizionari.Esito.findByCodiceEsterno(sedutaVotazione.oggettoSedutaVotazione.esito)
            if (!esito) {
                messaggioErrore = "Esito con codice "+sedutaVotazione.oggettoSedutaVotazione.esito+" non trovato"
                esitoWs.codice = 34
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 34 - "+messaggioErrore)
                response.esito = esitoWs
                return response
            }

            oggettoSeduta.esito = esito

            //gestisco la votazione della proposta
            if (!sedutaVotazione.dettaglioVoti) {
                messaggioErrore = "Inserire i dettagli delle votazioni"
                esitoWs.codice = 30
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 30 - "+messaggioErrore)
                response.esito = esitoWs
                return response
            }

            esitoWs = votazioneProposta(esitoWs, oggettoSeduta, sedutaVotazione, presidente, segretario)
            response.esito = esitoWs
            return response
        } catch (Exception e) {
            log.error("Errore ws setPropostaVoto: Codice -1 - Errore per salvare la proposta: ${e.getMessage()}", e)
            Esito esitoWs = new Esito()
            esitoWs.codice = -1
            esitoWs.messaggioErrore = "Errore di sistema"
            SetPropostaVotoResp response = new SetPropostaVotoResp()
            response.esito = esitoWs
            return response
        }
    }

    As4SoggettoCorrente getSoggettoBySedutaCF(Seduta seduta, String codiceFiscale){
        As4SoggettoCorrente ret = null
        List<As4SoggettoCorrente> soggetti = As4SoggettoCorrente.findAllByCodiceFiscale(codiceFiscale)
        for(As4SoggettoCorrente soggettoCorrente:soggetti){
            List<SedutaPartecipante> sedutaPartecipanti=  SedutaPartecipante.findAllBySeduta(seduta)
            for(SedutaPartecipante sedutaPartecipante:sedutaPartecipanti){
                if(sedutaPartecipante.componenteEsterno!=null && sedutaPartecipante.componenteEsterno.id==soggettoCorrente.id){
                    ret=soggettoCorrente
                    break
                } else if (sedutaPartecipante.commissioneComponente!=null && sedutaPartecipante.commissioneComponente.componente.id==soggettoCorrente.id) {
                    ret=soggettoCorrente
                    break
                }

            }
        }
        return ret
    }

    //Gestione appello iniziale...viene impostata la data di fine seduta e la presenza / assenza dei componenti
    private Esito appelloIniziale(Esito esitoWs, Seduta seduta, SedutaVotazione sedutaVotazione) {
        String messaggioErrore

        //scorro tutti i partecipanti alla seduta e imposto la loro presenza / assenza
        for (OggettoPartecipanteVoto partecipante in sedutaVotazione.dettaglioVoti) {
            As4SoggettoCorrente soggetto = null
            if(partecipante.niSoggetto!=null &&  partecipante.niSoggetto>0){
                soggetto = As4SoggettoCorrente.get(partecipante.niSoggetto)
            } else if (partecipante.codiceFiscale!=null && partecipante.codiceFiscale.length()){
                soggetto= getSoggettoBySedutaCF(seduta,partecipante.codiceFiscale)
            } else {
                //errore
            }
            //As4SoggettoCorrente soggetto = As4SoggettoCorrente.get(partecipante.niSoggetto)

            if (!soggetto) {
                messaggioErrore = "Soggetto non specificato (è necessario inserire NI o CODICE FISCALE)"

                if(partecipante.niSoggetto!=null && partecipante.niSoggetto>0) {
                    messaggioErrore = "Soggetto con id " + partecipante.niSoggetto + " non trovato"
                }
                if(partecipante.codiceFiscale!=null && partecipante.codiceFiscale.length()>0) {
                    messaggioErrore = "Soggetto con cf " + partecipante.codiceFiscale + " non trovato"
                }
                esitoWs.codice = 15
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 15 - "+messaggioErrore)
                return esitoWs
            }

            SedutaPartecipante sedutaSoggetto = sedutaService.getPartecipanteSeduta(seduta, soggetto)
            if (!(sedutaSoggetto)) {
                if(partecipante.niSoggetto!=null && partecipante.niSoggetto>0) {
                    messaggioErrore =  "Il soggetto con id "+partecipante.niSoggetto+" non risulta presente in seduta"
                }
                if(partecipante.codiceFiscale!=null && partecipante.codiceFiscale.length()>0) {
                    messaggioErrore =  "Il soggetto con cf "+partecipante.codiceFiscale+" non risulta presente in seduta"
                }

                esitoWs.codice = 16
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 16 - "+messaggioErrore)
                return esitoWs
            }

            if (partecipante.presente == null) {
                if(partecipante.niSoggetto!=null && partecipante.niSoggetto>0) {
                    messaggioErrore = "Non è stata specificata la presenza in seduta per il soggetto con id "+partecipante.niSoggetto
                }
                if(partecipante.codiceFiscale!=null && partecipante.codiceFiscale.length()>0) {
                    messaggioErrore = "Non è stata specificata la presenza in seduta per il soggetto con cf "+partecipante.codiceFiscale
                }

                esitoWs.codice = 17
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 17 - "+messaggioErrore)
                return esitoWs
            }

            try {
                sedutaSoggetto.presente = partecipante.presente
                sedutaSoggetto.save()
            } catch (Exception e) {
                if(partecipante.niSoggetto!=null && partecipante.niSoggetto>0) {
                    messaggioErrore = "Errore nel salvare la presenza del soggetto con id "+partecipante.niSoggetto
                }
                if(partecipante.codiceFiscale!=null && partecipante.codiceFiscale.length()>0) {
                    messaggioErrore = "Errore nel salvare la presenza del soggetto con cf "+partecipante.codiceFiscale
                }
                log.error("Errore ws setPropostaVoto: Codice 18 - Errore nel salvare la presenza del soggetto con id "+partecipante.niSoggetto+": ${e.getMessage()}", e)
                esitoWs.codice = 18
                esitoWs.messaggioErrore = messaggioErrore
                return esitoWs
            }
        }

        try {
            seduta.dataFineSeduta = Date.parse("dd/MM/yyyy", sedutaVotazione.oggettoSedutaVotazione.dataOraVotazione)
            seduta.oraFineSeduta = Date.parse("dd/MM/yyyy hh:mm", sedutaVotazione.oggettoSedutaVotazione.dataOraVotazione).format('HH:mm')
        } catch (Exception e) {
            messaggioErrore = "Il formato della data/ora della seduta non è corretto, Deve essere nel formato dd/MM/yyyy hh:mm"
            esitoWs.codice = 19
            esitoWs.messaggioErrore = messaggioErrore
            log.error("Errore ws setPropostaVoto: Codice 19 - "+messaggioErrore)
            return esitoWs
        }

        try {
            seduta.save()
        } catch (Exception e) {
            log.error("Errore ws setPropostaVoto: Codice 20 - Errore per salvare la seduta: ${e.getMessage()}", e)
            esitoWs.codice = 20
            esitoWs.messaggioErrore = "Errore nel salvare la seduta"
            return esitoWs
        }

        esitoWs.codice = 0
        return esitoWs
    }

    //Gestione del voto della singola proposta
    private Esito votazioneProposta(Esito esitoWs, OggettoSeduta oggettoSeduta, SedutaVotazione sedutaVotazione, As4SoggettoCorrente presidente, As4SoggettoCorrente segretario) {
        String messaggioErrore

        //scorro tutti i partecipanti alla seduta e imposto la loro presenza / assenza
        for (OggettoPartecipanteVoto partecipante in sedutaVotazione.dettaglioVoti) {
            if (partecipante.presente == null) {
                if(partecipante.niSoggetto!=null && partecipante.niSoggetto>0) {
                    messaggioErrore = "Non è stata specificata la presenza in seduta per il soggetto con id "+partecipante.niSoggetto
                }
                if(partecipante.codiceFiscale!=null && partecipante.codiceFiscale.length()>0) {
                    messaggioErrore = "Non è stata specificata la presenza in seduta per il soggetto con cf "+partecipante.codiceFiscale
                }

                esitoWs.codice = 17
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 17 - "+messaggioErrore)
                return esitoWs
            }

            As4SoggettoCorrente soggetto = null
            if(partecipante.niSoggetto!=null && partecipante.niSoggetto>0) {
                soggetto=As4SoggettoCorrente.get(partecipante.niSoggetto)
            }
            if(partecipante.codiceFiscale!=null && partecipante.codiceFiscale.length()>0) {
//                Seduta seduta= Seduta.get(sedutaVotazione.idSeduta)
                if(oggettoSeduta.seduta!=null){
                    soggetto= getSoggettoBySedutaCF(oggettoSeduta.seduta,partecipante.codiceFiscale)
                }
            }

            if (!soggetto) {
                messaggioErrore = "Soggetto non specificato (è necessario inserire NI o CODICE FISCALE)"

                if(partecipante.niSoggetto!=null && partecipante.niSoggetto>0) {
                    messaggioErrore = "Soggetto con id " + partecipante.niSoggetto + " non trovato"
                }
                if(partecipante.codiceFiscale!=null && partecipante.codiceFiscale.length()>0) {
                    messaggioErrore = "Soggetto con cf " + partecipante.codiceFiscale + " non trovato"
                }
                esitoWs.codice = 15
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 15 - "+messaggioErrore)
                return esitoWs
            }

            OggettoPartecipante oggettoSedutaSoggetto = sedutaService.getPartecipanteOggettoSeduta(oggettoSeduta, soggetto)
            //se il partecipante non è già presente nell'oggettoseduta....verifico se è presente in seduta.
            //Se si...lo aggiungo all'oggettoSeduta...se no restituisco errore
            if (!(oggettoSedutaSoggetto)) {
                SedutaPartecipante sedutaPartecipante = sedutaService.getPartecipanteSeduta(oggettoSeduta.seduta, soggetto)

                if (!sedutaPartecipante) {
                    if(partecipante.niSoggetto!=null && partecipante.niSoggetto>0) {
                        messaggioErrore =  "Il soggetto con id "+soggetto.id+" non risulta presente in seduta"
                    }
                    if(partecipante.codiceFiscale!=null && partecipante.codiceFiscale.length()>0) {
                        messaggioErrore =  "Il soggetto con cf "+soggetto.codiceFiscale+" non risulta presente in seduta"
                    }
                    esitoWs.codice = 16
                    esitoWs.messaggioErrore = messaggioErrore
                    log.error("Errore ws setPropostaVoto: Codice 16 - "+messaggioErrore)
                    return esitoWs
                }

                try {
                    oggettoSedutaSoggetto = new OggettoPartecipante()
                    oggettoSedutaSoggetto.oggettoSeduta = oggettoSeduta
                    oggettoSedutaSoggetto.sedutaPartecipante = sedutaPartecipante
                    //Il presidente e il segretario (ruolo e sequenzafirma) li gestisco dopo
                    if (!((sedutaPartecipante.ruoloPartecipante == RuoloPartecipante.get(RuoloPartecipante.CODICE_PRESIDENTE)) ||
                            (sedutaPartecipante.ruoloPartecipante == RuoloPartecipante.get(RuoloPartecipante.CODICE_SEGRETARIO)))) {
                        oggettoSedutaSoggetto.ruoloPartecipante = sedutaPartecipante.ruoloPartecipante
                        oggettoSedutaSoggetto.firmatario = sedutaPartecipante.firmatario
                        oggettoSedutaSoggetto.sequenzaFirma = sedutaPartecipante.sequenzaFirma
                    }
                    oggettoSedutaSoggetto.sequenza = sedutaPartecipante.sequenzaPartecipante
                    oggettoSedutaSoggetto.save()
                } catch (Exception e) {
                    log.error("Errore ws setPropostaVoto: Codice 28 - Errore nel salvare il componente "+soggetto.id+" nella proposta: ${e.getMessage()}", e)
                    esitoWs.codice = 28
                    esitoWs.messaggioErrore = "Errore nel salvare il componente "+soggetto.id+" nella proposta"
                    return esitoWs
                }
            }

            if (!oggettoSedutaSoggetto) {
                if(partecipante.niSoggetto!=null && partecipante.niSoggetto>0) {
                    messaggioErrore =  "Il soggetto con id "+soggetto.id+" non risulta presente in seduta"
                    log.error("Errore ws setPropostaVoto: Codice 28 - Errore per salvare il componente "+soggetto.id+" nella proposta")
                    esitoWs.messaggioErrore = "Errore per salvare il componente "+soggetto.id+" nella proposta"
                }
                if(partecipante.codiceFiscale!=null && partecipante.codiceFiscale.length()>0) {
                    messaggioErrore =  "Il soggetto con cf "+soggetto.codiceFiscale+" non risulta presente in seduta"
                    log.error("Errore ws setPropostaVoto: Codice 28 - Errore per salvare il componente "+soggetto.codiceFiscale+" nella proposta")
                    esitoWs.messaggioErrore = "Errore per salvare il componente "+soggetto.codiceFiscale+" nella proposta"
                }
                esitoWs.codice = 28

                return esitoWs
            }

            oggettoSedutaSoggetto.presente = partecipante.presente

            if (partecipante.presente) {
//                if (!partecipante.voto) {
//                    if(partecipante.niSoggetto!=null && partecipante.niSoggetto>0) {
//                        messaggioErrore = "Inserire il voto per il soggetto "+partecipante.niSoggetto
//                    }
//                    if(partecipante.codiceFiscale!=null && partecipante.codiceFiscale.length()>0) {
//                        messaggioErrore = "Inserire il voto per il soggetto "+partecipante.codiceFiscale
//                    }
//                    esitoWs.codice = 31
//                    esitoWs.messaggioErrore = messaggioErrore
//                    log.error("Errore ws setPropostaVoto: Codice 31 - "+messaggioErrore)
//                    return esitoWs
//                }

                if (partecipante.voto) {
                    Voto voto = Voto.findByCodiceEsterno(partecipante.voto)
                    if (!voto) {
                        messaggioErrore = "Voto con codice "+partecipante.voto+" non trovato"
                        esitoWs.codice = 32
                        esitoWs.messaggioErrore = messaggioErrore
                        log.error("Errore ws setPropostaVoto: Codice 32 - "+messaggioErrore)
                        return esitoWs
                    }

                    oggettoSedutaSoggetto.voto = voto
                }
            }
            else {
                //se il partecipante assente tolgo il ruolo (servirebbe solo per presidente e segretario) e firma
                oggettoSedutaSoggetto.ruoloPartecipante = null
                oggettoSedutaSoggetto.firmatario = false
                oggettoSedutaSoggetto.save()
            }

            try {
                oggettoSedutaSoggetto.save()
            } catch (Exception e) {
                if(partecipante.niSoggetto!=null && partecipante.niSoggetto>0) {
                    messaggioErrore ="Errore per salvare la presenza del soggetto con id "+partecipante.niSoggetto
                }
                if(partecipante.codiceFiscale!=null && partecipante.codiceFiscale.length()>0) {
                    messaggioErrore = "Errore per salvare la presenza del soggetto con id "+partecipante.codiceFiscale
                }
                log.error("Errore ws setPropostaVoto: Codice 29 - " + messaggioErrore, e)
                esitoWs.codice = 29
                esitoWs.messaggioErrore = messaggioErrore
                return esitoWs
            }
        }

        //verifico che presidente e segretario siano presenti in seduta (se non hanno votato non sono stati registrati)
        SedutaPartecipante sedutaPresidente = sedutaService.getSedutaPartecipanteRuolo(oggettoSeduta.seduta, RuoloPartecipante.CODICE_PRESIDENTE)
        OggettoPartecipante oggettoPresidente = sedutaService.getPartecipanteOggettoSeduta(oggettoSeduta, presidente)
        if (oggettoPresidente) {
            //aggiorno il ruolo (potrebbe essere diverso da quello della convocazione
            oggettoPresidente.ruoloPartecipante = RuoloPartecipante.get(RuoloPartecipante.CODICE_PRESIDENTE)
            if (sedutaPresidente) {
                oggettoPresidente.firmatario = sedutaPresidente.firmatario
                oggettoPresidente.sequenzaFirma = sedutaPresidente.sequenzaFirma
            }
            else {
                oggettoPresidente.firmatario = oggettoPresidente.sedutaPartecipante?.commissioneComponente?.firmatario
                oggettoPresidente.sequenzaFirma = oggettoPresidente.sedutaPartecipante?.commissioneComponente?.sequenzaFirma
            }
            oggettoPresidente.presente = true
            oggettoPresidente.save()
        }
        else {
            //verifico che il soggetto sia presente nella seduta
            SedutaPartecipante oggettoSedutaPresidente = sedutaService.getPartecipanteSeduta(oggettoSeduta.seduta, presidente)
            if (!oggettoSedutaPresidente) {
                messaggioErrore =  "Il soggetto con id "+presidente.id+" non risulta presente in seduta"
                esitoWs.codice = 16
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 16 - "+messaggioErrore)
                return esitoWs
            }

            try {
                oggettoPresidente = new OggettoPartecipante()
                oggettoPresidente.oggettoSeduta = oggettoSeduta
                oggettoPresidente.sedutaPartecipante = oggettoSedutaPresidente
                oggettoPresidente.ruoloPartecipante = RuoloPartecipante.get(RuoloPartecipante.CODICE_PRESIDENTE)
                oggettoPresidente.firmatario = sedutaPresidente?.firmatario
                oggettoPresidente.sequenzaFirma = sedutaPresidente?.sequenzaFirma
                oggettoPresidente.sequenza = sedutaPresidente?.sequenzaPartecipante
                oggettoPresidente.presente = true
                oggettoPresidente.save()
            } catch (Exception e) {
                log.error("Errore ws setPropostaVoto: Codice 28 - Errore nel salvare il componente "+presidente.id+" nella proposta: ${e.getMessage()}", e)
                esitoWs.codice = 28
                esitoWs.messaggioErrore = "Errore nel salvare il componente "+presidente.id+" nella proposta"
                return esitoWs
            }
        }

        //verifico che presidente e segretario siano presenti in seduta (se non hanno votato non sono stati registrati)
        SedutaPartecipante sedutaSegretario = sedutaService.getSedutaPartecipanteRuolo(oggettoSeduta.seduta, RuoloPartecipante.CODICE_SEGRETARIO)
        OggettoPartecipante oggettoSegretario = sedutaService.getPartecipanteOggettoSeduta(oggettoSeduta, segretario)
        if (oggettoSegretario) {
            oggettoSegretario.ruoloPartecipante = RuoloPartecipante.get(RuoloPartecipante.CODICE_SEGRETARIO)
            if (sedutaSegretario) {
                oggettoSegretario.firmatario = sedutaSegretario.firmatario
                oggettoSegretario.sequenzaFirma = sedutaSegretario.sequenzaFirma
            }
            else {
                oggettoSegretario.firmatario = oggettoSegretario.sedutaPartecipante?.commissioneComponente?.firmatario
                oggettoSegretario.sequenzaFirma = oggettoSegretario.sedutaPartecipante?.commissioneComponente?.sequenzaFirma
            }
            oggettoSegretario.presente = true
            oggettoSegretario.save()
        }
        else {
            //verifico che il soggetto sia presente nella seduta
            SedutaPartecipante oggettoSedutaSegretario = sedutaService.getPartecipanteSeduta(oggettoSeduta.seduta, segretario)
            if (!oggettoSedutaSegretario) {
                messaggioErrore =  "Il soggetto con id "+segretario.id+" non risulta presente in seduta"
                esitoWs.codice = 16
                esitoWs.messaggioErrore = messaggioErrore
                log.error("Errore ws setPropostaVoto: Codice 16 - "+messaggioErrore)
                return esitoWs
            }

            try {
                oggettoSegretario = new OggettoPartecipante()
                oggettoSegretario.oggettoSeduta = oggettoSeduta
                oggettoSegretario.sedutaPartecipante = oggettoSedutaSegretario
                oggettoSegretario.ruoloPartecipante = RuoloPartecipante.get(RuoloPartecipante.CODICE_SEGRETARIO)
                oggettoSegretario.firmatario = sedutaSegretario?.firmatario
                oggettoSegretario.sequenzaFirma = sedutaSegretario?.sequenzaFirma
                oggettoSegretario.sequenza = sedutaSegretario?.sequenzaPartecipante
                oggettoSegretario.presente = true
                oggettoSegretario.save()
            } catch (Exception e) {
                log.error("Errore ws setPropostaVoto: Codice 28 - Errore nel salvare il componente "+segretario.id+" nella proposta: ${e.getMessage()}", e)
                esitoWs.codice = 28
                esitoWs.messaggioErrore = "Errore nel salvare il componente "+segretario.id+" nella proposta"
                return esitoWs
            }
        }

        //scorro tutti i partecipanti alla seduta....se non sono già presenti nella discussione della proposta li aggiungo come assenti
        for (SedutaPartecipante partecipante in oggettoSeduta.seduta.partecipanti) {
            OggettoPartecipante oggettoPartecipante = OggettoPartecipante.findByOggettoSedutaAndSedutaPartecipante(oggettoSeduta, partecipante)
            if (!oggettoPartecipante) {
                OggettoPartecipante newOggettoPartecipante = new OggettoPartecipante()
                newOggettoPartecipante.oggettoSeduta = oggettoSeduta
                newOggettoPartecipante.sedutaPartecipante = partecipante
               // newOggettoPartecipante.ruoloPartecipante = null
                newOggettoPartecipante.firmatario = false
                newOggettoPartecipante.sequenzaFirma = partecipante.sequenzaFirma
                newOggettoPartecipante.sequenza = partecipante.sequenzaPartecipante
                newOggettoPartecipante.presente = false
                newOggettoPartecipante.save()
            }

        }


            try {
            oggettoSeduta.dataDiscussione = Date.parse("dd/MM/yyyy", sedutaVotazione.oggettoSedutaVotazione.dataOraVotazione)
            oggettoSeduta.oraDiscussione = Date.parse("dd/MM/yyyy hh:mm", sedutaVotazione.oggettoSedutaVotazione.dataOraVotazione).format('HH:mm')
        } catch (Exception e) {
            messaggioErrore = "Il formato della data/ora della seduta non è corretto, Deve essere nel formato dd/MM/yyyy hh:mm"
            esitoWs.codice = 19
            esitoWs.messaggioErrore = messaggioErrore
            log.error("Errore ws setPropostaVoto: Codice 19 - "+messaggioErrore)
            return esitoWs
        }

        try {
            oggettoSeduta.save()
        } catch (Exception e) {
            log.error("Errore ws setPropostaVoto: Codice 20 - Errore per salvare la seduta: ${e.getMessage()}", e)
            esitoWs.codice = 20
            esitoWs.messaggioErrore = "Errore nel salvare la seduta"
            return esitoWs
        }

        esitoWs.codice = 0
        return esitoWs
    }

    /*
    Aggiungo valori al metodo getProposteSeduta (per ora usato solo dal ws REST)
     */
    GetProposteSedutaRespExt getProposteSedutaEstesa (GetProposteSedutaResp proposteSedutaResp) {

        GetProposteSedutaRespExt proposteSedutaRespExt = new GetProposteSedutaRespExt()
        SedutaPropostaExt sedutaExt = new SedutaPropostaExt()
        sedutaExt.idSeduta = proposteSedutaResp.seduta.idSeduta
        sedutaExt.idRiferimento  = proposteSedutaResp.seduta.idRiferimento
        sedutaExt.commissione  = proposteSedutaResp.seduta.commissione
        sedutaExt.anno =  proposteSedutaResp.seduta.anno
        sedutaExt.numero  = proposteSedutaResp.seduta.numero
        sedutaExt.odg  = proposteSedutaResp.seduta.odg

        Seduta seduta = Seduta.get(proposteSedutaResp.seduta.idSeduta)
        sedutaExt.dataConvocazione = seduta.dataSeduta.format("dd/MM/yyyy")
        sedutaExt.oraConvocazione = seduta.oraSeduta

        proposteSedutaRespExt.sedutaExt = sedutaExt
        proposteSedutaRespExt.esito = proposteSedutaResp.esito

        return proposteSedutaRespExt
    }

    public String ObjToStringXml(Object object){
        try {
            if (object == null)
                return ""
            if (object instanceof String
                    || object instanceof Integer
                    || object instanceof Long
                    || object instanceof Boolean
            ) {
                return object.toString()
            }

            StringWriter sw = new StringWriter();
            try {
                JAXB.marshal(object, sw);
            } catch (Exception ex) {
                log.info("[AttiWebService]: errore nella scrittura sul log della request di setPropostaVoto")
            }
            return sw.toString().replace("xsi:nil=\"true\"","").replace("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"","")
        }
        catch  (Exception ex){
            return ""
        }

    }

     static byte[] toBytes(DataHandler handler) throws IOException {


        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.writeTo(output);
        return output.toByteArray();
    }

    List<DizionarioContabile> getDatiDizionariContabili(String input,Soggetto operatore, String tipo) {
        List<DizionarioContabile> ret = new ArrayList<DizionarioContabile>()
        Document docInput = null

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null

        docBuilder = docFactory.newDocumentBuilder()
        docInput = docBuilder.parse(new InputSource(new StringReader(input)))

        NodeList nodeList = docInput.getElementsByTagName("dizionario");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node dizionario = nodeList.item(i)

            DizionarioContabile nuovo = new DizionarioContabile()
            NodeList nodeListDizionari = dizionario.getChildNodes()
            //nuovo.setUtenteIns(operatore.utente)
            nuovo.setTipo(tipo)


            for (int j = 0; j < nodeListDizionari.getLength(); j++) {

                if (nodeListDizionari.item(j).getNodeName().equals("codice"))
                    nuovo.setCodice(nodeListDizionari.item(j).getFirstChild().getNodeValue())

                if (nodeListDizionari.item(j).getNodeName().equals("descrizione"))
                    nuovo.setDescrizione(nodeListDizionari.item(j).getFirstChild().getNodeValue())

            }

            ret.add(nuovo)
        }

        return ret
    }

    String getXmlDatiDizionariContabili(String input) {
        StringBuilder ret = new StringBuilder("<root>")
        Document docInput = null

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null

        docBuilder = docFactory.newDocumentBuilder()
        docInput = docBuilder.parse(new InputSource(new StringReader(input)))

        NodeList nodeList = docInput.getElementsByTagName("dizionario");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node dizionario = nodeList.item(i)

            DizionarioContabile nuovo = new DizionarioContabile()
            NodeList nodeListDizionari = dizionario.getChildNodes()
            String xmlNodo="<dizionario>[CODICE]###[DESCRIZIONE]</dizionario>"
            String codice=""
            String descrizione=""
            for (int j = 0; j < nodeListDizionari.getLength(); j++) {
                if (nodeListDizionari.item(j).getNodeName().equals("codice"))
                    codice=nodeListDizionari.item(j).getFirstChild().getNodeValue().toString()

                if (nodeListDizionari.item(j).getNodeName().equals("descrizione"))
                    descrizione=nodeListDizionari.item(j).getFirstChild().getNodeValue().toString()
            }
            ret.append(xmlNodo.replace("[CODICE]",codice).replace("[DESCRIZIONE]",descrizione))
        }
        ret.append("</root>")
        return ret.toString()
    }

    CampiRicercaEstesa getCampiRicerca(String input){
        CampiRicercaEstesa ret= new CampiRicercaEstesa()
        Document docInput=null

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder=null

        docBuilder = docFactory.newDocumentBuilder()
        docInput = docBuilder.parse( new InputSource( new StringReader(input ) ) )

        Element rootInput = docInput.getDocumentElement()

        // definire i campi obbligatori

        ret.tipo=getXmlValue("tipo", rootInput)
        ret.oggetto=getXmlValue("oggetto",rootInput)

        ret.anno=text2Int(getXmlValue("annoAtto", rootInput))
        ret.numero=text2Int(getXmlValue("numeroAtto", rootInput))
        ret.numeroAttoDal=text2Long(getXmlValue("numeroAttoDal", rootInput))
        ret.numeroAttoAl=text2Long(getXmlValue("numeroAttoAl", rootInput))
        ret.dataAttoDal=text2Date(getXmlValue("dataAttoDal", rootInput))
        ret.dataAttoAl=text2Date(getXmlValue("dataAttoAl", rootInput))
        ret.registro=getXmlValue("registroAtto", rootInput)


        ret.codiceUnitaProponente=getXmlValue("codiceUnitaProponente", rootInput)
        ret.tipoPubblicazioneAlbo=getXmlValue("tipoPubblicazioneAlbo", rootInput)
        ret.codiceTipologiaAtto=getXmlValue("codiceTipologiaAtto", rootInput)

        ret.numeroProposta=text2Long(getXmlValue("numeroProposta", rootInput))
        ret.numeroPropostaDal=text2Long(getXmlValue("numeroPropostaDal", rootInput))
        ret.numeroPropostaAl=text2Long(getXmlValue("numeroPropostaAl", rootInput))
        ret.annoProposta=text2Long(getXmlValue("annoProposta", rootInput))

        ret.dataPubblicazioneDal=text2Date(getXmlValue("dataPubblicazioneDal", rootInput))
        ret.dataPubblicazioneAl=text2Date(getXmlValue("dataPubblicazioneAl", rootInput))

        ret.dataEsecutivitaDal=text2Date(getXmlValue("dataEsecutivitaDal", rootInput))
        ret.dataEsecutivitaAl=text2Date(getXmlValue("dataEsecutivitaAl", rootInput))


        ret.annoProtocollo=text2Long(getXmlValue("annoProtocollo", rootInput))
        ret.numeroProtocollo=text2Long(getXmlValue("numeroProtocollo", rootInput))
        ret.numeroProtocolloDal=text2Long(getXmlValue("numeroProtocolloDal", rootInput))
        ret.numeroProtocolloAl=text2Long(getXmlValue("numeroProtocolloAl", rootInput))
        //ret.dataProtocolloDal=text2Date(getXmlValue("dataProtocolloDal", rootInput))
        //ret.dataProtocolloAl=text2Date(getXmlValue("dataProtocolloAl", rootInput))
        ret.codiceClassifica=getXmlValue("codiceClassifica", rootInput)
        ret.numeroFascicolo=getXmlValue("numeroFascicolo", rootInput)
        ret.annoFascicolo=text2Int(getXmlValue("annoFascicolo", rootInput))
        ret.categoria=getXmlValue("categoria", rootInput)
        ret.inEvidenza=text2Int(getXmlValue("inEvidenza", rootInput))
        ret.dataInEvidenzaDa=text2Date(getXmlValue("dataInEvidenzaDa", rootInput))
        ret.dataInEvidenzaA=text2Date(getXmlValue("dataInEvidenzaA", rootInput))

        return ret
    }

    Integer text2Int(String numero){
        Integer ret
        if(numero!=null && numero.length()>0)
            try{
                ret=Integer.valueOf(numero)
            }catch(Exception ex){
                throw  new Exception("Errore nel formato dell'Intero ["+numero+"]",ex)
            }
        return ret
    }

    Long text2Long(String numero){
        Long ret
        if(numero!=null && numero.length()>0)
            try{
                ret=Long.valueOf(numero)
            }catch(Exception ex){
                throw  new Exception("Errore nel formato dell'Intero lungo ["+numero+"]",ex)
            }
        return ret
    }

    java.util.Date text2Date(String data) throws Exception{
        java.util.Date ret= null
        if(data!=null && data.length()>0) {
            try {
                ret = new SimpleDateFormat("dd/MM/yyyy").parse(data)
                return ret
            }catch (Exception ex){
                throw new Exception("Errore nel formato della data ["+data+"] ",ex )
            }
        }
    }

    String date2Text(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    public static String getXmlValue(String tagName, Element element) {
        NodeList list = element.getElementsByTagName(tagName)
        String ret=null
        if (list != null && list.getLength() > 0) {
            NodeList subList = list.item(0).getChildNodes()

            if (subList != null && subList.getLength() > 0) {
                ret= subList.item(0).getNodeValue()
            }
        }
        if(ret)
            ret=normalizeXml(ret)
        return ret;
    }

    public static String normalizeXml(String input){
        String ret=input;
        ret=ret.replace("&", "&amp;");
        ret=ret.replace("<", "&lt;");
        ret=ret.replace(">", "&gt;");
        ret=ret.replace("\"", "&quot;");
        ret=ret.replace("'", "&apos");
        return ret;
    }

    DocumentoEsteso toEstesoFormDocumento(Documento documento) {
        DocumentoEsteso documentoEsteso = new DocumentoEsteso()

        documentoEsteso.tipo = documento.tipo
        documentoEsteso.id = documento.id
        documentoEsteso.idRiferimento = documento.idRiferimento
        documentoEsteso.oggetto = documento.oggetto
        documentoEsteso.stato = documento.stato
        documentoEsteso.codiceTipologia = documento.codiceTipologia
        documentoEsteso.numero = documento.numero
        documentoEsteso.anno = documento.anno
        documentoEsteso.data = documento.data
        documentoEsteso.codiceRegistro = documento.codiceRegistro
        documentoEsteso.numero2 = documento.numero2
        documentoEsteso.anno2 = documento.anno2
        documentoEsteso.dataNumero2 = documento.dataNumero2
        documentoEsteso.codiceRegistro2 = documento.codiceRegistro2
        documentoEsteso.numeroProtocollo = documento.numeroProtocollo
        documentoEsteso.annoProtocollo = documento.annoProtocollo
        documentoEsteso.dataProtocollo = documento.dataProtocollo
        documentoEsteso.fascicolo = documento.fascicolo
        documentoEsteso.classificazione = documento.classificazione
        documentoEsteso.numeroProposta = documento.numeroProposta
        documentoEsteso.annoProposta = documento.annoProposta
        documentoEsteso.dataProposta = documento.dataProposta
        documentoEsteso.dataEsecutivita = documento.dataEsecutivita
        documentoEsteso.dataInizioPubblicazione = documento.dataInizioPubblicazione
        documentoEsteso.dataFinePubblicazione = documento.dataFinePubblicazione
        documentoEsteso.numeroAlbo = documento.numeroAlbo
        documentoEsteso.annoAlbo = documento.annoAlbo
        documentoEsteso.redattore = documento.redattore
        documentoEsteso.funzionario = documento.funzionario
        documentoEsteso.dirigente = documento.dirigente
        documentoEsteso.unitaProponente = documento.unitaProponente
        documentoEsteso.testo = documento.testo
        documentoEsteso.allegati = documento.allegati
        documentoEsteso.vistiPareri = documento.vistiPareri
        documentoEsteso.certificati = documento.certificati
        documentoEsteso.soggetti = documento.soggetti

        return documentoEsteso
    }

    Documento toDocumentoFormEsteso(DocumentoEsteso documentoEsteso) {
        Documento documento = new Documento()

        documento.tipo = documentoEsteso.tipo
        documento.id = documentoEsteso.id
        documento.idRiferimento = documentoEsteso.idRiferimento
        documento.oggetto = documentoEsteso.oggetto
        documento.stato = documentoEsteso.stato
        documento.codiceTipologia = documentoEsteso.codiceTipologia
        documento.numero = documentoEsteso.numero
        documento.anno = documentoEsteso.anno
        documento.data = documentoEsteso.data
        documento.codiceRegistro = documentoEsteso.codiceRegistro
        documento.numero2 = documentoEsteso.numero2
        documento.anno2 = documentoEsteso.anno2
        documento.dataNumero2 = documentoEsteso.dataNumero2
        documento.codiceRegistro2 = documentoEsteso.codiceRegistro2
        documento.numeroProtocollo = documentoEsteso.numeroProtocollo
        documento.annoProtocollo = documentoEsteso.annoProtocollo
        documento.dataProtocollo = documentoEsteso.dataProtocollo
        documento.fascicolo = documentoEsteso.fascicolo
        documento.classificazione = documentoEsteso.classificazione
        documento.numeroProposta = documentoEsteso.numeroProposta
        documento.annoProposta = documentoEsteso.annoProposta
        documento.dataProposta = documentoEsteso.dataProposta
        documento.dataEsecutivita = documentoEsteso.dataEsecutivita
        documento.dataInizioPubblicazione = documentoEsteso.dataInizioPubblicazione
        documento.dataFinePubblicazione = documentoEsteso.dataFinePubblicazione
        documento.numeroAlbo = documentoEsteso.numeroAlbo
        documento.annoAlbo = documentoEsteso.annoAlbo
        documento.redattore = documentoEsteso.redattore
        documento.funzionario = documentoEsteso.funzionario
        documento.dirigente = documentoEsteso.dirigente
        documento.unitaProponente = documentoEsteso.unitaProponente
        documento.testo = documentoEsteso.testo
        documento.allegati = documentoEsteso.allegati
        documento.vistiPareri = documentoEsteso.vistiPareri
        documento.certificati = documentoEsteso.certificati
        documento.soggetti = documentoEsteso.soggetti

        return documento
    }
}