package it.finmatica.atti.integrazioni.protocollo

import groovy.sql.Sql
import it.finmatica.atti.AbstractProtocolloEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IFascicolabile
import it.finmatica.atti.documenti.IProtocollabile
import it.finmatica.atti.exceptions.AttiRuntimeException
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Lazy
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.sql.DataSource

@Conditional(ProtocolloGs4Condition)
@Component("protocolloEsternoGs4")
@Lazy
class ProtocolloGs4 extends AbstractProtocolloEsterno {

    static class ProtocolloGs4Condition implements Condition {

        @Override
        boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            try {
                BeanDefinition beanDefinition = conditionContext.getBeanFactory().getBeanDefinition("dataSource_gs4")
                return beanDefinition != null
            } catch (NoSuchBeanDefinitionException e) {
                return false
            }
        }
    }

    // questo bean va configurato solo dove viene realmente utilizzato aggiungendolo al file DataSource.groovy sotto confapps.
    @Autowired
    @Qualifier("dataSource_gs4")
    DataSource dataSource_gs4
    @Autowired
    IGestoreFile gestoreFile

    @Override
    void sincronizzaClassificazioniEFascicoli() {}

    @Override
    @Transactional
    void fascicola(IFascicolabile fascicolabile) {

        // questa funzione fascicola solo gli Atti e non le proposte.
        if (!(fascicolabile instanceof IAtto)) {
            return
        }

        IAtto atto = (IAtto) fascicolabile
        // se l'atto non ha classifica E fascicolo, non faccio niente:
        if (atto.classificaCodice == null || atto.classificaDal == null || atto.fascicoloNumero == null || atto.fascicoloAnno == null) {
            return
        }

        String query = """{ call
   ? := delibera_agsde2.crea (
   ?, --:p_anno              -- NUMBER,
   ?, --:p_tipo_registro     -- VARCHAR2,
   ?, --:p_numero            -- NUMBER,
   ?, --:p_data              -- VARCHAR2,
   ?, --:p_oggetto           -- VARCHAR2,
   ?, --:p_classificazione   -- VARCHAR2,
   ?, --:p_class_dal         -- VARCHAR2,
   ?, --:p_anno_cla          -- NUMBER,
   ?, --:p_numero_cla        -- VARCHAR2,
   ?, --:p_data_esec         -- VARCHAR2,
   ?, --:p_data_pubbl        -- VARCHAR2,
   ?, --:p_data_fine_pubbl   -- VARCHAR2,
   ?, --:p_data_pubbl2       -- VARCHAR2,
   ?, --:p_data_fine_pubbl2  -- VARCHAR2,
   ?, --:p_utente            -- VARCHAR2,
   ?, --:p_testo             -- BLOB,
   ?, --:p_formato           -- VARCHAR2 default 'P',
   ?, --:p_firmata           -- VARCHAR2 default 'Y',
   ?  --:p_riservato         -- VARCHAR2 default null
) }"""

        Sql sql = new Sql(dataSource_gs4)
        sql.call(query,
                [Sql.NUMERIC                                                                   // p_result
                 , atto.annoAtto                                                                // p_anno
                 , atto.registroAtto.codice                                                   // p_tipo_registro
                 , atto.numeroAtto                                                              // p_numero
                 , atto.dataAtto.format("dd/MM/yyyy")                                           // p_data
                 , atto.oggetto                                                                 // p_oggetto
                 , atto.classificaCodice                                                        // p_classificazione
                 , atto.classificaDal.format("dd/MM/yyyy")                                      // p_class_dal
                 , atto.fascicoloAnno                                                           // p_anno_cla
                 , atto.fascicoloNumero?.toString() ?: ""                                         // p_numero_cla
                 , atto.dataEsecutivita?.format("dd/MM/yyyy") ?: ""                               // p_data_esec
                 , atto.dataPubblicazione?.format("dd/MM/yyyy") ?: ""                             // p_data_pubbl
                 , atto.dataFinePubblicazione?.format("dd/MM/yyyy") ?: ""                         // p_data_fine_pubbl
                 , atto.dataPubblicazione2?.format("dd/MM/yyyy") ?: ""                            // p_data_pubbl2
                 , atto.dataFinePubblicazione2?.format("dd/MM/yyyy") ?: ""                        // p_data_fine_pubbl2
                 , null                                                                           // p_utente, passando "NULL" verrà usato l'utente di default GS4.
                 , IOUtils.toByteArray(gestoreFile.getFile(atto, atto.testo))                   // p_testo
                 , atto.testo.modificabile ? 'D' : 'P'                                          // p_formato
                 , atto.testo.firmato ? 'Y' : 'N'                                               // p_firmata
                 , atto.riservato ? 'Y' : 'N']) { risultato ->                                  // p_riservato

            // il risultato è sempre zero perché in caso di errore il pkg ritorna eccezione.
            if (risultato < 0) {
                throw new AttiRuntimeException("Errore n. ${risultato}")
            }
        }
    }

    @Override
    void protocolla(IProtocollabile atto) {
        throw new AttiRuntimeException("Non Implementato! Utilizzare il ProtocolloDOCArea.");
    }

    @Transactional(readOnly = true)
    List<Classifica> getListaClassificazioni(String filtro, String codiceUoProponente) {
        String query = """select * from  
							 (select c.classificazione codice
								   , c.descrizione descrizione
								   , c.dal
								from classificazioni_valide c
								   , installazione_parametri ip
								   , tipi_registro_all tr
							   where ip.parametro = 'REGIPROT'
								 and (upper(c.descrizione) like upper(:filtro) OR upper(c.classificazione) like upper(:filtro))
								 and tr.tipo_registro = ip.valore
								 and c.tipo_classificazione = tr.tipo_classificazione
							   order by codice, descrizione) 
							where rownum < 50""";
        Sql sql = new Sql(dataSource_gs4);
        def rows = sql.rows(query, [filtro: "%" + filtro + "%"]);
        def listaClassifiche = []
        for (def row : rows) {
            listaClassifiche << new Classifica(codice: row.codice, descrizione: row.descrizione, dal: row.dal);
        }
        return listaClassifiche;
    }

    @Transactional(readOnly = true)
    List<Fascicolo> getListaFascicoli(String filtro, String codiceClassifica, Date classificaDal, String codiceUoProponente) {
        String query = """select * from  
							 (SELECT c.classificazione, c.dal, c.descrizione
                                 , f.anno
                                 , f.numero||decode (f.sub, '0', '', '.'||f.sub) numero
                                 , f.oggetto
                                 , f.anno || '/' || f.numero descrizione_2
                              FROM fascicoli_validi f, classificazioni_valide c
                             WHERE f.data_chiusura IS NULL 
                               and c.classificazione = f.classificazione
                               and f.class_dal = c.dal
							   AND (NVL (UPPER (f.oggetto), ' ') LIKE UPPER (:filtro) 	OR 
									UPPER(f.classificazione) 	LIKE UPPER (:filtro) 	OR 
									UPPER(to_char(f.anno) )   	LIKE UPPER (:filtro)	OR 
									UPPER(to_char(f.numero)||decode (f.sub, '0', '', '.'||f.sub)) 	LIKE UPPER (:filtro))
							   and f.classificazione = nvl(:classifica, f.classificazione)
							 order by f.anno desc, f.classificazione asc,  f.numero asc, f.sub asc, f.oggetto asc) 
							where rownum < 100""";

        Sql sql = new Sql(dataSource_gs4);
        def rows = sql.rows(query, [filtro: "%" + filtro + "%", classifica: codiceClassifica]);
        def listaFascicoli = []
        for (def row : rows) {
            listaFascicoli << new Fascicolo(classifica: new Classifica(codice: row.classificazione, dal: row.dal, descrizione: row.descrizione)
                    , anno: row.anno
                    , numero: row.numero
                    , oggetto: row.oggetto);
        }
        return listaFascicoli;
    }
}
