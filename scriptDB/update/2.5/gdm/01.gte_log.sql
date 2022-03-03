--liquibase formatted sql
--changeset rdestasio:01.gte_log runOnChange:true stripComments:false failOnError:false

CREATE TABLE gte_log
(
    ID_GTE_LOG               NUMBER NOT NULL,
    ID_DOCUMENTO             NUMBER,
    STATO                    VARCHAR2 (255),
    OPERAZIONE               VARCHAR2 (255),
    APPLICATIVO              VARCHAR2 (255),
    NOME_FILE                VARCHAR2 (255),
    ESTREMI_DOCUMENTO        VARCHAR2 (255),
    DATA_FINE_ELABORAZIONE   DATE,
    ERRORE                   CLOB,
    UTENTE_INS               VARCHAR2 (255) NOT NULL,
    DATA_INS                 DATE NOT NULL,
    UTENTE_UPD               VARCHAR2 (255) NOT NULL,
    DATA_UPD                 DATE NOT NULL,
    VALIDO                   CHAR (1)
)
/