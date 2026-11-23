--liquibase formatted sql
--changeset mfrancesconi:2.5_20250122_86.view_impostazioni_ad4 runOnChange:true stripComments:false failOnError:true

CREATE OR REPLACE FORCE VIEW IMPOSTAZIONI
(CODICE, ENTE, CARATTERISTICHE, DESCRIZIONE, ETICHETTA,
 MODIFICABILE, PREDEFINITO, RISORSA, VALORE, VERSION,
 CHIAVE_REGISTRO)
AS
SELECT SUBSTR (AD4_REGISTRO.chiave, INSTR (AD4_REGISTRO.CHIAVE, '/', -1) + 1)
          CODICE,
       GET_AD4_REGISTRO_VALORE (AD4_REGISTRO.CHIAVE, 'ENTE') ENTE,
       GET_AD4_REGISTRO_VALORE (AD4_REGISTRO.CHIAVE, 'CARATTERISTICHE')
          CARATTERISTICHE,
       GET_AD4_REGISTRO_VALORE (AD4_REGISTRO.CHIAVE, 'DESCRIZIONE')
          DESCRIZIONE,
       GET_AD4_REGISTRO_VALORE (AD4_REGISTRO.CHIAVE, 'ETICHETTA') ETICHETTA,
       cast(GET_AD4_REGISTRO_VALORE (AD4_REGISTRO.CHIAVE, 'MODIFICABILE') as char(1))
          MODIFICABILE,
       GET_AD4_REGISTRO_VALORE (AD4_REGISTRO.CHIAVE, '(Predefinito)')
          PREDEFINITO,
       TO_BLOB (
          UTL_RAW.cast_to_raw (
             GET_AD4_REGISTRO_VALORE (AD4_REGISTRO.CHIAVE, 'RISORSA')))
          RISORSA,
       GET_AD4_REGISTRO_VALORE (AD4_REGISTRO.CHIAVE, 'VALORE') VALORE,
       0 VERSION,
       AD4_REGISTRO.CHIAVE
  FROM AD4_REGISTRO, wkf_impostazioni
 WHERE     AD4_REGISTRO.CHIAVE LIKE 'PRODUCTS/%/AGSDE2/AGSDE2/IMPOSTAZIONI/%'
       AND STRINGA = '(Predefinito)'
       AND wkf_impostazioni.codice = 'ENTI_SO4'
       AND INSTR (
              wkf_impostazioni.VALORE,
              SUBSTR (
                 AD4_REGISTRO.chiave,
                   INSTR (AD4_REGISTRO.chiave, 'PRODUCTS/')
                 + LENGTH ('PRODUCTS/'),
                   INSTR (AD4_REGISTRO.CHIAVE,
                          '/AGSDE2/AGSDE2/IMPOSTAZIONI/')
                 - (  INSTR (AD4_REGISTRO.chiave, 'PRODUCTS/')
                    + LENGTH ('PRODUCTS/')))) > 0
/
create or replace trigger impostazioni_tioiud
    /******************************************************************************
        NOME:        IMPOSTAZIONI_TIOIUD
        DESCRIZIONE: Trigger instead of UPDATE on View IMPOSTAZIONI
        ANNOTAZIONI: -
        REVISIONI:
        Rev. Data       Autore         Descrizione
        ---- ---------- -------------  -------------------------------------------
         0 06/02/2024 MMalferrari    Creato.
    ******************************************************************************/
    instead of insert or update or delete
    on impostazioni
    for each row
declare
    d_istanza      varchar2 (10) := 'AGSDE2';
    d_modulo       varchar2 (10) := 'AGSDE2';
    d_risorsa      varchar2 (2000);
    d_chiave_new   varchar2 (4000);
begin
    if (:new.valore is null) and not deleting then
        raise_application_error(-20999,'Impossibile inserire un valore NULL per un''impostazione');
    end if;

    d_risorsa :=
        utl_raw.cast_to_varchar2 (dbms_lob.substr (:new.risorsa,
                                                   2000,
                                                   1));
    d_chiave_new :=
        'PRODUCTS/' ||
        :new.ente ||
        '/' ||
        d_modulo ||
        '/' ||
        d_istanza ||
        '/IMPOSTAZIONI/' ||
        :new.codice;

    if inserting then
        ad4_registro_utility.scrivi_stringa (d_chiave_new,
                                             'ENTE',
                                             :new.ente,
                                             '',
                                             false);

        ad4_registro_utility.scrivi_stringa (d_chiave_new,
                                             '(Predefinito)',
                                             :new.predefinito,
                                             '',
                                             false);

        ad4_registro_utility.scrivi_stringa (d_chiave_new,
                                             'CARATTERISTICHE',
                                             :new.caratteristiche,
                                             '',
                                             false);

        ad4_registro_utility.scrivi_stringa (d_chiave_new,
                                             'DESCRIZIONE',
                                             :new.descrizione,
                                             '',
                                             false);

        ad4_registro_utility.scrivi_stringa (d_chiave_new,
                                             'ETICHETTA',
                                             :new.etichetta,
                                             '',
                                             false);

        ad4_registro_utility.scrivi_stringa (d_chiave_new,
                                             'MODIFICABILE',
                                             :new.modificabile,
                                             '',
                                             false);

        ad4_registro_utility.scrivi_stringa (d_chiave_new,
                                             'VALORE',
                                             :new.valore,
                                             '',
                                             false);

        ad4_registro_utility.scrivi_stringa (d_chiave_new,
                                             'RISORSA',
                                             d_risorsa,
                                             '',
                                             false);
    end if;

    if updating then
        update ad4_registro
           set valore = :new.predefinito
         where chiave = :old.chiave_registro
           and stringa = '(Predefinito)';

        update ad4_registro
           set valore = :new.ente
         where chiave = :old.chiave_registro
           and stringa = 'ENTE';

        update ad4_registro
           set valore = :new.caratteristiche
         where chiave = :old.chiave_registro
           and stringa = 'CARATTERISTICHE';

        update ad4_registro
           set valore = :new.descrizione
         where chiave = :old.chiave_registro
           and stringa = 'DESCRIZIONE';

        update ad4_registro
           set valore = :new.etichetta
         where chiave = :old.chiave_registro
           and stringa = 'ETICHETTA';

        update ad4_registro
           set valore = :new.modificabile
         where chiave = :old.chiave_registro
           and stringa = 'MODIFICABILE';

        update ad4_registro
           set valore = :new.valore
         where chiave = :old.chiave_registro
           and stringa = 'VALORE';

        update ad4_registro
           set valore = d_risorsa
         where chiave = :old.chiave_registro
           and stringa = 'RISORSA';
    end if;

    if deleting then
        delete ad4_registro
         where chiave = :old.chiave_registro;
    end if;
exception when others then
    raise;
end;
/