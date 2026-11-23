--liquibase formatted sql
--changeset mfrancesconi:2.5_20241212_02.grant_ad4 runOnChange:true

GRANT EXECUTE ON KEY_ERROR_LOG_PKG TO ${global.db.target.username}
/
GRANT SELECT, INSERT, UPDATE, DELETE ON REGISTRO TO ${global.db.target.username} WITH GRANT OPTION
/
GRANT EXECUTE ON REGISTRO_UTILITY TO ${global.db.target.username} WITH GRANT OPTION
/