--liquibase formatted sql
--changeset mfrancesconi:2.6_20260128_01.06.alter_table_add_data_gestione runOnChange:true stripComments:false failOnError:false


ALTER TABLE TIPI_DETERMINA ADD (
    VIS_DATA_CONTROLLO_GESTIONE CHAR(1) DEFAULT 'N'
)
/

ALTER TABLE DETERMINE ADD (
    DATA_CONTROLLO_GESTIONE DATE
)
/