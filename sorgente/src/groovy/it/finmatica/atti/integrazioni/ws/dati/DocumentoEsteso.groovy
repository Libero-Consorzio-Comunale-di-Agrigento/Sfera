package it.finmatica.atti.integrazioni.ws.dati

import javax.xml.bind.annotation.*

@XmlRootElement(name = "documento")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class DocumentoEsteso  extends Documento{

    @XmlElement(required = false, nillable = true)
    String tipologiaPubblicazione

    @XmlElement(required = false, nillable = true)
    String titoloTipologia

    @XmlElement(required = false, nillable = true)
    String nominativoDirigente

    @XmlElement(required = false, nillable = true)
    String uoProponenteDescrizione

    @XmlElement(required = false, nillable = true)
    String cig

    @XmlElement(required = false, nillable = true)
    String cup

    @XmlElement(required = false, nillable = true)
    boolean daInviareCorteConti

    @XmlElement(required = false, nillable = true)
    Date dataInvioCorteConti

    @XmlElement(required = false, nillable = true)
    String note

    @XmlElement(required = false, nillable = true)
    boolean eseguibilitaImmediata

    @XmlElement(required = false, nillable = true)
    String motivazioniEseguibilita

    @XmlElement(required = false, nillable = true)
    Date 	dataScadenza

    @XmlElement(required = false, nillable = true)
    Integer priorita

    @XmlElement(required = false, nillable = true)
    String  motivazione

    @XmlElement(required = false, nillable = true)
    String  codiceCategoria

    //ATS PAVIA
    @XmlElement(required = false, nillable = true)
    boolean     inEvidenza

    @XmlElement(required = false, nillable = true)
    Date 	    dataInEvidenzaDa

    @XmlElement(required = false, nillable = true)
    Date 	    dataInEvidenzaA

    @XmlElement(required = false, nillable = true)
    BigDecimal  importo


    List<MovimentoContabileWS> movimentiContabili



}
