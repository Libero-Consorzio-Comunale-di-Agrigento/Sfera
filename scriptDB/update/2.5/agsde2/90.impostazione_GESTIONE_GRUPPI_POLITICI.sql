--liquibase formatted sql
--changeset amarinelli:2.5_20250616_90.impostazione_GESTIONE_GRUPPI_POLITICI runOnChange:true stripComments:false failOnError:true


begin
    impostazioni_pkg.add_impostazione (
          'GESTIONE_GRUPPI_POLITICI'
        , 'Abilita la gestione Gruppi Politici.'
        , 'GESTIONE_GRUPPI_POLITICI'
        , 'N'
        , 'Y'
        , ''
    );
end;
/
