--liquibase formatted sql
--changeset mfrancesconi:2.6_20260518_19.create_table_NUMERAZIONI_COMMITTATE runOnChange:true stripComments:false failOnError:false

-- Tabella di supporto per il commit del numero determina in TX indipendente.
-- Viene scritta dalla inner TX (REQUIRES_NEW) e sopravvive al rollback della outer TX.
-- Garantisce che il numero assegnato venga preservato e riutilizzato al ciclo successivo
-- senza generare gap aggiuntivi nel registro.
-- Vedi: DeterminaService.assegnaNumeroDeterminaECommit / DeterminaAction.numeraDeterminaECommit
CREATE TABLE determine_numeri (
    id_determina_numero     number(19)      not null,
    id_determina            number(19)      not null,
    numero_determina        number(19)      not null,
    anno_determina          number(19)      not null,
    data_numero_determina   date            not null,
    registro_determina      varchar2(255)   not null,
    data_operazione         date            not null,
    ente                    varchar2 (255)  not null
)
/

create unique index determine_numeri_PK on determine_numeri (id_determina_numero)
/

alter table determine_numeri add (
  primary key (id_determina_numero)
  using index determine_numeri_PK enable validate)
/

create unique index detnum_dete_fk on determine_numeri (id_determina)
/

alter table determine_numeri add (
  constraint detnum_dete_fk foreign key (id_determina)
  references determine (id_determina) enable validate)
/

create index detnum_tipreg_fk on determine_numeri (registro_determina)
/

alter table determine_numeri add (
  constraint detnum_tipreg_fk foreign key (registro_determina)
  references tipi_registro (tipo_registro) enable validate)
/

create unique index detnum_numero_dete_uk on determine_numeri (anno_determina, nvl2(numero_determina,ente,null), numero_determina, registro_determina)
/