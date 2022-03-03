-- gestione dei file allegati "storici" su GDM
grant select, insert, update on file_allegati to ${global.db.gdm.username}
/

grant select on hibernate_sequence to ${global.db.gdm.username}
/

grant select on determine to ${global.db.gdm.username}
/

grant select on delibere to ${global.db.gdm.username}
/

grant select on proposte_delibera to ${global.db.gdm.username}
/

grant select on visti_pareri to ${global.db.gdm.username}
/

grant select on certificati to ${global.db.gdm.username}
/
