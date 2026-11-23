--liquibase formatted sql
--changeset mfrancesconi:2.5_20250513_87_i87.impostazione_VISUALIZZATORE_TESTO_LOGO runOnChange:true stripComments:false failOnError:true

begin
    impostazioni_pkg.add_impostazione (
          'VIS_TESTO_LOGO'
        , 'Testo alternativo del logo del visualizzatore'
        , 'VISUALIZZATORE TESTO LOGO'
        , 'Ente'
        , 'Y'
        , ''
    );
end;
/
