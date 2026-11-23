--liquibase formatted sql
--changeset mfrancesconi:2.6_20260427_15.DELIBERA_COMPETENZA_ALLA_PROPOSTA runOnChange:true stripComments:false failOnError:true


begin
    impostazioni_pkg.add_impostazione (
          'DELIBERA_COMPETENZA_ALLA_PROPOSTA'
        , 'Propaga le competenze della delibera alla proposta di delibera (pareri / allegati)'
        , 'DELIBERA: COMPETENZA ALLA PROPOSTA'
        , 'Y'
        , 'Y'
        , '<rowset><row label="No" value="N" /><row label="Si" value="Y" /></rowset>'
    );
end;
/