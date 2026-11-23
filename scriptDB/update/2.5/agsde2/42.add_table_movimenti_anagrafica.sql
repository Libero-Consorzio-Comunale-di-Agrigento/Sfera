--liquibase formatted sql
--changeset mfrancesconi:2.5_20230518_42.add_table_movimenti_anagrafica runOnChange:true stripComments:false failOnError:false

create table movimenti_anagrafica
(
    id_movimento_anagrafica     number (19) not null,
    version                     number (19) not null,
    id_determina                number (19),
    id_proposta_delibera        number (19),
    id_esterno                  number (19),
    ragione_sociale             varchar2 (255),
    cognome                     varchar2 (255),
    nome                        varchar2 (255),
    tipo                        varchar2 (255),
    codice_fiscale              varchar2 (255),
    partita_iva                 varchar2 (255),
    data_nascita                date,
    comune_nascita              varchar2 (255),
    sigla_provincia_nascita     varchar2 (255),
    comune                      varchar2 (255),
    cap                         varchar2 (255),
    sigla_provincia             varchar2 (255),
    email                       varchar2 (255),
    telefono                    varchar2 (255),
    cellulare                   varchar2 (255),
    indirizzo                   varchar2 (255),
    tipo_indirizzo              varchar2 (255),
    id_applicativo_esterno      varchar2 (255),
    codice_applicativo_esterno  varchar2 (255),
    ente                        varchar2 (255 byte) not null,
    data_ins                    date not null,
    utente_ins                  varchar2 (255 byte) not null,
    data_upd                    date not null,
    utente_upd                  varchar2 (255 byte) not null
)
/

create unique index movimenti_anagrafica_PK
    on movimenti_anagrafica (id_movimento_anagrafica)
/

create index movana_det_FK
    on movimenti_anagrafica (id_determina)
/

create index movana_prodel_FK
    on movimenti_anagrafica (id_proposta_delibera)
/

alter table movimenti_anagrafica add (
  primary key
  (id_movimento_anagrafica)
  using index movimenti_anagrafica_PK
  enable validate)
/

alter table movimenti_anagrafica add (
  constraint movana_det_FK
  foreign key (id_determina)
  references determine (id_determina)
  enable validate,
  constraint movana_prodel_FK
  foreign key (id_proposta_delibera)
  references proposte_delibera (id_proposta_delibera)
  enable validate)
/

alter table movimenti_contabili add (id_movimento_anagrafica number(19),
                                     id_esterno varchar2(255),
                                     id_capitolo varchar2(255),
                                     descrizione_pdc_liv_5 varchar2(4000),
                                     id_progetto varchar2(255),
                                     codice_progetto varchar2(255),
                                     cop_fpv varchar2(255),
                                     sig_cpt varchar2(255),
                                     des_cpt varchar2(255),
                                     cod_centro_costo varchar2(255),
                                     des_centro_costo varchar2(255))
/

create index movcon_movana_FK
    on movimenti_contabili (id_movimento_anagrafica)
/

alter table movimenti_contabili add (
  constraint movcon_movana_FK
  foreign key (id_movimento_anagrafica)
  references movimenti_anagrafica (id_movimento_anagrafica)
  enable validate)
/