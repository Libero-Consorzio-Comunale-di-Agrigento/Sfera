--liquibase formatted sql
--changeset mfrancesconi:2.6_20260514_18.impostazione_SO4_SUDDIVISIONE_DIREZIONE runOnChange:true stripComments:false failOnError:true


begin
    impostazioni_pkg.add_impostazione (
          'SO4_SUDDIVISIONE_DIREZIONE'
        , 'Codice della suddivisione SO4 che individua la direzione (usata in stampa per il calcolo dell''area). Indicare - se non utilizzata'
        , 'SO4 SUDDIVISIONE DIREZIONE'
        , '-'
        , 'Y'
        , null
    );
end;
/