package it.finmatica.atti.integrazioni.ws.dati.odgVoti

import javax.xml.bind.annotation.*

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class SedutaVotazione {
	@XmlElement(required = false, nillable = true)
	Long idSeduta

	@XmlElement(required = false, nillable = true)
	String idRiferimentoSeduta  // id del documento dell'applicativo esterno

	@XmlElement(required = false, nillable = true)
	String commissione

	@XmlElement(required = false, nillable = true)
	Integer	anno

	@XmlElement(required = false, nillable = true)
	Integer numero

	@XmlElement(required = false, nillable = true)
	OggettoSedutaVotazione oggettoSedutaVotazione

	@XmlElement(required = false, nillable = true)
	List<OggettoPartecipanteVoto> dettaglioVoti
}
