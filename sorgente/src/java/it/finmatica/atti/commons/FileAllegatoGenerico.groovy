package it.finmatica.atti.commons

import it.finmatica.atti.IFileAllegato
import it.finmatica.dto.DTO
import org.apache.commons.io.FilenameUtils

/**
 * Created by czappavigna on 19/09/2017.
 */
class FileAllegatoGenerico implements IFileAllegato, DTO<FileAllegatoGenerico> {
    String	nome
    byte[]	allegato
    String	contentType
    long 	dimensione = -1
    String 	testo
    Long id

    // indica se il file è stato firmato dall'applicativo. Questo è sufficiente fintanto che si passa dalla domain Allegato che invece mantiene
    // lo statoFirma. Quindi se allegato.statoFirma = FIRMATO ma file.firmato = false, allora il file è stato caricato già firmato.
    boolean	firmato 	 		 = false;

    // indica se il documento è un testo ancora modificabile dall'utente o no.
    boolean modificabile 		 = false;

    Long idFileEsterno	// indica l'id del file se salvato su un repository esterno (ad es GDM)

    transient boolean isPdf () {
        return this.nome.toLowerCase().endsWith("pdf");
    }

    transient boolean isP7m () {
        return this.nome.toLowerCase().endsWith("p7m");
    }

    transient String getNomeFileSbustato () {
        return this.nome.replaceAll(/(\.[pP]7[mM])+$/, "")
    }

    transient String getNomePdf () {
        return this.nome.replaceAll(/\..+$/, ".pdf")
    }

    transient String getEstensione () {
        return FilenameUtils.getExtension(this.nome).toLowerCase()
    }

    public DTO<FileAllegatoGenerico> toDTO (){
        return this
    }

    @Override
    FileAllegatoGenerico getDomainObject() {
        return this
    }

    @Override
    FileAllegatoGenerico copyToDomainObject() {
        return null
    }

    @Override
    public String getNomeFileOriginale(){
        return this.nome
    }

}
