drop package agsde_competenze
/

CREATE OR REPLACE SYNONYM GDM_GAT_DETERMINA FOR ${global.db.gdm.username}.GAT_DETERMINA
/

CREATE OR REPLACE SYNONYM GDM_GAT_DELIBERA FOR ${global.db.gdm.username}.GAT_DELIBERA
/

CREATE OR REPLACE SYNONYM GDM_GAT_PROPOSTA_DELIBERA FOR ${global.db.gdm.username}.GAT_PROPOSTA_DELIBERA
/

create OR REPLACE synonym GDM_OGGETTI_FILE for ${global.db.gdm.username}.OGGETTI_FILE
/

grant execute on integrazione_gdm_pkg to ${global.db.gdm.username}
/

-- aggiorno le dimensioni dei file (presenti su gdm)
merge into file_allegati fa
using (SELECT f.id_file_allegato,
       F.NOME,
       nvl(length(gom.testoocr), 0) dimensione_file
  FROM file_allegati f
     , gdm_oggetti_file gom
 WHERE gom.id_oggetto_file  = f.id_file_Esterno
   and f.dimensione < 0) n
   on (fa.id_file_allegato = n.id_file_allegato)
when matched then update set fa.dimensione = n.dimensione_file
/