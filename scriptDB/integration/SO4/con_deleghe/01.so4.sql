--liquibase formatted sql
--changeset rdestasio:install_20200221_deleghe_01

-- questi potrebbero non esserci se so4 non è aggiornato
grant select on deleghe to ${global.db.target.username}
/

grant select on applicativi to ${global.db.target.username}
/

grant select on COMPETENZE_DELEGA to ${global.db.target.username}
/

grant execute on competenze_delega_tpk to ${global.db.target.username}
/