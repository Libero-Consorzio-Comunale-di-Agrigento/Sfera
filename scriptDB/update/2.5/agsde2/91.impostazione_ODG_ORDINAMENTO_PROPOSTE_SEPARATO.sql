--liquibase formatted sql
--changeset amarinelli:2.5_20251119_91.impostazione_ODG_ORDINAMENTO_PROPOSTE_SEPARATO runOnChange:true stripComments:false failOnError:true


begin
    impostazioni_pkg.add_impostazione (
          'ODG_ORDINAMENTO_PROPOSTE_SEPARATO'
        , 'Abilita ordinamento proposte separato dalla modifica'
        , 'ODG_ORDINAMENTO_PROPOSTE_SEPARATO'
        , 'N'
        , 'Y'
        , '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>'
    );
end;
/
