--liquibase formatted sql
--changeset mfrancesconi:2.6_20260402_12.impostazione_DATA_SCADENZA_DISPOSIZIONI runOnChange:true stripComments:false failOnError:true


begin
    impostazioni_pkg.add_impostazione (
          'DATA_SCADENZA_DISPOSIZIONI'
        , 'Abilita l''inserimento della data di scadenza delle disposizioni'
        , 'DATA SCADENZA DISPOSIZIONI'
        , 'N'
        , 'Y'
        , '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>'
    );
end;
/