--liquibase formatted sql
--changeset mfrancesconi:STAT_C_G337_DETE_IMPATTO_GEN runOnChange:true

create or replace force view stat_c_g337_dete_impatto_gen
(
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
    unita_proponente,
    impatto_generazionale,
    importo_impegnato
)
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
           utility_pkg.get_unita_prop_determina (d.id_determina) unita_proponente,
           ig.titolo,
           nvl((select sum (importo)
                  from movimenti_contabili
                 where id_documento = d.id_determina
                   and tipo_documento = 'DETERMINA'
                   and opera = 'IMPEGNO'),0) impegnato
      from determine              d,
           tipi_registro          tp,
           wkf_engine_iter        iter,
           tipi_determina         tipo,
           impatti_generazionali  ig
     where d.valido = 'Y'
       and tp.tipo_registro(+) = d.registro_determina
       and d.id_tipo_determina = tipo.id_tipo_determina
       and iter.id_engine_iter = d.id_engine_iter
       and d.id_impatto_generazionale = ig.id_impatto_generazionale
       and (iter.data_fine is not null or d.data_pubblicazione is not null)
/