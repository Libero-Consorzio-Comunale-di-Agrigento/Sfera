package it.finmatica.atti.impostazioni

import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto

import org.apache.commons.lang.builder.HashCodeBuilder

/**
 * Domain Class che rappresenta i vari campi/blocchi dei vari documenti. Ad esempio:
 * Determina->OGGETTO
 * Determina->DATI_PROTOCOLLO
 * Determina->TAB_VISTI
 *
 */
class CampiDocumento implements Serializable {

	// separatore utilizzato per concatenare i codici dei campi
	public static final transient String SEPARATORE_CAMPI = "#"

	// il tipo oggetto a cui questi campi/blocchi riferiscono
	WkfTipoOggetto tipoOggetto

	// il nome del blocco di campi (ad es. DATI_PROTOCOLLO può contenere vari dati come NUMERO_PROTOCOLLO, ANNO_PROTOCOLLO, ma da un certo cliente può contenere anche il campo UTENTE_FIRMATARIO)
	String blocco

	// il codice che identifica il campo sullo .zul (può essere anche un "tab" o qualsiasi altra cosa prevista dalla maschera)
	String campo

    static constraints = {
    }

	static mapping = {
		table 		'campi_documento'
		version 	false
		id 			generator: 'assigned', composite: ['tipoOggetto', 'campo', 'blocco']
		tipoOggetto column: "tipo_oggetto"
	}

	/**
	 * Ritorna i campi di un tipo oggetto
	 *
	 * @param codiceTipoOggetto il codice del tipo oggetto di cui si vogliono i campi
	 * @return l'elenco dei codici di campi.
	 */
	public List<String> getCampi(String codiceTipoOggetto) {
		return CampiDocumento.createCriteria().list {
			projections {
				distinct "campo"
			}
			eq ("tipoOggetto.codice", codiceTipoOggetto)
		}
	}

	/**
	 * Ritorna i Blocchi di un tipo oggetto
	 *
	 * @param codiceTipoOggetto il codice del tipo oggetto di cui si vogliono i campi
	 * @return l'elenco dei codici dei blocchi
	 */
	public List<String> getBlocchi (String codiceTipoOggetto) {
		return CampiDocumento.createCriteria().list {
			projections {
				distinct "blocco"
			}
			eq ("tipoOggetto.codice", codiceTipoOggetto)
		}
	}

	/**
	 * Ritorna i campi per il blocco specificato
	 *
	 * @param codiceTipoOggetto il codice del tipo oggetto
	 * @param blocco			il codice del blocco
	 * @return					l'elenco dei campi del blocco del tipo oggetto
	 */
	public static List<String> getCampiBlocco (String codiceTipoOggetto, String blocco) {
		return CampiDocumento.createCriteria().list {
			projections {
				distinct "campo"
			}
			eq ("tipoOggetto.codice", codiceTipoOggetto)
			eq ("blocco", blocco)
		}
	}

	/**
	 * Splitta i campi presenti in listaCampi separati da SEPARATORE_CAMPI
	 *
	 * @param listaCampi la stringa dei campi come ritornata da joinListaCampi()
	 * @return l'elenco dei campi contenuti nella stringa. se nessuno, ritorna lista vuota. mai null.
	 */
	public static List<String> splitListaCampi (String listaCampi) {
		if (listaCampi == null)
			return []

		return listaCampi.tokenize (CampiDocumento.SEPARATORE_CAMPI)
	}

	/**
	 * Unisce la lista dei campi con il SEPARATORE_CAMPI
	 *
	 * @param listaCampi la lista dei campi da unire
	 * @return una stringa con i campi uniti separati da SEPARATORE_CAMPI
	 */
	public static String joinListaCampi (List<String> listaCampi) {
		return listaCampi.join(CampiDocumento.SEPARATORE_CAMPI)
	}

	public static String proteggiCampo (String listaCampi, String campo) {
		List<String> campi = splitListaCampi (listaCampi);
		if (!campi.contains(campo)) {
			campi.add(campo);
		}
		return joinListaCampi(campi);
	}

	public static String abilitaCampo (String listaCampi, String campo) {
		List<String> campi = splitListaCampi (listaCampi);
		campi.remove(campo);
		return joinListaCampi(campi);
	}

	/**
	 * Ritorna la mappa di campi in cui la chiave è il codice del campo e il valore è true, questa mappa è utile per essere usata direttamente dallo .zul per la protezione dei campi
	 *
	 * @param listaCampiProtetti stringa dei campi protetti del documento separati da SEPARATORE_CAMPI
	 * @return una mappa della forma: map[NOME_CAMPO] = true
	 */
	public static def getMappaCampi (String listaCampiProtetti) {
		def map = [:]
		def list = splitListaCampi(listaCampiProtetti)
		for (String campo : list)
			map[campo] = true
		return map
	}

	boolean equals(other) {
		if (!(other instanceof CampiDocumento)) {
			return false
		}

		return (other.tipoOggetto?.codice == tipoOggetto?.codice &&
				other.dal 	== blocco &&
				other.campo == campo)
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (tipoOggetto) builder.append(tipoOggetto.codice)
		if (blocco) builder.append(blocco)
		if (campo) builder.append(campo)
		builder.toHashCode()
	}
}
