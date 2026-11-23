--liquibase formatted sql
--changeset mfrancesconi:2.5_20240304_60.update_firma_digitale_file runOnChange:true

update firma_digitale_file set posizione_firma = 0 where posizione_firma is null
/