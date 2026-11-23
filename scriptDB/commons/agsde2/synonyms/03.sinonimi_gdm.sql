--liquibase formatted sql
--changeset mfrancesconi:20240412_03.synonyms_gdm runOnChange:true stripComments:false

begin
    execute immediate 'CREATE OR REPLACE SYNONYM gdm_documenti FOR ${global.db.gdm.username}.documenti';
    exception when others then null;
end;
/
