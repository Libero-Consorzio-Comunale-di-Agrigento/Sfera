package it.finmatica.atti.integrazioni.ws.dati.odgVoti

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class SedutaPropostaExt extends SedutaProposta {

    @XmlElement(required = false, nillable = true)
    String dataConvocazione

    @XmlElement(required = false, nillable = true)
    String oraConvocazione
}
