--liquibase formatted sql
--changeset mfrancesconi:2.5_20230620_44.alter_table_movimenti_contabili runOnChange:true stripComments:false failOnError:false

alter table movimenti_contabili add ( id_programmazione varchar2(255)
                                    , id_impegno varchar2(255)
                                    , cod_tipo_finanz varchar2(255)
                                    , des_tipo_finanz  varchar2(255)
                                    , cod_lavoro  varchar2(255)
                                    , des_lavoro varchar2(255)
                                    , cod_tipo_spesa varchar2(255)
                                    , des_tipo_spesa varchar2(255))
/

alter table movimenti_anagrafica add (civico varchar2(255))
/