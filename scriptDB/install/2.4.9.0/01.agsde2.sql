--liquibase formatted sql
--changeset rdestasio:2.4.9.0_20200221_01

/* Formatted on 11/12/2019 12:38:26 (QP5 v5.336) */
DECLARE
d_statement varchar2(4000);
d_usrGDM varchar2(100) := 'GDM';
BEGIN
   FOR ind
      IN ( SELECT DECODE (b.table_name, NULL, '****', 'ok')     Status,
       a.table_name,
       a.constraint_name,
       a.columns FK_COLUMNS,
       b.columns
  FROM (  SELECT a.table_name,
                 a.constraint_name,
                    MAX (
                        DECODE (position, 1, SUBSTR (column_name, 1, 30), NULL))
                 || MAX (
                        DECODE (position,
                                2, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (position,
                                3, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (position,
                                4, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (position,
                                5, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (position,
                                6, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (position,
                                7, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (position,
                                8, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (position,
                                9, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (position,
                                10, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (position,
                                11, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (position,
                                12, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (position,
                                13, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (position,
                                14, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (position,
                                15, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (position,
                                16, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))    columns
            FROM user_cons_columns a, user_constraints b
           WHERE     a.constraint_name = b.constraint_name
                 AND b.constraint_type = 'R'
        GROUP BY a.table_name, a.constraint_name) a,
       (  SELECT table_name,
                 index_name,
                    MAX (
                        DECODE (column_position,
                                1, SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                2, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                3, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                4, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                5, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                6, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                7, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                8, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                9, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                10, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                11, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                12, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                13, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                14, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                15, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))
                 || MAX (
                        DECODE (column_position,
                                16, ', ' || SUBSTR (column_name, 1, 30),
                                NULL))    columns
            FROM user_ind_columns
        GROUP BY table_name, index_name) b
 WHERE a.table_name = b.table_name(+) AND b.columns(+) LIKE a.columns || '%'
 AND b.table_name IS NULL
 ORDER BY 1,2,3)
   LOOP
      d_statement := 'create index '||ind.CONSTRAINT_NAME||' on '||ind.table_name||' ('||ind.FK_COLUMNS||')';
      DBMS_OUTPUT.put_line (d_statement||';');
      execute immediate d_statement;
   END LOOP;
END;
/

ALTER TABLE TIPI_DETERMINA ADD (QUERY_MOVIMENTI CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE TIPI_DELIBERA ADD (QUERY_MOVIMENTI CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE TIPI_VISTO_PARERE ADD (QUERY_MOVIMENTI CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE TIPI_DELIBERA ADD (ALLEGATO_TESTO_PROPOSTA CHAR(1 BYTE) DEFAULT 'N' NOT NULL)
/

INSERT INTO TIPI_ALLEGATO (CODICE, DATA_INS, DATA_UPD, DESCRIZIONE, ENTE, ID_MODELLO_TESTO, ID_TIPO_ALLEGATO, MODIFICABILE, MODIFICA_CAMPI, PUBBLICA_ALBO, PUBBLICA_CASA_DI_VETRO, STAMPA_UNICA, STATO_FIRMA, TIPOLOGIA, TITOLO, UTENTE_INS, UTENTE_UPD, VALIDO, VALIDO_AL, VALIDO_DAL, VERSION)
   SELECT 'ALLEGATO_TESTO_PROPOSTA', SYSDATE, SYSDATE,
          'Testo Proposta',
          (SELECT valore
             FROM impostazioni
            WHERE codice = 'ENTI_SO4' AND ente = '*'),
          NULL,
          HIBERNATE_SEQUENCE.NEXTVAL,
          'N',
          'N',
          'Y',
          'Y',
          'Y',
          'DA_NON_FIRMARE',
          'DELIBERA',
          'Testo Proposta',
          (SELECT utente_ins
             FROM tipi_delibera
            WHERE ROWNUM = 1),
          (SELECT utente_ins
             FROM tipi_delibera
            WHERE ROWNUM = 1),
          'Y',
          NULL,
          TRUNC (SYSDATE),
          0
     FROM DUAL
    WHERE     NOT EXISTS
                     (SELECT *
                        FROM TIPI_ALLEGATO
                       WHERE (    codice = 'ALLEGATO_TESTO_PROPOSTA'
                              AND tipologia = 'DELIBERA'))
          AND EXISTS (SELECT 1 FROM tipi_allegato)
/