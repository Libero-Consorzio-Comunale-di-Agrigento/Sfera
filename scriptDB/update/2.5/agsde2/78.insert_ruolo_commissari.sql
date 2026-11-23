--liquibase formatted sql
--changeset mfrancesconi:2.5_2024129_78.insert_ruolo_commissari runOnChange:true failOnError:false

insert into odg_ruoli_partecipanti (ruolo_partecipante,
                                    data_ins,
                                    descrizione,
                                    ente,
                                    data_upd,
                                    utente_ins,
                                    utente_upd,
                                    valido,
                                    valido_al,
                                    valido_dal)
    select 'COMMISSARIO_1',
           sysdate,
           'COMMISSARIO 1',
           impostazioni_pkg.get_impostazione ('ENTI_SO4', '*'),
           sysdate,
           'AGSDE2',
           'AGSDE2',
           'Y',
           null,
           sysdate
      from dual
     where not exists
               (select 1
                  from odg_ruoli_partecipanti
                 where ruolo_partecipante = 'COMMISSARIO_1'
                   and ente = impostazioni_pkg.get_impostazione ('ENTI_SO4', '*'))
/

insert into odg_ruoli_partecipanti (ruolo_partecipante,
                                    data_ins,
                                    descrizione,
                                    ente,
                                    data_upd,
                                    utente_ins,
                                    utente_upd,
                                    valido,
                                    valido_al,
                                    valido_dal)
select 'COMMISSARIO_2',
       sysdate,
       'COMMISSARIO 2',
       impostazioni_pkg.get_impostazione ('ENTI_SO4', '*'),
       sysdate,
       'AGSDE2',
       'AGSDE2',
       'Y',
       null,
       sysdate
from dual
where not exists
          (select 1
           from odg_ruoli_partecipanti
           where ruolo_partecipante = 'COMMISSARIO_2'
             and ente = impostazioni_pkg.get_impostazione ('ENTI_SO4', '*'))
/

insert into odg_ruoli_partecipanti (ruolo_partecipante,
                                    data_ins,
                                    descrizione,
                                    ente,
                                    data_upd,
                                    utente_ins,
                                    utente_upd,
                                    valido,
                                    valido_al,
                                    valido_dal)
select 'COMMISSARIO_3',
       sysdate,
       'COMMISSARIO 3',
       impostazioni_pkg.get_impostazione ('ENTI_SO4', '*'),
       sysdate,
       'AGSDE2',
       'AGSDE2',
       'Y',
       null,
       sysdate
from dual
where not exists
          (select 1
           from odg_ruoli_partecipanti
           where ruolo_partecipante = 'COMMISSARIO_3'
             and ente = impostazioni_pkg.get_impostazione ('ENTI_SO4', '*'))
/

insert into tipi_soggetti (codice,
                           categoria,
                           descrizione,
                           titolo)
select 'COMMISSARIO_1',
       'COMPONENTE',
       null,
       'Commissario 1'
from dual
where not exists
          (select 1
           from tipi_soggetti
           where codice = 'COMMISSARIO_1')
/

insert into tipi_soggetti (codice,
                           categoria,
                           descrizione,
                           titolo)
select 'COMMISSARIO_2',
       'COMPONENTE',
       null,
       'Commissario 2'
from dual
where not exists
          (select 1
           from tipi_soggetti
           where codice = 'COMMISSARIO_2')
/

insert into tipi_soggetti (codice,
                           categoria,
                           descrizione,
                           titolo)
select 'COMMISSARIO_3',
       'COMPONENTE',
       null,
       'Commissario 3'
from dual
where not exists
          (select 1
           from tipi_soggetti
           where codice = 'COMMISSARIO_3')
/