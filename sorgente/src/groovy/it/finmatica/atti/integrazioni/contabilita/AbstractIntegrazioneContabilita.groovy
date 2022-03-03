package it.finmatica.atti.integrazioni.contabilita

import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.documenti.VistoParere

public abstract class AbstractIntegrazioneContabilita implements IntegrazioneContabilita {
	
	/**
	 * @return il path dello zul da includere nella maschera di delibera/determina/proposta
	 */
	public String getZul (IDocumento documento) {
		return ""
	}

	/**
	 * @return true se la contabilità gestisce l'associazione con le tipologie.
	 */
	public boolean isTipiDocumentoAbilitati () {
		return false
	}

	/**
	 * Questo metodo serve per lo più per ottimizzazione: prima di aggiornare la maschera dei
	 * movimenti contabili, viene invocato prima di aggiornare tale maschera così da caricarla solo in caso di reale necessità.
	 * @param documento
	 * @return true se il documento ha dei documenti contabili
	 */
	public boolean isConDocumentiContabili (IDocumento documento) {
		return false
	}

	/**
	 * @return i movimenti contabili per il documento
	 */
	public List<?> getMovimentiContabili (IDocumento documento) {
		return []
	}

	/**
	 * @return aggiorna i movimenti contabili recuperati dalla contabilità esterna.
	 */
	public void aggiornaMovimentiContabili (IDocumento documento) {
		
	}

	/**
	 * Esegue il refresh della maschera della contabilità
	 * @param documento
	 * @param modifica se true indica che l'utente correntemente loggato ha i diritti di modifica, false altrimenti
	 */
	public void aggiornaMaschera (IDocumento documento, boolean modifica) {
		
	}

	/**
	 * Salva la proposta sulla contabilità
	 * @param proposta
	 */
	public void salvaProposta (IProposta proposta) {
		
	}

	/**
	 * Salva l'atto sulla contabilità
	 * @param atto
	 */
	public void salvaAtto (IAtto atto) {
		
	}

	/**
	 * Annulla la proposta in contabilità
	 * @param proposta
	 */
	public void annullaProposta (IProposta proposta) {
		
	}

	/**
	 * Rende Esecutivo l'atto in contabilità
	 * @param atto
	 */
	public void rendiEsecutivoAtto (IAtto atto) {
		
	}

	/**
	 * @param proposta
	 * @return l'input stream della scheda contabile
	 */
	public InputStream getSchedaContabile (IDocumento proposta) {
		return null
	}
	
	/**
	 * Copia i movimenti contabili dalla proposta all'atto.
	 * Usato quando viene creata la delibera, per copiare i movimenti contabili dalla proposta alla delibera.
	 */
	public void copiaMovimentiContabili(IProposta proposta, IAtto atto) {
		
	}
	
	/**
	 * Sblocca i documenti che sono in attesa che i movimenti contabili siano conclusi
	 */
	public List<IDocumento> getDocumentiDaSbloccare () {
		return []
	}

	@Override
	public boolean isAbilitata(IDocumento documento) {

		// se il documento è un visto ed è di tipo contabile, allora abilito la contabilità
		if (documento.id > 0 && documento instanceof VistoParere && documento.tipologia.contabile) {
			return true;
		}
		
		// considero la contabilità come "abilitata" solo se la tipologia della proposta lo prevede e se ho il numero della proposta.
		IProposta proposta = getProposta(documento);
		if (proposta.numeroProposta > 0 && proposta.tipologiaDocumento.movimentiContabili) {
			return true;
		}
		
		return false;
	}
	
	protected final IProposta getProposta (IDocumento documento) {
		if (documento instanceof VistoParere) {
			documento = documento.documentoPrincipale
		}

		if (documento instanceof IAtto) {
			documento = documento.proposta;
		}

		return documento;
	}

	boolean isMovimentiCigCompleti(IDocumento documento) {
		return true;
	}

}
