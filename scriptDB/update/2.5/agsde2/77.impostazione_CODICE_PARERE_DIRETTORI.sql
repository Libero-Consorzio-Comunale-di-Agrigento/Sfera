--liquibase formatted sql
--changeset mfrancesconi:2.5_20240924_77.impostazione_CODICE_PARERE_DIRETTORI runOnChange:true stripComments:false failOnError:true

begin
    impostazioni_pkg.add_impostazione (
          'CODICE_PARERE_DIRETTORE_AMMINISTRATIVO'
        , 'Codice del parere del Direttore Amministrativo'
        , 'CODICE PARERE DIRETTORE AMMINISTRATIVO'
        , '-'
        , 'Y'
        , ''
    );

    impostazioni_pkg.add_impostazione (
          'CODICE_PARERE_DIRETTORE_SANITARIO'
        , 'Codice del parere del Direttore Sanitario'
        , 'CODICE PARERE DIRETTORE SANITARIO'
        , '-'
        , 'Y'
        , ''
    );
end;
/