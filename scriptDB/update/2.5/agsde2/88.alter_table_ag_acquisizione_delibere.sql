--liquibase formatted sql
--changeset mfrancesconi:2.5_20250530_88.alter_table_ag_acquisizione_delibere runOnChange:true stripComments:false failOnError:false

alter table ag_acquisizione_delibere modify (   tipo_registro           null,
                                                movimento               null,
                                                codice_modello          null,
                                                unita_protocollante     null,
                                                utente_protocollante    null)
/

drop index ix_acq_deli
/
