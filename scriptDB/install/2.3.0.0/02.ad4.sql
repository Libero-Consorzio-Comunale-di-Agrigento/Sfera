--liquibase formatted sql
--changeset rdestasio:2.3.0.0_20200221_02

-- aggiorno le versioni delle istanze di agiter e agsde2.vis se hanno numeri sballati.
update istanze set versione = 'V1.0.0.0' where istanza = 'AGSDE2.VIS'
/

update istanze set versione = 'V'||versione where istanza = 'AGSDE2ITER' and versione not like 'V%'
/
