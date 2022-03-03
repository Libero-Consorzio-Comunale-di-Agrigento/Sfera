--liquibase formatted sql
--changeset rdestasio:2.1.2.0_20200221_04

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
