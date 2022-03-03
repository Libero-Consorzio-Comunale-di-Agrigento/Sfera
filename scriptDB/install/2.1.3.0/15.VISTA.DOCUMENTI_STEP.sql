--liquibase formatted sql
--changeset rdestasio:2.1.3.0_20200221_15

-- aggiunta la data di adozione

create or replace force view documenti_step
(
   tipo_oggetto
 , id_documento
 , id_determina
 , id_proposta_delibera
 , id_delibera
 , id_visto_parere
 , id_certificato
 , id_padre
 , id_tipologia
 , titolo_tipologia
 , descrizione_tipologia
 , anno_proposta
 , numero_proposta
 , anno
 , numero
 , oggetto
 , stato
 , stato_firma
 , stato_conservazione
 , stato_odg
 , unita_proponente
 , id_step
 , step_utente
 , step_unita_progr
 , step_unita_dal
 , step_unita_ottica
 , step_ruolo
 , step_nome
 , step_descrizione
 , step_titolo
 , ente
 , riservato
 , tipo_registro
 , data_adozione
)
as
   select 'DELIBERA'
        , deli.id_delibera
        , null
        ,                                                                                                         -- ID_DETERMINA,
         null
        ,                                                                                                 -- ID_PROPOSTA_DELIBERA,
         deli.id_delibera
        ,                                                                                                          -- ID_DELIBERA,
         null
        ,                                                                                                      -- ID_VISTO_PARERE,
         null
        ,                                                                                                       -- ID_CERTIFICATO,
         null
        , tipo.id_tipo_delibera
        , tipo.titolo
        , tipo.descrizione
        , pr_deli.anno_proposta
        , pr_deli.numero_proposta
        , deli.anno_delibera
        , deli.numero_delibera
        , deli.oggetto
        , deli.stato
        , deli.stato_firma
        , deli.stato_conservazione
        , pr_deli.stato_odg
        , utility_pkg.get_unita_prop_delibera (pr_deli.id_proposta_delibera)
        , step.id_engine_step
        , a_step.utente
        , a_step.unita_progr
        , a_step.unita_dal
        , a_step.unita_ottica
        , a_step.ruolo
        , cfg_step.nome
        , cfg_step.descrizione
        , cfg_step.titolo
        , pr_deli.ente
        , pr_deli.riservato
        , deli.registro_delibera
        , deli.data_adozione
     from proposte_delibera pr_deli
        , delibere deli
        , wkf_engine_step step
        , wkf_engine_step_attori a_step
        , wkf_engine_iter iter
        , wkf_cfg_step cfg_step
        , tipi_delibera tipo
        , tipi_registro registro
    where deli.id_engine_iter 		= iter.id_engine_iter
      and step.id_engine_step 		= iter.id_step_corrente
      and tipo.id_tipo_delibera 	= pr_deli.id_tipo_delibera
      and cfg_step.id_cfg_step 		= step.id_cfg_step
      and step.id_engine_step 		= a_step.id_engine_step
      and deli.id_proposta_delibera = pr_deli.id_proposta_delibera
      and iter.data_fine is null
      and step.data_fine is null
      and deli.registro_delibera 	= registro.tipo_registro(+)
      and pr_deli.valido 			= 'Y'
      and deli.valido 				= 'Y'
   union all
   select 'PROPOSTA_DELIBERA'
        , pr_deli.id_proposta_delibera
        , null
        ,                                                                                                         -- ID_DETERMINA,
         pr_deli.id_proposta_delibera
        ,                                                                                                 -- ID_PROPOSTA_DELIBERA,
         null
        ,                                                                                                          -- ID_DELIBERA,
         null
        ,                                                                                                      -- ID_VISTO_PARERE,
         null
        ,                                                                                                       -- ID_CERTIFICATO,
         null
        , tipo.id_tipo_delibera
        , tipo.titolo
        , tipo.descrizione
        , pr_deli.anno_proposta
        , pr_deli.numero_proposta
        , deli.anno_delibera
        , deli.numero_delibera
        , pr_deli.oggetto
        , pr_deli.stato
        , pr_deli.stato_firma
        , deli.stato_conservazione
        , pr_deli.stato_odg
        , utility_pkg.get_unita_prop_delibera (pr_deli.id_proposta_delibera)
        , step.id_engine_step
        , a_step.utente
        , a_step.unita_progr
        , a_step.unita_dal
        , a_step.unita_ottica
        , a_step.ruolo
        , cfg_step.nome
        , cfg_step.descrizione
        , cfg_step.titolo
        , pr_deli.ente
        , pr_deli.riservato
        , decode (deli.id_delibera
                , null, decode (tipo.id_tipo_registro_delibera, null, comm.id_tipo_registro, tipo.id_tipo_registro_delibera)
                , deli.registro_delibera)
        , null
     from proposte_delibera pr_deli
        , delibere deli
        , odg_commissioni comm
        , wkf_engine_step step
        , wkf_engine_step_attori a_step
        , wkf_engine_iter iter
        , wkf_cfg_step cfg_step
        , tipi_delibera tipo
    where pr_deli.id_engine_iter = iter.id_engine_iter
      and step.id_engine_step = iter.id_step_corrente
      and tipo.id_tipo_delibera = pr_deli.id_tipo_delibera
      and cfg_step.id_cfg_step = step.id_cfg_step
      and step.id_engine_step = a_step.id_engine_step
      and deli.id_proposta_delibera(+) = pr_deli.id_proposta_delibera
      and iter.data_fine is null
      and step.data_fine is null
      and tipo.id_tipo_delibera = pr_deli.id_tipo_delibera
      and pr_deli.id_commissione = comm.id_commissione
      and pr_deli.valido = 'Y'
      and deli.valido(+) = 'Y'
   union all
   select 'DETERMINA'
        , dete.id_determina
        , dete.id_determina
        ,                                                                                                         -- ID_DETERMINA,
         null
        ,                                                                                                 -- ID_PROPOSTA_DELIBERA,
         null
        ,                                                                                                          -- ID_DELIBERA,
         null
        ,                                                                                                      -- ID_VISTO_PARERE,
         null
        ,                                                                                                       -- ID_CERTIFICATO,
         null
        , tipo.id_tipo_determina
        , tipo.titolo
        , tipo.descrizione
        , dete.anno_proposta
        , dete.numero_proposta
        , dete.anno_determina
        , dete.numero_determina
        , dete.oggetto
        , dete.stato
        , dete.stato_firma
        , dete.stato_conservazione
        , dete.stato_odg
        , utility_pkg.get_unita_prop_determina (dete.id_determina)
        , step.id_engine_step
        , a_step.utente
        , a_step.unita_progr
        , a_step.unita_dal
        , a_step.unita_ottica
        , a_step.ruolo
        , cfg_step.nome
        , cfg_step.descrizione
        , cfg_step.titolo
        , dete.ente
        , dete.riservato
        , decode (dete.numero_determina, null, tipo.id_tipo_registro, dete.registro_determina)
        , dete.data_numero_determina
     from determine dete
        , wkf_engine_step step
        , wkf_engine_step_attori a_step
        , wkf_engine_iter iter
        , wkf_cfg_step cfg_step
        , tipi_determina tipo
        , tipi_registro registro
    where dete.id_engine_iter = iter.id_engine_iter
      and step.id_engine_step = iter.id_step_corrente
      and tipo.id_tipo_determina = dete.id_tipo_determina
      and cfg_step.id_cfg_step = step.id_cfg_step
      and step.id_engine_step = a_step.id_engine_step
      and iter.data_fine is null
      and step.data_fine is null
      and dete.valido = 'Y'
      and dete.registro_determina = registro.tipo_registro(+)
   union all
   select 'VISTO'
        , vp.id_visto_parere
        , null
        ,                                                                                                         -- ID_DETERMINA,
         null
        ,                                                                                                 -- ID_PROPOSTA_DELIBERA,
         null
        ,                                                                                                          -- ID_DELIBERA,
         vp.id_visto_parere
        ,                                                                                                      -- ID_VISTO_PARERE,
         null
        ,                                                                                                       -- ID_CERTIFICATO,
         dete.id_determina
        , tipo.id_tipo_visto_parere
        , tipo.titolo
        , tipo.descrizione
        , dete.anno_proposta
        , dete.numero_proposta
        , dete.anno_determina
        , dete.numero_determina
        , dete.oggetto
        , vp.stato
        , vp.stato_firma
        , dete.stato_conservazione
        , dete.stato_odg
        , utility_pkg.get_unita_prop_determina (dete.id_determina)
        , step.id_engine_step
        , a_step.utente
        , a_step.unita_progr
        , a_step.unita_dal
        , a_step.unita_ottica
        , a_step.ruolo
        , cfg_step.nome
        , cfg_step.descrizione
        , cfg_step.titolo
        , dete.ente
        , dete.riservato
        , decode (dete.numero_determina, null, tipo_dete.id_tipo_registro, dete.registro_determina)
        , dete.data_numero_determina
     from visti_pareri vp
        , wkf_engine_step step
        , wkf_engine_step_attori a_step
        , wkf_engine_iter iter
        , determine dete
        , wkf_cfg_step cfg_step
        , tipi_visto_parere tipo
        , tipi_determina tipo_dete
    where vp.id_determina = dete.id_determina
      and step.id_engine_step = iter.id_step_corrente
      and vp.id_engine_iter = iter.id_engine_iter
      and cfg_step.id_cfg_step = step.id_cfg_step
      and tipo.id_tipo_visto_parere = vp.id_tipologia
      and step.id_engine_step = a_step.id_engine_step
      and iter.data_fine is null
      and step.data_fine is null
      and dete.id_tipo_determina = tipo_dete.id_tipo_determina
      and vp.valido = 'Y'
   union all
   select 'PARERE'
        ,                                                                                     -- pareri della PROPOSTA DI DELIBERA
         vp.id_visto_parere
        , null
        ,                                                                                                         -- ID_DETERMINA,
         null
        ,                                                                                                 -- ID_PROPOSTA_DELIBERA,
         null
        ,                                                                                                          -- ID_DELIBERA,
         vp.id_visto_parere
        ,                                                                                                      -- ID_VISTO_PARERE,
         null
        ,                                                                                                       -- ID_CERTIFICATO,
         pr_deli.id_proposta_delibera
        , tipo.id_tipo_visto_parere
        , tipo.titolo
        , tipo.descrizione
        , pr_deli.anno_proposta
        , pr_deli.numero_proposta
        , deli.anno_delibera
        , deli.numero_delibera
        , pr_deli.oggetto
        , vp.stato
        , vp.stato_firma
        , deli.stato_conservazione
        , pr_deli.stato_odg
        , utility_pkg.get_unita_prop_delibera (pr_deli.id_proposta_delibera)
        , step.id_engine_step
        , a_step.utente
        , a_step.unita_progr
        , a_step.unita_dal
        , a_step.unita_ottica
        , a_step.ruolo
        , cfg_step.nome
        , cfg_step.descrizione
        , cfg_step.titolo
        , pr_deli.ente
        , pr_deli.riservato
        , decode (
             deli.id_delibera
           , null, decode (tipo_deli.id_tipo_registro_delibera, null, comm.id_tipo_registro, tipo_deli.id_tipo_registro_delibera)
           , deli.registro_delibera)
        , deli.data_adozione
     from visti_pareri vp
        , wkf_engine_step step
        , wkf_engine_step_attori a_step
        , wkf_engine_iter iter
        , proposte_delibera pr_deli
        , delibere deli
        , wkf_cfg_step cfg_step
        , tipi_visto_parere tipo
        , odg_commissioni comm
        , tipi_delibera tipo_deli
    where vp.id_proposta_delibera = pr_deli.id_proposta_delibera
      and step.id_engine_step = iter.id_step_corrente
      and vp.id_engine_iter = iter.id_engine_iter
      and cfg_step.id_cfg_step = step.id_cfg_step
      and tipo.id_tipo_visto_parere = vp.id_tipologia
      and step.id_engine_step = a_step.id_engine_step
      and deli.id_proposta_delibera(+) = pr_deli.id_proposta_delibera
      and iter.data_fine is null
      and step.data_fine is null
      and vp.valido = 'Y'
      and tipo_deli.id_tipo_delibera = pr_deli.id_tipo_delibera
      and pr_deli.id_commissione = comm.id_commissione
   union all
   select 'PARERE'
        ,                                                                                                 -- PARERI DELLA DELIBERA
         vp.id_visto_parere
        , null
        ,                                                                                                         -- ID_DETERMINA,
         null
        ,                                                                                                 -- ID_PROPOSTA_DELIBERA,
         null
        ,                                                                                                          -- ID_DELIBERA,
         vp.id_visto_parere
        ,                                                                                                      -- ID_VISTO_PARERE,
         null
        ,                                                                                                       -- ID_CERTIFICATO,
         pr_deli.id_proposta_delibera
        , tipo.id_tipo_visto_parere
        , tipo.titolo
        , tipo.descrizione
        , pr_deli.anno_proposta
        , pr_deli.numero_proposta
        , deli.anno_delibera
        , deli.numero_delibera
        , pr_deli.oggetto
        , vp.stato
        , vp.stato_firma
        , deli.stato_conservazione
        , pr_deli.stato_odg
        , utility_pkg.get_unita_prop_delibera (pr_deli.id_proposta_delibera)
        , step.id_engine_step
        , a_step.utente
        , a_step.unita_progr
        , a_step.unita_dal
        , a_step.unita_ottica
        , a_step.ruolo
        , cfg_step.nome
        , cfg_step.descrizione
        , cfg_step.titolo
        , pr_deli.ente
        , pr_deli.riservato
        , decode (
             deli.id_delibera
           , null, decode (tipo_deli.id_tipo_registro_delibera, null, comm.id_tipo_registro, tipo_deli.id_tipo_registro_delibera)
           , deli.registro_delibera)
        , deli.data_adozione
     from visti_pareri vp
        , wkf_engine_step step
        , wkf_engine_step_attori a_step
        , wkf_engine_iter iter
        , proposte_delibera pr_deli
        , delibere deli
        , wkf_cfg_step cfg_step
        , tipi_visto_parere tipo
        , odg_commissioni comm
        , tipi_delibera tipo_deli
    where vp.id_delibera = deli.id_delibera
      and step.id_engine_step = iter.id_step_corrente
      and vp.id_engine_iter = iter.id_engine_iter
      and cfg_step.id_cfg_step = step.id_cfg_step
      and tipo.id_tipo_visto_parere = vp.id_tipologia
      and step.id_engine_step = a_step.id_engine_step
      and deli.id_proposta_delibera(+) = pr_deli.id_proposta_delibera
      and iter.data_fine is null
      and step.data_fine is null
      and vp.valido = 'Y'
      and tipo_deli.id_tipo_delibera = pr_deli.id_tipo_delibera
      and pr_deli.id_commissione = comm.id_commissione
   union all
   select 'CERTIFICATO'
        , cert.id_certificato
        , null
        ,                                                                                                         -- ID_DETERMINA,
         null
        ,                                                                                                 -- ID_PROPOSTA_DELIBERA,
         null
        ,                                                                                                          -- ID_DELIBERA,
         null
        ,                                                                                                      -- ID_VISTO_PARERE,
         cert.id_certificato
        ,                                                                                                       -- ID_CERTIFICATO,
         deli.id_delibera
        , null
        , 'CERTIFICATO DI ' ||
          decode (cert.tipo
                , 'AVVENUTA_PUBBLICAZIONE', 'AVVENUTA PUBBLICAZIONE'
                , 'IMMEDIATA_ESEGUIBILITA', 'IMMEDIATA ESEGUIBILITA'
                , cert.tipo)
        , ''
        , pr_deli.anno_proposta
        , pr_deli.numero_proposta
        , deli.anno_delibera
        , deli.numero_delibera
        , deli.oggetto
        , cert.stato
        , cert.stato_firma
        , deli.stato_conservazione
        , pr_deli.stato_odg
        , utility_pkg.get_unita_prop_delibera (pr_deli.id_proposta_delibera)
        , step.id_engine_step
        , a_step.utente
        , a_step.unita_progr
        , a_step.unita_dal
        , a_step.unita_ottica
        , a_step.ruolo
        , cfg_step.nome
        , cfg_step.descrizione
        , cfg_step.titolo
        , cert.ente
        , pr_deli.riservato
        , deli.registro_delibera
        , deli.data_adozione
     from certificati cert
        , wkf_engine_step step
        , wkf_engine_step_attori a_step
        , wkf_engine_iter iter
        , proposte_delibera pr_deli
        , delibere deli
        , wkf_cfg_step cfg_step
    where cert.id_delibera = deli.id_delibera
      and pr_deli.id_proposta_delibera = deli.id_proposta_delibera
      and cert.id_engine_iter = iter.id_engine_iter
      and step.id_engine_step = iter.id_step_corrente
      and cfg_step.id_cfg_step = step.id_cfg_step
      and step.id_engine_step = a_step.id_engine_step
      and iter.data_fine is null
      and step.data_fine is null
      and cert.valido = 'Y'
   union all
   select 'CERTIFICATO'
        , cert.id_certificato
        , null
        ,                                                                                                         -- ID_DETERMINA,
         null
        ,                                                                                                 -- ID_PROPOSTA_DELIBERA,
         null
        ,                                                                                                          -- ID_DELIBERA,
         null
        ,                                                                                                      -- ID_VISTO_PARERE,
         cert.id_certificato
        ,                                                                                                       -- ID_CERTIFICATO,
         dete.id_determina
        , null
        , 'CERTIFICATO DI ' || decode (cert.tipo, 'AVVENUTA_PUBBLICAZIONE', 'AVVENUTA PUBBLICAZIONE', cert.tipo)
        , ''
        , dete.anno_proposta
        , dete.numero_proposta
        , dete.anno_determina
        , dete.numero_determina
        , dete.oggetto
        , cert.stato
        , cert.stato_firma
        , dete.stato_conservazione
        , dete.stato_odg
        , utility_pkg.get_unita_prop_determina (dete.id_determina)
        , step.id_engine_step
        , a_step.utente
        , a_step.unita_progr
        , a_step.unita_dal
        , a_step.unita_ottica
        , a_step.ruolo
        , cfg_step.nome
        , cfg_step.descrizione
        , cfg_step.titolo
        , cert.ente
        , dete.riservato
        , dete.registro_determina
        , dete.data_numero_determina
     from certificati cert
        , wkf_engine_step step
        , wkf_engine_step_attori a_step
        , wkf_engine_iter iter
        , determine dete
        , wkf_cfg_step cfg_step
    where cert.id_determina = dete.id_determina
      and cert.id_engine_iter = iter.id_engine_iter
      and step.id_engine_step = iter.id_step_corrente
      and cfg_step.id_cfg_step = step.id_cfg_step
      and step.id_engine_step = a_step.id_engine_step
      and iter.data_fine is null
      and step.data_fine is null
      and cert.valido = 'Y'
/
