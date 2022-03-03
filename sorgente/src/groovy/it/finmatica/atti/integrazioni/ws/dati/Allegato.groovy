package it.finmatica.atti.integrazioni.ws.dati

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

/**
 * Created by esasdelli on 15/05/2017.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class Allegato {

    @XmlElement(required = false, nillable = true)
    Integer id

    @XmlElement(required = false, nillable = true)
    String  idRiferimento  // id del documento dell'applicativo esterno

    @XmlElement(required = false, nillable = true)
    String titolo // il titolo dell'allegato

    @XmlElement(required = false, nillable = true)
    String tipo // il codice che identifica il tipo di allegato TipoAllegato.codiceEsterno

    @XmlElement(required = false, nillable = true)
    String titoloTipologia // il titolo della tipologia dell'allegato

    @XmlElement(required = false, nillable = true)
    List<RiferimentoFile> riferimentiFile
}

