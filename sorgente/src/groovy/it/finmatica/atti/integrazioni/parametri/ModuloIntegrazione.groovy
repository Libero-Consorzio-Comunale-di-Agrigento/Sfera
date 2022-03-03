package it.finmatica.atti.integrazioni.parametri

interface ModuloIntegrazione {

	String getCodice ()
	String getDescrizione ()
	List<ParametroIntegrazione> getListaParametri ()
	boolean isVisibile();
}