CREATE OR REPLACE PACKAGE          jwf_utility_pkg AS
/******************************************************************************
   NAME:       jwf_utility_pkg
   PURPOSE:

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        29/09/2014      esasdelli       1. Created this package.
******************************************************************************/

  function is_in_step (p_id_riferimento in varchar2, p_nome_step in varchar2) RETURN number;
END jwf_utility_pkg;

/

GRANT EXECUTE ON JWF_UTILITY_PKG TO JWF
/
CREATE OR REPLACE package body          jwf_utility_pkg
is
   
  function is_in_step (p_id_riferimento in varchar2, p_nome_step in varchar2) RETURN number
      /******************************************************************************
       NOME:        is_in_step
       DESCRIZIONE: restituisce il valore dell'impostazione per l'ente specificato.
       PARAMETRI:   --
       RITORNA:     STRINGA VARCHAR2 CONTENENTE VERSIONE E DATA.
       NOTE:        IL SECONDO NUMERO DELLA VERSIONE CORRISPONDE ALLA REVISIONE
                    DEL PACKAGE.
      ******************************************************************************/
   is
        d_ret number(19);
        d_id_documento number(19);
   begin
     
     d_id_documento := to_number(regexp_substr(p_id_riferimento, '[0-9]+$'));
   
     select count(1)
       into d_ret
       from documenti_step s 
      where s.id_documento = d_id_documento
        and s.step_nome like '%'||p_nome_step||'%';
     
     if (d_ret > 0)
     then
        d_ret := 1;
     end if;
     
     return d_ret;
   end is_in_step;
 

end jwf_utility_pkg;

/

GRANT EXECUTE ON JWF_UTILITY_PKG TO JWF
/
