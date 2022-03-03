--liquibase formatted sql
--changeset rdestasio:install_20210221_integrazioni_02 runOnChange:true

CREATE OR REPLACE FORCE VIEW CONS_DELIBERE_ALLEGATI
(
   TIPO_OGGETTO,
   ID_DELIBERA,
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
   SELECT 'PARERE',
          d.id_delibera,
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
          delibere d,
          tipi_visto_parere tvp
    WHERE     vp.id_determina IS NULL
          AND vp.id_file_allegato_testo = fa.id_file_allegato
          AND d.id_delibera = vp.id_delibera
          AND tvp.id_tipo_visto_parere = vp.id_tipologia
          AND vp.valido = 'Y'
          AND f.id_visto_parere = vp.id_visto_parere
          AND f.firmato = 'Y'
          AND f.data_firma IS NOT NULL
   UNION
   SELECT 'PARERE',
          d.id_delibera,
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
          delibere d,
          tipi_visto_parere tvp
    WHERE     vp.id_determina IS NULL
          AND vp.id_file_allegato_testo = fa.id_file_allegato
          AND d.id_proposta_delibera = vp.id_proposta_delibera
          AND tvp.id_tipo_visto_parere = vp.id_tipologia
          AND NOT EXISTS (select 1 from visti_pareri v where v.id_delibera = d.id_delibera and v.id_tipologia = vp.id_tipologia)
          AND vp.valido = 'Y'
          AND f.id_visto_parere = vp.id_visto_parere
          AND f.firmato = 'Y'
          AND f.data_firma IS NOT NULL
   UNION
   SELECT 'ALLEGATO',
          a.id_delibera,
          a.id_allegato,
          a.id_documento_esterno,
          fa.id_file_esterno,
          ta.titolo,
          fa.nome filename,
          a.titolo,
          DECODE (
             fa.firmato,
             'Y', DECODE (
                     a.id_proposta_delibera,
                     NULL, utility_pkg.get_primo_firmatario_delibera (
                              d.id_delibera),
                     utility_pkg.get_primo_firmatario_prop_deli (
                        d.id_proposta_delibera)),
             ''),
          DECODE (
             fa.firmato,
             'Y', DECODE (
                     a.id_proposta_delibera,
                     NULL, utility_pkg.get_prima_data_firma_delibera (
                              d.id_delibera),
                     utility_pkg.get_prima_data_firma_prop_deli (
                        d.id_proposta_delibera)),
             '')
     FROM allegati a,
          allegati_file af,
          file_allegati fa,
          delibere d,
          tipi_allegato ta
    WHERE     fa.id_file_allegato = af.id_file
          AND af.id_allegato = a.id_allegato
          AND a.id_delibera = d.id_delibera
          AND a.id_tipo_allegato = ta.id_tipo_allegato(+)
          AND a.valido = 'Y'
          AND d.valido = 'Y'
          AND d.numero_delibera IS NOT NULL
   UNION
   SELECT 'CERTIFICATO',
          c.id_delibera,
          c.id_certificato,
          c.id_documento_esterno,
          fa.id_file_esterno,
          c.tipo,
          fa.nome filename,
          tc.titolo,
          utility_pkg.get_primo_firmatario_certif (c.id_certificato),
          utility_pkg.get_prima_data_firma_certif (c.id_certificato)
     FROM file_allegati fa, certificati c, tipi_certificato tc
    WHERE     c.id_file_allegato_testo = fa.id_file_allegato
          AND c.id_tipologia = tc.id_tipo_certificato
          AND c.valido = 'Y'
          AND c.id_delibera IS NOT NULL
/