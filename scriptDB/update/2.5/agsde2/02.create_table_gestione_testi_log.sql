--liquibase formatted sql
--changeset rdestasio:2.5.6.3_20210804_01.create_table_gestione_testi_log.sql runOnChange:true failOnError:false

DROP TABLE GTE_LOG
/
commit
/
