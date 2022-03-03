package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.commons.TokenIntegrazione
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.dto.commons.TokenIntegrazioneDTO
import org.hibernate.FetchMode
import org.zkoss.bind.annotation.*
import org.zkoss.zul.Window

class LockAttiListaViewModel {

    // Paginazione
    int pageSize = AfcAbstractGrid.PAGE_SIZE_DEFAULT
    int activePage = 0
    int totalSize = 0

    // componenti
    Window self

    // dati
    TokenIntegrazioneDTO selectedRecord
    List<TokenIntegrazioneDTO> listaAttiLock = []

    // services
    SpringSecurityService springSecurityService
    TokenIntegrazioneService tokenIntegrazioneService

    @Init
    init (@ContextParam(ContextType.COMPONENT) Window w) {
        activePage = 0
        totalSize = 0
        this.self = w
        caricaListaLock()
    }

    @NotifyChange(["listaAttiLock", "totalSize"])
    private void caricaListaLock () {
        PagedResultList lista = TokenIntegrazione.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
            eq ("tipo", TokenIntegrazione.TIPO_LOCK_DOCUMENTO)
            order("dateCreated", "asc")
            fetchMode("utenteIns", FetchMode.JOIN)
        }
        totalSize = lista.totalCount
        listaAttiLock = lista.toDTO();

        for (TokenIntegrazioneDTO ti : listaAttiLock) {
            ti.tipo = ti.idRiferimento.substring(0, ti.idRiferimento.lastIndexOf("_"))
            String id = ti.idRiferimento.substring(ti.idRiferimento.lastIndexOf("_")+1)

            def doc = DocumentoFactory.getDocumento(Long.parseLong(id), ti.tipo)
            ti.dati = getInfoDatiNumerazione(doc) ?: "Oggetto: " + getOggetto(doc)
        }
    }

    @NotifyChange(["listaAttiLock", "totalSize"])
    @Command
    void onPagina () {
        caricaListaLock()
    }

    @NotifyChange(["listaAttiLock", "selectedRecord", "activePage", "totalSize"])
    @Command
    void onRefresh () {
        selectedRecord = null
        activePage = 0
        caricaListaLock()
    }

    @NotifyChange(["listaAttiLock", "selectedRecord", "activePage", "totalSize"])
    @Command
    void onUnlock () {
        // sblocco il testo con l'utente che lo ha lockato.
        tokenIntegrazioneService.rimuoviLock(selectedRecord.id)
        onRefresh()
    }

    private String getInfoDatiNumerazione (def doc) {
        if (doc instanceof Allegato) {
            return getInfoDatiNumerazione(doc.documentoPrincipale);
        } else if (doc instanceof Certificato) {
            return getInfoDatiNumerazione(doc.documentoPrincipale);
        } else if (doc instanceof VistoParere) {
            return getInfoDatiNumerazione(doc.documentoPrincipale);
        } else if (doc instanceof Determina) {
            return (doc.numeroDetermina != null && doc.numeroDetermina != "") ? "Determina: " + doc.numeroDetermina + " / " + doc.annoDetermina + " del " + doc.dataNumeroDetermina?.
                    format("dd/MM/yyyy") :
                    ((doc.numeroProposta != null && doc.numeroProposta != "") ? "Proposta: " + doc.numeroProposta + " / " + doc.annoProposta + " del " + doc.dataNumeroProposta?.
                            format("dd/MM/yyyy") : "");
        } else if (doc instanceof Delibera) {
            return (doc.numeroDelibera != null && doc.numeroDelibera != "") ? "Delibera: " + doc.numeroDelibera + " / " + doc.annoDelibera + " del " + doc.dataNumeroDelibera?.
                    format("dd/MM/yyyy") : "";
        } else if (doc instanceof PropostaDelibera) {
            return (doc.numeroProposta != null && doc.numeroProposta != "") ? "Proposta: " + doc.numeroProposta + " / " + doc.annoProposta + " del " + doc.dataNumeroProposta?.
                    format("dd/MM/yyyy") : "";
        }
    }

    private String getOggetto (def doc) {
        if (doc instanceof Allegato) {
            return getOggetto(doc.documentoPrincipale);
        } else if (doc instanceof Certificato) {
            return getOggetto(doc.documentoPrincipale);
        } else if (doc instanceof VistoParere) {
            return getOggetto(doc.documentoPrincipale);
        } else if (doc instanceof Determina) {
            return doc.oggetto
        } else if (doc instanceof Delibera) {
            return doc.oggetto
        } else if (doc instanceof PropostaDelibera) {
            return doc.oggetto
        }
    }

}
