--liquibase formatted sql
--changeset mfrancesconi:2.5_20231218_01.cfa.grant runOnChange:true failOnError:false

grant execute on integrazione_cfa_pkg to ${global.db.cfa.username}
/