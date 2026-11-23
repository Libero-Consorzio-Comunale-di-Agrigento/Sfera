--liquibase formatted sql
--changeset amarinelli:2.5_20240827_71_impostazione_RICERCA_NEL_TESTO runOnChange:true stripComments:false failOnError:false

begin
    impostazioni_pkg.add_impostazione (
          'RICERCA_NEL_TESTO'
        , 'Abilita la ricerca nel testo dei file'
        , 'RICERCA NEL TESTO'
        , 'N'
        , 'Y'
        , '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
    );
end;
/
