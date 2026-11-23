--liquibase formatted sql
--changeset mfrancesconi:2.5_20240522_64.alter_table_odg_commissioni_stampe runOnChange:true stripComments:false failOnError:false

alter table odg_commissioni_stampe add ( pubblicazione char(1) default 'N', giorni_pubblicazione number(10))
/

update odg_commissioni_stampe
set pubblicazione = 'Y'
  , giorni_pubblicazione = 15
/