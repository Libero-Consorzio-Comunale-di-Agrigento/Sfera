package it.finmatica.zkutils;

import it.finmatica.atti.commons.AttiUtils;
import it.finmatica.atti.impostazioni.Impostazioni;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.validator.AbstractValidator;

public class CharsetValidator extends AbstractValidator {
	@Override
	public void validate(ValidationContext validationContext) {
		String stringa = (String) validationContext.getProperty().getValue();

		if (!AttiUtils.controllaCharset(stringa, Impostazioni.DB_CHARSET.getValore())) {
			addInvalidMessage(validationContext, "Il campo contiene dei caratteri non validi!");
		}
	}
}
