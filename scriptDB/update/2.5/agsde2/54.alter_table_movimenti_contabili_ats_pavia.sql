--liquibase formatted sql
--changeset mfrancesconi:2.5_20240111_54.alter_table_movimenti_contabili_ats_pavia runOnChange:true stripComments:false failOnError:false

alter table movimenti_contabili add ( conto varchar2(255)
                                    , conto_descrizione varchar2(4000)
                                    , bilancio varchar2(255)
                                    , bilancio_descrizione varchar2(4000)
                                    , sotto_budget varchar2(255)
                                    , sotto_budget_descrizione varchar2(4000)
                                    , note varchar2(4000)
                                    , importo_iniziale number(19,2)
                                    , importo_variazione number(19,2)
                                    , importo_residuo number(19,2)
                                    , importo_provvedimento number(19,2))
/