--liquibase formatted sql
--changeset mferrara:2.6_20260202_01.utility_pkg runOnChange:true

CREATE OR REPLACE package utility_pkg is
   type split_tbl is table of varchar2 (32767);


   function get_codice_unita (p_unita_progr number, p_unita_dal date)
      return varchar2;

   function get_codice_unita_prop_delibera (p_id_proposta_delibera number)
      return varchar2;

   function get_codice_unita_prop_dete (p_id_determina number)
      return varchar2;

   function unita_get_radice_determina (p_id_determina number)
      return varchar2;

   function unita_get_radice_prop_delibera (p_id_proposta_delibera number)
      return varchar2;

   function get_cognome (p_ni number)
      return varchar2;

   function get_codice_fiscale (p_ni number)
      return varchar2;

   function get_cognome_nome (p_ni number, p_ente varchar2 default null)
      return varchar2;

   function get_nominativo_soggetto (p_ni number, p_ente varchar2)
      return varchar2;

   function get_nominativo_utente_ad4 (p_utente varchar2, p_ente varchar2)
      return varchar2;

   function get_elenco_firmatari_documento (p_id_documento number, p_ente varchar2, p_tipo_documento varchar2)
      return varchar2;

   function get_elenco_relatori_prop_deli (p_id_proposta_delibera number, p_id_delega number, p_id_oggetto_seduta number)
      return varchar2;

   function get_elenco_delegati (p_id_proposta_delibera number, p_id_oggetto_seduta number, p_ente varchar2 default null)
      return varchar2;

   function get_data_firma_delibera (p_id_delibera number, p_tipo_soggetto varchar2)
      return date;

   function get_data_firma_par_contabile (p_id_delibera number)
      return date;

   function get_data_firma_visto_contabile (p_id_determina number)
      return date;

   function get_desc_ascendenti_unita (p_codice_uo     varchar2,
                                       p_data          date default null,
                                       p_ottica        varchar2,
                                       p_separatore    varchar2 default null)
      return varchar2;

   function get_esito_parere_contabile (p_id_delibera number)
      return varchar2;

   function get_esito_visto_contabile (p_id_determina number)
      return varchar2;

   function get_firmatario_delibera (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2;

   function get_firmatario_deli_cognome (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2;

   function get_firmatario_deli_nome (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2;

   function get_firmatario_deli_cf (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2;

   function get_firmatario_par_contabile (p_id_delibera number)
      return varchar2;

   function get_firmatario_visto_contabile (p_id_determina number)
      return varchar2;

   function get_firmatario_visto_contab_c (p_id_determina number)
      return varchar2;

   function get_firmatario_visto_contab_n (p_id_determina number)
      return varchar2;

   function get_firmatario_visto_contab_cf (p_id_determina number)
      return varchar2;

   function get_firmatari_allegato(p_id_file_allegato number)
      return varchar2;

   function get_ni_soggetto (p_utente in varchar2)
      return number;

   function get_nome (p_ni number)
      return varchar2;

   function get_nominativo_sogg_deli (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2;

   function get_nominativo_sogg_deli_c (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2;

   function get_nominativo_sogg_deli_n (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2;

   function get_nominativo_sogg_dete (p_id_determina number, p_tipo_soggetto varchar2)
      return varchar2;

   function get_nominativo_sogg_prop_deli (p_id_proposta_delibera number, p_tipo_soggetto varchar2)
      return varchar2;

   function get_presenti_oggetto_seduta (p_id_oggetto_seduta number, p_presente varchar2, p_ruolo varchar2)
      return varchar2;

   function get_presenti_seduta (p_id_seduta number, p_presente varchar2, p_ruolo varchar2)
      return varchar2;

   function get_prima_data_firma_certif (p_id_certificato number)
      return date;

   function get_prima_data_firma_delibera (p_id_delibera number)
      return date;

   function get_prima_data_firma_determina (p_id_determina number)
      return date;

   function get_prima_data_firma_prop_deli (p_id_proposta_delibera number)
      return date;

   function get_prima_data_firma_visto_par (p_id_visto_parere number)
      return date;

   function get_primo_firmatario_certif (p_id_certificato number)
      return varchar2;

   function get_soggetto_delibera (p_id_delibera number, p_tipo_soggetto varchar2, p_sequenza number default 0)
      return number;

   function get_primo_firmatario_delibera (p_id_delibera number)
      return varchar2;

   function get_primo_firmatario_determina (p_id_determina number)
      return varchar2;

   function get_primo_firmatario_det_c (p_id_determina number)
      return varchar2;

   function get_primo_firmatario_det_n (p_id_determina number)
      return varchar2;

   function get_primo_firmatario_det_cf (p_id_determina number)
      return varchar2;

   function get_primo_firmatario_prop_deli (p_id_proposta_delibera number)
      return varchar2;

   function get_primo_firmatario_visto_par (p_id_visto_parere number)
      return varchar2;

   function get_sogg_notifica_delibera (p_id_delibera number)
      return varchar2;

   function get_sogg_notifica_determina (p_id_determina number)
      return varchar2;

   function get_suddivisione_descrizione (p_unita_progr     number,
                                          p_unita_dal       date,
                                          p_suddivisione    varchar2,
                                          p_ente            varchar2)
      return varchar2;

   function get_suddivisione_determina (p_id_determina number, p_suddivisione varchar2, p_ente varchar2)
      return varchar2;

   function get_suddivisione_prop_delibera (p_id_proposta_delibera number, p_suddivisione varchar2, p_ente varchar2)
      return varchar2;

   function get_suddivisione_uo_determina (p_id_determina number)
      return varchar2;

   function get_suddivisione_uo_prop_deli (p_id_proposta_delibera number)
      return varchar2;

   function get_suddivisione_vistoparere (p_id_visto_parere number)
      return varchar2;

   function get_testo_proposta_delibera (p_id_proposta_delibera number)
      return blob;

   function get_testo_originale_determina (p_id_determina number)
      return blob;

   function get_unita_prop_determina (p_id_determina number)
      return varchar2;

   function get_unita_parere_contabile (p_id_delibera number)
      return varchar2;

   function get_unita_prop_delibera (p_id_proposta_delibera number)
      return varchar2;

   function get_unita_visto_contabile (p_id_determina number)
      return varchar2;

   function get_uo_descrizione (p_unita_progr number, p_unita_dal date)
      return varchar2;

   function get_uo_padre_descrizione (p_unita_progr number, p_unita_dal date, p_ottica varchar2)
      return varchar2;

   function get_uo_padre_determina (p_id_determina number)
      return varchar2;

   function get_uo_padre_prop_delibera (p_id_proposta_delibera number)
      return varchar2;

   function get_votanti_oggetto_seduta (p_id_oggetto_seduta number, p_voto varchar2)
      return varchar2;

   function giorno_lettere (p_data in date)
      return varchar2;

   function mese_lettere (a_numero in number)
      return varchar2;

   function numero_lettere (a_numero in number)
      return varchar2;

   function join_str (p_cursor sys_refcursor, p_del varchar2 := ',')
      return varchar2;

   function join_clob (p_cursor sys_refcursor, p_del varchar2 := ',')
      return clob;

   function split_str (p_list varchar2, p_del varchar2 := ',')
      return split_tbl
      pipelined;

   function get_stato_visti (p_id_atto number)
      return varchar2;

   function get_descrizione_firmatario (p_utente varchar2, p_ente varchar2)
      return varchar2;

   function get_delegante (p_firmatario varchar2, p_utente varchar2, p_ente varchar2)
      return varchar2;

   function inserisci_tipo_delega_so4 (p_id         varchar2,
                                       p_titolo     varchar2,
                                       p_modulo     varchar2,
                                       p_istanza    varchar2)
      return varchar2;

   function aggiorna_tipo_delega_so4 (p_id         varchar2,
                                      p_titolo     varchar2,
                                      p_modulo     varchar2,
                                      p_istanza    varchar2)
      return varchar2;

   function rimuovi_tipo_delega_so4 (p_id varchar2, p_modulo varchar2, p_istanza varchar2)
      return varchar2;

   function get_determina_conclusa (p_id_determina number)
      return char;

   function get_delibera_conclusa (p_id_delibera number)
      return char;

   function get_esito_rinvio_proposta (p_id_proposta_delibera number)
      return varchar2;

   function allegatiDeterminaVisibili (p_id_determina number)
      return number;

   function allegatiDeliberaVisibili (p_id_delibera number)
      return number;

   procedure elimina_componenti_non_validi (pd_id_commissione number);

   function cons_get_codice_aoo (p_ente varchar2)
      return varchar2;

   function cons_get_descrizione_aoo (p_ente varchar2)
      return varchar2;

   function cons_get_denominazione_amm (p_ente varchar2)
      return varchar2;

   function cons_indice_allegati_determina (p_id_determina number)
      return varchar2;

   function cons_indice_allegati_delibera (p_id_delibera number, p_id_proposta_delibera number)
      return varchar2;

   function oggetto_coll_visibile_seduta (p_id_seduta number, p_id_oggetto_seduta number)
      return number;

   function elenco_allegati_determina (p_id_determina number)
      return clob;

   function elenco_allegati_delibera (p_id_delibera number)
      return clob;

   function get_cig_determina (p_id_determina number, p_ente varchar2)
      return varchar2;

   function get_elenco_cig_determina (p_id_determina number, p_ente varchar2)
      return varchar2;

   function get_numero_presenza_seduta (p_id_commissione number, p_data_inizio_periodo varchar2, p_data_fine_periodo varchar2, p_ni number, p_ente varchar2)
      return number;

   function get_date_presenza_seduta (p_id_commissione number, p_data_inizio_periodo varchar2, p_data_fine_periodo varchar2, p_ni number)
      return varchar2;

   function get_importo_gettone (p_id_commissione number, p_data_inizio_periodo varchar2, p_data_fine_periodo varchar2, p_ni number, p_ente varchar2)
      return number;

   procedure rebuid_diz_contabile (p_tipo varchar2, p_ente varchar2, p_utente varchar2, dati clob);

   procedure set_servizio_proponente (p_tipo_documento in varchar2);
end utility_pkg;
/
create or replace package body utility_pkg is
  function get_codice_unita
  (
    p_unita_progr number,
    p_unita_dal   date
  ) return varchar2
   is
    d_codice_desc varchar2(255) := null;
  begin
    begin
      d_codice_desc := so4_util.anuo_get_codice_uo(p_unita_progr,
                                                   p_unita_dal);
    exception
      when no_data_found then
        d_codice_desc := null;
    end;

    return d_codice_desc;
  end get_codice_unita;

  function get_codice_unita_prop_delibera(p_id_proposta_delibera number)
    return varchar2
   is
    d_unita_progr number(19);
    d_unita_dal   date;
    d_codice_desc varchar2(255) := null;
  begin
    begin
      select unita_progr,
             unita_dal
        into d_unita_progr,
             d_unita_dal
        from proposte_delibera_soggetti
       where id_proposta_delibera = p_id_proposta_delibera
         and tipo_soggetto = 'UO_PROPONENTE';

      d_codice_desc := so4_util.anuo_get_codice_uo(d_unita_progr,
                                                   d_unita_dal);
    exception
      when no_data_found then
        d_codice_desc := null;
    end;

    return d_codice_desc;
  end get_codice_unita_prop_delibera;

  function get_codice_unita_prop_dete(p_id_determina number) return varchar2
   is
    d_unita_progr number(19);
    d_unita_dal   date;
    d_codice_desc varchar2(255) := null;
  begin
    begin
      select unita_progr,
             unita_dal
        into d_unita_progr,
             d_unita_dal
        from determine_soggetti
       where id_determina = p_id_determina
         and tipo_soggetto = 'UO_PROPONENTE';

      d_codice_desc := so4_util.anuo_get_codice_uo(d_unita_progr,
                                                   d_unita_dal);
    exception
      when no_data_found then
        d_codice_desc := null;
    end;

    return d_codice_desc;
  end get_codice_unita_prop_dete;

  function unita_get_radice_determina(p_id_determina number) return varchar2
   is
    d_unita_progr  number(19);
    d_unita_dal    date;
    d_unita_ottica varchar2(255);
    d_codice_desc  varchar2(255) := null;
  begin
    begin
      select unita_progr,
             unita_dal,
             unita_ottica
        into d_unita_progr,
             d_unita_dal,
             d_unita_ottica
        from determine_soggetti
       where id_determina = p_id_determina
         and tipo_soggetto = 'UO_PROPONENTE';

      begin
        d_codice_desc := substr(so4_util.unita_get_radice(d_unita_progr,
                                                          d_unita_ottica,
                                                          sysdate),
                                instr(so4_util.unita_get_radice(d_unita_progr,
                                                                d_unita_ottica,
                                                                sysdate),
                                      '#',
                                      -1) + 1);
      exception
        when others then
          d_codice_desc := null;
      end;
    exception
      when no_data_found then
        d_codice_desc := null;
    end;

    return d_codice_desc;
  end unita_get_radice_determina;

  function unita_get_radice_prop_delibera(p_id_proposta_delibera number)
    return varchar2
   is
    d_unita_progr  number(19);
    d_unita_dal    date;
    d_unita_ottica varchar2(255);
    d_codice_desc  varchar2(255) := null;
  begin
    begin
      select unita_progr,
             unita_dal,
             unita_ottica
        into d_unita_progr,
             d_unita_dal,
             d_unita_ottica
        from proposte_delibera_soggetti
       where id_proposta_delibera = p_id_proposta_delibera
         and tipo_soggetto = 'UO_PROPONENTE';

      begin
        d_codice_desc := substr(so4_util.unita_get_radice(d_unita_progr,
                                                          d_unita_ottica,
                                                          sysdate),
                                instr(so4_util.unita_get_radice(d_unita_progr,
                                                                d_unita_ottica,
                                                                sysdate),
                                      '#',
                                      -1) + 1);
      exception
        when others then
          d_codice_desc := null;
      end;
    exception
      when no_data_found then
        d_codice_desc := null;
    end;

    return d_codice_desc;
  end unita_get_radice_prop_delibera;

  function get_cognome(p_ni number) return varchar2
   is
    d_cognome_nome varchar2(500);
  begin
    if (p_ni is null) then
      return '';
    end if;

    begin
      select cognome
        into d_cognome_nome
        from as4_v_soggetti_correnti
       where ni = p_ni
         and rownum = 1;

      return d_cognome_nome;
    exception
      when no_data_found then
        return '';
    end;
  end get_cognome;

  function get_codice_fiscale(p_ni number) return varchar2
   is
    d_codice_fiscale varchar2(500);
  begin
    if (p_ni is null) then
      return '';
    end if;

    begin
      select codice_fiscale
        into d_codice_fiscale
        from as4_v_soggetti_correnti
       where ni = p_ni
         and rownum = 1;

      return d_codice_fiscale;
    exception
      when no_data_found then
        return '';
    end;
  end get_codice_fiscale;

  function get_cognome_nome
  (
    p_ni   number,
    p_ente varchar2 default null
  ) return varchar2
   is
    d_cognome_nome varchar2(500);
    d_impostazione varchar2(255);
  begin
    if (p_ni is null) then
      return '';
    end if;

    if (p_ente is null) then
      d_impostazione := 'COGNOME_NOME';
    else
      d_impostazione := impostazioni_pkg.get_impostazione('SOGGETTI_FORMATO',
                                                          p_ente);
    end if;

    begin
      if (d_impostazione = 'COGNOME_NOME') then
        select cognome || ' ' || nome
          into d_cognome_nome
          from as4_v_soggetti_correnti
         where ni = p_ni
           and rownum = 1;
      else
        select nome || ' ' || cognome
          into d_cognome_nome
          from as4_v_soggetti_correnti
         where ni = p_ni
           and rownum = 1;
      end if;

      return d_cognome_nome;
    exception
      when no_data_found then
        return '';
    end;
  end get_cognome_nome;

  function get_nominativo_soggetto
  (
    p_ni   number,
    p_ente varchar2
  ) return varchar2
   is
    d_nominativo   varchar2(500);
    d_impostazione varchar2(500);
  begin
    if (p_ni is null) then
      return '';
    end if;

    begin
      d_impostazione := impostazioni_pkg.get_impostazione('SOGGETTI_FORMATO',
                                                          p_ente);

      if (d_impostazione = 'COGNOME_NOME') then
        select cognome || ' ' || nome
          into d_nominativo
          from as4_v_soggetti_correnti
         where ni = p_ni
           and rownum = 1;
      else
        select nome || ' ' || cognome
          into d_nominativo
          from as4_v_soggetti_correnti
         where ni = p_ni
           and rownum = 1;
      end if;

      return d_nominativo;
    exception
      when no_data_found then
        return '';
    end;
  end get_nominativo_soggetto;

  function get_nominativo_utente_ad4
  (
    p_utente varchar2,
    p_ente   varchar2
  ) return   varchar2
   is
    d_nominativo   varchar2(500);
    d_impostazione varchar2(500);
  begin
    if (p_utente is null) then
      return '';
    end if;

    begin
      d_impostazione := impostazioni_pkg.get_impostazione('SOGGETTI_FORMATO', p_ente);

      if (d_impostazione = 'COGNOME_NOME') then
        select cognome || ' ' || nome
          into d_nominativo
          from as4_v_soggetti_correnti
         where utente = p_utente
           and rownum = 1;
      else
        select nome || ' ' || cognome
          into d_nominativo
          from as4_v_soggetti_correnti
         where utente = p_utente
           and rownum = 1;
      end if;

      return d_nominativo;
    exception
      when no_data_found then
        return '';
    end;
  end get_nominativo_utente_ad4;

  function get_elenco_firmatari_documento
  (
    p_id_documento   number,
    p_ente           varchar2,
    p_tipo_documento varchar2
  ) return varchar2
   is
    d_firmatari    varchar2(4000) := null;
    d_impostazione varchar2(255);
  begin
    d_impostazione := impostazioni_pkg.get_impostazione('EXCEL_ELENCO_FIRMATARI',
                                                        p_ente);

    if (d_impostazione = 'Y') then
      begin
        case p_tipo_documento
          when 'DETERMINA' then
            for c in (select as4_v_soggetti_correnti.cognome || ' ' ||
                             as4_v_soggetti_correnti.nome nominativo
                        from firmatari f,
                             as4_v_soggetti_correnti
                       where f.id_determina = p_id_documento
                         and f.utente_firmatario_effettivo =
                             as4_v_soggetti_correnti.utente
                         and f.firmato = 'Y'
                         and f.data_firma is not null
                       order by sequenza asc) loop
              if (d_firmatari is null) then
                d_firmatari := c.nominativo;
              else
                d_firmatari := d_firmatari || ', ' || c.nominativo;
              end if;
            end loop;
          when 'PROPOSTA_DELIBERA' then
            for c in (select as4_v_soggetti_correnti.cognome || ' ' ||
                             as4_v_soggetti_correnti.nome nominativo
                        from firmatari f,
                             as4_v_soggetti_correnti
                       where f.id_proposta_delibera = p_id_documento
                         and f.utente_firmatario_effettivo =
                             as4_v_soggetti_correnti.utente
                         and f.firmato = 'Y'
                         and f.data_firma is not null
                       order by sequenza asc) loop
              if (d_firmatari is null) then
                d_firmatari := c.nominativo;
              else
                d_firmatari := d_firmatari || ', ' || c.nominativo;
              end if;
            end loop;
          when 'DELIBERA' then
            for c in (select as4_v_soggetti_correnti.cognome || ' ' ||
                             as4_v_soggetti_correnti.nome nominativo
                        from firmatari f,
                             as4_v_soggetti_correnti
                       where f.id_delibera = p_id_documento
                         and f.utente_firmatario_effettivo =
                             as4_v_soggetti_correnti.utente
                         and f.firmato = 'Y'
                         and f.data_firma is not null
                       order by sequenza asc) loop
              if (d_firmatari is null) then
                d_firmatari := c.nominativo;
              else
                d_firmatari := d_firmatari || ', ' || c.nominativo;
              end if;
            end loop;
          when 'CERTIFICATO' then
            for c in (select as4_v_soggetti_correnti.cognome || ' ' ||
                             as4_v_soggetti_correnti.nome nominativo
                        from firmatari f,
                             as4_v_soggetti_correnti
                       where f.id_certificato = p_id_documento
                         and f.utente_firmatario_effettivo =
                             as4_v_soggetti_correnti.utente
                         and f.firmato = 'Y'
                         and f.data_firma is not null
                       order by sequenza asc) loop
              if (d_firmatari is null) then
                d_firmatari := c.nominativo;
              else
                d_firmatari := d_firmatari || ', ' || c.nominativo;
              end if;
            end loop;
          when 'VISTO_PARERE' then
            for c in (select as4_v_soggetti_correnti.cognome || ' ' ||
                             as4_v_soggetti_correnti.nome nominativo
                        from firmatari f,
                             as4_v_soggetti_correnti
                       where f.id_visto_parere = p_id_documento
                         and f.utente_firmatario_effettivo =
                             as4_v_soggetti_correnti.utente
                         and f.firmato = 'Y'
                         and f.data_firma is not null
                       order by sequenza asc) loop
              if (d_firmatari is null) then
                d_firmatari := c.nominativo;
              else
                d_firmatari := d_firmatari || ', ' || c.nominativo;
              end if;
            end loop;
        end case;
      exception
        when others then
          d_firmatari := null;
      end;
    end if;
    return d_firmatari;
  end get_elenco_firmatari_documento;

  -- elenco degli ni_assessore di tutte le deleghe collegate alla proposta di delibera (delega principale/seduta + deleghe aggiuntive su proposta ed eventuale oggetto seduta), separati da '#'
  function get_elenco_relatori_prop_deli
  (
    p_id_proposta_delibera number,
    p_id_delega            number,
    p_id_oggetto_seduta    number
  ) return varchar2
   is
    d_relatori varchar2(4000) := null;
  begin
    begin
      for c in (select rel.ni_assessore ni_assessore
                  from deleghe rel
                 where rel.id_delega = p_id_delega
                   and rel.valido = 'Y'
                   and p_id_oggetto_seduta is null
                union
                select rel.ni_assessore
                  from deleghe rel, odg_oggetti_seduta oos
                 where rel.id_delega = oos.id_delega
                   and rel.valido = 'Y'
                   and p_id_oggetto_seduta = oos.id_oggetto_seduta
                union
                select del.ni_assessore
                  from proposte_delibera_deleghe pdd, deleghe del
                 where pdd.id_proposta_delibera = p_id_proposta_delibera
                   and pdd.id_delega = del.id_delega
                   and del.valido = 'Y'
                union
                select del.ni_assessore
                  from odg_oggetti_seduta_deleghe osd, deleghe del
                 where osd.id_oggetto_seduta = p_id_oggetto_seduta
                   and osd.id_delega = del.id_delega
                   and del.valido = 'Y') loop
        if (d_relatori is null) then
          d_relatori := to_char(c.ni_assessore);
        else
          d_relatori := d_relatori || '#' || to_char(c.ni_assessore);
        end if;
      end loop;
    exception
      when others then
        d_relatori := null;
    end;
    return d_relatori;
  end get_elenco_relatori_prop_deli;

  -- elenco dei nominativi (cognome nome) dei delegati, separati da ', '.
  -- se l'oggetto seduta ha delegati (delega principale e/o aggiuntive) vengono restituiti solo quelli,
  -- altrimenti (oggetto seduta assente o senza delegati) vengono restituiti quelli della proposta di delibera
  function get_elenco_delegati
  (
    p_id_proposta_delibera number,
    p_id_oggetto_seduta    number,
    p_ente                 varchar2 default null
  ) return varchar2
   is
    d_delegati   varchar2(4000) := null;
    d_nominativo varchar2(4000) := null;
  begin
    begin
      if (p_id_oggetto_seduta is not null) then
        for c in (select del.ni_assessore
                    from odg_oggetti_seduta oos, deleghe del
                   where oos.id_oggetto_seduta = p_id_oggetto_seduta
                     and del.id_delega = oos.id_delega
                     and del.valido = 'Y'
                  union
                  select del.ni_assessore
                    from odg_oggetti_seduta_deleghe osd, deleghe del
                   where osd.id_oggetto_seduta = p_id_oggetto_seduta
                     and osd.id_delega = del.id_delega
                     and del.valido = 'Y')
         loop
             d_nominativo := get_cognome_nome(c.ni_assessore, p_ente);
             if (d_nominativo is not null) then
                if (d_delegati is null) then
                   d_delegati := d_nominativo;
                else
                   d_delegati := d_delegati || ', ' || d_nominativo;
                end if;
              end if;
        end loop;
      else
        --p_id_oggetto_seduta is null
        for c in (select del.ni_assessore
                    from proposte_delibera pd, deleghe del
                   where pd.id_proposta_delibera = p_id_proposta_delibera
                     and del.id_delega = pd.id_delega
                     and del.valido = 'Y'
                  union
                  select del.ni_assessore
                    from proposte_delibera_deleghe pdd, deleghe del
                   where pdd.id_proposta_delibera = p_id_proposta_delibera
                     and pdd.id_delega = del.id_delega
                     and del.valido = 'Y')
          loop
             d_nominativo := get_cognome_nome(c.ni_assessore, p_ente);
             if (d_nominativo is not null) then
                if (d_delegati is null) then
                   d_delegati := d_nominativo;
                else
                   d_delegati := d_delegati || ', ' || d_nominativo;
                end if;
             end if;
        end loop;
      end if;
    exception when others then
        d_delegati := null;
    end;
    return d_delegati;
  end get_elenco_delegati;

  function get_data_firma_delibera
  (
    p_id_delibera   number,
    p_tipo_soggetto varchar2
  ) return date
   is
    d_data date;
  begin
    for c in (select data_firma
                from firmatari         f,
                     delibere_soggetti ds
               where f.id_delibera = p_id_delibera
                 and ds.id_delibera = p_id_delibera
                 and f.utente_firmatario_effettivo = ds.utente
                 and ds.tipo_soggetto = p_tipo_soggetto
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by f.sequenza asc) loop
      d_data := c.data_firma;
      exit;
    end loop;

    return d_data;
  end get_data_firma_delibera;

  function get_data_firma_par_contabile(p_id_delibera number) return date
   is
    d_data date;
  begin
    for c in (select f.data_firma
                from firmatari         f,
                     visti_pareri      vp,
                     delibere          d,
                     tipi_visto_parere tvp
               where d.id_delibera = p_id_delibera
                 and vp.id_proposta_delibera = d.id_proposta_delibera
                 and vp.id_tipologia = tvp.id_tipo_visto_parere
                 and tvp.contabile = 'Y'
                 and f.id_visto_parere = vp.id_visto_parere
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_data := c.data_firma;
      exit;
    end loop;

    return d_data;
  end get_data_firma_par_contabile;

  function get_data_firma_visto_contabile(p_id_determina number) return date
   is
    d_data date;
  begin
    for c in (select f.data_firma
                from firmatari         f,
                     visti_pareri      vp,
                     tipi_visto_parere tvp
               where vp.id_determina = p_id_determina
                 and vp.id_tipologia = tvp.id_tipo_visto_parere
                 and tvp.contabile = 'Y'
                 and f.id_visto_parere = vp.id_visto_parere
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_data := c.data_firma;
      exit;
    end loop;

    return d_data;
  end get_data_firma_visto_contabile;

  function get_desc_ascendenti_unita
  (
    p_codice_uo  varchar2,
    p_data       date default null,
    p_ottica     varchar2,
    p_separatore varchar2 default null
  ) return varchar2
   is
    v_struttura sys_refcursor;

    d_ottica            varchar2(18);
    d_separatore        varchar2(1) := chr(10);
    d_desc_suddivisione varchar2(60);
    d_result            varchar2(32000);

    type v_struttura_t is record(
      progr_uo        number(8),
      codice_uo       varchar2(50),
      desc_uo         varchar2(240),
      dal             date,
      al              date,
      id_suddivisione number(8));

    v_struttura_row v_struttura_t;
  begin
    d_ottica := so4_util.set_ottica_default(p_ottica);

    if p_separatore is not null then
      d_separatore := p_separatore;
    end if;

    v_struttura := so4_util.unita_get_ascendenti_sudd(p_codice_uo,
                                                      p_data,
                                                      d_ottica);

    if v_struttura%isopen then
      loop
        fetch v_struttura
          into v_struttura_row;

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

        d_result := d_desc_suddivisione || ': ' || v_struttura_row.desc_uo ||
                    d_separatore || d_result;
      end loop;
    end if;

    return d_result;
  exception
    when others then
      return null;
  end get_desc_ascendenti_unita;

  function get_esito_parere_contabile(p_id_delibera number) return varchar2
   is
    d_esito varchar2(255);
  begin
    for c in (select vp.esito
                from visti_pareri      vp,
                     delibere          d,
                     tipi_visto_parere tvp
               where d.id_delibera = p_id_delibera
                 and vp.id_proposta_delibera = d.id_proposta_delibera
                 and vp.id_tipologia = tvp.id_tipo_visto_parere
                 and tvp.contabile = 'Y') loop
      d_esito := c.esito;
      exit;
    end loop;

    return d_esito;
  end get_esito_parere_contabile;

  function get_esito_visto_contabile(p_id_determina number) return varchar2
   is
    d_esito varchar2(255);
  begin
    for c in (select vp.esito
                from visti_pareri      vp,
                     tipi_visto_parere tvp
               where vp.id_determina = p_id_determina
                 and vp.id_tipologia = tvp.id_tipo_visto_parere
                 and tvp.contabile = 'Y') loop
      d_esito := c.esito;
      exit;
    end loop;

    return d_esito;
  end get_esito_visto_contabile;

  function get_firmatario_delibera
  (
    p_id_delibera   number,
    p_tipo_soggetto varchar2
  ) return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.cognome || ' ' ||
                     as4_v_soggetti_correnti.nome nominativo
                from firmatari               f,
                     as4_v_soggetti_correnti,
                     delibere_soggetti       ds
               where f.id_delibera = p_id_delibera
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and ds.id_delibera = p_id_delibera
                 and f.utente_firmatario_effettivo = ds.utente
                 and ds.tipo_soggetto = p_tipo_soggetto
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by f.sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    return d_firmatario;
  end get_firmatario_delibera;

  function get_firmatario_deli_cognome
  (
    p_id_delibera   number,
    p_tipo_soggetto varchar2
  ) return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.cognome nominativo
                from firmatari               f,
                     as4_v_soggetti_correnti,
                     delibere_soggetti       ds
               where f.id_delibera = p_id_delibera
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and ds.id_delibera = p_id_delibera
                 and f.utente_firmatario_effettivo = ds.utente
                 and ds.tipo_soggetto = p_tipo_soggetto
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by f.sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    return d_firmatario;
  end get_firmatario_deli_cognome;

  function get_firmatario_deli_cf
  (
    p_id_delibera   number,
    p_tipo_soggetto varchar2
  ) return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.codice_fiscale nominativo
                from firmatari               f,
                     as4_v_soggetti_correnti,
                     delibere_soggetti       ds
               where f.id_delibera = p_id_delibera
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and ds.id_delibera = p_id_delibera
                 and f.utente_firmatario_effettivo = ds.utente
                 and ds.tipo_soggetto = p_tipo_soggetto
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by f.sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    return d_firmatario;
  end get_firmatario_deli_cf;

  function get_firmatario_deli_nome
  (
    p_id_delibera   number,
    p_tipo_soggetto varchar2
  ) return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.nome nominativo
                from firmatari               f,
                     as4_v_soggetti_correnti,
                     delibere_soggetti       ds
               where f.id_delibera = p_id_delibera
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and ds.id_delibera = p_id_delibera
                 and f.utente_firmatario_effettivo = ds.utente
                 and ds.tipo_soggetto = p_tipo_soggetto
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by f.sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    return d_firmatario;
  end get_firmatario_deli_nome;

  function get_firmatario_par_contabile(p_id_delibera number) return varchar2 is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.cognome || ' ' ||
                     as4_v_soggetti_correnti.nome nominativo
                from firmatari               f,
                     as4_v_soggetti_correnti,
                     visti_pareri            vp,
                     delibere                d,
                     tipi_visto_parere       tvp
               where d.id_delibera = p_id_delibera
                 and vp.id_proposta_delibera = d.id_proposta_delibera
                 and vp.id_tipologia = tvp.id_tipo_visto_parere
                 and tvp.contabile = 'Y'
                 and f.id_visto_parere = vp.id_visto_parere
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    return d_firmatario;
  end get_firmatario_par_contabile;

  function get_firmatario_visto_contabile(p_id_determina number)
    return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.cognome || ' ' ||
                     as4_v_soggetti_correnti.nome nominativo
                from firmatari               f,
                     as4_v_soggetti_correnti,
                     visti_pareri            vp,
                     tipi_visto_parere       tvp
               where vp.id_determina = p_id_determina
                 and vp.id_tipologia = tvp.id_tipo_visto_parere
                 and tvp.contabile = 'Y'
                 and f.id_visto_parere = vp.id_visto_parere
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    return d_firmatario;
  end get_firmatario_visto_contabile;

  function get_firmatario_visto_contab_c(p_id_determina number)
    return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.cognome nominativo
                from firmatari               f,
                     as4_v_soggetti_correnti,
                     visti_pareri            vp,
                     tipi_visto_parere       tvp
               where vp.id_determina = p_id_determina
                 and vp.id_tipologia = tvp.id_tipo_visto_parere
                 and tvp.contabile = 'Y'
                 and f.id_visto_parere = vp.id_visto_parere
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    return d_firmatario;
  end get_firmatario_visto_contab_c;

  function get_firmatario_visto_contab_n(p_id_determina number)
    return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.nome nominativo
                from firmatari               f,
                     as4_v_soggetti_correnti,
                     visti_pareri            vp,
                     tipi_visto_parere       tvp
               where vp.id_determina = p_id_determina
                 and vp.id_tipologia = tvp.id_tipo_visto_parere
                 and tvp.contabile = 'Y'
                 and f.id_visto_parere = vp.id_visto_parere
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    return d_firmatario;
  end get_firmatario_visto_contab_n;

  function get_firmatario_visto_contab_cf(p_id_determina number)
    return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.codice_fiscale cf
                from firmatari               f,
                     as4_v_soggetti_correnti,
                     visti_pareri            vp,
                     tipi_visto_parere       tvp
               where vp.id_determina = p_id_determina
                 and vp.id_tipologia = tvp.id_tipo_visto_parere
                 and tvp.contabile = 'Y'
                 and f.id_visto_parere = vp.id_visto_parere
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_firmatario := c.cf;
      exit;
    end loop;

    return d_firmatario;
  end get_firmatario_visto_contab_cf;

  function get_firmatari_allegato(p_id_file_allegato number) return varchar2
   is
    d_firmatari varchar2(4000);
  begin
    for c in (select distinct ff.nominativo
                from file_firmati ff
               where ff.stato = 'VERIFICATO'
                 and ff.id_file_allegato = p_id_file_allegato
              union
              select decode(avu.nominativo_soggetto,
                            null,
                            ff.nominativo,
                            avu.cognome_soggetto || ' ' || avu.nome_soggetto)
                from ad4_v_utenti avu,
                     file_firmati ff
               where ff.nominativo = avu.nominativo(+)
                 and ff.stato != 'VERIFICATO'
                 and ff.id_file_allegato = p_id_file_allegato
               order by 1 asc) loop
      if (d_firmatari is null) then
        d_firmatari := c.nominativo;
      else
        d_firmatari := d_firmatari || ', ' || c.nominativo;
      end if;
    end loop;

    return d_firmatari;
  end get_firmatari_allegato;

  function get_ni_soggetto(p_utente in varchar2)
    return number
   is
    d_ni number;
  begin
    if (p_utente is null) then
      return null;
    end if;

    begin
      select ni
        into d_ni
        from as4_v_soggetti_correnti
       where utente = p_utente
         and rownum = 1;

      return d_ni;
    exception
      when no_data_found then
         return null;
    end;
  end get_ni_soggetto;

  function get_nome(p_ni number) return varchar2
   is
    d_cognome_nome varchar2(500);
  begin
    if (p_ni is null) then
      return '';
    end if;

    begin
      select nome
        into d_cognome_nome
        from as4_v_soggetti_correnti
       where ni = p_ni
         and rownum = 1;

      return d_cognome_nome;
    exception
      when no_data_found then
         return '';
    end;
  end get_nome;

  function get_nominativo_sogg_deli
  (
    p_id_delibera   number,
    p_tipo_soggetto varchar2
  ) return varchar2
   is
    d_utente     varchar2(255) := null;
    d_nominativo varchar2(255) := null;
    d_ente       varchar2(255) := null;
  begin
    begin
      select ds.utente,
             d.ente
        into d_utente,
             d_ente
        from delibere          d,
             delibere_soggetti ds
       where d.id_delibera = p_id_delibera
         and d.id_delibera = ds.id_delibera
         and ds.tipo_soggetto = p_tipo_soggetto
         and ds.attivo = 'Y';

      d_nominativo := get_cognome_nome(get_ni_soggetto(d_utente),
                                       d_ente);
    exception
      when no_data_found then
        d_nominativo := null;
    end;

    return d_nominativo;
  end get_nominativo_sogg_deli;

  function get_nominativo_sogg_deli_c
  (
    p_id_delibera   number,
    p_tipo_soggetto varchar2
  ) return varchar2
   is
    d_utente     varchar2(255) := null;
    d_nominativo varchar2(255) := null;
  begin
    begin
      select utente
        into d_utente
        from delibere_soggetti
       where id_delibera = p_id_delibera
         and tipo_soggetto = p_tipo_soggetto
         and attivo = 'Y';

      d_nominativo := get_cognome(get_ni_soggetto(d_utente));
    exception
      when no_data_found then
        d_nominativo := null;
    end;

    return d_nominativo;
  end get_nominativo_sogg_deli_c;

  function get_nominativo_sogg_deli_n
  (
    p_id_delibera   number,
    p_tipo_soggetto varchar2
  ) return varchar2
   is
    d_utente     varchar2(255) := null;
    d_nominativo varchar2(255) := null;
  begin
    begin
      select utente
        into d_utente
        from delibere_soggetti
       where id_delibera = p_id_delibera
         and tipo_soggetto = p_tipo_soggetto
         and attivo = 'Y';

      d_nominativo := get_nome(get_ni_soggetto(d_utente));
    exception
      when no_data_found then
        d_nominativo := null;
    end;

    return d_nominativo;
  end get_nominativo_sogg_deli_n;

  function get_nominativo_sogg_dete
  (
    p_id_determina  number,
    p_tipo_soggetto varchar2
  ) return varchar2
   is
    d_utente     varchar2(255) := null;
    d_nominativo varchar2(255) := null;
    d_ente       varchar2(255) := null;
  begin
    begin
      select ds.utente,
             d.ente
        into d_utente,
             d_ente
        from determine          d,
             determine_soggetti ds
       where d.id_determina = p_id_determina
         and d.id_determina = ds.id_determina
         and ds.tipo_soggetto = p_tipo_soggetto
         and ds.attivo = 'Y';

      d_nominativo := get_cognome_nome(get_ni_soggetto(d_utente),
                                       d_ente);
    exception
      when no_data_found then
        d_nominativo := null;
    end;

    return d_nominativo;
  end get_nominativo_sogg_dete;

  function get_nominativo_sogg_prop_deli
  (
    p_id_proposta_delibera number,
    p_tipo_soggetto        varchar2
  ) return varchar2
   is
    d_utente     varchar2(255) := null;
    d_nominativo varchar2(255) := null;
    d_ente       varchar2(255) := null;
  begin
    begin
      select pds.utente,
             pd.ente
        into d_utente,
             d_ente
        from proposte_delibera          pd,
             proposte_delibera_soggetti pds
       where pd.id_proposta_delibera = p_id_proposta_delibera
         and pd.id_proposta_delibera = pds.id_proposta_delibera
         and pds.tipo_soggetto = p_tipo_soggetto
         and pds.attivo = 'Y';

      d_nominativo := get_cognome_nome(get_ni_soggetto(d_utente),
                                       d_ente);
    exception
      when no_data_found then
        d_nominativo := null;
    end;

    return d_nominativo;
  end get_nominativo_sogg_prop_deli;

  function get_presenti_oggetto_seduta
  (
    p_id_oggetto_seduta number,
    p_presente          varchar2,
    p_ruolo             varchar2
  ) return varchar2
   is
    d_cognome         varchar2(1000);
    d_nome            varchar2(1000);
    d_elenco_presenti varchar2(32767);
  begin
    for c in (select sequenza,
                     cognome_nome
                from (select occ.sequenza,
                             initcap(get_cognome_nome(nvl(occ.ni_componente,
                                                          osp.ni_componente_esterno),
                                                      oc.ente)) cognome_nome
                        from odg_sedute_partecipanti    osp,
                             odg_oggetti_partecipanti   oop,
                             odg_oggetti_seduta         oos,
                             odg_sedute                 os,
                             odg_commissioni_componenti occ,
                             odg_commissioni            oc
                       where oos.id_oggetto_seduta = p_id_oggetto_seduta
                         and oos.id_oggetto_seduta = oop.id_oggetto_seduta
                         and osp.id_seduta_partecipante =
                             oop.id_seduta_partecipante
                         and oos.id_seduta = os.id_seduta
                         and os.id_commissione = oc.id_commissione
                         and occ.id_commissione_componente(+) =
                             osp.id_commissione_componente
                         and ((p_ruolo is null) or
                             (p_ruolo is not null and
                             oop.ruolo_partecipante = p_ruolo))
                         and ((p_presente is null) or
                             (p_presente is not null and
                             oop.presente = p_presente)))
               order by sequenza,
                        cognome_nome) loop
      d_elenco_presenti := d_elenco_presenti || c.cognome_nome || ', ';
    end loop;

    if (d_elenco_presenti is not null) then
      d_elenco_presenti := substr(d_elenco_presenti,
                                  1,
                                  length(d_elenco_presenti) - 2) || '.';
    end if;

    return d_elenco_presenti;
  end get_presenti_oggetto_seduta;

  function get_presenti_seduta
  (
    p_id_seduta number,
    p_presente  varchar2,
    p_ruolo     varchar2
  ) return varchar2
   is
    d_cognome         varchar2(1000);
    d_nome            varchar2(1000);
    d_elenco_presenti varchar2(32767);
  begin
    for c in (select sequenza,
                     cognome_nome
                from (select occ.sequenza,
                             initcap(get_cognome_nome(nvl(occ.ni_componente,
                                                          osp.ni_componente_esterno),
                                                      oc.ente)) cognome_nome
                        from odg_sedute_partecipanti    osp,
                             odg_sedute                 os,
                             odg_commissioni_componenti occ,
                             odg_commissioni            oc
                       where os.id_seduta = p_id_seduta
                         and osp.id_seduta = os.id_seduta
                         and os.id_seduta = osp.id_seduta
                         and os.id_commissione = oc.id_commissione
                         and occ.id_commissione_componente(+) =
                             osp.id_commissione_componente
                         and (p_ruolo is null or
                             osp.ruolo_partecipante = p_ruolo)
                         and (p_presente is null or osp.presente = p_presente)
                       order by sequenza,
                                cognome_nome)) loop
      d_elenco_presenti := d_elenco_presenti || c.cognome_nome || ', ';
    end loop;

    if (d_elenco_presenti is not null) then
      d_elenco_presenti := substr(d_elenco_presenti,
                                  1,
                                  length(d_elenco_presenti) - 2) || '.';
    end if;

    return d_elenco_presenti;
  end get_presenti_seduta;

  function get_prima_data_firma_certif(p_id_certificato number) return date
   is
    d_data date;
  begin
    for c in (select data_firma
                from firmatari f
               where f.id_certificato = p_id_certificato
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_data := c.data_firma;
      exit;
    end loop;

    return d_data;
  end get_prima_data_firma_certif;

  function get_prima_data_firma_delibera(p_id_delibera number) return date
   is
    d_data date;
  begin
    for c in (select data_firma
                from firmatari f
               where f.id_delibera = p_id_delibera
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_data := c.data_firma;
      exit;
    end loop;

    return d_data;
  end get_prima_data_firma_delibera;

  function get_prima_data_firma_determina(p_id_determina number) return date
   is
    d_data date;
  begin
    for c in (select data_firma
                from firmatari f
               where f.id_determina = p_id_determina
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_data := c.data_firma;
      exit;
    end loop;

    if d_data is null then
      for c in (select data_numero_determina data_firma
                  from determine d
                 where d.id_determina = p_id_determina
                   and d.valido = 'Y'
                   and d.id_engine_iter is null) loop
        d_data := c.data_firma;
        exit;
      end loop;
    end if;

    return d_data;
  end get_prima_data_firma_determina;

  function get_prima_data_firma_prop_deli(p_id_proposta_delibera number)
    return date
   is
    d_data date;
  begin
    for c in (select data_firma
                from firmatari f
               where f.id_proposta_delibera = p_id_proposta_delibera
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_data := c.data_firma;
      exit;
    end loop;

    return d_data;
  end get_prima_data_firma_prop_deli;

  function get_prima_data_firma_visto_par(p_id_visto_parere number)
    return date
   is
    d_data date;
  begin
    for c in (select data_firma
                from firmatari f
               where f.id_visto_parere = p_id_visto_parere
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_data := c.data_firma;
      exit;
    end loop;

    return d_data;
  end get_prima_data_firma_visto_par;

  function get_primo_firmatario_certif(p_id_certificato number)
    return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.cognome || ' ' ||
                     as4_v_soggetti_correnti.nome nominativo
                from firmatari f,
                     as4_v_soggetti_correnti
               where f.id_certificato = p_id_certificato
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    return d_firmatario;
  end get_primo_firmatario_certif;

  function get_soggetto_delibera
  (
    p_id_delibera   number,
    p_tipo_soggetto varchar2,
    p_sequenza      number default 0
  ) return number
   is
    d_ni_soggetto number(19);
  begin
    for c in (select get_ni_soggetto(utente) ni_soggetto
                from delibere_soggetti
               where tipo_soggetto = p_tipo_soggetto
                 and id_delibera = p_id_delibera
                 and attivo = 'Y'
                 and sequenza = p_sequenza) loop
      d_ni_soggetto := c.ni_soggetto;
      exit;
    end loop;

    return d_ni_soggetto;
  end get_soggetto_delibera;

  function get_primo_firmatario_delibera(p_id_delibera number)
    return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.cognome || ' ' ||
                     as4_v_soggetti_correnti.nome nominativo
                from firmatari f,
                     as4_v_soggetti_correnti
               where f.id_delibera = p_id_delibera
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    return d_firmatario;
  end get_primo_firmatario_delibera;

  function get_primo_firmatario_determina(p_id_determina number)
    return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.cognome || ' ' ||
                     as4_v_soggetti_correnti.nome nominativo
                from firmatari f,
                     as4_v_soggetti_correnti
               where f.id_determina = p_id_determina
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    if d_firmatario is null then
      for c in (select as4_v_soggetti_correnti.cognome || ' ' ||
                       as4_v_soggetti_correnti.nome nominativo
                  from determine               d,
                       determine_soggetti      ds,
                       as4_v_soggetti_correnti
                 where d.id_determina = p_id_determina
                   and d.valido = 'Y'
                   and d.id_engine_iter is null
                   and d.numero_determina is not null
                   and d.id_determina = ds.id_determina
                   and ds.tipo_soggetto = 'DIRIGENTE'
                   and ds.sequenza = 0
                   and ds.utente = as4_v_soggetti_correnti.utente) loop
        d_firmatario := c.nominativo;
        exit;
      end loop;
    end if;

    return d_firmatario;
  end get_primo_firmatario_determina;

  function get_primo_firmatario_det_c(p_id_determina number) return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.cognome nominativo
                from firmatari f,
                     as4_v_soggetti_correnti
               where f.id_determina = p_id_determina
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    if d_firmatario is null then
      for c in (select as4_v_soggetti_correnti.cognome nominativo
                  from determine               d,
                       determine_soggetti      ds,
                       as4_v_soggetti_correnti
                 where d.id_determina = p_id_determina
                   and d.valido = 'Y'
                   and d.id_engine_iter is null
                   and d.numero_determina is not null
                   and d.id_determina = ds.id_determina
                   and ds.tipo_soggetto = 'DIRIGENTE'
                   and ds.sequenza = 0
                   and ds.utente = as4_v_soggetti_correnti.utente) loop
        d_firmatario := c.nominativo;
        exit;
      end loop;
    end if;

    return d_firmatario;
  end get_primo_firmatario_det_c;

  function get_primo_firmatario_det_n(p_id_determina number) return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.nome nominativo
                from firmatari f,
                     as4_v_soggetti_correnti
               where f.id_determina = p_id_determina
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    if d_firmatario is null then
      for c in (select as4_v_soggetti_correnti.nome nominativo
                  from determine               d,
                       determine_soggetti      ds,
                       as4_v_soggetti_correnti
                 where d.id_determina = p_id_determina
                   and d.valido = 'Y'
                   and d.id_engine_iter is null
                   and d.numero_determina is not null
                   and d.id_determina = ds.id_determina
                   and ds.tipo_soggetto = 'DIRIGENTE'
                   and ds.sequenza = 0
                   and ds.utente = as4_v_soggetti_correnti.utente) loop
        d_firmatario := c.nominativo;
        exit;
      end loop;
    end if;

    return d_firmatario;
  end get_primo_firmatario_det_n;

  function get_primo_firmatario_det_cf(p_id_determina number) return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.codice_fiscale nominativo
                from firmatari f,
                     as4_v_soggetti_correnti
               where f.id_determina = p_id_determina
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    if d_firmatario is null then
      for c in (select as4_v_soggetti_correnti.codice_fiscale nominativo
                  from determine               d,
                       determine_soggetti      ds,
                       as4_v_soggetti_correnti
                 where d.id_determina = p_id_determina
                   and d.valido = 'Y'
                   and d.id_engine_iter is null
                   and d.numero_determina is not null
                   and d.id_determina = ds.id_determina
                   and ds.tipo_soggetto = 'DIRIGENTE'
                   and ds.sequenza = 0
                   and ds.utente = as4_v_soggetti_correnti.utente) loop
        d_firmatario := c.nominativo;
        exit;
      end loop;
    end if;

    return d_firmatario;
  end get_primo_firmatario_det_cf;

  function get_primo_firmatario_prop_deli(p_id_proposta_delibera number)
    return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select get_cognome_nome(as4_v_soggetti_correnti.ni,
                                      pd.ente) nominativo
                from proposte_delibera       pd,
                     firmatari               f,
                     as4_v_soggetti_correnti
               where pd.id_proposta_delibera = p_id_proposta_delibera
                 and pd.id_proposta_delibera = f.id_proposta_delibera
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    return d_firmatario;
  end get_primo_firmatario_prop_deli;

  function get_primo_firmatario_visto_par(p_id_visto_parere number)
    return varchar2
   is
    d_firmatario varchar2(4000);
  begin
    for c in (select as4_v_soggetti_correnti.cognome || ' ' ||
                     as4_v_soggetti_correnti.nome nominativo
                from firmatari f,
                     as4_v_soggetti_correnti
               where f.id_visto_parere = p_id_visto_parere
                 and f.utente_firmatario_effettivo =
                     as4_v_soggetti_correnti.utente
                 and f.firmato = 'Y'
                 and f.data_firma is not null
               order by sequenza asc) loop
      d_firmatario := c.nominativo;
      exit;
    end loop;

    return d_firmatario;
  end get_primo_firmatario_visto_par;

  function get_sogg_notifica_delibera(p_id_delibera number) return varchar2
   is
    d_elenco varchar2(4000);
    d_ente   varchar2(255);
    d_ruolo  varchar2(255);
  begin
    begin
      select d.ente
        into d_ente
        from delibere d
       where d.id_delibera = p_id_delibera;

      d_ruolo := impostazioni_pkg.get_impostazione('RUOLO_SO4_NOTIFICHE',
                                                         d_ente);

      for c in (select avsc.cognome || ' ' || avsc.nome nominativo
                  from destinatari_notifiche   dn,
                       as4_v_soggetti_correnti avsc
                 where tipo_destinatario = 'INTERNO'
                   and dn.utente_ad4 = avsc.utente
                   and id_delibera = p_id_delibera
                union
                select avsc.cognome || ' ' || avsc.nome nominativo
                  from destinatari_notifiche   dn,
                       as4_v_soggetti_correnti avsc,
                       delibere                d
                 where tipo_destinatario = 'INTERNO'
                   and dn.utente_ad4 = avsc.utente
                   and d.id_delibera = p_id_delibera
                   and d.id_proposta_delibera = dn.id_proposta_delibera
                union
                select avsc.cognome || ' ' || avsc.nome nominativo
                  from destinatari_notifiche      dn,
                       so4_v_utenti_ruoli_sogg_uo svurs,
                       as4_v_soggetti_correnti    avsc,
                       delibere                   d
                 where dn.tipo_destinatario = 'INTERNO'
                   and d.id_delibera = p_id_delibera
                   and dn.id_delibera = d.id_delibera
                   and svurs.utente = avsc.utente
                   and svurs.uo_progr = dn.unita_progr
                   and svurs.ottica = dn.unita_ottica
                   and svurs.ruolo = d_ruolo
                union
                select avsc.cognome || ' ' || avsc.nome nominativo
                  from destinatari_notifiche      dn,
                       so4_v_utenti_ruoli_sogg_uo svurs,
                       as4_v_soggetti_correnti    avsc,
                       proposte_delibera          pd,
                       delibere                   d
                 where dn.tipo_destinatario = 'INTERNO'
                   and d.id_delibera = p_id_delibera
                   and d.id_proposta_delibera = pd.id_proposta_delibera
                   and dn.id_proposta_delibera = pd.id_proposta_delibera
                   and svurs.utente = avsc.utente
                   and svurs.uo_progr = dn.unita_progr
                   and svurs.ottica = dn.unita_ottica
                   and svurs.ruolo = d_ruolo
                union
                select avsc.cognome || ' ' || avsc.nome nominativo
                  from proposte_delibera_soggetti pds,
                       as4_v_soggetti_correnti    avsc,
                       delibere                   d
                 where d.id_delibera = p_id_delibera
                   and pds.id_proposta_delibera = d.id_proposta_delibera
                   and pds.tipo_soggetto = 'REDATTORE'
                   and pds.utente = avsc.utente
                union
                select avsc.cognome || ' ' || avsc.nome nominativo
                  from proposte_delibera_soggetti pds,
                       as4_v_soggetti_correnti    avsc,
                       delibere                   d
                 where d.id_delibera = p_id_delibera
                   and pds.id_proposta_delibera = d.id_proposta_delibera
                   and pds.tipo_soggetto = 'FUNZIONARIO'
                   and pds.utente = avsc.utente
                union
                select avsc.cognome || ' ' || avsc.nome nominativo
                  from proposte_delibera_soggetti pds,
                       as4_v_soggetti_correnti    avsc,
                       delibere                   d
                 where d.id_delibera = p_id_delibera
                   and pds.id_proposta_delibera = d.id_proposta_delibera
                   and pds.tipo_soggetto = 'DIRIGENTE'
                   and pds.utente = avsc.utente
                union
                select avsc.cognome || ' ' || avsc.nome nominativo
                  from proposte_delibera_soggetti pds,
                       so4_v_utenti_ruoli_sogg_uo svurs,
                       as4_v_soggetti_correnti    avsc,
                       delibere                   d
                 where d.id_delibera = p_id_delibera
                   and pds.id_proposta_delibera = d.id_proposta_delibera
                   and pds.tipo_soggetto = 'UO_PROPONENTE'
                   and svurs.utente = avsc.utente
                   and svurs.uo_progr = pds.unita_progr
                   and svurs.ottica = pds.unita_ottica
                   and svurs.ruolo = d_ruolo
                 order by 1) loop
        d_elenco := d_elenco || c.nominativo || '; ';
      end loop;
    exception
      when others then
        d_elenco := '';
    end;

    return d_elenco;
  end get_sogg_notifica_delibera;

  function get_sogg_notifica_determina(p_id_determina number) return varchar2
   is
    d_elenco varchar2(4000);
    d_ente   varchar2(255);
    d_ruolo  varchar2(255);
  begin
    begin
      select d.ente
        into d_ente
        from determine d
       where d.id_determina = p_id_determina;

      d_ruolo := impostazioni_pkg.get_impostazione('RUOLO_SO4_NOTIFICHE',
                                                         d_ente);

      for c in (select avsc.cognome || ' ' || avsc.nome nominativo
                  from destinatari_notifiche   dn,
                       as4_v_soggetti_correnti avsc
                 where tipo_destinatario = 'INTERNO'
                   and dn.utente_ad4 = avsc.utente
                   and id_determina = p_id_determina
                union
                select avsc.cognome || ' ' || avsc.nome nominativo
                  from destinatari_notifiche      dn,
                       so4_v_utenti_ruoli_sogg_uo svurs,
                       as4_v_soggetti_correnti    avsc,
                       determine                  d
                 where dn.tipo_destinatario = 'INTERNO'
                   and d.id_determina = p_id_determina
                   and dn.id_determina = d.id_determina
                   and svurs.utente = avsc.utente
                   and svurs.uo_progr = dn.unita_progr
                   and svurs.ottica = dn.unita_ottica
                   and svurs.ruolo = d_ruolo
                union
                select avsc.cognome || ' ' || avsc.nome nominativo
                  from determine_soggetti      ds,
                       as4_v_soggetti_correnti avsc,
                       determine               d
                 where d.id_determina = p_id_determina
                   and ds.id_determina = d.id_determina
                   and ds.tipo_soggetto = 'REDATTORE'
                   and ds.utente = avsc.utente
                union
                select avsc.cognome || ' ' || avsc.nome nominativo
                  from determine_soggetti      ds,
                       as4_v_soggetti_correnti avsc,
                       determine               d
                 where d.id_determina = p_id_determina
                   and ds.id_determina = d.id_determina
                   and ds.tipo_soggetto = 'FUNZIONARIO'
                   and ds.utente = avsc.utente
                union
                select avsc.cognome || ' ' || avsc.nome nominativo
                  from determine_soggetti      ds,
                       as4_v_soggetti_correnti avsc,
                       determine               d
                 where d.id_determina = p_id_determina
                   and ds.id_determina = d.id_determina
                   and ds.tipo_soggetto = 'DIRIGENTE'
                   and ds.utente = avsc.utente
                union
                select avsc.cognome || ' ' || avsc.nome nominativo
                  from determine_soggetti         ds,
                       so4_v_utenti_ruoli_sogg_uo svurs,
                       as4_v_soggetti_correnti    avsc,
                       determine                  d
                 where d.id_determina = p_id_determina
                   and ds.id_determina = d.id_determina
                   and ds.tipo_soggetto = 'UO_PROPONENTE'
                   and svurs.utente = avsc.utente
                   and svurs.uo_progr = ds.unita_progr
                   and svurs.ottica = ds.unita_ottica
                   and svurs.ruolo = d_ruolo
                 order by 1) loop
        d_elenco := d_elenco || c.nominativo || '; ';
      end loop;
    exception
      when others then
        d_elenco := '';
    end;

    return d_elenco;
  end get_sogg_notifica_determina;

  function get_suddivisione_descrizione
  (
    p_unita_progr  number,
    p_unita_dal    date,
    p_suddivisione varchar2,
    p_ente         varchar2
  ) return varchar2
   is
    d_id_suddivisione    number(19);
    d_suddivisione_progr number(19);
    d_ottica             varchar2(255);
    d_codice             varchar2(500);
    d_descrizione        varchar2(4000);
  begin
    d_ottica := impostazioni_pkg.get_impostazione('OTTICA_SO4', p_ente);

    if (p_suddivisione = 'SO4_SUDDIVISIONE_AREA') then
      d_codice := impostazioni_pkg.get_impostazione('SO4_SUDDIVISIONE_DIREZIONE', p_ente);

      -- Se '-' il cliente non usa la direzione (la usa solo il comune di parma), vai direttamente ad AREA
      if d_codice != '-' then
        select id_suddivisione
          into d_id_suddivisione
          from so4_v_suddivisioni_struttura
         where codice = d_codice
           and ottica = d_ottica;

        d_suddivisione_progr := so4_util.get_area_unita(d_id_suddivisione,
                                                        p_unita_progr,
                                                        p_unita_dal,
                                                        d_ottica);
        d_descrizione := get_uo_descrizione(d_suddivisione_progr,
                                            p_unita_dal);
      end if;
      -- Se d_codice era '-', oppure il calcolo con DIREZIONE non ha restituito nulla
      if d_descrizione is null then
        d_codice := impostazioni_pkg.get_impostazione('SO4_SUDDIVISIONE_AREA', p_ente);

        select id_suddivisione
          into d_id_suddivisione
          from so4_v_suddivisioni_struttura
         where codice = d_codice
           and ottica = d_ottica;

        d_suddivisione_progr := so4_util.get_area_unita(d_id_suddivisione,
                                                        p_unita_progr,
                                                        p_unita_dal,
                                                        d_ottica);
        d_descrizione := get_uo_descrizione(d_suddivisione_progr,
                                            p_unita_dal);
      end if;

    else
      --NON STO CALCOLANDO L'AREA MA IL SERVIZIO
      d_codice := impostazioni_pkg.get_impostazione(p_suddivisione, p_ente);

      select id_suddivisione
        into d_id_suddivisione
        from so4_v_suddivisioni_struttura
       where codice = d_codice
         and ottica = d_ottica;

      d_suddivisione_progr := so4_util.get_area_unita(d_id_suddivisione,
                                                      p_unita_progr,
                                                      p_unita_dal,
                                                      d_ottica);
      d_descrizione := get_uo_descrizione(d_suddivisione_progr,
                                          p_unita_dal);
    end if;

    return d_descrizione;
  end get_suddivisione_descrizione;

  function get_suddivisione_determina
  (
    p_id_determina number,
    p_suddivisione varchar2,
    p_ente         varchar2
  ) return varchar2
   is
    d_unita_progr  number(19);
    d_unita_dal    date;
    d_suddivisione varchar2(255) := null;
  begin
    begin
      select ds.unita_progr,
             ds.unita_dal
        into d_unita_progr,
             d_unita_dal
        from determine_soggetti ds
       where ds.id_determina = p_id_determina
         and ds.tipo_soggetto = 'UO_PROPONENTE';

      d_suddivisione := utility_pkg.get_suddivisione_descrizione(d_unita_progr,
                                                                 trunc(sysdate),
                                                                 p_suddivisione,
                                                                 p_ente);
    exception
      when no_data_found then
        d_suddivisione := null;
    end;

    return d_suddivisione;
  end get_suddivisione_determina;

  function get_suddivisione_prop_delibera
  (
    p_id_proposta_delibera number,
    p_suddivisione         varchar2,
    p_ente                 varchar2
  ) return varchar2
   is
    d_unita_progr  number(19);
    d_unita_dal    date;
    d_suddivisione varchar2(255) := null;
  begin
    begin
      select pds.unita_progr,
             pds.unita_dal
        into d_unita_progr,
             d_unita_dal
        from proposte_delibera_soggetti pds
       where pds.id_proposta_delibera = p_id_proposta_delibera
         and pds.tipo_soggetto = 'UO_PROPONENTE';

      d_suddivisione := utility_pkg.get_suddivisione_descrizione(d_unita_progr,
                                                                 trunc(sysdate),
                                                                 p_suddivisione,
                                                                 p_ente);
    exception
      when no_data_found then
        d_suddivisione := null;
    end;

    return d_suddivisione;
  end get_suddivisione_prop_delibera;

  function get_suddivisione_uo_determina(p_id_determina number)
    return varchar2
   is
    d_suddivisione varchar2(4000) := null;
  begin
    begin
      select so4_suddivisioni.descrizione suddivisione
        into d_suddivisione
        from determine                      d,
             determine_soggetti             d_uoprop,
             so4_v_unita_organizzative_pubb so4_unita,
             so4_v_suddivisioni_struttura   so4_suddivisioni
       where d.id_determina = p_id_determina
         and d_uoprop.id_determina = d.id_determina
         and d_uoprop.tipo_soggetto = 'UO_PROPONENTE'
         and d_uoprop.unita_dal = so4_unita.dal
         and d_uoprop.unita_ottica = so4_unita.ottica
         and d_uoprop.unita_progr = so4_unita.progr
         and so4_unita.id_suddivisione = so4_suddivisioni.id_suddivisione;
    exception
      when no_data_found then
        d_suddivisione := null;
    end;

    return d_suddivisione;
  end get_suddivisione_uo_determina;

  function get_suddivisione_uo_prop_deli(p_id_proposta_delibera number)
    return varchar2
   is
    d_suddivisione varchar2(4000) := null;
  begin
    begin
      select so4_suddivisioni.descrizione suddivisione
        into d_suddivisione
        from proposte_delibera              pd,
             proposte_delibera_soggetti     pd_uoprop,
             so4_v_unita_organizzative_pubb so4_unita,
             so4_v_suddivisioni_struttura   so4_suddivisioni
       where pd.id_proposta_delibera = p_id_proposta_delibera
         and pd_uoprop.id_proposta_delibera = pd.id_proposta_delibera
         and pd_uoprop.tipo_soggetto = 'UO_PROPONENTE'
         and pd_uoprop.unita_dal = so4_unita.dal
         and pd_uoprop.unita_ottica = so4_unita.ottica
         and pd_uoprop.unita_progr = so4_unita.progr
         and so4_unita.id_suddivisione = so4_suddivisioni.id_suddivisione;
    exception
      when no_data_found then
        d_suddivisione := null;
    end;

    return d_suddivisione;
  end get_suddivisione_uo_prop_deli;

  function get_suddivisione_vistoparere(p_id_visto_parere number)
    return varchar2
   is
    d_suddivisione varchar2(4000) := null;
  begin
    begin
      select so4_suddivisioni.descrizione suddivisione
        into d_suddivisione
        from visti_pareri                   vp,
             so4_v_unita_organizzative_pubb so4_unita,
             so4_v_suddivisioni_struttura   so4_suddivisioni
       where vp.id_visto_parere = p_id_visto_parere
         and vp.unita_dal = so4_unita.dal
         and vp.unita_ottica = so4_unita.ottica
         and vp.unita_progr = so4_unita.progr
         and so4_unita.id_suddivisione = so4_suddivisioni.id_suddivisione;
    exception
      when no_data_found then
        d_suddivisione := null;
    end;

    return d_suddivisione;
  end get_suddivisione_vistoparere;

  function get_testo_proposta_delibera(p_id_proposta_delibera number)
    return blob
   is
    d_testo_proposta   blob;
    d_integrazione_gdm varchar(1);
    d_ente             proposte_delibera.ente%type;
  begin
    dbms_lob.createtemporary(d_testo_proposta,
                             true);

    select ente
      into d_ente
      from proposte_delibera d
     where d.id_proposta_delibera = p_id_proposta_delibera;

    d_integrazione_gdm := impostazioni_pkg.get_impostazione('INTEGRAZIONE_GDM',
                                                            d_ente);

    if d_integrazione_gdm = 'Y' then
      execute immediate '
            select testo_proposta
              from (select gdm_oggetti_file_pkg.DOWNLOADOGGETTOFILE(fa.id_file_esterno) as testo_proposta, 1 pos
                      from proposte_delibera pd
                         , file_allegati fa
                     where pd.id_proposta_delibera = :p_id_proposta_delibera
                       and pd.id_file_allegato_testo_odt is not null
                       and pd.id_file_allegato_testo_odt = fa.id_file_allegato
                       and fa.id_file_esterno is not null
                    union all
                    select gdm_oggetti_file_pkg.DOWNLOADOGGETTOFILE(fa.id_file_esterno) as testo_proposta, 2 pos
                      from proposte_delibera pd
                         , file_allegati fa
                     where pd.id_proposta_delibera = :p_id_proposta_delibera
                       and pd.id_file_allegato_testo_odt is null
                       and pd.id_file_allegato_testo = fa.id_file_allegato
                       and fa.id_file_esterno is not null
                    order by 2)
             where rownum = 1'
        into d_testo_proposta
        using in p_id_proposta_delibera, p_id_proposta_delibera;
    else
      execute immediate '
            select fa.allegato testo_proposta
              from proposte_delibera pd, file_allegati fa
             where pd.id_proposta_delibera = :p_id_proposta_delibera
               and ( (pd.id_file_allegato_testo_odt is not null
                  and pd.id_file_allegato_testo_odt = fa.id_file_allegato)
                  or (pd.id_file_allegato_testo_odt is null
                  and pd.id_file_allegato_testo = fa.id_file_allegato))'
        into d_testo_proposta
        using in p_id_proposta_delibera;
    end if;

    return d_testo_proposta;
  end get_testo_proposta_delibera;

  function get_testo_originale_determina(p_id_determina number)
    return blob
   is
    d_testo_originale  blob;
    d_integrazione_gdm varchar(1);
    d_ente             determine.ente%type;
  begin
    dbms_lob.createtemporary(d_testo_originale,
                             true);

    select ente
      into d_ente
      from determine d
     where d.id_determina = p_id_determina;

    d_integrazione_gdm := impostazioni_pkg.get_impostazione('INTEGRAZIONE_GDM',
                                                            d_ente);

    if d_integrazione_gdm = 'Y' then
      execute immediate '
            select testo_originale
              from (select gdm_oggetti_file_pkg.DOWNLOADOGGETTOFILE(fa.id_file_esterno) as testo_originale
                      from determine d
                          , allegati a
                          , allegati_file af
                          , file_allegati fa
                     where d.id_determina = :p_id_determina
                       and a.id_determina = d.id_determina
                       and a.codice = ''TESTO_ORIGINALE''
                       and af.id_allegato = a.id_allegato
                       and af.id_file = fa.id_file_allegato
                       and fa.id_file_esterno is not null)'
        into d_testo_originale
        using in p_id_determina;
    else
      execute immediate '
            select fa.allegato testo_originale
              from    determine d
                    , allegati a
                    , allegati_file af
                    , file_allegati fa
               where d.id_determina = :p_id_determina
                 and a.id_determina = d.id_determina
                 and a.codice = ''TESTO_ORIGINALE''
                 and af.id_allegato = a.id_allegato
                 and af.id_file = fa.id_file_allegato
                 and fa.id_file_esterno is not null'
        into d_testo_originale
        using in p_id_determina;
    end if;

    return d_testo_originale;
  end get_testo_originale_determina;

  function get_unita_prop_determina(p_id_determina number) return varchar2
   is
    d_unita_progr number(19);
    d_unita_dal   date;
    d_unita_desc  varchar2(255) := null;
  begin
    begin
      select ds.unita_progr,
             ds.unita_dal
        into d_unita_progr,
             d_unita_dal
        from determine_soggetti ds
       where ds.id_determina = p_id_determina
         and ds.tipo_soggetto = 'UO_PROPONENTE';

      d_unita_desc := utility_pkg.get_uo_descrizione(d_unita_progr,
                                                     d_unita_dal);
    exception
      when no_data_found then
        null;
    end;

    return d_unita_desc;
  end get_unita_prop_determina;

  function get_unita_parere_contabile(p_id_delibera number) return varchar2
   is
    d_descrizione varchar2(4000);
  begin
    for c in (select get_uo_descrizione(unita_progr,
                                        unita_dal) uo_visto
                from visti_pareri      vp,
                     tipi_visto_parere tvp,
                     delibere          d
               where d.id_delibera = p_id_delibera
                 and vp.id_proposta_delibera = d.id_proposta_delibera
                 and vp.id_tipologia = tvp.id_tipo_visto_parere
                 and tvp.contabile = 'Y'
               order by vp.data_upd desc) loop
      d_descrizione := c.uo_visto;
      exit;
    end loop;

    return d_descrizione;
  end get_unita_parere_contabile;

  function get_unita_prop_delibera(p_id_proposta_delibera number)
    return varchar2
   is
    d_unita_progr number(19);
    d_unita_dal   date;
    d_unita_desc  varchar2(255) := null;
  begin
    begin
      select pds.unita_progr,
             pds.unita_dal
        into d_unita_progr,
             d_unita_dal
        from proposte_delibera_soggetti pds
       where pds.id_proposta_delibera = p_id_proposta_delibera
         and pds.tipo_soggetto = 'UO_PROPONENTE';

      d_unita_desc := utility_pkg.get_uo_descrizione(d_unita_progr,
                                                     d_unita_dal);
    exception
      when no_data_found then
        null;
    end;

    return d_unita_desc;
  end get_unita_prop_delibera;

  function get_unita_visto_contabile(p_id_determina number) return varchar2
   is
    d_descrizione varchar2(4000);
  begin
    for c in (select get_uo_descrizione(unita_progr,
                                        unita_dal) uo_visto
                from visti_pareri      vp,
                     tipi_visto_parere tvp
               where vp.id_determina = p_id_determina
                 and vp.id_tipologia = tvp.id_tipo_visto_parere
                 and tvp.contabile = 'Y'
               order by vp.data_upd desc) loop
      d_descrizione := c.uo_visto;
      exit;
    end loop;

    return d_descrizione;
  end get_unita_visto_contabile;

  function get_uo_descrizione
  (
    p_unita_progr number,
    p_unita_dal   date
  ) return varchar2
   is
    d_descrizione varchar2(4000);
  begin
    begin
      d_descrizione := so4_util.anuo_get_descrizione(p_unita_progr,
                                                     p_unita_dal);
    exception
      when others then
        d_descrizione := '--';
    end;

    return d_descrizione;
  end get_uo_descrizione;

  function get_uo_padre_descrizione
  (
    p_unita_progr number,
    p_unita_dal   date,
    p_ottica      varchar2
  ) return varchar2
   is
    d_descrizione varchar2(4000);
  begin
    begin
      d_descrizione := so4_util.unita_get_unita_padre(p_unita_progr,
                                                      p_ottica,
                                                      p_unita_dal);

      d_descrizione := substr(d_descrizione,
                              instr(d_descrizione,
                                    '#',
                                    -1) + 1,
                              length(d_descrizione) -
                              instr(d_descrizione,
                                    '#',
                                    -1));
    exception
      when others then
        d_descrizione := '';
    end;

    return d_descrizione;
  end get_uo_padre_descrizione;

  function get_uo_padre_determina(p_id_determina number) return varchar2
   is
    d_unita_progr  number(19);
    d_unita_ottica varchar2(255);
    d_unita_dal    date;
    d_descrizione  varchar2(4000);
  begin
    begin
      select ds.unita_progr,
             ds.unita_ottica,
             trunc(sysdate)
        into d_unita_progr,
             d_unita_ottica,
             d_unita_dal
        from determine_soggetti ds
       where ds.id_determina = p_id_determina
         and ds.tipo_soggetto = 'UO_PROPONENTE';

      d_descrizione := get_uo_padre_descrizione(d_unita_progr,
                                                d_unita_dal,
                                                d_unita_ottica);
    exception
      when others then
        d_descrizione := '';
    end;

    return d_descrizione;
  end get_uo_padre_determina;

  function get_uo_padre_prop_delibera(p_id_proposta_delibera number)
    return varchar2
   is
    d_unita_progr  number(19);
    d_unita_ottica varchar2(255);
    d_unita_dal    date;
    d_descrizione  varchar2(4000);
  begin
    begin
      select pds.unita_progr,
             pds.unita_ottica,
             trunc(sysdate)
        into d_unita_progr,
             d_unita_ottica,
             d_unita_dal
        from proposte_delibera_soggetti pds
       where pds.id_proposta_delibera = p_id_proposta_delibera
         and pds.tipo_soggetto = 'UO_PROPONENTE';

      d_descrizione := get_uo_padre_descrizione(d_unita_progr,
                                                d_unita_dal,
                                                d_unita_ottica);
    exception
      when others then
        d_descrizione := '';
    end;

    return d_descrizione;
  end get_uo_padre_prop_delibera;

  function get_votanti_oggetto_seduta
  (
    p_id_oggetto_seduta number,
    p_voto              varchar2
  ) return varchar2
   is
    d_elenco varchar2(32767);
  begin
    for c in (select sequenza,
                     cognome_nome
                from (select oop.sequenza,
                             initcap(get_cognome_nome(nvl(occ.ni_componente,
                                                          osp.ni_componente_esterno),
                                                      ov.ente)) cognome_nome
                        from odg_sedute_partecipanti    osp,
                             odg_oggetti_partecipanti   oop,
                             odg_oggetti_seduta         oos,
                             odg_commissioni_componenti occ,
                             odg_voti                   ov
                       where oos.id_oggetto_seduta = p_id_oggetto_seduta
                         and oos.id_oggetto_seduta = oop.id_oggetto_seduta
                         and osp.id_seduta_partecipante =
                             oop.id_seduta_partecipante
                         and occ.id_commissione_componente(+) =
                             osp.id_commissione_componente
                         and oop.id_voto = ov.id_voto(+)
                         and ((p_voto is null) or
                             (p_voto is not null and ov.codice = p_voto)))
               order by sequenza,
                        cognome_nome asc) loop
      d_elenco := d_elenco || c.cognome_nome || ', ';
    end loop;

    if (d_elenco is not null) then
      d_elenco := substr(d_elenco,
                         1,
                         length(d_elenco) - 2);
    end if;

    return d_elenco;
  end get_votanti_oggetto_seduta;

  function giorno_lettere(p_data in date) return varchar2
   is
    d_valore   varchar2(10);
    dep_giorno varchar2(10);
  begin
    select trim(upper(to_char(p_data,
                              'DAY',
                              'NLS_DATE_LANGUAGE=italian')))
      into dep_giorno
      from dual;

    if (dep_giorno = 'LUNED¿') then
      d_valore := 'lunedi''';
    elsif (dep_giorno = 'MARTED¿') then
      d_valore := 'martedi''';
    elsif (dep_giorno = 'MERCOLED¿') then
      d_valore := 'mercoledi''';
    elsif (dep_giorno = 'GIOVED¿') then
      d_valore := 'giovedi''';
    elsif (dep_giorno = 'VENERD¿') then
      d_valore := 'venerdi''';
    elsif dep_giorno = 'SABATO' then
      d_valore := 'sabato';
    elsif dep_giorno = 'DOMENICA' then
      d_valore := 'domenica';
    else
      d_valore := lower(dep_giorno);
    end if;

    return d_valore;
  end giorno_lettere;

  function mese_lettere(a_numero in number) return varchar2
   is
    d_stringa varchar2(256);
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

    return lower(d_stringa);
  end mese_lettere;

  function numero_lettere(a_numero in number)
    return varchar2
   is
    d_importo     varchar2(12);
    d_importo_dec varchar2(2);
    d_cifra       number;
    d_stringa     varchar2(256);
  begin
    d_stringa := null;
    d_importo := lpad(to_char(trunc(a_numero)),
                      12,
                      '0');
    d_cifra   := mod(round(a_numero,
                           2),
                     1) * 100;

    if d_cifra > 9 then
      d_importo_dec := rpad(to_char(mod(round(a_numero,
                                              2),
                                        1) * 100),
                            2,
                            '0');
    else
      d_importo_dec := lpad(to_char(mod(round(a_numero,
                                              2),
                                        1) * 100),
                            2,
                            '0');
    end if;

    for i in 1 .. 12 loop
      d_cifra := substr(d_importo,
                        i,
                        1);

      if i in (1,
               4,
               7,
               10) then

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

      elsif i in (2,
                  5,
                  8,
                  11) then
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
          if substr(d_importo,
                    i + 1,
                    1) in (1,
                           8) then
            null;
          else
            d_stringa := d_stringa || 'I';
          end if;
        elsif d_cifra > 2 then
          if substr(d_importo,
                    i + 1,
                    1) in (1,
                           8) then
            null;
          else
            d_stringa := d_stringa || 'A';
          end if;
        end if;

        if d_cifra = 1 then
          if substr(d_importo,
                    i + 1,
                    1) = 0 then
            d_stringa := d_stringa || 'DIECI';
          elsif substr(d_importo,
                       i + 1,
                       1) = 1 then
            d_stringa := d_stringa || 'UNDICI';
          elsif substr(d_importo,
                       i + 1,
                       1) = 2 then
            d_stringa := d_stringa || 'DODICI';
          elsif substr(d_importo,
                       i + 1,
                       1) = 3 then
            d_stringa := d_stringa || 'TREDICI';
          elsif substr(d_importo,
                       i + 1,
                       1) = 4 then
            d_stringa := d_stringa || 'QUATTORDICI';
          elsif substr(d_importo,
                       i + 1,
                       1) = 5 then
            d_stringa := d_stringa || 'QUINDICI';
          elsif substr(d_importo,
                       i + 1,
                       1) = 6 then
            d_stringa := d_stringa || 'SEDICI';
          elsif substr(d_importo,
                       i + 1,
                       1) = 7 then
            d_stringa := d_stringa || 'DICIASSETTE';
          elsif substr(d_importo,
                       i + 1,
                       1) = 8 then
            d_stringa := d_stringa || 'DICIOTTO';
          elsif substr(d_importo,
                       i + 1,
                       1) = 9 then
            d_stringa := d_stringa || 'DICIANNOVE';
          end if;
        else
          if substr(d_importo,
                    i + 1,
                    1) = 1 then
            if substr(d_importo,
                      i - 1,
                      3) = '001' then
              if i in (2,
                       5) then
                d_stringa := d_stringa || 'UN';
              elsif i = 11 then
                d_stringa := d_stringa || 'UNO';
              end if;
            else
              d_stringa := d_stringa || 'UNO';
            end if;
          elsif substr(d_importo,
                       i + 1,
                       1) = 2 then
            d_stringa := d_stringa || 'DUE';
          elsif substr(d_importo,
                       i + 1,
                       1) = 3 then
            d_stringa := d_stringa || 'TRE';
          elsif substr(d_importo,
                       i + 1,
                       1) = 4 then
            d_stringa := d_stringa || 'QUATTRO';
          elsif substr(d_importo,
                       i + 1,
                       1) = 5 then
            d_stringa := d_stringa || 'CINQUE';
          elsif substr(d_importo,
                       i + 1,
                       1) = 6 then
            d_stringa := d_stringa || 'SEI';
          elsif substr(d_importo,
                       i + 1,
                       1) = 7 then
            d_stringa := d_stringa || 'SETTE';
          elsif substr(d_importo,
                       i + 1,
                       1) = 8 then
            d_stringa := d_stringa || 'OTTO';
          elsif substr(d_importo,
                       i + 1,
                       1) = 9 then
            d_stringa := d_stringa || 'NOVE';
          end if;
        end if;
      end if;

      if i = 2 then
        if substr(d_importo,
                  1,
                  3) = '000' then
          null;
        elsif substr(d_importo,
                     1,
                     3) = '001' then
          d_stringa := d_stringa || 'MILIARDO';
        else
          d_stringa := d_stringa || 'MILIARDI';
        end if;
      elsif i = 5 then
        if substr(d_importo,
                  4,
                  3) = '000' then
          null;
        elsif substr(d_importo,
                     4,
                     3) = '001' then
          d_stringa := d_stringa || 'MILIONE';
        else
          d_stringa := d_stringa || 'MILIONI';
        end if;
      elsif i = 8 then
        if substr(d_importo,
                  7,
                  3) = '000' then
          null;
        elsif substr(d_importo,
                     7,
                     3) = '001' then
          d_stringa := d_stringa || 'MILLE';
        else
          d_stringa := d_stringa || 'MILA';
        end if;
      end if;

    end loop;

    if d_importo = '000000000000' then
      d_stringa := 'ZERO';
    end if;

    if d_importo_dec != '00' then
      d_stringa := d_stringa || ' VIRGOLA ';
      d_cifra   := substr(d_importo_dec,
                          1,
                          1);

      if substr(d_importo_dec,
                2,
                1) != 0 then
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
          if substr(d_importo_dec,
                    2,
                    1) in (1,
                           8) then
            null;
          else
            d_stringa := d_stringa || 'I';
          end if;
        elsif d_cifra > 2 then
          if substr(d_importo_dec,
                    2,
                    1) in (1,
                           8) then
            null;
          else
            d_stringa := d_stringa || 'A';
          end if;
        end if;

        if d_cifra = 1 then
          if substr(d_importo_dec,
                    2,
                    1) = 1 then
            d_stringa := d_stringa || 'UNDICI';
          elsif substr(d_importo_dec,
                       2,
                       1) = 2 then
            d_stringa := d_stringa || 'DODICI';
          elsif substr(d_importo_dec,
                       2,
                       1) = 3 then
            d_stringa := d_stringa || 'TREDICI';
          elsif substr(d_importo_dec,
                       2,
                       1) = 4 then
            d_stringa := d_stringa || 'QUATTORDICI';
          elsif substr(d_importo_dec,
                       2,
                       1) = 5 then
            d_stringa := d_stringa || 'QUINDICI';
          elsif substr(d_importo_dec,
                       2,
                       1) = 6 then
            d_stringa := d_stringa || 'SEDICI';
          elsif substr(d_importo_dec,
                       2,
                       1) = 7 then
            d_stringa := d_stringa || 'DICIASSETTE';
          elsif substr(d_importo_dec,
                       2,
                       1) = 8 then
            d_stringa := d_stringa || 'DICIOTTO';
          elsif substr(d_importo_dec,
                       2,
                       1) = 9 then
            d_stringa := d_stringa || 'DICIANNOVE';
          end if;
        end if;
      end if;

      if d_cifra = 1 and substr(d_importo_dec,
                                2,
                                1) > 0 then
        null;
      else
        if substr(d_importo_dec,
                  2,
                  1) != 0 then
          d_cifra := substr(d_importo_dec,
                            2,
                            1);
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

    return lower(d_stringa);
  end numero_lettere;

  function join_str
  (
    p_cursor sys_refcursor,
    p_del    varchar2 := ','
  ) return varchar2
   is
    l_value  varchar2(32767);
    l_result varchar2(32767);
  begin
    loop
      fetch p_cursor
        into l_value;

      exit when p_cursor%notfound;

      if l_result is not null then
        l_result := l_result || p_del;
      end if;

      l_result := l_result || l_value;
    end loop;

    return l_result;
  end join_str;

  function join_clob
  (
    p_cursor sys_refcursor,
    p_del    varchar2 := ','
  ) return clob
   is
    l_value  varchar2(32767);
    l_result varchar2(32767);
    d_clob   clob := empty_clob();
  begin
    dbms_lob.createtemporary(d_clob,
                             true,
                             dbms_lob.call);

    loop
      fetch p_cursor
        into l_value;

      exit when p_cursor%notfound;

      if d_clob is not null then
        d_clob := d_clob || p_del;
      end if;

      d_clob := d_clob || l_value;
    end loop;

    return d_clob;
    dbms_lob.freetemporary(d_clob);
  end join_clob;

  function split_str
  (
    p_list varchar2,
    p_del  varchar2 := ','
  ) return split_tbl
    pipelined
   is
    l_idx   pls_integer;
    l_list  varchar2(32767) := p_list;
    l_value varchar2(32767);
  begin
    loop
      l_idx := instr(l_list,
                     p_del);

      if l_idx > 0 then
        pipe row(substr(l_list,
                        1,
                        l_idx - 1));
        l_list := substr(l_list,
                         l_idx + length(p_del));
      else
        pipe row(l_list);
        exit;
      end if;
    end loop;

    return;
  end split_str;

  function get_stato_visti(p_id_atto number) return varchar2
   is
    d_stato         varchar2(100) := '';
    num_totale      int;
    num_contrari    int;
    num_non_apposti int;
    num_favorevoli  int;
  begin
    select count(*)
      into num_totale
      from visti_pareri
     where (id_determina = p_id_atto or id_proposta_delibera = p_id_atto or
           id_delibera = p_id_atto)
       and esito != 'DA_VALUTARE'
       and valido = 'Y';

    select count(*)
      into num_contrari
      from visti_pareri
     where (id_determina = p_id_atto or id_proposta_delibera = p_id_atto or
           id_delibera = p_id_atto)
       and esito in ('RIMANDA_INDIETRO',
                     'CONTRARIO')
       and valido = 'Y';

    select count(*)
      into num_non_apposti
      from visti_pareri
     where (id_determina = p_id_atto or id_proposta_delibera = p_id_atto or
           id_delibera = p_id_atto)
       and esito = 'NON_APPOSTO'
       and valido = 'Y';

    select count(*)
      into num_favorevoli
      from visti_pareri
     where (id_determina = p_id_atto or id_proposta_delibera = p_id_atto or
           id_delibera = p_id_atto)
       and esito in ('FAVOREVOLE',
                     'FAVOREVOLE_CON_PRESCRIZIONI')
       and valido = 'Y';

    if num_contrari > 0 then
      d_stato := 'CONTRARIO';
    elsif num_non_apposti > 0 then
      d_stato := 'NON_APPOSTO';
    elsif num_totale = num_favorevoli and num_totale > 0 then
      d_stato := 'FAVOREVOLE';
    end if;

    return d_stato;
  end get_stato_visti;

  function get_descrizione_firmatario
  (
    p_utente varchar2,
    p_ente   varchar2
  ) return varchar2
   is
    d_utente varchar2(4000);
    d_ruolo  varchar2(500);
  begin
    if (p_utente is null) then
      return null;
    end if;

    d_ruolo := impostazioni_pkg.get_impostazione('RUOLO_SO4_DIRIGENTE',
                                                 p_ente);

    begin
      select utente
        into d_utente
        from so4_v_utenti_ruoli_sogg_uo
       where utente = p_utente
         and ruolo = d_ruolo
         and rownum = 1;

      return impostazioni_pkg.get_impostazione('MODELLI_STAMPE_DIRIGENTE',
                                               p_ente);
    exception
      when no_data_found then

        return impostazioni_pkg.get_impostazione('MODELLI_STAMPE_FIRMATARIO',
                                                 p_ente);
    end;
  end get_descrizione_firmatario;

  function get_delegante
  (
    p_firmatario varchar2,
    p_utente     varchar2,
    p_ente       varchar2
  ) return varchar2
   is
  begin
    if (p_utente is null) then
      return null;
    end if;

    if (p_firmatario is not null and p_firmatario <> p_utente) then
      return '(' || impostazioni_pkg.get_impostazione('MODELLI_STAMPE_DELEGATO',
                                                      p_ente) || ' ' || utility_pkg.get_cognome_nome(utility_pkg.get_ni_soggetto(p_utente),
                                                                                                     p_ente) || ')';
    end if;

    return '';
  end get_delegante;

  function inserisci_tipo_delega_so4
  (
    p_id      varchar2,
    p_titolo  varchar2,
    p_modulo  varchar2,
    p_istanza varchar2
  ) return varchar2
   is
    d_id_applicativo number;
  begin
    select id_applicativo
      into d_id_applicativo
      from so4_applicativi appl
     where modulo = p_modulo
       and appl.istanza = p_istanza;

    return so4_competenze_delega_tpk.ins(null,
                                         p_id,
                                         p_titolo,
                                         d_id_applicativo,
                                         null);
  end inserisci_tipo_delega_so4;

  function aggiorna_tipo_delega_so4
  (
    p_id      varchar2,
    p_titolo  varchar2,
    p_modulo  varchar2,
    p_istanza varchar2
  ) return varchar2
   is
    d_id_applicativo       number;
    d_id_competenza_delega number;
    d_dataval_al           date := null;
  begin
    begin
      select id_applicativo
        into d_id_applicativo
        from so4_applicativi appl
       where modulo = p_modulo
         and appl.istanza = p_istanza;

      select id_competenza_delega
        into d_id_competenza_delega
        from so4_competenze_delega code,
             so4_applicativi       appl
       where appl.modulo = p_modulo
         and appl.istanza = p_istanza
         and code.id_applicativo = appl.id_applicativo
         and code.codice = p_id;

      so4_competenze_delega_tpk.upd(p_check_old                => 0,
                                    p_old_id_competenza_delega => d_id_competenza_delega,
                                    p_new_id_competenza_delega => d_id_competenza_delega,
                                    p_old_codice               => p_id,
                                    p_new_codice               => p_id,
                                    p_new_id_applicativo       => d_id_applicativo,
                                    p_old_id_applicativo       => d_id_applicativo,
                                    p_new_descrizione          => p_titolo,
                                    p_old_fine_validita        => d_dataval_al,
                                    p_new_fine_validita        => d_dataval_al);
    exception when no_data_found then
      null;
    end;
    return p_id;
  end aggiorna_tipo_delega_so4;

  function rimuovi_tipo_delega_so4
  (
    p_id      varchar2,
    p_modulo  varchar2,
    p_istanza varchar2
  ) return varchar2
   is
    d_id_applicativo       number;
    d_id_competenza_delega number;
    d_dataval_al           date := sysdate;
  begin
    select id_applicativo
      into d_id_applicativo
      from so4_applicativi appl
     where modulo = p_modulo
       and appl.istanza = p_istanza;

    select id_competenza_delega
      into d_id_competenza_delega
      from so4_competenze_delega code,
           so4_applicativi       appl
     where appl.modulo = p_modulo
       and appl.istanza = p_istanza
       and code.id_applicativo = appl.id_applicativo
       and code.codice = p_id;

    so4_competenze_delega_tpk.del(p_check_old            => 0,
                                  p_id_competenza_delega => d_id_competenza_delega,
                                  p_codice               => p_id);
    return p_id;
  end rimuovi_tipo_delega_so4;

  function get_determina_conclusa(p_id_determina number) return char
   is
    v_count               number(19) := 0;
    v_cert_conclusi       number(19) := 0;
    v_cert_non_conclusi   number(19) := 0;
    v_tipo_cert           number(19) := 0;
    v_tipo_determina      number(19) := 0;
    v_tipo_cert_pubb      number(19) := 0;
    v_tipo_cert_pubb2     number(19) := 0;
    v_tipo_cert_avv_pubb  number(19) := 0;
    v_tipo_cert_avv_pubb2 number(19) := 0;
    v_tipo_cert_esec      number(19) := 0;
    v_stato_determina     varchar2(255) := '';
  begin
    begin
      select id_tipo_determina,
             stato
        into v_tipo_determina,
             v_stato_determina
        from determine
       where id_determina = p_id_determina;
    exception
      when no_data_found then
        return('N');
    end;

    if nvl(v_stato_determina,
           'XXX') in ('NON_ESECUTIVO',
                      'ANNULLATO') then
      return('Y');
    end if;

    select count(*)
      into v_count
      from visti_pareri
     where id_determina = p_id_determina
       and valido = 'Y'
       and nvl(stato,
               'XXX') not in ('CONCLUSO',
                              'DA_PROCESSARE');

    if (v_count > 0) then
      return('N');
    end if;

    select id_tipo_cert_pubb,
           id_tipo_cert_pubb2,
           id_tipo_cert_avv_pubb,
           id_tipo_cert_avv_pubb2,
           id_tipo_cert_esec
      into v_tipo_cert_pubb,
           v_tipo_cert_pubb2,
           v_tipo_cert_avv_pubb,
           v_tipo_cert_avv_pubb2,
           v_tipo_cert_esec
      from tipi_determina
     where id_tipo_determina = v_tipo_determina;

    if (v_tipo_cert_pubb is not null) then
      select (select count(*)
                from certificati
               where id_determina = p_id_determina
                 and id_tipologia = v_tipo_cert_pubb
                 and valido = 'Y'
                 and stato <> 'CONCLUSO'),
             (select count(*)
                from certificati
               where id_determina = p_id_determina
                 and id_tipologia = v_tipo_cert_pubb
                 and valido = 'Y'
                 and stato = 'CONCLUSO')
        into v_cert_non_conclusi,
             v_cert_conclusi
        from dual;

      if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0) then
        return('N');
      end if;
    end if;

    if (v_tipo_cert_pubb2 is not null) then
      select (select count(*)
                from certificati
               where id_determina = p_id_determina
                 and id_tipologia = v_tipo_cert_pubb2
                 and valido = 'Y'
                 and stato <> 'CONCLUSO'),
             (select count(*)
                from certificati
               where id_determina = p_id_determina
                 and id_tipologia = v_tipo_cert_pubb2
                 and valido = 'Y'
                 and stato = 'CONCLUSO')
        into v_cert_non_conclusi,
             v_cert_conclusi
        from dual;

      if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0) then
        return('N');
      end if;
    end if;

    if (v_tipo_cert_avv_pubb is not null) then
      select (select count(*)
                from certificati
               where id_determina = p_id_determina
                 and id_tipologia = v_tipo_cert_avv_pubb
                 and valido = 'Y'
                 and stato <> 'CONCLUSO'),
             (select count(*)
                from certificati
               where id_determina = p_id_determina
                 and id_tipologia = v_tipo_cert_avv_pubb
                 and valido = 'Y'
                 and stato = 'CONCLUSO')
        into v_cert_non_conclusi,
             v_cert_conclusi
        from dual;

      if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0) then
        return('N');
      end if;
    end if;

    if (v_tipo_cert_avv_pubb2 is not null) then
      select (select count(*)
                from certificati
               where id_determina = p_id_determina
                 and id_tipologia = v_tipo_cert_avv_pubb2
                 and valido = 'Y'
                 and stato <> 'CONCLUSO'),
             (select count(*)
                from certificati
               where id_determina = p_id_determina
                 and id_tipologia = v_tipo_cert_avv_pubb2
                 and valido = 'Y'
                 and stato = 'CONCLUSO')
        into v_cert_non_conclusi,
             v_cert_conclusi
        from dual;

      if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0) then
        return('N');
      end if;
    end if;

    if (v_tipo_cert_esec is not null) then
      select (select count(*)
                from certificati
               where id_determina = p_id_determina
                 and id_tipologia = v_tipo_cert_esec
                 and valido = 'Y'
                 and stato <> 'CONCLUSO'),
             (select count(*)
                from certificati
               where id_determina = p_id_determina
                 and id_tipologia = v_tipo_cert_esec
                 and valido = 'Y'
                 and stato = 'CONCLUSO')
        into v_cert_non_conclusi,
             v_cert_conclusi
        from dual;

      if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0) then
        return('N');
      end if;
    end if;

    return('Y');
  end get_determina_conclusa;

  function get_delibera_conclusa(p_id_delibera number) return char
   is
    v_count               number(19) := 0;
    v_tipo_delibera       number(19) := 0;
    v_immediata           char(1) := 'N';
    v_cert_conclusi       number(19) := 0;
    v_cert_non_conclusi   number(19) := 0;
    v_tipo_cert           number(19) := 0;
    v_tipo_cert_pubb      number(19) := 0;
    v_tipo_cert_pubb2     number(19) := 0;
    v_tipo_cert_avv_pubb  number(19) := 0;
    v_tipo_cert_avv_pubb2 number(19) := 0;
    v_tipo_cert_eseg      number(19) := 0;
    v_tipo_cert_imm_eseg  number(19) := 0;
  begin
    begin
      select p.id_tipo_delibera,
             d.eseguibilita_immediata
        into v_tipo_delibera,
             v_immediata
        from delibere          d,
             proposte_delibera p
       where d.id_delibera = p_id_delibera
         and p.id_proposta_delibera = d.id_proposta_delibera;
    exception
      when no_data_found then
        return('N');
    end;

    select count(*)
      into v_count
      from visti_pareri
     where id_delibera = p_id_delibera
       and valido = 'Y'
       and nvl(stato,
               'XXX') not in ('CONCLUSO',
                              'DA_PROCESSARE');

    if (v_count > 0) then
      return('N');
    end if;

    select id_tipo_cert_pubb,
           id_tipo_cert_pubb2,
           id_tipo_cert_avv_pubb,
           id_tipo_cert_avv_pubb2,
           id_tipo_cert_esec,
           id_tipo_cert_imm_eseg
      into v_tipo_cert_pubb,
           v_tipo_cert_pubb2,
           v_tipo_cert_avv_pubb,
           v_tipo_cert_avv_pubb2,
           v_tipo_cert_eseg,
           v_tipo_cert_imm_eseg
      from tipi_delibera
     where id_tipo_delibera = v_tipo_delibera;

    if (v_tipo_cert_pubb is not null) then
      select (select count(*)
                from certificati
               where id_delibera = p_id_delibera
                 and id_tipologia = v_tipo_cert_pubb
                 and valido = 'Y'
                 and stato <> 'CONCLUSO'),
             (select count(*)
                from certificati
               where id_delibera = p_id_delibera
                 and id_tipologia = v_tipo_cert_pubb
                 and valido = 'Y'
                 and stato = 'CONCLUSO')
        into v_cert_non_conclusi,
             v_cert_conclusi
        from dual;

      if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0) then
        return('N');
      end if;
    end if;

    if (v_tipo_cert_pubb2 is not null) then
      select (select count(*)
                from certificati
               where id_delibera = p_id_delibera
                 and id_tipologia = v_tipo_cert_pubb2
                 and valido = 'Y'
                 and stato <> 'CONCLUSO'),
             (select count(*)
                from certificati
               where id_delibera = p_id_delibera
                 and id_tipologia = v_tipo_cert_pubb2
                 and valido = 'Y'
                 and stato = 'CONCLUSO')
        into v_cert_non_conclusi,
             v_cert_conclusi
        from dual;

      if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0) then
        return('N');
      end if;
    end if;

    if (v_tipo_cert_avv_pubb is not null) then
      select (select count(*)
                from certificati
               where id_delibera = p_id_delibera
                 and id_tipologia = v_tipo_cert_avv_pubb
                 and valido = 'Y'
                 and stato <> 'CONCLUSO'),
             (select count(*)
                from certificati
               where id_delibera = p_id_delibera
                 and id_tipologia = v_tipo_cert_avv_pubb
                 and valido = 'Y'
                 and stato = 'CONCLUSO')
        into v_cert_non_conclusi,
             v_cert_conclusi
        from dual;

      if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0) then
        return('N');
      end if;
    end if;

    if (v_tipo_cert_avv_pubb2 is not null) then
      select (select count(*)
                from certificati
               where id_delibera = p_id_delibera
                 and id_tipologia = v_tipo_cert_avv_pubb2
                 and valido = 'Y'
                 and stato <> 'CONCLUSO'),
             (select count(*)
                from certificati
               where id_delibera = p_id_delibera
                 and id_tipologia = v_tipo_cert_avv_pubb2
                 and valido = 'Y'
                 and stato = 'CONCLUSO')
        into v_cert_non_conclusi,
             v_cert_conclusi
        from dual;

      if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0) then
        return('N');
      end if;
    end if;

    if (v_immediata = 'Y' and v_tipo_cert_imm_eseg is not null) then
      select (select count(*)
                from certificati
               where id_delibera = p_id_delibera
                 and id_tipologia = v_tipo_cert_imm_eseg
                 and valido = 'Y'
                 and stato <> 'CONCLUSO'),
             (select count(*)
                from certificati
               where id_delibera = p_id_delibera
                 and id_tipologia = v_tipo_cert_imm_eseg
                 and valido = 'Y'
                 and stato = 'CONCLUSO')
        into v_cert_non_conclusi,
             v_cert_conclusi
        from dual;

      if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0) then
        return('N');
      end if;
    end if;

    if (v_tipo_cert_eseg is not null) then
      select (select count(*)
                from certificati
               where id_delibera = p_id_delibera
                 and id_tipologia = v_tipo_cert_eseg
                 and valido = 'Y'
                 and stato <> 'CONCLUSO'),
             (select count(*)
                from certificati
               where id_delibera = p_id_delibera
                 and id_tipologia = v_tipo_cert_eseg
                 and valido = 'Y'
                 and stato = 'CONCLUSO')
        into v_cert_non_conclusi,
             v_cert_conclusi
        from dual;

      if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0) then
        return('N');
      end if;
    end if;

    return('Y');
  end get_delibera_conclusa;

  function get_esito_rinvio_proposta(p_id_proposta_delibera number)
    return varchar2
   is
    v_esito varchar2(4000);
  begin
    select esito.titolo || ' nella Seduta del C.C. del ' ||
           to_char(esito.data_discussione,
                   'DD/MM/YYYY')
      into V_ESITO
      from (select *
              from (select e.titolo,
                           e.esito_standard,
                           ogg.data_discussione
                      from odg_esiti          e,
                           odg_oggetti_seduta ogg
                     where OGG.ID_PROPOSTA_DELIBERA = p_id_proposta_delibera
                       and ogg.conferma_esito = 'Y'
                       and ogg.id_esito = e.id_esito
                     order by ogg.data_discussione desc)
             where rownum = 1) esito
     where esito.esito_standard = 'PARZIALE';

    return V_ESITO;
  end get_esito_rinvio_proposta;

  procedure elimina_componenti_non_validi(pd_id_commissione number)
   is
  begin
    merge into odg_commissioni_componenti occ
    using (select ni,
                  al
             from odg_commissioni_componenti c,
                  as4_v_soggetti             sog
            where valido_al is null
              and not exists (select 1
                     from as4_v_soggetti_correnti a
                    where a.ni = c.ni_componente)
              and not exists (select 1
                     from as4_v_soggetti aa
                    where aa.al > sog.al
                      and aa.ni = sog.ni)
              and c.id_commissione = pd_id_commissione
              and c.ni_componente = sog.ni) upd
    on (occ.id_commissione = pd_id_commissione and occ.ni_componente = upd.ni)
    when matched then
      update
         set occ.valido    = 'N',
             occ.valido_al = upd.al;
  end;

  function allegatiDeterminaVisibili(p_id_determina number) return number
   is
    p_ente                   varchar2(255);
    v_data_pubblicazione_dal date;
    v_data_pubblicazione_al  date;
    v_pubblica_allegati      varchar(1);
    d_impostazione           varchar(1);
  begin
    select d.ente,
           d.data_pubblicazione,
           d.data_fine_pubblicazione,
           TD.PUBBLICA_ALLEGATI_VIS
      into p_ente,
           v_data_pubblicazione_dal,
           v_data_pubblicazione_al,
           v_pubblica_allegati
      from determine      d,
           tipi_determina td
     where d.id_determina = p_id_determina
       and td.id_tipo_determina = d.id_tipo_determina;

    d_impostazione := impostazioni_pkg.get_impostazione('VIS_GESTIONE_PUBBLCAZIONE_ALLEGATI',
                                                        p_ente);

    if (d_impostazione = 'N' or v_pubblica_allegati = 'Y' or
       (trunc(v_data_pubblicazione_dal) <= trunc(sysdate) and
       trunc(v_data_pubblicazione_al) >= trunc(sysdate))) then
      return 1;
    end if;

    return 0;
  end allegatiDeterminaVisibili;

  function allegatiDeliberaVisibili(p_id_delibera number) return number
   is
    p_ente                   varchar2(255);
    v_data_pubblicazione_dal date;
    v_data_pubblicazione_al  date;
    v_pubblica_allegati      varchar(1);
    d_impostazione           varchar(1);
  begin
    select d.ente,
           d.data_pubblicazione,
           d.data_fine_pubblicazione,
           TD.PUBBLICA_ALLEGATI_VIS
      into p_ente,
           v_data_pubblicazione_dal,
           v_data_pubblicazione_al,
           v_pubblica_allegati
      from delibere          d,
           tipi_delibera     td,
           proposte_delibera p
     where d.id_delibera = p_id_delibera
       and p.id_proposta_delibera = d.id_proposta_delibera
       and td.id_tipo_delibera = p.id_tipo_delibera;

    d_impostazione := impostazioni_pkg.get_impostazione('VIS_GESTIONE_PUBBLCAZIONE_ALLEGATI',
                                                        p_ente);

    if (d_impostazione = 'N' or v_pubblica_allegati = 'Y' or
       (trunc(v_data_pubblicazione_dal) <= trunc(sysdate) and
       trunc(v_data_pubblicazione_al) >= trunc(sysdate))) then
      return 1;
    end if;

    return 0;
  end allegatiDeliberaVisibili;

  function cons_get_codice_aoo(p_ente varchar2) return varchar2
   is
    d_codice_aoo varchar2(255) := null;
  begin
    begin
      select codice_aoo
        into d_codice_aoo
        from so4_aoo
       where codice_amministrazione = p_ente
         and codice_ipa is not null
         and rownum = 1
       order by progr_aoo;
    exception
      when no_data_found then
        d_codice_aoo := null;
    end;

    return d_codice_aoo;
  end cons_get_codice_aoo;

  function cons_get_descrizione_aoo(p_ente varchar2) return varchar2
   is
    d_descrizione_aoo varchar2(255) := null;
  begin
    begin
      select descrizione
        into d_descrizione_aoo
        from so4_aoo
       where codice_amministrazione = p_ente
         and codice_ipa is not null
         and rownum = 1
       order by progr_aoo;
    exception
      when no_data_found then
        d_descrizione_aoo := null;
    end;

    return d_descrizione_aoo;
  end cons_get_descrizione_aoo;

  function cons_get_denominazione_amm(p_ente varchar2) return varchar2
   is
    d_denominazione_amm varchar2(255) := null;
  begin
    begin
      select DESCRIZIONE
        into d_denominazione_amm
        from ad4_enti
       where ente = p_ente
         and rownum = 1;
    exception
      when no_data_found then
        d_denominazione_amm := null;
    end;

    return d_denominazione_amm;
  end cons_get_denominazione_amm;

  function cons_indice_allegati_determina(p_id_determina number)
    return varchar2
   is
    d_indice varchar2(4000) := null;
  begin
    begin
      for al in (select *
                   from allegati alle
                  where alle.id_determina = p_id_determina
                    and alle.valido = 'Y'
                  order by sequenza) loop
        d_indice := d_indice || al.id_allegato || ' - ' || al.titolo || '#';
      end loop;
    end;

    d_indice := substr(d_indice,
                       0,
                       length(d_indice) - 1);

    return d_indice;
  end cons_indice_allegati_determina;

  function cons_indice_allegati_delibera
  (
    p_id_delibera          number,
    p_id_proposta_delibera number
  ) return varchar2
   is
    d_indice varchar2(4000) := null;
  begin
    begin
      for al in (select *
                   from allegati alle
                  where (alle.id_delibera = p_id_delibera or
                        alle.id_proposta_delibera = p_id_proposta_delibera)
                    and alle.valido = 'Y'
                  order by sequenza) loop
        d_indice := d_indice || al.id_allegato || ' - ' || al.titolo || '#';
      end loop;
    end;

    d_indice := substr(d_indice,
                       0,
                       length(d_indice) - 1);

    return d_indice;
  end cons_indice_allegati_delibera;

  function oggetto_coll_visibile_seduta
  (
    p_id_seduta         number,
    p_id_oggetto_seduta number
  ) return number
   is
    d_num                  number;
    d_visibile             number;
    d_id_proposta_delibera number;
  begin
    select id_proposta_delibera
      into d_id_proposta_delibera
      from odg_oggetti_seduta
     where id_oggetto_seduta = p_id_oggetto_seduta;

    select count(1)
      into d_num
      from odg_oggetti_seduta
     where id_seduta = p_id_seduta
       and id_proposta_delibera = d_id_proposta_delibera;

    if d_num = 0 then
      d_visibile := 1;
    else
      d_visibile := 0;
    end if;

    return d_visibile;
  end oggetto_coll_visibile_seduta;

  function elenco_allegati_determina(p_id_determina number) return clob
   is
    d_clob             clob := empty_clob();
    d_stringa          varchar2(32767);
    d_stringa_allegato varchar2(32767);
    d_lunghezza        number;
    d_hash_code        varchar2(500);
    d_data_firma       date;
  begin
    dbms_lob.createTemporary(d_clob,
                             true,
                             dbms_lob.CALL);

    begin
      select f.data_firma
        into d_data_firma
        from firmatari f,
             determine d
       where d.id_determina = p_id_determina
         and f.id_determina = d.id_determina
         and f.firmato = 'N'
         and f.data_firma is not null;
    exception
      when others then
        d_stringa_allegato := '${documentRoot.determina.ALLEGATI_HASH_DEFINITIVO}';
        d_lunghezza        := length(d_stringa_allegato);
        dbms_lob.WRITEAPPEND(d_clob,
                             d_lunghezza,
                             d_stringa_allegato);
        return d_clob;
    end;

    if d_data_firma is null then
      d_stringa_allegato := '${documentRoot.determina.ALLEGATI_HASH_DEFINITIVO}';
      d_lunghezza        := length(d_stringa_allegato);
      dbms_lob.WRITEAPPEND(d_clob,
                           d_lunghezza,
                           d_stringa_allegato);
      return d_clob;
    end if;

    for c in (select a.id_allegato,
                     a.titolo ALLEGATO_TITOLO,
                     a.stato_firma,
                     a.riservato,
                     decode(a.stato_firma,
                            'FIRMATO',
                            'Si',
                            'No') ALLEGATO_FIRMATO,
                     decode(a.riservato,
                            'Y',
                            ' - RISERVATO',
                            '') ALLEGATO_RISERVATO,
                     a.sequenza,
                     a.id_documento_esterno
                from allegati a
               where a.valido = 'Y'
                 and (a.codice is null or a.codice not in ('FRONTESPIZIO'))
                 and a.pubblica_albo = 'Y'
                 and a.id_determina = p_id_determina
               order by a.sequenza asc) loop
      if (d_stringa is not null) then
        d_stringa_allegato := chr(10);
      else
        d_stringa_allegato := null;
      end if;

      d_stringa_allegato := d_stringa_allegato || c.sequenza || '. ' ||
                            c.allegato_titolo || c.allegato_riservato ||
                            chr(10);

      for c1 in (select fa.nome,
                        fa.id_file_esterno
                   from allegati_file af,
                        file_allegati fa
                  where af.id_allegato = c.id_allegato
                    and af.id_file = fa.id_file_allegato
                    and fa.valido = 'Y') loop
        if (d_stringa_allegato is not null) then
          d_lunghezza := length(d_stringa_allegato);
          dbms_lob.WRITEAPPEND(d_clob,
                               d_lunghezza,
                               d_stringa_allegato);
          d_stringa_allegato := null;
        end if;

        if (nvl(c.stato_firma,
                'DA_NON_FIRMARE') in ('DA_NON_FIRMARE')) then
          begin
            select chr(10) || 'Hash: ' || hashcode || chr(10)
              into d_hash_code
              from gdm_impronte_file gif,
                   gdm_oggetti_file  gof
             where gif.id_documento = c.id_documento_esterno
               and gof.id_oggetto_file = c1.id_file_esterno
               and gif.filename = gof.filename;
          exception
            when no_data_found then
              d_hash_code := '';
          end;
        else
          d_hash_code := '';
        end if;

        d_stringa   := 'Nome file: ' || c1.nome || d_hash_code || chr(10);
        d_lunghezza := length(d_stringa);
        dbms_lob.WRITEAPPEND(d_clob,
                             d_lunghezza,
                             d_stringa);
      end loop;
    end loop;

    return d_clob;
  end elenco_allegati_determina;

  function elenco_allegati_delibera(p_id_delibera number) return clob
   is
    d_clob             clob := empty_clob();
    d_stringa          varchar2(32767);
    d_stringa_allegato varchar2(32767);
    d_lunghezza        number;
    d_hash_code        varchar2(500);
    d_data_firma       date;
  begin
    dbms_lob.createTemporary(d_clob,
                             true,
                             dbms_lob.CALL);

    begin
      select f.data_firma
        into d_data_firma
        from firmatari f,
             delibere  d
       where d.id_delibera = p_id_delibera
         and f.id_delibera = d.id_delibera
         and f.firmato = 'N'
         and f.data_firma is not null;
    exception
      when others then
        d_stringa_allegato := '${documentRoot.delibera.ALLEGATI_HASH_DEFINITIVO}';
        d_lunghezza        := length(d_stringa_allegato);
        dbms_lob.WRITEAPPEND(d_clob,
                             d_lunghezza,
                             d_stringa_allegato);
        return d_clob;
    end;

    if d_data_firma is null then
      d_stringa_allegato := '${documentRoot.delibera.ALLEGATI_HASH_DEFINITIVO}';
      d_lunghezza        := length(d_stringa_allegato);
      dbms_lob.WRITEAPPEND(d_clob,
                           d_lunghezza,
                           d_stringa_allegato);
      return d_clob;
    end if;

    for c in (select a.id_allegato,
                     a.titolo ALLEGATO_TITOLO,
                     a.stato_firma,
                     a.riservato,
                     decode(a.stato_firma,
                            'FIRMATO',
                            'Si',
                            'No') ALLEGATO_FIRMATO,
                     decode(a.riservato,
                            'Y',
                            ' - RISERVATO',
                            '') ALLEGATO_RISERVATO,
                     a.sequenza,
                     a.id_documento_esterno
                from allegati a
               where a.valido = 'Y'
                 and (a.codice is null or a.codice not in ('FRONTESPIZIO'))
                 and a.pubblica_albo = 'Y'
                 and a.id_delibera = p_id_delibera
               order by a.sequenza asc) loop
      if (d_stringa is not null) then
        d_stringa_allegato := chr(10);
      else
        d_stringa_allegato := null;
      end if;

      d_stringa_allegato := d_stringa_allegato || c.sequenza || '. ' ||
                            c.allegato_titolo || c.allegato_riservato ||
                            chr(10);

      for c1 in (select fa.nome,
                        fa.id_file_esterno
                   from allegati_file af,
                        file_allegati fa
                  where af.id_allegato = c.id_allegato
                    and af.id_file = fa.id_file_allegato
                    and fa.valido = 'Y') loop
        if (d_stringa_allegato is not null) then
          d_lunghezza := length(d_stringa_allegato);
          dbms_lob.WRITEAPPEND(d_clob,
                               d_lunghezza,
                               d_stringa_allegato);
          d_stringa_allegato := null;
        end if;

        if (nvl(c.stato_firma,
                'DA_NON_FIRMARE') in ('DA_NON_FIRMARE')) then
          begin
            select chr(10) || 'Hash: ' || hashcode || chr(10)
              into d_hash_code
              from gdm_impronte_file gif,
                   gdm_oggetti_file  gof
             where gif.id_documento = c.id_documento_esterno
               and gof.id_oggetto_file = c1.id_file_esterno
               and gif.filename = gof.filename;
          exception
            when no_data_found then
              d_hash_code := '';
          end;

        else
          d_hash_code := '';
        end if;

        d_stringa   := 'Nome file: ' || c1.nome || d_hash_code || chr(10);
        d_lunghezza := length(d_stringa);
        dbms_lob.WRITEAPPEND(d_clob,
                             d_lunghezza,
                             d_stringa);
      end loop;
    end loop;

    return d_clob;
  end elenco_allegati_delibera;

  function get_cig_determina
  (
    p_id_determina number,
    p_ente         varchar2
  ) return varchar2
   is
    d_cig         varchar2(255);
    d_contabilita varchar2(255);
  begin
    begin

      d_contabilita := impostazioni_pkg.get_impostazione('CONTABILITA',
                                                         p_ente);

      if (d_contabilita = 'integrazioneContabilitaJSerfin') then
        select (cig)
          into d_cig
          from (select cig
                  from movimenti_contabili
                 where id_documento = p_id_determina
                   and tipo_documento = 'DETERMINA'
                   and cig is not null
                 order by data_ins asc)
         where rownum = 1;
      else
        select cig
          into d_cig
          from determine
         where id_determina = p_id_determina;
      end if;
    exception
      when others then
        d_cig := '';
    end;
    return d_cig;
  end get_cig_determina;

  function get_elenco_cig_determina
  (
    p_id_determina number,
    p_ente         varchar2
  ) return varchar2
   is
    d_cig         varchar2(255);
    d_contabilita varchar2(255);
  begin
    begin
      d_contabilita := impostazioni_pkg.get_impostazione('CONTABILITA',
                                                         p_ente);

      if (d_contabilita = 'integrazioneContabilitaJSerfin') then
        d_cig := null;

        for c in (select cig
                    from movimenti_contabili
                   where id_documento = p_id_determina
                     and tipo_documento = 'DETERMINA'
                     and cig is not null
                   order by cig asc) loop
          if d_cig is null then
            d_cig := c.cig;
          else
            d_cig := d_cig || ', ' || c.cig;
          end if;
        end loop;
      else
        select cig
          into d_cig
          from determine
         where id_determina = p_id_determina;
      end if;
    exception
      when others then
        d_cig := '';
    end;

    return d_cig;
  end get_elenco_cig_determina;

  function get_numero_presenza_seduta
  (
    p_id_commissione      number,
    p_data_inizio_periodo varchar2,
    p_data_fine_periodo   varchar2,
    p_ni                  number,
    p_ente                varchar2
  ) return number
   is
    d_numero                 number := 0;
    d_calcolo_gettone_seduta varchar2(255);
    d_presenza_giornaliera   varchar2(255);
    d_numero_seduta          number;
  begin
    begin

      d_calcolo_gettone_seduta := impostazioni_pkg.get_impostazione('ODG_CALCOLO_GETTONE_PER_SEDUTA',
                                                                    p_ente);

      if d_calcolo_gettone_seduta = 'Y' then
        for c_seduta in (select distinct s.id_seduta,
                                         c.gettone_presenza_giornaliera
                           from odg_sedute                 s,
                                odg_oggetti_seduta         os,
                                odg_sedute_partecipanti    sp,
                                odg_oggetti_partecipanti   op,
                                odg_commissioni_componenti cc,
                                odg_commissioni            c
                          where os.id_seduta = s.id_seduta
                            and op.id_oggetto_seduta = os.id_oggetto_seduta
                            and sp.id_seduta_partecipante =
                                op.id_seduta_partecipante
                            and s.id_commissione = c.id_commissione
                            and cc.id_commissione_componente(+) =
                                sp.id_commissione_componente
                            and s.valido = 'Y'
                            and op.presente = 'Y'
                            and (op.ruolo_partecipante is null or
                                nvl(sp.ruolo_partecipante,
                                     'XXX') <> 'I')
                            and (p_id_commissione < 0 or
                                s.id_commissione = p_id_commissione)
                            and trunc(s.data_inizio_seduta) >=
                                to_date(p_data_inizio_periodo,
                                        'dd/mm/yyyy')
                            and trunc(s.data_inizio_seduta) <=
                                to_date(p_data_fine_periodo,
                                        'dd/mm/yyyy')
                            and (cc.ni_componente = p_ni or
                                sp.ni_componente_esterno = p_ni)) loop

          if (c_seduta.gettone_presenza_giornaliera = 'Y') then
            select count(1)
              into d_numero_seduta
              from (select distinct os.data_discussione
                      from odg_sedute                 s,
                           odg_oggetti_seduta         os,
                           odg_sedute_partecipanti    sp,
                           odg_oggetti_partecipanti   op,
                           odg_commissioni_componenti cc
                     where s.id_seduta = c_seduta.id_seduta
                       and os.id_seduta = s.id_seduta
                       and op.id_oggetto_seduta = os.id_oggetto_seduta
                       and sp.id_seduta_partecipante =
                           op.id_seduta_partecipante
                       and cc.id_commissione_componente(+) =
                           sp.id_commissione_componente
                       and s.valido = 'Y'
                       and op.presente = 'Y'
                       and (op.ruolo_partecipante is null or
                           nvl(sp.ruolo_partecipante,
                                'XXX') <> 'I')
                       and (p_id_commissione < 0 or
                           s.id_commissione = p_id_commissione)
                       and trunc(s.data_inizio_seduta) >=
                           to_date(p_data_inizio_periodo,
                                   'dd/mm/yyyy')
                       and trunc(s.data_inizio_seduta) <=
                           to_date(p_data_fine_periodo,
                                   'dd/mm/yyyy')
                       and (cc.ni_componente = p_ni or
                           sp.ni_componente_esterno = p_ni));

            d_numero := d_numero + d_numero_seduta;
          else
            select count(1)
              into d_numero_seduta
              from (select distinct s.data_inizio_seduta
                      from odg_sedute                 s,
                           odg_oggetti_seduta         os,
                           odg_sedute_partecipanti    sp,
                           odg_oggetti_partecipanti   op,
                           odg_commissioni_componenti cc
                     where s.id_seduta = c_seduta.id_seduta
                       and os.id_seduta = s.id_seduta
                       and op.id_oggetto_seduta = os.id_oggetto_seduta
                       and sp.id_seduta_partecipante =
                           op.id_seduta_partecipante
                       and cc.id_commissione_componente(+) =
                           sp.id_commissione_componente
                       and s.valido = 'Y'
                       and op.presente = 'Y'
                       and (op.ruolo_partecipante is null or
                           nvl(sp.ruolo_partecipante,
                                'XXX') <> 'I')
                       and (p_id_commissione < 0 or
                           s.id_commissione = p_id_commissione)
                       and trunc(s.data_inizio_seduta) >=
                           to_date(p_data_inizio_periodo,
                                   'dd/mm/yyyy')
                       and trunc(s.data_inizio_seduta) <=
                           to_date(p_data_fine_periodo,
                                   'dd/mm/yyyy')
                       and (cc.ni_componente = p_ni or
                           sp.ni_componente_esterno = p_ni));

            d_numero := d_numero + d_numero_seduta;
          end if;
        end loop;
      else
        for c_commissione in (select distinct s.id_commissione,
                                              c.gettone_presenza_giornaliera
                                from odg_sedute                 s,
                                     odg_oggetti_seduta         os,
                                     odg_sedute_partecipanti    sp,
                                     odg_oggetti_partecipanti   op,
                                     odg_commissioni_componenti cc,
                                     odg_commissioni            c
                               where os.id_seduta = s.id_seduta
                                 and op.id_oggetto_seduta =
                                     os.id_oggetto_seduta
                                 and sp.id_seduta_partecipante =
                                     op.id_seduta_partecipante
                                 and s.id_commissione = c.id_commissione
                                 and cc.id_commissione_componente(+) =
                                     sp.id_commissione_componente
                                 and s.valido = 'Y'
                                 and op.presente = 'Y'
                                 and (op.ruolo_partecipante is null or
                                     nvl(sp.ruolo_partecipante,
                                          'XXX') <> 'I')
                                 and (p_id_commissione < 0 or
                                     s.id_commissione = p_id_commissione)
                                 and trunc(s.data_inizio_seduta) >=
                                     to_date(p_data_inizio_periodo,
                                             'dd/mm/yyyy')
                                 and trunc(s.data_inizio_seduta) <=
                                     to_date(p_data_fine_periodo,
                                             'dd/mm/yyyy')
                                 and (cc.ni_componente = p_ni or
                                     sp.ni_componente_esterno = p_ni)) loop

          if (c_commissione.gettone_presenza_giornaliera = 'Y') then
            select count(1)
              into d_numero_seduta
              from (select distinct os.data_discussione
                      from odg_sedute                 s,
                           odg_oggetti_seduta         os,
                           odg_sedute_partecipanti    sp,
                           odg_oggetti_partecipanti   op,
                           odg_commissioni_componenti cc,
                           odg_commissioni            c
                     where s.id_commissione = c_commissione.id_commissione
                       and os.id_seduta = s.id_seduta
                       and op.id_oggetto_seduta = os.id_oggetto_seduta
                       and sp.id_seduta_partecipante =
                           op.id_seduta_partecipante
                       and s.id_commissione = c.id_commissione
                       and cc.id_commissione_componente(+) =
                           sp.id_commissione_componente
                       and s.valido = 'Y'
                       and op.presente = 'Y'
                       and (op.ruolo_partecipante is null or
                           nvl(sp.ruolo_partecipante,
                                'XXX') <> 'I')
                       and (p_id_commissione < 0 or
                           s.id_commissione = p_id_commissione)
                       and trunc(s.data_inizio_seduta) >=
                           to_date(p_data_inizio_periodo,
                                   'dd/mm/yyyy')
                       and trunc(s.data_inizio_seduta) <=
                           to_date(p_data_fine_periodo,
                                   'dd/mm/yyyy')
                       and (cc.ni_componente = p_ni or
                           sp.ni_componente_esterno = p_ni));

            d_numero := d_numero + d_numero_seduta;
          else
            select count(1)
              into d_numero_seduta
              from (select distinct s.data_inizio_seduta
                      from odg_sedute                 s,
                           odg_oggetti_seduta         os,
                           odg_sedute_partecipanti    sp,
                           odg_oggetti_partecipanti   op,
                           odg_commissioni_componenti cc,
                           odg_commissioni            c
                     where s.id_commissione = c_commissione.id_commissione
                       and os.id_seduta = s.id_seduta
                       and op.id_oggetto_seduta = os.id_oggetto_seduta
                       and sp.id_seduta_partecipante =
                           op.id_seduta_partecipante
                       and s.id_commissione = c.id_commissione
                       and cc.id_commissione_componente(+) =
                           sp.id_commissione_componente
                       and s.valido = 'Y'
                       and op.presente = 'Y'
                       and (op.ruolo_partecipante is null or
                           nvl(sp.ruolo_partecipante,
                                'XXX') <> 'I')
                       and (p_id_commissione < 0 or
                           s.id_commissione = p_id_commissione)
                       and trunc(s.data_inizio_seduta) >=
                           to_date(p_data_inizio_periodo,
                                   'dd/mm/yyyy')
                       and trunc(s.data_inizio_seduta) <=
                           to_date(p_data_fine_periodo,
                                   'dd/mm/yyyy')
                       and (cc.ni_componente = p_ni or
                           sp.ni_componente_esterno = p_ni));

            d_numero := d_numero + d_numero_seduta;
          end if;
        end loop;
      end if;
    exception
      when others then
        d_numero := 0;
    end;

    return d_numero;
  end get_numero_presenza_seduta;

  function get_date_presenza_seduta
  (
    p_id_commissione      number,
    p_data_inizio_periodo varchar2,
    p_data_fine_periodo   varchar2,
    p_ni                  number
  ) return varchar2
   is
    d_elenco_date varchar2(4000);
  begin
    for c in (select distinct nvl(os.data_discussione,
                                  s.data_inizio_seduta) data_discussione
                from odg_sedute                 s,
                     odg_oggetti_seduta         os,
                     odg_sedute_partecipanti    sp,
                     odg_oggetti_partecipanti   op,
                     odg_commissioni_componenti cc,
                     odg_commissioni            c
               where os.id_seduta = s.id_seduta
                 and op.id_oggetto_seduta = os.id_oggetto_seduta
                 and sp.id_seduta_partecipante = op.id_seduta_partecipante
                 and s.id_commissione = c.id_commissione
                 and cc.id_commissione_componente(+) =
                     sp.id_commissione_componente
                 and s.valido = 'Y'
                 and op.presente = 'Y'
                 and (op.ruolo_partecipante is null or
                     nvl(sp.ruolo_partecipante,
                          'XXX') <> 'I')
                 and (p_id_commissione < 0 or
                     s.id_commissione = p_id_commissione)
                 and trunc(s.data_inizio_seduta) >=
                     to_date(p_data_inizio_periodo,
                             'dd/mm/yyyy')
                 and trunc(s.data_inizio_seduta) <=
                     to_date(p_data_fine_periodo,
                             'dd/mm/yyyy')
                 and (cc.ni_componente = p_ni or
                     sp.ni_componente_esterno = p_ni)
               order by 1 asc) loop
      if (d_elenco_date is null) then
        d_elenco_date := to_char(c.data_discussione,
                                 'dd/mm/yyyy');
      else
        d_elenco_date := d_elenco_date || ', ' ||
                         to_char(c.data_discussione,
                                 'dd/mm/yyyy');
      end if;
    end loop;

    return d_elenco_date;
  end get_date_presenza_seduta;

  function get_importo_gettone
  (
    p_id_commissione      number,
    p_data_inizio_periodo varchar2,
    p_data_fine_periodo   varchar2,
    p_ni                  number,
    p_ente                varchar2
  ) return number
   is
    d_importo                number := 0;
    d_calcolo_gettone_seduta varchar2(255);
    d_presenza_giornaliera   varchar2(255);
    d_importo_seduta         number;
  begin
    begin

      d_calcolo_gettone_seduta := impostazioni_pkg.get_impostazione('ODG_CALCOLO_GETTONE_PER_SEDUTA',
                                                                    p_ente);

      if d_calcolo_gettone_seduta = 'Y' then
        for c_seduta in (select distinct s.id_seduta,
                                         c.gettone_presenza_giornaliera
                           from odg_sedute                 s,
                                odg_oggetti_seduta         os,
                                odg_sedute_partecipanti    sp,
                                odg_oggetti_partecipanti   op,
                                odg_commissioni_componenti cc,
                                odg_commissioni            c,
                                odg_importi_gettone        ig
                          where os.id_seduta = s.id_seduta
                            and op.id_oggetto_seduta = os.id_oggetto_seduta
                            and sp.id_seduta_partecipante =
                                op.id_seduta_partecipante
                            and s.id_commissione = c.id_commissione
                            and cc.id_commissione_componente(+) =
                                sp.id_commissione_componente
                            and s.valido = 'Y'
                            and op.presente = 'Y'
                            and ig.id_commissione = s.id_commissione
                            and ig.valido = 'Y'
                            and trunc(ig.valido_dal) <=
                                trunc(s.data_inizio_seduta)
                            and (ig.valido_al is null or
                                trunc(ig.valido_al) >=
                                trunc(s.data_inizio_seduta))
                            and (op.ruolo_partecipante is null or
                                nvl(sp.ruolo_partecipante,
                                     'XXX') <> 'I')
                            and (p_id_commissione < 0 or
                                s.id_commissione = p_id_commissione)
                            and trunc(s.data_inizio_seduta) >=
                                to_date(p_data_inizio_periodo,
                                        'dd/mm/yyyy')
                            and trunc(s.data_inizio_seduta) <=
                                to_date(p_data_fine_periodo,
                                        'dd/mm/yyyy')
                            and (cc.ni_componente = p_ni or
                                sp.ni_componente_esterno = p_ni)) loop

          if (c_seduta.gettone_presenza_giornaliera = 'Y') then
            select sum(importo)
              into d_importo_seduta
              from (select distinct ig.importo,
                                    os.data_discussione
                      from odg_sedute                 s,
                           odg_oggetti_seduta         os,
                           odg_sedute_partecipanti    sp,
                           odg_oggetti_partecipanti   op,
                           odg_importi_gettone        ig,
                           odg_commissioni_componenti cc
                     where s.id_seduta = c_seduta.id_seduta
                       and os.id_seduta = s.id_seduta
                       and op.id_oggetto_seduta = os.id_oggetto_seduta
                       and sp.id_seduta_partecipante =
                           op.id_seduta_partecipante
                       and cc.id_commissione_componente(+) =
                           sp.id_commissione_componente
                       and s.valido = 'Y'
                       and op.presente = 'Y'
                       and ig.id_commissione = s.id_commissione
                       and ig.valido = 'Y'
                       and trunc(ig.valido_dal) <=
                           trunc(s.data_inizio_seduta)
                       and (ig.valido_al is null or
                           trunc(ig.valido_al) >=
                           trunc(s.data_inizio_seduta))
                       and (op.ruolo_partecipante is null or
                           nvl(sp.ruolo_partecipante,
                                'XXX') <> 'I')
                       and (p_id_commissione < 0 or
                           s.id_commissione = p_id_commissione)
                       and trunc(s.data_inizio_seduta) >=
                           to_date(p_data_inizio_periodo,
                                   'dd/mm/yyyy')
                       and trunc(s.data_inizio_seduta) <=
                           to_date(p_data_fine_periodo,
                                   'dd/mm/yyyy')
                       and (cc.ni_componente = p_ni or
                           sp.ni_componente_esterno = p_ni));

            d_importo := d_importo + d_importo_seduta;
          else
            select sum(importo)
              into d_importo_seduta
              from (select distinct ig.importo,
                                    s.data_inizio_seduta
                      from odg_sedute                 s,
                           odg_oggetti_seduta         os,
                           odg_sedute_partecipanti    sp,
                           odg_oggetti_partecipanti   op,
                           odg_importi_gettone        ig,
                           odg_commissioni_componenti cc
                     where s.id_seduta = c_seduta.id_seduta
                       and os.id_seduta = s.id_seduta
                       and op.id_oggetto_seduta = os.id_oggetto_seduta
                       and sp.id_seduta_partecipante =
                           op.id_seduta_partecipante
                       and cc.id_commissione_componente(+) =
                           sp.id_commissione_componente
                       and s.valido = 'Y'
                       and op.presente = 'Y'
                       and ig.id_commissione = s.id_commissione
                       and ig.valido = 'Y'
                       and trunc(ig.valido_dal) <=
                           trunc(s.data_inizio_seduta)
                       and (ig.valido_al is null or
                           trunc(ig.valido_al) >=
                           trunc(s.data_inizio_seduta))
                       and (op.ruolo_partecipante is null or
                           nvl(sp.ruolo_partecipante,
                                'XXX') <> 'I')
                       and (p_id_commissione < 0 or
                           s.id_commissione = p_id_commissione)
                       and trunc(s.data_inizio_seduta) >=
                           to_date(p_data_inizio_periodo,
                                   'dd/mm/yyyy')
                       and trunc(s.data_inizio_seduta) <=
                           to_date(p_data_fine_periodo,
                                   'dd/mm/yyyy')
                       and (cc.ni_componente = p_ni or
                           sp.ni_componente_esterno = p_ni));

            d_importo := d_importo + d_importo_seduta;
          end if;
        end loop;
      else
        for c_commissione in (select distinct s.id_commissione,
                                              c.gettone_presenza_giornaliera
                                from odg_sedute                 s,
                                     odg_oggetti_seduta         os,
                                     odg_sedute_partecipanti    sp,
                                     odg_oggetti_partecipanti   op,
                                     odg_commissioni_componenti cc,
                                     odg_commissioni            c,
                                     odg_importi_gettone        ig
                               where os.id_seduta = s.id_seduta
                                 and op.id_oggetto_seduta =
                                     os.id_oggetto_seduta
                                 and sp.id_seduta_partecipante =
                                     op.id_seduta_partecipante
                                 and s.id_commissione = c.id_commissione
                                 and cc.id_commissione_componente(+) =
                                     sp.id_commissione_componente
                                 and s.valido = 'Y'
                                 and op.presente = 'Y'
                                 and ig.id_commissione = s.id_commissione
                                 and ig.valido = 'Y'
                                 and trunc(ig.valido_dal) <=
                                     trunc(s.data_inizio_seduta)
                                 and (ig.valido_al is null or
                                     trunc(ig.valido_al) >=
                                     trunc(s.data_inizio_seduta))
                                 and (op.ruolo_partecipante is null or
                                     nvl(sp.ruolo_partecipante,
                                          'XXX') <> 'I')
                                 and (p_id_commissione < 0 or
                                     s.id_commissione = p_id_commissione)
                                 and trunc(s.data_inizio_seduta) >=
                                     to_date(p_data_inizio_periodo,
                                             'dd/mm/yyyy')
                                 and trunc(s.data_inizio_seduta) <=
                                     to_date(p_data_fine_periodo,
                                             'dd/mm/yyyy')
                                 and (cc.ni_componente = p_ni or
                                     sp.ni_componente_esterno = p_ni)) loop

          if (c_commissione.gettone_presenza_giornaliera = 'Y') then
            select sum(importo)
              into d_importo_seduta
              from (select distinct ig.importo,
                                    os.data_discussione
                      from odg_sedute                 s,
                           odg_oggetti_seduta         os,
                           odg_sedute_partecipanti    sp,
                           odg_oggetti_partecipanti   op,
                           odg_importi_gettone        ig,
                           odg_commissioni_componenti cc,
                           odg_commissioni            c
                     where s.id_commissione = c_commissione.id_commissione
                       and os.id_seduta = s.id_seduta
                       and op.id_oggetto_seduta = os.id_oggetto_seduta
                       and sp.id_seduta_partecipante =
                           op.id_seduta_partecipante
                       and s.id_commissione = c.id_commissione
                       and cc.id_commissione_componente(+) =
                           sp.id_commissione_componente
                       and s.valido = 'Y'
                       and op.presente = 'Y'
                       and ig.id_commissione = s.id_commissione
                       and ig.valido = 'Y'
                       and trunc(ig.valido_dal) <=
                           trunc(s.data_inizio_seduta)
                       and (ig.valido_al is null or
                           trunc(ig.valido_al) >=
                           trunc(s.data_inizio_seduta))
                       and (op.ruolo_partecipante is null or
                           nvl(sp.ruolo_partecipante,
                                'XXX') <> 'I')
                       and (p_id_commissione < 0 or
                           s.id_commissione = p_id_commissione)
                       and trunc(s.data_inizio_seduta) >=
                           to_date(p_data_inizio_periodo,
                                   'dd/mm/yyyy')
                       and trunc(s.data_inizio_seduta) <=
                           to_date(p_data_fine_periodo,
                                   'dd/mm/yyyy')
                       and (cc.ni_componente = p_ni or
                           sp.ni_componente_esterno = p_ni));

            d_importo := d_importo + d_importo_seduta;
          else
            select sum(importo)
              into d_importo_seduta
              from (select distinct ig.importo,
                                    s.data_inizio_seduta
                      from odg_sedute                 s,
                           odg_oggetti_seduta         os,
                           odg_sedute_partecipanti    sp,
                           odg_oggetti_partecipanti   op,
                           odg_importi_gettone        ig,
                           odg_commissioni_componenti cc,
                           odg_commissioni            c
                     where s.id_commissione = c_commissione.id_commissione
                       and os.id_seduta = s.id_seduta
                       and op.id_oggetto_seduta = os.id_oggetto_seduta
                       and sp.id_seduta_partecipante =
                           op.id_seduta_partecipante
                       and s.id_commissione = c.id_commissione
                       and cc.id_commissione_componente(+) =
                           sp.id_commissione_componente
                       and s.valido = 'Y'
                       and op.presente = 'Y'
                       and ig.id_commissione = s.id_commissione
                       and ig.valido = 'Y'
                       and trunc(ig.valido_dal) <=
                           trunc(s.data_inizio_seduta)
                       and (ig.valido_al is null or
                           trunc(ig.valido_al) >=
                           trunc(s.data_inizio_seduta))
                       and (op.ruolo_partecipante is null or
                           nvl(sp.ruolo_partecipante,
                                'XXX') <> 'I')
                       and (p_id_commissione < 0 or
                           s.id_commissione = p_id_commissione)
                       and trunc(s.data_inizio_seduta) >=
                           to_date(p_data_inizio_periodo,
                                   'dd/mm/yyyy')
                       and trunc(s.data_inizio_seduta) <=
                           to_date(p_data_fine_periodo,
                                   'dd/mm/yyyy')
                       and (cc.ni_componente = p_ni or
                           sp.ni_componente_esterno = p_ni));

            d_importo := d_importo + d_importo_seduta;
          end if;
        end loop;
      end if;
    exception
      when others then
        d_importo := 0;
    end;

    return d_importo;
  end get_importo_gettone;

  procedure rebuid_diz_contabile
  (
    p_tipo   varchar2,
    p_ente   varchar2,
    p_utente varchar2,
    dati     clob
  )
   is
    d_codice      varchar2(255);
    d_descrizione varchar2(4000);
  begin
    delete from dizionari_contabili
     where tipo = p_tipo;

    for cur0 in (SELECT extractvalue(column_value,
                                     '//dizionario') dizionario
                   FROM TABLE(XMLSequence(XMLTYPE(dati).extract('//dizionario'))) t) loop
      begin
        SELECT SUBSTR(cur0.dizionario,
                      1,
                      Instr(cur0.dizionario,
                            '###',
                            -1,
                            1) - 1),
               SUBSTR(cur0.dizionario,
                      Instr(cur0.dizionario,
                            '###',
                            -1,
                            1) + 3)
          into d_codice,
               d_descrizione
          FROM dual;

        INSERT INTO dizionari_contabili
          (tipo,
           codice,
           descrizione,
           valido,
           sequenza,
           ente,
           data_ins,
           utente_ins,
           data_upd,
           utente_upd)
          SELECT p_tipo,
                 d_codice,
                 d_descrizione,
                 'Y',
                 1,
                 p_ente,
                 sysdate,
                 p_utente,
                 sysdate,
                 p_utente
            FROM DUAL;

      exception
        when others then
          null;
      end;
    end loop;

  end rebuid_diz_contabile;

  procedure set_servizio_proponente
  (
    p_tipo_documento in varchar2
  ) is
    v_id           number;
    v_esiste       number;
    v_unita_progr  number;
    v_unita_dal    date;
    v_unita_ottica varchar2(50);

    v_cursor sys_refcursor;

    type t_unita_rec is record(
      progr_unita_organizzativa number,
      dal                       date);

    v_rec t_unita_rec;

    function calcola_suddivisione_progr
    (
      p_suddivisione in varchar2,
      p_ente         in number,
      p_unita_progr  in number,
      p_unita_dal    in date,
      p_ottica       in varchar2
    ) return sys_refcursor is
      d_id_suddivisione    number;
      d_suddivisione_progr number;
    begin
      select id_suddivisione
        into d_id_suddivisione
        from so4_v_suddivisioni_struttura
       where codice = impostazioni_pkg.get_impostazione(p_suddivisione,
                                                        p_ente)
         and ottica = p_ottica;

      d_suddivisione_progr := so4_util.get_area_unita(d_id_suddivisione,
                                                      p_unita_progr,
                                                      p_unita_dal,
                                                      p_ottica);

      return so4_util.get_unita_valida(p_progr_unor => d_suddivisione_progr,
                                       p_data       => p_unita_dal,
                                       p_ottica     => p_ottica);

    exception
      when no_data_found then
        return null;
    end calcola_suddivisione_progr;

  begin
    select count(*)
      into v_esiste
      from tipi_soggetti
     where codice = 'UO_SERVIZIO';

    if v_esiste = 0 then
       raise_application_error(-20999, 'Tipo soggetto uo_servizio inesistente nel dizionario');
    end if;

    /* =========================
    delibera
    ========================= */
    if p_tipo_documento = 'DELIBERA' then
      for r_prop in (select d.id_delibera,
                            d.ente,
                            ds.unita_progr,
                            ds.unita_dal,
                            ds.unita_ottica
                       from delibere_soggetti ds,
                            delibere          d
                      where ds.tipo_soggetto = 'UO_PROPONENTE'
                        and ds.attivo = 'Y'
                        and ds.id_delibera = d.id_delibera) loop
        /* ===== calcolo servizio ===== */
        begin
          v_unita_progr  := r_prop.unita_progr;
          v_unita_dal    := r_prop.unita_dal;
          v_unita_ottica := r_prop.unita_ottica;

          v_cursor := calcola_suddivisione_progr('SO4_SUDDIVISIONE_SERVIZIO',
                                                 r_prop.ente,
                                                 r_prop.unita_progr,
                                                 r_prop.unita_dal,
                                                 r_prop.unita_ottica);

          if v_cursor is not null then
            fetch v_cursor
              into v_rec;

            if v_cursor%found then
              v_unita_progr := v_rec.progr_unita_organizzativa;
              v_unita_dal   := v_rec.dal;
            end if;

            close v_cursor;
          end if;

        exception
          when invalid_cursor then
            null;

          when others then
            null;
        end;

        select count(*)
          into v_esiste
          from delibere_soggetti
         where id_delibera = r_prop.id_delibera
           and tipo_soggetto = 'UO_SERVIZIO'
           and attivo = 'Y'
           and unita_progr = v_unita_progr
           and unita_dal = v_unita_dal
           and unita_ottica = v_unita_ottica;

        if v_esiste = 0 then
          select hibernate_sequence.nextval into v_id from dual;

          insert into delibere_soggetti
            (id_delibera_soggetto,
             id_delibera,
             tipo_soggetto,
             utente,
             unita_progr,
             unita_dal,
             unita_ottica,
             sequenza,
             version,
             attivo)
          values
            (v_id,
             r_prop.id_delibera,
             'UO_SERVIZIO',
             null,
             v_unita_progr,
             v_unita_dal,
             v_unita_ottica,
             0,
             0,
             'Y');
        end if;

      end loop;

      /* =========================
      proposta delibera
      ========================= */
    elsif p_tipo_documento = 'PROPOSTA_DELIBERA' then
      for r_prop in (select p.id_proposta_delibera,
                            p.ente,
                            ps.unita_progr,
                            ps.unita_dal,
                            ps.unita_ottica
                       from proposte_delibera_soggetti ps,
                            proposte_delibera          p
                      where ps.tipo_soggetto = 'UO_PROPONENTE'
                        and ps.attivo = 'Y'
                        and ps.id_proposta_delibera = p.id_proposta_delibera) loop
        /* ===== calcolo servizio ===== */
        begin
          v_unita_progr  := r_prop.unita_progr;
          v_unita_dal    := r_prop.unita_dal;
          v_unita_ottica := r_prop.unita_ottica;

          v_cursor := calcola_suddivisione_progr('SO4_SUDDIVISIONE_SERVIZIO',
                                                 r_prop.ente,
                                                 r_prop.unita_progr,
                                                 r_prop.unita_dal,
                                                 r_prop.unita_ottica);

          if v_cursor is not null then
            fetch v_cursor
              into v_rec;

            if v_cursor%found then
              v_unita_progr := v_rec.progr_unita_organizzativa;
              v_unita_dal   := v_rec.dal;
            end if;

            close v_cursor;
          end if;

        exception
          when invalid_cursor then
            null;

          when others then
            null;
        end;

        select count(*)
          into v_esiste
          from proposte_delibera_soggetti
         where id_proposta_delibera = r_prop.id_proposta_delibera
           and tipo_soggetto = 'UO_SERVIZIO'
           and attivo = 'Y'
           and unita_progr = v_unita_progr
           and unita_dal = v_unita_dal
           and unita_ottica = v_unita_ottica;

        if v_esiste = 0 then
          select hibernate_sequence.nextval into v_id from dual;

          insert into proposte_delibera_soggetti
            (id_proposta_delibera_soggetto,
             id_proposta_delibera,
             tipo_soggetto,
             utente,
             unita_progr,
             unita_dal,
             unita_ottica,
             sequenza,
             version,
             attivo)
          values
            (v_id,
             r_prop.id_proposta_delibera,
             'UO_SERVIZIO',
             null,
             v_unita_progr,
             v_unita_dal,
             v_unita_ottica,
             0,
             0,
             'Y');
        end if;

      end loop;

      /* =========================
      determina
      ========================= */
    elsif p_tipo_documento = 'DETERMINA' then
      for r_prop in (select d.id_determina,
                            d.ente,
                            ds.unita_progr,
                            ds.unita_dal,
                            ds.unita_ottica
                       from determine_soggetti ds,
                            determine          d
                      where tipo_soggetto = 'UO_PROPONENTE'
                        and attivo = 'Y'
                        and ds.id_determina = d.id_determina) loop
        /* ===== calcolo servizio ===== */
        begin
          v_unita_progr  := r_prop.unita_progr;
          v_unita_dal    := r_prop.unita_dal;
          v_unita_ottica := r_prop.unita_ottica;

          v_cursor := calcola_suddivisione_progr('SO4_SUDDIVISIONE_SERVIZIO',
                                                 r_prop.ente,
                                                 r_prop.unita_progr,
                                                 r_prop.unita_dal,
                                                 r_prop.unita_ottica);

          if v_cursor is not null then
            fetch v_cursor
              into v_rec;

            if v_cursor%found then
              v_unita_progr := v_rec.progr_unita_organizzativa;
              v_unita_dal   := v_rec.dal;
            end if;

            close v_cursor;
          end if;

        exception
          when invalid_cursor then
            null;

          when others then
            null;
        end;

        select count(*)
          into v_esiste
          from determine_soggetti
         where id_determina = r_prop.id_determina
           and attivo = 'Y'
           and tipo_soggetto = 'UO_SERVIZIO'
           and unita_progr = v_unita_progr
           and unita_dal = v_unita_dal
           and unita_ottica = v_unita_ottica;

        if v_esiste = 0 then
          select hibernate_sequence.nextval into v_id from dual;

          insert into determine_soggetti
            (id_determina_soggetto,
             id_determina,
             tipo_soggetto,
             utente,
             unita_progr,
             unita_dal,
             unita_ottica,
             sequenza,
             version,
             attivo)
          values
            (v_id,
             r_prop.id_determina,
             'UO_SERVIZIO',
             null,
             v_unita_progr,
             v_unita_dal,
             v_unita_ottica,
             0,
             0,
             'Y');
        end if;

      end loop;

    end if;
  end set_servizio_proponente;
end utility_pkg;
/