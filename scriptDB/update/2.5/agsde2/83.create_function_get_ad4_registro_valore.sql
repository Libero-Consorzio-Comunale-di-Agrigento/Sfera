--liquibase formatted sql
--changeset mfrancesconi:2.5_20241127_83.create_function_get_ad4_registro_valore runOnChange:true stripComments:false failOnError:true

create or replace synonym ad4_key_error_log_pkg for ${global.db.ad4.username}.key_error_log_pkg
/

create or replace synonym ad4_registro for ${global.db.ad4.username}.registro
/

create or replace function get_ad4_registro_valore (
    p_chiave    varchar2,
    p_stringa   varchar2)
    return varchar2
is
    d_return   varchar2 (32000);
begin
    select valore
      into d_return
      from ad4_registro
     where chiave = p_chiave
       and stringa = p_stringa;

    return d_return;
exception when no_data_found then
        dbms_output.put_line (p_chiave || ' ' || p_stringa);
        return '';
end;
/