package it.finmatica.atti.integrazioni.ws.dati

import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

import javax.xml.bind.annotation.*

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
class UnitaOrganizzativa {
    @XmlElement(required = false, nillable = true)
    Long   progressivo

    @XmlElement(required = false, nillable = true)
    String codiceOttica

    @XmlElement(required = false, nillable = true)
    Date   dal

    @XmlElement(required = false, nillable = true)
    String codice

    @XmlElement(required = false, nillable = true)
    String descrizione

    @XmlTransient
    So4UnitaPubb getUnitaPubb () {
        if (codiceOttica == null)
            codiceOttica = Impostazioni.OTTICA_SO4.getValore()
        if (codiceOttica) {
            if (dal == null)
                dal = new Date()
            if (progressivo > 0) {
                return So4UnitaPubb.perOttica(codiceOttica).findByProgrAndDal(progressivo, dal)
            } else if (codice?.trim()?.length() > 0) {
                return So4UnitaPubb.allaData(dal).perOttica(codiceOttica).findByCodice(codice)
            }
        }

        return null
    }
}
