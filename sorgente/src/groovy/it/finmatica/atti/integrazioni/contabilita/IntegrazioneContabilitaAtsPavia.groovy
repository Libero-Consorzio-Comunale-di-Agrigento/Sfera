package it.finmatica.atti.integrazioni.contabilita

import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.dizionari.DizionarioContabile
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.ITipologia
import it.finmatica.atti.integrazioni.ricercadocumenti.DocumentoEsterno
import it.finmatica.atti.integrazioni.ricercadocumenti.PagedList
import it.finmatica.so4.struttura.So4Amministrazione
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.zkoss.bind.BindUtils
import org.zkoss.zk.ui.event.EventQueues

import java.text.SimpleDateFormat

@Component("integrazioneContabilitaAtsPavia")
@Lazy
class IntegrazioneContabilitaAtsPavia extends AbstractIntegrazioneContabilita {

    @Autowired IntegrazioneContabilitaAtsPaviaConfig integrazioneContabilitaAtsPaviaConfig

    @Override
    String getZul(IDocumento documento) {
        return "/atti/integrazioni/contabilita/movimentiContabiliAtsPavia.zul";
    }

    @Override
    boolean isConDocumentiContabili(IDocumento documento) {
        def documentoPrincipale = getProposta(documento);
        return (MovimentoContabile.countByIdDocumentoAndTipoDocumento(documentoPrincipale.id, documentoPrincipale.TIPO_OGGETTO) > 0)
    }

    @Override
    boolean isTipiDocumentoAbilitati() {
        return false;
    }

    void aggiornaMaschera(IDocumento documento, boolean modifica) {
        BindUtils.postGlobalCommand("movimentiContabiliQueue", EventQueues.DESKTOP, "aggiornaAtto", [atto: documento, competenza: modifica ? "W" : "R"])
    }

    List<?> getMovimentiContabili(IDocumento documento) {
        def documentoPrincipale = getProposta(documento);
      //  return MovimentoContabile.findAllByIdDocumentoAndTipoDocumento(documentoPrincipale.id, documentoPrincipale.TIPO_OGGETTO, [sort: 'tipo', order: 'asc']);
        List<MovimentoContabile> risultati = MovimentoContabile.createCriteria().list {
            eq('idDocumento', documentoPrincipale.id)
            eq('tipoDocumento', documentoPrincipale.TIPO_OGGETTO)
            order('annoEsercizio', 'asc')
            order('id', 'asc')
        }
    }

    boolean isAttoConImpegnoSpesa(ITipologia tipologia) {
        return integrazioneContabilitaAtsPaviaConfig.isAttoConImpegnoSpesa(tipologia.id)
    }

    boolean isAttoConVariazione(ITipologia tipologia) {
        return integrazioneContabilitaAtsPaviaConfig.isAttoConVariazione(tipologia.id)
    }

    boolean isAttoTrovaCopertura(ITipologia tipologia) {
        return integrazioneContabilitaAtsPaviaConfig.isAttoTrovaCopertura(tipologia.id)
    }

    List<DizionarioContabile> getAnni(So4Amministrazione ente) {
        String annoCorrente = new SimpleDateFormat("yyyy").format(new Date())
        return DizionarioContabile.createCriteria().list() {
            eq("tipo", "ANNO")
        //    ge("codice", annoCorrente)
            eq("valido", true)
            eq("ente", ente)
            order("sequenza", "asc")
        }
    }

    PagedList<DizionarioContabile> getDizionario(So4Amministrazione ente, String dizionario, String codice, String descrizione, int inizio, int numeroRisultati) {
        List<DizionarioContabile> documenti = DizionarioContabile.createCriteria().list() {
            groupProperty("codice")
            groupProperty("descrizione")

            eq("tipo", dizionario)
            ilike('codice', "%${codice}%")
            ilike('descrizione', "%${descrizione}%")
            eq("valido", true)
            eq("ente", ente)

            order("sequenza", "asc")
            order("codice", "asc")

            firstResult(inizio)
            maxResults(numeroRisultati)

        }

        int totalCount = DizionarioContabile.createCriteria().get() {

            projections { countDistinct("codice") }

            eq("tipo", dizionario)
            ilike('codice', "%${codice}%")
            ilike('descrizione', "%${descrizione}%")
            eq("valido", true)
            eq("ente", ente)

        }

        return new PagedList<DocumentoEsterno>(documenti, totalCount)
    }

    @Override
    BigDecimal totaleImporto(IDocumento documento, String tipo) {
        boolean isAttoConImpegnoSpesa = integrazioneContabilitaAtsPaviaConfig.isAttoConImpegnoSpesa(documento.tipologiaDocumento.id)
        boolean isAttoConVariazione = integrazioneContabilitaAtsPaviaConfig.isAttoConVariazione(documento.tipologiaDocumento.id)

        String nomeCampo
        if (isAttoConImpegnoSpesa) {
            nomeCampo = "importo"
        }
        else {
            if (isAttoConVariazione) {
                nomeCampo = "importoVariazione"
            }
        }


        BigDecimal importo

        if (isAttoConImpegnoSpesa || isAttoConVariazione) {
            importo = MovimentoContabile.createCriteria().get {
                projections {
                    sum(nomeCampo)
                }

                and {
                    eq("idDocumento", documento.id)
                    eq("tipoDocumento", documento.TIPO_OGGETTO)
                    isNotNull(nomeCampo)
                }
            }
        }
        else {

        }

        return importo
    }
}
