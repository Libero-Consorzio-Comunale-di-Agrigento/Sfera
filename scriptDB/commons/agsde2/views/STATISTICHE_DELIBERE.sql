--liquibase formatted sql
--changeset mfrancesconi:STATISTICHE_DELIBERE runOnChange:true

create or replace force view statistiche_delibere (
   id_atto,
   numero_atto,
   anno_atto,
   registro,
   descrizione_registro,
   tipologia,
   data_adozione,
   data_esecutivita,
   data_inizio,
   data_fine,
   durata_giorni,
   unita_proponente,
   stato)
as
   select d.id_delibera,
          numero_delibera,
          anno_delibera,
          registro_delibera,
          tp.descrizione descrizione_registro,
          tipo.titolo tipologia,
          d.data_adozione,
          d.data_esecutivita,
          prop.data_ins data_inizio,
          nvl (d.data_pubblicazione, iter.data_fine) data_fine,
          round ( trunc((nvl (d.data_pubblicazione, iter.data_fine)) - trunc(prop.data_ins)) + 1) durata_giorni,
          utility_pkg.get_unita_prop_delibera (prop.id_proposta_delibera) unita_proponente,
          d.stato
     from delibere d,
          proposte_delibera prop,
          tipi_registro tp,
          wkf_engine_iter iter,
          tipi_delibera tipo
    where d.valido = 'Y'
      and prop.valido = 'Y'
      and prop.id_proposta_delibera = d.id_proposta_delibera
      and tp.tipo_registro(+) = d.registro_delibera
      and prop.id_tipo_delibera = tipo.id_tipo_delibera
      and iter.id_engine_iter = d.id_engine_iter
      and (iter.data_fine is not null or d.data_pubblicazione is not null)
   union
   --proposte di delibera concluse che non sono mai diventate delibera
   select prop.id_proposta_delibera,
          null,
          null,
          null,
          null descrizione_registro,
          tipo.titolo tipologia,
          null,
          null,
          prop.data_ins data_inizio,
          iter.data_fine data_fine,
          (trunc (iter.data_fine - prop.data_ins) + 1) durata_giorni,
          utility_pkg.get_unita_prop_delibera (prop.id_proposta_delibera) unita_proponente,
          prop.stato
     from proposte_delibera prop, wkf_engine_iter iter, tipi_delibera tipo
    where prop.valido = 'Y'
      and prop.id_tipo_delibera = tipo.id_tipo_delibera
      and iter.id_engine_iter = prop.id_engine_iter
      and iter.data_fine is not null
      and not exists
             (select 1
                from delibere
               where id_proposta_delibera = prop.id_proposta_delibera)
/