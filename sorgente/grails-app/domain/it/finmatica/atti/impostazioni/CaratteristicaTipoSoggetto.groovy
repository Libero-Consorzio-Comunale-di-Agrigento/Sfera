package it.finmatica.atti.impostazioni

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import org.hibernate.FetchMode;

class CaratteristicaTipoSoggetto {

    CaratteristicaTipologia caratteristicaTipologia

    int           sequenza
    TipoSoggetto  tipoSoggetto
    RegolaCalcolo regolaCalcoloLista
    RegolaCalcolo regolaCalcoloDefault
    Ad4Ruolo      ruolo
    TipoSoggetto  tipoSoggettoPartenza

    static belongsTo = [caratteristicaTipologia: CaratteristicaTipologia]

    static mapping = {
        table 'caratteristiche_tipi_soggetto'
        id column: 'id_caratteristica_soggetto'
        caratteristicaTipologia column: 'id_caratteristica_tipologia', index: 'cartipsog_cartip_fk'
        tipoSoggetto column: 'tipo_soggetto'
        tipoSoggettoPartenza column: 'tipo_soggetto_partenza'
        regolaCalcoloLista column: 'id_regola_lista', index: 'cartipsog_regcal_fk'
        regolaCalcoloDefault column: 'id_regola_default', index: 'cartipsog_regcaldef_fk'
        ruolo column: 'ruolo'
        sequenza insertable: false, updateable: false
    }

    static constraints = {
        ruolo nullable: true
        regolaCalcoloDefault nullable: true
        regolaCalcoloLista nullable: true
        tipoSoggettoPartenza nullable: true
    }
}