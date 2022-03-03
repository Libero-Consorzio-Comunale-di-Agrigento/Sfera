CREATE OR REPLACE PACKAGE DELIBERA_AGSDE2 IS
/******************************************************************************
 NOME:        DELIBERA.
 DESCRIZIONE: Package della tabella DELIBERE utilizzato da AGSDE2 per integrazione con GS4.
              Questo package è una copia 1-1 del package DELIBERE ma senza la gestione delle eccezioni
              e senza commit/rollback.
 ANNOTAZIONI: Versione 1.0
 Rev. Data        Autore Descrizione
 ---- ----------  ------ ------------------------------------------------------
 0   15/09/2016  ES      Creazione.
******************************************************************************/
d_aggiorna_dati NUMBER(1) := 1;
cursor C_DELIBERE_PROPOSTA( p_anno_proposta NUMBER
                                                 , p_unita_proponente VARCHAR2
                                                 , p_numero_proposta NUMBER) is
(select anno, tipo_registro, numero
   from delibere
 where anno_proposta = p_anno_proposta
    and numero_proposta = p_numero_proposta
    and unita_proponente = p_unita_proponente)
;
Function  VERSIONE
RETURN varchar2;
FUNCTION  GET_INVIO_CORTE_CONTI
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
RETURN VARCHAR2;
FUNCTION  GET_INVIO_REV_CONTI
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
RETURN VARCHAR2;
FUNCTION  CALCOLA_DATA_FINE_PUBBL
( P_DATA_PUBBLICAZIONE IN DELIBERE.DATA_PUBBLICAZIONE%TYPE)
RETURN DELIBERE.DATA_FINE_PUBBLICAZIONE%TYPE;
FUNCTION  CALCOLA_DATA_FINE_PUBBL2
( P_DATA_PUBBLICAZIONE2 IN DELIBERE.DATA_PUBBLICAZIONE2%TYPE,
  P_TIPO_PROPOSTA IN DELIBERE.TIPO_PROPOSTA%TYPE)
RETURN DELIBERE.DATA_FINE_PUBBLICAZIONE2%TYPE;
FUNCTION  GET_DATA_MODIFICA
( p_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TABELLA IN VARCHAR2 DEFAULT NULL)
RETURN DELIBERE.DATA_MODIFICA%TYPE;
FUNCTION  GET_TIPO_TRATTAMENTO
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
RETURN VARCHAR2;
FUNCTION  GET_UTENTE_MODIFICA
( p_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TABELLA IN VARCHAR2 DEFAULT NULL)
RETURN DELIBERE.UTENTE_MODIFICA%TYPE;
FUNCTION  IS_TESTO_VISUALIZZABILE
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_UTENTE IN AD4_UTENTI.UTENTE%TYPE)
RETURN VARCHAR2;
FUNCTION  GET_FIRMATA
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
RETURN VARCHAR2;
FUNCTION  IS_TESTO_MODIFICABILE
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
RETURN VARCHAR2;
FUNCTION  GET_SE_POSITIVO_VISTO
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TIPO_VISTO IN TIPI_VISTO.TIPO_VISTO%TYPE)
RETURN VISTI_DELIBERA.SE_POSITIVO%TYPE;
FUNCTION  GET_DATA_VISTO
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TIPO_VISTO IN TIPI_VISTO.TIPO_VISTO%TYPE)
RETURN VISTI_DELIBERA.DATA_VISTO%TYPE;
FUNCTION  GET_DATA_FINE_PUBBL
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
RETURN VARCHAR2;
FUNCTION  GET_DATA_FINE_PUBBL2
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
RETURN VARCHAR2;
FUNCTION  GET_DATA_SPED_CRC
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
RETURN VARCHAR2;
FUNCTION  GET_DATA_ESECUTIVITA
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
RETURN VARCHAR2;
FUNCTION  GET_NI_FIRMATARIO_VISTO
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TIPO_VISTO IN TIPI_VISTO.TIPO_VISTO%TYPE)
RETURN VISTI_DELIBERA.CI%TYPE;
FUNCTION  GET_DES_FIRMATARIO_VISTO
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TIPO_VISTO IN TIPI_VISTO.TIPO_VISTO%TYPE)
RETURN VARCHAR2;
FUNCTION  GET_NUM_ALLEGATI
( P_ANNO DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO DELIBERE.NUMERO%TYPE
, P_CHECK_WEB varchar2 DEFAULT NULL) RETURN NUMBER;
PROCEDURE  SET_INVIO_REV_CONTI
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_DATA_INVIO DELIBERE.INVIO_REV_CONTI%TYPE);
PROCEDURE  SET_INVIO_CORTE_CONTI
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_DATA_INVIO DELIBERE.INVIO_CORTE_CONTI%TYPE);
PROCEDURE  SET_TIPO_TRATTAMENTO
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TIPO_TRATTAMENTO DELIBERE.TIPO_TRATTAMENTO%TYPE);
PROCEDURE  SET_TIPO_TRATTAMENTO
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TIPO_TRATTAMENTO DELIBERE.TIPO_TRATTAMENTO%TYPE
, P_RETURN_VALUE IN OUT NUMBER);
Procedure INSERT_DELIBERA
( p_ANNO_PROPOSTA IN PROPOSTE.ANNO%TYPE
, p_UNITA_PROPONENTE IN PROPOSTE.UNITA_PROPONENTE%TYPE
, p_NUMERO_PROPOSTA IN PROPOSTE.NUMERO%TYPE
, p_TIPO_PROPOSTA IN PROPOSTE.TIPO_PROPOSTA%TYPE
, p_DATA_DELIBERA IN OUT DOCUMENTI.DATA_PROTOCOLLO%TYPE
, p_OGGETTO IN DOCUMENTI.OGGETTO%TYPE
, p_CLASSIFICAZIONE IN DOCUMENTI.CLASSIFICAZIONE%TYPE
, p_ANNO_CLA IN DOCUMENTI.ANNO_CLA%TYPE
, p_NUMERO_CLA IN OUT DOCUMENTI.NUMERO_CLA%TYPE
, p_SUB IN OUT DOCUMENTI.SUB%TYPE
, p_CLASS_DAL IN DOCUMENTI.CLASS_DAL%TYPE
, P_UTENTE IN AD4_UTENTI.UTENTE%TYPE
, P_ANNO_DELIBERA IN OUT DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN OUT DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO_DELIBERA IN OUT DELIBERE.NUMERO%TYPE
, P_TIPO_DOCUMENTO IN DOCUMENTI.TIPO_DOCUMENTO%TYPE
, P_SE_CAPI IN DELIBERE.DA_SPED_CAPI%TYPE
, P_SE_CRC IN DELIBERE.DA_SPED_CRC%TYPE
, P_SE_PREF IN DELIBERE.DA_SPED_PREF%TYPE
, p_RISERVATO IN DOCUMENTI.RISERVATO%TYPE
, p_UNITA_RIS IN DOCUMENTI.UNITA_RIS %TYPE
, p_VISUALIZZA_LOTUS IN DOCUMENTI.VISUALIZZA_LOTUS%TYPE
, p_ESEC_IMMEDIATA IN DELIBERE.ESEGUIBILITA_IMMEDIATA%TYPE
, p_data_esecutivita in delibere.DATA_ESECUTIVITA%type default null
, p_data_pubblicazione in delibere.DATA_pubblicazione%type default null
, p_data_fine_pubblicazione in delibere.DATA_fine_pubblicazione%type default null
, p_data_pubblicazione2 in delibere.DATA_pubblicazione2%type default null
, p_data_fine_pubblicazione2 in delibere.data_fine_pubblicazione2%type default null
);
FUNCTION  GET_PROPOSTA
( P_ANNO              IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO     IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO            IN DELIBERE.NUMERO%TYPE)
RETURN VARCHAR2;
function crea(
   p_anno               NUMBER,
   p_tipo_registro      VARCHAR2,
   p_numero             NUMBER,
   p_data               VARCHAR2,
   p_oggetto            VARCHAR2,
   p_classificazione    VARCHAR2,
   p_class_dal          VARCHAR2,
   p_anno_cla           NUMBER,
   p_numero_cla         VARCHAR2,
   p_data_esec          VARCHAR2,
   p_data_pubbl         VARCHAR2,
   p_data_fine_pubbl    VARCHAR2,
   p_data_pubbl2        VARCHAR2,
   p_data_fine_pubbl2   VARCHAR2,
   p_utente             VARCHAR2,
   p_testo              BLOB,
   p_formato            VARCHAR2 default 'P',
   p_firmata            VARCHAR2 default 'Y',
   p_riservato          VARCHAR2 default null
)
return number;
FUNCTION  CHECK_DATA_FINE_PUBBL
( P_DATA_PUBBLICAZIONE IN date
, P_DATA_FINE_PUBBLICAZIONE IN date)
RETURN number;
FUNCTION  CHECK_DATA_FINE_PUBBL2
( P_DATA_PUBBLICAZIONE IN date
, P_DATA_FINE_PUBBLICAZIONE IN date
, p_tipo_proposta varchar2)
RETURN number;
END DELIBERA_AGSDE2;
/

CREATE OR REPLACE PACKAGE BODY DELIBERA_AGSDE2 IS
FUNCTION  VERSIONE
/******************************************************************************
 NOME:        VERSIONE
 DESCRIZIONE: Restituisce la versione e la data di distribuzione del package.
 PARAMETRI:   --
 RITORNA:     stringa varchar2 contenente versione e data.
 NOTE:        Il secondo numero della versione corrisponde alla revisione
              del package.
******************************************************************************/
RETURN varchar2 IS
BEGIN
   RETURN '1.7';
END VERSIONE;
FUNCTION CALCOLA_DATA_FINE_PUBBL
( P_DATA_PUBBLICAZIONE IN DELIBERE.DATA_PUBBLICAZIONE%TYPE)
/******************************************************************************
 NOME:        CALCOLA_DATA_FINE_PUBBL
 DESCRIZIONE: Calcola la data di la data di fine prima pubblicazione
                         in formato dd/mm/yyyy.
 RITORNO : Ula data di fine prima pubblicazione
                         in formato dd/mm/yyyy.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore   Descrizione
 ---- ----------  ------   ------------------------------------------------------
 0   14/01/2005   MT       Att.6682.0 Rif. DMP.GS4de.6: Creazione
 6   11/04/2011   MM       A42936.0.0: Gestire un parametro che permetta di
                           indicare se considerare il giorno di prima
                           pubblicazione o meno nel conteggio della data di
                           termine pubblicazione.
                           In base a questo andra' uniformato il calcolo su
                           protocollo e delibere considerando il valore di tale
                           parametro.
******************************************************************************/
   RETURN DELIBERE.DATA_FINE_PUBBLICAZIONE%TYPE
IS
   d_ritorno   DELIBERE.DATA_FINE_PUBBLICAZIONE%TYPE;
BEGIN
   SELECT DECODE (
             P_DATA_PUBBLICAZIONE,
             NULL, TO_DATE (NULL),
             TO_DATE (
                TO_CHAR (P_DATA_PUBBLICAZIONE, 'J')
                + TEMPO_DELIBERA.GET_GIORNI ('TERP')
                - DECODE (
                     NVL (
                        INSTALLAZIONE_PARAMETRO.GET_VALORE ('PUB_CONTA_PRIMO'),
                        'N'),
                     'Y', 1,
                     0),
                'J'))
     INTO d_ritorno
     FROM DUAL;
   RETURN d_ritorno;
EXCEPTION
   WHEN OTHERS
   THEN
      RETURN NULL;
END CALCOLA_DATA_FINE_PUBBL;
FUNCTION  CHECK_DATA_FINE_PUBBL
( P_DATA_PUBBLICAZIONE IN DATE
, P_DATA_FINE_PUBBLICAZIONE IN DATE)
RETURN NUMBER
IS
   d_ritorno   NUMBER := 1;
BEGIN
   IF p_data_pubblicazione IS NOT NULL
   THEN
      IF NVL (p_data_fine_pubblicazione,
              TO_DATE ('01/01/1900', 'dd/mm/yyyy'))
         - CALCOLA_DATA_FINE_PUBBL (p_data_pubblicazione) < 0
      THEN
         d_ritorno := 0;
      END IF;
   END IF;
   RETURN d_ritorno;
END;
FUNCTION CALCOLA_DATA_FINE_PUBBL2
( P_DATA_PUBBLICAZIONE2   IN DELIBERE.DATA_PUBBLICAZIONE2%TYPE
, P_TIPO_PROPOSTA         IN DELIBERE.TIPO_PROPOSTA%TYPE)
/******************************************************************************
 NOME:        CALCOLA_DATA_FINE_PUBBL2
 DESCRIZIONE: Calcola la data di la data di fine seconda pubblicazione
                         in formato dd/mm/yyyy.
 RITORNO : La data di fine seconda pubblicazione
                         in formato dd/mm/yyyy.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   14/01/2005  MT  Att.6682.0 Rif. DMP.GS4de.6: Creazione
 6   11/04/2011   MM       A42936.0.0: Gestire un parametro che permetta di
                           indicare se considerare il giorno di prima
                           pubblicazione o meno nel conteggio della data di
                           termine pubblicazione.
                           In base a questo andra' uniformato il calcolo su
                           protocollo e delibere considerando il valore di tale
                           parametro.
******************************************************************************/
   RETURN DELIBERE.DATA_FINE_PUBBLICAZIONE2%TYPE
IS
   d_ritorno   DELIBERE.DATA_FINE_PUBBLICAZIONE2%TYPE;
BEGIN
   SELECT DECODE (
             P_DATA_PUBBLICAZIONE2,
             NULL, TO_DATE (NULL),
             TO_DATE (
                TO_CHAR (P_DATA_PUBBLICAZIONE2, 'J')
                + tede_fine_seconda_pubbl (P_TIPO_PROPOSTA)
                - DECODE (
                      NVL (
                         INSTALLAZIONE_PARAMETRO.GET_VALORE (
                            'PUB_CONTA_PRIMO'),
                         'N'),
                      'Y', 1,
                      0),
                'J'))
     INTO d_ritorno
     FROM DUAL;
   RETURN d_ritorno;
EXCEPTION
   WHEN OTHERS
   THEN
      RETURN NULL;
END CALCOLA_DATA_FINE_PUBBL2;
FUNCTION  CHECK_DATA_FINE_PUBBL2
( P_DATA_PUBBLICAZIONE IN date
, P_DATA_FINE_PUBBLICAZIONE IN date
, p_tipo_proposta varchar2)
RETURN number
IS
   d_ritorno number:= 1;
BEGIN
   if p_data_pubblicazione is not null then
      if nvl(p_data_fine_pubblicazione, to_date('01/01/1900','dd/mm/yyyy')) - CALCOLA_DATA_FINE_PUBBL2(p_data_pubblicazione, p_tipo_proposta) < 0 then
         d_ritorno := 0;
      end if;
   end if;
   return d_ritorno;
END;
FUNCTION  GET_DATA_MODIFICA
( p_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TABELLA IN VARCHAR2 DEFAULT NULL)
RETURN DELIBERE.DATA_MODIFICA%TYPE IS
/******************************************************************************
 NOME:        GET_DATA_MODIFICA
 DESCRIZIONE: Restituisce DATA_MODIFICA della proposta o del testo
              o la max dei due, dipende da P_TABELLA .
 PARAMETRI:   chiave della proposta
              P_TABELLA IN VARCHAR2 (D = si vuole DELIBERE.DATA_MODIFICA,
                                  T = si vuole DELIBERE__TESTI.DATA_MODIFICA
                            null = si vuole il max dei due).
 RITORNA:     DATE      Restituisce DATA_MODIFICA
 ECCEZIONI:
 ANNOTAZIONI: Se la data e' null restituisce 01/01/1900
              Se ci sono errori restituisce null.
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    24/11/2004 SC     Attivita 6728.
******************************************************************************/
d_ritorno DELIBERE.DATA_MODIFICA%TYPE;
d_data_delibera DELIBERE.DATA_MODIFICA%TYPE := to_date('01/01/1900','dd/mm/yyyy');
d_data_testo DELIBERE.DATA_MODIFICA%TYPE := to_date('01/01/1900','dd/mm/yyyy');
BEGIN
   if nvl(P_TABELLA, 'D') = 'D' then
      select nvl(DATA_MODIFICA, to_date('01/01/1900','dd/mm/yyyy'))
        into d_data_delibera
        from DELIBERE
       where anno = p_anno
         and tipo_registro = p_tipo_registro
         and numero = p_numero
      ;
   end if;
   if nvl(P_TABELLA, 'T') = 'T' then
      d_data_testo := nvl(DELIBERE_TESTO.GET_DATA_MODIFICA(p_anno, p_tipo_registro, p_numero),to_date('01/01/1900','dd/mm/yyyy'));
   end if;
   if d_data_delibera >= d_data_testo then
      d_ritorno := d_data_delibera;
   else
      d_ritorno := d_data_testo;
   end if;
   return d_ritorno;
EXCEPTION
WHEN OTHERS THEN
   return null;
END GET_DATA_MODIFICA;
FUNCTION  GET_UTENTE_MODIFICA
( p_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TABELLA IN VARCHAR2 DEFAULT NULL)
RETURN DELIBERE.UTENTE_MODIFICA%TYPE IS
/******************************************************************************
 NOME:        GET_UTENTE_MODIFICA
 DESCRIZIONE: Restituisce UTENTE_MODIFICA della proposta o del testo
              o lil poiu' recente dei due, dipende da P_TABELLA .
 PARAMETRI:   chiave della proposta
              P_TABELLA IN VARCHAR2 (D = si vuole DELIBERE.DATA_MODIFICA,
                                  T = si vuole DELIBERE_TESTI.DATA_MODIFICA
                            null = si vuole il piu' recente dei due).
 RITORNA:     VARCHAR2      Restituisce UTENTE_MODIFICA
 ECCEZIONI:
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    24/11/2004 SC     Attivita 6728.
******************************************************************************/
d_ritorno DELIBERE.UTENTE_MODIFICA%TYPE;
d_data_delibera DELIBERE.DATA_MODIFICA%TYPE := to_date('01/01/1900','dd/mm/yyyy');
d_data_testo DELIBERE.DATA_MODIFICA%TYPE := to_date('01/01/1900','dd/mm/yyyy');
BEGIN
   if nvl(P_TABELLA, 'D') = 'D' then
      d_data_delibera := GET_DATA_MODIFICA(p_anno, p_tipo_registro, p_numero, 'D');
   end if;
   if nvl(P_TABELLA, 'T') = 'T' then
      d_data_testo := GET_DATA_MODIFICA(p_anno, p_tipo_registro, p_numero, 'T');
   end if;
   if d_data_delibera >= d_data_testo
   and nvl(P_TABELLA, 'D') = 'D' then
      select UTENTE_MODIFICA
        into d_ritorno
        from DELIBERE
       where anno = p_anno
         and tipo_registro = p_tipo_registro
         and numero = p_numero
      ;
   else
      d_ritorno := DELIBERE_TESTO.GET_UTENTE_MODIFICA(p_anno, p_tipo_registro, p_numero);
   end if;
   return d_ritorno;
EXCEPTION
WHEN OTHERS THEN
   return null;
END GET_UTENTE_MODIFICA;
FUNCTION  GET_FIRMATA
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
/******************************************************************************
 NOME:        GET_FIRMATA
 DESCRIZIONE: Restituisce FIRMATA.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 5   29/12/2010   MM          CREAZIONE
******************************************************************************/
RETURN VARCHAR2 IS
d_ritorno varchar2(10);
BEGIN
   select FIRMATA
      into d_ritorno
     from delibere
   where anno = p_anno
      and numero = p_numero
      and tipo_registro = p_tipo_registro
   ;
   return d_ritorno;
EXCEPTION
WHEN OTHERS THEN
   return null;
END GET_FIRMATA;
FUNCTION  IS_TESTO_MODIFICABILE
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
/******************************************************************************
 NOME:        IS_TESTO_MODIFICABILE
 DESCRIZIONE: verifica se il testo e' modificabile, cioe' se ha tipo_trattamento <= 2
 RITORNO       : 'Y' se e' modificabile
                         'N' altriemnti.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   16/10/2003  SC  Creazione.
******************************************************************************/
RETURN VARCHAR2 IS
d_ritorno VARCHAR2(1);
BEGIN
   select decode(nvl(tipo_trattamento,0),3,'N',4,'N','Y')
      into d_ritorno
     from delibere
  where anno = p_anno
     and numero = p_numero
     and tipo_registro = p_tipo_registro
   ;
   return d_ritorno;
EXCEPTION
WHEN OTHERS THEN
   return null;
END IS_TESTO_MODIFICABILE;
FUNCTION  IS_TESTO_VISUALIZZABILE
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_UTENTE IN AD4_UTENTI.UTENTE%TYPE)
/******************************************************************************
 NOME:        IS_TESTO_VISUALIZZABILE
 DESCRIZIONE: verifica se p_utente puo' visualizzare il testo della delibera
 RITORNO       : 'Y' se e' visualizzabile
                         'N' altriemnti.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   16/10/2003   SC          Creazione.
 7   28/06/2012   MM          Gestione parametro  PUBB_ESPL_TESTO
******************************************************************************/
RETURN VARCHAR2 IS
d_ritorno VARCHAR2(1);
BEGIN
   --select decode( sign(2-tipo_trattamento), -1, decode(nvl(DOCUMENTO.GET_RISERVATO(ANNO, TIPO_REGISTRO, NUMERO), 'N'), 'Y', decode(nvl(get_comp_unita(p_utente, 'V_RISERVATO_TX', DOCUMENTO.GET_UNITA_RIS(ANNO, TIPO_REGISTRO, NUMERO)),0), 0, 'N', 'Y'), 'Y'), 'N')
   select DECODE(nvl(INSTALLAZIONE_PARAMETRO.get_valore('PUBB_ESPL_TESTO'),'N'), 'Y', nvl(DELIBERE_COPIA.GET_TESTO_PUBBLICABILE(p_anno, p_tipo_registro, p_numero),'N'), decode( sign(2-tipo_trattamento), -1, decode(nvl(DOCUMENTO.GET_RISERVATO(ANNO, TIPO_REGISTRO, NUMERO), 'N'), 'Y', decode(nvl(get_comp_unita(p_utente, 'V_RISERVATO_TX', DOCUMENTO.GET_UNITA_RIS(ANNO, TIPO_REGISTRO, NUMERO)),0), 0, 'N', 'Y'), 'Y'), 'N'))
     into d_ritorno
     from delibere
   where anno = p_anno
     and numero = p_numero
     and tipo_registro = p_tipo_registro
   ;
   return d_ritorno;
EXCEPTION
WHEN OTHERS THEN
   return null;
END IS_TESTO_VISUALIZZABILE;
FUNCTION  GET_SE_POSITIVO_VISTO
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TIPO_VISTO IN TIPI_VISTO.TIPO_VISTO%TYPE)
/******************************************************************************
 NOME:        GET_SE_POSITIVO_VISTO
 DESCRIZIONE: Verifica se il vosto e' favorevole
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   16/10/2003  SC  Creazione.
******************************************************************************/
RETURN VISTI_DELIBERA.SE_POSITIVO%TYPE IS
BEGIN
   return VISTO_DELIBERA.GET_SE_POSITIVO(p_anno, p_tipo_registro, p_numero, p_tipo_visto);
EXCEPTION
WHEN OTHERS THEN
   return null;
END GET_SE_POSITIVO_VISTO;
FUNCTION  GET_DATA_VISTO
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TIPO_VISTO IN TIPI_VISTO.TIPO_VISTO%TYPE)
/******************************************************************************
 NOME:        GET_DATA_VISTO
 DESCRIZIONE: Restituisce la data in cui e' stato opposto il visto
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   16/10/2003  SC  Creazione.
******************************************************************************/
RETURN VISTI_DELIBERA.DATA_VISTO%TYPE IS
BEGIN
   return VISTO_DELIBERA.GET_DATA_VISTO(p_anno, p_tipo_registro, p_numero, p_tipo_visto);
EXCEPTION
WHEN OTHERS THEN
   return null;
END GET_DATA_VISTO;
FUNCTION  GET_INVIO_REV_CONTI
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
/******************************************************************************
 NOME:        GET_INVIO_REV_CONTI
 DESCRIZIONE: Restituisce la data di invio ai revisori dei conti
                         in formato dd/mm/yyyy.
 RITORNO : Una stringa contenente la data di invio ai revisori dei conti
                         in formato dd/mm/yyyy.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   29/09/2005  SC  A11444.
******************************************************************************/
RETURN VARCHAR2 IS
d_ritorno varchar2(10);
BEGIN
   select to_char(INVIO_REV_CONTI,'dd/mm/yyyy')
      into d_ritorno
     from delibere
   where anno = p_anno
      and numero = p_numero
      and tipo_registro = p_tipo_registro
   ;
   return d_ritorno;
EXCEPTION
WHEN OTHERS THEN
   return null;
END GET_INVIO_REV_CONTI;
FUNCTION  GET_INVIO_CORTE_CONTI
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
/******************************************************************************
 NOME:        GET_INVIO_CORTE_CONTI
 DESCRIZIONE: Restituisce la data di invio alla corte dei conti
                         in formato dd/mm/yyyy.
 RITORNO : Una stringa contenente la data di invio alla corte dei conti
                         in formato dd/mm/yyyy.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   29/09/2005  SC  A11444.
******************************************************************************/
RETURN VARCHAR2 IS
d_ritorno varchar2(10);
BEGIN
   select to_char(INVIO_CORTE_CONTI,'dd/mm/yyyy')
      into d_ritorno
     from delibere
   where anno = p_anno
      and numero = p_numero
      and tipo_registro = p_tipo_registro
   ;
   return d_ritorno;
EXCEPTION
WHEN OTHERS THEN
   return null;
END GET_INVIO_CORTE_CONTI;
FUNCTION  GET_TIPO_TRATTAMENTO
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
/******************************************************************************
 NOME:        GET_TIPO_TRATTAMENTO
 DESCRIZIONE: Restituisce TIPO_TRATTAMENTO.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   29/09/2005  SC  A11444.
******************************************************************************/
RETURN VARCHAR2 IS
d_ritorno varchar2(10);
BEGIN
   select TIPO_TRATTAMENTO
      into d_ritorno
     from delibere
   where anno = p_anno
      and numero = p_numero
      and tipo_registro = p_tipo_registro
   ;
   return d_ritorno;
EXCEPTION
WHEN OTHERS THEN
   return null;
END GET_TIPO_TRATTAMENTO;
PROCEDURE  SET_INVIO_CORTE_CONTI
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_DATA_INVIO DELIBERE.INVIO_CORTE_CONTI%TYPE) IS
/******************************************************************************
 NOME:        SET_INVIO_CORTE_CONTI
 DESCRIZIONE: Modifica la data di invio alla corte dei conti
                         in formato dd/mm/yyyy.
 RITORNO : Una stringa contenente la data di invio alla corte dei conti
                         in formato dd/mm/yyyy.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   29/09/2005  SC  A11444.
******************************************************************************/
BEGIN
   update delibere
      set INVIO_CORTE_CONTI = p_data_invio
   where anno = p_anno
      and numero = p_numero
      and tipo_registro = p_tipo_registro
   ;
EXCEPTION
WHEN OTHERS THEN
   raise;
END SET_INVIO_CORTE_CONTI;
PROCEDURE  SET_INVIO_REV_CONTI
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_DATA_INVIO DELIBERE.INVIO_REV_CONTI%TYPE) IS
/******************************************************************************
 NOME:        SET_INVIO_REV_CONTI
 DESCRIZIONE: Modifica la data di invio ai revisori dei conti
                         in formato dd/mm/yyyy.
 RITORNO : Una stringa contenente la data di invio ai revisori dei conti
                         in formato dd/mm/yyyy.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   29/09/2005  SC  A11444.
******************************************************************************/
BEGIN
   update delibere
      set INVIO_REV_CONTI = p_data_invio
   where anno = p_anno
      and numero = p_numero
      and tipo_registro = p_tipo_registro
   ;
EXCEPTION
WHEN OTHERS THEN
   raise;
END SET_INVIO_REV_CONTI;
PROCEDURE  SET_TIPO_TRATTAMENTO
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TIPO_TRATTAMENTO DELIBERE.TIPO_TRATTAMENTO%TYPE) IS
/******************************************************************************
 NOME:        SET_TIPO_TRATTAMENTO
 DESCRIZIONE: Modifica TIPO_TRATTAMENTO.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   16/01/2006   SC.
******************************************************************************/
BEGIN
   update delibere
      set tipo_trattamento = p_tipo_trattamento
   where anno = p_anno
      and numero = p_numero
      and tipo_registro = p_tipo_registro
   ;
EXCEPTION
WHEN OTHERS THEN
   raise;
END SET_TIPO_TRATTAMENTO;
PROCEDURE  SET_TIPO_TRATTAMENTO
( P_ANNO             IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO    IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO           IN DELIBERE.NUMERO%TYPE
, P_TIPO_TRATTAMENTO IN DELIBERE.TIPO_TRATTAMENTO%TYPE
, P_RETURN_VALUE     IN OUT NUMBER) IS
/******************************************************************************
 NOME:        SET_TIPO_TRATTAMENTO
 DESCRIZIONE: Modifica il TIPO_TRATTAMENTO di un atto. Rispetto alla precedente,
              gestisce la variabile di package d_aggiorna_dati che consente di
              disabilitare la parte di codice del trigger DELIBERE_TIU che aggiorna
              i campi utente e data_modifica dell'atto.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   28/03/2007   SN.         A20343.0.0 In caso di modifica del tipo trattamento di
                              una delibera/determina viene aggiornata l'informazione
                              relativa all'utente e alla data di modifica dell'atto
                              anche se in realtà l'utente ha solo effettuata la
                              stampa del testo.
******************************************************************************/
BEGIN
   d_aggiorna_dati := 0;
   update delibere
      set tipo_trattamento = p_tipo_trattamento
    where anno = p_anno
      and numero = p_numero
      and tipo_registro = p_tipo_registro
   ;
   d_aggiorna_dati := 1;
   if sql%rowcount = 0 then
      P_RETURN_VALUE := -1;
   end if;
EXCEPTION
WHEN OTHERS THEN
   P_RETURN_VALUE  := -1;
   d_aggiorna_dati := 1;
   raise;
END SET_TIPO_TRATTAMENTO;
FUNCTION  GET_DATA_FINE_PUBBL
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
/******************************************************************************
 NOME:        GET_DATA_FINE_PUBBL
 DESCRIZIONE: Restituisce la data di fine prima pubblicazione
                         in formato dd/mm/yyyy.
 RITORNO : Una stringa contenente la data di fine prima pubblicazione
                         in formato dd/mm/yyyy.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   16/10/2003  SC  Creazione.
 1   14/01/2005  MT  Att.6682.0 Rif. DMP.GS4de.6: Restituise il campo di DB DELIBERA.DATA_FINE_PUBBLICAZION
******************************************************************************/
RETURN VARCHAR2 IS
d_ritorno varchar2(10);
BEGIN
   select to_char(DATA_FINE_PUBBLICAZIONE,'dd/mm/yyyy')
      into d_ritorno
     from delibere
   where anno = p_anno
      and numero = p_numero
      and tipo_registro = p_tipo_registro
   ;
   return d_ritorno;
EXCEPTION
WHEN OTHERS THEN
   return null;
END GET_DATA_FINE_PUBBL;
FUNCTION  GET_DATA_ESECUTIVITA
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
/******************************************************************************
 NOME:        GET_DATA_ESECUTIVITA
 DESCRIZIONE: Restituisce la data di esecutivita'
                         in formato dd/mm/yyyy.
 RITORNO : Una stringa contenente la data di esecutivita'
                         in formato dd/mm/yyyy.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   03/11/2003  SC  Creazione.
******************************************************************************/
RETURN VARCHAR2 IS
d_ritorno varchar2(10);
BEGIN
   select to_char(data_esecutivita, 'dd/mm/yyyy')
      into d_ritorno
     from delibere
   where anno = p_anno
      and numero = p_numero
      and tipo_registro = p_tipo_registro
   ;
   return d_ritorno;
EXCEPTION
WHEN OTHERS THEN
   return null;
END GET_DATA_ESECUTIVITA;
FUNCTION  GET_DATA_SPED_CRC
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
/******************************************************************************
 NOME:        GET_DATA_SPED_CRC
 DESCRIZIONE: Restituisce la data di spedizione  crc
                         in formato dd/mm/yyyy.
 RITORNO : Una stringa contenente la data di spedizione  crc
                         in formato dd/mm/yyyy.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   03/11/2003  SC  Creazione.
******************************************************************************/
RETURN VARCHAR2 IS
d_ritorno varchar2(10);
BEGIN
   select to_char(data_spedizione_crc, 'dd/mm/yyyy')
      into d_ritorno
     from delibere
   where anno = p_anno
      and numero = p_numero
      and tipo_registro = p_tipo_registro
   ;
   return d_ritorno;
EXCEPTION
WHEN OTHERS THEN
   return null;
END GET_DATA_SPED_CRC;
FUNCTION  GET_DATA_FINE_PUBBL2
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE)
/******************************************************************************
 NOME:        GET_DATA_FINE_PUBBL2
 DESCRIZIONE: Restituisce la data di fine seconda pubblicazione
                         in formato dd/mm/yyyy.
 RITORNO : Una stringa contenente la data di fine seconda pubblicazione
                         in formato dd/mm/yyyy.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   16/10/2003  SC  Creazione.
 1   14/01/2005  MT  Att.6682.0 Rif. DMP.GS4de.6: Restituise il campo di DB DELIBERA.DATA_FINE_PUBBLICAZION
******************************************************************************/
RETURN VARCHAR2 IS
d_ritorno varchar2(10);
BEGIN
   select to_char(DATA_FINE_PUBBLICAZIONE2,'dd/mm/yyyy')
      into d_ritorno
     from delibere
   where anno = p_anno
      and numero = p_numero
      and tipo_registro = p_tipo_registro
   ;
   return d_ritorno;
EXCEPTION
WHEN OTHERS THEN
   return null;
END GET_DATA_FINE_PUBBL2;
FUNCTION  GET_NI_FIRMATARIO_VISTO
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TIPO_VISTO IN TIPI_VISTO.TIPO_VISTO%TYPE)
/******************************************************************************
 NOME:        GET_NI_FIRMATARIO_VISTO
 DESCRIZIONE: Restituisce l'identificativo del firmatario del visto
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   16/10/2003  SC  Creazione.
******************************************************************************/
RETURN VISTI_DELIBERA.CI%TYPE IS
BEGIN
   return VISTO_DELIBERA.GET_NI_FIRMATARIO(p_anno, p_tipo_registro, p_numero, p_tipo_visto);
EXCEPTION
WHEN OTHERS THEN
   return null;
END GET_NI_FIRMATARIO_VISTO;
FUNCTION  GET_DES_FIRMATARIO_VISTO
( P_ANNO IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO IN DELIBERE.NUMERO%TYPE
, P_TIPO_VISTO IN TIPI_VISTO.TIPO_VISTO%TYPE)
/******************************************************************************
 NOME:        GET_DES_FIRMATARIO_VISTO
 DESCRIZIONE: Restituisce il cognome/nome del firmatario del visto
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data        Autore      Descrizione
 ---- ----------  ------      ------------------------------------------------------
 0   16/10/2003  SC  Creazione.
******************************************************************************/
RETURN VARCHAR2 IS
BEGIN
   return VISTO_DELIBERA.GET_NI_FIRMATARIO(p_anno, p_tipo_registro, p_numero, p_tipo_visto);
EXCEPTION
WHEN OTHERS THEN
   return null;
END GET_DES_FIRMATARIO_VISTO;
FUNCTION  GET_NUM_ALLEGATI
( P_ANNO DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO DELIBERE.NUMERO%TYPE
, P_CHECK_WEB varchar2 DEFAULT NULL)
/******************************************************************************
 NOME:        GET_NUM_ALLEGATI
 DESCRIZIONE: Somma i campi ALLEGATI_DOCUMENTO.QUANTITA e
              ALLEGATI_PROPOSTA.QUANTITA per la delibera in INPUT
  PARAMETRI:   Chiave delibera
 RITORNA:     INTEGER Somma ALLEGATI_DOCUMENTO.QUANTITA e
              ALLEGATI_PROPOSTA.QUANTITA
 ******************************************************************************/
RETURN NUMBER IS
d_anno_pro           PROPOSTE.ANNO%TYPE;
d_unita_proponente   PROPOSTE.UNITA_PROPONENTE%TYPE;
d_numero_pro         PROPOSTE.NUMERO%TYPE;
d_return number;
BEGIN
   select anno_proposta,unita_proponente,numero_proposta
     into d_anno_pro, d_unita_proponente , d_numero_pro
     from DELIBERE
    where anno = p_anno
      and tipo_registro = p_tipo_registro
      and numero = p_numero
   ;
   d_return := ALLEGATO_DOCUMENTO.GET_NUM_ALLEGATI(p_anno, p_tipo_registro, p_numero, p_check_web);
   d_return := d_return + ALLEGATO_PROPOSTA.GET_NUM_ALLEGATI(d_anno_pro, d_unita_proponente , d_numero_pro, p_check_web);
   return d_return;
END GET_NUM_ALLEGATI;
Procedure INSERT_DELIBERA
( p_ANNO_PROPOSTA IN PROPOSTE.ANNO%TYPE
, p_UNITA_PROPONENTE IN PROPOSTE.UNITA_PROPONENTE%TYPE
, p_NUMERO_PROPOSTA IN PROPOSTE.NUMERO%TYPE
, p_TIPO_PROPOSTA IN PROPOSTE.TIPO_PROPOSTA%TYPE
, p_DATA_DELIBERA IN OUT DOCUMENTI.DATA_PROTOCOLLO%TYPE
, p_OGGETTO IN DOCUMENTI.OGGETTO%TYPE
, p_CLASSIFICAZIONE IN DOCUMENTI.CLASSIFICAZIONE%TYPE
, p_ANNO_CLA IN DOCUMENTI.ANNO_CLA%TYPE
, p_NUMERO_CLA IN OUT DOCUMENTI.NUMERO_CLA%TYPE
, p_SUB IN OUT DOCUMENTI.SUB%TYPE
, p_CLASS_DAL IN DOCUMENTI.CLASS_DAL%TYPE
, P_UTENTE IN AD4_UTENTI.UTENTE%TYPE
, P_ANNO_DELIBERA IN OUT DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO IN OUT DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO_DELIBERA IN OUT DELIBERE.NUMERO%TYPE
, P_TIPO_DOCUMENTO IN DOCUMENTI.TIPO_DOCUMENTO%TYPE
, P_SE_CAPI IN DELIBERE.DA_SPED_CAPI%TYPE
, P_SE_CRC IN DELIBERE.DA_SPED_CRC%TYPE
, P_SE_PREF IN DELIBERE.DA_SPED_PREF%TYPE
, p_RISERVATO IN DOCUMENTI.RISERVATO%TYPE
, p_UNITA_RIS IN DOCUMENTI.UNITA_RIS %TYPE
, p_VISUALIZZA_LOTUS IN DOCUMENTI.VISUALIZZA_LOTUS%TYPE
, p_ESEC_IMMEDIATA IN DELIBERE.ESEGUIBILITA_IMMEDIATA%TYPE
, p_data_esecutivita in delibere.DATA_ESECUTIVITA%type default null
, p_data_pubblicazione in delibere.DATA_pubblicazione%type default null
, p_data_fine_pubblicazione in delibere.DATA_fine_pubblicazione%type default null
, p_data_pubblicazione2 in delibere.DATA_pubblicazione2%type default null
, p_data_fine_pubblicazione2 in delibere.data_fine_pubblicazione2%type default null
) IS
/******************************************************************************
 NOME:        INSERT_DELIBERA
 DESCRIZIONE: Crea DELIBERA, fa anche l'insert in DELIBERE_TESTI
                         perche da web fa solo update dell'eventuale testo.
 PARAMETRI:
 ECCEZIONI:   Exception
 REVISIONE:   Stefi - venerdi 29 agosto 2003
              MT    16/07/2004  Per far scattare il trigger in fase di inserimento nella DEL
                                nserisco anche la DATA_ESECUTIVITA
                                se PROPOSTE.ESECUTIVITA = 'Y'
 11/01/2006 SC  A14263.1 Passa il class_dal a PROTOCOLLO_ESTERNO.
******************************************************************************/
d_oggetto_fasc FASCICOLI.OGGETTO%TYPE;
--MT 16/07/2004
d_data_esecutivita    DELIBERE.DATA_ESECUTIVITA%TYPE := p_data_esecutivita;
BEGIN
   si4.utente := nvl(p_utente, si4.utente);
      PROTOCOLLO_ESTERNO.SET_PROTOCOLLO
     ( p_anno_delibera
     , p_tipo_registro
     , p_numero_delibera
      , p_data_delibera
     , 'INT'
     , p_tipo_documento
     , p_oggetto
     , p_classificazione
     , p_anno_cla
      , p_numero_cla
     , p_sub
     , d_oggetto_fasc
      , null --uff smistamento
      , null -- mitt dest
      , null -- unita prot
      , si4.utente
      , null -- p_numero_documento IN varchar2
      , null -- p_data_documento IN date
      , null -- p_data_arrivo IN date
      , null -- p_mod_ricevimento IN varchar2
      , null -- p_data_scadenza IN date
      , null -- p_note IN varchar2
      , 'GS4DE' -- p_applicativo_esterno IN varchar2
      , 'N' -- p_smist_esterni IN varchar2 default 'Y'
      , 'N' -- p_insert_mittdest IN varchar2 default 'Y'
      , 'N' --, p_apri_registro IN varchar2 default 'N'
      , p_riservato --, p_riservato IN varchar2 default 'N'
      , p_unita_ris --, p_unita_ris IN varchar2 default null
      , p_visualizza_lotus --, p_visualizza_lotus IN varchar2 default 'Y'
     , null --, p_ora_protocollo IN NUMBER default null
     , null --, p_utente_firma IN VARCHAR2 default null
     , p_class_dal --, p_class_dal IN DATE default null
      );
               --MT 16/07/2004
         --per far scattare il trigger in fase di inserimento nella DEL
         --inserisco anche la DATA_ESECUTIVITA se PROPOSTE.ESECUTIVITA = 'Y'
         /*insert into delibere(anno, tipo_registro, numero, anno_proposta, unita_proponente, numero_proposta
         , tipo_proposta, da_sped_capi, da_sped_pref, da_sped_crc, eseguibilita_immediata)
         values (p_anno_delibera, p_tipo_registro, p_numero_delibera, p_anno_proposta
         , p_unita_proponente, p_numero_proposta, p_tipo_proposta, p_se_capi, p_se_pref, p_se_crc, p_esec_immediata)
         ;*/
         if d_data_esecutivita is null then
            select decode(esecutivita,'Y',TRUNC(SYSDATE),null)
              into d_data_esecutivita
              from PROPOSTE
             where anno = p_anno_proposta
               and unita_proponente = P_UNITA_PROPONENTE
               and numero = p_numerO_proposta
            ;
         end if;
         insert into delibere(anno, tipo_registro, numero, anno_proposta, unita_proponente, numero_proposta
         , tipo_proposta, da_sped_capi, da_sped_pref, da_sped_crc, eseguibilita_immediata,data_esecutivita
         , data_pubblicazione, data_fine_pubblicazione, data_pubblicazione2, data_fine_pubblicazione2)
         values (p_anno_delibera, p_tipo_registro, p_numero_delibera, p_anno_proposta
         , p_unita_proponente, p_numero_proposta, p_tipo_proposta, p_se_capi, p_se_pref, p_se_crc, p_esec_immediata,d_data_esecutivita
         , p_data_pubblicazione, p_data_fine_pubblicazione, p_data_pubblicazione2, p_data_fine_pubblicazione2)
         ;
END INSERT_DELIBERA;

Procedure ADD_ALLEGATO
( P_ALLEGATO_ID IN OUT ALLEGATI_DOCUMENTO.ALLEGATO_ID%TYPE
, P_TIPO IN VARCHAR2
, P_ANNO DOCUMENTI.ANNO%TYPE
, P_NUMERO DOCUMENTI.NUMERO%TYPE
, P_TIPO_REGISTRO DOCUMENTI.TIPO_REGISTRO%TYPE
, P_TIPO_ALLEGATO TIPI_ALLEGATO.TIPO_ALLEGATO%TYPE
, P_DESCRIZIONE ALLEGATI_DOCUMENTO.DESCRIZIONE%TYPE
, P_QUANTITA ALLEGATI_DOCUMENTO.QUANTITA%TYPE
, P_FILE_ALLEGATO ALLEGATI_DOCUMENTO.FILE_ALLEGATO%TYPE
, P_PAGINE ALLEGATI_DOCUMENTO.PAGINE%TYPE
, P_UTENTE AD4_UTENTI.UTENTE%TYPE
, P_DATA_MODIFICA VARCHAR2
, P_UNITA_MODIFICA UNITA.UNITA%TYPE
, P_DOCUMENT_ID ALLEGATI_DOCUMENTO.DOCUMENT_ID%TYPE DEFAULT NULL
, P_FIRMATO ALLEGATI_DOCUMENTO.FIRMATO%TYPE DEFAULT 'N'
, P_VERIFICA_FIRMA ALLEGATI_DOCUMENTO.VERIFICA_FIRMA%TYPE DEFAULT 'N'
, P_VALIDITA ALLEGATI_DOCUMENTO.VALIDITA%TYPE DEFAULT 'N'
, P_TITOLO_DOCUMENTO ALLEGATI_DOCUMENTO.TITOLO_DOCUMENTO%TYPE DEFAULT NULL
, P_PR_CODICE_AMMINISTRAZIONE ALLEGATI_DOCUMENTO.PR_CODICE_AMMINISTRAZIONE%TYPE DEFAULT NULL
, P_PR_CODICE_AOO ALLEGATI_DOCUMENTO.PR_CODICE_AOO%TYPE DEFAULT NULL
, P_PR_NUMERO ALLEGATI_DOCUMENTO.PR_NUMERO%TYPE DEFAULT NULL
, P_PR_DATA VARCHAR2 DEFAULT NULL) IS
/******************************************************************************
 NOME:        INSERT_ALLEGATO_DOCUMENTO
 DESCRIZIONE: Inserisce un record in ALLEGATI_DOCUMENTO
 PARAMETRI: P_ALLEGATO_ID IN OUT ALLEGATI_DOCUMENTO.ALLEGATO_ID%TYPE Id dell'allegato
                      P_TIPO VARCHAR2 Indica se allegato di delibera o di proposta. Valori possibili: D Delibera
                                                                                                                                            P Proposta
                      P_ANNO DOCUMENTI.ANNO%TYPE           Anno del documento o della proposta
                      P_NUMERO DOCUMENTI.NUMERO%TYPE Numero del documento o della proposta
                      P_TIPO_REGISTRO DOCUMENTI.TIPO_REGISTRO%TYPE Tipo registro del documento
                                                                                        o Unita' proponente della proposta
                      P_TIPO_ALLEGATO TIPI_ALLEGATO.TIPO_ALLEGATO%TYPE Tipo di allegato (obbligatorio)
                      P_DESCRIZIONE ALLEGATI_DOCUMENTO.DESCRIZIONE%TYPE Descrizione dell'allegato
                      P_QUANTITA ALLEGATI_DOCUMENTO.QUANTITA%TYPE Numero di allegati dellos tesso tipo
                      P_FILE_ALLEGATO ALLEGATI_DOCUMENTO.FILE_ALLEGATO%TYPE File dell'allegato
                      P_PAGINE ALLEGATI_DOCUMENTO.PAGINE%TYPE     Numero di pagine da cui e' costituito l'allegato
                      P_UTENTE AD4_UTENTI.UTENTE%TYPE                   Utente che effettua l'inserimento
                      P_DATA_MODIFICA ALLEGATI_DOCUMENTO.DATA_MODIFICA%TYPE DEFAULT TRUNC(SYSDATE) Data dell'inserimento
                      P_DOCUMENT_ID ALLEGATI_DOCUMENTO.DOCUMENT_ID%TYPE DEFAULT NULL Id del file generato da Ascent Capture
                      P_FIRMATO ALLEGATI_DOCUMENTO.FIRMATO%TYPE DEFAULT 'N'               Y/N Indica se il file e firmato
                      P_VERIFICA_FIRMA ALLEGATI_DOCUMENTO.VERIFICA_FIRMA%TYPE DEFAULT 'N' Y/N Indica se la firma digitale e' stata verificata
                      P_VALIDITA ALLEGATI_DOCUMENTO.VALIDITA%TYPE DEFAULT 'N'        Y/N Indica se il file e' stato firmato da un soggetto abilitato a farlo
                      P_TITOLO_DOCUMENTO ALLEGATI_DOCUMENTO.TITOLO_DOCUMENTO%TYPE DEFAULT NULL
                      P_PR_CODICE_AMMINISTRAZIONE ALLEGATI_DOCUMENTO.PR_CODICE_AMMINISTRAZIONE%TYPE DEFAULT NULL
                      P_PR_CODICE_AOO ALLEGATI_DOCUMENTO.PR_CODICE_AOO%TYPE DEFAULT NULL
                      P_PR_NUMERO ALLEGATI_DOCUMENTO.PR_NUMERO%TYPE DEFAULT NULL
                      P_PR_DATA ALLEGATI_DOCUMENTO.PR_DATA%TYPE DEFAULT NULL        Dati di prima registrazione ricavati dalla Segnatura)
 RITORNA:
 ECCEZIONI:
 ANNOTAZIONI: Gestisce le date come VARCHAR2                    Formato da utilizzare DD/MM/YYYY
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 1    31/10/2003  SC    Prima emissione.
******************************************************************************/
BEGIN
   if p_tipo = 'D' then
      ALLEGATO_DOCUMENTO.INSERT_ALLEGATO_DOCUMENTO(p_allegato_id, p_anno, p_numero, p_tipo_registro, p_tipo_allegato, p_descrizione
      , p_quantita, p_file_allegato, p_pagine, p_utente, p_data_modifica, p_document_id, p_firmato, p_verifica_firma, p_validita
      , p_titolo_documento, p_pr_codice_amministrazione, p_pr_codice_aoo, p_pr_numero, p_pr_data);
   else
      ALLEGATO_PROPOSTA.INSERT_ALLEGATO_PROPOSTA(p_allegato_id, p_anno, p_numero
      , p_tipo_registro, p_tipo_allegato, p_descrizione, p_quantita, p_file_allegato
      , p_pagine, p_unita_modifica, p_utente, p_data_modifica, p_firmato, p_verifica_firma, p_validita);
   end if;
EXCEPTION
WHEN OTHERS THEN
   raise;
END ADD_ALLEGATO;
Procedure SET_TESTO_ALLEGATO
( P_ALLEGATO_ID IN ALLEGATI_DOCUMENTO.ALLEGATO_ID%TYPE
, P_TESTO IN FILE_ALLEGATO.FILE_ALLEGATO%TYPE
, P_TIPO IN VARCHAR2) IS
/******************************************************************************
 NOME:        SET_TESTO_ALLEGATO;
 DESCRIZIONE: Modifica il testo dell'allegato
 PARAMETRI:
                      P_TIPO VARCHAR2 Indica se allegato di delibera o di proposta. Valori possibili: D Delibera
                                                                                                                                            P Proposta
 RITORNA:
 ECCEZIONI:
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 1    31/10/2003  SC    Prima emissione.
******************************************************************************/
BEGIN
   if p_tipo = 'D' then
      ALLEGATO_DOCUMENTO.SET_TESTO(p_allegato_id, p_testo);
   else
      ALLEGATO_PROPOSTA.SET_TESTO(p_allegato_id, p_testo);
   end if;
EXCEPTION
WHEN OTHERS THEN
   raise;
END SET_TESTO_ALLEGATO;
Procedure MODIFICA_ALLEGATO
( P_ALLEGATO_ID IN ALLEGATI_DOCUMENTO.ALLEGATO_ID%TYPE
, P_TIPO IN VARCHAR2
, P_TIPO_ALLEGATO TIPI_ALLEGATO.TIPO_ALLEGATO%TYPE
, P_DESCRIZIONE ALLEGATI_DOCUMENTO.DESCRIZIONE%TYPE
, P_QUANTITA ALLEGATI_DOCUMENTO.QUANTITA%TYPE
, P_FILE_ALLEGATO ALLEGATI_DOCUMENTO.FILE_ALLEGATO%TYPE
, P_PAGINE ALLEGATI_DOCUMENTO.PAGINE%TYPE
, P_DOCUMENT_ID ALLEGATI_DOCUMENTO.DOCUMENT_ID%TYPE
, P_FIRMATO ALLEGATI_DOCUMENTO.FIRMATO%TYPE
, P_VERIFICA_FIRMA ALLEGATI_DOCUMENTO.VERIFICA_FIRMA%TYPE
, P_VALIDITA ALLEGATI_DOCUMENTO.VALIDITA%TYPE
, P_UTENTE AD4_UTENTI.UTENTE%TYPE
, P_DATA_MODIFICA VARCHAR2 DEFAULT NULL) IS
/******************************************************************************
 NOME:        MODIFICA_ALLEGATO
 DESCRIZIONE: Inserisce un record in ALLEGATI_DOCUMENTO
 PARAMETRI:
                      P_TIPO VARCHAR2 Indica se allegato di delibera o di proposta. Valori possibili: D Delibera
                                                                                                                                            P Proposta
 RITORNA:
 ECCEZIONI:
 ANNOTAZIONI: Gestisce le date come VARCHAR2                    Formato da utilizzare DD/MM/YYYY
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 1    31/10/2003  SC    Prima emissione.
******************************************************************************/
BEGIN
   if p_tipo = 'D' then
   ALLEGATO_DOCUMENTO.UPDATE_ALLEGATO_DOCUMENTO(p_allegato_id, p_tipo_allegato, p_descrizione, p_quantita, p_file_allegato, p_pagine
   , p_document_id, p_firmato, p_verifica_firma, p_validita, p_utente, p_data_modifica);
   else
      PROPOSTA.MODIFICA_ALLEGATO(p_allegato_id, p_tipo_allegato, p_descrizione, p_quantita, p_file_allegato, p_pagine
      , p_firmato, p_verifica_firma, p_validita, p_utente, p_data_modifica);
   end if;
EXCEPTION
WHEN OTHERS THEN
   raise;
END MODIFICA_ALLEGATO;
FUNCTION  GET_PROPOSTA
( P_ANNO              IN DELIBERE.ANNO%TYPE
, P_TIPO_REGISTRO     IN DELIBERE.TIPO_REGISTRO%TYPE
, P_NUMERO            IN DELIBERE.NUMERO%TYPE)
/******************************************************************************
 NOME:        GET_PROPOSTA
 DESCRIZIONE: Restituisce i dati della proposta associata all'atto concatenati
              in una stringa
 PARAMETRI:   P_ANNO            anno dell'atto
              P_TIPO_REGISTRO   registro dell'atto
              P_NUMERO          numero dell'atto
 RITORNA:     Stringa contenente i dati della proposta concatenati con la @
 ECCEZIONI:
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 1    21/08/2007  SN    A20940.0.0 + A22107.1.0 Creazione
******************************************************************************/
RETURN VARCHAR2 IS
d_stringa_dati VARCHAR2(32000) := '';
BEGIN
select anno_proposta||'@'||unita_proponente||'@'||numero_proposta
  into d_stringa_dati
  from delibere
 where anno = p_anno
   and tipo_registro = p_tipo_registro
   and numero = p_numero;
 return d_stringa_dati;
EXCEPTION WHEN OTHERS THEN
   raise;
END GET_PROPOSTA;
function crea(
   p_anno               NUMBER,
   p_tipo_registro      VARCHAR2,
   p_numero             NUMBER,
   p_data               VARCHAR2,
   p_oggetto            VARCHAR2,
   p_classificazione    VARCHAR2,
   p_class_dal          VARCHAR2,
   p_anno_cla           NUMBER,
   p_numero_cla         VARCHAR2,
   p_data_esec          VARCHAR2,
   p_data_pubbl         VARCHAR2,
   p_data_fine_pubbl    VARCHAR2,
   p_data_pubbl2        VARCHAR2,
   p_data_fine_pubbl2   VARCHAR2,
   p_utente             VARCHAR2,
   p_testo              BLOB,
   p_formato            VARCHAR2 default 'P',
   p_firmata            VARCHAR2 default 'Y',
   p_riservato          VARCHAR2 default null
)
/******************************************************************************
 NOME:        CREA.
 DESCRIZIONE: Crea una delibera con i dati passati legandola ad una proposta
              fissa la cui chiave viene letta in INSTALLAZIONE_PARAMETRI.
              Aggiorna la tabella REGISTRI per l'anno ed il registro dati se il
              numeroo dato è > dell'ultimo numero memorizzato.
 PARAMETRI:   P_ANNO            anno dell'atto
              P_TIPO_REGISTRO   registro dell'atto
              P_NUMERO          numero dell'atto
 RITORNA:       0 OK.
              < 0 Errore
 ECCEZIONI:
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 1.1  15/12/2009 MM     Creazione
******************************************************************************/
return number
is
   d_anno               NUMBER := p_anno;
   d_tipo_registro      VARCHAR2(100) := p_tipo_registro;
   d_numero             NUMBER := p_numero;
   d_data               date   := nvl(to_date(p_data, 'dd/mm/yyyy'), to_date(TO_CHAR (SYSDATE, 'dd/mm/yyyy'),'dd/mm/yyyy'));
   d_utente             VARCHAR2 (8)    := nvl(p_utente, 'GS4');
   d_anno_prop          NUMBER
                      := installazione_parametro.get_valore ('GDM_ANNO_PROP');
   d_unita_prop         VARCHAR2 (10)
                      := installazione_parametro.get_valore ('GDM_UNIT_PROP');
   d_num_prop           NUMBER
                       := installazione_parametro.get_valore ('GDM_NUM_PROP');
   d_numero_cla         NUMBER;
   d_sub                NUMBER;
   d_tipo_proposta      VARCHAR2(200);
   d_tipo_documento     VARCHAR2 (200);
   d_se_capi            VARCHAR2 (200);
   d_se_crc             VARCHAR2 (200);
   d_se_pref            VARCHAR2 (200);
   d_riservato          VARCHAR2 (200);
   d_unita_ris          VARCHAR2 (200);
   d_visualizza_lotus   VARCHAR2 (200) := 'Y';
   d_esec_immediata     VARCHAR2 (200);
   d_completa           VARCHAR2 (1);
   d_formato            VARCHAR2 (1) := nvl(p_formato, 'P');
   d_firmata            VARCHAR2 (1) := nvl(p_firmata, 'Y');
   d_return             NUMBER := 0;
   d_ultimo_numero number;
BEGIN
   d_riservato := p_riservato;
      select tipo_proposta, completa, esecutivita
        into d_tipo_proposta, d_completa, d_esec_immediata
        from proposte
       where anno = d_anno_prop
         and numero = d_num_prop
         and unita_proponente = d_unita_prop;

   if nvl(d_completa, 'N') = 'N' then
      d_return := -2;
   end if;

      d_tipo_documento  := nvl(tipo_proposta.GET_TIPO_DOCUMENTO(d_tipo_proposta), 'DELI');
      d_se_capi         := tipo_proposta.get_se_capi(d_tipo_proposta);
      d_se_crc          := tipo_proposta.get_se_crc(d_tipo_proposta);
      d_se_pref         := tipo_proposta.get_se_pref(d_tipo_proposta);

      if instr(p_numero_cla, '.') > 0 then
         d_numero_cla := to_number(substr(p_numero_cla, 1, instr(p_numero_cla, '.') - 1));
         d_sub := to_number(substr(p_numero_cla, instr(p_numero_cla, '.') + 1));
      else
         d_numero_cla := to_number(p_numero_cla);
         d_sub := 0;
      end if;

      select ultimo_numero
        into d_ultimo_numero
        from registri
       where anno = d_anno
         and tipo_registro = d_tipo_registro
         for update of ultimo_numero
      ;
      if d_ultimo_numero < d_numero then
         update registri
            set ultimo_numero = d_numero
              , ultima_data = d_data
          where anno = d_anno
            and tipo_registro = d_tipo_registro
         ;
      end if;

      INSERT INTO prenotazioni
                  (numero_prenotato, anno, tipo_registro, data_prenotazione,
                   prenotante, utente
                  )
           VALUES (p_numero, p_anno, p_tipo_registro, TRUNC (SYSDATE),
                   d_utente, d_utente
                  );

      insert_delibera (d_anno_prop,
                       d_unita_prop,
                       d_num_prop,
                       d_tipo_proposta,
                       d_data,
                       p_oggetto,
                       p_classificazione,
                       p_anno_cla,
                       d_numero_cla,
                       d_sub,
                       to_date(p_class_dal, 'dd/mm/yyyy'),
                       d_utente,
                       d_anno,
                       d_tipo_registro,
                       d_numero,
                       d_tipo_documento,
                       d_se_capi,
                       d_se_crc,
                       d_se_pref,
                       d_riservato,
                       d_unita_ris,
                       d_visualizza_lotus,
                       d_esec_immediata,
                       to_date(p_data_esec, 'dd/mm/yyyy'),
                       to_date(p_data_pubbl, 'dd/mm/yyyy'),
                       to_date(p_data_fine_pubbl, 'dd/mm/yyyy'),
                       to_date(p_data_pubbl2, 'dd/mm/yyyy'),
                       to_date(p_data_fine_pubbl2, 'dd/mm/yyyy')
                      );

   begin
      delibere_testo.INSERT_DELIBERE_TESTO(d_anno, d_tipo_registro, d_numero, d_formato);
   exception
      when others then
         if sqlcode <> -1 then
            raise;
         end if;
   end;

   if p_testo is not null and dbms_lob.GETLENGTH(p_testo) > 0 then
     delibere_testo.UPDATE_TESTO(d_anno, d_tipo_registro, d_numero, p_testo);
     delibere_testo.UPDATE_FORMATO(d_anno, d_tipo_registro, d_numero, d_formato);
     update delibere
         set tipo_trattamento = 3, firmata = d_firmata
       where anno = d_anno
         and tipo_registro = d_tipo_registro
         and numero = d_numero
     ;
   end if;
   return d_return;
END;
END DELIBERA_AGSDE2;
/
