--liquibase formatted sql
--changeset mfrancesconi:2.5_20220307_01.grant runOnChange:true failOnError:true

--assegno le grant allo user AD4
grant select on impostazioni to ${global.db.ad4.username}
/

--assegno le grant allo user GDM
grant select on impostazioni to ${global.db.gdm.username}
/