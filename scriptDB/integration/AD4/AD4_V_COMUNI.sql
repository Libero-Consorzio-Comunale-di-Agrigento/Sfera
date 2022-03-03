--liquibase formatted sql
--changeset rdestasio:install_20200221_ad4_01 runOnChange:true

CREATE OR REPLACE FORCE VIEW AD4_V_COMUNI
(
   ID,
   COMUNE,
   STATO,
   PROVINCIA,
   DENOMINAZIONE,
   CAP,
   SIGLA_CFIS,
   DATA_SOPPRESSIONE
)
AS
   SELECT TO_NUMBER (c.provincia_stato || LPAD (c.comune, 4, 0)) id,
          c.comune,
          COALESCE (s.stato_territorio, 100) stato,
          CASE WHEN provincia_stato < 200 THEN provincia_stato ELSE NULL END
             provincia,
          c.denominazione,
          c.cap,
          c.sigla_cfis,
          c.data_soppressione
     FROM AD4_COMUNI c, ad4_stati_territori s
    WHERE s.stato_territorio(+) = c.provincia_stato
/



