--liquibase formatted sql
--changeset mferrara:2.6_20260119_04.insert_tipo_soggetto_servizio runOnChange:true failOnError:false

insert into tipi_soggetti (codice,
                           categoria,
                           descrizione,
                           titolo)
select 'UO_SERVIZIO',
       'UNITA',
       null,
       'Uo Servizio'
from dual
where not exists
          (select 1
           from tipi_soggetti
           where codice = 'UO_SERVIZIO')
/

