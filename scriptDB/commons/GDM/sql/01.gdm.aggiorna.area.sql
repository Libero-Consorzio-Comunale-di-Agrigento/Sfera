--liquibase formatted sql
--changeset rdestasio:install_20200221_01_gdm_aggiorna_area

begin
    for c in (select td.id_tipodoc, jl.id_tipodoc id_tipodoc_link, ac.tipo_oggetto tipo_oggetto, ac.titolo_popup titolo_popup
                from tipi_documento td
                   , jdms_link jl
                   , (select 'DETERMINA' cm, 'SEGRETERIA.ATTI.2_0' area, 'DETERMINA' tipo_oggetto, 'DETERMINA' titolo_popup from dual union
                      select 'DELIBERA' cm, 'SEGRETERIA.ATTI.2_0' area, 'DELIBERA' tipo_oggetto, 'DELIBERA' titolo_popup from dual union
                      select 'PROPOSTA_DELIBERA' cm, 'SEGRETERIA.ATTI.2_0' area, 'PROPOSTA_DELIBERA' tipo_oggetto, 'PROPOSTA DELIBERA' titolo_popup from dual UNION
                      select 'SEDUTA_STAMPA' cm, 'SEGRETERIA.ATTI.2_0' area, 'SEDUTA_STAMPA' tipo_oggetto, 'STAMPA della SEDUTA' titolo_popup from dual) ac
               where td.nome = ac.cm and td.area_modello = ac.area
                 and jl.id_tipodoc(+) = td.id_tipodoc) loop
        
        if (c.id_tipodoc_link is null)
        then
            insert into JDMS_LINK (ID_TIPODOC,TAG,URL,ICONA,TOOLTIP,UTENTE_AGGIORNAMENTO,DATA_AGGIORNAMENTO,ICONA_EXP) values (to_char(c.id_tipodoc),'5','var wd=window.open(''/Atti/standalone.zul?operazione=APRI_DOCUMENTO&tipoDocumento='||c.tipo_oggetto||'&idDocumentoEsterno=:idOggetto'', '''||c.titolo_popup||''',''toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 0,copyhistory= 0,modal=yes'');resizeFullScreen(wd,0,100);','PROT_MODIFICA','Apre la '||c.titolo_popup,'GDM', sysdate, null);
            insert into JDMS_LINK (ID_TIPODOC,TAG,URL,ICONA,TOOLTIP,UTENTE_AGGIORNAMENTO,DATA_AGGIORNAMENTO,ICONA_EXP) values (to_char(c.id_tipodoc),'-5','/Atti/standalone.zul?operazione=APRI_DOCUMENTO&tipoDocumento='||c.tipo_oggetto||'&idDocumentoEsterno=:idOggetto','PROT_MODIFICA','Apre la '||c.titolo_popup,'GDM', sysdate, null);
        end if;
    end loop;
end;
/

begin
	MERGE INTO TIPI_RELAZIONE A USING
	 (SELECT
	  'SEGRETERIA.ATTI.2_0' as AREA,
	  'ALLEGATI' as TIPO_RELAZIONE,
	  'Allegati' as DESCRIZIONE,
	  'S' as VISIBILE,
	  'S' as DIPENDENZA,
	  sysdate as DATA_AGGIORNAMENTO,
	  'GDM' as UTENTE_AGGIORNAMENTO
	  FROM DUAL) B
	ON (A.AREA = B.AREA and A.TIPO_RELAZIONE = B.TIPO_RELAZIONE)
	WHEN NOT MATCHED THEN 
	INSERT (
	  AREA, TIPO_RELAZIONE, DESCRIZIONE, VISIBILE, DIPENDENZA, 
	  DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)
	VALUES (
	  B.AREA, B.TIPO_RELAZIONE, B.DESCRIZIONE, B.VISIBILE, B.DIPENDENZA, 
	  B.DATA_AGGIORNAMENTO, B.UTENTE_AGGIORNAMENTO)
	WHEN MATCHED THEN
	UPDATE SET 
	  A.DESCRIZIONE = B.DESCRIZIONE,
	  A.VISIBILE = B.VISIBILE,
	  A.DIPENDENZA = B.DIPENDENZA,
	  A.DATA_AGGIORNAMENTO = B.DATA_AGGIORNAMENTO,
	  A.UTENTE_AGGIORNAMENTO = B.UTENTE_AGGIORNAMENTO;
	
	MERGE INTO TIPI_RELAZIONE A USING
	 (SELECT
	  'SEGRETERIA.ATTI.2_0' as AREA,
	  'CERT' as TIPO_RELAZIONE,
	  'Certificati' as DESCRIZIONE,
	  'S' as VISIBILE,
	  'S' as DIPENDENZA,
	  sysdate as DATA_AGGIORNAMENTO,
	  'GDM' as UTENTE_AGGIORNAMENTO
	  FROM DUAL) B
	ON (A.AREA = B.AREA and A.TIPO_RELAZIONE = B.TIPO_RELAZIONE)
	WHEN NOT MATCHED THEN 
	INSERT (
	  AREA, TIPO_RELAZIONE, DESCRIZIONE, VISIBILE, DIPENDENZA, 
	  DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)
	VALUES (
	  B.AREA, B.TIPO_RELAZIONE, B.DESCRIZIONE, B.VISIBILE, B.DIPENDENZA, 
	  B.DATA_AGGIORNAMENTO, B.UTENTE_AGGIORNAMENTO)
	WHEN MATCHED THEN
	UPDATE SET 
	  A.DESCRIZIONE = B.DESCRIZIONE,
	  A.VISIBILE = B.VISIBILE,
	  A.DIPENDENZA = B.DIPENDENZA,
	  A.DATA_AGGIORNAMENTO = B.DATA_AGGIORNAMENTO,
	  A.UTENTE_AGGIORNAMENTO = B.UTENTE_AGGIORNAMENTO;
	
	MERGE INTO TIPI_RELAZIONE A USING
	 (SELECT
	  'SEGRETERIA.ATTI.2_0' as AREA,
	  'VISTI' as TIPO_RELAZIONE,
	  'Visti e Pareri' as DESCRIZIONE,
	  'S' as VISIBILE,
	  'S' as DIPENDENZA,
	  sysdate as DATA_AGGIORNAMENTO,
	  'GDM' as UTENTE_AGGIORNAMENTO
	  FROM DUAL) B
	ON (A.AREA = B.AREA and A.TIPO_RELAZIONE = B.TIPO_RELAZIONE)
	WHEN NOT MATCHED THEN 
	INSERT (
	  AREA, TIPO_RELAZIONE, DESCRIZIONE, VISIBILE, DIPENDENZA, 
	  DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)
	VALUES (
	  B.AREA, B.TIPO_RELAZIONE, B.DESCRIZIONE, B.VISIBILE, B.DIPENDENZA, 
	  B.DATA_AGGIORNAMENTO, B.UTENTE_AGGIORNAMENTO)
	WHEN MATCHED THEN
	UPDATE SET 
	  A.DESCRIZIONE = B.DESCRIZIONE,
	  A.VISIBILE = B.VISIBILE,
	  A.DIPENDENZA = B.DIPENDENZA,
	  A.DATA_AGGIORNAMENTO = B.DATA_AGGIORNAMENTO,
	  A.UTENTE_AGGIORNAMENTO = B.UTENTE_AGGIORNAMENTO;

	MERGE INTO TIPI_RELAZIONE A USING
	 (SELECT
	  'SEGRETERIA.ATTI.2_0' as AREA,
	  'MAIL' as TIPO_RELAZIONE,
	  'Mail associata' as DESCRIZIONE,
	  'S' as VISIBILE,
	  'N' as DIPENDENZA,
	  sysdate as DATA_AGGIORNAMENTO,
	  'GDM' as UTENTE_AGGIORNAMENTO
	  FROM DUAL) B
	ON (A.AREA = B.AREA and A.TIPO_RELAZIONE = B.TIPO_RELAZIONE)
	WHEN NOT MATCHED THEN
	INSERT (
	  AREA, TIPO_RELAZIONE, DESCRIZIONE, VISIBILE, DIPENDENZA,
	  DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)
	VALUES (
	  B.AREA, B.TIPO_RELAZIONE, B.DESCRIZIONE, B.VISIBILE, B.DIPENDENZA,
	  B.DATA_AGGIORNAMENTO, B.UTENTE_AGGIORNAMENTO)
	WHEN MATCHED THEN
	UPDATE SET
	  A.DESCRIZIONE = B.DESCRIZIONE,
	  A.VISIBILE = B.VISIBILE,
	  A.DIPENDENZA = B.DIPENDENZA,
	  A.DATA_AGGIORNAMENTO = B.DATA_AGGIORNAMENTO,
	  A.UTENTE_AGGIORNAMENTO = B.UTENTE_AGGIORNAMENTO;

	COMMIT;

end;
/

MERGE INTO TIPI_RELAZIONE A USING
	 (SELECT
	  'SEGRETERIA.ATTI.2_0' as AREA,
	  'MAIL' as TIPO_RELAZIONE,
	  'Mail associata' as DESCRIZIONE,
	  'S' as VISIBILE,
	  'N' as DIPENDENZA,
	  sysdate as DATA_AGGIORNAMENTO,
	  'GDM' as UTENTE_AGGIORNAMENTO
	  FROM DUAL) B
	ON (A.AREA = B.AREA and A.TIPO_RELAZIONE = B.TIPO_RELAZIONE)
	WHEN NOT MATCHED THEN
	INSERT (
	  AREA, TIPO_RELAZIONE, DESCRIZIONE, VISIBILE, DIPENDENZA,
	  DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)
	VALUES (
	  B.AREA, B.TIPO_RELAZIONE, B.DESCRIZIONE, B.VISIBILE, B.DIPENDENZA,
	  B.DATA_AGGIORNAMENTO, B.UTENTE_AGGIORNAMENTO)
	WHEN MATCHED THEN
	UPDATE SET
	  A.DESCRIZIONE = B.DESCRIZIONE,
	  A.VISIBILE = B.VISIBILE,
	  A.DIPENDENZA = B.DIPENDENZA,
	  A.DATA_AGGIORNAMENTO = B.DATA_AGGIORNAMENTO,
	  A.UTENTE_AGGIORNAMENTO = B.UTENTE_AGGIORNAMENTO
/

begin
    execute immediate 'CREATE INDEX GAT$DELI$NOTE_CTX ON GAT_DELIBERA (NOTE) INDEXTYPE IS CTXSYS.CTXCAT PARAMETERS(''lexer italian_lexer wordlist italian_wordlist stoplist italian_stoplist memory 10M'') NOPARALLEL';
exception when others then
    null;
end;
/

begin
    execute immediate 'CREATE INDEX GAT$DELI$OGGE_CTX ON GAT_DELIBERA  (OGGETTO) 	INDEXTYPE IS  CTXSYS.CTXCAT PARAMETERS(''lexer italian_lexer wordlist italian_wordlist stoplist italian_stoplist memory 10M'') NOPARALLEL';
exception when others then
    null;
end;
/

begin
    execute immediate 'CREATE INDEX GAT$DETE$OGGE_CTX ON GAT_DETERMINA (OGGETTO) 	INDEXTYPE IS CTXSYS.CTXCAT PARAMETERS(''lexer italian_lexer wordlist italian_wordlist stoplist italian_stoplist memory 10M'') NOPARALLEL';
exception when others then
    null;
end;
/

begin
    execute immediate 'CREATE INDEX GAT$DETE$NOTE_CTX ON GAT_DETERMINA (NOTE) 		INDEXTYPE IS CTXSYS.CTXCAT PARAMETERS(''lexer italian_lexer wordlist italian_wordlist stoplist italian_stoplist memory 10M'') NOPARALLEL';
exception when others then
    null;
end;
/

begin
    execute immediate 'CREATE INDEX GAT$STAM$OGGE_CTX ON GAT_SEDUTA_STAMPA (OGGETTO) 	INDEXTYPE IS CTXSYS.CTXCAT PARAMETERS(''lexer italian_lexer wordlist italian_wordlist stoplist italian_stoplist memory 10M'') NOPARALLEL';
exception when others then
    null;
end;
/

begin
    execute immediate 'CREATE INDEX GAT$STAM$NOTE_CTX ON GAT_SEDUTA_STAMPA (NOTE) 		INDEXTYPE IS CTXSYS.CTXCAT PARAMETERS(''lexer italian_lexer wordlist italian_wordlist stoplist italian_stoplist memory 10M'') NOPARALLEL';
exception when others then
    null;
end;
/

DECLARE
   d_id_funzione           NUMBER;
   d_id_modello           NUMBER;
   d_id_abilitazione    number;
   d_codice_funzione     varchar2(4000);
   d_nome_funzione      varchar2(500);
   d_id_tipo_oggetto    NUMBER;
   d_id_tipo_abilitazione NUMBER;
   d_tipo_abilitazione varchar(10);
BEGIN

   delete from si4_competenze where tipo_competenza = 'F' and oggetto in (select m.id_tipodoc from modelli m where m.codice_modello in ('CERTIFICATO', 'ALLEGATO', 'DELIBERA', 'DETERMINA', 'VISTO', 'PROPOSTA_DELIBERA', 'SEDUTA_STAMPA') AND m.area = 'SEGRETERIA.ATTI.2_0');

   FOR c
      IN (SELECT 'DA' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_FILE_VISTO'       as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_visto_par('':Utente'', '':OGGETTO'', 1, 1, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'VISTO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'UA' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_FILE_VISTO'       as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_visto_par('':Utente'', '':OGGETTO'', 1, 1, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'VISTO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'LA' tipo_abilitazione, id_tipodoc, 'AGSDE_LETTURA_FILE_VISTO'        as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_visto_par('':Utente'', '':OGGETTO'', 1, 0, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'VISTO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'DA' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_FILE_DETERMINA'   as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_determina('':Utente'', '':OGGETTO'', 1, 1, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'DETERMINA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'UA' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_FILE_DETERMINA'   as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_determina('':Utente'', '':OGGETTO'', 1, 1, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'DETERMINA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'LA' tipo_abilitazione, id_tipodoc, 'AGSDE_LETTURA_FILE_DETERMINA'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_determina('':Utente'', '':OGGETTO'', 1, 0, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'DETERMINA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'DA' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_FILE_SED_STAMPA'  as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_documento('':Utente'', '':OGGETTO'', 1, 1, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'SEDUTA_STAMPA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'UA' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_FILE_SED_STAMPA'  as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_documento('':Utente'', '':OGGETTO'', 1, 1, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'SEDUTA_STAMPA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'LA' tipo_abilitazione, id_tipodoc, 'AGSDE_LETTURA_FILE_SED_STAMPA'   as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_documento('':Utente'', '':OGGETTO'', 1, 0, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'SEDUTA_STAMPA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'DA' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_FILE_DELIBERA'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_delibera ('':Utente'', '':OGGETTO'', 1, 1, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'DELIBERA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'UA' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_FILE_DELIBERA'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_delibera ('':Utente'', '':OGGETTO'', 1, 1, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'DELIBERA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'LA' tipo_abilitazione, id_tipodoc, 'AGSDE_LETTURA_FILE_DELIBERA'     as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_delibera ('':Utente'', '':OGGETTO'', 1, 0, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'DELIBERA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'DA' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_FILE_PROPDELI'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_prop_del ('':Utente'', '':OGGETTO'', 1, 1, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'PROPOSTA_DELIBERA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'UA' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_FILE_PROPDELI'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_prop_del ('':Utente'', '':OGGETTO'', 1, 1, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'PROPOSTA_DELIBERA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'LA' tipo_abilitazione, id_tipodoc, 'AGSDE_LETTURA_FILE_PROPDELI'     as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_prop_del ('':Utente'', '':OGGETTO'', 1, 0, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'PROPOSTA_DELIBERA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'DA' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_FILE_CERT'        as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_cert     ('':Utente'', '':OGGETTO'', 1, 1, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'CERTIFICATO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'UA' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_FILE_CERT'        as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_cert     ('':Utente'', '':OGGETTO'', 1, 1, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'CERTIFICATO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'LA' tipo_abilitazione, id_tipodoc, 'AGSDE_LETTURA_FILE_CERT'         as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_cert     ('':Utente'', '':OGGETTO'', 1, 0, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'CERTIFICATO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'DA' tipo_abilitazione, id_tipodoc, 'AGSDE_CANCELLA_FILE_ALLEGATO'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_allegato ('':Utente'', '':OGGETTO'', 1, 1, 1, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'ALLEGATO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'UA' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_FILE_ALLEGATO'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_allegato ('':Utente'', '':OGGETTO'', 1, 1, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'ALLEGATO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'LA' tipo_abilitazione, id_tipodoc, 'AGSDE_LETTURA_FILE_ALLEGATO'     as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_allegato ('':Utente'', '':OGGETTO'', 1, 0, 0, 1);' as codice_funzione FROM modelli WHERE codice_modello = 'ALLEGATO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'U' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_VISTO'       as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_visto_par      ('':Utente'', '':OGGETTO'', 1, 1, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'VISTO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'C' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_VISTO'       as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_visto_par      ('':Utente'', '':OGGETTO'', 1, 1, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'VISTO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'L' tipo_abilitazione, id_tipodoc, 'AGSDE_LETTURA_VISTO'        as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_visto_par      ('':Utente'', '':OGGETTO'', 1, 0, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'VISTO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'U' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_DETERMINA'   as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_determina      ('':Utente'', '':OGGETTO'', 1, 1, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'DETERMINA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'C' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_DETERMINA'   as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_determina      ('':Utente'', '':OGGETTO'', 1, 1, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'DETERMINA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'L' tipo_abilitazione, id_tipodoc, 'AGSDE_LETTURA_DETERMINA'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_determina      ('':Utente'', '':OGGETTO'', 1, 0, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'DETERMINA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'U' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_SEDUTA_STAMPA'   as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_documento      ('':Utente'', '':OGGETTO'', 1, 1, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'SEDUTA_STAMPA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'C' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_SEDUTA_STAMPA'   as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_documento      ('':Utente'', '':OGGETTO'', 1, 1, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'SEDUTA_STAMPA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'L' tipo_abilitazione, id_tipodoc, 'AGSDE_LETTURA_SEDUTA_STAMPA'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_documento      ('':Utente'', '':OGGETTO'', 1, 0, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'SEDUTA_STAMPA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'U' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_DELIBERA'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_delibera       ('':Utente'', '':OGGETTO'', 1, 1, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'DELIBERA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'C' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_DELIBERA'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_delibera       ('':Utente'', '':OGGETTO'', 1, 1, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'DELIBERA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'L' tipo_abilitazione, id_tipodoc, 'AGSDE_LETTURA_DELIBERA'     as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_delibera       ('':Utente'', '':OGGETTO'', 1, 0, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'DELIBERA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'U' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_PROPDELI'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_prop_del       ('':Utente'', '':OGGETTO'', 1, 1, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'PROPOSTA_DELIBERA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'C' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_PROPDELI'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_prop_del       ('':Utente'', '':OGGETTO'', 1, 1, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'PROPOSTA_DELIBERA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'L' tipo_abilitazione, id_tipodoc, 'AGSDE_LETTURA_PROPDELI'     as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_prop_del       ('':Utente'', '':OGGETTO'', 1, 0, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'PROPOSTA_DELIBERA' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'U' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_CERT'        as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_cert           ('':Utente'', '':OGGETTO'', 1, 1, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'CERTIFICATO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'C' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_CERT'        as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_cert           ('':Utente'', '':OGGETTO'', 1, 1, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'CERTIFICATO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'L' tipo_abilitazione, id_tipodoc, 'AGSDE_LETTURA_CERT'         as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_cert           ('':Utente'', '':OGGETTO'', 1, 0, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'CERTIFICATO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'U' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_ALLEGATO'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_allegato       ('':Utente'', '':OGGETTO'', 1, 1, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'ALLEGATO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'C' tipo_abilitazione, id_tipodoc, 'AGSDE_MODIFICA_ALLEGATO'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_allegato       ('':Utente'', '':OGGETTO'', 1, 1, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'ALLEGATO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'L' tipo_abilitazione, id_tipodoc, 'AGSDE_LETTURA_ALLEGATO'     as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_allegato       ('':Utente'', '':OGGETTO'', 1, 0, 0, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'ALLEGATO' AND area = 'SEGRETERIA.ATTI.2_0'
    union SELECT 'D' tipo_abilitazione, id_tipodoc, 'AGSDE_CANCELLA_ALLEGATO'    as nome_funzione, ':=AGSDE2_INTEGRAZIONE_GDM_PKG.controlla_competenze_allegato       ('':Utente'', '':OGGETTO'', 1, 1, 1, 0);' as codice_funzione FROM modelli WHERE codice_modello = 'ALLEGATO' AND area = 'SEGRETERIA.ATTI.2_0'
    )
   LOOP
        d_id_modello        := c.id_tipodoc;
        d_nome_funzione     := c.nome_funzione;
        d_codice_funzione   := c.codice_funzione;
        d_tipo_abilitazione := c.tipo_abilitazione;

        SELECT t.id_tipo_oggetto, ta.id_tipo_abilitazione, a.id_abilitazione
         INTO d_id_tipo_oggetto, d_id_tipo_abilitazione, d_id_abilitazione
         FROM si4_tipi_oggetto t,
              si4_tipi_abilitazione ta,
              si4_abilitazioni a
        WHERE t.tipo_oggetto = 'TIPI_DOCUMENTO'
          AND a.id_tipo_abilitazione = ta.id_tipo_abilitazione
          and t.id_tipo_oggetto = a.id_tipo_oggetto
          and ta.tipo_abilitazione = d_tipo_abilitazione;

      BEGIN
         SELECT id_funzione
           INTO d_id_funzione
           FROM SI4_FUNZIONI
          WHERE NOME = d_nome_funzione;

        update si4_funzioni set testo = d_codice_funzione where id_funzione = d_id_funzione;

      EXCEPTION
         WHEN NO_DATA_FOUND
         THEN
            SELECT FUNZ_SQ.NEXTVAL INTO d_id_funzione FROM DUAL;

            INSERT INTO SI4_FUNZIONI (ID_FUNZIONE, NOME, TESTO)
                 VALUES (d_id_funzione, d_nome_funzione, d_codice_funzione);
      END;

      INSERT INTO SI4_COMPETENZE (ID_ABILITAZIONE,
                                  OGGETTO,
                                  ACCESSO,
                                  RUOLO,
                                  DAL,
                                  DATA_AGGIORNAMENTO,
                                  UTENTE_AGGIORNAMENTO,
                                  ID_FUNZIONE,
                                  TIPO_COMPETENZA)
         SELECT d_id_abilitazione,
                TO_CHAR (d_id_modello),
                'S',
                'GDM',
                SYSDATE-10,
                SYSDATE,
                'GDM',
                d_id_funzione,
                'F'
           FROM DUAL
          WHERE NOT EXISTS
                       (SELECT 1
                          FROM si4_competenze
                         WHERE     ID_ABILITAZIONE = d_id_abilitazione
                               AND OGGETTO = TO_CHAR (d_id_modello)
                               AND ID_FUNZIONE = d_id_funzione);
   END LOOP;

   COMMIT;
END;
/

update tipi_documento set competenze_allegati = 'S' where area_modello = 'SEGRETERIA.ATTI.2_0' and acronimo_modello is not null
/