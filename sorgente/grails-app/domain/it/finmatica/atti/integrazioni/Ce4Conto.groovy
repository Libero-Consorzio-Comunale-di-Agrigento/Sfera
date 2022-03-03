package it.finmatica.atti.integrazioni


class Ce4Conto implements Serializable {

    //String conto
    String contoEsteso
    String descrizione
    String tipoConto

    static mapping = {
        table 'ce4_conti'
        id column: 'conto', generator: 'assigned'
        //conto column: 'conto'
        contoEsteso column: 'conto_esteso'
        descrizione column: 'descrizione'
        tipoConto column: 'tipo_conto'
        version false
    }

    static constraints = {
        //conto nullable: true
        contoEsteso nullable: true
        descrizione nullable: true
        tipoConto nullable: true
    }

}
