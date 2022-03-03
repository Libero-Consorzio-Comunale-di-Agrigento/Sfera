package it.finmatica.atti.documenti.beans

import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.FileFirmatoDettaglioService
import it.finmatica.atti.documenti.StatoMarcatura
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.gestionetesti.TipoFile
import it.finmatica.grails.firmadigitale.FirmaDigitaleFile
import it.finmatica.grails.firmadigitale.StorageFileService
import org.springframework.transaction.annotation.Transactional

@CompileStatic
class GdmFirmaDigitaleFileStorage implements StorageFileService {

    private final AttiGestioneTesti gestioneTesti
    private final IGestoreFile gestoreFile
    private final SpringSecurityService springSecurityService
    private final FileFirmatoDettaglioService fileFirmatoDettaglioService

    GdmFirmaDigitaleFileStorage (AttiGestioneTesti attiGestioneTesti, IGestoreFile gestoreFile, FileFirmatoDettaglioService fileFirmatoDettaglioService, SpringSecurityService springSecurityService) {
        this.gestioneTesti = attiGestioneTesti
        this.gestoreFile = gestoreFile
        this.springSecurityService = springSecurityService
        this.fileFirmatoDettaglioService = fileFirmatoDettaglioService
    }

    @Override
    @Transactional
    void salvaFileFirmato (FirmaDigitaleFile file, File fileFirmato) {
        IDocumentoEsterno documentoEsterno = AttiFirmaService.getDocumentoIdRiferimento(file.idRiferimentoFile)
        FileAllegato fileAllegato = AttiFirmaService.getFileAllegatoIdRiferimento(file.idRiferimentoFile)

        // Qui ho due casistiche:
        // - con applet: l'utente risulta già loggato, quindi riuso quello che ho già in sessione
        // - con jnlp: l'utente non risulta loggato quindi "faccio una cosa brutta": rifaccio il login con l'utente che ha caricato precedentemente il file.
        // FIXME: siccome questa funzione viene caricata dal plugin di firma che non fa l'autenticazione, eseguo l'autenticazione usando
        // l'utente del documento. Non è il top. bisognerebbe gestire l'autenticazione nel plugin di firma.
        if (springSecurityService.currentUser == null) {
            AttiUtils.eseguiAutenticazione(fileAllegato.utenteUpd.nominativo, getCodiceEnte(documentoEsterno))
        }

        salvaFileFirmato(file, new FileInputStream(fileFirmato))
    }

    @Transactional
    void salvaFileFirmato (FirmaDigitaleFile file, InputStream fileFirmato) {
        IDocumentoEsterno documentoEsterno = AttiFirmaService.getDocumentoIdRiferimento(file.idRiferimentoFile)
        FileAllegato fileAllegato = AttiFirmaService.getFileAllegatoIdRiferimento(file.idRiferimentoFile)

        // se il file in firma non è mai stato firmato da sfera, lo salvo come file originale
        // per poterlo poi ripristinare da zero.
        if (!fileAllegato.firmato) {
            gestioneTesti.salvaFileOriginale(fileAllegato)
        }

        // rimuovo i troppi .p7m se ci sono.
        String estensione = file.nomeFirmato.substring(file.nomeFirmato.lastIndexOf(".") + 1)
        fileAllegato.nome = file.nomeFirmato.replaceAll(/(\.[pP]7[mM])+$/, ".p7m")
        fileAllegato.contentType = TipoFile.getInstanceByEstensione(estensione).contentType
        // Questo è (e DEVE) essere l'unico punto in cui viene impostato a "TRUE" il flag "firmato"
        fileAllegato.firmato = true
        fileAllegato.statoMarcatura = StatoMarcatura.DA_MARCARE

        // salvo il file firmato sul documento.
        gestoreFile.addFile(documentoEsterno, fileAllegato, fileFirmato)

        if (Impostazioni.ALLEGATO_VERIFICA_FIRMA.abilitato) {
            fileFirmatoDettaglioService.calcolaInformazioniFileFirmato(documentoEsterno, fileAllegato)
        }
    }

    @Override
    @Transactional(readOnly = true)
    InputStream getFileDaFirmare (FirmaDigitaleFile file) {
        IDocumentoEsterno documentoEsterno = AttiFirmaService.getDocumentoIdRiferimento(file.idRiferimentoFile)
        FileAllegato fileAllegato = AttiFirmaService.getFileAllegatoIdRiferimento(file.idRiferimentoFile)
        return gestoreFile.getFile(documentoEsterno, fileAllegato)
    }

    @CompileDynamic
    String getCodiceEnte (IDocumentoEsterno documentoEsterno) {
        return documentoEsterno.ente.codice
    }
}
