--liquibase formatted sql
--changeset rdestasio:2.0.1.0_20200221_05

-- Questo va qui perché non è detto che esista il pkg
-- update della data di esecutività delle delibere:
update delibere d 
   set d.data_esecutivita = trunc(d.data_pubblicazione) + IMPOSTAZIONI_PKG.GET_IMPOSTAZIONE('PUBBLICAZIONE_GIORNI_ESECUTIVITA', d.ente)
 where d.data_esecutivita is null
/
