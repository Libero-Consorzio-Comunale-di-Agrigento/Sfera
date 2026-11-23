package it.finmatica.atti.commons

class GestioneTestiLogService {

	/**
	 * Da inserire nel finally del metodo che converte il pdf o che esegue l'operazione che si sta loggando
	 *
	 * @param log
	 */
	void log(GestioneTestiLog testoLog) {
		try {
		testoLog.save(flush: true)
		}
		catch (RuntimeException) {
			log.error("Problema nel salvataggio nella tabella gte_log")
		}
	}
}