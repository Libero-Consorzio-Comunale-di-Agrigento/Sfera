--liquibase formatted sql
--changeset amarinelli:2.5_20240911_75_impostazione_IMPATTO_GENERAZIONALE runOnChange:true stripComments:false failOnError:true

begin
    impostazioni_pkg.add_impostazione (
          'IMPATTO_GENERAZIONALE'
        , 'Abilita la gestione dell''Impatto Generazionale'
        , 'IMPATTO GENERAZIONALE'
        , 'N'
        , 'Y'
        , '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
    );
end;
/
