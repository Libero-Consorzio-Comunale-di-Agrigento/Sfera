--liquibase formatted sql
--changeset rdestasio:install_20210221_viste_06 runOnChange:true

create or replace force view ricerca_unita_documenti_attivi
(
   tipo_documento
 , id_documento
 , id_documento_padre
 , id_atto
 , id_proposta
 , id_iter
 , anno_proposta
 , numero_proposta
 , registro_proposta
 , anno_atto
 , numero_atto
 , registro_atto
 , oggetto
 , data_esecutivita
 , data_pubblicazione_dal
 , data_pubblicazione_al
 , tipo_soggetto
 , soggetto_utente
 , soggetto_unita_progr
 , soggetto_unita_dal
 , soggetto_unita_ottica
 , attore_utente
 , attore_unita_progr
 , attore_unita_dal
 , attore_unita_ottica
 , stato
 , ente
)
as
   select tipo_documento
        , id_documento
        , id_documento_padre
        , id_atto
        , id_proposta
        , id_iter
        , anno_proposta
        , numero_proposta
        , registro_proposta
        , anno_atto
        , numero_atto
        , registro_atto
        , oggetto
        , data_esecutivita
        , data_pubblicazione_dal
        , data_pubblicazione_al
        , tipo_soggetto
        , soggetto_utente
        , soggetto_unita_progr
        , soggetto_unita_dal
        , soggetto_unita_ottica
        , attore_utente
        , attore_unita_progr
        , attore_unita_dal
        , attore_unita_ottica
        , stato
        , ente
     from (
     select 'DETERMINA' tipo_documento
                , d.id_determina id_documento
                , null id_documento_padre
                , d.id_determina id_atto
                , d.id_determina id_proposta
                , d.id_engine_iter id_iter
                , d.anno_proposta anno_proposta
                , d.numero_proposta numero_proposta
                , d.registro_proposta registro_proposta
                , d.anno_determina anno_atto
                , d.numero_determina numero_atto
                , d.registro_determina registro_atto
                , d.oggetto oggetto
                , d.data_esecutivita data_esecutivita
                , d.data_pubblicazione data_pubblicazione_dal
                , d.data_fine_pubblicazione data_pubblicazione_al
                , ds.tipo_soggetto
                , ds.utente soggetto_utente
                , ds.unita_progr soggetto_unita_progr
                , ds.unita_dal soggetto_unita_dal
                , ds.unita_ottica soggetto_unita_ottica
                , a.utente attore_utente
                , a.unita_progr attore_unita_progr
                , a.unita_dal attore_unita_dal
                , a.unita_ottica attore_unita_ottica
                , upper (decode (d.stato, 'ANNULLATO', d.stato, nvl (cs.titolo, d.stato))) as stato
                , d.ente
             from determine d, determine_soggetti ds
                , wkf_engine_iter i
                , wkf_engine_step s
                , wkf_cfg_step cs
                , wkf_engine_step_attori a
            where  i.id_engine_iter = d.id_engine_iter
              and s.id_engine_step = i.id_step_corrente
              and a.id_engine_step (+) = s.id_engine_step
              and cs.id_cfg_step = s.id_cfg_step
              and ((i.data_fine is null) or (exists (select 1 from certificati c where c.stato <> 'CONCLUSO' and c.id_determina = d.id_determina)))
              and ds.id_determina = d.id_determina and ds.utente is null and d.valido = 'Y'
           union all
           select 'PROPOSTA_DELIBERA' tipo_documento
                , d.id_proposta_delibera id_documento
                , null id_documento_padre
                , de.id_delibera id_atto
                , d.id_proposta_delibera id_proposta
                , d.id_engine_iter id_iter
                , d.anno_proposta anno_proposta
                , d.numero_proposta numero_proposta
                , d.registro_proposta registro_proposta
                , de.anno_delibera anno_atto
                , de.numero_delibera numero_atto
                , de.registro_delibera registro_atto
                , d.oggetto oggetto
                , de.data_esecutivita data_esecutivita
                , de.data_pubblicazione data_pubblicazione_dal
                , de.data_fine_pubblicazione data_pubblicazione_al
                , pds.tipo_soggetto
                , pds.utente soggetto_utente
                , pds.unita_progr soggetto_unita_progr
                , pds.unita_dal soggetto_unita_dal
                , pds.unita_ottica soggetto_unita_ottica
                , a.utente attore_utente
                , a.unita_progr attore_unita_progr
                , a.unita_dal attore_unita_dal
                , a.unita_ottica attore_unita_ottica
                , upper (decode (d.stato, 'ANNULLATO', d.stato, nvl (cs.titolo, d.stato))) as stato
                , d.ente
             from proposte_delibera d, delibere de, proposte_delibera_soggetti pds
                , wkf_engine_iter i
                , wkf_engine_step s
                , wkf_cfg_step cs
                , wkf_engine_step_attori a
            where i.id_engine_iter = d.id_engine_iter
              and s.id_engine_step = i.id_step_corrente
              and a.id_engine_step (+) = s.id_engine_step
              and cs.id_cfg_step = s.id_cfg_step
              and ((i.data_fine is null))-- or (exists (select 1 from certificati c where c.stato <> 'CONCLUSO' and c.id_delibera = de.id_delibera)))
              and de.id_proposta_delibera(+) = d.id_proposta_delibera
              and pds.id_proposta_delibera = d.id_proposta_delibera
              and pds.utente is null
              and d.valido = 'Y'
           union all
           select 'DELIBERA' tipo_documento
                , de.id_delibera id_documento
                , null id_documento_padre
                , de.id_delibera id_atto
                , d.id_proposta_delibera id_proposta
                , de.id_engine_iter id_iter
                , d.anno_proposta anno_proposta
                , d.numero_proposta numero_proposta
                , d.registro_proposta registro_proposta
                , de.anno_delibera anno_atto
                , de.numero_delibera numero_atto
                , de.registro_delibera registro_atto
                , d.oggetto oggetto
                , de.data_esecutivita data_esecutivita
                , de.data_pubblicazione data_pubblicazione_dal
                , de.data_fine_pubblicazione data_pubblicazione_al
                , des.tipo_soggetto
                , des.utente soggetto_utente
                , des.unita_progr soggetto_unita_progr
                , des.unita_dal soggetto_unita_dal
                , des.unita_ottica soggetto_unita_ottica
                , a.utente attore_utente
                , a.unita_progr attore_unita_progr
                , a.unita_dal attore_unita_dal
                , a.unita_ottica attore_unita_ottica
                , upper (decode (de.stato, 'ANNULLATO', de.stato, nvl (cs.titolo, de.stato))) as stato
                , d.ente
             from proposte_delibera d, delibere de, delibere_soggetti des
                , wkf_engine_iter i
                , wkf_engine_step s
                , wkf_cfg_step cs
                , wkf_engine_step_attori a
            where i.id_engine_iter = de.id_engine_iter
              and s.id_engine_step = i.id_step_corrente
              and a.id_engine_step (+) = s.id_engine_step
              and cs.id_cfg_step = s.id_cfg_step
              and ((i.data_fine is null) or (exists (select 1 from certificati c where c.stato <> 'CONCLUSO' and c.id_delibera = de.id_delibera)))
              and des.id_delibera = de.id_delibera and de.id_proposta_delibera = d.id_proposta_delibera and des.utente is null and de.valido = 'Y'
           union all
           select 'VISTO' tipo_documento
                , v.id_visto_parere id_documento
                , d.id_determina id_documento_padre
                , d.id_determina id_atto
                , d.id_determina id_proposta
                , v.id_engine_iter id_iter
                , d.anno_proposta anno_proposta
                , d.numero_proposta numero_proposta
                , d.registro_proposta registro_proposta
                , d.anno_determina anno_atto
                , d.numero_determina numero_atto
                , d.registro_determina registro_atto
                , d.oggetto oggetto
                , d.data_esecutivita data_esecutivita
                , d.data_pubblicazione data_pubblicazione_dal
                , d.data_fine_pubblicazione data_pubblicazione_al
                , null tipo_soggetto
                , v.utente_firmatario soggetto_utente
                , v.unita_progr soggetto_unita_progr
                , v.unita_dal soggetto_unita_dal
                , v.unita_ottica soggetto_unita_ottica
                , a.utente attore_utente
                , a.unita_progr attore_unita_progr
                , a.unita_dal attore_unita_dal
                , a.unita_ottica attore_unita_ottica
                , v.stato
                , v.ente
             from visti_pareri v
                , determine d
                , wkf_engine_iter i
                , wkf_engine_step s
                , wkf_cfg_step cs
                , wkf_engine_step_attori a
            where i.id_engine_iter(+) = v.id_engine_iter
              and s.id_engine_step = i.id_step_corrente
              and a.id_engine_step (+) = s.id_engine_step
              and cs.id_cfg_step = s.id_cfg_step
              and i.data_fine is null
              and v.stato <> 'CONCLUSO'
              and d.id_determina = v.id_determina
              and v.id_determina is not null
              and v.valido = 'Y'
              union all
              select 'PARERE' tipo_documento
                , v.id_visto_parere id_documento
                , nvl (de.id_delibera, pd.id_proposta_delibera) id_documento_padre
                , nvl (de.id_delibera, pd.id_proposta_delibera) id_atto
                , nvl (de.id_delibera, pd.id_proposta_delibera) id_proposta
                , v.id_engine_iter id_iter
                , pd.anno_proposta anno_proposta
                , pd.numero_proposta numero_proposta
                , pd.registro_proposta registro_proposta
                , de.anno_delibera anno_atto
                , de.numero_delibera numero_atto
                , de.registro_delibera registro_atto
                , nvl (de.oggetto, pd.oggetto) oggetto
                , de.data_esecutivita data_esecutivita
                , de.data_pubblicazione data_pubblicazione_dal
                , de.data_fine_pubblicazione data_pubblicazione_al
                , null tipo_soggetto
                , v.utente_firmatario soggetto_utente
                , v.unita_progr soggetto_unita_progr
                , v.unita_dal soggetto_unita_dal
                , v.unita_ottica soggetto_unita_ottica
                , a.utente attore_utente
                , a.unita_progr attore_unita_progr
                , a.unita_dal attore_unita_dal
                , a.unita_ottica attore_unita_ottica
                , v.stato
                , v.ente
             from visti_pareri v
                , proposte_delibera pd
                , delibere de
                , wkf_engine_iter i
                , wkf_engine_step s
                , wkf_cfg_step cs
                , wkf_engine_step_attori a
            where i.id_engine_iter(+) = v.id_engine_iter
              and s.id_engine_step = i.id_step_corrente
              and a.id_engine_step (+) = s.id_engine_step
              and cs.id_cfg_step = s.id_cfg_step
              and i.data_fine is null
              and v.stato <> 'CONCLUSO'
              and pd.id_proposta_delibera(+) = v.id_proposta_delibera
              and de.id_delibera(+) = v.id_delibera
              and (v.id_delibera is not null or v.id_proposta_delibera is not null)
              and v.valido = 'Y'
           union all
           select 'CERTIFICATO' tipo_documento
                , v.id_certificato id_documento
                , nvl (d.id_determina, nvl (de.id_delibera, pd.id_proposta_delibera)) id_documento_padre
                , nvl (d.id_determina, nvl (de.id_delibera, pd.id_proposta_delibera)) id_atto
                , nvl (d.id_determina, nvl (de.id_delibera, pd.id_proposta_delibera)) id_proposta
                , v.id_engine_iter id_iter
                , nvl (d.anno_proposta, pd.anno_proposta) anno_proposta
                , nvl (d.numero_proposta, pd.numero_proposta) numero_proposta
                , nvl (d.registro_proposta, pd.registro_proposta) registro_proposta
                , nvl (d.anno_determina, de.anno_delibera) anno_atto
                , nvl (d.numero_determina, de.numero_delibera) numero_atto
                , nvl (d.registro_determina, de.registro_delibera) registro_atto
                , nvl (d.oggetto, nvl (de.oggetto, pd.oggetto)) oggetto
                , nvl (d.data_esecutivita, de.data_esecutivita) data_esecutivita
                , nvl (d.data_pubblicazione, de.data_pubblicazione) data_pubblicazione_dal
                , nvl (d.data_fine_pubblicazione, de.data_fine_pubblicazione) data_pubblicazione_al
                , 'FIRMATARIO' tipo_soggetto
                , v.firmatario utente
                , null unita_progr
                , null unita_dal
                , null unita_ottica
                , a.utente attore_utente
                , a.unita_progr attore_unita_progr
                , a.unita_dal attore_unita_dal
                , a.unita_ottica attore_unita_ottica
                , v.stato
                , v.ente
             from certificati v
                , determine d
                , proposte_delibera pd
                , delibere de
                , wkf_engine_iter i
                , wkf_engine_step s
                , wkf_cfg_step cs
                , wkf_engine_step_attori a
            where i.id_engine_iter = v.id_engine_iter
              and s.id_engine_step = i.id_step_corrente
              and a.id_engine_step (+) = s.id_engine_step
              and cs.id_cfg_step = s.id_cfg_step
              and i.data_fine is null
              and d.id_determina(+) = v.id_determina
              and de.id_delibera(+) = v.id_delibera
              and pd.id_proposta_delibera(+) = de.id_proposta_delibera
              and v.valido = 'Y')
/