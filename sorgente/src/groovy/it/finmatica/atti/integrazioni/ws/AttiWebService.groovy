package it.finmatica.atti.integrazioni.ws

import it.finmatica.atti.integrazioni.ws.dati.*

import javax.jws.WebMethod
import javax.jws.WebParam
import javax.jws.WebService
import javax.jws.soap.SOAPBinding

/**
 * Created by esasdelli on 15/05/2017.
 */
@SOAPBinding
@WebService(name = "Atti", serviceName = "AttiService", portName = "AttiPort")
interface AttiWebService {

    @WebMethod(operationName = "numeraAtto")
    Documento numeraAtto (
            @WebParam(name = "operatore") Soggetto operatore, @WebParam(name = "ente") String ente, @WebParam(name = "documento") Documento documento)

    @WebMethod(operationName = "creaProposta")
    Documento creaProposta (
            @WebParam(name = "operatore") Soggetto operatore, @WebParam(name = "ente") String ente, @WebParam(name = "documento") Documento proposta)

    @WebMethod(operationName = "ricercaDocumenti")
    RisultatoRicerca ricercaDocumenti (
            @WebParam(name = "operatore") Soggetto operatore,
            @WebParam(name = "ente") String ente,
            @WebParam(name = "campiRicerca") CampiRicerca campiRicerca, @WebParam(name = "pagina") Integer pagina, @WebParam(name = "max") Integer max)

    @WebMethod(operationName = "annullaAtto")
    Documento annullaAtto (
            @WebParam(name = "operatore") Soggetto operatore, @WebParam(name = "ente") String ente, @WebParam(name = "documento") Documento documento)

    @WebMethod(operationName = "getDocumento")
    Documento getDocumento (
            @WebParam(name = "operatore") Soggetto operatore,
            @WebParam(name = "ente") String ente, @WebParam(name = "idDocumento") long idDocumento)

    @WebMethod(operationName = "getFile")
    RiferimentoFile getFile (
            @WebParam(name = "operatore") Soggetto operatore,
            @WebParam(name = "ente") String ente, @WebParam(name = "riferimentoFile") RiferimentoFile riferimentoFile)
}
