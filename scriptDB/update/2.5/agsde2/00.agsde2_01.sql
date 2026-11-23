--liquibase formatted sql
--changeset rdestasio:2.5.4.0_20200221_01

insert into TIPI_ALLEGATO (
   CODICE, DATA_INS, DATA_UPD,
   DESCRIZIONE, ENTE,
   ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
   MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
   STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
   TITOLO, UTENTE_INS, UTENTE_UPD,
   VALIDO, VALIDO_AL, VALIDO_DAL,
   VERSION)
select 'ALLEGATO_COPIA_TESTO',
 sysdate,
 sysdate,
 'Allegato Copia Testo',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 'Y',
 'Y',
 'Y',
 'DA_FIRMARE',
 'DETERMINA',
 'Allegato Copia Testo',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                 from TIPI_ALLEGATO
                 where (codice = 'ALLEGATO_COPIA_TESTO' and tipologia = 'DETERMINA')) and exists (select 1 from tipi_allegato)
/

insert into TIPI_ALLEGATO (
   CODICE, DATA_INS, DATA_UPD,
   DESCRIZIONE, ENTE,
   ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE,
   MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO,
   STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA,
   TITOLO, UTENTE_INS, UTENTE_UPD,
   VALIDO, VALIDO_AL, VALIDO_DAL,
   VERSION)
select 'ALLEGATO_COPIA_TESTO',
 sysdate,
 sysdate,
 'Allegato Copia Testo',
 (select valore from impostazioni where codice = 'ENTI_SO4' and ente = '*'),
 null,
 HIBERNATE_SEQUENCE.nextval,
 'N',
 'Y',
 'Y',
 'Y',
 'Y',
 'DA_FIRMARE',
 'PROPOSTA_DELIBERA',
 'Allegato Copia Testo',
 (select utente_ins from tipi_determina where rownum = 1),
 (select utente_ins from tipi_determina where rownum = 1),
 'Y',
 null,
 trunc(sysdate),
 0
from dual
where not exists(select *
                 from TIPI_ALLEGATO
                 where (codice = 'ALLEGATO_COPIA_TESTO' and tipologia = 'PROPOSTA_DELIBERA')) and exists (select 1 from tipi_allegato)
/

