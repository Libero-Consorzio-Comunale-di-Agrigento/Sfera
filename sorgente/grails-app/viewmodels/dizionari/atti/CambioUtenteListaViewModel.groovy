package dizionari.atti

import atti.ricerca.MascheraRicercaDocumento
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.*
import it.finmatica.atti.impostazioni.TipoSoggetto
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Window

class CambioUtenteListaViewModel {

	Window self

	def documentiSelezionati

	def listaTipiSoggetto
	def tipoSoggetto

	def listaSoggetti
	def soggetto

	// ricerca
	MascheraRicercaDocumento ricerca

	private final String NO_VALUE = "- -"
	def tipoOggetto
	def tipiOggetto = [(Determina.TIPO_OGGETTO)			:[popup: "/atti/popupRicercaDocumenti.zul", 		 nome: Labels.getLabel("tipoOggetto.determine")	, labelCategoria: Labels.getLabel("label.categoria.determina")			, zul:"/atti/documenti/determina.zul", 			icona:"/images/agsde2/22x22/logo_determina_22.png"         		]
        			, (PropostaDelibera.TIPO_OGGETTO)	:[popup: "/atti/popupRicercaDocumenti.zul", 		 nome: Labels.getLabel("tipoOggetto.delibere")	, labelCategoria: Labels.getLabel("label.categoria.propostaDelibera")	, zul:"/atti/documenti/propostaDelibera.zul", 	icona:"/images/agsde2/22x22/logo_proposta_delibera_22.png" 	]
					, (VistoParere.TIPO_OGGETTO)		:[popup: "/atti/popupRicercaDocumentiCollegati.zul", nome: Labels.getLabel("tipoOggetto.visti")		, labelCategoria: Labels.getLabel("label.categoria.determina")			, zul:"/atti/documenti/visto.zul", 				icona:"/images/agsde2/22x22/logo_visto_22.png"             	]
	 			    , (VistoParere.TIPO_OGGETTO_PARERE)	:[popup: "/atti/popupRicercaDocumentiCollegati.zul", nome: Labels.getLabel("tipoOggetto.pareri")	, labelCategoria: Labels.getLabel("label.categoria.propostaDelibera")	, zul:"/atti/documenti/parere.zul", 			icona:"/images/agsde2/22x22/logo_parere_22.png"            	]
					, (Certificato.TIPO_OGGETTO)		:[popup: "/atti/popupRicercaDocumentiCollegati.zul", nome: Labels.getLabel("tipoOggetto.certificati"),labelCategoria: ""													, zul:"/atti/documenti/certificato.zul"	, 		icona:"/images/agsde2/22x22/logo_certificato_22.png"       	]]

	int selectedIndexTipiSoggetto 	= -1
	int selectedIndexSoggetti 		= -1

     @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		onCambiaTipo()
		BindUtils.postNotifyChange(null, null, this, "ricerca")
	}

	@NotifyChange(["ricerca", "listaTipiSoggetto", "tipoSoggetto", "selectedIndexTipiSoggetto", "listaSoggetti", "soggetto", "selectedIndexSoggetti"])
	@Command onCambiaTipo() {
		String tipoDocumento 	= ricerca?.tipoDocumento?:Determina.TIPO_OGGETTO
		ricerca 				= new MascheraRicercaDocumento(tipoDocumento:tipoDocumento)
		ricerca.registroAtto  	= null

		tipoOggetto = tipiOggetto[tipoDocumento]
		ricerca.caricaListe()
		onCerca()
		caricaListaTipiSoggetto()
		caricaListaSoggetti()
	}

	@NotifyChange("ricerca")
	@Command onRefresh() {
		ricerca.ricerca (null)
	}

	@NotifyChange("ricerca")
	@Command onPagina() {
		ricerca.pagina (null)
	}

	@NotifyChange("ricerca")
	@Command onCerca() {
		String tipoDocumento 	= ricerca?.tipoDocumento?:Determina.TIPO_OGGETTO

		ricerca.filtroAggiuntivo = {
			createAlias('step', 		'step', 	CriteriaSpecification.INNER_JOIN)
			createAlias('step.attori', 	'attori', 	CriteriaSpecification.INNER_JOIN)

			isNull("step.dataFine")
			isNotNull("step.id")

			if (tipoDocumento == Determina.TIPO_OGGETTO || tipoDocumento == PropostaDelibera.TIPO_OGGETTO) {
				if (tipoSoggetto?.codice == "UTENTE_IN_CARICO") {
					eq ("attori.utenteAd4",  		soggetto)
				} else {
					eq ("utenteSoggetto.id",  		soggetto?.id)
					eqProperty ("attori.utenteAd4", "utenteSoggetto")
					eq ("tipoSoggetto", 			tipoSoggetto?.codice)
				}
			} else {
				if (tipoSoggetto?.codice == "UTENTE_IN_CARICO") {
					eq ("attori.utenteAd4",  		soggetto)
				} else {
					eq ("utenteFirmatario.id",  		soggetto?.id)
					eqProperty("attori.utenteAd4",  "utenteFirmatario")
				}
			}
		}
		ricerca.ricerca (null)
	}

	private void caricaListaTipiSoggetto () {
		if (ricerca.tipoDocumento == Determina.TIPO_OGGETTO) {
			listaTipiSoggetto = DeterminaSoggetto.createCriteria().list() {
				projections {
					distinct("tipoSoggetto")
				}
				tipoSoggetto {
					eq("categoria", TipoSoggetto.CATEGORIA_COMPONENTE)
				}
			}
		} else if (ricerca.tipoDocumento == PropostaDelibera.TIPO_OGGETTO) {
			listaTipiSoggetto =new ArrayList<Object>()
			def tipiSoggDelibera= DeliberaSoggetto.createCriteria().list() {
				projections {
					distinct("tipoSoggetto")
				}
				tipoSoggetto {
					eq("categoria", TipoSoggetto.CATEGORIA_COMPONENTE)
				}
			}
			def tipiSoggPropDelibera = PropostaDeliberaSoggetto.createCriteria().list() {
				projections {
					distinct("tipoSoggetto")
				}
				tipoSoggetto {
					eq("categoria", TipoSoggetto.CATEGORIA_COMPONENTE)
				}
			}

			for (i in tipiSoggDelibera) {
				listaTipiSoggetto.add(i)
			}

			for (i in tipiSoggPropDelibera) {
				if (!listaTipiSoggetto.contains(i)) {
					listaTipiSoggetto.add(i)
				}
			}
		} else {
			//se invece è un visto, un parere o un codice:
			listaTipiSoggetto = new ArrayList()	
			listaTipiSoggetto.add(new TipoSoggetto(codice:TipoSoggetto.FIRMATARIO, titolo: "Firmatario"))
		}
		if (listaTipiSoggetto == null){
			listaTipiSoggetto = new ArrayList()
		}
		//listaTipiSoggetto.add(0, new TipoSoggetto(codice:"UTENTE_IN_CARICO", titolo: "Utente in Carico"))
		if (listaTipiSoggetto.size() > 0) {
			tipoSoggetto = listaTipiSoggetto[0]
			selectedIndexTipiSoggetto = 0
		}
		else {
			tipoSoggetto = null;
			selectedIndexTipiSoggetto = -1
		}
		
	}

	private void caricaListaSoggetti () {
		listaSoggetti = []
		if (ricerca.tipoDocumento == Determina.TIPO_OGGETTO) {
			listaSoggetti = Determina.createCriteria().list() {
				createAlias('soggetti', 'ds', CriteriaSpecification.INNER_JOIN)
  				createAlias('iter', 'it', CriteriaSpecification.INNER_JOIN)
  				createAlias('iter.stepCorrente', 'step', CriteriaSpecification.INNER_JOIN)
  				createAlias('iter.stepCorrente.attori', 'attori', CriteriaSpecification.INNER_JOIN)
  				projections {
                    distinct("attori.utenteAd4")
                }
				eq ("valido", true)
				isNull("step.dataFine")
				
				if ("UTENTE_IN_CARICO" == tipoSoggetto.codice) {
					isNotNull("attori.utenteAd4")
				} else {
					eqProperty("attori.utenteAd4","ds.utenteAd4")
					eq("ds.tipoSoggetto.codice", tipoSoggetto.codice)
				}
			}.toDTO()

		} else if (ricerca.tipoDocumento == PropostaDelibera.TIPO_OGGETTO) {
			listaSoggetti = new ArrayList<Object>()
			def delibereSoggetti = Delibera.createCriteria().list() {
				createAlias('soggetti', 'ds', CriteriaSpecification.INNER_JOIN)
  				createAlias('iter', 'it', CriteriaSpecification.INNER_JOIN)
  				createAlias('iter.stepCorrente', 'step', CriteriaSpecification.INNER_JOIN)
  				createAlias('iter.stepCorrente.attori', 'attori', CriteriaSpecification.INNER_JOIN)
  				projections {
                    distinct("ds.utenteAd4")
                }
				eq ("valido", true)
				isNull("step.dataFine")
                
				if ("UTENTE_IN_CARICO" == tipoSoggetto.codice) {
					isNotNull("attori.utenteAd4")
				} else {
					eqProperty("attori.utenteAd4","ds.utenteAd4")
					eq("ds.tipoSoggetto.codice", tipoSoggetto.codice)
				}
			}?.toDTO()

			def propDelibereSoggetti = PropostaDelibera.createCriteria().list() {
				createAlias('soggetti', 'ds', CriteriaSpecification.INNER_JOIN)
				createAlias('iter', 'it', CriteriaSpecification.INNER_JOIN)
				createAlias('iter.stepCorrente', 'step', CriteriaSpecification.INNER_JOIN)
				createAlias('iter.stepCorrente.attori', 'attori', CriteriaSpecification.INNER_JOIN)
				projections {
					distinct("ds.utenteAd4")
				}

				isNull("step.dataFine")
				eq ("valido", true)
				
				if ("UTENTE_IN_CARICO" == tipoSoggetto.codice) {
					isNotNull("attori.utenteAd4")
				} else {
					eqProperty("attori.utenteAd4","ds.utenteAd4")
					eq("ds.tipoSoggetto.codice", tipoSoggetto.codice)
				}
			}?.toDTO()

			for(i in propDelibereSoggetti) {
				listaSoggetti.add(i)
			}
			for(i in delibereSoggetti) {
				listaSoggetti.add(i)
			}

		} else if (ricerca.tipoDocumento == VistoParere.TIPO_OGGETTO) {
			listaSoggetti = VistoParere.createCriteria().list() {
				createAlias('iter', 'it', CriteriaSpecification.INNER_JOIN)
  				createAlias('iter.stepCorrente', 'step', CriteriaSpecification.INNER_JOIN)
  				createAlias('iter.stepCorrente.attori', 'attori', CriteriaSpecification.INNER_JOIN)
  				projections {
					distinct("firmatario")
				}
				isNull("step.dataFine")
				eq ("valido", true)
				isNotNull("determina")
				
				if ("UTENTE_IN_CARICO" == tipoSoggetto.codice) {
					isNotNull("attori.utenteAd4")
				} else {
					eqProperty("attori.utenteAd4","firmatario")
				}
			}.toDTO()

		} else if (ricerca.tipoDocumento == VistoParere.TIPO_OGGETTO_PARERE) {
			listaSoggetti = VistoParere.createCriteria().list() {
				createAlias('iter', 'it', CriteriaSpecification.INNER_JOIN)
  				createAlias('iter.stepCorrente', 'step', CriteriaSpecification.INNER_JOIN)
  				createAlias('iter.stepCorrente.attori', 'attori', CriteriaSpecification.INNER_JOIN)
  				projections {
					distinct("firmatario")
				}
				isNull("step.dataFine")
				eq ("valido", true)
				isNotNull("propostaDelibera")
                
				if ("UTENTE_IN_CARICO" == tipoSoggetto.codice) {
					isNotNull("attori.utenteAd4")
				} else {
					eqProperty("attori.utenteAd4","firmatario")
				}
			}.toDTO()

		} else if (ricerca.tipoDocumento == Certificato.TIPO_OGGETTO) {
			listaSoggetti = Certificato.createCriteria().list() {
				createAlias('iter', 'it', CriteriaSpecification.INNER_JOIN)
  				createAlias('iter.stepCorrente', 'step', CriteriaSpecification.INNER_JOIN)
  				createAlias('iter.stepCorrente.attori', 'attori', CriteriaSpecification.INNER_JOIN)
  				projections {
					distinct("firmatario")
				}
				isNull("step.dataFine")
				eq ("valido", true)
				
                if ("UTENTE_IN_CARICO" == tipoSoggetto.codice) {
					isNotNull("attori.utenteAd4")
				} else {
					eqProperty("attori.utenteAd4","firmatario")
				}
			}.toDTO()
		}

		listaSoggetti.sort { it.nominativoSoggetto }
		listaSoggetti.add(0, (new Ad4Utente(id:"", nominativo:"")).toDTO())
		soggetto = listaSoggetti[0]
		selectedIndexSoggetti = 0
	}

	@NotifyChange(["ricerca", "tipoSoggetto"])
	@Command onCambiaTipoOggetto() {
		// se sono in init, "ricerca" è null quindi imposto come default la ricerca su Determina.
		// altrimenti, l'utente ha cambiato il tipo documento da interfaccia, quindi me lo segno e reinizializzo la ricerca con quei valori.
		String tipoDocumento 	= ricerca?.tipoDocumento?:Determina.TIPO_OGGETTO;
		ricerca 				= new MascheraRicercaDocumento(tipoDocumento:tipoDocumento)
		ricerca.caricaListe()
		tipoSoggetto = null
	}

	@NotifyChange(["ricerca", "listaSoggetti", "soggetto", "selectedIndexSoggetti"])
	@Command onCambiaTipoSoggetto() {
		caricaListaSoggetti ()
	}

	@NotifyChange("ricerca")
	@Command onCambiaSoggetto() {
		onCerca()
	}

	@Command onApriDocumento (@BindingParam("documento") def documento){
		Window w = Executions.createComponents(tipoOggetto.zul, self, [id: documento.idDocumento, idPadre: -1])
		w.doModal()
	}

	@Command onModificaUtente () {
		if (!documentiSelezionati.isEmpty()) {
			Window w = Executions.createComponents ("/dizionari/impostazioni/cambioUtenteDettaglio.zul"
												  , self
												  , [listaOggetti:documentiSelezionati, tipoDoc:ricerca.tipoDocumento, tipoSoggetto:tipoSoggetto, utentePrecedente:soggetto])
			w.onClose {
				onCerca()
				caricaListaSoggetti ()
				documentiSelezionati = null
				BindUtils.postNotifyChange(null, null, this, "ricerca")
				BindUtils.postNotifyChange(null, null, this, "documentiSelezionati")
				BindUtils.postNotifyChange(null, null, this, "ricerca")
			}
			w.doModal()
		}
	}

}
