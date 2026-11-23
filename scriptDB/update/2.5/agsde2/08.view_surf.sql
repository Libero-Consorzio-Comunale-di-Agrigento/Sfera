--liquibase formatted sql
--changeset mfrancesconi:2.5_20220428_08_view_surf runOnChange:true stripComments:false failOnError:false

create or replace force view surf_determine
(
   id_determina
 , anno_proposta
 , numero_proposta
 , registro_proposta
 , data_numero_determina
 , anno_determina
 , numero_determina
 , registro_determina
 , tipologia
 , data_esecutivita
 , oggetto
 , stato
)
as
select d.id_determina
     , d.anno_proposta
     , d.numero_proposta
     , d.registro_proposta
     , trunc (d.data_numero_determina)
     , d.anno_determina
     , d.numero_determina
     , d.registro_determina
     , td.titolo
     , trunc (d.data_esecutivita)
     , d.oggetto
     , d.stato
from determine d, tipi_determina td
where d.valido = 'Y' and d.id_tipo_determina = td.id_tipo_determina
/

create or replace force view surf_determine_file
(
   id_determina
 , id_file_gdm
 , id_documento_gdm
 , filename
 , tipo
 , area
 , codice_richiesta
 , modello
)
as
select d.id_determina
     , fa.id_file_esterno
     , gd.id_documento
     , fa.nome filename
     , 'PRINCIPALE'
     , gd.area
     , gd.codice_richiesta
     , 'DETERMINA'
from gdm_documenti gd, file_allegati fa, determine d
where d.id_file_allegato_testo = fa.id_file_allegato
  and d.valido = 'Y'
  and d.id_documento_esterno = gd.id_documento
union
select d.id_determina
     , fa.id_file_esterno
     , gd.id_documento
     , fa.nome filename
     , 'ALLEGATO'
     , gd.area
     , gd.codice_richiesta
     , 'ALLEGATO'
from gdm_documenti gd
   , allegati_file af
   , allegati a
   , file_allegati fa
   , determine d
where d.valido = 'Y'
  and d.id_determina = a.id_determina
  and a.valido = 'Y'
  and a.id_allegato = af.id_allegato
  and af.id_file = fa.id_file_allegato
  and a.id_documento_esterno = gd.id_documento
union
select v.id_determina
     , fa.id_file_esterno
     , gd.id_documento
     , fa.nome filename
     , 'VISTO'
     , gd.area
     , gd.codice_richiesta
     , 'ALLEGATO'
from gdm_documenti gd, file_allegati fa, visti_pareri v
where v.id_file_allegato_testo = fa.id_file_allegato
  and v.valido = 'Y'
  and v.id_documento_esterno = gd.id_documento
  and v.id_determina is not null
union
select v.id_determina
     , fa.id_file_esterno
     , gd.id_documento
     , fa.nome filename
     , 'ALLEGATO VISTO'
     , gd.area
     , gd.codice_richiesta
     , 'ALLEGATO'
from gdm_documenti gd
   , allegati_file af
   , allegati a
   , file_allegati fa
   , visti_pareri v
where v.valido = 'Y'
  and v.id_visto_parere = a.id_visto_parere
  and a.valido = 'Y'
  and a.id_allegato = af.id_allegato
  and af.id_file = fa.id_file_allegato
  and a.id_documento_esterno = gd.id_documento
  and v.id_determina is not null
union
select c.id_determina
     , fa.id_file_esterno
     , gd.id_documento
     , fa.nome filename
     , 'CERTIFICATO'
     , gd.area
     , gd.codice_richiesta
     , 'ALLEGATO'
from gdm_documenti gd, file_allegati fa, certificati c
where c.id_file_allegato_testo = fa.id_file_allegato
  and c.valido = 'Y'
  and c.id_documento_esterno = gd.id_documento
  and c.id_determina is not null
/

create or replace force view surf_delibere
(
   id_delibera
 , anno_proposta
 , numero_proposta
 , registro_proposta
 , data_numero_delibera
 , anno_delibera
 , numero_delibera
 , registro_delibera
 , tipologia
 , data_esecutivita
 , oggetto
 , stato
)
as
select d.id_delibera
     , pd.anno_proposta
     , pd.numero_proposta
     , pd.registro_proposta
     , trunc (d.data_numero_delibera)
     , d.anno_delibera
     , d.numero_delibera
     , d.registro_delibera
     , td.titolo
     , trunc (d.data_esecutivita)
     , d.oggetto
     , d.stato
from proposte_delibera pd, delibere d, tipi_delibera td
where d.id_proposta_delibera = pd.id_proposta_delibera and d.valido = 'Y' and pd.valido = 'Y' and pd.id_tipo_delibera = td.id_tipo_delibera
/

create or replace force view surf_delibere_file
(
   id_delibera
 , id_file_gdm
 , id_documento_gdm
 , filename
 , tipo
 , area
 , codice_richiesta
 , modello
)
as
select d.id_delibera
     , fa.id_file_esterno
     , gd.id_documento
     , fa.nome filename
     , 'PRINCIPALE'
     , gd.area
     , gd.codice_richiesta
     , 'DELIBERA'
from gdm_documenti gd, file_allegati fa, delibere d
where d.id_file_allegato_testo = fa.id_file_allegato
  and d.valido = 'Y'
  and d.id_documento_esterno = gd.id_documento
union
select d.id_delibera
     , fa.id_file_esterno
     , gd.id_documento
     , fa.nome filename
     , 'ALLEGATO'
     , gd.area
     , gd.codice_richiesta
     , 'ALLEGATO'
from gdm_documenti gd
   , allegati_file af
   , allegati a
   , file_allegati fa
   , delibere d
where d.valido = 'Y'
  and d.id_delibera = a.id_delibera
  and a.valido = 'Y'
  and a.id_allegato = af.id_allegato
  and af.id_file = fa.id_file_allegato
  and a.id_documento_esterno = gd.id_documento
union
select d.id_delibera
     , fa.id_file_esterno
     , gd.id_documento
     , fa.nome filename
     , 'PARERE'
     , gd.area
     , gd.codice_richiesta
     , 'ALLEGATO'
from gdm_documenti gd, file_allegati fa, visti_pareri v, delibere d
where v.id_file_allegato_testo = fa.id_file_allegato
  and v.valido = 'Y'
  and v.id_documento_esterno = gd.id_documento
  and v.id_proposta_delibera is not null
  and d.id_proposta_delibera = v.id_proposta_delibera
  and d.valido = 'Y'
union
select v.id_delibera
     , fa.id_file_esterno
     , gd.id_documento
     , fa.nome filename
     , 'PARERE'
     , gd.area
     , gd.codice_richiesta
     , 'ALLEGATO'
from gdm_documenti gd, file_allegati fa, visti_pareri v
where v.id_file_allegato_testo = fa.id_file_allegato
  and v.valido = 'Y'
  and v.id_documento_esterno = gd.id_documento
  and v.id_delibera is not null
union
select d.id_delibera
     , fa.id_file_esterno
     , gd.id_documento
     , fa.nome filename
     , 'ALLEGATO PARERE'
     , gd.area
     , gd.codice_richiesta
     , 'ALLEGATO'
from gdm_documenti gd
   , allegati_file af
   , allegati a
   , file_allegati fa
   , visti_pareri v
   , delibere d
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
select v.id_delibera
     , fa.id_file_esterno
     , gd.id_documento
     , fa.nome filename
     , 'ALLEGATO PARERE'
     , gd.area
     , gd.codice_richiesta
     , 'ALLEGATO'
from gdm_documenti gd
   , allegati_file af
   , allegati a
   , file_allegati fa
   , visti_pareri v
where v.valido = 'Y'
  and v.id_visto_parere = a.id_visto_parere
  and a.valido = 'Y'
  and a.id_allegato = af.id_allegato
  and af.id_file = fa.id_file_allegato
  and a.id_documento_esterno = gd.id_documento
  and v.id_delibera is not null
union
select c.id_delibera
     , fa.id_file_esterno
     , gd.id_documento
     , fa.nome filename
     , 'CERTIFICATO'
     , gd.area
     , gd.codice_richiesta
     , 'ALLEGATO'
from gdm_documenti gd, file_allegati fa, certificati c
where c.id_file_allegato_testo = fa.id_file_allegato
  and c.valido = 'Y'
  and c.id_documento_esterno = gd.id_documento
  and c.id_delibera is not null
/