package it.finmatica.atti.documenti;

import it.finmatica.atti.impostazioni.CaratteristicaTipologia;
import it.finmatica.gestionetesti.reporter.GestioneTestiModello;

public interface ITipologia {
	void setProgressivoCfgIter (Long progressivoCfgIterPubblicazione);
	Long getProgressivoCfgIter ();

	void setTitolo (String titolo);
	String getTitolo ();

	void setDescrizione (String descrizione);
	String getDescrizione ();

	void setCaratteristicaTipologia (CaratteristicaTipologia caratteristicaTipologia);
	CaratteristicaTipologia getCaratteristicaTipologia ();

	void setModelloTesto (GestioneTestiModello caratteristicaTipologia);
	GestioneTestiModello getModelloTesto ();
}
