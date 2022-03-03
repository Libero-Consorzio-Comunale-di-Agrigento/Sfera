package it.finmatica.atti.integrazioni.contabilita

import grails.util.Holders
import groovy.transform.CompileStatic
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.config.MultiEnteProxyFactoryBean
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.lookup.LookupEnti
import it.finmatica.atti.integrazioni.lookup.LookupStatoEsecutivo
import it.finmatica.atti.integrazioni.lookup.LookupUfficio
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistService
import it.finmatica.atti.integrazioniws.comunemodena.contabilita.AttiAmministrativi
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy
@CompileStatic
class IntegrazioneContabilitaComuneModenaConfig extends AbstractWebServiceConfig {

    public static final String MAPPING_CATEGORIA = "CONTABILITA_MODENA"
    public static final String MAPPING_ENTE = "ENTE"
    public static final String MAPPING_SETTORE = "SETTORE"
    public static final String MAPPING_UFFICIO = "UFFICIO"
    public static final String MAPPING_DIVISIONE = "DIVISIONE"
    public static final String MAPPING_UTENTE = "UTENTE"
    public static final String MAPPING_STATO = "STATO"
    public static final String MAPPING_CODICI_EUROPEI = "CODICI_EUROPEI"
    public static final String MAPPING_TIPI_USCITA = "TIPI_USCITA"
    public static final String MAPPING_TIPI_ENTRATA = "TIPI_ENTRATA"
    public static final String MAPPING_TIPI_CODICI_STATISTICI = "TIPI_CODICI_STATISTICI"

    /*
     * Implementazione interfaccia ModuloIntegrazione
     */

    @Override
    String getCodice() {
        return MAPPING_CATEGORIA
    }

    @Override
    List<ParametroIntegrazione> getListaParametriAggiuntivi() {
        return [new ParametroIntegrazione(MAPPING_ENTE, "Ente", LookupEnti.INSTANCE),
                new ParametroIntegrazione(MAPPING_SETTORE, "Settore"),
                new ParametroIntegrazione(MAPPING_UFFICIO, "Ufficio"),
                new ParametroIntegrazione(MAPPING_DIVISIONE, "Divisione"),
                new ParametroIntegrazione(MAPPING_UTENTE, "Utente", LookupUfficio.INSTANCE),
                new ParametroIntegrazione(MAPPING_STATO, "Stato Esecutività", LookupStatoEsecutivo.INSTANCE),
                new ParametroIntegrazione(MAPPING_TIPI_CODICI_STATISTICI, "Tipo Codice Statistico"),
                new ParametroIntegrazione(MAPPING_CODICI_EUROPEI, "Codice Europeo"),
                new ParametroIntegrazione(MAPPING_TIPI_ENTRATA, "Tipi Entrata"),
                new ParametroIntegrazione(MAPPING_TIPI_USCITA, "Tipi Uscita")
        ]
    }

    @Override
    String getDescrizione() {
        return "Contabilità Modena"
    }

    @Override
    void ricaricaParametri() {
        MultiEnteProxyFactoryBean<AttiAmministrativi> factoryBean = (MultiEnteProxyFactoryBean<AttiAmministrativi>) Holders.getApplicationContext().getBean("&integrazioneContabilitaComuneModenaWebService")
        factoryBean.invalidateCache()
    }

    @Override
    void testWebservice() {
        // fa un po' schifino così ma pazienza.
        AttiAmministrativi service = (AttiAmministrativi) Holders.getApplicationContext().getBean("integrazioneContabilitaComuneModenaWebService")
        service.ricercaTipiGara("-1", "-1")
    }

    String getCodiceEnte(IDocumento documento) {
        return getCodiceEnte(documento.ente.codice)
    }

    String getCodiceEnte(String codiceEnteInterno) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_ENTE, codiceEnteInterno)
    }

    String getCodiceUtente(IProposta proposta) {
        return getCodiceUtente(proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.codice)
    }

    String getCodiceUtente(String codiceUo) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_UTENTE, codiceUo)
    }

    String getCodiceClassifica(IAtto atto) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_SETTORE)
    }

    String getFascicoloAnno(IAtto atto) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_UFFICIO, MappingIntegrazione.VALORE_INTERNO_TUTTI, null)
    }

    String getFascicoloNumero(IAtto atto) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_DIVISIONE, MappingIntegrazione.VALORE_INTERNO_TUTTI, null)
    }

    String getStatoEsecutivita() {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_STATO, StatoDocumento.ESECUTIVO.toString(), null)
    }

    List<CodiceDescrizione> getListaCodiciIdEuropei() {
        return MappingIntegrazione.getValoriEsterni(MAPPING_CATEGORIA, MAPPING_CODICI_EUROPEI)
    }

    List<CodiceDescrizione> getTipiUscita() {
        return MappingIntegrazione.getValoriEsterni(MAPPING_CATEGORIA, MAPPING_TIPI_USCITA)
    }

    List<CodiceDescrizione> getTipiEntrata() {
        return MappingIntegrazione.getValoriEsterni(MAPPING_CATEGORIA, MAPPING_TIPI_ENTRATA)
    }

    List<CodiceDescrizione> getTipiCodiciStatistici() {
        return MappingIntegrazione.getValoriEsterni(MAPPING_CATEGORIA, MAPPING_TIPI_CODICI_STATISTICI)
    }

    int getCodiceDivisione() {
        return MappingIntegrazione.getValoreEsternoInt(MAPPING_CATEGORIA, MAPPING_DIVISIONE)
    }

    int getCodiceSettore() {
        return MappingIntegrazione.getValoreEsternoInt(MAPPING_CATEGORIA, MAPPING_SETTORE)
    }

    int getCodiceUfficio() {
        return MappingIntegrazione.getValoreEsternoInt(MAPPING_CATEGORIA, MAPPING_UFFICIO)
    }
}
