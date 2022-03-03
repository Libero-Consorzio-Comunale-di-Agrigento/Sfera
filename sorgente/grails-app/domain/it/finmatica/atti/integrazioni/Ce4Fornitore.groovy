package it.finmatica.atti.integrazioni

class Ce4Fornitore implements Serializable {

    String contoFornitore
    String ragioneSociale
    String tipoConto
    String partitaIva
    String codiceFiscale

    static mapping = {
        table 'ce4_fornitori'
        id column: 'conto', generator: 'assigned'
        contoFornitore column: 'conto_fornitore'
        ragioneSociale column: 'ragione_sociale'
        tipoConto column: 'tipo_conto'
        partitaIva column: 'partita_iva'
        codiceFiscale column: 'codice_fiscale'
        version false
    }

    static constraints = {
        contoFornitore nullable: true
        ragioneSociale nullable: true
        tipoConto nullable: true
        partitaIva nullable: true
        codiceFiscale nullable: true
    }

}
