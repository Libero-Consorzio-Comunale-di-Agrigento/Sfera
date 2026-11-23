--liquibase formatted sql
--changeset mfrancesconi:2.5_20220307_01.agdcons_nel_gruppo_conweb_u runOnChange:true failOnError:false

declare
   d_ruolo      varchar2 (255);
   d_utente     varchar2 (100);
   d_gruppo     varchar2 (100) := 'CONWEB_U';
   d_presente   number;
begin
   begin
      select nvl (valore, 'AGDCONS')
        into d_ruolo
        from ${global.db.target.username}.impostazioni
       where codice = 'RUOLO_SO4_CONSERVAZIONE';
   exception
      when others then
         d_ruolo   := 'AGDCONS';
   end;

   begin
      select utente
        into d_utente
        from utenti
       where gruppo_lavoro = d_ruolo;

      select count (1)
        into d_presente
        from utenti_gruppo
       where utente = d_utente
         and gruppo = d_gruppo;

      if d_presente = 0 then
         insert into utenti_gruppo (utente, gruppo)
              values (d_utente, d_gruppo);
      end if;
   exception
      when others then
         null;
   end;
end;
/