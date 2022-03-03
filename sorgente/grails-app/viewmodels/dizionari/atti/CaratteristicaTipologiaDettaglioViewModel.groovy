package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.tipologie.TipoCertificato
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.documenti.tipologie.TipoVistoParere
import it.finmatica.atti.dto.documenti.tipologie.CaratteristicaTipologiaDTOService
import it.finmatica.atti.dto.impostazioni.CaratteristicaTipoSoggettoDTO
import it.finmatica.atti.dto.impostazioni.CaratteristicaTipologiaDTO
import it.finmatica.atti.dto.impostazioni.TipoSoggettoDTO
import it.finmatica.atti.dto.impostazioni.RegolaCalcoloDTO
import it.finmatica.atti.impostazioni.*
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfTipoOggettoDTO
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class CaratteristicaTipologiaDettaglioViewModel extends AfcAbstractRecord {

	// services
	CaratteristicaTipologiaDTOService	caratteristicaTipologiaDTOService

	// componenti
	Window 		self

	// dati
	List<WkfTipoOggettoDTO> 			listaOggetti
	List<CaratteristicaTipoSoggettoDTO>	listaCarTipiSoggetto
	List<TipoSoggettoDTO>				listaTipoSoggetto
	List<Ad4RuoloDTO> 					listaRuoli
	List<RegolaCalcoloDTO>				listaRegoleComponenteDefault
	List<RegolaCalcoloDTO>				listaRegoleUnitaDefault
	List<RegolaCalcoloDTO>				listaRegoleComponenteLista
	List<RegolaCalcoloDTO>				listaRegoleUnitaLista

	// stato
	def selectedLayout
	def listaZul
	public static def listaZulPossibili = [[ label: 		"Determina Standard"
					, tipoOggetto:	Determina.TIPO_OGGETTO
					, descrizione: 	"Mostra il Proponente, Unità Proponente, Dirigente, Funzionario"
					, url:			"/atti/documenti/determina/determina_standard.zul"
					, suggerimento:	"""Mostra i soggetti:
* REDATTORE:		tipicamente è l'utente corrente. Può appartenere a più unità.
* UO_PROPONENTE:	tipicamente corrisponde all'unità del redattore.
* FUNZIONARIO:		il funzionario può essere facoltativo.
* DIRIGENTE:		il dirigente della determina è anche colui che la firma."""
					, soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.UO_PROPONENTE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE]],

				   [ label: 		"Determina con Incaricato"
					 , tipoOggetto:	Determina.TIPO_OGGETTO
					 , descrizione: 	"Mostra il Proponente, Unità Proponente, Dirigente, Funzionario e Incaricato"
					 , url:			"/atti/documenti/determina/determina_con_incaricato.zul"
					 , suggerimento:	"""Mostra i soggetti:
* REDATTORE:		tipicamente è l'utente corrente. Può appartenere a più unità.
* UO_PROPONENTE:	tipicamente corrisponde all'unità del redattore.
* FUNZIONARIO:		il funzionario può essere facoltativo.
* INCARICATO:		Incaricato a cui inviare la determina.
* DIRIGENTE:		il dirigente della determina è anche colui che la firma."""
					, soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.UO_PROPONENTE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE, TipoSoggetto.INCARICATO]],

					[ label: 		"Determina con Controllo Contabile"
					, tipoOggetto:	Determina.TIPO_OGGETTO
					, descrizione: 	"Mostra il Proponente, Unità Proponente, Dirigente, Funzionario"
					, url:			"/atti/documenti/determina/determina_con_controllo.zul"
					, suggerimento:	"""Mostra i soggetti:
* REDATTORE:		tipicamente è l'utente corrente. Può appartenere a più unità.
* UO_PROPONENTE:	tipicamente corrisponde all'unità del redattore.
* FUNZIONARIO:		il funzionario può essere facoltativo.
* DIRIGENTE:		il dirigente della determina è anche colui che la firma.
* UO_CONTROLLO:		l'unità di ulteriore controllo prima della firma"""
					, soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.UO_PROPONENTE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE, TipoSoggetto.UO_CONTROLLO]],

					[ label: 		"Determina con Controllo Contabile e Unità destinataria"
					, tipoOggetto:	Determina.TIPO_OGGETTO
					, descrizione: 	"Mostra il Proponente, Unità Proponente, Dirigente, Funzionario"
					, url:			"/atti/documenti/determina/determina_con_controllo_unita_destinataria.zul"
					, suggerimento:	"""Mostra i soggetti:
* REDATTORE:		tipicamente è l'utente corrente. Può appartenere a più unità.
* UO_PROPONENTE:	tipicamente corrisponde all'unità del redattore.
* FUNZIONARIO:		il funzionario può essere facoltativo.
* DIRIGENTE:		il dirigente della determina è anche colui che la firma.
* UO_CONTROLLO:		l'unità di ulteriore controllo prima della firma
* UO_DESTINATARIA:	eventuale unità a cui inviare la determina."""
					, soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.UO_PROPONENTE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE, TipoSoggetto.UO_CONTROLLO, TipoSoggetto.UO_DESTINATARIA]],

					[ label: 		"Determina congiunta"
					, tipoOggetto:	Determina.TIPO_OGGETTO
					, descrizione: 	"Mostra il Proponente, Unità Proponente, Dirigente, Funzionario, l'unità aggiuntiva di passaggio."
					, url:			"/atti/documenti/determina/determina_congiunta.zul"
					, suggerimento:	"""Mostra i soggetti:
* REDATTORE:		tipicamente è l'utente corrente. Può appartenere a più unità.
* UO_PROPONENTE:	tipicamente corrisponde all'unità del redattore.
* FUNZIONARIO:		il funzionario può essere facoltativo.
* DIRIGENTE:		il dirigente della determina è anche colui che la firma.
* UNITA FIRMATARIO:	unità del firmatario congiunto
* FIRMATARIO:		dirigente firmatario congiunto"""
					, soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.UO_PROPONENTE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE, TipoSoggetto.UO_FIRMATARIO, TipoSoggetto.FIRMATARIO]],

				   [ label: 		"Determina Congiunta con Incaricato"
					 , tipoOggetto:	Determina.TIPO_OGGETTO
					 , descrizione: "Mostra il Proponente, Unità Proponente, Dirigente, Funzionario, l'unità aggiuntiva di passaggio e l'Incaricato."
					 , url:			"/atti/documenti/determina/determina_congiunta_con_incaricato.zul"
					 , suggerimento:	"""Mostra i soggetti:
* REDATTORE:		tipicamente è l'utente corrente. Può appartenere a più unità.
* UO_PROPONENTE:	tipicamente corrisponde all'unità del redattore.
* FUNZIONARIO:		il funzionario può essere facoltativo.
* DIRIGENTE:		il dirigente della determina è anche colui che la firma.
* UNITA FIRMATARIO:	unità del firmatario congiunto
* FIRMATARIO:		dirigente firmatario congiunto
* INCARICATO:		Incaricato a cui inviare la determina."""
					 , soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.UO_PROPONENTE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE, TipoSoggetto.UO_FIRMATARIO, TipoSoggetto.FIRMATARIO, TipoSoggetto.INCARICATO]],

					[ label: 		"Determina congiunta con Controllo Contabile e unità destinataria"
					, tipoOggetto:	Determina.TIPO_OGGETTO
					, descrizione: 	"Mostra il Proponente, Unità Proponente, Dirigente, Funzionario, l'unità aggiuntiva di passaggio."
					, url:			"/atti/documenti/determina/determina_congiunta_con_controllo.zul"
					, suggerimento:	"""Mostra i soggetti:
* REDATTORE:		tipicamente è l'utente corrente. Può appartenere a più unità.
* UO_PROPONENTE:	tipicamente corrisponde all'unità del redattore.
* FUNZIONARIO:		il funzionario può essere facoltativo.
* DIRIGENTE:		il dirigente della determina è anche colui che la firma.
* UNITA FIRMATARIO:	unità del firmatario congiunto
* FIRMATARIO:		dirigente firmatario congiunto
* UO_CONTROLLO:		l'unità di ulteriore controllo prima della firma
* UO_DESTINATARIA:	eventuale unità a cui inviare la determina."""
					, soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.UO_PROPONENTE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE, TipoSoggetto.UO_FIRMATARIO, TipoSoggetto.FIRMATARIO, TipoSoggetto.UO_CONTROLLO, TipoSoggetto.UO_DESTINATARIA]],

					[ label: 		"Determina con passaggi aggiuntivi a unità"
					, tipoOggetto:	Determina.TIPO_OGGETTO
					, descrizione: 	"Mostra il Proponente, Unità Proponente, Dirigente, Funzionario, l'unità aggiuntiva di passaggio."
					, url:			"/atti/documenti/determina/determina_congiunta_uo_destinataria.zul"
					, suggerimento:	"""Mostra i soggetti:
* REDATTORE:		tipicamente è l'utente corrente. Può appartenere a più unità.
* UO_PROPONENTE:	tipicamente corrisponde all'unità del redattore.
* FUNZIONARIO:		il funzionario può essere facoltativo.
* DIRIGENTE:		il dirigente della determina è anche colui che la firma.
* UNITA DESTINATARIA:	unità aggiuntiva di controllo"""
					, soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.UO_PROPONENTE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE, TipoSoggetto.UO_DESTINATARIA]],

					[ label: 		"Decreto"
					, tipoOggetto:	Determina.TIPO_OGGETTO
					, descrizione: 	"Mostra il Proponente, Unità Proponente, Dirigente, Funzionario, Dirigente Aggiuntivo"
					, url:			"/atti/documenti/determina/decreto_standard.zul"
					, suggerimento:	"""Mostra i soggetti:
* REDATTORE:		tipicamente è l'utente corrente. Può appartenere a più unità.
* UO_PROPONENTE:	tipicamente corrisponde all'unità del redattore.
* FUNZIONARIO:		il funzionario può essere facoltativo.
* DIRIGENTE:		il dirigente della determina è anche colui che la firma.
* FIRMATARIO:		il dirigente aggiuntivo della determina è anche colui che la firma."""
					, soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.UO_PROPONENTE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE, TipoSoggetto.FIRMATARIO]],

					[ label: 		"Proposta Delibera Standard"
					, tipoOggetto:	PropostaDelibera.TIPO_OGGETTO
					, descrizione: 	"Mostra il Proponente, Unità Proponente, Dirigente, Funzionario"
					, url:			"/atti/documenti/propostaDelibera/propostaDelibera_standard.zul"
					, suggerimento:	"""Mostra i soggetti:
* REDATTORE:		tipicamente è l'utente corrente. Può appartenere a più unità.
* UO_PROPONENTE:	tipicamente corrisponde all'unità del redattore.
* FUNZIONARIO:		il funzionario può essere facoltativo.
* DIRIGENTE:		il dirigente della proposta è anche colui che la firma."""
					, soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.UO_PROPONENTE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE]],

					[ label: 		"Proposta Delibera Fuori Sacco"
					, tipoOggetto:	PropostaDelibera.TIPO_OGGETTO
					, descrizione: 	"Mostra il Proponente, Unità Proponente, Funzionario, Dirigente"
					, url:			"/atti/documenti/propostaDelibera/propostaDelibera_fuoriSacco.zul"
					, suggerimento:	"""Mostra i soggetti:
* REDATTORE:		tipicamente è l'utente corrente. Può appartenere a più unità.
* UO_PROPONENTE:	tipicamente corrisponde all'unità del redattore.
* FUNZIONARIO:		il funzionario può essere facoltativo.
* DIRIGENTE:		il dirigente della proposta è anche colui che la firma."""
					, soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.UO_PROPONENTE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE]],

					 [ label: 		 "Proposta Delibera con Firmatari"
				     , tipoOggetto:	 PropostaDelibera.TIPO_OGGETTO
				     , descrizione:  "Mostra il Proponente, Unità Proponente, Dirigente, Funzionario e i Firmatari"
				     , url:			 "/atti/documenti/propostaDelibera/propostaDelibera_conFirmatari.zul"
				     , suggerimento: """Mostra i soggetti:
* REDATTORE:		tipicamente è l'utente corrente. Può appartenere a più unità.
* UO_PROPONENTE:	tipicamente corrisponde all'unità del redattore.
* FUNZIONARIO:		il funzionario può essere facoltativo.
* DIRIGENTE:		il dirigente della proposta è anche colui che la firma.
* FIRMATARIO:		il dirigente aggiuntivo della proposta di delibera è anche colui che la firma."""
							   , soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.UO_PROPONENTE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE, TipoSoggetto.FIRMATARIO]],

				     [ label: 		"Proposta Delibera con Controllo Contabile"
					 , tipoOggetto:	PropostaDelibera.TIPO_OGGETTO
					 , descrizione: "Mostra il Proponente, Unità Proponente, Dirigente, Funzionario, unità di controllo"
					 , url:			"/atti/documenti/propostaDelibera/propostaDelibera_con_controllo.zul"
					 , suggerimento:	"""Mostra i soggetti:
* REDATTORE:		 tipicamente è l'utente corrente. Può appartenere a più unità.
* UO_PROPONENTE:	 tipicamente corrisponde all'unità del redattore.
* FUNZIONARIO:		 il funzionario può essere facoltativo.
* DIRIGENTE:		 il dirigente della proposta è anche colui che la firma.
* UO_CONTROLLO:      l'unità di ulteriore controllo contabile"""
                     , soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.UO_PROPONENTE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE, TipoSoggetto.UO_CONTROLLO]],

				   [ label: 		"Proposta Delibera con Incaricato"
					 , tipoOggetto:	PropostaDelibera.TIPO_OGGETTO
					 , descrizione: "Mostra il Proponente, Unità Proponente, Dirigente, Funzionario, Incaricato"
					 , url:			"/atti/documenti/propostaDelibera/propostaDelibera_con_incaricato.zul"
					 , suggerimento:	"""Mostra i soggetti:
* REDATTORE:		tipicamente è l'utente corrente. Può appartenere a più unità.
* UO_PROPONENTE:	tipicamente corrisponde all'unità del redattore.
* FUNZIONARIO:		il funzionario può essere facoltativo.
* DIRIGENTE:		il dirigente della proposta è anche colui che la firma.
* INCARICATO:		incaricato."""
					 , soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.UO_PROPONENTE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE, TipoSoggetto.INCARICATO]],

					 [ label: 		"Delibera"
					, tipoOggetto:	Delibera.TIPO_OGGETTO
					, descrizione: 	"Mostra il Segretario e il Presidente."
					, url:			"/atti/documenti/delibera/delibera_standard.zul"
					, suggerimento:	"""Mostra i soggetti:
* PRESIDENTE:		Il Presidente della Seduta che ha approvato la delibera.
* SEGRETARIO:		Il Segretario della Seduta che ha approvato la delibera."""
					, soggetti: [TipoSoggetto.PRESIDENTE, TipoSoggetto.SEGRETARIO]],

				   [ label: 		"Delibera presidente - segretario"
					 , tipoOggetto:	Delibera.TIPO_OGGETTO
					 , descrizione: 	"Mostra il Segretario e il Presidente che si possono modificare."
					 , url:			"/atti/documenti/delibera/delibera_con_soggetti_modificabili.zul"
					 , suggerimento:	"""Mostra i soggetti:
* PRESIDENTE:		Il Presidente della Seduta che ha approvato la delibera.
* SEGRETARIO:		Il Segretario della Seduta che ha approvato la delibera."""
											 , soggetti: [TipoSoggetto.PRESIDENTE, TipoSoggetto.SEGRETARIO]],

					[ label: 		"Delibera ASL"
					, tipoOggetto:	Delibera.TIPO_OGGETTO
					, descrizione: 	"Mostra il Direttore Amministrativo, Sanitario, Generale ed il Segretario."
					, url:			"/atti/documenti/delibera/delibera_asl.zul"
					, suggerimento:	"""Mostra i soggetti:
* DIRETTORE_AMMINISTRATIVO:		Il Direttore Amministrativo.
* DIRETTORE_SANITARIO:			Il Direttore Sanitario.
* DIRETTORE_GENERALE:			Il Direttore Generale.
* SEGRETARIO:					Il Segretario della seduta
* FIRMATARIO:					Ulteriori firmatari della Delibera"""
					, soggetti: [TipoSoggetto.DIRETTORE_AMMINISTRATIVO, TipoSoggetto.DIRETTORE_SANITARIO, TipoSoggetto.DIRETTORE_GENERALE, TipoSoggetto.SEGRETARIO, TipoSoggetto.FIRMATARIO]],
				
					[ label: 		"Delibera ASL Senza Segretario"
					, tipoOggetto:	Delibera.TIPO_OGGETTO
					, descrizione: 	"Mostra il Direttore Amministrativo, Sanitario e Generale."
					, url:			"/atti/documenti/delibera/delibera_asl_senza_segretario.zul"
					, suggerimento:	"""Mostra i soggetti:
* DIRETTORE_AMMINISTRATIVO:		Il Direttore Amministrativo.
* DIRETTORE_SANITARIO:			Il Direttore Sanitario.
* DIRETTORE_GENERALE:			Il Direttore Generale."""
					, soggetti: [TipoSoggetto.DIRETTORE_AMMINISTRATIVO, TipoSoggetto.DIRETTORE_SANITARIO, TipoSoggetto.DIRETTORE_GENERALE]],

					[ label: 		"Delibera ASL Direttore Generale e Segretario"
							   , tipoOggetto:	Delibera.TIPO_OGGETTO
							   , descrizione: 	"Mostra il Direttore Generale ed il Segretario."
							   , url:			"/atti/documenti/delibera/delibera_asl_direttoreGenerale_segretario.zul"
							   , suggerimento:	"""Mostra i soggetti:
* DIRETTORE_GENERALE:			Il Direttore Generale
* SEGRETARIO: 					Il Segretario della seduta"""
							   , soggetti: [TipoSoggetto.DIRETTORE_GENERALE, TipoSoggetto.SEGRETARIO]],

                   [ label: 		"Delibera ASL Con Direttore Socio Sanitario"
                     , tipoOggetto:	Delibera.TIPO_OGGETTO
                     , descrizione: 	"Mostra il Direttore Amministrativo, Sanitario, Generale ed Socio Sanitario."
                     , url:			"/atti/documenti/delibera/delibera_asl_direttoreSocioSanitario.zul"
                     , suggerimento:	"""Mostra i soggetti:
* DIRETTORE_AMMINISTRATIVO:		Il Direttore Amministrativo.
* DIRETTORE_SANITARIO:			Il Direttore Sanitario.
* DIRETTORE_GENERALE:			Il Direttore Generale.
* DIRETTORE_SOCIO_SANITARIO:    Il Direttore Socio Sanitario."""
                                             , soggetti: [TipoSoggetto.DIRETTORE_AMMINISTRATIVO, TipoSoggetto.DIRETTORE_SANITARIO, TipoSoggetto.DIRETTORE_GENERALE, TipoSoggetto.DIRETTORE_SOCIO_SANITARIO]],
					[ label: 		"Visto e Parere Standard"
					, tipoOggetto:	VistoParere.TIPO_OGGETTO
					, descrizione: 	"Mostra l'unità destinataria e il dirigente firmatario."
					, url:			"/atti/documenti/visto/visto_standard.zul"
					, suggerimento:	"""Mostra i soggetti:
* UO_DESTINATARIA:	l'utente che riceve il visto o parere per la redazione e l'eventuale cambio del dirigente.
* FIRMATARIO:		il firmatario del visto o parere"""
					, soggetti: [TipoSoggetto.UO_DESTINATARIA, TipoSoggetto.FIRMATARIO]],

					[ label: 		"Certificato Standard"
					, tipoOggetto:	Certificato.TIPO_OGGETTO
					, descrizione: 	"Mostra il FIRMATARIO."
					, url:			"/atti/documenti/certificato.zul"
					, suggerimento:	"""Mostra i soggetti:
* FIRMATARIO:		il firmatario del certificato"""
					, soggetti: [TipoSoggetto.FIRMATARIO]],

				   [ label: 		"Stampa di Seduta con firmatari multipli"
					 , tipoOggetto:	SedutaStampa.TIPO_OGGETTO
					 , descrizione: "Mostra i vari firmatari e il redattore del documento."
					 , url:			"/odg/seduta/sedutaStampa/sedutaStampa_firmatari_multipli.zul"
					 , suggerimento:"""Mostra i soggetti:
* REDATTORE:        colui che redige il documento
* FIRMATARIO:		i vari firmatari del documento
* UO_PROTOCOLLO:	unità con cui protocollare"""
					 , soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.FIRMATARIO, TipoSoggetto.UO_PROPONENTE]],

				   [ label: 		"Stampa di Seduta con singolo firmatario"
					 , tipoOggetto:	SedutaStampa.TIPO_OGGETTO
					 , descrizione: "Mostra i vari firmatari e il redattore del documento."
					 , url:			"/odg/seduta/sedutaStampa/sedutaStampa_standard.zul"
					 , suggerimento:"""Mostra i soggetti:
* REDATTORE:        colui che redige il documento
* FIRMATARIO:		il firmatario del documento
* UO_PROTOCOLLO: 	unità con cui protocollare"""
											 , soggetti: [TipoSoggetto.REDATTORE, TipoSoggetto.FIRMATARIO, TipoSoggetto.UO_PROPONENTE]]]

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		listaOggetti 			= WkfTipoOggetto.list(sort: "nome", order: "asc").toDTO()
		listaTipoSoggetto		= [new TipoSoggettoDTO(codice:"-- nessuno --", titolo:"-- nessuno --")] + TipoSoggetto.list(sort: "titolo", order: "asc").toDTO()
		def nessuno = new it.finmatica.atti.dto.impostazioni.RegolaCalcoloDTO(id:-1, titolo:"-- nessuno --")
		listaRegoleComponenteDefault	= [nessuno] + RegolaCalcolo.findAllByCategoriaAndTipo(TipoSoggetto.CATEGORIA_COMPONENTE, RegolaCalcolo.TIPO_DEFAULT, [sort:"titolo", order:"asc"]).toDTO()
		listaRegoleUnitaDefault     	= [nessuno] + RegolaCalcolo.findAllByCategoriaAndTipo(TipoSoggetto.CATEGORIA_UNITA, 	 RegolaCalcolo.TIPO_DEFAULT, [sort:"titolo", order:"asc"]).toDTO()
		listaRegoleComponenteLista  	= [nessuno] + RegolaCalcolo.findAllByCategoriaAndTipo(TipoSoggetto.CATEGORIA_COMPONENTE, RegolaCalcolo.TIPO_LISTA, [sort:"titolo", order:"asc"]).toDTO()
		listaRegoleUnitaLista       	= [nessuno] + RegolaCalcolo.findAllByCategoriaAndTipo(TipoSoggetto.CATEGORIA_UNITA, 	 RegolaCalcolo.TIPO_LISTA, [sort:"titolo", order:"asc"]).toDTO()

		listaRuoli = [new Ad4RuoloDTO(ruolo:"-- nessuno --", descrizione: "-- nessuno --")] + Ad4Ruolo.findAllByRuoloLike(Impostazione.PREFISSO_RUOLO+'%', [sort: "ruolo", order: "asc"])?.toDTO()

		if (id != null) {
			selectedRecord 			= caricaCaratteristica(id)
			listaCarTipiSoggetto 	= caricaCaratteristicaTipiSoggetto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica (selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)

			caricaListaZul ()

			selectedLayout = listaZul.find { it.url == selectedRecord.layoutSoggetti }
		} else {
			selectedRecord = new CaratteristicaTipologiaDTO(valido:true)
		}
	}

	@NotifyChange(["listaZul"])
	@Command caricaListaZul () {
		listaZul = listaZulPossibili.findAll { it.tipoOggetto == selectedRecord.tipoOggetto.codice }
	}

	@NotifyChange("listaCarTipiSoggetto")
	@Command onSelectLayout () {
		// svuoto l'elenco dei soggetti.
		listaCarTipiSoggetto = [];
		// lo ricreo
		for (String tipoSoggetto : selectedLayout.soggetti) {
			listaCarTipiSoggetto << new CaratteristicaTipoSoggettoDTO(sequenza:listaCarTipiSoggetto.size(), tipoSoggetto: TipoSoggetto.get(tipoSoggetto).toDTO());
		}
		self.invalidate()
	}

	private CaratteristicaTipologiaDTO caricaCaratteristica (Long idCaratteristicaTipologia) {
		CaratteristicaTipologia caratteristica = CaratteristicaTipologia.createCriteria().get {
			eq("id", idCaratteristicaTipologia)

			fetchMode("utenteIns", FetchMode.JOIN)
			fetchMode("utenteUpd", FetchMode.JOIN)
		}
		return caratteristica.toDTO()
	}

	private List<CaratteristicaTipologiaDTO> caricaCaratteristicaTipiSoggetto (Long idCaratteristicaTipologia) {
		return CaratteristicaTipoSoggetto.createCriteria().list {
			eq ("caratteristicaTipologia.id", idCaratteristicaTipologia)
			order ("sequenza", "asc")

			fetchMode("tipoSoggetto", 			FetchMode.JOIN)
			fetchMode("tipoSoggettoPartenza", 	FetchMode.JOIN)
			fetchMode("regolaCalcoloLista", 	FetchMode.JOIN)
			fetchMode("regolaCalcoloDefault", 	FetchMode.JOIN)
			fetchMode("ruolo", 					FetchMode.JOIN)
		}.toDTO()
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		selectedRecord.layoutSoggetti = selectedLayout.url
		def idCar = caratteristicaTipologiaDTOService.salva(selectedRecord, listaCarTipiSoggetto).id
		selectedRecord 		 = caricaCaratteristica(idCar)
		listaCarTipiSoggetto = caricaCaratteristicaTipiSoggetto(idCar)
		if (selectedRecord.id == null) {
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		}
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalvaChiudi() {
		onSalva()
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		// se voglio disattivare la caratteristica di tipologia, prima verifico che non sia usata da nessuna tipologia di determina/delibera ancora valida.
		if (selectedRecord.valido && valido == false) {
			def tipologie = [];
			tipologie.addAll(TipoDetermina.inUsoPerCaratteristicaTipologia(selectedRecord.id).list())
			tipologie.addAll(TipoDelibera.inUsoPerCaratteristicaTipologia(selectedRecord.id).list())
			tipologie.addAll(TipoCertificato.inUsoPerCaratteristicaTipologia(selectedRecord.id).list())
			tipologie.addAll(TipoVistoParere.inUsoPerCaratteristicaTipologia(selectedRecord.id).list())

			if (tipologie.size() > 0) {
				Clients.showNotification ("Non è possibile disattivare il modello testo perché è usato da altre tipologie ancora attive:\n" +
										  (tipologie.titolo.join("\n")), Clients.NOTIFICATION_TYPE_WARNING, self, "before_center", tipologie.size()*3000, true);
				return;
			}
		}

		Messagebox.show("Modificare la validità della caratteristica?", "Modifica validità",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
				public void onEvent(Event e){
					if (Messagebox.ON_OK.equals(e.getName())) {
						super.getSelectedRecord().valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, CaratteristicaTipologiaDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, CaratteristicaTipologiaDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, CaratteristicaTipologiaDettaglioViewModel.this, "datiModifica")
					}
				}
			}
		)
	}
}
