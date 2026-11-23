package it.finmatica.atti.dizionari

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.viste.RicercaDetermina
import it.finmatica.atti.documenti.viste.RicercaDelibera
import it.finmatica.atti.documenti.viste.RicercaParere
import it.finmatica.atti.documenti.viste.RicercaVisto
import it.finmatica.atti.documenti.viste.RicercaCertificato
import it.finmatica.so4.struttura.So4Amministrazione

class StatoIterService {
    SpringSecurityService springSecurityService

    public aggiornaStatiIter() {
        So4Amministrazione ente = So4Amministrazione.get(springSecurityService.principal.amm().codice)

        //inserisco gli stati delle determine
        RicercaDetermina.createCriteria().list {
            projections {
                groupProperty("stato")
            }
            isNotNull("stato")
        }.collect {
            //inserisco solo gli stati non presenti
            StatoIter statoIter = StatoIter.findByEnteAndTipoDocumentoAndStato(ente, Determina.TIPO_OGGETTO, it.trim().toUpperCase())
            if (!statoIter) {
                new StatoIter(ente: ente, tipoDocumento: Determina.TIPO_OGGETTO, stato: it.trim().toUpperCase()).save()
            }
        }

        //inserisco gli stati delle delibere
        RicercaDelibera.createCriteria().list {
            projections {
                groupProperty("stato")
            }
            isNotNull("stato")
        }.collect {
            //inserisco solo gli stati non presenti
            StatoIter statoIter = StatoIter.findByEnteAndTipoDocumentoAndStato(ente, Delibera.TIPO_OGGETTO, it.trim().toUpperCase())
            if (!statoIter) {
                new StatoIter(ente: ente, tipoDocumento: Delibera.TIPO_OGGETTO, stato: it.trim().toUpperCase()).save()
            }
        }

        //inserisco gli stati delle proposte di delibera
        RicercaDelibera.createCriteria().list {
            projections {
                groupProperty("stato")
            }
            isNotNull("stato")
        }.collect {
            //inserisco solo gli stati non presenti
            StatoIter statoIter = StatoIter.findByEnteAndTipoDocumentoAndStato(ente, PropostaDelibera.TIPO_OGGETTO, it.trim().toUpperCase())
            if (!statoIter) {
                new StatoIter(ente: ente, tipoDocumento: PropostaDelibera.TIPO_OGGETTO, stato: it.trim().toUpperCase()).save()
            }
        }

        //inserisco gli stati dei visti
        RicercaVisto.createCriteria().list {
            projections {
                groupProperty("stato")
            }
            isNotNull("stato")
        }.collect {
            //inserisco solo gli stati non presenti
            StatoIter statoIter = StatoIter.findByEnteAndTipoDocumentoAndStato(ente, VistoParere.TIPO_OGGETTO, it.trim().toUpperCase())
            if (!statoIter) {
                new StatoIter(ente: ente, tipoDocumento: VistoParere.TIPO_OGGETTO, stato: it.trim().toUpperCase()).save()
            }
        }

        //inserisco gli stati dei pareri
        RicercaParere.createCriteria().list {
            projections {
                groupProperty("stato")
            }
            isNotNull("stato")
        }.collect {
            //inserisco solo gli stati non presenti
            StatoIter statoIter = StatoIter.findByEnteAndTipoDocumentoAndStato(ente, VistoParere.TIPO_OGGETTO_PARERE, it.trim().toUpperCase())
            if (!statoIter) {
                new StatoIter(ente: ente, tipoDocumento: VistoParere.TIPO_OGGETTO_PARERE, stato: it.trim().toUpperCase()).save()
            }
        }

        //inserisco gli stati dei certificati
        RicercaCertificato.createCriteria().list {
            projections {
                groupProperty("stato")
            }
            isNotNull("stato")
        }.collect {
            //inserisco solo gli stati non presenti
            StatoIter statoIter = StatoIter.findByEnteAndTipoDocumentoAndStato(ente, Certificato.TIPO_OGGETTO, it.trim().toUpperCase())
            if (!statoIter) {
                new StatoIter(ente: ente, tipoDocumento: Certificato.TIPO_OGGETTO, stato: it.trim().toUpperCase()).save()
            }
        }
    }
}
