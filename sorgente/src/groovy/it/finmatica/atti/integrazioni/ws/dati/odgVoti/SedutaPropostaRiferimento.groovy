package it.finmatica.atti.integrazioni.ws.dati.odgVoti

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

//Utilizzata da getSedute più snella senza Oggetto

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class SedutaPropostaRiferimento {

        @XmlElement(required = false, nillable = true)
        Long idSeduta

        //@XmlElement(required = false, nillable = true)
        //String idRiferimento  // id del documento dell'applicativo esterno

        @XmlElement(required = false, nillable = true)
        String commissione

        String dataSeduta

        String oraSeduta

        @XmlElement(required = false, nillable = true)
        Integer	anno

        @XmlElement(required = false, nillable = true)
        Integer numero

}
