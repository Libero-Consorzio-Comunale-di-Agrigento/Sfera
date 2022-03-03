package it.finmatica.atti.documenti;

import it.finmatica.atti.commons.FileAllegato;
import it.finmatica.atti.dizionari.TipoRegistro;

import java.util.Date;

public interface IAtto extends IProtocollabile, IDocumento, IFascicolabile, IPubblicabile {

	// dati dell'atto:
	IProposta getProposta();

	Integer getAnnoAtto();
	Integer getNumeroAtto();
	TipoRegistro getRegistroAtto();
	Date getDataAtto();

	// gestione corte dei conti
	boolean isDaInviareCorteConti();
	void setDaInviareCorteConti(boolean value);
	Date getDataInvioCorteConti ();
	void setDataInvioCorteConti (Date date);

	boolean isRiservato();
	void setRiservato(boolean riservato);

	void setStampaUnica (FileAllegato stampaUnica);
	FileAllegato getStampaUnica ();

	void setOggetto (String oggetto);
	String getOggetto ();

	// dati di protocollo
	void setDataNumeroProtocollo (Date dataNumeroProtocollo);
	Date getDataNumeroProtocollo ();

	void setNumeroProtocollo (Integer numeroProtocollo);
	Integer getNumeroProtocollo ();

	void setAnnoProtocollo (Integer annoProtocollo);
	Integer getAnnoProtocollo ();

	void setRegistroProtocollo (TipoRegistro registroProtocollo);
	TipoRegistro getRegistroProtocollo ();

	// dati di esecutività
	void setDataEsecutivita (Date dataEsecutivita);
	Date getDataEsecutivita ();

	// funzioni di utilità generale
	String getEstremiAtto();

	void setStatoConservazione(StatoConservazione statoConservazione);
	StatoConservazione getStatoConservazione();
}
