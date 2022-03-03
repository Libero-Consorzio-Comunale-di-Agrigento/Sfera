--liquibase formatted sql
--changeset rdestasio:install_20200221_integrazioni_10

CREATE OR REPLACE package            jwf_utility_pkg
as
   /******************************************************************************
      NAME:       jwf_utility_pkg
      PURPOSE:

      REVISIONS:
      Ver        Date        Author           Description
      ---------  ----------  ---------------  ------------------------------------
      1.0        29/09/2014      esasdelli       1. Created this package.
   ******************************************************************************/

   function is_in_step (p_id_riferimento in varchar2, p_nome_step in varchar2)
      return number;

   function is_in_step_unita (p_id_riferimento   in varchar2
                            , p_nome_step        in varchar2
                            , p_tipo_oggetto     in varchar2
                            , p_unita_progr         number)
      return number;

   /*
    * Questa funzione è usata dal comune di modena dal JWF nella tabella "desktop"
    *
    * Ritorna il tipo_oggetto del documento dato l'id_riferimento (ad es: 'DETERMINA', 'DELIBERA' etc)
    */
   function get_oggetto (p_id_riferimento in varchar2)
      return varchar2;

   /*
    * Ritorna 1 o 0 in base al fatto che l'utente possa creare una delibera / determina o accedere agli atti
    * Usata dalla smartDesktop
    */
   function get_competenza (p_utente in varchar2, p_codice_azione varchar2)
      return number;
end jwf_utility_pkg;
/

CREATE OR REPLACE package body            jwf_utility_pkg
is

    function get_oggetto (p_id_riferimento in varchar2)
      return varchar2
    is
      d_id_documento   number (30);
      d_tipo_oggetto varchar2(255);
    begin
      d_tipo_oggetto := trim('_' from regexp_substr(p_id_riferimento, '[A-Z_]+_' ));

      if (d_tipo_oggetto = 'VISTO') then
        d_id_documento := to_number (regexp_substr (p_id_riferimento, '[0-9]+$'));
        select decode(id_determina, null, 'PARERE', 'VISTO') into d_tipo_oggetto from visti_pareri where id_visto_parere = d_id_documento;
      end if;

      return d_tipo_oggetto;
    end;

   function is_in_step (p_id_riferimento in varchar2, p_nome_step in varchar2)
      return number
   /******************************************************************************
    NOME:        is_in_step
    DESCRIZIONE: restituisce 1 se il documento è nello step specificato
    PARAMETRI:   p_id_riferimento: id del documento
                 p_nome_step: nome dello step
    RITORNA:     NUMBER: 1 se il documento è nello step indicato, 0 altrimenti
   ******************************************************************************/
   is
      d_ret            number (19);
      d_id_documento   number (30);
   begin
      d_id_documento := to_number (regexp_substr (p_id_riferimento, '[0-9]+$'));

      select count (1)
        into d_ret
        from documenti_step s
       where s.id_documento = d_id_documento and s.step_nome like '%' || p_nome_step || '%';

      if (d_ret > 0) then
         d_ret := 1;
      end if;

      return d_ret;
   end is_in_step;

   function is_in_step_unita (p_id_riferimento   in varchar2
                            , p_nome_step        in varchar2
                            , p_tipo_oggetto     in varchar2
                            , p_unita_progr         number)
      return number
   /******************************************************************************
    NOME:        is_in_step_unita
    DESCRIZIONE: restituisce 1 se il documento è nello step specificato e l'unità che lo gestisce è quella specificata
    PARAMETRI:   p_id_riferimento: id del documento
                 p_nome_step: nome dello step
                 p_tipo_oggetto: valori possibili DETERMINA, DELIBERA, PROPOSTA_DELIBERA, VISTO_PARERE
                 p_unita_progr: progressivo dell'unità
    RITORNA:     NUMBER: 1 se il documento è nello step indicato e se l'unità corrisponde, 0 altrimenti
   ******************************************************************************/
   is
      d_ret            number (19);
      d_id_documento   number (30);
   begin
      if (p_tipo_oggetto = 'VISTO_PARERE') then
         --per ora questa funzione è richiesta solo per i visti / pareri
         d_id_documento := to_number (regexp_substr (p_id_riferimento, '[0-9]+$'));

         select count (1)
           into d_ret
           from visti_pareri
          where id_visto_parere = d_id_documento and unita_progr = p_unita_progr;

         if (d_ret > 0) then
            select count (1)
              into d_ret
              from documenti_step s
             where s.id_documento = d_id_documento and s.step_nome like '%' || p_nome_step || '%';

            if (d_ret > 0) then
               d_ret := 1;
            end if;
         end if;
      end if;
      return d_ret;
   end is_in_step_unita;

   function get_competenza (p_utente in varchar2, p_codice_azione varchar2)
      return number
   /******************************************************************************
    NOME:        get_competenza
    DESCRIZIONE: Ritorna 1 o 0 in base al fatto che l'utente possa creare una delibera / determina o accedere agli atti
    PARAMETRI:   p_utente: utente collegato
                 p_codice_azione: azione da controllare (CREA_DETERMINA, CREA_DELIBERA, ATTI)
    RITORNA:     NUMBER: 1 se l'utente può eseguire l'azione, 0 altrimenti
   ******************************************************************************/
   is
      d_ret                 number;
      d_ente                varchar2 (255);
      d_nominativo_utente   varchar2 (255);
      d_ruolo_accesso       varchar2 (255);
      d_ruolo               varchar2 (255);
      d_num_ruolo           number;
      d_ni                  number;
   begin
      d_ente   := impostazioni_pkg.get_impostazione ('ENTI_SO4', null);

      select nominativo
        into d_nominativo_utente
        from ad4_utenti
       where utente = p_utente;

      if (d_nominativo_utente = 'AGSDE2') then
         return 1;
      end if;

      --se l'utente ha il diritto di accesso AGDAMMI, allora può vedere sempre tutto
      begin
         select count (*)
           into d_ruolo_accesso
           from ad4_v_utenti_ruoli
          where utente = p_utente
            and ruolo = 'AGSDE2_AGDAMMI';

         if (d_ruolo_accesso = 1) then
            return 1;
         end if;
      exception
         when others then
            null;
      end;

      if p_codice_azione = 'CREA_DETERMINA' then
         select impostazioni_pkg.get_impostazione ('RUOLO_SO4_CREA_DETERMINA', d_ente) into d_ruolo from dual;
      else
         if p_codice_azione = 'CREA_DELIBERA' then
            select impostazioni_pkg.get_impostazione ('RUOLO_SO4_CREA_PROPOSTA_DELIBERA', d_ente) into d_ruolo from dual;
         else
            if p_codice_azione = 'ATTI' then
               select impostazioni_pkg.get_impostazione ('RUOLO_ACCESSO_APPLICATIVO', d_ente) into d_ruolo from dual;
            end if;
         end if;
      end if;

      select ni
        into d_ni
        from as4_v_soggetti_correnti
       where utente = p_utente;

      --verifico se l'utente ha il ruolo indicato (la vista filtra già per le assegnazioni attive alla data odierna)
      begin
          select ni
            into d_ni
            from as4_v_soggetti_correnti
           where utente = p_utente;

          --verifico se l'utente ha il ruolo indicato (la vista filtra già per le assegnazioni attive alla data odierna)
          select count (1)
            into d_num_ruolo
            from so4_v_utenti_ruoli_sogg_uo
           where id_soggetto = d_ni
             and ruolo = d_ruolo;

          if d_num_ruolo >= 1 then
             d_ret   := 1;
          else
             d_ret   := 0;
          end if;
        exception
         when others then
            d_ret := 0;
      end;

      return d_ret;
   end get_competenza;
end jwf_utility_pkg;
/
