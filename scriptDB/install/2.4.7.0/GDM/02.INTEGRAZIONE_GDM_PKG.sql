create or replace package integrazione_gdm_pkg
as
   function controlla_competenze_determina (p_utente           varchar2
                                          , p_oggetto          varchar2
                                          , p_lettura          number
                                          , p_modifica         number
                                          , p_cancellazione    number
                                          , p_file             number)
      return number;

   function controlla_competenze_visto_par (p_utente           varchar2
                                          , p_oggetto          varchar2
                                          , p_lettura          number
                                          , p_modifica         number
                                          , p_cancellazione    number
                                          , p_file             number)
      return number;

   function controlla_competenze_cert (p_utente           varchar2
                                     , p_oggetto          varchar2
                                     , p_lettura          number
                                     , p_modifica         number
                                     , p_cancellazione    number
                                     , p_file             number)
      return number;

   function controlla_competenze_delibera (p_utente           varchar2
                                         , p_oggetto          varchar2
                                         , p_lettura          number
                                         , p_modifica         number
                                         , p_cancellazione    number
                                         , p_file             number)
      return number;

   function controlla_competenze_prop_del (p_utente           varchar2
                                         , p_oggetto          varchar2
                                         , p_lettura          number
                                         , p_modifica         number
                                         , p_cancellazione    number
                                         , p_file             number)
      return number;

   function controlla_competenze_allegato (p_utente           varchar2
                                         , p_oggetto          varchar2
                                         , p_lettura          number
                                         , p_modifica         number
                                         , p_cancellazione    number
                                         , p_file             number)
      return number;

   function controlla_competenze_documento (p_utente           varchar2
                                          , p_oggetto          varchar2
                                          , p_lettura          number
                                          , p_modifica         number
                                          , p_cancellazione    number
                                          , p_file             number)
      return number;
end integrazione_gdm_pkg;
/
create or replace package body integrazione_gdm_pkg
as
   function controlla_competenze_visto_par (p_utente           varchar2
                                          , p_oggetto          varchar2
                                          , p_lettura          number
                                          , p_modifica         number
                                          , p_cancellazione    number
                                          , p_file             number)
      return number
   is
      d_controlla_riservatezza   number;
      d_id_documento             number (19);
      d_id_visto_parere          varchar (40);
      d_ruolo_accesso            varchar (40);
      d_valido                   varchar2 (1);
      d_nominativo_utente        varchar2 (40);
   begin
      d_id_visto_parere := gdm_f_valore_campo (p_oggetto, 'ID_DOCUMENTO_GRAILS');
      d_controlla_riservatezza := 1;

      if (p_file = 0) then
         d_controlla_riservatezza := 0;
      end if;

-- per i visti / pareri il campo VALIDO è uguale a N anche per i documenti che sono stati gestiti e poi rigenerati dal flusso.
-- quindi devono essere visibili da interfaccia
-- ho creato l'attività su redmine per risolvere il problema: #29783
--      begin
--         select valido
--           into d_valido
--           from visti_pareri
--          where id_visto_parere = d_id_visto_parere;
--
--         if (d_valido = 'N') then
--            return 0;
--         end if;
--      exception
--         when no_data_found then
--            null;
--      end;

      select nominativo
        into d_nominativo_utente
        from ad4_utenti
       where utente = p_utente;

      if (d_nominativo_utente = 'GDM' or d_nominativo_utente = 'AGSDE2' or d_nominativo_utente = 'RPI') then
         return 1;
      end if;

      --se l'utente ha il diritto di accesso AGDAMMI, allora può vedere sempre tutto
      begin
         select ruolo
           into d_ruolo_accesso
           from ad4_v_utenti_ruoli
          where utente = p_utente;

         if (d_ruolo_accesso = 'AGSDE2_AGDAMMI') then
            return 1;
         end if;
      exception
         when others then
            null;
      end;

      begin
         d_id_documento := to_number (d_id_visto_parere);
      exception
         when others then
            return 1;
      end;

      return competenze_pkg.controlla_competenze_visto_par (d_id_documento
                                                          , p_utente
                                                          , p_lettura
                                                          , p_modifica
                                                          , p_cancellazione
                                                          , d_controlla_riservatezza
                                                          , 0);
   end;

   function controlla_competenze_cert (p_utente           varchar2
                                     , p_oggetto          varchar2
                                     , p_lettura          number
                                     , p_modifica         number
                                     , p_cancellazione    number
                                     , p_file             number)
      return number
   is
      d_controlla_riservatezza   number;
      d_id_documento             number (19);
      d_id_certificato           varchar (40);
      d_ruolo_accesso            varchar (40);
      d_valido                   varchar2 (1);
      d_nominativo_utente        varchar2 (40);
   begin
      d_id_certificato := gdm_f_valore_campo (p_oggetto, 'ID_DOCUMENTO_GRAILS');
      d_controlla_riservatezza := 1;

      if (p_file = 0) then
         d_controlla_riservatezza := 0;
      end if;

      begin
         select valido
           into d_valido
           from certificati
          where id_certificato = d_id_certificato;

         if (d_valido = 'N') then
            return 0;
         end if;
      exception
         when no_data_found then
            null;
      end;

      select nominativo
        into d_nominativo_utente
        from ad4_utenti
       where utente = p_utente;

      if (d_nominativo_utente = 'GDM' or d_nominativo_utente = 'AGSDE2' or d_nominativo_utente = 'RPI') then
         return 1;
      end if;

      --se l'utente ha il diritto di accesso AGDAMMI, allora può vedere sempre tutto
      begin
         select ruolo
           into d_ruolo_accesso
           from ad4_v_utenti_ruoli
          where utente = p_utente;

         if (d_ruolo_accesso = 'AGSDE2_AGDAMMI') then
            return 1;
         end if;
      exception
         when others then
            null;
      end;

      begin
         d_id_documento := to_number (d_id_certificato);
      exception
         when others then
            return 1;
      end;

      return competenze_pkg.controlla_competenze_cert (d_id_documento
                                                     , p_utente
                                                     , p_lettura
                                                     , p_modifica
                                                     , p_cancellazione
                                                     , d_controlla_riservatezza
                                                     , 0);
   end;

   function controlla_competenze_prop_del (p_utente           varchar2
                                         , p_oggetto          varchar2
                                         , p_lettura          number
                                         , p_modifica         number
                                         , p_cancellazione    number
                                         , p_file             number)
      return number
   is
      d_controlla_riservatezza   number;
      d_id_documento             number (19);
      d_id_proposta_delibera     varchar (40);
      d_ruolo_accesso            varchar (40);
      d_valido                   varchar2 (1);
      d_nominativo_utente        varchar2 (40);
   begin
      d_id_proposta_delibera := gdm_f_valore_campo (p_oggetto, 'ID_DOCUMENTO_GRAILS');
      d_controlla_riservatezza := 1;

      if (p_file = 0) then
         d_controlla_riservatezza := 0;
      end if;

      begin
         select valido
           into d_valido
           from proposte_delibera
          where id_proposta_delibera = d_id_proposta_delibera;

         if (d_valido = 'N') then
            return 0;
         end if;
      exception
         when no_data_found then
            null;
      end;

      select nominativo
        into d_nominativo_utente
        from ad4_utenti
       where utente = p_utente;

      if (d_nominativo_utente = 'GDM' or d_nominativo_utente = 'AGSDE2' or d_nominativo_utente = 'RPI') then
         return 1;
      end if;

      --se l'utente ha il diritto di accesso AGDAMMI, allora può vedere sempre tutto
      begin
         select ruolo
           into d_ruolo_accesso
           from ad4_v_utenti_ruoli
          where utente = p_utente;

         if (d_ruolo_accesso = 'AGSDE2_AGDAMMI') then
            return 1;
         end if;
      exception
         when others then
            null;
      end;

      begin
         d_id_documento := to_number (d_id_proposta_delibera);
      exception
         when others then
            return 1;
      end;

      return competenze_pkg.controlla_competenze_prop_del (d_id_documento
                                                         , p_utente
                                                         , p_lettura
                                                         , p_modifica
                                                         , p_cancellazione
                                                         , d_controlla_riservatezza
                                                         , 0);
   end;

   function controlla_competenze_delibera (p_utente           varchar2
                                         , p_oggetto          varchar2
                                         , p_lettura          number
                                         , p_modifica         number
                                         , p_cancellazione    number
                                         , p_file             number)
      return number
   is
      d_controlla_riservatezza   number;
      d_id_documento             number (19);
      d_id_delibera              varchar (40);
      d_ruolo_accesso            varchar (40);
      d_valido                   varchar2 (1);
      d_nominativo_utente        varchar2 (40);
   begin
      d_id_delibera := gdm_f_valore_campo (p_oggetto, 'ID_DOCUMENTO_GRAILS');
      d_controlla_riservatezza := 1;

      if (p_file = 0) then
         d_controlla_riservatezza := 0;
      end if;

      begin
         select valido
           into d_valido
           from delibere
          where id_delibera = d_id_delibera;

         if (d_valido = 'N') then
            return 0;
         end if;
      exception
         when no_data_found then
            null;
      end;

      select nominativo
        into d_nominativo_utente
        from ad4_utenti
       where utente = p_utente;

      if (d_nominativo_utente = 'GDM' or d_nominativo_utente = 'AGSDE2' or d_nominativo_utente = 'RPI') then
         return 1;
      end if;

      --se l'utente ha il diritto di accesso AGDAMMI, allora può vedere sempre tutto
      begin
         select ruolo
           into d_ruolo_accesso
           from ad4_v_utenti_ruoli
          where utente = p_utente;

         if (d_ruolo_accesso = 'AGSDE2_AGDAMMI') then
            return 1;
         end if;
      exception
         when others then
            null;
      end;

      begin
         d_id_documento := to_number (d_id_delibera);
      exception
         when others then
            return 1;
      end;

      return competenze_pkg.controlla_competenze_delibera (d_id_documento
                                                         , p_utente
                                                         , p_lettura
                                                         , p_modifica
                                                         , p_cancellazione
                                                         , d_controlla_riservatezza
                                                         , 0);
   end;

   function controlla_competenze_determina (p_utente           varchar2
                                          , p_oggetto          varchar2
                                          , p_lettura          number
                                          , p_modifica         number
                                          , p_cancellazione    number
                                          , p_file             number)
      return number
   is
      d_controlla_riservatezza   number;
      d_id_documento             number (19);
      d_id_determina             varchar (40);
      d_ruolo_accesso            varchar (40);
      d_valido                   varchar2 (1);
      d_nominativo_utente        varchar2 (40);
   begin
      d_id_determina := gdm_f_valore_campo (p_oggetto, 'ID_DOCUMENTO_GRAILS');
      d_controlla_riservatezza := 1;

      if (p_file = 0) then
         d_controlla_riservatezza := 0;
      end if;

      begin
         select valido
           into d_valido
           from determine
          where id_determina = d_id_determina;

         if (d_valido = 'N') then
            return 0;
         end if;
      exception
         when no_data_found then
            null;
      end;

      select nominativo
        into d_nominativo_utente
        from ad4_utenti
       where utente = p_utente;

      if (d_nominativo_utente = 'GDM' or d_nominativo_utente = 'AGSDE2' or d_nominativo_utente = 'RPI') then
         return 1;
      end if;

      --se l'utente ha il diritto di accesso AGDAMMI, allora può vedere sempre tutto
      begin
         select ruolo
           into d_ruolo_accesso
           from ad4_v_utenti_ruoli
          where utente = p_utente;

         if (d_ruolo_accesso = 'AGSDE2_AGDAMMI') then
            return 1;
         end if;
      exception
         when others then
            null;
      end;

      begin
         d_id_documento := to_number (d_id_determina);
      exception
         when others then
            return 1;
      end;

      return competenze_pkg.controlla_competenze_determina (d_id_documento
                                                          , p_utente
                                                          , p_lettura
                                                          , p_modifica
                                                          , p_cancellazione
                                                          , d_controlla_riservatezza
                                                          , 0);
   end;

   function controlla_competenze_allegato (p_utente           varchar2
                                         , p_oggetto          varchar2
                                         , p_lettura          number
                                         , p_modifica         number
                                         , p_cancellazione    number
                                         , p_file             number)
      return number
   is
      d_id_determina              number (19);
      d_id_delibera               number (19);
      d_id_proposta_delibera      number (19);
      d_id_documento              number (19);
      d_id_documento_principale   number (19);
      d_riservato                 varchar (1);
      d_id_allegato               varchar (40);
      p_ente                      varchar (40);
      p_riservato                 varchar (40);
      d_ruolo_accesso             varchar (40);
      d_valido                    varchar2 (1);
      d_nominativo_utente         varchar2 (40);
   begin
      d_id_allegato := gdm_f_valore_campo (p_oggetto, 'ID_DOCUMENTO_GRAILS');

      if (p_file = 0) then
         p_riservato := 'N';
      end if;

      begin
         select valido
           into d_valido
           from allegati
          where id_allegato = d_id_allegato;

         if (d_valido = 'N') then
            return 0;
         end if;
      exception
         when no_data_found then
            null;
      end;

      select nominativo
        into d_nominativo_utente
        from ad4_utenti
       where utente = p_utente;

      if (d_nominativo_utente = 'GDM' or d_nominativo_utente = 'AGSDE2' or d_nominativo_utente = 'RPI') then
         return 1;
      end if;

      --se l'utente ha il diritto di accesso AGDAMMI, allora può vedere sempre tutto
      begin
         select ruolo
           into d_ruolo_accesso
           from ad4_v_utenti_ruoli
          where utente = p_utente;

         if (d_ruolo_accesso = 'AGSDE2_AGDAMMI') then
            return 1;
         end if;
      exception
         when others then
            null;
      end;

      begin
         d_id_documento := to_number (d_id_allegato);
      exception
         when others then
            return 1;
      end;

      select id_determina
           , id_delibera
           , id_proposta_delibera
           , riservato
        into d_id_determina
           , d_id_delibera
           , d_id_proposta_delibera
           , d_riservato
        from allegati
       where id_allegato = d_id_documento;

      if (d_id_delibera is not null) then
         select id_documento_esterno
           into d_id_documento_principale
           from delibere
          where id_delibera = d_id_delibera;

         return controlla_competenze_delibera (p_utente
                                             , d_id_documento_principale
                                             , p_lettura
                                             , p_modifica
                                             , p_cancellazione
                                             , p_file);
      end if;

      if (d_id_proposta_delibera is not null) then
         select id_documento_esterno
           into d_id_documento_principale
           from proposte_delibera
          where id_proposta_delibera = d_id_proposta_delibera;

         return controlla_competenze_prop_del (p_utente
                                             , d_id_documento_principale
                                             , p_lettura
                                             , p_modifica
                                             , p_cancellazione
                                             , p_file);
      end if;

      if (d_id_determina is not null) then
         select id_documento_esterno
           into d_id_documento_principale
           from determine
          where id_determina = d_id_determina;

         return controlla_competenze_determina (p_utente
                                              , d_id_documento_principale
                                              , p_lettura
                                              , p_modifica
                                              , p_cancellazione
                                              , p_file);
      end if;

      return 1;
   end;

   function controlla_competenze_documento (p_utente           varchar2
                                          , p_oggetto          varchar2
                                          , p_lettura          number
                                          , p_modifica         number
                                          , p_cancellazione    number
                                          , p_file             number)
      return number
   is
      d_controlla_riservatezza   number;
      d_id_documento             number (19);
      d_id_documento_string      varchar (40);
      d_ruolo_accesso            varchar (40);
      d_valido                   varchar2 (1);
      d_nominativo_utente        varchar2 (40);
   begin
      d_id_documento_string := gdm_f_valore_campo (p_oggetto, 'ID_DOCUMENTO_GRAILS');
      d_controlla_riservatezza := 1;

      if (p_file = 0) then
         d_controlla_riservatezza := 0;
      end if;

      begin
         select valido
           into d_valido
           from gdo_documenti
          where id_documento = d_id_documento_string;

         if (d_valido = 'N') then
            return 0;
         end if;
      exception
         when no_data_found then
            null;
      end;

      select nominativo
        into d_nominativo_utente
        from ad4_utenti
       where utente = p_utente;

      if (d_nominativo_utente = 'GDM' or d_nominativo_utente = 'AGSDE2' or d_nominativo_utente = 'RPI') then
         return 1;
      end if;

      --se l'utente ha il diritto di accesso AGDAMMI, allora può vedere sempre tutto
      begin
         select ruolo
           into d_ruolo_accesso
           from ad4_v_utenti_ruoli
          where utente = p_utente;

         if (d_ruolo_accesso = 'AGSDE2_AGDAMMI') then
            return 1;
         end if;
      exception
         when others then
            null;
      end;

      begin
         d_id_documento := to_number (d_id_documento_string);
      exception
         when others then
            return 1;
      end;

      return competenze_pkg.controlla_competenze_doc (d_id_documento
                                                    , p_utente
                                                    , p_lettura
                                                    , p_modifica
                                                    , p_cancellazione
                                                    , d_controlla_riservatezza
                                                    , 0);
   end;
end integrazione_gdm_pkg;
/