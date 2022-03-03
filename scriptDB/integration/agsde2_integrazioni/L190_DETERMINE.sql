--liquibase formatted sql
--changeset rdestasio:install_20200221_integrazioni_12

CREATE OR REPLACE FORCE VIEW L190_DETERMINE
(
   ID_DETERMINA,
   ANNO_PROPOSTA,
   NUMERO_PROPOSTA,
   REGISTRO_PROPOSTA,
   REGISTRO_PROPOSTA_ESTERNO,
   DATA_NUMERO_DETERMINA,
   ANNO_DETERMINA,
   NUMERO_DETERMINA,
   REGISTRO_DETERMINA,
   REGISTRO_DETERMINA_DESCRIZIONE,
   REGISTRO_DETERMINA_ESTERNO,
   DATA_ESECUTIVITA,
   UO_PROPONENTE,
   DIRIGENTE,
   OGGETTO,
   CODICE_OGGETTO_RICORRENTE,
   SERVIZIO_FORNITURA,
   TIPO_OGGETTO,
   NORMA,
   MODALITA,
   STATO,
   CIG,
   ANNO_PROTOCOLLO,
   NUMERO_PROTOCOLLO,
   DATA_NUMERO_PROTOCOLLO

)
AS
   SELECT d.id_determina,
          d.anno_proposta,
          d.numero_proposta,
          d.registro_proposta,
          registro_prop.registro_esterno,
          TRUNC (d.data_numero_determina),
          d.anno_determina,
          d.numero_determina,
          d.registro_determina,
          registro_dete.descrizione,
          registro_dete.registro_esterno,
          TRUNC (d.data_esecutivita),
          utility_pkg.get_unita_prop_determina (d.id_determina) uo_proponente,
          utility_pkg.get_nominativo_sogg_dete (d.id_determina, 'DIRIGENTE') dirigente,
          d.oggetto,
          ogg.codice,
          ogg.servizio_fornitura,
          ogg.tipo,
          ogg.norma,
          ogg.modalita,
          d.stato,
          d.cig,
          d.anno_protocollo,
          d.numero_protocollo,
          d.data_numero_protocollo
     FROM determine d,
          oggetti_ricorrenti ogg,
          tipi_registro registro_prop,
          tipi_registro registro_dete
    WHERE     d.valido = 'Y'
          AND d.registro_proposta = registro_prop.tipo_registro
          AND d.registro_determina = registro_dete.tipo_registro(+)
          AND d.id_oggetto_ricorrente = ogg.id_oggetto_ricorrente(+)
/