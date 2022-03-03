--liquibase formatted sql
--changeset rdestasio:2.0.2.0_20200221_08

-- in questo file ci vanno tutti quei package e viste che servono "vuoti" o "finti" in caso non ci sia la relativa integrazione.


-- Senza integrazione con JCONS:
CREATE OR REPLACE PACKAGE        JCONS_PKG AS
/******************************************************************************
   NAME:       JCONS_PKG
   PURPOSE:

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        21/04/2015      esasdelli       1. Created this package.
******************************************************************************/

  function get_log_conservazione (p_id_documento in number) return varchar2;

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
   ******************************************************************************/

   function get_log_conservazione (p_id_documento in number)
      return varchar2
   is
      d_log   varchar2 (1000);
   begin
		d_log := '';
		return d_log;
   end;
   
end jcons_pkg;
/
