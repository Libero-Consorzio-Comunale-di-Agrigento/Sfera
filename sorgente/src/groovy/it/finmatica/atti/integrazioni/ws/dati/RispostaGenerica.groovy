package it.finmatica.atti.integrazioni.ws.dati

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class RispostaGenerica {

    @XmlElement(required = false, nillable = true)
    String esito // OK o KO se OK codiceErrore=0 e errore è nullo

    @XmlElement(required = false, nillable = true)
    Integer codiceErrore  // codice dell'errore 0 se tutto ok

    @XmlElement(required = false, nillable = true)
    String errore // messaggio errore

    RispostaGenerica(){

    }

    RispostaGenerica(String esito,Integer codiceErrore, String errore){
        this.esito=esito
        this.codiceErrore=codiceErrore
        this.errore=errore
    }



}
