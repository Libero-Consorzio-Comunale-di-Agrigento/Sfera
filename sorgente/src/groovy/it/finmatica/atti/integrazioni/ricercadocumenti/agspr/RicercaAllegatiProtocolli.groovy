package it.finmatica.atti.integrazioni.ricercadocumenti.agspr

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdmCondition
import org.springframework.transaction.annotation.Transactional
import groovy.sql.Sql
import it.finmatica.atti.documenti.beans.GdmDocumentaleEsterno
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.ricercadocumenti.AllegatoEsterno
import it.finmatica.atti.integrazioni.ricercadocumenti.CampiRicerca
import it.finmatica.atti.integrazioni.ricercadocumenti.PagedList
import it.finmatica.atti.integrazioni.ricercadocumenti.RicercaAllegatiDocumentiEsterni
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Lazy
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

import javax.sql.DataSource

/**
 * Created by dscandurra on 15/11/2017.
 */
@Lazy
@Component
@Conditional(ProtocolloGdmCondition)
@Order(3)
@Transactional(readOnly = true)
class RicercaAllegatiProtocolli implements RicercaAllegatiDocumentiEsterni {

    @Autowired
    private DataSource dataSource_gdm

    @Autowired
    private SpringSecurityService springSecurityService

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
        return "Import Allegati"
    }

    @Override
    String getZulCampiRicerca () {
        return "/atti/integrazioni/ricercaDocumenti/agspr/campiRicerca.zul"
    }

    @Override
    PagedList<AllegatoEsterno> ricerca (CampiRicerca campiRicerca) {
        Sql sql = new Sql(dataSource_gdm)

        String select = """   select descrizione_tipo_registro,
                                     anno,
                                     numero,
                                     to_char (p.data, 'dd/mm/yyyy') data,
                                     alh.ogfi_id_documento id_documento_esterno,
                                     alh.id_oggetto_file id_file_esterno,
                                     p.oggetto,
                                     alh.ogfi_id_documento id_documento,
                                     alh.id_oggetto_file id_oggetto_file,
                                     alh.filename nome_file,
                                     alh.nome_formato formato_file
                                from (select data,
                                             anno,
                                             numero,
                                             descrizione_tipo_registro,
                                             oggetto,
                                             id_documento
                                        from (select p.data,
                                                     p.anno,
                                                     p.numero,
                                                     p.tipo_registro,
                                                     p.descrizione_tipo_registro,
                                                     p.oggetto,
                                                     p.id_documento
                                                from proto_view p
                                               where ( :anno is null or p.anno = :anno)
                                                 and (numero between :numero_dal and :numero_al)
                                                 and (p.data between :data_dal and :data_al)
                                                 and p.oggetto like decode ( :oggetto, '', p.oggetto, '%' || upper ( :oggetto) || '%')
                                                 and p.tipo_registro like decode (upper ( :tipo_registro), '', p.tipo_registro, upper ( :tipo_registro))
                                                 and p.modalita like decode (upper ( :modalita), '', p.modalita, upper ( :modalita))
                                                 and nvl (p.tipo_documento, ' ') like
                                                        decode (upper ( :tipo_documento), '', nvl (p.tipo_documento, ' '), upper ( :tipo_documento))
                                              union all
                                              select to_date (null),
                                                     to_number (null),
                                                     to_number (null),
                                                     to_char (null),
                                                     to_char (null),
                                                     to_char (null),
                                                     to_number (null)
                                                from dual) prot,
                                             dual
                                       where    gdm_competenza.gdm_verifica ('DOCUMENTI',
                                                                             prot.id_documento,
                                                                             'L',
                                                                             :utente,
                                                                             'GDM')
                                             || dummy = '1X') p,
                                     allegati_lista_html alh,
                                     documenti d
                               where alh.id_documento = p.id_documento and d.stato_documento = 'BO' and d.id_documento = p.id_documento
                            order by anno desc, numero asc """
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
                      firstRow      : campiRicerca.startFrom]

        String sqlPaging = "SELECT * FROM ( SELECT tmp.*, rownum rn FROM ( ${select} ) tmp WHERE rownum <= :maxRows ) WHERE rn > :firstRow"
        def result = sql.rows(sqlPaging, params)
        List<AllegatoEsterno> allegati = result.collect {
            new AllegatoEsterno(idDocumentoPrincipale: it.ID_DOCUMENTO_ESTERNO,
                                tipoDocumento: "PROTOCOLLO",
                                idFileEsterno: it.ID_FILE_ESTERNO,
                                idFileAllegato: it.ID_OGGETTO_FILE,
                                nome: it.NOME_FILE,
                                contentType: "application/octet-stream",
                                formatoFile: it.FORMATO_FILE,
                                estremi: "${it.DESCRIZIONE_TIPO_REGISTRO} - ${it.NUMERO} / ${it.ANNO} - del ${it.DATA}",
                                oggetto: it.OGGETTO)
        }

        String sqlCount = "select count(1) total_count from (${select})"
        int totalCount = sql.rows(sqlCount, params)[0].TOTAL_COUNT

        return new PagedList<AllegatoEsterno>(allegati, totalCount)
    }

    @Override
    CampiRicerca getCampiRicerca () {
        String ente = springSecurityService.principal.amministrazione.codice
        return new CampiRicerca(filtri: [LISTA_TIPI_MODALITA: getTipiModalita(ente), LISTA_TIPI_REGISTRO: getTipiRegistro(
                ente), LISTA_TIPI_DOCUMENTO                 : getTipiDocumento(ente)])
    }

    private List<Map> getTipiModalita (String ente) {
        return new Sql(dataSource_gdm).rows(
                "select '' codice, '--' descrizione ,0 ord from dual union select tipo_movimento codice, movimento descrizione,1 ord from seg_movimenti where codice_amministrazione = :ente order by ord,descrizione asc",
                [ente: ente]).collect { [codice: it.CODICE, descrizione: it.DESCRIZIONE] }
    }

    private List<Map> getTipiRegistro (String ente) {
        return new Sql(dataSource_gdm).rows(
                "select '' codice, '--' descrizione ,0 ord from dual union select tipo_registro codice, max(descrizione_tipo_registro) descrizione,1 ord from seg_registri r, documenti d where d.id_documento = r.id_documento and d.stato_documento = 'BO' and r.in_uso = 'Y' and r.codice_amministrazione = :ente group by tipo_registro order by ord, descrizione asc",
                [ente: ente]).collect { [codice: it.CODICE, descrizione: it.DESCRIZIONE] }
    }

    private List<Map> getTipiDocumento (String ente) {
        return new Sql(dataSource_gdm).rows(
                "select '' codice, '--' descrizione ,0 ord from dual union select tipo_documento codice, descrizione_tipo_documento descrizione,1 ord from seg_tipi_documento r, documenti d where d.id_documento = r.id_documento and d.stato_documento = 'BO' and r.codice_amministrazione = :ente order by ord,descrizione asc",
                [ente: ente]).collect { [codice: it.CODICE, descrizione: it.DESCRIZIONE] }
    }

}
