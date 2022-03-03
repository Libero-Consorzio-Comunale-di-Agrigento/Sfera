--liquibase formatted sql
--changeset rdestasio:install_20200221_03

-- Script di migrazione delle tipologie nella tabella competenze_delega

DECLARE
   d_id_applicativo   NUMBER;
   d_modulo           VARCHAR2 (10) := 'AGSDE2';
   d_istanza           VARCHAR2 (10) := 'AGSDE2';
BEGIN
   INSERT INTO SO4_APPLICATIVI (DESCRIZIONE, ISTANZA, MODULO)
      SELECT 'Gestione Atti Amministrativi', d_istanza, d_modulo
        FROM DUAL
       WHERE NOT EXISTS
                    (SELECT id_applicativo
                       FROM so4_applicativi appl, ad4_istanze ista
                      WHERE     modulo = d_modulo
                            AND ista.user_oracle = USER
                            AND appl.istanza = ista.istanza
                            AND ista.istanza = d_istanza
                            AND ROWNUM = 1);

   SELECT id_applicativo
     INTO d_id_applicativo
     FROM so4_applicativi appl, ad4_istanze ista
    WHERE     modulo = d_modulo
          AND ista.user_oracle = USER
          AND appl.istanza = ista.istanza;

   FOR tipo IN (SELECT * FROM tipi_determina order by titolo)
   LOOP
      DECLARE
         d_dataval_al   DATE := NULL;
      BEGIN
         IF NVL (tipo.valido, 'Y') = 'N'
         THEN
            d_dataval_al := SYSDATE;
         END IF;

         IF NVL (tipo.valido, 'Y') = 'Y'
         THEN
            d_dataval_al := NULL;
         END IF;

         so4_competenze_delega_tpk.ins (NULL,
                                        tipo.id_tipo_determina,
                                        tipo.titolo,
                                        d_id_applicativo,
                                        d_dataval_al);
      END;
   END LOOP;

   FOR tipo IN (SELECT * FROM tipi_delibera order by titolo)
   LOOP
      DECLARE
         d_dataval_al   DATE := NULL;
      BEGIN
         IF NVL (tipo.valido, 'Y') = 'N'
         THEN
            d_dataval_al := SYSDATE;
         END IF;

         IF NVL (tipo.valido, 'Y') = 'Y'
         THEN
            d_dataval_al := NULL;
         END IF;

         so4_competenze_delega_tpk.ins (NULL,
                                        tipo.id_tipo_delibera,
                                        tipo.titolo,
                                        d_id_applicativo,
                                        d_dataval_al);
      END;
   END LOOP;

   FOR tipo IN (SELECT * FROM tipi_visto_parere order by titolo)
   LOOP
      DECLARE
         d_dataval_al   DATE := NULL;
      BEGIN
         IF NVL (tipo.valido, 'Y') = 'N'
         THEN
            d_dataval_al := SYSDATE;
         END IF;

         IF NVL (tipo.valido, 'Y') = 'Y'
         THEN
            d_dataval_al := NULL;
         END IF;

         so4_competenze_delega_tpk.ins (NULL,
                                        tipo.id_tipo_visto_parere,
                                        tipo.titolo,
                                        d_id_applicativo,
                                        d_dataval_al);
      END;
   END LOOP;

   COMMIT;
END;