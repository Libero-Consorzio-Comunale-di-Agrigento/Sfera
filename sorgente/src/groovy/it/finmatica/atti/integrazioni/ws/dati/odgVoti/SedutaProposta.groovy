package it.finmatica.atti.integrazioni.ws.dati.odgVoti


import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class SedutaProposta {

        @XmlElement(required = false, nillable = true)
        Long idSeduta

        @XmlElement(required = false, nillable = true)
        String idRiferimento  // id del documento dell'applicativo esterno

        @XmlElement(required = false, nillable = true)
        String commissione

        @XmlElement(required = false, nillable = true)
        Integer	anno

        @XmlElement(required = false, nillable = true)
        Integer numero

        @XmlElement(required = false, nillable = true)
        List<OggettoSedutaProposta> odg
}
