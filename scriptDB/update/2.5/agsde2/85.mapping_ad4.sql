--liquibase formatted sql
--changeset mfrancesconi:2.5_20241127_85.mapping_ad4 runOnChange:true stripComments:false failOnError:true

alter table mapping_integrazioni rename to mapping_integrazioni_table
/

create or replace procedure popola_regi_ad4_mapping
is
    d_tabella   varchar2 (100) := 'Tabella ' || user || ' MAPPING_INTEGRAZIONI. ';
    d_risorsa   varchar2 (2000);

    procedure crea_registro_ad4 (p_chiave     varchar2,
                                 p_stringa    varchar2,
                                 p_valore     varchar2,
                                 p_commento   varchar2)
    is
    begin
        ad4_registro_utility.scrivi_stringa (p_chiave,
                                             p_stringa,
                                             p_valore,
                                             p_commento,
                                             false);
    exception
        when others
        then
            ad4_key_error_log_pkg.ins (null,
                                       sys_context ('USERENV', 'SID'),
                                       sysdate,
                                       d_tabella ||
                                       'Errore in inserimento CHIAVE ' ||
                                       p_chiave ||
                                       ' e STRINGA ' ||
                                       p_stringa,
                                       user,
                                       substr (sqlerrm,
                                               1,
                                               2000),
                                       'E');
    end;
begin
    for map_int in (select * from mapping_integrazioni_table)
    loop
        crea_registro_ad4 ('PRODUCTS/' ||
                           map_int.ente ||
                           '/AGSDE2/AGSDE2/MAPPING_INTEGRAZIONI/' ||
                           map_int.id_mapping_integrazione,
                           'CATEGORIA',
                           map_int.categoria,
                           '');

        crea_registro_ad4 ('PRODUCTS/' ||
                           map_int.ente ||
                           '/AGSDE2/AGSDE2/MAPPING_INTEGRAZIONI/' ||
                           map_int.id_mapping_integrazione,
                           'CODICE',
                           map_int.codice,
                           '');

        crea_registro_ad4 ('PRODUCTS/' ||
                           map_int.ente ||
                           '/AGSDE2/AGSDE2/MAPPING_INTEGRAZIONI/' ||
                           map_int.id_mapping_integrazione,
                           'ENTE',
                           map_int.ente,
                           '');

        crea_registro_ad4 ('PRODUCTS/' ||
                           map_int.ente ||
                           '/AGSDE2/AGSDE2/MAPPING_INTEGRAZIONI/' ||
                           map_int.id_mapping_integrazione,
                           'SEQUENZA',
                           map_int.sequenza,
                           '');

        crea_registro_ad4 ('PRODUCTS/' ||
                           map_int.ente ||
                           '/AGSDE2/AGSDE2/MAPPING_INTEGRAZIONI/' ||
                           map_int.id_mapping_integrazione,
                           'VALORE_INTERNO',
                           map_int.valore_interno,
                           '');

        crea_registro_ad4 ('PRODUCTS/' ||
                           map_int.ente ||
                           '/AGSDE2/AGSDE2/MAPPING_INTEGRAZIONI/' ||
                           map_int.id_mapping_integrazione,
                           'VALORE_ESTERNO',
                           map_int.valore_esterno,
                           '');
    end loop;
end;
/

create or replace force view mapping_integrazioni
(
    id_mapping_integrazione,
    categoria,
    codice,
    descrizione,
    ente,
    sequenza,
    valore_esterno,
    valore_interno,
    chiave_registro
)
as
    select substr (ad4_registro.chiave,
                   instr (ad4_registro.chiave,
                   '/',
                   -1) + 1)
               id_mapping_integrazione,
           get_ad4_registro_valore (ad4_registro.chiave, 'CATEGORIA') categoria,
           get_ad4_registro_valore (ad4_registro.chiave, 'CODICE') codice,
           null descrizione,
           get_ad4_registro_valore (ad4_registro.chiave, 'ENTE') ente,
           to_number (get_ad4_registro_valore (ad4_registro.chiave, 'SEQUENZA')) sequenza,
           get_ad4_registro_valore (ad4_registro.chiave, 'VALORE_ESTERNO') valore_esterno,
           get_ad4_registro_valore (ad4_registro.chiave, 'VALORE_INTERNO') valore_interno,
           ad4_registro.chiave chiave_registro
      from ad4_registro
     where ad4_registro.chiave like 'PRODUCTS/%/AGSDE2/AGSDE2/MAPPING_INTEGRAZIONI/%'
       and stringa = '(Predefinito)'
/

create or replace trigger mapping_integrazioni_tioiud
    /******************************************************************************
            NOME:        MAPPING_INTEGRAZIONI_TIOIUD
            DESCRIZIONE: Trigger instead of UPDATE on View MAPPING_INTEGRAZIONI
            ANNOTAZIONI: -
            REVISIONI:
            Rev. Data       Autore         Descrizione
            ---- ---------- -------------  -------------------------------------------
             0 27/11/2024 MFrancesconi    Creato.
        ******************************************************************************/
    instead of insert or update or delete
    on mapping_integrazioni
    for each row
declare
    d_istanza      varchar2 (10) := 'AGSDE2';
    d_modulo       varchar2 (10) := 'AGSDE2';
    d_risorsa      varchar2 (2000);
    d_chiave_new   varchar2 (4000);
    d_chiave_old   varchar2 (4000);
    d_id_new       varchar2 (4000);
begin
    if inserting then
        select to_char (hibernate_sequence.nextval) into d_id_new from dual;

        select 'PRODUCTS/' ||
               :new.ente ||
               '/' ||
               d_modulo ||
               '/' ||
               d_istanza ||
               '/MAPPING_INTEGRAZIONI/' ||
               d_id_new --:new.id_mapping_integrazione
          into d_chiave_new
          from dual;

        ad4_registro_utility.scrivi_stringa (d_chiave_new,
                                             'CATEGORIA',
                                             :new.categoria,
                                             '',
                                             false);

        ad4_registro_utility.scrivi_stringa (d_chiave_new,
                                             'CODICE',
                                             :new.codice,
                                             '',
                                             false);

        ad4_registro_utility.scrivi_stringa (d_chiave_new,
                                             'ENTE',
                                             to_char (:new.ente),
                                             '',
                                             false);

        ad4_registro_utility.scrivi_stringa (d_chiave_new,
                                             'SEQUENZA',
                                             to_char (:new.sequenza),
                                             '',
                                             false);

        ad4_registro_utility.scrivi_stringa (d_chiave_new,
                                             'VALORE_INTERNO',
                                             :new.valore_interno,
                                             '',
                                             false);

        ad4_registro_utility.scrivi_stringa (d_chiave_new,
                                             'VALORE_ESTERNO',
                                             :new.valore_esterno,
                                             '',
                                             false);
    end if;

    if updating then
        d_chiave_old :=
            'PRODUCTS/' ||
            :old.ente ||
            '/' ||
            d_modulo ||
            '/' ||
            d_istanza ||
            '/MAPPING_INTEGRAZIONI/' ||
            :old.id_mapping_integrazione;

        update ad4_registro
           set valore = :new.sequenza
         where chiave = d_chiave_old
           and stringa = 'SEQUENZA';

        update ad4_registro
           set valore = :new.valore_interno
         where chiave = d_chiave_old
           and stringa = 'VALORE_INTERNO';

        update ad4_registro
           set valore = :new.valore_esterno
         where chiave = d_chiave_old
           and stringa = 'VALORE_ESTERNO';
    end if;

    if deleting then
        d_chiave_old :=
            'PRODUCTS/' ||
            :old.ente ||
            '/' ||
            d_modulo ||
            '/' ||
            d_istanza ||
            '/MAPPING_INTEGRAZIONI/' ||
            :old.id_mapping_integrazione;

        delete ad4_registro
         where chiave = d_chiave_old;
    end if;
exception when others then
        raise;
end;
/

begin
    popola_regi_ad4_mapping;
end;
/