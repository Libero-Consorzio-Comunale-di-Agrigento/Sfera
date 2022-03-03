package it.finmatica.atti.integrazioni.documenti

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupCategoria
import it.finmatica.atti.integrazioni.lookup.LookupTipoAllegato
import it.finmatica.atti.integrazioni.lookup.LookupTipologiaAtto
import it.finmatica.atti.integrazioni.parametri.ModuloIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy
@CompileStatic
class AllegatiObbligatori implements ModuloIntegrazione {

    public static final String MAPPING_CATEGORIA = "ALLEGATI_OBBLIGATORI"
    public static final String MAPPING_CODICE_CATEGORIA = "CATEGORIA"
    public static final String MAPPING_CODICE_TIPOLOGIA = "TIPOLOGIA"

    @Override
    boolean isVisibile() {
        return true;
    }

    @Override
    String getCodice() {
        return MAPPING_CATEGORIA;
    }

    @Override
    String getDescrizione() {
        return "Allegati Obbligatori"
    }

    @Override
    List<ParametroIntegrazione> getListaParametri() {
        return [new ParametroIntegrazione(MAPPING_CODICE_CATEGORIA, "Categoria", LookupCategoria.INSTANCE, LookupTipoAllegato.INSTANCE),
                new ParametroIntegrazione(MAPPING_CODICE_TIPOLOGIA, "Tipologia", LookupTipologiaAtto.INSTANCE, LookupTipoAllegato.INSTANCE)]
    }

    String getCategoria(IProposta proposta) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_CODICE_CATEGORIA, proposta.categoria?.codice, null)
    }

    @CompileDynamic
    String getTipologia(IProposta proposta, String valoreDefault) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_CODICE_TIPOLOGIA, String.valueOf(proposta.tipologiaDocumento?.id), valoreDefault)
    }

}
