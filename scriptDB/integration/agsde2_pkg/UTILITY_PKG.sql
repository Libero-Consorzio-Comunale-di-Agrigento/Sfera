--liquibase formatted sql
--changeset rdestasio:install_20210116_pkg_03 runOnChange:true

create or replace package utility_pkg
is
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

   function get_cognome_nome (p_ni number)
      return varchar2;

   function get_nominativo_soggetto (p_ni number, p_ente varchar2)
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

  function get_firmatario_deli_cognome (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2;

   function get_firmatario_deli_nome (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2;

   function get_firmatario_par_contabile (p_id_delibera number)
      return varchar2;

   function get_firmatario_visto_contabile (p_id_determina number)
      return varchar2;

   function get_firmatario_visto_contab_c (p_id_determina number)
  return varchar2;

   function get_firmatario_visto_contab_n (p_id_determina number)
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

   function get_primo_firmatario_prop_deli (p_id_proposta_delibera number)
      return varchar2;

   function get_primo_firmatario_visto_par (p_id_visto_parere number)
      return varchar2;

   function get_sogg_notifica_delibera (p_id_delibera number)
      return varchar2;

   function get_sogg_notifica_determina (p_id_determina number)
      return varchar2;

   function get_suddivisione_descrizione (p_unita_progr   number
                                        , p_unita_dal     date
                                        , p_suddivisione  varchar2
                                        , p_ente          varchar2)
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

   function inserisci_tipo_delega_so4 (p_id            varchar2
                                     , p_titolo        varchar2
                                     , p_modulo        varchar2
                                     , p_istanza       varchar2)
      return varchar2;

   function aggiorna_tipo_delega_so4 (p_id            varchar2
                                    , p_titolo        varchar2
                                    , p_modulo        varchar2
                                    , p_istanza       varchar2)
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

  procedure elimina_componenti_non_validi( pd_id_commissione number);

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

end utility_pkg;
/
create or replace package body utility_pkg
is
   function get_codice_unita (p_unita_progr number, p_unita_dal date)
      return varchar2
   /***************************************************************
   Funzione che restituisce il codice di unità su SO4
   ***************************************************************/
   is
      d_codice_desc   varchar2 (255) := null;
begin
begin
         d_codice_desc   := so4_util.anuo_get_codice_uo (p_unita_progr, p_unita_dal);
exception
         when no_data_found then
            d_codice_desc   := null;
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
      d_codice_desc   varchar2 (255) := null;
begin
begin
         -- in questo punto voglio ottenere il codice dell'unità, quindi è giusto usare il suo _dal in quanto parte della chiave primaria (non è una "ricerca")
select unita_progr, unita_dal
into d_unita_progr, d_unita_dal
from proposte_delibera_soggetti
where id_proposta_delibera = p_id_proposta_delibera
  and tipo_soggetto = 'UO_PROPONENTE';

d_codice_desc   := so4_util.anuo_get_codice_uo (d_unita_progr, d_unita_dal);
exception
         when no_data_found then
            d_codice_desc   := null;
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
      d_codice_desc   varchar2 (255) := null;
begin
begin
         -- in questo punto voglio ottenere il codice dell'unità, quindi è giusto usare il suo _dal in quanto parte della chiave primaria (non è una "ricerca")
select unita_progr, unita_dal
into d_unita_progr, d_unita_dal
from determine_soggetti
where id_determina = p_id_determina
  and tipo_soggetto = 'UO_PROPONENTE';

d_codice_desc   := so4_util.anuo_get_codice_uo (d_unita_progr, d_unita_dal);
exception
         when no_data_found then
            d_codice_desc   := null;
end;

return d_codice_desc;
end get_codice_unita_prop_dete;

   function unita_get_radice_determina (p_id_determina number)
      return varchar2
   /***************************************************************
   Funzione che restituisce l'unita radice della struttura dell'unità proponente della determina
   ***************************************************************/
   is
      d_unita_progr    number (19);
      d_unita_dal      date;
      d_unita_ottica   varchar2 (255);
      d_codice_desc    varchar2 (255) := null;
begin
begin
select unita_progr, unita_dal, unita_ottica
into d_unita_progr, d_unita_dal, d_unita_ottica
from determine_soggetti
where id_determina = p_id_determina
  and tipo_soggetto = 'UO_PROPONENTE';

begin
            d_codice_desc   := substr (so4_util.unita_get_radice (d_unita_progr, d_unita_ottica, sysdate), instr (so4_util.unita_get_radice (d_unita_progr, d_unita_ottica, sysdate), '#', -1) + 1);
exception
            when others then
               d_codice_desc   := null;
end;
exception
         when no_data_found then
            d_codice_desc   := null;
end;

return d_codice_desc;
end unita_get_radice_determina;

   function unita_get_radice_prop_delibera (p_id_proposta_delibera number)
      return varchar2
   /***************************************************************
   Funzione che restituisce l'unita radice della struttura dell'unità proponente della determina
   ***************************************************************/
   is
      d_unita_progr    number (19);
      d_unita_dal      date;
      d_unita_ottica   varchar2 (255);
      d_codice_desc    varchar2 (255) := null;
begin
begin
select unita_progr, unita_dal, unita_ottica
into d_unita_progr, d_unita_dal, d_unita_ottica
from proposte_delibera_soggetti
where id_proposta_delibera = p_id_proposta_delibera
  and tipo_soggetto = 'UO_PROPONENTE';

begin
            d_codice_desc   := substr (so4_util.unita_get_radice (d_unita_progr, d_unita_ottica, sysdate), instr (so4_util.unita_get_radice (d_unita_progr, d_unita_ottica, sysdate), '#', -1) + 1);
exception
            when others then
               d_codice_desc   := null;
end;
exception
         when no_data_found then
            d_codice_desc   := null;
end;

return d_codice_desc;
end unita_get_radice_prop_delibera;

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
where ni = p_ni
  and rownum = 1;                                                                                                                -- questo serve per parare i casi (errati) in cui un soggetto

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
where ni = p_ni
  and rownum = 1;                                                                                                                -- questo serve per parare i casi (errati) in cui un soggetto

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

   function get_nominativo_soggetto (p_ni number, p_ente varchar2)
      return varchar2
   is
      /******************************************************************************
       RESTITUISCE IL COGNOME||' '||NOME OPPURE IL NOME||' '||COGNOME DEL SOGGETTO LEGGENDO DALLA VISTA AS4_V_SOGGETTI_CORRENTI IN BASE ALL'IMPOSTAZIONE
       ******************************************************************************/
      d_nominativo     varchar2 (500);
      d_impostazione   varchar2 (500);
begin
      if (p_ni is null) then
         return '';
end if;

begin
         d_impostazione   := impostazioni_pkg.get_impostazione ('SOGGETTI_FORMATO', p_ente);

         if (d_impostazione = 'COGNOME_NOME') then
select cognome || ' ' || nome
into d_nominativo
from as4_v_soggetti_correnti
where ni = p_ni
  and rownum = 1;                                                                                                             -- questo serve per parare i casi (errati) in cui un soggetto
else
select nome || ' ' || cognome
into d_nominativo
from as4_v_soggetti_correnti
where ni = p_ni
  and rownum = 1;                                                                                                             -- questo serve per parare i casi (errati) in cui un soggetto
end if;

         -- in anagrafica ha più di un utente collegato:
         -- in tali casi infatti nella vista as4_v_soggetti_correnti
         -- ci sono più righe con stesso ni.

return d_nominativo;
exception
         when no_data_found then
            -- in caso di errore, cioè di ni non trovato, ritorno stringa vuota in modo che la stampa
            -- comunque funzioni.
            return '';
end;
end get_nominativo_soggetto;


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
                     and f.utente_firmatario_effettivo = ds.utente
                     and ds.tipo_soggetto = p_tipo_soggetto
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by f.sequenza asc)
      loop
         d_data   := c.data_firma;
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
         d_data   := c.data_firma;
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
         d_data   := c.data_firma;
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
      d_ottica      := so4_util.set_ottica_default (p_ottica);

      if p_separatore is not null then
         d_separatore   := p_separatore;
end if;

      v_struttura   := so4_util.unita_get_ascendenti_sudd (p_codice_uo, p_data, d_ottica);

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
                  d_desc_suddivisione   := null;
end;

            d_result   := d_desc_suddivisione || ': ' || v_struttura_row.desc_uo || d_separatore || d_result;
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
for c in (select vp.esito
                  from visti_pareri vp, delibere d, tipi_visto_parere tvp
                 where d.id_delibera = p_id_delibera
                   and vp.id_proposta_delibera = d.id_proposta_delibera
                   and vp.id_tipologia = tvp.id_tipo_visto_parere
                   and tvp.contabile = 'Y')
      loop
         d_esito   := c.esito;
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
                 where vp.id_determina = p_id_determina
                   and vp.id_tipologia = tvp.id_tipo_visto_parere
                   and tvp.contabile = 'Y')
      loop
         d_esito   := c.esito;
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
for c in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                    from firmatari f, as4_v_soggetti_correnti, delibere_soggetti ds
                   where f.id_delibera = p_id_delibera
                     and f.utente_firmatario_effettivo = as4_v_soggetti_correnti.utente
                     and ds.id_delibera = p_id_delibera
                     and f.utente_firmatario_effettivo = ds.utente
                     and ds.tipo_soggetto = p_tipo_soggetto
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by f.sequenza asc)
      loop
         d_firmatario   := c.nominativo;
         exit;
end loop;

return d_firmatario;
end get_firmatario_delibera;

     function get_firmatario_deli_cognome (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2

   is
      d_firmatario   varchar2 (4000);
begin
for c in (  select as4_v_soggetti_correnti.cognome  nominativo
                    from firmatari f, as4_v_soggetti_correnti, delibere_soggetti ds
                   where f.id_delibera = p_id_delibera
                     and f.utente_firmatario_effettivo = as4_v_soggetti_correnti.utente
                     and ds.id_delibera = p_id_delibera
                     and f.utente_firmatario_effettivo = ds.utente
                     and ds.tipo_soggetto = p_tipo_soggetto
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by f.sequenza asc)
      loop
         d_firmatario   := c.nominativo;
         exit;
end loop;

return d_firmatario;
end get_firmatario_deli_cognome;

      function get_firmatario_deli_nome (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2

   is
      d_firmatario   varchar2 (4000);
begin
for c in (  select as4_v_soggetti_correnti.nome  nominativo
                    from firmatari f, as4_v_soggetti_correnti, delibere_soggetti ds
                   where f.id_delibera = p_id_delibera
                     and f.utente_firmatario_effettivo = as4_v_soggetti_correnti.utente
                     and ds.id_delibera = p_id_delibera
                     and f.utente_firmatario_effettivo = ds.utente
                     and ds.tipo_soggetto = p_tipo_soggetto
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by f.sequenza asc)
      loop
         d_firmatario   := c.nominativo;
         exit;
end loop;

return d_firmatario;
end get_firmatario_deli_nome;

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
                       , as4_v_soggetti_correnti
                       , visti_pareri vp
                       , delibere d
                       , tipi_visto_parere tvp
                   where d.id_delibera = p_id_delibera
                     and vp.id_proposta_delibera = d.id_proposta_delibera
                     and vp.id_tipologia = tvp.id_tipo_visto_parere
                     and tvp.contabile = 'Y'
                     and f.id_visto_parere = vp.id_visto_parere
                     and f.utente_firmatario_effettivo = as4_v_soggetti_correnti.utente
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_firmatario   := c.nominativo;
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
                       , as4_v_soggetti_correnti
                       , visti_pareri vp
                       , tipi_visto_parere tvp
                   where vp.id_determina = p_id_determina
                     and vp.id_tipologia = tvp.id_tipo_visto_parere
                     and tvp.contabile = 'Y'
                     and f.id_visto_parere = vp.id_visto_parere
                     and f.utente_firmatario_effettivo = as4_v_soggetti_correnti.utente
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_firmatario   := c.nominativo;
         exit;
end loop;

return d_firmatario;
end get_firmatario_visto_contabile;

      function get_firmatario_visto_contab_c (p_id_determina number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la denominazione del firmatario del visto contabile di una determina
   Se ci sono due visti con il campo CONTABILE = Y in tipologia
   allora la funzione ritorna uno dei due firmatari (a caso)
   ***************************************************************/
   is
      d_firmatario   varchar2 (4000);
begin
for c in (  select as4_v_soggetti_correnti.cognome  nominativo
                    from firmatari f
                       , as4_v_soggetti_correnti
                       , visti_pareri vp
                       , tipi_visto_parere tvp
                   where vp.id_determina = p_id_determina
                     and vp.id_tipologia = tvp.id_tipo_visto_parere
                     and tvp.contabile = 'Y'
                     and f.id_visto_parere = vp.id_visto_parere
                     and f.utente_firmatario_effettivo = as4_v_soggetti_correnti.utente
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_firmatario   := c.nominativo;
         exit;
end loop;

return d_firmatario;
end get_firmatario_visto_contab_c;

      function get_firmatario_visto_contab_n (p_id_determina number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la denominazione del firmatario del visto contabile di una determina
   Se ci sono due visti con il campo CONTABILE = Y in tipologia
   allora la funzione ritorna uno dei due firmatari (a caso)
   ***************************************************************/
   is
      d_firmatario   varchar2 (4000);
begin
for c in (  select as4_v_soggetti_correnti.nome nominativo
                    from firmatari f
                       , as4_v_soggetti_correnti
                       , visti_pareri vp
                       , tipi_visto_parere tvp
                   where vp.id_determina = p_id_determina
                     and vp.id_tipologia = tvp.id_tipo_visto_parere
                     and tvp.contabile = 'Y'
                     and f.id_visto_parere = vp.id_visto_parere
                     and f.utente_firmatario_effettivo = as4_v_soggetti_correnti.utente
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_firmatario   := c.nominativo;
         exit;
end loop;

return d_firmatario;
end get_firmatario_visto_contab_n;

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
where utente = p_utente
  and rownum = 1;                                                                                                                -- questo serve per parare i casi (errati) in cui un soggetto

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
where ni = p_ni
  and rownum = 1;                                                                                                                -- questo serve per parare i casi (errati) in cui un soggetto

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

   function get_nominativo_sogg_deli (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2
   /******************************************************************************
    restituisce il cognome||' '||nome di un soggetto della delibera
   ******************************************************************************/
   is
      d_utente       varchar2 (255) := null;
      d_nominativo   varchar2 (255) := null;
begin
begin
select utente
into d_utente
from delibere_soggetti
where id_delibera = p_id_delibera
  and tipo_soggetto = p_tipo_soggetto
  and attivo = 'Y';

d_nominativo   := get_cognome_nome (get_ni_soggetto (d_utente));
exception
         when no_data_found then
            d_nominativo   := null;
end;

return d_nominativo;
end get_nominativo_sogg_deli;

      function get_nominativo_sogg_deli_c (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2
   /******************************************************************************
    restituisce il cognome di un soggetto della delibera
   ******************************************************************************/
   is
      d_utente       varchar2 (255) := null;
      d_nominativo   varchar2 (255) := null;
begin
begin
select utente
into d_utente
from delibere_soggetti
where id_delibera = p_id_delibera
  and tipo_soggetto = p_tipo_soggetto
  and attivo = 'Y';

d_nominativo   := get_cognome (get_ni_soggetto (d_utente));
exception
         when no_data_found then
            d_nominativo   := null;
end;

return d_nominativo;
end get_nominativo_sogg_deli_c;

      function get_nominativo_sogg_deli_n (p_id_delibera number, p_tipo_soggetto varchar2)
      return varchar2
   /******************************************************************************
    restituisce il nome di un soggetto della delibera
   ******************************************************************************/
   is
      d_utente       varchar2 (255) := null;
      d_nominativo   varchar2 (255) := null;
begin
begin
select utente
into d_utente
from delibere_soggetti
where id_delibera = p_id_delibera
  and tipo_soggetto = p_tipo_soggetto
  and attivo = 'Y';

d_nominativo   := get_nome (get_ni_soggetto (d_utente));
exception
         when no_data_found then
            d_nominativo   := null;
end;

return d_nominativo;
end get_nominativo_sogg_deli_n;

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
where id_determina = p_id_determina
  and tipo_soggetto = p_tipo_soggetto
  and attivo = 'Y';

d_nominativo   := get_cognome_nome (get_ni_soggetto (d_utente));
exception
         when no_data_found then
            d_nominativo   := null;
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
where id_proposta_delibera = p_id_proposta_delibera
  and tipo_soggetto = p_tipo_soggetto
  and attivo = 'Y';

d_nominativo   := get_cognome_nome (get_ni_soggetto (d_utente));
exception
         when no_data_found then
            d_nominativo   := null;
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
for c in (  select sequenza, cognome_nome
                    from (select occ.sequenza, initcap (get_cognome_nome (nvl (occ.ni_componente, osp.ni_componente_esterno))) cognome_nome
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
                             and ( (p_ruolo is null)
                               or (p_ruolo is not null
                               and oop.ruolo_partecipante = p_ruolo))
                             and ( (p_presente is null)
                               or (p_presente is not null
                               and oop.presente = p_presente)))
                order by sequenza, cognome_nome)
      loop
         d_elenco_presenti   := d_elenco_presenti || c.cognome_nome || ', ';
end loop;

      if (d_elenco_presenti is not null) then
         d_elenco_presenti   := substr (d_elenco_presenti, 1, length (d_elenco_presenti) - 2) || '.';
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
for c in (select sequenza, cognome_nome
                  from (  select occ.sequenza, initcap (get_cognome_nome (nvl (occ.ni_componente, osp.ni_componente_esterno))) cognome_nome
                            from odg_sedute_partecipanti osp, odg_sedute os, odg_commissioni_componenti occ
                           where os.id_seduta = p_id_seduta
                             and osp.id_seduta = os.id_seduta
                             and os.id_seduta = osp.id_seduta
                             and occ.id_commissione_componente(+) = osp.id_commissione_componente
                             and (p_ruolo is null
                               or osp.ruolo_partecipante = p_ruolo)
                             and (p_presente is null
                               or osp.presente = p_presente)
                        order by sequenza, cognome_nome))
      loop
         d_elenco_presenti   := d_elenco_presenti || c.cognome_nome || ', ';
end loop;

      if (d_elenco_presenti is not null) then
         d_elenco_presenti   := substr (d_elenco_presenti, 1, length (d_elenco_presenti) - 2) || '.';
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
                   where f.id_certificato = p_id_certificato
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_data   := c.data_firma;
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
         d_data   := c.data_firma;
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
         d_data   := c.data_firma;
         exit;
end loop;

      if d_data is null then
         for c in (select data_numero_determina data_firma
                     from determine d
                    where d.id_determina = p_id_determina
                      and d.valido = 'Y'
                      and d.id_engine_iter is null)
         loop
            d_data   := c.data_firma;
            exit;
end loop;
end if;

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
         d_data   := c.data_firma;
         exit;
end loop;

return d_data;
end get_prima_data_firma_prop_deli;

   function get_prima_data_firma_visto_par (p_id_visto_parere number)
      return date
   /***************************************************************
   Funzione che restituisce la data in cui ha firmato il primo firmatario di un visto o parere
   ***************************************************************/
   is
      d_data   date;
begin
for c in (  select data_firma
                    from firmatari f
                   where f.id_visto_parere = p_id_visto_parere
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_data   := c.data_firma;
         exit;
end loop;

return d_data;
end get_prima_data_firma_visto_par;

   function get_primo_firmatario_certif (p_id_certificato number)
      return varchar2
   /***************************************************************
   Funzione che restituisce la denominazione del primo firmatario di un certificato
   ***************************************************************/
   is
      d_firmatario   varchar2 (4000);
begin
for c in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                    from firmatari f, as4_v_soggetti_correnti
                   where f.id_certificato = p_id_certificato
                     and f.utente_firmatario_effettivo = as4_v_soggetti_correnti.utente
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_firmatario   := c.nominativo;
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
      d_ni_soggetto   number (19);
begin
for c in (select get_ni_soggetto (utente) ni_soggetto
                  from delibere_soggetti
                 where tipo_soggetto = p_tipo_soggetto
                   and id_delibera = p_id_delibera
                   and attivo = 'Y'
                   and sequenza = p_sequenza)
      loop
         d_ni_soggetto   := c.ni_soggetto;
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
for c in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                    from firmatari f, as4_v_soggetti_correnti
                   where f.id_delibera = p_id_delibera
                     and f.utente_firmatario_effettivo = as4_v_soggetti_correnti.utente
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_firmatario   := c.nominativo;
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
for c in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                    from firmatari f, as4_v_soggetti_correnti
                   where f.id_determina = p_id_determina
                     and f.utente_firmatario_effettivo = as4_v_soggetti_correnti.utente
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_firmatario   := c.nominativo;
         exit;
end loop;

      if d_firmatario is null then
         for c in (select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                     from determine d, determine_soggetti ds, as4_v_soggetti_correnti
                    where d.id_determina = p_id_determina
                      and d.valido = 'Y'
                      and d.id_engine_iter is null
                      and d.numero_determina is not null
                      and d.id_determina = ds.id_determina
                      and ds.tipo_soggetto = 'DIRIGENTE'
                      and ds.sequenza = 0
                      and ds.utente = as4_v_soggetti_correnti.utente)
         loop
            d_firmatario   := c.nominativo;
            exit;
end loop;
end if;

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
for c in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                    from firmatari f, as4_v_soggetti_correnti
                   where f.id_proposta_delibera = p_id_proposta_delibera
                     and f.utente_firmatario_effettivo = as4_v_soggetti_correnti.utente
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_firmatario   := c.nominativo;
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
for c in (  select as4_v_soggetti_correnti.cognome || ' ' || as4_v_soggetti_correnti.nome nominativo
                    from firmatari f, as4_v_soggetti_correnti
                   where f.id_visto_parere = p_id_visto_parere
                     and f.utente_firmatario_effettivo = as4_v_soggetti_correnti.utente
                     and f.firmato = 'Y'
                     and f.data_firma is not null
                order by sequenza asc)
      loop
         d_firmatario   := c.nominativo;
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
            d_elenco   := d_elenco || c.nominativo || '; ';
end loop;
exception
         when others then
            d_elenco   := '';
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
            d_elenco   := d_elenco || c.nominativo || '; ';
end loop;
exception
         when others then
            d_elenco   := '';
end;

return d_elenco;
end get_sogg_notifica_determina;

   function get_suddivisione_descrizione (p_unita_progr   number
                                        , p_unita_dal     date
                                        , p_suddivisione  varchar2
                                        , p_ente          varchar2)
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
      d_ottica        := impostazioni_pkg.get_impostazione ('OTTICA_SO4', p_ente);

select id_suddivisione
into d_id_suddivisione
from so4_v_suddivisioni_struttura
where codice = impostazioni_pkg.get_impostazione (p_suddivisione, p_ente)
  and ottica = d_ottica;

d_suddivisione_progr      :=
         so4_util.get_area_unita (d_id_suddivisione
                                , p_unita_progr
                                , p_unita_dal
                                , d_ottica);

      d_descrizione   := get_uo_descrizione (d_suddivisione_progr, p_unita_dal);

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
         --         select ds.unita_progr, nvl(d.data_numero_proposta, d.data_ins)
         --           into d_unita_progr, d_unita_dal
         --           from determine_soggetti ds, determine d
         --          where ds.id_determina = d.id_determina and d.id_determina = p_id_determina and tipo_soggetto = 'UO_PROPONENTE';

select ds.unita_progr, ds.unita_dal
into d_unita_progr, d_unita_dal
from determine_soggetti ds
where ds.id_determina = p_id_determina
  and ds.tipo_soggetto = 'UO_PROPONENTE';

d_suddivisione      :=
            utility_pkg.get_suddivisione_descrizione (d_unita_progr
                                                    , trunc(sysdate)  --d_unita_dal
                                                    , p_suddivisione
                                                    , p_ente);
exception
         when no_data_found then
            d_suddivisione   := null;
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
         --         select pds.unita_progr, nvl(pd.data_numero_proposta, pd.data_ins)
         --           into d_unita_progr, d_unita_dal
         --           from proposte_delibera_soggetti pds, proposte_delibera pd
         --          where pds.id_proposta_delibera = pd.id_proposta_delibera and pd.id_proposta_delibera = p_id_proposta_delibera and tipo_soggetto = 'UO_PROPONENTE';

select pds.unita_progr, pds.unita_dal
into d_unita_progr, d_unita_dal
from proposte_delibera_soggetti pds
where pds.id_proposta_delibera = p_id_proposta_delibera
  and pds.tipo_soggetto = 'UO_PROPONENTE';

d_suddivisione      :=
            utility_pkg.get_suddivisione_descrizione (d_unita_progr
                                                    , trunc(sysdate) --d_unita_dal
                                                    , p_suddivisione
                                                    , p_ente);
exception
         when no_data_found then
            d_suddivisione   := null;
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
            d_suddivisione   := null;
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
            d_suddivisione   := null;
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
from visti_pareri vp, so4_v_unita_organizzative_pubb so4_unita, so4_v_suddivisioni_struttura so4_suddivisioni
where vp.id_visto_parere = p_id_visto_parere
  and vp.unita_dal = so4_unita.dal
  and vp.unita_ottica = so4_unita.ottica
  and vp.unita_progr = so4_unita.progr
  and so4_unita.id_suddivisione = so4_suddivisioni.id_suddivisione;
exception
         when no_data_found then
            d_suddivisione   := null;
end;

return d_suddivisione;
end get_suddivisione_vistoparere;

   function get_testo_proposta_delibera (p_id_proposta_delibera number)
      /******************************************************************************
     RESTITUISCE IL TESTO DELLA PROPOSTA (BLOB)
    ******************************************************************************/
      return blob
   is
      d_testo_proposta     blob;
      d_integrazione_gdm   varchar (1);
      d_ente               proposte_delibera.ente%type;
begin
      dbms_lob.createtemporary (d_testo_proposta, true);

select ente
into d_ente
from proposte_delibera d
where d.id_proposta_delibera = p_id_proposta_delibera;

d_integrazione_gdm   := impostazioni_pkg.get_impostazione ('INTEGRAZIONE_GDM', d_ente);

      if d_integrazione_gdm = 'Y' then
         /* INIZIO QUERY DA USARE IN PRESENZA DI GDM */
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
                  and pd.id_file_allegato_testo = fa.id_file_allegato))' into d_testo_proposta using in p_id_proposta_delibera;
end if;

return d_testo_proposta;
end get_testo_proposta_delibera;

   function get_testo_originale_determina (p_id_determina number)
      /******************************************************************************
     RESTITUISCE IL TESTO CONTENUTO NELL'ALLEGATO TESTO_ORIGINALE DELLA DETERMINA (BLOB)
    ******************************************************************************/
      return blob
   is
      d_testo_originale    blob;
      d_integrazione_gdm   varchar (1);
      d_ente               determine.ente%type;
begin
      dbms_lob.createtemporary (d_testo_originale, true);

select ente
into d_ente
from determine d
where d.id_determina = p_id_determina;

d_integrazione_gdm   := impostazioni_pkg.get_impostazione ('INTEGRAZIONE_GDM', d_ente);

      if d_integrazione_gdm = 'Y' then
         /* INIZIO QUERY DA USARE IN PRESENZA DI GDM */
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
                       and fa.id_file_esterno is not null)' into d_testo_originale using in p_id_determina;
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
                 and fa.id_file_esterno is not null' into d_testo_originale using in p_id_determina;
end if;

return d_testo_originale;
end get_testo_originale_determina;

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
         --         select unita_progr, nvl(d.data_numero_proposta, d.data_ins)
         --           into d_unita_progr, d_unita_dal
         --           from determine_soggetti ds, determine d
         --          where ds.id_determina = d.id_determina and d.id_determina = p_id_determina and tipo_soggetto = 'UO_PROPONENTE';

select ds.unita_progr, ds.unita_dal
into d_unita_progr, d_unita_dal
from determine_soggetti ds
where ds.id_determina = p_id_determina
  and ds.tipo_soggetto = 'UO_PROPONENTE';

d_unita_desc   := utility_pkg.get_uo_descrizione (d_unita_progr, d_unita_dal);
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
for c in (  select get_uo_descrizione (unita_progr, unita_dal) uo_visto
                    from visti_pareri vp, tipi_visto_parere tvp, delibere d
                   where d.id_delibera = p_id_delibera
                     and vp.id_proposta_delibera = d.id_proposta_delibera
                     and vp.id_tipologia = tvp.id_tipo_visto_parere
                     and tvp.contabile = 'Y'
                order by vp.data_upd desc)
      loop
         d_descrizione   := c.uo_visto;
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
         --         select unita_progr, nvl(pd.data_numero_proposta, pd.data_ins)
         --           into d_unita_progr, d_unita_dal
         --           from proposte_delibera_soggetti pds, proposte_delibera pd
         --          where pds.id_proposta_delibera = pd.id_proposta_delibera and pd.id_proposta_delibera = p_id_proposta_delibera and tipo_soggetto = 'UO_PROPONENTE';

select pds.unita_progr, pds.unita_dal
into d_unita_progr, d_unita_dal
from proposte_delibera_soggetti pds
where pds.id_proposta_delibera = p_id_proposta_delibera
  and pds.tipo_soggetto = 'UO_PROPONENTE';

d_unita_desc   := utility_pkg.get_uo_descrizione (d_unita_progr, d_unita_dal);
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
                   where vp.id_determina = p_id_determina
                     and vp.id_tipologia = tvp.id_tipo_visto_parere
                     and tvp.contabile = 'Y'
                order by vp.data_upd desc)
      loop
         d_descrizione   := c.uo_visto;
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
         d_descrizione   := so4_util.anuo_get_descrizione (p_unita_progr, p_unita_dal);
exception
         when others then
            d_descrizione   := '--';
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
         d_descrizione   := so4_util.unita_get_unita_padre (p_unita_progr, p_ottica, p_unita_dal);

         d_descrizione   := substr (d_descrizione, instr (d_descrizione, '#', -1) + 1, length (d_descrizione) - instr (d_descrizione, '#', -1));
exception
         when others then
            d_descrizione   := '';
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
         --         select ds.unita_progr, ds.unita_ottica, nvl (d.data_numero_proposta, d.data_ins)
         --           into d_unita_progr, d_unita_ottica, d_unita_dal
         --           from determine_soggetti ds, determine d
         --          where ds.id_determina = d.id_determina and d.id_determina = p_id_determina and tipo_soggetto = 'UO_PROPONENTE';

select ds.unita_progr, ds.unita_ottica, trunc(sysdate) --ds.unita_dal
into d_unita_progr, d_unita_ottica, d_unita_dal
from determine_soggetti ds
where ds.id_determina = p_id_determina
  and ds.tipo_soggetto = 'UO_PROPONENTE';

d_descrizione   := get_uo_padre_descrizione (d_unita_progr, d_unita_dal, d_unita_ottica);
exception
         when others then
            d_descrizione   := '';
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
         --         select pds.unita_progr, pds.unita_ottica, nvl(pd.data_numero_proposta, pd.data_ins) unita_dal
         --           into d_unita_progr, d_unita_ottica, d_unita_dal
         --           from proposte_delibera_soggetti pds, proposte_delibera pd
         --          where pds.id_proposta_delibera = pd.id_proposta_delibera
         --            and pd.id_proposta_delibera  = p_id_proposta_delibera
         --            and tipo_soggetto = 'UO_PROPONENTE';

select pds.unita_progr, pds.unita_ottica, trunc(sysdate) --pds.unita_dal
into d_unita_progr, d_unita_ottica, d_unita_dal
from proposte_delibera_soggetti pds
where pds.id_proposta_delibera = p_id_proposta_delibera
  and pds.tipo_soggetto = 'UO_PROPONENTE';

d_descrizione   := get_uo_padre_descrizione (d_unita_progr, d_unita_dal, d_unita_ottica);
exception
         when others then
            d_descrizione   := '';
end;

return d_descrizione;
end get_uo_padre_prop_delibera;

   function get_votanti_oggetto_seduta (p_id_oggetto_seduta number, p_voto varchar2)
      return varchar2
   is
      d_elenco   varchar2 (32767);
begin
for c in (  select sequenza, cognome_nome
                    from (select occ.sequenza, initcap (get_cognome_nome (nvl (occ.ni_componente, osp.ni_componente_esterno))) cognome_nome
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
                             and ( (p_voto is null)
                               or (p_voto is not null
                               and ov.valore = p_voto)))
                order by sequenza, cognome_nome asc)
      loop
         d_elenco   := d_elenco || c.cognome_nome || ', ';
end loop;

      if (d_elenco is not null) then
         d_elenco   := substr (d_elenco, 1, length (d_elenco) - 2) || '.';
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
         d_valore   := 'lunedi''';
      elsif dep_giorno = 'MARTEDÌ' then
         d_valore   := 'martedi''';
      elsif dep_giorno = 'MERCOLEDÌ' then
         d_valore   := 'mercoledi''';
      elsif dep_giorno = 'GIOVEDÌ' then
         d_valore   := 'giovedi''';
      elsif dep_giorno = 'VENERDÌ' then
         d_valore   := 'venerdi''';
      elsif dep_giorno = 'SABATO' then
         d_valore   := 'sabato';
      elsif dep_giorno = 'DOMENICA' then
         d_valore   := 'domenica';
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
         d_stringa   := 'GENNAIO';
      elsif a_numero = 2 then
         d_stringa   := 'FEBBRAIO';
      elsif a_numero = 3 then
         d_stringa   := 'MARZO';
      elsif a_numero = 4 then
         d_stringa   := 'APRILE';
      elsif a_numero = 5 then
         d_stringa   := 'MAGGIO';
      elsif a_numero = 6 then
         d_stringa   := 'GIUGNO';
      elsif a_numero = 7 then
         d_stringa   := 'LUGLIO';
      elsif a_numero = 8 then
         d_stringa   := 'AGOSTO';
      elsif a_numero = 9 then
         d_stringa   := 'SETTEMBRE';
      elsif a_numero = 10 then
         d_stringa   := 'OTTOBRE';
      elsif a_numero = 11 then
         d_stringa   := 'NOVEMBRE';
      elsif a_numero = 12 then
         d_stringa   := 'DICEMBRE';
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
      d_stringa   := null;
      d_importo   := lpad (to_char (trunc (a_numero)), 12, '0');
      d_cifra     := mod (round (a_numero, 2), 1) * 100;

      if d_cifra > 9 then
         d_importo_dec   := rpad (to_char (mod (round (a_numero, 2), 1) * 100), 2, '0');
else
         d_importo_dec   := lpad (to_char (mod (round (a_numero, 2), 1) * 100), 2, '0');
end if;

      --
for i in 1 .. 12
      loop
         d_cifra   := substr (d_importo, i, 1);

         --
         /* TEST SULLE CENTINAIA */
         --
         if i in (1
                , 4
                , 7
                , 10) then
            --
            if d_cifra = 2 then
               d_stringa   := d_stringa || 'DUE';
            elsif d_cifra = 3 then
               d_stringa   := d_stringa || 'TRE';
            elsif d_cifra = 4 then
               d_stringa   := d_stringa || 'QUATTRO';
            elsif d_cifra = 5 then
               d_stringa   := d_stringa || 'CINQUE';
            elsif d_cifra = 6 then
               d_stringa   := d_stringa || 'SEI';
            elsif d_cifra = 7 then
               d_stringa   := d_stringa || 'SETTE';
            elsif d_cifra = 8 then
               d_stringa   := d_stringa || 'OTTO';
            elsif d_cifra = 9 then
               d_stringa   := d_stringa || 'NOVE';
end if;

            if d_cifra != 0 then
               d_stringa   := d_stringa || 'CENTO';
end if;
         /* TEST SULLE DECINE */
         elsif i in (2
                   , 5
                   , 8
                   , 11) then
            if d_cifra = 2 then
               d_stringa   := d_stringa || 'VENT';
            elsif d_cifra = 3 then
               d_stringa   := d_stringa || 'TRENT';
            elsif d_cifra = 4 then
               d_stringa   := d_stringa || 'QUARANT';
            elsif d_cifra = 5 then
               d_stringa   := d_stringa || 'CINQUANT';
            elsif d_cifra = 6 then
               d_stringa   := d_stringa || 'SESSANT';
            elsif d_cifra = 7 then
               d_stringa   := d_stringa || 'SETTANT';
            elsif d_cifra = 8 then
               d_stringa   := d_stringa || 'OTTANT';
            elsif d_cifra = 9 then
               d_stringa   := d_stringa || 'NOVANT';
end if;

            if d_cifra = 2 then
               if substr (d_importo, i + 1, 1) in (1, 8) then
                  null;
else
                  d_stringa   := d_stringa || 'I';
end if;
            elsif d_cifra > 2 then
               if substr (d_importo, i + 1, 1) in (1, 8) then
                  null;
else
                  d_stringa   := d_stringa || 'A';
end if;
end if;

            if d_cifra = 1 then
               if substr (d_importo, i + 1, 1) = 0 then
                  d_stringa   := d_stringa || 'DIECI';
               elsif substr (d_importo, i + 1, 1) = 1 then
                  d_stringa   := d_stringa || 'UNDICI';
               elsif substr (d_importo, i + 1, 1) = 2 then
                  d_stringa   := d_stringa || 'DODICI';
               elsif substr (d_importo, i + 1, 1) = 3 then
                  d_stringa   := d_stringa || 'TREDICI';
               elsif substr (d_importo, i + 1, 1) = 4 then
                  d_stringa   := d_stringa || 'QUATTORDICI';
               elsif substr (d_importo, i + 1, 1) = 5 then
                  d_stringa   := d_stringa || 'QUINDICI';
               elsif substr (d_importo, i + 1, 1) = 6 then
                  d_stringa   := d_stringa || 'SEDICI';
               elsif substr (d_importo, i + 1, 1) = 7 then
                  d_stringa   := d_stringa || 'DICIASSETTE';
               elsif substr (d_importo, i + 1, 1) = 8 then
                  d_stringa   := d_stringa || 'DICIOTTO';
               elsif substr (d_importo, i + 1, 1) = 9 then
                  d_stringa   := d_stringa || 'DICIANNOVE';
end if;
else
               if substr (d_importo, i + 1, 1) = 1 then
                  if substr (d_importo, i - 1, 3) = '001' then
                     if i in (2, 5) then
                        d_stringa   := d_stringa || 'UN';
                     elsif i = 11 then
                        d_stringa   := d_stringa || 'UNO';
end if;
else
                     d_stringa   := d_stringa || 'UNO';
end if;
               elsif substr (d_importo, i + 1, 1) = 2 then
                  d_stringa   := d_stringa || 'DUE';
               elsif substr (d_importo, i + 1, 1) = 3 then
                  d_stringa   := d_stringa || 'TRE';
               elsif substr (d_importo, i + 1, 1) = 4 then
                  d_stringa   := d_stringa || 'QUATTRO';
               elsif substr (d_importo, i + 1, 1) = 5 then
                  d_stringa   := d_stringa || 'CINQUE';
               elsif substr (d_importo, i + 1, 1) = 6 then
                  d_stringa   := d_stringa || 'SEI';
               elsif substr (d_importo, i + 1, 1) = 7 then
                  d_stringa   := d_stringa || 'SETTE';
               elsif substr (d_importo, i + 1, 1) = 8 then
                  d_stringa   := d_stringa || 'OTTO';
               elsif substr (d_importo, i + 1, 1) = 9 then
                  d_stringa   := d_stringa || 'NOVE';
end if;
end if;
end if;

         --
         if i = 2 then
            if substr (d_importo, 1, 3) = '000' then
               null;
            elsif substr (d_importo, 1, 3) = '001' then
               d_stringa   := d_stringa || 'MILIARDO';
else
               d_stringa   := d_stringa || 'MILIARDI';
end if;
         elsif i = 5 then
            if substr (d_importo, 4, 3) = '000' then
               null;
            elsif substr (d_importo, 4, 3) = '001' then
               d_stringa   := d_stringa || 'MILIONE';
else
               d_stringa   := d_stringa || 'MILIONI';
end if;
         elsif i = 8 then
            if substr (d_importo, 7, 3) = '000' then
               null;
            elsif substr (d_importo, 7, 3) = '001' then
               d_stringa   := d_stringa || 'MILLE';
else
               d_stringa   := d_stringa || 'MILA';
end if;
end if;
      --
end loop;

      --
      if d_importo = '000000000000' then
         d_stringa   := 'ZERO';
end if;

      --
      if d_importo_dec != '00' then
         d_stringa   := d_stringa || ' VIRGOLA ';
         d_cifra     := substr (d_importo_dec, 1, 1);

         if substr (d_importo_dec, 2, 1) != 0 then
            if d_cifra = 0 then
               d_stringa   := d_stringa || 'ZERO';
            elsif d_cifra = 2 then
               d_stringa   := d_stringa || 'VENT';
            elsif d_cifra = 3 then
               d_stringa   := d_stringa || 'TRENT';
            elsif d_cifra = 4 then
               d_stringa   := d_stringa || 'QUARANT';
            elsif d_cifra = 5 then
               d_stringa   := d_stringa || 'CINQUANT';
            elsif d_cifra = 6 then
               d_stringa   := d_stringa || 'SESSANT';
            elsif d_cifra = 7 then
               d_stringa   := d_stringa || 'SETTANT';
            elsif d_cifra = 8 then
               d_stringa   := d_stringa || 'OTTANT';
            elsif d_cifra = 9 then
               d_stringa   := d_stringa || 'NOVANT';
end if;

            if d_cifra = 2 then
               if substr (d_importo_dec, 2, 1) in (1, 8) then
                  null;
else
                  d_stringa   := d_stringa || 'I';
end if;
            elsif d_cifra > 2 then
               if substr (d_importo_dec, 2, 1) in (1, 8) then
                  null;
else
                  d_stringa   := d_stringa || 'A';
end if;
end if;

            if d_cifra = 1 then
               if substr (d_importo_dec, 2, 1) = 1 then
                  d_stringa   := d_stringa || 'UNDICI';
               elsif substr (d_importo_dec, 2, 1) = 2 then
                  d_stringa   := d_stringa || 'DODICI';
               elsif substr (d_importo_dec, 2, 1) = 3 then
                  d_stringa   := d_stringa || 'TREDICI';
               elsif substr (d_importo_dec, 2, 1) = 4 then
                  d_stringa   := d_stringa || 'QUATTORDICI';
               elsif substr (d_importo_dec, 2, 1) = 5 then
                  d_stringa   := d_stringa || 'QUINDICI';
               elsif substr (d_importo_dec, 2, 1) = 6 then
                  d_stringa   := d_stringa || 'SEDICI';
               elsif substr (d_importo_dec, 2, 1) = 7 then
                  d_stringa   := d_stringa || 'DICIASSETTE';
               elsif substr (d_importo_dec, 2, 1) = 8 then
                  d_stringa   := d_stringa || 'DICIOTTO';
               elsif substr (d_importo_dec, 2, 1) = 9 then
                  d_stringa   := d_stringa || 'DICIANNOVE';
end if;
end if;
end if;

         if d_cifra = 1
        and substr (d_importo_dec, 2, 1) > 0 then
            null;
else
            if substr (d_importo_dec, 2, 1) != 0 then
               d_cifra   := substr (d_importo_dec, 2, 1);
end if;

            if d_cifra = 1 then
               d_stringa   := d_stringa || 'UNO';
            elsif d_cifra = 2 then
               d_stringa   := d_stringa || 'DUE';
            elsif d_cifra = 3 then
               d_stringa   := d_stringa || 'TRE';
            elsif d_cifra = 4 then
               d_stringa   := d_stringa || 'QUATTRO';
            elsif d_cifra = 5 then
               d_stringa   := d_stringa || 'CINQUE';
            elsif d_cifra = 6 then
               d_stringa   := d_stringa || 'SEI';
            elsif d_cifra = 7 then
               d_stringa   := d_stringa || 'SETTE';
            elsif d_cifra = 8 then
               d_stringa   := d_stringa || 'OTTO';
            elsif d_cifra = 9 then
               d_stringa   := d_stringa || 'NOVE';
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
            l_result   := l_result || p_del;
end if;

         l_result   := l_result || l_value;
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
            d_clob   := d_clob || p_del;
end if;

         d_clob   := d_clob || l_value;
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
l_idx   := instr (l_list, p_del);

         if l_idx > 0 then
            pipe row (substr (l_list, 1, l_idx - 1));
            l_list   := substr (l_list, l_idx + length (p_del));
else
            pipe row (l_list);
            exit;
end if;
end loop;

      return;
end split_str;


   function get_stato_visti (p_id_atto number)
      return varchar2
   /***************************************************************
   Funzione che restituisce lo stato dei visti di un atto.
   Vengono considerati solo i pareri / visti validi e con esito differente da DA_VALUTARE:
   Se esiste almeno 1 visto/parere con esito CONTRARIO o RIMANDA_INDIETRO: la funzione ritorna 'CONTRARIO'
   Se esiste almeno 1 visto/parere con esito NON_APPOSTO e nessun visto/parere con esito CONTRARIO o RIMANDA_INDIETRO: la funzione ritorna 'NON_APPOSTO'
   Se tutti i visti/pareri sono con esito FAVOREVOLE o FAVOREVOLE_CON_PRESCRIZIONI: la funzione restituisce 'FAVOREVOLE'
   ***************************************************************/
   is
      d_stato           varchar2 (100) := '';
      num_totale        int;
      num_contrari      int;
      num_non_apposti   int;
      num_favorevoli    int;
begin
select count (*)
into num_totale
from visti_pareri
where (id_determina = p_id_atto
    or id_proposta_delibera = p_id_atto
    or id_delibera = p_id_atto)
  and esito != 'DA_VALUTARE'
         and valido = 'Y';

select count (*)
into num_contrari
from visti_pareri
where (id_determina = p_id_atto
    or id_proposta_delibera = p_id_atto
    or id_delibera = p_id_atto)
  and esito in ('RIMANDA_INDIETRO', 'CONTRARIO')
  and valido = 'Y';

select count (*)
into num_non_apposti
from visti_pareri
where (id_determina = p_id_atto
    or id_proposta_delibera = p_id_atto
    or id_delibera = p_id_atto)
  and esito = 'NON_APPOSTO'
  and valido = 'Y';

select count (*)
into num_favorevoli
from visti_pareri
where (id_determina = p_id_atto
    or id_proposta_delibera = p_id_atto
    or id_delibera = p_id_atto)
  and esito in ('FAVOREVOLE', 'FAVOREVOLE_CON_PRESCRIZIONI')
  and valido = 'Y';

if num_contrari > 0 then
         d_stato   := 'CONTRARIO';
      elsif num_non_apposti > 0 then
         d_stato   := 'NON_APPOSTO';
      elsif num_totale = num_favorevoli
        and num_totale > 0 then
         d_stato   := 'FAVOREVOLE';
end if;

return d_stato;
end get_stato_visti;

   function get_descrizione_firmatario (p_utente varchar2, p_ente varchar2)
      return varchar2
   is
      v_utente   varchar2 (4000);
begin
      if (p_utente is null) then
         return null;
end if;

begin
select utente
into v_utente
from so4_v_utenti_ruoli_sogg_uo
where utente = p_utente
  and ruolo = impostazioni_pkg.get_impostazione ('RUOLO_SO4_DIRIGENTE', p_ente)
  and rownum = 1;                                                                                                                -- questo serve per parare i casi (errati) in cui un soggetto

return impostazioni_pkg.get_impostazione ('MODELLI_STAMPE_DIRIGENTE', p_ente);
exception
         when no_data_found then
            -- in caso di errore, cioè di utente non trovato, vuol dire che non si tratta
            -- del dirigente quindi ritorniamo il testo presente sotto l'impostazione;
            return impostazioni_pkg.get_impostazione ('MODELLI_STAMPE_FIRMATARIO', p_ente);
end;
end get_descrizione_firmatario;

   function get_delegante (p_firmatario varchar2, p_utente varchar2, p_ente varchar2)
      return varchar2
   is
begin
      if (p_utente is null) then
         return null;
end if;

      if (p_firmatario is not null
      and p_firmatario <> p_utente) then
         return '(' || impostazioni_pkg.get_impostazione ('MODELLI_STAMPE_DELEGATO', p_ente) || ' ' || utility_pkg.get_cognome_nome (utility_pkg.get_ni_soggetto (p_utente)) || ')';
end if;

return '';
end get_delegante;


   function inserisci_tipo_delega_so4 (p_id            varchar2
                                     , p_titolo        varchar2
                                     , p_modulo        varchar2
                                     , p_istanza       varchar2)
      return varchar2
   is
      d_id_applicativo   number;
begin
select id_applicativo
into d_id_applicativo
from so4_applicativi appl
where modulo = p_modulo
  and appl.istanza = p_istanza;

return so4_competenze_delega_tpk.ins (null
    , p_id
    , p_titolo
    , d_id_applicativo
    , null);
end inserisci_tipo_delega_so4;


   function aggiorna_tipo_delega_so4 (p_id            varchar2
                                    , p_titolo        varchar2
                                    , p_modulo        varchar2
                                    , p_istanza       varchar2)
      return varchar2
   is
      d_id_applicativo         number;
      d_id_competenza_delega   number;
      d_dataval_al             date := null;
begin
select id_applicativo
into d_id_applicativo
from so4_applicativi appl
where modulo = p_modulo
  and appl.istanza = p_istanza;

select id_competenza_delega
into d_id_competenza_delega
from so4_competenze_delega code, so4_applicativi appl
where appl.modulo = p_modulo
  and appl.istanza = p_istanza
  and code.id_applicativo = appl.id_applicativo
  and code.codice = p_id;

so4_competenze_delega_tpk.upd (p_check_old => 0
                                   , p_old_id_competenza_delega => d_id_competenza_delega
                                   , p_new_id_competenza_delega => d_id_competenza_delega
                                   , p_old_codice => p_id
                                   , p_new_codice => p_id
                                   , p_new_id_applicativo => d_id_applicativo
                                   , p_old_id_applicativo => d_id_applicativo
                                   , p_new_descrizione => p_titolo
                                   , p_old_fine_validita => d_dataval_al
                                   , p_new_fine_validita => d_dataval_al);

return p_id;
end aggiorna_tipo_delega_so4;

   function rimuovi_tipo_delega_so4 (p_id varchar2, p_modulo varchar2, p_istanza varchar2)
      return varchar2
   is
      d_id_applicativo         number;
      d_id_competenza_delega   number;
      d_dataval_al             date := sysdate;
begin
select id_applicativo
into d_id_applicativo
from so4_applicativi appl
where modulo = p_modulo
  and appl.istanza = p_istanza;

select id_competenza_delega
into d_id_competenza_delega
from so4_competenze_delega code, so4_applicativi appl
where appl.modulo = p_modulo
  and appl.istanza = p_istanza
  and code.id_applicativo = appl.id_applicativo
  and code.codice = p_id;

so4_competenze_delega_tpk.del (p_check_old => 0, p_id_competenza_delega => d_id_competenza_delega, p_codice => p_id);
return p_id;
end rimuovi_tipo_delega_so4;

       function get_determina_conclusa (p_id_determina number)
       return char
    /********************************************************************
    Funzione che restituisce il se una determina è un atto concluso o meno
    *********************************************************************/
    is   v_count                 number (19) := 0;
   v_cert_conclusi         number (19) := 0;
   v_cert_non_conclusi     number (19) := 0;
   v_tipo_cert             number (19) := 0;
   v_tipo_determina        number (19) := 0;
   v_tipo_cert_pubb        number (19) := 0;
   v_tipo_cert_pubb2       number (19) := 0;
   v_tipo_cert_avv_pubb    number (19) := 0;
   v_tipo_cert_avv_pubb2   number (19) := 0;
   v_tipo_cert_esec        number (19) := 0;
begin
begin
select id_tipo_determina
into v_tipo_determina
from determine
where id_determina = p_id_determina;
exception
      when no_data_found
      then
         return ('N');
end;

select count (*)
into v_count
from visti_pareri
where     id_determina = p_id_determina
  and valido = 'Y'
  and stato <> 'CONCLUSO';

if (v_count > 0)
   then
      return ('N');
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

if (v_tipo_cert_pubb is not null)
   then
select (select count (*)
        from certificati
        where     id_determina = p_id_determina
          and id_tipologia = v_tipo_cert_pubb
          and valido = 'Y'
          and stato <> 'CONCLUSO'),
       (select count (*)
        from certificati
        where     id_determina = p_id_determina
          and id_tipologia = v_tipo_cert_pubb
          and valido = 'Y'
          and stato = 'CONCLUSO')
into v_cert_non_conclusi, v_cert_conclusi
from dual;

if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0)
      then
         return ('N');
end if;
end if;

   if (v_tipo_cert_pubb2 is not null)
   then
select (select count (*)
        from certificati
        where     id_determina = p_id_determina
          and id_tipologia = v_tipo_cert_pubb2
          and valido = 'Y'
          and stato <> 'CONCLUSO'),
       (select count (*)
        from certificati
        where     id_determina = p_id_determina
          and id_tipologia = v_tipo_cert_pubb2
          and valido = 'Y'
          and stato = 'CONCLUSO')
into v_cert_non_conclusi, v_cert_conclusi
from dual;

if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0)
      then
         return ('N');
end if;
end if;

   if (v_tipo_cert_avv_pubb is not null)
   then
select (select count (*)
        from certificati
        where     id_determina = p_id_determina
          and id_tipologia = v_tipo_cert_avv_pubb
          and valido = 'Y'
          and stato <> 'CONCLUSO'),
       (select count (*)
        from certificati
        where     id_determina = p_id_determina
          and id_tipologia = v_tipo_cert_avv_pubb
          and valido = 'Y'
          and stato = 'CONCLUSO')
into v_cert_non_conclusi, v_cert_conclusi
from dual;

if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0)
      then
         return ('N');
end if;
end if;

   if (v_tipo_cert_avv_pubb2 is not null)
   then
select (select count (*)
        from certificati
        where     id_determina = p_id_determina
          and id_tipologia = v_tipo_cert_avv_pubb2
          and valido = 'Y'
          and stato <> 'CONCLUSO'),
       (select count (*)
        from certificati
        where     id_determina = p_id_determina
          and id_tipologia = v_tipo_cert_avv_pubb2
          and valido = 'Y'
          and stato = 'CONCLUSO')
into v_cert_non_conclusi, v_cert_conclusi
from dual;

if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0)
      then
         return ('N');
end if;
end if;

   if (v_tipo_cert_esec is not null)
   then
select (select count (*)
        from certificati
        where     id_determina = p_id_determina
          and id_tipologia = v_tipo_cert_esec
          and valido = 'Y'
          and stato <> 'CONCLUSO'),
       (select count (*)
        from certificati
        where     id_determina = p_id_determina
          and id_tipologia = v_tipo_cert_esec
          and valido = 'Y'
          and stato = 'CONCLUSO')
into v_cert_non_conclusi, v_cert_conclusi
from dual;

if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0)
      then
         return ('N');
end if;
end if;
return ('Y');
end get_determina_conclusa;


    function get_delibera_conclusa (p_id_delibera number)
       return char
    /********************************************************************
    Funzione che restituisce il se una delibera è un atto concluso o meno
    *********************************************************************/
    is v_count                 number (19) := 0;
       v_tipo_delibera         number (19) := 0;
       v_immediata             char(1)     :='N';
       v_cert_conclusi         number (19) := 0;
       v_cert_non_conclusi     number (19) := 0;
       v_tipo_cert             number (19) := 0;
       v_tipo_cert_pubb        number (19) := 0;
       v_tipo_cert_pubb2       number (19) := 0;
       v_tipo_cert_avv_pubb    number (19) := 0;
       v_tipo_cert_avv_pubb2   number (19) := 0;
       v_tipo_cert_eseg        number (19) := 0;
       v_tipo_cert_imm_eseg    number (19) := 0;
begin
begin
select p.id_tipo_delibera, d. eseguibilita_immediata
into v_tipo_delibera, v_immediata
from delibere d, proposte_delibera p
where     d.id_delibera = p_id_delibera
  and p.id_proposta_delibera = d.id_proposta_delibera;
exception
         when no_data_found then
            return ('N');
end;

select count (*)
into v_count
from visti_pareri
where     id_delibera = p_id_delibera
  and valido = 'Y'
  and stato <> 'CONCLUSO';

if (v_count > 0)
       then
          return ('N');
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

if (v_tipo_cert_pubb is not null)
   then
select (select count (*)
        from certificati
        where     id_delibera = p_id_delibera
          and id_tipologia = v_tipo_cert_pubb
          and valido = 'Y'
          and stato <> 'CONCLUSO'),
       (select count (*)
        from certificati
        where     id_delibera = p_id_delibera
          and id_tipologia = v_tipo_cert_pubb
          and valido = 'Y'
          and stato = 'CONCLUSO')
into v_cert_non_conclusi, v_cert_conclusi
from dual;

if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0)
      then
         return ('N');
end if;
end if;

   if (v_tipo_cert_pubb2 is not null)
   then
select (select count (*)
        from certificati
        where     id_delibera = p_id_delibera
          and id_tipologia = v_tipo_cert_pubb2
          and valido = 'Y'
          and stato <> 'CONCLUSO'),
       (select count (*)
        from certificati
        where     id_delibera = p_id_delibera
          and id_tipologia = v_tipo_cert_pubb2
          and valido = 'Y'
          and stato = 'CONCLUSO')
into v_cert_non_conclusi, v_cert_conclusi
from dual;

if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0)
      then
         return ('N');
end if;
end if;

   if (v_tipo_cert_avv_pubb is not null)
   then
select (select count (*)
        from certificati
        where     id_delibera = p_id_delibera
          and id_tipologia = v_tipo_cert_avv_pubb
          and valido = 'Y'
          and stato <> 'CONCLUSO'),
       (select count (*)
        from certificati
        where     id_delibera = p_id_delibera
          and id_tipologia = v_tipo_cert_avv_pubb
          and valido = 'Y'
          and stato = 'CONCLUSO')
into v_cert_non_conclusi, v_cert_conclusi
from dual;

if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0)
      then
         return ('N');
end if;
end if;

   if (v_tipo_cert_avv_pubb2 is not null)
   then
select (select count (*)
        from certificati
        where     id_delibera = p_id_delibera
          and id_tipologia = v_tipo_cert_avv_pubb2
          and valido = 'Y'
          and stato <> 'CONCLUSO'),
       (select count (*)
        from certificati
        where     id_delibera = p_id_delibera
          and id_tipologia = v_tipo_cert_avv_pubb2
          and valido = 'Y'
          and stato = 'CONCLUSO')
into v_cert_non_conclusi, v_cert_conclusi
from dual;

if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0)
      then
         return ('N');
end if;
end if;

   if (v_immediata = 'Y' and v_tipo_cert_imm_eseg is not null)
   then
select (select count (*)
        from certificati
        where     id_delibera = p_id_delibera
          and id_tipologia = v_tipo_cert_imm_eseg
          and valido = 'Y'
          and stato <> 'CONCLUSO'),
       (select count (*)
        from certificati
        where     id_delibera = p_id_delibera
          and id_tipologia = v_tipo_cert_imm_eseg
          and valido = 'Y'
          and stato = 'CONCLUSO')
into v_cert_non_conclusi, v_cert_conclusi
from dual;

if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0)
      then
         return ('N');
end if;

end if;

   if (v_immediata = 'N' and v_tipo_cert_eseg is not null)
   then
select (select count (*)
        from certificati
        where     id_delibera = p_id_delibera
          and id_tipologia = v_tipo_cert_eseg
          and valido = 'Y'
          and stato <> 'CONCLUSO'),
       (select count (*)
        from certificati
        where     id_delibera = p_id_delibera
          and id_tipologia = v_tipo_cert_eseg
          and valido = 'Y'
          and stato = 'CONCLUSO')
into v_cert_non_conclusi, v_cert_conclusi
from dual;

if (v_cert_non_conclusi > 0 or v_cert_conclusi = 0)
      then
         return ('N');
end if;
end if;


return ('Y');
end get_delibera_conclusa;


  function get_esito_rinvio_proposta (p_id_proposta_delibera number)
      return varchar2
    /********************************************************************
    Funzione che restituisce il codice dell'ultimo esito di rinvio
    *********************************************************************/
    is
    v_esito varchar2 (4000);
begin
SELECT  esito.titolo || ' nella Seduta del C.C. del ' || TO_CHAR (esito.data_discussione, 'DD/MM/YYYY')
INTO V_ESITO
FROM (SELECT *
      FROM (  SELECT e.titolo, e.esito_standard, ogg.data_discussione
              FROM odg_esiti e, odg_oggetti_seduta ogg
              WHERE   OGG.ID_PROPOSTA_DELIBERA =  p_id_proposta_delibera
                AND ogg.conferma_esito = 'Y'
                AND ogg.id_esito = e.id_esito
              ORDER BY ogg.data_discussione)
      WHERE ROWNUM = 1) esito
WHERE esito.esito_standard = 'PARZIALE';
RETURN V_ESITO;
end get_esito_rinvio_proposta;

PROCEDURE elimina_componenti_non_validi (pd_id_commissione number)
IS
BEGIN
MERGE INTO odg_commissioni_componenti occ
    USING (select ni, al
           from odg_commissioni_componenti c, as4_v_soggetti sog
           where     valido_al is null
             and not exists
               (select 1
                from as4_v_soggetti_correnti a
                where a.ni = c.ni_componente)
             and not exists
               (select 1
                from as4_v_soggetti aa
                where aa.al > sog.al and aa.ni = sog.ni)
             and c.id_commissione = pd_id_commissione
             and c.ni_componente = sog.ni) upd
    ON (occ.id_commissione = pd_id_commissione and occ.ni_componente = upd.ni)
    WHEN MATCHED
        THEN
        UPDATE SET
            occ.valido = 'N',
            occ.valido_al = upd.al;
END;

FUNCTION allegatiDeterminaVisibili (p_id_determina NUMBER)
      RETURN NUMBER
   IS
      p_ente                                   VARCHAR2 (255);
      v_data_pubblicazione_dal                 DATE;
      v_data_pubblicazione_al                  DATE;
      v_pubblica_allegati   VARCHAR (1);
      d_impostazione                           VARCHAR (1);
BEGIN
SELECT d.ente,
       d.data_pubblicazione,
       d.data_fine_pubblicazione,
       TD.PUBBLICA_ALLEGATI_VIS
INTO p_ente,
    v_data_pubblicazione_dal,
    v_data_pubblicazione_al,
    v_pubblica_allegati
FROM determine d, tipi_determina td
WHERE     d.id_determina = p_id_determina
  AND td.id_tipo_determina = d.id_tipo_determina;

d_impostazione := impostazioni_pkg.get_impostazione ('VIS_GESTIONE_PUBBLCAZIONE_ALLEGATI',p_ente);

      IF (   d_impostazione = 'N'
          OR v_pubblica_allegati = 'Y'
          OR (    trunc(v_data_pubblicazione_dal) <= trunc(SYSDATE)
              AND trunc(v_data_pubblicazione_al) >= trunc(SYSDATE)))
      THEN
         RETURN 1;
END IF;

RETURN 0;
END allegatiDeterminaVisibili;

   FUNCTION allegatiDeliberaVisibili (p_id_delibera NUMBER)
      RETURN NUMBER
   IS
      p_ente                                   VARCHAR2 (255);
      v_data_pubblicazione_dal                 DATE;
      v_data_pubblicazione_al                  DATE;
      v_pubblica_allegati   VARCHAR (1);
      d_impostazione                           VARCHAR (1);
BEGIN
SELECT d.ente,
       d.data_pubblicazione,
       d.data_fine_pubblicazione,
       TD.PUBBLICA_ALLEGATI_VIS
INTO p_ente,
    v_data_pubblicazione_dal,
    v_data_pubblicazione_al,
    v_pubblica_allegati
FROM delibere d, tipi_delibera td, proposte_delibera p
WHERE     d.id_delibera = p_id_delibera
  AND p.id_proposta_delibera = d.id_proposta_delibera
  AND td.id_tipo_delibera = p.id_tipo_delibera;

d_impostazione := impostazioni_pkg.get_impostazione ('VIS_GESTIONE_PUBBLCAZIONE_ALLEGATI',p_ente);

      IF (   d_impostazione = 'N'
          OR v_pubblica_allegati = 'Y'
          OR (    trunc(v_data_pubblicazione_dal) <= trunc(SYSDATE)
              AND trunc(v_data_pubblicazione_al) >= trunc(SYSDATE)))
      THEN
         RETURN 1;
END IF;

RETURN 0;
END allegatiDeliberaVisibili;


function cons_get_codice_aoo (p_ente varchar2)
      return varchar2
       is
      d_codice_aoo   varchar2 (255) := null;
begin
begin
select codice_aoo into d_codice_aoo from so4_aoo where codice_amministrazione=p_ente and codice_ipa is not null and rownum=1 order by progr_aoo ;
exception
         when no_data_found then
            d_codice_aoo   := null;
end;

return d_codice_aoo;
end cons_get_codice_aoo;


function cons_get_descrizione_aoo (p_ente varchar2)
      return varchar2
       is
      d_descrizione_aoo   varchar2 (255) := null;
begin
begin
select descrizione into d_descrizione_aoo from so4_aoo where codice_amministrazione=p_ente and codice_ipa is not null and rownum=1 order by progr_aoo ;
exception
         when no_data_found then
            d_descrizione_aoo   := null;
end;

return d_descrizione_aoo;
end cons_get_descrizione_aoo;

   function cons_get_denominazione_amm (p_ente varchar2)
      return varchar2
       is
      d_denominazione_amm   varchar2 (255) := null;
begin
begin
select DESCRIZIONE into d_denominazione_amm from  ad4_enti where ente = p_ente and rownum=1;
exception
         when no_data_found then
            d_denominazione_amm   := null;
end;

return d_denominazione_amm;
end cons_get_denominazione_amm;

   function cons_indice_allegati_determina (p_id_determina number)
      return varchar2
       is
      d_indice   varchar2 (4000) := null;
begin

BEGIN
FOR al in (SELECT * FROM allegati alle WHERE alle.id_determina = p_id_determina AND alle.valido = 'Y' order by sequenza)
          LOOP
             d_indice := d_indice || al.id_allegato || ' - ' || al.titolo || '#';
END LOOP;
END;

        -- rimuove se presente ultimo #
        d_indice := SUBSTR(d_indice, 0, LENGTH(d_indice) - 1);

return d_indice;
end cons_indice_allegati_determina;

  function cons_indice_allegati_delibera (p_id_delibera number, p_id_proposta_delibera number)
      return varchar2
       is
      d_indice   varchar2 (4000) := null;
begin

BEGIN
FOR al in (SELECT * FROM allegati alle WHERE (alle.id_delibera = p_id_delibera OR alle.id_proposta_delibera = p_id_proposta_delibera) AND alle.valido = 'Y' order by sequenza)
          LOOP
             d_indice := d_indice || al.id_allegato || ' - ' || al.titolo || '#';
END LOOP;
END;

        -- rimuove se presente ultimo #
        d_indice := SUBSTR(d_indice, 0, LENGTH(d_indice) - 1);

return d_indice;
end cons_indice_allegati_delibera;

end utility_pkg;
/