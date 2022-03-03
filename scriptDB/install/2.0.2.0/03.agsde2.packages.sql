--liquibase formatted sql
--changeset rdestasio:2.0.2.0_20200221_03

CREATE OR REPLACE PACKAGE        agsde_competenze
AS

    procedure aggiorna_class_fasc (p_id_documento_esterno  number
                                 , p_class_cod      varchar2
                                 , p_class_dal      date
                                 , p_class_descr    varchar2
                                 , p_fasc_anno      number
                                 , p_fasc_numero    varchar2
                                 , p_fasc_oggetto   varchar2);
    
    
    function controlla_competenze_determina (p_utente       varchar2
                                         , p_oggetto        varchar2
                                         , p_lettura        number
                                         , p_modifica       number
                                         , p_cancellazione  number
                                         , p_file           number) RETURN NUMBER;
                                         
    function controlla_competenze_visto_par (p_utente       varchar2
                                         , p_oggetto        varchar2
                                         , p_lettura        number
                                         , p_modifica       number
                                         , p_cancellazione  number
                                         , p_file           number) RETURN NUMBER;
    
    function controlla_competenze_cert (p_utente            varchar2
                                         , p_oggetto        varchar2
                                         , p_lettura        number
                                         , p_modifica       number
                                         , p_cancellazione  number
                                         , p_file           number) RETURN NUMBER;
    
    function controlla_competenze_delibera (p_utente        varchar2
                                         , p_oggetto        varchar2
                                         , p_lettura        number
                                         , p_modifica       number
                                         , p_cancellazione  number
                                         , p_file           number) RETURN NUMBER;
                                         
    function controlla_competenze_prop_del (p_utente        varchar2
                                         , p_oggetto        varchar2
                                         , p_lettura        number
                                         , p_modifica       number
                                         , p_cancellazione  number
                                         , p_file           number) RETURN NUMBER;
                                         
    function controlla_competenze_allegato (p_utente        varchar2
                                         , p_oggetto        varchar2
                                         , p_lettura        number
                                         , p_modifica       number
                                         , p_cancellazione  number
                                         , p_file           number) RETURN NUMBER;
                                                                                  
END agsde_competenze;

/

CREATE OR REPLACE package          assistenza_pkg
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
   
   procedure cambia_ente_ottica (p_user_db varchar2, p_nuovo_ente in varchar2, p_nuova_ottica in varchar2, p_vecchio_ente in varchar2 default null, p_vecchia_ottica in varchar2 default null);
   
   procedure cambia_valore_colonna (p_user_db in varchar2, p_nome_colonna in varchar2, p_valore in varchar2);
   
   procedure cambia_vecchio_valore_colonna (p_user_db in varchar2, p_nome_colonna in varchar2, p_valore in varchar2, p_vecchio_valore in varchar2);
   
end assistenza_pkg;

/

CREATE OR REPLACE PACKAGE        impostazioni_pkg AS
/******************************************************************************
   NAME:       impostazioni_pkg
   PURPOSE:

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        18/02/2014      esasdelli       1. Created this package.
******************************************************************************/

  procedure add_impostazione (p_codice IN varchar2, p_descrizione in varchar2, p_etichetta in varchar2, p_predefinito in varchar2, p_modificabile in varchar2, p_caratteristiche in varchar2);

  function get_impostazione  (p_codice IN varchar2, p_ente in varchar2) RETURN varchar2;
  
  procedure set_impostazione (p_codice IN varchar2, p_ente in varchar2, p_valore in varchar2);

END impostazioni_pkg;

/

CREATE OR REPLACE PACKAGE          jwf_utility_pkg AS
/******************************************************************************
   NAME:       jwf_utility_pkg
   PURPOSE:

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        29/09/2014      esasdelli       1. Created this package.
******************************************************************************/

  function is_in_step (p_id_riferimento in varchar2, p_nome_step in varchar2) RETURN number;
END jwf_utility_pkg;

/

CREATE OR REPLACE package        reporter_pkg
is
   /******************************************************************************
      NOME:    REPORTER_PKG.
    DESCRIZIONE: PACKAGE PER IL CALCOLO DELLE VARIABILI UTILIZZATE NEI MODELLI DEI
                 TESTI
    ANNOTAZIONI: VERSIONE 1.0
    REV. DATA        AUTORE           DESCRIZIONE
    ---- ----------  -----------      ------------------------------------------------------
    0   20/01/2013   FRANCESCONI      CREAZIONE.
   ******************************************************************************/
   function versione
      return varchar2;

   function get_date_gettoni_presenza (p_chiave_calcolo varchar2, p_utente in varchar2)
      return varchar2;

   function get_presenti_oggetto_seduta (p_id_oggetto_seduta number, p_presente varchar2, p_ruolo varchar2)
      return varchar2;

   function get_presenti_seduta (p_id_seduta number, p_presente varchar2, p_ruolo varchar2)
      return varchar2;

   function get_suddivisione_descrizione (p_unita_progr     number
                                        ,  p_unita_dal       date
                                        ,  p_suddivisione    varchar2
                                        ,  p_ente            varchar2)
      return varchar2;

   function get_testo_proposta_delibera (p_id_delibera number)
      return blob;

   function get_votanti_oggetto_seduta (p_id_oggetto_seduta number, p_voto varchar2)
      return varchar2;

   function numero_lettere (a_numero in number)
      return varchar2;

   function mese_lettere (a_numero in number)
      return varchar2;

   function giorno_lettere (p_data in date)
      return varchar2;

   function get_desc_ascendenti_unita (p_codice_uo     varchar2
                                     ,  p_data          date default null
                                     ,  p_ottica        varchar2
                                     ,  p_separatore    varchar2 default null)
      return varchar2;
      
   function get_cognome_nome (p_ni number) return varchar2;
   function get_nome (p_ni number) return varchar2;
   function get_cognome (p_ni number) return varchar2;
end reporter_pkg;
/

CREATE OR REPLACE package        utility_pkg
is
   function get_data_firma_delibera (p_id_delibera number, p_tipo_soggetto varchar2)
      return date;
      
   function get_data_firma_par_contabile (p_id_delibera number)
      return date;
      
   function get_data_firma_visto_contabile (p_id_determina number)
      return date;

   function get_firmatario_delibera (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2;
      
   function get_firmatario_par_contabile (p_id_delibera number)
      return varchar2;
      
   function get_firmatario_visto_contabile (p_id_determina number)
      return varchar2;

   function get_unita_parere_contabile (p_id_delibera number)
      return varchar2;

   function get_unita_visto_contabile (p_id_determina number)
      return varchar2;

   function get_prima_data_firma_certif (p_id_certificato number)
      return date;
      
   function get_prima_data_firma_delibera (p_id_delibera number)
      return date;
           
   function get_prima_data_firma_determina (p_id_determina number)
      return date;

   function get_prima_data_firma_prop_deli (p_id_proposta_delibera number)
      return date;

   function get_primo_firmatario_determina (p_id_determina number)
      return varchar2;

   function get_sogg_notifica_delibera (p_id_delibera number)
      return varchar2;

   function get_sogg_notifica_determina (p_id_determina number)
      return varchar2;

   function get_suddivisione_descrizione (p_unita_progr     number
                                        ,  p_unita_dal       date
                                        ,  p_suddivisione    varchar2
                                        ,  p_ente            varchar2)
      return varchar2;

   function get_uo_descrizione (p_unita_progr number, p_unita_dal date)
      return varchar2;

   function get_uo_padre_descrizione (p_unita_progr number, p_unita_dal date, p_ottica varchar2)
      return varchar2;           
end utility_pkg;

/

CREATE OR REPLACE PACKAGE BODY        agsde_competenze 
AS

    procedure aggiorna_class_fasc (p_id_documento_esterno  number
                                 , p_class_cod      varchar2
                                 , p_class_dal      date
                                 , p_class_descr    varchar2
                                 , p_fasc_anno      number
                                 , p_fasc_numero    varchar2
                                 , p_fasc_oggetto   varchar2) is
    begin
        update determine d 
           set d.classifica_codice      = p_class_cod
             , d.classifica_dal         = p_class_dal
             , d.classifica_descrizione = p_class_descr
             , d.fascicolo_anno    = p_fasc_anno
             , d.fascicolo_numero  = p_fasc_numero
             , d.fascicolo_oggetto = p_fasc_oggetto
         where d.id_documento_esterno = p_id_documento_esterno;
         
         update proposte_delibera d 
           set d.classifica_codice      = p_class_cod
             , d.classifica_dal         = p_class_dal
             , d.classifica_descrizione = p_class_descr
             , d.fascicolo_anno    = p_fasc_anno
             , d.fascicolo_numero  = p_fasc_numero
             , d.fascicolo_oggetto = p_fasc_oggetto
         where d.id_documento_esterno = p_id_documento_esterno;
    end;
       
    function controlla_competenze_visto_par (p_utente       varchar2
                                         , p_oggetto        varchar2
                                         , p_lettura        number
                                         , p_modifica       number
                                         , p_cancellazione  number
                                         , p_file           number)
    return number is
        d_lettura           number(1);
        d_modifica          number(1);
        d_cancellazione     number(1);
        d_ottica            varchar(40);
        d_ruolo_riservato   varchar(40);
        d_riservato         varchar(1);
        d_id_documento      number(19);
        p_id_visto          varchar(40);
        p_ente              varchar(40);
        p_riservato         varchar(40);
    begin
        p_id_visto  := gdm_f_valore_campo(p_oggetto, 'ID_DOCUMENTO_GRAILS');
        p_ente      := gdm_f_valore_campo(p_oggetto, 'ENTE');
        p_riservato := gdm_f_valore_campo(p_oggetto, 'RISERVATO');
        
        if (p_file = 0) then
            p_riservato := 'N';
        end if;
        
        if (p_utente = 'GDM' or p_utente = 'AGSDE2' or p_utente = 'RPI') then
            return 1;
        end if;
    
        begin
            d_id_documento := to_number (p_id_visto);
        exception when others then
            return 1;
        end;
        
        d_ottica := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('OTTICA_SO4', p_ente);
        begin
            select decode(max(dc.lettura), 'Y', 1, 0), decode(max(dc.modifica), 'Y', 1, 0), decode(max(dc.cancellazione), 'Y', 1, 0)
            into d_lettura, d_modifica, d_cancellazione
            from so4_v_utenti_ruoli_sogg_uo uo_r
               , visti_pareri_competenze dc
           where uo_r.ottica = d_ottica
             and uo_r.utente = p_utente
             and (dc.utente  = p_utente
              or (dc.unita_progr is null     and dc.ruolo is not null and dc.ruolo = uo_r.ruolo)
              or (dc.unita_progr is not null and dc.unita_progr = uo_r.uo_progr and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)))
             and dc.id_visto_parere = d_id_documento
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
        exception when no_data_found then
            return 0;
        end;
        return 1;
    end;
    
    function controlla_competenze_cert (p_utente       varchar2
                                         , p_oggetto        varchar2
                                         , p_lettura        number
                                         , p_modifica       number
                                         , p_cancellazione  number
                                         , p_file           number)
    return number is
        d_lettura           number(1);
        d_modifica          number(1);
        d_cancellazione     number(1);
        d_ottica            varchar(40);
        d_ruolo_riservato   varchar(40);
        d_riservato         varchar(1);
        d_id_documento      number(19);
        p_id_certificato    varchar(40);
        p_ente              varchar(40);
        p_riservato         varchar(40);
    begin
        p_id_certificato  := gdm_f_valore_campo(p_oggetto, 'ID_DOCUMENTO_GRAILS');
        p_ente      := gdm_f_valore_campo(p_oggetto, 'ENTE');
        p_riservato := gdm_f_valore_campo(p_oggetto, 'RISERVATO');
        
        if (p_file = 0) then
            p_riservato := 'N';
        end if;
        
        if (p_utente = 'GDM' or p_utente = 'AGSDE2' or p_utente = 'RPI') then
            return 1;
        end if;
        
        begin
            d_id_documento := to_number (p_id_certificato);
        exception when others then
            return 1;
        end;
        
        d_ottica := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('OTTICA_SO4', p_ente);
        begin
            select decode(max(dc.lettura), 'Y', 1, 0), decode(max(dc.modifica), 'Y', 1, 0), decode(max(dc.cancellazione), 'Y', 1, 0)
            into d_lettura, d_modifica, d_cancellazione
            from so4_v_utenti_ruoli_sogg_uo uo_r
               , certificati_competenze dc
           where uo_r.ottica = d_ottica
             and uo_r.utente = p_utente
             and (dc.utente  = p_utente
              or (dc.unita_progr is null     and dc.ruolo is not null and dc.ruolo = uo_r.ruolo)
              or (dc.unita_progr is not null and dc.unita_progr = uo_r.uo_progr and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)))
             and dc.id_certificato = d_id_documento
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
        exception when no_data_found then
            return 0;
        end;
        return 1;
    end;
    
    function controlla_competenze_prop_del (p_utente       varchar2
                                         , p_oggetto        varchar2
                                         , p_lettura        number
                                         , p_modifica       number
                                         , p_cancellazione  number
                                         , p_file           number)
    return number is
        d_lettura           number(1);
        d_modifica          number(1);
        d_cancellazione     number(1);
        d_ottica            varchar(40);
        d_ruolo_riservato   varchar(40);
        d_riservato         varchar(1);
        d_id_documento      number(19);
        p_id_prop_deli      varchar(40);
        p_ente              varchar(40);
        p_riservato         varchar(40);
        d_ruolo_access_app  varchar(40);
    begin
    
        p_id_prop_deli  := gdm_f_valore_campo(p_oggetto, 'ID_DOCUMENTO_GRAILS');
        p_ente      := gdm_f_valore_campo(p_oggetto, 'ENTE');
        p_riservato := gdm_f_valore_campo(p_oggetto, 'RISERVATO');
        
        if (p_file = 0) then
            p_riservato := 'N';
        end if;
        
        if (p_utente = 'GDM' or p_utente = 'AGSDE2' or p_utente = 'RPI') then
            return 1;
        end if;    
    
        begin
            d_id_documento := to_number (p_id_prop_deli);
        exception when others then
            return 1;
        end;

        if (p_riservato <> 'Y') then
            d_riservato := 'N';
        else
            d_riservato := 'Y';
        end if;
        
        d_ruolo_access_app  := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_ACCESSO_APPLICATIVO', p_ente);
        d_ruolo_riservato   := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_SO4_RISERVATO_DELI',  p_ente);
        d_ottica            := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('OTTICA_SO4',                p_ente);
        
        begin
            select decode(max(dc.lettura), 'Y', 1, 0), decode(max(dc.modifica), 'Y', 1, 0), decode(max(dc.cancellazione), 'Y', 1, 0)
            into d_lettura, d_modifica, d_cancellazione
            from so4_v_utenti_ruoli_sogg_uo uo_r
               , proposte_delibera_competenze dc
           where uo_r.ottica = d_ottica
             and uo_r.utente = p_utente
             and (dc.utente  = p_utente
              or (dc.unita_progr is null 
                  and dc.ruolo is not null 
                  and dc.ruolo = uo_r.ruolo
                  and (d_riservato = 'N' or not(dc.ruolo = 'AGD') and exists(select 1 from so4_v_utenti_ruoli_sogg_uo r where r.ruolo = d_ruolo_riservato and r.utente = p_utente)))
              or (dc.unita_progr is not null 
                  and dc.unita_progr = uo_r.uo_progr 
                  and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)
                  and (d_riservato = 'N' or exists(select 1 from so4_v_utenti_ruoli_sogg_uo r where r.ruolo = d_ruolo_riservato and r.utente = p_utente 
                                    and r.uo_progr = dc.unita_progr and r.uo_dal = dc.unita_dal and r.ottica = dc.unita_ottica))
              ))
             and dc.id_proposta_delibera = d_id_documento
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
           
           return 0;
        exception when no_data_found then
            return 0;
        end;
        return 1;
    end;
    
    function controlla_competenze_delibera (p_utente       varchar2
                                         , p_oggetto        varchar2
                                         , p_lettura        number
                                         , p_modifica       number
                                         , p_cancellazione  number
                                         , p_file           number)
    return number is
        d_lettura           number(1);
        d_modifica          number(1);
        d_cancellazione     number(1);
        d_ottica            varchar(40);
        d_ruolo_riservato   varchar(40);
        d_riservato         varchar(1);
        d_id_documento      number(19);
        p_id_delibera       varchar(40);
        p_ente              varchar(40);
        p_riservato         varchar(40);
        d_ruolo_access_app  varchar(40);
    begin
    
        p_id_delibera  := gdm_f_valore_campo(p_oggetto, 'ID_DOCUMENTO_GRAILS');
        p_ente      := gdm_f_valore_campo(p_oggetto, 'ENTE');
        p_riservato := gdm_f_valore_campo(p_oggetto, 'RISERVATO');
        
        if (p_file = 0) then
            p_riservato := 'N';
        end if;
        
        if (p_utente = 'GDM' or p_utente = 'AGSDE2' or p_utente = 'RPI') then
            return 1;
        end if;
    
        begin
            d_id_documento := to_number (p_id_delibera);
        exception when others then
            return 1;
        end;
        
        if (p_riservato <> 'Y') then
            d_riservato := 'N';
        else
            d_riservato := 'Y';
        end if;
        
        d_ruolo_access_app  := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_ACCESSO_APPLICATIVO', p_ente);
        d_ruolo_riservato   := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_SO4_RISERVATO_DELI',  p_ente);
        d_ottica            := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('OTTICA_SO4',                p_ente);
        
        begin
            select decode(max(dc.lettura), 'Y', 1, 0), decode(max(dc.modifica), 'Y', 1, 0), decode(max(dc.cancellazione), 'Y', 1, 0)
            into d_lettura, d_modifica, d_cancellazione
            from so4_v_utenti_ruoli_sogg_uo uo_r
               , delibere_competenze dc
           where uo_r.ottica = d_ottica
             and uo_r.utente = p_utente
             and (dc.utente  = p_utente
              or (dc.unita_progr is null 
                  and dc.ruolo is not null 
                  and dc.ruolo = uo_r.ruolo
                  and (d_riservato = 'N' or not(dc.ruolo = 'AGD') and exists(select 1 from so4_v_utenti_ruoli_sogg_uo r where r.ruolo = d_ruolo_riservato and r.utente = p_utente)))
              or (dc.unita_progr is not null 
                  and dc.unita_progr = uo_r.uo_progr 
                  and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)
                  and (d_riservato = 'N' or exists(select 1 from so4_v_utenti_ruoli_sogg_uo r where r.ruolo = d_ruolo_riservato and r.utente = p_utente 
                                    and r.uo_progr = dc.unita_progr and r.uo_dal = dc.unita_dal and r.ottica = dc.unita_ottica))
              ))
             and dc.id_delibera = d_id_documento
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
        exception when no_data_found then
            return 0;
        end;
        
        return 1;
    end;
    
    function controlla_competenze_determina (p_utente       varchar2
                                         , p_oggetto        varchar2
                                         , p_lettura        number
                                         , p_modifica       number
                                         , p_cancellazione  number
                                         , p_file           number)
    return number is
        d_lettura           number(1);
        d_modifica          number(1);
        d_cancellazione     number(1);
        d_ottica            varchar(40);
        d_ruolo_riservato   varchar(40);
        d_riservato         varchar(1);
        d_id_documento      number(19);
        p_id_determina      varchar(40);
        p_ente              varchar(40);
        p_riservato         varchar(40);
        d_ruolo_access_app  varchar(40);
    begin
    
        p_id_determina  := gdm_f_valore_campo(p_oggetto, 'ID_DOCUMENTO_GRAILS');
        p_ente      := gdm_f_valore_campo(p_oggetto, 'ENTE');
        p_riservato := gdm_f_valore_campo(p_oggetto, 'RISERVATO');
        
        if (p_file = 0) then
            p_riservato := 'N';
        end if;   
    
        if (p_utente = 'GDM' or p_utente = 'AGSDE2' or p_utente = 'RPI') then
            return 1;
        end if;    
    
        begin
            d_id_documento := to_number (p_id_determina);
        exception when others then
            return 1;
        end;
        
        if (p_riservato <> 'Y') then
            d_riservato := 'N';
        else
            d_riservato := 'Y';
        end if;
        
        d_ruolo_access_app  := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_ACCESSO_APPLICATIVO', p_ente);
        d_ruolo_riservato   := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_SO4_RISERVATO_DETE',  p_ente);
        d_ottica            := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('OTTICA_SO4',                p_ente);
        
        begin
            select decode(max(dc.lettura), 'Y', 1, 0), decode(max(dc.modifica), 'Y', 1, 0), decode(max(dc.cancellazione), 'Y', 1, 0)
            into d_lettura, d_modifica, d_cancellazione
            from so4_v_utenti_ruoli_sogg_uo uo_r
               , determine_competenze dc
           where uo_r.ottica = d_ottica
             and uo_r.utente = p_utente
             and (dc.utente  = p_utente
              or (dc.unita_progr is null 
                  and dc.ruolo is not null 
                  and dc.ruolo = uo_r.ruolo
                  and (d_riservato = 'N' or not(dc.ruolo = 'AGD') and exists(select 1 from so4_v_utenti_ruoli_sogg_uo r where r.ruolo = d_ruolo_riservato and r.utente = p_utente)))
              or (dc.unita_progr is not null 
                  and dc.unita_progr = uo_r.uo_progr 
                  and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)
                  and (d_riservato = 'N' or exists(select 1 from so4_v_utenti_ruoli_sogg_uo r where r.ruolo = d_ruolo_riservato and r.utente = p_utente 
                                    and r.uo_progr = dc.unita_progr and r.uo_dal = dc.unita_dal and r.ottica = dc.unita_ottica))
              ))
             and dc.id_determina = d_id_documento
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
        exception when no_data_found then
            return 0;
        end;
        
        return 1;
    end;
    
    
    function controlla_competenze_allegato (p_utente        varchar2
                                         , p_oggetto        varchar2
                                         , p_lettura        number
                                         , p_modifica       number
                                         , p_cancellazione  number
                                         , p_file           number)
    return number is
        d_id_determina          number(19);
        d_id_delibera           number(19);
        d_id_proposta_delibera  number(19);
        d_id_documento          number(19);
        d_riservato             varchar(1);
        p_id_allegato       varchar(40);
        p_ente              varchar(40);
        p_riservato         varchar(40);
    begin
        
        p_id_allegato  := gdm_f_valore_campo(p_oggetto, 'ID_DOCUMENTO_GRAILS');
        p_ente      := gdm_f_valore_campo(p_oggetto, 'ENTE');
        p_riservato := gdm_f_valore_campo(p_oggetto, 'RISERVATO');
        
        if (p_file = 0) then
            p_riservato := 'N';
        end if;
    
        if (p_utente = 'GDM' or p_utente = 'AGSDE2' or p_utente = 'RPI') then
            return 1;
        end if;
        
        begin
            d_id_documento := to_number (p_id_allegato);
        exception when others then
            return 1;
        end;

        select id_determina,   id_delibera,   id_proposta_delibera, riservato
          into d_id_determina, d_id_delibera, d_id_proposta_delibera, d_riservato
          from allegati
         where id_allegato = d_id_documento;
         
         if (d_id_delibera is not null) then
            return controlla_competenze_delibera(p_utente
                                               , p_oggetto
                                               , p_lettura      
                                               , p_modifica     
                                               , p_cancellazione
                                               , p_file);
         end if;
         
         if (d_id_proposta_delibera is not null) then
            return controlla_competenze_prop_del(p_utente
                                               , p_oggetto
                                               , p_lettura      
                                               , p_modifica     
                                               , p_cancellazione
                                               , p_file);
         end if;
         
         if (d_id_determina is not null) then
            return controlla_competenze_determina(p_utente
                                               , p_oggetto
                                               , p_lettura      
                                               , p_modifica     
                                               , p_cancellazione
                                               , p_file);
         end if;
         return 1;
    end;
    
END agsde_competenze;

/

CREATE OR REPLACE package body        assistenza_pkg
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
       where id_determina = d_id_determina
         and modifica = 'Y';

      update wkf_engine_iter
         set data_fine = sysdate
       where id_engine_iter = d_id_engine_iter;

      update wkf_engine_step
         set data_fine = sysdate
       where id_engine_iter = d_id_engine_iter
         and data_fine is null;
         

      -- chiudo gli iter di tutti i visti ancora attivi
      for c in (select vp.id_visto_parere, vp.id_engine_iter
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
          where id_engine_iter = c.id_engine_iter
            and data_fine is null;

         update visti_pareri_competenze
            set modifica = 'N'
          where id_visto_parere = c.id_visto_parere
            and modifica = 'Y';
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
       where id_proposta_delibera = d_id_proposta_delibera
         and modifica = 'Y';

      update wkf_engine_iter
         set data_fine = sysdate
       where id_engine_iter = d_id_engine_iter;

      update wkf_engine_step
         set data_fine = sysdate
       where id_engine_iter = d_id_engine_iter
         and data_fine is null;

      -- chiudo gli iter di tutti i pareri ancora attivi
      for c in (select vp.id_visto_parere, vp.id_engine_iter
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
          where id_engine_iter = c.id_engine_iter
            and data_fine is null;

         update visti_pareri_competenze
            set modifica = 'N'
          where id_visto_parere = c.id_visto_parere
            and modifica = 'Y';
      end loop;
   end annulla_proposta_delibera;
   
   procedure elimina_dati
   as
       RetVal NUMBER;
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
   delete from odg_oggetti_seduta;
   delete from odg_sedute;

   delete from ORGANI_CONTROLLO_NOTIFICHE_DOC;
   delete from ORGANI_CONTROLLO_NOTIFICHE;
   
   delete from delibere;
   delete from proposte_delibera;

   delete from determine;

   delete from file_allegati;
   delete from gte_lock;
   delete from notifiche_email;
   delete from email;

   update wkf_engine_iter
      set id_step_corrente = null;
      
   delete from wkf_engine_step_attori;
   delete from wkf_engine_step;
   delete from wkf_engine_iter;
   delete from file_allegati_storico;
      
  
   begin
        execute immediate 'begin :this := GDM.F_ELIMINA_DOCUMENTI ( ''SEGRETERIA.ATTI.2_0'', ''DETERMINA'', ''N''); end;' using in out RetVal;        
        execute immediate 'begin :this := GDM.F_ELIMINA_DOCUMENTI ( ''SEGRETERIA.ATTI.2_0'', ''DELIBERA'', ''N''); end;' using in out RetVal;
        execute immediate 'begin :this := GDM.F_ELIMINA_DOCUMENTI ( ''SEGRETERIA.ATTI.2_0'', ''PROPOSTA_DELIBERA'', ''N''); end;' using in out RetVal;
        execute immediate 'begin :this := GDM.F_ELIMINA_DOCUMENTI ( ''SEGRETERIA.ATTI.2_0'', ''VISTO'', ''N''); end;' using in out RetVal;
        execute immediate 'begin :this := GDM.F_ELIMINA_DOCUMENTI ( ''SEGRETERIA.ATTI.2_0'', ''CERTIFICATO'', ''N''); end;' using in out RetVal;
        execute immediate 'begin :this := GDM.F_ELIMINA_DOCUMENTI ( ''SEGRETERIA.ATTI.2_0'', ''ALLEGATO'', ''N''); end;' using in out RetVal;
   exception when others then
        null;
   end;

   end elimina_dati;
   
   procedure cambia_ente_ottica (p_user_db varchar2, p_nuovo_ente in varchar2, p_nuova_ottica in varchar2, p_vecchio_ente in varchar2 default null, p_vecchia_ottica in varchar2 default null)
   as
   begin
   
        -- cambio l'ente dell'applicativo:
        if (p_vecchio_ente is null) then
            cambia_valore_colonna(p_user_db, 'ENTE', p_nuovo_ente);
        else
            cambia_vecchio_valore_colonna(p_user_db, 'ENTE', p_nuovo_ente, p_vecchio_ente);
        end if;

        -- cambio l'ottica dell'applicativo:
        if (p_vecchia_ottica is null) then
            cambia_valore_colonna(p_user_db, 'UNITA_OTTICA', p_nuova_ottica);
        else
            cambia_vecchio_valore_colonna(p_user_db, 'UNITA_OTTICA', p_nuova_ottica, p_vecchia_ottica);
        end if;
               
        -- cambio l'ottica dell'applicativo
        IMPOSTAZIONI_PKG.SET_IMPOSTAZIONE('ENTI_SO4', '*', p_nuovo_ente);
        
        -- cambio l'ottica dell'applicativo
        IMPOSTAZIONI_PKG.SET_IMPOSTAZIONE('OTTICA_SO4', p_nuovo_ente, p_nuova_ottica);
   end;
   
   procedure cambia_valore_colonna (p_user_db in varchar2, p_nome_colonna in varchar2, p_valore in varchar2)
   as
   begin
       for c
          in (select c.table_name, c.column_name
                from all_tab_cols c, all_objects o
               where c.column_name  = p_nome_colonna
                 and c.owner        = p_user_db
                 and o.object_type  = 'TABLE'
                 and o.object_name  = c.table_name
                 and o.owner        = p_user_db)
       loop
          execute immediate
                'update '
             || c.table_name
             || ' set '
             || c.column_name
             || ' = '''||p_valore||''' ';
       end loop;
   end;
   
   procedure cambia_vecchio_valore_colonna (p_user_db in varchar2, p_nome_colonna in varchar2, p_valore in varchar2, p_vecchio_valore in varchar2)
   as
   begin
       for c
          in (select c.table_name
                   , c.column_name
                   , decode(p_vecchio_valore, null, 'is null', ' = '''||p_vecchio_valore||'''') vecchio_valore
                from all_tab_cols c, all_objects o
               where c.column_name  = p_nome_colonna
                 and c.owner        = p_user_db
                 and o.object_type  = 'TABLE'
                 and o.object_name  = c.table_name
                 and o.owner        = p_user_db)
       loop        
          execute immediate
                'update '
             || c.table_name
             || ' set '
             || c.column_name
             || ' = '''||p_valore||''' '
             || ' where '||c.column_name||' '||c.vecchio_valore||' ';
       end loop;
   end;
end assistenza_pkg;

/

CREATE OR REPLACE PACKAGE BODY        impostazioni_pkg
IS
   PROCEDURE add_impostazione (p_codice            IN VARCHAR2,
                               p_descrizione       IN VARCHAR2,
                               p_etichetta         IN VARCHAR2,
                               p_predefinito       IN VARCHAR2,
                               p_modificabile      IN VARCHAR2,
                               p_caratteristiche   IN VARCHAR2)
   /******************************************************************************
   NOME:        add_impostazione
   DESCRIZIONE: aggiunge una nuova impostazione se non già presente.
   PARAMETRI:
   ******************************************************************************/
   IS
   BEGIN
      FOR c IN (SELECT ente
                  FROM impostazioni
                 WHERE codice = 'OTTICA_SO4')
      LOOP
         MERGE INTO IMPOSTAZIONI A
              USING (SELECT p_codice AS CODICE,
                            c.ente AS ENTE,
                            1 AS VERSION,
                            p_caratteristiche AS CARATTERISTICHE,
                            p_descrizione AS DESCRIZIONE,
                            p_etichetta AS ETICHETTA,
                            p_modificabile AS MODIFICABILE,
                            p_predefinito AS PREDEFINITO,
                            p_predefinito AS VALORE
                       FROM DUAL) B
                 ON (A.CODICE = B.CODICE AND A.ENTE = B.ENTE)
         WHEN NOT MATCHED
         THEN
            INSERT     (CODICE,
                        ENTE,
                        VERSION,
                        CARATTERISTICHE,
                        DESCRIZIONE,
                        ETICHETTA,
                        MODIFICABILE,
                        PREDEFINITO,
                        VALORE)
                VALUES (B.CODICE,
                        B.ENTE,
                        B.VERSION,
                        B.CARATTERISTICHE,
                        B.DESCRIZIONE,
                        B.ETICHETTA,
                        B.MODIFICABILE,
                        B.PREDEFINITO,
                        B.VALORE)
         WHEN MATCHED
         THEN
            UPDATE SET A.VERSION = B.VERSION,
                       A.CARATTERISTICHE = B.CARATTERISTICHE,
                       A.DESCRIZIONE = B.DESCRIZIONE,
                       A.ETICHETTA = B.ETICHETTA,
                       A.MODIFICABILE = B.MODIFICABILE,
                       A.PREDEFINITO = B.PREDEFINITO;
      END LOOP;
   END;

   FUNCTION get_impostazione (p_codice IN VARCHAR2, p_ente IN VARCHAR2)
      RETURN VARCHAR2
   /******************************************************************************
   NOME:        get_impostazione
   DESCRIZIONE: restituisce il valore dell'impostazione per l'ente specificato.
   PARAMETRI:   --
   RITORNA:     STRINGA VARCHAR2 CONTENENTE VERSIONE E DATA.
   NOTE:        IL SECONDO NUMERO DELLA VERSIONE CORRISPONDE ALLA REVISIONE
   DEL PACKAGE.
   ******************************************************************************/
   IS
      d_valore_impostazione   IMPOSTAZIONI.VALORE%TYPE;
   BEGIN
      BEGIN
         SELECT valore
           INTO d_valore_impostazione
           FROM impostazioni i
          WHERE i.ente = p_ente AND i.codice = p_codice;
      EXCEPTION
         WHEN NO_DATA_FOUND
         THEN
            SELECT valore
              INTO d_valore_impostazione
              FROM impostazioni i
             WHERE i.ente = '*' AND i.codice = p_codice;
      END;

      RETURN d_valore_impostazione;
   END get_impostazione;

   PROCEDURE set_impostazione (p_codice   IN VARCHAR2,
                               p_ente     IN VARCHAR2,
                               p_valore   IN VARCHAR2)
   /******************************************************************************
   NOME:        set_impostazione
   DESCRIZIONE: imposta il valore dell'impostazione per l'ente specificato.
                se l'ente ha valore NULL, allora il valore dell'impostazione viene settato per tutti gli enti.
   PARAMETRI:   --
   RITORNA:     STRINGA VARCHAR2 CONTENENTE VERSIONE E DATA.
   NOTE:        IL SECONDO NUMERO DELLA VERSIONE CORRISPONDE ALLA REVISIONE
   DEL PACKAGE.
   ******************************************************************************/
   IS
   BEGIN
   
      FOR c IN (SELECT ente
                  FROM impostazioni
                 WHERE codice = 'OTTICA_SO4'
                   and (ente = p_ente or p_ente is null))
      LOOP   
          UPDATE impostazioni
             SET valore = p_valore
           WHERE codice = p_codice AND ente = c.ente;
      end loop;
   END set_impostazione;
END impostazioni_pkg;

/

CREATE OR REPLACE package body          jwf_utility_pkg
is
   
  function is_in_step (p_id_riferimento in varchar2, p_nome_step in varchar2) RETURN number
      /******************************************************************************
       NOME:        is_in_step
       DESCRIZIONE: restituisce il valore dell'impostazione per l'ente specificato.
       PARAMETRI:   --
       RITORNA:     STRINGA VARCHAR2 CONTENENTE VERSIONE E DATA.
       NOTE:        IL SECONDO NUMERO DELLA VERSIONE CORRISPONDE ALLA REVISIONE
                    DEL PACKAGE.
      ******************************************************************************/
   is
        d_ret number(19);
        d_id_documento number(19);
   begin
     
     d_id_documento := to_number(regexp_substr(p_id_riferimento, '[0-9]+$'));
   
     select count(1)
       into d_ret
       from documenti_step s 
      where s.id_documento = d_id_documento
        and s.step_nome like '%'||p_nome_step||'%';
     
     if (d_ret > 0)
     then
        d_ret := 1;
     end if;
     
     return d_ret;
   end is_in_step;
end jwf_utility_pkg;
/

CREATE OR REPLACE package body        reporter_pkg
is
   function versione
      /******************************************************************************
       NOME:        VERSIONE
       DESCRIZIONE: RESTITUISCE LA VERSIONE E LA DATA DI DISTRIBUZIONE DEL PACKAGE.
       PARAMETRI:   --
       RITORNA:     STRINGA VARCHAR2 CONTENENTE VERSIONE E DATA.
       NOTE:        IL SECONDO NUMERO DELLA VERSIONE CORRISPONDE ALLA REVISIONE
                    DEL PACKAGE.
      ******************************************************************************/
      return varchar2
   is
   begin
      return '1.0';
   end versione;

   function get_date_gettoni_presenza (p_chiave_calcolo varchar2, p_utente in varchar2)
      /******************************************************************************
     NOME:        GET_DATE_GETTONI_PRESENZA
     DESCRIZIONE: RESTITUISCE L'ELENCO DI DATE IN CUI L'UTENTE ERA PRESENTE IN SEDUTA
     PARAMETRI:   P_UTENTE
     RITORNA:     VARCHAR2
     ECCEZIONI:
     ANNOTAZIONI: -
     REVISIONI:
     REV. DATA       AUTORE DESCRIZIONE
     ---- ---------- ------ ------------------------------------------------------
     0    05/08/2013  MF     PRIMA EMISSIONE.
    ******************************************************************************/
      return varchar2
   is
      d_elenco_date   varchar2 (32767) := '';
      d_sep           varchar2 (2);
   begin
      for c in (  select distinct to_char (data_seduta, 'dd/mm/yyyy') data_seduta
                    from odg_gettoni_presenza ogp_utente
                   where ogp_utente.utente = p_utente
                     and chiave_calcolo = p_chiave_calcolo
                order by to_date (data_seduta, 'dd/mm/yyyy') asc)
      loop
         d_elenco_date := d_elenco_date || d_sep || c.data_seduta;
         d_sep := ', ';
      end loop;
      return d_elenco_date;
   end get_date_gettoni_presenza;


   function get_presenti_oggetto_seduta (p_id_oggetto_seduta number, p_presente varchar2, p_ruolo varchar2)
      return varchar2
   is
      d_cognome           varchar2 (1000);
      d_nome              varchar2 (1000);
      d_elenco_presenti   varchar2 (32767);
   begin
      for c
         in (  select sequenza, cognome_nome
                 from (select occ.sequenza
                            , initcap (get_cognome_nome (nvl (occ.ni_componente, osp.ni_componente_esterno))) cognome_nome
                         from odg_sedute_partecipanti osp
                            , odg_oggetti_partecipanti oop
                            , odg_oggetti_seduta oos
                            , odg_sedute os
                            , odg_commissioni_componenti occ
                        where oos.id_oggetto_seduta = p_id_oggetto_seduta
                          and oos.id_oggetto_seduta = oop.id_oggetto_seduta
                          and osp.id_seduta_partecipante = oop.id_seduta_partecipante
                          and oos.id_seduta = os.id_seduta
                          and occ.id_commissione_componente(+) = osp.id_commissione_componente
                          and ( (p_ruolo is null) or (p_ruolo is not null and oop.ruolo_partecipante = p_ruolo))
                          and ( (p_presente is null) or (p_presente is not null and oop.presente = p_presente)))
             order by sequenza, cognome_nome)
      loop
         d_elenco_presenti := d_elenco_presenti || c.cognome_nome || ', ';
      end loop;

      if (d_elenco_presenti is not null) then
         d_elenco_presenti := substr (d_elenco_presenti, 1, length (d_elenco_presenti) - 2) || '.';
      end if;

      return d_elenco_presenti;
   end get_presenti_oggetto_seduta;

   function get_presenti_seduta (p_id_seduta number, p_presente varchar2, p_ruolo varchar2)
      return varchar2
   is
      d_cognome           varchar2 (1000);
      d_nome              varchar2 (1000);
      d_elenco_presenti   varchar2 (32767);
   begin
      for c
         in (select sequenza, cognome_nome
               from (  select occ.sequenza
                            , initcap (get_cognome_nome (nvl (occ.ni_componente, osp.ni_componente_esterno))) cognome_nome
                         from odg_sedute_partecipanti osp, odg_sedute os, odg_commissioni_componenti occ
                        where os.id_seduta = p_id_seduta
                          and osp.id_seduta = os.id_seduta
                          and os.id_seduta = osp.id_seduta
                          and occ.id_commissione_componente(+) = osp.id_commissione_componente
                          and (p_ruolo is null or osp.ruolo_partecipante = p_ruolo)
                          and (p_presente is null or osp.presente = p_presente)
                     order by sequenza, cognome_nome))
      loop
         d_elenco_presenti := d_elenco_presenti || c.cognome_nome || ', ';
      end loop;

      if (d_elenco_presenti is not null) then
         d_elenco_presenti := substr (d_elenco_presenti, 1, length (d_elenco_presenti) - 2) || '.';
      end if;

      return d_elenco_presenti;
   end get_presenti_seduta;

   function get_suddivisione_descrizione (p_unita_progr     number
                                        ,  p_unita_dal       date
                                        ,  p_suddivisione    varchar2
                                        ,  p_ente            varchar2)
      return varchar2
   /***************************************************************
  Funzione che restituisce la descrizione di una suddivisione (AREA / SERVIZIO)
  a partire da un'unità su SO4
  ***************************************************************/
   is
   begin
      return utility_pkg.get_suddivisione_descrizione (p_unita_progr
                                                     ,  p_unita_dal
                                                     ,  p_suddivisione
                                                     ,  p_ente);
   end get_suddivisione_descrizione;

   function get_testo_proposta_delibera (p_id_delibera number)
      /******************************************************************************
     NOME:        GET_TESTO_PROPOSTA_DELIBERA
     DESCRIZIONE: RESTITUISCE IL TESTO DELLA PROPOSTA (BLOB)
     PARAMETRI:   P_ID_DELIBERA
     RITORNA:     VARCHAR2
     ECCEZIONI:
     ANNOTAZIONI: -
     REVISIONI:
     REV. DATA       AUTORE DESCRIZIONE
     ---- ---------- ------ ------------------------------------------------------
     0    09/11/2013  MF     PRIMA EMISSIONE.
    ******************************************************************************/
      return blob
   is
      d_testo_proposta     blob;
      d_integrazione_gdm   varchar (1);
      d_ente               delibere.ente%type;
   begin
      dbms_lob.createtemporary (d_testo_proposta, true);

      select ente
        into d_ente
        from delibere d
       where d.id_delibera = p_id_delibera;

      d_integrazione_gdm := impostazioni_pkg.get_impostazione ('INTEGRAZIONE_GDM', d_ente);

      if d_integrazione_gdm = 'Y' then
         /* INIZIO QUERY DA USARE IN PRESENZA DI GDM */
         execute immediate '
          select testo_proposta
            from (select fa.allegato testo_proposta, 1 pos
                    from delibere d, proposte_delibera pd, file_allegati fa
                   where d.id_delibera = :p_id_delibera
                     and d.id_proposta_delibera = pd.id_proposta_delibera
                     and pd.id_file_allegato_testo_odt is not null
                     and pd.id_file_allegato_testo_odt = fa.id_file_allegato
                  union all
                  select gom.testoocr, 2 pos
                    from delibere d
                       ,  proposte_delibera pd
                       ,  file_allegati fa
                       ,  gdm_oggetti_file gom
                   where d.id_delibera = :p_id_delibera
                     and d.id_proposta_delibera = pd.id_proposta_delibera
                     and pd.id_file_allegato_testo_odt is null
                     and pd.id_file_allegato_testo = fa.id_file_allegato
                     and gom.id_oggetto_file = fa.id_file_esterno
                     and fa.id_file_esterno is not null
                  order by 2)
           where rownum = 1' into d_testo_proposta using in p_id_delibera, p_id_delibera;
      else
         execute immediate '
            select fa.allegato testo_proposta
              from delibere d, proposte_delibera pd, file_allegati fa
             where d.id_delibera = :p_id_delibera
               and d.id_proposta_delibera = pd.id_proposta_delibera
               and ( (pd.id_file_allegato_testo_odt is not null
                  and pd.id_file_allegato_testo_odt = fa.id_file_allegato)
                  or (pd.id_file_allegato_testo_odt is null
                  and pd.id_file_allegato_testo = fa.id_file_allegato))' into d_testo_proposta using in p_id_delibera;
      end if;

      return d_testo_proposta;
   end get_testo_proposta_delibera;

   function get_votanti_oggetto_seduta (p_id_oggetto_seduta number, p_voto varchar2)
      return varchar2
   is
      d_elenco   varchar2 (32767);
   begin
      for c in (  select sequenza, cognome_nome
                    from (select occ.sequenza, initcap (get_cognome_nome (nvl (occ.ni_componente, osp.ni_componente_esterno))) cognome_nome
                            from odg_sedute_partecipanti osp
                               ,  odg_oggetti_partecipanti oop
                               ,  odg_oggetti_seduta oos
                               ,  odg_commissioni_componenti occ
                               ,  odg_voti ov
                           where oos.id_oggetto_seduta = p_id_oggetto_seduta
                             and oos.id_oggetto_seduta = oop.id_oggetto_seduta
                             and osp.id_seduta_partecipante = oop.id_seduta_partecipante
                             and occ.id_commissione_componente(+) = osp.id_commissione_componente
                             and oop.id_voto = ov.id_voto(+)
                             and ( (p_voto is null)
                               or (p_voto is not null
                               and ov.valore = p_voto)))
                order by sequenza, cognome_nome asc)
      loop
         d_elenco := d_elenco || c.cognome_nome || ', ';
      end loop;

      if (d_elenco is not null) then
         d_elenco := substr (d_elenco, 1, length (d_elenco) - 2) || '.';
      end if;

      return d_elenco;
   end get_votanti_oggetto_seduta;

   function numero_lettere (a_numero in number)
      /******************************************************************************
     NOME:        NUMERO_LETTERE
     DESCRIZIONE: RESTITUISCE IL NUMERO IN FORMATO LETTERE
     PARAMETRI:   A_NUMERO NUMBER
     RITORNA:     VARCHAR2
     ECCEZIONI:
     ANNOTAZIONI: -
     REVISIONI:
     REV. DATA       AUTORE DESCRIZIONE
     ---- ---------- ------ ------------------------------------------------------
     0    17/12/2008 LT     PRIMA EMISSIONE.
    ******************************************************************************/
      return varchar2
   is
      d_importo       varchar2 (12);
      d_importo_dec   varchar2 (2);
      d_cifra         number;
      d_stringa       varchar2 (256);
   begin
      d_stringa := null;
      d_importo := lpad (to_char (trunc (a_numero)), 12, '0');
      d_cifra := mod (round (a_numero, 2), 1) * 100;

      if d_cifra > 9 then
         d_importo_dec := rpad (to_char (mod (round (a_numero, 2), 1) * 100), 2, '0');
      else
         d_importo_dec := lpad (to_char (mod (round (a_numero, 2), 1) * 100), 2, '0');
      end if;

      --
      for i in 1 .. 12
      loop
         d_cifra := substr (d_importo, i, 1);

         --
         /* TEST SULLE CENTINAIA */
         --
         if i in (1, 4, 7, 10) then
            --
            if d_cifra = 2 then
               d_stringa := d_stringa || 'DUE';
            elsif d_cifra = 3 then
               d_stringa := d_stringa || 'TRE';
            elsif d_cifra = 4 then
               d_stringa := d_stringa || 'QUATTRO';
            elsif d_cifra = 5 then
               d_stringa := d_stringa || 'CINQUE';
            elsif d_cifra = 6 then
               d_stringa := d_stringa || 'SEI';
            elsif d_cifra = 7 then
               d_stringa := d_stringa || 'SETTE';
            elsif d_cifra = 8 then
               d_stringa := d_stringa || 'OTTO';
            elsif d_cifra = 9 then
               d_stringa := d_stringa || 'NOVE';
            end if;

            if d_cifra != 0 then
               d_stringa := d_stringa || 'CENTO';
            end if;
         /* TEST SULLE DECINE */
         elsif i in (2, 5, 8, 11) then
            if d_cifra = 2 then
               d_stringa := d_stringa || 'VENT';
            elsif d_cifra = 3 then
               d_stringa := d_stringa || 'TRENT';
            elsif d_cifra = 4 then
               d_stringa := d_stringa || 'QUARANT';
            elsif d_cifra = 5 then
               d_stringa := d_stringa || 'CINQUANT';
            elsif d_cifra = 6 then
               d_stringa := d_stringa || 'SESSANT';
            elsif d_cifra = 7 then
               d_stringa := d_stringa || 'SETTANT';
            elsif d_cifra = 8 then
               d_stringa := d_stringa || 'OTTANT';
            elsif d_cifra = 9 then
               d_stringa := d_stringa || 'NOVANT';
            end if;

            if d_cifra = 2 then
               if substr (d_importo, i + 1, 1) in (1, 8) then
                  null;
               else
                  d_stringa := d_stringa || 'I';
               end if;
            elsif d_cifra > 2 then
               if substr (d_importo, i + 1, 1) in (1, 8) then
                  null;
               else
                  d_stringa := d_stringa || 'A';
               end if;
            end if;

            if d_cifra = 1 then
               if substr (d_importo, i + 1, 1) = 0 then
                  d_stringa := d_stringa || 'DIECI';
               elsif substr (d_importo, i + 1, 1) = 1 then
                  d_stringa := d_stringa || 'UNDICI';
               elsif substr (d_importo, i + 1, 1) = 2 then
                  d_stringa := d_stringa || 'DODICI';
               elsif substr (d_importo, i + 1, 1) = 3 then
                  d_stringa := d_stringa || 'TREDICI';
               elsif substr (d_importo, i + 1, 1) = 4 then
                  d_stringa := d_stringa || 'QUATTORDICI';
               elsif substr (d_importo, i + 1, 1) = 5 then
                  d_stringa := d_stringa || 'QUINDICI';
               elsif substr (d_importo, i + 1, 1) = 6 then
                  d_stringa := d_stringa || 'SEDICI';
               elsif substr (d_importo, i + 1, 1) = 7 then
                  d_stringa := d_stringa || 'DICIASSETTE';
               elsif substr (d_importo, i + 1, 1) = 8 then
                  d_stringa := d_stringa || 'DICIOTTO';
               elsif substr (d_importo, i + 1, 1) = 9 then
                  d_stringa := d_stringa || 'DICIANNOVE';
               end if;
            else
               if substr (d_importo, i + 1, 1) = 1 then
                  if substr (d_importo, i - 1, 3) = '001' then
                     if i in (2, 5) then
                        d_stringa := d_stringa || 'UN';
                     elsif i = 11 then
                        d_stringa := d_stringa || 'UNO';
                     end if;
                  else
                     d_stringa := d_stringa || 'UNO';
                  end if;
               elsif substr (d_importo, i + 1, 1) = 2 then
                  d_stringa := d_stringa || 'DUE';
               elsif substr (d_importo, i + 1, 1) = 3 then
                  d_stringa := d_stringa || 'TRE';
               elsif substr (d_importo, i + 1, 1) = 4 then
                  d_stringa := d_stringa || 'QUATTRO';
               elsif substr (d_importo, i + 1, 1) = 5 then
                  d_stringa := d_stringa || 'CINQUE';
               elsif substr (d_importo, i + 1, 1) = 6 then
                  d_stringa := d_stringa || 'SEI';
               elsif substr (d_importo, i + 1, 1) = 7 then
                  d_stringa := d_stringa || 'SETTE';
               elsif substr (d_importo, i + 1, 1) = 8 then
                  d_stringa := d_stringa || 'OTTO';
               elsif substr (d_importo, i + 1, 1) = 9 then
                  d_stringa := d_stringa || 'NOVE';
               end if;
            end if;
         end if;

         --
         if i = 2 then
            if substr (d_importo, 1, 3) = '000' then
               null;
            elsif substr (d_importo, 1, 3) = '001' then
               d_stringa := d_stringa || 'MILIARDO';
            else
               d_stringa := d_stringa || 'MILIARDI';
            end if;
         elsif i = 5 then
            if substr (d_importo, 4, 3) = '000' then
               null;
            elsif substr (d_importo, 4, 3) = '001' then
               d_stringa := d_stringa || 'MILIONE';
            else
               d_stringa := d_stringa || 'MILIONI';
            end if;
         elsif i = 8 then
            if substr (d_importo, 7, 3) = '000' then
               null;
            elsif substr (d_importo, 7, 3) = '001' then
               d_stringa := d_stringa || 'MILLE';
            else
               d_stringa := d_stringa || 'MILA';
            end if;
         end if;
      --
      end loop;

      --
      if d_importo = '000000000000' then
         d_stringa := 'ZERO';
      end if;

      --
      if d_importo_dec != '00' then
         d_stringa := d_stringa || ' VIRGOLA ';
         d_cifra := substr (d_importo_dec, 1, 1);

         if substr (d_importo_dec, 2, 1) != 0 then
            if d_cifra = 0 then
               d_stringa := d_stringa || 'ZERO';
            elsif d_cifra = 2 then
               d_stringa := d_stringa || 'VENT';
            elsif d_cifra = 3 then
               d_stringa := d_stringa || 'TRENT';
            elsif d_cifra = 4 then
               d_stringa := d_stringa || 'QUARANT';
            elsif d_cifra = 5 then
               d_stringa := d_stringa || 'CINQUANT';
            elsif d_cifra = 6 then
               d_stringa := d_stringa || 'SESSANT';
            elsif d_cifra = 7 then
               d_stringa := d_stringa || 'SETTANT';
            elsif d_cifra = 8 then
               d_stringa := d_stringa || 'OTTANT';
            elsif d_cifra = 9 then
               d_stringa := d_stringa || 'NOVANT';
            end if;

            if d_cifra = 2 then
               if substr (d_importo_dec, 2, 1) in (1, 8) then
                  null;
               else
                  d_stringa := d_stringa || 'I';
               end if;
            elsif d_cifra > 2 then
               if substr (d_importo_dec, 2, 1) in (1, 8) then
                  null;
               else
                  d_stringa := d_stringa || 'A';
               end if;
            end if;

            if d_cifra = 1 then
               if substr (d_importo_dec, 2, 1) = 1 then
                  d_stringa := d_stringa || 'UNDICI';
               elsif substr (d_importo_dec, 2, 1) = 2 then
                  d_stringa := d_stringa || 'DODICI';
               elsif substr (d_importo_dec, 2, 1) = 3 then
                  d_stringa := d_stringa || 'TREDICI';
               elsif substr (d_importo_dec, 2, 1) = 4 then
                  d_stringa := d_stringa || 'QUATTORDICI';
               elsif substr (d_importo_dec, 2, 1) = 5 then
                  d_stringa := d_stringa || 'QUINDICI';
               elsif substr (d_importo_dec, 2, 1) = 6 then
                  d_stringa := d_stringa || 'SEDICI';
               elsif substr (d_importo_dec, 2, 1) = 7 then
                  d_stringa := d_stringa || 'DICIASSETTE';
               elsif substr (d_importo_dec, 2, 1) = 8 then
                  d_stringa := d_stringa || 'DICIOTTO';
               elsif substr (d_importo_dec, 2, 1) = 9 then
                  d_stringa := d_stringa || 'DICIANNOVE';
               end if;
            end if;
         end if;

         if d_cifra = 1
        and substr (d_importo_dec, 2, 1) > 0 then
            null;
         else
            if substr (d_importo_dec, 2, 1) != 0 then
               d_cifra := substr (d_importo_dec, 2, 1);
            end if;

            if d_cifra = 1 then
               d_stringa := d_stringa || 'UNO';
            elsif d_cifra = 2 then
               d_stringa := d_stringa || 'DUE';
            elsif d_cifra = 3 then
               d_stringa := d_stringa || 'TRE';
            elsif d_cifra = 4 then
               d_stringa := d_stringa || 'QUATTRO';
            elsif d_cifra = 5 then
               d_stringa := d_stringa || 'CINQUE';
            elsif d_cifra = 6 then
               d_stringa := d_stringa || 'SEI';
            elsif d_cifra = 7 then
               d_stringa := d_stringa || 'SETTE';
            elsif d_cifra = 8 then
               d_stringa := d_stringa || 'OTTO';
            elsif d_cifra = 9 then
               d_stringa := d_stringa || 'NOVE';
            end if;
         end if;
      end if;

      --
      return lower (d_stringa);
   end numero_lettere;

   function mese_lettere (a_numero in number)
      return varchar2
   is
      /******************************************************************************
       NOME:        MESE_LETTERE
       DESCRIZIONE: RESTITUISCE IL MESE IN LETTERE
       PARAMETRI:   A_NUMERO NUMBER MESE IN NUMERO
       RITORNA:     VARCHAR2 : MESE IN LETTERE
       ECCEZIONI:
       ANNOTAZIONI: -
       REVISIONI:
       REV. DATA       AUTORE DESCRIZIONE
       ---- ---------- ------ ------------------------------------------------------
       0    17/12/2008 LT     PRIMA EMISSIONE.
      ******************************************************************************/
      d_stringa   varchar2 (256);
   begin
      if a_numero = 1 then
         d_stringa := 'GENNAIO';
      elsif a_numero = 2 then
         d_stringa := 'FEBBRAIO';
      elsif a_numero = 3 then
         d_stringa := 'MARZO';
      elsif a_numero = 4 then
         d_stringa := 'APRILE';
      elsif a_numero = 5 then
         d_stringa := 'MAGGIO';
      elsif a_numero = 6 then
         d_stringa := 'GIUGNO';
      elsif a_numero = 7 then
         d_stringa := 'LUGLIO';
      elsif a_numero = 8 then
         d_stringa := 'AGOSTO';
      elsif a_numero = 9 then
         d_stringa := 'SETTEMBRE';
      elsif a_numero = 10 then
         d_stringa := 'OTTOBRE';
      elsif a_numero = 11 then
         d_stringa := 'NOVEMBRE';
      elsif a_numero = 12 then
         d_stringa := 'DICEMBRE';
      end if;

      return lower (d_stringa);
   end mese_lettere;

   function giorno_lettere (p_data in date)
      return varchar2
   is
      /******************************************************************************
      NOME:        GIORNO
      DESCRIZIONE: RESTITUISCE IL GIORNO DELLA SETTIMANA
      PARAMETRI:   P_DATA DATE
      RITORNA:     VARCHAR2 : GIORNO DELLA SETTIMANA
      ECCEZIONI:
      ANNOTAZIONI: -
      REVISIONI:
      REV. DATA       AUTORE DESCRIZIONE
      ---- ---------- ------ ------------------------------------------------------
      0    22/12/2008 LT     PRIMA EMISSIONE.
     ******************************************************************************/
      d_valore     varchar2 (10);
      dep_giorno   varchar2 (10);
   begin
      select trim (upper (to_char (p_data, 'DAY', 'NLS_DATE_LANGUAGE=italian'))) into dep_giorno from dual;

      if dep_giorno = 'LUNEDÌ' then
         d_valore := 'lunedì';
      elsif dep_giorno = 'MARTEDÌ' then
         d_valore := 'martedì';
      elsif dep_giorno = 'MERCOLEDÌ' then
         d_valore := 'mercoledì';
      elsif dep_giorno = 'GIOVEDÌ' then
         d_valore := 'giovedì';
      elsif dep_giorno = 'VENERDÌ' then
         d_valore := 'venerdì';
      elsif dep_giorno = 'SABATO' then
         d_valore := 'sabato';
      elsif dep_giorno = 'DOMENICA' then
         d_valore := 'domenica';
      end if;
      --      if dep_giorno = 'LUNEDÌ' then
      --         d_valore := 'LUNEDI' || chr (96);
      --      elsif dep_giorno = 'MARTEDÌ' then
      --         d_valore := 'MARTEDI' || chr (96);
      --      elsif dep_giorno = 'MERCOLEDÌ' then
      --         d_valore := 'MERCOLEDI' || chr (96);
      --      elsif dep_giorno = 'GIOVEDÌ' then
      --         d_valore := 'GIOVEDI' || chr (96);
      --      elsif dep_giorno = 'VENERDÌ' then
      --         d_valore := 'VENERDI' || chr (96);
      --      elsif dep_giorno = 'SABATO' then
      --         d_valore := 'SABATO';
      --      elsif dep_giorno = 'DOMENICA' then
      --         d_valore := 'DOMENICA';
      --      end if;

      return d_valore;
   end giorno_lettere;

   function get_desc_ascendenti_unita (p_codice_uo     varchar2
                                     ,  p_data          date default null
                                     ,  p_ottica        varchar2
                                     ,  p_separatore    varchar2 default null)
      return varchar2
   is
      /******************************************************************************
       NOME:        GET_DESC_ASCENDENTI_UNITA
       DESCRIZIONE: RESTITUISCE LA STRUTTURA DEGLI ASCENDENTI A PARTIRE DALL'UNITA'
                    CON ETICHETTA E SEPARATORE
       PARAMETRI:   P_COSICE_UO, P_DATA, P_OTTICA, P_SEPARATORE
       RITORNA:     VARCHAR2
       ECCEZIONI:
       ANNOTAZIONI: -
       REVISIONI:
       REV. DATA       AUTORE   DESCRIZIONE
       ---- ---------- -------- ------------------------------------------------------
       0    14/0872014 MFERRARA PRIMA EMISSIONE.
       ******************************************************************************/

      v_struttura           sys_refcursor;

      d_ottica              varchar2 (18);
      d_separatore          varchar2 (1) := chr (10);
      d_desc_suddivisione   varchar2 (60);
      d_result              varchar2 (32000);

      type v_struttura_t is record
      (
         progr_uo          number (8)
       ,  codice_uo         varchar2 (50)
       ,  desc_uo           varchar2 (240)
       ,  dal               date
       ,  al                date
       ,  id_suddivisione   number (8)
      );

      v_struttura_row       v_struttura_t;
   begin
      d_ottica := so4_util.set_ottica_default (p_ottica);

      if p_separatore is not null then
         d_separatore := p_separatore;
      end if;

      v_struttura := so4_util.unita_get_ascendenti_sudd (p_codice_uo, p_data, d_ottica);

      if v_struttura%isopen then
         loop
            fetch v_struttura into v_struttura_row;
            exit when v_struttura%notfound;


            begin
               select descrizione
                 into d_desc_suddivisione
                 from so4_suddivisioni_struttura
                where ottica = p_ottica
                  and id_suddivisione = v_struttura_row.id_suddivisione;
            exception
               when others then
                  d_desc_suddivisione := null;
            end;

            d_result := d_desc_suddivisione || ': ' || v_struttura_row.desc_uo || d_separatore || d_result;
         end loop;
      end if;

      return d_result;
   exception
      when others then
         return null;
   end;


   function get_cognome_nome (p_ni number)
      return varchar2
   is
      /******************************************************************************
       NOME:        GET_NOME_COGNOME
       DESCRIZIONE: RESTITUISCE IL COGNOME||' '||NOME DEL SOGGETTO LEGGENDO DALLA VISTA AS4_V_SOGGETTI_CORRENTI
       PARAMETRI:   P_NI
       RITORNA:     VARCHAR2
       ECCEZIONI:
       ANNOTAZIONI: -
       REVISIONI:
       REV. DATA       AUTORE   DESCRIZIONE
       ---- ---------- -------- ------------------------------------------------------
       0    08/09/2015 ESASDELLI PRIMA EMISSIONE.
       ******************************************************************************/
      d_cognome_nome   varchar2 (500);
   begin
      if (p_ni is null) then
         return '';
      end if;

      begin
         select cognome || ' ' || nome
           into d_cognome_nome
           from as4_v_soggetti_correnti
          where ni = p_ni
            and rownum = 1; -- questo serve per parare i casi (errati) in cui un soggetto
                            -- in anagrafica ha più di un utente collegato:
                            -- in tali casi infatti nella vista as4_v_soggetti_correnti
                            -- ci sono più righe con stesso ni.

         return d_cognome_nome;
      exception
         when no_data_found then
            -- in caso di errore, cioè di ni non trovato, ritorno stringa vuota in modo che la stampa
            -- comunque funzioni.
            return '';
      end;
   end;

   function get_nome (p_ni number)
      return varchar2
   is
      /******************************************************************************
       NOME:        GET_NOME_COGNOME
       DESCRIZIONE: RESTITUISCE IL COGNOME||' '||NOME DEL SOGGETTO LEGGENDO DALLA VISTA AS4_V_SOGGETTI_CORRENTI
       PARAMETRI:   P_NI
       RITORNA:     VARCHAR2
       ECCEZIONI:
       ANNOTAZIONI: -
       REVISIONI:
       REV. DATA       AUTORE   DESCRIZIONE
       ---- ---------- -------- ------------------------------------------------------
       0    08/09/2015 ESASDELLI PRIMA EMISSIONE.
       ******************************************************************************/
      d_cognome_nome   varchar2 (500);
   begin
      if (p_ni is null) then
         return '';
      end if;

      begin
         select nome
           into d_cognome_nome
           from as4_v_soggetti_correnti
          where ni = p_ni
            and rownum = 1; -- questo serve per parare i casi (errati) in cui un soggetto
                            -- in anagrafica ha più di un utente collegato:
                            -- in tali casi infatti nella vista as4_v_soggetti_correnti
                            -- ci sono più righe con stesso ni.

         return d_cognome_nome;
      exception
         when no_data_found then
            -- in caso di errore, cioè di ni non trovato, ritorno stringa vuota in modo che la stampa
            -- comunque funzioni.
            return '';
      end;
   end;

   function get_cognome (p_ni number)
      return varchar2
   is
      /******************************************************************************
       NOME:        GET_NOME_COGNOME
       DESCRIZIONE: RESTITUISCE IL COGNOME||' '||NOME DEL SOGGETTO LEGGENDO DALLA VISTA AS4_V_SOGGETTI_CORRENTI
       PARAMETRI:   P_NI
       RITORNA:     VARCHAR2
       ECCEZIONI:
       ANNOTAZIONI: -
       REVISIONI:
       REV. DATA       AUTORE   DESCRIZIONE
       ---- ---------- -------- ------------------------------------------------------
       0    08/09/2015 ESASDELLI PRIMA EMISSIONE.
       ******************************************************************************/
      d_cognome_nome   varchar2 (500);
   begin
      if (p_ni is null) then
         return '';
      end if;

      begin
         select cognome
           into d_cognome_nome
           from as4_v_soggetti_correnti
          where ni = p_ni
            and rownum = 1; -- questo serve per parare i casi (errati) in cui un soggetto
                            -- in anagrafica ha più di un utente collegato:
                            -- in tali casi infatti nella vista as4_v_soggetti_correnti
                            -- ci sono più righe con stesso ni.

         return d_cognome_nome;
      exception
         when no_data_found then
            -- in caso di errore, cioè di ni non trovato, ritorno stringa vuota in modo che la stampa
            -- comunque funzioni.
            return '';
      end;
   end;
end reporter_pkg;
/

CREATE OR REPLACE package body        utility_pkg
is
   function get_data_firma_delibera (p_id_delibera number, p_tipo_soggetto varchar2)
      return date
   /***************************************************************
   Funzione che restituisce la data di firma di una delibera
   ***************************************************************/
   is
      d_data   date;
   begin                     
      for c in (  select data_firma
                    from firmatari f, delibere_soggetti ds
                   where f.id_delibera = p_id_delibera
                     and ds.id_delibera = p_id_delibera
                     and f.utente_firmatario = ds.utente
                     and ds.tipo_soggetto = p_tipo_soggetto
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by f.sequenza asc)
      loop
         d_data := c.data_firma;
         exit;
      end loop;

      return d_data;
   end get_data_firma_delibera;
   
   function get_data_firma_par_contabile (p_id_delibera number)
      return date
   /***************************************************************
   Funzione che restituisce la data di firma del parere contabile di una delibera
   Se ci sono due visti con il campo CONTABILE = Y in tipologia
   allora la funzione ritorna uno dei due firmatari (a caso)
   ***************************************************************/
   is
      d_data   date;
   begin
      for c in (  select f.data_firma
                    from firmatari f
                       ,  visti_pareri vp
                       ,  delibere d
                       ,  tipi_visto_parere tvp
                   where d.id_delibera = p_id_delibera
                     and vp.id_proposta_delibera = d.id_proposta_delibera
                     and vp.id_tipologia = tvp.id_tipo_visto_parere
                     and tvp.contabile = 'Y'
                     and f.id_visto_parere = vp.id_visto_parere
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_data := c.data_firma;
         exit;
      end loop;

      return d_data;
   end get_data_firma_par_contabile;

   function get_data_firma_visto_contabile (p_id_determina number)
      return date
   /***************************************************************
   Funzione che restituisce la data di firma del visto contabile di una determina
   Se ci sono due visti con il campo CONTABILE = Y in tipologia
   allora la funzione ritorna uno dei due firmatari (a caso)
   ***************************************************************/
   is
      d_data   date;
   begin
      for c in (  select f.data_firma
                    from firmatari f, visti_pareri vp, tipi_visto_parere tvp
                   where vp.id_determina = p_id_determina
                     and vp.id_tipologia = tvp.id_tipo_visto_parere
                     and tvp.contabile = 'Y'
                     and f.id_visto_parere = vp.id_visto_parere
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_data := c.data_firma;
         exit;
      end loop;

      return d_data;
   end get_data_firma_visto_contabile;

   function get_firmatario_delibera (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2
   /***************************************************************
   Funzione che restituisce la denominazione di un firmatario di una delibera
   ***************************************************************/
   is
      d_firmatario   varchar2 (4000);
   begin
      for c in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                    from firmatari f, as4_v_soggetti_correnti, delibere_soggetti ds
                   where f.id_delibera = p_id_delibera
                     and f.utente_firmatario = as4_v_soggetti_correnti.utente
                     and ds.id_delibera = p_id_delibera
                     and f.utente_firmatario = ds.utente
                     and ds.tipo_soggetto = p_tipo_soggetto
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by f.sequenza asc)
      loop
         d_firmatario := c.nominativo;
         exit;
      end loop;

      return d_firmatario;
   end get_firmatario_delibera;
   
   function get_firmatario_par_contabile (p_id_delibera number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la denominazione del firmatario del parere contabile di una delibera
   Se ci sono due visti con il campo CONTABILE = Y in tipologia
   allora la funzione ritorna uno dei due firmatari (a caso)
   ***************************************************************/
   is
      d_firmatario   varchar2 (4000);
   begin
      for c in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                    from firmatari f
                       ,  as4_v_soggetti_correnti
                       ,  visti_pareri vp
                       ,  delibere d
                       ,  tipi_visto_parere tvp
                   where d.id_delibera = p_id_delibera
                     and vp.id_proposta_delibera = d.id_proposta_delibera
                     and vp.id_tipologia = tvp.id_tipo_visto_parere
                     and tvp.contabile = 'Y'
                     and f.id_visto_parere = vp.id_visto_parere
                     and f.utente_firmatario = as4_v_soggetti_correnti.utente
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_firmatario := c.nominativo;
         exit;
      end loop;

      return d_firmatario;
   end get_firmatario_par_contabile;

   function get_firmatario_visto_contabile (p_id_determina number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la denominazione del firmatario del visto contabile di una determina
   Se ci sono due visti con il campo CONTABILE = Y in tipologia
   allora la funzione ritorna uno dei due firmatari (a caso)
   ***************************************************************/
   is
      d_firmatario   varchar2 (4000);
   begin
      for c in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                    from firmatari f
                       ,  as4_v_soggetti_correnti
                       ,  visti_pareri vp
                       ,  tipi_visto_parere tvp
                   where vp.id_determina = p_id_determina
                     and vp.id_tipologia = tvp.id_tipo_visto_parere
                     and tvp.contabile = 'Y'
                     and f.id_visto_parere = vp.id_visto_parere
                     and f.utente_firmatario = as4_v_soggetti_correnti.utente
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_firmatario := c.nominativo;
         exit;
      end loop;

      return d_firmatario;
   end get_firmatario_visto_contabile;

   function get_unita_parere_contabile (p_id_delibera number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la descrizione dell'unità di redazione del parere contabile di una delibera
   Se ci sono due visti con il campo CONTABILE = Y in tipologia
   allora la funzione ritorna uno dei due firmatari (a caso)
   ***************************************************************/
   is
      d_descrizione   varchar2 (4000);
   begin
      for c in (  select get_uo_descrizione (unita_progr, unita_dal) uo_visto
                    from visti_pareri vp, tipi_visto_parere tvp, delibere d
                   where d.id_delibera = p_id_delibera
                     and vp.id_proposta_delibera = d.id_proposta_delibera
                     and vp.id_tipologia = tvp.id_tipo_visto_parere
                     and tvp.contabile = 'Y'
                order by vp.data_upd desc)
      loop
         d_descrizione := c.uo_visto;
         exit;
      end loop;

      return d_descrizione;
   end get_unita_parere_contabile;

   function get_unita_visto_contabile (p_id_determina number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la descrizione dell'unità di redazione del visto contabile di una determina
   Se ci sono due visti con il campo CONTABILE = Y in tipologia
   allora la funzione ritorna uno dei due firmatari (a caso)
   ***************************************************************/
   is
      d_descrizione   varchar2 (4000);
   begin
      for c in (  select get_uo_descrizione (unita_progr, unita_dal) uo_visto
                    from visti_pareri vp, tipi_visto_parere tvp
                   where vp.id_determina = p_id_determina
                     and vp.id_tipologia = tvp.id_tipo_visto_parere
                     and tvp.contabile = 'Y'
                order by vp.data_upd desc)
      loop
         d_descrizione := c.uo_visto;
         exit;
      end loop;

      return d_descrizione;
   end get_unita_visto_contabile;

   function get_prima_data_firma_certif (p_id_certificato number)
      return date
   /***************************************************************
   Funzione che restituisce la data in cui ha firmato il primo firmatario di un certificato
   A casalecchio è capitato che un certificato venisse firmato due volte
   ***************************************************************/
   is
      d_data   date;
   begin
      for c in (  select data_firma
                    from firmatari f
                   where f.id_certificato = p_id_certificato
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_data := c.data_firma;
         exit;
      end loop;

      return d_data;
   end get_prima_data_firma_certif;
   
   function get_prima_data_firma_delibera (p_id_delibera number)
      return date
   /***************************************************************
   Funzione che restituisce la data in cui ha firmato il primo firmatario di una delibera
   ***************************************************************/
   is
      d_data   date;
   begin
      for c in (  select data_firma
                    from firmatari f
                   where f.id_delibera = p_id_delibera
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_data := c.data_firma;
         exit;
      end loop;

      return d_data;
   end get_prima_data_firma_delibera;
      
   function get_prima_data_firma_determina (p_id_determina number)
      return date
   /***************************************************************
   Funzione che restituisce la data in cui ha firmato il primo firmatario di una determina
   ***************************************************************/
   is
      d_data   date;
   begin
      for c in (  select data_firma
                    from firmatari f
                   where f.id_determina = p_id_determina
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_data := c.data_firma;
         exit;
      end loop;

      return d_data;
   end get_prima_data_firma_determina;

   function get_prima_data_firma_prop_deli (p_id_proposta_delibera number)
      return date
   /***************************************************************
   Funzione che restituisce la data in cui ha firmato il primo firmatario di una proposta di delibera
   ***************************************************************/
   is
      d_data   date;
   begin
      for c in (  select data_firma
                    from firmatari f
                   where f.id_proposta_delibera = p_id_proposta_delibera
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_data := c.data_firma;
         exit;
      end loop;

      return d_data;
   end get_prima_data_firma_prop_deli;

   function get_primo_firmatario_determina (p_id_determina number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la denominazione del primo firmatario di una determina
   ***************************************************************/
   is
      d_firmatario   varchar2 (4000);
   begin
      for c in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                    from firmatari f, as4_v_soggetti_correnti
                   where f.id_determina = p_id_determina
                     and f.utente_firmatario = as4_v_soggetti_correnti.utente
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_firmatario := c.nominativo;
         exit;
      end loop;

      return d_firmatario;
   end get_primo_firmatario_determina;

   function get_sogg_notifica_delibera (p_id_delibera number)
      return varchar2
   /***************************************************************
   Funzione che restituisce l'elenco delle persone che hanno ricevuto la notifica
   di esecutività di una delibera (separati da ;)
   ***************************************************************/
   is
      d_elenco   varchar2 (4000);
   begin
      begin
         for c in (select avsc.cognome || ' ' || avsc.nome nominativo
                     from destinatari_notifiche dn, as4_v_soggetti_correnti avsc
                    where tipo_destinatario = 'INTERNO'
                      and dn.utente_ad4 = avsc.utente
                      and id_delibera = p_id_delibera
                   union
                   select avsc.cognome || ' ' || avsc.nome nominativo
                     from destinatari_notifiche dn, as4_v_soggetti_correnti avsc, delibere d
                    where tipo_destinatario = 'INTERNO'
                      and dn.utente_ad4 = avsc.utente
                      and d.id_delibera = p_id_delibera
                      and d.id_proposta_delibera = dn.id_proposta_delibera
                   union
                   select avsc.cognome || ' ' || avsc.nome nominativo
                     from destinatari_notifiche dn
                        ,  so4_v_utenti_ruoli_sogg_uo svurs
                        ,  as4_v_soggetti_correnti avsc
                        ,  delibere d
                    where dn.tipo_destinatario = 'INTERNO'
                      and d.id_delibera = p_id_delibera
                      and dn.id_delibera = d.id_delibera
                      and svurs.utente = avsc.utente
                      and svurs.uo_progr = dn.unita_progr
                      and svurs.ottica = dn.unita_ottica
                      and impostazioni_pkg.get_impostazione ('RUOLO_SO4_NOTIFICHE', d.ente) = svurs.ruolo
                   union
                   select avsc.cognome || ' ' || avsc.nome nominativo
                     from destinatari_notifiche dn
                        ,  so4_v_utenti_ruoli_sogg_uo svurs
                        ,  as4_v_soggetti_correnti avsc
                        ,  proposte_delibera pd
                        ,  delibere d
                    where dn.tipo_destinatario = 'INTERNO'
                      and d.id_delibera = p_id_delibera
                      and d.id_proposta_delibera = pd.id_proposta_delibera
                      and dn.id_proposta_delibera = pd.id_proposta_delibera
                      and svurs.utente = avsc.utente
                      and svurs.uo_progr = dn.unita_progr
                      and svurs.ottica = dn.unita_ottica
                      and impostazioni_pkg.get_impostazione ('RUOLO_SO4_NOTIFICHE', d.ente) = svurs.ruolo
                   union
                   select avsc.cognome || ' ' || avsc.nome nominativo
                     from proposte_delibera_soggetti pds, as4_v_soggetti_correnti avsc, delibere d
                    where d.id_delibera = p_id_delibera
                      and pds.id_proposta_delibera = d.id_proposta_delibera
                      and pds.tipo_soggetto = 'REDATTORE'
                      and pds.utente = avsc.utente
                   union
                   select avsc.cognome || ' ' || avsc.nome nominativo
                     from proposte_delibera_soggetti pds, as4_v_soggetti_correnti avsc, delibere d
                    where d.id_delibera = p_id_delibera
                      and pds.id_proposta_delibera = d.id_proposta_delibera
                      and pds.tipo_soggetto = 'FUNZIONARIO'
                      and pds.utente = avsc.utente
                   union
                   select avsc.cognome || ' ' || avsc.nome nominativo
                     from proposte_delibera_soggetti pds, as4_v_soggetti_correnti avsc, delibere d
                    where d.id_delibera = p_id_delibera
                      and pds.id_proposta_delibera = d.id_proposta_delibera
                      and pds.tipo_soggetto = 'DIRIGENTE'
                      and pds.utente = avsc.utente
                   union
                   select avsc.cognome || ' ' || avsc.nome nominativo
                     from proposte_delibera_soggetti pds
                        ,  so4_v_utenti_ruoli_sogg_uo svurs
                        ,  as4_v_soggetti_correnti avsc
                        ,  delibere d
                    where d.id_delibera = p_id_delibera
                      and pds.id_proposta_delibera = d.id_proposta_delibera
                      and pds.tipo_soggetto = 'UO_PROPONENTE'
                      and svurs.utente = avsc.utente
                      and svurs.uo_progr = pds.unita_progr
                      and svurs.ottica = pds.unita_ottica
                      and impostazioni_pkg.get_impostazione ('RUOLO_SO4_NOTIFICHE', d.ente) = svurs.ruolo
                   order by 1)
         loop
            d_elenco := d_elenco || c.nominativo || '; ';
         end loop;
      exception
         when others then
            d_elenco := '';
      end;

      return d_elenco;
   end get_sogg_notifica_delibera;

   function get_sogg_notifica_determina (p_id_determina number)
      return varchar2
   /***************************************************************
   Funzione che restituisce l'elenco delle persone che hanno ricevuto la notifica
   di esecutività di una determina (separati da ;)
   ***************************************************************/
   is
      d_elenco   varchar2 (4000);
   begin
      begin
         for c in (select avsc.cognome || ' ' || avsc.nome nominativo
                     from destinatari_notifiche dn, as4_v_soggetti_correnti avsc
                    where tipo_destinatario = 'INTERNO'
                      and dn.utente_ad4 = avsc.utente
                      and id_determina = p_id_determina
                   union
                   select avsc.cognome || ' ' || avsc.nome nominativo
                     from destinatari_notifiche dn
                        ,  so4_v_utenti_ruoli_sogg_uo svurs
                        ,  as4_v_soggetti_correnti avsc
                        ,  determine d
                    where dn.tipo_destinatario = 'INTERNO'
                      and d.id_determina = p_id_determina
                      and dn.id_determina = d.id_determina
                      and svurs.utente = avsc.utente
                      and svurs.uo_progr = dn.unita_progr
                      and svurs.ottica = dn.unita_ottica
                      and impostazioni_pkg.get_impostazione ('RUOLO_SO4_NOTIFICHE', d.ente) = svurs.ruolo
                   union
                   select avsc.cognome || ' ' || avsc.nome nominativo
                     from determine_soggetti ds, as4_v_soggetti_correnti avsc, determine d
                    where d.id_determina = p_id_determina
                      and ds.id_determina = d.id_determina
                      and ds.tipo_soggetto = 'REDATTORE'
                      and ds.utente = avsc.utente
                   union
                   select avsc.cognome || ' ' || avsc.nome nominativo
                     from determine_soggetti ds, as4_v_soggetti_correnti avsc, determine d
                    where d.id_determina = p_id_determina
                      and ds.id_determina = d.id_determina
                      and ds.tipo_soggetto = 'FUNZIONARIO'
                      and ds.utente = avsc.utente
                   union
                   select avsc.cognome || ' ' || avsc.nome nominativo
                     from determine_soggetti ds, as4_v_soggetti_correnti avsc, determine d
                    where d.id_determina = p_id_determina
                      and ds.id_determina = d.id_determina
                      and ds.tipo_soggetto = 'DIRIGENTE'
                      and ds.utente = avsc.utente
                   union
                   select avsc.cognome || ' ' || avsc.nome nominativo
                     from determine_soggetti ds
                        ,  so4_v_utenti_ruoli_sogg_uo svurs
                        ,  as4_v_soggetti_correnti avsc
                        ,  determine d
                    where d.id_determina = p_id_determina
                      and ds.id_determina = d.id_determina
                      and ds.tipo_soggetto = 'UO_PROPONENTE'
                      and svurs.utente = avsc.utente
                      and svurs.uo_progr = ds.unita_progr
                      and svurs.ottica = ds.unita_ottica
                      and impostazioni_pkg.get_impostazione ('RUOLO_SO4_NOTIFICHE', d.ente) = svurs.ruolo
                   order by 1)
         loop
            d_elenco := d_elenco || c.nominativo || '; ';
         end loop;
      exception
         when others then
            d_elenco := '';
      end;

      return d_elenco;
   end get_sogg_notifica_determina;

   function get_suddivisione_descrizione (p_unita_progr     number
                                        ,  p_unita_dal       date
                                        ,  p_suddivisione    varchar2
                                        ,  p_ente            varchar2)
      return varchar2
   /***************************************************************
   Funzione che restituisce la descrizione di una suddivisione (AREA / SERVIZIO)
   a partire da un'unità su SO4
   ***************************************************************/
   is
      d_id_suddivisione      number (19);
      d_suddivisione_progr   number (19);
      d_ottica               varchar2 (255);
      d_descrizione          varchar2 (4000);
   begin
      d_ottica := impostazioni_pkg.get_impostazione ('OTTICA_SO4', p_ente);

      select id_suddivisione
        into d_id_suddivisione
        from so4_v_suddivisioni_struttura
       where codice = impostazioni_pkg.get_impostazione (p_suddivisione, p_ente)
         and ottica = d_ottica;

      d_suddivisione_progr :=
         so4_util.get_area_unita (d_id_suddivisione
                                ,  p_unita_progr
                                ,  p_unita_dal
                                ,  d_ottica);

      d_descrizione := get_uo_descrizione (d_suddivisione_progr, p_unita_dal);

      return d_descrizione;
   end get_suddivisione_descrizione;

   function get_uo_descrizione (p_unita_progr number, p_unita_dal date)
      return varchar2
   /***************************************************************
   Funzione che restituisce la descrizione di unità su SO4
   ***************************************************************/
   is
      d_descrizione   varchar2 (4000);
   begin
      begin
         d_descrizione := so4_util.anuo_get_descrizione (p_unita_progr, p_unita_dal);
      exception
         when others then
            d_descrizione := '--';
      end;

      return d_descrizione;
   end get_uo_descrizione;

   function get_uo_padre_descrizione (p_unita_progr number, p_unita_dal date, p_ottica varchar2)
      return varchar2
   /***************************************************************
   Funzione che restituisce la descrizione dell'unità padre su SO4 a partire da un'unità
   ***************************************************************/
   is
      d_descrizione   varchar2 (4000);
   begin
     
      begin
         d_descrizione := so4_util.unita_get_unita_padre (p_unita_progr, p_ottica, p_unita_dal);

         d_descrizione := substr (d_descrizione
              ,  instr (d_descrizione, '#', -1) + 1
              ,  length (d_descrizione) - instr (d_descrizione, '#', -1));

      exception
         when others then
            d_descrizione := '';
      end;

      return d_descrizione;
   end get_uo_padre_descrizione;   
end utility_pkg;
/