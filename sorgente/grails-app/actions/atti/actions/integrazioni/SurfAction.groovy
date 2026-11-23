package atti.actions.integrazioni

import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.Sql
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.documenti.DatoAggiuntivo
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.TipoDatoAggiuntivo
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource

/**
 * Contiene le azioni per la comunicazione con SURF.
 */
class SurfAction {
    SpringSecurityService springSecurityService
    @Autowired DataSource dataSource

    DatiAggiuntiviService datiAggiuntiviService

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO],
            nome = "Salva collegamento alla pratica SURF",
            descrizione = "Salva collegamento alla pratica SURF")
    def salvaCollegamentoPraticaSurf (def documento) {
        String idPraticaSportello = TipoDatoAggiuntivo.getValore(documento.datiAggiuntivi.toDTO(), TipoDatoAggiuntivo.ID_PRATICA_SPORTELLO)
        String codiceUtente = springSecurityService.currentUser.id
        String codiceUnita = documento.unitaProponente.codice
        String codiceEnte = documento.ente.codice
        new Sql(dataSource).execute("{ call sportello_integrazione_agsde2_pkg.ins_pratica_atto (:p_id_pratica, :p_id_atto, 'DETERMINA', :p_oggetto, :p_utente, :p_ente, :p_uo ) }", [p_id_pratica: idPraticaSportello, p_id_atto: documento.id, p_oggetto: documento.oggetto, p_utente: codiceUtente, p_ente: codiceEnte, p_uo: codiceUnita])

        return documento;
    }


    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO],
            nome = "Elimina collegamento alla pratica SURF",
            descrizione = "Elimina collegamento alla pratica SURF")
    def eliminaCollegamentoPraticaSurf (def documento) {
        DatoAggiuntivo datoAggiuntivo = datiAggiuntiviService.getDatoAggiuntivo(documento, TipoDatoAggiuntivo.ID_PRATICA_SPORTELLO)
        String idPraticaSportello = datoAggiuntivo.valore
        new Sql(dataSource).execute("{ call sportello_integrazione_agsde2_pkg.del_pratica_atto (:p_id_pratica, :p_id_atto ) }", [p_id_pratica: idPraticaSportello, p_id_atto: documento.id])

        datiAggiuntiviService.elimina(datoAggiuntivo)
        return documento;
    }
}
