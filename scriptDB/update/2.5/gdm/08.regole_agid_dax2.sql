--liquibase formatted sql
--changeset svalenti:2.5_20240213_01.08.regole_agid_dax2 runOnChange:true failOnError:false

DECLARE
d_esiste                  NUMBER;
    d_esiste_old              NUMBER;
    d_esiste_conservazione    NUMBER;
    D_CONSERVATORE            VARCHAR2(200);
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
  AND CONSERVAZIONE = 'DAX2';
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
  AND CONSERVAZIONE = 'DAX2_OLD';
EXCEPTION
          WHEN NO_DATA_FOUND
          THEN
             d_esiste_old := 0;
END;

 IF d_esiste = 0
    THEN
        D_CONSERVATORE := 'DAX2';
    ELSIF d_esiste_old = 0
    THEN
        D_CONSERVATORE := 'DAX2_2022';
ELSE
        D_CONSERVATORE := 'DAX2';
END IF;

    -- cancello se presente TRUST_2022
    DELETE CONSERVAZIONI
    WHERE AREA = 'SEGRETERIA.ATTI.2_0'
    AND CODICE_MODELLO = 'DELIBERA'
    AND CONSERVAZIONE = 'DAX2_2022';

    DELETE CONSERVAZIONI_PARAMETRI
    WHERE AREA = 'SEGRETERIA.ATTI.2_0'
    AND CODICE_MODELLO = 'DELIBERA'
    AND CONSERVAZIONE = 'DAX2_2022';

    -- cancello se già è stato fatto lo switch per aggiornare le regole
IF D_CONSERVATORE = 'DAX2' THEN
        DELETE CONSERVAZIONI
        WHERE AREA = 'SEGRETERIA.ATTI.2_0'
        AND CODICE_MODELLO = 'DELIBERA'
        AND CONSERVAZIONE = 'DAX2';

        DELETE CONSERVAZIONI_PARAMETRI
        WHERE AREA = 'SEGRETERIA.ATTI.2_0'
        AND CODICE_MODELLO = 'DELIBERA'
        AND CONSERVAZIONE = 'DAX2';
END IF;




INSERT INTO CONSERVAZIONI (AREA,
                           CODICE_MODELLO,
                           CONSERVAZIONE,
                           TIPO_DOCUMENTO,
                           DESCRIZIONE)
VALUES ('SEGRETERIA.ATTI.2_0',
        'DELIBERA',
        D_CONSERVATORE,
        'Delibera',
        'Delibera :NUMERO_DELIBERA/:ANNO_DELIBERA :ID_REGISTRO_DELIBERA');

INSERT INTO CONSERVAZIONI_PARAMETRI (AREA,
                                     CODICE_MODELLO,
                                     CONSERVAZIONE,
                                     TIPO,
                                     SEQUENZA,
                                     CHIAVE)
VALUES ('SEGRETERIA.ATTI.2_0',
        'DELIBERA',
        D_CONSERVATORE,
        'Allegati',
        '1',
        'SELECT');

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = 'SELECT id_documento_esterno id_documento,
       filename,
       filename filenameALL,
       filename chiave_allegato,
       ''Contenuti/Item/SDStdInfo/+/DigDocs.Id'',
       rownum || ''_'' || id_documento_esterno || ''_'' || TO_CHAR (SYSDATE, ''yyyymmddhh24mi''),
       ''Contenuti/Item/SDStdInfo/DigDocs.RelVsSD'',
       ''ALL'',
       ''Contenuti/Item/SDStdInfo/DigDocs.NroAllegato'',
       ''#NUMEROALLEGATO#'',
       ''Contenuti/Item/SDStdInfo/DigDocs.VersioneDelDocumento'',
       ''1.0'',
       ''Contenuti/Item/SDStdInfo/DigDocs/SupportoFisico/Codice'',
       ''D'',
       ''Contenuti/Item/SDStdInfo/DigDocs/Tipo/Intestazione'',
       ''Allegato 2022'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ModalitaDiFormazione'',
       ''creazione tramite utilizzo di strumenti software che assicurino la produzione di documenti nei formati previsti in allegato 2'',
       ''Contenuti/Item/SDStdInfo/DigDocs/Riservato'',
       ''true'',
       ''Contenuti/Item/SDStdInfo/DigDocs/DesOgg'',
       titolo,
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Path'',
       filename,
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Hash.func'',
       ''SHA-256'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Hash.encoding'',
       ''hex'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Hash'',
       ''#HASHALLEGATO#'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/IdentificativoDelFormato/Formato'',
       ''#FORMATO#'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/FirmatoDigitalmente'',
       ''#FIRMATO#'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/SigillatoElettronicamente'',
       ''false'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/MarcaturaTemporale'',
       ''false'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/ConformitaCopieImmagineSuSupportoInformatico'',
       ''false''
  FROM agsde2_cons_delibere_allegati alle
 WHERE alle.id_delibera = '':ID_DOCUMENTO_GRAILS'''
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DELIBERA'
  AND TIPO = 'Allegati'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

INSERT INTO CONSERVAZIONI_PARAMETRI (AREA,
                                     CODICE_MODELLO,
                                     CONSERVAZIONE,
                                     TIPO,
                                     SEQUENZA,
                                     CHIAVE)
VALUES ('SEGRETERIA.ATTI.2_0',
        'DELIBERA',
        D_CONSERVATORE,
        'DocumentoPrincipale',
        '1',
        'SELECT');

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = VALORE||'SELECT ''2.0'' sipver,
       ''Id'',
       id_documento_esterno id_documento,
       ''Nome_Documento'',
       filename nome_documento,
       ''SelfDescription.Id'',
       id_documento_esterno || ''_'' || TO_CHAR (SYSDATE, ''yyyymmddhh24mi''),
       ''SelfDescription/Produttore.CodiceAmministrazione'',
       ''#CODICEAMMINISTRAZIONE#'',
       ''SelfDescription/Produttore/Tipo'',
       ''PA'',
       ''SelfDescription/Produttore/Intestazione'',
       ''#INTESTAZIONE#'',
       ''SelfDescription/CreatoDaApplicazione.nome'',
       ''#CREATODAAPPLICAZIONE#'',
       ''SelfDescription/CreatoDaApplicazione.ver'',
       ''1.0'',
       ''SelfDescription/CreatoDaApplicazione/Descrizione.lang'',
       ''it'',
       ''SelfDescription/CreatoDaApplicazione/Descrizione'',
       ''FINMATICA'',
       ''SelfDescription/TsGenerazione'',
       TO_CHAR (SYSDATE, ''YYYY-MM-DD"T"HH24::mi::ss"Z"''),
       ''SelfDescription/Label'',
       ''ID_DELIBERA: '' || ID_DELIBERA,
       ''Contenuti'',
       '''',
       ''Contenuti/Item.type'',
       ''UA'',
       ''Contenuti/Item.Id'',
          ''DELIBERA_''
       || id_documento_esterno
       || ''_''
       ||''_''||(select id_transazione from gdm_t_log_conservazione where id_documento_rif = id_documento_esterno),
       ''Contenuti/Item/UAStdInfo/Tipo.Cat'',
       ''UAR'',
       ''Contenuti/Item/UAStdInfo/Tipo/Intestazione'',
       ''FASCICOLO'',
       ''Contenuti/Item/UAStdInfo/Progressivo'',
       ''1'',
       ''Contenuti/Item/UAStdInfo/Assegnazione/TipoAssegnazione/PerCompetenza/TipoAssegnazioneRuolo'',
       ''Per Competenza'',
       ''Contenuti/Item/UAStdInfo/Assegnazione/TipoAssegnazione/PerCompetenza/SoggettoAssegnatario/DenominazioneOrganizzazione'',
       ''#DENOMINAZIONE_ENTE#'',
       ''Contenuti/Item/UAStdInfo/Assegnazione/TipoAssegnazione/PerCompetenza/SoggettoAssegnatario/DenominazioneUfficio'',
       ''#DENOMINAZIONE_UFFICIO#'',
       ''Contenuti/Item/UAStdInfo/Assegnazione/TipoAssegnazione/PerCompetenza/DataInizioAssegnazione'',
       TO_CHAR (TO_DATE (data_numero_delibera, ''dd/mm/RRRR hh24::mi::ss''),
                ''YYYY-MM-DD''),
       ''Contenuti/Item/UAStdInfo/Soggetti/Ruolo/AmministrazioneTitolare/TipoRuolo'',
       ''Amministrazione Titolare'',
       ''Contenuti/Item/UAStdInfo/Soggetti/Ruolo/AmministrazioneTitolare/PAI/IPAAmm/Denominazione'',
       ''#DENOMINAZIONE_ENTE#'',
       ''Contenuti/Item/UAStdInfo/Soggetti/Ruolo/AmministrazioneTitolare/PAI/IPAAmm/CodiceIPA'',
       ''#CODICE_AMMINISTRAZIONE#'',
       ''Contenuti/Item/UAStdInfo/Soggetti/Ruolo/AmministrazioneTitolare/PAI/IPAAOO/Denominazione'',
       ''#DESCRIZIONE_AOO#'',
       ''Contenuti/Item/UAStdInfo/Soggetti/Ruolo/AmministrazioneTitolare/PAI/IPAAOO/CodiceIPA'',
       ''#CODICE_IPA_AOO#'',
       ''Contenuti/Item/UAStdInfo/Soggetti/Ruolo/AmministrazioneTitolare/PAI/IndirizziDigitaliDiRiferimento'',
       ''#INDIRIZZO_DIGITALE#'',
       ''Contenuti/Item/UAStdInfo/Intitolazione'',
       SUBSTR (OGGETTO, 0, 249),
       ''Contenuti/Item/UAStdInf'
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DELIBERA'
  AND TIPO = 'DocumentoPrincipale'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = VALORE||'o/Identificativo/FormaNativa'',
       SUBSTR (OGGETTO, 0, 140) || '' '' || ID_DELIBERA,
       ''Contenuti/Item/UAStdInfo/Identificativo/ConvertitaXOrdinamento'',
       ''DEL_'' || ID_DELIBERA,
       ''Contenuti/Item/UAStdInfo/DataApertura'',
       TO_CHAR (TO_DATE (data_numero_delibera, ''dd/mm/RRRR hh24::mi::ss''),
                ''YYYY-MM-DD''),
       ''Contenuti/Item/UAStdInfo/PosizioneFisicaAggregazioneDocumentale'',
       ''Non disponibile'',
       ''Contenuti/Item/UAStdInfo/Conservazione/ConservazionePerAnni'',
       ''20'',
       ''Contenuti/+/Item.type'',
       ''SD'',
       ''Contenuti/Item.Id'',
       id_documento_esterno || ''_'' || TO_CHAR (SYSDATE, ''yyyymmddhh24mi''),
       ''Contenuti/Item/SDStdInfo/Identificativo/FormaNativa'',
       id_documento_esterno || ''_'' || TO_CHAR (SYSDATE, ''yyyymmddhh24mi''),
       ''Contenuti/Item/SDStdInfo/Identificativo/ConvertitaXOrdinamento'',
       ''A0000001'',
       ''Contenuti/Item/SDStdInfo/Soggetti/Ruolo/Autore/TipoRuolo'',
       ''Autore'',
       ''Contenuti/Item/SDStdInfo/Soggetti/Ruolo/Autore/PF/Cognome'',
       NVL (cognome_segretario, ''Non Applicabile''),
       ''Contenuti/Item/SDStdInfo/Soggetti/Ruolo/Autore/PF/Nome'',
       NVL (nome_segretario, ''Non Applicabile''),
       ''Contenuti/Item/SDStdInfo/RegistrazioniNumerazioni/Registrazione/TipologiaDiFlusso'',
       ''I'',
       ''Contenuti/Item/SDStdInfo/RegistrazioniNumerazioni/Registrazione/CategoriaReg'',
       ''repertorio'',
       ''Contenuti/Item/SDStdInfo/RegistrazioniNumerazioni/Registrazione/SiglaRegistro'',
       REGISTRO_DELIBERA,
       ''Contenuti/Item/SDStdInfo/RegistrazioniNumerazioni/Registrazione/AnnoReg'',
       ANNO_DELIBERA,
       ''Contenuti/Item/SDStdInfo/RegistrazioniNumerazioni/Registrazione/NroReg'',
       NUMERO_DELIBERA,
       ''Contenuti/Item/SDStdInfo/DesOgg'',
       SUBSTR (OGGETTO, 0, 249) oggetto,
       ''Contenuti/Item/SDStdInfo/DigDocs.RelVsSD'',
       ''P'',
       ''Contenuti/Item/SDStdInfo/DigDocs.Id'',
       id_documento_esterno || ''_'' || TO_CHAR (SYSDATE, ''yyyymmddhh24mi''),
       ''Contenuti/Item/SDStdInfo/DigDocs.NumeroAllegati'',
       ''0'',
       ''Contenuti/Item/SDStdInfo/DigDocs.VersioneDelDocumento'',
       ''1.0'',
       ''Contenuti/Item/SDStdInfo/DigDocs/SupportoFisico/Codice'',
       ''D'',
       ''Contenuti/Item/SDStdInfo/DigDocs/Tipo/Intestazione'',
       ''Delibera 2022'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ModalitaDiFormazione'',
       ''creazione tramite utilizzo di strumenti software che assicurino la produzione di documenti nei formati previsti in allegato 2'',
       ''Contenuti/Item/SDStdInfo/DigDocs/Riservato'',
       ''true'',
       ''Contenuti/Item/SDStdInfo/DigDocs/DesOgg'',
       SUBSTR (OGGETTO, 0, 249),
       DECODE (
          FUNZIONARIO,
          NULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo''),
       DECODE (FUNZIONARIO, NULL, ''###'', ''DET_AUTORE''),
       DECODE (
          FUNZIONARIO,
          NULL, ''#'
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DELIBERA'
  AND TIPO = 'DocumentoPrincipale'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = VALORE||'##'',
          ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice''),
       DECODE (FUNZIONARIO, NULL, ''###'', FUNZIONARIO),
       ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo'',
       ''DET_STRUTTURA_PROPONENTE'',
       ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice'',
       UNITA_PROPONENTE,
       ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo'',
       ''DET_NRO_PROP'',
       ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice'',
       NUMERO_PROPOSTA,
       ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo'',
       ''DET_DT_PROP'',
       ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice'',
       TO_CHAR (NVL(DATA_NUMERO_PROPOSTA,data_numero_delibera), ''yyyy-mm-dd''),
       ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo'',
       ''DET_DTA_PAR_REG_TEC'',
       ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice'',
        nvl((SELECT to_char(data_firma,''yyyy-mm-dd'')
          FROM AGSDE2_CONS_DELIBERE_ALLEGATI
         WHERE     TIPO_OGGETTO = ''PARERE''
               AND CODICE = ''PTECN''
               AND id_delibera = '':ID_DOCUMENTO_GRAILS'' and rownum=1),to_char(data_numero_delibera,''yyyy-mm-dd'')),
       ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo'',
       ''DET_TIPO_VISTO_REG_CONT'',
       ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice'',
       DECODE (ESITO_PARERE_CONTABILE, ''FAVOREVOLE'', ''Positivo'', ''Negativo''),
       ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo'',
       ''DET_AUTORE_VISTO_REG_CONT'',
       ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice'',
       FIRMATARIO_PARERE_CONTABILE,
       DECODE (
          DATA_FIRMA_PARERE_CONTABILE,
          NULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo''),
       DECODE (DATA_FIRMA_PARERE_CONTABILE,
               NULL, ''###'',
               ''DET_DATA_VISTO_REG_CONT''),
       DECODE (
          DATA_FIRMA_PARERE_CONTABILE,
          NULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice''),
       DECODE (DATA_FIRMA_PARERE_CONTABILE,
               NULL, ''###'',
               TO_CHAR (DATA_FIRMA_PARERE_CONTABILE, ''yyyy-mm-dd'')),
       DECODE (
          DATA_ESECUTIVITA,
          NULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo''),
       ''DET_DATA_ESECUTIVITA'',
       DECODE (
          DATA_ESECUTIVITA,
          NULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice''),
       TO_CHAR (DATA_ESECUTIVITA, ''yyyy-mm-dd''),
       DECODE (
          DATA_PUBBLICAZIONE,
          NULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo''),
       ''DET_DT_INIZIO_PUBB'',
       DECODE (
          DATA_PUBBLICAZIONE,
          N'
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DELIBERA'
  AND TIPO = 'DocumentoPrincipale'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = VALORE||'ULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice''),
       TO_CHAR (DATA_PUBBLICAZIONE, ''yyyy-mm-dd''),
       DECODE (
          DATA_FINE_PUBBLICAZIONE,
          NULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo''),
       ''DET_DT_FINE_PUBB'',
       DECODE (
          DATA_FINE_PUBBLICAZIONE,
          NULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice''),
       TO_CHAR (DATA_FINE_PUBBLICAZIONE, ''yyyy-mm-dd''),
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Path'',
       filename,
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Hash.func'',
       ''SHA-256'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Hash.encoding'',
       ''hex'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Hash'',
       ''#HASHPRINCIPALE#'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/IdentificativoDelFormato/Formato'',
       ''#FORMATO#'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/FirmatoDigitalmente'',
       ''#FIRMATO#'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/SigillatoElettronicamente'',
       ''false'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/MarcaturaTemporale'',
       ''false'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/ConformitaCopieImmagineSuSupportoInformatico'',
       ''false''
  FROM agsde2_cons_delibere
 WHERE id_delibera = '':ID_DOCUMENTO_GRAILS'''
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
  AND CONSERVAZIONE = 'DAX2';
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
  AND CONSERVAZIONE = 'DAX2_OLD';
EXCEPTION
              WHEN NO_DATA_FOUND
              THEN
                 d_esiste_old := 0;
END;

        IF d_esiste = 0
        THEN
            D_CONSERVATORE := 'DAX2';
        ELSIF d_esiste_old = 0
        THEN
            D_CONSERVATORE := 'DAX2_2022';
ELSE
            D_CONSERVATORE := 'DAX2';
END IF;

        -- cancello se presente TRUST_2022
        DELETE CONSERVAZIONI
        WHERE AREA = 'SEGRETERIA.ATTI.2_0'
        AND CODICE_MODELLO = 'DETERMINA'
        AND CONSERVAZIONE = 'DAX2_2022';

        DELETE CONSERVAZIONI_PARAMETRI
        WHERE AREA = 'SEGRETERIA.ATTI.2_0'
        AND CODICE_MODELLO = 'DETERMINA'
        AND CONSERVAZIONE = 'DAX2_2022';

        -- cancello se già è stato fatto lo switch per aggiornare le regole
        IF D_CONSERVATORE = 'DAX2' THEN
            DELETE CONSERVAZIONI
            WHERE AREA = 'SEGRETERIA.ATTI.2_0'
            AND CODICE_MODELLO = 'DETERMINA'
            AND CONSERVAZIONE = 'DAX2';

            DELETE CONSERVAZIONI_PARAMETRI
            WHERE AREA = 'SEGRETERIA.ATTI.2_0'
            AND CODICE_MODELLO = 'DETERMINA'
            AND CONSERVAZIONE = 'DAX2';
END IF;


INSERT INTO CONSERVAZIONI (AREA,
                           CODICE_MODELLO,
                           CONSERVAZIONE,
                           TIPO_DOCUMENTO,
                           DESCRIZIONE)
VALUES (
           'SEGRETERIA.ATTI.2_0',
           'DETERMINA',
           D_CONSERVATORE,
           'Determina',
           'Determina :NUMERO_DETERMINA/:ANNO_DETERMINA :ID_REGISTRO_DETERMINA');

INSERT INTO CONSERVAZIONI_PARAMETRI (AREA,
                                     CODICE_MODELLO,
                                     CONSERVAZIONE,
                                     TIPO,
                                     SEQUENZA,
                                     CHIAVE)
VALUES ('SEGRETERIA.ATTI.2_0',
        'DETERMINA',
        D_CONSERVATORE,
        'Allegati',
        '1',
        'SELECT');

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = 'SELECT id_documento_esterno id_documento,
       filename,
       filename filenameALL,
       filename chiave_allegato,
       ''Contenuti/Item/SDStdInfo/+/DigDocs.Id'',
       rownum || ''_'' || id_documento_esterno,
       ''Contenuti/Item/SDStdInfo/DigDocs.RelVsSD'',
       ''ALL'',
       ''Contenuti/Item/SDStdInfo/DigDocs.NroAllegato'',
       ''#NUMEROALLEGATO#'',
       ''Contenuti/Item/SDStdInfo/DigDocs.VersioneDelDocumento'',
       ''1.0'',
       ''Contenuti/Item/SDStdInfo/DigDocs/SupportoFisico/Codice'',
       ''D'',
       ''Contenuti/Item/SDStdInfo/DigDocs/Tipo/Intestazione'',
       ''Allegato 2022'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ModalitaDiFormazione'',
       ''creazione tramite utilizzo di strumenti software che assicurino la produzione di documenti nei formati previsti in allegato 2'',
       ''Contenuti/Item/SDStdInfo/DigDocs/Riservato'',
       ''true'',
       ''Contenuti/Item/SDStdInfo/DigDocs/DesOgg'',
       titolo,
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Path'',
       filename,
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Hash.func'',
       ''SHA-256'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Hash.encoding'',
       ''hex'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Hash'',
       ''#HASHALLEGATO#'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/IdentificativoDelFormato/Formato'',
       ''#FORMATO#'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/FirmatoDigitalmente'',
       ''#FIRMATO#'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/SigillatoElettronicamente'',
       ''false'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/MarcaturaTemporale'',
       ''false'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/ConformitaCopieImmagineSuSupportoInformatico'',
       ''false''
  FROM agsde2_cons_determine_allegati alle
 WHERE alle.id_determina = '':ID_DOCUMENTO_GRAILS'''
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DETERMINA'
  AND TIPO = 'Allegati'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

INSERT INTO CONSERVAZIONI_PARAMETRI (AREA,
                                     CODICE_MODELLO,
                                     CONSERVAZIONE,
                                     TIPO,
                                     SEQUENZA,
                                     CHIAVE)
VALUES ('SEGRETERIA.ATTI.2_0',
        'DETERMINA',
        D_CONSERVATORE,
        'DocumentoPrincipale',
        '1',
        'SELECT');

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = VALORE||'SELECT  ''2.0'' sipver,
       ''Id'',
       id_documento_esterno id_documento,
       ''Nome_Documento'',
       filename nome_documento,
       ''SelfDescription.Id'',
       id_documento_esterno || ''_'' || TO_CHAR (SYSDATE, ''yyyymmddhh24mi''),
       ''SelfDescription/Produttore.CodiceAmministrazione'',
       ''#CODICEAMMINISTRAZIONE#'',
       ''SelfDescription/Produttore/Tipo'',
       ''PA'',
       ''SelfDescription/Produttore/Intestazione'',
       ''#INTESTAZIONE#'',
       ''SelfDescription/CreatoDaApplicazione.nome'',
       ''#CREATODAAPPLICAZIONE#'',
       ''SelfDescription/CreatoDaApplicazione.ver'',
       ''1.0'',
       ''SelfDescription/CreatoDaApplicazione/Descrizione.lang'',
       ''it'',
       ''SelfDescription/CreatoDaApplicazione/Descrizione'',
       ''FINMATICA'',
       ''SelfDescription/TsGenerazione'',
       TO_CHAR (sysdate,
                ''YYYY-MM-DD"T"HH24::mi::ss"Z"''),
       ''SelfDescription/Label'',
       ''ID_DOCUMENTO: '' || id_documento_esterno,
       ''Contenuti'',
       '''',
       ''Contenuti/Item.type'',
       ''UA'',
       ''Contenuti/Item.Id'',
       ''DETERMINA_'' || id_documento_esterno ||''_''||(select id_transazione from gdm_t_log_conservazione where id_documento_rif = id_documento_esterno),
       ''Contenuti/Item/UAStdInfo/Tipo.Cat'',
       ''UAR'',
       ''Contenuti/Item/UAStdInfo/Tipo/Intestazione'',
       ''FASCICOLO'',

        ''Contenuti/Item/UAStdInfo/Progressivo'',
       ''1'',
       ''Contenuti/Item/UAStdInfo/Assegnazione/TipoAssegnazione/PerCompetenza/TipoAssegnazioneRuolo'',
       ''Per Competenza'',
       ''Contenuti/Item/UAStdInfo/Assegnazione/TipoAssegnazione/PerCompetenza/SoggettoAssegnatario/DenominazioneOrganizzazione'',
       ''#DENOMINAZIONE_ENTE#'',
       ''Contenuti/Item/UAStdInfo/Assegnazione/TipoAssegnazione/PerCompetenza/SoggettoAssegnatario/DenominazioneUfficio'',
       ''#DENOMINAZIONE_UFFICIO#'',
       ''Contenuti/Item/UAStdInfo/Assegnazione/TipoAssegnazione/PerCompetenza/DataInizioAssegnazione'',
       TO_CHAR (TO_DATE (data_numero_Determina, ''dd/mm/RRRR hh24::mi::ss''),
                ''YYYY-MM-DD''),
       ''Contenuti/Item/UAStdInfo/Soggetti/Ruolo/AmministrazioneTitolare/TipoRuolo'',
       ''Amministrazione Titolare'',
       ''Contenuti/Item/UAStdInfo/Soggetti/Ruolo/AmministrazioneTitolare/PAI/IPAAmm/Denominazione'',
       ''#DENOMINAZIONE_ENTE#'',
       ''Contenuti/Item/UAStdInfo/Soggetti/Ruolo/AmministrazioneTitolare/PAI/IPAAmm/CodiceIPA'',
       ''#CODICE_AMMINISTRAZIONE#'',
       ''Contenuti/Item/UAStdInfo/Soggetti/Ruolo/AmministrazioneTitolare/PAI/IPAAOO/Denominazione'',
       ''#DESCRIZIONE_AOO#'',
       ''Contenuti/Item/UAStdInfo/Soggetti/Ruolo/AmministrazioneTitolare/PAI/IPAAOO/CodiceIPA'',
       ''#CODICE_IPA_AOO#'',
       ''Contenuti/Item/UAStdInfo/Soggetti/Ruolo/AmministrazioneTitolare/PAI/IndirizziDigitaliDiRiferimento'',
       ''#INDIRIZZO_DIGITALE#'',



       ''Contenuti/Item/UAStdInfo/Intitolazione'',
       DECODE (impegno_spesa,
      '
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DETERMINA'
  AND TIPO = 'DocumentoPrincipale'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = VALORE||'         ''Y'', ''Determina con impegno di spesa'',
               ''Determina senza impegno di spesa''),
       ''Contenuti/Item/UAStdInfo/Identificativo/FormaNativa'',
       '''',
       ''Contenuti/Item/UAStdInfo/Identificativo/ConvertitaXOrdinamento'',
       '''',
       ''Contenuti/Item/UAStdInfo/DataApertura'',
       TO_CHAR (TO_DATE (data_numero_Determina, ''dd/mm/RRRR hh24::mi::ss''),
                ''YYYY-MM-DD''),
       ''Contenuti/Item/UAStdInfo/PosizioneFisicaAggregazioneDocumentale'',
       ''Non disponibile'',
       ''Contenuti/Item/UAStdInfo/Conservazione/ConservazionePerAnni'',
       ''20'',

       ''Contenuti/+/Item.type'',
       ''SD'',
       ''Contenuti/Item.Id'',
       id_documento_esterno || ''_'' || TO_CHAR (SYSDATE, ''yyyymmddhh24mi''),
       ''Contenuti/Item/SDStdInfo/Identificativo/FormaNativa'',
       id_documento_esterno || ''_'' || TO_CHAR (SYSDATE, ''yyyymmddhh24mi''),
       ''Contenuti/Item/SDStdInfo/Identificativo/ConvertitaXOrdinamento'',
       ''A0000001'',


       ''Contenuti/Item/SDStdInfo/Soggetti/Ruolo/Autore/TipoRuolo'',
       ''Autore'',
       ''Contenuti/Item/SDStdInfo/Soggetti/Ruolo/Autore/PF/Cognome'',
       NVL (cognome_firmatario, ''Non Applicabile''),
       ''Contenuti/Item/SDStdInfo/Soggetti/Ruolo/Autore/PF/Nome'',
       NVL (nome_firmatario, ''Non Applicabile''),
       ''Contenuti/Item/SDStdInfo/RegistrazioniNumerazioni/Registrazione/TipologiaDiFlusso'',
       ''I'',

       ''Contenuti/Item/SDStdInfo/RegistrazioniNumerazioni/Registrazione/CategoriaReg'',
       ''repertorio'',
       ''Contenuti/Item/SDStdInfo/RegistrazioniNumerazioni/Registrazione/SiglaRegistro'',
       REGISTRO_Determina,
       ''Contenuti/Item/SDStdInfo/RegistrazioniNumerazioni/Registrazione/AnnoReg'',
       ANNO_Determina,
       ''Contenuti/Item/SDStdInfo/RegistrazioniNumerazioni/Registrazione/NroReg'',
       NUMERO_Determina,

       ''Contenuti/Item/SDStdInfo/DesOgg.lang'',
       ''it'',
       ''Contenuti/Item/SDStdInfo/DesOgg'',
       Oggetto oggetto,
       ''Contenuti/Item/SDStdInfo/DigDocs.RelVsSD'',
       ''P'',
       ''Contenuti/Item/SDStdInfo/DigDocs.Id'',
       id_documento_esterno,
       ''Contenuti/Item/SDStdInfo/DigDocs.RelVsSD'',
       ''P'',
       ''Contenuti/Item/SDStdInfo/DigDocs.NumeroAllegati'',
       ''0'',
       ''Contenuti/Item/SDStdInfo/DigDocs.VersioneDelDocumento'',
       ''1.0'',

       ''Contenuti/Item/SDStdInfo/DigDocs/SupportoFisico/Codice'',
       ''D'',
       ''Contenuti/Item/SDStdInfo/DigDocs/Tipo/Intestazione'',
       decode(REGISTRO_Determina,''ORDI'',''Ordinanza 2022'',DECODE (impegno_spesa,
               ''Y'', ''Determina con impegno di spesa 2022'',
               ''Determina senza impegno di spesa 2022'')),
       ''Contenuti/Item/SDStdInfo/DigDocs/ModalitaDiFormazione'',
       ''creazione tramite utilizzo di strumenti software che assicurino la produzione di documenti nei formati previsti in allegato 2'',
       ''Contenuti/Item/SDStdInfo/DigDocs/Riservato'',
  '
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DETERMINA'
  AND TIPO = 'DocumentoPrincipale'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = VALORE||'     ''true'',
       ''Contenuti/Item/SDStdInfo/DigDocs/DesOgg'',
       Oggetto,
       ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo'',
       ''DET_NRO_FASCICOLO'',
       ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice'',
       FASCICOLO_NUMERO,
       ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo'',
       ''DET_STRUTTURA_PROPONENTE'',
       ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice'',
       UNITA_PROPONENTE,
       ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo'',
       ''DET_FIRMATARIO'',
       ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice'',
       FIRMATARIO,
       ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo'',
       ''DET_FLG_VISTO_REG_CONT'',
       ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice'',
       DECODE (IMPEGNO_SPESA, NULL, ''N'', ''S''),
       DECODE (
          DATA_FIRMA_VISTO_CONTABILE,
          NULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo''),
       ''DET_DATA_VISTO_REG_CONT'',
       DECODE (
          DATA_FIRMA_VISTO_CONTABILE,
          NULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice''),
       TO_CHAR (DATA_FIRMA_VISTO_CONTABILE, ''yyyy-mm-dd''),
       ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo'',
       ''DET_TIPO_VISTO_REG_CONT'',
       ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice'',
       DECODE (ESITO_VISTO_CONTABILE,
               ''FAVOREVOLE'', ''F'',
               ''CONTRARIO'', ''C'',
               NULL),
       ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo'',
       ''DET_DATA_ESECUTIVITA'',
       ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice'',
       TO_CHAR (
          NVL (DATA_ESECUTIVITA,
               NVL (DATA_FIRMA_VISTO_CONTABILE, DATA_FIRMA)),
          ''yyyy-mm-dd''),
       DECODE (
          DATA_PUBBLICAZIONE,
          NULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo''),
       ''DET_DT_INIZIO_PUBB'',
       DECODE (
          DATA_PUBBLICAZIONE,
          NULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice''),
       TO_CHAR (DATA_PUBBLICAZIONE, ''yyyy-mm-dd''),
       DECODE (
          DATA_FINE_PUBBLICAZIONE,
          NULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/+/AttributoCustom.NomeAttributo''),
       ''DET_DT_FINE_PUBB'',
       DECODE (
          DATA_FINE_PUBBLICAZIONE,
          NULL, ''###'',
          ''Contenuti/Item/SDStdInfo/DigDocs/AttributoCustom/+/ValoreSemplice''),
       TO_CHAR (DATA_FINE_PUBBLICAZIONE, ''yyyy-mm-dd''),
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Path'',
       filename,
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Hash.func'',
       ''SHA-256'',
     '
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DETERMINA'
  AND TIPO = 'DocumentoPrincipale'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;

UPDATE CONSERVAZIONI_PARAMETRI SET
    VALORE = VALORE||'  ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Hash.encoding'',
       ''hex'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Hash'',
       ''#HASHPRINCIPALE#'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/IdentificativoDelFormato/Formato'',
       ''#FORMATO#'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/FirmatoDigitalmente'',
       ''#FIRMATO#'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/SigillatoElettronicamente'',
       ''false'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/MarcaturaTemporale'',
       ''false'',
       ''Contenuti/Item/SDStdInfo/DigDocs/ComponentiDigitali/FilePrincipale/Verifica/ConformitaCopieImmagineSuSupportoInformatico'',
       ''false''
  FROM agsde2_cons_determine
 WHERE id_Determina = '':ID_DOCUMENTO_GRAILS''
'
WHERE AREA = 'SEGRETERIA.ATTI.2_0'
  AND CODICE_MODELLO = 'DETERMINA'
  AND TIPO = 'DocumentoPrincipale'
  AND SEQUENZA = '1'
  AND CONSERVAZIONE = D_CONSERVATORE;


END IF;

END;
/