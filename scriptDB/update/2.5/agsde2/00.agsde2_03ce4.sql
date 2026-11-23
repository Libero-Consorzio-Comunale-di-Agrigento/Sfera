--liquibase formatted sql
--changeset mfrancesconi:2.5_20221007_00.agsde2_03ce4 failOnError:false

--creo la vista CE4_CONTI solo se non esiste
declare
    d_esiste number(10);
begin
    d_esiste := 0;

    --devo verificare se esiste la vista
    select count (1) into d_esiste from all_views where upper(view_name) in ('CE4_CONTI') and upper(owner) = upper('${global.db.agsde2.username}');

    if (d_esiste = 0) then
        --se non esite la vista la creo (in versione dummy)
        begin execute immediate
            'create force view CE4_CONTI
                                    (
                                       CONTO,
                                       CONTO_ESTESO,
                                       DESCRIZIONE,
                                       TIPO_CONTO
                                    )
                                    as
                                       select cast (null as number (8)),
                                              cast (null as varchar2 (4000)),
                                              cast (null as varchar2 (4000)),
                                              cast (null as varchar2 (1))
                                         from dual';

        exception when others then
            null;
        end;
    end if;
end;
/
--creo la vista CE4_FORNITORI solo se non esiste
declare
    d_esiste number(10);
begin
    d_esiste := 0;

    --devo verificare se esiste la vista
    select count (1) into d_esiste from all_views where upper(view_name) in ('CE4_FORNITORI') and upper(owner) = upper('${global.db.agsde2.username}');

    if (d_esiste = 0) then
        --se non esite la vista la creo (in versione dummy)
        begin execute immediate
            'create force view CE4_FORNITORI
                                    (
                                       CONTO,
                                       CONTO_FORNITORE,
                                       RAGIONE_SOCIALE,
                                       TIPO_CONTO,
                                       PARTITA_IVA,
                                       CODICE_FISCALE
                                    )
                                    as
                                       select cast (null as number (8)),
                                              cast (null as varchar2 (4000)),
                                              cast (null as varchar2 (4000)),
                                              cast (null as varchar2 (1)),
                                              cast (null as varchar2 (100)),
                                              cast (null as varchar2 (100))
                                         from dual';

        exception when others then
                    null;
        end;
    end if;
end;
/