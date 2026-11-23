--liquibase formatted sql
--changeset mfrancesconi:2.5_20240119_56.alter_table_dete_prop_ats_pavia.sql runOnChange:true stripComments:false failOnError:false

alter table determine add ( IN_EVIDENZA char(1) default 'N',
                            DATA_IN_EVIDENZA_DA date,
                            DATA_IN_EVIDENZA_A date,
                            IMPORTO NUMBER(19,2))
/

alter table determine_storico add ( IN_EVIDENZA char(1) default 'N',
                                    DATA_IN_EVIDENZA_DA date,
                                    DATA_IN_EVIDENZA_A date,
                                    IMPORTO NUMBER(19,2))
/

alter table proposte_delibera add ( IN_EVIDENZA char(1) default 'N',
                                    DATA_IN_EVIDENZA_DA date,
                                    DATA_IN_EVIDENZA_A date,
                                    IMPORTO NUMBER(19,2))
/

alter table proposte_delibera_storico add ( IN_EVIDENZA char(1) default 'N',
                                            DATA_IN_EVIDENZA_DA date,
                                            DATA_IN_EVIDENZA_A date,
                                            IMPORTO NUMBER(19,2))
/