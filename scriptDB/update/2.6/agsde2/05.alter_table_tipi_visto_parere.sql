--liquibase formatted sql
--changeset mferrara:2.6_20260126_05.alter_table_tipi_visto_parere runOnChange:true stripComments:false failOnError:false


ALTER TABLE TIPI_VISTO_PARERE ADD (
    ID_TIPO_ALLEGATO NUMBER(19)
)
/