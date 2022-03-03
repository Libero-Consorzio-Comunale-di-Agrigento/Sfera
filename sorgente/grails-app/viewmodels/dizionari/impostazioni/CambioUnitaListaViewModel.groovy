package dizionari.impostazioni

import it.finmatica.atti.documenti.viste.RicercaUnitaDocumentoAttivo
import it.finmatica.atti.dto.documenti.viste.RicercaUnitaDocumentoAttivoDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Window

class CambioUnitaListaViewModel {

	// Componenti
	Window self

	// Dati
	List<RicercaUnitaDocumentoAttivoDTO> documentiSelezionati
	List<RicercaUnitaDocumentoAttivoDTO> listaDocumenti

	// Lista delle unità chiuse di documenti ancora attivi
	List<So4UnitaPubbDTO> 	listaSoggetti

	// unità chiusa scelta
	So4UnitaPubbDTO soggetto

	int selectedIndexTipiSoggetto 	= -1
	int selectedIndexSoggetti 		= -1
	def tipoOggetto
	List tipiOggetto = [
		[codice: 'DETERMINA'  , nome: "Determine", 	 zul: '/atti/documenti/determina.zul', 			icona: "/images/agsde2/22x22/logo_determina_22.png"]
	  ,	[codice: 'DELIBERA'	  , nome: "Delibere", 	 zul: '/atti/documenti/propostaDelibera.zul',  	icona: "/images/agsde2/22x22/logo_delibera_22.png"]
	  , [codice: 'VISTO'	  , nome: "Visti", 		 zul: '/atti/documenti/visto.zul',  			icona: "/images/agsde2/22x22/logo_visto_22.png"]
	  , [codice: 'PARERE'	  , nome: "Pareri", 	 zul: '/atti/documenti/parere.zul',  			icona: "/images/agsde2/22x22/logo_parere_22.png"]
	  //, [codice: 'CERTIFICATO', nome: "Certificati", zul: '/atti/documenti/certificato.zul',		icona: "/images/agsde2/22x22/logo_certificato_22.png"]
        ]

    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
    }

	private void caricaListaSoggetti() {

        listaSoggetti = (RicercaUnitaDocumentoAttivo.createCriteria().list {
            projections {
                groupProperty ("unitaAttore")
            }
            unitaAttore {
                lt("al", new Date().clearTime())
                eq("ottica.codice", Impostazioni.OTTICA_SO4.valore.toString())
            }
        } + RicercaUnitaDocumentoAttivo.createCriteria().list {
            projections {
                groupProperty ("unitaSoggetto")
            }
            unitaSoggetto {
                lt("al", new Date().clearTime())
                eq("ottica.codice", Impostazioni.OTTICA_SO4.valore.toString())
            }
        }).inject ([]) { result, item ->
            item.each { unita ->
                if (unita == null) {
                    return
                }

                if (unita.al != null && unita.al < new Date().clearTime()) {
                    result << unita
                }
            }

            return result
        }.unique().toDTO()
	}

	@Command onCambiaSoggetto () {
		caricaListaDocumenti()
        BindUtils.postNotifyChange(null, null, this, "listaDocumenti")
	}

	@Command onCambiaTipoOggetto () {
		self.invalidate()
		caricaListaSoggetti()
        listaDocumenti = []
        soggetto = null
        selectedIndexSoggetti 		= -1
        BindUtils.postNotifyChange(null, null, this, "listaDocumenti")
        BindUtils.postNotifyChange(null, null, this, "soggetto")
        BindUtils.postNotifyChange(null, null, this, "listaSoggetti")
	}

	private void caricaListaDocumenti () {
        listaDocumenti = RicercaUnitaDocumentoAttivo.createCriteria().list {
            projections {
                groupProperty ("tipoDocumento")     // 0
                groupProperty ("idDocumento")       // 1
                groupProperty ("idDocumentoPadre")  // 2
                groupProperty ("idAtto")            // 3
                groupProperty ("idProposta")        // 4
                groupProperty ("iter")              // 5
                groupProperty ("annoProposta")      // 6
                groupProperty ("numeroProposta")    // 7
                groupProperty ("registroProposta")  // 8
                groupProperty ("annoAtto")          // 9
                groupProperty ("numeroAtto")        // 10
                groupProperty ("registroAtto")      // 11
                groupProperty ("oggetto")           // 12
                groupProperty ("stato")             // 13
            }

            or {
                eq ("unitaSoggetto", soggetto.domainObject)
                eq ("unitaAttore", soggetto.domainObject)
            }

            if(tipoOggetto != null && tipoOggetto.codice.equals("DELIBERA")) {
                or {
                    eq("tipoDocumento", "DELIBERA")
                    eq("tipoDocumento", "PROPOSTA_DELIBERA")
                }
            }
            else if(tipoOggetto != null) {
                eq("tipoDocumento", tipoOggetto.codice)
            }

            //order ("tipoDocumento", 	"asc")
            //order ("registroAtto",	    "asc")
            order ("annoAtto", 		    "desc")
            order ("numeroAtto", 	    "desc")
            //order ("registroProposta",  "asc")
            order ("annoProposta",	    "desc")
            order ("numeroProposta",	"desc")
        }.collect { row ->
            new RicercaUnitaDocumentoAttivoDTO (
                    tipoDocumento:     row[0],
                    idDocumento:       row[1],
                    idDocumentoPadre:  row[2],
                    idAtto:            row[3],
                    idProposta:        row[4],
                    iter:              row[5]?.toDTO(),
                    annoProposta:      row[6],
                    numeroProposta:    row[7],
                    registroProposta:  row[8]?.toDTO(),
                    annoAtto:          row[9],
                    numeroAtto:        row[10],
                    registroAtto:      row[11]?.toDTO(),
                    oggetto:           row[12],
                    stato:             row[13])
        }

		BindUtils.postNotifyChange(null, null, this, "listaDocumenti")
	}

	@Command onApriDocumento (@BindingParam("documento") RicercaUnitaDocumentoAttivoDTO documento) {
		Window w = Executions.createComponents (tipoOggetto.zul, self, [id: documento.idDocumento, idPadre: documento.idDocumentoPadre])
		w.doModal()
		w.onClose {
			caricaListaDocumenti()
		}
	}

	@Command onModificaUnita () {
		if (!documentiSelezionati.isEmpty()) {
			Window w = Executions.createComponents ("/dizionari/impostazioni/cambioUnitaDettaglio.zul", self, [listaDocumenti:documentiSelezionati, unitaSo4Vecchia:soggetto])
			w.onClose {
				caricaListaDocumenti()
				BindUtils.postNotifyChange(null, null, this, "listaDocumenti")
			}
			w.doModal()
		}
	}
}
