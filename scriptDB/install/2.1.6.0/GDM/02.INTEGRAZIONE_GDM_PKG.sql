CREATE OR REPLACE PACKAGE        integrazione_gdm_pkg
AS

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

END integrazione_gdm_pkg;
/

CREATE OR REPLACE PACKAGE BODY        integrazione_gdm_pkg
AS

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

END integrazione_gdm_pkg;
/
