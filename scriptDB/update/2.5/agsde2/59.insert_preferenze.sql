--liquibase formatted sql
--changeset mfrancesconi:2.5_20240304_59.insert_preferenze runOnChange:true failOnError:false

insert into PREFERENZE (ID,
                        CODICE,
                        ETICHETTA,
                        DESCRIZIONE,
                        NOME_METODO,
                        ENTE,
                        VALORE_DEFAULT)
select hibernate_sequence.nextval,
       'UNITA_DEFAULT',
       'Unità preferita',
       'Unità scelta dall''utente come preferita',
       'it.finmatica.atti.integrazioni.lookup.LookupUfficioUtente',
       impostazioni_pkg.get_impostazione ('ENTI_SO4', '*'),
       ''
from dual
where not exists
              (select 1
               from PREFERENZE
               where codice = 'UNITA_DEFAULT' and ente = impostazioni_pkg.get_impostazione ('ENTI_SO4', '*'))
/