package it.finmatica.zkutils

import org.zkoss.bind.BindContext
import org.zkoss.bind.Converter
import org.zkoss.zk.ui.Component
import org.zkoss.zul.Textbox

class OnlyNumbersConverter implements Converter<Object, Object, Component> {
    @Override
    Object coerceToUi(Object beanProp, Component component, BindContext ctx) {
        String value = removeCharsFromString(beanProp.toString())
        if (component instanceof Textbox) {
            component.setValue(value)
        }

        // Valore in entrata dal viewmodel
        return value
    }

    @Override
    Object coerceToBean(Object beanProp, Component component, BindContext ctx) {
        String value = removeCharsFromString(beanProp.toString())
        if (component instanceof Textbox) {
            component.setValue(value)
        }

        // Valore in uscita dal componente
        return value
    }

    private static String removeCharsFromString(String string) {
        return string.replaceAll("[^0-9]", "")
    }
}
