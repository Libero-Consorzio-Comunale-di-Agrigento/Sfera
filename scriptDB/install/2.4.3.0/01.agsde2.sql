--liquibase formatted sql
--changeset rdestasio:2.4.3.0_20200221_01

-- aggiorno i valori del webservice ducd
update mapping_integrazioni set codice = 'UTENTE_WEBSERVICE' where codice = 'UTENTE' and categoria = 'PEC_DUCD'
/

update mapping_integrazioni set codice = 'PASSWORD_WEBSERVICE' where codice = 'PASSWORD' and categoria = 'PEC_DUCD'
/

update mapping_integrazioni set codice = 'URL_WEBSERVICE' where codice = 'URL' and categoria = 'PEC_DUCD'
/

-- aggiorno i parametri per la jworklist
BEGIN
  for c in (select codice, ente, valore from impostazioni where codice = 'JWORKLIST_WSDL_URL') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'JWORKLIST', 'URL_WEBSERVICE', '*', c.valore, c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'JWORKLIST', 'UTENTE_WEBSERVICE', '*', 'JWF', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'JWORKLIST', 'PASSWORD_WEBSERVICE', '*', 'JWF', c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'JWORKLIST_URL') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'JWORKLIST', c.codice, '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'JWORKLIST_ELIMINA_NOTIFICA_UO') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'JWORKLIST', c.codice, '*', decode(c.valore, 'UTENTE', 'N', 'Y'), c.ente, 0);
  end loop;
end;
/

-- aggiorno i parametri per il protocollo DOCarea
BEGIN
  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO_WS_UTENTE_CORRENTE') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_DOCAREA', 'USA_UTENTE_SESSIONE', '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO_WS_UTENTE') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_DOCAREA', 'UTENTE_WEBSERVICE', '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO_WS_PASSWORD') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_DOCAREA', 'PASSWORD_WEBSERVICE', '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO_WS_URL') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_DOCAREA', 'URL_WEBSERVICE', '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO_WS_CODICE_ENTE') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_DOCAREA', 'CODICE_ENTE', '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO_WS_CODICE_AOO') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_DOCAREA', 'CODICE_AOO', '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO_WS_TIPO_DOCUMENTO_DETERMINA') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_DOCAREA', 'TIPO_DOCUMENTO_DETERMINA', '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO_WS_TIPO_DOCUMENTO_DELIBERA') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_DOCAREA', 'TIPO_DOCUMENTO_DELIBERA', '*', c.valore, c.ente, 0);
  end loop;
end;
/

-- aggiorno i parametri per treviso:
BEGIN
  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO' and valore = 'protocolloTrevisoTest') loop

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'URL_WEBSERVICE', '*', 'http://10.100.200.1:10024/web/services/WSPROTOCT?wsdl', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'PASSWORD_WEBSERVICE', '*', 'DETETV00', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'FTP_SERVER_URL', '*', '10.100.200.116', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'FTP_UTENTE', '*', 'ftp', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'FTP_PASSWORD', '*', 'ftptreviso', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'FTP_DIRECTORY', '*', 'protocollazione', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'CODICE_ENTE', '*', 'p_tv', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'CODICE_AOO', '*', 'AOODIR01', c.ente, 0);

  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO' and valore = 'protocolloTreviso') loop

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'URL_WEBSERVICE', '*', 'http://10.100.200.1:10024/web/services/WSPROTOC?wsdl', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'PASSWORD_WEBSERVICE', '*', 'DETETV00', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'FTP_SERVER_URL', '*', '10.100.200.118', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'FTP_UTENTE', '*', 'ftp', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'FTP_PASSWORD', '*', 'ftptreviso', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'FTP_DIRECTORY', '*', 'protocollazione', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'CODICE_ENTE', '*', 'p_tv', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_TREVISO', 'CODICE_AOO', '*', 'AOODIR01', c.ente, 0);

  end loop;
end;
/

-- aggiorno i parametri per la contabilità del comune di modena:
BEGIN
  for c in (select codice, ente, valore from impostazioni where codice = 'CONTABILITA' and valore = 'integrazioneContabilitaComuneModena') loop

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'CONTABILITA_MODENA', 'URL_WEBSERVICE', '*', 'http://sib.comune.modena.it/sib/AttiAmministrativi', c.ente, 0);

  end loop;
end;
/

-- aggiorno i parametri per il protocollo gdm:
BEGIN
  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO_REGISTRO') loop

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_GDM', 'CODICE_REGISTRO', '*', c.valore, c.ente, 0);

  end loop;
  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO_GDM_PROPERTIES') loop

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_GDM', 'GDM_PROPERTIES', '*', c.valore, c.ente, 0);

  end loop;
end;
/

-- aggiorno i parametri per la contabilità del protocollo di modena:
BEGIN
  for c in (select codice, ente, valore from impostazioni where codice = 'PROTOCOLLO' and valore = 'protocolloComuneModena') loop

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_MODENA', 'URL_WEBSERVICE', '*', 'http://protocollo.comune.modena.it/ulisse/iride/web_services_20/WSProtocolloDM/WSProtocolloDM.asmx', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_MODENA', 'URL_WEBSERVICE_TITOLARIO', '*', 'http://sfera-iride-titolfascic.comune.modena.it/protocol-webservice-for-ads/services/ProtocolForADS.ProtocolForADSHttpSoap12Endpoint/', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_MODENA', 'URL_WEBSERVICE_FASCICOLO', '*', 'http://protocollo.comune.modena.it/ulisse/iride/web_services_20/WSFascicolo/WSFascicolo.asmx', c.ente, 0);

    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'PROTOCOLLO_MODENA', 'UTENTE_WEBSERVICE', '*', 'SFERA', c.ente, 0);

  end loop;
end;
/

-- aggiorno i parametri per la conservazione JCons
BEGIN
  for c in (select codice, ente, valore from impostazioni where codice = 'JCONS_WSDL_URL') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'JCONS', 'URL_WEBSERVICE', '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'JCONS_URL_SERVER') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'JCONS', 'URL_SERVER', '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'JCONS_CONTEXT_PATH') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'JCONS', 'CONTEXT_PATH', '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'JCONS_NOME_ITER') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'JCONS', 'NOME_ITER', '*', c.valore, c.ente, 0);
  end loop;
end;
/

-- aggiorno i parametri per la conservazione L190
BEGIN
  for c in (select codice, ente, valore from impostazioni where codice = 'CASA_DI_VETRO_WSDL') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'CASA_DI_VETRO', 'URL_WEBSERVICE', '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'CASA_DI_VETRO_ADMIN') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'CASA_DI_VETRO', 'UTENTE_WEBSERVICE', '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'CASA_DI_VETRO_URL') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'CASA_DI_VETRO', 'URL_SERVER', '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'CASA_DI_VETRO_SEZIONI_DETERMINE') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'CASA_DI_VETRO', 'SEZIONE_DETERMINE', '*', c.valore, c.ente, 0);
  end loop;

  for c in (select codice, ente, valore from impostazioni where codice = 'CASA_DI_VETRO_SEZIONI_DELIBERE') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'CASA_DI_VETRO', 'SEZIONE_DELIBERE', '*', c.valore, c.ente, 0);
  end loop;
end;
/

-- aggiorno i parametri per l'integrazione con Doc-ER
BEGIN
  for c in (select codice, ente, valore from impostazioni where codice like 'DOCER_%') loop
    insert into mapping_integrazioni (id_mapping_integrazione, categoria, codice, valore_interno, valore_esterno, ente, sequenza)
         values (hibernate_sequence.nextval, 'DOCER', c.codice, '*', c.valore, c.ente, 0);
  end loop;
end;
/

ALTER TABLE DESTINATARI_NOTIFICHE_ATTIVITA ADD (ID_NOTIFICA NUMBER(19))
/
