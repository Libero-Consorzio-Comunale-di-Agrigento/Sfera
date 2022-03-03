package it.finmatica.gestioneiter.dto.configuratore.dizionari

import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzione

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class WkfAzioneDTO implements it.finmatica.dto.DTO<WkfAzione> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String categoria;
    String descrizione;
    String nome;
    String nomeBean;
    String nomeMetodo;
    String istruzioneSql;
    Set<WkfAzioneParametroDTO> parametri;
    TipoAzione tipo;
    WkfTipoOggettoDTO tipoOggetto;
    boolean valido;

    public void addToParametri (WkfAzioneParametroDTO wkfAzioneParametro) {
        if (this.parametri == null)
            this.parametri = new HashSet<WkfAzioneParametroDTO>()
        this.parametri.add (wkfAzioneParametro);
        wkfAzioneParametro.azione = this
    }

    public void removeFromParametri (WkfAzioneParametroDTO wkfAzioneParametro) {
        if (this.parametri == null)
            this.parametri = new HashSet<WkfAzioneParametroDTO>()
        this.parametri.remove (wkfAzioneParametro);
        wkfAzioneParametro.azione = null
    }

    public WkfAzione getDomainObject () {
        return WkfAzione.get(this.id)
    }

    public WkfAzione copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.
}
