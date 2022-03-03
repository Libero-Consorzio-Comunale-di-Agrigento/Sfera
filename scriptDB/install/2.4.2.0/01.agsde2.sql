--liquibase formatted sql
--changeset rdestasio:2.4.2.0_20200221_01

-- marcatura temporale
ALTER TABLE DETERMINE ADD (MARCATURA_TEMPORALE char(1) default 'N')
/

ALTER TABLE DELIBERE ADD (MARCATURA_TEMPORALE char(1) default 'N')
/

ALTER TABLE VISTI_PARERI ADD (MARCATURA_TEMPORALE char(1) default 'N')
/

ALTER TABLE CERTIFICATI ADD (MARCATURA_TEMPORALE char(1) default 'N')
/

ALTER TABLE FILE_ALLEGATI ADD (MARCATURA_TEMPORALE char(1) default 'N')
/

-- conversione della pec in gestione notifiche come jworklist ed email

-- rimuovo colonne "vecchie" e non più usate:
ALTER TABLE DESTINATARI_NOTIFICHE_ATTIVITA DROP COLUMN ID_DELIBERA
/

ALTER TABLE DESTINATARI_NOTIFICHE_ATTIVITA DROP COLUMN ID_DETERMINA
/

ALTER TABLE DESTINATARI_NOTIFICHE_ATTIVITA DROP COLUMN ID_PROPOSTA_DELIBERA
/

alter table destinatari_notifiche_attivita modify (utente_ad4 null)
/

-- aggiungo la colonna modalita_invio per i destinatari delle notifiche:
alter table destinatari_notifiche_attivita add (MODALITA_INVIO VARCHAR(255) default 'JWORKLIST' NOT NULL )
/

-- per ogni pec inviata, creo un record nella destinatari_notifiche_attivita
declare
begin
  for c in (select dn.id_destinatario_notifica, ss.riferimento_pec, ss.id_seduta_stampa
  from destinatari_notifiche dn, odg_sedute_stampe ss
 where ss.id_seduta_stampa = dn.id_seduta_stampa
   and ss.riferimento_pec is not null) loop

   insert into destinatari_notifiche_attivita (id_dest_notifica_attivita, version, id_riferimento, id_destinatario_notifica, id_attivita, modalita_invio, soggetto_notifica)
   values (hibernate_sequence.nextval, 0, 'DOCUMENTO_'||c.id_seduta_stampa, c.id_destinatario_notifica, c.riferimento_pec, 'PEC', 'UTENTE');

   end loop;
end;
/

-- aggiorno l'azione:
update wkf_diz_azioni set nome_metodo = 'aggiungiDestinatariNotificaConvocazione' where nome_metodo = 'aggiungiConvocatiDestinatari'
/

update wkf_diz_azioni set nome_metodo = 'notificaConvocazione' where nome_metodo = 'notificaPEC'
/