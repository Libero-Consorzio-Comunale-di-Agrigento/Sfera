--liquibase formatted sql
--changeset rdestasio:AGSPR_PROCEDURE_COMPILE_ALL runOnChange:true stripComments:false

CREATE OR REPLACE PROCEDURE compile_all
/******************************************************************************
 NOME:        Compile_All
 DESCRIZIONE: Compilazione di tutti gli oggetti invalidi presenti nel DB.
 ARGOMENTI:   p_java_class NUMBER indica se deve essere effettuata la
                                  compilazione anche degli oggetti di tipo
                                  JAVA CLASS.
 ANNOTAZIONI: Tenta la compilazione in cicli successivi.
              Termina la compilazione quando il numero degli oggetti
              invalidi non varia rispetto al ciclo precedente.
 REVISIONI:
 Rev. Data        Autore  Descrizione
 ---- ----------  ------  ----------------------------------------------------
 1    23/01/2001  MF      Inserimento commento.
 2    17/12/2003  MM      Aggiunta compilazione classi java
 4    14/12/2006  MM      Introduzione del parametro p_java_class.
 5    08/10/2007  FT      Aggiunta compilazione synonym
 6    12/12/2007  FT      compile_all: esclusione degli oggetti il cui nome
                          inizia con 'BIN$'
******************************************************************************/
( p_java_class in number default 1 )
IS
   d_obj_name       VARCHAR2(30);
   d_obj_type       VARCHAR2(30);
   d_command        VARCHAR2(200);
   d_cursor         INTEGER;
   d_rows           INTEGER;
   d_old_rows       INTEGER;
   d_return         INTEGER;
   s_oracle_ver     integer;
   CURSOR c_obj IS
      SELECT object_name, object_type
        FROM OBJ
       WHERE ( object_type IN ( 'PROCEDURE'
                              , 'TRIGGER'
                              , 'FUNCTION'
                              , 'PACKAGE'
                              , 'PACKAGE BODY'
                              , 'VIEW')
             OR (object_type = 'JAVA CLASS' AND p_java_class = 1)
             OR (object_type = 'SYNONYM' AND (SELECT to_number(substr(version, 0, instr(version, '.')-1)) FROM PRODUCT_COMPONENT_VERSION where product like 'Oracle%') >= 10)
             )
       AND   status = 'INVALID'
       AND   substr( object_name, 1, 4 ) != 'BIN$'
      ORDER BY  DECODE(object_type
                      ,'PACKAGE',1
                      ,'PACKAGE BODY',2
                      ,'FUNCTION',3
                      ,'PROCEDURE',4
                      ,'VIEW',5
                             ,6)
              , object_name
      ;
BEGIN
   d_old_rows := 0;
   LOOP
      d_rows := 0;
      BEGIN
         OPEN  c_obj;
         LOOP
            BEGIN
               FETCH c_obj INTO d_obj_name, d_obj_type;
               EXIT WHEN c_obj%NOTFOUND;
               d_rows := d_rows + 1;
               IF d_obj_type = 'PACKAGE BODY' THEN
                  d_command := 'alter PACKAGE '||d_obj_name||' compile BODY';
               ELSIF d_obj_type = 'JAVA CLASS' THEN
                  d_command := 'alter '||d_obj_type||' "'||d_obj_name||'" compile';
               ELSE
                  d_command := 'alter '||d_obj_type||' '||d_obj_name||' compile';
               END IF;
               d_cursor  := DBMS_SQL.OPEN_CURSOR;
               DBMS_SQL.PARSE(d_cursor,d_command,dbms_sql.native);
               d_return := DBMS_SQL.EXECUTE(d_cursor);
               DBMS_SQL.CLOSE_CURSOR(d_cursor);
            EXCEPTION
               WHEN OTHERS THEN NULL;
            END;
         END LOOP;
         CLOSE c_obj;
      END;
      IF d_rows = d_old_rows THEN
         EXIT;
      ELSE
         d_old_rows := d_rows;
      END IF;
   END LOOP;
   IF d_rows > 0 THEN
      RAISE_APPLICATION_ERROR(-20999,'Esistono n.'||TO_CHAR(d_rows)||' Oggetti di DataBase non validabili !');
   END IF;
END Compile_All;
/

begin
  compile_all();
end;
/