package it.finmatica.atti.dto.impostazioni

import it.finmatica.atti.impostazioni.CampiDocumento
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfTipoOggettoDTO

import grails.compiler.GrailsCompileStatic
public class CampiDocumentoDTO implements it.finmatica.dto.DTO<CampiDocumento> {
    private static final long serialVersionUID = 1L;

    Long id;
    String blocco;
    String campo;
    WkfTipoOggettoDTO tipoOggetto;


    public CampiDocumento getDomainObject () {
        return CampiDocumento.createCriteria().get {
            eq('tipoOggetto.codice', this.tipoOggetto.codice)
            eq('campo', this.campo)
            eq('blocco', this.blocco)
        }
    }

    public CampiDocumento copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
