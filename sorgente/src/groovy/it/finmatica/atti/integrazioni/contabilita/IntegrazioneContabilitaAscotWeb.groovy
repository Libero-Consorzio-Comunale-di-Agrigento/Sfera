package it.finmatica.atti.integrazioni.contabilita

import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.contabilita.MovimentoContabileInterno
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.documenti.DatoAggiuntivo
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.TipoDatoAggiuntivo
import it.finmatica.atti.dto.documenti.DatoAggiuntivoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.TipoFile
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.GestioneTestiTipoModello
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.zkoss.bind.BindUtils
import org.zkoss.zk.ui.event.EventQueues

@Component("integrazioneContabilitaAscotWeb")
@Lazy
class IntegrazioneContabilitaAscotWeb extends AbstractIntegrazioneContabilita {

    @Autowired
    IntegrazioneContabilitaAscotWebConfig integrazioneContabilitaAscotWebConfig
    @Autowired
    DatiAggiuntiviService                 datiAggiuntiviService

    @Override
    String getZul(IDocumento documento) {
        return "/atti/integrazioni/contabilita/movimentiContabiliAscot.zul"
    }

    @Override
    boolean isConDocumentiContabili(IDocumento documento) {
        def documentoPrincipale = getProposta(documento);

        String gestioneInterna = MappingIntegrazione.getValoreEsterno("CONTABILITA_ASCOT", "GESTIONE_INTERNA").trim()
        if (gestioneInterna == 'N') {
            return (MovimentoContabile.countByIdDocumentoAndTipoDocumento(documentoPrincipale.id, documentoPrincipale.TIPO_OGGETTO) > 0)
        } else {
              if (documento instanceof Determina) {
                return (MovimentoContabileInterno.countByDetermina(documentoPrincipale) > 0)
            } else {
                return (MovimentoContabileInterno.countByPropostaDelibera(documentoPrincipale) > 0)
            }
        }

    }

    @Override
    boolean isTipiDocumentoAbilitati() {
        return false;
    }

    void aggiornaMaschera(IDocumento documento, boolean modifica) {
        BindUtils.postGlobalCommand("movimentiContabiliQueue", EventQueues.DESKTOP, "aggiornaAtto", [atto: documento, competenza: modifica?"W":"R"])
    }

    List<?> getMovimentiContabili(IDocumento documento) {
        def documentoPrincipale = getProposta(documento);
        aggiornaMovimentiContabili(documento)
        return MovimentoContabile.findAllByIdDocumentoAndTipoDocumento(documentoPrincipale.id, documentoPrincipale.TIPO_OGGETTO, [sort: 'tipo', order: 'asc']);
    }

    InputStream getSchedaContabile(IDocumento documento) {
        return null
    }

    public String getToken() {
        def rest = new RESTClient(integrazioneContabilitaAscotWebConfig.getUrlWebService())
        def response = rest.post(path: new URI(integrazioneContabilitaAscotWebConfig.getUrlWebService()).getPath()+"/security/auth",
                requestContentType: ContentType.URLENC,
                contentType: ContentType.JSON,
                body: [username: integrazioneContabilitaAscotWebConfig.getUtenteWebService(), password: integrazioneContabilitaAscotWebConfig.getPasswordWebService()])

        HttpResponseDecorator decorator = response as HttpResponseDecorator;
        JSONObject json = decorator.getData() as JSONObject;
        if (json.esito?.status == 'OK') {
            String token = json?.dati[0]
            log.info("Token ${token}")
            return token
        } else {
            throw new AttiRuntimeException("Connessione fallita");
        }
    }

    @Transactional
    void aggiornaMovimentiContabili(IDocumento documento) {
        IProposta proposta = getProposta(documento);
        DatoAggiuntivo datoAggiuntivo = datiAggiuntiviService.getDatoAggiuntivo(proposta, TipoDatoAggiuntivo.CONTABILITA_ASCOT)
        if (datoAggiuntivo != null && datoAggiuntivo?.valore?.contains("#")) {
            String[] valori = datoAggiuntivo.valore.split("#")
            def rest = new RESTClient(integrazioneContabilitaAscotWebConfig.getUrlWebService())
            String token = getToken();
            rest.headers['Authorization'] = "Bearer ${token}"
            def response = rest.get(path: new URI(integrazioneContabilitaAscotWebConfig.getUrlWebService()).getPath()+"/delibere/movimenti",
                    query: [esercizioDeliberaAscot  : valori[0],
                            progressivoDeliberaAscot: valori[1]])
            HttpResponseDecorator decorato = (HttpResponseDecorator) response;
            JSONObject json = (JSONObject) decorato.getData()
            if (json.esito?.status == 'OK') {
                log.info(json.dati.toString())
                upsertDettaglioContabile(proposta, json.dati)
            }
        }
    }

    @Transactional
    void inserisciProposta(IDocumento documento) {
        IProposta proposta = getProposta(documento);
        DatoAggiuntivo datoAggiuntivo = datiAggiuntiviService.getDatoAggiuntivo(proposta, TipoDatoAggiuntivo.CONTABILITA_ASCOT)
        if (datoAggiuntivo == null) {
            def rest = new RESTClient(integrazioneContabilitaAscotWebConfig.getUrlWebService())
            String token = getToken();
            rest.headers['Authorization'] = "Bearer ${token}"
            def response = rest.post(path: new URI(integrazioneContabilitaAscotWebConfig.getUrlWebService()).getPath()+"/delibere/insertProposta",
                    requestContentType: ContentType.URLENC,
                    contentType: ContentType.JSON,
                    body: [esercizioProposta: proposta.annoProposta,
                           numeroProposta   : proposta.numeroProposta,
                           dataProposta     : proposta.dataNumeroProposta.format("yyyy-MM-dd"),
                           tipoDelibera     : null,
                           oggetto          : proposta.oggetto])
            HttpResponseDecorator decorato = (HttpResponseDecorator) response;
            JSONObject json = (JSONObject) decorato.getData()
            if (json.esito?.status == 'OK') {
                log.debug(json.dati.toString())
                String id = json?.dati?.esercizioDeliberaAscot[0] + "#" + json?.dati?.progressivoDeliberaAscot[0]
                DatoAggiuntivoDTO datoAggiuntivoDTO = new DatoAggiuntivoDTO()
                datoAggiuntivoDTO.codice = TipoDatoAggiuntivo.CONTABILITA_ASCOT
                datoAggiuntivoDTO.valore = id
                def dto = proposta.toDTO()
                dto.addToDatiAggiuntivi(datoAggiuntivoDTO)
                datiAggiuntiviService.salvaDatiAggiuntivi(proposta, dto)
                proposta.save()
            } else if (json.esito?.status == 'KO') {
                log.error(json.esito.message)
                throw new AttiRuntimeException("Errore durante la richiesta di inserimento proposta in contabilità: " + json.esito.message)
            }
        }
    }

    @Transactional
    void esecutivitaAtto(IAtto documento) {
        IProposta proposta = getProposta(documento);
        DatoAggiuntivo datoAggiuntivo = datiAggiuntiviService.getDatoAggiuntivo(proposta, TipoDatoAggiuntivo.CONTABILITA_ASCOT)
        if (datoAggiuntivo != null && datoAggiuntivo?.valore?.contains("#")) {
            String[] valori = datoAggiuntivo.valore.split("#")
            def rest = new RESTClient(integrazioneContabilitaAscotWebConfig.getUrlWebService())
            String token = getToken();
            rest.headers['Authorization'] = "Bearer ${token}"
            def response = rest.post(path: new URI(integrazioneContabilitaAscotWebConfig.getUrlWebService()).getPath()+"/delibere/esecutivita",
                    requestContentType: ContentType.URLENC,
                    contentType: ContentType.JSON,
                    body: [esercizioAscot   : valori[0],
                           progressivoAscot : valori[1],
                           tipoDelibera     : null,
                           numeroEsecutivita: documento.numeroAtto,
                           dataEsecutivita  : documento.getDataEsecutivita().format("yyyy-MM-dd")])
            HttpResponseDecorator decorato = (HttpResponseDecorator) response;
            JSONObject json = (JSONObject) decorato.getData()
            if (json.esito?.status == 'KO') {
                log.error(json.esito.message)
                throw new AttiRuntimeException("Errore durante la richiesta di esecutività dell'atto in contabilità: " + json.esito.message)
            }
        }
    }

    @Transactional
    void adozioneAtto(IAtto documento) {
        IProposta proposta = getProposta(documento);
        DatoAggiuntivo datoAggiuntivo = datiAggiuntiviService.getDatoAggiuntivo(proposta, TipoDatoAggiuntivo.CONTABILITA_ASCOT)
        if (datoAggiuntivo != null && datoAggiuntivo?.valore?.contains("#")) {
            String[] valori = datoAggiuntivo.valore.split("#")
            def rest = new RESTClient(integrazioneContabilitaAscotWebConfig.getUrlWebService())
            String token = getToken();
            rest.headers['Authorization'] = "Bearer ${token}"
            def response = rest.post(path: new URI(integrazioneContabilitaAscotWebConfig.getUrlWebService()).getPath()+"/delibere/adozione",
                    requestContentType: ContentType.URLENC,
                    contentType: ContentType.JSON,
                    body: [esercizioAscot  : valori[0],
                           progressivoAscot: valori[1],
                           tipoDelibera    : getCodiceRegistroEsterno(proposta),
                           numeroAdozione  : documento.numeroAtto,
                           dataAdozione    : documento.getDataAtto().format("yyyy-MM-dd")])
            HttpResponseDecorator decorato = (HttpResponseDecorator) response;
            JSONObject json = (JSONObject) decorato.getData()
            if (json.esito?.status == 'KO') {
                log.error(json.esito.message)
                throw new AttiRuntimeException("Errore durante la richiesta di adozione dell'atto in contabilità: " + json.esito.message)
            }
        }
    }

    private void upsertDettaglioContabile(IProposta proposta, JSONArray records) {
        // prima svuoto i movimenti contabili
        MovimentoContabile.findAllByIdDocumentoAndTipoDocumento(proposta.id, proposta.TIPO_OGGETTO)*.delete()

        // poi li ricreo
        records.each {
            MovimentoContabile m = new MovimentoContabile()
            m.idDocumento = proposta.id
            m.tipoDocumento = proposta.tipoOggetto
            m.tipoDettaglio = it.tipoMovimento
            m.progressivoEsterno = it.progressivoDeliberaAscot
            m.esercizioEsterno = it.esercizioDeliberaAscot
            m.tipo = it.EoS?.equals("E") ? MovimentoContabile.TIPO_ENTRATA : MovimentoContabile.TIPO_USCITA
            m.annoEsercizio = it.esercizio
            m.esercizioEsterno = it.esercizioProvenienza
            m.capitolo = it.capitolo
            m.numeroMovimento = it.numeroMovimento
            m.importo = it.importo
            if (it.codiceCIG != null && !it.codiceCIG.equals("null")) {
                m.cig = it.codiceCIG
            }
            m.descrizionePdcf = it.descrizionePDCF
            m.codicePdcf = it.codicePDCF
            m.esecutivita = it.esecutivita
            if (it.dataMovimento != null) {
                try {
                    m.dataMovimento = Date.parse("yyyy-MM-dd", it.dataMovimento)
                } catch (Exception ex) {
                }
            }
            m.save()
        }
    }

    private String getCodiceRegistroEsterno(IProposta proposta) {
        if (proposta instanceof Determina) {
            return proposta.tipologia.tipoRegistro?.registroEsterno
        } else if (proposta instanceof PropostaDelibera) {
            return (proposta.tipologia.tipoRegistroDelibera?.registroEsterno) ?: (proposta.commissione?.tipoRegistro?.registroEsterno)
        }
    }

    @Transactional
    public JSONArray getCapitoli(String esercizio, String esercizioProvenienza, String EoS) {
        def rest = new RESTClient(integrazioneContabilitaAscotWebConfig.getUrlWebService())
        String token = getToken();
        rest.headers['Authorization'] = "Bearer ${token}"
        def response = rest.get(path: new URI(integrazioneContabilitaAscotWebConfig.getUrlWebService()).getPath()+"/capitoli",
                requestContentType: ContentType.URLENC,
                contentType: ContentType.JSON,
                query: [esercizio           : esercizio,
                        esercizioProvenienza: esercizioProvenienza,
                        EoS                 : EoS])
        HttpResponseDecorator decorato = (HttpResponseDecorator) response;
        JSONObject json = (JSONObject) decorato.getData()

        if (json.esito?.status == 'OK') {
            return json.dati
        } else {
            log.error(json.esito.message)
            throw new AttiRuntimeException("Errore durante la richiesta di elenco capitoli: " + json.esito.message)
        }
    }

    @Transactional
    public JSONArray getCapitoliMovInt(String esercizio, String esercizioProvenienza, String EoS, String capitolo, String articolo) {
        def rest = new RESTClient(integrazioneContabilitaAscotWebConfig.getUrlWebService())
        String token = getToken();
        rest.headers['Authorization'] = "Bearer ${token}"
        def response = rest.get(path: new URI(integrazioneContabilitaAscotWebConfig.getUrlWebService()).getPath()+"/capitoli2",
                requestContentType: ContentType.URLENC,
                contentType: ContentType.JSON,
                query: [esercizio           : esercizio,
                        esercizioProvenienza: esercizioProvenienza,
                        EoS                 : EoS,
                        articolo            : articolo,
                        capitolo            : capitolo
                ])
        HttpResponseDecorator decorato = (HttpResponseDecorator) response;
        JSONObject json = (JSONObject) decorato.getData()

        if (json.esito?.status == 'OK') {
            return json.dati
        } else {
            log.error(json.esito.message)
            throw new AttiRuntimeException("Errore durante la richiesta di elenco capitoli: " + json.esito.message)
        }
    }

    @Transactional
    public JSONArray getSoggetti(String cognome, String nome, String cf, String piva, String idSoggetto) {
        if (cognome == null || cognome=='null') {
            cognome = ''
        }
        if (nome == null || nome=='null') {
            nome = ''
        }
        if (cf == null || cf=='null') {
            cf = ''
        }
        if (piva == null || piva=='null') {
            piva = ''
        }
        if (idSoggetto == null || idSoggetto=='null') {
            idSoggetto = ''
        }
        def rest = new RESTClient(integrazioneContabilitaAscotWebConfig.getUrlWebService())
        String token = getToken();
        rest.headers['Authorization'] = "Bearer ${token}"
        def response = rest.get(path: new URI(integrazioneContabilitaAscotWebConfig.getUrlWebService()).getPath()+"/soggetti",
                requestContentType: ContentType.URLENC,
                contentType: ContentType.JSON,
                query: [CF        : cf,
                        cognome   : cognome,
                        nome      : nome,
                        PIVA      : piva,
                        idSoggetto: idSoggetto])
        HttpResponseDecorator decorato = (HttpResponseDecorator) response;
        JSONObject json = (JSONObject) decorato.getData()

        if (json.esito?.status == 'OK') {
            return json.dati
        } else {
            log.error(json.esito.message)
            throw new AttiRuntimeException("Errore durante la richiesta di elenco soggetti: " + json.esito.message)
        }
    }
}
