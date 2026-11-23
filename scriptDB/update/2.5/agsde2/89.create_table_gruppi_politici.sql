--liquibase formatted sql
--changeset mfrancesconi:2.5_20250613_89.create_table_gruppi_politici runOnChange:true stripComments:false failOnError:true

create table gruppi_politici
(
    id_gruppo_politico    number (19) not null,
    version               number (19) not null,
    titolo                varchar2 (255) not null,
    descrizione           varchar2 (4000),
    sequenza              number (10) not null,
    ente                  varchar2 (255) not null,
    data_ins              date not null,
    data_upd              date not null,
    utente_ins            varchar2 (255) not null,
    utente_upd            varchar2 (255) not null,
    valido                char (1) not null,
    valido_dal            date not null,
    valido_al             date
)
/

create unique index gruppi_politici_pk on gruppi_politici (id_gruppo_politico)
/

alter table gruppi_politici add (primary key (id_gruppo_politico) enable validate)
/

create table gruppi_politici_componenti
(
    id_gruppo_politico_componente    number (19) not null,
    version                          number (19) not null,
    id_gruppo_politico               number (19) not null,
    ni_componente                    number (19) not null,
    data_ins                         date not null,
    data_upd                         date not null,
    utente_ins                       varchar2 (255) not null,
    utente_upd                       varchar2 (255) not null,
    valido                           char (1) not null,
    valido_dal                       date not null,
    valido_al                        date
)
/

alter table gruppi_politici_componenti
    add (primary key(id_gruppo_politico_componente) using index enable validate)
/

create index grupol_grupolcom_fk on gruppi_politici_componenti (id_gruppo_politico)
/

alter table gruppi_politici_componenti
    add ( constraint grupol_grupolcom_fk foreign key (id_gruppo_politico)
            references gruppi_politici (id_gruppo_politico) enable validate)
/

create table gruppi_politici_documenti
(
    id_gruppo_politico_documento    number (19) not null,
    version                         number (19) not null,
    id_proposta_delibera            number (19),
    id_delibera                     number (19),
    id_gruppo_politico              number (19) not null,
    titolo                          varchar2 (255) not null,
    descrizione                     varchar2 (4000),
    ni_componente                   number (19) not null,
    data_ins                        date not null,
    data_upd                        date not null,
    utente_ins                      varchar2 (255) not null,
    utente_upd                      varchar2 (255) not null,
    valido                          char (1) not null,
    valido_dal                      date not null,
    valido_al                       date
)
/

alter table gruppi_politici_documenti
    add (
        primary key
            (id_gruppo_politico_documento)
            using index enable validate)
/

create index grupoldoc_prodel_fk on gruppi_politici_documenti (id_proposta_delibera)
/

create index grupoldoc_del_fk on gruppi_politici_documenti (id_delibera)
/

create index grupoldoc_grupol_fk on gruppi_politici_documenti (id_gruppo_politico)
/

alter table gruppi_politici_documenti
    add (
        constraint grupoldoc_grupol_fk foreign key (id_gruppo_politico)
            references gruppi_politici (id_gruppo_politico)
            enable validate,
        constraint grupoldoc_prodel_fk foreign key (id_proposta_delibera)
            references proposte_delibera (id_proposta_delibera)
            enable validate,
        constraint grupoldoc_del_fk foreign key (id_delibera)
            references delibere (id_delibera)
            enable validate)
/

alter table tipi_delibera add (gestione_gruppi_politici char (1 byte) default 'N' not null)
/

alter table proposte_delibera
    add (anno_protocollo_richiesta number (10),
         numero_protocollo_richiesta number (10),
         pubblica_casa_di_vetro char (1) default 'N' not null)
/