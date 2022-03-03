package it.finmatica.atti.integrazioni.ws.dati

import javax.xml.bind.annotation.*

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class TipoRegistro {

    @XmlElement(required = false, nillable = true)
    String codice

    @XmlElement(required = false, nillable = true)
    String descrizione
}