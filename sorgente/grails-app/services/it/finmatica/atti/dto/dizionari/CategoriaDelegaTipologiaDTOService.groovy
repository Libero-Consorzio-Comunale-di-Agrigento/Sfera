package it.finmatica.atti.dto.dizionari

import it.finmatica.atti.dizionari.CategoriaDelega
import it.finmatica.atti.dizionari.CategoriaDelegaTipologia
import it.finmatica.atti.dto.dizionari.CategoriaDelegaTipologiaDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.sql.DataSource

class CategoriaDelegaTipologiaDTOService {

	DataSource        dataSource
	GrailsApplication grailsApplication

	CategoriaDelegaTipologiaDTO salva (CategoriaDelegaTipologiaDTO categoriaDelegaTipologiaDto) {
		CategoriaDelegaTipologia categoriaDelegaTipologia = CategoriaDelegaTipologia.findByIdCategoriaDelegaAndIdTipoDelibera(categoriaDelegaTipologiaDto.idCategoriaDelega, categoriaDelegaTipologiaDto.idTipoDelibera)?:new CategoriaDelegaTipologia()
		categoriaDelegaTipologia.idCategoriaDelega=categoriaDelegaTipologiaDto.idCategoriaDelega
		categoriaDelegaTipologia.idTipoDelibera=categoriaDelegaTipologiaDto.idTipoDelibera
		categoriaDelegaTipologia.save (failOnError: true)

		return 	categoriaDelegaTipologia.toDTO()
    }

	void elimina (CategoriaDelegaTipologiaDTO categoriaDelegaTipologiaDto) {
		CategoriaDelegaTipologia categoriaDelegaTipologia = CategoriaDelegaTipologia.findByIdCategoriaDelegaAndIdTipoDelibera(categoriaDelegaTipologiaDto.idCategoriaDelega, categoriaDelegaTipologiaDto.idTipoDelibera)?:new CategoriaDelegaTipologia()
		categoriaDelegaTipologia.delete(failOnError: true)
	}
}
