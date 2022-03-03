--liquibase formatted sql
--changeset rdestasio:2.0.1.0_20200221_01.so4.sql runOnChange:true

GRANT EXECUTE ON ${global.db.so4.username}.SO4_UTIL TO ${global.db.target.username}
/
