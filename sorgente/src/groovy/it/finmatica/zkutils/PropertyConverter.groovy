package it.finmatica.zkutils

import org.zkoss.bind.BindContext
import org.zkoss.bind.Converter
import org.zkoss.zk.ui.Component

class PropertyConverter implements Converter {

	public Object coerceToBean(Object obj, Component component, BindContext context) {
		return obj?.value
	}

	/**
	 * Tra tutti gli elementi del model prendo quello con #property
	 */
	public Object coerceToUi(Object obj, Component component, BindContext context) {
		String propertyName	= context.getConverterArg("property");
		def defaultValue 		= context.getConverterArg("defaultValue");

		def propertyValue = null;		
		if (propertyName != null) {
			// se il selectedItem non ha property allora ne prendo la stringa
			propertyValue = obj?.hasProperty(propertyName) ? obj?."$propertyName" : obj
		} else {
			// se non devo cercare la proprietà, uso direttamente il valore dell'oggetto
			propertyValue = obj;
		}

		if (propertyValue == null) {
			propertyValue = defaultValue;
		}

		for (def item : component.items) {
			if (propertyName != null) {
				MetaProperty p = item.value.hasProperty(propertyName);
				if (p != null) {
					def pValue = propertyValue?.asType(p.type)
					if (item.value."$propertyName" == pValue) {
						return item;
					}
				} else if (item.value == propertyValue) {
					return item;
				}
			} else {
				if (item.value == propertyValue) {
					return item;
				}
			}
		}

		return null;
	}
}