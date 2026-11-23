--liquibase formatted sql
--changeset mfrancesconi:2.5_20250422_05.integrazione_stats_pkg runOnChange:true

create or replace package integrazione_stats_pkg
as

   function get_data_ingresso_step (p_id_engine_iter number, p_id_engine_step number, p_titolo_step varchar2 )
      return varchar2;

   function get_data_uscita_step (p_id_engine_iter number, p_id_engine_step number, p_titolo_step varchar2 )
      return varchar2;

   function get_utente_ingresso_step (p_id_engine_iter number, p_id_engine_step number, p_titolo_step varchar2 )
      return varchar2;

   function get_utente_uscita_step (p_id_engine_iter number, p_id_engine_step number, p_titolo_step varchar2 )
      return varchar2;
end integrazione_stats_pkg;
/

create or replace package body integrazione_stats_pkg
as
    function get_data_ingresso_step (p_id_engine_iter   number,
                                     p_id_engine_step   number,
                                     p_titolo_step      varchar2)
        return varchar2
    /******************************************************************************
     NOME:        get_data_ingresso_step
     DESCRIZIONE: restituisce la data di ingresso in uno step (utilizzata nella vista STAT_C_G337_DETE_TEMPI_CONT) successivo ad un altro step
     PARAMETRI:   p_id_engine_iter - iter dello step
                  p_id_engine_step - step (lo step calcolato sarà successivo a questo)
                  p_titolo_step - titolo dello step
     RITORNA:     ritorna la DATA_INIZIO dello step (come stringa)
    ******************************************************************************/
    is
        d_data_ingresso  varchar2 (16);
    begin
        begin
            select data_inizio
            into d_data_ingresso
              from (  select wes_visto.id_engine_step,
                             to_char (wes_visto.data_inizio,'dd/mm/yyyy hh24:mi') data_inizio
                        from wkf_engine_step wes_visto, wkf_cfg_step wcs_visto
                       where wes_visto.id_engine_iter = p_id_engine_iter
                         and wes_visto.id_cfg_step = wcs_visto.id_cfg_step
                         and wcs_visto.titolo = p_titolo_step
                         and (wes_visto.id_engine_step >= p_id_engine_step)
                    order by wes_visto.id_engine_step)
             where rownum = 1;
        exception when others then
                d_data_ingresso := null;
        end;

        return d_data_ingresso;
    end get_data_ingresso_step;

    function get_data_uscita_step (p_id_engine_iter   number,
                                   p_id_engine_step   number,
                                   p_titolo_step      varchar2)
        return varchar2
    /******************************************************************************
     NOME:        get_data_uscita_step
     DESCRIZIONE: restituisce la data di uscita da uno step (utilizzata nella vista STAT_C_G337_DELI_TEMPI_CONT) successivo ad un altro step
     PARAMETRI:   p_id_engine_iter - iter dello step
                  p_id_engine_step - step (lo step calcolato sarà successivo a questo)
                  p_titolo_step - titolo dello step
     RITORNA:     ritorna la DATA_FINE dello step (come stringa)
    ******************************************************************************/
    is
        d_data_fine   varchar2 (16);
    begin
        begin
            select data_fine
              into d_data_fine
              from (  select wes_visto.id_engine_step,
                             to_char (wes_visto.data_fine, 'dd/mm/yyyy hh24:mi') data_fine
                        from wkf_engine_step wes_visto, wkf_cfg_step wcs_visto
                       where wes_visto.id_engine_iter = p_id_engine_iter
                         and wes_visto.id_cfg_step = wcs_visto.id_cfg_step
                         and wcs_visto.titolo = p_titolo_step
                         and (wes_visto.id_engine_step >= p_id_engine_step)
                    order by wes_visto.id_engine_step)
             where rownum = 1;
        exception when others then
                d_data_fine := null;
        end;

        return d_data_fine;
    end get_data_uscita_step;

    function get_utente_ingresso_step (p_id_engine_iter   number,
                                       p_id_engine_step   number,
                                       p_titolo_step      varchar2)
        return varchar2
    /******************************************************************************
     NOME:        get_utente_ingresso_step
     DESCRIZIONE: restituisce il nominativo di ingresso in uno step (utilizzata nella vista STAT_C_G337_DETE_TEMPI_CONT) successivo ad un altro step
     PARAMETRI:   p_id_engine_iter - iter dello step
                  p_id_engine_step - step (lo step calcolato sarà successivo a questo)
                  p_titolo_step - titolo dello step
     RITORNA:     ritorna il nominativo dell'utente di ingresso nello step
    ******************************************************************************/
    is
        d_nominativo   varchar2 (255);
    begin
        begin
            select nominativo
            into d_nominativo
              from (  select wes_visto.id_engine_step,
                             utility_pkg.get_cognome_nome ( utility_pkg.get_ni_soggetto (wes_visto.utente_ins)) nominativo
                        from wkf_engine_step wes_visto, wkf_cfg_step wcs_visto
                       where wes_visto.id_engine_iter = p_id_engine_iter
                         and wes_visto.id_cfg_step = wcs_visto.id_cfg_step
                         and wcs_visto.titolo = p_titolo_step
                         and (wes_visto.id_engine_step >= p_id_engine_step)
                    order by wes_visto.id_engine_step)
             where rownum = 1;
        exception when others then
                d_nominativo := null;
        end;

        return d_nominativo;
    end get_utente_ingresso_step;

    function get_utente_uscita_step (p_id_engine_iter   number,
                                     p_id_engine_step   number,
                                     p_titolo_step      varchar2)
        return varchar2
    /******************************************************************************
     NOME:        get_utente_uscita_step
     DESCRIZIONE: restituisce il nominativo dell'utente che preme il pulsante per uscire da uno step (utilizzata nella vista STAT_C_G337_DELI_TEMPI_CONT) successivo ad un altro step
     PARAMETRI:   p_id_engine_iter - iter dello step
                  p_id_engine_step - step (lo step calcolato sarà successivo a questo)
                  p_titolo_step - titolo dello step
     RITORNA:     ritorna la DATA_FINE dello step
    ******************************************************************************/
    is
        d_nominativo   varchar2 (255);
    begin
        begin
            select nominativo
              into d_nominativo
              from (  select wes_visto.id_engine_step,
                             utility_pkg.get_cognome_nome(utility_pkg.get_ni_soggetto(wes_visto.utente_upd)) nominativo
                        from wkf_engine_step wes_visto, wkf_cfg_step wcs_visto
                       where wes_visto.id_engine_iter = p_id_engine_iter
                         and wes_visto.id_cfg_step = wcs_visto.id_cfg_step
                         and wcs_visto.titolo = p_titolo_step
                         and (wes_visto.id_engine_step >= p_id_engine_step)
                    order by wes_visto.id_engine_step)
             where rownum = 1;
        exception when others then
                d_nominativo := '';
        end;

        return d_nominativo;
    end get_utente_uscita_step;
end integrazione_stats_pkg;
/