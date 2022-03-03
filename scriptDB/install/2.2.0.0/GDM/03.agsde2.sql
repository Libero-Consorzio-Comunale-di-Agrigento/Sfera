revoke select, insert, update on file_allegati from ${global.db.gdm.username}
/

revoke select on determine from ${global.db.gdm.username}
/

revoke select on delibere from ${global.db.gdm.username}
/

revoke select on proposte_delibera from ${global.db.gdm.username}
/

revoke select on visti_pareri from ${global.db.gdm.username}
/

revoke select on certificati from ${global.db.gdm.username}
/

REVOKE SELECT ON HIBERNATE_SEQUENCE FROM ${global.db.gdm.username}
/
