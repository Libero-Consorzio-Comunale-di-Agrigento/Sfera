package it.finmatica.atti.integrazioni.ws.dati

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class CampiRicercaEstesa  extends CampiRicerca{



    @XmlElement(required = false, nillable = true)
    Long    numeroAttoDal

    @XmlElement(required = false, nillable = true)
    Long   numeroAttoAl

    @XmlElement(required = false, nillable = true)
    Date dataAttoDal

    @XmlElement(required = false, nillable = true)
    Date dataAttoAl

    @XmlElement(required = false, nillable = true)
    Date dataPubblicazioneDal

    @XmlElement(required = false, nillable = true)
    Date dataPubblicazioneAl

    @XmlElement(required = false, nillable = true)
    Long annoProposta

    @XmlElement(required = false, nillable = true)
    Long   numeroProposta

    @XmlElement(required = false, nillable = true)
    Long   numeroPropostaDal

    @XmlElement(required = false, nillable = true)
    Long   numeroPropostaAl

    @XmlElement(required = false, nillable = true)
    Date   dataEsecutivitaDal

    @XmlElement(required = false, nillable = true)
    Date   dataEsecutivitaAl

    @XmlElement(required = false, nillable = true)
    String codiceUnitaProponente

    @XmlElement(required = false, nillable = true)
    String codiceTipologiaAtto

    @XmlElement(required = true, nillable = false)
    String  tipoPubblicazioneAlbo


    @XmlElement(required = false, nillable = true)
    Long annoProtocollo

    @XmlElement(required = false, nillable = true)
    Long   numeroProtocollo

    @XmlElement(required = false, nillable = true)
    Long   numeroProtocolloDal

    @XmlElement(required = false, nillable = true)
    Long   numeroProtocolloAl

    @XmlElement(required = true, nillable = false)
    String  codiceClassifica

    @XmlElement(required = true, nillable = false)
    String  numeroFascicolo

    @XmlElement(required = true, nillable = false)
    Integer  annoFascicolo

    @XmlElement(required = true, nillable = false)
    String  categoria

    @XmlElement(required = false, nillable = true)
    Integer     inEvidenza = 0   // 0: tutti, 1: in Evidenza, 2: non in Evidenza

    @XmlElement(required = false, nillable = true)
    Date 	    dataInEvidenzaDa

    @XmlElement(required = false, nillable = true)
    Date 	    dataInEvidenzaA

}