--liquibase formatted sql
--changeset mfrancesconi:2.5_20230406_34.vista_documenti_errore_albo runOnChange:true

create or replace force view DOCUMENTI_ERRORE_ALBO
(
   TIPO_OGGETTO,
   ID,
   ID_ALBO,
   NUMERO,
   ANNO,
   REGISTRO,
   DATA_INIZIO_PUBBLICAZIONE,
   DATA_FINE_PUBBLICAZIONE,
   OGGETTO
)
as
   select 'Delibera' tipo_oggetto,
          d.id_delibera id,
          d.id_documento_albo id_albo,
          d.numero_delibera,
          d.anno_delibera,
          d.registro_delibera,
          d.data_pubblicazione,
          d.data_fine_pubblicazione,
          d.oggetto
     from delibere d
    where d.id_documento_albo is not null
      and d.valido = 'Y'
      and id_documento_albo not in (select id_documento
                                      from gdm_mes_albi gma
                                     where gma.id_documento = d.id_documento_albo
                                       and gma.id_doc_delibera = d.id_documento_esterno)
   union all
   select 'Determina' tipo_oggetto,
          d.id_determina id,
          d.id_documento_albo id_albo,
          d.numero_determina,
          d.anno_determina,
          d.registro_determina,
          d.data_pubblicazione,
          d.data_fine_pubblicazione,
          d.oggetto
     from determine d
    where d.id_documento_albo is not null
      and d.valido = 'Y'
      and id_documento_albo not in (select id_documento
                                      from gdm_mes_albi gma
                                     where gma.id_documento = d.id_documento_albo
                                       and gma.id_doc_determina = d.id_documento_esterno)
   union all
   select 'Stampa Seduta' tipo_oggetto,
          d.id_seduta_stampa id,
          d.id_documento_albo id_albo,
          null,
          null,
          null,
          d.data_pubblicazione,
          d.data_fine_pubblicazione,
          null
     from odg_sedute_stampe d
    where d.id_documento_albo is not null
      and d.valido = 'Y'
      and id_documento_albo not in (select id_documento
                                      from gdm_mes_albi gma
                                     where gma.id_documento = d.id_documento_albo)
/