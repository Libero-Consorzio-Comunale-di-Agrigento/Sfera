package it.finmatica.atti;

import it.finmatica.atti.documenti.IAtto;
import it.finmatica.atti.documenti.IDocumento;
import it.finmatica.atti.documenti.IProposta;

import java.io.InputStream;
import java.util.List;

public interface IntegrazioneContabilita {

	/**
	 * @return il path dello zul da includere nella maschera di delibera/determina/proposta
	 */
	String getZul (IDocumento documento);

	/**
	 * @return true se la contabilità è abilitata (ritorna false solo per il bean NessunaContabilita)
	 */
	boolean isAbilitata (IDocumento documento);

	/**
	 * @return true se la contabilità gestisce l'associazione con le tipologie.
	 */
	boolean isTipiDocumentoAbilitati ();

	/**
	 * Questo metodo serve per lo più per ottimizzazione: prima di aggiornare la maschera dei
	 * movimenti contabili, viene invocato prima di aggiornare tale maschera così da caricarla solo in caso di reale necessità.
	 * @param documento
	 * @return true se il documento ha dei documenti contabili
	 */
	boolean isConDocumentiContabili (IDocumento documento);

	/**
	 * @return i movimenti contabili per il documento
	 */
	List<?> getMovimentiContabili (IDocumento documento);

	/**
	 * @return aggiorna i movimenti contabili recuperati dalla contabilità esterna.
	 */
	void aggiornaMovimentiContabili (IDocumento documento);

	/**
	 * Esegue il refresh della maschera della contabilità
	 * @param documento
	 * @param modifica se true indica che l'utente correntemente loggato ha i diritti di modifica, false altrimenti
	 */
	void aggiornaMaschera (IDocumento documento, boolean modifica);

	/**
	 * Salva la proposta sulla contabilità
	 * @param proposta
	 */
	void salvaProposta (IProposta proposta);

	/**
	 * Salva l'atto sulla contabilità
	 * @param atto
	 */
	void salvaAtto (IAtto atto);

	/**
	 * Annulla la proposta in contabilità
	 * @param proposta
	 */
	void annullaProposta (IProposta proposta);

	/**
	 * Rende Esecutivo l'atto in contabilità
	 * @param atto
	 */
	void rendiEsecutivoAtto (IAtto atto);

	/**
	 * @param proposta
	 * @return l'input stream della scheda contabile
	 */
	InputStream getSchedaContabile (IDocumento proposta);
	
	/**
	 * Copia i movimenti contabili dalla proposta all'atto.
	 * Usato quando viene creata la delibera, per copiare i movimenti contabili dalla proposta alla delibera.
	 */
	void copiaMovimentiContabili(IProposta proposta, IAtto atto);
	
	/**
	 * Sblocca i documenti che sono in attesa che i movimenti contabili siano conclusi
	 */
	List<IDocumento> getDocumentiDaSbloccare ();

	/**
	 * Verifica che tutti i movimenti contabili abbiano il campo cig valorizzato
	 */
	boolean isMovimentiCigCompleti(IDocumento documento);

}
