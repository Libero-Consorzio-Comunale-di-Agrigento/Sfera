package it.finmatica.atti.dto.documenti.viste

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.viste.So4Delega

/**
 * Created by czappavigna on 28/11/2017.
 */
class So4DelegaService {

    def grailsApplication

    public List<Ad4Utente> getDeleganti(Ad4Utente utente) {
        return So4Delega.createCriteria().list {
            projections {
                distinct "deleganteUtente"
            }
            or {
                isNull("al")
                ge ("al", new Date())
            }
            or {
                isNull("dal")
                le("dal", new Date())
            }

            or {
                isNull("istanzaApplicativo")
                eq ("istanzaApplicativo", grailsApplication.config.grails.plugins.amministrazionedatabase.istanza)
            }
            or {
                isNull("moduloApplicativo")
                eq ("moduloApplicativo", grailsApplication.config.grails.plugins.amministrazionedatabase.modulo)
            }

            eq ("delegatoUtente", utente)
        }
    }

    public List<So4Delega> getDeleghe(Ad4Utente delegato, Ad4Utente delegante = null) {
        return So4Delega.createCriteria().list {
            or {
                isNull("al")
                ge ("al", new Date())
            }
            or {
                isNull("dal")
                le("dal", new Date())
            }
            if (delegante != null){
                eq ("deleganteUtente", delegante)
            }
            or {
                isNull("istanzaApplicativo")
                eq ("istanzaApplicativo", grailsApplication.config.grails.plugins.amministrazionedatabase.istanza)
            }
            or {
                isNull("moduloApplicativo")
                eq ("moduloApplicativo", grailsApplication.config.grails.plugins.amministrazionedatabase.modulo)
            }
            eq ("delegatoUtente", delegato)
        }
    }

    public boolean hasDelega(Ad4Utente delegato, Ad4Utente delegante) {
        return getDeleghe(delegato, delegante) != null;
    }

}
