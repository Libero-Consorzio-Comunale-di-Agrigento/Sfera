--liquibase formatted sql
--changeset mfrancesconi:2.5_20220914_15.view_surf_determine_delibere_file runOnChange:true stripComments:false failOnError:false

create or replace force view SURF_DETERMINE_FILE
(
   ID_DETERMINA,
   ID_FILE_GDM,
   ID_DOCUMENTO_GDM,
   FILENAME,
   TIPO,
   AREA,
   CODICE_RICHIESTA,
   MODELLO
) as
select /*+ index(DETERMINE_PK) */
    d.id_determina,
    fa.id_file_esterno,
    gd.id_documento,
    fa.nome filename,
    'PRINCIPALE',
    gd.area,
    gd.codice_richiesta,
    'DETERMINA'
from gdm_documenti gd, file_allegati fa, determine d
where d.id_file_allegato_testo = fa.id_file_allegato and d.id_documento_esterno = gd.id_documento and d.valido || '' = 'Y'
union all
select d.id_determina,
       fa.id_file_esterno,
       gd.id_documento,
       fa.nome filename,
       'ALLEGATO',
       gd.area,
       gd.codice_richiesta,
       'ALLEGATO'
from gdm_documenti gd,
     file_allegati fa,
     allegati_file af,
     allegati a,
     determine d
where d.valido || '' = 'Y'
  and a.id_determina = d.id_determina
  and a.valido = 'Y'
  and a.id_allegato = af.id_allegato
  and af.id_file = fa.id_file_allegato
  and a.id_documento_esterno = gd.id_documento
union all
select v.id_determina,
       fa.id_file_esterno,
       gd.id_documento,
       fa.nome filename,
       'VISTO',
       gd.area,
       gd.codice_richiesta,
       'ALLEGATO'
from gdm_documenti gd, file_allegati fa, visti_pareri v
where v.id_file_allegato_testo = fa.id_file_allegato
  and v.valido = 'Y'
  and v.id_documento_esterno = gd.id_documento
  and v.id_determina is not null
union all
select v.id_determina,
       fa.id_file_esterno,
       gd.id_documento,
       fa.nome filename,
       'ALLEGATO VISTO',
       gd.area,
       gd.codice_richiesta,
       'ALLEGATO'
from gdm_documenti gd,
     allegati_file af,
     allegati a,
     file_allegati fa,
     visti_pareri v
where v.valido = 'Y'
  and v.id_visto_parere = a.id_visto_parere
  and a.valido = 'Y'
  and a.id_allegato = af.id_allegato
  and af.id_file = fa.id_file_allegato
  and a.id_documento_esterno = gd.id_documento
  and v.id_determina is not null
union all
select c.id_determina,
       fa.id_file_esterno,
       gd.id_documento,
       fa.nome filename,
       'CERTIFICATO',
       gd.area,
       gd.codice_richiesta,
       'ALLEGATO'
from gdm_documenti gd, file_allegati fa, certificati c
where c.id_file_allegato_testo = fa.id_file_allegato
  and c.valido = 'Y'
  and c.id_documento_esterno = gd.id_documento
  and c.id_determina is not null
/

create or replace force view SURF_DELIBERE_FILE
(
   ID_DELIBERA,
   ID_FILE_GDM,
   ID_DOCUMENTO_GDM,
   FILENAME,
   TIPO,
   AREA,
   CODICE_RICHIESTA,
   MODELLO
) as
select  /*+ index(DELIBERE_PK) */
       d.id_delibera,
       fa.id_file_esterno,
       gd.id_documento,
       fa.nome filename,
       'PRINCIPALE',
       gd.area,
       gd.codice_richiesta,
       'DELIBERA'
from gdm_documenti gd, file_allegati fa, delibere d
where d.id_file_allegato_testo = fa.id_file_allegato and d.valido = 'Y' and d.id_documento_esterno = gd.id_documento
union
select d.id_delibera,
       fa.id_file_esterno,
       gd.id_documento,
       fa.nome filename,
       'ALLEGATO',
       gd.area,
       gd.codice_richiesta,
       'ALLEGATO'
from gdm_documenti gd,
     allegati_file af,
     allegati a,
     file_allegati fa,
     delibere d
where d.valido = 'Y'
  and d.id_delibera = a.id_delibera
  and a.valido = 'Y'
  and a.id_allegato = af.id_allegato
  and af.id_file = fa.id_file_allegato
  and a.id_documento_esterno = gd.id_documento
union
select d.id_delibera,
       fa.id_file_esterno,
       gd.id_documento,
       fa.nome filename,
       'PARERE',
       gd.area,
       gd.codice_richiesta,
       'ALLEGATO'
from gdm_documenti gd,
     file_allegati fa,
     visti_pareri v,
     delibere d
where v.id_file_allegato_testo = fa.id_file_allegato
  and v.valido = 'Y'
  and v.id_documento_esterno = gd.id_documento
  and v.id_proposta_delibera is not null
  and d.id_proposta_delibera = v.id_proposta_delibera
  and d.valido = 'Y'
union
select v.id_delibera,
       fa.id_file_esterno,
       gd.id_documento,
       fa.nome filename,
       'PARERE',
       gd.area,
       gd.codice_richiesta,
       'ALLEGATO'
from gdm_documenti gd, file_allegati fa, visti_pareri v
where v.id_file_allegato_testo = fa.id_file_allegato
  and v.valido = 'Y'
  and v.id_documento_esterno = gd.id_documento
  and v.id_delibera is not null
union
select d.id_delibera,
       fa.id_file_esterno,
       gd.id_documento,
       fa.nome filename,
       'ALLEGATO PARERE',
       gd.area,
       gd.codice_richiesta,
       'ALLEGATO'
from gdm_documenti gd,
     allegati_file af,
     allegati a,
     file_allegati fa,
     visti_pareri v,
     delibere d
where v.valido = 'Y'
  and v.id_visto_parere = a.id_visto_parere
  and a.valido = 'Y'
  and a.id_allegato = af.id_allegato
  and af.id_file = fa.id_file_allegato
  and a.id_documento_esterno = gd.id_documento
  and v.id_proposta_delibera is not null
  and d.id_proposta_delibera = v.id_proposta_delibera
  and d.valido = 'Y'
union
select v.id_delibera,
       fa.id_file_esterno,
       gd.id_documento,
       fa.nome filename,
       'ALLEGATO PARERE',
       gd.area,
       gd.codice_richiesta,
       'ALLEGATO'
from gdm_documenti gd,
     allegati_file af,
     allegati a,
     file_allegati fa,
     visti_pareri v
where v.valido = 'Y'
  and v.id_visto_parere = a.id_visto_parere
  and a.valido = 'Y'
  and a.id_allegato = af.id_allegato
  and af.id_file = fa.id_file_allegato
  and a.id_documento_esterno = gd.id_documento
  and v.id_delibera is not null
union
select c.id_delibera,
       fa.id_file_esterno,
       gd.id_documento,
       fa.nome filename,
       'CERTIFICATO',
       gd.area,
       gd.codice_richiesta,
       'ALLEGATO'
from gdm_documenti gd, file_allegati fa, certificati c
where c.id_file_allegato_testo = fa.id_file_allegato
  and c.valido = 'Y'
  and c.id_documento_esterno = gd.id_documento
  and c.id_delibera is not null
/
