package it.finmatica.atti.integrazioni

import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.Sql
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.AbstractAlboEsterno
import it.finmatica.atti.documenti.IPubblicabile
import it.finmatica.atti.integrazioni.albo.AlboEsternoConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Created by czappavigna on 15/10/2018.
 */
@Component("alboEsterno")
@Lazy
class AlboEsterno extends AbstractAlboEsterno {

    @Autowired AlboEsternoConfig alboEsternoConfig
    @Autowired SpringSecurityService springSecurityService

    @Override
    @Transactional
    public void pubblicaAtto(IPubblicabile atto) {
        log.info("AlboEsterno pubblicaAtto "+ atto.id);
        log.info("AlboEsterno pubblicaAtto DNS: "+ alboEsternoConfig.getDNS());
        log.info("AlboEsterno pubblicaAtto User: "+ alboEsternoConfig.getUser());
        log.info("AlboEsterno pubblicaAtto Driver: "+ alboEsternoConfig.getDriver());
        def url = alboEsternoConfig.getDNS()
        def user = alboEsternoConfig.getUser()
        def password = alboEsternoConfig.getPassword()
        def driver = alboEsternoConfig.getDriver()

        def sql = null

        try {
            sql = Sql.newInstance(url, user, password, driver)

            As4SoggettoCorrente s = As4SoggettoCorrente.findByUtenteAd4(springSecurityService.currentUser)
            String insert = '''INSERT
                                INTO V_ALBO_PRETORIO_ATTIADS
                                  (
                                    UTENTE_INS ,
                                    CF ,
                                    DATA_PROTOCOLLO ,
                                    NUM_PROTOCOLLO ,
                                    PUBBL_DA ,
                                    PUBBL_A ,
                                    GIORNI_PUBBL ,
                                    TIPO_DOC ,
                                    DATA_ADOZIONE ,
                                    NUM_DELI_REGISTRO ,
                                    ANNO_REGISTRO ,
                                    OGGETTO ,
                                    SETTORE_PROPONENTE
                                  )
                                  VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?)'''
            sql.executeInsert(insert, [s?.cognome ? s.cognome + " " + s.nome : springSecurityService.currentUser?.nominativo,
                                       s?.codiceFiscale ?: '',
                                       atto.dataNumeroProtocollo != null ? new java.sql.Date(atto.dataNumeroProtocollo.getTime()) : null,
                                       atto.numeroProtocollo,
                                       new java.sql.Date(atto.dataPubblicazione?.getTime()),
                                       new java.sql.Date(atto.dataFinePubblicazione?.getTime()),
                                       atto.giorniPubblicazione,
                                       atto.tipologiaDocumento?.tipoPubblicazioneAlbo?.trim(), //DA MODIFICARE
                                       new java.sql.Date(atto.dataAtto?.getTime()),
                                       atto.numeroAtto,
                                       atto.annoAtto,
                                       atto.oggetto,
                                       atto.getUnitaProponente()?.descrizione
            ]);
        }
        finally {
            if (sql != null)
                sql.close()
        }
    }
}
