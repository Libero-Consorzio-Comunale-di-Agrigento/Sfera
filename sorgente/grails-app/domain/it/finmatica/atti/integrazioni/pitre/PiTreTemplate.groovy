package it.finmatica.atti.integrazioni.pitre

class PiTreTemplate implements  Serializable{

    String nome
    String codice
    String modello
    boolean protocolla = true
    boolean valido = true

    static mapping = {
        table 'protocollo_pitre_template'
        id column: 'template_id'

        codice sqlType: 'Clob'
        modello sqlType: 'Clob'
        valido type: 'yes_no'
        protocolla type: 'yes_no'
        version false
    }

    static constraints = {
        nome    nullable: true
        codice    nullable: true
        modello   nullable: true
    }

    static hasMany = [fields : PiTreField]
}


/*

CREATE TABLE PROTOCOLLO_PITRE_TEMPLATE
(
  TEMPLATE_ID  NUMBER                           NOT NULL,
  NOME         VARCHAR2(255 BYTE),
  CODICE       CLOB,
  MODELLO      CLOB,
  VALIDO       CHAR(1 BYTE),
  PROTOCOLLA   CHAR(1 BYTE)                     DEFAULT 'N'
)

ALTER TABLE AGSDE2.PROTOCOLLO_PITRE_TEMPLATE
 ADD (PROTOCOLLA  CHAR(1 BYTE)                      DEFAULT 'N');
 */