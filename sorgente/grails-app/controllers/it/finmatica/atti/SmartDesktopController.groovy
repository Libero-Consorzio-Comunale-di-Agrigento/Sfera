package it.finmatica.atti

import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.DocumentoFactory
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.dto.documenti.viste.DocumentoStepDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.atti.integrazioni.SmartDesktopService
import it.finmatica.atti.integrazioni.jworklist.JWorklistDispatcher

class SmartDesktopController {

    AttiFirmaService    attiFirmaService
    JWorklistDispatcher jworklistDispatcher
    AttiFileDownloader  attiFileDownloader
    SmartDesktopService smartDesktopService

    def index () {}

    def firma () {
        String lista = params.LISTA_ID

        if (lista == null) {
            return
        }
        redirect(url: preparaFirma(lista))
    }

    def firmaAutografa () {
        String lista = params.LISTA_ID
        if (lista == null) {
            return
        }

        redirect(url: preparaFirma(lista))
    }

    def firmaRemota () {
        String lista = params.LISTA_ID
        if (lista == null) {
            return
        }

        redirect(url: preparaFirma(lista))
    }

    def firmaRemotaPdf () {
        String lista = params.LISTA_ID
        if (lista == null) {
            return
        }

        redirect(url: preparaFirma(lista))
    }

    private String preparaFirma(String lista){
        Collection<DocumentoStepDTO> documentiDaFirmare = []
        String[] listaRiferimenti = lista.split("#")


        for (String riferimento in listaRiferimenti) {
            def doc = jworklistDispatcher.getDocumento(riferimento)
            if (doc.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE){
                throw new AttiRuntimeException("Operazione non consentita, effettuare l'operazione di Completa Firma manualmente per il seguente documento:\n" + smartDesktopService.getDescrizione(doc))
            }
            DocumentoStepDTO documento = new DocumentoStepDTO(idDocumento: doc.idDocumento, tipoOggetto: doc.tipoOggetto)
            documentiDaFirmare.add(documento)
        }

        String urlFirma = attiFirmaService.multiFirma(documentiDaFirmare)

        if(urlFirma!=null && urlFirma.indexOf("/UploadDownload",0)!=-1)
            urlFirma = urlFirma.replace("/UploadDownload","/../UploadDownload")

        return urlFirma;
    }

    def allegato () {
        String idRif = params.rif
        if (idRif == null) {
            return;
        }

        def doc = jworklistDispatcher.getDocumento(idRif)
        if (doc == null){
            return;
        }
        download()
    }

    def download () {
        String idFile = params.file
        String idDoc = params.doc
        String tipo = params.tipo

        if (idFile == null || idDoc == null || tipo == null) {
            return;
        }

        FileAllegato fileAllegato = FileAllegato.get(Long.valueOf(idFile))
        if (fileAllegato.firmato){
            redirect (url: "/commons/popupFileFirmatoSmartDesktop.zul?" + request.queryString)
        }
        else {
            def documento = DocumentoFactory.getDocumento(Long.parseLong(idDoc), tipo)
            attiFileDownloader.downloadFile(documento, fileAllegato, false, false, response)
        }

    }

    def presaVisione () {
        redirect (url: "/commons/presaVisione.zul?" + request.queryString)
    }

    def completaFirma () {
        redirect (url: "/commons/completaFirma.zul?" + request.queryString)
    }
}
