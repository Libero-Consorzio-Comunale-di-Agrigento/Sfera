package it.finmatica.atti.integrazioni.ricercadocumenti.agsde2

import grails.plugin.springsecurity.SpringSecurityService
import org.springframework.transaction.annotation.Transactional
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.viste.RicercaAllegatiDetermina
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.integrazioni.ricercadocumenti.AllegatoEsterno
import it.finmatica.atti.integrazioni.ricercadocumenti.CampiRicerca
import it.finmatica.atti.integrazioni.ricercadocumenti.PagedList
import it.finmatica.atti.integrazioni.ricercadocumenti.RicercaAllegatiDocumentiEsterni
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
class RicercaAllegatiDetermine implements RicercaAllegatiDocumentiEsterni {

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
        return "Determine"
    }

    @Override
    String getDescrizione () {
        return "Ricerca Allegati Determine su Sfera"
    }

    @Override
    String getZulCampiRicerca () {
        return "/atti/integrazioni/ricercaDocumenti/agsde2/campiRicercaDetermine.zul"
    }

    @Override
    PagedList<AllegatoEsterno> ricerca (CampiRicerca campiRicerca) {

        List<RicercaAllegatiDetermina> risultatiRicerca = RicercaAllegatiDetermina.createCriteria().list() {

            projections {
                groupProperty("idDocumento")           //0
                groupProperty("tipoDocumento")         //1
                groupProperty("idFileEsterno")         //2
                groupProperty("idFileAllegato")        //3
                groupProperty("annoDetermina")         //4
                groupProperty("numeroDetermina")       //5
                groupProperty("dataNumeroDetermina")   //6
                groupProperty("oggetto")               //7
                groupProperty("codiceRegistro")        //8
                groupProperty("registro")              //9
                groupProperty("nome")                  //10
            }

            gestoreCompetenze.controllaCompetenze(delegate, "compUtente", "compUnita", "compRuolo")(springSecurityService.principal)
            getFiltri(delegate)(campiRicerca)

            order("annoDetermina", "asc")
            order("numeroDetermina", "asc")

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

        // calcolo il totale dei record
        int totalCount = RicercaAllegatiDetermina.createCriteria().get() {
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
        return campiRicerca
    }


    private Closure getFiltri (def delegate) {
        Closure filtri = { CampiRicerca campiRicerca ->
            if (campiRicerca.filtri.ANNO != null) {
                eq("annoDetermina", Long.parseLong(String.valueOf(campiRicerca.filtri.ANNO)))
            }
            if (campiRicerca.filtri.NUMERO != null) {
                eq("numeroDetermina", Long.parseLong(String.valueOf(campiRicerca.filtri.NUMERO)))
            }
            if (campiRicerca.filtri.REGISTRO != null && campiRicerca.filtri.REGISTRO.codice != "none") {
                eq("codiceRegistro", campiRicerca.filtri.REGISTRO.codice)
            }
            if (campiRicerca.filtri.OGGETTO != null) {
                ilike("oggetto", "%" + campiRicerca.filtri.OGGETTO + "%")
            }
        }

        filtri.delegate = delegate

        return filtri
    }

    private List<TipoRegistroDTO> getRegistri () {
        List<TipoRegistroDTO> registri = Determina.createCriteria().list {
            projections {
                distinct("registroDetermina")
            }

            eq("valido", true)
            isNotNull("registroDetermina")

            fetchMode("registroDetermina", FetchMode.JOIN)
        }.toDTO()

        registri.add(0, new TipoRegistroDTO(codice: "none", descrizione: "-- nessuno --"))

        return registri
    }
}

