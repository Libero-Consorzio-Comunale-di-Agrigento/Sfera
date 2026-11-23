--liquibase formatted sql
--changeset mfrancesconi:2.5_20251030_92.alter_table_controllo_regolarita_doc runOnChange:true stripComments:false failOnError:false

alter table controllo_regolarita_doc modify (note varchar2 (4000 byte))
/