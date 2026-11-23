package it.finmatica.atti.integrazioni.pnd

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import grails.plugin.springsecurity.SpringSecurityService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.json.internal.LazyMap
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.*
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioni.pnd.classes.GenericError
import it.finmatica.atti.integrazioni.pnd.classes.GenericResponse
import it.finmatica.atti.integrazioni.pnd.classes.PdnException
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

import java.nio.charset.StandardCharsets

@Component
@Lazy
class SmartPnd {

    private static final Logger log = Logger.getLogger(SmartPnd.class)

    @Autowired
    SpringSecurityService springSecurityService
    @Autowired
    SmartPndConfig smartPndConfig
    @Autowired
    IGestoreFile gestoreFile
    @Autowired
    AllegatoService allegatoService

    boolean isNotificaPagamento(Long idTipologia) {
        boolean flag = false
        String servizio = smartPndConfig.getServizio(idTipologia)
        if (servizio)
            flag = true

        return flag
    }

    int getGiorniScadenza(Long idTipologia) {
        return smartPndConfig.getGiorniScadenza(idTipologia).toInteger()
    }

    void creaNotifica(IAtto atto) {
        //metodo per invocare lo smartpnd...per ora implementato solo per la determina
        if (atto instanceof Determina) {
            Determina documento = Determina.get(atto.id)

            if (documento) {
                //gli allegati sono uguali per tutti i destinatari...quindi li gestisco all'inizio e non dentro al ciclo
                //Creo un array con tutti i file
                List mapAllegati = new ArrayList()
                //Aggiungo il testo princinciaple
                LinkedHashMap testoPrincipale = [
                        "descrizione"   : "Documento Principale",
                        "filename"      : replaceCaratteri(documento.testo.nome),
                        "firmare"       : "N",
                        "idOggettoFile" : documento.testo.idFileEsterno,
                        "controlloCompetenze"    : documento.riservato
                ]
                mapAllegati.add(testoPrincipale)

                int contaAllegati = 0
                for (Allegato allegato : documento.allegati.sort { it.sequenza }) {
                    //Sembra che pnd accetti solo 10 file (quindi testo principale + 8 file...il non sarà la ricevuta di depag
                    if (contaAllegati < 8) {
                        // non vanno pubblicati gli allegati non validi
                        if (allegato.valido) {
                            // allego i file
                            for (FileAllegato fileAllegato : allegato.fileAllegati.sort { it.id }) {
                                LinkedHashMap nodoFile = [
                                        "descrizione"   : allegato.titolo,
                                        "filename"      : replaceCaratteri(allegato.sequenza+"_"+fileAllegato.nome),
                                        "firmare"       : "N",
                                        "idOggettoFile" : fileAllegato.idFileEsterno,
                                        "controlloCompetenze"    : allegato.riservato
                                ]

                                mapAllegati.add(nodoFile)
                                contaAllegati = contaAllegati + 1
                            }
                        }
                    }
                }

                //scorro tutti i destinatari
                for (DestinatarioNotifica dest in documento.destinatariNotifiche) {
                    //considereo solo le notifiche esterne, ti tipo PND, non ancora notificate
                    if ((DestinatarioNotifica.TIPO_DESTINATARIO_ESTERNO.equals(dest.tipoDestinatario)) && (DestinatarioNotifica.TIPO_NOTIFICA_PND.equals(dest.tipoNotifica)) && (dest.codiceEsterno == null)) {

                        //memorizzo i dati dell'utente in variabili per non accedere piu' volte
                        String codiceFiscale = dest.codiceFiscale
                        String cognome = dest.cognome
                        String nome = dest.nome
                        String email = dest.indirizzoEmail
                        String indirizzo = dest.indirizzo.replaceAll(',', ' ').replaceAll('  ',' ');
                        String comune = dest.comune
                        String provincia = dest.provincia
                        String cap = dest.cap

                        List mapInvioPnd = new ArrayList()
                        List mapDestinatari = new ArrayList()
                        LinkedHashMap destinatario
                        destinatario = [
                                "tipoDestinatario": "PF",
                                "cfiscPiva"       : codiceFiscale,
                                "cognome"         : cognome,
                                "nome"            : nome,
                                "email"           : email,
                                "indirizzo"       : indirizzo,
                                "comune"          : comune,
                                "provincia"       : provincia,
                                "cap"             : cap
                        ]
                        mapDestinatari.add(destinatario)

                        LinkedHashMap tagInvioPnd = [
                                "tag"                      : smartPndConfig.getTagPnd(),
                                "oggetto"                  : documento.oggetto,
                                "notificationFeePolicy"    : smartPndConfig.getNotificationFeePolicy(),
                                "physicalCommunicationType": smartPndConfig.getPhysicalCommunicationType(),
                                "senderPiva"               : documento.ente.soggetto.partitaIva ?: "",
                                "senderDenom"              : documento.ente.soggetto.denominazione,  //da capire se mettere parametro o se va bene l'ente
                                "destinatari"              : mapDestinatari
                        ]
                        mapInvioPnd.add(tagInvioPnd)

                        List mapPagamenti = new ArrayList()
                        if (isNotificaPagamento(documento.tipologia.id)) {
                            List mapVersamenti = new ArrayList()
                            LinkedHashMap versamento
                            versamento = ["progressivo": 1,
                                          "servizio"   : smartPndConfig.getServizio(documento.tipologia.id),
                                          "causale"    : documento.oggetto,
                                          "importo"    : dest.importo]
                            mapVersamenti.add(versamento)


                            LinkedHashMap pagamento
                            pagamento = [
                                    "tipoPagatore"      : "F",
                                    "chiave"            : documento.id + '#' + dest.id,
                                    "codicePagatore"    : codiceFiscale,
                                    "anagraficaPagatore": cognome+" "+nome,
                                    "causaleVersamento" : documento.oggetto,
                                    "dataScadenza"      : (dest.dataScadenza?.format("dd/MM/yyyy")) ?: "",
                                    "ggScadenza"        : dest.giorniScadenza,
                                    "importoTotale"     : dest.importo,
                                    "versamenti"        : mapVersamenti
                            ]

                            mapPagamenti.add(pagamento)
                        }

                        //Creo un array con tutti i tipi allegati da passare a PND
                        List mapTipiAllegati = new ArrayList()
                        //Aggiungo il testo princinciaple
                        LinkedHashMap tipiAllegati = [
                                "NotificaPND"   : smartPndConfig.getTipoAllegatoNotificaPnd(),
                                "NotificaDEPAG" : smartPndConfig.getTipoAllegatoNotificaDePag()
                        ]

                        mapTipiAllegati.add(tipiAllegati)

                        LinkedHashMap mapComunicazione = [
                                "applicativo"           : "SFERA",  //per ora lo metto fisso e non parametrico
                                "inSospeso"             : "N",
                                "idRifApplicativo"      : documento.id + '#' + dest.id,
                                "codEnte"               : documento.ente.codice,
                                "cfiscPiva"             : codiceFiscale,
                                "cognomeRgs"            : cognome,
                                "nome"                  : nome,
                                "data"                  : new Date().format("dd/MM/yyyy"),
                                "tipoPratica"           : documento.tipologiaDocumento.titolo,
                                "dataPratica"           : (documento.dataAtto?.format("dd/MM/yyyy")) ?: "",
                                "annoPratica"           : documento.annoAtto,
                                "numeroPratica"         : documento.numeroAtto,
                                "labelPratica"          : documento.tipologiaDocumento.titolo,
                                "oggetto"               : documento.oggetto,
                                "tipoComunicazione"     : smartPndConfig.getTipoComunicazione(documento.tipologia.id),
                                "classCod"              : documento.classificaCodice ?: "",
                                "fascicoloAnno"         : documento.fascicoloAnno ?: "",
                                "fascicoloNumero"       : documento.fascicoloNumero ?: "",
                                // "tipoComunicazioneDescr": documento.tipologia.titolo,
                                "utenteAd4"             : springSecurityService.currentUser.id,
                                "inviaPagamenti"        : isNotificaPagamento(documento.tipologia.id) ? "S" : "N",
                                "pagamenti"             : mapPagamenti,
                                "allegati"              : mapAllegati,
                                "invioPND"              : mapInvioPnd,
                                "tipiAllegati"          : mapTipiAllegati]


                        GenericResponse resp = creaComunicazione(mapComunicazione, smartPndConfig.getUrlWebService(), smartPndConfig.getUtenteWebService(), smartPndConfig.getPasswordWebService())

                        if (resp.status.toString() == GenericResponse.KO) {
                            throw new AttiRuntimeException(resp.message.substring(0,800))
                        }
                        Long idComunicazione = resp?.idComunicazione
                        //memorizzo l'id della comunicazione
                        if (idComunicazione) {
                            dest.codiceEsterno = idComunicazione
                            dest.save()
                        }
                    }
                }
            }
        }
    }

    GenericResponse creaComunicazione(LinkedHashMap mapComunicazione, String endPoint, String user, String passwd) throws Exception {
        GenericResponse ret = null
        String path = "/creaComunicazione"
        endPoint += path

        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = new MediaType("application", "json", StandardCharsets.UTF_8);
        headers.setContentType(mediaType);

        String auth = user + ":" + passwd

        String authHeader = "Basic " + auth.bytes.encodeBase64().toString()
        headers.set("Authorization", authHeader);

        String jsonBody = JsonOutput.toJson(mapComunicazione)
        log.info("SmartPnd - creaComunicazione - request: " + jsonBody)
        HttpEntity<String> request = new HttpEntity<String>(jsonBody, headers);

        String jsonResponse
        RestTemplate restTemplate = new RestTemplate()
        jsonResponse = restTemplate.postForObject(endPoint, request, String.class);

        log.info("SmartPnd - creaComunicazione - response: " + jsonResponse)

        ObjectMapper objectMapper = new ObjectMapper()
        LazyMap json = new JsonSlurper().parseText(jsonResponse)

        if (json.status.toString() == GenericResponse.OK || json.status.toString() == GenericResponse.KO) {
            ret = objectMapper.readValue(jsonResponse, GenericResponse.class)
        } else {
            GenericError genericError = objectMapper.readValue(jsonResponse, GenericError.class)
            throw new PdnException(genericError)
        }
        return ret
    }

//    GenericResponse uploadFileComunicazione(Long idComunicazione, String filename, String fileBase64, String endPoint, String user, String passwd) throws Exception {
//        GenericResponse ret = null
//        String path = "/uploadFileComunicazioneBase64"
//        endPoint += path
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA)
//        String auth = user + ":" + passwd
//
//        String authHeader = "Basic " + auth.bytes.encodeBase64().toString()
//        headers.set("Authorization", authHeader);
//
//        MultiValueMap<String, Object> map = new LinkedMultiValueMap()
//        map.add("idComunicazione", idComunicazione)
//        map.add("filename", filename)
//        map.add("fileBase64", fileBase64)
//
//        HttpEntity<String> request = new HttpEntity<String>(map, headers)
//        log.info("SmartPnd - uploadFileComunicazione - idComunicazione: " + idComunicazione+ " - filename: "+filename)
//        String jsonResponse
//        RestTemplate restTemplate = new RestTemplate()
//
//        jsonResponse = restTemplate.postForObject(endPoint, request, String.class);
//        log.info("SmartPnd - uploadFileComunicazione - response: " + jsonResponse)
//        ObjectMapper objectMapper = new ObjectMapper()
//        LazyMap json = new JsonSlurper().parseText(jsonResponse)
//        if (json.status.toString() == GenericResponse.OK || json.status.toString() == GenericResponse.KO) {
//            ret = objectMapper.readValue(jsonResponse, GenericResponse.class)
//        } else {
//            GenericError genericError = objectMapper.readValue(jsonResponse, GenericError.class)
//            throw new PdnException(genericError)
//        }
//        return ret
//    }

//    ComunicazioneResponse getComunicazione(Long idComunicazione, String endPoint, String user, String passwd) throws Exception {
//        ComunicazioneResponse ret = null
//        String path = "/getComunicazione"
//        endPoint += path + "?idComunicazione=" + idComunicazione.toString()
//
//        HttpHeaders headers = new HttpHeaders();
//        MediaType mediaType = new MediaType("application", "json", StandardCharsets.UTF_8);
//        headers.setContentType(mediaType);
//
//        String auth = user + ":" + passwd
//
//        String authHeader = "Basic " + auth.bytes.encodeBase64().toString()
//        headers.set("Authorization", authHeader);
//
//        RestTemplate restTemplate = new RestTemplate()
//        String jsonResponse = restTemplate.getForObject(endPoint, String.class)  // (endPoint, request, String.class);
//        ObjectMapper objectMapper = new ObjectMapper()
//        Map<String, ?> json = (Map<String, ?>) (new JsonSlurper().parseText(jsonResponse))
//        if (json.applicativo?.toString().length() > 0) {
//            ret = objectMapper.readValue(jsonResponse, ComunicazioneResponse.class)
//        } else {
//            if (json.status?.toString().equals(GenericResponse.KO)) {
//                GenericResponse genericResponse = objectMapper.readValue(jsonResponse, GenericResponse.class)
//                throw new PdnException(genericResponse)
//            } else {
//                GenericError genericError = objectMapper.readValue(jsonResponse, GenericError.class)
//                throw new PdnException(genericError)
//            }
//        }
//        return ret
//    }
//
//    GenericResponse eliminaComunicazione(Long idComunicazione, String endPoint, String user, String passwd) throws Exception {
//        GenericResponse ret = null
//        String path = "/eliminaComunicazione"
//        endPoint += path + "?idComunicazione=" + idComunicazione.toString()
//
//        HttpHeaders headers = new HttpHeaders();
//        MediaType mediaType = new MediaType("application", "json", StandardCharsets.UTF_8);
//        headers.setContentType(mediaType);
//
//        String auth = user + ":" + passwd
//
//        String authHeader = "Basic " + auth.bytes.encodeBase64().toString()
//        headers.set("Authorization", authHeader);
//
//        RestTemplate restTemplate = new RestTemplate()
//        String jsonResponse = restTemplate.getForObject(endPoint, String.class)  // (endPoint, request, String.class);
//        ObjectMapper objectMapper = new ObjectMapper()
//        Map<String, ?> json = (Map<String, ?>) (new JsonSlurper().parseText(jsonResponse))
//        if (json.status.toString() == GenericResponse.OK || json.status.toString() == GenericResponse.KO) {
//            ret = objectMapper.readValue(jsonResponse, GenericResponse.class)
//        } else {
//            GenericError genericError = objectMapper.readValue(jsonResponse, GenericError.class)
//            throw new PdnException(genericError)
//        }
//        return ret
//    }
//
//    GenericResponse abilitaComunicazione(Long idComunicazione, String endPoint, String user, String passwd) throws Exception {
//        GenericResponse ret = null
//        String path = "/abilitaComunicazione"
//        endPoint += path + "?idComunicazione=" + idComunicazione.toString()
//
//        HttpHeaders headers = new HttpHeaders();
//        MediaType mediaType = new MediaType("application", "json", StandardCharsets.UTF_8);
//        headers.setContentType(mediaType);
//
//        String auth = user + ":" + passwd
//
//        String authHeader = "Basic " + auth.bytes.encodeBase64().toString()
//        headers.set("Authorization", authHeader);
//
//        RestTemplate restTemplate = new RestTemplate()
//        String jsonResponse = restTemplate.getForObject(endPoint, String.class)  // (endPoint, request, String.class);
//        ObjectMapper objectMapper = new ObjectMapper()
//        Map<String, ?> json = (Map<String, ?>) (new JsonSlurper().parseText(jsonResponse))
//        if (json.status.toString() == GenericResponse.OK || json.status.toString() == GenericResponse.KO) {
//            ret = objectMapper.readValue(jsonResponse, GenericResponse.class)
//        } else {
//            GenericError genericError = objectMapper.readValue(jsonResponse, GenericError.class)
//            throw new PdnException(genericError)
//        }
//        return ret
//    }
//
//    GenericResponse disabilitaComunicazione(Long idComunicazione, String endPoint, String user, String passwd) throws Exception {
//        GenericResponse ret = null
//        String path = "/disabilitaComunicazione"
//        endPoint += path + "?idComunicazione=" + idComunicazione.toString()
//
//        HttpHeaders headers = new HttpHeaders();
//        MediaType mediaType = new MediaType("application", "json", StandardCharsets.UTF_8);
//        headers.setContentType(mediaType);
//
//        String auth = user + ":" + passwd
//
//        String authHeader = "Basic " + auth.bytes.encodeBase64().toString()
//        headers.set("Authorization", authHeader);
//
//        RestTemplate restTemplate = new RestTemplate()
//        String jsonResponse = restTemplate.getForObject(endPoint, String.class)  // (endPoint, request, String.class);
//        ObjectMapper objectMapper = new ObjectMapper()
//        Map<String, ?> json = (Map<String, ?>) (new JsonSlurper().parseText(jsonResponse))
//        if (json.status.toString() == GenericResponse.OK || json.status.toString() == GenericResponse.KO) {
//            ret = objectMapper.readValue(jsonResponse, GenericResponse.class)
//        } else {
//            GenericError genericError = objectMapper.readValue(jsonResponse, GenericError.class)
//            throw new PdnException(genericError)
//        }
//        return ret
//    }

    static String objectToJson(Object classObject) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter()
        return ow.writeValueAsString(classObject)
    }

    String getUrlComunicazione(String idSmartPnd) {
        return smartPndConfig.getUrlSmartPnd() + "/ComunicazioneInfo.zul?idComunicazione=" + idSmartPnd
    }

    String replaceCaratteri(String stringa) {
        return stringa.replaceAll(" ", "_")
    }
}