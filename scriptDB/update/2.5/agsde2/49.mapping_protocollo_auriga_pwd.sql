--liquibase formatted sql
--changeset mfrancesconi:2.5_20231020_49.mapping_protocollo_auriga_pwd runOnChange:true stripComments:false failOnError:false

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
    'PROTOCOLLO_AURIGA',
    'USARE_PASSWORD_UNICA',
    'N',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                     from mapping_integrazioni
                    where categoria = 'PROTOCOLLO_AURIGA'
                      and codice = 'USARE_PASSWORD_UNICA' )
   and exists ( select 1
                from mapping_integrazioni
                where categoria = 'PROTOCOLLO_AURIGA'
                  and codice = 'CODICE_APPLICAZIONE')
/