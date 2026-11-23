--liquibase formatted sql
--changeset mfrancesconi:L190_PROP_GRUPPI_POLITICI runOnChange:true

create or replace force view l190_prop_gruppi_politici
(
    id_documento,
    id_proposta_delibera,
    id_delibera,
    anno_proposta,
    numero_proposta,
    registro_proposta,
    anno_delibera,
    numero_delibera,
    registro_delibera,
    descrizione_registro_delibera,
    numero_protocollo_richiesta,
    anno_protocollo_richiesta,
    oggetto,
    titolo,
    data_adozione,
    data_presentazione,
    esito_odg
)
as
    select pd.id_proposta_delibera,
           pd.id_proposta_delibera,
           null,
           pd.anno_proposta,
           pd.numero_proposta,
           pd.registro_proposta,
           null anno_delibera,
           null numero_delibera,
           null registro_delibera,
           null registro_delibera_descrizione,
           pd.numero_protocollo_richiesta,
           pd.anno_protocollo_richiesta,
           pd.oggetto,
           td.titolo,
           cast (null as date) data_adozione,
           to_date (da.valore, 'dd/mm/yyyy') data_presentazione,
           'IN TRATTAZIONE' esito_odg
      from proposte_delibera pd,
           dati_aggiuntivi da,
           tipi_delibera td,
           wkf_engine_iter wei
     where pd.id_tipo_delibera = td.id_tipo_delibera
       and td.gestione_gruppi_politici = 'Y'
       and pd.valido = 'Y'
       and pd.pubblica_casa_di_vetro = 'Y'
       and pd.id_proposta_delibera = da.id_proposta_delibera(+)
       and da.codice(+) = 'DATA_ISTANZA'
       and pd.id_engine_iter = wei.id_engine_iter
       and wei.data_fine is null --la proposta è ancora in trattazione
       and not exists
               (select 1
                  from delibere d
                 where d.id_proposta_delibera = pd.id_proposta_delibera
                   and d.valido = 'Y')
    union
    select pd.id_proposta_delibera,
           pd.id_proposta_delibera,
           null,
           pd.anno_proposta,
           pd.numero_proposta,
           pd.registro_proposta,
           null anno_delibera,
           null numero_delibera,
           null registro_delibera,
           null registro_delibera_descrizione,
           pd.numero_protocollo_richiesta,
           pd.anno_protocollo_richiesta,
           pd.oggetto,
           td.titolo,
           cast (null as date) data_adozione,
           to_date (da.valore, 'dd/mm/yyyy') data_presentazione,
           wcs.titolo esito_odg
      from proposte_delibera pd,
           dati_aggiuntivi da,
           tipi_delibera td,
           wkf_engine_iter wei,
           wkf_engine_step wes,
           wkf_cfg_step wcs
     where pd.id_tipo_delibera = td.id_tipo_delibera
       and td.gestione_gruppi_politici = 'Y'
       and pd.valido = 'Y'
       and pd.pubblica_casa_di_vetro = 'Y'
       and pd.id_proposta_delibera = da.id_proposta_delibera(+)
       and da.codice(+) = 'DATA_ISTANZA'
       and pd.id_engine_iter = wei.id_engine_iter
       and wei.data_fine is not null --la proposta ha terminato l'iter
       and wei.id_step_corrente = wes.id_engine_step
       and wes.id_cfg_step = wcs.id_cfg_step
       and not exists
               (select 1
                  from delibere d
                 where d.id_proposta_delibera = pd.id_proposta_delibera
                   and d.valido = 'Y')
    union
    select d.id_delibera,
           pd.id_proposta_delibera,
           d.id_delibera,
           pd.anno_proposta,
           pd.numero_proposta,
           pd.registro_proposta,
           d.anno_delibera,
           d.numero_delibera,
           d.registro_delibera,
           tr.descrizione,
           pd.numero_protocollo_richiesta,
           pd.anno_protocollo_richiesta,
           d.oggetto,
           td.titolo,
           d.data_adozione,
           to_date (da.valore, 'dd/mm/yyyy') data_presentazione,
           oe.titolo esito_odg
      from proposte_delibera pd,
           delibere d,
           odg_oggetti_seduta oos,
           odg_esiti oe,
           dati_aggiuntivi da,
           tipi_delibera td,
           tipi_registro tr
     where pd.id_tipo_delibera = td.id_tipo_delibera
       and d.id_proposta_delibera = pd.id_proposta_delibera
       and d.registro_delibera = tr.tipo_registro
       and td.gestione_gruppi_politici = 'Y'
       and pd.valido = 'Y'
       and pd.pubblica_casa_di_vetro = 'Y'
       and pd.id_oggetto_seduta = oos.id_oggetto_seduta  --la delibera viene creata dalla proposta e non da odg
       and oos.id_esito = oe.id_esito
       and oos.conferma_esito = 'Y'
       and pd.id_proposta_delibera = da.id_proposta_delibera(+)
       and da.codice(+) = 'DATA_ISTANZA'
    union
    select d.id_delibera,
           pd.id_proposta_delibera,
           d.id_delibera,
           pd.anno_proposta,
           pd.numero_proposta,
           pd.registro_proposta,
           d.anno_delibera,
           d.numero_delibera,
           d.registro_delibera,
           tr.descrizione,
           pd.numero_protocollo_richiesta,
           pd.anno_protocollo_richiesta,
           d.oggetto,
           td.titolo,
           d.data_adozione,
           to_date (da.valore, 'dd/mm/yyyy') data_presentazione,
           wcs.titolo esito_odg
      from proposte_delibera pd,
           delibere d,
           wkf_engine_iter wei,
           wkf_engine_step wes,
           wkf_cfg_step wcs,
           dati_aggiuntivi da,
           tipi_delibera td,
           tipi_registro tr
     where pd.id_tipo_delibera = td.id_tipo_delibera
       and d.id_proposta_delibera = pd.id_proposta_delibera
       and d.registro_delibera = tr.tipo_registro
       and d.id_engine_iter = wei.id_engine_iter
       and wei.id_step_corrente = wes.id_engine_step
       and wes.id_cfg_step = wcs.id_cfg_step
       and td.gestione_gruppi_politici = 'Y'
       and pd.valido = 'Y'
       and pd.pubblica_casa_di_vetro = 'Y'
       and pd.id_proposta_delibera = da.id_proposta_delibera(+)
       and da.codice(+) = 'DATA_ISTANZA'
       and not exists
               (select 1
                  from odg_oggetti_seduta
                 where id_proposta_delibera = pd.id_proposta_delibera)
/