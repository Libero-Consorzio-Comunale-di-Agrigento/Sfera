--liquibase formatted sql
--changeset rdestasio:install_20200221_06

CREATE OR REPLACE FORCE VIEW SO4_V_ATTR_COMPONENTE
(
   ID_ATTR_COMPONENTE,
   ID_COMPONENTE,
   DAL,
   AL,
   CODICE_INCARICO,
   DESCRIZIONE_INCARICO,
   SE_RESPONSABILE,
   ORDINAMENTO,
   TELEFONO,
   E_MAIL,
   FAX,
   PERCENTUALE_IMPIEGO,
   GRADAZIONE,
   TIPO_ASSEGNAZIONE,
   ASSEGNAZIONE_PREVALENTE
)
AS
   SELECT id_attr_componente,
          id_componente,
          dal,
          al,
          incarico codice_incarico,
          des_incarico descrizione_incarico,
          DECODE (responsabile,  'SI', 1,  'NO', 0,  NULL) se_Responsabile,
          ordinamento,
          telefono,
          e_mail,
          fax,
          percentuale_impiego,
          gradazione,
          tipo_assegnazione,
          assegnazione_prevalente_char assegnazione_prevalente
     FROM SO4_ATTRIBUTI_COMPONENTE
/
