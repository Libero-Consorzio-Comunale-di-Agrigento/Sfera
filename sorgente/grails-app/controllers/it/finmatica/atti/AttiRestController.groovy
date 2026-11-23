package it.finmatica.atti

import it.finmatica.atti.integrazioni.ws.AttiWebServiceImpl
import it.finmatica.atti.integrazioni.ws.dati.*
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.GetComponentiCommissioneResp
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.GetProposteSedutaResp
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.GetProposteSedutaRespExt
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.GetSeduteResp
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.SedutaVotazione
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.SetPropostaVotoResp
import it.finmatica.login.DBLogin
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils
import org.json.JSONObject
import org.json.XML

import javax.activation.DataHandler
import javax.sql.DataSource
import java.sql.Connection

/**
 * Classe per la gestione dei webservice Rest degli Atti
 */
class AttiRestController {
    AttiWebServiceImpl attiWebService
    DataSource ad4DataSource


    /**
     * restituisce l'informazione che l'utente collegato può visualizzare o meno l'allegato
     * @param richiesta (  Soggetto operatore, String ente, String tipo, String operazione, String dati) {
     * @param risposta true/false
     */
    def verificaLetturaAllegato(RequestVerificaLetturaAllegati richiesta) {
        ResponseVerificaLetturaAllegati ret = new ResponseVerificaLetturaAllegati()
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")

            ret.esito=attiWebService.verificaLetturaAllegato(richiesta.operatore, richiesta.ente, richiesta.idDocumento, richiesta.idOggettoFile)
            respond ret, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }
    }

    /**
     * metodo nuovo per l'intregrazione con i Dizionari Contabili
     * aggiorna o crea le informazioni nella tabella dizionari_contabili
     * @param richiesta (  Soggetto operatore, String ente, String tipo, String operazione, String dati) {
     * @param risposta RispostaGenerica
     */
    def setDizionariContabili(RequestSetDizionariContabili richiesta) {
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")

            String xmlString=""
            if(richiesta.dati) {
                JSONObject jsonCampi = new JSONObject(richiesta.dati)
                xmlString = XML.toString(jsonCampi)
            }

            RispostaGenerica ret = attiWebService.setDizionariContabili(richiesta.operatore, richiesta.ente, richiesta.tipo, richiesta.operazione, xmlString)
            respond ret, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }
    }

    /**
     * metodo nuovo che utilizza più campi per cercare gli atti.
     * nello specifico in campiRicerca va specificato una stringa json contenente i dati da fornire
     * @param richiesta ( Soggetto operatore, String ente,  String campiRicerca,  Integer pagina,  Integer max )
     * @return
     */
    def ricercaDocumentiEstesa(RequestRicercaDocumentiEstesa richiesta) {
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")

            String xmlString=""
            if(richiesta.campiRicerca) {
                JSONObject jsonCampi = new JSONObject(richiesta.campiRicerca)
                xmlString = XML.toString(jsonCampi)
            }

            String dati = attiWebService.ricercaDocumentiEstesa(richiesta.operatore, richiesta.ente, xmlString,richiesta.pagina,richiesta.max)
            RisultatoRicercaEstesaRest ret= new RisultatoRicercaEstesaRest()
            JSONObject json=XML.toJSONObject(dati)
            ret.dati=json.toString()
            respond ret, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }

    }

    /**
     * metodo che ritorna i metadati di un Atto
     *
     * @param richiesta (Soggetto operatore,String ente,long idDocumento)
     * @return
     */
    def getDocumento(RequestGetDocumento richiesta) {
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")
            Documento ret = attiWebService.getDocumento(richiesta.operatore, richiesta.ente, richiesta.idDocumento)
            DocumentoRest pulito = pulisciDoc(ret)
            respond pulito, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }

    }

    /**
     * metodo che ritorna i metadati di un Atto Pubblico
     *
     * @param richiesta (Soggetto operatore,String ente,long idDocumento)
     * @return
     */
    def getDocumentoPubblico(RequestGetDocumento richiesta) {
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")
            Documento ret = attiWebService.getDocumentoPubblico(richiesta.operatore, richiesta.ente, richiesta.idDocumento)
            DocumentoRest pulito = pulisciDoc(ret)
            respond pulito, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }

    }

    /**
     * metodo che aggiunge un allegato ad un atto esistente
     *
     * @param richiesta
     * @return
     */
    def addAllegato(RequestAddAllegato richiesta) {
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")

            //check dati, Operatore, ente, riferimentoFile.nome e riferimentoFile.file sono obbligatori

            RiferimentoFile riferimentoFile = new RiferimentoFile()
            riferimentoFile.nome = richiesta.riferimentiFile?.nome
            riferimentoFile.file =Base64ToDataHandler(richiesta.riferimentiFile?.file)

            Long idAllegato = attiWebService.addAllegato(richiesta.operatore, richiesta.ente, richiesta.titolo, richiesta.tipoAllegato, richiesta.statoFirma, richiesta.stampaUnica,richiesta.pubblicaAlbo,richiesta.pubblicaCasaDiVetro,richiesta.pubblicaVisualizzatore,richiesta.riservato, richiesta.idDocumento,riferimentoFile)
            ResponseAddAllegato ret = new ResponseAddAllegato()
            ret.idAllegato=idAllegato
            respond ret, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }

    }

    /**
     * metodo che ricerca i documenti
     *
     * @param richiesta
     * @return
     */
    def ricercaDocumenti(RequestRicercaDocumenti richiesta) {
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")
            RisultatoRicerca ret = attiWebService.ricercaDocumenti(richiesta.operatore, richiesta.ente, richiesta.campiRicerca, richiesta.pagina, richiesta.max)

            RisultatoRicercaRest pulito = new RisultatoRicercaRest()
            pulito.totaleDocumenti=ret.totaleDocumenti
            pulito.documenti = new ArrayList<DocumentoRest>()
            for(Documento tempDoc :ret.documenti) {
                pulito.documenti.add(pulisciDoc(tempDoc))
            }
            respond pulito, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }
    }

    def numeraAtto(RequestNumeraAtto richiesta) {
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")
            Documento documentoSoap=toSoapDocument(richiesta.documento)
            Documento ret = attiWebService.numeraAtto(richiesta.operatore, richiesta.ente, documentoSoap)
            DocumentoRest pulito = pulisciDoc(ret)
            respond pulito, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }
    }

    def creaProposta(RequestCreaProposta richiesta) {
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")
            Documento documentoSoap=toSoapDocument(richiesta.documento)
            Documento ret = attiWebService.creaProposta(richiesta.operatore, richiesta.ente, documentoSoap)
            DocumentoRest pulito = pulisciDoc(ret)
            respond pulito, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }
    }

    def creaPropostaEstesa(RequestCreaPropostaEstesa richiesta) {
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")
            Documento documentoSoap=toSoapDocument(richiesta.documento)
            DocumentoEsteso documentoEsteso = attiWebService.toEstesoFormDocumento(documentoSoap)

            //aggiungo i campi gestiti dal documento esteso
            documentoEsteso.cig = richiesta.documento.cig?:""
            documentoEsteso.cup = richiesta.documento.cup?:""
            if (richiesta.documento.daInviareCorteConti) {
                documentoEsteso.daInviareCorteConti = richiesta.documento.daInviareCorteConti
            }
            if (richiesta.documento.dataInvioCorteConti) {
                documentoEsteso.dataInvioCorteConti = richiesta.documento.dataInvioCorteConti
            }
            documentoEsteso.note = richiesta.documento.note?:""
            documentoEsteso.eseguibilitaImmediata = richiesta.documento.eseguibilitaImmediata?:false
            documentoEsteso.motivazioniEseguibilita = richiesta.documento.motivazioniEseguibilita?:""
            if (richiesta.documento.dataScadenza) {
                documentoEsteso.dataScadenza = richiesta.documento.dataScadenza
            }
            if (richiesta.documento.priorita) {
                documentoEsteso.priorita = richiesta.documento.priorita
            }
            documentoEsteso.motivazione = richiesta.documento.motivazione?:""
            documentoEsteso.codiceCategoria = richiesta.documento.codiceCategoria?:""

            DocumentoEsteso ret = attiWebService.creaPropostaEstesa(richiesta.operatore, richiesta.ente, documentoEsteso)
            DocumentoRest pulito = pulisciDoc(ret)
            respond pulito, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }
    }

//    def creaPropostaEstesa(RequestCreaPropostaEstesa richiesta) {
//        try {
//            String AUTH = request.getHeader("Authorization")
//            if (!checkAuth(AUTH))
//                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")
//            DocumentoEsteso documentoSoap=toSoapDocument(richiesta.documento)
//            Documento documento = new Documento()
//            documento.tipo = documentoSoap.tipo
//            documento.id = documentoSoap.id
//            documento.idRiferimento = documentoSoap.idRiferimento
//            documento.oggetto = documentoSoap.oggetto
//            documento.stato = documentoSoap.stato
//            documento.codiceTipologia = documentoSoap.codiceTipologia
//            documento.numero = documentoSoap.numero
//            documento.anno = documentoSoap.anno
//            documento.data = documentoSoap.data
//            documento.codiceRegistro = documentoSoap.codiceRegistro
//            documento.numero2 = documentoSoap.numero2
//            documento.anno2 = documentoSoap.anno2
//            documento.dataNumero2 = documentoSoap.dataNumero2
//            documento.codiceRegistro2 = documentoSoap.codiceRegistro2
//            documento.numeroProtocollo = documentoSoap.numeroProtocollo
//            documento.annoProtocollo = documentoSoap.annoProtocollo
//            documento.dataProtocollo = documentoSoap.dataProtocollo
//            documento.fascicolo = documentoSoap.fascicolo
//            documento.classificazione = documentoSoap.classificazione
//            documento.numeroProposta = documentoSoap.numeroProposta
//            documento.annoProposta = documentoSoap.annoProposta
//            documento.dataProposta = documentoSoap.dataProposta
//            documento.dataEsecutivita = documentoSoap.dataEsecutivita
//            documento.dataInizioPubblicazione = documentoSoap.dataInizioPubblicazione
//            documento.dataFinePubblicazione = documentoSoap.dataFinePubblicazione
//            documento.numeroAlbo = documentoSoap.numeroAlbo
//            documento.annoAlbo = documentoSoap.annoAlbo
//            documento.redattore = documentoSoap.redattore
//            documento.funzionario = documentoSoap.funzionario
//            documento.dirigente = documentoSoap.dirigente
//            documento.unitaProponente = documentoSoap.unitaProponente
//            documento.testo = documentoSoap.testo
//            documento.allegati = documentoSoap.allegati
//            documento.vistiPareri = documentoSoap.vistiPareri
//            documento.certificati = documentoSoap.certificati
//            documento.soggetti = documentoSoap.soggetti
//            Documento ret = attiWebService.creaProposta(richiesta.operatore, richiesta.ente, documento)
//            DocumentoRest pulito = pulisciDoc(ret)
//            respond pulito, formats: ['json']
//        } catch (Exception ex) {
//            respond new Fault("error", ex.message), formats: ['json']
//        }
//    }

    def annullaAtto(RequestAnnullaAtto richiesta) {
        try {
            Documento documentoSoap=toSoapDocument(richiesta.documento)
            Documento ret = attiWebService.annullaAtto(richiesta.operatore, richiesta.ente, documentoSoap)
            DocumentoRest pulito = pulisciDoc(ret)
            respond pulito, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }
    }

    def getFile(RequestGetFile richiesta) {
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")
            RiferimentoFile temp = new RiferimentoFile()
            temp.tipoDocumento = richiesta.riferimentoFile.tipoDocumento
            temp.idFile = richiesta.riferimentoFile.idFile
            temp.idDocumento = richiesta.riferimentoFile.idDocumento
            temp.nome = richiesta.riferimentoFile.nome

            RiferimentoFile ret = attiWebService.getFile(richiesta.operatore, richiesta.ente, temp)
            RiferimentoFileRest ret2 = new RiferimentoFileRest()
            ret2.idDocumento = ret.idDocumento
            ret2.tipoDocumento = ret.tipoDocumento
            ret2.nome = ret.nome
            ret2.idFile = ret.idFile
            ret2.file = HandlerToBase64(ret.file)
            respond ret2, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }
    }

    def getProposteSeduta(RequestGetProposteSeduta richiesta) {
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")
            GetProposteSedutaResp ret = attiWebService.getProposteSeduta(richiesta.operatore, richiesta.ente, richiesta.idSeduta, richiesta.idRiferimento, richiesta.commissione, richiesta.anno, richiesta.numero)
            //per Parma nella risposta REST aggiungo data e ora convocazione
            GetProposteSedutaRespExt retExt = attiWebService.getProposteSedutaEstesa(ret)
            GetProposteSedutaResp risposta = new GetProposteSedutaResp()
            //Questo dovrebbe dare errore perchè sedutaExt ha degli attributi in più rispetto a seduta (da verificare con springboot)
            risposta.seduta = retExt.sedutaExt
            risposta.esito = retExt.esito
            respond risposta, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }
    }

    def setPropostaVoto(RequestSetPropostaVoto richiesta) {
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")
            SetPropostaVotoResp ret = attiWebService.setPropostaVoto(richiesta.operatore, richiesta.ente, richiesta.seduta)
            respond ret, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }
    }

    def getSedute(RequestGetSedute richiesta){
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")

            GetSeduteResp ret = attiWebService.getSedute(richiesta.operatore,richiesta.ente,richiesta.commissione, richiesta.dataDal,richiesta.dataAl,richiesta.pubblicate)
            respond ret, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }
    }

    def getComponentiCommissione(RequestGetComponentiCommissione richiesta){
        try {
            String AUTH = request.getHeader("Authorization")
            if (!checkAuth(AUTH))
                throw new Exception("Attenzione: non è possibile autenticare l'operazione.")

            GetComponentiCommissioneResp ret = attiWebService.getComponentiCommissione(richiesta.operatore,richiesta.ente,richiesta.commissione)
            respond ret, formats: ['json']
        } catch (Exception ex) {
            respond new Fault("error", ex.message), formats: ['json']
        }
    }


    //Utility
    boolean checkAuth(String authorization) throws Exception {
        boolean ret
        Connection conn = null
        if(authorization!=null && authorization.length()>0) {
            String USRPWD64 = authorization.replace("BASIC ", "").replace("Basic ", "")
            String USRPWDCLEAR = new String(Base64.decodeBase64(USRPWD64))
            if (USRPWDCLEAR.contains(":")) {
                String[] USRPWD = USRPWDCLEAR.split(":")
                String user = USRPWD[0]
                String pwd = USRPWD[1]
                try {
                    conn = ad4DataSource.connection
                    DBLogin dbl = new DBLogin(conn)
                    //Controllo USER/PWD
                    ret = (dbl.authenticate(user, pwd) >= 0)
                    if (ret) {
                        //Controllo MODULO/ISTANZA
                        ret = (dbl.authorize(user, grailsApplication.config.grails.plugins.amministrazionedatabase.modulo, grailsApplication.config.grails.plugins.amministrazionedatabase.istanza, 0, "") >= 0)
                    }
                } catch (Exception ex) {
                    throw new Exception(ex)
                }
                finally {
                    if (conn != null && !conn.isClosed())
                        conn.close()
                }

            }
        }
        return ret;
    }

    DataHandler Base64ToDataHandler(String codeFile){
        DataHandler ret = null
        try {
            javax.activation.DataSource ds = new ByteArrayDatasource(Base64.decodeBase64(codeFile));
            ret = new DataHandler(ds);
        }
        catch(Exception ex){
            println ex.message
        }
        return ret
    }

    String HandlerToBase64(DataHandler handler) throws IOException {
        if(handler==null) return ""
        ByteArrayOutputStream output = new ByteArrayOutputStream()
        handler.writeTo(output)
        return Base64.encodeBase64String(output.toByteArray())
    }

    Documento toSoapDocument(DocumentoRest input){
        Documento ret=new Documento()
        ret.tipo				   =input.tipo
        ret.id                      =input.id
        ret.idRiferimento          =input.idRiferimento
        ret.oggetto                =input.oggetto
        ret.stato                  =input.stato
        ret.codiceTipologia        =input.codiceTipologia
        ret.numero                 =input.numero
        ret.anno                   =input.anno
        ret.data                   =input.data
        ret.codiceRegistro         =input.codiceRegistro
        ret.numero2                =input.numero2
        ret.anno2                  =input.anno2
        ret.dataNumero2            =input.dataNumero2
        ret.codiceRegistro2        =input.codiceRegistro2
        ret.numeroProtocollo       =input.numeroProtocollo
        ret.annoProtocollo         =input.annoProtocollo
        ret.dataProtocollo         =input.dataProtocollo
        ret.fascicolo              =input.fascicolo
        ret.classificazione        =input.classificazione
        ret.numeroProposta         =input.numeroProposta
        ret.annoProposta           =input.annoProposta
        ret.dataProposta           =input.dataProposta
        ret.dataEsecutivita        =input.dataEsecutivita
        ret.dataInizioPubblicazione=input.dataInizioPubblicazione
        ret.dataFinePubblicazione  =input.dataFinePubblicazione
        ret.numeroAlbo             =input.numeroAlbo
        ret.annoAlbo               =input.annoAlbo
        ret.redattore              =toSoggettoSoapObject(input.redattore)
        ret.funzionario            =toSoggettoSoapObject(input.funzionario)
        ret.dirigente              =toSoggettoSoapObject(input.dirigente)
        ret.unitaProponente        =input.unitaProponente
        ret.testo                  =toRiferimentoSoapObject(input.testo)
        ret.allegati               =toSoapObjectList(input.allegati)
        ret.vistiPareri            =input.vistiPareri
        ret.certificati            =input.certificati
        ret.soggetti = input.soggetti
        return ret
    }


    DocumentoRest pulisciDoc(Documento input){
        DocumentoRest ret=new DocumentoRest()
        ret.tipo				   =input.tipo
        ret.id                      =input.id
        ret.idRiferimento          =input.idRiferimento
        ret.oggetto                =input.oggetto
        ret.stato                  =input.stato
        ret.codiceTipologia        =input.codiceTipologia
        ret.numero                 =input.numero
        ret.anno                   =input.anno
        ret.data                   =input.data
        ret.codiceRegistro         =input.codiceRegistro
        ret.numero2                =input.numero2
        ret.anno2                  =input.anno2
        ret.dataNumero2            =input.dataNumero2
        ret.codiceRegistro2        =input.codiceRegistro2
        ret.numeroProtocollo       =input.numeroProtocollo
        ret.annoProtocollo         =input.annoProtocollo
        ret.dataProtocollo         =input.dataProtocollo
        ret.fascicolo              =input.fascicolo
        ret.classificazione        =input.classificazione
        ret.numeroProposta         =input.numeroProposta
        ret.annoProposta           =input.annoProposta
        ret.dataProposta           =input.dataProposta
        ret.dataEsecutivita        =input.dataEsecutivita
        ret.dataInizioPubblicazione=input.dataInizioPubblicazione
        ret.dataFinePubblicazione  =input.dataFinePubblicazione
        ret.numeroAlbo             =input.numeroAlbo
        ret.annoAlbo               =input.annoAlbo
        ret.redattore              =pulisciSoggetto(input.redattore)
        ret.funzionario            =pulisciSoggetto(input.funzionario)
        ret.dirigente              =pulisciSoggetto(input.dirigente)
        ret.unitaProponente        =input.unitaProponente
        ret.testo                  =toRiferimentoRestObject(input.testo)
        ret.allegati               =toRestObjectList(input.allegati)
        ret.vistiPareri            =pulisciVistiParere(input.vistiPareri)
        ret.certificati            =pulisciCertificati(input.certificati)
        ret.soggetti = new ArrayList<SoggettoRest>()
        for(Soggetto tempSogg: input.soggetti){
            ret.soggetti.add(pulisciSoggetto(tempSogg))
        }

        return ret
    }

    DocumentoEstesoRest pulisciDocEsteso(DocumentoEsteso input){
        DocumentoEstesoRest ret = pulisciDoc(input)
//        ret.tipo				   =input.tipo
//        ret.id                     =input.id
//        ret.idRiferimento          =input.idRiferimento
//        ret.oggetto                =input.oggetto
//        ret.stato                  =input.stato
//        ret.codiceTipologia        =input.codiceTipologia
//        ret.numero                 =input.numero
//        ret.anno                   =input.anno
//        ret.data                   =input.data
//        ret.codiceRegistro         =input.codiceRegistro
//        ret.numero2                =input.numero2
//        ret.anno2                  =input.anno2
//        ret.dataNumero2            =input.dataNumero2
//        ret.codiceRegistro2        =input.codiceRegistro2
//        ret.numeroProtocollo       =input.numeroProtocollo
//        ret.annoProtocollo         =input.annoProtocollo
//        ret.dataProtocollo         =input.dataProtocollo
//        ret.fascicolo              =input.fascicolo
//        ret.classificazione        =input.classificazione
//        ret.numeroProposta         =input.numeroProposta
//        ret.annoProposta           =input.annoProposta
//        ret.dataProposta           =input.dataProposta
//        ret.dataEsecutivita        =input.dataEsecutivita
//        ret.dataInizioPubblicazione=input.dataInizioPubblicazione
//        ret.dataFinePubblicazione  =input.dataFinePubblicazione
//        ret.numeroAlbo             =input.numeroAlbo
//        ret.annoAlbo               =input.annoAlbo
//        ret.redattore              =pulisciSoggetto(input.redattore)
//        ret.funzionario            =pulisciSoggetto(input.funzionario)
//        ret.dirigente              =pulisciSoggetto(input.dirigente)
//        ret.unitaProponente        =input.unitaProponente
//        ret.testo                  =toRiferimentoRestObject(input.testo)
//        ret.allegati               =toRestObjectList(input.allegati)
//        ret.vistiPareri            =pulisciVistiParere(input.vistiPareri)
//        ret.certificati            =pulisciCertificati(input.certificati)
//        ret.soggetti = new ArrayList<SoggettoRest>()
//        for(Soggetto tempSogg: input.soggetti){
//            ret.soggetti.add(pulisciSoggetto(tempSogg))
//        }

        //aggiungo i campi in più
        ret.tipologiaPubblicazione = input.tipologiaPubblicazione
        ret.titoloTipologia= input.titoloTipologia
        ret.nominativoDirigente= input.nominativoDirigente
        ret.uoProponenteDescrizione= input.uoProponenteDescrizione
        ret.cig= input.cig
        ret.cup= input.cup
        ret.daInviareCorteConti= input.daInviareCorteConti
        ret.dataInvioCorteConti= input.dataInvioCorteConti
        ret.note= input.note
        ret.eseguibilitaImmediata= input.eseguibilitaImmediata
        ret.motivazioniEseguibilita= input.motivazioniEseguibilita
        ret.dataScadenza= input.dataScadenza
        ret.priorita= input.priorita
        ret.motivazione= input.motivazione
        ret.codiceCategoria= input.codiceCategoria

        return ret
    }

    List<AllegatoRest> toRestObjectList(List<Allegato> allegati) {
        List<AllegatoRest> ret = new ArrayList<AllegatoRest>()
        for (Allegato allegato : allegati) {
            ret.add(toAllegatoRestObject(allegato))
        }
        return  ret
    }

    List<Allegato> toSoapObjectList(List<AllegatoRest> allegati) {
        List<Allegato> ret = new ArrayList<Allegato>()
        for (AllegatoRest allegato : allegati) {
            ret.add(toAllegatoSoapObject(allegato))
        }
        return ret
    }

    AllegatoRest toAllegatoRestObject(Allegato allegato){
        if(allegato==null) return null
        AllegatoRest ret= new AllegatoRest()
        ret.id=allegato.id
        ret.idRiferimento=allegato.idRiferimento
        ret.titolo=allegato.titolo
        ret.tipo=allegato.tipo
        ret.titoloTipologia=allegato.titoloTipologia
        ret.riferimentiFile = new ArrayList<RiferimentoFileRest>()
        for(RiferimentoFile riferimentoFile: allegato.riferimentiFile){
            ret.riferimentiFile.add(toRiferimentoRestObject(riferimentoFile))
        }
        return ret
    }

    Allegato toAllegatoSoapObject(AllegatoRest allegato){
        if(allegato==null) return null
        Allegato ret= new Allegato()
        ret.id=allegato.id
        ret.idRiferimento=allegato.idRiferimento
        ret.titolo=allegato.titolo
        ret.tipo=allegato.tipo
        ret.titoloTipologia=allegato.titoloTipologia
        ret.riferimentiFile = new ArrayList<RiferimentoFile>()
        for(RiferimentoFileRest riferimentoFile: allegato.riferimentiFile){
            ret.riferimentiFile.add(toRiferimentoSoapObject(riferimentoFile))
        }
        return ret
    }

    RiferimentoFileRest toRiferimentoRestObject(RiferimentoFile riferimentoFile){
        if(riferimentoFile==null) return null
        RiferimentoFileRest ret = new RiferimentoFileRest()
        ret.idDocumento=riferimentoFile.idDocumento
        ret.nome= riferimentoFile.nome
        ret.idFile=riferimentoFile.idFile
        ret.tipoDocumento=riferimentoFile.tipoDocumento
        ret.file = (riferimentoFile.file!=null?HandlerToBase64(riferimentoFile.file):null)
        return ret
    }

    RiferimentoFile toRiferimentoSoapObject(RiferimentoFileRest riferimentoFile){
        if(riferimentoFile==null) return null
        RiferimentoFile ret = new RiferimentoFile()
        ret.idDocumento=riferimentoFile.idDocumento
        ret.nome= riferimentoFile.nome
        ret.idFile=riferimentoFile.idFile
        ret.tipoDocumento=riferimentoFile.tipoDocumento
        ret.file = (riferimentoFile.file!=null?Base64ToDataHandler(riferimentoFile.file):null)
        return ret
    }


    SoggettoRest pulisciSoggetto(Soggetto input){
        SoggettoRest ret = new SoggettoRest()
        ret.nome       =    input.nome
        ret.cognome     =   input.cognome
        ret.tipo         =  input.tipo
        ret.unita        =  input.unita
        return ret
    }

    List<VistoParere> pulisciVistiParere(List<VistoParere> input){
        List<VistoParereRest> ret = new ArrayList<VistoParereRest>()
        for (VistoParere vistoParere:input) {
            VistoParereRest temp= new VistoParereRest()
            temp.id = vistoParere.id
            temp.idRiferimento = vistoParere.idRiferimento
            temp.firmatario = pulisciSoggetto(vistoParere.firmatario)
            temp.unita = pulisciSoggetto(vistoParere.unita)
            temp.testo = toRiferimentoRestObject(vistoParere.testo)
            temp.allegati = toRestObjectList(vistoParere.allegati)
            ret.add(temp)
        }
        return  ret
    }

    List<CertificatoRest> pulisciCertificati(List<Certificato> input) {
        List<CertificatoRest> ret = new ArrayList<CertificatoRest>()
        for (Certificato certificato:input) {
            CertificatoRest temp= new CertificatoRest()
            temp.id = certificato.id
            temp.idRiferimento = certificato.idRiferimento
            temp.tipo=certificato.tipo
            temp.firmatario = pulisciSoggetto(certificato.firmatario)
            temp.testo = toRiferimentoRestObject(certificato.testo)
            ret.add(temp)
        }
        return  ret
    }

    Soggetto toSoggettoSoapObject(SoggettoRest input){
        Soggetto ret = new Soggetto()
        ret.nome       =    input.nome
        ret.cognome     =   input.cognome
        ret.tipo         =  input.tipo
        ret.unita        =  input.unita
        ret.codiceFiscale=input.codicefiscale
        ret.niAs4 = input.niAs4
        ret.utenteAd4=input.utenteAd4
        ret.getUtente()
        return ret
    }


}


// Definizione CLassi usate

class DocumentoRest {
    String tipo // DETERMINA o DELIBERA
    Long id
    String idRiferimento  // id del documento dell'applicativo esterno
    String oggetto
    String stato
    String codiceTipologia
    Integer numero
    Integer anno
    Date data
    String codiceRegistro
    Integer numero2
    Integer anno2
    Date dataNumero2
    String codiceRegistro2
    Integer numeroProtocollo
    Integer annoProtocollo
    Date dataProtocollo
    Fascicolo fascicolo
    Classificazione classificazione
    Integer numeroProposta
    Integer annoProposta
    Date dataProposta
    Date dataEsecutivita
    Date dataInizioPubblicazione
    Date dataFinePubblicazione
    Integer numeroAlbo
    Integer annoAlbo
    SoggettoRest redattore
    SoggettoRest funzionario
    SoggettoRest dirigente
    UnitaOrganizzativa unitaProponente
    RiferimentoFileRest testo
    List<AllegatoRest> allegati
    List<VistoParereRest> vistiPareri
    List<Certificato> certificati
    List<SoggettoRest> soggetti
}

class DocumentoEstesoRest extends DocumentoRest {
    String tipologiaPubblicazione
    String titoloTipologia
    String nominativoDirigente
    String uoProponenteDescrizione
    String cig
    String cup
    boolean daInviareCorteConti
    Date dataInvioCorteConti
    String note
    boolean eseguibilitaImmediata
    String motivazioniEseguibilita
    Date 	dataScadenza
    Integer priorita
    String  motivazione
    String  codiceCategoria
}

class SoggettoRest {
    String nome
    String cognome
    String tipo
    UnitaOrganizzativa unita
    String codicefiscale
    String niAs4
    String utenteAd4
}

class VistoParereRest {
    Integer id
    String  idRiferimento  // id del documento dell'applicativo esterno
    SoggettoRest        firmatario
    SoggettoRest        unita
    RiferimentoFileRest testo // il testo principale
    List<AllegatoRest>  allegati // gli allegati del visto
}

class CertificatoRest {
    Integer id
    String  idRiferimento  // id del documento dell'applicativo esterno
    String tipo // il tipo di certificato (PUBBLICAZIONE, ESECUTIVITA, etc..)
    SoggettoRest        firmatario
    RiferimentoFileRest testo
}

class RisultatoRicercaRest {
    int             totaleDocumenti
    List<DocumentoRest> documenti
}

class RisultatoRicercaEstesaRest {
    String dati
}

class RiferimentoFileRest {
    String tipoDocumento
    Long idDocumento
    Long idFile
    String nome
    String file
}

class AllegatoRest {

    Integer id
    String  idRiferimento  // id del documento dell'applicativo esterno
    String titolo // il titolo dell'allegato
    String tipo // il codice che identifica il tipo di allegato TipoAllegato.codiceEsterno
    String titoloTipologia // il titolo della tipologia dell'allegato
    List<RiferimentoFileRest> riferimentiFile
}

// classi di input
class RequestAddAllegato {
    Soggetto operatore
    String ente
    String titolo
    String tipoAllegato
    String statoFirma
    boolean stampaUnica
    boolean pubblicaAlbo
    boolean pubblicaCasaDiVetro
    boolean pubblicaVisualizzatore
    boolean riservato
    long idDocumento
    RiferimentoFileRest riferimentiFile
}

class RequestRicercaDocumentiEstesa {
    Soggetto operatore
    String ente
    String campiRicerca
    Integer pagina
    Integer max
}

class RequestSetDizionariContabili {
    Soggetto operatore
    String ente
    String tipo
    String operazione
    String dati
}

class RequestVerificaLetturaAllegati {
    Soggetto operatore
    String ente
    String idDocumento
    Long idOggettoFile
}

class ResponseVerificaLetturaAllegati {
    boolean esito
}

class RequestGetDocumento {
    Soggetto operatore
    String ente
    long idDocumento
}

class RequestNumeraAtto {
    Soggetto operatore
    String ente
    DocumentoRest documento
}

class RequestRicercaDocumenti {
    Soggetto operatore
    String ente
    CampiRicerca campiRicerca
    Integer pagina
    Integer max
}

class RequestCreaProposta {
    Soggetto operatore
    String ente
    DocumentoRest documento
}

class RequestCreaPropostaEstesa {
    Soggetto operatore
    String ente
    DocumentoEstesoRest documento
}


class RequestAnnullaAtto {
    Soggetto operatore
    String ente
    DocumentoRest documento
}

class RequestGetFile {
    Soggetto operatore
    String ente
    RiferimentoFileRest riferimentoFile
}

class RequestGetProposteSeduta {
    Soggetto operatore
    String ente
    Long idSeduta
    Long idRiferimento
    String commissione
    Integer anno
    Integer numero
}

class RequestSetPropostaVoto {
    Soggetto operatore
    String ente
    SedutaVotazione seduta
}

class RequestGetSedute {
    Soggetto operatore
    String ente
    String commissione
    String dataDal
    String dataAl
    String pubblicate  // S/N null
}

class RequestGetComponentiCommissione {
    Soggetto operatore
    String ente
    String commissione
}


class ResponseAddAllegato{
    Long idAllegato
}

class Fault {
    String faultcode
    String faultstring

    Fault(String faultcode, String faultstring) {
        this.faultcode = faultcode
        this.faultstring = faultstring
    }
}



//Classe per gestire i DataHandler
class ByteArrayDatasource implements javax.activation.DataSource {

    private final byte[] content;

    public ByteArrayDatasource(byte[] content) {
        this.content = content;
    }

    public ByteArrayDatasource(InputStream stream) throws IOException {
        int length = stream.available();
        content = new byte[length];
        IOUtils.readFully(stream,content);
        IOUtils.closeQuietly(stream);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Non supportato");
    }

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }

    @Override
    public String getName() {
        return "ByteArrayDatasource";
    }
}

