--liquibase formatted sql
--changeset rdestasio:2.0.3.0_20200221_24

/* Formatted on 09/02/2016 10:03:37 (QP5 v5.215.12089.38647) */
CREATE OR REPLACE FORCE VIEW ASS_FILE_ALLEGATI_VUOTI
(
   ID_ALLEGATO,
   DESCRIZIONE,
   NOME,
   ID_PROPOSTA_DELIBERA,
   ID_DELIBERA,
   ID_DETERMINA
)
AS
   SELECT a.id_allegato,
          a.descrizione,
          F.NOME,
          a.id_proposta_delibera,
          a.id_delibera,
          a.id_determina
     FROM allegati a, allegati_file af, file_allegati f
    WHERE     A.ID_ALLEGATO = af.id_allegato
          AND f.id_file_allegato = af.id_file
          AND (   NOT EXISTS
                     (SELECT '1'
                        FROM gdm_oggetti_file o
                       WHERE O.ID_OGGETTO_FILE = F.ID_FILE_ESTERNO)
               OR EXISTS
                     (SELECT '1'
                        FROM gdm_oggetti_file o
                       WHERE     O.ID_OGGETTO_FILE = F.ID_FILE_ESTERNO
                             AND NVL (DBMS_LOB.getlength (testoocr), 0) < 1))
/
