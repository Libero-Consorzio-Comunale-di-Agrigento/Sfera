--liquibase formatted sql
--changeset rdestasio:GDM_GTE_LOG runOnChange:true stripComments:false

CREATE OR REPLACE VIEW gte_log_view
AS
SELECT * FROM ${global.db.gdm.username}.gte_log
/

COMMIT
/