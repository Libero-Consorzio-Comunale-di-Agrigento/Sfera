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

        String select = """  SELECT  descrizione_tipo_registro,
                                         anno,
                                         numero,
                                         TO_CHAR (p.data, 'dd/mm/yyyy') data,
                                         d.id_documento
                                            id_documento_esterno,
                                         ogfi.ID_OGGETTO_FILE id_file_esterno,
                                         ogfi.ID_OGGETTO_FILE id_oggetto_file,
                                         ogfi.FILENAME nome_file,
                                         fofi.nome formato_file,
                                         p.oggetto
                                    FROM (SELECT data,
                                                 anno,
                                                 numero,
                                                 descrizione_tipo_registro,
                                                 oggetto,
                                                 id_documento
                                            FROM (SELECT p.data,
                                                         p.anno,
                                                         p.numero,
                                                         p.tipo_registro,
                                                         p.descrizione_tipo_registro,
                                                         p.oggetto,
                                                         p.id_documento
                                                    FROM proto_view p, documenti d
                                                   WHERE     p.id_documento = d.id_documento
                                                         AND ( :anno IS NULL OR p.anno = :anno)
                                                         AND (numero BETWEEN :numero_dal AND :numero_al)
                                                         AND (p.data BETWEEN :data_dal AND :data_al)
                                                         AND p.oggetto LIKE decode(:oggetto, '', p.oggetto,  '%'||UPPER(:oggetto)||'%') 
                                                         AND p.tipo_registro LIKE decode(UPPER(:tipo_registro), '', p.tipo_registro, UPPER(:tipo_registro))
                                                         AND p.modalita LIKE decode(UPPER(:modalita), '', p.modalita, UPPER(:modalita))
                                                         AND NVL(p.tipo_documento, ' ') LIKE decode(UPPER(:tipo_documento), '', NVL(p.tipo_documento, ' '), UPPER(:tipo_documento))
                                                     
                                                  UNION ALL
                                                  SELECT TO_DATE (NULL),
                                                         TO_NUMBER (NULL),
                                                         TO_NUMBER (NULL),
                                                         TO_CHAR (NULL),
                                                         TO_CHAR (NULL),
                                                         TO_CHAR (NULL),
                                                         TO_NUMBER (NULL)
                                                    FROM DUAL) p,
                                                 DUAL
                                           WHERE    gdm_competenza.gdm_verifica ('DOCUMENTI',
                                                                                 p.id_documento,
                                                                                 'L',
                                                                                 :utente,
                                                                                 'GDM')
                                                 || dummy = '1X') p,
                                                 oggetti_file ogfi,
                                                 formati_file fofi,
                                                 documenti d,
                                                 modelli m
                                           WHERE     ogfi.id_formato = fofi.id_formato
                                                 AND fofi.visibile = 'S'
                                                 AND d.id_documento = ogfi.id_documento
                                                 AND d.id_tipodoc = m.id_tipodoc
                                                 AND d.stato_documento NOT IN ('CA', 'RE', 'PB')
                                                 AND ogfi.id_documento = d.id_documento
                                                 and (d.id_documento = p.id_documento
                                                 or d.id_documento_padre = p.id_documento)
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
