--liquibase formatted sql
--changeset amarinelli:2.6_20260812_20.impostazione_INVIO_MAIL_ERRORI runOnChange:true stripComments:false failOnError:true


begin
    impostazioni_pkg.add_impostazione (
          'INVIO_MAIL_ERRORI'
        , 'Contiene le mail destinatarie degli errori delle integrazioni (inserire le mail separate da punto e virgola)'
        , 'INVIO_MAIL_ERRORI'
        , '-'
        , 'Y'
        , null
    );
end;
/