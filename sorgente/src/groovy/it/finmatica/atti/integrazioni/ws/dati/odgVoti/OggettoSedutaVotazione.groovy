package it.finmatica.atti.integrazioni.ws.dati.odgVoti

import javax.xml.bind.annotation.*

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class OggettoSedutaVotazione {
	@XmlElement(required = false, nillable = true)
	Long idOggetto

	@XmlElement(required = false, nillable = true)
	String idRiferimentoOggetto

	@XmlElement(required = false, nillable = true)
	Long tipoVoto

	@XmlElement(required = false, nillable = true)
	String dataOraVotazione

	@XmlElement(required = false, nillable = true)
	Long niPresidente

	@XmlElement(required = false, nillable = true)
	String cfPresidente

	@XmlElement(required = false, nillable = true)
	Long niSegretario

	@XmlElement(required = false, nillable = true)
	String cfSegretario

//	@XmlElement(required = false, nillable = true)
//	Integer totaliPresenti
//
//	@XmlElement(required = false, nillable = true)
//	Long totaliVotanti
//
//	@XmlElement(required = false, nillable = true)
//	Long totaliFavorevoli
//
//	@XmlElement(required = false, nillable = true)
//	Long totaliContrari
//
//	@XmlElement(required = false, nillaSble = true)
//	Long totaliAstenuti
//
//	@XmlElement(required = false, nillable = true)
//	Long totaliNonVotanti

	@XmlElement(required = false, nillable = true)
	Boolean immediataEseguibilita

	@XmlElement(required = false, nillable = true)
	String esito
}
