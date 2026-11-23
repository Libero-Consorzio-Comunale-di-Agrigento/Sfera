package it.finmatica.atti.integrazioni.ws.dati.odgVoti


import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name="esito")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class Esito {
//	SUCCESSO(0, "Esito positivo dell’operazione richiesta."),
//	ERRORE_INTERNO(-1, "Errore nel codice del Web Services."),
//	ERRORE_PARAMETRI(1, "Errore nella richiesta: mancano dei parametri."),
//	ERRORE_SEDUTA(2, "Nessuna seduta trovata con i parametri specificati."),

	@XmlElement(required = false, nillable = true)
	Integer codice

	@XmlElement(required = false, nillable = true)
	String messaggioErrore
}
