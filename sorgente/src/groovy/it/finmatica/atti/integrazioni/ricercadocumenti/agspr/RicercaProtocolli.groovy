package it.finmatica.atti.integrazioni.ricercadocumenti.agspr

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdmCondition
import org.springframework.beans.factory.annotation.Qualifier
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
 * Created by esasdelli on 02/10/2017.
 */
@Lazy
@Component
@Conditional(ProtocolloGdmCondition)
@Order(3)
@Transactional(readOnly = true)
class RicercaProtocolli implements RicercaDocumentiEsterni {

    @Autowired
    @Qualifier("dataSource_gdm")
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
        return "Protocolli"
    }

    @Override
    String getDescrizione () {
        return "Ricerca Protocolli su AGSPR"
    }

    @Override
    String getZulCampiRicerca () {
        return "/atti/integrazioni/ricercaDocumenti/agspr/campiRicerca.zul"
    }

    @Override
    PagedList<DocumentoEsterno> ricerca (CampiRicerca campiRicerca) {
        Sql sql = new Sql(dataSource_gdm)

        String select = """select p.anno,
       p.numero,
       p.tipo_registro,
       p.oggetto,
       p.id_documento
  from proto_view p, documenti d, tipi_documento td
 where p.id_documento = d.id_documento
   and TD.ID_TIPODOC = D.ID_TIPODOC
   and TD.AREA_MODELLO not in (:area)
   and (:anno is null or p.anno = :anno)
   and (numero between :numero_dal and :numero_al)
   and (p.data between :data_dal and :data_al)
   and (:oggetto is null or upper (p.oggetto) like '%' || upper (:oggetto) || '%')
   and (:tipo_registro is null or p.tipo_registro = :tipo_registro)
   and (:tipo_modalita is null or p.modalita = :tipo_modalita)
   and (:tipo_documento is null or p.tipo_documento = :tipo_documento)
   and codice_amministrazione = :codice_amm"""

        String sqlCompetenze = """SELECT anno, 
                         numero,
                         tipo_registro,
                         oggetto,
                         id_documento
                    FROM (${select}) p
                   WHERE gdm_competenza.gdm_verifica ('DOCUMENTI', p.id_documento, 'L', :utente, 'GDM') = 1
                ORDER BY anno DESC, numero ASC"""

        Map params = [anno          : campiRicerca.filtri.ANNO ?: "",
                      numero_dal    : campiRicerca.filtri.NUMERO_DAL ?: Integer.MIN_VALUE,
                      numero_al     : campiRicerca.filtri.NUMERO_AL ?: Integer.MAX_VALUE,
                      data_dal      : new java.sql.Date(
                              (campiRicerca.filtri.DATA_DAL ?: new Date().clearTime().copyWith(year: 1800, month: 0, date: 1)).getTime()),
                      data_al       : new java.sql.Date(
                              (campiRicerca.filtri.DATA_AL ?: new Date().clearTime().copyWith(year: 2800, month: 0, date: 1)).getTime()),
                      tipo_modalita : campiRicerca.filtri.TIPO_MODALITA?.codice ?: "",
                      tipo_registro : campiRicerca.filtri.TIPO_REGISTRO?.codice ?: "",
                      tipo_documento: campiRicerca.filtri.TIPO_DOCUMENTO?.codice ?: "",
                      oggetto       : campiRicerca.filtri.OGGETTO ?: "",
                      utente        : springSecurityService.currentUser.id,
                      codice_amm    : springSecurityService.principal.amministrazione.codice,
                      maxRows       : campiRicerca.startFrom + campiRicerca.maxResults,
                      firstRow      : campiRicerca.startFrom,
                      area          : GdmDocumentaleEsterno.AREA]

        String sqlPaging = "SELECT * FROM ( SELECT tmp.*, rownum rn FROM ( ${sqlCompetenze} ) tmp WHERE rownum <= :maxRows ) WHERE rn > :firstRow"
        def result = sql.rows(sqlPaging, params)
        List<DocumentoEsterno> documenti = result.collect {
            new DocumentoEsterno(idDocumentoEsterno: it.ID_DOCUMENTO, oggetto: it.OGGETTO, estremi: "${it.NUMERO} / ${it.ANNO} - ${it.TIPO_REGISTRO}")
        }

        String sqlCount = "select count(1) total_count from (${sqlCompetenze})"
        int totalCount = sql.rows(sqlCount, params)[0].TOTAL_COUNT

        return new PagedList<DocumentoEsterno>(documenti, totalCount)
    }

    @Override
    CampiRicerca getCampiRicerca () {
        String ente = springSecurityService.principal.amministrazione.codice
        return new CampiRicerca(filtri: [LISTA_TIPI_MODALITA: getTipiModalita(ente), LISTA_TIPI_REGISTRO: getTipiRegistro(
                ente), LISTA_TIPI_DOCUMENTO                 : getTipiDocumento(ente)])
    }

    private List<Map> getTipiModalita (String ente) {
        return new Sql(dataSource_gdm).rows(
                "select tipo_movimento codice, movimento descrizione from seg_movimenti where codice_amministrazione = :ente order by movimento asc",
                [ente: ente]).collect { [codice: it.CODICE, descrizione: it.DESCRIZIONE] }
    }

    private List<Map> getTipiRegistro (String ente) {
        return new Sql(dataSource_gdm).rows(
                "select tipo_registro codice, max(descrizione_tipo_registro) descrizione from seg_registri r, documenti d where d.id_documento = r.id_documento and d.stato_documento = 'BO' and r.in_uso = 'Y' and r.codice_amministrazione = :ente group by tipo_registro order by max(descrizione_tipo_registro) asc",
                [ente: ente]).collect { [codice: it.CODICE, descrizione: it.DESCRIZIONE] }
    }

    private List<Map> getTipiDocumento (String ente) {
        return new Sql(dataSource_gdm).rows(
                "select tipo_documento codice, descrizione_tipo_documento descrizione from seg_tipi_documento r, documenti d where d.id_documento = r.id_documento and d.stato_documento = 'BO' and r.codice_amministrazione = :ente order by descrizione_tipo_documento asc",
                [ente: ente]).collect { [codice: it.CODICE, descrizione: it.DESCRIZIONE] }
    }

    @Override
    DocumentoCollegatoDTO creaDocumentoCollegato (DocumentoEsterno documentoEsterno, String operazione) {
        return new DocumentoCollegatoDTO(operazione: operazione,
                                         riferimentoEsternoCollegato: new RiferimentoEsternoDTO(titolo: "${documentoEsterno.estremi} - ${documentoEsterno.oggetto}",
                                                                                                idDocumentoEsterno: documentoEsterno.idDocumentoEsterno,
                                                                                                codiceDocumentaleEsterno: GdmDocumentaleEsterno.CODICE_DOCUMENTALE_ESTERNO,
                                                                                                tipoDocumento: "PROTOCOLLO"))
    }
}
