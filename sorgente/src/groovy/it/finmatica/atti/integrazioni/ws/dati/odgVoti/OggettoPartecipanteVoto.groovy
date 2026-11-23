package it.finmatica.atti.integrazioni.ws.dati.odgVoti

import javax.xml.bind.annotation.*

@XmlRootElement(name="dettaglio_voti")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class OggettoPartecipanteVoto {
	@XmlElement(required = false, nillable = true)
	Long niSoggetto

	@XmlElement(required = false, nillable = true)
	String codiceFiscale

	@XmlElement(required = false, nillable = true)
	Boolean presente

	@XmlElement(required = false, nillable = true)
	String voto
}
