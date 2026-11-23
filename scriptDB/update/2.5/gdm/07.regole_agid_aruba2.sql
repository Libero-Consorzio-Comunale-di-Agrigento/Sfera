--liquibase formatted sql
--changeset svalenti:2.5_20230520_01.07.regole_agid_aruba2 runOnChange:true failOnError:false

DECLARE
    d_esiste          		NUMBER;
    d_esiste_old          	NUMBER;
	d_esiste_conservazione  NUMBER;
    D_CONSERVATORE    		VARCHAR2(200);
    d_sequenza              NUMBER;
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
      AND CONSERVAZIONE = 'ARUBA2';
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
      AND CONSERVAZIONE = 'ARUBA2_OLD';
    EXCEPTION
              WHEN NO_DATA_FOUND
              THEN
                 d_esiste_old := 0;
    END;

        IF d_esiste = 0
        THEN
            D_CONSERVATORE := 'ARUBA2';
        ELSIF d_esiste_old = 0
        THEN
            D_CONSERVATORE := 'ARUBA2_2022';
    ELSE
            D_CONSERVATORE := 'ARUBA2';
    END IF;

        -- cancello se presente ARUBA2_2022
        DELETE CONSERVAZIONI
        WHERE AREA = 'SEGRETERIA.ATTI.2_0'
        AND CODICE_MODELLO = 'DELIBERA'
        AND CONSERVAZIONE = 'ARUBA2_2022';

        DELETE CONSERVAZIONI_PARAMETRI
        WHERE AREA = 'SEGRETERIA.ATTI.2_0'
        AND CODICE_MODELLO = 'DELIBERA'
        AND CONSERVAZIONE = 'ARUBA2_2022';

        -- cancello se già è stato fatto lo switch per aggiornare le regole
        IF D_CONSERVATORE = 'ARUBA2' THEN
            DELETE CONSERVAZIONI
            WHERE AREA = 'SEGRETERIA.ATTI.2_0'
            AND CODICE_MODELLO = 'DELIBERA'
            AND CONSERVAZIONE = 'ARUBA2';

            DELETE CONSERVAZIONI_PARAMETRI
            WHERE AREA = 'SEGRETERIA.ATTI.2_0'
            AND CODICE_MODELLO = 'DELIBERA'
            AND CONSERVAZIONE = 'ARUBA2';
    END IF;

    INSERT INTO CONSERVAZIONI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO_DOCUMENTO, DESCRIZIONE)
    VALUES
    ('SEGRETERIA.ATTI.2_0','DELIBERA',D_CONSERVATORE,'Delibera','Delibera :NUMERO_DELIBERA/:ANNO_DELIBERA :ID_REGISTRO_DELIBERA');

    INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
    VALUES
    ('SEGRETERIA.ATTI.2_0',
     'DELIBERA',
     D_CONSERVATORE,
     'Agid2022',
     '1',
     'SELECT');

    UPDATE CONSERVAZIONI_PARAMETRI SET
        VALORE = 'select

     ''oggetto'', oggetto

    , ''Modalita_formazione'',''creazione tramite l''''utilizzo di strumenti software che assicurino la produzione di documenti nei formati previsti nell''''Allegato 2 delle Linee Guida''
    , ''impronta'',''#CODICE_HASH#''
    , ''algoritmo'',''SHA-256''
    , ''identificativo'',id_delibera
    , ''segnatura'', case
               when  length(numero_protocollo)>0 and length(data_numero_protocollo)>0 then
                   decode(numero_protocollo,'''',NULL, NULL,NULL,
         ''Progressivo di protocollo: '' || numero_protocollo || '' data: '' || to_char(data_numero_protocollo,''dd/mm/yyyy'') ||
        (select '' IPA: '' ||  to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_ENTE'')||
       '' Codice AOO: '' || CODICE_AOO  ||
        '' Descrizione AOO: '' || DESCRIZIONE_AOO
        )
          else
          ''Non Applicabile''
         end
    , ''Tipologia_documentale'',''ATTO COLLEGGIALE''

    , ''tipologia_flusso'',''I''
    , ''tipo_registro'',''Repertorio/Registro''
    , ''data_registrazione'',to_char (to_date (nvl(data_numero_delibera,data_numero_proposta), ''dd/mm/YY hh24::mi::ss''),''YYYY-MM-DD"T"HH24::mi::ss"+02"'')
    , ''numero_documento'',NVL(NVL(numero_protocollo, numero_delibera),0)
    , ''Idregistro'',descrizione_registro_delibera

    , ''Indice_classificazione'',nvl(classifica_codice,''Non Applicabile'')
    , ''Descrizione'',nvl(classifica_descrizione,''Non Applicabile'')
    , ''Riservato'',decode(nvl(riservato,''N''),''N'',''false'',''true'')

    , ''Formato'',''PDF''

    , ''Firmato_digitalmente'',''#FIRMATO#''
    , ''Sigillato_elettronicamente'',''false''
    , ''Marcatura_temporale'',''false''
    , ''Conformita_copie_immagine'',''false''

    , ''IdDoc_primario'',id_delibera
    , ''nome_doc'',filename
    , ''versione_doc'',''1''

    , ''Tempo_conservazione'',''9999''
    , ''IdAgg'', decode(fascicolo_numero,'''',''Serie Documenatale'',''Fascicolo'') || '' '' || decode(fascicolo_numero,'''',''ATTO COLLEGGIALE '' || to_char(data_numero_delibera, ''YYYY'') ,fascicolo_numero || ''-'' || fascicolo_anno || '' '' || fascicolo_oggetto)

    ,''numero_allegati_ref'', (select count(1) from (select id_documento_esterno id_documento
                  from agsde2_cons_delibere_allegati alle
                 where alle.id_delibera = '':ID_DOCUMENTO_GRAILS''
                   and tipo_oggetto in (''PERERE'',''ALLEGATO'',''CERTIFICATO'')
               )) numero_allegati
    ,''id_allegati_ref'', ( SELECT XMLAGG(XMLELEMENT(E,id_documento_esterno||'','')).EXTRACT(''//text()'') "Result" FROM agsde2_cons_delibere_allegati  where tipo_oggetto in (''PERERE'',''ALLEGATO'',''CERTIFICATO'') and id_delibera = '':ID_DOCUMENTO_GRAILS'' ) || '' '' id_allegati
    ,''descrizione_allegati_ref'', (SELECT XMLAGG(XMLELEMENT(E,filename||'','')).EXTRACT(''//text()'') "Result" FROM agsde2_cons_delibere_allegati where tipo_oggetto in (''PERERE'',''ALLEGATO'',''CERTIFICATO'') and id_delibera = '':ID_DOCUMENTO_GRAILS'' ) || '' '' descrizione_allegati
      from agsde2_cons_delibere
     where id_delibera = '':ID_DOCUMENTO_GRAILS'''
    WHERE AREA = 'SEGRETERIA.ATTI.2_0'
      AND CODICE_MODELLO = 'DELIBERA'
      AND TIPO = 'Agid2022'
      AND SEQUENZA = '1'
      AND CONSERVAZIONE = D_CONSERVATORE;

    INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
    VALUES
    ('SEGRETERIA.ATTI.2_0',
     'DELIBERA',
     D_CONSERVATORE,
     'Allegati',
     '1',
     'SELECT');

    UPDATE CONSERVAZIONI_PARAMETRI SET
        VALORE = 'select id_documento_esterno id_documento ,id_documento_esterno ID_OGGETTO_FILE  ,  filename,filename filenameall
      from agsde2_cons_delibere_allegati alle
     where alle.id_delibera = '':ID_DOCUMENTO_GRAILS''
       and tipo_oggetto = ''VISTO''
    union
    select id_documento_esterno id_documento ,id_documento_esterno ID_OGGETTO_FILE  ,  filename,filename filenameall
      from agsde2_cons_delibere_allegati alle
     where alle.id_delibera = '':ID_DOCUMENTO_GRAILS''
       and tipo_oggetto = ''ALLEGATO''
    union
    select id_documento_esterno id_documento ,id_documento_esterno ID_OGGETTO_FILE ,  filename,filename filenameall
      from agsde2_cons_delibere_allegati alle
     where alle.id_delibera = '':ID_DOCUMENTO_GRAILS''
       and tipo_oggetto = ''CERTIFICATO''
    '
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
     'DocumentoPrincipale',
     '1',
     'SELECT');

    UPDATE CONSERVAZIONI_PARAMETRI SET
        VALORE = '  select id_documento_esterno id_documento  ,
         filename nome_documento,
          ''docFileName'',replace(filename,''/'',''-''),
            ''protocollo'',nvl(to_char(numero_protocollo),''ND''),
            ''anno_protocollo'',nvl(to_char(anno_protocollo),''ND''),
            ''oggetto'',oggetto,
    ''tipo_procedura'',tipo_delibera,
    ''area'',descrizione_area,
    ''data_documento'', to_char (to_date (data_numero_delibera, ''dd/mm/YY hh24::mi::ss''),''YYYY-MM-DD"T"HH24::mi::ss"+02"''),
    ''numero_documento'',numero_delibera,
    ''anno_documento'',anno_delibera,
    ''registro'',descrizione_registro_delibera,
    ''data_pubblicazione'',  to_char (to_date (data_pubblicazione, ''dd/mm/YY hh24::mi::ss''), ''YYYY-MM-DD"T"HH24::mi::ss"+02"''),
    ''Data_esecutiva'',to_char (to_date (data_esecutivita, ''DD/MM/YY hh24::mi::ss''),''YYYY-MM-DD"T"HH24::mi::ss"+02"''),
    ''Ufficio_proponente'',unita_proponente,
    ''Firmatario'',FIRMATARIO_PARERE_CONTABILE,
    ''Titolario'',classifica_descrizione,
    ''Pratica'',fascicolo_oggetto
      from agsde2_cons_delibere
    where id_delibera = '':ID_DOCUMENTO_GRAILS'''
    WHERE AREA = 'SEGRETERIA.ATTI.2_0'
      AND CODICE_MODELLO = 'DELIBERA'
      AND TIPO = 'DocumentoPrincipale'
      AND SEQUENZA = '1'
      AND CONSERVAZIONE = D_CONSERVATORE;

    INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
    VALUES
    ('SEGRETERIA.ATTI.2_0',
     'DELIBERA',
     D_CONSERVATORE,
     'Soggetti2022',
     '1',
     'SELECT');

    UPDATE CONSERVAZIONI_PARAMETRI SET
        VALORE = 'select
    ''Ruolo_assegnatario.tiposoggetto'',''AS''
    , ''Ruolo_assegnatario.indriferimento'',(select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''INDIRIZZO_DIGITALE_ENTE'')
    , ''Ruolo_assegnatario.denamministrazione'',(select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''DENOMINAZIONE_ENTE'')
    , ''Ruolo_assegnatario.denamministrazioneipa'',(select upper(to_char(nvl(valore,''-''))) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_ENTE'')
    , ''Ruolo_assegnatario.denamministrazioneaoo'', CODICE_AOO || '' '' || DESCRIZIONE_AOO
    , ''Ruolo_assegnatario.denamministrazioneuor'',UNITA_PROPONENTE

    , ''Ruolo_autore.tiposoggetto'',''PF''
    , ''Ruolo_autore.nome'', nvl(nome_segretario,''Non Applicabile'')
    , ''Ruolo_autore.cognome'',nvl(cognome_segretario,''Non Applicabile'')

    , ''Ruolo_aer.tiposoggetto'',''PAI''
    , ''Ruolo_aer.denamministrazione'',(select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''DENOMINAZIONE_ENTE'')
    , ''Ruolo_aer.denamministrazioneipa'',(select upper(to_char(nvl(valore,''-''))) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_ENTE'')
    , ''Ruolo_aer.denamministrazioneaoo'', (select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AOO'') || '' '' || (select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AOO'')
    , ''Ruolo_aer.indriferimento'',(select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''INDIRIZZO_DIGITALE_ENTE'')

    , ''Ruolo_rup.tiposoggetto'',''PF''
    , ''Ruolo_rup.nome'', nvl(nome_funzionario,''Non Applicabile'')
    , ''Ruolo_rup.cognome'',nvl(cognome_funzionario,''Non Applicabile'')
    , ''Ruolo_rup.indriferimento'',(select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''INDIRIZZO_DIGITALE_ENTE'')
    , ''Ruolo_rup.Denamministrazione'',(select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''DENOMINAZIONE_ENTE'')
    , ''Ruolo_rup.Denamministrazioneipa'',(select upper(to_char(nvl(valore,''-''))) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_ENTE'')
    , ''Ruolo_rup.Denamministrazioneaoo'', CODICE_AOO || '' '' || DESCRIZIONE_AOO
    , ''Ruolo_rup.Denamministrazioneuor'',UNITA_PROPONENTE
    , ''1'',''1''
      from agsde2_cons_delibere
    where id_delibera = '':ID_DOCUMENTO_GRAILS'''
    WHERE AREA = 'SEGRETERIA.ATTI.2_0'
      AND CODICE_MODELLO = 'DELIBERA'
      AND TIPO = 'Soggetti2022'
      AND SEQUENZA = '1'
      AND CONSERVAZIONE = D_CONSERVATORE;


        -- gestione determina
        BEGIN
        SELECT 1
        INTO d_esiste
        FROM CONSERVAZIONI
        WHERE AREA = 'SEGRETERIA.ATTI.2_0'
          AND CODICE_MODELLO = 'DETERMINA'
          AND CONSERVAZIONE = 'ARUBA2';
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
          AND CONSERVAZIONE = 'ARUBA2_OLD';
        EXCEPTION
                      WHEN NO_DATA_FOUND
                      THEN
                         d_esiste_old := 0;
        END;

                IF d_esiste = 0
                THEN
                    D_CONSERVATORE := 'ARUBA2';
                ELSIF d_esiste_old = 0
                THEN
                    D_CONSERVATORE := 'ARUBA2_2022';
        ELSE
                    D_CONSERVATORE := 'ARUBA2';
        END IF;

                -- cancello se presente ARUBA2_2022
                DELETE CONSERVAZIONI
                WHERE AREA = 'SEGRETERIA.ATTI.2_0'
                AND CODICE_MODELLO = 'DETERMINA'
                AND CONSERVAZIONE = 'ARUBA2_2022';

                DELETE CONSERVAZIONI_PARAMETRI
                WHERE AREA = 'SEGRETERIA.ATTI.2_0'
                AND CODICE_MODELLO = 'DETERMINA'
                AND CONSERVAZIONE = 'ARUBA2_2022';

                -- cancello se già è stato fatto lo switch per aggiornare le regole
                IF D_CONSERVATORE = 'ARUBA2' THEN
                    DELETE CONSERVAZIONI
                    WHERE AREA = 'SEGRETERIA.ATTI.2_0'
                    AND CODICE_MODELLO = 'DETERMINA'
                    AND CONSERVAZIONE = 'ARUBA2';

                    DELETE CONSERVAZIONI_PARAMETRI
                    WHERE AREA = 'SEGRETERIA.ATTI.2_0'
                    AND CODICE_MODELLO = 'DETERMINA'
                    AND CONSERVAZIONE = 'ARUBA2';
        END IF;


        INSERT INTO CONSERVAZIONI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO_DOCUMENTO, DESCRIZIONE)
        VALUES
        ('SEGRETERIA.ATTI.2_0','DETERMINA',D_CONSERVATORE,'Determina','Determina :NUMERO_DETERMINA/:ANNO_DETERMINA :ID_REGISTRO_DETERMINA');

        INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
        VALUES
        ('SEGRETERIA.ATTI.2_0',
         'DETERMINA',
         D_CONSERVATORE,
         'Agid2022',
         '1',
         'SELECT');

        UPDATE CONSERVAZIONI_PARAMETRI SET
            VALORE = 'select

         ''oggetto'', oggetto

        , ''Modalita_formazione'',''creazione tramite l''''utilizzo di strumenti software che assicurino la produzione di documenti nei formati previsti nell''''Allegato 2 delle Linee Guida''
        , ''impronta'',''#CODICE_HASH#''
        , ''algoritmo'',''SHA-256''
        , ''identificativo'',id_determina
        , ''segnatura'', case
                   when  length(numero_protocollo)>0 and length(data_numero_protocollo)>0 then
                       decode(numero_protocollo,'''',NULL, NULL,NULL,
             ''Progressivo di protocollo: '' || numero_protocollo || '' data: '' || to_char(data_numero_protocollo,''dd/mm/yyyy'') ||
            (select '' IPA: '' ||  to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_ENTE'')||
           '' Codice AOO: '' || CODICE_AOO  ||
            '' Descrizione AOO: '' || DESCRIZIONE_AOO
            )
              else
              ''Non Applicabile''
             end
        , ''Tipologia_documentale'',''ATTO MONOCRATICO''

        , ''tipologia_flusso'',''I''
        , ''tipo_registro'',''Repertorio/Registro''
        , ''data_registrazione'',to_char (to_date (nvl(data_numero_determina,data_numero_proposta), ''dd/mm/YY hh24::mi::ss''),''YYYY-MM-DD"T"HH24::mi::ss"+02"'')
        , ''numero_documento'',nvl(NVL(numero_protocollo, numero_determina),0)
        , ''Idregistro'',descrizione_registro_determina

        , ''Indice_classificazione'',nvl(classifica_codice,''Non Applicabile'')
        , ''Descrizione'',nvl(classifica_descrizione,''Non Applicabile'')
        , ''Riservato'',decode(nvl(riservato,''N''),''N'',''false'',''true'')

        , ''Formato'',''PDF''

        , ''Firmato_digitalmente'',''#FIRMATO#''
        , ''Sigillato_elettronicamente'',''false''
        , ''Marcatura_temporale'',''false''
        , ''Conformita_copie_immagine'',''false''

        , ''IdDoc_primario'',id_determina
        , ''nome_doc'',filename
        , ''versione_doc'',''1''

        , ''Tempo_conservazione'',''9999''
        , ''IdAgg'', decode(fascicolo_numero,'''',''Serie Documenatale'',''Fascicolo'') || '' '' || decode(fascicolo_numero,'''',''ATTO COLLEGGIALE '' || to_char(data_numero_determina, ''YYYY'') ,fascicolo_numero || ''-'' || fascicolo_anno || '' '' || fascicolo_oggetto)

        ,''numero_allegati_ref'', (select count(1) from (select id_documento_esterno id_documento
                      from agsde2_cons_determine_allegati alle
                     where alle.id_determina = '':ID_DOCUMENTO_GRAILS''
                       and tipo_oggetto in (''VISTO'',''ALLEGATO'',''CERTIFICATO'')
                   )) numero_allegati
        ,''id_allegati_ref'', ( SELECT XMLAGG(XMLELEMENT(E,id_documento_esterno||'','')).EXTRACT(''//text()'') "Result" FROM agsde2_cons_determine_allegati  where tipo_oggetto in (''VISTO'',''ALLEGATO'',''CERTIFICATO'') and id_determina = '':ID_DOCUMENTO_GRAILS'' ) || '' '' id_allegati
        ,''descrizione_allegati_ref'', (SELECT XMLAGG(XMLELEMENT(E,filename||'','')).EXTRACT(''//text()'') "Result" FROM agsde2_cons_determine_allegati where tipo_oggetto in (''VISTO'',''ALLEGATO'',''CERTIFICATO'') and id_determina = '':ID_DOCUMENTO_GRAILS'' ) || '' '' descrizione_allegati
          from agsde2_cons_determine
         where id_determina = '':ID_DOCUMENTO_GRAILS'''
        WHERE AREA = 'SEGRETERIA.ATTI.2_0'
          AND CODICE_MODELLO = 'DETERMINA'
          AND TIPO = 'Agid2022'
          AND SEQUENZA = '1'
          AND CONSERVAZIONE = D_CONSERVATORE;

        INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
        VALUES
        ('SEGRETERIA.ATTI.2_0',
         'DETERMINA',
         D_CONSERVATORE,
         'Allegati',
         '1',
         'SELECT');

        UPDATE CONSERVAZIONI_PARAMETRI SET
            VALORE = 'select id_documento_esterno id_documento ,id_documento_esterno ID_OGGETTO_FILE   ,  filename,filename filenameall
          from agsde2_cons_determine_allegati alle
         where alle.id_determina = '':ID_DOCUMENTO_GRAILS''
           and tipo_oggetto = ''VISTO''
        union
        select id_documento_esterno id_documento ,id_documento_esterno ID_OGGETTO_FILE      ,  filename,filename filenameall
          from agsde2_cons_determine_allegati alle
         where alle.id_determina = '':ID_DOCUMENTO_GRAILS''
           and tipo_oggetto = ''ALLEGATO''
        union
        select id_documento_esterno id_documento ,id_documento_esterno ID_OGGETTO_FILE      ,  filename,filename filenameall
          from agsde2_cons_determine_allegati alle
         where alle.id_determina = '':ID_DOCUMENTO_GRAILS''
           and tipo_oggetto = ''CERTIFICATO''
        '
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
         'DocumentoPrincipale',
         '1',
         'SELECT');

        UPDATE CONSERVAZIONI_PARAMETRI SET
            VALORE = '  select id_documento_esterno id_documento  ,
             filename nome_documento,
           ''docFileName'',replace(filename,''/'',''-''),
                ''protocollo'',nvl(to_char(numero_protocollo),''ND''),
                ''anno_protocollo'',nvl(to_char(anno_protocollo),''ND''),
                ''oggetto'',oggetto,
        ''tipo_procedura'',tipo_determina,
        ''area'',descrizione_area,
        ''data_documento'', to_char (to_date (data_numero_determina, ''dd/mm/YY hh24::mi::ss''),''YYYY-MM-DD"T"HH24::mi::ss"+02"''),
        ''numero_documento'',numero_determina,
        ''anno_documento'',anno_determina,
        ''registro'',descrizione_registro_determina,
        ''data_pubblicazione'',  to_char (to_date (data_pubblicazione, ''dd/mm/YY hh24::mi::ss''), ''YYYY-MM-DD"T"HH24::mi::ss"+02"''),
        ''Data_esecutiva'',to_char (to_date (data_esecutivita, ''DD/MM/YY hh24::mi::ss''),''YYYY-MM-DD"T"HH24::mi::ss"+02"''),
        ''Ufficio_proponente'',unita_proponente,
        ''Firmatario'',FIRMATARIO,
        ''Titolario'',classifica_descrizione,
        ''Pratica'',fascicolo_oggetto
          from agsde2_cons_determine
        where id_determina = '':ID_DOCUMENTO_GRAILS'''
        WHERE AREA = 'SEGRETERIA.ATTI.2_0'
          AND CODICE_MODELLO = 'DETERMINA'
          AND TIPO = 'DocumentoPrincipale'
          AND SEQUENZA = '1'
          AND CONSERVAZIONE = D_CONSERVATORE;

        INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
        VALUES
        ('SEGRETERIA.ATTI.2_0',
         'DETERMINA',
         D_CONSERVATORE,
         'Soggetti2022',
         '1',
         'SELECT');

        UPDATE CONSERVAZIONI_PARAMETRI SET
            VALORE = 'select
        ''Ruolo_assegnatario.tiposoggetto'',''AS''
        , ''Ruolo_assegnatario.indriferimento'',(select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''INDIRIZZO_DIGITALE_ENTE'')
        , ''Ruolo_assegnatario.denamministrazione'',(select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''DENOMINAZIONE_ENTE'')
        , ''Ruolo_assegnatario.denamministrazioneipa'',(select upper(to_char(nvl(valore,''-''))) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_ENTE'')
        , ''Ruolo_assegnatario.denamministrazioneaoo'', CODICE_AOO || '' '' || DESCRIZIONE_AOO
        , ''Ruolo_assegnatario.denamministrazioneuor'',UNITA_PROPONENTE

        , ''Ruolo_autore.tiposoggetto'',''PF''
        , ''Ruolo_autore.nome'', nvl(nome_firmatario,''Non Applicabile'')
        , ''Ruolo_autore.cognome'',nvl(cognome_firmatario,''Non Applicabile'')

        , ''Ruolo_aer.tiposoggetto'',''PAI''
        , ''Ruolo_aer.denamministrazione'',(select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''DENOMINAZIONE_ENTE'')
        , ''Ruolo_aer.denamministrazioneipa'',(select upper(to_char(nvl(valore,''-''))) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_ENTE'')
        , ''Ruolo_aer.denamministrazioneaoo'', (select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AOO'') || '' '' || (select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AOO'')
        , ''Ruolo_aer.indriferimento'',(select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''INDIRIZZO_DIGITALE_ENTE'')

        , ''Ruolo_rup.tiposoggetto'',''PF''
        , ''Ruolo_rup.nome'', nvl(nome_funzionario,''Non Applicabile'')
        , ''Ruolo_rup.cognome'',nvl(cognome_funzionario,''Non Applicabile'')
        , ''Ruolo_rup.indriferimento'',(select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''INDIRIZZO_DIGITALE_ENTE'')
        , ''Ruolo_rup.Denamministrazione'',(select to_char(valore) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''DENOMINAZIONE_ENTE'')
        , ''Ruolo_rup.Denamministrazioneipa'',(select upper(to_char(nvl(valore,''-''))) from conservazioni_parametri where conservazione=''ARUBA2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_ENTE'')
        , ''Ruolo_rup.Denamministrazioneaoo'', CODICE_AOO || '' '' || DESCRIZIONE_AOO
        , ''Ruolo_rup.Denamministrazioneuor'',UNITA_PROPONENTE
        , ''1'',''1''
          from agsde2_cons_determine
        where id_determina = '':ID_DOCUMENTO_GRAILS'''
        WHERE AREA = 'SEGRETERIA.ATTI.2_0'
          AND CODICE_MODELLO = 'DETERMINA'
          AND TIPO = 'Soggetti2022'
          AND SEQUENZA = '1'
          AND CONSERVAZIONE = D_CONSERVATORE;

-- gestione paremtri
    select nvl(max(sequenza),0) into d_sequenza
    from CONSERVAZIONI_PARAMETRI
    WHERE CONSERVAZIONE = 'ARUBA2' AND TIPO='Parametri';

    BEGIN
        SELECT 1
        INTO d_esiste
        FROM CONSERVAZIONI_PARAMETRI
        WHERE CONSERVAZIONE = 'ARUBA2' AND CHIAVE = 'DETERMINA' AND TIPO='Parametri';
    EXCEPTION
          WHEN NO_DATA_FOUND
          THEN
             d_esiste := 0;
    END;

    IF d_esiste = 0
       THEN
         d_sequenza := d_sequenza + 1;

        Insert into CONSERVAZIONI_PARAMETRI
        (CONSERVAZIONE, TIPO, SEQUENZA, AREA, CODICE_MODELLO,
         CHIAVE, VALORE , VERSION, DATA_INS,
         DATA_UPD, UTENTE_INS, UTENTE_UPD, VALIDO)
        Values
        ('ARUBA2', 'Parametri', d_sequenza, '-', '-',
         'DETERMINA', '?bucketId determina?' , 0, sysdate,
         sysdate, 'GDM', 'GDM', 'Y');
    END IF;


    BEGIN
        SELECT 1
        INTO d_esiste
        FROM CONSERVAZIONI_PARAMETRI
        WHERE CONSERVAZIONE = 'ARUBA2' AND CHIAVE = 'DELIBERA' AND TIPO='Parametri';
    EXCEPTION
          WHEN NO_DATA_FOUND
          THEN
             d_esiste := 0;
    END;

    IF d_esiste = 0
       THEN
        d_sequenza := d_sequenza + 1;

        Insert into CONSERVAZIONI_PARAMETRI
        (CONSERVAZIONE, TIPO, SEQUENZA, AREA, CODICE_MODELLO,
         CHIAVE, VALORE , VERSION, DATA_INS,
         DATA_UPD, UTENTE_INS, UTENTE_UPD, VALIDO)
        Values
        ('ARUBA2', 'Parametri', d_sequenza, '-', '-',
         'DELIBERA', '?bucketId delibera?' , 0, sysdate,
         sysdate, 'GDM', 'GDM', 'Y');
    END IF;

COMMIT;

END IF;

END;
/