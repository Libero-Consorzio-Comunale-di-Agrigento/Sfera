--liquibase formatted sql
--changeset mfrancesconi:2.6_20260622_20.mapping_jworklist_data_numero_proposta runOnChange:true stripComments:false failOnError:true

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
    'JWORKLIST',
    'JWORKLIST_DATA_NUMERO_PROPOSTA',
    'N',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'JWORKLIST'
                     and codice = 'JWORKLIST_DATA_NUMERO_PROPOSTA' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'JWORKLIST')
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
    'JWORKLIST',
    'JWORKLIST_ATTORE_OPERAZIONE',
    'N',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'JWORKLIST'
                     and codice = 'JWORKLIST_ATTORE_OPERAZIONE' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'JWORKLIST')
/