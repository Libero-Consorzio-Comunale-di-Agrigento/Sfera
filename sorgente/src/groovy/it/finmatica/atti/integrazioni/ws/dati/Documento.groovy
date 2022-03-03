package it.finmatica.atti.integrazioni.ws.dati

import javax.xml.bind.annotation.*

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class Documento {

    @XmlElement(required = true, nillable = false)
    String tipo // DETERMINA o DELIBERA

    @XmlElement(required = false, nillable = true)
    Long id
    @XmlElement(required = false, nillable = true)
    String idRiferimento  // id del documento dell'applicativo esterno

    @XmlElement(required = false, nillable = true)
    String oggetto
    @XmlElement(required = false, nillable = true)
    String stato
    @XmlElement(required = false, nillable = true)
    String codiceTipologia

    @XmlElement(required = false, nillable = true)
    Integer numero
    @XmlElement(required = false, nillable = true)
    Integer anno
    @XmlElement(required = false, nillable = true)
    Date data
    @XmlElement(required = false, nillable = true)
    String codiceRegistro

    @XmlElement(required = false, nillable = true)
    Integer numero2
    @XmlElement(required = false, nillable = true)
    Integer anno2
    @XmlElement(required = false, nillable = true)
    Date dataNumero2
    @XmlElement(required = false, nillable = true)
    String codiceRegistro2

    @XmlElement(required = false, nillable = true)
    Integer numeroProtocollo
    @XmlElement(required = false, nillable = true)
    Integer annoProtocollo
    @XmlElement(required = false, nillable = true)
    Date dataProtocollo

    // dati di classificazione
    @XmlElement(required = false, nillable = true)
    Fascicolo fascicolo
    @XmlElement(required = false, nillable = true)
    Classificazione classificazione

    @XmlElement(required = false, nillable = true)
    Integer numeroProposta
    @XmlElement(required = false, nillable = true)
    Integer annoProposta
    @XmlElement(required = false, nillable = true)
    Date dataProposta

    @XmlElement(required = false, nillable = true)
    Date dataEsecutivita
    @XmlElement(required = false, nillable = true)
    Date dataInizioPubblicazione
    @XmlElement(required = false, nillable = true)
    Date dataFinePubblicazione

    @XmlElement(required = false, nillable = true)
    Integer numeroAlbo
    @XmlElement(required = false, nillable = true)
    Integer annoAlbo

    @XmlElement(required = false, nillable = true)
    Soggetto redattore
    @XmlElement(required = false, nillable = true)
    Soggetto funzionario
    @XmlElement(required = false, nillable = true)
    Soggetto dirigente
    @XmlElement(required = false, nillable = true)
    UnitaOrganizzativa unitaProponente

    @XmlElement(required = false, nillable = true)
    RiferimentoFile testo

    @XmlElement(required = false, nillable = true)
    List<Allegato> allegati

    @XmlElement(required = false, nillable = true)
    List<VistoParere> vistiPareri

    @XmlElement(required = false, nillable = true)
    List<Certificato> certificati

    @XmlElement(required = false, nillable = true)
    List<Soggetto> soggetti
}
