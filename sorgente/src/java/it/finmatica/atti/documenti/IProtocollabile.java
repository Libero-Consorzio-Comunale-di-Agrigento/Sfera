package it.finmatica.atti.documenti;

import it.finmatica.atti.dizionari.TipoRegistro;

import java.util.Date;
import java.util.List;

public interface IProtocollabile extends IFascicolabile, IDocumento {

	enum Movimento { ARRIVO, PARTENZA, INTERNO }

    String getOggetto();

	void setNumeroProtocollo (Integer numeroProtocollo);
	Integer getNumeroProtocollo ();

	void setAnnoProtocollo (Integer annoProtocollo);
	Integer getAnnoProtocollo ();

	void setDataNumeroProtocollo (Date dataNumeroProtocollo);
	Date getDataNumeroProtocollo ();

	void setRegistroProtocollo (TipoRegistro tipoRegistro);
	TipoRegistro getRegistroProtocollo ();

	Movimento getMovimento ();

	List<DestinatarioNotifica> getDestinatari();
}
