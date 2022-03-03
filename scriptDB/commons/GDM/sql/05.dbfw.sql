--liquibase formatted sql
--changeset rdestasio:install_20200221_05_dbfw

declare
    d_utente_agd    varchar2(255);
    d_esiste        number (10);
begin
    select utente into d_utente_agd from ad4_utenti u where U.GRUPPO_LAVORO = 'AGD' and U.TIPO_UTENTE = 'O';
    
    select count(1) into d_esiste from si4_competenze where oggetto = 'GDMWEB' and utente = d_utente_agd and accesso = 'S' and tipo_competenza = 'U';
    
    if (d_esiste = 0) then
        INSERT INTO SI4_COMPETENZE (
           ACCESSO, AL, DAL, 
           DATA_AGGIORNAMENTO, ID_ABILITAZIONE, 
           ID_FUNZIONE, OGGETTO, RUOLO, 
           TIPO_COMPETENZA, UTENTE, UTENTE_AGGIORNAMENTO) 
    VALUES ( 'S', null, sysdate-100, sysdate, (select id_tipo_abilitazione from si4_tipi_abilitazione where tipo_abilitazione = 'EX'), null, 'GDMWEB', null, 'U', d_utente_agd, null);
    
    end if;
end;
/
