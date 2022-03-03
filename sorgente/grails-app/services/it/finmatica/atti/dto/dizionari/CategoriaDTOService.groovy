package it.finmatica.atti.dto.dizionari


import it.finmatica.atti.dizionari.Categoria
import it.finmatica.atti.exceptions.AttiRuntimeException


class CategoriaDTOService {

    public CategoriaDTO salva (CategoriaDTO categoriaDto) {
		Categoria categoria = Categoria.get(categoriaDto.id)?:new Categoria()
		categoria.codice = categoriaDto.codice
		categoria.descrizione = categoriaDto.descrizione
		categoria.sequenza = categoriaDto.sequenza
		categoria.tipoOggetto = categoriaDto.tipoOggetto
		categoria.controlloCdv = categoriaDto.controlloCdv
		categoria.valido = categoriaDto.valido
		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		if(categoria.version != categoriaDto.version) throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		categoria = categoria.save ()

		return 	categoria.toDTO()
    }

	public void elimina (CategoriaDTO categoriaDto) {
		Categoria categoria = Categoria.get(categoriaDto.id)
		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		if(categoria.version != categoriaDto.version) throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		categoria.delete(failOnError: true)
	}

}
