--liquibase formatted sql
--changeset mfrancesconi:2.5_20240712_68.mapping_contabilita_jserfin_add_parametri runOnChange:true stripComments:false failOnError:true

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
    'UTENTE_SESSIONE',
    'N',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'CONTABILITA_JSERFIN'
                     and codice = 'UTENTE_SESSIONE' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'CONTABILITA_JSERFIN')
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
    'CONTABILITA_JSERFIN',
    'RICERCA_ANAGRAFICA',
    'Y',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                     from mapping_integrazioni
                    where categoria = 'CONTABILITA_JSERFIN'
                      and codice = 'RICERCA_ANAGRAFICA' )
   and exists ( select 1
                from mapping_integrazioni
                where categoria = 'CONTABILITA_JSERFIN')
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
    'CONTABILITA_JSERFIN',
    'RICERCA_CAPITOLI_CODICE_LAVORO',
    'N',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'CONTABILITA_JSERFIN'
                     and codice = 'RICERCA_CAPITOLI_CODICE_LAVORO' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'CONTABILITA_JSERFIN')
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
    'CONTABILITA_JSERFIN',
    'CODICE_SETTORE',
    'PD',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'CONTABILITA_JSERFIN'
                     and codice = 'CODICE_SETTORE' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'CONTABILITA_JSERFIN')
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
    'CONTABILITA_JSERFIN',
    'CODICE_TIPO_ATTO',
    'DETERMINA',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'CONTABILITA_JSERFIN'
                     and codice = 'CODICE_TIPO_ATTO' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'CONTABILITA_JSERFIN')
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
    'CONTABILITA_JSERFIN',
    'VARIAZIONE_SUBIMPEGNO',
    'N',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'CONTABILITA_JSERFIN'
                     and codice = 'VARIAZIONE_SUBIMPEGNO' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'CONTABILITA_JSERFIN')
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
    'CONTABILITA_JSERFIN',
    'VISUALIZZA_TOTALI',
    'N',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'CONTABILITA_JSERFIN'
                     and codice = 'VISUALIZZA_TOTALI' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'CONTABILITA_JSERFIN')
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
    'CONTABILITA_JSERFIN',
    'RICERCA_MOVIMENTI_CODICE_GLOBALE',
    'N',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'CONTABILITA_JSERFIN'
                     and codice = 'RICERCA_MOVIMENTI_CODICE_GLOBALE' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'CONTABILITA_JSERFIN')
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
    'CONTABILITA_JSERFIN',
    'VISUALIZZA_TIPO_FINANZIAMENTO',
    'N',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'CONTABILITA_JSERFIN'
                     and codice = 'VISUALIZZA_TIPO_FINANZIAMENTO' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'CONTABILITA_JSERFIN')
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
    'CONTABILITA_JSERFIN',
    'VARIAZIONE_COMPETENZA_UGUALE_ANNO_IMPEGNO',
    'Y',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'CONTABILITA_JSERFIN'
                     and codice = 'VARIAZIONE_COMPETENZA_UGUALE_ANNO_IMPEGNO' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'CONTABILITA_JSERFIN')
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
    'CONTABILITA_JSERFIN',
    'VISUALIZZARE_CAPITOLI_FPV',
    'N',
    '*',
    impostazioni_pkg.get_impostazione('ENTI_SO4', '*'),
    0,
    null
from dual
where not exists ( select 1
                   from mapping_integrazioni
                   where categoria = 'CONTABILITA_JSERFIN'
                     and codice = 'VISUALIZZARE_CAPITOLI_FPV' )
  and exists ( select 1
               from mapping_integrazioni
               where categoria = 'CONTABILITA_JSERFIN')
/