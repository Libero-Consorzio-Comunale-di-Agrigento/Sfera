--liquibase formatted sql
--changeset mfrancesconi:2.5_20230609_43_view_l190_determine runOnChange:true stripComments:false failOnError:false

create or replace force view L190_DETERMINE
    (id_determina,
    anno_proposta,
    numero_proposta,
    registro_proposta,
    registro_proposta_esterno,
    data_numero_determina,
    anno_determina,
    numero_determina,
    registro_determina,
    registro_determina_descrizione,
     registro_determina_esterno,
    data_esecutivita,
    uo_proponente,
    dirigente,
    oggetto,
    codice_oggetto_ricorrente,
    servizio_fornitura,
    tipo_oggetto,
    norma,
    modalita,
    stato,
    cig,
    anno_protocollo,
    numero_protocollo,
    data_numero_protocollo)
as
select d.id_determina,
       d.anno_proposta,
       d.numero_proposta,
       d.registro_proposta,
       registro_prop.registro_esterno,
       trunc (d.data_numero_determina),
       d.anno_determina,
       d.numero_determina,
       d.registro_determina,
       registro_dete.descrizione,
       registro_dete.registro_esterno,
       trunc (d.data_esecutivita),
       utility_pkg.get_unita_prop_determina (d.id_determina) uo_proponente,
       utility_pkg.get_nominativo_sogg_dete (d.id_determina, 'DIRIGENTE') dirigente,
       d.oggetto,
       ogg.codice,
       ogg.servizio_fornitura,
       ogg.tipo,
       ogg.norma,
       ogg.modalita,
       d.stato,
       utility_pkg.get_cig_determina (d.id_determina, d.ente) cig,
       d.anno_protocollo,
       d.numero_protocollo,
       d.data_numero_protocollo
  from determine d,
       oggetti_ricorrenti ogg,
       tipi_registro registro_prop,
       tipi_registro registro_dete
 where d.valido = 'Y'
   and d.registro_proposta = registro_prop.tipo_registro
   and d.registro_determina = registro_dete.tipo_registro(+)
   and d.id_oggetto_ricorrente = ogg.id_oggetto_ricorrente(+)
/

create or replace force view L190_DELIBERE
(
   ID_DELIBERA,
   ORGANO,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   REGISTRO_PROPOSTA,
   DATA_NUMERO_DELIBERA,
   ANNO_DELIBERA,
   NUMERO_DELIBERA,
   REGISTRO_DELIBERA,
   DATA_ESECUTIVITA,
   UO_PROPONENTE,
   OGGETTO,
   STATO,
   ANNO_PROTOCOLLO,
   NUMERO_PROTOCOLLO,
   DATA_NUMERO_PROTOCOLLO
)
as
   select d.id_delibera,
          decode (registro_delibera,  'GIU', 'Giunta',  'CON', 'Consiglio',  '') organo,
          pd.anno_proposta,
          pd.numero_proposta,
          pd.registro_proposta,
          trunc (d.data_numero_delibera),
          d.anno_delibera,
          d.numero_delibera,
          d.registro_delibera,
          trunc (d.data_esecutivita),
          utility_pkg.get_unita_prop_delibera (pd.id_proposta_delibera) uo_proponente,
          d.oggetto,
          d.stato,
          d.anno_protocollo,
          d.numero_protocollo,
          d.data_numero_protocollo
     from delibere d, proposte_delibera pd
    where d.id_proposta_delibera = pd.id_proposta_delibera and d.valido = 'Y' and pd.valido = 'Y'
/