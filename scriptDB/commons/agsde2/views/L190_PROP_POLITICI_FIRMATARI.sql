--liquibase formatted sql
--changeset mfrancesconi:L190_PROP_POLITICI_FIRMATARI runOnChange:true

create or replace force view l190_prop_politici_firmatari
(
    id_gruppo_politico_documento,
    id_documento,
    titolo_gruppo_politico,
    descrizione_gruppo_politico,
    ni_componente,
    nominativo
)
as
    select gpd.id_gruppo_politico_documento,
           gpd.id_delibera id_documento,
           gpd.titolo titolo_gruppo_politico,
           gpd.descrizione descrizione_gruppo_politico,
           gpd.ni_componente,
           utility_pkg.get_cognome_nome (gpd.ni_componente, null) nominativo
      from delibere d,
           proposte_delibera pd,
           gruppi_politici_documenti gpd,
           tipi_delibera td
     where d.id_proposta_delibera = pd.id_proposta_delibera
       and pd.id_tipo_delibera = td.id_tipo_delibera
       and td.gestione_gruppi_politici = 'Y'
       and pd.valido = 'Y'
       and pd.pubblica_casa_di_vetro = 'Y'
       and d.id_delibera = gpd.id_delibera
       and gpd.id_delibera is not null
    union all
    select gpd.id_gruppo_politico_documento,
           gpd.id_proposta_delibera id_documento,
           gpd.titolo titolo_gruppo_politico,
           gpd.descrizione descrizione_gruppo_politico,
           gpd.ni_componente,
           utility_pkg.get_cognome_nome (gpd.ni_componente, null) nominativo
      from proposte_delibera pd,
           gruppi_politici_documenti gpd,
           tipi_delibera td
     where pd.id_tipo_delibera = td.id_tipo_delibera
       and td.gestione_gruppi_politici = 'Y'
       and pd.valido = 'Y'
       and pd.pubblica_casa_di_vetro = 'Y'
       and pd.id_proposta_delibera = gpd.id_proposta_delibera
       and gpd.id_proposta_delibera is not null
/