package it.finmatica.atti.integrazioni.ricercadocumenti.agsde2

import grails.plugin.springsecurity.SpringSecurityService
import org.springframework.transaction.annotation.Transactional
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.competenze.DeterminaCompetenze
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.documenti.DeterminaDTO
import it.finmatica.atti.dto.documenti.DocumentoCollegatoDTO
import it.finmatica.atti.integrazioni.ricercadocumenti.CampiRicerca
import it.finmatica.atti.integrazioni.ricercadocumenti.DocumentoEsterno
import it.finmatica.atti.integrazioni.ricercadocumenti.PagedList
import it.finmatica.atti.integrazioni.ricercadocumenti.RicercaDocumentiEsterni
import org.hibernate.FetchMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Created by esasdelli on 03/10/2017.
 */
@Lazy
@Component
@Order(1)
@Transactional(readOnly = true)
class RicercaDetermine implements RicercaDocumentiEsterni {

    @Autowired
    private AttiGestoreCompetenze gestoreCompetenze

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
        return "Ricerca Determine su Sfera"
    }

    @Override
    String getZulCampiRicerca () {
        return "/atti/integrazioni/ricercaDocumenti/agsde2/campiRicercaDetermine.zul"
    }

    @Override
    PagedList<DocumentoEsterno> ricerca (CampiRicerca campiRicerca) {
        // restituisce la lista contenente le determine che fanno match
        def risultatiRicerca = DeterminaCompetenze.createCriteria().list() {

            projections {
                determina {
                    groupProperty("id")
                    groupProperty("annoDetermina")
                    groupProperty("numeroDetermina")
                    groupProperty("oggetto")
                    groupProperty("idDocumentoEsterno")

                    registroDetermina { groupProperty("codice") }
                }
            }

            gestoreCompetenze.controllaCompetenze(delegate)(springSecurityService.principal)
            getFiltri(delegate)(campiRicerca)

            determina {
                order("annoDetermina", "asc")
                order("numeroDetermina", "asc")
            }

            firstResult(campiRicerca.startFrom)
            maxResults(campiRicerca.maxResults)

        }.collect { row ->
            new DocumentoEsterno(idDocumento: row[0], estremi: "${row[2]} / ${row[1]} - ${row[5]}", oggetto: row[3], idDocumentoEsterno: row[4],
                                 tipoDocumento: Determina.TIPO_OGGETTO)
        }

        // calcolo il totale dei record
        int totalCount = DeterminaCompetenze.createCriteria().get() {
            projections { countDistinct("determina") }
            gestoreCompetenze.controllaCompetenze(delegate)(springSecurityService.principal)
            getFiltri(delegate)(campiRicerca)
        }

        return new PagedList<DocumentoEsterno>(risultatiRicerca, totalCount)
    }

    @Override
    CampiRicerca getCampiRicerca () {
        CampiRicerca campiRicerca = new CampiRicerca()
        campiRicerca.filtri = [:]
        campiRicerca.filtri.LISTA_REGISTRI = getRegistri()
        return campiRicerca
    }

    @Override
    DocumentoCollegatoDTO creaDocumentoCollegato (DocumentoEsterno documentoEsterno, String operazione) {
        return new DocumentoCollegatoDTO(determinaCollegata: new DeterminaDTO(id: documentoEsterno.idDocumento), operazione: operazione)
    }

    private Closure getFiltri (def delegate) {
        Closure filtri = { CampiRicerca campiRicerca ->
            determina {
                if (campiRicerca.filtri.ANNO != null) {
                    eq("annoDetermina", campiRicerca.filtri.ANNO)
                }
                if (campiRicerca.filtri.NUMERO != null) {
                    eq("numeroDetermina", campiRicerca.filtri.NUMERO)
                }
                if (campiRicerca.filtri.REGISTRO != null && campiRicerca.filtri.REGISTRO.codice != "none") {
                    eq("registroDetermina.codice", campiRicerca.filtri.REGISTRO.codice)
                }
                if (campiRicerca.filtri.OGGETTO != null) {
                    ilike("oggetto", "%" + campiRicerca.filtri.OGGETTO + "%")
                }
                fetchMode("registroDetermina", FetchMode.JOIN)
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

