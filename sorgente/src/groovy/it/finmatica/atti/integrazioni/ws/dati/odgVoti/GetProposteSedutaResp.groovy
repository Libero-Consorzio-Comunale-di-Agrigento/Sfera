package it.finmatica.atti.integrazioni.ws.dati.odgVoti


import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name="getProposteSedutaResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class GetProposteSedutaResp {

	@XmlElement(required = false, nillable = true)
	SedutaProposta seduta

	@XmlElement(required = false, nillable = true)
	Esito esito
}
