--liquibase formatted sql
--changeset amarinelli:2.5_20241029_78.alter_table_soggetto_macrocategoria runOnChange:true stripComments:false failOnError:true

alter table DETERMINE add (ATTO_SOGGETTO_CONTROLLO  CHAR(1) default 'N' not null)
/

alter table DETERMINE add (MACROCATEGORIA  VARCHAR2(4000))
/
