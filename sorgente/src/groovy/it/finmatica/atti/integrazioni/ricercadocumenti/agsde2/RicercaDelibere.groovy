package it.finmatica.atti.integrazioni.ricercadocumenti.agsde2

import grails.plugin.springsecurity.SpringSecurityService
import org.springframework.transaction.annotation.Transactional
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.competenze.DeliberaCompetenze
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.documenti.DeliberaDTO
import it.finmatica.atti.dto.documenti.DocumentoCollegatoDTO
import it.finmatica.atti.dto.odg.dizionari.EsitoDTO
import it.finmatica.atti.integrazioni.ricercadocumenti.CampiRicerca
import it.finmatica.atti.integrazioni.ricercadocumenti.DocumentoEsterno
import it.finmatica.atti.integrazioni.ricercadocumenti.PagedList
import it.finmatica.atti.integrazioni.ricercadocumenti.RicercaDocumentiEsterni
import it.finmatica.atti.odg.dizionari.Esito
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
@Order(2)
@Transactional(readOnly = true)
class RicercaDelibere implements RicercaDocumentiEsterni {

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
        return "Delibere"
    }

    @Override
    String getDescrizione () {
        return "Ricerca Delibere su Sfera"
    }

    @Override
    String getZulCampiRicerca () {
        return "/atti/integrazioni/ricercaDocumenti/agsde2/campiRicercaDelibere.zul"
    }

    @Override
    PagedList<DocumentoEsterno> ricerca (CampiRicerca campiRicerca) {
        // restituisce la lista contenente le delibere che fanno match
        def documenti = DeliberaCompetenze.createCriteria().list() {

            projections {
                delibera {
                    groupProperty("id")
                    groupProperty("annoDelibera")
                    groupProperty("numeroDelibera")
                    groupProperty("oggetto")
                    groupProperty("idDocumentoEsterno")

                    registroDelibera { groupProperty("codice") }
                }
            }

            gestoreCompetenze.controllaCompetenze(delegate)(springSecurityService.principal)

            getFiltri(delegate)(campiRicerca)

            delibera {
                order("annoDelibera", "asc")
                order("numeroDelibera", "asc")
            }

            firstResult(campiRicerca.startFrom)
            maxResults(campiRicerca.maxResults)

        }.collect { row ->
            new DocumentoEsterno(idDocumento: row[0], estremi: "${row[2]} / ${row[1]} - ${row[5]}", oggetto: row[3], idDocumentoEsterno: row[4],
                                 tipoDocumento: Delibera.TIPO_OGGETTO)
        }

        //calcolo il totale dei record
        int totalCount = DeliberaCompetenze.createCriteria().get() {

            projections { countDistinct("delibera") }

            gestoreCompetenze.controllaCompetenze(delegate)(springSecurityService.principal)

            getFiltri(delegate)(campiRicerca)
        }

        return new PagedList<DocumentoEsterno>(documenti, totalCount)
    }

    private Closure getFiltri (def delegate) {
        Closure filtri = { CampiRicerca campiRicerca ->
            delibera {
                if (campiRicerca.filtri.ANNO != null) {
                    eq("annoDelibera", campiRicerca.filtri.ANNO)
                }
                if (campiRicerca.filtri.NUMERO != null) {
                    eq("numeroDelibera", campiRicerca.filtri.NUMERO)
                }
                if (campiRicerca.filtri.REGISTRO != null && campiRicerca.filtri.REGISTRO.codice != "none") {
                    eq("registroDelibera.codice", campiRicerca.filtri.REGISTRO.codice)
                }
                if (campiRicerca.filtri.ESITO != null && campiRicerca.filtri.ESITO.id > 0) {
                    propostaDelibera { oggettoSeduta { eq("esito.id", campiRicerca.filtri.ESITO.id) } }
                }
                if (campiRicerca.filtri.OGGETTO != null) {
                    ilike("oggetto", "%" + campiRicerca.filtri.OGGETTO + "%")
                }
                fetchMode("registroDelibera", FetchMode.JOIN)
            }
        }

        filtri.delegate = delegate

        return filtri
    }

    @Override
    CampiRicerca getCampiRicerca () {
        CampiRicerca campiRicerca = new CampiRicerca()
        campiRicerca.filtri = [:]
        campiRicerca.filtri.LISTA_REGISTRI = getRegistri()
        campiRicerca.filtri.LISTA_ESITI = getEsiti()
        return campiRicerca
    }

    @Override
    DocumentoCollegatoDTO creaDocumentoCollegato (DocumentoEsterno documentoEsterno, String operazione) {
        return new DocumentoCollegatoDTO(deliberaCollegata: new DeliberaDTO(id: documentoEsterno.idDocumento), operazione: operazione)
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
