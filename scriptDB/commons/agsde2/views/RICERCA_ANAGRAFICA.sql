--liquibase formatted sql
--changeset mfrancesconi:RICERCA_ANAGRAFICHE runOnChange:true
create or replace force view RICERCA_ANAGRAFICA
(
   ID_RICERCA_ANAGRAFICA,
   NI,
   DENOMINAZIONE,
   COGNOME,
   NOME,
   PARTITA_IVA,
   CODICE_FISCALE,
   INDIRIZZO_COMPLETO,
   EMAIL,
   FAX,
   INDIRIZZO,
   CAP,
   COMUNE,
   PROVINCIA,
   TELEFONO,
   SESSO,
   COMUNE_NASCITA,
   DATA_NASCITA,
   TIPO,
   CODICE_FISCALE_ESTERO,
   ID_TIPO_RECAPITO,
   DESCRIZIONE_TIPO_RECAPITO,
   ID_RECAPITO,
   ID_CONTATTO,
   TIPO_SPEDIZIONE,
   TIPO_SOGGETTO,
   DAL,
   AL
)
as
select to_char(anag.ni)||'#'||reca.id_recapito||'#'||cont.id_contatto
       , to_char (anag.ni) ni,
       anag.denominazione,
       cognome,
       nome,
       partita_iva,
       codice_fiscale,
          reca.indirizzo
       || ' '
       || decode (reca.cap, null, null, lpad (trim (reca.cap), 5, '0') || ' ')
       || decode (ad4_comuni.denominazione, null, null, ad4_comuni.denominazione || ' ')
       || decode (ad4_province.sigla, null, '', '(' || ad4_province.sigla || ')')
          indirizzo,
       decode (tico.tipo_spedizione, 'MAIL', cont.valore, to_char (null)) email,
       decode (tico.tipo_spedizione, 'FAX', cont.valore, to_char (null)) fax,
       reca.indirizzo indirizzo_res,
       reca.cap cap_res,
       ad4_comuni.denominazione comune_res,
       ad4_province.sigla provincia_res,
       decode (tico.id_tipo_contatto, 1, cont.valore, to_char (null)) tel_res,
       sesso,
       comuni_nas.denominazione comune_nascita,
       to_char (data_nas, 'dd/mm/yyyy') data_nascita,
       anag.tipo_soggetto tipo,
       codice_fiscale_estero cf_estero,
       reca.id_tipo_recapito,
       tire.descrizione descrizione_tipo_recapito,
       reca.id_recapito id_recapito,
       cont.id_contatto id_contatto,
       tico.tipo_spedizione,
       1 tipo_soggetto,
       anag.dal,
       anag.al
  from as4_anagrafici anag,
       as4_recapiti reca,
       as4_tipi_recapito tire,
       as4_contatti cont,
       as4_tipi_contatto tico,
       ad4_comuni comuni_nas,
       ad4_province,
       ad4_comuni
 where anag.ni = reca.ni(+)
   and reca.id_tipo_recapito = tire.id_tipo_recapito(+)
   and reca.id_recapito = cont.id_recapito(+)
   and cont.id_tipo_contatto = tico.id_tipo_contatto(+)
   and cont.id_tipo_contatto(+) <> 1
   and nvl (anag.al, trunc (sysdate)) >= trunc (sysdate)
   and nvl (reca.al, trunc (sysdate)) >= trunc (sysdate)
   and nvl (cont.al, trunc (sysdate)) >= trunc (sysdate)
   and reca.provincia = ad4_comuni.provincia_stato(+)
   and reca.comune = ad4_comuni.comune(+)
   and ad4_comuni.provincia_stato = ad4_province.provincia(+)
   and comuni_nas.comune(+) = comune_nas
   and comuni_nas.provincia_stato(+) = provincia_nas
   and not exists
          (select 1
             from so4_amministrazioni ammi
            where ammi.ni = anag.ni)
   and not exists
          (select 1
             from so4_soggetti_aoo aoo
            where aoo.ni = anag.ni)
   and not exists
          (select 1
             from so4_soggetti_unita soun
            where soun.ni = anag.ni)
union
select to_char(anag.ni)||'#'||reca.id_recapito||'#',
       to_char (anag.ni) ni,
       anag.denominazione,
       cognome,
       nome,
       partita_iva,
       codice_fiscale,
          reca.indirizzo
       || ' '
       || decode (reca.cap, null, null, lpad (trim (reca.cap), 5, '0') || ' ')
       || decode (ad4_comuni.denominazione, null, null, ad4_comuni.denominazione || ' ')
       || decode (ad4_province.sigla, null, '', '(' || ad4_province.sigla || ')')
          indirizzo,
       to_char (null) email,
       to_char (null) fax,
       reca.indirizzo indirizzo_res,
       reca.cap cap_res,
       ad4_comuni.denominazione comune_res,
       ad4_province.sigla provincia_res,
       to_char (null) tel_res,
       sesso,
       comuni_nas.denominazione comune_nascita,
       to_char (data_nas, 'dd/mm/yyyy') data_nascita,
       anag.tipo_soggetto tipo,
       codice_fiscale_estero cf_estero,
       reca.id_tipo_recapito,
       tire.descrizione descrizione_tipo_recapito,
       reca.id_recapito id_recapito,
       null id_contatto,
       null tipo_spedizione,
       1 tipo_soggetto,
       anag.dal,
       anag.al
  from as4_anagrafici anag,
       as4_recapiti reca,
       as4_tipi_recapito tire,
       ad4_comuni comuni_nas,
       ad4_province,
       ad4_comuni
 where anag.ni = reca.ni(+)
   and reca.id_tipo_recapito = tire.id_tipo_recapito(+)
   and nvl (anag.al, trunc (sysdate)) >= trunc (sysdate)
   and nvl (reca.al, trunc (sysdate)) >= trunc (sysdate)
   and not exists
          (select 1
             from as4_contatti cont
            where cont.id_recapito(+) = reca.id_recapito
              and cont.id_tipo_contatto(+) <> 1
              and nvl (cont.al, trunc (sysdate)) >= trunc (sysdate))
   and reca.provincia = ad4_comuni.provincia_stato(+)
   and reca.comune = ad4_comuni.comune(+)
   and ad4_comuni.provincia_stato = ad4_province.provincia(+)
   and comuni_nas.comune(+) = comune_nas
   and comuni_nas.provincia_stato(+) = provincia_nas
   and not exists
          (select 1
             from so4_amministrazioni ammi
            where ammi.ni = anag.ni)
   and not exists
          (select 1
             from so4_soggetti_aoo aoo
            where aoo.ni = anag.ni)
   and not exists
          (select 1
             from so4_soggetti_unita soun
            where soun.ni = anag.ni)
/