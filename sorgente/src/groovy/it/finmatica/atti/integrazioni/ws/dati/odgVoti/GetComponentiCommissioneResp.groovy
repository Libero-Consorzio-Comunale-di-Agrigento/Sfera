package it.finmatica.atti.integrazioni.ws.dati.odgVoti

import it.finmatica.atti.integrazioni.ws.dati.ComponenteCommissione

import javax.xml.bind.annotation.*

@XmlRootElement(name="getComponentiCommissioneResp")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class GetComponentiCommissioneResp {

	@XmlElement(required = false, nillable = true)
	String commissione

	@XmlElement(required = false, nillable = true)
	String descrizione

	@XmlElement
	List<ComponenteCommissione> componenti

	@XmlElement
	long  totaleComponenti

}
