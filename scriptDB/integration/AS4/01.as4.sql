--liquibase formatted sql
--changeset rdestasio:install_20200221_as4_01 runOnChange:true

GRANT SELECT ON ANAGRAFE_SOGGETTI TO ${global.db.target.username} WITH GRANT OPTION
/
GRANT EXECUTE ON ANAGRAFE_SOGGETTI_PKG TO ${global.db.target.username}
/
GRANT EXECUTE ON ANAGRAFE_SOGGETTI_TPK TO ${global.db.target.username}
/
GRANT SELECT ON TIPI_SOGGETTO TO ${global.db.target.username} WITH GRANT OPTION
/
GRANT SELECT ON ANAGRAFICI TO ${global.db.target.username} with grant option
/
GRANT SELECT ON RECAPITI TO ${global.db.target.username} with grant option
/
GRANT SELECT ON TIPI_RECAPITO TO ${global.db.target.username} with grant option
/
GRANT SELECT ON CONTATTI TO ${global.db.target.username} with grant option
/
GRANT SELECT ON TIPI_CONTATTO TO ${global.db.target.username} with grant option
/