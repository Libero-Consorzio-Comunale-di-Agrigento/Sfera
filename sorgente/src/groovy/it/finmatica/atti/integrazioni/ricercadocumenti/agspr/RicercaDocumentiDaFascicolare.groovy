package it.finmatica.atti.integrazioni.ricercadocumenti.agspr

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdmCondition
import org.springframework.context.annotation.Conditional
import org.springframework.transaction.annotation.Transactional
import groovy.sql.Sql
import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.documenti.beans.GdmDocumentaleEsterno
import it.finmatica.atti.dto.documenti.DocumentoCollegatoDTO
import it.finmatica.atti.dto.documenti.RiferimentoEsternoDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.ricercadocumenti.CampiRicerca
import it.finmatica.atti.integrazioni.ricercadocumenti.DocumentoEsterno
import it.finmatica.atti.integrazioni.ricercadocumenti.PagedList
import it.finmatica.atti.integrazioni.ricercadocumenti.RicercaDocumentiEsterni
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

import javax.sql.DataSource

/**
 * Created by dscandurra on 27/11/2017.
 */
@Lazy
@Component
@Conditional(ProtocolloGdmCondition)
@Order(4)
@Transactional(readOnly = true)
class RicercaDocumentiDaFascicolare implements RicercaDocumentiEsterni {

    @Autowired
    private DataSource dataSource_gdm

    @Autowired
    private SpringSecurityService springSecurityService

    @Autowired
    private IProtocolloEsterno protocolloEsterno

    @Override
    boolean isAbilitato () {
        return Impostazioni.PROTOCOLLO.valore == "protocolloEsternoGdm"
    }

    @Override
    String getTitolo () {
        return "Documenti da fascicolare"
    }

    @Override
    String getDescrizione () {
        return "Ricerca Documenti da fascicolare su AGSPR"
    }

    @Override
    String getZulCampiRicerca () {
        return "/atti/integrazioni/ricercaDocumenti/agspr/campiRicercaDocumenti.zul"
    }

    @Override
    PagedList<DocumentoEsterno> ricerca (CampiRicerca campiRicerca) {
        Sql sql = new Sql(dataSource_gdm)

        String select = """ SELECT id id_documento,
                                   ti tipo_documento,
                                   to_char(a.data,'dd/mm/yyyy') data,
                                   codice_amministrazione,
                                   codice_aoo,
                                   class_cod,
                                   fascicolo_anno,
                                   fascicolo_numero,
                                   oggetto,
                                   categoria
                              FROM mail_classificabili_view a
                             WHERE NVL (a.codice_amministrazione, :codice_amm) = :codice_amm
                                   and NVL (a.codice_aoo, :codice_aoo) = :codice_aoo
                                   and (a.data between :data_dal and :data_al)                                  
                                   and (:class_cod is null or (a.class_cod = :class_cod))
                                   and (:fascicolo_anno is null or (a.fascicolo_anno = :fascicolo_anno))
                                   and (:fascicolo_numero is null or (a.fascicolo_anno = :fascicolo_numero))
                                   and (:oggetto is null or upper (a.oggetto) like '%' || upper (:oggetto) || '%')
                                   and (:categoria is null or a.categoria = :categoria)"""

        String sqlCompetenze = """  SELECT id_documento,tipo_documento,data,class_cod,fascicolo_anno,fascicolo_numero,oggetto,categoria
                                    FROM (${select}) p
                                    WHERE gdm_competenza.gdm_verifica ('DOCUMENTI', p.id_documento, 'L', :utente, 'GDM') = 1
                                    """

        Map params = [
                codice_amm      : springSecurityService.principal.amministrazione.codice,
                codice_aoo      : Impostazioni.CODICE_AOO.valore,
                data_dal        : new java.sql.Date(
                        (campiRicerca.filtri.DATA_DAL ?: new Date().clearTime().copyWith(year: 1800, month: 0, date: 1)).getTime()),
                data_al         : new java.sql.Date(
                        (campiRicerca.filtri.DATA_AL ?: new Date().clearTime().copyWith(year: 2800, month: 0, date: 1)).getTime()),
                class_cod       : campiRicerca.filtri.CLASSIFICA ?: "",
                fascicolo_anno  : campiRicerca.filtri.FASCICOLO_ANNO ?: "",
                fascicolo_numero: campiRicerca.filtri.FASCICOLO_NUMERO ?: "",
                oggetto         : campiRicerca.filtri.OGGETTO ?: "",
                categoria       : campiRicerca.filtri.CATEGORIA?.codice ?: "",
                utente          : springSecurityService.currentUser.id,
                maxRows         : campiRicerca.startFrom + campiRicerca.maxResults,
                firstRow        : campiRicerca.startFrom,
                area            : GdmDocumentaleEsterno.AREA]

        if (!(params.fascicolo_anno.trim().length() > 0) &&
            !(params.fascicolo_numero.trim().length() > 0) &&
            !(params.oggetto.trim().length() > 0) &&
            !(params.categoria.trim().length() > 0) &&
            !(params.class_cod.trim().length() > 0)) {
            throw new AttiRuntimeException("È necessario specificare almeno un criterio di ricerca.")
        }

        String sqlPaging = "SELECT * FROM ( SELECT tmp.*, rownum rn FROM ( ${sqlCompetenze} ) tmp WHERE rownum <= :maxRows ) WHERE rn > :firstRow"
        def result = sql.rows(sqlPaging, params)
        List<DocumentoEsterno> documenti = result.collect {
            String estremi = ""
            if (it.CATEGORIA.equals("POSTA_ELETTRONICA")) {
                estremi = "Mail Mittente - Data Ricezione ${it.DATA}"
            } else {
                estremi = "Documento del ${it.DATA}"
            }

            new DocumentoEsterno(idDocumentoEsterno: it.ID_DOCUMENTO, oggetto: it.OGGETTO, estremi: estremi)
        }

        String sqlCount = "select count(1) total_count from (${sqlCompetenze})"
        int totalCount = sql.rows(sqlCount, params)[0].TOTAL_COUNT

        return new PagedList<DocumentoEsterno>(documenti, totalCount)
    }

    @Override
    CampiRicerca getCampiRicerca () {
        return new CampiRicerca(filtri: [LISTA_CATEGORIA: getCategoria()])
    }

    private List<Map> getCategoria () {
        return new Sql(dataSource_gdm).rows(
                "select '' codice,'--' descrizione,0 ord from dual union select categoria, descrizione,1 ord from categorie where categoria in ('CLASSIFICABILE','POSTA_ELETTRONICA') order by ord,descrizione asc").collect {
            [codice: it.CODICE, descrizione: it.DESCRIZIONE]
        }
    }

    @Override
    DocumentoCollegatoDTO creaDocumentoCollegato (DocumentoEsterno documentoEsterno, String operazione) {
        return new DocumentoCollegatoDTO(operazione: operazione,
                                         riferimentoEsternoCollegato: new RiferimentoEsternoDTO(
                                                 titolo: "${documentoEsterno.estremi} - ${documentoEsterno.oggetto}",
                                                 idDocumentoEsterno: documentoEsterno.idDocumentoEsterno,
                                                 codiceDocumentaleEsterno: GdmDocumentaleEsterno.CODICE_DOCUMENTALE_ESTERNO,
                                                 tipoDocumento: "PROTOCOLLO"))
    }
}
