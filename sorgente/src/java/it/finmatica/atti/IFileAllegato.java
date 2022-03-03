package it.finmatica.atti;

public interface IFileAllegato {

	String getNome();

	String getContentType();

	long getDimensione();

	String getTesto ();

	Long getIdFileEsterno();

	boolean isPdf ();

	boolean isP7m ();

	boolean isFirmato ();

	// ritorna true se il file è un odt, doc o docx.
	boolean isModificabile ();

	String getNomeFileSbustato ();

	String getNomeFileOriginale ();

	String getNomePdf ();
}
