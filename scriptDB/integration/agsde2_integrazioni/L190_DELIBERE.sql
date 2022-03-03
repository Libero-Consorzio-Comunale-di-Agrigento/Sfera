--liquibase formatted sql
--changeset rdestasio:install_20200221_integrazioni_11

create or replace force view l190_delibere
(
   id_delibera
 , organo
 , anno_proposta
 , numero_proposta
 , registro_proposta
 , data_numero_delibera
 , anno_delibera
 , numero_delibera
 , registro_delibera
 , data_esecutivita
 , uo_proponente
 , oggetto
 , stato
 , ANNO_PROTOCOLLO
 , NUMERO_PROTOCOLLO
 , DATA_NUMERO_PROTOCOLLO
)
as
   select d.id_delibera
        , decode (registro_delibera
                , 'GIU', 'Giunta'
                , 'CON', 'Consiglio'
                , '')
             organo
        , pd.anno_proposta
        , pd.numero_proposta
        , pd.registro_proposta
        , trunc (d.data_numero_delibera)
        , d.anno_delibera
        , d.numero_delibera
        , d.registro_delibera
        , trunc (d.data_esecutivita)
        , utility_pkg.get_unita_prop_delibera (pd.id_proposta_delibera) uo_proponente
        , d.oggetto
        , d.stato
        , d.anno_protocollo
        , d.numero_protocollo
        , d.data_numero_protocollo
     from delibere d, proposte_delibera pd
    where d.id_proposta_delibera = pd.id_proposta_delibera
      and d.valido = 'Y'
      and pd.valido = 'Y'
/