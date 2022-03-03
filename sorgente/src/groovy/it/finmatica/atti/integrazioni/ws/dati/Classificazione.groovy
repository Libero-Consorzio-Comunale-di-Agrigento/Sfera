package it.finmatica.atti.integrazioni.ws.dati

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlTransient
import javax.xml.bind.annotation.XmlType

/**
 * Created by esasdelli on 01/12/2017.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class Classificazione {
    @XmlElement(required = false, nillable = true)
    String codice

    @XmlElement(required = false, nillable = true)
    String descrizione

    @XmlElement(required = false, nillable = true)
    Date dataValidita

    @XmlTransient
    Long getAnnoClassificazione () {
        return dataValidita?.getAt(Calendar.YEAR)
    }

    @XmlTransient
    Long getNumeroClassificazione () {
        try {
            return Long.parseLong(codice)
        } catch (NumberFormatException e) {
            return null
        }
    }
}
