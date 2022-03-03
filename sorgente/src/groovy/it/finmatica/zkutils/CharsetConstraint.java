package it.finmatica.zkutils;

import it.finmatica.atti.commons.AttiUtils;
import it.finmatica.atti.impostazioni.Impostazioni;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Constraint;

public class CharsetConstraint implements Constraint {

    public void validate(Component comp, Object value) throws WrongValueException {
    	String stringa = (String) value;
        if (!AttiUtils.controllaCharset(stringa, Impostazioni.DB_CHARSET.getValore())) {
            throw new WrongValueException(comp, "Attenzione: sono presenti caratteri non supportati!");
        }
    }
}
