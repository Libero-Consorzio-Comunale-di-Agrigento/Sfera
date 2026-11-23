--liquibase formatted sql
--changeset mfrancesconi:2.5_20231121_50.alter_table_firma_digitale_file runOnChange:true stripComments:false failOnError:false

alter table firma_digitale_file add ( posizione_firma number(19))
/