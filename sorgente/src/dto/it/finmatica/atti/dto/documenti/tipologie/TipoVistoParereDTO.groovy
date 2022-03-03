package it.finmatica.atti.dto.documenti.tipologie

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.tipologie.TipoVistoParere
import it.finmatica.atti.dto.impostazioni.CaratteristicaTipologiaDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic

public class TipoVistoParereDTO implements it.finmatica.dto.DTO<TipoVistoParere> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    CaratteristicaTipologiaDTO caratteristicaTipologia;
    String codice;
    boolean conFirma;
    boolean conRedazioneDirigente;
    boolean conRedazioneUnita;
    boolean contabile;
    Date dateCreated;
    String descrizione;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    GestioneTestiModelloDTO modelloTesto;
    Long progressivoCfgIter;
    Long progressivoCfgIterDelibera;
    boolean pubblicazione;
    boolean pubblicaAllegati;
    boolean pubblicaAllegatiDefault;
    int sequenzaStampaUnica;
    boolean stampaUnica;
    boolean testoObbligatorio;
    String titolo;
    String descrizioneNotifica;
    String unitaDestinatarie;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;
    boolean queryMovimenti;


    public TipoVistoParere getDomainObject () {
        return TipoVistoParere.get(this.id)
    }

    public TipoVistoParere copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

	public void addUnitaDestinataria (long progressivo) {
		String progr = Long.toString(progressivo)
		if (unitaDestinatarie == null)
			unitaDestinatarie = ""
		unitaDestinatarie = (unitaDestinatarie.tokenize(TipoVistoParere.SEPARATORE) << progr).unique().join(TipoVistoParere.SEPARATORE)
	}

	public void removeUnitaDestinataria (long progressivo) {
		String progr = Long.toString(progressivo)
		if (unitaDestinatarie == null)
			unitaDestinatarie = ""
		def ud = unitaDestinatarie.tokenize(TipoVistoParere.SEPARATORE)
		ud.remove(progr)
		unitaDestinatarie = ud.join(TipoVistoParere.SEPARATORE)
	}

	public long[] getListaUnitaDestinatarie () {
		if (unitaDestinatarie == null)
			unitaDestinatarie = ""
		return unitaDestinatarie.tokenize(TipoVistoParere.SEPARATORE).collect { Long.parseLong(it) }.toArray()
	}
}
