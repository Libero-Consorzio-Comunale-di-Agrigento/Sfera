--liquibase formatted sql
--changeset mfrancesconi:2.5_20220922_17.create_table_odg_oggetti_seduta_collegati runOnChange:true

create table ODG_OGGETTI_SEDUTA_COLLEGATI
(
    ID_OGGETTO_SEDUTA_COLL   number (19) not null,
    VERSION                  number (19) not null,
    ID_SEDUTA                number (19) not null,
    ID_OGGETTO_SEDUTA        number (19) not null,
    SEQUENZA_CONVOCAZIONE    number (10) not null,
    DATA_INS                 date not null,
    DATA_UPD                 date not null,
    UTENTE_INS               varchar2 (255 byte) not null,
    UTENTE_UPD               varchar2 (255 byte) not null
)
/

create index ODGOGGSEDCOL_ODGOGGSED_FK
    on ODG_OGGETTI_SEDUTA_COLLEGATI (ID_OGGETTO_SEDUTA)
/

create index ODGOGGSEDCOL_ODGSED_FK
    on ODG_OGGETTI_SEDUTA_COLLEGATI (ID_SEDUTA)
/

alter table ODG_OGGETTI_SEDUTA_COLLEGATI add (
  primary key
  (ID_OGGETTO_SEDUTA_COLL))
/

alter table ODG_OGGETTI_SEDUTA_COLLEGATI add (
  constraint ODGOGGSEDCOL_ODGOGGSED_FK
  foreign key (ID_OGGETTO_SEDUTA)
  references ODG_OGGETTI_SEDUTA (ID_OGGETTO_SEDUTA)
  enable validate,
  constraint ODGOGGSEDCOL_ODGSED_FK
  foreign key (ID_SEDUTA)
  references ODG_SEDUTE (ID_SEDUTA)
  enable validate)
/