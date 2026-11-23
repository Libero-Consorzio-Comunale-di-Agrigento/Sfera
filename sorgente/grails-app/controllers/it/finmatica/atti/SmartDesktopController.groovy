package it.finmatica.atti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.DocumentoFactory
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.dto.documenti.viste.DocumentoStepDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.atti.integrazioni.SmartDesktopService
import it.finmatica.atti.integrazioni.jworklist.JWorklistDispatcher
import it.finmatica.atti.odg.SedutaStampa
import org.apache.commons.codec.binary.Base64
import org.codehaus.groovy.grails.commons.GrailsApplication

class SmartDesktopController {

    AttiGestoreCompetenze   gestoreCompetenze
    AttiFirmaService        attiFirmaService
    JWorklistDispatcher     jworklistDispatcher
    AttiFileDownloader      attiFileDownloader
    SmartDesktopService     smartDesktopService
    SpringSecurityService   springSecurityService
    GrailsApplication       grailsApplication

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

            if (!(doc.tipoOggetto)) {
                //per seduta_stampa non viene letto tipoOggetto
                if (doc instanceof SedutaStampa) {
                    doc.tipoOggetto = SedutaStampa.TIPO_OGGETTO
                }
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
        def documento = DocumentoFactory.getDocumento(Long.parseLong(idDoc), tipo)
        def documentoPrincipale
        //prima di effettuare il download verifico se il file è riservato, e nel caso se l'utente collegato può visualizzarlo
        if (documento.riservato) {
            //Il calcolo sulla visione dei file riservati, viene fatto a partire dal documento principale (determina, delibera o proposta di delibera)
            if ((documento instanceof Allegato) || (documento instanceof VistoParere) || (documento instanceof Certificato)) {
                documentoPrincipale = documento.documentoPrincipale
            }
            else {
                documentoPrincipale = documento
            }
            if (!(gestoreCompetenze.utenteCorrenteVedeRiservato (documentoPrincipale))){
                throw new AttiRuntimeException("Attenzione: l'utente corrente non è abilitato alla visione del file riservato.")
            }
        }

        if (fileAllegato.firmato){
            redirect (url: "/commons/popupFileFirmatoSmartDesktop.zul?" + request.queryString)
        }
        else {
            attiFileDownloader.downloadFile(documento, fileAllegato, false, false, response)
        }
    }

    def downloadFile() {
        String idFile = params.file
        String idDoc = params.doc
        String tipo = params.tipo

        if (idFile == null || idDoc == null || tipo == null) {
            return;
        }

        try {
            boolean trasformaInPdf = params.boolean('trasformaInPdf')
            boolean sbusta = params.boolean('sbusta')

            IFileAllegato fileAllegato = FileAllegato.get(Long.valueOf(idFile))
            def documento = DocumentoFactory.getDocumento(Long.parseLong(idDoc), tipo)

            attiFileDownloader.downloadFile(documento, fileAllegato, trasformaInPdf, sbusta, response)

        } catch (Exception e) {
            log.error("Errore durante il download del file.", e)
            throw new AttiRuntimeException("Errore durante il download del file.")
        }
    }

    def presaVisione () {
        redirect (url: "/commons/presaVisione.zul?" + request.queryString)
    }

    def completaFirma () {
        redirect (url: "/commons/completaFirma.zul?" + request.queryString)
    }

    def apriUrlUsr() {
        String appo=request.queryString
        if (springSecurityService.currentUser != null) {
            //recupero Url da parametro
            //"http://localhost:8080/Atti/smartDesktop/apriUrlUsr/"
            String url = grailsApplication.config.urlDaAprireInPopup?:null
            //prendo dall'utente il nominativo
            String nominativo=springSecurityService.currentUser.nominativo
            String parametro= Base64.encodeBase64String(nominativo.bytes)
            redirect (url: url + "?USR="+parametro,target:"_blank")
            //Executions.getCurrent().sendRedirect(url + "?USR="+parametro, "_blank");
        }

    }

    def pulsanteIter () {
        redirect (url: "/commons/pulsanteIterSmartDesktop.zul?" + request.queryString)
    }

}
