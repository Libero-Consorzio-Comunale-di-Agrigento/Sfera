--liquibase formatted sql
--changeset amarinelli:2.5_20251119_91.impostazione_ODG_ORDINAMENTO_PROPOSTE_SEPARATO runOnChange:true stripComments:false failOnError:true


begin
    impostazioni_pkg.add_impostazione (
          'FIRMA MULTIPLA ABILITATA'
        , 'Abilita il tab per le firme multiple'
        , 'FIRMA_MULTIPLA_ABILITATA'
        , 'Y'
        , 'Y'
        , '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>'
    );
end;
/

begin
    update impostazioni
    set valore = 'N',
        modificabile = 'N'
    where codice = 'CASA_DI_VETRO_RISERVATI';
exception when others then
    null;
end;
/
