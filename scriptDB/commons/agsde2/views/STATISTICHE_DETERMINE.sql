--liquibase formatted sql
--changeset mfrancesconi:STATISTICHE_DETERMINE runOnChange:true

create or replace force view statistiche_determine (
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
   select d.id_determina,
          numero_determina,
          anno_determina,
          registro_determina,
          tp.descrizione descrizione_registro,
          tipo.titolo tipologia,
          d.data_numero_determina,
          d.data_esecutivita,
          d.data_ins data_inizio,
          nvl (d.data_pubblicazione, iter.data_fine) data_fine,
          round ( ( (trunc(nvl (d.data_pubblicazione, iter.data_fine)) - trunc(d.data_ins))) + 1) durata_giorni,
          utility_pkg.get_unita_prop_determina (d.id_determina) unita_proponente,
          d.stato
     from determine d,
          tipi_registro tp,
          wkf_engine_iter iter,
          tipi_determina tipo
    where d.valido = 'Y'
      and tp.tipo_registro(+) = d.registro_determina
      and d.id_tipo_determina = tipo.id_tipo_determina
      and iter.id_engine_iter = d.id_engine_iter
      and (iter.data_fine is not null or d.data_pubblicazione is not null)
/