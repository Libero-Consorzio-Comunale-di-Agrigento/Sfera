package it.finmatica.atti.integrazioni.ws.dati

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class CampiRicerca {
    @XmlElement(required = true, nillable = false)
    String  tipo // DETERMINA o DELIBERA

    @XmlElement(required = false, nillable = true)
    String  oggetto

    @XmlElement(required = false, nillable = true)
    Integer numero

    @XmlElement(required = false, nillable = true)
    Integer anno

    @XmlElement(required = false, nillable = true)
    String  registro
}


