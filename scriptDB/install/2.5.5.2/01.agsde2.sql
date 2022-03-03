--liquibase formatted sql
--changeset rdestasio:2.5.5.2_20200221_01
-- script presente nella 2.5.2.2 a volte non eseguito in fase di aggiornamento

begin
    execute immediate 'ALTER TABLE TIPI_DETERMINA ADD (eseguibilita_immediata CHAR(1 BYTE) DEFAULT ''N'' NOT NULL)';
exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE DETERMINE ADD (eseguibilita_immediata CHAR(1 BYTE) DEFAULT ''N'' NOT NULL)';
exception when others then null;
end;
/

begin
    execute immediate 'alter table DETERMINE add motivazioni_eseguibilita varchar2(255 byte) NULL';
exception when others then null;
end;
/

-- Gestione dei certificati di immediata eseguibilit.
begin
    execute immediate 'ALTER TABLE TIPI_DETERMINA ADD (ID_TIPO_CERT_IMM_ESEG NUMBER(19))';
exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE PROPOSTE_DELIBERA ADD (CONTROLLA_DESTINATARI CHAR(1 BYTE) DEFAULT ''N'' NOT NULL)';
exception when others then null;
end;
/

begin
    execute immediate 'ALTER TABLE DETERMINE ADD (CONTROLLA_DESTINATARI CHAR(1 BYTE) DEFAULT ''N'' NOT NULL)';
exception when others then null;
end;
/

update impostazioni set codice = 'SOGGETTI_FORMATO', descrizione = 'Indica il formato di stampa dei soggetti nelle stampe.' where codice = 'FIRMATARIO_FORMATO'
/

ALTER TABLE TIPI_BUDGET add (tipo VARCHAR2 (255 BYTE) DEFAULT 'BUDGET' NOT NULL ,
                             conto_economico VARCHAR2 (4000 BYTE))
/

ALTER TABLE TIPI_BUDGET_STORICO add (tipo VARCHAR2 (255 BYTE) DEFAULT 'BUDGET'  NOT NULL,
                                     conto_economico VARCHAR2 (4000 BYTE))
/

ALTER TABLE BUDGET add (annullato CHAR(1 BYTE) DEFAULT 'N' NOT NULL,
                        conto_economico VARCHAR2 (4000 BYTE),
                        codice_progetto VARCHAR2 (4000 BYTE),
                        codice_fornitore VARCHAR2 (4000 BYTE),
                        data_inizio_validita DATE,
                        data_fine_validita DATE)
/

ALTER TABLE TIPI_DELIBERA     ADD (PUBBLICA_ALLEGATI_VIS CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE TIPI_DETERMINA    ADD (PUBBLICA_ALLEGATI_VIS CHAR(1 BYTE) DEFAULT 'Y' NOT NULL)
/

CREATE OR REPLACE FORCE VIEW CE4_CONTI
            (
             CONTO,
             CONTO_ESTESO,
             DESCRIZIONE,
             TIPO_CONTO
                )
AS
SELECT cast(NULL as NUMBER(8)),cast(NULL as varchar2(4000)),cast(NULL as varchar2(4000)),cast(NULL as varchar2(1)) FROM DUAL
/

CREATE OR REPLACE FORCE VIEW CE4_FORNITORI
            (
             CONTO,
             CONTO_FORNITORE,
             RAGIONE_SOCIALE,
             TIPO_CONTO,
             PARTITA_IVA,
             CODICE_FISCALE
                )
AS
SELECT cast(NULL as NUMBER(8)),cast(NULL as varchar2(4000)),cast(NULL as varchar2(4000)),cast(NULL as varchar2(1)),cast(NULL as varchar2(100)),cast(NULL as varchar2(100)) FROM DUAL
/

--ricreo la vista CF4_VISTA_PROP_DEL se  nella versione dummy
declare
    d_esiste number(10);
    d_valore number(10);
begin
    -- CF4
    d_esiste := 0;
    d_valore := 0;

    --devo verificare se esiste le vista dummy
    select count (1) into d_esiste from all_views where upper(view_name) in ('CF4_VISTA_PROP_DEL') and upper(owner) = upper('${global.db.agsde2.username}');

    if (d_esiste = 1) then
        --se esite la vista, restituisce valori? (se non esiste "fisicamente" d errore)
        begin
            execute immediate 'select count (1) from CF4_VISTA_PROP_DEL' into d_valore;
        exception when others then
            null;
        end;

        if (d_valore = 1) then
            -- creo la vista dummy
            begin execute immediate
                'CREATE OR REPLACE FORCE VIEW CF4_VISTA_PROP_DEL
                            (
                             TIPO,
                             CODICE_TIPO,
                             E_S,
                             UNITA_PROP,
                             NUMERO_PROP,
                             ANNO_PROP,
                             SEDE_DEL,
                             NUMERO_DEL,
                             ANNO_DEL,
                             RIF_BIL_PEG,
                             ANNO,
                             NUMERO,
                             DESCRIZIONE,
                             IMPORTO,
                             CODICE_BENEFICIARIO,
                             RAGIONE_SOCIALE,
                             DATA,
                             COD_LIVELLO_5,
                             CODICE_SIOPE,
                             COD_TRANSAZIONE_ELEMENTARE,
                             CODICE_FISCALE_PARTITA_IVA,
                             NUMERO_IMP,
                             ANNO_IMP,
                             DESCR_VOCE_PEG,
                             FAT_DATA_SCADENZA,
                             IMP_ANNO_DEL,
                             IMP_NUMERO_DEL,
                             IMP_SEDE_DEL,
                             IMP_ID_PROPOSTA,
                             IMP_ANNO_PROPOSTA,
                             IMP_UNITA_PROPONENTE,
                             IMP_NUMERO_PROPOSTA,
                             IMPORTO_ATTUALE
                                )
                AS
                SELECT NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL,
                       NULL
                FROM DUAL';
            exception when others then null;
            end;
        end if;
    end if;
end;
/