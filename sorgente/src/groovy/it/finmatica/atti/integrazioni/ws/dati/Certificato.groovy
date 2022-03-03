package it.finmatica.atti.integrazioni.ws.dati

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class Certificato {
    @XmlElement(required = false, nillable = true)
    Integer id
    @XmlElement(required = false, nillable = true)
    String  idRiferimento  // id del documento dell'applicativo esterno

    @XmlElement(required = false, nillable = true)
    String tipo // il tipo di certificato (PUBBLICAZIONE, ESECUTIVITA, etc..)

    @XmlElement(required = false, nillable = true)
    Soggetto        firmatario

    @XmlElement(required = false, nillable = true)
    RiferimentoFile testo
}
