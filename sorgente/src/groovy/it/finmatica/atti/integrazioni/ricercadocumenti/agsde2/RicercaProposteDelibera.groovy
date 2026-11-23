package it.finmatica.atti.integrazioni.ricercadocumenti.agsde2

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.competenze.DeterminaCompetenze
import it.finmatica.atti.documenti.competenze.PropostaDeliberaCompetenze
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.documenti.DeterminaDTO
import it.finmatica.atti.dto.documenti.DocumentoCollegatoDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoDeliberaDTO
import it.finmatica.atti.integrazioni.ricercadocumenti.CampiRicerca
import it.finmatica.atti.integrazioni.ricercadocumenti.DocumentoEsterno
import it.finmatica.atti.integrazioni.ricercadocumenti.PagedList
import it.finmatica.atti.integrazioni.ricercadocumenti.RicercaDocumentiEsterni
import org.hibernate.FetchMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Created by mfrancesconi on 20/10/2022.
 */
@Lazy
@Component
@Order(1)
@Transactional(readOnly = true)
class RicercaProposteDelibera implements RicercaDocumentiEsterni {

    @Autowired
    private AttiGestoreCompetenze gestoreCompetenze

    @Autowired
    private SpringSecurityService springSecurityService

    @Override
    boolean isAbilitato () {
        return true
    }

    @Override
    String getTipoOggetto () {
        return PropostaDelibera.TIPO_OGGETTO
    }

    @Override
    String getTitolo () {
        return "Proposte di Delibera"
    }

    @Override
    String getDescrizione () {
        return "Ricerca Proposte di Delibera su Sfera"
    }

    @Override
    String getZulCampiRicerca () {
        return "/atti/integrazioni/ricercaDocumenti/agsde2/campiRicercaProposteDelibera.zul"
    }

    @Override
    PagedList<DocumentoEsterno> ricerca (CampiRicerca campiRicerca) {
        // restituisce la lista contenente le proposte di delibera che fanno match
        def risultatiRicerca = PropostaDeliberaCompetenze.createCriteria().list() {

            projections {
                propostaDelibera {
                    groupProperty("id")
                    groupProperty("annoProposta")
                    groupProperty("numeroProposta")
                    groupProperty("oggetto")
                    groupProperty("idDocumentoEsterno")
                }
            }

            gestoreCompetenze.controllaCompetenze(delegate)(springSecurityService.principal)
            getFiltri(delegate)(campiRicerca)

            propostaDelibera {
                order("annoProposta", "asc")
                order("numeroProposta", "asc")
            }

            firstResult(campiRicerca.startFrom)
            maxResults(campiRicerca.maxResults)

        }.collect { row ->
            new DocumentoEsterno(idDocumento: row[0], estremi: "${row[2]} / ${row[1]}", oggetto: row[3], idDocumentoEsterno: row[4],
                                 tipoDocumento: PropostaDelibera.TIPO_OGGETTO)
        }

        // calcolo il totale dei record
        int totalCount = PropostaDeliberaCompetenze.createCriteria().get() {
            projections { countDistinct("propostaDelibera") }
            gestoreCompetenze.controllaCompetenze(delegate)(springSecurityService.principal)
            getFiltri(delegate)(campiRicerca)
        }

        return new PagedList<DocumentoEsterno>(risultatiRicerca, totalCount)
    }

    @Override
    CampiRicerca getCampiRicerca () {
        CampiRicerca campiRicerca = new CampiRicerca()
        campiRicerca.filtri = [:]
        campiRicerca.filtri.LISTA_TIPOLOGIE = getTipologie()
        return campiRicerca
    }

    @Override
    DocumentoCollegatoDTO creaDocumentoCollegato (DocumentoEsterno documentoEsterno, String operazione) {
        return new DocumentoCollegatoDTO(propostaDeliberaCollegata: new PropostaDeliberaDTO(id: documentoEsterno.idDocumento), operazione: operazione)
    }

    private Closure getFiltri (def delegate) {
        Closure filtri = { CampiRicerca campiRicerca ->
            propostaDelibera {
                if (campiRicerca.filtri.ANNO != null) {
                    eq("annoProposta", campiRicerca.filtri.ANNO)
                }
                if (campiRicerca.filtri.NUMERO != null) {
                    eq("numeroProposta", campiRicerca.filtri.NUMERO)
                }
                if (campiRicerca.filtri.TIPOLOGIA != null && campiRicerca.filtri.TIPOLOGIA.id > 0) {
                    eq("tipologia.id", campiRicerca.filtri.TIPOLOGIA.id)
                }
                if (campiRicerca.filtri.OGGETTO != null) {
                    ilike("oggetto", "%" + campiRicerca.filtri.OGGETTO + "%")
                }
            }
        }

        filtri.delegate = delegate

        return filtri
    }

    private List<TipoDeliberaDTO> getTipologie () {
        List<TipoDeliberaDTO> tipologie = TipoDelibera.createCriteria().list {
            eq("valido", true)
        }.toDTO()

        tipologie.add(0, new TipoDeliberaDTO(id: -1, descrizione: "-- nessuno --"))

        return tipologie
    }
}

