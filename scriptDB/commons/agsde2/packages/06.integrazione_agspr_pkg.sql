--liquibase formatted sql
--changeset mfrancesconi:2.6_20260721_06.integrazione_agspr_pkg runOnChange:true failOnError:false
--preconditions onFail:CONTINUE
--precondition-sql-check expectedResult:1 select count(1) from user_synonyms s where s.synonym_name = 'GDM_SEG_CLASSIFICAZIONI'

CREATE OR REPLACE PACKAGE integrazione_agspr_pkg
AS
    /**
       Allinea tutte le classifiche/fascicoli copiando da AGSPR ad AGSDE2 quando i dati
       differiscono tra i vari documenti (ad es classifica diversa tra GDM.GAT_DETERMINA e AGSDE2.DETERMINE)

       Questa procedure viene invocata dal AttiJob notturno.
    */
    procedure allinea_class_fasc;

END integrazione_agspr_pkg;
/

create or replace package body integrazione_agspr_pkg
as
   procedure allinea_class_fasc
   is
   begin
      merge into determine d
           using (select d.id_determina
                       , d.classifica_codice class_cod
                       , gd.class_cod gdm_class_cod
                       , d.classifica_dal class_dal
                       , gd.class_dal gdm_class_dal
                       , d.classifica_descrizione class_descr
                       , c.class_descr gdm_class_descr
                       , d.fascicolo_numero fasc_num
                       , gd.fascicolo_numero gdm_fasc_num
                       , d.fascicolo_anno fasc_anno
                       , gd.fascicolo_anno gdm_fasc_anno
                       , d.fascicolo_oggetto fasc_oggetto
                       , decode(f.riservato, 'Y', 'RISERVATO', f.fascicolo_oggetto) gdm_fasc_oggetto
                    from gdm_gat_determina gd
                       , determine d
                       , gdm_seg_classificazioni c
                       , gdm_documenti dc
                       , gdm_seg_fascicoli f
                       , gdm_documenti fc
                   where d.id_documento_esterno = gd.id_documento
                     and d.valido = 'Y'
                     and d.id_engine_iter is not null
                     and c.class_cod(+) = gd.class_cod
                     and c.class_dal(+) = gd.class_dal
                     and f.class_cod(+) = gd.class_cod
                     and f.class_dal(+) = gd.class_dal
                     and f.fascicolo_numero(+) = gd.fascicolo_numero
                     and f.fascicolo_anno(+) = gd.fascicolo_anno
                     and dc.id_documento(+) = c.id_documento
                     and nvl (dc.stato_documento, 'BO') = 'BO'
                     and fc.id_documento(+) = f.id_documento
                     and nvl (fc.stato_documento, 'BO') = 'BO'
                     and ( (decode (d.classifica_codice, gd.class_cod, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.classifica_dal, gd.class_dal, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.classifica_descrizione, c.class_descr, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.fascicolo_numero, gd.fascicolo_numero, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.fascicolo_anno, gd.fascicolo_anno, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.fascicolo_oggetto, decode(f.riservato, 'Y', 'RISERVATO', f.fascicolo_oggetto), 'uguale', 'diverso') = 'diverso'))) upd
              on (d.id_determina = upd.id_determina)
      when matched then
         update set d.classifica_codice        = upd.gdm_class_cod
                  , d.classifica_dal           = upd.gdm_class_dal
                  , d.classifica_descrizione   = upd.gdm_class_descr
                  , d.fascicolo_numero         = upd.gdm_fasc_num
                  , d.fascicolo_oggetto        = upd.gdm_fasc_oggetto
                  , d.fascicolo_anno           = upd.gdm_fasc_anno;

      merge into proposte_delibera d
           using (select d.id_proposta_delibera
                       , d.classifica_codice class_cod
                       , gd.class_cod gdm_class_cod
                       , d.classifica_dal class_dal
                       , gd.class_dal gdm_class_dal
                       , d.classifica_descrizione class_descr
                       , c.class_descr gdm_class_descr
                       , d.fascicolo_numero fasc_num
                       , gd.fascicolo_numero gdm_fasc_num
                       , d.fascicolo_anno fasc_anno
                       , gd.fascicolo_anno gdm_fasc_anno
                       , d.fascicolo_oggetto fasc_oggetto
                       , decode(f.riservato, 'Y', 'RISERVATO', f.fascicolo_oggetto) gdm_fasc_oggetto
                    from gdm_gat_proposta_delibera gd
                       , proposte_delibera d
                       , gdm_seg_classificazioni c
                       , gdm_documenti dc
                       , gdm_seg_fascicoli f
                       , gdm_documenti fc
                   where d.id_documento_esterno = gd.id_documento
                     and d.valido = 'Y'
                     and d.id_engine_iter is not null
                     and c.class_cod(+) = gd.class_cod
                     and c.class_dal(+) = gd.class_dal
                     and f.class_cod(+) = gd.class_cod
                     and f.class_dal(+) = gd.class_dal
                     and f.fascicolo_numero(+) = gd.fascicolo_numero
                     and f.fascicolo_anno(+) = gd.fascicolo_anno
                     and dc.id_documento(+) = c.id_documento
                     and nvl (dc.stato_documento, 'BO') = 'BO'
                     and fc.id_documento(+) = f.id_documento
                     and nvl (fc.stato_documento, 'BO') = 'BO'
                     and ( (decode (d.classifica_codice, gd.class_cod, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.classifica_dal, gd.class_dal, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.classifica_descrizione, c.class_descr, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.fascicolo_numero, gd.fascicolo_numero, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.fascicolo_anno, gd.fascicolo_anno, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.fascicolo_oggetto, decode(f.riservato, 'Y', 'RISERVATO', f.fascicolo_oggetto), 'uguale', 'diverso') = 'diverso'))) upd
              on (d.id_proposta_delibera = upd.id_proposta_delibera)
      when matched then
         update set d.classifica_codice        = upd.gdm_class_cod
                  , d.classifica_dal           = upd.gdm_class_dal
                  , d.classifica_descrizione   = upd.gdm_class_descr
                  , d.fascicolo_numero         = upd.gdm_fasc_num
                  , d.fascicolo_oggetto        = upd.gdm_fasc_oggetto
                  , d.fascicolo_anno           = upd.gdm_fasc_anno;


      merge into delibere d
           using (select d.id_delibera
                       , d.classifica_codice class_cod
                       , gd.class_cod gdm_class_cod
                       , d.classifica_dal class_dal
                       , gd.class_dal gdm_class_dal
                       , d.classifica_descrizione class_descr
                       , c.class_descr gdm_class_descr
                       , d.fascicolo_numero fasc_num
                       , gd.fascicolo_numero gdm_fasc_num
                       , d.fascicolo_anno fasc_anno
                       , gd.fascicolo_anno gdm_fasc_anno
                       , d.fascicolo_oggetto fasc_oggetto
                       , decode(f.riservato, 'Y', 'RISERVATO', f.fascicolo_oggetto) gdm_fasc_oggetto
                    from gdm_gat_delibera gd
                       , delibere d
                       , gdm_seg_classificazioni c
                       , gdm_documenti dc
                       , gdm_seg_fascicoli f
                       , gdm_documenti fc
                   where d.id_documento_esterno = gd.id_documento
                     and d.valido = 'Y'
                     and d.id_engine_iter is not null
                     and c.class_cod(+) = gd.class_cod
                     and c.class_dal(+) = gd.class_dal
                     and f.class_cod(+) = gd.class_cod
                     and f.class_dal(+) = gd.class_dal
                     and f.fascicolo_numero(+) = gd.fascicolo_numero
                     and f.fascicolo_anno(+) = gd.fascicolo_anno
                     and dc.id_documento(+) = c.id_documento
                     and nvl (dc.stato_documento, 'BO') = 'BO'
                     and fc.id_documento(+) = f.id_documento
                     and nvl (fc.stato_documento, 'BO') = 'BO'
                     and ( (decode (d.classifica_codice, gd.class_cod, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.classifica_dal, gd.class_dal, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.classifica_descrizione, c.class_descr, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.fascicolo_numero, gd.fascicolo_numero, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.fascicolo_anno, gd.fascicolo_anno, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.fascicolo_oggetto, decode(f.riservato, 'Y', 'RISERVATO', f.fascicolo_oggetto), 'uguale', 'diverso') = 'diverso'))) upd
              on (d.id_delibera = upd.id_delibera)
      when matched then
         update set d.classifica_codice        = upd.gdm_class_cod
                  , d.classifica_dal           = upd.gdm_class_dal
                  , d.classifica_descrizione   = upd.gdm_class_descr
                  , d.fascicolo_numero         = upd.gdm_fasc_num
                  , d.fascicolo_oggetto        = upd.gdm_fasc_oggetto
                  , d.fascicolo_anno           = upd.gdm_fasc_anno;


      merge into odg_sedute_stampe d
           using (select d.id_seduta_stampa
                       , d.classifica_codice class_cod
                       , gd.class_cod gdm_class_cod
                       , d.classifica_dal class_dal
                       , gd.class_dal gdm_class_dal
                       , d.classifica_descrizione class_descr
                       , c.class_descr gdm_class_descr
                       , d.fascicolo_numero fasc_num
                       , gd.fascicolo_numero gdm_fasc_num
                       , d.fascicolo_anno fasc_anno
                       , gd.fascicolo_anno gdm_fasc_anno
                       , d.fascicolo_oggetto fasc_oggetto
                       , decode(f.riservato, 'Y', 'RISERVATO', f.fascicolo_oggetto) gdm_fasc_oggetto
                    from gdm_gat_seduta_stampa gd
                       , odg_sedute_stampe d
                       , gdo_documenti gdo_d
                       , gdm_seg_classificazioni c
                       , gdm_documenti dc
                       , gdm_seg_fascicoli f
                       , gdm_documenti fc
                   where gdo_d.id_documento_esterno = gd.id_documento
                     and d.id_seduta_stampa = gdo_d.id_documento
                     and gdo_d.valido = 'Y'
                     and gdo_d.id_engine_iter is not null
                     and c.class_cod(+) = gd.class_cod
                     and c.class_dal(+) = gd.class_dal
                     and f.class_cod(+) = gd.class_cod
                     and f.class_dal(+) = gd.class_dal
                     and f.fascicolo_numero(+) = gd.fascicolo_numero
                     and f.fascicolo_anno(+) = gd.fascicolo_anno
                     and dc.id_documento(+) = c.id_documento
                     and nvl (dc.stato_documento, 'BO') = 'BO'
                     and fc.id_documento(+) = f.id_documento
                     and nvl (fc.stato_documento, 'BO') = 'BO'
                     and ( (decode (d.classifica_codice, gd.class_cod, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.classifica_dal, gd.class_dal, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.classifica_descrizione, c.class_descr, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.fascicolo_numero, gd.fascicolo_numero, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.fascicolo_anno, gd.fascicolo_anno, 'uguale', 'diverso') = 'diverso')
                       or (decode (d.fascicolo_oggetto, decode(f.riservato, 'Y', 'RISERVATO', f.fascicolo_oggetto), 'uguale', 'diverso') = 'diverso'))) upd
              on (d.id_seduta_stampa = upd.id_seduta_stampa)
      when matched then
         update set d.classifica_codice        = upd.gdm_class_cod
                  , d.classifica_dal           = upd.gdm_class_dal
                  , d.classifica_descrizione   = upd.gdm_class_descr
                  , d.fascicolo_numero         = upd.gdm_fasc_num
                  , d.fascicolo_oggetto        = upd.gdm_fasc_oggetto
                  , d.fascicolo_anno           = upd.gdm_fasc_anno;
   end;
end integrazione_agspr_pkg;
/