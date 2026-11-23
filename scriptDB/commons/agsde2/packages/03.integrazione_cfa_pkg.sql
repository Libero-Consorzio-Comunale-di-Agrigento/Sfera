--liquibase formatted sql
--changeset mfrancesconi:2.5_20231218_03.integrazione_cfa_pkg runOnChange:true

create or replace package integrazione_cfa_pkg
as
   /******************************************************************************
      NAME:       integrazione_cfa_pkg
      PURPOSE:

      REVISIONS:
      Ver        Date        Author           Description
      ---------  ----------  ---------------  ------------------------------------
      1.0        18/12/2023   mfrancesconi       1. Created this package.
   ******************************************************************************/
   function get_registro_proposte (p_ente varchar2)
      return varchar2;
end integrazione_cfa_pkg;
/

create or replace package body integrazione_cfa_pkg
as
   /******************************************************************************
      NAME:       integrazione_cfa_pkg
      PURPOSE:

      REVISIONS:
      Ver        Date        Author           Description
      ---------  ----------  ---------------  ------------------------------------
      1.0        18/12/2023   mfrancesconi       1. Created this package.
   ******************************************************************************/
   function get_registro_proposte (p_ente varchar2)
      return varchar2
   is
      d_registro_proposte varchar2 (255);
   begin
      begin
         select nvl (registro_esterno, tipo_registro) codice
           into d_registro_proposte
           from tipi_registro
          where tipo_registro in (select valore
                                    from impostazioni
                                   where codice = 'REGISTRO_PROPOSTE' and ente = p_ente);
      exception
         when no_data_found then
            d_registro_proposte := '';
      end;

      return d_registro_proposte;
   end;
end integrazione_cfa_pkg;
/