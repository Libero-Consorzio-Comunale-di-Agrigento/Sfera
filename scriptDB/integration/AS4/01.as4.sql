--liquibase formatted sql
--changeset rdestasio:install_20200221_as4_01 runOnChange:true

GRANT SELECT ON ${global.db.as4.username}.ANAGRAFE_SOGGETTI TO ${global.db.target.username} WITH GRANT OPTION
/
GRANT EXECUTE ON ${global.db.as4.username}.ANAGRAFE_SOGGETTI_PKG TO ${global.db.target.username}
/
GRANT EXECUTE ON ${global.db.as4.username}.ANAGRAFE_SOGGETTI_TPK TO ${global.db.target.username}
/
GRANT SELECT ON ${global.db.as4.username}.TIPI_SOGGETTO TO ${global.db.target.username} WITH GRANT OPTION
/