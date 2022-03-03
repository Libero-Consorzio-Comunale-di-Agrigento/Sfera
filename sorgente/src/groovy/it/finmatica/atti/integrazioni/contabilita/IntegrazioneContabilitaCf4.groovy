package it.finmatica.atti.integrazioni.contabilita

import groovy.sql.Sql
import it.finmatica.atti.documenti.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.zkoss.bind.BindUtils
import org.zkoss.zk.ui.event.EventQueues

import javax.sql.DataSource

@Component("integrazioneContabilitaCf4")
@Lazy
class IntegrazioneContabilitaCf4 extends AbstractIntegrazioneContabilita {

	private static final String QUERY_MOVIMENTI_DETERMINA = """
select c.tipo
     , c.descrizione
     , c.importo
     , c.ragione_sociale
     , c.rif_bil_peg
     , c.anno
     , c.numero
  from cf4_vista_prop_del c, determine d, tipi_registro tr
 where d.id_determina = :id
   and d.registro_proposta = tr.tipo_registro
   and c.anno_prop = d.anno_proposta
   and c.numero_prop = d.numero_proposta
   and tr.registro_esterno = c.unita_prop
union
select c.tipo
     , c.descrizione
     , c.importo
     , c.ragione_sociale
     , c.rif_bil_peg
     , c.anno
     , c.numero
  from cf4_vista_prop_del c, determine d, tipi_registro tr
 where d.id_determina = :id
   and d.registro_determina = tr.tipo_registro
   and c.anno_del = d.anno_determina
   and c.numero_del = d.numero_determina
   and tr.registro_esterno = c.sede_del
order by anno, numero"""

	private static final String QUERY_MOVIMENTI_DELIBERA  = """
select c.tipo
     , c.descrizione
     , c.importo
     , c.ragione_sociale
     , c.rif_bil_peg
     , c.anno
     , c.numero
  from cf4_vista_prop_del 	c
     , proposte_delibera 	pd
     , tipi_registro 		tr
 where pd.id_proposta_delibera 	= :id_prop
   and pd.registro_proposta 	= tr.tipo_registro
   and c.anno_prop 				= pd.anno_proposta
   and c.numero_prop 			= pd.numero_proposta
   and tr.registro_esterno 		= c.unita_prop
 union
select c.tipo
     , c.descrizione
     , c.importo
     , c.ragione_sociale
     , c.rif_bil_peg
     , c.anno
     , c.numero
 from cf4_vista_prop_del c
    , delibere 			 d
    , tipi_registro 	 tr
where d.id_delibera 		= :id_deli
  and d.registro_delibera 	= tr.tipo_registro
  and c.anno_del 			= d.anno_delibera
  and c.numero_del 			= d.numero_delibera
  and tr.registro_esterno 	= c.sede_del
order by anno, numero"""

	@Autowired DataSource dataSource
	
	@Override
	String getZul(IDocumento documento) {
		return "/atti/integrazioni/contabilita/movimentiCf4.zul"
	}

	@Override
	boolean isConDocumentiContabili(IDocumento documento) {
		return false
	}

	@Override
	boolean isTipiDocumentoAbilitati() {
		return false
	}

	void aggiornaMaschera (IDocumento documento, boolean modifica) {
		BindUtils.postGlobalCommand("movimentiContabiliQueue", EventQueues.DESKTOP, "aggiornaAtto", [atto:documento, competenza:modifica?"W":"R"])
	}

	@Transactional(readOnly = true)
	List<?> getMovimentiContabili (IDocumento documento) {
		if (documento instanceof VistoParere) {
			documento = documento.documentoPrincipale
		}	
		
		if (documento instanceof Determina) {
			return caricaMovimenti (QUERY_MOVIMENTI_DETERMINA, [id: documento.id])
		}

		if (documento instanceof PropostaDelibera) {
			return caricaMovimenti (QUERY_MOVIMENTI_DELIBERA, [id_prop: documento.id, id_deli: documento.atto?.id?:-1]);
		}

		if (documento instanceof Delibera) {
			return caricaMovimenti (QUERY_MOVIMENTI_DELIBERA, [id_prop: documento.proposta.id, id_deli: documento.id]);
		}
	}
	
	/**
	 * Metodo che calcola i movimenti contabili (CF4) di una proposta di delibera o delibera.
	 * @param idProposta	id della proposta
	 * @param idDelibera	id della delibera
	 * @return				List dei movimenti contabili trovati
	 */
	private List<?> caricaMovimenti (String query, def parametri) {
		List listaMovimenti = [];
		Sql sql = new Sql (dataSource);
		def rows = sql.rows(query, parametri);
		for (def row : rows) {
			def movimento = [ tipo: 			row.tipo,
							  descrizione: 		row.descrizione,
							  importo: 			row.importo,
							  ragioneSociale: 	row.ragione_sociale,
							  rifBilPeg: 		row.rif_bil_peg,
							  anno: 			row.anno,
							  numero: 			row.numero]
			 listaMovimenti.add(movimento)
		}
		return listaMovimenti;
	}
}
