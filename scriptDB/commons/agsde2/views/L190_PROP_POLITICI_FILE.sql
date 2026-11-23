--liquibase formatted sql
--changeset mfrancesconi:L190_PROP_POLITICI_FILE runOnChange:true

create or replace force view l190_prop_politici_file
(
    id_documento,
    id_allegato,
    id_file_allegato,
    id_file_esterno,
    titolo,
    descrizione,
    content_type,
    firmato,
    nome,
    testoocr
)
as
    --allegati della proposta
    select pd.id_proposta_delibera id_documento,
           a.id_allegato,
           fa.id_file_allegato,
           fa.id_file_esterno,
           a.titolo,
           a.descrizione,
           fa.content_type,
           fa.firmato,
           fa.nome,
           gdm_oggetti_file_pkg.downloadoggettofile (fa.id_file_esterno) testoocr
      from file_allegati fa,
           allegati_file af,
           allegati a,
           proposte_delibera pd,
           tipi_delibera td
     where pd.id_tipo_delibera = td.id_tipo_delibera
       and a.id_proposta_delibera = pd.id_proposta_delibera
       and a.id_allegato = af.id_allegato
       and af.id_file = fa.id_file_allegato
       and td.gestione_gruppi_politici = 'Y'
       and pd.valido = 'Y'
       and pd.pubblica_casa_di_vetro = 'Y'
       and a.pubblica_casa_di_vetro = 'Y'
       and a.riservato = 'N'
       and a.valido = 'Y'
       and fa.valido = 'Y'
       and not exists
               (select 1
                  from delibere d, wkf_engine_iter wei
                 where d.id_proposta_delibera = pd.id_proposta_delibera
                   and d.valido = 'Y'
                   and d.id_engine_iter = wei.id_engine_iter
                   and (d.data_pubblicazione is not null
                     or wei.data_fine is not null))
    union all
    --allegati della delibera
    select d.id_delibera id_documento,
           a.id_allegato,
           fa.id_file_allegato,
           fa.id_file_esterno,
           a.titolo,
           a.descrizione,
           fa.content_type,
           fa.firmato,
           fa.nome,
           gdm_oggetti_file_pkg.downloadoggettofile (fa.id_file_esterno) testoocr
      from file_allegati fa,
           allegati_file af,
           allegati a,
           proposte_delibera pd,
           delibere d,
         --  wkf_engine_iter wei,  --vogliono il documento pubblicato anche prima dell'attivazione dell'iter
           tipi_delibera td
     where pd.id_tipo_delibera = td.id_tipo_delibera
       and a.id_delibera = d.id_delibera
   --    and d.id_engine_iter = wei.id_engine_iter
       and pd.id_proposta_delibera = d.id_proposta_delibera
     --  and (d.data_pubblicazione is not null or wei.data_fine is not null)
       and a.id_allegato = af.id_allegato
       and af.id_file = fa.id_file_allegato
       and td.gestione_gruppi_politici = 'Y'
       and pd.valido = 'Y'
       and pd.pubblica_casa_di_vetro = 'Y'
       and a.pubblica_casa_di_vetro = 'Y'
       and a.riservato = 'N'
       and a.valido = 'Y'
       and fa.valido = 'Y'
    union all
    --testo della delibera
    select d.id_delibera id_documento,
           d.id_file_allegato_testo,
           fa.id_file_allegato,
           fa.id_file_esterno,
           fa.nome,
           null,
           fa.content_type,
           fa.firmato,
           fa.nome,
           gdm_oggetti_file_pkg.downloadoggettofile (fa.id_file_esterno) testoocr
      from file_allegati fa,
           proposte_delibera pd,
           odg_oggetti_seduta oos,
           delibere d,
    --       wkf_engine_iter wei,
           tipi_delibera td,
           odg_esiti oe
     where pd.id_tipo_delibera = td.id_tipo_delibera
       and d.id_file_allegato_testo = fa.id_file_allegato
       and pd.id_proposta_delibera = d.id_proposta_delibera
    --   and d.id_engine_iter = wei.id_engine_iter
    --   and (d.data_pubblicazione is not null or wei.data_fine is not null)
       and td.gestione_gruppi_politici = 'Y'
       and pd.valido = 'Y'
       and pd.pubblica_casa_di_vetro = 'Y'
       and fa.valido = 'Y'
       and d.id_oggetto_seduta = oos.id_oggetto_seduta
       and oos.id_esito = oe.id_esito
       and oos.conferma_esito = 'Y'
/