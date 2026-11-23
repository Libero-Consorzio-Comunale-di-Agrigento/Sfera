--liquibase formatted sql
--changeset mfrancesconi:2.6_20260514_18.impostazione_SO4_SUDDIVISIONE_DIREZIONE runOnChange:true stripComments:false failOnError:true


begin
    impostazioni_pkg.add_impostazione (
          'VIS_RICERCA_UNITA_PROPONENTE'
        , 'Visualizzatore: bottone per filtrare le unità proponenti'
        , 'VIS RICERCA UNITA PROPONENTE'
        , 'N'
        , 'Y'
        , '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>'
    );
end;
/