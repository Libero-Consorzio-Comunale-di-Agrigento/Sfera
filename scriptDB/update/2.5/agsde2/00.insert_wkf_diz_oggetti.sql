--liquibase formatted sql
--changeset mfrancesconi:2.5_20220307_00.insert_wkf_diz_oggetti runOnChange:true

insert into wkf_diz_tipi_oggetto (codice
                                 , descrizione
                                 , iterabile
                                 , nome
                                 , oggetti_figli
                                 , valido)
select 'ALLEGATO'
     , 'Documento di tipo Allegato'
     , 'N'
     , 'Allegato'
     , null
     , 'Y'
from dual
where not exists
    (select 1
     from wkf_diz_tipi_oggetto
     where codice = 'ALLEGATO')
/

insert into wkf_diz_tipi_oggetto (codice
                                 , descrizione
                                 , iterabile
                                 , nome
                                 , oggetti_figli
                                 , valido)
select 'DETERMINA'
     , 'Documento iterabile di tipo Determina'
     , 'Y'
     , 'Determina'
     , 'ALLEGATO#VISTO#CERTIFICATO'
     , 'Y'
from dual
where not exists
    (select 1
     from wkf_diz_tipi_oggetto
     where codice = 'DETERMINA')
/

insert into wkf_diz_tipi_oggetto (codice
                                 , descrizione
                                 , iterabile
                                 , nome
                                 , oggetti_figli
                                 , valido)
select 'PROPOSTA_DELIBERA'
     , 'Documento iterabile di tipo Proposta di delibera'
     , 'Y'
     , 'Proposta di delibera'
     , 'ALLEGATO#VISTO'
     , 'Y'
from dual
where not exists
    (select 1
     from wkf_diz_tipi_oggetto
     where codice = 'PROPOSTA_DELIBERA')
/

insert into wkf_diz_tipi_oggetto (codice
                                 , descrizione
                                 , iterabile
                                 , nome
                                 , oggetti_figli
                                 , valido)
select 'DELIBERA'
     , 'Documento iterabile di tipo Delibera'
     , 'Y'
     , 'Delibera'
     , 'ALLEGATO#CERTIFICATO'
     , 'Y'
from dual
where not exists
    (select 1
     from wkf_diz_tipi_oggetto
     where codice = 'DELIBERA')
/

insert into wkf_diz_tipi_oggetto (codice
                                 , descrizione
                                 , iterabile
                                 , nome
                                 , oggetti_figli
                                 , valido)
select 'CERTIFICATO'
     , 'Documento iterabile di tipo Certificato'
     , 'Y'
     , 'Certificato'
     , null
     , 'Y'
from dual
where not exists
    (select 1
     from wkf_diz_tipi_oggetto
     where codice = 'CERTIFICATO')
/

insert into wkf_diz_tipi_oggetto (codice
                                 , descrizione
                                 , iterabile
                                 , nome
                                 , oggetti_figli
                                 , valido)
select 'VISTO'
     , 'Documento iterabile di tipo Visto o Parere'
     , 'Y'
     , 'Visto/Parere'
     , null
     , 'Y'
from dual
where not exists
    (select 1
     from wkf_diz_tipi_oggetto
     where codice = 'VISTO')
/

insert into wkf_diz_tipi_oggetto (codice
                                 , descrizione
                                 , iterabile
                                 , nome
                                 , oggetti_figli
                                 , valido)
select 'SEDUTA_STAMPA'
     , 'Documento di seduta (Verbale o Convocazione)'
     , 'Y'
     , 'Stampa di Seduta'
     , null
     , 'Y'
from dual
where not exists
    (select 1
     from wkf_diz_tipi_oggetto
     where codice = 'SEDUTA_STAMPA')
/

COMMIT
/