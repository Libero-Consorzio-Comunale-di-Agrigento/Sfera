--liquibase formatted sql
--changeset rdestasio:install_20200221_04_gdm runOnChange:true

create or replace synonym agsde2_integrazione_gdm_pkg for ${global.db.target.username}.integrazione_gdm_pkg
/

/*
 * Questo script serve per allineare la sequence dei formati file di gdm che spesso non è allineata con il max della tabella formati_file
 */
DECLARE
  d_max NUMBER;
  d_fofi_sq number;
BEGIN
  select max(id_formato) into d_max from formati_file;
  d_fofi_sq := 0;
  while d_max >= d_fofi_sq loop
    select FOFI_SQ.NEXTVAL into d_fofi_sq from dual;
  end loop;
end;
/

/*
 * se è presente il formato file _HD lo rinomino in HD. (inizialmente si chiamava _HD ora si chiama HD, per questo lo rinomino)
 * se non è presente, aggiungo HD
 */
DECLARE
   d_id_formato_file_hidd   number;
   d_estensione_nascosta    varchar2(3);
   d_vecchia_estensione    varchar2(3);
BEGIN
   d_estensione_nascosta := 'HD';
   d_vecchia_estensione := '_HD';

   begin
    select id_formato into d_id_formato_file_hidd from formati_file where nome = d_vecchia_estensione;

    update formati_file set nome = d_estensione_nascosta where id_formato = d_id_formato_file_hidd;

   exception when no_data_found THEN
    select FOFI_SQ.NEXTVAL into d_id_formato_file_hidd from dual;

    INSERT INTO FORMATI_FILE (
   DATA_AGGIORNAMENTO, ID_FORMATO, VISIBILE,
   NOME, ICONA, UTENTE_AGGIORNAMENTO)
VALUES ( sysdate,
 d_id_formato_file_hidd,
 'N',
 d_estensione_nascosta,
 'generico.gif',
 'GDM');
   end;
end;
/