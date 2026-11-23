--liquibase formatted sql
--changeset mfrancesconi:2.6_20260506_16.impostazione_CONTABILITA_CFA_IMPEGNO_OBBLIGATORIO runOnChange:true stripComments:false failOnError:true


begin
    impostazioni_pkg.add_impostazione (
          'CONTABILITA_CFA_IMPEGNO_OBBLIGATORIO'
        , 'Indica se è visibile il flag impegno obbligatorio in tipologia'
        , 'CONTABILITA CFA IMPEGNO OBBLIGATORIO'
        , 'N'
        , 'Y'
        , '<rowset><row label="No" value="N" /><row label="Si" value="Y" /></rowset>'
    );
end;
/