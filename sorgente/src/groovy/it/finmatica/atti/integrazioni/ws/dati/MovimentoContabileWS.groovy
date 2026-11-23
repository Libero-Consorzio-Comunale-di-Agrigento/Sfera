package it.finmatica.atti.integrazioni.ws.dati

import javax.xml.bind.annotation.*

@XmlRootElement(name = "movimentoContabile")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class MovimentoContabileWS{

    @XmlElement(required = false, nillable = true)
    Integer annoEsercizio

    @XmlElement(required = false, nillable = true)
    String conto

    @XmlElement(required = false, nillable = true)
    String contoDescrizione

    @XmlElement(required = false, nillable = true)
    String bilancio

    @XmlElement(required = false, nillable = true)
    String bilancioDescrizione

    @XmlElement(required = false, nillable = true)
    String sottoBudget

    @XmlElement(required = false, nillable = true)
    String sottoBudgetDescrizione

    @XmlElement(required = false, nillable = true)
    String note

    @XmlElement(required = false, nillable = true)
    BigDecimal importoIniziale

    @XmlElement(required = false, nillable = true)
    BigDecimal importoVariazione

    @XmlElement(required = false, nillable = true)
    BigDecimal importoResiduo

    @XmlElement(required = false, nillable = true)
    BigDecimal importoProvvedimento

}
