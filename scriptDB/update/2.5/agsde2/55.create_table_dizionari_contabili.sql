--liquibase formatted sql
--changeset mfrancesconi:2.5_20240115_55.create_table_dizionari_contabili runOnChange:true stripComments:false failOnError:false

begin
    execute immediate 'create table dizionari_contabili ( tipo         varchar2(255)       not null
        , codice       varchar2(255)       not null
        , descrizione  varchar2(4000)
        , valido       char(1)             not null
        , sequenza     number(19)
        , ente         varchar2(255)       not null
        , data_ins     date                not null
        , utente_ins   varchar2(255 byte)  not null
        , data_upd     date                not null
        , utente_upd   varchar2(255 byte)  not null)';
    exception when others then null;
end;
/

begin
    execute immediate 'create unique index dizionari_contabili_pk on dizionari_contabili (tipo, codice, ente)';
    exception when others then null;
end;
/

begin
    execute immediate 'alter table dizionari_contabili add ( primary key (tipo, codice, ente) using index dizionari_contabili_pk enable validate)';
    exception when others then null;
end;
/

declare
   contatore      number;
   anno           number;
begin
   begin
      for contatore in 1 .. 17
      loop
         anno := 2023 + contatore;

         insert into dizionari_contabili (tipo,
                                          codice,
                                          descrizione,
                                          valido,
                                          sequenza,
                                          ente,
                                          data_ins,
                                          utente_ins,
                                          data_upd,
                                          utente_upd)
              values ('ANNO',
                      anno,
                      null,
                      'Y',
                      contatore,
                      impostazioni_pkg.get_impostazione ('ENTI_SO4', '*'),
                      sysdate,
                      'AGSDE2',
                      sysdate,
                      'AGSDE2');
      end loop;
   exception
      when others then
         null;
   end;
end;
/