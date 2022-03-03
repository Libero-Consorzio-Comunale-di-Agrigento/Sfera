--liquibase formatted sql
--changeset rdestasio:install_20200221_pkg_01

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

CREATE OR REPLACE PACKAGE BODY        competenze_pkg
AS
    
    function get_tipo_documento (p_id_documento number)
    return varchar2 is
        d_tipo_documento varchar2(255);
    begin
        select tipo_documento
        into d_tipo_documento
        from (
            select 'DETERMINA' as tipo_documento
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
            select C.TIPO_OGGETTO as tipo_documento
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
    return number is
        d_tipo_documento varchar(255);

    begin
        d_tipo_documento := get_tipo_documento(p_id_documento);

        CASE
            WHEN d_tipo_documento = 'DETERMINA'         THEN return controlla_competenze_determina (p_id_documento, p_utente, p_lettura, p_modifica, p_cancellazione, p_controlla_riservatezza, p_forza_riservato);
            WHEN d_tipo_documento = 'DELIBERA'          THEN return controlla_competenze_delibera (p_id_documento, p_utente, p_lettura, p_modifica, p_cancellazione, p_controlla_riservatezza, p_forza_riservato);
            WHEN d_tipo_documento = 'PROPOSTA_DELIBERA' THEN return controlla_competenze_prop_del (p_id_documento, p_utente, p_lettura, p_modifica, p_cancellazione, p_controlla_riservatezza, p_forza_riservato);
            WHEN d_tipo_documento = 'VISTO_PARERE'      THEN return controlla_competenze_visto_par (p_id_documento, p_utente, p_lettura, p_modifica, p_cancellazione, p_controlla_riservatezza, p_forza_riservato);
            WHEN d_tipo_documento = 'CERTIFICATO'       THEN return controlla_competenze_cert (p_id_documento, p_utente, p_lettura, p_modifica, p_cancellazione, p_controlla_riservatezza, p_forza_riservato);
            ELSE return controlla_competenze_doc (p_id_documento, p_utente, p_lettura, p_modifica, p_cancellazione, p_controlla_riservatezza, p_forza_riservato);
       END CASE;
    end;

    function controlla_competenze_determina (p_id_determina  number
                                           , p_utente        varchar2
                                           , p_lettura       number default 1
                                           , p_modifica      number default 0
                                           , p_cancellazione number default 0
                                           , p_controlla_riservatezza number default 1
                                           , p_forza_riservato number default 0)
    return number is
        d_ottica            varchar(40);
        p_ente              varchar(40);
        d_ruolo_access_app  varchar(40);
        d_ruolo_riservato   varchar(40);
        d_lettura           number(1);
        d_modifica          number(1);
        d_cancellazione     number(1);
        d_riservato         number(1);
    begin
        begin
            select ente, decode(d.riservato, 'Y', 1, 0) 
            into p_ente, d_riservato
            from determine d
            where d.id_determina = p_id_determina
              and valido = 'Y';
        exception when no_data_found then
            return 0;
        end;

        d_ruolo_access_app  := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_ACCESSO_APPLICATIVO', p_ente);
        d_ottica            := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('OTTICA_SO4',                p_ente);
        d_ruolo_riservato   := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_SO4_RISERVATO_DETE',  p_ente);
        
        -- devo controllare la riservatezza solo se il documento è riservato e il parametro in input richiede
        -- di controllare la riservatezza. 
        if ((d_riservato = 1 or p_forza_riservato = 1) and p_controlla_riservatezza = 1)
        then
            d_riservato := 1;
        else
            d_riservato := 0;
        end if;

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
                  and (d_riservato = 0 or not(dc.ruolo = d_ruolo_access_app) and exists(select 1 from so4_v_utenti_ruoli_sogg_uo r where r.ruolo = d_ruolo_riservato and r.utente = p_utente)))
              or (dc.unita_progr is not null
                  and dc.unita_progr = uo_r.uo_progr
                  and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)
                  and (d_riservato = 0 or exists(select 1 from so4_v_utenti_ruoli_sogg_uo r where r.ruolo = d_ruolo_riservato and r.utente = p_utente
                                    and r.uo_progr = dc.unita_progr and r.uo_dal = dc.unita_dal and r.ottica = dc.unita_ottica))
              ))
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
        exception when no_data_found then
            return 0;
        end;
    end;

    function controlla_competenze_delibera ( p_id_delibera   number
                                           , p_utente        varchar2
                                           , p_lettura       number default 1
                                           , p_modifica      number default 0
                                           , p_cancellazione number default 0
                                           , p_controlla_riservatezza number default 1
                                           , p_forza_riservato number default 0)
    return number is
        d_ottica            varchar(40);
        p_ente              varchar(40);
        d_ruolo_access_app  varchar(40);
        d_ruolo_riservato   varchar(40);
        d_lettura           number(1);
        d_modifica          number(1);
        d_cancellazione     number(1);
        d_riservato         number(1);
    begin
        begin
            select ente, decode(d.riservato, 'Y', 1, 0) 
            into p_ente, d_riservato
            from delibere d
            where d.id_delibera = p_id_delibera
              and valido = 'Y';
        exception when no_data_found then
            return 0;
        end;

        d_ruolo_access_app  := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_ACCESSO_APPLICATIVO', p_ente);
        d_ottica            := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('OTTICA_SO4',                p_ente);
        d_ruolo_riservato   := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_SO4_RISERVATO_DETE',  p_ente);
        
        -- devo controllare la riservatezza solo se il documento è riservato e il parametro in input richiede
        -- di controllare la riservatezza. 
        if ((d_riservato = 1 or p_forza_riservato = 1) and p_controlla_riservatezza = 1)
        then
            d_riservato := 1;
        else
            d_riservato := 0;
        end if;

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
                  and (d_riservato = 0 or not(dc.ruolo = d_ruolo_access_app) and exists(select 1 from so4_v_utenti_ruoli_sogg_uo r where r.ruolo = d_ruolo_riservato and r.utente = p_utente)))
              or (dc.unita_progr is not null
                  and dc.unita_progr = uo_r.uo_progr
                  and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)
                  and (d_riservato = 0 or exists(select 1 from so4_v_utenti_ruoli_sogg_uo r where r.ruolo = d_ruolo_riservato and r.utente = p_utente
                                    and r.uo_progr = dc.unita_progr and r.uo_dal = dc.unita_dal and r.ottica = dc.unita_ottica))
              ))
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
        exception when no_data_found then
            return 0;
        end;
    end;

    function controlla_competenze_prop_del  (p_id_proposta_delibera   number
                                           , p_utente        varchar2
                                           , p_lettura       number default 1
                                           , p_modifica      number default 0
                                           , p_cancellazione number default 0
                                           , p_controlla_riservatezza number default 1
                                           , p_forza_riservato number default 0)
    return number is
        d_ottica            varchar(40);
        p_ente              varchar(40);
        d_ruolo_access_app  varchar(40);
        d_ruolo_riservato   varchar(40);
        d_lettura           number(1);
        d_modifica          number(1);
        d_cancellazione     number(1);
        d_riservato         number(1);
    begin
        begin
            select ente, decode(d.riservato, 'Y', 1, 0) 
            into p_ente, d_riservato
            from proposte_delibera d
            where d.id_proposta_delibera = p_id_proposta_delibera
              and valido = 'Y';
        exception when no_data_found then
            return 0;
        end;

        d_ruolo_access_app  := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_ACCESSO_APPLICATIVO', p_ente);
        d_ottica            := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('OTTICA_SO4',                p_ente);
        d_ruolo_riservato   := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_SO4_RISERVATO_DETE',  p_ente);
        
        -- devo controllare la riservatezza solo se il documento è riservato e il parametro in input richiede
        -- di controllare la riservatezza oppure se è richiesto di forzare il controllo.
        if ((d_riservato = 1 or p_forza_riservato = 1) and p_controlla_riservatezza = 1)
        then
            d_riservato := 1;
        else
            d_riservato := 0;
        end if;

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
                  and (d_riservato = 0 or not(dc.ruolo = d_ruolo_access_app) and exists(select 1 from so4_v_utenti_ruoli_sogg_uo r where r.ruolo = d_ruolo_riservato and r.utente = p_utente)))
              or (dc.unita_progr is not null
                  and dc.unita_progr = uo_r.uo_progr
                  and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)
                  and (d_riservato = 0 or exists(select 1 from so4_v_utenti_ruoli_sogg_uo r where r.ruolo = d_ruolo_riservato and r.utente = p_utente
                                    and r.uo_progr = dc.unita_progr and r.uo_dal = dc.unita_dal and r.ottica = dc.unita_ottica))
              ))
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

           return 0;
        exception when no_data_found then
            return 0;
        end;
    end;

    function controlla_competenze_visto_par (p_id_visto_parere number
                                           , p_utente          varchar2
                                           , p_lettura         number default 1
                                           , p_modifica        number default 0
                                           , p_cancellazione   number default 0
                                           , p_controlla_riservatezza number default 1
                                           , p_forza_riservato number default 0)
    return number is
        d_ottica            varchar(40);
        p_ente              varchar(40);
        d_ruolo_access_app  varchar(40);
        d_ruolo_riservato   varchar(40);
        d_lettura           number(1);
        d_modifica          number(1);
        d_cancellazione     number(1);
        d_riservato         number(1);
    begin
    
        /*
         *  NOTA: per il visto/parere non si verifica la riservatezza.
         */
    
        begin
            select ente 
            into p_ente
            from visti_pareri d
            where d.id_visto_parere = p_id_visto_parere
              and valido = 'Y';
        exception when no_data_found then
            return 0;
        end;

        d_ruolo_access_app  := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_ACCESSO_APPLICATIVO', p_ente);
        d_ottica            := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('OTTICA_SO4',                p_ente);
        d_ruolo_riservato   := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_SO4_RISERVATO_DETE',  p_ente);
        
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
        exception when no_data_found then
            return 0;
        end;
    end;

    function controlla_competenze_cert ( p_id_certificato       number
                                       , p_utente        varchar2
                                       , p_lettura       number default 1
                                       , p_modifica      number default 0
                                       , p_cancellazione number default 0
                                       , p_controlla_riservatezza number default 1
                                           , p_forza_riservato number default 0)
    return number is
        d_ottica            varchar(40);
        p_ente              varchar(40);
        d_ruolo_access_app  varchar(40);
        d_ruolo_riservato   varchar(40);
        d_lettura           number(1);
        d_modifica          number(1);
        d_cancellazione     number(1);
        d_riservato         number(1);
    begin
    
        /*
         *  NOTA: per il visto/parere non si verifica la riservatezza.
         */
    
        begin
            select ente 
            into p_ente
            from certificati d
            where d.id_certificato = p_id_certificato
              and valido = 'Y';
        exception when no_data_found then
            return 0;
        end;

        d_ruolo_access_app  := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_ACCESSO_APPLICATIVO', p_ente);
        d_ottica            := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('OTTICA_SO4',                p_ente);
        d_ruolo_riservato   := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_SO4_RISERVATO_DETE',  p_ente);
        
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
        exception when no_data_found then
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
    return number is
        d_ottica            varchar(40);
        p_ente              varchar(40);
        d_ruolo_access_app  varchar(40);
        d_ruolo_riservato   varchar(40);
        d_lettura           number(1);
        d_modifica          number(1);
        d_cancellazione     number(1);
        d_riservato         number(1);
    begin
        begin
            select ente, decode(d.riservato, 'Y', 1, 0) 
            into p_ente, d_riservato
            from gdo_documenti d
            where d.id_documento = p_id_documento
              and valido = 'Y';
        exception when no_data_found then
            return 0;
        end;

        d_ruolo_access_app  := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_ACCESSO_APPLICATIVO', p_ente);
        d_ottica            := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('OTTICA_SO4',                p_ente);
        d_ruolo_riservato   := IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('RUOLO_SO4_RISERVATO_DETE',  p_ente);
        
        -- devo controllare la riservatezza solo se il documento è riservato e il parametro in input richiede
        -- di controllare la riservatezza oppure se è richiesto di forzare il controllo.
        if ((d_riservato = 1 or p_forza_riservato = 1) and p_controlla_riservatezza = 1)
        then
            d_riservato := 1;
        else
            d_riservato := 0;
        end if;

        begin
            select decode(max(dc.lettura), 'Y', 1, 0), decode(max(dc.modifica), 'Y', 1, 0), decode(max(dc.cancellazione), 'Y', 1, 0)
            into d_lettura, d_modifica, d_cancellazione
            from so4_v_utenti_ruoli_sogg_uo uo_r
               , gdo_documenti_competenze dc
           where uo_r.ottica = d_ottica
             and uo_r.utente = p_utente
             and (dc.utente  = p_utente
              or (dc.unita_progr is null
                  and dc.ruolo is not null
                  and dc.ruolo = uo_r.ruolo
                  and (d_riservato = 0 or not(dc.ruolo = d_ruolo_access_app) and exists(select 1 from so4_v_utenti_ruoli_sogg_uo r where r.ruolo = d_ruolo_riservato and r.utente = p_utente)))
              or (dc.unita_progr is not null
                  and dc.unita_progr = uo_r.uo_progr
                  and (dc.ruolo is null or dc.ruolo = uo_r.ruolo)
                  and (d_riservato = 0 or exists(select 1 from so4_v_utenti_ruoli_sogg_uo r where r.ruolo = d_ruolo_riservato and r.utente = p_utente
                                    and r.uo_progr = dc.unita_progr and r.uo_dal = dc.unita_dal and r.ottica = dc.unita_ottica))
              ))
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
        exception when no_data_found then
            return 0;
        end;
    end;

END competenze_pkg;
/
