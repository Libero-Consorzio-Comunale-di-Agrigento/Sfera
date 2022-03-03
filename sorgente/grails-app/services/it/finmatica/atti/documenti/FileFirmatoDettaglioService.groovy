package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.FileFirmatoDettaglio
import it.finmatica.grails.firmadigitale.FirmaDigitaleService

class FileFirmatoDettaglioService {

    IGestoreFile            gestoreFile
    FirmaDigitaleService    firmaDigitaleService
    SpringSecurityService   springSecurityService


    public void salvaInformazioniFileFirmato(IDocumentoEsterno documento, FileAllegato fileAllegato, def listaFirmatari){
        for (utenteFirmatario in listaFirmatari) {
            if (utenteFirmatario.firmatario) {
                FileFirmatoDettaglio fileFirmatoDettaglio = new FileFirmatoDettaglio()
                fileFirmatoDettaglio.stato = FileFirmatoDettaglio.VERIFICATO
                fileFirmatoDettaglio.dataFirma = utenteFirmatario.data
                fileFirmatoDettaglio.fileAllegato = fileAllegato;
                fileFirmatoDettaglio.nominativo = utenteFirmatario.firmatario.replaceAll("Documento firmato da: ", "").toUpperCase()
                fileFirmatoDettaglio.dataVerifica = new Date()
                fileFirmatoDettaglio.idDocumento = documento.id
                fileFirmatoDettaglio.save()
            }
        }
    }

    public void calcolaInformazioniFileFirmato(IDocumentoEsterno documento, FileAllegato fileAllegato){
        FileFirmatoDettaglio fileFirmatoDettaglio = new FileFirmatoDettaglio()
        fileFirmatoDettaglio.stato = FileFirmatoDettaglio.CALCOLATO
        fileFirmatoDettaglio.dataFirma = new Date()
        fileFirmatoDettaglio.fileAllegato = fileAllegato;
        fileFirmatoDettaglio.nominativo = springSecurityService.currentUser.nominativo
        fileFirmatoDettaglio.dataVerifica = new Date()
        fileFirmatoDettaglio.idDocumento = documento.id
        fileFirmatoDettaglio.save()
    }

    public estraiInformazioniFileFirmati(){
        List<FileFirmatoDettaglio> fileFirmati = FileFirmatoDettaglio.findAllByStato(FileFirmatoDettaglio.CALCOLATO)
        for (FileFirmatoDettaglio fileFirmatoDettaglio : fileFirmati){
            def documento = DocumentoFactory.getDocumento(fileFirmatoDettaglio.idDocumento)
            def listaFirmatari = isFileFirmato(gestoreFile.getFile(documento, fileFirmatoDettaglio.fileAllegato))
            salvaInformazioniFileFirmato(documento, fileFirmatoDettaglio.fileAllegato, listaFirmatari)
        }
        fileFirmati*.delete()
    }

    public void estraiInformazioneFileFirmato(def documento, FileAllegato fileAllegato) {
        def listaFirmatari = isFileFirmato(gestoreFile.getFile(documento, fileAllegato))
        if (listaFirmatari?.size() > 0) {
            fileAllegato.firmato = 'Y';
            fileAllegato.save()
        }
        salvaInformazioniFileFirmato(documento, fileAllegato, listaFirmatari)
    }


    private def isFileFirmato(InputStream input) {
        try {
            return firmaDigitaleService.verificaFirma(input)
        } catch (Exception e) {
            //log.info(e, e);
        }
        return [:]
    }

}
