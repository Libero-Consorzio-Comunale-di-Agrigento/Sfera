--liquibase formatted sql
--changeset mfrancesconi:2.5_20240528_66.alter_table_odg_commissioni runOnChange:true stripComments:false failOnError:false

alter table odg_commissioni add ( visibile_solo_convocati char(1) default 'N')
/

update odg_commissioni
set visibile_solo_convocati = 'N'
/