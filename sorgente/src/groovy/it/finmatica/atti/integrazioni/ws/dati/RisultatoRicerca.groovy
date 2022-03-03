package it.finmatica.atti.integrazioni.ws.dati

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class RisultatoRicerca {
    int             totaleDocumenti
    List<Documento> documenti
}