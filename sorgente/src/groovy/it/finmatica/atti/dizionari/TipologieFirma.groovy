package it.finmatica.atti.dizionari

import grails.compiler.GrailsCompileStatic

/**
 * Rappresenta le tipologie di firma gestite da applicativo
 *
 * Per questa domainClass non è prevista una interfaccia di gestione
 */
@GrailsCompileStatic
class TipologieFirma {
	public static final String PDF 			= "PDF"
	public static final String P7M 			= "P7M"
}
