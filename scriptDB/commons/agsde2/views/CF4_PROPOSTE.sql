--liquibase formatted sql
--changeset mfrancesconi:CF4_PROPOSTE runOnChange:true stripComments:false

create or replace view cf4_proposte (
       id_documento,
       anno,
       unita_proponente,
       numero,
       oggetto,
       data_proposta,
       impegno,
       anno_cla,
       annullata,
       classificazione,
       class_dal,
       commissione,
       completa,
       data_modifica,
       data_ricevimento,
       data_trasmissione,
       esecutivita,
       file_proposta,
       file_relazione,
       firmata,
       iter,
       note,
       numero_cla,
       relatore,
       sub,
       tipo_proposta,
       tipo_trattamento,
       unita_ubicata,
       utente_modifica,
       workflow,
       tipologia,
       stato_proposta,
       impegno_obbligatorio,
       url_documento)
as
select d.id_determina,
       anno_proposta anno,
       tr.registro_esterno unita_proponente,
       numero_proposta numero,
       oggetto,
       data_proposta,
       (select decode (count (1), 0, 'N', 'Y')
        from visti_pareri vp, tipi_visto_parere tvp
        where d.id_determina = vp.id_determina
          and vp.id_tipologia = tvp.id_tipo_visto_parere
          and tvp.contabile = 'Y'
          and vp.valido = 'Y') impegno,
       null anno_cla,
       null annullata,
       null classificazione,
       null class_dal,
       null commissione,
       null completa,
       null data_modifica,
       null data_ricevimento,
       null data_trasmissione,
       null esecutivita,
       null file_proposta,
       null file_relazione,
       null firmata,
       null iter,
       null note,
       null numero_cla,
       null relatore,
       null sub,
       null tipo_proposta,
       null tipo_trattamento,
       null unita_ubicata,
       null utente_modifica,
       null workflow,
       null tipologia,
       null stato_proposta,
       td.impegno_obbligatorio,
       trim (impostazioni_pkg.get_impostazione ('URL_SERVER', d.ente)) || '/standalone.zul?operazione=APRI_DOCUMENTO&tipoDocumento=DETERMINA&id='|| d.id_determina
from determine d, tipi_registro tr, tipi_determina td
where d.valido = 'Y'
  and d.anno_proposta is not null
  and d.numero_proposta is not null
  and tr.tipo_registro = d.registro_proposta
  and tr.registro_esterno is not null
  and d.id_tipo_determina = td.id_tipo_determina
union
select pd.id_proposta_delibera,
       anno_proposta anno,
       tr.registro_esterno unita_proponente,
       numero_proposta numero,
       oggetto,
       data_proposta,
       (select decode (count (1), 0, 'N', 'Y')
        from visti_pareri vp, tipi_visto_parere tvp
        where pd.id_proposta_delibera = vp.id_proposta_delibera
          and vp.id_tipologia = tvp.id_tipo_visto_parere
          and tvp.contabile = 'Y'
          and vp.valido = 'Y') impegno,
       null anno_cla,
       null annullata,
       null classificazione,
       null class_dal,
       null commissione,
       null completa,
       null data_modifica,
       null data_ricevimento,
       null data_trasmissione,
       null esecutivita,
       null file_proposta,
       null file_relazione,
       null firmata,
       null iter,
       null note,
       null numero_cla,
       null relatore,
       null sub,
       null tipo_proposta,
       null tipo_trattamento,
       null unita_ubicata,
       null utente_modifica,
       null workflow,
       null tipologia,
       null stato_proposta,
       td.impegno_obbligatorio,
       trim (impostazioni_pkg.get_impostazione ('URL_SERVER', pd.ente)) || '/standalone.zul?operazione=APRI_DOCUMENTO&tipoDocumento=PROPOSTA_DELIBERA&id='|| pd.id_proposta_delibera
from proposte_delibera pd, tipi_registro tr, tipi_delibera td
where pd.valido = 'Y'
  and pd.anno_proposta is not null
  and pd.numero_proposta is not null
  and tr.tipo_registro = pd.registro_proposta
  and tr.registro_esterno is not null
  and pd.id_tipo_delibera= td.id_tipo_delibera
/
