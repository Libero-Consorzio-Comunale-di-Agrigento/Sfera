--liquibase formatted sql
--changeset rdestasio:install_20200221_commons_02 runAlways:true failOnError:false

update impostazioni set valore = 'DA_AGGIORNARE' where codice = 'AGGIORNAMENTO_IN_CORSO'
/