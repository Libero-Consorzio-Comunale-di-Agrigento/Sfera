--liquibase formatted sql
--changeset amarinelli:2.5_20240912_76.alter_table_impatto_generazionale runOnChange:true stripComments:false failOnError:true

alter table tipi_delibera
    add (impatto_generazionale char (1 byte) default 'N' not null)
/

alter table tipi_determina
    add (impatto_generazionale char (1 byte) default 'N' not null)
/

alter table determine
    add (id_impatto_generazionale number(19))
/

create index det_impgen_fk
    on determine (id_impatto_generazionale)
/

alter table determine
    add (
        constraint det_impgen_fk foreign key (id_impatto_generazionale)
            references impatti_generazionali (id_impatto_generazionale)
            enable validate)
/

alter table delibere
    add (id_impatto_generazionale number (19))
/

create index del_impgen_fk
    on delibere (id_impatto_generazionale)
/

alter table delibere
    add (
        constraint del_impgen_fk foreign key (id_impatto_generazionale)
            references impatti_generazionali (id_impatto_generazionale)
            enable validate)
/

alter table proposte_delibera
    add (id_impatto_generazionale number (19))
/

create index prodel_impgen_fk
    on proposte_delibera (id_impatto_generazionale)
/

alter table proposte_delibera
    add (
        constraint prodel_impgen_fk foreign key (id_impatto_generazionale)
            references impatti_generazionali (id_impatto_generazionale)
            enable validate)
/