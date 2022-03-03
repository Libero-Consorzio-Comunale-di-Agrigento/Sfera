package it.finmatica.atti.commons

class GestioneTestiLogService {

	/**
	 * Da inserire nel finally del metodo che converte il pdf o che esegue l'operazione che si sta loggando
	 *
	 * @param log
	 */
	void log(GestioneTestiLog log) {
		log.save(flush: true)
	}
}
