--liquibase formatted sql
--changeset mfrancesconi:2.5_20231201_52.alter_table_odg_commissioni runOnChange:true stripComments:false failOnError:false

alter table odg_commissioni add ( gettone_presenza_giornaliera char(1) default 'Y' not null, singolo_presidente  char(1) default 'Y' not null)
/
