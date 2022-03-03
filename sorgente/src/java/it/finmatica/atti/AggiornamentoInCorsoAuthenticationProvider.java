package it.finmatica.atti;

import it.finmatica.ad4.AD4AuthenticationProvider;
import it.finmatica.atti.impostazioni.Impostazioni;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AggiornamentoInCorsoAuthenticationProvider extends AD4AuthenticationProvider {

	public Authentication authenticate (Authentication authentication) throws AuthenticationException {

		// se c'è in corso un aggiornamento e l'utente non è AGSDE2 (cioè l'amministratore), non consento l'accesso:
		if (isAggiornamentoInCorso() && !("AGSDE2".equalsIgnoreCase(authentication.getName()))) {
			throw new BadCredentialsException("Il login è possibile solo per l'utente amministratore.");
		}

		// altrimenti, proseguo normalmente
		return super.authenticate(authentication);
	}

	public static boolean isAggiornamentoInCorso () {
		String value = Impostazioni.AGGIORNAMENTO_IN_CORSO.getValore();

		// se non ho il valore, imposto FALSE di default (magari sono in fase di prima installazione)
		if (value == null) {
			return false;
		}

		SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy hh:mm");
		Date date = null;
		boolean aggiornamentoInCorso = false;
		try {
			date = parser.parse(value);
		} catch (ParseException e) {
			// se la data non è parsabile, suppongo che il valore sia Y.
		}

		// considero l'aggiornamento in corso solo se il valore è Y o una data parsabile
		if ("Y".equalsIgnoreCase(value)) {
			aggiornamentoInCorso = true;
		} else if (date != null && date.after(new Date())) {
			aggiornamentoInCorso = true;
		}

		return aggiornamentoInCorso;
	}
}
