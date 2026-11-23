--liquibase formatted sql
--changeset mferrara:2.6_20260127_03.regole_agid_trust runOnChange:true failOnError:false

--gestione delibera
UPDATE CONSERVAZIONI_PARAMETRI
   SET VALORE = 'SELECT distinct id_documento_esterno id_documento
               ,''NomeDelDocumento''
               ,filename
               ,0
               ,''Impronta''
               ,''#CODICE_HASH#''
               ,1
               ,''Algoritmo''
               ,''SHA-256''
               ,2
               ,''Identificativo''
               ,id_delibera
               ,3
               ,''Segnatura''
               ,case
                  when length(numero_protocollo) > 0
                       and length(data_numero_protocollo) > 0 then
                   decode(numero_protocollo
                         ,''''
                         ,NULL
                         ,NULL
                         ,NULL
                         ,''Progressivo di protocollo: '' || numero_protocollo ||
                          '' data: '' || to_char(data_numero_protocollo
                                              ,''dd/MM/yyyy'') ||
                          (select '' IPA: '' || to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione = ''TRUST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''CODICE_IPA_AMM''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente) ||
                          (select '' Codice AOO: '' || to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione = ''TRUST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''CODICE_IPA_AOO''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente) ||
                          (select '' Descrizione AOO: '' ||to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione = ''TRUST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''DESCRIZIONE_AOO''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente))
                  else
                   ''NON APPLICABILE''
                end
               ,4
               ,''ModalitaDiFormazione''
               ,''A''
               ,5
               ,''TipologiaDocumentale''
               ,''ATTO COLLEGIALE''
               ,6
               ,''TipologiaDiFlusso''
               ,''I''
               ,7
               ,''TipoRegistro''
               ,''REPERTORIO''
               ,8
               ,''DataDocumento''
               ,to_char(data_numero_delibera
                       ,''dd/MM/yyyy'')
               ,9
               ,''DataRegistrazione''
               ,to_char(data_nume'
 WHERE AREA = 'SEGRETERIA.ATTI.2_0'
   AND CODICE_MODELLO = 'DELIBERA'
   AND TIPO = 'DocumentoPrincipale'
   AND SEQUENZA = '1'
   AND CONSERVAZIONE = 'TRUST';

UPDATE CONSERVAZIONI_PARAMETRI
   SET VALORE = VALORE ||
                'ro_delibera
                       ,''dd/MM/yyyy hh24::mi::ss'')
               ,10
               ,''NumeroDocumento''
               ,numero_delibera
               ,11
               ,''CodiceRegistro''
               ,registro_delibera
               ,12
               ,''RuoloSoggetto1''
               ,''Amministrazione che effettua la registrazione''
               ,13
               ,''TipoSoggetto1''
               ,''PAI '' || (select to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione = ''TRUST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''DENOMINAZIONE_AMM''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente) || '' '' ||
                (select to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione = ''TRUST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''CODICE_IPA_AMM''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente) || '' '' ||
                (select to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione = ''TRUST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''INDIRIZZO_ISTITUZIONALE_AMM''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente)
               ,14
               ,''RuoloSoggetto2''
               ,''Autore''
               ,15
               ,''TipoSoggetto2''
               ,''PF '' || nvl(cognome_segretario
                            ,''NON APPLICABILE'') || '' '' ||
                nvl(nome_segretario
                   ,''NON APPLICABILE'')
               ,16
               ,''RuoloSoggetto3''
               ,''Produttore''
               ,17
               ,''TipoSoggetto3''
               ,''NON APPLICABILE''
               ,18
               ,''RuoloSoggetto4''
               ,''Destinatario''
               ,19
               ,''TipoSoggetto4''
               ,''NON APPLICABILE''
               ,20
               ,''RuoloSoggetto5''
               ,''Operatore''
               ,21
               ,''TipoSoggetto5''
               ,''NON APPLICABILE''
               ,22
               ,''RuoloSoggetto6''
               ,''Assegnatario''
               ,23
               ,''TipoSoggetto6''
               ,((select ''AS '' || to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione = ''TRU'
 WHERE AREA = 'SEGRETERIA.ATTI.2_0'
   AND CODICE_MODELLO = 'DELIBERA'
   AND TIPO = 'DocumentoPrincipale'
   AND SEQUENZA = '1'
   AND CONSERVAZIONE = 'TRUST';

UPDATE CONSERVAZIONI_PARAMETRI
   SET VALORE = VALORE || 'ST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''DENOMINAZIONE_ENTE''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente) || '' - '' ||
                unita_proponente)
               ,24
               ,''RuoloSoggetto7''
               ,''RUP''
               ,25
               ,''TipoSoggetto7''
               ,''RUP '' || nvl(cognome_funzionario
                             ,''NON APPLICABILE'') || '' '' ||
                nvl(nome_funzionario
                   ,''NON APPLICABILE'')
               ,26
               ,''Oggetto''
               ,oggetto
               ,27
               ,''NumeroAllegati''
               ,''#NUMERO_ALLEGATI#''
               ,28
               ,''IndiceAllegati''
               ,''#INDICE_ALLEGATI#''
               ,29
               ,''IndiceDiClassificazione''
               ,nvl(classifica_codice
                   ,''NON APPLICABILE'')
               ,30
               ,''Descrizione''
               ,nvl(classifica_descrizione
                   ,''NON APPLICABILE'')
               ,31
               ,''PianoDiClassificazione''
               ,''NON APPLICABILE''
               ,32
               ,''Riservato''
               ,''FALSO''
               ,33
               ,''Formato''
               ,''PDF''
               ,34
               ,''ProdottoSoftware''
               ,''''
               ,35
               ,''FirmatoDigitalmente''
               ,''VERO''
               ,36
               ,''SigillatoElettronicamente''
               ,''FALSO''
               ,37
               ,''MarcaturaTemporale''
               ,''FALSO''
               ,38
               ,''ConfCopieImgSuSuppInformatico''
               ,''FALSO''
               ,39
               ,''Agg''
               ,decode(fascicolo_numero
                      ,''''
                      ,''Serie Documentale''
                      ,''Fascicolo'') || '' '' ||
                decode(fascicolo_numero
                      ,''''
                      ,''ATTO COLLEGGIALE '' ||
                       to_char(data_numero_delibera
                              ,''YYYY'')
                      ,fascicolo_numero || ''-'' || fascicolo_anno || '' '' ||
                       fascicolo_oggetto)
               ,40
               ,''IdDocumentoPrimario''
               ,id_file_gdm
               ,41
               ,''VersioneDelDocumento''
               ,''1''
               ,42
               ,''TipoModifica''
               ,''''
               ,43
               ,''SoggettoAutoreDellaModifica''
               ,''''
               ,44
               ,''DataModifica''
               ,''''
               ,45
               ,''IdDocVersionePrecedente''
               ,''''
               ,46
               ,''TempoDiConservazione''
               ,''9999''
               ,47
               ,''Note''
               ,''-''
               ,48
               ,''DataEsecutivit ''
               ,to_char(data_esecutivita
                      '
 WHERE AREA = 'SEGRETERIA.ATTI.2_0'
   AND CODICE_MODELLO = 'DELIBERA'
   AND TIPO = 'DocumentoPrincipale'
   AND SEQUENZA = '1'
   AND CONSERVAZIONE = 'TRUST';

UPDATE CONSERVAZIONI_PARAMETRI
   SET VALORE = VALORE || ' ,''dd/MM/yyyy hh24::mi::ss'')
               ,49
  from agsde2_cons_delibere
 where id_delibera = '':ID_DOCUMENTO_GRAILS''
'
 WHERE AREA = 'SEGRETERIA.ATTI.2_0'
   AND CODICE_MODELLO = 'DELIBERA'
   AND TIPO = 'DocumentoPrincipale'
   AND SEQUENZA = '1'
   AND CONSERVAZIONE = 'TRUST';

--gestione determina
UPDATE CONSERVAZIONI_PARAMETRI
   SET VALORE = 'SELECT distinct id_documento_esterno id_documento
               ,''NomeDelDocumento''
               ,filename
               ,0
               ,''Impronta''
               ,''#CODICE_HASH#''
               ,1
               ,''Algoritmo''
               ,''SHA-256''
               ,2
               ,''Identificativo''
               ,id_determina
               ,3
               ,''Segnatura''
               ,case
                  when length(numero_protocollo) > 0
                       and length(data_numero_protocollo) > 0 then
                   decode(numero_protocollo
                         ,''''
                         ,NULL
                         ,NULL
                         ,NULL
                         ,''Progressivo di protocollo: '' || numero_protocollo ||
                          '' data: '' || to_char(data_numero_protocollo
                                              ,''dd/MM/yyyy'') ||
                          (select '' IPA: '' || to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione = ''TRUST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''CODICE_IPA_AMM''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente) ||
                          (select '' Codice AOO: '' || to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione = ''TRUST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''CODICE_IPA_AOO''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente) ||
                          (select '' Descrizione AOO: '' ||to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione = ''TRUST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''DESCRIZIONE_AOO''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente))
                  else
                   ''NON APPLICABILE''
                end
               ,4
               ,''ModalitaDiFormazione''
               ,''A''
               ,5
               ,''TipologiaDocumentale''
               ,''ATTO MONOCRATICO''
               ,6
               ,''TipologiaDiFlusso''
               ,''I''
               ,7
               ,''TipoRegistro''
               ,''REPERTORIO''
               ,8
               ,''DataDocumento''
               ,to_char(data_numero_determina
                       ,''dd/MM/yyyy'')
               ,9
               ,''DataRegistrazione''
               ,to_char(data_n'
 WHERE AREA = 'SEGRETERIA.ATTI.2_0'
   AND CODICE_MODELLO = 'DETERMINA'
   AND TIPO = 'DocumentoPrincipale'
   AND SEQUENZA = '1'
   AND CONSERVAZIONE = 'TRUST';

UPDATE CONSERVAZIONI_PARAMETRI
   SET VALORE = VALORE || 'umero_determina
                       ,''dd/MM/yyyy hh24::mi::ss'')
               ,10
               ,''NumeroDocumento''
               ,numero_determina
               ,11
               ,''CodiceRegistro''
               ,registro_determina
               ,12
               ,''RuoloSoggetto1''
               ,''Amministrazione che effettua la registrazione''
               ,13
               ,''TipoSoggetto1''
               ,''PAI '' || (select to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione = ''TRUST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''DENOMINAZIONE_AMM''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente) || '' '' ||
                (select to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione = ''TRUST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''CODICE_IPA_AMM''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente) || '' '' ||
                (select to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione = ''TRUST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''INDIRIZZO_ISTITUZIONALE_AMM''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente)
               ,14
               ,''RuoloSoggetto2''
               ,''Autore''
               ,15
               ,''TipoSoggetto2''
               ,''PF '' || nvl(cognome_firmatario
                            ,''NON APPLICABILE'') || '' '' ||
                nvl(nome_firmatario
                   ,''NON APPLICABILE'')
               ,16
               ,''RuoloSoggetto3''
               ,''Produttore''
               ,17
               ,''TipoSoggetto3''
               ,''NON APPLICABILE''
               ,18
               ,''RuoloSoggetto4''
               ,''Destinatario''
               ,19
               ,''TipoSoggetto4''
               ,''NON APPLICABILE''
               ,20
               ,''RuoloSoggetto5''
               ,''Operatore''
               ,21
               ,''TipoSoggetto5''
               ,''NON APPLICABILE''
               ,22
               ,''RuoloSoggetto6''
               ,''Assegnatario''
               ,23
               ,''TipoSoggetto6''
               ,((select ''AS '' || to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione '
 WHERE AREA = 'SEGRETERIA.ATTI.2_0'
   AND CODICE_MODELLO = 'DETERMINA'
   AND TIPO = 'DocumentoPrincipale'
   AND SEQUENZA = '1'
   AND CONSERVAZIONE = 'TRUST';

UPDATE CONSERVAZIONI_PARAMETRI
   SET VALORE = VALORE || '= ''TRUST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''DENOMINAZIONE_ENTE''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente) || '' - '' ||
                unita_proponente)
               ,24
               ,''RuoloSoggetto7''
               ,''RUP''
               ,25
               ,''TipoSoggetto7''
               ,''RUP '' || nvl(cognome_funzionario
                             ,''NON APPLICABILE'') || '' '' ||
                nvl(nome_funzionario
                   ,''NON APPLICABILE'')
               ,26
               ,''Oggetto''
               ,oggetto
               ,27
               ,''NumeroAllegati''
               ,''#NUMERO_ALLEGATI#''
               ,28
               ,''IndiceAllegati''
               ,''#INDICE_ALLEGATI#''
               ,29
               ,''IndiceDiClassificazione''
               ,nvl(classifica_codice
                   ,''NON APPLICABILE'')
               ,30
               ,''Descrizione''
               ,nvl(classifica_descrizione
                   ,''NON APPLICABILE'')
               ,31
               ,''PianoDiClassificazione''
               ,''NON APPLICABILE''
               ,32
               ,''Riservato''
               ,decode(nvl(riservato
                          ,''N'')
                      ,''N''
                      ,''FALSO''
                      ,''VERO'')
               ,33
               ,''Formato''
               ,''PDF''
               ,34
               ,''ProdottoSoftware''
               ,''''
               ,35
               ,''FirmatoDigitalmente''
               ,''VERO''
               ,36
               ,''SigillatoElettronicamente''
               ,''FALSO''
               ,37
               ,''MarcaturaTemporale''
               ,''FALSO''
               ,38
               ,''ConfCopieImgSuSuppInformatico''
               ,''FALSO''
               ,39
               ,''Agg''
               ,decode(fascicolo_numero
                      ,''''
                      ,''Serie Documentale''
                      ,''Fascicolo'') || '' '' ||
                decode(fascicolo_numero
                      ,''''
                      ,''ATTO MONOCRATICO '' ||
                       to_char(data_numero_determina
                              ,''YYYY'')
                      ,fascicolo_numero || ''-'' || fascicolo_anno || '' '' ||
                       fascicolo_oggetto)
               ,40
               ,''IdDocumentoPrimario''
               ,id_file_gdm
               ,41
               ,''VersioneDelDocumento''
               ,''1''
               ,42
               ,''TipoModifica''
               ,''''
               ,43
               ,''SoggettoAutoreDellaModifica''
               ,''''
               ,44
               ,''DataModifica''
               ,''''
               ,45
               ,''IdDocVersionePrecedente''
               ,''''
               ,46
               ,''TempoDiConservazione''
               ,''9999''
               ,47
               ,''N'
 WHERE AREA = 'SEGRETERIA.ATTI.2_0'
   AND CODICE_MODELLO = 'DETERMINA'
   AND TIPO = 'DocumentoPrincipale'
   AND SEQUENZA = '1'
   AND CONSERVAZIONE = 'TRUST';

UPDATE CONSERVAZIONI_PARAMETRI
   SET VALORE = VALORE || 'ote''
               ,''-''
               ,47
               ,''DataEsecutivit ''
               ,to_char(data_esecutivita
                       ,''dd/MM/yyyy hh24::mi::ss'')
               ,49
               ,DECODE((select to_char(c.valore)
                             from conservazioni_parametri c
                                 ,gdm_t_log_conservazione l
                            where c.conservazione = ''TRUST''
                              and c.tipo = ''Parametri''
                              and c.chiave = ''ALIAS_SP''
                              and L.ID_DOCUMENTO_RIF = '':ID_DOCUMENTO''
                              AND c.ente = l.ente)
                      ,''ESU-PADOVA''
                      ,decode(REGISTRO_DETERMINA
                             ,''LIQU''
                             ,''CIG''
                             ,''***'')
                      ,''***'') cig
               ,decode(REGISTRO_DETERMINA
                      ,''LIQU''
                      ,cig
                      ,''***'')
               ,decode(REGISTRO_DETERMINA
                      ,''LIQU''
                      ,50
                      ,0)
  from agsde2_cons_determine
 where id_determina = '':ID_DOCUMENTO_GRAILS''
'
 WHERE AREA = 'SEGRETERIA.ATTI.2_0'
   AND CODICE_MODELLO = 'DETERMINA'
   AND TIPO = 'DocumentoPrincipale'
   AND SEQUENZA = '1'
   AND CONSERVAZIONE = 'TRUST';
