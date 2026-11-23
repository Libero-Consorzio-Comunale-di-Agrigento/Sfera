--liquibase formatted sql
--changeset mfrancesconi:STAT_C_G337_DELI_IMPATTO_GEN runOnChange:true

create or replace force view stat_c_g337_deli_impatto_gen
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
    select d.id_delibera,
           numero_delibera,
           anno_delibera,
           registro_delibera,
           tp.descrizione descrizione_registro,
           tipo.titolo tipologia,
           d.data_adozione,
           d.data_esecutivita,
           d.data_ins data_inizio,
           nvl (d.data_pubblicazione, iter.data_fine) data_fine,
           utility_pkg.get_unita_prop_delibera (d.id_delibera) unita_proponente,
           ig.titolo,
           nvl ((select sum (importo)
                   from movimenti_contabili
                  where (id_documento = d.id_proposta_delibera)
                    and tipo_documento = 'DELIBERA'
                    and opera = 'IMPEGNO'),
                0)
               impegnato
      from delibere               d,
           proposte_delibera      prop,
           tipi_registro          tp,
           wkf_engine_iter        iter,
           tipi_delibera          tipo,
           impatti_generazionali  ig
     where d.valido = 'Y'
       and prop.valido = 'Y'
       and prop.id_proposta_delibera = d.id_proposta_delibera
       and tp.tipo_registro(+) = d.registro_delibera
       and prop.id_tipo_delibera = tipo.id_tipo_delibera
       and iter.id_engine_iter = d.id_engine_iter
       and d.id_impatto_generazionale = ig.id_impatto_generazionale
       and (iter.data_fine is not null or d.data_pubblicazione is not null)
/