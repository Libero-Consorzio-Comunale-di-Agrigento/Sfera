--liquibase formatted sql
--changeset rdestasio:2.0.1.0_20200221_04

create or replace PROCEDURE compile_all
/******************************************************************************
 NOME:        Compile_All
 DESCRIZIONE: Compilazione di tutti gli oggetti invalidi presenti nel DB.
 ARGOMENTI:   p_java_class NUMBER indica se deve essere effettuata la
                                  compilazione anche degli oggetti di tipo
                                  JAVA CLASS.
 ANNOTAZIONI: Tenta la compilazione in cicli successivi.
              Termina la compilazione quando il numero degli oggetti
              invalidi non varia rispetto al ciclo precedente.
 REVISIONI:
 Rev. Data        Autore  Descrizione
 ---- ----------  ------  ----------------------------------------------------
 1    23/01/2001  MF      Inserimento commento.
 2    17/12/2003  MM      Aggiunta compilazione classi java
 4    14/12/2006  MM      Introduzione del parametro p_java_class.
 5    08/10/2007  FT      Aggiunta compilazione synonym
 6    12/12/2007  FT      compile_all: esclusione degli oggetti il cui nome
                          inizia con 'BIN$'
******************************************************************************/
( p_java_class in number default 1 )
IS
   d_obj_name       VARCHAR2(30);
   d_obj_type       VARCHAR2(30);
   d_command        VARCHAR2(200);
   d_cursor         INTEGER;
   d_rows           INTEGER;
   d_old_rows       INTEGER;
   d_return         INTEGER;
   s_oracle_ver     integer;
   CURSOR c_obj IS
      SELECT object_name, object_type
        FROM OBJ
       WHERE ( object_type IN ( 'PROCEDURE'
                              , 'TRIGGER'
                              , 'FUNCTION'
                              , 'PACKAGE'
                              , 'PACKAGE BODY'
                              , 'VIEW')
             OR (object_type = 'JAVA CLASS' AND p_java_class = 1)
             OR (object_type = 'SYNONYM' AND (SELECT to_number(substr(version, 0, instr(version, '.')-1)) FROM PRODUCT_COMPONENT_VERSION where product like 'Oracle%') >= 10)
             )
       AND   status = 'INVALID'
       AND   substr( object_name, 1, 4 ) != 'BIN$'
      ORDER BY  DECODE(object_type
                      ,'PACKAGE',1
                      ,'PACKAGE BODY',2
                      ,'FUNCTION',3
                      ,'PROCEDURE',4
                      ,'VIEW',5
                             ,6)
              , object_name
      ;
BEGIN
   d_old_rows := 0;
   LOOP
      d_rows := 0;
      BEGIN
         OPEN  c_obj;
         LOOP
            BEGIN
               FETCH c_obj INTO d_obj_name, d_obj_type;
               EXIT WHEN c_obj%NOTFOUND;
               d_rows := d_rows + 1;
               IF d_obj_type = 'PACKAGE BODY' THEN
                  d_command := 'alter PACKAGE '||d_obj_name||' compile BODY';
               ELSIF d_obj_type = 'JAVA CLASS' THEN
                  d_command := 'alter '||d_obj_type||' "'||d_obj_name||'" compile';
               ELSE
                  d_command := 'alter '||d_obj_type||' '||d_obj_name||' compile';
               END IF;
               d_cursor  := DBMS_SQL.OPEN_CURSOR;
               DBMS_SQL.PARSE(d_cursor,d_command,dbms_sql.native);
               d_return := DBMS_SQL.EXECUTE(d_cursor);
               DBMS_SQL.CLOSE_CURSOR(d_cursor);
            EXCEPTION
               WHEN OTHERS THEN NULL;
            END;
         END LOOP;
         CLOSE c_obj;
      END;
      IF d_rows = d_old_rows THEN
         EXIT;
      ELSE
         d_old_rows := d_rows;
      END IF;
   END LOOP;
   IF d_rows > 0 THEN
      RAISE_APPLICATION_ERROR(-20999,'Esistono n.'||TO_CHAR(d_rows)||' Oggetti di DataBase non validabili !');
   END IF;
END Compile_All;
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

CREATE OR REPLACE PACKAGE          impostazioni_pkg AS
/******************************************************************************
   NAME:       impostazioni_pkg
   PURPOSE:

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        18/02/2014      esasdelli       1. Created this package.
******************************************************************************/

  function get_impostazione (p_codice IN varchar2, p_ente in varchar2) RETURN varchar2;
  
  procedure set_impostazione (p_codice IN varchar2, p_ente in varchar2, p_valore in varchar2);

END impostazioni_pkg;

/

CREATE OR REPLACE package          reporter_pkg
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
      
   function get_date_gettoni_presenza(p_chiave_calcolo varchar2, p_utente in varchar2)
      return varchar2;

   function get_presenti_oggetto_seduta (p_id_oggetto_seduta number, p_presente varchar2, p_ruolo varchar2)
      return varchar2;

   function get_presenti_seduta (p_id_seduta number, p_presente varchar2, p_ruolo varchar2)
      return varchar2;
      
   function get_testo_proposta_delibera(p_id_delibera number)
   return blob;

   function get_votanti_oggetto_seduta (p_id_oggetto_seduta number, p_voto varchar2)
      return varchar2;
                 
   function numero_lettere (a_numero in number)
      return varchar2;
      
   function mese_lettere (a_numero in number)
      return varchar2;
      
   function giorno_lettere (p_data in date)
      return varchar2;
      
   function get_desc_ascendenti_unita(p_codice_uo varchar2, p_data date default null, p_ottica varchar2, p_separatore varchar2 default null)
      return varchar2;
end reporter_pkg;

/


CREATE OR REPLACE package body          assistenza_pkg
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

   delete from delibere;
   delete from proposte_delibera;

   delete from determine;

   delete from file_allegati;
   delete from gte_lock;

   delete from email;

   update wkf_engine_iter
      set id_step_corrente = null;
      
   delete from wkf_engine_step_attori;
   delete from wkf_engine_step;
   delete from wkf_engine_iter;
   delete from file_allegati_storico;
      
     
      
/*
DECLARE 
  RetVal NUMBER;
BEGIN 
    RetVal := ${global.db.gdm.username}.F_ELIMINA_DOCUMENTI ( 'SEGRETERIA.ATTI.2_0', 'DETERMINA', 'N');
    RetVal := ${global.db.gdm.username}.F_ELIMINA_DOCUMENTI ( 'SEGRETERIA.ATTI.2_0', 'DELIBERA', 'N');
    RetVal := ${global.db.gdm.username}.F_ELIMINA_DOCUMENTI ( 'SEGRETERIA.ATTI.2_0', 'PROPOSTA_DELIBERA', 'N');
    RetVal := ${global.db.gdm.username}.F_ELIMINA_DOCUMENTI ( 'SEGRETERIA.ATTI.2_0', 'VISTO', 'N');
    RetVal := ${global.db.gdm.username}.F_ELIMINA_DOCUMENTI ( 'SEGRETERIA.ATTI.2_0', 'CERTIFICATO', 'N');
    RetVal := ${global.db.gdm.username}.F_ELIMINA_DOCUMENTI ( 'SEGRETERIA.ATTI.2_0', 'ALLEGATO', 'N');
    COMMIT; 
END;
*/      
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
        
        -- cambio l'ottica dell'applicativo
        IMPOSTAZIONI_PKG.SET_IMPOSTAZIONE('ENTI_SO4', '*', p_nuova_ottica);
        
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
             || ' and '||c.column_name||' '||c.vecchio_valore||' ';
       end loop;
   end;
end assistenza_pkg;

/

CREATE OR REPLACE package body          impostazioni_pkg
is
   FUNCTION get_impostazione (p_codice IN varchar2, p_ente in varchar2) RETURN varchar2
      /******************************************************************************
       NOME:        get_impostazione
       DESCRIZIONE: restituisce il valore dell'impostazione per l'ente specificato.
       PARAMETRI:   --
       RITORNA:     STRINGA VARCHAR2 CONTENENTE VERSIONE E DATA.
       NOTE:        IL SECONDO NUMERO DELLA VERSIONE CORRISPONDE ALLA REVISIONE
                    DEL PACKAGE.
      ******************************************************************************/
   is
       d_valore_impostazione IMPOSTAZIONI.VALORE%type;
   begin
        begin
            select valore
              into d_valore_impostazione
              from impostazioni i
             where i.ente   = p_ente
               and i.codice = p_codice;
        exception when no_data_found then
        
             select valore
              into d_valore_impostazione
              from impostazioni i
             where i.ente   = '*'
               and i.codice = p_codice;
        
        end;
        
      return d_valore_impostazione;
   end get_impostazione;
   
   procedure set_impostazione (p_codice IN varchar2, p_ente in varchar2, p_valore in varchar2)
      /******************************************************************************
       NOME:        set_impostazione
       DESCRIZIONE: imposta il valore dell'impostazione per l'ente specificato.
       PARAMETRI:   --
       RITORNA:     STRINGA VARCHAR2 CONTENENTE VERSIONE E DATA.
       NOTE:        IL SECONDO NUMERO DELLA VERSIONE CORRISPONDE ALLA REVISIONE
                    DEL PACKAGE.
      ******************************************************************************/
   is
   begin
      -- ottengo il valore precedente (in caso debba concatenare 
      update impostazioni set valore = p_valore where codice = p_codice and ente = p_ente;
   end set_impostazione;

end impostazioni_pkg;

/



CREATE OR REPLACE package body          reporter_pkg
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
      for c in (select distinct to_char (data_seduta, 'dd/mm/yyyy') data_seduta
                  from odg_gettoni_presenza ogp_utente
                 where ogp_utente.utente = p_utente
                   and chiave_calcolo = p_chiave_calcolo)
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
         in (  select decode (osp.id_commissione_componente, null, as4_soggetti_esterno.cognome, as4_soggetti_interno.cognome) cognome
                    ,  decode (osp.id_commissione_componente, null, as4_soggetti_esterno.nome, as4_soggetti_interno.nome) nome
                 from as4_v_soggetti_correnti as4_soggetti_interno
                    ,  as4_v_soggetti_correnti as4_soggetti_esterno
                    ,  odg_sedute_partecipanti osp
                    ,  odg_oggetti_partecipanti oop
                    ,  odg_oggetti_seduta oos
                    ,  odg_commissioni_componenti occ
                    ,  odg_ruoli_soggetti ors
                where oos.id_oggetto_seduta = p_id_oggetto_seduta
                  and oos.id_oggetto_seduta = oop.id_oggetto_seduta
                  and osp.id_seduta_partecipante = oop.id_seduta_partecipante
                  and occ.id_commissione_componente(+) = osp.id_commissione_componente
                  and osp.ni_componente_esterno = as4_soggetti_esterno.ni(+)
                  and occ.ni_componente = as4_soggetti_interno.ni(+)
                  and ( (p_ruolo is null)
                    or (p_ruolo is not null
                    and oop.ruolo_partecipante = p_ruolo))
                  and ( (p_presente is null)
                    or (p_presente is not null
                    and oop.presente = p_presente))
                  and occ.ni_componente = ors.ni_soggetto(+)
             order by occ.sequenza, as4_soggetti_interno.denominazione, as4_soggetti_esterno.denominazione asc)
      loop
         d_cognome := upper (substr (c.cognome, 1, 1)) || lower (substr (c.cognome, 2));
         d_nome := upper (substr (c.nome, 1, 1)) || lower (substr (c.nome, 2));
         d_elenco_presenti := d_elenco_presenti || d_cognome || ' ' || d_nome || ', ';
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
         in (  select decode (osp.id_commissione_componente, null, as4_soggetti_esterno.cognome, as4_soggetti_interno.cognome) cognome
                    ,  decode (osp.id_commissione_componente, null, as4_soggetti_esterno.nome, as4_soggetti_interno.nome) nome
                 from as4_v_soggetti_correnti as4_soggetti_interno
                    ,  as4_v_soggetti_correnti as4_soggetti_esterno
                    ,  odg_sedute_partecipanti osp
                    ,  odg_commissioni_componenti occ
                    ,  odg_ruoli_soggetti ors
                where osp.id_seduta = p_id_seduta
                  and occ.id_commissione_componente(+) = osp.id_commissione_componente
                  and osp.ni_componente_esterno = as4_soggetti_esterno.ni(+)
                  and occ.ni_componente = as4_soggetti_interno.ni(+)
                  and ( (p_ruolo is null)
                    or (p_ruolo is not null
                    and osp.ruolo_partecipante = p_ruolo))
                  and ( (p_presente is null)
                    or (p_presente is not null
                    and osp.presente = p_presente))
                  and occ.ni_componente = ors.ni_soggetto(+)
             order by occ.sequenza, as4_soggetti_interno.denominazione, as4_soggetti_esterno.denominazione asc)
      loop
         d_cognome := upper (substr (c.cognome, 1, 1)) || lower (substr (c.cognome, 2));
         d_nome := upper (substr (c.nome, 1, 1)) || lower (substr (c.nome, 2));
         d_elenco_presenti := d_elenco_presenti || d_cognome || ' ' || d_nome || ', ';
      end loop;

      if (d_elenco_presenti is not null) then
         d_elenco_presenti := substr (d_elenco_presenti, 1, length (d_elenco_presenti) - 2) || '.';
      end if;
      return d_elenco_presenti;
   end get_presenti_seduta;

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
      d_testo_proposta   blob;
      d_integrazione_gdm varchar(1);
      d_ente             DELIBERE.ENTE%type;
   begin
      dbms_lob.createtemporary (d_testo_proposta, true);
      
      select ente
        into d_ente
        from delibere d
       where d.id_delibera = p_id_delibera;
      
      d_integrazione_gdm := impostazioni_pkg.get_impostazione ('INTEGRAZIONE_GDM', d_ente);
      
      if d_integrazione_gdm = 'Y'
      then
         
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
                       ,  ${global.db.gdm.username}.oggetti_file gom
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

   function get_votanti_oggetto_seduta (p_id_oggetto_seduta number, p_voto varchar2)
      return varchar2
   is
      d_cognome   varchar2 (1000);
      d_nome      varchar2 (1000);
      d_elenco    varchar2 (32767);
   begin
      for c
         in (  select decode (osp.id_commissione_componente, null, as4_soggetti_esterno.cognome, as4_soggetti_interno.cognome) cognome
                    ,  decode (osp.id_commissione_componente, null, as4_soggetti_esterno.nome, as4_soggetti_interno.nome) nome
                 from as4_v_soggetti_correnti as4_soggetti_interno
                    ,  as4_v_soggetti_correnti as4_soggetti_esterno
                    ,  odg_sedute_partecipanti osp
                    ,  odg_oggetti_partecipanti oop
                    ,  odg_oggetti_seduta oos
                    ,  odg_commissioni_componenti occ
                    ,  odg_voti ov
                where oos.id_oggetto_seduta = p_id_oggetto_seduta
                  and oos.id_oggetto_seduta = oop.id_oggetto_seduta
                  and osp.id_seduta_partecipante = oop.id_seduta_partecipante
                  and occ.id_commissione_componente(+) = osp.id_commissione_componente
                  and osp.ni_componente_esterno = as4_soggetti_esterno.ni(+)
                  and occ.ni_componente = as4_soggetti_interno.ni(+)
                  and oop.id_voto = ov.id_voto(+)
                  and ( (p_voto is null)
                    or (p_voto is not null
                    and ov.valore = p_voto))
             order by occ.sequenza, as4_soggetti_interno.denominazione, as4_soggetti_esterno.denominazione asc)
      loop
         d_cognome := upper (substr (c.cognome, 1, 1)) || lower (substr (c.cognome, 2));
         d_nome := upper (substr (c.nome, 1, 1)) || lower (substr (c.nome, 2));
         d_elenco := d_elenco || d_cognome || ' ' || d_nome || ', ';
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
   
   function get_desc_ascendenti_unita(p_codice_uo varchar2, p_data date default null, p_ottica varchar2, p_separatore varchar2 default null)
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
   
   v_struttura         sys_refcursor;

   d_ottica            varchar2(18);
   d_separatore        varchar2(1):= chr(10);
   d_desc_suddivisione varchar2(60);
   d_result            varchar2(32000);

   TYPE v_struttura_t IS RECORD (
      progr_uo        number(8),
      codice_uo       varchar2(50),
      desc_uo         varchar2(240),
      dal             date,
      al              date,
      id_suddivisione number(8)
      );
      
   v_struttura_row v_struttura_t;
 
   begin
      d_ottica := so4_util.set_ottica_default(p_ottica); 
   
      if p_separatore is not null then
         d_separatore := p_separatore;
      end if;
   
      v_struttura:= so4_util.unita_get_ascendenti_sudd(p_codice_uo,p_data,d_ottica);
      
      if v_struttura%isopen then
         loop
            fetch v_struttura
            into  v_struttura_row;
            exit when v_struttura%notfound;
         
         
            begin
               select descrizione
                 into d_desc_suddivisione
                 from so4_suddivisioni_struttura   
                where ottica = p_ottica
                  and id_suddivisione = v_struttura_row.id_suddivisione;
            
               exception when others then
                  d_desc_suddivisione := null;
            end;
                  
            d_result := d_desc_suddivisione||': '||v_struttura_row.desc_uo||d_separatore||d_result;
               
         end loop;
     
      end if;
   
      return d_result;
   
      exception when others then
         return null;   
end;
end reporter_pkg;
/


-- ricompilo tutto
begin
	compile_all();
end;
/