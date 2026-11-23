--liquibase formatted sql
--changeset mfrancesconi:2.6_20260203_07.mapping_contabilita_jserfin_add_parametri runOnChange:true stripComments:false failOnError:true

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
    'CONTABILITA_JSERFIN',
    'V_LIVELLO_OBBLIGATORIO',
    'N',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'CONTABILITA_JSERFIN'
                     and codice = 'V_LIVELLO_OBBLIGATORIO' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'CONTABILITA_JSERFIN')
/