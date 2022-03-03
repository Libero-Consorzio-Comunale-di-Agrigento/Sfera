package it.finmatica.atti.integrazioni.pec

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.documenti.DestinatarioNotifica
import it.finmatica.atti.documenti.DestinatarioNotificaAttivita
import it.finmatica.atti.documenti.IProtocollabile
import it.finmatica.atti.documenti.SoggettoNotifica
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdmCondition
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdmConfig
import it.finmatica.atti.integrazioniws.ads.ducd.ParametriIngresso
import it.finmatica.atti.integrazioniws.ads.ducd.ParametriUscita
import it.finmatica.atti.integrazioniws.ads.ducd.PecSOAPImpl
import it.finmatica.zkutils.LabelUtils
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Created by esasdelli on 08/11/2017.
 */
@Conditional(ProtocolloGdmCondition)
@Component
@Lazy
class IntegrazionePecDucd {
    private static final transient Logger log = Logger.getLogger(IntegrazionePecDucd.class)
    @Autowired
    private SpringSecurityService springSecurityService

    @Autowired
    private IGestoreFile gestoreFile

    @Autowired
    private PecSOAPImpl ducdPecClient
    @Autowired ProtocolloGdmConfig protocolloGdmConfig

    void inviaPec (String tipoNotifica, IProtocollabile protocollabile, List<SoggettoNotifica> soggetti) {
        log.info("Invio atto tramite Ducd")
        if (!(protocollabile.numeroProtocollo > 0)) {
            throw new AttiRuntimeException("Non è possibile inviare via PEC un documento non protocollato.")
        }
        String destinatari = getListaDestinatari(soggetti)

        if (destinatari.length() > 4000){
            def sottolisteSoggetto = dividiSoggetti(soggetti)
            for (List<SoggettoNotifica> sottolista : sottolisteSoggetto){
                inviaPecSingola(tipoNotifica, protocollabile, sottolista)
            }
        }
        else {
            inviaPecSingola(tipoNotifica, protocollabile, soggetti)
        }
        protocollabile.save()
        log.info("Invio atto tramite Ducd terminato")
    }


    private inviaPecSingola (String tipoNotifica, IProtocollabile protocollabile, List<SoggettoNotifica> soggetti) {
        String destinatari = getListaDestinatari(soggetti)
        log.info("Invio atto tramite Ducd ai destinatari: ${destinatari}")
        ParametriUscita pu = ducdPecClient.invioPec(getParametriIngresso(protocollabile, destinatari))
        if (pu.codice < 0) {
            throw new AttiRuntimeException("Errore in spedizione PEC: ${pu.descrizione}")
        }
        log.info("Invio atto tramite Ducd - aggiornamento dei destinatari")
        // per ogni destinatario a cui ho inviato la mail, creo un destinatarionotificaattività con il codice del messaggio della pec:
        for (SoggettoNotifica soggettoNotifica : soggetti) {
            if (soggettoNotifica != null) {
                DestinatarioNotifica destinatario = soggettoNotifica.destinatarioNotificaDTO.domainObject

                new DestinatarioNotificaAttivita(idRiferimento: "${tipoNotifica}_${protocollabile.getProperties().TIPO_OGGETTO}_${protocollabile.id}", idAttivita: pu.msgId, destinatarioNotifica: destinatario, modalitaInvio: DestinatarioNotificaAttivita.TIPO_NOTIFICA_PEC, soggettoNotifica: DestinatarioNotificaAttivita.NOTIFICA_UTENTE).save()
            }
        }
    }
    String getUrlRicevuta (IProtocollabile protocollabile) {
        return "${Impostazioni.URL_SERVER_GDM.valore}/jdms/common/ElencoCartellePerDoc.do?idDoc=${protocollabile.idDocumentoEsterno}&Provenienza=Q&idCartProveninez=null&idQueryProveninez=-1&tipo=D&stato=BO"
    }

    ParametriIngresso getParametriIngresso (IProtocollabile protocollabile, String destinatari) {
        ParametriIngresso p = new ParametriIngresso()

        p.numero = protocollabile.numeroProtocollo.toString()
        p.anno = protocollabile.annoProtocollo.toString()
        p.idDocumento = -1
        p.listaDestinatari = destinatari
        p.utenteCreazione = (String)springSecurityService.currentUser.nominativo
        p.tipoRegistro = protocolloGdmConfig.getCodiceRegistro()

        return p
    }

    List<List<SoggettoNotifica>> dividiSoggetti (List<SoggettoNotifica> soggetti) {
        log.info("Invio atto tramite Ducd - Troppi soggetti presenti: verranno effettuati più invii")
        def sottoliste = []
        String email = ""
        def sottolista = []
        soggetti.findAll {it?.email != null}.each{
            if ((email+"###"+it.email).length() < 4000){
                email += "###"+it.email
                sottolista << it
            }
            else {
                email = "###"+it.email
                sottoliste << sottolista
                sottolista = [it]
            }
        }
        sottoliste << sottolista
        return sottoliste
    }

    String getListaDestinatari (List<SoggettoNotifica> soggetti) {
        return soggetti.findAll {it?.email != null}.email.join("###")
    }
}
