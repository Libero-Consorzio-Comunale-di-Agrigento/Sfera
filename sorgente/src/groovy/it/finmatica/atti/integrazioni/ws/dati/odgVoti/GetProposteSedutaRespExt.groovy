package it.finmatica.atti.integrazioni.ws.dati.odgVoti

import javax.xml.bind.annotation.*

@XmlRootElement(name="getProposteSedutaResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class GetProposteSedutaRespExt extends GetProposteSedutaResp {

	@XmlElement(required = false, nillable = true)
	SedutaPropostaExt sedutaExt
}
