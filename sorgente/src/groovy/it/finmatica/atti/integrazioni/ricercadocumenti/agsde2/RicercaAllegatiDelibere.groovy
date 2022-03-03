package it.finmatica.atti.integrazioni.ricercadocumenti.agsde2

import grails.plugin.springsecurity.SpringSecurityService
import org.springframework.transaction.annotation.Transactional
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.viste.RicercaAllegatiDelibera
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.odg.dizionari.EsitoDTO
import it.finmatica.atti.integrazioni.ricercadocumenti.AllegatoEsterno
import it.finmatica.atti.integrazioni.ricercadocumenti.CampiRicerca
import it.finmatica.atti.integrazioni.ricercadocumenti.PagedList
import it.finmatica.atti.integrazioni.ricercadocumenti.RicercaAllegatiDocumentiEsterni
import it.finmatica.atti.odg.dizionari.Esito
import org.hibernate.FetchMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Created by dscandurra on 16/11/2017.
 */
@Lazy
@Component
@Order(1)
@Transactional(readOnly = true)
class RicercaAllegatiDelibere implements RicercaAllegatiDocumentiEsterni {

    @Autowired
    private AttiGestoreCompetenze gestoreCompetenze

    @Autowired
    private IGestoreFile gestoreFile

    @Autowired
    private SpringSecurityService springSecurityService

    @Override
    boolean isAbilitato () {
        return true
    }

    @Override
    String getTitolo () {
        return "Delibere"
    }

    @Override
    String getDescrizione () {
        return "Ricerca Allegati Delibere su Sfera"
    }

    @Override
    String getZulCampiRicerca () {
        return "/atti/integrazioni/ricercaDocumenti/agsde2/campiRicercaDelibere.zul"
    }

    @Override
    PagedList<AllegatoEsterno> ricerca (CampiRicerca campiRicerca) {

        List<RicercaAllegatiDelibera> risultatiRicerca = RicercaAllegatiDelibera.createCriteria().list() {

            projections {
                groupProperty("idDocumento")           //0
                groupProperty("tipoDocumento")         //1
                groupProperty("idFileEsterno")         //2
                groupProperty("idFileAllegato")        //3
                groupProperty("annoDelibera")         //4
                groupProperty("numeroDelibera")       //5
                groupProperty("dataNumeroDelibera")   //6
                groupProperty("oggetto")               //7
                groupProperty("codiceRegistro")        //8
                groupProperty("registro")              //9
                groupProperty("nome")                  //10
            }

            gestoreCompetenze.controllaCompetenze(delegate, "compUtente", "compUnita", "compRuolo")(springSecurityService.principal)

            getFiltri(delegate)(campiRicerca)

            order("annoDelibera", "asc")
            order("numeroDelibera", "asc")

            firstResult(campiRicerca.startFrom)
            maxResults(campiRicerca.maxResults)

        }.collect {
            row ->
                new AllegatoEsterno(idDocumentoPrincipale: row[0],
                                    tipoDocumento: row[1],
                                    idFileEsterno: row[2],
                                    idFileAllegato: row[3],
                                    nome: row[10],
                                    contentType: "application/octet-stream",
                                    estremi: "${row[9]} - ${row[5]} / ${row[4]} - del ${row[6]?.format("dd/MM/yyyy")}",
                                    oggetto: row[7])

        }

        //calcolo il totale dei record
        int totalCount = RicercaAllegatiDelibera.createCriteria().get() {
            projections { countDistinct("idDocumento") }
            gestoreCompetenze.controllaCompetenze(delegate, "compUtente", "compUnita", "compRuolo")(springSecurityService.principal)
            getFiltri(delegate)(campiRicerca)
        }
        return new PagedList<AllegatoEsterno>(risultatiRicerca, totalCount)
    }

    @Override
    CampiRicerca getCampiRicerca () {
        CampiRicerca campiRicerca = new CampiRicerca()
        campiRicerca.filtri = [:]
        campiRicerca.filtri.LISTA_REGISTRI = getRegistri()
        campiRicerca.filtri.LISTA_ESITI = getEsiti()
        return campiRicerca
    }

    private Closure getFiltri (def delegate) {
        Closure filtri = { CampiRicerca campiRicerca ->
            if (campiRicerca.filtri.ANNO != null) {
                eq("annoDelibera", Long.parseLong(String.valueOf(campiRicerca.filtri.ANNO)))
            }
            if (campiRicerca.filtri.NUMERO != null) {
                eq("numeroDelibera", Long.parseLong(String.valueOf(campiRicerca.filtri.NUMERO)))
            }
            if (campiRicerca.filtri.REGISTRO != null && campiRicerca.filtri.REGISTRO.codice != "none") {
                eq("codiceRegistro", campiRicerca.filtri.REGISTRO.codice)
            }
            if (campiRicerca.filtri.ESITO != null && campiRicerca.filtri.ESITO.id > 0) {
                eq("codiceEsito", campiRicerca.filtri.ESITO.id)
            }
            if (campiRicerca.filtri.OGGETTO != null) {
                ilike("oggetto", "%" + campiRicerca.filtri.OGGETTO + "%")
            }
        }

        filtri.delegate = delegate

        return filtri
    }

    private List<TipoRegistroDTO> getRegistri () {
        List<TipoRegistroDTO> registri = Delibera.createCriteria().list {
            projections {
                distinct("registroDelibera")
            }

            eq("valido", true)
            isNotNull("registroDelibera")

            fetchMode("registroDelibera", FetchMode.JOIN)
        }.toDTO()

        registri.add(0, new TipoRegistroDTO(codice: "none", descrizione: "-- nessuno --"))

        return registri
    }

    private List<EsitoDTO> getEsiti () {
        List<EsitoDTO> esiti = Esito.createCriteria().list() {
            eq("valido", true)
            esitoStandard {
                eq("creaDelibera", true)
            }
            order('titolo', 'asc')

            fetchMode("esitoStandard", FetchMode.JOIN)
        }.toDTO()

        esiti.add(0, new EsitoDTO(id: -1, titolo: "-- nessuno --"))

        return esiti
    }

}

