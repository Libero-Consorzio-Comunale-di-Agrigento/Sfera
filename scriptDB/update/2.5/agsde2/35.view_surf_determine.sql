--liquibase formatted sql
--changeset mfrancesconi:2.5_20230407_35_view_surf_determine runOnChange:true stripComments:false failOnError:false

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
 , riservato
 , stato
 , id_pratica
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
     , d.riservato
     , d.stato
     , (select valore from dati_aggiuntivi where id_determina = d.id_determina and codice = 'ID_PRATICA_SPORTELLO') id_pratica
from determine d, tipi_determina td
where d.valido = 'Y' and d.id_tipo_determina = td.id_tipo_determina
/