--liquibase formatted sql
--changeset rdestasio:install_20200221_senza_deleghe_02

-- queste cose servono solo fintanto che agsde2 viene installato dove c'è un so4 non aggiornato.

create or replace view so4_applicativi_fake as
SELECT
0 ID_APPLICATIVO, 'descrizione' DESCRIZIONE, 'istanza' ISTANZA,
   'modulo' MODULO, 'utente' UTENTE_AGGIORNAMENTO, sysdate DATA_AGGIORNAMENTO
FROM dual
/

create or replace view so4_competenze_delega_fake as
SELECT
1 ID_COMPETENZA_DELEGA, 'descrizione' DESCRIZIONE, 'codice' CODICE,
   1 ID_APPLICATIVO, sysdate FINE_VALIDITA, 'utente' UTENTE_AGGIORNAMENTO,
   sysdate DATA_AGGIORNAMENTO
FROM dual
/

CREATE OR REPLACE PACKAGE so4_competenze_delega_tpk_fake
IS

   FUNCTION ins (
      p_id_competenza_delega          IN number,
      p_codice          IN varchar2,
      p_descrizione     IN varchar2,
      p_id_applicativo     IN number,
      p_fine_validita   IN date DEFAULT NULL)
      RETURN NUMBER;

   -- Aggiornamento di una riga
   PROCEDURE upd (
      p_check_old           IN INTEGER DEFAULT 0,
      p_new_id_competenza_delega IN number,
      p_old_id_competenza_delega IN number DEFAULT NULL,
      p_new_codice          IN varchar2 default null,
      p_old_codice          IN varchar2 default null,
      p_new_descrizione     IN varchar2 default null,
      p_old_descrizione     in varchar2 default null,
      p_new_id_applicativo     IN number default null,
      p_old_id_applicativo     IN number default null,
      p_new_fine_validita   IN date default null,
      p_old_fine_validita   IN date default null);

   -- Cancellazione di una riga
   PROCEDURE del /*+ SOA  */
      (p_check_old            IN INTEGER DEFAULT 0,
      p_id_competenza_delega          IN number,
      p_codice          IN varchar2 DEFAULT NULL,
      p_descrizione     IN varchar2 DEFAULT NULL,
      p_id_applicativo     IN number DEFAULT NULL,
      p_fine_validita   IN date DEFAULT NULL);

END so4_competenze_delega_tpk_fake;
/

CREATE OR REPLACE PACKAGE BODY so4_competenze_delega_tpk_fake
IS
   FUNCTION ins (
      p_id_competenza_delega          IN number,
      p_codice          IN varchar2,
      p_descrizione     IN varchar2,
      p_id_applicativo     IN number,
      p_fine_validita   IN date DEFAULT NULL)
      RETURN NUMBER as
   begin
    return 1;
   end;

   -- Aggiornamento di una riga
   PROCEDURE upd /*+ SOA  */
                 (
       p_check_old           IN INTEGER DEFAULT 0,
      p_new_id_competenza_delega IN number,
      p_old_id_competenza_delega IN number DEFAULT NULL,
      p_new_codice          IN varchar2 default null,
      p_old_codice          IN varchar2 default null,
      p_new_descrizione     IN varchar2 default null,
      p_old_descrizione     in varchar2 default null,
      p_new_id_applicativo     IN number default null,
      p_old_id_applicativo     IN number default null,
      p_new_fine_validita   IN date default null,
      p_old_fine_validita   IN date default null) as
      begin
      null;
      end;

   -- Cancellazione di una riga
   PROCEDURE del /*+ SOA  */
      (p_check_old            IN INTEGER DEFAULT 0,
      p_id_competenza_delega          IN number,
      p_codice          IN varchar2 DEFAULT NULL,
      p_descrizione     IN varchar2 DEFAULT NULL,
      p_id_applicativo     IN number DEFAULT NULL,
      p_fine_validita   IN date DEFAULT NULL)  as
      begin
      null;
      end;

END so4_competenze_delega_tpk_fake;
/