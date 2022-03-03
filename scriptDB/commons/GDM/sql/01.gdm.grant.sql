--liquibase formatted sql
--changeset rdestasio:install_20200221_02_gdm_crea runOnChange:true

GRANT SELECT  ON OGGETTI_FILE 	TO ${global.db.target.username}
/

GRANT EXECUTE ON F_VALORE_CAMPO 	TO ${global.db.target.username}
/

grant select on gat_determina to ${global.db.target.username}
/

grant select on gat_delibera to ${global.db.target.username}
/

grant select on gat_proposta_delibera to ${global.db.target.username}
/

grant select on gat_seduta_stampa to ${global.db.target.username}
/

grant select on impronte_file to ${global.db.target.username}
/

-- il pkg gdm_oggetti_file non è presente in alcune installazioni. per questo motivo metto la grant in execute-immediate.
begin
    execute immediate 'grant execute on gdm_oggetti_file to ${global.db.target.username}';
exception when others then
    null;
end;
/