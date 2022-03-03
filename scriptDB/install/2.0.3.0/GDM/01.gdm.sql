begin
	execute immediate 'DROP TRIGGER ${global.db.gdm.username}.AGSDE2_DELI_TU';
exception when others then
	null;
end;
/

begin
	execute immediate 'DROP TRIGGER ${global.db.gdm.username}.AGSDE2_DETE_TU';
exception when others then
	null;
end;
/

create or replace synonym AGSDE2_CONS_DETERMINE for ${global.db.target.username}.CONS_DETERMINE
/

create or replace synonym AGSDE2_CONS_DETERMINE_ALLEGATI for ${global.db.target.username}.CONS_DETERMINE_ALLEGATI
/

create or replace synonym AGSDE2_CONS_DETE_COLLEGATE for ${global.db.target.username}.CONS_DETERMINE_COLLEGATE
/

create or replace synonym AGSDE2_CONS_DELIBERE for ${global.db.target.username}.CONS_DELIBERE
/

create or replace synonym AGSDE2_CONS_DELIBERE_ALLEGATI for ${global.db.target.username}.CONS_DELIBERE_ALLEGATI
/

create or replace synonym AGSDE2_IMPOSTAZIONI for ${global.db.target.username}.IMPOSTAZIONI
/

-- ripulisco eventuali dati "sporcati" dal tentativo di gestire da trigger il cambio di classifica/fascicolo
update gat_delibera d set ente = substr(ente, length('UPDATE_DA_AGSDE2-')+1) where ente like 'UPDATE_DA_AGSDE2%'
/

update gat_determina d set ente = substr(ente, length('UPDATE_DA_AGSDE2-')+1) where ente like 'UPDATE_DA_AGSDE2%'
/

update gat_certificato d set ente = substr(ente, length('UPDATE_DA_AGSDE2-')+1) where ente like 'UPDATE_DA_AGSDE2%'
/

update gat_vistoparere d set ente = substr(ente, length('UPDATE_DA_AGSDE2-')+1) where ente like 'UPDATE_DA_AGSDE2%'
/

update gat_allegato d set ente = substr(ente, length('UPDATE_DA_AGSDE2-')+1) where ente like 'UPDATE_DA_AGSDE2%'
/