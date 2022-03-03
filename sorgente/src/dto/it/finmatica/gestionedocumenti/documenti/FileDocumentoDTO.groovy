package it.finmatica.gestionedocumenti.documenti

import it.finmatica.atti.dto.commons.FileAllegatoDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO

public class FileDocumentoDTO extends FileAllegatoDTO implements it.finmatica.dto.DTO<FileDocumento> {
    private static final long serialVersionUID = 1L;

    String codice;
    Integer sequenza;
    GestioneTestiModelloDTO modelloTesto;
    DocumentoDTO documento;
    Long revisione;

    public FileDocumento getDomainObject () {
        return FileDocumento.get(this.id)
    }

    public FileDocumento copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

	/* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
	// qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

	public String getDimensioneMB () {
		return new Double(((dimensione) / 1_000_000)).round(2) + " MB"
	}
}
