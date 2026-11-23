--liquibase formatted sql
--changeset mfrancesconi:2.5_20230322_29.create_table_stati_iter runOnChange:true stripComments:false failOnError:false

create table STATI_ITER
(
    TIPO_DOCUMENTO varchar2 (255 byte) not null,
    STATO          varchar2 (255 byte) not null,
    ENTE           varchar2 (255 byte) not null
)
/
create unique index STATI_ITER_PK on STATI_ITER (TIPO_DOCUMENTO, STATO, ENTE)
/

alter table STATI_ITER add (primary key (TIPO_DOCUMENTO, STATO, ENTE) using index STATI_ITER_PK enable validate)
/