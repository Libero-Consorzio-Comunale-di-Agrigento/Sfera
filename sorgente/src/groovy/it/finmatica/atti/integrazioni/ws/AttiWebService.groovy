package it.finmatica.atti.integrazioni.ws


import it.finmatica.atti.integrazioni.ws.dati.CampiRicerca
import it.finmatica.atti.integrazioni.ws.dati.Documento
import it.finmatica.atti.integrazioni.ws.dati.DocumentoEsteso
import it.finmatica.atti.integrazioni.ws.dati.RiferimentoFile
import it.finmatica.atti.integrazioni.ws.dati.RispostaGenerica
import it.finmatica.atti.integrazioni.ws.dati.RisultatoRicerca
import it.finmatica.atti.integrazioni.ws.dati.Soggetto
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.GetProposteSedutaResp
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.SedutaVotazione
import it.finmatica.atti.integrazioni.ws.dati.odgVoti.SetPropostaVotoResp

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
    Documento numeraAtto(
            @WebParam(name = "operatore") Soggetto operatore, @WebParam(name = "ente") String ente, @WebParam(name = "documento") Documento documento)

    @WebMethod(operationName = "creaPropostaEstesa")
    DocumentoEsteso creaPropostaEstesa(
            @WebParam(name = "operatore") Soggetto operatore, @WebParam(name = "ente") String ente, @WebParam(name = "documento") DocumentoEsteso proposta)

    @WebMethod(operationName = "creaProposta")
    Documento creaProposta(
            @WebParam(name = "operatore") Soggetto operatore, @WebParam(name = "ente") String ente, @WebParam(name = "documento") Documento proposta)

    @WebMethod(operationName = "ricercaDocumenti")
    RisultatoRicerca ricercaDocumenti(
            @WebParam(name = "operatore") Soggetto operatore,
            @WebParam(name = "ente") String ente,
            @WebParam(name = "campiRicerca") CampiRicerca campiRicerca, @WebParam(name = "pagina") Integer pagina, @WebParam(name = "max") Integer max)

    @WebMethod(operationName = "annullaAtto")
    Documento annullaAtto(
            @WebParam(name = "operatore") Soggetto operatore, @WebParam(name = "ente") String ente, @WebParam(name = "documento") Documento documento)

    @WebMethod(operationName = "getDocumentoPubblico")
    Documento getDocumentoPubblico(
            @WebParam(name = "operatore") Soggetto operatore,
            @WebParam(name = "ente") String ente,
            @WebParam(name = "idDocumento") long idDocumento
    )

    @WebMethod(operationName = "getDocumento")
    Documento getDocumento(
            @WebParam(name = "operatore") Soggetto operatore,
            @WebParam(name = "ente") String ente,
            @WebParam(name = "idDocumento") long idDocumento
    )

    @WebMethod(operationName = "getFile")
    RiferimentoFile getFile(
            @WebParam(name = "operatore") Soggetto operatore,
            @WebParam(name = "ente") String ente, @WebParam(name = "riferimentoFile") RiferimentoFile riferimentoFile)

    @WebMethod
    GetProposteSedutaResp getProposteSeduta(
            @WebParam(name = "operatore") Soggetto operatore,
            @WebParam(name = "ente") String ente,
            @WebParam(name = "idSeduta") Long idSeduta,
            @WebParam(name = "idRiferimento") Long idRiferimento,
            @WebParam(name = "commissione") String titoloCommissione,
            @WebParam(name = "anno") Integer anno,
            @WebParam(name = "numero") Integer numero
    )

    @WebMethod
    Long addAllegato(
            @WebParam(name = "operatore") Soggetto operatore,
            @WebParam(name = "ente") String ente,
            @WebParam(name = "titolo") String titolo,
            @WebParam(name = "tipoAllegato") String tipoAllegato,
            @WebParam(name = "statoFirma") String statoFirma,
            @WebParam(name = "stampaUnica") boolean stampaUnica,
            @WebParam(name = "pubblicaAlbo") boolean pubblicaAlbo,
            @WebParam(name = "pubblicaCasaDiVetro") boolean pubblicaCasaDiVetro,
            @WebParam(name = "pubblicaVisualizzatore") boolean pubblicaVisualizzatore,
            @WebParam(name = "riservato") boolean riservato,
            @WebParam(name = "idDocumento") long idDocumento,
            @WebParam(name = "riferimentiFile") RiferimentoFile file
    )


    @WebMethod
    SetPropostaVotoResp setPropostaVoto(
            @WebParam(name = "operatore") Soggetto operatore,
            @WebParam(name = "ente") String ente,
            @WebParam(name = "seduta") SedutaVotazione sedutaVotazione
    )

    @WebMethod
    String ricercaDocumentiEstesa(
            @WebParam(name = "operatore") Soggetto operatore,
            @WebParam(name = "ente") String ente,
            @WebParam(name = "campiRicerca") String campiRicerca,
            @WebParam(name = "pagina") Integer pagina,
            @WebParam(name = "max") Integer max
    )

    @WebMethod
    RispostaGenerica setDizionariContabili(
            @WebParam(name = "operatore") Soggetto operatore,
            @WebParam(name = "ente") String ente,
            @WebParam(name = "tipo") String tipo,   /* BILANCIO, CONTO, SOTTOBUDGET*/
            @WebParam(name = "operazione") String operazione,   /* REBUILD (CLEAN + INSERT), UPDATE */
            @WebParam(name = "dati") String dati  /* xml */
    )


}
