begin
   execute immediate 'update applicativi_azioni
                         set funzione   = ''AGSDE2_JWF_UTILITY_PKG.GET_COMPETENZA''
                       where applicativo_id = 2
                             and codice_azione in (''CREA_DETERMINA'', ''CREA_DELIBERA'', ''ATTI'')';
   commit;
exception
   when others then
      null;
end;
/


create or replace synonym agsde2_jwf_utility_pkg for ${global.db.target.username}.jwf_utility_pkg
/
