--liquibase formatted sql
--changeset mferrara:2.6_20260119_03.impostazione_RICERCA_PER_SERVIZIO runOnChange:true stripComments:false failOnError:true


begin
    impostazioni_pkg.add_impostazione (
          'RICERCA_PER_SERVIZIO'
        , 'Indica se è abilitata la ricerca per servizio'
        , 'Attiva ricerca per servizio'
        , 'N'
        , 'Y'
        , '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>'
    );
end;
/