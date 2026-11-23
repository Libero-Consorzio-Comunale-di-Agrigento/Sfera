--liquibase formatted sql
--changeset mfrancesconi:2.5_20240130_57.update_table_notifiche.sql runOnChange:true stripComments:false failOnError:false

update NOTIFICHE
   set oggetti = 'DETERMINA#DELIBERA#PROPOSTA_DELIBERA#VISTO_PARERE#CERTIFICATO#SEDUTA_STAMPA'
 where tipo_notifica = 'ASSEGNAZIONE'
/