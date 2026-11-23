--liquibase formatted sql
--changeset mfrancesconi:2.5_20230504_40.mapping_protocollo_iride_aoo_utilizzo runOnChange:true stripComments:false failOnError:false

insert into mapping_integrazioni (
    id_mapping_integrazione,
    categoria,
    codice,
    valore_esterno,
    valore_interno,
    ente,
    sequenza,
    descrizione
)
select
    hibernate_sequence.nextval,
    'PROTOCOLLO_IRIDE',
    'AOO_UTILIZZO',
    'Y',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                     from mapping_integrazioni
                    where categoria = 'PROTOCOLLO_IRIDE'
                      and codice = 'AOO_UTILIZZO' )
   and exists ( select 1
                from mapping_integrazioni
                where categoria = 'PROTOCOLLO_IRIDE'
                  and codice = 'AOO')
/

insert into mapping_integrazioni (
    id_mapping_integrazione,
    categoria,
    codice,
    valore_esterno,
    valore_interno,
    ente,
    sequenza,
    descrizione
)
select
    hibernate_sequence.nextval,
    'PROTOCOLLO_IRIDE',
    'UNITA_PROTOCOLLO_FISSA',
    'N',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'PROTOCOLLO_IRIDE'
                     and codice = 'UNITA_PROTOCOLLO_FISSA' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'PROTOCOLLO_IRIDE'
                 and codice = 'AOO')
/

insert into mapping_integrazioni (
    id_mapping_integrazione,
    categoria,
    codice,
    valore_esterno,
    valore_interno,
    ente,
    sequenza,
    descrizione
)
select
    hibernate_sequence.nextval,
    'PROTOCOLLO_IRIDE',
    'CODICE_UNITA_PROTOCOLLO_FISSA',
    '-',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'PROTOCOLLO_IRIDE'
                     and codice = 'CODICE_UNITA_PROTOCOLLO_FISSA' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'PROTOCOLLO_IRIDE'
                 and codice = 'AOO')
/