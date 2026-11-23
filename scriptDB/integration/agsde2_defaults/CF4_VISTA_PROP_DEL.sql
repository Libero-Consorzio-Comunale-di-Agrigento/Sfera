--liquibase formatted sql
--changeset rdestasio:install_20200221_defaults_01 runOnChange:true stripComments:false failOnError:false

declare
  v_object_type VARCHAR2(20);
  d_valore      number(10);
begin
  d_valore := 0;
  -- Controlla il tipo di oggetto CF4_VISTA_PROP_DEL nello schema corrente
  begin
    select object_type
      into v_object_type
      from user_objects
     where object_name = 'CF4_VISTA_PROP_DEL';
  exception when NO_DATA_FOUND then
    v_object_type := null;
  end;

  if v_object_type is null or v_object_type = 'VIEW' then
    if v_object_type = 'VIEW' then
      begin
        execute immediate 'select count (1) from CF4_VISTA_PROP_DEL' into d_valore;
      exception when others then
          null;
      end;
    end if;

    if v_object_type is null or (d_valore = 1) then
      -- In installazione (oggetto non esistente) o vista fake del nostro user la creiamo/ricreiamo
      execute immediate  'CREATE OR REPLACE FORCE VIEW CF4_VISTA_PROP_DEL
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
                             IMPORTO_ATTUALE,
                             IMPEGNO_OBBLIGATORIO)
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
                       NULL,
                       NULL
                FROM DUAL';
    end if;
  end if;
end;
/