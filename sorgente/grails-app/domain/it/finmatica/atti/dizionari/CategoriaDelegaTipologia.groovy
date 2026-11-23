package it.finmatica.atti.dizionari

/**
 * Rappresenta il mapping tra CategoriaDelega e TipologieDelibere
 * @author amarinelli
 *
 */
class CategoriaDelegaTipologia implements Serializable{

	Long idCategoriaDelega
	Long idTipoDelibera

	static mapping = {
		table 'categorie_deleghe_tipologie'

		id composite: ['idCategoriaDelega', 'idTipoDelibera']

		version false

		columns {
			idCategoriaDelega column: 'id_categoria_delega'
			idTipoDelibera column: 'id_tipo_delibera'
		}
	}

}
