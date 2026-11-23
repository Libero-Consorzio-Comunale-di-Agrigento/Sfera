package it.finmatica.atti.dto.dizionari

import groovy.sql.Sql
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.dizionari.CategoriaDelega
import it.finmatica.atti.dto.dizionari.CategoriaDelegaDTO
import it.finmatica.atti.documenti.ITipologia
import it.finmatica.atti.exceptions.AttiRuntimeException
import oracle.jdbc.OracleTypes
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.sql.DataSource
import java.sql.Connection

class CategoriaDelegaDTOService {

	DataSource        dataSource
	GrailsApplication grailsApplication

	CategoriaDelegaDTO salva (CategoriaDelegaDTO categoriaDelegaDto) {
		CategoriaDelega categoriaDelega = CategoriaDelega.get(categoriaDelegaDto.id)?:new CategoriaDelega()
		if(categoriaDelega.version != categoriaDelegaDto.version) {
			throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
		}
		categoriaDelega.titolo=categoriaDelegaDto.titolo?.toUpperCase()
		categoriaDelega.descrizione=categoriaDelegaDto.descrizione
		categoriaDelega.valido = categoriaDelegaDto.valido

		categoriaDelega.save (failOnError: true)

		return 	categoriaDelega.toDTO()
    }

	void elimina (CategoriaDelegaDTO categoriaDelegaDTO) {
		CategoriaDelega.get(categoriaDelegaDTO.id).delete(failOnError: true)
	}

	/*void inserisciTipologiaDelega (ITipologia tipologia, boolean update){
		try {
			Connection conn = dataSource.connection
			String modulo = grailsApplication.config.grails.plugins.amministrazionedatabase.modulo
			String istanza 	= grailsApplication.config.grails.plugins.amministrazionedatabase.istanza
			Sql sql = new Sql(conn)
			sql.call(update ? SO4_UPDATE : SO4_INSERT, [Sql.out(OracleTypes.VARCHAR), Long.toString(tipologia.id), tipologia.titolo, modulo, istanza])
		} catch (Exception e){
			log.error("Errore nella creazione/aggiornamento del tipo delega ${e.message}", e)
		}

	}

	void eliminaTipologiaDelega (ITipologia tipologia){
		try {
			Connection conn = dataSource.connection
			String modulo = grailsApplication.config.grails.plugins.amministrazionedatabase.modulo
			String istanza 	= grailsApplication.config.grails.plugins.amministrazionedatabase.istanza
			Sql sql = new Sql(conn)
			sql.call(SO4_DELETE, [Sql.out(OracleTypes.VARCHAR), Long.toString(tipologia.id), modulo, istanza])
		} catch (Exception e){
			log.error("Errore nell'eliminazione del tipo delega ${e.message}", e)
		}
	}*/
}
