--liquibase formatted sql
--changeset rdestasio:01.gte_log runOnChange:true stripComments:false failOnError:false

GRANT ALL ON gte_log TO ${global.db.target.username} WITH GRANT OPTION
/
