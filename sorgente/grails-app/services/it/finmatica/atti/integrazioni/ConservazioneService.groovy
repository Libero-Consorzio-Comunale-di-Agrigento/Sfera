package it.finmatica.atti.integrazioni

import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.GdmDocumentaleEsterno
import it.finmatica.atti.dto.integrazioni.JConsLogConservazioneDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.conservazione.JConsConfig
import it.finmatica.atti.integrazioniws.ads.jcons.JConsService
import it.finmatica.atti.integrazioniws.ads.jcons.JConsServiceResponse

import javax.sql.DataSource

class ConservazioneService {

    SpringSecurityService springSecurityService
    VistoParereService vistoParereService
    CertificatoService certificatoService
    GdmDocumentaleEsterno gestoreDocumentaleEsterno
    TokenIntegrazioneService tokenIntegrazioneService
    JConsService jconsServiceClient
    JConsConfig JConsConfig

    // connessioni al db
    DataSource dataSource_gdm

    JConsLogConservazioneDTO getLastLog(Long idDocumentoEsterno, StatoConservazione statoConservazione) {
        JConsLogConservazione logConservazione = JConsLogConservazione.getLast(idDocumentoEsterno).get()
        if (logConservazione == null) {
            return null
        }

        JConsLogConservazioneDTO dto = logConservazione.toDTO()
        dto.statoConservazione = statoConservazione
        dto.urlRicevuta = gestoreDocumentaleEsterno.getUrlDocumento(logConservazione)

        return dto
    }

    void conservaDocumenti (def documenti) {
        setDocumentiInConservazione(documenti.toList())
        documenti = DocumentoFactory.getDocumenti(documenti.toList())
        conserva (documenti*.idDocumentoEsterno)
    }

    void documentiDaConservare (def documenti) {
        setDocumentiInConservazione(documenti.toList())
        documenti = DocumentoFactory.getDocumenti(documenti.toList())
        inviaPerConservazione (documenti*.idDocumentoEsterno)
    }

    private void setDocumentiInConservazione (List documenti) {
        Determina.withNewTransaction {
            List atti = DocumentoFactory.getDocumenti(documenti)
            atti*.setStatoConservazione(StatoConservazione.IN_CONSERVAZIONE)
            atti*.save()
        }
    }

    /**
     * Cerca i documenti in stato DA_CONSERVARE e che sono conclusi e li invia in conservazione.
     */
    void inviaDocumentiInConservazione() {
        // ottengo i documenti da conservare:
        List documenti = getDelibereDaConservare() + getDetermineDaConservare()
        List daConservare = new ArrayList()
        for (def documento : documenti) {
            if (isDocumentiCollegatiConclusi(documento) && (daConservare.size() < Impostazioni.CONSERVAZIONE_AUTOMATICA_LIMITE.valoreInt)) {
                daConservare.add([idDocumentoPrincipale: documento.id, tipoDocumentoPrincipale: documento.tipoOggetto])
            }
        }

        if (!daConservare.isEmpty()) {
            try {
                conservaDocumenti(daConservare)
            } catch (AttiRuntimeException e) {
                log.error(e.message, e)
            }
        }
    }

    /**
     * Ritorna le determine che sono in stato DA_CONSERVARE
     *
     * @return
     */
    List<Determina> getDetermineDaConservare() {
        List<Determina> listaDetermineDaConservare = []

        Determina.findAllByValidoAndStatoConservazione(true, StatoConservazione.DA_CONSERVARE, [sort:'dataEsecutivita', order:'asc']).each { dete ->
            listaDetermineDaConservare.add(dete)
        }

        Determina.findAllByValidoAndStatoConservazione(true, StatoConservazione.ERRORE_INVIO, [sort:'dataEsecutivita', order:'asc']).each { dete ->
           listaDetermineDaConservare.add(dete)
        }

        return listaDetermineDaConservare
    }

    /**
     * Ritorna le delibere che sono in stato DA_CONSERVARE o ERRORE INVIO
     *
     * @return
     */
    List<Delibera> getDelibereDaConservare() {
        List<Delibera> listaDelibereDaConservare = []

        Delibera.findAllByValidoAndStatoConservazione(true, StatoConservazione.DA_CONSERVARE, [sort:'dataEsecutivita', order:'asc']).each { deli ->
            listaDelibereDaConservare.add(deli)
        }

        Delibera.findAllByValidoAndStatoConservazione(true, StatoConservazione.ERRORE_INVIO, [sort:'dataEsecutivita', order:'asc']).each { deli ->
            listaDelibereDaConservare.add(deli)
        }

        return listaDelibereDaConservare
    }

    /**
     * Ritorna TRUE se tutti i visti e i certificati sono conclusi sul documento richiesto.
     *
     * @param documento
     * @return
     */
    boolean isDocumentiCollegatiConclusi(def documento) {
        if (!vistoParereService.isTuttiVistiConclusi(documento)) {
            return false
        }

        if (!certificatoService.isTuttiCertificatiConclusi(documento)) {
            return false
        }

        return true
    }

    /**
     * Conserva i documenti inviandoli al JCons.
     *
     * Invia i documenti al JCons per l'immediata conservazione.
     *
     * @param idDocs
     * @param dataEsecuzione
     * @return
     */
    String conserva(List<String> idDocs, Date dataEsecuzione = new Date()) {
        JConsServiceResponse resp = jconsServiceClient.storeDocuments(idDocs
                                                                    , dataEsecuzione.format("dd/MM/yyyy HH:mm:ss")
                                                                    , springSecurityService.currentUser.id
                                                                    , JConsConfig.getUrlServer()
                                                                    , JConsConfig.getContextPath()
                                                                    , JConsConfig.getNomeIter()
                                                                    , "", "")

        if (!resp.getResult().equals("0")) {
            throw new AttiRuntimeException("Si è verificato un problema durante la richiesta di conservazione!\nErrore: " + resp.getErrStr())
        }

        return resp.getUrlReport()
    }

    /**
     * Predispobe i documenti per la conservazione su jcons
     * Invia i documenti al JCons segnandoli come da conservare. Il JCons li conserverà in un secondo momento.
     *
     * @param idDocs
     * @return
     */
    String inviaPerConservazione(List<String> idDocs) {
        JConsServiceResponse resp = jconsServiceClient.markDocumentsToStore(idDocs, springSecurityService.currentUser.id, JConsConfig.getUrlServer(), JConsConfig.getContextPath(), JConsConfig.getNomeIter())

        if (!resp.getResult().equals("0")) {
            throw new AttiRuntimeException("Si è verificato un problema durante la richiesta di conservazione!\nErrore: " + resp.getErrStr());
        }

        return resp.getUrlReport()
    }

    /**
     * Ritorna le determine inviata al JCons per la conservazione
     *
     * @return
     */
    List<Determina> getDetermineInConservazione() {
        return Determina.findAllByStatoConservazioneInListAndIdDocumentoEsternoIsNotNull([StatoConservazione.IN_CONSERVAZIONE, StatoConservazione.ERRORE])
    }

    /**
     * Ritorna le delibere inviata al JCons per la conservazione
     *
     * @return
     */
    List<Delibera> getDelibereInConservazione() {
        return Delibera.findAllByStatoConservazioneInListAndIdDocumentoEsternoIsNotNull([StatoConservazione.IN_CONSERVAZIONE, StatoConservazione.ERRORE])
    }

    /**
     * Aggiorna sulle determine in sfera lo stato di conservazione letto da GDM_T_LOG_CONSERVAZIONE
     *
     * @param determina
     */
    void aggiornaStatoConservazione(Determina determina) {
        if (determina.idDocumentoEsterno == null) {
            return
        }

        StatoConservazione statoConservazione = getStatoConservazione(determina.idDocumentoEsterno)
        if (statoConservazione == null) {
            return
        }

        log.debug("Setto lo stato della Determina ${determina.id} del documento esterno GDM ${determina.idDocumentoEsterno} a STATO_CONSERVAZIONE =  ${statoConservazione}")
        determina.statoConservazione = statoConservazione
        determina.save()
    }

    /**
     * Aggiorna sulle determine lo stato di conservazione letto da GDM_T_LOG_CONSERVAZIONE sulle Delibere
     *
     * @param delibera
     */
    void aggiornaStatoConservazione(Delibera delibera) {
        if (delibera.idDocumentoEsterno == null) {
            return
        }

        StatoConservazione statoConservazione = getStatoConservazione(delibera.idDocumentoEsterno)
        if (statoConservazione == null) {
            return
        }

        log.debug("Setto lo stato della Delibera ${delibera.id} del documento esterno GDM ${delibera.idDocumentoEsterno} a STATO_CONSERVAZIONE =  ${statoConservazione}")
        delibera.statoConservazione = statoConservazione
        delibera.save()
    }

    /**
     * Legge lo stato di conservazione dalla tabella GDM_T_LOG_CONSERVAZIONE
     *
     * @param idDocumento
     * @return lo stato di conservazione
     */
    StatoConservazione getStatoConservazione(long idDocumento) {
        StatoConservazione stato = null
        Integer countLogs, countDocs
        List<GroovyRowResult> result

        def sql = new Sql(dataSource_gdm)
        countDocs = 0
        result = sql.rows("select stato_conservazione from GDM_T_LOG_CONSERVAZIONE where id_documento_rif = :idDocumento", [idDocumento: idDocumento])

        if (result.size() <= 0) {
            stato = StatoConservazione.ERRORE_INVIO;
            return stato
        }

        countLogs = result.size()

        for (def res : result) {
            String stato_conservazione = res.getAt(0)

            if (stato_conservazione.equals("CC")) {
                stato = StatoConservazione.CONSERVATO
                return stato
            }

            if (stato_conservazione.equals("FC"))
                countDocs++
        }

        if (countLogs == countDocs)
            stato = StatoConservazione.ERRORE

        return stato
    }

    /**
     * Aggiorna lo stato della conservazione delle delibere
     */
    public void aggiornaStatiConservazioneDelibere() {
        List<Delibera> delibere = getDelibereInConservazione()
        for (Delibera delibera : delibere){
            aggiornaStatoConservazione(delibera)
        }
    }

    /**
     * Aggiorna lo stato della conservazione delle determine
     */
    public void aggiornaStatiConservazioneDetermine() {
        List<Determina> determine = getDetermineInConservazione()
        for (Determina determina : determine){
            aggiornaStatoConservazione(determina)
        }
    }
}

