--liquibase formatted sql
--changeset rdestasio:install_20200221_defaults_04

-- Senza integrazione con JCONS:
CREATE OR REPLACE PACKAGE        JCONS_PKG AS
/******************************************************************************
   NAME:       JCONS_PKG
   PURPOSE:

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        21/04/2015      esasdelli       1. Created this package.
   2.0        26/09/2018      czappavigna     1. Add get_data_conservazinoe
******************************************************************************/

  function get_log_conservazione  (p_id_documento in number) return varchar2;
  function get_data_conservazione (p_id_documento in number) return date;

END JCONS_PKG;
/

CREATE OR REPLACE PACKAGE BODY        JCONS_PKG
AS
   /******************************************************************************
      NAME:       JCONS_PKG
      PURPOSE:

      REVISIONS:
      Ver        Date        Author           Description
      ---------  ----------  ---------------  ------------------------------------
      1.0        21/04/2015      esasdelli       1. Created this package.
      2.0        26/09/2018      czappavigna     1. Add get_data_conservazinoe
      ******************************************************************************/

   function get_log_conservazione (p_id_documento in number)
      return varchar2
   is
      d_log   varchar2 (32767);
   begin
		d_log := '';
		return d_log;
   end;

   function get_data_conservazione (p_id_documento in number)
      return date
   is
      d_data   date;
   begin
      d_data := null;
      return d_data;
   end;

end jcons_pkg;
/