--liquibase formatted sql
--changeset rdestasio:install_20200221_01 runOnChange:true

GRANT SELECT ON AMMINISTRAZIONI                 TO ${global.db.target.username}
/

GRANT SELECT ON AOO                             TO ${global.db.target.username}
/

GRANT SELECT ON OTTICHE                         TO ${global.db.target.username}
/

GRANT SELECT ON RUOLI_COMPONENTE                TO ${global.db.target.username}
/

GRANT SELECT ON SUDDIVISIONI_STRUTTURA          TO ${global.db.target.username}
/

GRANT SELECT ON VISTA_ATCO_GRAILS               TO ${global.db.target.username}
/

GRANT SELECT ON VISTA_ATCO_GRAILS_PUBB          TO ${global.db.target.username}
/

GRANT SELECT ON VISTA_COMP_GRAILS               TO ${global.db.target.username}
/

GRANT SELECT ON VISTA_COMP_GRAILS_PUBB          TO ${global.db.target.username}
/

GRANT SELECT ON VISTA_PUBB_RUCO                 TO ${global.db.target.username}
/

GRANT SELECT ON VISTA_UNITA_ORGANIZZATIVE       TO ${global.db.target.username}
/

GRANT SELECT ON VISTA_UNITA_ORGANIZZATIVE_PUBB  TO ${global.db.target.username}
/

GRANT EXECUTE ON SO4_UTIL  					    TO ${global.db.target.username} WITH GRANT OPTION
/

grant select on indirizzi_telematici TO ${global.db.target.username}
/

grant select on aoo_view TO ${global.db.target.username}
/