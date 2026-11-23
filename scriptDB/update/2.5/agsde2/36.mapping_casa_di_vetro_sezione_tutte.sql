--liquibase formatted sql
--changeset mfrancesconi:2.5_20230419_36.mapping_casa_di_vetro_sezione_tutte runOnChange:true stripComments:false failOnError:false

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
    'CASA_DI_VETRO',
    'SEZIONE_TUTTE',
    'N',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                     from mapping_integrazioni
                    where categoria = 'CASA_DI_VETRO'
                      and codice = 'SEZIONE_TUTTE' )
/