--liquibase formatted sql
--changeset mfrancesconi:2.5_20221026_23.alter_table_odg_sedute_partecipanti_delega runOnChange:true

begin
execute immediate 'ALTER TABLE ODG_SEDUTE_PARTECIPANTI ADD
    (DELEGA CHAR(1) DEFAULT ''N'' NOT NULL
     , ID_PARTECIPANTE_DELEGATO  NUMBER(19)
     , DATA_NUMERO_PROTOCOLLO_DELEGA DATE
     , NUMERO_PROTOCOLLO_DELEGA NUMBER(19)
     , ANNO_PROTOCOLLO_DELEGA NUMBER(4)
     , REGISTRO_PROTOCOLLO_DELEGA VARCHAR2(255))';
exception
   when others then
      null;
end;
/

begin
execute immediate 'create index ODGSEDPAR_ODGSEDPAR_FK on ODG_SEDUTE_PARTECIPANTI (ID_PARTECIPANTE_DELEGATO)';
exception
   when others then
      null;
end;
/

begin
execute immediate 'alter table ODG_SEDUTE_PARTECIPANTI add (constraint ODGSEDPAR_ODGSEDPAR_FK
            foreign key (ID_PARTECIPANTE_DELEGATO)
            references  ODG_SEDUTE_PARTECIPANTI (ID_SEDUTA_PARTECIPANTE)
            enable validate)';
exception
   when others then
      null;
end;
/