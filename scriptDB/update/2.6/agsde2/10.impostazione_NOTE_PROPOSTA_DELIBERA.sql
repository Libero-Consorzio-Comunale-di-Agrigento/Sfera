--liquibase formatted sql
--changeset mfrancesconi:2.6_20260320_10.impostazione_NOTE_PROPOSTA_DELIBERA runOnChange:true stripComments:false failOnError:true


begin
    impostazioni_pkg.add_impostazione (
          'NOTE_PROPOSTA_DELIBERA'
        , 'Abilita la visualizzazione delle note della proposta direttamente dalla delibera'
        , 'NOTE PROPOSTA DELIBERA'
        , 'Y'
        , 'Y'
        , '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>'
    );
end;
/