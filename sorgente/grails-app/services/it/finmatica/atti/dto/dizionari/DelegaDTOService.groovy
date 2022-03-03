package it.finmatica.atti.dto.dizionari

import groovy.sql.Sql
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.dizionari.Delega
import it.finmatica.atti.documenti.ITipologia
import it.finmatica.atti.exceptions.AttiRuntimeException
import oracle.jdbc.OracleTypes
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.sql.DataSource
import java.sql.Connection

class DelegaDTOService {

	DataSource        dataSource
	GrailsApplication grailsApplication

	private static final String SO4_INSERT = "{ call ? := utility_pkg.inserisci_tipo_delega_so4(?, ?, ?, ?) }"
	private static final String SO4_UPDATE = "{ call ? := utility_pkg.aggiorna_tipo_delega_so4(?, ?, ?, ?) }"
	private static final String SO4_DELETE = "{ call ? := utility_pkg.rimuovi_tipo_delega_so4(?, ?, ?) }"

	DelegaDTO salva (DelegaDTO delegaDto) {
		Delega delega = Delega.get(delegaDto.id)?:new Delega()
		if(delega.version != delegaDto.version) {
			throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
		}
		delega.idDelegaStorico = delegaDto.idDelegaStorico
		delega.assessore = As4SoggettoCorrente.get(delegaDto.assessore?.id)
		delega.descrizioneAssessorato = delegaDto.descrizioneAssessorato
		delega.valido = delegaDto.valido

		// gestione delle deleghe storicizzate
		// nel caso di delega storicizzata viene disabilitata la delega originale
		// e viene salvata la nuova delega collegandola alla precedente
		if (delega.idDelegaStorico > 0) {
			Delega storico = Delega.get(delega.idDelegaStorico);

			// se viene salvata la stessa delega con gli stessi valori della delega originale non viene effettuato un nuovo inserimento
			if (delega.assessore?.id?.equals(storico.assessore?.id) && delega.descrizioneAssessorato?.equals(storico.descrizioneAssessorato)){
				return storico.toDTO()
			}

			// se la delega è già una storicizzazione riporto l'id della delega originale
			if (storico.idDelegaStorico > 0){
				delega.idDelegaStorico = storico.idDelegaStorico
			}

			// viene disabilitata la delega originale
			if (storico.valido){
				storico.valido = false;
				storico.save(failOnError: true)
			}
		}
		delega.save (failOnError: true)

		return 	delega.toDTO()
    }

	void elimina (DelegaDTO delegaDto) {
		Delega.get(delegaDto.id).delete(failOnError: true)
	}

	void inserisciTipologiaDelega (ITipologia tipologia, boolean update){
		try {
			Connection conn = dataSource.connection
			String modulo = grailsApplication.config.grails.plugins.amministrazionedatabase.modulo
			String istanza 	= grailsApplication.config.grails.plugins.amministrazionedatabase.istanza
			Sql sql = new Sql(conn)
			sql.call(update ? SO4_UPDATE : SO4_INSERT, [Sql.out(OracleTypes.VARCHAR), Long.toString(tipologia.id), tipologia.titolo, modulo, istanza])
		} catch (Exception e){
			log.error("Errore nella creazione/aggiornamento del tipo delega ${e.message}", e)
		}

	}

	void eliminaTipologiaDelega (ITipologia tipologia){
		try {
			Connection conn = dataSource.connection
			String modulo = grailsApplication.config.grails.plugins.amministrazionedatabase.modulo
			String istanza 	= grailsApplication.config.grails.plugins.amministrazionedatabase.istanza
			Sql sql = new Sql(conn)
			sql.call(SO4_DELETE, [Sql.out(OracleTypes.VARCHAR), Long.toString(tipologia.id), modulo, istanza])
		} catch (Exception e){
			log.error("Errore nell'eliminazione del tipo delega ${e.message}", e)
		}
	}
}
