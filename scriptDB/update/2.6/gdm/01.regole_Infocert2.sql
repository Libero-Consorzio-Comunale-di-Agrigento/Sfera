--liquibase formatted sql
--changeset mferrara:2.6_20260127_01.regole_infocert2 runOnChange:true failOnError:false

--gestione delibera
UPDATE CONSERVAZIONI_PARAMETRI
   SET VALORE = 'select  ''nomefile_s'' nome01
,  ''Nome file'' label01
,  filename valore01
,  ''iddocimp_s'' nome02
,  ''Impronta'' label02
,  ''#CODICE_HASH#'' valore02
,  ''iddocalg_s'' nome03
,  ''Algoritmo'' label03
,  ''SHA-256'' valore03
,  ''iddocid_s'' nome20
,  ''Identificativo documento'' label04
,  id_delibera valore04
,  ''segnatura_s'' nome05
,  ''Segnatura'' label05
,  decode(numero_protocollo,'''',''Non Applicabile'', NULL,''Non Applicabile'',
 ''Progressivo di protocollo: '' || numero_protocollo || '' data: '' || to_char(data_numero_protocollo,''dd/mm/yyyy'') ||
   (SELECT  '' IPA: '' || SO4_AOO.CODICE_amministrazione || '' Codice AOO: '' || SO4_AOO.CODICE_IPA || '' Descrizione AOO: '' || SO4_AOO.DESCRIZIONE
  FROM SO4_AOO, PROTO_VIEW
  WHERE PROTO_VIEW.numero=numero_protocollo and PROTO_VIEW.anno = to_char(data_numero_protocollo,''yyyy'')
  AND PROTO_VIEW.CODICE_AOO=SO4_AOO.CODICE_AOO
  AND ROWNUM=1)
  )  valore05
,  ''modform_s'' nome06
,  ''Modalita di formazione'' label06
,  ''A'' valore06
,  ''tipdoc_s'' nome07
,  ''Tipologia documentale'' label07
,  ''ATTO COLLEGGIALE'' valore07
,  ''datregflusso_s'' nome08
,  ''Tipologia di flusso'' label08
,  ''I'' valore08
,  ''datregtipreg_s'' nome09
,  ''Tipo registro'' label09
,  ''Repertorio/Registro'' valore09
,  ''datregdata_dt'' nome11
,  ''Data registrazione'' label11
,  to_char(data_numero_delibera, ''DD-MM-YYYY hh24::mi::ss'') valore11
,  ''datregnum_s'' nome12
,  ''Numero documento'' label12
,  numero_delibera  valore12
,  ''datregid_s'' nome13
,  ''Codice registro'' label13
,  registro_delibera valore13
,  ''soggru_s'' nome14
,  ''Soggetti Ruolo'' label14
,  ''Amministrazione che effettua la registrazione''  valore14
,  ''soggtip_s'' nome15
,  ''Tipo soggetto'' label15
,  ''PAI''  valore15
,  ''denominazione_s'' nome16
,  ''Denominazione'' label16
,  (select to_char(c.valore) from conservazioni_parametri c, gdm_t_log_conservazione l where c.conservazione=''INFOCERT2'' and c.tipo = ''Parametri'' and c.chiave = ''DENOMINAZIONE_ENTE'' and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO'' AND c.ente = l.ente) valore16
,  ''soggru_2_s'' nome17
,  ''Soggetti Ruolo 2'' label17
,  ''Autore''  valore17
,  ''soggtip_2_s'' nome18
,  ''Tipo soggetto 2'' label18
,  ''PF''  valore18
,  ''cognome_2_s'' nome19
,  ''Cognome 2'' label19
,  nvl(cognome_segretario,''Non Applicabile'')   valore19
,  ''nome_2_s'' nome20
,  ''Nome 2'' label20
,  nvl(nome_segretario,''Non Applicabile'') valore20
,  ''soggru_3_s'' nome21
,  ''Soggetti Ruolo 3'' label21
,  ''Assegnatario''  valore21
,  ''soggtip_3_s'' nome22
,  ''Tipo soggetto 3'' label22
,  ''AS''  valore22
,  ''denominazione_3_s'' nome23
,  ''Denominazione 3'' label23
,  (select to_char(c.valore) from conservazioni_parametri c, gdm_t_log_conservazione l where c.conservazione=''INFOCERT2'' and c.tipo = ''Parametri'' and c.chiave = ''DENOMINAZIONE_ENTE'' and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO'' AND c.ente = l.ente) || '' - '' || unita_proponente  valore23
,  ''soggru_4_s'' nome24
,  ''Soggetti Ruolo 4'' label24
,  ''RUP''  valore24
,  ''soggtip_4_s'' nome25
,  ''Tipo soggetto 4'' label25
,  ''PF''  valore25
,  ''cogno'
 WHERE AREA = 'SEGRETERIA.ATTI.2_0'
   AND CODICE_MODELLO = 'DELIBERA'
   AND TIPO = 'Agid2022'
   AND SEQUENZA = '1'
   AND CONSERVAZIONE = 'INFOCERT2';

UPDATE CONSERVAZIONI_PARAMETRI
   SET VALORE = VALORE || 'me_4_s'' nome26
,  ''Cognome 4'' label26
,  nvl(cognome_funzionario,''Non Applicabile'')   valore26
,  ''nome_4_s'' nome27
,  ''Nome 4'' label27
,  nvl(nome_funzionario,''Non Applicabile'')    valore27
,  ''ogg_s'' nome28
,  ''Oggetto'' label28
,  oggetto valore28
,  ''alleg_i'' nome29
,  ''Allegati Numero'' label29
,  ''0'' valore29
,  ''indclass_s'' nome30
,  ''Indice di classificazioni'' label30
,  nvl(classifica_codice,''Non Applicabile'') valore30
,  ''descrclass_s'' nome31
,  ''Descrizione classificazioni'' label31
,  nvl(classifica_descrizione,''Non Applicabile'') valore31
,  ''riservato_b'' nome32
,  ''Riservato'' label32
,  decode(nvl(riservato,''N''),''N'',''false'',''true'') valore32
,  ''formid_s'' nome33
,  ''Identificativo del formato'' label33
,  ''PDF'' valore33
,  ''firm_b'' nome34
,  ''Verifica firma digitale'' label34
,  ''true'' valore34
,  ''sig_b'' nome35
,  ''Verifica sigillo'' label35
,  ''false'' valore35
,  ''marc_b'' nome36
,  ''Verifica marca temporale'' label36
,  ''false'' valore36
,  ''cop_b'' nome37
,  ''Verifica conformita copie'' label37
,  ''false'' valore37
,  ''idaggtip_s'' nome38
,  ''Tipo Aggregazione'' label38
,  decode(fascicolo_numero,'''',''Serie Documenatale'',''Fascicolo'')   valore38
,  ''idagg_s'' nome39
,  ''IdAgg'' label39
,  decode(fascicolo_numero,'''',''ATTO COLLEGGIALE '' || to_char(data_numero_delibera, ''YYYY'') ,fascicolo_numero || ''-'' || fascicolo_anno || '' '' || fascicolo_oggetto) valore39
,  ''iddocprinc_s'' nome40
,  ''Identificativo Documento Principale'' label40
,  id_delibera valore40
,  ''vers_i'' nome41
,  ''Versione del documento'' label41
,  1 valore41
,  ''tempcons_s'' nome42
,  ''Tempo di conservazione'' label42
,  9999 valore42
,  ''note_s'' nome43
,  ''Note'' label43
,  ''Non Applicabile'' valore43
  from agsde2_cons_delibere
where id_delibera = '':ID_DOCUMENTO_GRAILS'''
 WHERE AREA = 'SEGRETERIA.ATTI.2_0'
   AND CODICE_MODELLO = 'DELIBERA'
   AND TIPO = 'Agid2022'
   AND SEQUENZA = '1'
   AND CONSERVAZIONE = 'INFOCERT2';

--gestione determina
UPDATE CONSERVAZIONI_PARAMETRI
   SET VALORE = 'select  ''nomefile_s'' nome01
,  ''Nome file'' label01
,  filename valore01
,  ''iddocimp_s'' nome02
,  ''Impronta'' label02
,  ''#CODICE_HASH#'' valore02
,  ''iddocalg_s'' nome03
,  ''Algoritmo'' label03
,  ''SHA-256'' valore03
,  ''iddocid_s'' nome20
,  ''Identificativo documento'' label04
,  id_determina valore04
,  ''segnatura_s'' nome05
,  ''Segnatura'' label05
,  decode(numero_protocollo,'''',''Non Applicabile'', NULL,''Non Applicabile'',
 ''Progressivo di protocollo: '' || numero_protocollo || '' data: '' || to_char(data_numero_protocollo,''dd/mm/yyyy'') ||
   (SELECT  '' IPA: '' || SO4_AOO.CODICE_amministrazione || '' Codice AOO: '' || SO4_AOO.CODICE_IPA || '' Descrizione AOO: '' || SO4_AOO.DESCRIZIONE
  FROM SO4_AOO, PROTO_VIEW
  WHERE PROTO_VIEW.numero=numero_protocollo and PROTO_VIEW.anno = to_char(data_numero_protocollo,''yyyy'')
  AND PROTO_VIEW.CODICE_AOO=SO4_AOO.CODICE_AOO
  AND ROWNUM=1)
  )  valore05
,  ''modform_s'' nome06
,  ''Modalita di formazione'' label06
,  ''A'' valore06
,  ''tipdoc_s'' nome07
,  ''Tipologia documentale'' label07
,  ''ATTO MONOCRATICO'' valore07
,  ''datregflusso_s'' nome08
,  ''Tipologia di flusso'' label08
,  ''I'' valore08
,  ''datregtipreg_s'' nome09
,  ''Tipo registro'' label09
,  ''Repertorio/Registro'' valore09
,  ''datregdata_dt'' nome11
,  ''Data registrazione'' label11
,  to_char(data_numero_determina, ''DD-MM-YYYY hh24::mi::ss'') valore11
,  ''datregnum_s'' nome12
,  ''Numero documento'' label12
,  numero_determina  valore12
,  ''datregid_s'' nome13
,  ''Codice registro'' label13
,  registro_determina valore13
,  ''soggru_s'' nome14
,  ''Soggetti Ruolo'' label14
,  ''Amministrazione che effettua la registrazione''  valore14
,  ''soggtip_s'' nome15
,  ''Tipo soggetto'' label15
,  ''PAI''  valore15
,  ''denominazione_s'' nome16
,  ''Denominazione'' label16
,  (select to_char(c.valore) from conservazioni_parametri c, gdm_t_log_conservazione l where c.conservazione=''INFOCERT2'' and c.tipo = ''Parametri'' and c.chiave = ''DENOMINAZIONE_ENTE'' and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO'' AND c.ente = l.ente) valore16
,  ''soggru_2_s'' nome17
,  ''Soggetti Ruolo 2'' label17
,  ''Autore''  valore17
,  ''soggtip_2_s'' nome18
,  ''Tipo soggetto 2'' label18
,  ''PF''  valore18
,  ''cognome_2_s'' nome19
,  ''Cognome 2'' label19
,  nvl(cognome_firmatario,''Non Applicabile'')    valore19
,  ''nome_2_s'' nome20
,  ''Nome 2'' label20
,  nvl(nome_firmatario,''Non Applicabile'') valore20
,  ''soggru_3_s'' nome21
,  ''Soggetti Ruolo 3'' label21
,  ''Assegnatario''  valore21
,  ''soggtip_3_s'' nome22
,  ''Tipo soggetto 3'' label22
,  ''AS''  valore22
,  ''denominazione_3_s'' nome23
,  ''Denominazione 3'' label23
,  (select to_char(c.valore) from conservazioni_parametri c, gdm_t_log_conservazione l where c.conservazione=''INFOCERT2'' and c.tipo = ''Parametri'' and c.chiave = ''DENOMINAZIONE_ENTE'' and L.ID_DOCUMENTO_RIF = ''4838432'' AND c.ente = l.ente) || '' - '' || unita_proponente  valore23
,  ''soggru_4_s'' nome24
,  ''Soggetti Ruolo 4'' label24
,  ''RUP''  valore24
,  ''soggtip_4_s'' nome25
,  ''Tipo soggetto 4'' label25
,  ''PF''  valore25
,  ''cognome'
 WHERE AREA = 'SEGRETERIA.ATTI.2_0'
   AND CODICE_MODELLO = 'DETERMINA'
   AND TIPO = 'Agid2022'
   AND SEQUENZA = '1'
   AND CONSERVAZIONE = 'INFOCERT2';

UPDATE CONSERVAZIONI_PARAMETRI
   SET VALORE = VALORE || '_4_s'' nome26
,  ''Cognome 4'' label26
,  nvl(cognome_funzionario,''Non Applicabile'')   valore26
,  ''nome_4_s'' nome27
,  ''Nome 4'' label27
,  nvl(nome_funzionario,''Non Applicabile'')    valore27
,  ''ogg_s'' nome28
,  ''Oggetto'' label28
,  oggetto valore28
,  ''alleg_i'' nome29
,  ''Allegati Numero'' label29
,  ''0'' valore29
,  ''indclass_s'' nome30
,  ''Indice di classificazioni'' label30
,  nvl(classifica_codice,''Non Applicabile'') valore30
,  ''descrclass_s'' nome31
,  ''Descrizione classificazioni'' label31
,  nvl(classifica_descrizione,''Non Applicabile'') valore31
,  ''riservato_b'' nome32
,  ''Riservato'' label32
,  decode(nvl(riservato,''N''),''N'',''false'',''true'') valore32
,  ''formid_s'' nome33
,  ''Identificativo del formato'' label33
,  ''PDF'' valore33
,  ''firm_b'' nome34
,  ''Verifica firma digitale'' label34
,  ''true'' valore34
,  ''sig_b'' nome35
,  ''Verifica sigillo'' label35
,  ''false'' valore35
,  ''marc_b'' nome36
,  ''Verifica marca temporale'' label36
,  ''false'' valore36
,  ''cop_b'' nome37
,  ''Verifica conformita copie'' label37
,  ''false'' valore37
,  ''idaggtip_s'' nome38
,  ''Tipo Aggregazione'' label38
,  decode(fascicolo_numero,'''',''Serie Documenatale'',''Fascicolo'')   valore38
,  ''idagg_s'' nome39
,  ''IdAgg'' label39
,  decode(fascicolo_numero,'''',''ATTO MONOCRATICO '' || to_char(data_numero_determina, ''YYYY'') ,fascicolo_numero || ''-'' || fascicolo_anno || '' '' || fascicolo_oggetto)   valore39
,  ''iddocprinc_s'' nome40
,  ''Identificativo Documento Principale'' label40
,  id_determina valore40
,  ''vers_i'' nome41
,  ''Versione del documento'' label41
,  1 valore41
,  ''tempcons_s'' nome42
,  ''Tempo di conservazione'' label42
,  9999 valore42
,  ''note_s'' nome43
,  ''Note'' label43
,  ''Non Applicabile'' valore43
  from agsde2_cons_determine
where id_determina = '':ID_DOCUMENTO_GRAILS'''
 WHERE AREA = 'SEGRETERIA.ATTI.2_0'
   AND CODICE_MODELLO = 'DETERMINA'
   AND TIPO = 'Agid2022'
   AND SEQUENZA = '1'
   AND CONSERVAZIONE = 'INFOCERT2';
