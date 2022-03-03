package it.finmatica.atti.commons

import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.Sql
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.sql.DataSource
import java.sql.Connection

class AttiAd4Service {
	DataSource ad4DataSource
	DataSource dataSource

	SpringSecurityService springSecurityService
	GrailsApplication grailsApplication

	void logAd4 (String note, String testoMsg) {
		String testo = testoMsg
		String istanza = grailsApplication.config.grails.plugins.amministrazionedatabase.istanza
		String modulo  = grailsApplication.config.grails.plugins.amministrazionedatabase.modulo
		String utente  = springSecurityService.getCurrentUser().id
		Connection conn = dataSource.connection
		String utenteDb = conn.metaData.userName
		Sql sql = new Sql(ad4DataSource)
		sql.call("{ $Sql.NUMERIC = call ad4_evento.INSERT_EVENTO ($testo, $utenteDb, to_char(sysdate, 'dd/mm/yyyy hh24:mi:ss'), 0, 'I', 'APPTRC', $note, $utente, $modulo, $istanza) }")
	}
}
