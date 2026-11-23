--liquibase formatted sql
--changeset svalenti:2.5_20230520_01.06.regole_agid_trust runOnChange:true failOnError:false

DECLARE
    d_esiste          		NUMBER;
    d_esiste_old          	NUMBER;
	d_esiste_conservazione  NUMBER;
    D_CONSERVATORE    		VARCHAR2(200);
BEGIN

BEGIN
    SELECT COUNT(*) INTO d_esiste_conservazione
    FROM all_objects
    WHERE object_type in ('TABLE')
      AND object_name IN ('CONSERVAZIONI','CONSERVAZIONI_PARAMETRI')
  AND upper(owner) = upper('${global.db.gdm.username}');
EXCEPTION
      WHEN NO_DATA_FOUND
      THEN
         d_esiste_conservazione := 0;
END;

	-- esiste la conservazione installata
	IF d_esiste_conservazione = 2 THEN

	-- gestione delibera
BEGIN
    SELECT 1
    INTO d_esiste
    FROM CONSERVAZIONI
    WHERE AREA = 'SEGRETERIA.ATTI.2_0'
      AND CODICE_MODELLO = 'DELIBERA'
      AND CONSERVAZIONE = 'TRUST';
EXCEPTION
      WHEN NO_DATA_FOUND
      THEN
         d_esiste := 0;
END;

BEGIN
    SELECT 1
    INTO d_esiste_old
    FROM CONSERVAZIONI
    WHERE AREA = 'SEGRETERIA.ATTI.2_0'
      AND CODICE_MODELLO = 'DELIBERA'
      AND CONSERVAZIONE = 'TRUST_OLD';
EXCEPTION
          WHEN NO_DATA_FOUND
          THEN
             d_esiste_old := 0;
END;

    IF d_esiste = 0
    THEN
        D_CONSERVATORE := 'TRUST';
    ELSIF d_esiste_old = 0
    THEN
        D_CONSERVATORE := 'TRUST_2022';
ELSE
        D_CONSERVATORE := 'TRUST';
END IF;

	-- cancello se presente TRUST_2022
	DELETE CONSERVAZIONI
    WHERE AREA = 'SEGRETERIA.ATTI.2_0'
    AND CODICE_MODELLO = 'DELIBERA'
    AND CONSERVAZIONE = 'TRUST_2022';

	DELETE CONSERVAZIONI_PARAMETRI
	WHERE AREA = 'SEGRETERIA.ATTI.2_0'
    AND CODICE_MODELLO = 'DELIBERA'
    AND CONSERVAZIONE = 'TRUST_2022';

    -- cancello se già è stato fatto lo switch per aggiornare le regole
    IF D_CONSERVATORE = 'TRUST' THEN
        DELETE CONSERVAZIONI
        WHERE AREA = 'SEGRETERIA.ATTI.2_0'
        AND CODICE_MODELLO = 'DELIBERA'
        AND CONSERVAZIONE = 'TRUST';

        DELETE CONSERVAZIONI_PARAMETRI
        WHERE AREA = 'SEGRETERIA.ATTI.2_0'
        AND CODICE_MODELLO = 'DELIBERA'
        AND CONSERVAZIONE = 'TRUST';
END IF;

INSERT INTO CONSERVAZIONI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO_DOCUMENTO, DESCRIZIONE)
VALUES
('SEGRETERIA.ATTI.2_0','DELIBERA',D_CONSERVATORE,'Delibera','Delibera :NUMERO_DELIBERA/:ANNO_DELIBERA :ID_REGISTRO_DELIBERA');

INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
VALUES
('SEGRETERIA.ATTI.2_0',
 'DELIBERA',
 D_CONSERVATORE,
 'Allegati',
 '1',
 'SELECT');

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = 'select id_documento_esterno id_documento
         ,  filename
         ,  filename chiave_allegato
         ,  ''DocumentoGenerico'' tipo_struttura
         ,  titolo tipo_documento
         ,  ''false'' utilizzodatafirmaperriftemp
         ,  to_char (data_firma, ''yyyy-mm-dd'') || ''T'' || to_char (data_firma, ''hh24::mi::ss'') riferimento_temporale
         ,  decode (data_firma, null, '''', ''Apposizione della firma'') desc_riferimento_temporale
         ,  2 ordinamento
         , id_documento_esterno || ''_'' || filename filenameall
      from agsde2_cons_delibere_allegati alle
     where alle.id_delibera = '':ID_DOCUMENTO_GRAILS''
       and tipo_oggetto = ''PARERE''
    union
    select id_documento_esterno id_documento
         ,  filename
         ,  filename chiave_allegato
         ,  ''DocumentoGenerico'' tipo_struttura
         ,  ''GENERICO'' tipo_documento
         ,  ''false'' utilizzodatafirmaperriftemp
         ,  ''T'' riferimento_temporale
         ,  '''' desc_riferimento_temporale
         ,  1 ordinamento
         , id_documento_esterno || ''_'' || filename filenameall
      from agsde2_cons_delibere_allegati alle
     where alle.id_delibera = '':ID_DOCUMENTO_GRAILS''
       and tipo_oggetto = ''ALLEGATO''
    union
    select id_documento_esterno id_documento
         ,  filename
         ,  filename chiave_allegato
         ,  ''DocumentoGenerico'' tipo_struttura
         ,  decode (titolo,  ''PUBBLICAZIONE'', ''RELAZIONE DI PUBBLICAZIONE'',  ''ESECUTIVITA'', ''CERTIFICATO DI ESECUTIVITA'',  ''AVVENUTA_PUBBLICAZIONE'', ''ATTESTAZIONE DI PUBBLICAZIONE'')
               tipo_documento
         ,  ''false'' utilizzodatafirmaperriftemp
         ,  to_char (data_firma, ''yyyy-mm-dd'') || ''T'' || to_char (data_firma, ''hh24::mi::ss'') riferimento_temporale
         ,  decode (data_firma, null, '''', ''Apposizione della firma'') desc_riferimento_temporale
         ,  decode (titolo,  ''PUBBLICAZIONE'', 3,  ''ESECUTIVITA'', 4,  ''AVVENUTA_PUBBLICAZIONE'', 5) ordinamento
         , id_documento_esterno || ''_'' || filename filenameall
      from agsde2_cons_delibere_allegati alle
     where alle.id_delibera = '':ID_DOCUMENTO_GRAILS''
       and tipo_oggetto = ''CERTIFICATO''
    order by ordinamento'
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DELIBERA'
  AND TIPO = 'Allegati'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
VALUES
('SEGRETERIA.ATTI.2_0',
 'DELIBERA',
 D_CONSERVATORE,
 'DescrizioneArchivistica',
 '1',
 'DESCRIZIONE_ARCHIVISTICA');

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = 'AttoCollegiale2022'
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DELIBERA'
  AND TIPO = 'DescrizioneArchivistica'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
VALUES
('SEGRETERIA.ATTI.2_0',
 'DELIBERA',
 D_CONSERVATORE,
 'DocumentoPrincipale',
 '1',
 'SELECT');

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = VALORE||'SELECT distinct id_documento_esterno id_documento,
    ''NomeDelDocumento'',filename,0,
    ''Impronta'',''#CODICE_HASH#'',1,
    ''Algoritmo'',''SHA-256'',2,
    ''Identificativo'',id_delibera,3,
    ''Segnatura'',case
               when  length(numero_protocollo)>0 and length(data_numero_protocollo)>0 then
                   decode(numero_protocollo,'''',NULL, NULL,NULL,
         ''Progressivo di protocollo: '' || numero_protocollo || '' data: '' || to_char(data_numero_protocollo,''dd/MM/yyyy'') ||
        (select '' IPA: '' ||  to_char(valore) from conservazioni_parametri where conservazione=''TRUST'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AMM'')||
        (select '' Codice AOO: '' ||  to_char(valore) from conservazioni_parametri where conservazione=''TRUST'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AOO'')||
        (select '' Descrizione AOO: '' ||  to_char(valore) from conservazioni_parametri where conservazione=''TRUST'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AOO'')
        )
          else
          ''NON APPLICABILE''
         end,4,
    ''ModalitaDiFormazione'',''A'',5,
    ''TipologiaDocumentale'',''ATTO COLLEGIALE'',6,
    ''TipologiaDiFlusso'',''I'',7,
    ''TipoRegistro'',''REPERTORIO'',8,
    ''DataDocumento'',to_char (data_numero_delibera, ''dd/MM/yyyy''),9,
    ''DataRegistrazione'',to_char (data_numero_delibera, ''dd/MM/yyyy hh24::mi::ss''),10,
    ''NumeroDocumento'',numero_delibera,11,
    ''CodiceRegistro'',registro_delibera,12,
    ''RuoloSoggetto1'',''Amministrazione che effettua la registrazione'',13,
    ''TipoSoggetto1'',''PAI '' ||
    (select to_char(valore) from conservazioni_parametri where conservazione=''TRUST'' and tipo = ''Parametri'' and chiave = ''DENOMINAZIONE_AMM'') || '' '' ||
    (select to_char(valore) from conservazioni_parametri where conservazione=''TRUST'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AMM'')  || '' '' ||
    (select to_char(valore) from conservazioni_parametri where conservazione=''TRUST'' and tipo = ''Parametri'' and chiave = ''INDIRIZZO_ISTITUZIONALE_AMM'') ,14,
    ''RuoloSoggetto2'',''Autore'',15,
    ''TipoSoggetto2'', ''PF '' || nvl(cognome_segretario,''NON APPLICABILE'') || '' '' || nvl(nome_segretario,''NON APPLICABILE'')  ,16,
    ''RuoloSoggetto3'',''Produttore'',17,
    ''TipoSoggetto3'', ''NON APPLICABILE''  ,18,
    ''RuoloSoggetto4'',''Destinatario'',19,
    ''TipoSoggetto4'', ''NON APPLICABILE''  ,20,
    ''RuoloSoggetto5'',''Operatore'',21,
    ''TipoSoggetto5'', ''NON APPLICABILE''  ,22,
    ''RuoloSoggetto6'',''Assegnatario'',23,
    ''TipoSoggetto6'', ((select ''AS '' || to_char(valore) from conservazioni_parametri where conservazione=''TRUST'' and tipo = ''Parametri'' and chiave = ''DENOMINAZIONE_ENTE'') || '' - '' || unita_proponente ) ,24,
    ''RuoloSoggetto7'',''RUP'',25,
    ''TipoSoggetto7'', ''RUP '' || nvl(cognome_funzionario,''NON APPLICABILE'') || '' '' || nvl(nome_funzionario,''NON APPLICABILE'') ,26,
    ''Oggetto'',oggetto,27,
    ''NumeroAllegati'',''#NUMERO_ALLEGATI#'',28,
    ''IndiceAllegati'',''#INDICE_ALLEGATI#'',29,
    ''IndiceDiClassificazione'',nvl(classifica_codice,''NON APPLICABILE''),30,
    ''Descrizione'',nvl(classifica_descrizione,''NON APPLICABILE''),31,
    ''PianoDiClassificazione'',''NON APPLICABILE'',32,
    ''Riservato'',''FA'
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DELIBERA'
  AND TIPO = 'DocumentoPrincipale'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = VALORE||'LSO'',33,
    ''Formato'',''PDF'',34,
    ''ProdottoSoftware'','''',35,
    ''FirmatoDigitalmente'',''VERO'',36,
    ''SigillatoElettronicamente'',''FALSO'',37,
    ''MarcaturaTemporale'',''FALSO'',38,
    ''ConfCopieImgSuSuppInformatico'',''FALSO'',39,
    ''Agg'', decode(fascicolo_numero,'''',''Serie Documentale'',''Fascicolo'') || '' '' || decode(fascicolo_numero,'''',''ATTO COLLEGGIALE '' || to_char(data_numero_delibera, ''YYYY'') ,fascicolo_numero || ''-'' || fascicolo_anno || '' '' || fascicolo_oggetto) ,40,
    ''IdDocumentoPrimario'',id_file_gdm,41,
    ''VersioneDelDocumento'',''1'',42,
    ''TipoModifica'','''',43,
    ''SoggettoAutoreDellaModifica'','''',44,
    ''DataModifica'','''',45,
    ''IdDocVersionePrecedente'','''',46,
    ''TempoDiConservazione'',''9999'',47,
    ''Note'',''-'',48,
    ''DataEsecutività'',to_char (data_esecutivita, ''dd/MM/yyyy hh24::mi::ss''),49
     from agsde2_cons_delibere
     where id_delibera = '':ID_DOCUMENTO_GRAILS'' '
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DELIBERA'
  AND TIPO = 'DocumentoPrincipale'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

-- gestione determina
BEGIN
SELECT 1
INTO d_esiste
FROM CONSERVAZIONI
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DETERMINA'
  AND CONSERVAZIONE = 'TRUST';
EXCEPTION
		  WHEN NO_DATA_FOUND
		  THEN
			 d_esiste := 0;
END;

BEGIN
SELECT 1
INTO d_esiste_old
FROM CONSERVAZIONI
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DETERMINA'
  AND CONSERVAZIONE = 'TRUST_OLD';
EXCEPTION
              WHEN NO_DATA_FOUND
              THEN
                 d_esiste_old := 0;
END;

		IF d_esiste = 0
        THEN
            D_CONSERVATORE := 'TRUST';
        ELSIF d_esiste_old = 0
        THEN
            D_CONSERVATORE := 'TRUST_2022';
ELSE
            D_CONSERVATORE := 'TRUST';
END IF;

		-- cancello se presente TRUST_2022
		DELETE CONSERVAZIONI
		WHERE AREA = 'SEGRETERIA.ATTI.2_0'
		AND CODICE_MODELLO = 'DETERMINA'
		AND CONSERVAZIONE = 'TRUST_2022';

		DELETE CONSERVAZIONI_PARAMETRI
		WHERE AREA = 'SEGRETERIA.ATTI.2_0'
		AND CODICE_MODELLO = 'DETERMINA'
		AND CONSERVAZIONE = 'TRUST_2022';

        -- cancello se già è stato fatto lo switch per aggiornare le regole
        IF D_CONSERVATORE = 'TRUST' THEN
            DELETE CONSERVAZIONI
            WHERE AREA = 'SEGRETERIA.ATTI.2_0'
            AND CODICE_MODELLO = 'DETERMINA'
            AND CONSERVAZIONE = 'TRUST';

            DELETE CONSERVAZIONI_PARAMETRI
            WHERE AREA = 'SEGRETERIA.ATTI.2_0'
            AND CODICE_MODELLO = 'DETERMINA'
            AND CONSERVAZIONE = 'TRUST';
END IF;

INSERT INTO CONSERVAZIONI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO_DOCUMENTO, DESCRIZIONE)
VALUES
('SEGRETERIA.ATTI.2_0','DETERMINA',D_CONSERVATORE,'Determina','Determina :NUMERO_DETERMINA/:ANNO_DETERMINA :ID_REGISTRO_DETERMINA');

INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
VALUES
('SEGRETERIA.ATTI.2_0',
 'DETERMINA',
 D_CONSERVATORE,
 'Allegati',
 '1',
 'SELECT');

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = 'select id_documento_esterno id_documento
             ,  filename
             ,  filename chiave_allegato
             ,  ''DocumentoGenerico'' tipo_struttura
             ,  titolo tipo_documento
             ,  ''false'' utilizzodatafirmaperriftemp
             ,  to_char (data_firma, ''yyyy-mm-dd'') || ''T'' || to_char (data_firma, ''hh24::mi::ss'') riferimento_temporale
             ,  decode (data_firma, null, '''', ''Apposizione della firma'') desc_riferimento_temporale
             ,  2 ordinamento
             , id_documento_esterno || ''_'' || filename filenameall
          from agsde2_cons_determine_allegati alle
         where alle.id_determina = '':ID_DOCUMENTO_GRAILS''
           and tipo_oggetto = ''VISTO'' union select id_documento_esterno id_documento
             ,  filename
             ,  filename chiave_allegato
             ,  ''DocumentoGenerico'' tipo_struttura
             ,  ''GENERICO'' tipo_documento
             ,  ''false'' utilizzodatafirmaperriftemp
             ,  ''T'' riferimento_temporale
             ,  '''' desc_riferimento_temporale
             ,  1 ordinamento
             , id_documento_esterno || ''_'' || filename filenameall
               from agsde2_cons_determine_allegati alle
         where alle.id_determina = '':ID_DOCUMENTO_GRAILS''
           and tipo_oggetto = ''ALLEGATO'' union select id_documento_esterno id_documento
             ,  filename
             ,  filename chiave_allegato
             ,  ''DocumentoGenerico'' tipo_struttura
             ,  decode (titolo,  ''PUBBLICAZIONE'', ''RELAZIONE DI PUBBLICAZIONE'',  ''ESECUTIVITA'', ''CERTIFICATO DI ESECUTIVITA'',  ''AVVENUTA_PUBBLICAZIONE'', ''ATTESTAZIONE DI PUBBLICAZIONE'')
                   tipo_documento
             ,  ''false'' utilizzodatafirmaperriftemp
             ,  to_char (data_firma, ''yyyy-mm-dd'') || ''T'' || to_char (data_firma, ''hh24::mi::ss'') riferimento_temporale
             ,  decode (data_firma, null, '''', ''Apposizione della firma'') desc_riferimento_temporale
             ,  decode (titolo,  ''PUBBLICAZIONE'', 3,  ''ESECUTIVITA'', 4,  ''AVVENUTA_PUBBLICAZIONE'', 5) ordinamento
             , id_documento_esterno || ''_'' || filename filenameall
          from agsde2_cons_determine_allegati alle
         where alle.id_determina = '':ID_DOCUMENTO_GRAILS''
           and tipo_oggetto = ''CERTIFICATO'' order by ordinamento'
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DETERMINA'
  AND TIPO = 'Allegati'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
VALUES
('SEGRETERIA.ATTI.2_0',
 'DETERMINA',
 D_CONSERVATORE,
 'DescrizioneArchivistica',
 '1',
 'DESCRIZIONE_ARCHIVISTICA');

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = 'AttoMonocratico2022'
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DETERMINA'
  AND TIPO = 'DescrizioneArchivistica'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
VALUES
('SEGRETERIA.ATTI.2_0',
 'DETERMINA',
 D_CONSERVATORE,
 'DocumentoPrincipale',
 '1',
 'SELECT');

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = VALORE||'SELECT distinct id_documento_esterno id_documento,
        ''NomeDelDocumento'',filename,0,
        ''Impronta'',''#CODICE_HASH#'',1,
        ''Algoritmo'',''SHA-256'',2,
        ''Identificativo'',id_determina,3,
        ''Segnatura'',case
                   when  length(numero_protocollo)>0 and length(data_numero_protocollo)>0 then
                       decode(numero_protocollo,'''',NULL, NULL,NULL,
             ''Progressivo di protocollo: '' || numero_protocollo || '' data: '' || to_char(data_numero_protocollo,''dd/MM/yyyy'') ||
            (select '' IPA: '' ||  to_char(valore) from conservazioni_parametri where conservazione=''TRUST'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AMM'')||
            (select '' Codice AOO: '' ||  to_char(valore) from conservazioni_parametri where conservazione=''TRUST'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AOO'')||
            (select '' Descrizione AOO: '' ||  to_char(valore) from conservazioni_parametri where conservazione=''TRUST'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AOO'')
            )
              else
              ''NON APPLICABILE''
             end,4,
        ''ModalitaDiFormazione'',''A'',5,
        ''TipologiaDocumentale'',''ATTO MONOCRATICO'',6,
        ''TipologiaDiFlusso'',''I'',7,
        ''TipoRegistro'',''REPERTORIO'',8,
        ''DataDocumento'',to_char (data_numero_determina, ''dd/MM/yyyy''),9,
        ''DataRegistrazione'',to_char (data_numero_determina, ''dd/MM/yyyy hh24::mi::ss''),10,
        ''NumeroDocumento'',numero_determina,11,
        ''CodiceRegistro'',registro_determina,12,
        ''RuoloSoggetto1'',''Amministrazione che effettua la registrazione'',13,
        ''TipoSoggetto1'',''PAI '' ||
        (select to_char(valore) from conservazioni_parametri where conservazione=''TRUST'' and tipo = ''Parametri'' and chiave = ''DENOMINAZIONE_AMM'') || '' '' ||
        (select to_char(valore) from conservazioni_parametri where conservazione=''TRUST'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AMM'')  || '' '' ||
        (select to_char(valore) from conservazioni_parametri where conservazione=''TRUST'' and tipo = ''Parametri'' and chiave = ''INDIRIZZO_ISTITUZIONALE_AMM'') ,14,
        ''RuoloSoggetto2'',''Autore'',15,
        ''TipoSoggetto2'', ''PF '' || nvl(cognome_firmatario,''NON APPLICABILE'') || '' '' || nvl(nome_firmatario,''NON APPLICABILE'')  ,16,
        ''RuoloSoggetto3'',''Produttore'',17,
        ''TipoSoggetto3'', ''NON APPLICABILE''  ,18,
        ''RuoloSoggetto4'',''Destinatario'',19,
        ''TipoSoggetto4'', ''NON APPLICABILE''  ,20,
        ''RuoloSoggetto5'',''Operatore'',21,
        ''TipoSoggetto5'', ''NON APPLICABILE''  ,22,
        ''RuoloSoggetto6'',''Assegnatario'',23,
        ''TipoSoggetto6'', ((select ''AS '' || to_char(valore) from conservazioni_parametri where conservazione=''TRUST'' and tipo = ''Parametri'' and chiave = ''DENOMINAZIONE_ENTE'') || '' - '' || unita_proponente ) ,24,
        ''RuoloSoggetto7'',''RUP'',25,
        ''TipoSoggetto7'', ''RUP '' || nvl(cognome_funzionario,''NON APPLICABILE'') || '' '' || nvl(nome_funzionario,''NON APPLICABILE'') ,26,
        ''Oggetto'',oggetto,27,
        ''NumeroAllegati'',''#NUMERO_ALLEGATI#'',28,
        ''IndiceAllegati'',''#INDICE_ALLEGATI#'',29,
        ''IndiceDiClassificazione'',nvl(classifica_codice,''NON APPLICABILE''),30,
        ''Descrizione'',nvl(classifica_descrizione,''NON APPLICABILE''),31,
        ''PianoDiClassificazione'',''NON APPLICABILE'',32,
        ''Riservat'
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DETERMINA'
  AND TIPO = 'DocumentoPrincipale'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = VALORE||'o'',decode(nvl(riservato,''N''),''N'',''FALSO'',''VERO''),33,
        ''Formato'',''PDF'',34,
        ''ProdottoSoftware'','''',35,
        ''FirmatoDigitalmente'',''VERO'',36,
        ''SigillatoElettronicamente'',''FALSO'',37,
        ''MarcaturaTemporale'',''FALSO'',38,
        ''ConfCopieImgSuSuppInformatico'',''FALSO'',39,
        ''Agg'', decode(fascicolo_numero,'''',''Serie Documentale'',''Fascicolo'') || '' '' || decode(fascicolo_numero,'''',''ATTO MONOCRATICO '' || to_char(data_numero_determina, ''YYYY'') ,fascicolo_numero || ''-'' || fascicolo_anno || '' '' || fascicolo_oggetto) ,40,
        ''IdDocumentoPrimario'',id_file_gdm,41,
        ''VersioneDelDocumento'',''1'',42,
        ''TipoModifica'','''',43,
        ''SoggettoAutoreDellaModifica'','''',44,
        ''DataModifica'','''',45,
        ''IdDocVersionePrecedente'','''',46,
        ''TempoDiConservazione'',''9999'',47,
        ''Note'',''-'',47,
        ''DataEsecutività'',to_char (data_esecutivita, ''dd/MM/yyyy hh24::mi::ss''),49,
        DECODE((select to_char(valore)
        from conservazioni_parametri
        where conservazione=''TRUST''
        and tipo = ''Parametri''
        and chiave = ''ALIAS_SP''),''ESU-PADOVA'',  decode(REGISTRO_DETERMINA, ''LIQU'',''CIG'',''***''),''***'') cig,
        decode(REGISTRO_DETERMINA, ''LIQU'',cig,''***''),
        decode(REGISTRO_DETERMINA, ''LIQU'',50,0)
         from agsde2_cons_determine
         where id_determina = '':ID_DOCUMENTO_GRAILS'' '
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DETERMINA'
  AND TIPO = 'DocumentoPrincipale'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;


-- gestione liquidazioni per esu padova
BEGIN
SELECT 1
INTO d_esiste
FROM CONSERVAZIONI_PARAMETRI
WHERE AREA = 'ESU-PADOVA'
  AND CODICE_MODELLO = 'LIQU'
  AND CONSERVAZIONE = 'TRUST';
EXCEPTION
              WHEN NO_DATA_FOUND
              THEN
                 d_esiste := 0;
END;

BEGIN
SELECT 1
INTO d_esiste_old
FROM CONSERVAZIONI_PARAMETRI
WHERE AREA = 'ESU-PADOVA'
  AND CODICE_MODELLO = 'LIQU'
  AND CONSERVAZIONE = 'TRUST_OLD';
EXCEPTION
                  WHEN NO_DATA_FOUND
                  THEN
                     d_esiste_old := 0;
END;

    IF d_esiste = 0
        THEN
            D_CONSERVATORE := 'TRUST';
        ELSIF d_esiste_old = 0
        THEN
            D_CONSERVATORE := 'TRUST_2022';
ELSE
            D_CONSERVATORE := 'TRUST';
END IF;

	-- cancello se presente TRUST_2022
	DELETE CONSERVAZIONI_PARAMETRI
	WHERE AREA = 'ESU-PADOVA'
    AND CODICE_MODELLO = 'LIQU'
    AND CONSERVAZIONE = 'TRUST_2022';

    -- cancello se già è stato fatto lo switch per aggiornare le regole
    IF D_CONSERVATORE = 'TRUST' THEN
        DELETE CONSERVAZIONI
        WHERE AREA = 'ESU-PADOVA'
        AND CODICE_MODELLO = 'LIQU'
        AND CONSERVAZIONE = 'TRUST';

        DELETE CONSERVAZIONI_PARAMETRI
        WHERE AREA = 'ESU-PADOVA'
        AND CODICE_MODELLO = 'LIQU'
        AND CONSERVAZIONE = 'TRUST';
END IF;

INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
VALUES
('ESU-PADOVA',
 'LIQU',
 D_CONSERVATORE,
 'DescrizioneArchivistica',
 '1',
 'DESCRIZIONE_ARCHIVISTICA');

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = 'AttoDiLiquidazione2022'
WHERE AREA = 'ESU-PADOVA'
  AND CODICE_MODELLO = 'LIQU'
  AND TIPO = 'DescrizioneArchivistica'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

COMMIT;

END IF;

END;
/