--liquibase formatted sql
--changeset rdestasio:install_20200221_ass_file_all_vuoti failOnError:false

-- QUESTI OGGETTI DOVREBBERO ESSERE DISTRIBUITI DA GDM. A VOLTE NON LO SONO. LI CREO SOLO SE NON SONO PRESENTI.

CREATE TABLE TMP_FILE
(
  FILE_TEMPORANY  BLOB
);
/

CREATE PACKAGE gdm_oggetti_file
AS
/*
   Per il corretto funzionamento e' necessario lanciare come sys:
   grant select on dba_directories to gdm;
   grant execute on dbms_backup_restore to gdm;
*/
PROCEDURE GETPATH_FILE_FS(P_IDOGGETTO_FILE NUMBER, P_DIRECTORY IN OUT VARCHAR2, P_PATH_DIR_FS IN OUT  VARCHAR2 ,  P_PATH_FILE IN OUT  VARCHAR2, P_ISLOG NUMBER DEFAULT 0 );
FUNCTION IS_FS_FILE(P_IDOGGETTO_FILE NUMBER,P_ISLOG NUMBER DEFAULT 0) RETURN NUMBER;
PROCEDURE DELETEOGGETTOFILE(P_IDOGGETTO_FILE NUMBER);
FUNCTION DOWNLOADOGGETTOFILE(P_IDOGGETTO_FILE NUMBER) RETURN BLOB;
FUNCTION DOWNLOADOGGETTOFILE_LOG(P_IDOGGETTO_FILE_LOG NUMBER) RETURN BLOB;
PROCEDURE DOWNLOADOGGETTOFILE_TMP(P_IDOGGETTO_FILE NUMBER);
PROCEDURE OGGETTO_FILE_TO_FS_NOCOMMIT(A_IDOGGETTOFILE NUMBER, A_ID_LOG NUMBER,A_PULISCIBLOB NUMBER, A_USABLOB NUMBER  DEFAULT 0, A_USAQUESTOBLOB BLOB DEFAULT EMPTY_BLOB(),A_TYPE_FILESYS VARCHAR2 DEFAULT 'LNX');
PROCEDURE OGGETTO_FILE_TO_FS(A_IDOGGETTOFILE NUMBER, A_ID_LOG NUMBER,A_PULISCIBLOB NUMBER, A_USABLOB NUMBER  DEFAULT 0, A_USAQUESTOBLOB BLOB DEFAULT EMPTY_BLOB(),A_TYPE_FILESYS VARCHAR2 DEFAULT 'LNX');
PROCEDURE OGGETTO_FILE_TO_FS(A_IDOGGETTOFILE NUMBER,A_PULISCIBLOB NUMBER,A_TYPE_FILESYS VARCHAR2 DEFAULT 'LNX');
END;
/

CREATE PACKAGE BODY gdm_oggetti_file
AS
    PROCEDURE OGGETTO_FILE_TO_FS(A_IDOGGETTOFILE NUMBER,A_PULISCIBLOB NUMBER,A_TYPE_FILESYS VARCHAR2 DEFAULT 'LNX')
    AS
    BEGIN
          OGGETTO_FILE_TO_FS(A_IDOGGETTOFILE, -1,A_PULISCIBLOB,0,EMPTY_BLOB(),A_TYPE_FILESYS);   
    END;  
    PROCEDURE OGGETTO_FILE_TO_FS_NOCOMMIT(A_IDOGGETTOFILE NUMBER, A_ID_LOG NUMBER,A_PULISCIBLOB NUMBER, A_USABLOB NUMBER DEFAULT 0, A_USAQUESTOBLOB BLOB DEFAULT EMPTY_BLOB(),A_TYPE_FILESYS VARCHAR2 DEFAULT 'LNX')  
    AS
    A_ESISTE NUMBER(1):=1;
    A_PATH_FILE_ORACLE               VARCHAR2(1000);
    A_ACR_AREA                            VARCHAR2(50);
    A_ACR_MODELLO                      VARCHAR2(50);
    A_IDDOC_FRATTOMILLE            VARCHAR2(50);    
    A_PERCORSO                           VARCHAR2(1000);    
    A_IDDOCUMENTO                     NUMBER(10);
    A_BLOB                                   BLOB;
    
   l_pos INTEGER := 1;
   l_blob_len INTEGER;
   l_file UTL_FILE.FILE_TYPE;
   l_buffer RAW(32767);
   l_amount BINARY_INTEGER := 32767;    
   l_separator varchar2(1) :='/';   
    BEGIN
        BEGIN
           IF A_TYPE_FILESYS<>'LNX' THEN 
               l_separator:='\';
           END IF;

            IF A_ID_LOG=-1 THEN
                --CASO OGGETTO_FILE
                
                SELECT PATH_FILE_ORACLE, AREE.ACRONIMO,TIPI_DOCUMENTO.ACRONIMO_MODELLO, TO_CHAR(TRUNC(DOCUMENTI.ID_DOCUMENTO/1000)) ,DOCUMENTI.ID_DOCUMENTO, TESTOOCR
                   INTO A_PATH_FILE_ORACLE,A_ACR_AREA, A_ACR_MODELLO,A_IDDOC_FRATTOMILLE,A_IDDOCUMENTO,A_BLOB
                FROM TIPI_DOCUMENTO, AREE , OGGETTI_FILE, DOCUMENTI
                WHERE OGGETTI_FILE.ID_OGGETTO_FILE=A_IDOGGETTOFILE AND
                            DOCUMENTI.ID_DOCUMENTO=OGGETTI_FILE.ID_DOCUMENTO AND
                            DOCUMENTI.AREA = AREE.AREA AND
                            DOCUMENTI.ID_TIPODOC=TIPI_DOCUMENTO.ID_TIPODOC AND
                            (OGGETTI_FILE.PATH_FILE IS NULL) AND                
                            (PATH_FILE_ORACLE IS NOT NULL) AND
                            (AREE.ACRONIMO IS NOT NULL) AND
                            (TIPI_DOCUMENTO.ACRONIMO_MODELLO IS NOT NULL) AND
                            decode(A_USABLOB,1,1,nvl(DBMS_LOB.getlength(TESTOOCR),0))>0
                            ;
            ELSE
                --CASO OGGETTO_FILE_LOG
                SELECT PATH_FILE_ORACLE, AREE.ACRONIMO,TIPI_DOCUMENTO.ACRONIMO_MODELLO, TO_CHAR(TRUNC(DOCUMENTI.ID_DOCUMENTO/1000)) ,DOCUMENTI.ID_DOCUMENTO,TESTOOCR
                   INTO A_PATH_FILE_ORACLE,A_ACR_AREA, A_ACR_MODELLO,A_IDDOC_FRATTOMILLE, A_IDDOCUMENTO,A_BLOB
                FROM TIPI_DOCUMENTO, AREE , OGGETTI_FILE_LOG, DOCUMENTI, ACTIVITY_LOG
                WHERE OGGETTI_FILE_LOG.ID_OGGETTO_FILE=A_IDOGGETTOFILE AND
                            OGGETTI_FILE_LOG.ID_LOG=A_ID_LOG AND
                            ACTIVITY_LOG.ID_LOG = OGGETTI_FILE_LOG.ID_LOG AND
                            DOCUMENTI.ID_DOCUMENTO=ACTIVITY_LOG.ID_DOCUMENTO AND
                            DOCUMENTI.AREA = AREE.AREA AND
                            DOCUMENTI.ID_TIPODOC=TIPI_DOCUMENTO.ID_TIPODOC AND
                            (OGGETTI_FILE_LOG.PATH_FILE IS NULL) AND
                            (PATH_FILE_ORACLE IS NOT NULL) AND
                            (AREE.ACRONIMO IS NOT NULL) AND
                            (TIPI_DOCUMENTO.ACRONIMO_MODELLO IS NOT NULL) 
                            AND rownum=1    AND
                            decode(A_USABLOB,1,1,nvl(DBMS_LOB.getlength(TESTOOCR),0))>0
                            ;                
                NULL;
            END IF;
        EXCEPTION WHEN NO_DATA_FOUND THEN
            A_ESISTE:=0;
        END;                                     
    
        IF A_ESISTE=1 THEN
            IF A_USABLOB=1 THEN
                A_BLOB := A_USAQUESTOBLOB;
            END IF;
        
            A_PERCORSO:=A_PATH_FILE_ORACLE||l_separator||A_ACR_AREA;
           -- GDM_UTILITY.MKDIR(A_PERCORSO);
           -- GDM_UTILITY.CHMOD(A_PERCORSO);
            
            A_PERCORSO:=A_PERCORSO||l_separator||A_ACR_MODELLO;
          --  GDM_UTILITY.MKDIR(A_PERCORSO);
          --  GDM_UTILITY.CHMOD(A_PERCORSO);
            
            A_PERCORSO:=A_PERCORSO||l_separator||A_IDDOC_FRATTOMILLE;
           -- GDM_UTILITY.MKDIR(A_PERCORSO);
          --  GDM_UTILITY.CHMOD(A_PERCORSO);      
            
            A_PERCORSO:=A_PERCORSO||l_separator||A_IDDOCUMENTO;
          --  GDM_UTILITY.MKDIR(A_PERCORSO);
          --  GDM_UTILITY.CHMOD(A_PERCORSO);
            
           
            
            IF A_ID_LOG<>-1 THEN
                A_PERCORSO:=A_PERCORSO||l_separator||'LOG_'||TO_CHAR(A_ID_LOG);
                
               -- GDM_UTILITY.MKDIR(A_PERCORSO);
               -- GDM_UTILITY.CHMOD(A_PERCORSO);     
            END IF;
            
            GDM_UTILITY.MKDIR(A_PERCORSO);
   
            BEGIN
                 execute immediate 'create or replace directory DIR_'||A_ACR_AREA||' as '''||A_PATH_FILE_ORACLE||l_separator||A_ACR_AREA||'''';
            EXCEPTION WHEN OTHERS THEN
                 RAISE_APPLICATION_ERROR(-20999,'Errore in creazione dir oracle di area per path '||A_PATH_FILE_ORACLE||l_separator||A_ACR_AREA||': '||sqlerrm); 
            END;        
            

           BEGIN
              execute immediate 'create or replace directory TEMP_FILE as '''||A_PERCORSO||'''';
           EXCEPTION WHEN OTHERS THEN
             RAISE_APPLICATION_ERROR(-20999,'Errore in creazione dir oracle temporanea per path '||A_PERCORSO||': '||sqlerrm); 
           END;                       
           
           l_blob_len := DBMS_LOB.getlength(A_BLOB);
           l_pos:= 1;
              
           BEGIN
              l_file := UTL_FILE.fopen('TEMP_FILE',A_IDOGGETTOFILE,'wb', 32767);
                -- dbms_output.put_line(l_blob_len);
              WHILE l_pos <= l_blob_len LOOP
                    DBMS_LOB.read(A_BLOB, l_amount, l_pos, l_buffer);
                    UTL_FILE.put_raw(l_file, l_buffer, TRUE);
                    --dbms_output.put_line(l_amount);
                    l_pos := l_pos + l_amount;
                    
                     --dbms_output.put_line(l_pos);
              END LOOP;
              UTL_FILE.fclose(l_file);
           EXCEPTION WHEN OTHERS THEN
              RAISE_APPLICATION_ERROR(-20999,'Errore in scrittura file '||A_PERCORSO||l_separator||A_IDOGGETTOFILE||': '||sqlerrm);  
           END;           
                
              
           IF A_TYPE_FILESYS='LNX' THEN
             GDM_UTILITY.CHMOD(A_PERCORSO||l_separator||A_IDOGGETTOFILE);
           END IF;
           
           --Controllo esistenza e dimensione del file corretti
           DECLARE
           A_FAKE   NUMBER(1);
           A_PATH   VARCHAR2(32000);
           BEGIN              
                A_PATH := A_ACR_MODELLO||l_separator||A_IDDOC_FRATTOMILLE||l_separator||A_IDDOCUMENTO;
                 IF A_ID_LOG<>-1 THEN
                 A_PATH := A_PATH || l_separator||'LOG_'||TO_CHAR(A_ID_LOG);
                 END IF;
                 
                 A_PATH := A_PATH || l_separator ||A_IDOGGETTOFILE;
           
                SELECT 1
                INTO A_FAKE
                FROM DUAL
                WHERE  dbms_lob.fileexists(bfilename('DIR_'||A_ACR_AREA,A_PATH))<>0  AND 
                            dbms_lob.getlength(bfilename('DIR_'||A_ACR_AREA,A_PATH))=l_blob_len;
           EXCEPTION WHEN NO_DATA_FOUND THEN
                    RAISE_APPLICATION_ERROR(-20999,'Il file non esiste sul file system dopo il trasporto, oppure la dimensione dei file tra db e fs non � la stessa. Verificare. Il file su FS si trova qui: '||
                                                                            A_PERCORSO||l_separator||A_IDOGGETTOFILE);
                            WHEN OTHERS THEN
                   RAISE_APPLICATION_ERROR(-20999,'Errore nel controllo presenza e dimensione e file su FS dopo il trasporto. Il file su FS si trova qui: '||
                                                                            A_PERCORSO||l_separator||A_IDOGGETTOFILE||' - Errore: '||sqlerrm);         
           END;                   
       
           IF A_PULISCIBLOB=1 THEN
              BEGIN
                  IF A_ID_LOG=-1 THEN
                  --null;
                      UPDATE OGGETTI_FILE SET PATH_FILE=A_ACR_AREA, TESTOOCR=NULL WHERE ID_OGGETTO_FILE=A_IDOGGETTOFILE;
                 ELSE
                   --null;
                      UPDATE OGGETTI_FILE_LOG SET PATH_FILE=A_ACR_AREA, TESTOOCR=NULL WHERE ID_OGGETTO_FILE=A_IDOGGETTOFILE AND ID_LOG=A_ID_LOG;
                 END IF;
                
              EXCEPTION WHEN OTHERS THEN                
                 RAISE_APPLICATION_ERROR(-20999,'Errore in aggiornamento path_file e annullamento blob su oggetti file: '||sqlerrm);  
              END;
           END IF;
             
        END IF;             
    
        
    
    EXCEPTION WHEN OTHERS  THEN
       RAISE_APPLICATION_ERROR(-20999,'Errore in OGGETTO_FILE_TO_FS per IDOGGETTOFILE='||A_IDOGGETTOFILE||' e ID_LOG '||A_ID_LOG||'. Errore: '||sqlerrm); 
    END;
    
    PROCEDURE OGGETTO_FILE_TO_FS(A_IDOGGETTOFILE NUMBER, A_ID_LOG NUMBER,A_PULISCIBLOB NUMBER, A_USABLOB NUMBER DEFAULT 0, A_USAQUESTOBLOB BLOB DEFAULT EMPTY_BLOB(),A_TYPE_FILESYS VARCHAR2 DEFAULT 'LNX')  
    AS    
   PRAGMA AUTONOMOUS_TRANSACTION;
    BEGIN
        OGGETTO_FILE_TO_FS_NOCOMMIT(A_IDOGGETTOFILE,A_ID_LOG,A_PULISCIBLOB,A_USABLOB,A_USAQUESTOBLOB,A_TYPE_FILESYS);
        COMMIT;
    EXCEPTION WHEN OTHERS  THEN 
        ROLLBACK;
       RAISE;
    END;
    FUNCTION IS_FS_FILE(P_IDOGGETTO_FILE NUMBER,P_ISLOG NUMBER DEFAULT 0) RETURN NUMBER
    IS
    A_RET NUMBER(1) := 0;
    BEGIN
        IF P_ISLOG=0 THEN
            SELECT DECODE(PATH_FILE,NULL, 0 ,DECODE(PATH_FILE,'',0,1))
               INTO  A_RET
               FROM OGGETTI_FILE
               WHERE ID_OGGETTO_FILE = P_IDOGGETTO_FILE;
        ELSE
            SELECT DECODE(PATH_FILE,NULL, 0 ,DECODE(PATH_FILE,'',0,1))
               INTO  A_RET
               FROM OGGETTI_FILE_LOG
               WHERE ID_OGGETTO_FILE_LOG = P_IDOGGETTO_FILE;            
        END IF;
        
        RETURN A_RET;
    END;
    PROCEDURE GETPATH_FILE_FS( P_IDOGGETTO_FILE NUMBER, P_DIRECTORY IN OUT VARCHAR2, P_PATH_DIR_FS IN OUT  VARCHAR2 ,  P_PATH_FILE IN OUT  VARCHAR2, P_ISLOG NUMBER DEFAULT 0  )
    AS
    BEGIN
        IF P_ISLOG=0 THEN
            select F_GETDIRECTORY_AREA_NAME(DOCUMENTI.ID_DOCUMENTO),
                     TIPI_DOCUMENTO.ACRONIMO_MODELLO || '/' || to_char(trunc(DOCUMENTI.ID_DOCUMENTO/1000)) || '/' || 
                     DOCUMENTI.ID_DOCUMENTO ||  '/' || P_IDOGGETTO_FILE
               into P_DIRECTORY,P_PATH_FILE
              from TIPI_DOCUMENTO, AREE, DOCUMENTI, OGGETTI_FILE
            WHERE      DOCUMENTI.AREA=AREE.AREA
                     AND DOCUMENTI.ID_TIPODOC=TIPI_DOCUMENTO.ID_TIPODOC
                     AND OGGETTI_FILE.ID_OGGETTO_FILE  = P_IDOGGETTO_FILE
                     AND OGGETTI_FILE.ID_DOCUMENTO = DOCUMENTI.ID_DOCUMENTO;
         ELSE
            select F_GETDIRECTORY_AREA_NAME(DOCUMENTI.ID_DOCUMENTO),
                     TIPI_DOCUMENTO.ACRONIMO_MODELLO || '/' || to_char(trunc(DOCUMENTI.ID_DOCUMENTO/1000)) || '/' || 
                     DOCUMENTI.ID_DOCUMENTO ||  '/LOG_' || TO_CHAR(OGGETTI_FILE_LOG.ID_LOG) || '/' || OGGETTI_FILE_LOG.ID_OGGETTO_FILE
               into P_DIRECTORY,P_PATH_FILE
              from TIPI_DOCUMENTO, AREE, DOCUMENTI, ACTIVITY_LOG, OGGETTI_FILE_LOG
            WHERE      DOCUMENTI.AREA=AREE.AREA
                     AND DOCUMENTI.ID_TIPODOC=TIPI_DOCUMENTO.ID_TIPODOC
                     AND ACTIVITY_LOG.ID_LOG = OGGETTI_FILE_LOG.ID_LOG 
                     AND ACTIVITY_LOG.ID_DOCUMENTO = DOCUMENTI.ID_DOCUMENTO
                     AND OGGETTI_FILE_LOG.ID_OGGETTO_FILE_LOG =P_IDOGGETTO_FILE;
         END IF;
          
             SELECT  directory_path
                INTO P_PATH_DIR_FS
                FROM ALL_DIRECTORIES
                WHERE  upper(directory_name) = P_DIRECTORY;
    END;
    PROCEDURE DELETEOGGETTOFILE(P_IDOGGETTO_FILE NUMBER) 
    AS
    a_dir VARCHAR2(1000);
    a_path_dir_fs VARCHAR2(1000);
    a_path_file VARCHAR2(1000);
    a_isFileFs NUMBER(1) :=0;
    BEGIN
        IF IS_FS_FILE(P_IDOGGETTO_FILE)=1 THEN
            GETPATH_FILE_FS( P_IDOGGETTO_FILE, a_dir,a_path_dir_fs ,  a_path_file );            
            a_isFileFs:=1;       
        END IF;
        
      DELETE FROM OGGETTI_FILE WHERE ID_OGGETTO_FILE = P_IDOGGETTO_FILE;
       
       IF a_isFileFs=1 THEN      
             --SYS.DBMS_BACKUP_RESTORE.DELETEFILE(a_path_dir_fs||'/'||replace(a_path_file,'$','\$'));       
			 gdm_utility.RMDIR( a_path_dir_fs || '/' ||  a_path_file ,1);			 
        END IF;
    END;
    FUNCTION DOWNLOADOGGETTOFILE_LOG(P_IDOGGETTO_FILE_LOG NUMBER) RETURN BLOB
    IS
    RET BLOB := EMPTY_BLOB(); 
    a_dir VARCHAR2(1000);
    a_path_dir_fs VARCHAR2(1000);
    a_path_file VARCHAR2(1000);  
    fils       BFILE ;      
    BEGIN
        IF IS_FS_FILE(P_IDOGGETTO_FILE_LOG,1)=1 THEN
             GETPATH_FILE_FS( P_IDOGGETTO_FILE_LOG, a_dir,a_path_dir_fs ,  a_path_file,1 );
              fils  := BFILENAME(a_dir,a_path_file);
              if  nvl(dbms_lob.getlength(fils),0)>0 then
                  dbms_lob.fileopen(fils, dbms_lob.file_readonly); 
                   DBMS_LOB.CREATETEMPORARY(RET,TRUE,dbms_lob.call);
                  dbms_lob.loadfromfile(RET, fils,DBMS_LOB.LOBMAXSIZE);    
                  dbms_lob.fileclose(fils);    
              ELSE 
                    RET:=NULL;
              end if;                              
        ELSE
            SELECT TESTOOCR
                INTO RET
               FROM OGGETTI_FILE_LOG
             WHERE ID_OGGETTO_FILE_LOG = P_IDOGGETTO_FILE_LOG;
             
             if  nvl(dbms_lob.getlength(RET),0)=0 then
                 RET := null;
             end if;
        END IF;
                 
         
        RETURN RET;
    END;
    
    FUNCTION DOWNLOADOGGETTOFILE(P_IDOGGETTO_FILE NUMBER) RETURN BLOB
    IS
    RET BLOB := EMPTY_BLOB(); 
    a_dir VARCHAR2(1000);
    a_path_dir_fs VARCHAR2(1000);
    a_path_file VARCHAR2(1000);  
    fils       BFILE ;  
    BEGIN
        IF IS_FS_FILE(P_IDOGGETTO_FILE)=1 THEN
              GETPATH_FILE_FS( P_IDOGGETTO_FILE, a_dir,a_path_dir_fs ,  a_path_file );    
              fils  := BFILENAME(a_dir,a_path_file);
              if  nvl(dbms_lob.getlength(fils),0)>0 then
                  dbms_lob.fileopen(fils, dbms_lob.file_readonly); 
                   DBMS_LOB.CREATETEMPORARY(RET,TRUE,dbms_lob.call);
                  dbms_lob.loadfromfile(RET, fils,DBMS_LOB.LOBMAXSIZE);    
                  dbms_lob.fileclose(fils);    
              ELSE 
                    RET:=NULL;
              end if;              
        ELSE     
            SELECT TESTOOCR
                INTO RET
               FROM OGGETTI_FILE
             WHERE ID_OGGETTO_FILE = P_IDOGGETTO_FILE;
             
             if  nvl(dbms_lob.getlength(RET),0)=0 then
                 RET := null;
             end if;
        END IF;
                 
         
        RETURN RET;
    END DOWNLOADOGGETTOFILE;
    PROCEDURE DOWNLOADOGGETTOFILE_TMP(P_IDOGGETTO_FILE NUMBER)
    AS
    RET BLOB := EMPTY_BLOB(); 
    a_dir VARCHAR2(1000);
    a_path_dir_fs VARCHAR2(1000);
    a_path_file VARCHAR2(1000);  
    fils       BFILE ;      
    PRAGMA AUTONOMOUS_TRANSACTION;    
    BEGIN
         delete from TMP_FILE;
         insert into  TMP_FILE (FILE_TEMPORANY) values (EMPTY_BLOB());
         COMMIT;
         
       IF IS_FS_FILE(P_IDOGGETTO_FILE)=1 THEN
              GETPATH_FILE_FS( P_IDOGGETTO_FILE, a_dir,a_path_dir_fs ,  a_path_file );    
              fils  := BFILENAME(a_dir,a_path_file);
              if  nvl(dbms_lob.getlength(fils),0)>0 then
                       
                    SELECT FILE_TEMPORANY INTO RET FROM TMP_FILE  FOR UPDATE;
                  dbms_lob.fileopen(fils, dbms_lob.file_readonly); 
                 --  DBMS_LOB.CREATETEMPORARY(RET,TRUE,dbms_lob.call);
                  dbms_lob.loadfromfile(RET, fils,DBMS_LOB.LOBMAXSIZE);    
                  dbms_lob.fileclose(fils);    
              ELSE 
                    RET:=NULL;
              end if;              
        ELSE     
            SELECT TESTOOCR
                INTO RET
               FROM OGGETTI_FILE
             WHERE ID_OGGETTO_FILE = P_IDOGGETTO_FILE;
                     
             if  nvl(dbms_lob.getlength(RET),0)=0 then
                 RET := null;
             end if;
        END IF;
                         
        IF RET IS NOT NULL THEN
            update TMP_FILE
            set FILE_TEMPORANY=RET;
                
            BEGIN
                  dbms_lob.fileclose(fils);    
            EXCEPTION WHEN OTHERS THEN 
                    NULL;      
            END;
        END IF;
            
        COMMIT;
                 
           
    EXCEPTION WHEN OTHERS THEN
        BEGIN
              dbms_lob.fileclose(fils);    
        EXCEPTION WHEN OTHERS THEN 
                NULL;      
        END;
        ROLLBACK;
        RAISE;         
    END DOWNLOADOGGETTOFILE_TMP;    
END;
/

CREATE PACKAGE GDM_UTILITY AS
   FUNCTION PARSEQUERYSTRING (P_QRYSTR IN VARCHAR2,P_CAMPO IN VARCHAR2) RETURN VARCHAR2;
   FUNCTION SOVRASCRIVI_SEMPRE (P_AREA IN VARCHAR2,P_TIPODOC IN VARCHAR2,P_STATUS IN VARCHAR2) RETURN VARCHAR2;
   FUNCTION PARSEBODY_CF (P_IDDOCUMENTO IN NUMBER,P_BODY IN VARCHAR2) RETURN VARCHAR2;
   PROCEDURE extract_file (idobj IN NUMBER, idlog IN NUMBER);
   PROCEDURE MKDIR(A_PERCORSO VARCHAR2);
   PROCEDURE RMDIR(A_PERCORSO VARCHAR2, A_FILE NUMBER DEFAULT 0);
   PROCEDURE CHMOD(A_PERCORSO VARCHAR2) ;
END GDM_UTILITY;
/

CREATE PACKAGE BODY GDM_UTILITY AS
   FUNCTION PARSEQUERYSTRING (P_QRYSTR IN VARCHAR2,P_CAMPO IN VARCHAR2) RETURN VARCHAR2 AS
          SEPARATOR VARCHAR2(1) := ',';
         EQUALS VARCHAR2(1) := '=';
         QRYSTR  VARCHAR2(32000);
      BEGIN
          QRYSTR := P_QRYSTR;
            --CASO BASE
         --STRINGA DEL TIPO CAMPO=VALORE
         IF (INSTR(QRYSTR,SEPARATOR)=0) THEN
            IF (SUBSTR(QRYSTR,1,INSTR(QRYSTR,EQUALS)-1)=P_CAMPO) THEN
              RETURN SUBSTR(QRYSTR,INSTR(QRYSTR,EQUALS)+1,LENGTH(QRYSTR));
           END IF;
            --CASO GENERALE
         --STRINGA DEL TIPO CAMPO1=VALORE1&CAMPO2=VALORE2&.....&CAMPON=VALOREN
         ELSE
            DECLARE
             D_INDEX_CAMPO NUMBER;
            D_VALORESINGOLO VARCHAR2(2000);
              BEGIN
             D_INDEX_CAMPO:=INSTR(SEPARATOR||QRYSTR,SEPARATOR||P_CAMPO);
            D_VALORESINGOLO:=SUBSTR(SEPARATOR||QRYSTR||SEPARATOR,D_INDEX_CAMPO+1,
                                                 INSTR(SEPARATOR||QRYSTR||SEPARATOR,SEPARATOR,D_INDEX_CAMPO+1)-
                                      D_INDEX_CAMPO-1);
            RETURN PARSEQUERYSTRING(D_VALORESINGOLO,P_CAMPO);
           END;
         END IF;
         RETURN SEPARATOR;
      END;
   FUNCTION SOVRASCRIVI_SEMPRE (P_AREA IN VARCHAR2,P_TIPODOC IN VARCHAR2,P_STATUS IN VARCHAR2) RETURN VARCHAR2 AS
      BEGIN
          RETURN 'N';
      END;
   FUNCTION PARSEBODY_CF (P_IDDOCUMENTO IN NUMBER,P_BODY IN VARCHAR2) RETURN VARCHAR2 AS
         A_RET              NUMBER(1) := 0;
        ACTUALINDEX_I       NUMBER(10) := 0;
        ACTUALINDEX_F       NUMBER(10) := 0;
        A_VALORE_SOSTITUITO VARCHAR2(32000);
        A_BODY              VARCHAR2(32000) :='';
        A_SEPARATOR         VARCHAR2(1) :='#';
      BEGIN
            --SOSTITUZIONE DEI PARAMETRI
         ACTUALINDEX_I := INSTR(P_BODY,A_SEPARATOR);
         ACTUALINDEX_F := INSTR(P_BODY,A_SEPARATOR,ACTUALINDEX_I+1);
         A_BODY        := P_BODY;
         WHILE ACTUALINDEX_F<>0 LOOP
            --ESISTE IL # FINALE
            DECLARE
            A_PARAMETRO VARCHAR2(500);
            BEGIN
                A_PARAMETRO:=SUBSTR(A_BODY,ACTUALINDEX_I+1,ACTUALINDEX_F-ACTUALINDEX_I-1);
             BEGIN
                   SELECT NVL(DBMS_LOB.SUBSTR(VALORE_CLOB, DBMS_LOB.GETLENGTH(VALORE_CLOB),1), NVL(TO_CHAR(VALORE_DATA, REPLACE(FORMATO_DATA, 'hh:', 'hh24:')),TO_CHAR(VALORE_NUMERO) ))
                        INTO A_VALORE_SOSTITUITO
                     FROM VALORI VA, CAMPI_DOCUMENTO CD, DATI D, DOCUMENTI DOC
                    WHERE VA.ID_DOCUMENTO   = P_IDDOCUMENTO
                      AND D.DATO = CD.NOME
                      AND VA.ID_CAMPO=CD.ID_CAMPO
                      AND CD.ID_TIPODOC = DOC.ID_TIPODOC
                      AND DOC.ID_DOCUMENTO = P_IDDOCUMENTO
                      AND D.AREA = DOC.AREA AND D.DATO=A_PARAMETRO;
                    EXCEPTION WHEN NO_DATA_FOUND THEN
                      A_VALORE_SOSTITUITO:=A_PARAMETRO;
                           WHEN OTHERS THEN
                      A_VALORE_SOSTITUITO:=A_PARAMETRO;
             END;
             A_BODY:=SUBSTR(A_BODY,1,ACTUALINDEX_I-1)||A_VALORE_SOSTITUITO||SUBSTR(A_BODY,ACTUALINDEX_F+1,LENGTH(A_BODY));
            END;
            ACTUALINDEX_I := INSTR(A_BODY,A_SEPARATOR);
            ACTUALINDEX_F := INSTR(A_BODY,A_SEPARATOR,ACTUALINDEX_I+1);
            END LOOP;
         RETURN A_BODY;
      END;
    PROCEDURE extract_file (idobj IN NUMBER, idlog IN NUMBER)
       IS
       vblob      BLOB;
       vstart     NUMBER                       := 1;
       bytelen    NUMBER                       := 32000;
       len        NUMBER;
       filename   oggetti_file.filename%TYPE;
       my_vr      RAW (32000);
       x          NUMBER;
       l_output   UTL_FILE.file_type;
    BEGIN
       IF idlog > 0
       THEN
          SELECT DBMS_LOB.getlength (testoocr), filename, testoocr
            INTO len, filename, vblob
            FROM oggetti_file_log
           WHERE id_oggetto_file = idobj AND id_log = idlog;
       ELSE
          SELECT DBMS_LOB.getlength (testoocr), filename, testoocr
            INTO len, filename, vblob
            FROM oggetti_file
           WHERE id_oggetto_file = idobj;
       END IF;
       IF len=0 THEN
          RETURN;
       END IF;
       l_output := UTL_FILE.fopen ('TEMP_FILE', idobj, 'wb', 32760);
       vstart := 1;
       bytelen := 32000;
       x := len;
       IF len < 32760
       THEN
          UTL_FILE.put_raw (l_output, vblob);
          UTL_FILE.fflush (l_output);
       ELSE
          vstart := 1;
          WHILE vstart < len AND bytelen > 0
          LOOP
             DBMS_LOB.READ (vblob, bytelen, vstart, my_vr);
             UTL_FILE.put_raw (l_output, my_vr);
             UTL_FILE.fflush (l_output);
             vstart := vstart + bytelen;
             x := x - bytelen;
             IF x < 32000
             THEN
                bytelen := x;
             END IF;
          END LOOP;
       END IF;
       UTL_FILE.fclose (l_output);
    END;
   PROCEDURE MKDIR(A_PERCORSO VARCHAR2)
   AS
   A_CMD                            VARCHAR2(1000);
   A_RESULT                         NUMBER;
   BEGIN
      A_CMD:='mkdir -m 777 -p '||A_PERCORSO;
      execute immediate 'call os_command.exec('''||A_CMD||''') into :A_RESULT ' using out A_RESULT;
      IF A_RESULT<>0 THEN
         RAISE_APPLICATION_ERROR(-20999,'Errore nel creare/verificare il percorso '||A_PERCORSO);
      END IF;
   EXCEPTION WHEN OTHERS THEN
      -- TENTO DI ESEGURILO WINDOWS
      BEGIN
           A_CMD:='cmd.exe /c IF exist '||A_PERCORSO||' ( echo dir exists ) ELSE ( mkdir '||A_PERCORSO||' ) ';

        execute immediate 'call os_command.exec('''||A_CMD||''') into :A_RESULT ' using out A_RESULT;
          IF A_RESULT<>0 THEN
             RAISE_APPLICATION_ERROR(-20999,'Errore nel creare il percorso '||A_PERCORSO);
          END IF;
      EXCEPTION WHEN OTHERS THEN
         RAISE_APPLICATION_ERROR(-20999,'Errore in MKDIR('||A_PERCORSO||'): '||sqlerrm);
      END;
   END;

   PROCEDURE RMDIR(A_PERCORSO VARCHAR2, A_FILE NUMBER DEFAULT 0)
   AS
   A_CMD                            VARCHAR2(1000);
   A_RESULT                         NUMBER;
   BEGIN
      IF A_FILE=1 THEN
          A_CMD:='rm '||A_PERCORSO;
      ELSE
          A_CMD:='rm -rf '||A_PERCORSO;
      END IF;
      execute immediate 'call os_command.exec('''||A_CMD||''') into :A_RESULT ' using out A_RESULT;
      IF A_RESULT<>0 THEN
         RAISE_APPLICATION_ERROR(-20999,'Errore nel rimuovere il percorso '||A_PERCORSO);
      END IF;
   EXCEPTION WHEN OTHERS THEN
      -- TENTO DI ESEGURILO WINDOWS
      BEGIN
           IF A_FILE=1 THEN
               A_CMD:='cmd.exe /c IF not exist '||A_PERCORSO||' ( echo dir noexists ) ELSE ( del '||A_PERCORSO||' ) ';
           ELSE
                A_CMD:='cmd.exe /c IF not exist '||A_PERCORSO||' ( echo dir noexists ) ELSE ( rmdir '||A_PERCORSO||' ) ';
           END IF;

        execute immediate 'call os_command.exec('''||A_CMD||''') into :A_RESULT ' using out A_RESULT;
          IF A_RESULT<>0 THEN
             RAISE_APPLICATION_ERROR(-20999,'Errore nel rimuovere il percorso '||A_PERCORSO);
          END IF;
      EXCEPTION WHEN OTHERS THEN
         RAISE_APPLICATION_ERROR(-20999,'Errore in RMDIR('||A_PERCORSO||'): '||sqlerrm);
      END;

   END;

   PROCEDURE CHMOD(A_PERCORSO VARCHAR2)
   AS
   A_CMD                            VARCHAR2(1000);
   A_RESULT                         NUMBER;
   BEGIN
      A_CMD:='chmod 777 '||A_PERCORSO;
      execute immediate 'call os_command.exec('''||A_CMD||''') into :A_RESULT ' using out A_RESULT;
      IF A_RESULT<>0 THEN
         RAISE_APPLICATION_ERROR(-20999,'Errore nel fornire i diritti al percorso '||A_PERCORSO);
      END IF;
   EXCEPTION WHEN OTHERS THEN
      if instr(sqlerrm,'Cannot run program')   >0 then
         NULL;
      ELSE
          RAISE_APPLICATION_ERROR(-20999,'Errore in CHMOD('||A_PERCORSO||'): '||sqlerrm);
      end if;
   END;

END GDM_UTILITY;
/

grant execute on gdm_oggetti_file to ${global.db.agsde2.username}
/