package it.finmatica.atti.integrazioni.pitre

class PiTreField implements Serializable    {

    public static final String ATTO_SOGGETTO_CONTROLLO="ATTO_SOGGETTO_CONTROLLO"
    public static final String MACROCATEGORIA = "MACROCATEGORIA"
    public static final String MACROCATEGORIA_NO = "MACROCATEGORIA_NO"
    public static final String DATA_SEDUTA = "DATA_SEDUTA"
    public static final String NUMERO_ATTO = "NUMERO_ATTO"

    String nome
    String valori
    String tipo
    String codice
    boolean richiesto=true
    boolean valido=true

    static mapping = {
        table 		'protocollo_pitre_field'
        id			    column: 'field_id'
        valido			type: 	'yes_no'
        richiesto		type: 	'yes_no'
        piTreTemplate   column: 'template_id'
        version false
    }

    static constraints = {
        nome nullable: true
        valori nullable: true
        tipo nullable: true
        codice nullable: true
        piTreTemplate nullable: true

    }

     static belongsTo = [piTreTemplate: PiTreTemplate]

}
