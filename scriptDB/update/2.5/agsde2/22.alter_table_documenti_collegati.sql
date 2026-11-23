--liquibase formatted sql
--changeset mfrancesconi:2.5_20221021_22.alter_table_documenti_collegati runOnChange:true

begin
    execute immediate 'alter table DOCUMENTI_COLLEGATI modify (OPERAZIONE varchar2 (255 byte))';
exception when others then null;
end;
/

begin
    execute immediate 'alter table DOCUMENTI_COLLEGATI add (ID_PROPOSTA_DELIBERA_COLL number(19))';
exception when others then null;
end;
/

begin
    execute immediate 'create index PRDELCOL_PRODEL_FK on DOCUMENTI_COLLEGATI (ID_PROPOSTA_DELIBERA_COLL)';
exception when others then null;
end;
/

begin
    execute immediate 'alter table DOCUMENTI_COLLEGATI add (constraint PRDELCOL_PRODEL_FK
            foreign key (ID_PROPOSTA_DELIBERA_COLL)
            references  PROPOSTE_DELIBERA (ID_PROPOSTA_DELIBERA)
            enable validate)';
exception when others then null;
end;
/