--liquibase formatted sql
--changeset mfrancesconi:2.5_20220506_03.05.regole_agid_parer runOnChange:true failOnError:false

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
        AND owner = '${global.db.gdm.username}';
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
           AND CONSERVAZIONE = 'PARER2';
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
          AND CONSERVAZIONE = 'PARER2_OLD';
    EXCEPTION
          WHEN NO_DATA_FOUND
          THEN
             d_esiste_old := 0;
    END;

    IF d_esiste = 0
    THEN
        D_CONSERVATORE := 'PARER2';
    ELSIF d_esiste_old = 0
    THEN
        D_CONSERVATORE := 'PARER2_2022';
    ELSE
        D_CONSERVATORE := 'PARER2';
    END IF;
	
	-- cancello se presente PARER2_2022
	DELETE CONSERVAZIONI
    WHERE AREA = 'SEGRETERIA.ATTI.2_0'
    AND CODICE_MODELLO = 'DELIBERA'
    AND CONSERVAZIONE = 'PARER2_2022';

	DELETE CONSERVAZIONI_PARAMETRI
	WHERE AREA = 'SEGRETERIA.ATTI.2_0'
    AND CODICE_MODELLO = 'DELIBERA'
    AND CONSERVAZIONE = 'PARER2_2022'
    AND TIPO <> 'DatiSpecifici';

    -- cancello se già è stato fatto lo switch per aggiornare le regole
    IF D_CONSERVATORE = 'PARER2' THEN
        DELETE CONSERVAZIONI
        WHERE AREA = 'SEGRETERIA.ATTI.2_0'
        AND CODICE_MODELLO = 'DELIBERA'
        AND CONSERVAZIONE = 'PARER2';

        DELETE CONSERVAZIONI_PARAMETRI
        WHERE AREA = 'SEGRETERIA.ATTI.2_0'
        AND CODICE_MODELLO = 'DELIBERA'
        AND CONSERVAZIONE = 'PARER2'
        AND TIPO <> 'DatiSpecifici';
    END IF;

	INSERT INTO CONSERVAZIONI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO_DOCUMENTO, DESCRIZIONE)
	VALUES
	('SEGRETERIA.ATTI.2_0','DELIBERA',D_CONSERVATORE,'Delibera',':NUMERO_DELIBERA/:ANNO_DELIBERA :ID_REGISTRO_DELIBERA Delibera');

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DELIBERA',
	 D_CONSERVATORE,
	 'Agid2022',
	 '1',
	 'Versione');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'AGID_V_1.0' 
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
	 'Agid2022',
	 '2',
	 'SELECT');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = VALORE||'SELECT
	''DocumentoAmministrativoInformatico/IdDoc/ImprontaCrittograficaDelDocumento/Impronta'' att01,
	''#CODICE_HASH#''  val01,
	''DocumentoAmministrativoInformatico/IdDoc/ImprontaCrittograficaDelDocumento/Algoritmo'' att02,
	''SHA-256'' val02,
	''DocumentoAmministrativoInformatico/IdDoc/Identificativo'' att03,
	id_delibera val03,
	''DocumentoAmministrativoInformatico/IdDoc/Segnatura'' att03,
	case
		   when  length(numero_protocollo)>0 and length(data_numero_protocollo)>0 then
			   decode(numero_protocollo,'''',NULL, NULL,NULL,
	 ''Progressivo di protocollo: '' || numero_protocollo || '' data: '' || to_char(data_numero_protocollo,''dd/mm/yyyy'') ||
	(select '' IPA: '' ||  to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AMM'')||
	(select '' Codice AOO: '' ||  to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AOO'')||
	(select '' Descrizione AOO: '' ||  to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AOO'')
	)
	  else
	  NULL
	 end val03,
	''DocumentoAmministrativoInformatico/ModalitaDiFormazione'' att04,
	''creazione tramite utilizzo di strumenti software che assicurino la produzione di documenti nei formati previsti in allegato 2'' val04,
	''DocumentoAmministrativoInformatico/TipologiaDocumentale'' att05,
	''ATTO COLLEGGIALE'' val05,
	''DocumentoAmministrativoInformatico/DatiDiRegistrazione/TipologiaDiFlusso'' att06,
	''I'' val06,
	''DocumentoAmministrativoInformatico/DatiDiRegistrazione/TipoRegistro/Repertorio_Registro/TipoRegistro'' att07,
	''Repertorio\Registro'' val07,
	''DocumentoAmministrativoInformatico/DatiDiRegistrazione/TipoRegistro/Repertorio_Registro/DataRegistrazioneDocumento'' att08,
	to_char(data_numero_delibera, ''yyyy-mm-dd'') val08,
	''DocumentoAmministrativoInformatico/DatiDiRegistrazione/TipoRegistro/Repertorio_Registro/NumeroRegistrazioneDocumento'' att09,
	numero_delibera val09,
	''DocumentoAmministrativoInformatico/DatiDiRegistrazione/TipoRegistro/Repertorio_Registro/CodiceRegistro'' att10,
	registro_delibera val10,
	''DocumentoAmministrativoInformatico/Soggetti'' att11,
	''#'' val1,
	''DocumentoAmministrativoInformatico/ChiaveDescrittiva/Oggetto'' att31,
	oggetto val31,
	''DocumentoAmministrativoInformatico/Allegati'' att32,
	''#'' val32,
	''DocumentoAmministrativoInformatico/Classificazione/IndiceDiClassificazione'' att33,
	nvl(classifica_codice,''Non Applicabile'') val33,
	''DocumentoAmministrativoInformatico/Classificazione/Descrizione'' att34,
	nvl(classifica_descrizione,''Non Applicabile'') val34,
	''DocumentoAmministrativoInformatico/Riservato'' att35,
	decode(nvl(riservato,''N''),''N'',''false'',''true'') val35,
	''DocumentoAmministrativoInformatico/IdentificativoDelFormato/Formato'' att36,
	 UPPER(substr(filename,instr(filename,''.'',-1)+1,length(filename)-instr(filename,''.'',-1))) val36,
	''DocumentoAmministrativoInformatico/Verifica/FirmatoDigitalmente'' att37,
	''#FIRMA' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DELIBERA' 
	AND TIPO = 'Agid2022' 
	AND SEQUENZA = '2' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = VALORE||'TO#'' val37,
	''DocumentoAmministrativoInformatico/Verifica/SigillatoElettronicamente'' att38,
	''false'' val38,
	''DocumentoAmministrativoInformatico/Verifica/MarcaturaTemporale'' att39,
	''false'' val39,
	''DocumentoAmministrativoInformatico/Verifica/ConformitaCopieImmagineSuSupportoInformatico'' att40,
	''false'' val40,
	''DocumentoAmministrativoInformatico/Agg/TipoAgg/TipoAggregazione'' att41,
	decode(fascicolo_numero,'''',''Serie Documentale'',''Fascicolo'')  val41,
	''DocumentoAmministrativoInformatico/Agg/TipoAgg/IdAggregazione'' att42,
	decode(fascicolo_numero,'''',''ATTO COLLEGGIALE '' || to_char(data_numero_delibera, ''YYYY'') ,fascicolo_numero || ''-'' || fascicolo_anno || '' '' || fascicolo_oggetto) val42,
	''DocumentoAmministrativoInformatico/IdIdentificativoDocumentoPrimario/ImprontaCrittograficaDelDocumento/Impronta'' att43,
	''#CODICE_HASH#'' val43,
	''DocumentoAmministrativoInformatico/IdIdentificativoDocumentoPrimario/ImprontaCrittograficaDelDocumento/Algoritmo'' att44,
	''SHA-256'' val44,
	''DocumentoAmministrativoInformatico/IdIdentificativoDocumentoPrimario/Identificativo'' att45,
	id_file_gdm val45,
	''DocumentoAmministrativoInformatico/NomeDelDocumento'' att46,
	filename val46,
	''DocumentoAmministrativoInformatico/VersioneDelDocumento'' att47,
	''1.0'' val47,
	''DocumentoAmministrativoInformatico/TempoDiConservazione'' att48,
	''9999'' val48,
	''DocumentoAmministrativoInformatico/Note'' att49,
	''-'' val49
	from agsde2_cons_delibere
	where id_delibera = '':ID_DOCUMENTO_GRAILS''
	' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DELIBERA' 
	AND TIPO = 'Agid2022' 
	AND SEQUENZA = '2' 
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
	VALORE = 'select distinct id_documento_esterno id_documento
		 ,  filename
		 ,  id_file_gdm chiave_allegato
		 ,  ''DocumentoGenerico'' tipo_struttura
		 ,  titolo tipo_documento
		 ,  ''false'' utilizzodatafirmaperriftemp
		 ,  to_char (data_firma, ''yyyy-mm-dd'') || ''T'' || to_char (data_firma, ''hh24::mi::ss'') riferimento_temporale
		 ,  decode (data_firma, null, '''', ''Apposizione della firma'') desc_riferimento_temporale
		 ,  2 ordinamento
	  from agsde2_cons_delibere_allegati alle
	 where alle.id_delibera = '':ID_DOCUMENTO_GRAILS''
	   and tipo_oggetto = ''PARERE''
	union
	select distinct id_documento_esterno id_documento
		 ,  filename
		 ,  id_file_gdm chiave_allegato
		 ,  ''DocumentoGenerico'' tipo_struttura
		 ,  ''GENERICO'' tipo_documento
		 ,  ''false'' utilizzodatafirmaperriftemp
		 ,  to_char (data_firma, ''yyyy-mm-dd'') || ''T'' || to_char (data_firma, ''hh24::mi::ss'') riferimento_temporale
		 ,  decode (data_firma, null, '''', ''Apposizione della firma'') desc_riferimento_temporale
		 ,  1 ordinamento
	  from agsde2_cons_delibere_allegati alle
	 where alle.id_delibera = '':ID_DOCUMENTO_GRAILS''
	   and tipo_oggetto = ''ALLEGATO''
	union
	select distinct id_documento_esterno id_documento
		 ,  filename
		 ,  id_file_gdm chiave_allegato
		 ,  ''DocumentoGenerico'' tipo_struttura
		 , nvl (
			  decode (titolo
					, ''PUBBLICAZIONE'', ''RELATA DI PUBBLICAZIONE''
					, ''ESECUTIVITA'', ''CERTIFICATO DI ESECUTIVITA''
					, ''AVVENUTA_PUBBLICAZIONE'', ''ATTESTAZIONE DI PUBBLICAZIONE'')
			, decode (codice
					, ''PUBBLICAZIONE'', ''RELATA DI PUBBLICAZIONE''
					, ''ESECUTIVITA'', ''CERTIFICATO DI ESECUTIVITA''
					, ''AVVENUTA_PUBBLICAZIONE'', ''ATTESTAZIONE DI PUBBLICAZIONE''))
			  tipo_documento
		 ,  ''false'' utilizzodatafirmaperriftemp
		 ,  to_char (data_firma, ''yyyy-mm-dd'') || ''T'' || to_char (data_firma, ''hh24::mi::ss'') riferimento_temporale
		 ,  decode (data_firma, null, '''', ''Apposizione della firma'') desc_riferimento_temporale
		 , nvl (decode (titolo,  ''PUBBLICAZIONE'', 3,  ''ESECUTIVITA'', 4,  ''AVVENUTA_PUBBLICAZIONE'', 5)
			  , decode (codice,  ''PUBBLICAZIONE'', 3,  ''ESECUTIVITA'', 4,  ''AVVENUTA_PUBBLICAZIONE'', 5))
			  ordinamento
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
	 'Chiave',
	 '1',
	 'Numero');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'NUMERO_DELIBERA' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DELIBERA' 
	AND TIPO = 'Chiave' 
	AND SEQUENZA = '1' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DELIBERA',
	 D_CONSERVATORE,
	 'Chiave',
	 '2',
	 'Anno');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'ANNO_DELIBERA' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DELIBERA' 
	AND TIPO = 'Chiave' 
	AND SEQUENZA = '2' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DELIBERA',
	 D_CONSERVATORE,
	 'Chiave',
	 '3',
	 'TipoRegistro');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'ID_REGISTRO_DELIBERA' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DELIBERA' 
	AND TIPO = 'Chiave' 
	AND SEQUENZA = '3' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DELIBERA',
	 D_CONSERVATORE,
	 'Configurazione',
	 '1',
	 'TipoConservazione');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'VERSAMENTO_ANTICIPATO'
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DELIBERA' 
	AND TIPO = 'Configurazione' 
	AND SEQUENZA = '1' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DELIBERA',
	 D_CONSERVATORE,
	 'Configurazione',
	 '2',
	 'ForzaAccettazione');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'true' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DELIBERA' 
	AND TIPO = 'Configurazione' 
	AND SEQUENZA = '2' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DELIBERA',
	 D_CONSERVATORE,
	 'Configurazione',
	 '3',
	 'ForzaConservazione');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'true' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DELIBERA' 
	AND TIPO = 'Configurazione' 
	AND SEQUENZA = '3' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DELIBERA',
	 D_CONSERVATORE,
	 'Configurazione',
	 '4',
	 'ForzaCollegamento');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'true' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DELIBERA' 
	AND TIPO = 'Configurazione' 
	AND SEQUENZA = '4' 
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
	VALORE = 'select distinct id_documento_esterno id_documento
		 ,  filename
		 ,  id_file_gdm chiave_allegato
		 ,  ''DocumentoGenerico'' tipo_struttura
		 ,  ''false'' utilizzodatafirmaperriftemp
		 ,     to_char (nvl (data_numero_protocollo, nvl (data_pubblicazione, nvl (data_esecutivita, data_firma_segretario))), ''yyyy-mm-dd'')
			|| ''T''
			|| to_char (nvl (data_numero_protocollo, nvl (data_pubblicazione, nvl (data_esecutivita, data_firma_segretario))), ''hh24::mi::ss'')
			   riferimento_temporale
		 ,  decode (
			   data_numero_protocollo
			 ,  null, decode (data_pubblicazione
							,  null, decode (data_esecutivita, null, decode (data_firma_segretario, null, '''', ''Apposizione della firma''), ''Data Esecutivita'''' atto'')
							,  ''Pubblicazione atto'')
			 ,  ''Protocollazione'')
			   desc_riferimento_temporale
		 ,  ''1.0'' versione
		 ,  ''Data_pubblicazione'' att1
		 ,  to_char (data_pubblicazione, ''yyyy-mm-dd'') val1
		 ,  ''Data_fine_pubblicazione'' att2
		 ,  to_char (data_fine_pubblicazione, ''yyyy-mm-dd'') val2
		 ,  ''Data_esecutivita'' att3
		 ,  to_char (data_esecutivita, ''yyyy-mm-dd'') val3
		 ,  ''Firmatario'' att4
		 ,  dirigente val4
		 ,  ''Unita_proponente'' att5
		 ,  unita_proponente val5
		 ,  ''Data_inizio_classificazione'' att6
		 ,  to_char (classifica_dal, ''yyyy-mm-dd'') val6
		 ,  ''Descrizione_classificazione'' att7
		 ,  classifica_descrizione val7
		 ,  ''Dipartimento_adottante'' att8
		 ,  descrizione_area val8
		 ,  ''Responsabile_procedimento'' att9
		 ,  dirigente val9
		 ,  ''Firmatario_parere_contabile'' att10
		 ,  decode (impegno_spesa, ''N'', ''atto senza rilevanza contabile'', firmatario_parere_contabile) val10
		 ,  ''Data_parere_contabile'' att11
		 ,  decode (impegno_spesa, ''N'', '''', to_char (data_firma_parere_contabile, ''yyyy-mm-dd'')) val11
		 ,  ''Identificativo_proposta'' att12
		 ,  numero_proposta||'' / ''||anno_proposta val12
		 ,  ''Data_proposta'' att13
		 ,  to_char (data_numero_proposta, ''yyyy-mm-dd'') val13
		 , ''Segretario'' att14
		 , segretario val14
		 , ''Presidente'' att15
		 , presidente val15
		 ,  ''Tipo_pubblicazione'' att16
		 ,  decode (riservato, ''Y'', ''Pubblicazione per Estratto'', decode (num_allegati_riservati, 0, ''Pubblicazione Integrale'', ''Pubblicazione per Estratto'')) val16
		 ,  ''Anno_pubblicazione'' att17
		 ,  decode ( (select valore
						from agsde2_impostazioni
					   where codice = ''INTEGRAZIONE_ALBO''
						 and ente = agsde2_cons_delibere.ente)
				  ,  ''Y'', to_char(anno_albo)
				  ,  ''Integrazione non presente'')
			   val17
		 ,  ''Numero_pubblicazione'' att18
		 ,  decode ( (select valore
						from agsde2_impostazioni
					   where codice = ''INTEGRAZIONE_ALBO''
						 and ente = agsde2_cons_delibere.ente)
				  ,  ''Y'', to_char(numero_albo)
				  ,  ''Integrazione non presente'')
			   val18
		 ,  ''Registro_protocollo'' att19
		 ,  registro_protocollo val19
		 ,  ''Anno_protocollo'' att20
		 ,  anno_protocollo val20
		 ,  ''Numero_protocollo'' att21
		 ,  numero_protocollo val21
		 ,  ''Data_protocollo'' att22
		 ,  to_char (data_numero_protocollo, ''yyyy-mm-dd'') val22
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
	 'Intestazione',
	 '1',
	 'TipologiaUnitaDocumentaria');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'ID_REGISTRO_DELIBERA' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DELIBERA' 
	AND TIPO = 'Intestazione' 
	AND SEQUENZA = '1' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DELIBERA',
	 D_CONSERVATORE,
	 'ProfiloArchivistico',
	 '1',
	 'SELECT');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'select classifica_codice CLASS_COD, 
		   fascicolo_numero FASCICOLO_NUMERO, 
		   fascicolo_anno FASCICOLO_ANNO, 
		   fascicolo_oggetto FASCICOLO_OGGETTO
	  from agsde2_cons_delibere
	  where id_delibera = '':ID_DOCUMENTO_GRAILS''' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DELIBERA' 
	AND TIPO = 'ProfiloArchivistico' 
	AND SEQUENZA = '1' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DELIBERA',
	 D_CONSERVATORE,
	 'ProfiloUnitaDocumentaria',
	 '1',
	 'Oggetto');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'OGGETTO' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DELIBERA' 
	AND TIPO = 'ProfiloUnitaDocumentaria' 
	AND SEQUENZA = '1' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DELIBERA',
	 D_CONSERVATORE,
	 'ProfiloUnitaDocumentaria',
	 '2',
	 'Data');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'DATA_ADOZIONE' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DELIBERA' 
	AND TIPO = 'ProfiloUnitaDocumentaria' 
	AND SEQUENZA = '2' 
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
	VALORE = VALORE||'SELECT distinct

	''AmministrazioneCheEffettuaLaRegistrazione/TipoRuolo'' att01,
	''Amministrazione Che Effettua La Registrazione'' val01,
	''AmministrazioneCheEffettuaLaRegistrazione/PAI/IPAAmm/Denominazione'' att02,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AMM'')  val02,
	''AmministrazioneCheEffettuaLaRegistrazione/PAI/IPAAmm/CodiceIPA'' att03,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AMM'')  val03,
	''AmministrazioneCheEffettuaLaRegistrazione/PAI/IPAAOO/Denominazione'' att04,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AOO'')  val04,
	''AmministrazioneCheEffettuaLaRegistrazione/PAI/IPAAOO/CodiceIPA'' att05,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AOO'')  val05,
	''AmministrazioneCheEffettuaLaRegistrazione/PAI/IndirizziDigitaliDiRiferimento'' att06,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''INDIRIZZO_DIGITALE'')  val06,
	NULL att07,
	NULL val07,
	NULL att08,
	NULL val08,
	NULL att09,
	NULL val09,
	NULL att10,
	NULL val10,
	NULL att11,
	NULL val11
	from agsde2_cons_delibere
	where id_delibera = '':ID_DOCUMENTO_GRAILS''

	UNION

	SELECT DISTINCT
	''Autore/TipoRuolo'' att01,
	''Autore'' val01,
	''Autore/PF/Cognome'' att02,
	nvl(cognome_segretario,''Non Applicabile'')  val02,
	''Autore/PF/Nome'' att03,
	nvl(nome_segretario,''Non Applicabile'')  val03,
	NULL att04,
	NULL val04,
	NULL att05,
	NULL val05,
	NULL att06,
	NULL val06,
	NULL att07,
	NULL val07,
	NULL att08,
	NULL val08,
	NULL att09,
	NULL val09,
	NULL att10,
	NULL val10,
	NULL att11,
	NULL val11
	from agsde2_cons_delibere
	where id_delibera = '':ID_DOCUMENTO_GRAILS''

	UNION

	SELECT DISTINCT
	''Assegnatario/TipoRuolo'' att01,
	''Assegnatario'' val01,
	''Assegnatario/AS/IPAAmm/Denominazione'' att02,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AMM'')  val02,
	''Assegnatario/AS/IPAAmm/CodiceIPA'' att03,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AMM'') val03,
	''Assegnatario/AS/IPAAOO/Denominazione'' att04,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AOO'') val04,
	''Assegnatario/AS/IPAAOO/CodiceIPA''  att05,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AOO'')  val05,
	''Assegnatario/AS/IPAUOR/Denominazione''  att06,
	unita_proponente  val06,
	''Assegnatario/AS/IPAUOR/CodiceIPA''  att07,
	''Non Applicabile''  val07,
	''Assegnatario/AS/IndirizziDigitaliDiRiferimento''  att' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DELIBERA' 
	AND TIPO = 'Soggetti2022' 
	AND SEQUENZA = '1' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = VALORE||'08,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''INDIRIZZO_DIGITALE'')   val08,
	NULL att09,
	NULL val09,
	NULL att10,
	NULL val10,
	NULL att11,
	NULL val11
	from agsde2_cons_delibere
	where id_delibera = '':ID_DOCUMENTO_GRAILS''

	UNION

	SELECT DISTINCT
	''RUP/TipoRuolo'' att01,
	''RUP'' val01,
	''RUP/RUP/Cognome'' att02,
	nvl(cognome_funzionario,''Non Applicabile'')  val02,
	''RUP/RUP/Nome'' att03,
	nvl(nome_funzionario,''Non Applicabile'') val03,
	''RUP/RUP/CodiceFiscale''  att04,
	nvl(cf_funzionario,''Non Applicabile'') val04,
	''RUP/RUP/IPAAmm/Denominazione'' att05,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AMM'')  val05,
	''RUP/RUP/IPAAmm/CodiceIPA'' att06,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AMM'')   val06,
	''RUP/RUP/IPAAOO/Denominazione'' att07,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AOO'')  val07,
	''RUP/RUP/IPAAOO/CodiceIPA'' att08,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AOO'')  val08,
	''RUP/RUP/IPAUOR/Denominazione'' att09,
	unita_proponente  val09,
	''RUP/RUP/IPAUOR/CodiceIPA'' att10,
	''Non Applicabile'' val10,
	''RUP/RUP/IndirizziDigitaliDiRiferimento'' att11,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''INDIRIZZO_DIGITALE'')  val11
	from agsde2_cons_delibere
	where id_delibera = '':ID_DOCUMENTO_GRAILS''
	and presenza_funzionario=''Y''
	' 
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
			   AND CONSERVAZIONE = 'PARER2';
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
              AND CONSERVAZIONE = 'PARER2_OLD';
        EXCEPTION
              WHEN NO_DATA_FOUND
              THEN
                 d_esiste_old := 0;
        END;

		IF d_esiste = 0
        THEN
            D_CONSERVATORE := 'PARER2';
        ELSIF d_esiste_old = 0
        THEN
            D_CONSERVATORE := 'PARER2_2022';
        ELSE
            D_CONSERVATORE := 'PARER2';
        END IF;
		
		-- cancello se presente PARER2_2022
		DELETE CONSERVAZIONI
		WHERE AREA = 'SEGRETERIA.ATTI.2_0'
		AND CODICE_MODELLO = 'DETERMINA'
		AND CONSERVAZIONE = 'PARER2_2022';

		DELETE CONSERVAZIONI_PARAMETRI
		WHERE AREA = 'SEGRETERIA.ATTI.2_0'
		AND CODICE_MODELLO = 'DETERMINA'
		AND CONSERVAZIONE = 'PARER2_2022'
        AND TIPO <> 'DatiSpecifici';

        -- cancello se già è stato fatto lo switch per aggiornare le regole
        IF D_CONSERVATORE = 'PARER2' THEN
            DELETE CONSERVAZIONI
            WHERE AREA = 'SEGRETERIA.ATTI.2_0'
            AND CODICE_MODELLO = 'DETERMINA'
            AND CONSERVAZIONE = 'PARER2';

            DELETE CONSERVAZIONI_PARAMETRI
            WHERE AREA = 'SEGRETERIA.ATTI.2_0'
            AND CODICE_MODELLO = 'DETERMINA'
            AND CONSERVAZIONE = 'PARER2'
            AND TIPO <> 'DatiSpecifici';
        END IF;

		INSERT INTO CONSERVAZIONI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO_DOCUMENTO, DESCRIZIONE)
		VALUES ('SEGRETERIA.ATTI.2_0','DETERMINA',D_CONSERVATORE,'Determina',':NUMERO_DETERMINA/:ANNO_DETERMINA :ID_REGISTRO_DETERMINA Determina');

		INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DETERMINA',
	 D_CONSERVATORE,
	 'Agid2022',
	 '1',
	 'Versione');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'AGID_V_1.0' 
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
	 'Agid2022',
	 '2',
	 'SELECT');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = VALORE||'SELECT
	''DocumentoAmministrativoInformatico/IdDoc/ImprontaCrittograficaDelDocumento/Impronta'' att01,
	''#CODICE_HASH#''  val01,
	''DocumentoAmministrativoInformatico/IdDoc/ImprontaCrittograficaDelDocumento/Algoritmo'' att02,
	''SHA-256'' val02,
	''DocumentoAmministrativoInformatico/IdDoc/Identificativo'' att03,
	id_determina val03,
	''DocumentoAmministrativoInformatico/IdDoc/Segnatura'' att03,
	case
		   when  length(numero_protocollo)>0 and length(data_numero_protocollo)>0 then
			   decode(numero_protocollo,'''',NULL, NULL,NULL,
	 ''Progressivo di protocollo: '' || numero_protocollo || '' data: '' || to_char(data_numero_protocollo,''dd/mm/yyyy'') ||
	(select '' IPA: '' ||  to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AMM'')||
	(select '' Codice AOO: '' ||  to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AOO'')||
	(select '' Descrizione AOO: '' ||  to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AOO'')
	)
	  else
	  NULL
	 end val03,
	''DocumentoAmministrativoInformatico/ModalitaDiFormazione'' att04,
	''creazione tramite utilizzo di strumenti software che assicurino la produzione di documenti nei formati previsti in allegato 2'' val04,
	''DocumentoAmministrativoInformatico/TipologiaDocumentale'' att05,
	''ATTO MONOCRATICO'' val05,
	''DocumentoAmministrativoInformatico/DatiDiRegistrazione/TipologiaDiFlusso'' att06,
	''I'' val06,
	''DocumentoAmministrativoInformatico/DatiDiRegistrazione/TipoRegistro/Repertorio_Registro/TipoRegistro'' att07,
	''Repertorio\Registro'' val07,
	''DocumentoAmministrativoInformatico/DatiDiRegistrazione/TipoRegistro/Repertorio_Registro/DataRegistrazioneDocumento'' att08,
	to_char(data_numero_determina, ''yyyy-mm-dd'') val08,
	''DocumentoAmministrativoInformatico/DatiDiRegistrazione/TipoRegistro/Repertorio_Registro/NumeroRegistrazioneDocumento'' att09,
	numero_determina val09,
	''DocumentoAmministrativoInformatico/DatiDiRegistrazione/TipoRegistro/Repertorio_Registro/CodiceRegistro'' att10,
	registro_determina val10,
	''DocumentoAmministrativoInformatico/Soggetti'' att11,
	''#'' val1,
	''DocumentoAmministrativoInformatico/ChiaveDescrittiva/Oggetto'' att31,
	oggetto val31,
	''DocumentoAmministrativoInformatico/Allegati'' att32,
	''#'' val32,
	''DocumentoAmministrativoInformatico/Classificazione/IndiceDiClassificazione'' att33,
	nvl(classifica_codice,''Non Applicabile'') val33,
	''DocumentoAmministrativoInformatico/Classificazione/Descrizione'' att34,
	nvl(classifica_descrizione,''Non Applicabile'') val34,
	''DocumentoAmministrativoInformatico/Riservato'' att35,
	decode(nvl(riservato,''N''),''N'',''false'',''true'') val35,
	''DocumentoAmministrativoInformatico/IdentificativoDelFormato/Formato'' att36,
	 UPPER(substr(filename,instr(filename,''.'',-1)+1,length(filename)-instr(filename,''.'',-1))) val36,
	''DocumentoAmministrativoInformatico/Verifica/FirmatoDigitalmente'' att37,
	''#F' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'Agid2022' 
	AND SEQUENZA = '2' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = VALORE||'IRMATO#'' val37,
	''DocumentoAmministrativoInformatico/Verifica/SigillatoElettronicamente'' att38,
	''false'' val38,
	''DocumentoAmministrativoInformatico/Verifica/MarcaturaTemporale'' att39,
	''false'' val39,
	''DocumentoAmministrativoInformatico/Verifica/ConformitaCopieImmagineSuSupportoInformatico'' att40,
	''false'' val40,
	''DocumentoAmministrativoInformatico/Agg/TipoAgg/TipoAggregazione'' att41,
	decode(fascicolo_numero,'''',''Serie Documentale'',''Fascicolo'')  val41,
	''DocumentoAmministrativoInformatico/Agg/TipoAgg/IdAggregazione'' att42,
	decode(fascicolo_numero,'''',''ATTO MONOCRATICO '' || to_char(data_numero_determina, ''YYYY'') ,fascicolo_numero || ''-'' || fascicolo_anno || '' '' || fascicolo_oggetto) val42,
	''DocumentoAmministrativoInformatico/IdIdentificativoDocumentoPrimario/ImprontaCrittograficaDelDocumento/Impronta'' att43,
	''#CODICE_HASH#'' val43,
	''DocumentoAmministrativoInformatico/IdIdentificativoDocumentoPrimario/ImprontaCrittograficaDelDocumento/Algoritmo'' att44,
	''SHA-256'' val44,
	''DocumentoAmministrativoInformatico/IdIdentificativoDocumentoPrimario/Identificativo'' att45,
	id_file_gdm val45,
	''DocumentoAmministrativoInformatico/NomeDelDocumento'' att46,
	filename val46,
	''DocumentoAmministrativoInformatico/VersioneDelDocumento'' att47,
	''1.0'' val47,
	''DocumentoAmministrativoInformatico/TempoDiConservazione'' att48,
	''9999'' val48,
	''DocumentoAmministrativoInformatico/Note'' att49,
	''-'' val49
	from agsde2_cons_determine
	where id_determina = '':ID_DOCUMENTO_GRAILS''
	' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'Agid2022' 
	AND SEQUENZA = '2' 
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
	VALORE = 'select distinct id_documento_esterno id_documento
		 ,  filename
		 ,  id_file_gdm chiave_allegato
		 ,  ''DocumentoGenerico'' tipo_struttura
		 ,  titolo tipo_documento
		 ,  ''false'' utilizzodatafirmaperriftemp
		 ,  to_char (data_firma, ''yyyy-mm-dd'') || ''T'' || to_char (data_firma, ''hh24::mi::ss'') riferimento_temporale
		 ,  decode (data_firma, null, '''', ''Apposizione della firma'') desc_riferimento_temporale
		 ,  2 ordinamento
	  from agsde2_cons_determine_allegati alle
	 where alle.id_determina = '':ID_DOCUMENTO_GRAILS''
	   and tipo_oggetto = ''VISTO''
	union
	select distinct id_documento_esterno id_documento
		 ,  filename
		 ,  id_file_gdm chiave_allegato
		 ,  ''DocumentoGenerico'' tipo_struttura
		 ,  ''GENERICO'' tipo_documento
		 ,  ''false'' utilizzodatafirmaperriftemp
		 ,  to_char (data_firma, ''yyyy-mm-dd'') || ''T'' || to_char (data_firma, ''hh24::mi::ss'') riferimento_temporale
		 ,  decode (data_firma, null, '''', ''Apposizione della firma'') desc_riferimento_temporale
		 ,  1 ordinamento
	  from agsde2_cons_determine_allegati alle
	 where alle.id_determina = '':ID_DOCUMENTO_GRAILS''
	   and tipo_oggetto = ''ALLEGATO''
	union
	select distinct id_documento_esterno id_documento
		 ,  filename
		 ,  id_file_gdm chiave_allegato
		 ,  ''DocumentoGenerico'' tipo_struttura
		 , nvl (
			  decode (titolo
					, ''PUBBLICAZIONE'', ''RELATA DI PUBBLICAZIONE''
					, ''ESECUTIVITA'', ''CERTIFICATO DI ESECUTIVITA''
					, ''AVVENUTA_PUBBLICAZIONE'', ''ATTESTAZIONE DI PUBBLICAZIONE'')
			, decode (codice
					, ''PUBBLICAZIONE'', ''RELATA DI PUBBLICAZIONE''
					, ''ESECUTIVITA'', ''CERTIFICATO DI ESECUTIVITA''
					, ''AVVENUTA_PUBBLICAZIONE'', ''ATTESTAZIONE DI PUBBLICAZIONE''))
			  tipo_documento
		 ,  ''false'' utilizzodatafirmaperriftemp
		 ,  to_char (data_firma, ''yyyy-mm-dd'') || ''T'' || to_char (data_firma, ''hh24::mi::ss'') riferimento_temporale
		 ,  decode (data_firma, null, '''', ''Apposizione della firma'') desc_riferimento_temporale
		 , nvl (decode (titolo,  ''PUBBLICAZIONE'', 3,  ''ESECUTIVITA'', 4,  ''AVVENUTA_PUBBLICAZIONE'', 5)
			  , decode (codice,  ''PUBBLICAZIONE'', 3,  ''ESECUTIVITA'', 4,  ''AVVENUTA_PUBBLICAZIONE'', 5))
			  ordinamento
	  from agsde2_cons_determine_allegati alle
	 where alle.id_determina = '':ID_DOCUMENTO_GRAILS''
	   and tipo_oggetto = ''CERTIFICATO''
	order by ordinamento' 
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
	 'Chiave',
	 '1',
	 'Numero');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'NUMERO_DETERMINA' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'Chiave' 
	AND SEQUENZA = '1' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DETERMINA',
	 D_CONSERVATORE,
	 'Chiave',
	 '2',
	 'Anno');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'ANNO_DETERMINA' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'Chiave' 
	AND SEQUENZA = '2' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DETERMINA',
	 D_CONSERVATORE,
	 'Chiave',
	 '3',
	 'TipoRegistro');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'ID_REGISTRO_DETERMINA' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'Chiave' 
	AND SEQUENZA = '3' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DETERMINA',
	 D_CONSERVATORE,
	 'Configurazione',
	 '1',
	 'TipoConservazione');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'VERSAMENTO_ANTICIPATO'
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'Configurazione' 
	AND SEQUENZA = '1' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DETERMINA',
	 D_CONSERVATORE,
	 'Configurazione',
	 '2',
	 'ForzaAccettazione');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'true' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'Configurazione' 
	AND SEQUENZA = '2' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DETERMINA',
	 D_CONSERVATORE,
	 'Configurazione',
	 '3',
	 'ForzaConservazione');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'true' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'Configurazione' 
	AND SEQUENZA = '3' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DETERMINA',
	 D_CONSERVATORE,
	 'Configurazione',
	 '4',
	 'ForzaCollegamento');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'true' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'Configurazione' 
	AND SEQUENZA = '4' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DETERMINA',
	 D_CONSERVATORE,
	 'DocumentiCollegati',
	 '1',
	 'SELECT');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'select id_determina, numero_determina_collegata numero
		 ,  anno_determina_collegata anno
		 ,  registro_determina_collegata tipo_registro
		 ,  descrizione_collegamento descrizione_collegamento
	  from agsde2_cons_dete_collegate
	 where id_determina = '':ID_DOCUMENTO_GRAILS''' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'DocumentiCollegati' 
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
	VALORE = 'select distinct id_documento_esterno id_documento
		 ,  filename
		 ,  id_file_gdm chiave_allegato
		 ,  ''DocumentoGenerico'' tipo_struttura
		 ,  ''false'' utilizzodatafirmaperriftemp
		 ,     to_char (nvl (data_numero_protocollo, nvl (data_esecutivita, nvl (data_pubblicazione, data_firma))), ''yyyy-mm-dd'')
			|| ''T''
			|| to_char (nvl (data_numero_protocollo, nvl (data_esecutivita, nvl (data_pubblicazione, data_firma))), ''hh24::mi::ss'')
			   riferimento_temporale
		 ,  decode (
			   data_numero_protocollo
			 ,  null, decode (data_esecutivita
							,  null, decode (data_pubblicazione, null, decode (data_firma, null, '''', ''Apposizione della firma''), ''Pubblicazione atto'')
							,  ''Data Esecutivita'''' atto'')
			 ,  ''Protocollazione'')
			   desc_riferimento_temporale
		 ,  ''1.0'' versione
		 ,  ''Data_pubblicazione'' att1
		 ,  to_char (data_pubblicazione, ''yyyy-mm-dd'') val1
		 ,  ''Data_fine_pubblicazione'' att2
		 ,  to_char (data_fine_pubblicazione, ''yyyy-mm-dd'') val2
		 ,  ''Data_esecutivita'' att3
		 ,  to_char (data_esecutivita, ''yyyy-mm-dd'') val3
		 ,  ''Firmatario'' att4
		 ,  firmatario val4
		 ,  ''Unita_proponente'' att5
		 ,  unita_proponente val5
		 ,  ''Data_inizio_classificazione'' att6
		 ,  to_char (classifica_dal, ''yyyy-mm-dd'') val6
		 ,  ''Descrizione_classificazione'' att7
		 ,  classifica_descrizione val7
		 ,  ''Dipartimento_adottante'' att8
		 ,  descrizione_area val8
		 ,  ''Responsabile_procedimento'' att9
		 ,  decode (funzionario, null, redattore, funzionario) val9
		 ,  ''Firmatario_visto_contabile'' att10
		 ,  decode (impegno_spesa, ''N'', ''atto senza impegno di spesa'', firmatario_visto_contabile) val10
		 ,  ''Data_visto_contabile'' att11
		 ,  decode (impegno_spesa, ''N'', '''', to_char (data_firma_visto_contabile, ''yyyy-mm-dd'')) val11
		 ,  ''Identificativo_proposta'' att12
		 ,  numero_proposta||'' / ''||anno_proposta val12
		 ,    ''Data_proposta'' att13
		 ,    to_char (data_numero_proposta, ''yyyy-mm-dd'') val13
		 ,  ''Tipo_pubblicazione'' att14
		 ,  decode (riservato, ''Y'', ''Pubblicazione per Estratto'', decode (num_allegati_riservati, 0, ''Pubblicazione Integrale'', ''Pubblicazione per Estratto'')) val14
		 ,  ''Anno_pubblicazione'' att15
		 ,  decode ( (select valore
						from agsde2_impostazioni
					   where codice = ''INTEGRAZIONE_ALBO''
						 and ente = agsde2_cons_determine.ente)
				  ,  ''Y'', to_char(anno_albo)
				  ,  ''Integrazione non presente'')
			   val15
		 ,  ''Numero_pubblicazione'' att16
		 ,  decode ( (select valore
						from agsde2_impostazioni
					   where codice = ''INTEGRAZIONE_ALBO''
						 and ente = agsde2_cons_determine.ente)
				  ,  ''Y'', to_char(numero_albo)
				  ,  ''Integrazione non presente'')
			   val16
		 ,  ''Registro_protocollo'' att17
		 ,  registro_protocollo val17
		 ,  ''Anno_protocollo'' att18
		 ,  anno_protocollo val18
		 ,  ''Numero_protocollo'' att19
		 ,  numero_protocollo val19
		 ,  ''Data_protocollo'' att20
		 ,  to_char (data_numero_protocollo, ''yyyy-mm-dd'') val20
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
	 'Intestazione',
	 '1',
	 'TipologiaUnitaDocumentaria');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'ID_REGISTRO_DETERMINA' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'Intestazione' 
	AND SEQUENZA = '1' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DETERMINA',
	 D_CONSERVATORE,
	 'ProfiloArchivistico',
	 '1',
	 'SELECT');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'select classifica_codice CLASS_COD, 
		   fascicolo_numero FASCICOLO_NUMERO, 
		   fascicolo_anno FASCICOLO_ANNO, 
		   fascicolo_oggetto FASCICOLO_OGGETTO
	  from agsde2_cons_determine
	  where id_determina = '':ID_DOCUMENTO_GRAILS''' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'ProfiloArchivistico' 
	AND SEQUENZA = '1' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DETERMINA',
	 D_CONSERVATORE,
	 'ProfiloUnitaDocumentaria',
	 '1',
	 'Oggetto');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'OGGETTO' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'ProfiloUnitaDocumentaria' 
	AND SEQUENZA = '1' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	INSERT INTO CONSERVAZIONI_PARAMETRI (AREA, CODICE_MODELLO, CONSERVAZIONE, TIPO, SEQUENZA, CHIAVE)
	 VALUES
	('SEGRETERIA.ATTI.2_0',
	 'DETERMINA',
	 D_CONSERVATORE,
	 'ProfiloUnitaDocumentaria',
	 '2',
	 'Data');

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = 'DATA_NUMERO_DETERMINA' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'ProfiloUnitaDocumentaria' 
	AND SEQUENZA = '2' 
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
	VALORE = VALORE||'SELECT distinct

	''AmministrazioneCheEffettuaLaRegistrazione/TipoRuolo'' att01,
	''Amministrazione Che Effettua La Registrazione'' val01,
	''AmministrazioneCheEffettuaLaRegistrazione/PAI/IPAAmm/Denominazione'' att02,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AMM'')  val02,
	''AmministrazioneCheEffettuaLaRegistrazione/PAI/IPAAmm/CodiceIPA'' att03,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AMM'')  val03,
	''AmministrazioneCheEffettuaLaRegistrazione/PAI/IPAAOO/Denominazione'' att04,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AOO'')  val04,
	''AmministrazioneCheEffettuaLaRegistrazione/PAI/IPAAOO/CodiceIPA'' att05,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AOO'')  val05,
	''AmministrazioneCheEffettuaLaRegistrazione/PAI/IndirizziDigitaliDiRiferimento'' att06,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''INDIRIZZO_DIGITALE'')  val06,
	NULL att07,
	NULL val07,
	NULL att08,
	NULL val08,
	NULL att09,
	NULL val09,
	NULL att10,
	NULL val10,
	NULL att11,
	NULL val11
	from agsde2_cons_determine
	where id_determina = '':ID_DOCUMENTO_GRAILS''

	UNION

	SELECT DISTINCT
	''Autore/TipoRuolo'' att01,
	''Autore'' val01,
	''Autore/PF/Cognome'' att02,
	nvl(cognome_firmatario,''Non Applicabile'')  val02,
	''Autore/PF/Nome'' att03,
	nvl(nome_firmatario,''Non Applicabile'') val03,
	NULL att04,
	NULL val04,
	NULL att05,
	NULL val05,
	NULL att06,
	NULL val06,
	NULL att07,
	NULL val07,
	NULL att08,
	NULL val08,
	NULL att09,
	NULL val09,
	NULL att10,
	NULL val10,
	NULL att11,
	NULL val11
	from agsde2_cons_determine
	where id_determina = '':ID_DOCUMENTO_GRAILS''

	UNION

	SELECT DISTINCT
	''Assegnatario/TipoRuolo'' att01,
	''Assegnatario'' val01,
	''Assegnatario/AS/IPAAmm/Denominazione'' att02,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AMM'')  val02,
	''Assegnatario/AS/IPAAmm/CodiceIPA'' att03,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AMM'') val03,
	''Assegnatario/AS/IPAAOO/Denominazione'' att04,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AOO'') val04,
	''Assegnatario/AS/IPAAOO/CodiceIPA''  att05,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AOO'')  val05,
	''Assegnatario/AS/IPAUOR/Denominazione''  att06,
	unita_proponente  val06,
	''Assegnatario/AS/IPAUOR/CodiceIPA''  att07,
	''Non Applicabile''  val07,
	''Assegnatario/AS/IndirizziDigitaliDiRiferimento''  ' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'Soggetti2022' 
	AND SEQUENZA = '1' 
	AND CONSERVAZIONE = D_CONSERVATORE; 

	UPDATE CONSERVAZIONI_PARAMETRI SET 
	VALORE = VALORE||'att08,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''INDIRIZZO_DIGITALE'')   val08,
	NULL att09,
	NULL val09,
	NULL att10,
	NULL val10,
	NULL att11,
	NULL val11
	from agsde2_cons_determine
	where id_determina = '':ID_DOCUMENTO_GRAILS''

	UNION

	SELECT DISTINCT
	''RUP/TipoRuolo'' att01,
	''RUP'' val01,
	''RUP/RUP/Cognome'' att02,
	nvl(cognome_funzionario,''Non Applicabile'')  val02,
	''RUP/RUP/Nome'' att03,
	nvl(nome_funzionario,''Non Applicabile'') val03,
	''RUP/RUP/CodiceFiscale''  att04,
	nvl(cf_funzionario,''Non Applicabile'') val04,
	''RUP/RUP/IPAAmm/Denominazione'' att05,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AMM'')  val05,
	''RUP/RUP/IPAAmm/CodiceIPA'' att06,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AMM'')   val06,
	''RUP/RUP/IPAAOO/Denominazione'' att07,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''DESCRIZIONE_AOO'')  val07,
	''RUP/RUP/IPAAOO/CodiceIPA'' att08,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''CODICE_IPA_AOO'')  val08,
	''RUP/RUP/IPAUOR/Denominazione'' att09,
	unita_proponente  val09,
	''RUP/RUP/IPAUOR/CodiceIPA'' att10,
	''Non Applicabile'' val10,
	''RUP/RUP/IndirizziDigitaliDiRiferimento'' att11,
	(select to_char(valore) from conservazioni_parametri where conservazione=''PARER2'' and tipo = ''Parametri'' and chiave = ''INDIRIZZO_DIGITALE'')  val11
	from agsde2_cons_determine
	where id_determina = '':ID_DOCUMENTO_GRAILS''
	and presenza_funzionario=''Y''
	' 
	WHERE AREA = 'SEGRETERIA.ATTI.2_0' 
	AND CODICE_MODELLO = 'DETERMINA' 
	AND TIPO = 'Soggetti2022' 
	AND SEQUENZA = '1' 
	AND CONSERVAZIONE = D_CONSERVATORE; 


	COMMIT;

END IF;

END;
/