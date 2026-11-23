--liquibase formatted sql
--changeset mfrancesconi:install_20220307_pkg runOnChange:true

CREATE OR REPLACE PACKAGE        competenze_pkg
AS
   /*
    Controlla le competenze in lettura di un utente per uno specifico documento
    Ritorna 1 se l'utente ha i diritti di lettura, 0 altrimenti
    */

   function controlla_competenze_documento (p_id_documento number, p_utente varchar2, p_lettura number default 1, p_modifica number default 0, p_cancellazione number default 0, p_controlla_riservatezza number default 1, p_forza_riservato number default 0) RETURN NUMBER;

   function controlla_competenze_determina (p_id_determina number, p_utente varchar2, p_lettura number default 1, p_modifica number default 0, p_cancellazione number default 0, p_controlla_riservatezza number default 1, p_forza_riservato number default 0) RETURN NUMBER;

   function controlla_competenze_delibera (p_id_delibera number, p_utente varchar2, p_lettura number default 1, p_modifica number default 0, p_cancellazione number default 0, p_controlla_riservatezza number default 1, p_forza_riservato number default 0) RETURN NUMBER;

   function controlla_competenze_prop_del (p_id_proposta_delibera number, p_utente varchar2, p_lettura number default 1, p_modifica number default 0, p_cancellazione number default 0, p_controlla_riservatezza number default 1, p_forza_riservato number default 0) RETURN NUMBER;

   function controlla_competenze_visto_par (p_id_visto_parere number, p_utente varchar2, p_lettura number default 1, p_modifica number default 0, p_cancellazione number default 0, p_controlla_riservatezza number default 1, p_forza_riservato number default 0) RETURN NUMBER;

   function controlla_competenze_cert (p_id_certificato number, p_utente varchar2, p_lettura number default 1, p_modifica number default 0, p_cancellazione number default 0, p_controlla_riservatezza number default 1, p_forza_riservato number default 0) RETURN NUMBER;
   
   function controlla_competenze_doc (p_id_documento number, p_utente varchar2, p_lettura number default 1, p_modifica number default 0, p_cancellazione number default 0, p_controlla_riservatezza number default 1, p_forza_riservato number default 0) RETURN NUMBER;

END competenze_pkg;
/

CREATE OR REPLACE package body competenze_pkg
as
   function get_tipo_documento (p_id_documento number)
      return varchar2
   is
      d_tipo_documento   varchar2 (255);
   begin
      select tipo_documento
        into d_tipo_documento
        from (select 'DETERMINA' as tipo_documento
                from determine d
               where d.id_determina = p_id_documento
              union all
              select 'DELIBERA' as tipo_documento
                from delibere d
               where d.id_delibera = p_id_documento
              union all
              select 'PROPOSTA_DELIBERA' as tipo_documento
                from proposte_delibera p
               where p.id_proposta_delibera = p_id_documento
              union all
              select 'VISTO_PARERE' as tipo_documento
                from visti_pareri vp
               where vp.id_visto_parere = p_id_documento
              union all
              select 'CERTIFICATO' as tipo_documento
                from certificati c
               where c.id_certificato = p_id_documento
              union all
              select c.tipo_oggetto as tipo_documento
                from gdo_documenti c
               where c.id_documento = p_id_documento);

      return d_tipo_documento;
   end;

   /*
    Controlla le competenze di un documento qualsiasi (determina, delibera, stampa seduta, certificato, etc)
    NB: non so bene da chi sia usata questa funzione.
   */
   function controlla_competenze_documento (p_id_documento  number
                                          , p_utente        varchar2
                                          , p_lettura       number default 1
                                          , p_modifica      number default 0
                                          , p_cancellazione number default 0
                                          , p_controlla_riservatezza number default 1
                                          , p_forza_riservato number default 0)
      return number
   is
      d_tipo_documento   varchar (255);
   begin
      d_tipo_documento   := get_tipo_documento (p_id_documento);

      case
         when d_tipo_documento = 'DETERMINA' then
            return controlla_competenze_determina (p_id_documento
                                                 , p_utente
                                                 , p_lettura
                                                 , p_modifica
                                                 , p_cancellazione
                                                 , p_controlla_riservatezza
                                                 , p_forza_riservato);
         when d_tipo_documento = 'DELIBERA' then
            return controlla_competenze_delibera (p_id_documento
                                                , p_utente
                                                , p_lettura
                                                , p_modifica
                                                , p_cancellazione
                                                , p_controlla_riservatezza
                                                , p_forza_riservato);
         when d_tipo_documento = 'PROPOSTA_DELIBERA' then
            return controlla_competenze_prop_del (p_id_documento
                                                , p_utente
                                                , p_lettura
                                                , p_modifica
                                                , p_cancellazione
                                                , p_controlla_riservatezza
                                                , p_forza_riservato);
         when d_tipo_documento = 'VISTO_PARERE' then
            return controlla_competenze_visto_par (p_id_documento
                                                 , p_utente
                                                 , p_lettura
                                                 , p_modifica
                                                 , p_cancellazione
                                                 , p_controlla_riservatezza
                                                 , p_forza_riservato);
         when d_tipo_documento = 'CERTIFICATO' then
            return controlla_competenze_cert (p_id_documento
                                            , p_utente
                                            , p_lettura
                                            , p_modifica
                                            , p_cancellazione
                                            , p_controlla_riservatezza
                                            , p_forza_riservato);
         else
            return controlla_competenze_doc (p_id_documento
                                           , p_utente
                                           , p_lettura
                                           , p_modifica
                                           , p_cancellazione
                                           , p_controlla_riservatezza
                                           , p_forza_riservato);
      end case;
   end;

   function controlla_competenze_determina (p_id_determina  number
                                          , p_utente        varchar2
                                          , p_lettura       number default 1
                                          , p_modifica      number default 0
                                          , p_cancellazione number default 0
                                          , p_controlla_riservatezza number default 1
                                          , p_forza_riservato number default 0)
      return number
   is
      d_ottica             varchar (40);
      p_ente               varchar (40);
      d_ruolo_access_app   varchar (40);
      d_ruolo_riservato    varchar (40);
      d_lettura            number (1);
      d_modifica           number (1);
      d_cancellazione      number (1);
      d_riservato          number (1);
   begin
      begin
         select d.ente, decode (d.riservato, 'Y', 1, 0), nvl (td.ruolo_riservato, impostazioni_pkg.get_impostazione ('RUOLO_SO4_RISERVATO_DETE', d.ente))
           into p_ente, d_riservato, d_ruolo_riservato
           from determine d, tipi_determina td
          where d.id_determina = p_id_determina and d.id_tipo_determina = td.id_tipo_determina and d.valido = 'Y';
      exception
         when no_data_found then
            return 0;
      end;

      d_ruolo_access_app   := impostazioni_pkg.get_impostazione ('RUOLO_ACCESSO_APPLICATIVO', p_ente);
      d_ottica             := impostazioni_pkg.get_impostazione ('OTTICA_SO4', p_ente);

      -- devo controllare la riservatezza solo se il documento è riservato e il parametro in input richiede
      -- di controllare la riservatezza.
      if ( (d_riservato = 1 or p_forza_riservato = 1) and p_controlla_riservatezza = 1) then
         d_riservato   := 1;
      else
         d_riservato   := 0;
      end if;

      begin
           select decode (max (dc.lettura), 'Y', 1, 0), decode (max (dc.modifica), 'Y', 1, 0), decode (max (dc.cancellazione), 'Y', 1, 0)
             into d_lettura, d_modifica, d_cancellazione
             from so4_v_utenti_ruoli_sogg_uo uo_r, determine_competenze dc
            where uo_r.ottica = d_ottica
              and uo_r.utente = p_utente
              and (dc.utente = p_utente
                or (dc.unita_progr is null
                and dc.ruolo is not null
                and dc.ruolo = uo_r.ruolo
                and (d_riservato = 0
                  or not (dc.ruolo = d_ruolo_access_app)
                 and exists
                        (select 1
                           from so4_v_utenti_ruoli_sogg_uo r
                          where r.ruolo = d_ruolo_riservato and r.utente = p_utente)))
                or (dc.unita_progr is not null
                and dc.unita_progr = uo_r.uo_progr
                and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)
                and (d_riservato = 0
                  or exists
                        (select 1
                           from so4_v_utenti_ruoli_sogg_uo r
                          where r.ruolo = d_ruolo_riservato and r.utente = p_utente and r.uo_progr = dc.unita_progr and r.uo_dal = dc.unita_dal and r.ottica = dc.unita_ottica))))
              and dc.id_determina = p_id_determina
         group by dc.id_determina;

         if (p_cancellazione = d_cancellazione) then
            return 1;
         end if;

         if (p_modifica = d_modifica) then
            return 1;
         end if;

         if (p_lettura = d_lettura) then
            return 1;
         end if;

         return 0;
      exception
         when no_data_found then
            return 0;
      end;
   end;

   function controlla_competenze_delibera (p_id_delibera   number
                                         , p_utente        varchar2
                                         , p_lettura       number default 1
                                         , p_modifica      number default 0
                                         , p_cancellazione number default 0
                                         , p_controlla_riservatezza number default 1
                                         , p_forza_riservato number default 0)
      return number
   is
      d_ottica             varchar (40);
      p_ente               varchar (40);
      d_ruolo_access_app   varchar (40);
      d_ruolo_riservato    varchar (40);
      d_lettura            number (1);
      d_modifica           number (1);
      d_cancellazione      number (1);
      d_riservato          number (1);
   begin
      begin
         select d.ente, decode (d.riservato, 'Y', 1, 0), nvl (td.ruolo_riservato, impostazioni_pkg.get_impostazione ('RUOLO_SO4_RISERVATO_DELI', d.ente))
           into p_ente, d_riservato, d_ruolo_riservato
           from delibere d, proposte_delibera pd, tipi_delibera td
          where d.id_delibera = p_id_delibera and d.id_proposta_delibera = pd.id_proposta_delibera and pd.id_tipo_delibera = td.id_tipo_delibera and d.valido = 'Y';
      exception
         when no_data_found then
            return 0;
      end;

      d_ruolo_access_app   := impostazioni_pkg.get_impostazione ('RUOLO_ACCESSO_APPLICATIVO', p_ente);
      d_ottica             := impostazioni_pkg.get_impostazione ('OTTICA_SO4', p_ente);

      -- devo controllare la riservatezza solo se il documento è riservato e il parametro in input richiede
      -- di controllare la riservatezza.
      if ( (d_riservato = 1 or p_forza_riservato = 1) and p_controlla_riservatezza = 1) then
         d_riservato   := 1;
      else
         d_riservato   := 0;
      end if;

      begin
           select decode (max (dc.lettura), 'Y', 1, 0), decode (max (dc.modifica), 'Y', 1, 0), decode (max (dc.cancellazione), 'Y', 1, 0)
             into d_lettura, d_modifica, d_cancellazione
             from so4_v_utenti_ruoli_sogg_uo uo_r, delibere_competenze dc
            where uo_r.ottica = d_ottica
              and uo_r.utente = p_utente
              and (dc.utente = p_utente
                or (dc.unita_progr is null
                and dc.ruolo is not null
                and dc.ruolo = uo_r.ruolo
                and (d_riservato = 0
                  or not (dc.ruolo = d_ruolo_access_app)
                 and exists
                        (select 1
                           from so4_v_utenti_ruoli_sogg_uo r
                          where r.ruolo = d_ruolo_riservato and r.utente = p_utente)))
                or (dc.unita_progr is not null
                and dc.unita_progr = uo_r.uo_progr
                and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)
                and (d_riservato = 0
                  or exists
                        (select 1
                           from so4_v_utenti_ruoli_sogg_uo r
                          where r.ruolo = d_ruolo_riservato and r.utente = p_utente and r.uo_progr = dc.unita_progr and r.uo_dal = dc.unita_dal and r.ottica = dc.unita_ottica))))
              and dc.id_delibera = p_id_delibera
         group by dc.id_delibera;

         if (p_cancellazione = d_cancellazione) then
            return 1;
         end if;

         if (p_modifica = d_modifica) then
            return 1;
         end if;

         if (p_lettura = d_lettura) then
            return 1;
         end if;

         return 0;
      exception
         when no_data_found then
            return 0;
      end;
   end;

   function controlla_competenze_prop_del (p_id_proposta_delibera number
                                         , p_utente        varchar2
                                         , p_lettura       number default 1
                                         , p_modifica      number default 0
                                         , p_cancellazione number default 0
                                         , p_controlla_riservatezza number default 1
                                         , p_forza_riservato number default 0)
      return number
   is
      d_ottica              varchar (40);
      p_ente                varchar (40);
      d_ruolo_access_app    varchar (40);
      d_ruolo_riservato     varchar (40);
      d_lettura             number (1);
      d_modifica            number (1);
      d_cancellazione       number (1);
      d_riservato           number (1);
      d_stato_odg           varchar (255);
      d_id_oggetto_seduta   number (19);
      d_ni                  number (19);
      d_convocato           number (19);
      d_ruolo_visualizza    varchar2 (255);
      d_ha_ruoli            number (19);
   begin
      begin
         select d.ente, decode (d.riservato, 'Y', 1, 0), nvl (td.ruolo_riservato, impostazioni_pkg.get_impostazione ('RUOLO_SO4_RISERVATO_DELI', d.ente))
           into p_ente, d_riservato, d_ruolo_riservato
           from proposte_delibera d, tipi_delibera td
          where d.id_proposta_delibera = p_id_proposta_delibera and d.id_tipo_delibera = td.id_tipo_delibera and d.valido = 'Y';
      exception
         when no_data_found then
            return 0;
      end;

      d_ruolo_access_app   := impostazioni_pkg.get_impostazione ('RUOLO_ACCESSO_APPLICATIVO', p_ente);
      d_ottica             := impostazioni_pkg.get_impostazione ('OTTICA_SO4', p_ente);

      -- devo controllare la riservatezza solo se il documento è riservato e il parametro in input richiede
      -- di controllare la riservatezza oppure se è richiesto di forzare il controllo.
      if ( (d_riservato = 1 or p_forza_riservato = 1) and p_controlla_riservatezza = 1) then
         d_riservato   := 1;
      else
         d_riservato   := 0;
      end if;

      begin
           select decode (max (dc.lettura), 'Y', 1, 0), decode (max (dc.modifica), 'Y', 1, 0), decode (max (dc.cancellazione), 'Y', 1, 0)
             into d_lettura, d_modifica, d_cancellazione
             from so4_v_utenti_ruoli_sogg_uo uo_r, proposte_delibera_competenze dc
            where uo_r.ottica = d_ottica
              and uo_r.utente = p_utente
              and (dc.utente = p_utente
                or (dc.unita_progr is null
                and dc.ruolo is not null
                and dc.ruolo = uo_r.ruolo
                and (d_riservato = 0
                  or not (dc.ruolo = d_ruolo_access_app)
                 and exists
                        (select 1
                           from so4_v_utenti_ruoli_sogg_uo r
                          where r.ruolo = d_ruolo_riservato and r.utente = p_utente)))
                or (dc.unita_progr is not null
                and dc.unita_progr = uo_r.uo_progr
                and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)
                and (d_riservato = 0
                  or exists
                        (select 1
                           from so4_v_utenti_ruoli_sogg_uo r
                          where r.ruolo = d_ruolo_riservato and r.utente = p_utente and r.uo_progr = dc.unita_progr and r.uo_dal = dc.unita_dal and r.ottica = dc.unita_ottica))))
              and dc.id_proposta_delibera = p_id_proposta_delibera
         group by dc.id_proposta_delibera;

         if (p_cancellazione = d_cancellazione) then
            return 1;
         end if;

         if (p_modifica = d_modifica) then
            return 1;
         end if;

         if (p_lettura = d_lettura) then
            return 1;
         end if;
      exception
         when no_data_found then
            null;
      end;

      if (d_riservato = 1) then
         begin
            --se il documento è riservato, verifico se il documento è in seduta e se l'utente è convocato o ha i ruoli di visulizzazione
            select stato_odg, id_oggetto_seduta
              into d_stato_odg, d_id_oggetto_seduta
              from proposte_delibera pd
             where id_proposta_delibera = p_id_proposta_delibera;

            -- verifico se il documento è in seduta
            if (d_id_oggetto_seduta is not null and (d_stato_odg = 'NON_VERBALIZZARE' or d_stato_odg = 'INSERITO' or d_stato_odg = 'CONCLUSO')) then
               -- recupero l'ni dell'utente collegato
               select ni
                 into d_ni
                 from as4_v_soggetti_correnti
                where utente = p_utente;

               --verifico se il soggetto è convocato alla seduta
               select count (1)
                 into d_convocato
                 from odg_commissioni_componenti occ, odg_sedute_partecipanti osp, odg_oggetti_seduta oos
                where oos.id_oggetto_seduta = d_id_oggetto_seduta
                  and osp.id_seduta = oos.id_seduta
                  and ( (osp.id_commissione_componente = occ.id_commissione_componente and occ.ni_componente = d_ni) or (osp.ni_componente_esterno = d_ni));

               if d_convocato > 0 then
                  return 1;
               end if;

               --se l'utente non è covnocato allora deve avere sia il ruolo definito in commissione (per la visualizzazione) che il ruolo definito per i riservati
               select ruolo_visualizza
                 into d_ruolo_visualizza
                 from odg_commissioni oc, odg_sedute os, odg_oggetti_seduta oos
                where oos.id_oggetto_seduta = d_id_oggetto_seduta and oos.id_seduta = os.id_seduta and os.id_commissione = oc.id_commissione;

               if (d_ruolo_visualizza is not null) then
                  --l'utente deve avere sia questo ruolo che il ruolo dei riservati per la stessa unità

                  select count (1)
                    into d_ha_ruoli
                    from so4_v_utenti_ruoli_sogg_uo r
                   where r.ruolo = d_ruolo_visualizza
                     and r.utente = p_utente
                     and (r.ruolo_al is null or r.ruolo_al > sysdate)
                     and (r.comp_al is null or r.comp_al > sysdate)
                     and (r.uo_al is null or r.uo_al > sysdate)
                     and exists
                            (select 1
                               from so4_v_utenti_ruoli_sogg_uo r1
                              where r1.uo_progr = r.uo_progr
                                and r1.uo_dal = r.uo_dal
                                and r1.ottica = r.ottica
                                and r1.ruolo = d_ruolo_riservato
                                and r1.utente = p_utente
                                and (r1.ruolo_al is null or r1.ruolo_al > sysdate)
                                and (r1.comp_al is null or r1.comp_al > sysdate)
                                and (r1.uo_al is null or r1.uo_al > sysdate));

                  if d_ha_ruoli > 0 then
                     return 1;
                  end if;
               end if;
            end if;
         exception
            when no_data_found then
               null;
         end;
      end if;

      return 0;
   end;

   function controlla_competenze_visto_par (p_id_visto_parere number
                                          , p_utente        varchar2
                                          , p_lettura       number default 1
                                          , p_modifica      number default 0
                                          , p_cancellazione number default 0
                                          , p_controlla_riservatezza number default 1
                                          , p_forza_riservato number default 0)
      return number
   is
      d_ottica             varchar (40);
      p_ente               varchar (40);
      d_ruolo_access_app   varchar (40);
      d_ruolo_riservato    varchar (40);
      d_lettura            number (1);
      d_modifica           number (1);
      d_cancellazione      number (1);
      d_riservato          number (1);
   begin
      /*
       *  NOTA: per il visto/parere non si verifica la riservatezza.
       */

      begin
         select ente
           into p_ente
           from visti_pareri d
          where d.id_visto_parere = p_id_visto_parere and valido = 'Y';
      exception
         when no_data_found then
            return 0;
      end;

      d_ruolo_access_app   := impostazioni_pkg.get_impostazione ('RUOLO_ACCESSO_APPLICATIVO', p_ente);
      d_ottica             := impostazioni_pkg.get_impostazione ('OTTICA_SO4', p_ente);
      d_ruolo_riservato    := impostazioni_pkg.get_impostazione ('RUOLO_SO4_RISERVATO_DETE', p_ente);

      begin
           select decode (max (dc.lettura), 'Y', 1, 0), decode (max (dc.modifica), 'Y', 1, 0), decode (max (dc.cancellazione), 'Y', 1, 0)
             into d_lettura, d_modifica, d_cancellazione
             from so4_v_utenti_ruoli_sogg_uo uo_r, visti_pareri_competenze dc
            where uo_r.ottica = d_ottica
              and uo_r.utente = p_utente
              and (dc.utente = p_utente
                or (dc.unita_progr is null and dc.ruolo is not null and dc.ruolo = uo_r.ruolo)
                or (dc.unita_progr is not null and dc.unita_progr = uo_r.uo_progr and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)))
              and dc.id_visto_parere = p_id_visto_parere
         group by dc.id_visto_parere;

         if (p_cancellazione = d_cancellazione) then
            return 1;
         end if;

         if (p_modifica = d_modifica) then
            return 1;
         end if;

         if (p_lettura = d_lettura) then
            return 1;
         end if;

         return 0;
      exception
         when no_data_found then
            return 0;
      end;
   end;

   function controlla_competenze_cert (p_id_certificato number
                                     , p_utente        varchar2
                                     , p_lettura       number default 1
                                     , p_modifica      number default 0
                                     , p_cancellazione number default 0
                                     , p_controlla_riservatezza number default 1
                                     , p_forza_riservato number default 0)
      return number
   is
      d_ottica             varchar (40);
      p_ente               varchar (40);
      d_ruolo_access_app   varchar (40);
      d_ruolo_riservato    varchar (40);
      d_lettura            number (1);
      d_modifica           number (1);
      d_cancellazione      number (1);
      d_riservato          number (1);
   begin
      /*
       *  NOTA: per il visto/parere non si verifica la riservatezza.
       */

      begin
         select ente
           into p_ente
           from certificati d
          where d.id_certificato = p_id_certificato and valido = 'Y';
      exception
         when no_data_found then
            return 0;
      end;

      d_ruolo_access_app   := impostazioni_pkg.get_impostazione ('RUOLO_ACCESSO_APPLICATIVO', p_ente);
      d_ottica             := impostazioni_pkg.get_impostazione ('OTTICA_SO4', p_ente);
      d_ruolo_riservato    := impostazioni_pkg.get_impostazione ('RUOLO_SO4_RISERVATO_DETE', p_ente);

      begin
           select decode (max (dc.lettura), 'Y', 1, 0), decode (max (dc.modifica), 'Y', 1, 0), decode (max (dc.cancellazione), 'Y', 1, 0)
             into d_lettura, d_modifica, d_cancellazione
             from so4_v_utenti_ruoli_sogg_uo uo_r, certificati_competenze dc
            where uo_r.ottica = d_ottica
              and uo_r.utente = p_utente
              and (dc.utente = p_utente
                or (dc.unita_progr is null and dc.ruolo is not null and dc.ruolo = uo_r.ruolo)
                or (dc.unita_progr is not null and dc.unita_progr = uo_r.uo_progr and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)))
              and dc.id_certificato = p_id_certificato
         group by dc.id_certificato;

         if (p_cancellazione = d_cancellazione) then
            return 1;
         end if;

         if (p_modifica = d_modifica) then
            return 1;
         end if;

         if (p_lettura = d_lettura) then
            return 1;
         end if;

         return 0;
      exception
         when no_data_found then
            return 0;
      end;
   end;

   /*
    Controlla le competenze dei documenti presenti sulla tabella odg_documenti.
   */
   function controlla_competenze_doc (p_id_documento  number
                                    , p_utente        varchar2
                                    , p_lettura       number default 1
                                    , p_modifica      number default 0
                                    , p_cancellazione number default 0
                                    , p_controlla_riservatezza number default 1
                                    , p_forza_riservato number default 0)
      return number
   is
      d_ottica             varchar (40);
      p_ente               varchar (40);
      d_ruolo_access_app   varchar (40);
      d_ruolo_riservato    varchar (40);
      d_lettura            number (1);
      d_modifica           number (1);
      d_cancellazione      number (1);
      d_riservato          number (1);
   begin
      begin
         select ente, decode (d.riservato, 'Y', 1, 0)
           into p_ente, d_riservato
           from gdo_documenti d
          where d.id_documento = p_id_documento and valido = 'Y';
      exception
         when no_data_found then
            return 0;
      end;

      d_ruolo_access_app   := impostazioni_pkg.get_impostazione ('RUOLO_ACCESSO_APPLICATIVO', p_ente);
      d_ottica             := impostazioni_pkg.get_impostazione ('OTTICA_SO4', p_ente);
      d_ruolo_riservato    := impostazioni_pkg.get_impostazione ('RUOLO_SO4_RISERVATO_DETE', p_ente);

      -- devo controllare la riservatezza solo se il documento è riservato e il parametro in input richiede
      -- di controllare la riservatezza oppure se è richiesto di forzare il controllo.
      if ( (d_riservato = 1 or p_forza_riservato = 1) and p_controlla_riservatezza = 1) then
         d_riservato   := 1;
      else
         d_riservato   := 0;
      end if;

      begin
           select decode (max (dc.lettura), 'Y', 1, 0), decode (max (dc.modifica), 'Y', 1, 0), decode (max (dc.cancellazione), 'Y', 1, 0)
             into d_lettura, d_modifica, d_cancellazione
             from so4_v_utenti_ruoli_sogg_uo uo_r, gdo_documenti_competenze dc
            where uo_r.ottica = d_ottica
              and uo_r.utente = p_utente
              and (dc.utente = p_utente
                or (dc.unita_progr is null
                and dc.ruolo is not null
                and dc.ruolo = uo_r.ruolo
                and (d_riservato = 0
                  or not (dc.ruolo = d_ruolo_access_app)
                 and exists
                        (select 1
                           from so4_v_utenti_ruoli_sogg_uo r
                          where r.ruolo = d_ruolo_riservato and r.utente = p_utente)))
                or (dc.unita_progr is not null
                and dc.unita_progr = uo_r.uo_progr
                and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)
                and (d_riservato = 0
                  or exists
                        (select 1
                           from so4_v_utenti_ruoli_sogg_uo r
                          where r.ruolo = d_ruolo_riservato and r.utente = p_utente and r.uo_progr = dc.unita_progr and r.uo_dal = dc.unita_dal and r.ottica = dc.unita_ottica))))
              and dc.id_documento = p_id_documento
         group by dc.id_documento;

         if (p_cancellazione = d_cancellazione) then
            return 1;
         end if;

         if (p_modifica = d_modifica) then
            return 1;
         end if;

         if (p_lettura = d_lettura) then
            return 1;
         end if;

         return 0;
      exception
         when no_data_found then
            return 0;
      end;
   end;
end competenze_pkg;
/
