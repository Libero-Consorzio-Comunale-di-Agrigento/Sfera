--liquibase formatted sql
--changeset mfrancesconi:04.documenti_grant runOnChange:true stripComments:false failOnError:false

GRANT SELECT ON documenti TO ${global.db.target.username} WITH GRANT OPTION
/
