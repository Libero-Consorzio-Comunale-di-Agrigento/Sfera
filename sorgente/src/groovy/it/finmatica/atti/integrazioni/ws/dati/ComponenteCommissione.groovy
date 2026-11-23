package it.finmatica.atti.integrazioni.ws.dati

import grails.compiler.GrailsCompileStatic

import javax.xml.bind.annotation.*

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@GrailsCompileStatic
class ComponenteCommissione {

    @XmlElement(required = false, nillable = true)
	String ni

    @XmlElement(required = false, nillable = true)
	String nome

    @XmlElement(required = false, nillable = true)
	String cognome

    @XmlElement(required = false, nillable = true)
	String codiceFiscale

    @XmlElement(required = false, nillable = true)
	Integer sequenza

    @XmlElement(required = false, nillable = true)
	boolean firmatario

	@XmlElement(required = false, nillable = true)
	String ruolo

	public Integer getSequenza(){
		return sequenza
	}

}
