package it.finmatica.atti.documenti;

import java.util.Date;

public interface IFascicolabile {
	void setClassificaCodice (String classificaCodice);
	String getClassificaCodice ();

	void setClassificaDescrizione (String classificaDescrizione);
	String getClassificaDescrizione ();

	void setClassificaDal (Date classificaDal);
	Date getClassificaDal ();

	void setFascicoloAnno (Integer fascicoloAnno);
	Integer getFascicoloAnno ();

	void setFascicoloNumero (String fascicoloNumero);
	String getFascicoloNumero ();

	void setFascicoloOggetto (String fascicoloOggetto);
	String getFascicoloOggetto ();
}
