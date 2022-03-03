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
 * trascodifica dei file "odt"
 */
DECLARE
   d_id_oggetto_file        NUMBER;
   d_id_file_allegato_orig  NUMBER;
   d_id_formato_file        NUMBER;
   d_nome_file_presente     number;
   d_nome_file              varchar2(255);
   d_id_formato_file_hidd   number;
   d_estensione_nascosta    varchar2(3);
   d_doc_gdm_esiste         number;
BEGIN
   d_estensione_nascosta := '_HD';

   begin
    select id_formato into d_id_formato_file_hidd from formati_file where nome = d_estensione_nascosta;
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

   FOR c
      IN (SELECT fa.id_file_allegato,
     fa.content_type,
     fa.nome,
     fa.allegato,
     nvl (d.id_documento_esterno, nvl(l.id_documento_esterno, nvl(pd.id_documento_esterno, nvl(c.id_documento_esterno, vp.id_documento_esterno)))) id_documento,
     nvl (d.utente_upd, nvl(l.utente_upd, nvl(pd.utente_upd, nvl(c.utente_upd, vp.utente_upd)))) utente_upd
FROM ${global.db.target.username}.file_allegati fa
   , ${global.db.target.username}.determine d
   , ${global.db.target.username}.visti_pareri vp
   , ${global.db.target.username}.delibere l
   , ${global.db.target.username}.proposte_delibera pd
   , ${global.db.target.username}.certificati c
WHERE     fa.id_file_esterno is null
     and fa.id_file_allegato_originale is null
     AND fa.allegato IS NOT NULL
     and d.id_file_allegato_testo_odt(+) = fa.id_file_allegato
     and d.id_documento_esterno(+) is not null
     and vp.id_file_allegato_testo_odt(+) = fa.id_file_allegato
     and vp.id_documento_esterno(+) is not null
     and pd.id_file_allegato_testo_odt(+) = fa.id_file_allegato
     and pd.id_documento_esterno(+) is not null
     and l.id_file_allegato_testo_odt(+) = fa.id_file_allegato
     and l.id_documento_esterno(+) is not null
     and c.id_file_allegato_testo_odt(+) = fa.id_file_allegato
     and c.id_documento_esterno(+) is not null)
   LOOP

      select count(1) into d_doc_gdm_esiste from documenti where id_documento = c.id_documento;

      if (c.id_documento is not null and d_doc_gdm_esiste > 0) then

       select ${global.db.target.username}.hibernate_sequence.NEXTVAL  into d_id_file_allegato_orig from dual;
       select ogg_file_sq.nextval                into d_id_oggetto_file       from dual;

      -- il nome del file del testo odt _DEVE_ essere diverso dal nome del testo originale (altrimenti GDM fa casini)
      d_nome_file := 'ODT'||c.nome||d_estensione_nascosta;

      INSERT INTO OGGETTI_FILE (ALLEGATO,
                                DATA_AGGIORNAMENTO,
                                DA_CANCELLARE,
                                FILENAME,
                                ID_DOCUMENTO,
                                ID_FORMATO,
                                ID_OGGETTO_FILE,
                                TESTOOCR,
                                UTENTE_AGGIORNAMENTO)
           VALUES ('N',
                   sysdate,
                   'N',
                   d_nome_file,
                   c.id_documento,
                   d_id_formato_file_hidd,
                   d_id_oggetto_file,
                   empty_blob(),
                   c.utente_upd);

       update oggetti_file set testoocr = c.allegato where id_oggetto_file = d_id_oggetto_file;

      UPDATE ${global.db.target.username}.file_allegati
         SET --allegato         = NULL,
             id_file_esterno  = d_id_oggetto_file,
             nome             = d_nome_file
       where id_file_allegato = c.id_file_allegato;
      commit;

      end if;
   END LOOP;
END;
/


/*
 * trascodifica dei file "originali", quelli salvati prima della firma
 */
DECLARE
   d_id_oggetto_file        NUMBER;
   d_id_file_allegato_orig  NUMBER;
   d_id_formato_file        NUMBER;
   d_nome_file_presente     number;
   d_nome_file              varchar2(255);
   d_id_formato_file_hidd   number;
   d_estensione_nascosta    varchar2(3);
BEGIN
   d_estensione_nascosta := '_HD';

   begin
    select id_formato into d_id_formato_file_hidd from formati_file where nome = d_estensione_nascosta;
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

   FOR c
      IN (SELECT fa.id_file_allegato,
                 fa.id_file_esterno,
                 fa.allegato_originale,
                 fa.content_type_originale,
                 fa.nome,
                 fa.nome_originale,
                 gom.id_documento,
                 gom.utente_aggiornamento,
                 gom.allegato,
                 gom.id_formato,
                 gom.data_aggiornamento
            FROM ${global.db.target.username}.file_allegati fa, oggetti_file gom
           WHERE     fa.id_file_esterno = gom.id_oggetto_file
                 and fa.id_file_allegato_originale is null
                 AND fa.allegato_originale IS NOT NULL
                 AND fa.nome_originale IS NOT NULL)
   LOOP

      select ${global.db.target.username}.hibernate_sequence.NEXTVAL  into d_id_file_allegato_orig from dual;
      select ogg_file_sq.nextval                into d_id_oggetto_file       from dual;

       d_nome_file := c.nome_originale||d_estensione_nascosta;

      INSERT INTO OGGETTI_FILE (ALLEGATO,
                                DATA_AGGIORNAMENTO,
                                DA_CANCELLARE,
                                FILENAME,
                                ID_DOCUMENTO,
                                ID_FORMATO,
                                ID_OGGETTO_FILE,
                                TESTOOCR,
                                UTENTE_AGGIORNAMENTO)
           VALUES (c.allegato,
                   c.data_aggiornamento,
                   'N',
                   d_nome_file,
                   c.id_documento,
                   d_id_formato_file_hidd,
                   d_id_oggetto_file,
                   empty_blob(),
                   c.utente_aggiornamento);

       update oggetti_file set testoocr = c.allegato_originale where id_oggetto_file = d_id_oggetto_file;

      INSERT INTO ${global.db.target.username}.FILE_ALLEGATI (ALLEGATO,
                                 ALLEGATO_ORIGINALE,
                                 CONTENT_TYPE,
                                 CONTENT_TYPE_ORIGINALE,
                                 DIMENSIONE,
                                 FIRMATO,
                                 ID_FILE_ALLEGATO,
                                 ID_FILE_ESTERNO,
                                 MODIFICABILE,
                                 NOME,
                                 NOME_ORIGINALE,
                                 TESTO,
                                 VERSION)
           VALUES (empty_blob(),
                   empty_blob(),
                   c.content_type_originale,
                   NULL,
                   DBMS_LOB.getlength (c.allegato_originale),
                   'N',
                   d_id_file_allegato_orig,
                   d_id_oggetto_file,
                   'N',
                   d_nome_file,
                   NULL,
                   NULL,
                   0);

      UPDATE ${global.db.target.username}.file_allegati
         SET id_file_allegato_originale = d_id_file_allegato_orig
         where id_file_allegato         = c.id_file_allegato;
      commit;
   END LOOP;
END;
/