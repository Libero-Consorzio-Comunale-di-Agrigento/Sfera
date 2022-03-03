--liquibase formatted sql
--changeset svalenti:2.5.6.0_20210928_05

DECLARE
x   NUMBER;
    y   NUMBER;
    d_ente VARCHAR2(255);
    TYPE T_ARRAY_OF_VARCHAR IS TABLE OF VARCHAR2(2000) INDEX BY BINARY_INTEGER;
    my_array T_ARRAY_OF_VARCHAR;
BEGIN

SELECT COUNT (1)
INTO x
FROM mapping_integrazioni
WHERE categoria='CONTABILITA_ASCOT';

IF (x > 0) THEN

SELECT COUNT (1)
INTO y
FROM mapping_integrazioni
WHERE categoria='CONTABILITA_ASCOT' AND codice='GESTIONE_INTERNA';

IF (y = 0) THEN

    d_ente := impostazioni_pkg.get_impostazione ('ENTI_SO4', '*');

    FOR current_row IN (
        WITH test AS (SELECT d_ente FROM dual)
            SELECT regexp_substr(d_ente, '[^#]+', 1, rownum) SPLIT from test
        CONNECT BY LEVEL <= length (regexp_replace(d_ente, '[^#]+'))  + 1)
    LOOP
        INSERT INTO  mapping_integrazioni(categoria, codice, valore_esterno, ente, id_mapping_integrazione, sequenza, valore_interno)
        VALUES ('CONTABILITA_ASCOT','GESTIONE_INTERNA','N',current_row.SPLIT,hibernate_sequence.nextval,0,'*');

        UPDATE mapping_integrazioni
        SET valore_esterno = valore_esterno || 'services'
        WHERE categoria='CONTABILITA_ASCOT' AND codice='URL_WEBSERVICE' AND ente=current_row.SPLIT;

        my_array(my_array.COUNT) := current_row.SPLIT;
    END LOOP;

COMMIT;
END IF;

END IF;
END;
/