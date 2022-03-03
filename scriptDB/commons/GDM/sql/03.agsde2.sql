--liquibase formatted sql
--changeset rdestasio:install_20200221_03_agsde2 runOnChange:true

CREATE OR REPLACE SYNONYM GDM_F_VALORE_CAMPO FOR ${global.db.gdm.username}.F_VALORE_CAMPO
/

CREATE OR REPLACE SYNONYM GDM_OGGETTI_FILE FOR ${global.db.gdm.username}.OGGETTI_FILE
/

CREATE OR REPLACE SYNONYM GDM_GAT_DETERMINA FOR ${global.db.gdm.username}.GAT_DETERMINA
/

CREATE OR REPLACE SYNONYM GDM_GAT_DELIBERA FOR ${global.db.gdm.username}.GAT_DELIBERA
/

CREATE OR REPLACE SYNONYM GDM_GAT_PROPOSTA_DELIBERA FOR ${global.db.gdm.username}.GAT_PROPOSTA_DELIBERA
/

CREATE OR REPLACE SYNONYM GDM_GAT_SEDUTA_STAMPA FOR ${global.db.gdm.username}.GAT_SEDUTA_STAMPA
/

CREATE OR REPLACE SYNONYM GDM_IMPRONTE_FILE FOR ${global.db.gdm.username}.IMPRONTE_FILE
/

create or replace synonym gdm_oggetti_file_pkg for ${global.db.gdm.username}.gdm_oggetti_file
/

GRANT EXECUTE ON INTEGRAZIONE_GDM_PKG TO ${global.db.gdm.username}
/