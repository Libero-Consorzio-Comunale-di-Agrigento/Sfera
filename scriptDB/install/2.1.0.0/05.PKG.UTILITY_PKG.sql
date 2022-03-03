--liquibase formatted sql
--changeset rdestasio:2.1.0.0_20200221_05

CREATE OR REPLACE package        utility_pkg
is
   type split_tbl is table of varchar2 (32767);


   function get_codice_unita (p_unita_progr number, p_unita_dal date)
      return varchar2;
      
   function get_codice_unita_prop_delibera (p_id_proposta_delibera number)
      return varchar2;

   function get_codice_unita_prop_dete (p_id_determina number)
      return varchar2;

   function get_cognome (p_ni number)
      return varchar2;

   function get_cognome_nome (p_ni number)
      return varchar2;

   function get_data_firma_delibera (p_id_delibera number, p_tipo_soggetto varchar2)
      return date;

   function get_data_firma_par_contabile (p_id_delibera number)
      return date;

   function get_data_firma_visto_contabile (p_id_determina number)
      return date;

   function get_desc_ascendenti_unita (p_codice_uo     varchar2
                                     , p_data          date default null
                                     , p_ottica        varchar2
                                     , p_separatore    varchar2 default null)
      return varchar2;

   function get_esito_parere_contabile (p_id_delibera number)
      return varchar2;

   function get_esito_visto_contabile (p_id_determina number)
      return varchar2;

   function get_firmatario_delibera (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2;

   function get_firmatario_par_contabile (p_id_delibera number)
      return varchar2;

   function get_firmatario_visto_contabile (p_id_determina number)
      return varchar2;

   function get_ni_soggetto (p_utente in varchar2)
      return number;

   function get_nome (p_ni number)
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

   function get_primo_firmatario_certif (p_id_certificato number)
      return varchar2;
      
   function get_soggetto_delibera (p_id_delibera number, p_tipo_soggetto varchar2, p_sequenza number default 0)
      return number;

   function get_primo_firmatario_delibera (p_id_delibera number)
      return varchar2;

   function get_primo_firmatario_determina (p_id_determina number)
      return varchar2;

   function get_primo_firmatario_prop_deli (p_id_proposta_delibera number)
      return varchar2;

   function get_primo_firmatario_visto_par (p_id_visto_parere number)
      return varchar2;

   function get_sogg_notifica_delibera (p_id_delibera number)
      return varchar2;

   function get_sogg_notifica_determina (p_id_determina number)
      return varchar2;

   function get_suddivisione_descrizione (p_unita_progr     number
                                        , p_unita_dal       date
                                        , p_suddivisione    varchar2
                                        , p_ente            varchar2)
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
      
   function get_testo_proposta_delibera (p_id_delibera number)
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
end utility_pkg;

/
CREATE OR REPLACE package body        utility_pkg
is
   function get_codice_unita (p_unita_progr number, p_unita_dal date)
      return varchar2
   /***************************************************************
   Funzione che restituisce il codice di unità su SO4
   ***************************************************************/
   is
      d_codice_desc    varchar2 (255) := null;
   begin
      begin
         d_codice_desc := so4_util.anuo_get_codice_uo (p_unita_progr, p_unita_dal);
      exception
         when no_data_found then
            d_codice_desc := null;
      end;

      return d_codice_desc;
   end get_codice_unita;
   
   function get_codice_unita_prop_delibera (p_id_proposta_delibera number)
      return varchar2
   /***************************************************************
   Funzione che restituisce il codice dell'unità proponente della proposta di delibera
   ***************************************************************/
   is
      d_unita_progr   number (19);
      d_unita_dal     date;
      d_codice_desc    varchar2 (255) := null;
   begin
      begin
         select unita_progr, unita_dal
           into d_unita_progr, d_unita_dal
           from proposte_delibera_soggetti
          where id_proposta_delibera = p_id_proposta_delibera and tipo_soggetto = 'UO_PROPONENTE';

         d_codice_desc := so4_util.anuo_get_codice_uo (d_unita_progr, d_unita_dal);
      exception
         when no_data_found then
            d_codice_desc := null;
      end;

      return d_codice_desc;
   end get_codice_unita_prop_delibera;

   function get_codice_unita_prop_dete (p_id_determina number)
      return varchar2
   /***************************************************************
   Funzione che restituisce il codice dell'unità proponente della determina
   ***************************************************************/
   is
      d_unita_progr   number (19);
      d_unita_dal     date;
      d_codice_desc    varchar2 (255) := null;
   begin
      begin
         select unita_progr, unita_dal
           into d_unita_progr, d_unita_dal
           from determine_soggetti
          where id_determina = p_id_determina and tipo_soggetto = 'UO_PROPONENTE';

         d_codice_desc := so4_util.anuo_get_codice_uo (d_unita_progr, d_unita_dal);
      exception
         when no_data_found then
            d_codice_desc := null;
      end;

      return d_codice_desc;
   end get_codice_unita_prop_dete;

   function get_cognome (p_ni number)
      return varchar2
   is
      /******************************************************************************
       RESTITUISCE IL COGNOME||' '||NOME DEL SOGGETTO LEGGENDO DALLA VISTA AS4_V_SOGGETTI_CORRENTI
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
          where ni = p_ni and rownum = 1;                            -- questo serve per parare i casi (errati) in cui un soggetto

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
   end get_cognome;

   function get_cognome_nome (p_ni number)
      return varchar2
   is
      /******************************************************************************
       RESTITUISCE IL COGNOME||' '||NOME DEL SOGGETTO LEGGENDO DALLA VISTA AS4_V_SOGGETTI_CORRENTI
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
          where ni = p_ni and rownum = 1;                            -- questo serve per parare i casi (errati) in cui un soggetto

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
   end get_cognome_nome;

   function get_data_firma_delibera (p_id_delibera number, p_tipo_soggetto varchar2)
      return date
   /***************************************************************
   Funzione che restituisce la data di firma di una delibera
   ***************************************************************/
   is
      d_data   date;
   begin
      for c
         in (  select data_firma
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
      for c
         in (  select f.data_firma
                 from firmatari f
                    , visti_pareri vp
                    , delibere d
                    , tipi_visto_parere tvp
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
      for c
         in (  select f.data_firma
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

   function get_desc_ascendenti_unita (p_codice_uo     varchar2
                                     , p_data          date default null
                                     , p_ottica        varchar2
                                     , p_separatore    varchar2 default null)
      return varchar2
   is
      /******************************************************************************
       RESTITUISCE LA STRUTTURA DEGLI ASCENDENTI A PARTIRE DALL'UNITA' CON ETICHETTA E SEPARATORE
       ******************************************************************************/

      v_struttura           sys_refcursor;

      d_ottica              varchar2 (18);
      d_separatore          varchar2 (1) := chr (10);
      d_desc_suddivisione   varchar2 (60);
      d_result              varchar2 (32000);

      type v_struttura_t is record
      (
         progr_uo          number (8)
       , codice_uo         varchar2 (50)
       , desc_uo           varchar2 (240)
       , dal               date
       , al                date
       , id_suddivisione   number (8)
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
                where ottica = p_ottica and id_suddivisione = v_struttura_row.id_suddivisione;
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
   end get_desc_ascendenti_unita;

   function get_esito_parere_contabile (p_id_delibera number)
      return varchar2
   /***************************************************************
   Funzione che restituisce l'esito del parere contabile di una delibera
   Se ci sono due visti con il campo CONTABILE = Y in tipologia
   allora la funzione ritorna uno dei due esiti (a caso)
   ***************************************************************/
   is
      d_esito   varchar2 (255);
   begin
      for c
         in (select vp.esito
               from visti_pareri vp, delibere d, tipi_visto_parere tvp
              where d.id_delibera = p_id_delibera
                and vp.id_proposta_delibera = d.id_proposta_delibera
                and vp.id_tipologia = tvp.id_tipo_visto_parere
                and tvp.contabile = 'Y')
      loop
         d_esito := c.esito;
         exit;
      end loop;

      return d_esito;
   end get_esito_parere_contabile;

   function get_esito_visto_contabile (p_id_determina number)
      return varchar2
   /***************************************************************
   Funzione che restituisce l'esito del visto contabile di una determina
   Se ci sono due visti con il campo CONTABILE = Y in tipologia
   allora la funzione ritorna uno dei due esiti (a caso)
   ***************************************************************/
   is
      d_esito   varchar2 (255);
   begin
      for c in (select vp.esito
                  from visti_pareri vp, tipi_visto_parere tvp
                 where vp.id_determina = p_id_determina and vp.id_tipologia = tvp.id_tipo_visto_parere and tvp.contabile = 'Y')
      loop
         d_esito := c.esito;
         exit;
      end loop;

      return d_esito;
   end get_esito_visto_contabile;

   function get_firmatario_delibera (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2
   /***************************************************************
   Funzione che restituisce la denominazione di un firmatario di una delibera
   ***************************************************************/
   is
      d_firmatario   varchar2 (4000);
   begin
      for c
         in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
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
      for c
         in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                 from firmatari f
                    , as4_v_soggetti_correnti
                    , visti_pareri vp
                    , delibere d
                    , tipi_visto_parere tvp
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
      for c
         in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                 from firmatari f
                    , as4_v_soggetti_correnti
                    , visti_pareri vp
                    , tipi_visto_parere tvp
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

   function get_ni_soggetto (p_utente in varchar2)
      /******************************************************************************
        RESTITUISCE NI DEL SOGGETTO LEGGENDO DALLA VISTA AS4_V_SOGGETTI_CORRENTI
      ******************************************************************************/
      return number
   is
      d_ni   number;
   begin
      if (p_utente is null) then
         return null;
      end if;

      begin
         select ni
           into d_ni
           from as4_v_soggetti_correnti
          where utente = p_utente and rownum = 1;                    -- questo serve per parare i casi (errati) in cui un soggetto

         -- in anagrafica ha più di un utente collegato:
         -- in tali casi infatti nella vista as4_v_soggetti_correnti
         -- ci sono più righe con stesso ni.

         return d_ni;
      exception
         when no_data_found then
            -- in caso di errore, cioè di ni non trovato, ritorno null in modo che la stampa
            -- comunque funzioni.
            return null;
      end;
   end get_ni_soggetto;

   function get_nome (p_ni number)
      return varchar2
   is
      /******************************************************************************
       RESTITUISCE IL COGNOME||' '||NOME DEL SOGGETTO LEGGENDO DALLA VISTA AS4_V_SOGGETTI_CORRENTI
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
          where ni = p_ni and rownum = 1;                            -- questo serve per parare i casi (errati) in cui un soggetto

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
   end get_nome;

   function get_nominativo_sogg_dete (p_id_determina number, p_tipo_soggetto varchar2)
      return varchar2
   /******************************************************************************
    restituisce il cognome||' '||nome di un soggetto della determina
   ******************************************************************************/
   is
      d_utente       varchar2 (255) := null;
      d_nominativo   varchar2 (255) := null;
   begin
      begin
         select utente
           into d_utente
           from determine_soggetti
          where id_determina = p_id_determina and tipo_soggetto = p_tipo_soggetto;

         d_nominativo := get_cognome_nome (get_ni_soggetto (d_utente));
      exception
         when no_data_found then
            d_nominativo := null;
      end;

      return d_nominativo;
   end get_nominativo_sogg_dete;

   function get_nominativo_sogg_prop_deli (p_id_proposta_delibera number, p_tipo_soggetto varchar2)
      return varchar2
   /******************************************************************************
    restituisce il cognome||' '||nome di un soggetto della proposta di delibera
   ******************************************************************************/
   is
      d_utente       varchar2 (255) := null;
      d_nominativo   varchar2 (255) := null;
   begin
      begin
         select utente
           into d_utente
           from proposte_delibera_soggetti
          where id_proposta_delibera = p_id_proposta_delibera and tipo_soggetto = p_tipo_soggetto;

         d_nominativo := get_cognome_nome (get_ni_soggetto (d_utente));
      exception
         when no_data_found then
            d_nominativo := null;
      end;

      return d_nominativo;
   end get_nominativo_sogg_prop_deli;

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
                   where f.id_certificato = p_id_certificato and f.firmato = 'Y' and f.data_firma is not null
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
                   where f.id_delibera = p_id_delibera and f.firmato = 'Y' and f.data_firma is not null
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
                   where f.id_determina = p_id_determina and f.firmato = 'Y' and f.data_firma is not null
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
                   where f.id_proposta_delibera = p_id_proposta_delibera and f.firmato = 'Y' and f.data_firma is not null
                order by sequenza asc)
      loop
         d_data := c.data_firma;
         exit;
      end loop;

      return d_data;
   end get_prima_data_firma_prop_deli;

   function get_primo_firmatario_certif (p_id_certificato number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la denominazione del primo firmatario di un certificato
   ***************************************************************/
   is
      d_firmatario   varchar2 (4000);
   begin
      for c
         in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                 from firmatari f, as4_v_soggetti_correnti
                where f.id_certificato = p_id_certificato
                  and f.utente_firmatario = as4_v_soggetti_correnti.utente
                  and f.firmato = 'Y'
                  and f.data_firma is not null
             order by sequenza asc)
      loop
         d_firmatario := c.nominativo;
         exit;
      end loop;

      return d_firmatario;
   end get_primo_firmatario_certif;
   
   function get_soggetto_delibera (p_id_delibera number, p_tipo_soggetto varchar2, p_sequenza number default 0)
      return number
   /***************************************************************
   Funzione che restituisce il ni del soggetto richiesto della delibera.
   ***************************************************************/
   is
      d_ni_soggetto number(19);
   begin
      
      for c in (select get_ni_soggetto(utente) ni_soggetto
                  from delibere_soggetti
                 where tipo_soggetto    = p_tipo_soggetto
                   and id_delibera      = p_id_delibera
                   and sequenza         = p_sequenza)
      loop
         d_ni_soggetto := c.ni_soggetto;
         exit;
      end loop;

      return d_ni_soggetto;
   end get_soggetto_delibera;

   function get_primo_firmatario_delibera (p_id_delibera number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la denominazione del primo firmatario di una delibera
   ***************************************************************/
   is
      d_firmatario   varchar2 (4000);
   begin
      for c
         in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                 from firmatari f, as4_v_soggetti_correnti
                where f.id_delibera = p_id_delibera
                  and f.utente_firmatario = as4_v_soggetti_correnti.utente
                  and f.firmato = 'Y'
                  and f.data_firma is not null
             order by sequenza asc)
      loop
         d_firmatario := c.nominativo;
         exit;
      end loop;

      return d_firmatario;
   end get_primo_firmatario_delibera;

   function get_primo_firmatario_determina (p_id_determina number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la denominazione del primo firmatario di una determina
   ***************************************************************/
   is
      d_firmatario   varchar2 (4000);
   begin
      for c
         in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
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

   function get_primo_firmatario_prop_deli (p_id_proposta_delibera number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la denominazione del primo firmatario di una proposta di delibera
   ***************************************************************/
   is
      d_firmatario   varchar2 (4000);
   begin
      for c
         in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                 from firmatari f, as4_v_soggetti_correnti
                where f.id_proposta_delibera = p_id_proposta_delibera
                  and f.utente_firmatario = as4_v_soggetti_correnti.utente
                  and f.firmato = 'Y'
                  and f.data_firma is not null
             order by sequenza asc)
      loop
         d_firmatario := c.nominativo;
         exit;
      end loop;

      return d_firmatario;
   end get_primo_firmatario_prop_deli;

   function get_primo_firmatario_visto_par (p_id_visto_parere number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la denominazione del primo firmatario di un visto parere
   ***************************************************************/
   is
      d_firmatario   varchar2 (4000);
   begin
      for c
         in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                 from firmatari f, as4_v_soggetti_correnti
                where f.id_visto_parere = p_id_visto_parere
                  and f.utente_firmatario = as4_v_soggetti_correnti.utente
                  and f.firmato = 'Y'
                  and f.data_firma is not null
             order by sequenza asc)
      loop
         d_firmatario := c.nominativo;
         exit;
      end loop;

      return d_firmatario;
   end get_primo_firmatario_visto_par;


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
         for c
            in (select avsc.cognome || ' ' || avsc.nome nominativo
                  from destinatari_notifiche dn, as4_v_soggetti_correnti avsc
                 where tipo_destinatario = 'INTERNO' and dn.utente_ad4 = avsc.utente and id_delibera = p_id_delibera
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
                     , so4_v_utenti_ruoli_sogg_uo svurs
                     , as4_v_soggetti_correnti avsc
                     , delibere d
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
                     , so4_v_utenti_ruoli_sogg_uo svurs
                     , as4_v_soggetti_correnti avsc
                     , proposte_delibera pd
                     , delibere d
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
                     , so4_v_utenti_ruoli_sogg_uo svurs
                     , as4_v_soggetti_correnti avsc
                     , delibere d
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
         for c
            in (select avsc.cognome || ' ' || avsc.nome nominativo
                  from destinatari_notifiche dn, as4_v_soggetti_correnti avsc
                 where tipo_destinatario = 'INTERNO' and dn.utente_ad4 = avsc.utente and id_determina = p_id_determina
                union
                select avsc.cognome || ' ' || avsc.nome nominativo
                  from destinatari_notifiche dn
                     , so4_v_utenti_ruoli_sogg_uo svurs
                     , as4_v_soggetti_correnti avsc
                     , determine d
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
                     , so4_v_utenti_ruoli_sogg_uo svurs
                     , as4_v_soggetti_correnti avsc
                     , determine d
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
                                        , p_unita_dal       date
                                        , p_suddivisione    varchar2
                                        , p_ente            varchar2)
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
       where codice = impostazioni_pkg.get_impostazione (p_suddivisione, p_ente) and ottica = d_ottica;

      d_suddivisione_progr :=
         so4_util.get_area_unita (d_id_suddivisione
                                , p_unita_progr
                                , p_unita_dal
                                , d_ottica);

      d_descrizione := get_uo_descrizione (d_suddivisione_progr, p_unita_dal);

      return d_descrizione;
   end get_suddivisione_descrizione;

   function get_suddivisione_determina (p_id_determina number, p_suddivisione varchar2, p_ente varchar2)
      return varchar2
   /***************************************************************
   Funzione che restituisce la descrizione della suddivisione in base al codice richiesto.
   Il calcolo viene fatto a partire dall'unità proponente della determina
   ***************************************************************/
   is
      d_unita_progr    number (19);
      d_unita_dal      date;
      d_suddivisione   varchar2 (255) := null;
   begin
      begin
         select unita_progr, unita_dal
           into d_unita_progr, d_unita_dal
           from determine_soggetti
          where id_determina = p_id_determina and tipo_soggetto = 'UO_PROPONENTE';

         d_suddivisione :=
            utility_pkg.get_suddivisione_descrizione (d_unita_progr
                                                    , d_unita_dal
                                                    , p_suddivisione
                                                    , p_ente);
      exception
         when no_data_found then
            d_suddivisione := null;
      end;

      return d_suddivisione;
   end get_suddivisione_determina;

   function get_suddivisione_prop_delibera (p_id_proposta_delibera number, p_suddivisione varchar2, p_ente varchar2)
      return varchar2
   /***************************************************************
   Funzione che restituisce la descrizione della suddivisione in base al codice richiesto.
   Il calcolo viene fatto a partire dall'unità proponente della proposta di delibera
   ***************************************************************/
   is
      d_unita_progr    number (19);
      d_unita_dal      date;
      d_suddivisione   varchar2 (255) := null;
   begin
      begin
         select unita_progr, unita_dal
           into d_unita_progr, d_unita_dal
           from proposte_delibera_soggetti
          where id_proposta_delibera = p_id_proposta_delibera and tipo_soggetto = 'UO_PROPONENTE';

         d_suddivisione :=
            utility_pkg.get_suddivisione_descrizione (d_unita_progr
                                                    , d_unita_dal
                                                    , p_suddivisione
                                                    , p_ente);
      exception
         when no_data_found then
            d_suddivisione := null;
      end;

      return d_suddivisione;
   end get_suddivisione_prop_delibera;

   function get_suddivisione_uo_determina (p_id_determina number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la descrizione della suddivisione dell'unità proponente di una determina
   ***************************************************************/
   is
      d_suddivisione   varchar2 (4000) := null;
   begin
      begin
         select so4_suddivisioni.descrizione suddivisione
           into d_suddivisione
           from determine d
              , determine_soggetti d_uoprop
              , so4_v_unita_organizzative_pubb so4_unita
              , so4_v_suddivisioni_struttura so4_suddivisioni
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

   function get_suddivisione_uo_prop_deli (p_id_proposta_delibera number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la descrizione della suddivisione dell'unità proponente di una proposta dei delibera
   ***************************************************************/
   is
      d_suddivisione   varchar2 (4000) := null;
   begin
      begin
         select so4_suddivisioni.descrizione suddivisione
           into d_suddivisione
           from proposte_delibera pd
              , proposte_delibera_soggetti pd_uoprop
              , so4_v_unita_organizzative_pubb so4_unita
              , so4_v_suddivisioni_struttura so4_suddivisioni
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

   function get_suddivisione_vistoparere (p_id_visto_parere number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la descrizione della suddivisione dell'unità di un visto / parere
   ***************************************************************/
   is
      d_suddivisione   varchar2 (4000) := null;
   begin
      begin
         select so4_suddivisioni.descrizione suddivisione
           into d_suddivisione
           from visti_pareri vp
              , so4_v_unita_organizzative_pubb so4_unita
              , so4_v_suddivisioni_struttura so4_suddivisioni
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
   
   function get_testo_proposta_delibera (p_id_delibera number)
      /******************************************************************************
     RESTITUISCE IL TESTO DELLA PROPOSTA (BLOB)
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
           where rownum = 1'
            into d_testo_proposta
            using in p_id_delibera, p_id_delibera;
      else
         execute immediate '
            select fa.allegato testo_proposta
              from delibere d, proposte_delibera pd, file_allegati fa
             where d.id_delibera = :p_id_delibera
               and d.id_proposta_delibera = pd.id_proposta_delibera
               and ( (pd.id_file_allegato_testo_odt is not null
                  and pd.id_file_allegato_testo_odt = fa.id_file_allegato)
                  or (pd.id_file_allegato_testo_odt is null
                  and pd.id_file_allegato_testo = fa.id_file_allegato))'
            into d_testo_proposta
            using in p_id_delibera;
      end if;

      return d_testo_proposta;
   end get_testo_proposta_delibera;

   function get_unita_prop_determina (p_id_determina number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la descrizione dell'unità proponente della determina
   ***************************************************************/
   is
      d_unita_progr   number (19);
      d_unita_dal     date;
      d_unita_desc    varchar2 (255) := null;
   begin
      begin
         select unita_progr, unita_dal
           into d_unita_progr, d_unita_dal
           from determine_soggetti
          where id_determina = p_id_determina and tipo_soggetto = 'UO_PROPONENTE';

         d_unita_desc := utility_pkg.get_uo_descrizione (d_unita_progr, d_unita_dal);
      exception
         when no_data_found then
            null;
      end;

      return d_unita_desc;
   end get_unita_prop_determina;

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
      for c
         in (  select get_uo_descrizione (unita_progr, unita_dal) uo_visto
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

   function get_unita_prop_delibera (p_id_proposta_delibera number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la descrizione dell'unità proponente della proposta di delibera
   ***************************************************************/
   is
      d_unita_progr   number (19);
      d_unita_dal     date;
      d_unita_desc    varchar2 (255) := null;
   begin
      begin
         select unita_progr, unita_dal
           into d_unita_progr, d_unita_dal
           from proposte_delibera_soggetti
          where id_proposta_delibera = p_id_proposta_delibera and tipo_soggetto = 'UO_PROPONENTE';

         d_unita_desc := utility_pkg.get_uo_descrizione (d_unita_progr, d_unita_dal);
      exception
         when no_data_found then
            null;
      end;

      return d_unita_desc;
   end get_unita_prop_delibera;

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
                   where vp.id_determina = p_id_determina and vp.id_tipologia = tvp.id_tipo_visto_parere and tvp.contabile = 'Y'
                order by vp.data_upd desc)
      loop
         d_descrizione := c.uo_visto;
         exit;
      end loop;

      return d_descrizione;
   end get_unita_visto_contabile;

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

         d_descrizione :=
            substr (d_descrizione, instr (d_descrizione, '#', -1) + 1, length (d_descrizione) - instr (d_descrizione, '#', -1));
      exception
         when others then
            d_descrizione := '';
      end;

      return d_descrizione;
   end get_uo_padre_descrizione;

   function get_uo_padre_determina (p_id_determina number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la descrizione dell'unità padre dell'unità proponente della determina
   ***************************************************************/
   is
      d_unita_progr    number (19);
      d_unita_ottica   varchar2 (255);
      d_unita_dal      date;
      d_descrizione    varchar2 (4000);
   begin
      begin
         select unita_progr, unita_ottica, unita_dal
           into d_unita_progr, d_unita_ottica, d_unita_dal
           from determine_soggetti
          where id_determina = p_id_determina and tipo_soggetto = 'UO_PROPONENTE';


         d_descrizione := get_uo_padre_descrizione (d_unita_progr, d_unita_dal, d_unita_ottica);
      exception
         when others then
            d_descrizione := '';
      end;

      return d_descrizione;
   end get_uo_padre_determina;
   
   function get_uo_padre_prop_delibera (p_id_proposta_delibera number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la descrizione dell'unità padre dell'unità proponente della proposta di delibera
   ***************************************************************/
   is
      d_unita_progr    number (19);
      d_unita_ottica   varchar2 (255);
      d_unita_dal      date;
      d_descrizione    varchar2 (4000);
   begin
      begin
         select unita_progr, unita_ottica, unita_dal
           into d_unita_progr, d_unita_ottica, d_unita_dal
           from proposte_delibera_soggetti
          where id_proposta_delibera = p_id_proposta_delibera and tipo_soggetto = 'UO_PROPONENTE';


         d_descrizione := get_uo_padre_descrizione (d_unita_progr, d_unita_dal, d_unita_ottica);
      exception
         when others then
            d_descrizione := '';
      end;

      return d_descrizione;
   end get_uo_padre_prop_delibera;

   function get_votanti_oggetto_seduta (p_id_oggetto_seduta number, p_voto varchar2)
      return varchar2
   is
      d_elenco   varchar2 (32767);
   begin
      for c
         in (  select sequenza, cognome_nome
                 from (select occ.sequenza
                            , initcap (get_cognome_nome (nvl (occ.ni_componente, osp.ni_componente_esterno))) cognome_nome
                         from odg_sedute_partecipanti osp
                            , odg_oggetti_partecipanti oop
                            , odg_oggetti_seduta oos
                            , odg_commissioni_componenti occ
                            , odg_voti ov
                        where oos.id_oggetto_seduta = p_id_oggetto_seduta
                          and oos.id_oggetto_seduta = oop.id_oggetto_seduta
                          and osp.id_seduta_partecipante = oop.id_seduta_partecipante
                          and occ.id_commissione_componente(+) = osp.id_commissione_componente
                          and oop.id_voto = ov.id_voto(+)
                          and ( (p_voto is null) or (p_voto is not null and ov.valore = p_voto)))
             order by sequenza, cognome_nome asc)
      loop
         d_elenco := d_elenco || c.cognome_nome || ', ';
      end loop;

      if (d_elenco is not null) then
         d_elenco := substr (d_elenco, 1, length (d_elenco) - 2) || '.';
      end if;

      return d_elenco;
   end get_votanti_oggetto_seduta;

   function giorno_lettere (p_data in date)
      return varchar2
   is
      /******************************************************************************
      RESTITUISCE IL GIORNO DELLA SETTIMANA
     ******************************************************************************/
      d_valore     varchar2 (10);
      dep_giorno   varchar2 (10);
   begin
      select trim (upper (to_char (p_data, 'DAY', 'NLS_DATE_LANGUAGE=italian'))) into dep_giorno from dual;

      if dep_giorno = 'LUNEDÌ' then
         d_valore := 'lunedi''';
      elsif dep_giorno = 'MARTEDÌ' then
         d_valore := 'martedi''';
      elsif dep_giorno = 'MERCOLEDÌ' then
         d_valore := 'mercoledi''';
      elsif dep_giorno = 'GIOVEDÌ' then
         d_valore := 'giovedi''';
      elsif dep_giorno = 'VENERDÌ' then
         d_valore := 'venerdi''';
      elsif dep_giorno = 'SABATO' then
         d_valore := 'sabato';
      elsif dep_giorno = 'DOMENICA' then
         d_valore := 'domenica';
      end if;

      return d_valore;
   end giorno_lettere;

   function mese_lettere (a_numero in number)
      return varchar2
   is
      /******************************************************************************
       RESTITUISCE IL MESE IN LETTERE
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

   function numero_lettere (a_numero in number)
      /******************************************************************************
      RESTITUISCE IL NUMERO IN FORMATO LETTERE
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
         if i in (1
                , 4
                , 7
                , 10) then
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
         elsif i in (2
                   , 5
                   , 8
                   , 11) then
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

         if d_cifra = 1 and substr (d_importo_dec, 2, 1) > 0 then
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

   function join_str (p_cursor sys_refcursor, p_del varchar2 := ',')
      return varchar2
   is
      l_value    varchar2 (32767);
      l_result   varchar2 (32767);
   begin
      loop
         fetch p_cursor into l_value;

         exit when p_cursor%notfound;

         if l_result is not null then
            l_result := l_result || p_del;
         end if;

         l_result := l_result || l_value;
      end loop;

      return l_result;
   end join_str;

   function join_clob (p_cursor sys_refcursor, p_del varchar2 := ',')
      return clob
   is
      l_value    varchar2 (32767);
      l_result   varchar2 (32767);
      d_clob     clob := empty_clob ();
   begin
      dbms_lob.createtemporary (d_clob, true, dbms_lob.call);

      loop
         fetch p_cursor into l_value;

         exit when p_cursor%notfound;

         if d_clob is not null then
            d_clob := d_clob || p_del;
         end if;

         d_clob := d_clob || l_value;
      end loop;

      return d_clob;
      dbms_lob.freetemporary (d_clob);
   end join_clob;

   function split_str (p_list varchar2, p_del varchar2 := ',')
      return split_tbl
      pipelined
   is
      l_idx     pls_integer;
      l_list    varchar2 (32767) := p_list;
      l_value   varchar2 (32767);
   begin
      loop
         l_idx := instr (l_list, p_del);

         if l_idx > 0 then
            pipe row (substr (l_list, 1, l_idx - 1));
            l_list := substr (l_list, l_idx + length (p_del));
         else
            pipe row (l_list);
            exit;
         end if;
      end loop;

      return;
   end split_str;
end utility_pkg;

/
