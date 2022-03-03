package it.finmatica.atti.documenti;

public interface ITipologiaPubblicazione extends ITipologia {

	Long getProgressivoCfgIterPubblicazione ();

	Integer getGiorniPubblicazione();

	boolean isPubblicazione ();
	boolean isSecondaPubblicazione ();
	boolean isManuale ();
	boolean isPubblicaAllegati ();
	boolean isPubblicazioneFinoARevoca ();
}
