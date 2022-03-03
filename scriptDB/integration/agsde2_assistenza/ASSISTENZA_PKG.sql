--liquibase formatted sql
--changeset rdestasio:install_20200221_assistenza_14 runOnChange:true

create or replace package assistenza_pkg
as
   /******************************************************************************
      NAME:       impostazioni_pkg
      PURPOSE:

      REVISIONS:
      Ver        Date            Author           Description
      ---------  -----------  ---------------  ------------------------------------
      1.0        01/08/2014     mfrancesconi       1. Created this package.
   ******************************************************************************/

   procedure annulla_determina (p_anno_proposta number, p_numero_proposta number, p_ente varchar2);

   procedure annulla_proposta_delibera (p_anno_proposta number, p_numero_proposta number, p_ente varchar2);

   procedure elimina_dati;

   procedure cambia_ente_ottica (p_user_db             varchar2
                               , p_nuovo_ente       in varchar2
                               , p_nuova_ottica     in varchar2
                               , p_vecchio_ente     in varchar2 default null
                               , p_vecchia_ottica   in varchar2 default null);

   procedure cambia_unita (p_unita_ottica_old    varchar2
                         , p_unita_progr_old     number
                         , p_unita_dal_old       date
                         , p_unita_ottica_new    varchar2
                         , p_unita_progr_new     number
                         , p_unita_dal_new       date);

   procedure cambia_valore_colonna (p_user_db in varchar2, p_nome_colonna in varchar2, p_valore in varchar2);

   procedure cambia_vecchio_valore_colonna (p_user_db          in varchar2
                                          , p_nome_colonna     in varchar2
                                          , p_valore           in varchar2
                                          , p_vecchio_valore   in varchar2);

   procedure ripristina_vecchio_iter (p_tipo_oggetto varchar2, p_id_documento number);
end assistenza_pkg;
/
create or replace package body assistenza_pkg
is
   procedure annulla_determina (p_anno_proposta number, p_numero_proposta number, p_ente varchar2)
   is
      d_ente             varchar2 (100);
      d_id_determina     number (19);
      d_id_engine_iter   number (19);
   begin
      if p_ente is null then
         d_ente := impostazioni_pkg.get_impostazione ('ENTI_SO4', '*');
      else
         d_ente := p_ente;
      end if;

      select id_determina, id_engine_iter
        into d_id_determina, d_id_engine_iter
        from determine
       where numero_proposta = p_numero_proposta
         and anno_proposta = p_anno_proposta
         and registro_proposta = impostazioni_pkg.get_impostazione ('REGISTRO_PROPOSTE', d_ente)
         and ente = d_ente
         and valido = 'Y';

      update determine
         set stato = 'ANNULLATO'
       where id_determina = d_id_determina;

      update determine_competenze
         set modifica = 'N'
       where id_determina = d_id_determina and modifica = 'Y';

      update wkf_engine_iter
         set data_fine = sysdate
       where id_engine_iter = d_id_engine_iter;

      update wkf_engine_step
         set data_fine = sysdate
       where id_engine_iter = d_id_engine_iter and data_fine is null;


      -- chiudo gli iter di tutti i visti ancora attivi
      for c
         in (select vp.id_visto_parere, vp.id_engine_iter
               from visti_pareri vp, wkf_engine_iter wkf
              where vp.id_proposta_delibera = d_id_determina
                and vp.id_engine_iter = wkf.id_engine_iter
                and wkf.data_fine is null
                and vp.valido = 'Y')
      loop
         update wkf_engine_iter
            set data_fine = sysdate
          where id_engine_iter = c.id_engine_iter;

         update wkf_engine_step
            set data_fine = sysdate
          where id_engine_iter = c.id_engine_iter and data_fine is null;

         update visti_pareri_competenze
            set modifica = 'N'
          where id_visto_parere = c.id_visto_parere and modifica = 'Y';
      end loop;
   end annulla_determina;

   procedure annulla_proposta_delibera (p_anno_proposta number, p_numero_proposta number, p_ente varchar2)
   is
      d_ente                   varchar2 (100);
      d_id_proposta_delibera   number (19);
      d_id_engine_iter         number (19);
   begin
      if p_ente is null then
         d_ente := impostazioni_pkg.get_impostazione ('ENTI_SO4', '*');
      else
         d_ente := p_ente;
      end if;

      select id_proposta_delibera, id_engine_iter
        into d_id_proposta_delibera, d_id_engine_iter
        from proposte_delibera
       where numero_proposta = p_numero_proposta
         and anno_proposta = p_anno_proposta
         and registro_proposta = impostazioni_pkg.get_impostazione ('REGISTRO_PROPOSTE', d_ente)
         and ente = d_ente
         and valido = 'Y';

      update proposte_delibera
         set stato = 'ANNULLATO'
       where id_proposta_delibera = d_id_proposta_delibera;

      update proposte_delibera_competenze
         set modifica = 'N'
       where id_proposta_delibera = d_id_proposta_delibera and modifica = 'Y';

      update wkf_engine_iter
         set data_fine = sysdate
       where id_engine_iter = d_id_engine_iter;

      update wkf_engine_step
         set data_fine = sysdate
       where id_engine_iter = d_id_engine_iter and data_fine is null;

      -- chiudo gli iter di tutti i pareri ancora attivi
      for c
         in (select vp.id_visto_parere, vp.id_engine_iter
               from visti_pareri vp, wkf_engine_iter wkf
              where vp.id_proposta_delibera = d_id_proposta_delibera
                and vp.id_engine_iter = wkf.id_engine_iter
                and wkf.data_fine is null
                and vp.valido = 'Y')
      loop
         update wkf_engine_iter
            set data_fine = sysdate
          where id_engine_iter = c.id_engine_iter;

         update wkf_engine_step
            set data_fine = sysdate
          where id_engine_iter = c.id_engine_iter and data_fine is null;

         update visti_pareri_competenze
            set modifica = 'N'
          where id_visto_parere = c.id_visto_parere and modifica = 'Y';
      end loop;
   end annulla_proposta_delibera;

   procedure elimina_dati
   as
      retval   number;
   begin
      delete from documenti_collegati;

      delete from allegati_competenze;

      delete from allegati_file;

      delete from delibere_storico;

      delete from determine_storico;

      delete from proposte_delibera_storico;

      delete from visti_pareri_storico;

      delete from file_allegati_storico;

      delete from firmatari;

      delete from gte_dettagli_lock;

      delete from firma_digitale_file;

      delete from destinatari_notifiche_attivita;

      delete from firma_digitale_transazione;

      delete from destinatari_notifiche;

      delete from certificati_competenze;

      delete from delibere_competenze;

      delete from determine_competenze;

      delete from proposte_delibera_competenze;

      delete from visti_pareri_competenze;

      delete from delibere_soggetti;

      delete from determine_soggetti;

      delete from proposte_delibera_soggetti;

      delete from allegati;

      delete from certificati;

      delete from visti_pareri;

      update delibere
         set id_oggetto_seduta = null;

      update proposte_delibera
         set id_oggetto_seduta = null;

      update odg_oggetti_seduta
         set id_proposta_delibera = null;

      delete from odg_oggetti_partecipanti;

      delete from odg_sedute_partecipanti;

      delete from odg_sedute_notifiche;

      delete from odg_oggetti_seduta;

      delete from odg_sedute;

      delete from organi_controllo_notifiche_doc;

      delete from organi_controllo_notifiche;

      delete from dati_aggiuntivi;

      delete from delibere;

      delete from proposte_delibera;

      delete from determine;

      delete from file_allegati;

      delete from gte_lock;

      --delete from notifiche_email;   NOOOOOOOOOOOOOOO FA DEI DANNI PERCHE' NON VANNO PIU' LE NOTIFICHE
      --delete from email;

      update wkf_engine_iter
         set id_step_corrente = null;

      delete from wkf_engine_step_attori;

      delete from wkf_engine_step;

      delete from wkf_engine_iter;

      delete from file_allegati_storico;


      begin
         execute immediate 'begin :this := GDM.F_ELIMINA_DOCUMENTI ( ''SEGRETERIA.ATTI.2_0'', ''DETERMINA'', ''N''); end;'
            using in out retval;

         execute immediate 'begin :this := GDM.F_ELIMINA_DOCUMENTI ( ''SEGRETERIA.ATTI.2_0'', ''DELIBERA'', ''N''); end;'
            using in out retval;

         execute immediate
            'begin :this := GDM.F_ELIMINA_DOCUMENTI ( ''SEGRETERIA.ATTI.2_0'', ''PROPOSTA_DELIBERA'', ''N''); end;'
            using in out retval;

         execute immediate 'begin :this := GDM.F_ELIMINA_DOCUMENTI ( ''SEGRETERIA.ATTI.2_0'', ''VISTO'', ''N''); end;'
            using in out retval;

         execute immediate 'begin :this := GDM.F_ELIMINA_DOCUMENTI ( ''SEGRETERIA.ATTI.2_0'', ''CERTIFICATO'', ''N''); end;'
            using in out retval;

         execute immediate 'begin :this := GDM.F_ELIMINA_DOCUMENTI ( ''SEGRETERIA.ATTI.2_0'', ''ALLEGATO'', ''N''); end;'
            using in out retval;
      exception
         when others then
            null;
      end;
   end elimina_dati;

   procedure cambia_ente_ottica (p_user_db             varchar2
                               , p_nuovo_ente       in varchar2
                               , p_nuova_ottica     in varchar2
                               , p_vecchio_ente     in varchar2 default null
                               , p_vecchia_ottica   in varchar2 default null)
   as
   begin
      -- cambio l'ente dell'applicativo:
      if (p_vecchio_ente is null) then
         cambia_valore_colonna (p_user_db, 'ENTE', p_nuovo_ente);
      else
         cambia_vecchio_valore_colonna (p_user_db
                                      , 'ENTE'
                                      , p_nuovo_ente
                                      , p_vecchio_ente);
      end if;

      -- cambio l'ottica dell'applicativo:
      if (p_vecchia_ottica is null) then
         cambia_valore_colonna (p_user_db, 'UNITA_OTTICA', p_nuova_ottica);
      else
         cambia_vecchio_valore_colonna (p_user_db
                                      , 'UNITA_OTTICA'
                                      , p_nuova_ottica
                                      , p_vecchia_ottica);
      end if;

      -- cambio l'ottica dell'applicativo
      impostazioni_pkg.set_impostazione ('ENTI_SO4', '*', p_nuovo_ente);

      -- cambio l'ottica dell'applicativo
      impostazioni_pkg.set_impostazione ('OTTICA_SO4', p_nuovo_ente, p_nuova_ottica);
   end;


   procedure cambia_unita (p_unita_ottica_old    varchar2
                         , p_unita_progr_old     number
                         , p_unita_dal_old       date
                         , p_unita_ottica_new    varchar2
                         , p_unita_progr_new     number
                         , p_unita_dal_new       date)
   as
   begin
      update allegati_competenze
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update certificati_competenze
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update delibere_competenze
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update determine_competenze
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update proposte_delibera_competenze
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update tipi_delibera_competenze
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update tipi_determina_competenze
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update visti_pareri_competenze
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update delibere_soggetti
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update destinatari_notifiche
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update destinatari_notifiche_attivita
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update determine_soggetti
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update gte_modelli_competenza
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update notifiche_email
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update proposte_delibera_soggetti
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update tipi_registro_unita
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update visti_pareri
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update visti_pareri_storico
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update wkf_diz_attori
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      update wkf_engine_step_attori
         set unita_dal = p_unita_dal_new, unita_ottica = p_unita_ottica_new, unita_progr = p_unita_progr_new
       where unita_dal = p_unita_dal_old and unita_ottica = p_unita_ottica_old and unita_progr = p_unita_progr_old;

      --questa update va bene solo se c'è un'unica unita per visto / parere
      update tipi_visto_parere
         set unita_destinatarie = p_unita_progr_new
       where unita_destinatarie = p_unita_progr_old;
   end;

   procedure cambia_valore_colonna (p_user_db in varchar2, p_nome_colonna in varchar2, p_valore in varchar2)
   as
   begin
      for c
         in (select c.table_name, c.column_name
               from all_tab_cols c, all_objects o
              where c.column_name = p_nome_colonna
                and c.owner = p_user_db
                and o.object_type = 'TABLE'
                and o.object_name = c.table_name
                and o.owner = p_user_db)
      loop
         execute immediate
            'update ' ||
            c.table_name ||
            ' set ' ||
            c.column_name ||
            ' = ''' ||
            p_valore ||
            ''' where ' ||
            c.column_name ||
            ' is not null';
      end loop;
   end;

   procedure cambia_vecchio_valore_colonna (p_user_db          in varchar2
                                          , p_nome_colonna     in varchar2
                                          , p_valore           in varchar2
                                          , p_vecchio_valore   in varchar2)
   as
   begin
      for c
         in (select c.table_name
                  , c.column_name
                  , decode (p_vecchio_valore, null, 'is null', ' = ''' || p_vecchio_valore || '''') vecchio_valore
               from all_tab_cols c, all_objects o
              where c.column_name = p_nome_colonna
                and c.owner = p_user_db
                and o.object_type = 'TABLE'
                and o.object_name = c.table_name
                and o.owner = p_user_db)
      loop
         execute immediate
            'update ' ||
            c.table_name ||
            ' set ' ||
            c.column_name ||
            ' = ''' ||
            p_valore ||
            ''' ' ||
            ' where ' ||
            c.column_name ||
            ' ' ||
            c.vecchio_valore ||
            ' ';
      end loop;
   end;

   procedure ripristina_vecchio_iter (p_tipo_oggetto varchar2, p_id_documento number)
   is
      d_id_engine_iter   number (19);
   begin
      if (p_tipo_oggetto = 'DETERMINA') then
         --leggo l'ultimo id_engine_iter usato dalla determina
         select id_engine_iter
           into d_id_engine_iter
           from (  select id_engine_iter
                     from determine_storico
                    where id_determina = p_id_documento
                 order by id_determina_storico desc)
          where rownum = 1;

         update determine
            set id_engine_iter = d_id_engine_iter
          where id_determina = p_id_documento;

         update wkf_engine_iter
            set data_fine = null
          where id_engine_iter = d_id_engine_iter;
      end if;
   end;
end assistenza_pkg;
/