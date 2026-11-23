--liquibase formatted sql
--changeset mfrancesconi:2.5_20220919_16.create_table_odg_commissioni_collegate runOnChange:true

CREATE TABLE odg_commissioni_collegate
(
    id_commissione_collegata   number (19) not null,
    version                    number (19) not null,
    id_commissione             number (19) not null,
    id_collegata               number (19) not null,
    data_ins                   date not null,
    data_upd                   date not null,
    utente_ins                 varchar2 (255 byte) not null,
    utente_upd                 varchar2 (255 byte) not null,
    valido                     char (1 byte) not null,
    valido_al                  date,
    valido_dal                 date not null
)
/

create unique index ODG_COMMISSIONI_COLLEGATE_PK
    on ODG_COMMISSIONI_COLLEGATE (ID_COMMISSIONE_COLLEGATA)
/

alter table ODG_COMMISSIONI_COLLEGATE add (
  primary key
  (ID_COMMISSIONE_COLLEGATA)
  using index ODG_COMMISSIONI_COLLEGATE_PK
  enable validate)
/
create index ODGCOMCOL_ODGCOM_FK
    on odg_commissioni_collegate (ID_COMMISSIONE)
/

alter table odg_commissioni_collegate add (
  constraint ODGCOMCOL_ODGCOM_FK
  foreign key (ID_COMMISSIONE)
  references ODG_COMMISSIONI (ID_COMMISSIONE)
  enable validate)
/

create index ODGCOMCOLCOL_ODGCOM_FK
    on odg_commissioni_collegate (ID_COLLEGATA)
/

alter table odg_commissioni_collegate add (
  constraint ODGCOMCOLCOL_ODGCOM_FK
  foreign key (ID_COLLEGATA)
  references ODG_COMMISSIONI (ID_COMMISSIONE)
  enable validate)
/
