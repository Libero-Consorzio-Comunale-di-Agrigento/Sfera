--liquibase formatted sql
--changeset amarinelli:2.5_20251204_92.impostazione_CHATBOT_AI_ABILITA runOnChange:true stripComments:false failOnError:true


begin
    impostazioni_pkg.add_impostazione (
          'CHATBOT_AI_ABILITA'
        , 'Chatbot  abilitato o meno'
        , 'CHATBOT_AI_ABILITA'
        , 'N'
        , 'Y'
        , '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>'
    );
end;
/
