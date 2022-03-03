package it.finmatica.atti.integrazioni.ws.dati

import javax.xml.bind.annotation.*

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class VistoParere {
    @XmlElement(required = false, nillable = true)
    Integer id

    @XmlElement(required = false, nillable = true)
    String  idRiferimento  // id del documento dell'applicativo esterno

    @XmlElement(required = false, nillable = true)
    Soggetto        firmatario

    @XmlElement(required = false, nillable = true)
    Soggetto        unita

    @XmlElement(required = false, nillable = true)
    RiferimentoFile testo // il testo principale

    @XmlElement(required = false, nillable = true)
    List<Allegato>  allegati // gli allegati del visto
}
