package it.finmatica.atti.integrazioni.ws.dati

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlTransient
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@GrailsCompileStatic
class Soggetto {

    @XmlElement(required = false, nillable = true)
	String utenteAd4	// è il nominativo dell'utente ad4

    @XmlElement(required = false, nillable = true)
	String niAs4

    @XmlElement(required = false, nillable = true)
	String nome

    @XmlElement(required = false, nillable = true)
	String cognome

    @XmlElement(required = false, nillable = true)
	String codiceFiscale

    @XmlElement(required = false, nillable = true)
	String tipo

    @XmlElement(required = false, nillable = true)
	UnitaOrganizzativa unita

	@XmlTransient
	Ad4Utente getUtente () {
		if (utenteAd4?.trim()?.length() > 0) {
			return Ad4Utente.findByNominativoIlike(utenteAd4)
		}

		if (niAs4?.trim()?.length() > 0) {
			return As4SoggettoCorrente.get(niAs4)?.utenteAd4
		}

		if (codiceFiscale?.trim()?.length() > 0) {
			return As4SoggettoCorrente.findByCodiceFiscale(codiceFiscale)?.utenteAd4
		}
	}
}
