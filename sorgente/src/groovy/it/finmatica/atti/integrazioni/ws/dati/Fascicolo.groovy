package it.finmatica.atti.integrazioni.ws.dati

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

/**
 * Created by esasdelli on 01/12/2017.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class Fascicolo {

    @XmlElement(required = false, nillable = true)
    String numero

    @XmlElement(required = false, nillable = true)
    int anno

    @XmlElement(required = false, nillable = true)
    String oggetto
}
