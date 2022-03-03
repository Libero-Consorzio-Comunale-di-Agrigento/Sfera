
CREATE OR REPLACE TRIGGER AGSDE2_DETE_TU
BEFORE UPDATE
ON GAT_DETERMINA
REFERENCING NEW AS New OLD AS Old
FOR EACH ROW
DECLARE
/******************************************************************************
   NAME:       AGSDE2_LINK_TIU
   PURPOSE:    

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        10/04/2015      esasdelli       1. Created this trigger.

   NOTES:

   Automatically available Auto Replace Keywords:
      Object Name:     AGSDE2_LINK_TIU
      Sysdate:         10/04/2015
      Date and Time:   10/04/2015, 14:43:21, and 10/04/2015 14:43:21
      Username:        esasdelli (set in TOAD Options, Proc Templates)
      Table Name:      LINKS (set in the "New PL/SQL Object" dialog)
      Trigger Options:  (set in the "New PL/SQL Object" dialog)
******************************************************************************/
BEGIN
   -- se class_cod class_dal, fasc_anno e fasc_numero sono diversi, aggiorno, altrimenti no.
   if (:New.class_cod != :Old.class_cod or
       :New.class_dal != :Old.class_dal or
       :New.fascicolo_anno   != :Old.fascicolo_anno or
       :New.fascicolo_numero != :Old.fascicolo_numero)
   then
        atti_agsde_competenze.aggiorna_class_fasc (:New.id_documento, :New.class_cod, :New.class_dal, :New.class_descr, :New.fascicolo_anno, :New.fascicolo_numero, :New.fascicolo_oggetto);        
   end if;
END AGSDE2_DETE_TU;
/

CREATE OR REPLACE TRIGGER AGSDE2_DELI_TU
BEFORE UPDATE
ON GAT_DELIBERA
REFERENCING NEW AS New OLD AS Old
FOR EACH ROW
DECLARE
/******************************************************************************
   NAME:       AGSDE2_LINK_TIU
   PURPOSE:    

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        10/04/2015      esasdelli       1. Created this trigger.

   NOTES:

   Automatically available Auto Replace Keywords:
      Object Name:     AGSDE2_LINK_TIU
      Sysdate:         10/04/2015
      Date and Time:   10/04/2015, 14:43:21, and 10/04/2015 14:43:21
      Username:        esasdelli (set in TOAD Options, Proc Templates)
      Table Name:      LINKS (set in the "New PL/SQL Object" dialog)
      Trigger Options:  (set in the "New PL/SQL Object" dialog)
******************************************************************************/
BEGIN
   -- se class_cod class_dal, fasc_anno e fasc_numero sono diversi, aggiorno, altrimenti no.
   if (:New.class_cod != :Old.class_cod or
       :New.class_dal != :Old.class_dal or
       :New.fascicolo_anno   != :Old.fascicolo_anno or
       :New.fascicolo_numero != :Old.fascicolo_numero)
   then
        atti_agsde_competenze.aggiorna_class_fasc (:New.id_documento, :New.class_cod, :New.class_dal, :New.class_descr, :New.fascicolo_anno, :New.fascicolo_numero, :New.fascicolo_oggetto);        
   end if;
END AGSDE2_DELI_TU;
/