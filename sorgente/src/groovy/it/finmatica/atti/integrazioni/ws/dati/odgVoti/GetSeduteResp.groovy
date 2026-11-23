package it.finmatica.atti.integrazioni.ws.dati.odgVoti

import it.finmatica.atti.integrazioni.ws.dati.odgVoti.SedutaPropostaRiferimento

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class GetSeduteResp {
    int             totaleSedute
    List<SedutaPropostaRiferimento> sedute
}