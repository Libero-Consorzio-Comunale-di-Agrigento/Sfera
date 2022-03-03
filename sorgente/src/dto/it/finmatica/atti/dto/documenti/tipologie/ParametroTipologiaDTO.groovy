package it.finmatica.atti.dto.documenti.tipologie

import it.finmatica.atti.documenti.tipologie.ParametroTipologia
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfGruppoStepDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class ParametroTipologiaDTO implements it.finmatica.dto.DTO<ParametroTipologia> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String codice;
    WkfGruppoStepDTO gruppoStep;
    TipoDeliberaDTO tipoDelibera;
    TipoDeterminaDTO tipoDetermina;
    String valore;


    public ParametroTipologia getDomainObject () {
        return ParametroTipologia.get(this.id)
    }

    public ParametroTipologia copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
