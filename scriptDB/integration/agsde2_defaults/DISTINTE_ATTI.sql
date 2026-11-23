--liquibase formatted sql
--changeset rdestasio:install_20200221_defaults_03 runOnChange:true stripComments:false failOnError:false

declare
  v_object_type VARCHAR2(20);
  d_valore      number(10);
begin
  d_valore := 0;
  -- Controlla il tipo di oggetto DISTINTE_ATTI nello schema corrente
  begin
    select object_type
      into v_object_type
      from user_objects
     where object_name = 'DISTINTE_ATTI';
  exception when NO_DATA_FOUND then
      v_object_type := null;
  end;

  if v_object_type is null or v_object_type = 'VIEW' then
    if v_object_type = 'VIEW' then
      begin
        execute immediate 'select count (1) from DISTINTE_ATTI' into d_valore;
      exception when others then
          null;
      end;
    end if;

    if v_object_type is null or (d_valore = 1) then
      -- In installazione (oggetto non esistente) o vista fake del nostro user la creiamo/ricreiamo
      execute immediate 'CREATE OR REPLACE FORCE VIEW DISTINTE_ATTI (
                   ANNO_DISTINTA,
                   NUMERO_DISTINTA,
                   DATA_DISTINTA,
                   TIPO_DIST,
                   TIPO_DIST_DESCRI,
                   SCADENZA_DAL,
                   SCADENZA_AL,
                   ANNO_DEL,
                   NUMERO_DEL,
                   SEDE_DEL,
                   ID_PROPOSTA,
                   UNITA_PROPONENTE,
                   ANNO_PROPOSTA,
                   NUMERO_PROPOSTA,
                   ELENCO_IMPEGNI,
                   ELENCO_FORNITORI,
                   IMPORTO,
                   TITOLO
                )
                AS
                  SELECT
                      TO_NUMBER (NULL),
                      TO_NUMBER (NULL),
                      TO_DATE (NULL),
                      NULL,
                      NULL,
                      TO_DATE (NULL),
                      TO_DATE (NULL),
                      TO_NUMBER (NULL),
                      TO_NUMBER (NULL),
                      NULL,
                      TO_NUMBER (NULL),
                      NULL,
                      TO_NUMBER (NULL),
                      TO_NUMBER (NULL),
                      NULL,
                      NULL,
                      TO_NUMBER (NULL),
                      NULL
                  FROM DUAL';
    end if;
  end if;
end;
/