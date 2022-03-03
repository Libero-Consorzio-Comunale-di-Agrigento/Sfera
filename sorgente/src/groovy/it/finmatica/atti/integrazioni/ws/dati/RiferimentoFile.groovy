package it.finmatica.atti.integrazioni.ws.dati

import javax.activation.DataHandler
import javax.xml.bind.annotation.*

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class RiferimentoFile {

    // indica il tipo del documento padre che "possiede" questo file
    @XmlElement(required = false, nillable = true)
    String tipoDocumento

    // id del documento a cui questo file appartiene
    @XmlElement(required = false, nillable = true)
    Long idDocumento

    // id del file
    @XmlElement(required = false, nillable = true)
    Long idFile

    // nome del file da usare solo quando viene inviato un file
    @XmlElement(required = false, nillable = true)
    String nome

    @XmlElement(required = false, nillable = true)
    @XmlMimeType("application/octet-stream")
    DataHandler file
}

