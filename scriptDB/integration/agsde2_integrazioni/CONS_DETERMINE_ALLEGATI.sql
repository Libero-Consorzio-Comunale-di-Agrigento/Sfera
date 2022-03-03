--liquibase formatted sql
--changeset rdestasio:install_20210221_integrazioni_04 runOnChange:true

CREATE OR REPLACE FORCE VIEW CONS_DETERMINE_ALLEGATI
(
   TIPO_OGGETTO,
   ID_DETERMINA,
   ID_OGGETTO,
   ID_DOCUMENTO_ESTERNO,
   ID_FILE_GDM,
   CODICE,
   FILENAME,
   TITOLO,
   FIRMATARIO,
   DATA_FIRMA
)
AS
   SELECT 'VISTO',
          vp.id_determina,
          vp.id_visto_parere,
          vp.id_documento_esterno,
          fa.id_file_esterno,
          tvp.codice,
          fa.nome filename,
          tvp.titolo,
          utility_pkg.get_primo_firmatario_visto_par (vp.id_visto_parere),
          utility_pkg.get_prima_data_firma_visto_par (vp.id_visto_parere)
     FROM file_allegati fa,
          firmatari f,
          visti_pareri vp,
          tipi_visto_parere tvp
    WHERE     vp.id_file_allegato_testo = fa.id_file_allegato
          AND tvp.id_tipo_visto_parere = vp.id_tipologia
          AND vp.valido = 'Y'
          AND f.id_visto_parere = vp.id_visto_parere
          AND f.firmato = 'Y'
          AND f.data_firma IS NOT NULL
   UNION
   SELECT 'ALLEGATO',
          a.id_determina,
          a.id_allegato,
          a.id_documento_esterno,
          fa.id_file_esterno,
          ta.titolo,
          fa.nome filename,
          a.titolo,
          DECODE (
             fa.firmato,
             'Y', utility_pkg.get_primo_firmatario_determina (d.id_determina),
             ''),
          decode (fa.firmato, 'Y', utility_pkg.get_prima_data_firma_determina(d.id_determina), null)
     FROM allegati a,
          allegati_file af,
          file_allegati fa,
          determine d,
          tipi_allegato ta
    WHERE     fa.id_file_allegato = af.id_file
          AND af.id_allegato = a.id_allegato
          AND a.id_determina = d.id_determina
          AND a.id_tipo_allegato = ta.id_tipo_allegato(+)
          AND a.valido = 'Y'
          AND d.valido = 'Y'
          AND d.numero_determina IS NOT NULL
   UNION
   SELECT 'CERTIFICATO',
          c.id_determina,
          c.id_certificato,
          c.id_documento_esterno,
          fa.id_file_esterno,
          c.tipo,
          fa.nome filename,
          c.tipo,
          utility_pkg.get_primo_firmatario_certif (c.id_certificato),
          utility_pkg.get_prima_data_firma_certif (c.id_certificato)
     FROM file_allegati fa, firmatari f, certificati c
    WHERE     c.id_file_allegato_testo = fa.id_file_allegato
          AND c.valido = 'Y'
          AND f.id_certificato = c.id_certificato
          AND f.firmato = 'Y'
          AND f.data_firma IS NOT NULL
/