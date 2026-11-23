--liquibase formatted sql
--changeset mfrancesconi:2.5_202507214_00.agsde2_impostazioni_pkg runOnChange:true

begin
    execute immediate 'ALTER TABLE IMPOSTAZIONI ADD (chiave_registro CHAR(512 BYTE))';
    exception when others then null;
end;
/

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
   is
   d_trovato    number;
   begin
      for c in (select ente
                  from impostazioni
                 where codice = 'OTTICA_SO4')
      loop
         select count (1)
           into d_trovato
           from impostazioni
          where ente = c.ente and codice = p_codice;

         if (d_trovato = 0) then
            insert into impostazioni (codice,
                                      ente,
                                      version,
                                      caratteristiche,
                                      descrizione,
                                      etichetta,
                                      modificabile,
                                      predefinito,
                                      valore)
                 values (p_codice,
                         c.ente,
                         0,
                         p_caratteristiche,
                         p_descrizione,
                         p_etichetta,
                         p_modificabile,
                         p_predefinito,
                         p_predefinito);
         else
            update impostazioni
               set caratteristiche = p_caratteristiche,
                   descrizione = p_descrizione,
                   etichetta = p_etichetta,
                   modificabile = p_modificabile,
                   predefinito = p_predefinito
             where ente = c.ente and codice = p_codice;
         end if;
      end loop;
   end;

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
   is
      d_valore_impostazione   impostazioni.valore%type;
      d_ente                  impostazioni.valore%type;
    begin
        if (p_ente is null or p_ente = '*') then
            select valore
              into d_ente
              from WKF_IMPOSTAZIONI
             where codice = 'ENTI_SO4'
               and rownum = 1; --da rivedere in caso di multiente
         /*
            select valore
              into d_ente
              from impostazioni i
             where i.chiave_registro like 'PRODUCTS/%/AGSDE2/AGSDE2/IMPOSTAZIONI/ENTI_SO4'
               and rownum = 1; --da rivedere in caso di multiente
              */
         else
            d_ente := p_ente;
        end if;

        begin
            select valore
              into d_valore_impostazione
              from impostazioni i
             where i.chiave_registro = 'PRODUCTS/' || d_ente || '/AGSDE2/AGSDE2/IMPOSTAZIONI/' || p_codice;
        exception when no_data_found then
                select valore
                  into d_valore_impostazione
                  from impostazioni i
                 where i.chiave_registro = 'PRODUCTS/*/AGSDE2/AGSDE2/IMPOSTAZIONI/' || p_codice;
        end;
    return d_valore_impostazione;
   end get_impostazione;

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
