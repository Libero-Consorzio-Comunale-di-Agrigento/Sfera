package system

import grails.validation.ValidationException
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.gestioneiter.exceptions.IterRuntimeException
import it.finmatica.zkutils.SuccessHandler
import org.apache.log4j.Logger
import org.hibernate.StaleObjectStateException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.orm.hibernate3.HibernateJdbcException
import org.springframework.transaction.HeuristicCompletionException
import org.springframework.transaction.UnexpectedRollbackException
import org.zkoss.bind.BindContext
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.UiException
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

import java.sql.SQLException

class ErrorViewModel {

	public static final Logger log = Logger.getLogger(ErrorViewModel.class)
	SuccessHandler successHandler

	// component
	private Window self

	// eccezione
	String title
	String stacktrace

	// mostrare dettagli
	boolean dettagli	 = false

	// auto closable
	boolean autoClosable = false

	@Init init (@ContextParam(ContextType.COMPONENT) Window w)  {
		self = w
		successHandler.clearMessages();
		Throwable exception = Executions.getCurrent().getAttribute("javax.servlet.error.exception")
		log.error (exception.message, exception)

		def cause = (exception instanceof UiException ? exception.cause : null)
		if (cause != null) {
			checkException(cause)
		} else {
			checkException(exception)
		}
	}

	private void checkException (Throwable e) {

		// pulisco un po' di eccezioni che non mi darebbero valore aggiunto:
		if (e instanceof HeuristicCompletionException && e.cause != null) {
			e = e.cause
		}

		if (e instanceof UnexpectedRollbackException && e.cause != null) {
			e = e.cause
		}

		stacktrace = e.message+"\n"+e.getStackTrace().toString().replace(')', ')\n');

		if (e instanceof AttiRuntimeException || e instanceof IterRuntimeException) {
			title 		 = e.message
			autoClosable = true
			stacktrace   = null
		} else if (e instanceof DataIntegrityViolationException) {
			title = "Oggetto non modificabile o eliminabile: esistono dipendenze."
		} else if (e instanceof StaleObjectStateException) {
			title = "Oggetto modificato da un altro utente."
		} else if (e instanceof ValidationException) {
			title = "Verificare i campi compilati"
		} else if (e instanceof HibernateJdbcException) {
			title = e.getSQLException().message
		} else {

            SQLException sqlException = getSQLException(e);

            if (sqlException != null) {
                e = sqlException
            }

			title = "Errore applicativo:\n\n "+e.message
		}
	}

	private SQLException getSQLException (Exception e) {
		if (e == null) {
			return null
		}

        // evitiamo loop infiniti...
        if (e == e.cause) {
            return null
        }

		if (e instanceof SQLException) {
			return e
		}

		return getSQLException(e.cause)
	}

	@Command onClose (@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		dettagli = false
		Events.postEvent("onClose", self, null)
	}

	@Command checkClose (@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		if (autoClosable)
			Events.postEvent("onClose", self, null)
	}
}
