package it.finmatica.atti.integrazioni.ws.dati.odgVoti


import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name="odg")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class OggettoSedutaProposta {
    @XmlElement(required = false, nillable = true)
    Long idOggetto
    @XmlElement(required = false, nillable = true)
    String idRiferimento  // id del documento dell'applicativo esterno

    @XmlElement(required = false, nillable = true)
    String oggettoEsteso

    @XmlElement(required = false, nillable = true)
    Integer numeroProposta

    @XmlElement(required = false, nillable = true)
    Integer annoProposta

    @XmlElement(required = false, nillable = true)
    Long idProposta
}
