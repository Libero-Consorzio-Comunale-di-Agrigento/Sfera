package it.finmatica.atti.integrazioni.protocollo

import grails.util.Holders
import groovy.sql.Sql
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupTipologiaDaProtocollare
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori
import it.finmatica.atti.integrazioni.parametri.ModuloIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Conditional(ProtocolloGdmCondition)
@Component
@Lazy
class ProtocolloGdmConfig implements ModuloIntegrazione {

    public static final String MAPPING_CATEGORIA = "PROTOCOLLO_GDM"
    public static final String MAPPING_CODICE_TIPO_DOCUMENTO = "TIPO_DOCUMENTO_PROTOCOLLO"
    public static final String MAPPING_CODICE_REGISTRO = "CODICE_REGISTRO"
    public static final String MAPPING_GDM_PROPERTIES = "GDM_PROPERTIES"


    @Override
    boolean isVisibile() {
        return true;
    }

    @Autowired
    @Qualifier("dataSource_gdm")
    DataSource dataSource_gdm

    @Override
    String getCodice() {
        return MAPPING_CATEGORIA
    }

    @Override
    String getDescrizione() {
        return "Protocollo GDM"
    }

    @Override
    List<ParametroIntegrazione> getListaParametri() {
        return [new ParametroIntegrazione(MAPPING_CODICE_TIPO_DOCUMENTO, "Tipo di Protocollo", LookupTipologiaDaProtocollare.INSTANCE, new LookupValori() {
            @Override
            List<CodiceDescrizione> getValori() {
                return getTipiDocumento()
            }
        })]
    }

    List<CodiceDescrizione> getTipiDocumento() {
        // questo fa schifus ma è comodo per poter riutilizzare questi tipiDocumento dentro i vari TipoDeterminaViewModel, TipoDeliberaViewModel e CommissioneDettaglioViewModel
        if (Impostazioni.PROTOCOLLO.valore != "protocolloEsternoGdm") {
            return []
        }

        return new Sql(dataSource_gdm).rows(
                "select tipo_documento as CODICE, descrizione_tipo_documento as DESCRIZIONE from seg_tipi_documento order by tipo_documento, descrizione_tipo_documento asc").collect { row ->
            new CodiceDescrizione(row.CODICE, row.DESCRIZIONE)
        }
    }

    String getTipoDocumento(long idTipologia) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_CODICE_TIPO_DOCUMENTO, idTipologia.toString(), "")
    }

    String getCodiceRegistro() {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_CODICE_REGISTRO, MappingIntegrazione.VALORE_INTERNO_TUTTI, "PROT")
    }

    String getPathGdmProperties() {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_GDM_PROPERTIES, MappingIntegrazione.VALORE_INTERNO_TUTTI, "target/confapps/Atti/gd4dm.properties")
    }
}
