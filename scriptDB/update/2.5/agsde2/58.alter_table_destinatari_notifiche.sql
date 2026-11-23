--liquibase formatted sql
--changeset mfrancesconi:2.5_20240205_58.alter_table_destinatari_notifiche.sql runOnChange:true stripComments:false failOnError:false

alter table destinatari_notifiche add (
    id_recapito number(19),
    id_contatto number(19),
    cognome varchar2(255),
    nome varchar2(255),
    codice_fiscale varchar2(255),
    indirizzo varchar2(255),
    comune varchar2(255),
    provincia varchar2(255),
    cap varchar2(255),
    indirizzo_email varchar2(255))
/

begin
   --per tutti i destinatari di tipo PND valorizzo i campi nuovi
   for c in (select id_destinatario_notifica, ni_soggetto
               from DESTINATARI_NOTIFICHE
              where tipo_notifica = 'PND' and ni_soggetto is not null)
   loop
      for c1 in (select COGNOME,
                        NOME,
                        CODICE_FISCALE,
                        INDIRIZZO_res,
                        ad4_comune.get_denominazione (PROVINCIA_res, comune_res) comune,
                        ad4_provincia.get_denominazione (PROVINCIA_res) PROVINCIA,
                        CAP_res,
                        INDIRIZZO_web
                   from AS4_V_SOGGETTI_CORRENTI
                  where ni = c.ni_soggetto)
      loop
         update DESTINATARI_NOTIFICHE
            set cognome = c1.cognome,
                nome = c1.nome,
                codice_fiscale = c1.codice_fiscale,
                indirizzo = c1.indirizzo_res,
                comune = c1.comune,
                provincia = c1.provincia,
                cap = c1.cap_res,
                indirizzo_email = c1.indirizzo_web
          where id_destinatario_notifica = c.id_destinatario_notifica;
      end loop;
   end loop;
exception
   when others then
      null;
end;