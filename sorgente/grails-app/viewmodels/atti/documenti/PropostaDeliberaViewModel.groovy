package atti.documenti

import dizionari.atti.CaratteristicaTipologiaDettaglioViewModel
import grails.orm.PagedResultList
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.FileAllegatoStorico
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.dizionari.*
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.tipologie.GestioneTestiModelloCompetenza
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.documenti.tipologie.TipoDeliberaCompetenza
import it.finmatica.atti.dto.dizionari.ImpattoGenerazionaleDTO
import it.finmatica.atti.dto.dizionari.DelegaDTO
import it.finmatica.atti.dto.documenti.*
import it.finmatica.atti.documenti.TipoDatoAggiuntivo
import it.finmatica.atti.dto.documenti.tipologie.TipoDeliberaDTO
import it.finmatica.atti.dto.integrazioni.Ce4ContoDTO
import it.finmatica.atti.dto.integrazioni.Ce4FornitoreDTO
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoComponenteDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.export.ExportService
import it.finmatica.atti.impostazioni.*
import it.finmatica.atti.integrazioni.CasaDiVetroService
import it.finmatica.atti.integrazioni.Ce4Conto
import it.finmatica.atti.integrazioni.Ce4Fornitore
import it.finmatica.atti.integrazioni.protocollo.ProtocolloPiTre
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.atti.odg.dizionari.EsitoStandard
import it.finmatica.dto.DTO
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dto.documenti.viste.So4DelegaService
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.gestionetesti.ui.dizionari.GestioneTestiModelloDTOService
import it.finmatica.so4.login.So4UserDetail
import it.finmatica.so4.login.detail.UnitaOrganizzativa
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.apache.log4j.Logger
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.SelectEvent
import org.zkoss.zul.ListModelList
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window
import it.finmatica.atti.documenti.GruppoPoliticoDocumentoService


import java.text.DecimalFormat

class PropostaDeliberaViewModel extends AbstractViewModel<PropostaDelibera> {

	private static final Logger log = Logger.getLogger(PropostaDeliberaViewModel.class)

	// services
	AttiGestoreCompetenze          gestoreCompetenze
	AttiFileDownloader             attiFileDownloader
	GestioneTestiService           gestioneTestiService
	CaratteristicaTipologiaService caratteristicaTipologiaService
	IntegrazioneContabilita        integrazioneContabilita
	CasaDiVetroService             casaDiVetroService
	ExportService                  exportService

	AllegatoDTOService              allegatoDTOService
	DocumentoDTOService				documentoDTOService
	VistoParereDTOService           vistoParereDTOService
	VistoParereService				vistoParereService
	DestinatarioNotificaDTOService  destinatarioNotificaDTOService
	PropostaDeliberaDTOService      propostaDeliberaDTOService
	DocumentoCollegatoDTOService    documentoCollegatoDTOService
	NotificheService                notificheService
	RegolaCampoService 				regolaCampoService
	DocumentoService				documentoService
	TokenIntegrazioneService		tokenIntegrazioneService
	DatiAggiuntiviService			datiAggiuntiviService
    BudgetDTOService                budgetDTOService
	PropostaDeliberaDelegaDTOService propostaDeliberaDelegaDTOService
	GestioneTestiModelloDTOService  gestioneTestiModelloDTOService
	GruppoPoliticoDocumentoDTOService gruppoPoliticoDocumentoDTOService
	GruppoPoliticoDocumentoService  gruppoPoliticoDocumentoService
	So4DelegaService				so4DelegaService

	// componenti
	Window popupCambiaTipologia

	// dati
	def listaTipologie
	PropostaDeliberaDTO propostaDelibera
	// visti
	VistoParereDTO visto
	List<VistoParereDTO> listaVisti
	def listaDestinatariInterni
	def listaDestinatariEsterni
	def listaDestinatariPiTre
	def listaAllegati
	ListModelList<GruppoPoliticoDocumentoDTO> listaFirmatariGruppoPolitico = new ListModelList<>();

	def listaDocumentiCollegati
	def listaModelliTestoPredefiniti
	def listaIndirizziDelibera
	def listaCommissioni
	def storico
	def listaCategorie
    def listaTipiBudget
    def listaBudget
	List<PropostaDeliberaDelegaDTO> listaAltreDeleghe
	List<Ce4ContoDTO> listaCe4Conti
	List<Ce4FornitoreDTO> listaCe4Fornitori
	boolean categoriaAbilitata
	boolean isNotificaPresente
	boolean isEstrattoPresente
    boolean budgetAbilitato
	boolean delegheMultipleAbilitato

	boolean mostraGestioneGruppiPolitici = false

	IProtocolloEsterno protocolloEsterno

	// mappa dei soggetti
	Map<String, it.finmatica.atti.zk.SoggettoDocumento> soggetti = [:]

	// stato
	String titolo
	def competenze
	def posizioneFlusso
	def campiProtetti
	String urlCasaDiVetro
	def noteTrasmissionePrecedenti
	boolean attorePrecedente
	String estremiDelibera
	boolean mostraNoteTrasmissionePrecedenti
	boolean abilitaNoteContabili
	boolean abilitaNoteCommissione
	boolean riservatoVisibile
	boolean abilitaDestinatariEsterni
	boolean abilitaDestinatariInterni
    boolean destinatariInterniObbligatori
	boolean abilitaDestinatari
	boolean abilitaPubblicazioneProposte
	boolean abilitaPubblicazioneFinoRevoca
	boolean riservatoModificabile
	boolean visContabilita = true //indica se l'utente può visualizzare il folder della contabilità
	boolean firmaRemotaAbilitata
	boolean mostraArchiviazioni
	boolean mostraCorteConti
	boolean mostraEseguibilitaImmediata
	boolean mostraParereRevisoriConti
	boolean mostraImportoTotale = false
	boolean modelloTestoUnitaProponente
	boolean mostraComboModelloTesti = true
	boolean gestioneFondi
	boolean gestioneGruppiPolitici

	// paginazione integrazione ce4
	int activePage  = 0
	int pageSize = 10
	int totalSize = 0
	String filtroRicerca

	// abilitazione del protocollo
	boolean protocollo
	boolean classifica_obb
	boolean fascicolo_obb

	boolean propostaFirmata = false
	So4UserDetail utente

	// gestione del testo
	boolean testoLockato
	boolean lockPermanente

	// gestione contabilità
	boolean conDocumentiContabili = false
	boolean contabilitaAbilitata  = false

	// indica se nella lista dei visti si mostrano solo i visti validi o anche i non validi
	boolean mostraSoloVistiValidi = true;
	String zulContabilita

	// indica se nella lista dei soggetti sono presenti firmatari
	boolean firmatariAbilitati = false
	def soggettiFirmatari

	// gestione della priorità
	boolean abilitaPriorita
	boolean abilitaRichiestaSeduta

	boolean mostraNote	  = true
	boolean mostraStorico = true

	// indica se è bloccato da un altro utente
	boolean isLocked = false

	// indica se il documento deve essere comunque aperto in lettura (delegato)
	boolean forzaCompetenzeLettura

	// utente per conto del quale si sta operando (delega So4)
	Ad4Utente delegante

	boolean controllaPriorita = false
	boolean isMotivazionePresente = false

	//PND per i destinatari gestita per la determina
	boolean  pndNotifica = false
	boolean  pndPagamento = false

	String tipoIntegrazioneContabilita
	boolean isIntegrazioneContabilitaAtsPavia = false

	// Impatto Generazionale
	boolean impattoGenerazionaleAbilitato
	List<ImpattoGenerazionaleDTO>  listaImpattoGenerazionale
	ImpattoGenerazionaleDTO impattoGenerazionale
	boolean mostraImpattoGenerazionale = false

	// Protocollazione
	String tipoProtocollazione
	boolean isIntegrazioneProtocolloPiTre = false

	boolean isProtocollato = false

	@NotifyChange(["propostaDelibera", "competenze", "mostraImpattoGenerazionale", "mostraGestioneGruppiPolitici"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long idPropostaDelibera,  @ExecutionArgParam("fuoriSacco") Boolean pFuoriSacco, @ExecutionArgParam("idDocumentoEsterno") Long idDocumentoEsterno, @ExecutionArgParam("competenzeLettura") Boolean competenzeLettura,
			   @ExecutionArgParam("classificazione") String classificazione, @ExecutionArgParam("numeroFascicolo") String numeroFascicolo, @ExecutionArgParam("annoFascicolo") String annoFascicolo,
			   @ExecutionArgParam("delegante") Ad4Utente delegante) {
		this.self = w

		firmaRemotaAbilitata 			= Impostazioni.FIRMA_REMOTA.abilitato
		protocollo						= Impostazioni.PROTOCOLLO_SEZIONE.abilitato
		classifica_obb 					= Impostazioni.PROTOCOLLO_CLASSIFICA_OBBL.abilitato
		fascicolo_obb 					= Impostazioni.PROTOCOLLO_FASCICOLO_OBBL.abilitato
		abilitaNoteContabili 			= Impostazioni.NOTE_CONTABILI.abilitato
		riservatoVisibile 				= Impostazioni.RISERVATO.abilitato
		abilitaDestinatariEsterni 		= Impostazioni.DESTINATARI_ESTERNI.abilitato
		abilitaDestinatariInterni 		= Impostazioni.DESTINATARI_INTERNI.abilitato
        destinatariInterniObbligatori   = Impostazioni.DESTINATARI_INTERNI_OBBLIG_DELI.abilitato
		abilitaDestinatari 				= (abilitaDestinatariEsterni || abilitaDestinatariInterni)
		abilitaPubblicazioneProposte 	= Impostazioni.PUBBLICAZIONE_VIS_PROP_DELIBERA.abilitato
		abilitaPubblicazioneFinoRevoca 	= Impostazioni.PUBBLICAZIONE_FINO_REVOCA.abilitato
		mostraArchiviazioni             = Impostazioni.PROTOCOLLO_MOSTRA_SEZIONE_ARCHIVIAZIONI.abilitato
		mostraCorteConti				= Impostazioni.GESTIONE_CORTE_CONTI.abilitato
		mostraEseguibilitaImmediata		= Impostazioni.ESEGUIBILITA_IMMEDIATA_ATTIVA.abilitato
		mostraParereRevisoriConti		= Impostazioni.PARERE_REVISORI_CONTI.abilitato
		abilitaPriorita					= Impostazioni.PRIORITA.abilitato
		abilitaRichiestaSeduta			= Impostazioni.RICHIESTA_SEDUTA.abilitato
		modelloTestoUnitaProponente 	= Impostazioni.MODELLI_TESTO_UNITA_PROPONENTE.abilitato
		gestioneFondi                   = Impostazioni.GESTIONE_FONDI.abilitato
		budgetAbilitato                 = Impostazioni.GESTIONE_BUDGET.abilitato
		delegheMultipleAbilitato        = Impostazioni.DELEGHE_MULTIPLE.abilitato
		gestioneGruppiPolitici          = Impostazioni.GESTIONE_GRUPPI_POLITICI.abilitato
		impattoGenerazionaleAbilitato   = Impostazioni.IMPATTO_GENERAZIONALE.abilitato
		tipoIntegrazioneContabilita     = Impostazioni.CONTABILITA.valore
		tipoProtocollazione             = Impostazioni.PROTOCOLLO.valore
		forzaCompetenzeLettura			= competenzeLettura
        this.delegante					= delegante

		if (tipoIntegrazioneContabilita == "integrazioneContabilitaAtsPavia") {
			isIntegrazioneContabilitaAtsPavia = true
			BindUtils.postNotifyChange(null, null, this, "isIntegrazioneContabilitaAtsPavia")
		}

		if (tipoProtocollazione == "protocolloPiTre") {
			isIntegrazioneProtocolloPiTre = true
			BindUtils.postNotifyChange(null, null, this, "isIntegrazioneProtocolloPiTre")
		}

		if (idPropostaDelibera != null) {
			propostaDelibera = new PropostaDeliberaDTO([id: idPropostaDelibera, fuoriSacco: pFuoriSacco?:false, statoOdg: pFuoriSacco?StatoOdg.COMPLETO:StatoOdg.INIZIALE])
		} else {
			propostaDelibera = PropostaDelibera.findByIdDocumentoEsterno(idDocumentoEsterno).toDTO()
			idPropostaDelibera = propostaDelibera.id
		}

		//se la proposta ha avuto almeno un esito (in odg) di tipo INVIA_COMMISSIONE allora devono essere visibili le note della commissione
		abilitaNoteCommissione = false
		List listaEsiti = OggettoSeduta.createCriteria().list() {
			projections {
				rowCount()
			}
			eq("propostaDelibera.id", idPropostaDelibera)
			eq("confermaEsito", true)
			esito {
				eq("esitoStandard.codice", EsitoStandard.INVIA_COMMISSIONE)
			}
		}

		if (listaEsiti[0] > 0)
			abilitaNoteCommissione = true

		utente = springSecurityService.principal;
		if (idPropostaDelibera > 0) {
			aggiornaMaschera(PropostaDelibera.get(idPropostaDelibera))
		} else {
			
			if ( ! springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_CREA_PROPOSTA_DELIBERA.valore)){
				throw new AttiRuntimeException("L'utente ${springSecurityService.principal.username} non ha i diritti di inserimento del documento.")
			}

			//quando creo un nuovo atto, la lista dei modelli testi si deve vedere solo se NON attiva l'impostazione modelloTestoUnitaProponente
			if (modelloTestoUnitaProponente) {
				mostraComboModelloTesti = false
			}

			titolo = "Crea" + Labels.getLabel("label.nuovaProposta");
			propostaDelibera.dataProposta = new Date()
			competenze = [lettura: true, modifica: true, cancellazione: true]

			//se il protocollo è GDM provo a valorizzare classifica e fascicolo se passati nell'url
			if ("protocolloEsternoGdm".equals(tipoProtocollazione)) {
				if (classificazione != null) {
					IProtocolloEsterno.Classifica classifica = protocolloEsterno.getClassifica(classificazione, propostaDelibera.dataProposta)
					if (classifica != null) {
						propostaDelibera.classificaCodice  = classifica.codice
						propostaDelibera.classificaDal = classifica.dal
						propostaDelibera.classificaDescrizione = classifica.descrizione

						if ((annoFascicolo != null) && (numeroFascicolo != null)) {
							IProtocolloEsterno.Fascicolo fascicolo = protocolloEsterno.getFascicolo(classifica, numeroFascicolo, Integer.parseInt(annoFascicolo))
							if (fascicolo != null) {
								propostaDelibera.fascicoloAnno = fascicolo.anno
								propostaDelibera.fascicoloNumero = fascicolo.numero
								propostaDelibera.fascicoloOggetto = fascicolo.oggetto
							}
						}
					}
				}
			}

			// in apertura della maschera, il redattore è l'utente corrente:
			As4SoggettoCorrente s = springSecurityService.principal.soggetto
			UnitaOrganizzativa uo = springSecurityService.principal.uo()[0]
			soggetti[TipoSoggetto.REDATTORE] = new it.finmatica.atti.zk.SoggettoDocumento(TipoSoggetto.get(TipoSoggetto.REDATTORE), s.utenteAd4, So4UnitaPubb.getUnita(uo.id, uo.ottica, uo.dal).get())

			propostaDelibera.fuoriSacco = (pFuoriSacco == null)?false:pFuoriSacco
            propostaDelibera.controllaDestinatari = destinatariInterniObbligatori
			listaVisti = []
		}

		listaTipologie = TipoDeliberaCompetenza.createCriteria().list() {
			projections {
				tipoDelibera {
					groupProperty("id")
					groupProperty("titolo")
					groupProperty("descrizione")
				}
			}

			tipoDelibera {
				eq ("valido", true)
				if (propostaDelibera.fuoriSacco) {
					isNotNull("progressivoCfgIterFuoriSacco")
				} else {
					isNotNull("progressivoCfgIter")
				}
			}

			AttiGestoreCompetenze.controllaCompetenze(delegate)(utente)

			tipoDelibera {
				order ("titolo")
			}
		}.collect { new TipoDeliberaDTO(id: it[0], titolo:it[1], descrizione:it[2]) }

		listaCommissioni = Commissione.createCriteria().list() {
			or {
				eq("valido", true)
				if (propostaDelibera?.commissione?.id) {
					eq("id", propostaDelibera.commissione.id)
				}
			}
			order("titolo", "asc")
		}.toDTO();

		aggiornaPulsanti()
		caricaListaCategorie()
		caricaFirmatari()
        caricaListaBudget()
		caricaListaAltreDeleghe()
		caricaListaImpattoGenerazionale()
		caricaGestioneGruppiPolitici()
		caricaListaIndirizziDelibera()
	}

	@AfterCompose
	void afterCompose(@SelectorParam("#popupCambiaTipologia") Window popupTipologia){
		if (propostaDelibera.tipologia == null) {
			this.popupCambiaTipologia = popupTipologia
			popupCambiaTipologia.doModal()
		}
	}

	@Command onSelectTipologia () {
		propostaDelibera.tipologia = TipoDelibera.findById(propostaDelibera.tipologia.id, [fetch: [caratteristicaTipologia:'eager', caratteristicaTipologiaFuoriSacco:'eager', commissione:'eager', oggettiRicorrenti: 'eager']]).toDTO()

		calcolaSoggetti (TipoSoggetto.REDATTORE)
		aggiornaPulsanti ()
		caricaListaModelliTestoPredefiniti ()

		propostaDelibera.controlloFunzionario 	= (propostaDelibera.tipologia.funzionarioObbligatorio || Impostazioni.DEFAULT_FUNZIONARIO.abilitato);
		propostaDelibera.giorniPubblicazione  	= propostaDelibera.tipologia.giorniPubblicazione;
		propostaDelibera.pubblicaRevoca	   		= propostaDelibera.tipologia.pubblicazioneFinoARevoca;
		propostaDelibera.commissione 			= propostaDelibera.tipologia.commissione;
		propostaDelibera.eseguibilitaImmediata  = propostaDelibera.tipologia.eseguibilitaImmediata;
		propostaDelibera.tipologia.caratteristicaTipologia = CaratteristicaTipologia.findById(propostaDelibera.tipologia.caratteristicaTipologia.id, [fetch: [caratteristicheTipiSoggetto: 'eager']]).toDTO();

		caricaFirmatari()
		caricaListaImpattoGenerazionale()
		abilitaImportoTotale()

		BindUtils.postNotifyChange(null, null, this, "listaImpattoGenerazionale")
		BindUtils.postNotifyChange(null, null, this, "listaDestinatariInterni")
		BindUtils.postNotifyChange(null, null, this, "soggetti")
		BindUtils.postNotifyChange(null, null, this, "soggettiFirmatari")
		BindUtils.postNotifyChange(null, null, this, "propostaDelibera")

		Events.postEvent(new Event(Events.ON_CLOSE, this.popupCambiaTipologia, null))
		self.invalidate()
	}

	void onCambiaTipologia (TipoDeliberaDTO tipologia) {
		propostaDelibera.tipologia = tipologia
		if (propostaDelibera.fuoriSacco)
			propostaDelibera.tipologia = TipoDelibera.findById(propostaDelibera.tipologia.id, [fetch: [caratteristicaTipologiaFuoriSacco:'eager', oggettiRicorrenti: 'eager']]).toDTO()
		else
			propostaDelibera.tipologia = TipoDelibera.findById(propostaDelibera.tipologia.id, [fetch: [caratteristicaTipologia:'eager', oggettiRicorrenti: 'eager']]).toDTO()

		if (propostaDelibera.tipologia.funzionarioObbligatorio)
			propostaDelibera.controlloFunzionario = propostaDelibera.tipologia.funzionarioObbligatorio
		else
			propostaDelibera.controlloFunzionario = Impostazioni.DEFAULT_FUNZIONARIO.abilitato
		propostaDelibera.giorniPubblicazione  	= propostaDelibera.tipologia.giorniPubblicazione
		propostaDelibera.pubblicaRevoca	   		= propostaDelibera.tipologia.pubblicazioneFinoARevoca

		calcolaSoggetti (TipoSoggetto.REDATTORE)
		aggiornaPulsanti ()
		caricaListaModelliTestoPredefiniti ()

		propostaDelibera = propostaDeliberaDTOService.cambiaTipologia(propostaDelibera)

		caricaListaImpattoGenerazionale()
		abilitaImportoTotale()

		BindUtils.postNotifyChange(null, null, this, "listaImpattoGenerazionale")
		BindUtils.postNotifyChange(null, null, this, "listaDestinatariInterni")
		BindUtils.postNotifyChange(null, null, this, "soggetti")
		BindUtils.postNotifyChange(null, null, this, "propostaDelibera")
	}

	/*
	 * Gestione Applet Testo
	 */
	@NotifyChange(["testoLockato"])
	@Command editaTesto() {
		testoLockato = gestioneTesti.editaTesto (propostaDelibera);
	}

	@Command onEliminaTesto () {
		gestioneTesti.eliminaTesto(propostaDelibera, this)
	}

	@Command onDownloadTesto () {
		PropostaDelibera d = propostaDelibera.domainObject
		attiFileDownloader.downloadFileAllegato(d, d.testo)
	}

	@Command onDownloadTestoStorico (@BindingParam("tipoOggetto") String tipoOggetto, @BindingParam("id") Long id, @BindingParam("idFileAllegato") Long idFileAllegato) {
		FileAllegatoStorico f = FileAllegatoStorico.get(idFileAllegato)
		attiFileDownloader.downloadFileAllegato(DocumentoFactory.getDocumentoStorico(id, tipoOggetto), f, true)
	}

	@Command onSceltaOggettoRicorrente () {
		def listaOggettiRicorrenti = Impostazioni.OGGETTI_RICORRENTI_TIPOLOGIE.abilitato ? propostaDelibera?.tipologia?.oggettiRicorrenti : OggettoRicorrente.findAllByValidoAndDelibera(true, true).toDTO()
		Window w = Executions.createComponents ("/atti/documenti/popupSceltaOggettoRicorrente.zul", self, [listaOggettiRicorrenti: listaOggettiRicorrenti, cancella: true])
		w.onClose { event ->
			if (event.data != null){
				if (event.data.id > 0) {
					propostaDelibera.oggettoRicorrente = event.data
					propostaDelibera.oggetto = event.data.oggetto.toUpperCase()
				}
				else {
					propostaDelibera.oggettoRicorrente = null
				}
				BindUtils.postNotifyChange(null, null, this, "propostaDelibera")
			}
		}
		w.doModal()
	}

	/*
	 * Gestione indirizzi delibera
	 */
	private void caricaListaIndirizziDelibera () {
		if (Impostazioni.INDIRIZZO_DELIBERA.abilitato) {
			listaIndirizziDelibera = IndirizzoDelibera.findAllByValido(true, [sort:"titolo", order:"desc"]).toDTO()
		} else {
			listaIndirizziDelibera = null
		}

		BindUtils.postNotifyChange(null, null, this, "listaIndirizziDelibera")
	}

	/*
	 * Gestione deleghe
	 */
	@Command onAggiungiDelega () {
		Window w = Executions.createComponents("/atti/documenti/popupSceltaDelega.zul", self, [propostaDelibera : propostaDelibera])
		w.onClose { event ->
			if (event.data!= null) {
				if (event.data instanceof String && event.data == "eliminaDelega") {
					propostaDelibera.delega =  null
				} else {
					propostaDelibera.delega =  event.data
				}
				BindUtils.postNotifyChange(null, null, this, "propostaDelibera")
				
				// questa invalidate serve perché da alcuni clienti le Deleghe hanno titoli
				// molto lunghi che sfagiolano l'interfaccia. Con questa invalidate zk si ricalcola
				// e rimane tutto leggibile.
				self.invalidate()
			}
		}
		w.doModal()
	}

	private void caricaListaAltreDeleghe () {
		if (delegheMultipleAbilitato) {
			listaAltreDeleghe = propostaDeliberaDelegaDTOService.getListaAltreDeleghe(propostaDelibera.domainObject)
		} else {
			listaAltreDeleghe = null
		}
		BindUtils.postNotifyChange(null, null, this, "listaAltreDeleghe")
	}

	@Command onAggiungiAltraDelega () {
		Window w = Executions.createComponents("/atti/documenti/popupSceltaDelega.zul", self, [propostaDelibera : propostaDelibera, mostraTogliDelega: false])
		w.onClose { event ->
			if (event?.data != null && !(event.data instanceof String)) {
				DelegaDTO delegaSelezionata = event.data
				if (propostaDelibera.delega?.id == delegaSelezionata.id) {
					Messagebox.show("La delega selezionata è già impostata come delega principale della proposta", "Attenzione!", Messagebox.OK, Messagebox.EXCLAMATION)
				} else if (listaAltreDeleghe*.delega*.id.contains(delegaSelezionata.id)) {
					Messagebox.show("La delega selezionata è già stata aggiunta", "Attenzione!", Messagebox.OK, Messagebox.EXCLAMATION)
				} else {
					propostaDeliberaDelegaDTOService.aggiungiDelega(propostaDelibera, delegaSelezionata)
					propostaDelibera.version = propostaDelibera.domainObject.version
					caricaListaAltreDeleghe()
				}
			}
		}
		w.doModal()
	}

	@Command
	void onEliminaAltraDelega (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("altraDelega") PropostaDeliberaDelegaDTO altraDelega) {
		Messagebox.show("Eliminare la delega selezionata?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				void onEvent (Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						propostaDeliberaDelegaDTOService.eliminaDelega(altraDelega.id)
						propostaDelibera.version = propostaDelibera.domainObject.version
						caricaListaAltreDeleghe()
					}
				}
			})
	}

	/*
	 * Gestione categorie
	 */
	private void caricaListaCategorie() {
		categoriaAbilitata = (Impostazioni.CATEGORIA_PROPOSTA_DELIBERA.abilitato)
		if(categoriaAbilitata){
			listaCategorie = Categoria.createCriteria().list() {
				eq("tipoOggetto", Categoria.TIPO_OGGETTO_PROPOSTA_DELIBERA)
				or {
					eq("valido", true)
					if (propostaDelibera?.categoria?.id) {
						eq("id", propostaDelibera.categoria.id)
					}
				}
				order ("sequenza", "asc")
				order ("codice","asc")
			}.toDTO()
			BindUtils.postNotifyChange(null, null, this, "listaCategorie")
		}

		BindUtils.postNotifyChange(null, null, this, "categoriaAbilitata")
	}

	public void caricaGestioneGruppiPolitici(){
		mostraGestioneGruppiPolitici = false
		if (propostaDelibera.tipologia) {
			if (gestioneGruppiPolitici && propostaDelibera.tipologia.gestioneGruppiPolitici) {
				mostraGestioneGruppiPolitici = true
			}
		}
		BindUtils.postNotifyChange(null, null, this, "mostraGestioneGruppiPolitici")
	}

	//visualizza o meno la lista degli impatti generazionali
	private void caricaListaImpattoGenerazionale() {
		mostraImpattoGenerazionale = false
		if (propostaDelibera.tipologia) {
			if (impattoGenerazionaleAbilitato && propostaDelibera.tipologia.impattoGenerazionale) {
				mostraImpattoGenerazionale = true

				//carico la lista degli impatti generazionali
				PagedResultList lista = ImpattoGenerazionale.createCriteria().list(max: 100, offset: 0) {
					eq ("valido",true)
					eq ("delibera", true)
					order ("titolo", "asc")
					order ("descrizione", "asc")
				}
				listaImpattoGenerazionale = lista.toDTO()

				if (propostaDelibera.impattoGenerazionale?.id > 0) {
					impattoGenerazionale = ImpattoGenerazionale.get(propostaDelibera.impattoGenerazionale.id).toDTO()
				}
				BindUtils.postNotifyChange(null, null, this, "listaImpattoGenerazionale")
			}
		}
		BindUtils.postNotifyChange(null, null, this, "mostraImpattoGenerazionale")
	}

	/*
	 * Carica la lista dei modelliTestoPredefiniti
	 */
	private void caricaListaModelliTestoPredefiniti () {
		listaModelliTestoPredefiniti = propostaDeliberaDTOService.getListaModelliTestoAbilitati (propostaDelibera.tipologia.id, utente)

		if(Impostazioni.MODELLI_TESTO_UNITA_PROPONENTE.abilitato && propostaDelibera.soggetti) {
			PropostaDelibera domainObject = propostaDelibera.getDomainObject()
			So4UnitaPubb uo = domainObject.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4
			if (uo) {
				List<GestioneTestiModelloDTO> toRemove= new ArrayList<GestioneTestiModelloDTO>()
				for (GestioneTestiModelloDTO gestioneTestiModelloDTO : listaModelliTestoPredefiniti) {
					List<GestioneTestiModelloCompetenza> lista = GestioneTestiModelloCompetenza.createCriteria().list() {
						eq("gestioneTestiModello.id", gestioneTestiModelloDTO.id)
						createAlias ("unitaSo4", "unita", CriteriaSpecification.LEFT_JOIN)
						ne("unita.progr",uo.progr)
					}
					if(lista!=null && lista.size()>0)
						toRemove.add(gestioneTestiModelloDTO)
				}
				for(GestioneTestiModelloDTO elem: toRemove){
					listaModelliTestoPredefiniti.removeElement(elem)
				}
			}
		}

		if (propostaDelibera.modelloTesto == null) {
			propostaDelibera.modelloTesto = propostaDelibera.tipologia?.modelloTesto?.getDomainObject()?.toDTO()
		}
		BindUtils.postNotifyChange(null, null, this, "listaModelliTestoPredefiniti")
	}

	/*
	 * Gestione dello storico:
	 */
	private void caricaStorico () {
		storico = propostaDeliberaDTOService.caricaStorico(propostaDelibera);

		BindUtils.postNotifyChange(null, null, this, "storico")
	}


	/*
	 * Gestione della contabilità
	 */
	@Command
	void onAggiornaContabilita () {
		aggiornaContabilita(propostaDelibera.domainObject)
	}

	void aggiornaContabilita (PropostaDelibera p) {
		if (p != null) {
			integrazioneContabilita.aggiornaMaschera (p, (competenze.modifica && !(campiProtetti.CONTABILITA) && p.tipologia.scritturaMovimentiContabili))
		}
	}

	/*
	 * Gestisce le note di trasmissioni
	 */
	private void aggiornaNoteTrasmissionePrecedenti () {
		def result = documentoDTOService.getNoteTrasmissionePrecedenti (propostaDelibera)
		noteTrasmissionePrecedenti 		 = result.noteTrasmissionePrecedenti
		attorePrecedente	 			 = result.attorePrecedente
		mostraNoteTrasmissionePrecedenti = result.mostraNoteTrasmissionePrecedenti

		BindUtils.postNotifyChange(null, null, this, "mostraNoteTrasmissionePrecedenti")
		BindUtils.postNotifyChange(null, null, this, "noteTrasmissionePrecedenti")
		BindUtils.postNotifyChange(null, null, this, "attorePrecedente")
	}

	/*
	 * Gestione Destinatari
	 */
	@Command onAggiungiDestinatariInterni () {
		Window w = Executions.createComponents("/commons/popupSceltaDestinatariInterni.zul", self, [destinatari: listaDestinatariInterni*.destinatario])
		w.onClose { event ->
			PropostaDeliberaDTO d = destinatarioNotificaDTOService.salvaDestinatariInterni (propostaDelibera, event.data)
			propostaDelibera.version = d.version;
			refreshListaDestinatariInterni()
		}
		w.doModal()
	}

	@Command onAggiungiDestinatariEsterni () {
		Window w = Executions.createComponents("/commons/popupSceltaDestinatariEsterniEsistenti.zul", self, [destinatari: listaDestinatariEsterni])
		w.onClose { event ->
			if (event.data == null)
				return null;

			PropostaDeliberaDTO d = destinatarioNotificaDTOService.aggiungiDestinatarioEsterno (propostaDelibera, event.data)
			propostaDelibera.version = d.version;
			refreshListaDestinatariEsterni()
		}
		w.doModal()
	}

	@Command onEliminaDestinatarioNotifica (@ContextParam(ContextType.TRIGGER_EVENT) Event event
										  , @BindingParam("destinatario") def destinatario
										  , @BindingParam("tipo") String tipo) {
		Messagebox.show("Eliminare il destinatario selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						if (tipo == "E") {
							destinatarioNotificaDTOService.eliminaDestinatarioNotifica (destinatario)
							PropostaDeliberaViewModel.this.refreshListaDestinatariEsterni()
						} else if (tipo == "I") {
							destinatarioNotificaDTOService.eliminaDestinatarioNotifica (destinatario.destinatario)
							PropostaDeliberaViewModel.this.refreshListaDestinatariInterni()
						} else if (tipo == "P3") {
							destinatarioNotificaDTOService.eliminaDestinatarioNotifica(destinatario)
							PropostaDeliberaViewModel.this.refreshListaDestinatariPiTre()
						}

						propostaDelibera.version = propostaDelibera.domainObject.version;
					}
				}
			}
		)
	}

	private void refreshListaDestinatariInterni () {
		listaDestinatariInterni = destinatarioNotificaDTOService.getListaDestinatariInterni(propostaDelibera)
		BindUtils.postNotifyChange(null, null, this, "listaDestinatariInterni")
	}

	private void refreshListaDestinatariEsterni () {
		listaDestinatariEsterni = destinatarioNotificaDTOService.getListaDestinatariEsterni(propostaDelibera)
		BindUtils.postNotifyChange(null, null, this, "listaDestinatariEsterni")
	}

	/*
	 *  Gestione Visti
	 */
	private void refreshListaVisti () {
		def visti = VistoParere.createCriteria().list {
			eq ("propostaDelibera.id", propostaDelibera.id)

			if (mostraSoloVistiValidi) {
				eq ("valido", true)
			} else {
				or {
					eq ("valido", true)
					ne ("esito", EsitoVisto.DA_VALUTARE)
				}
			}

			tipologia {
				order("sequenzaStampaUnica", "asc")
				order("codice", "asc")
			}
			order("valido", "asc")
			order("dateCreated", "asc")

			fetchMode ("tipologia",  FetchMode.JOIN)
			fetchMode ("unitaSo4",   FetchMode.JOIN)
			fetchMode ("firmatario", FetchMode.JOIN)
		}
		
		// alla fine, per ogni visto, controllo di avere le competenze in modifica:
		listaVisti = []
		for (VistoParere visto : visti) {
			VistoParereDTO dto = visto.toDTO();
			dto.competenzeInModifica = gestoreCompetenze.getCompetenze(visto);
			listaVisti << dto
		}
		
		BindUtils.postNotifyChange(null, null, this, "listaVisti")
	}

	@Command onModificaVistoParere (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("nuovo") boolean nuovo, @BindingParam("selected") def selected) {
		if (!nuovo && selected == null)
			return;

		Window w = Executions.createComponents ("/atti/documenti/parere.zul", self, [id: nuovo?-1:selected.id, documento: propostaDelibera, tipodoc : "propostaDelibera", competenzeLettura: forzaCompetenzeLettura])
		w.onClose {
			// potrei aver aggiornato la determina, quindi ne riprendo i numeri di versione e idDocumentoEsterno.
			PropostaDelibera pd = propostaDelibera.domainObject;
			propostaDelibera.version = pd.version;
			propostaDelibera.idDocumentoEsterno = pd.idDocumentoEsterno;
			refreshListaVisti()
		}
		w.doModal()
	}

	@Command onEliminaVistoParere (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("visto") VistoParereDTO visto) {
		Messagebox.show("Eliminare il parere selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						vistoParereDTOService.elimina (visto)
						propostaDelibera.version = propostaDelibera.domainObject.version;
						PropostaDeliberaViewModel.this.refreshListaVisti()
					}
				}
			}
		)
	}

	@NotifyChange("mostraSoloVistiValidi")
	@Command onMostraVistiValidi () {
		mostraSoloVistiValidi = !mostraSoloVistiValidi;
		refreshListaVisti()
	}

	/*
	 * 	Metodi per il calcolo dei Soggetti della propostaDelibera
	 */
	@Command onSceltaSoggetto (@BindingParam("tipoSoggetto") String tipoSoggetto, @BindingParam("categoriaSoggetto") String categoriaSoggetto) {
		long idCaratteristica = -1;
		if (propostaDelibera.fuoriSacco) {
			idCaratteristica = propostaDelibera.tipologia.caratteristicaTipologiaFuoriSacco.id;
		} else {
			idCaratteristica = propostaDelibera.tipologia.caratteristicaTipologia.id;
		}

		Window w = Executions.createComponents ("/atti/documenti/popupSceltaSoggetto.zul", self, [idCaratteristicaTipologia: idCaratteristica
			, documento: propostaDelibera
			, soggetti: soggetti
			, tipoSoggetto: tipoSoggetto
			, categoriaSoggetto:categoriaSoggetto])
		w.onClose { event ->
			// se ho annullato la modifica, non faccio niente:
			if (event.data == null)
				return;

			// altrimenti aggiorno i soggetti.
			BindUtils.postNotifyChange(null, null, this, "soggetti");
            self.invalidate()
		}
		w.doModal()
	}

	private void calcolaSoggetti (String tipoSoggetto) {
		if (propostaDelibera.fuoriSacco) {
			caratteristicaTipologiaService.aggiornaSoggetti(propostaDelibera.tipologia.caratteristicaTipologiaFuoriSacco.id, propostaDelibera.domainObject, soggetti, tipoSoggetto);
		} else {
			caratteristicaTipologiaService.aggiornaSoggetti(propostaDelibera.tipologia.caratteristicaTipologia.id, propostaDelibera.domainObject, soggetti, tipoSoggetto);
		}

		BindUtils.postNotifyChange(null, null, this, "soggetti")
	}

	/*
	 * Gestione allegati
	 */
	@Command onModificaAllegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("nuovo") boolean nuovo, @BindingParam("selected") def selected) {
		// succede quando un utente fa "doppio click" sulla tabella vuota.
		if (!nuovo && selected == null) {
			return;
		}
		
		Window w = Executions.createComponents("/atti/documenti/allegato.zul", self, [id: (nuovo?-1:selected.id), documento: propostaDelibera, competenzeLettura: forzaCompetenzeLettura])
		w.onClose {
			if (!(propostaDelibera.idDocumentoEsterno > 0)) {
				// potrei aver aggiornato la proposta, quindi ne riprendo i numeri di versione e idDocumentoEsterno.
				PropostaDelibera d = propostaDelibera.domainObject;
				propostaDelibera.version = d.version;
				propostaDelibera.idDocumentoEsterno = d.idDocumentoEsterno;
			}
			refreshListaAllegati ()
		}
		w.doModal()
	}

	@Command onEliminaAllegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("allegato") AllegatoDTO allegato) {
		Messagebox.show("Eliminare l'allegato selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						allegatoDTOService.elimina (allegato, propostaDelibera)
						propostaDelibera.version = propostaDelibera.domainObject.version;
						PropostaDeliberaViewModel.this.refreshListaAllegati()
					}
				}
			}
		)
	}

	private void refreshListaAllegati () {
		listaAllegati = Allegato.createCriteria().list {
			eq("propostaDelibera.id", propostaDelibera.id)
			order ("sequenza", "asc")
			order ("titolo",   "asc")
		}.toDTO(['fileAllegati'])
		BindUtils.postNotifyChange(null, null, this, "listaAllegati")
	}

	/*
	 * Gestione Gruppi Politici
	 */
	@Command onAggiungiFirmatarioGruppoPolitico (@BindingParam("nuovo") boolean nuovo, @BindingParam("selected") def selected) {
		Window w = Executions.createComponents("/dizionari/odg/gruppoPoliticoDocumentoPopUp.zul", self, [id: (nuovo?-1:selected.id), documento: propostaDelibera, competenzeLettura: forzaCompetenzeLettura])
		w.onClose { event ->
			if (event?.data != null) {
				List<GruppoPoliticoComponenteDTO> lista = event.data
				for(GruppoPoliticoComponenteDTO gruppoPoliticoComponente :lista){
					GruppoPoliticoDocumento gruppoPoliticoDocumento= new GruppoPoliticoDocumento()
					gruppoPoliticoDocumento.gruppoPolitico=gruppoPoliticoComponente.gruppoPolitico.domainObject
					gruppoPoliticoDocumento.titolo=gruppoPoliticoComponente.gruppoPolitico.titolo
					gruppoPoliticoDocumento.descrizione=gruppoPoliticoComponente.gruppoPolitico.descrizione
					gruppoPoliticoDocumento.componente=gruppoPoliticoComponente.componente.domainObject
					gruppoPoliticoDocumentoService.salva(gruppoPoliticoDocumento, propostaDelibera.domainObject)
				}
				refreshListaFirmatariGruppoPolitico ()
			}
		}
		w.doModal()
	}

	@Command onEliminaFirmatarioGruppoPolitico (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("firmatarioGruppoPolitico") GruppoPoliticoDocumentoDTO firmatarioGruppoPolitico) {
		Messagebox.show("Eliminare il firmatario selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							gruppoPoliticoDocumentoDTOService.elimina (firmatarioGruppoPolitico, propostaDelibera)
							listaFirmatariGruppoPolitico.remove(firmatarioGruppoPolitico)
							propostaDelibera.version = propostaDelibera.domainObject.version;
							PropostaDeliberaViewModel.this.refreshListaFirmatariGruppoPolitico()
						}
					}
				}
		)
	}

	private void refreshListaFirmatariGruppoPolitico () {
		caricaGestioneGruppiPolitici()
		if(mostraGestioneGruppiPolitici) {
			listaFirmatariGruppoPolitico = GruppoPoliticoDocumento.createCriteria().list {
				fetchMode("componente", FetchMode.JOIN)
				eq("propostaDelibera.id", propostaDelibera.id)
				order("id", "asc")
//				componente {
//					order("cognome", "asc")
//					order("nome", "asc")
//				}
			}.toDTO()
			BindUtils.postNotifyChange(null, null, this, "listaFirmatariGruppoPolitico")
		}
	}



	/*
	 * Gestione DelibereCollegate
	 */
	@Command onAggiungiDocumentoCollegato () {
		Window w = Executions.createComponents("/commons/popupAnnullamentoIntegrazione.zul", self, [tipoDocumento : PropostaDelibera.TIPO_OGGETTO])
		w.onClose { event ->
			if (event != null && event?.data != null) {
				PropostaDeliberaDTO d = documentoCollegatoDTOService.aggiungiDocumentiCollegati (propostaDelibera, event.data)
				propostaDelibera.version = d.version
				refreshListaDocumentiCollegati()
			}
		}
		w.doModal()
	}

	private void refreshListaDocumentiCollegati () {
		listaDocumentiCollegati = documentoCollegatoDTOService.getListaDocumentiCollegati(propostaDelibera.domainObject)
		BindUtils.postNotifyChange(null, null, this, "listaDocumentiCollegati")
	}

	@Command
	void onEliminaDocumentoCollegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("documentoCollegato") def documentoCollegato) {
		Messagebox.show("Eliminare il collegamento selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						documentoCollegatoDTOService.eliminaDocumentoCollegato (propostaDelibera, documentoCollegato.id)
						propostaDelibera.version = propostaDelibera.domainObject.version;
						PropostaDeliberaViewModel.this.refreshListaDocumentiCollegati()
					}
				}
			}
		)
	}

	@Command
	void apriDocumentoCollegato (@BindingParam("documentoCollegato") DocumentoCollegatoDTO documentoCollegato) {
		documentoCollegatoDTOService.apriDocumento(documentoCollegato)
	}

	/*
	 * Gestione dei dati di protocollo
	 */
	@NotifyChange('propostaDelibera')
	@Command apriClassificazione() {
		String zul = protocolloEsterno.getZulClassifica()
		Window w = Executions.createComponents(zul, self, [codiceUoProponente: soggetti[TipoSoggetto.UO_PROPONENTE].unita.codice])
		w.onClose { event ->
			if (event.data) {
				if (event.data.codice!= propostaDelibera.classificaCodice) {
					propostaDelibera.fascicoloAnno 		= 0
					propostaDelibera.fascicoloNumero 	= null
					propostaDelibera.fascicoloOggetto 	= null
				}
				propostaDelibera.classificaCodice 		= event.data.codice
				propostaDelibera.classificaDescrizione 	= event.data.descrizione
				propostaDelibera.classificaDal 			= event.data.dal
				BindUtils.postNotifyChange(null, null, this, "propostaDelibera")
			}
		}
		w.doModal()
	}

	@NotifyChange('propostaDelibera')
	@Command apriFascicoli() {
		String zul = protocolloEsterno.getZulFascicolo()
		Window w = null
		if(isIntegrazioneProtocolloPiTre) {
			So4UnitaPubb unitaProponente= soggetti[TipoSoggetto.UO_PROPONENTE].unita?.domainObject
			if(unitaProponente) {
				w = Executions.createComponents(zul, self,  [unitaProponente: unitaProponente,tipologia: String.valueOf(propostaDelibera.tipologia.id),sceltaUnitaProponente:false,utenteRedattore :soggetti[TipoSoggetto.REDATTORE].utente?.domainObject])
			}else{
				throw new AttiRuntimeException("Attenzione: è necessario selezionare una unità proponente.")
			}
		}  else {
			w = Executions.createComponents(zul, self, [classificaCodice: propostaDelibera.classificaCodice, classificaDescrizione: propostaDelibera.classificaDescrizione, classificaDal: propostaDelibera.classificaDal, codiceUoProponente: soggetti[TipoSoggetto.UO_PROPONENTE].unita.codice])
		}
		//Window w = Executions.createComponents(zul, self, [classificaCodice: propostaDelibera.classificaCodice, classificaDescrizione: propostaDelibera.classificaDescrizione, classificaDal: propostaDelibera.classificaDal, codiceUoProponente: soggetti[TipoSoggetto.UO_PROPONENTE].unita.codice])
		w.onClose { event ->
			if (event.data) {
				if(isIntegrazioneProtocolloPiTre){
					propostaDelibera.fascicoloNumero = event.data.numero
					propostaDelibera.fascicoloAnno = ProtocolloPiTre.estraiAnnoDaFascicoloPiTre(event.data.numero)
					propostaDelibera.fascicoloOggetto = event.data.oggetto
					propostaDelibera.idFascicoloEsterno = event.data.sub
				} else {
					// se ho cambiato la classificazione, la riaggiorno
					if (event.data.classifica.codice != propostaDelibera.classificaCodice) {
						propostaDelibera.classificaCodice 		= event.data.classifica.codice
						propostaDelibera.classificaDescrizione = event.data.classifica.descrizione
						propostaDelibera.classificaDal 		= event.data.classifica.dal
					}
					propostaDelibera.fascicoloAnno  = event.data.anno
					propostaDelibera.fascicoloNumero = event.data.numero
					propostaDelibera.fascicoloOggetto = event.data.oggetto
				}

				BindUtils.postNotifyChange(null, null, this, "propostaDelibera")
			}
		}
		w.doModal()
	}

	/**
	 * Quando l'utente seleziona il tab dei riferimenti, controllo che il documento sia in casa di vetro:
	 */
	@NotifyChange("urlCasaDiVetro")
	@Command onApriTabRiferimenti () {
		// aggiorno l'url del documento in casa di vetro:
		urlCasaDiVetro = casaDiVetroService.getUrlDocumentoSePresente(propostaDelibera);
	}

	/*
	 * Apertura della delibera:
	 */
	@Command onApriAtto () {
		Delibera delibera = Delibera.findWhere(['propostaDelibera.id': propostaDelibera.id])
		Window w = Executions.createComponents ("/atti/documenti/delibera.zul", self, [id: delibera.id])
		w.doModal()
	}

	/*
	 *  Gestione Chiusura Maschera
	 */
	@Command onChiudi () {
		// se devo rilasciare il lock sul testo, lo rilascio.
		gestioneTesti.uploadEUnlockTesto(propostaDelibera, lockPermanente)
		tokenIntegrazioneService.unlockDocumento(propostaDelibera.domainObject)
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	/*
	 *  Presa Visione
	 */
	@Command onPresaVisione () {
		notificheService.eliminaNotifica(propostaDelibera.domainObject, springSecurityService.currentUser)
		isNotificaPresente = false
		BindUtils.postNotifyChange(null, null, this, "isNotificaPresente")
		onChiudi()
	}

	/*
	 * Implementazione dei Metodi per AbstractViewModel
	 */
	DTO<PropostaDelibera> getDocumentoDTO () {
		return propostaDelibera
	}

	@Override
	WkfCfgIter getCfgIter() {
		if (propostaDelibera?.fuoriSacco) {
			return WkfCfgIter.getIterIstanziabile(propostaDelibera?.tipologia?.progressivoCfgIterFuoriSacco?:(long)-1).get()
		} else {
			return WkfCfgIter.getIterIstanziabile(propostaDelibera?.tipologia?.progressivoCfgIter?:(long)-1).get()
		}
	}

	PropostaDelibera getDocumentoIterabile (boolean controllaConcorrenza) {
		if (propostaDelibera.id > 0) {
			PropostaDelibera domainObject = propostaDelibera.getDomainObject()
			if (controllaConcorrenza && propostaDelibera?.version >= 0 && domainObject.version != propostaDelibera?.version) {
				throw new AttiRuntimeException("Attenzione: un altro utente ha modificato il documento su cui si sta lavorando. Impossibile continuare. \n (dto.version=${propostaDelibera.version}!=domain.version=${domainObject.version})")
			}

			return domainObject
		}

		return new PropostaDelibera()
	}

	Collection<String> validaMaschera () {
		def messaggi = []

		if (propostaDelibera.oggetto == null || propostaDelibera.oggetto.trim().length() == 0) {
			messaggi << "L'Oggetto è obbligatorio."
		}
		else {
			propostaDelibera.oggetto = AttiUtils.replaceCaratteriSpeciali(propostaDelibera.oggetto)
		}

		if (propostaDelibera.oggetto != null && !AttiUtils.controllaCharset(propostaDelibera.oggetto)) {
			messaggi << "L'Oggetto contiene dei caratteri non supportati."
		}

		if (propostaDelibera.oggetto != null && propostaDelibera.oggetto.size() > Impostazioni.LUNGHEZZA_OGGETTO.valoreInt) {
			messaggi << "La lunghezza dell'oggetto inserito è superiore a " + Impostazioni.LUNGHEZZA_OGGETTO.valore + " caratteri"
		}

		if (propostaDelibera.tipologia.delegaObbligatoria == true && propostaDelibera.delega == null) {
			messaggi << "È obbligatorio selezionare una delega per questa tipologia di proposta di Delibera"
		}

		boolean funzionarioPrevisto = propostaDelibera.tipologia.caratteristicaTipologia.caratteristicheTipiSoggetto?.any {
			it.tipoSoggetto.codice == TipoSoggetto.FUNZIONARIO
		}

		if (funzionarioPrevisto && propostaDelibera.controlloFunzionario && soggetti[TipoSoggetto.FUNZIONARIO]?.utente == null) {
			messaggi << Labels.getLabel("message.propostaDelibera.funzionario")
		}

		if (propostaDelibera.categoria == null && propostaDelibera.tipologia.categoriaObbligatoria == true) {
			messaggi << Labels.getLabel("message.propostaDelibera.categoria")
		}

		if (propostaDelibera.priorita > 0 && (controllaPriorita || isMotivazionePresente) && (propostaDelibera.motivazione == null || propostaDelibera.motivazione?.isEmpty())) {
			messaggi << "Motivazione obbligatoria per documenti con Priorità"
		}

		String doppiaFirma = TipoDatoAggiuntivo.getValore(propostaDelibera.datiAggiuntivi, TipoDatoAggiuntivo.ASSENZA_DOPPIA_FIRMA)
		String motivazioni = TipoDatoAggiuntivo.getValore(propostaDelibera.datiAggiuntivi, TipoDatoAggiuntivo.MOTIVAZIONE_ASSENZA_DOPPIA_FIRMA)
		if (TipoDatoAggiuntivo.isAbilitato(TipoDatoAggiuntivo.ASSENZA_DOPPIA_FIRMA) && doppiaFirma == 'Y' && !(motivazioni?.length() > 0)) {
			messaggi << Labels.getLabel("message.motivazioniDoppiaFirmaMancante")
		}

		if (Impostazioni.ESEGUIBIILITA_IMMEDIATA_MOTIVAZIONI.abilitato && propostaDelibera.eseguibilitaImmediata && (propostaDelibera.motivazioniEseguibilita == null || propostaDelibera.motivazioniEseguibilita?.isEmpty())) {
			messaggi << "Motivazione obbligatoria per documenti con Eseguibilità Immediata";
		}

		if (propostaDelibera.tipologia?.caratteristicaTipologia?.caratteristicheTipiSoggetto?.tipoSoggetto*.codice?.contains(TipoSoggetto.INCARICATO) &&
			propostaDelibera.tipologia.incaricatoObbligatorio && soggetti[TipoSoggetto.INCARICATO]?.utente == null){
			messaggi << Labels.getLabel("message.propostaDelibera.incaricato");
		}

		if (mostraImportoTotale && propostaDelibera.tipologia.importoTotaleObbligatorio && propostaDelibera.importoTotale == null) {
			messaggi << "Importo Totale obbligatorio"
		}

		if (messaggi.size() > 0) {
			messaggi.add(0, "Impossibile continuare:")
		}

		return messaggi
	}

	void aggiornaDocumentoIterabile (PropostaDelibera d) {
		// salvo e sblocco il testo
		gestioneTesti.uploadEUnlockTesto (d);

		d.oggetto   = propostaDelibera.oggetto.toUpperCase()
		d.tipologia	= propostaDelibera.tipologia?.domainObject

		// se ho modificato la uo proponente della proposta, devo aggiornarla anche per gli eventuali pareri
		if (soggetti[TipoSoggetto.UO_PROPONENTE] != null && d.id > 0) {
			vistoParereService.allineaUnitaDocumentoPrincipale(d, soggetti[TipoSoggetto.UO_PROPONENTE].unita?.domainObject)
		}

		// se ho modificato il dirigente della proposta, devo aggiornare anche il dirigente dell'eventuale parere tecnico (se presente)
        if (soggetti[TipoSoggetto.DIRIGENTE] != null && d.id > 0) {
            vistoParereService.allineaFirmatarioDocumentoPrincipale(d, soggetti[TipoSoggetto.DIRIGENTE].utente?.domainObject)
        }

		d.daInviareCorteConti 	= propostaDelibera.daInviareCorteConti
		d.controlloFunzionario  = propostaDelibera.controlloFunzionario

		d.statoOdg				= propostaDelibera.statoOdg
		d.commissione 			= propostaDelibera.commissione?.domainObject;
		d.delega				= propostaDelibera.delega?.domainObject
		d.indirizzo 			= propostaDelibera.indirizzo?.domainObject
		d.modelloTesto 			= propostaDelibera.modelloTesto?.domainObject
		d.categoria				= propostaDelibera.categoria?.domainObject

		d.fascicoloAnno			= propostaDelibera.fascicoloAnno
		d.fascicoloNumero		= propostaDelibera.fascicoloNumero
		d.fascicoloOggetto		= propostaDelibera.fascicoloOggetto
		d.idFascicoloEsterno	= propostaDelibera.idFascicoloEsterno
		d.classificaCodice		= propostaDelibera.classificaCodice
		d.classificaDal			= propostaDelibera.classificaDal
		d.classificaDescrizione	= propostaDelibera.classificaDescrizione
		d.fuoriSacco			= propostaDelibera.fuoriSacco
		d.eseguibilitaImmediata	= propostaDelibera.eseguibilitaImmediata
		d.motivazioniEseguibilita = propostaDelibera.motivazioniEseguibilita
		d.dataProposta 			= propostaDelibera.dataProposta
		d.note					= propostaDelibera.note
		d.noteTrasmissione		= propostaDelibera.noteTrasmissione
		d.noteContabili 		= propostaDelibera.noteContabili
		d.noteCommissione 		= propostaDelibera.noteCommissione

		d.controlloFunzionario 	= propostaDelibera.controlloFunzionario
		d.giorniPubblicazione  	= propostaDelibera.giorniPubblicazione
		d.pubblicaRevoca	   	= propostaDelibera.pubblicaRevoca
		d.riservato				= propostaDelibera.riservato
		d.parereRevisoriConti	= propostaDelibera.parereRevisoriConti
		d.motivazione			= propostaDelibera.motivazione
		d.priorita				= propostaDelibera.priorita

		d.dataScadenza 			= propostaDelibera?.dataScadenza

        d.controllaDestinatari = propostaDelibera?.controllaDestinatari

		d.oggettoRicorrente		= propostaDelibera?.oggettoRicorrente?.domainObject
		d.dataMinimaPubblicazione = propostaDelibera?.dataMinimaPubblicazione

		d.inEvidenza 		= propostaDelibera?.inEvidenza
		d.dataInEvidenzaDa	= propostaDelibera?.dataInEvidenzaDa
		d.dataInEvidenzaA	= propostaDelibera?.dataInEvidenzaA
		d.importo 			= propostaDelibera?.importo
		d.importoTotale 	= propostaDelibera?.importoTotale

		//GruppiPolitici
		d.pubblicaCasaDiVetro = propostaDelibera.pubblicaCasaDiVetro
		d.annoProtocolloRichiesta = propostaDelibera.annoProtocolloRichiesta
		d.numeroProtocolloRichiesta = propostaDelibera.numeroProtocolloRichiesta


		if(impattoGenerazionale?.id>0) {
			propostaDelibera.impattoGenerazionale=impattoGenerazionale
			d.impattoGenerazionale = propostaDelibera?.impattoGenerazionale.domainObject
		} else
			d.impattoGenerazionale = null

		documentoService.controllaOggettoRicorrente(d)

		caratteristicaTipologiaService.salvaSoggettiModificati(d, soggetti)
		documentoDTOService.salvaDatiAggiuntivi(d, propostaDelibera)
        budgetDTOService.salvaBudget(listaBudget)
	}

	private void abilitaImportoTotale () {
		mostraImportoTotale = Impostazioni.GESTIONE_IMPORTO_TOTALE.abilitato
		if (mostraImportoTotale) {
			if (propostaDelibera.tipologia) {
				mostraImportoTotale = propostaDelibera.tipologia.visImportoTotale
			}
			else {
				mostraImportoTotale = false
			}
			BindUtils.postNotifyChange(null, null, this, "mostraImportoTotale")
		}
	}

	void aggiornaMaschera (PropostaDelibera d) {
		// per prima cosa controllo che l'utente abbia le competenze in lettura sul documento
		competenze = gestoreCompetenze.getCompetenze(d, true)
		competenze.lettura = competenze.lettura ?: forzaCompetenzeLettura
		if (!competenze.lettura) {
			propostaDelibera = null
			throw new AttiRuntimeException("L'utente ${springSecurityService.principal.username} non ha i diritti di lettura sulla propostaDelibera con id ${d.id}")
		}

		if (hasDelegaValida()) {
			if (d.statoFirma == StatoFirma.DA_FIRMARE) {
				gestoreCompetenze.assegnaCompetenze(d, WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), new Attore(utenteAd4: springSecurityService.principal.soggetto.utenteAd4), true, true, false, null)
				competenze.modifica = true
			} else {
				// il documento è stato rimandato indietro: il delegante non ha più diritti di modifica
				competenze.modifica = false
				forzaCompetenzeLettura=false
			}
		}

		if (d.statoFirma == StatoFirma.IN_FIRMA || d.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE) {
			competenze.modifica 	 = false
			competenze.cancellazione = false
		}

        isLocked = tokenIntegrazioneService.isLocked(d)

		//il campo tipologia non si deve abilitare
		riservatoModificabile = d.tipologia.gestioneRiservato

		// verifico che l'utente possa gestire il riservato:
		if (riservatoModificabile) {
			riservatoModificabile = (!d.riservato || gestoreCompetenze.utenteCorrenteVedeRiservato(d));
		}

		//se l'utente non può vedere i riservati e la tipologia prevede questo controllo, allora non potrà visualizzare il folder contabilità
		if ((!riservatoModificabile) && (d.tipologia.contabilitaCheckRiservato)) {
			visContabilita = false
		}
		else {
			visContabilita = true
		}

		// calcolo il titolo
		titolo = (d.numeroProposta > 0 ? "PropostaDelibera n. ${d.numeroProposta}/${d.annoProposta}" : "Proposta di PropostaDelibera"+(d.numeroProposta > 0 ? " n. ${d.numeroProposta}/${d.annoProposta}" : ""))

		// calcolo la posizione del flusso
		posizioneFlusso = d.iter?.stepCorrente?.cfgStep?.nome

		// calcolo i campi che devo proteggere in lettura
		campiProtetti = CampiDocumento.getMappaCampi(d.campiProtetti)

		// prendo il DTO con tutti i campi necessari
		propostaDelibera = d.toDTO(["tipologia.caratteristicaTipologia", "tipologia.caratteristicaTipologia.caratteristicheTipiSoggetto.tipoSoggetto.codice", "tipologia.caratteristicaTipologiaFuoriSacco", "delega.assessore.utenteAd4", "categoria", "modelloTesto", "iter.stepCorrente.cfgStep", "oggettoSeduta.seduta.commissione", "oggettoSeduta.esito", "testo", "tipologia.oggettiRicorrenti", "oggettoRicorrente", "datiAggiuntivi"])

		// aggiorno i dati del lock sul testo:
		testoLockato = (gestioneTestiService.getDettaglioLock(AttiGestioneTesti.creaIdRiferimento(d)) != null)

		// commissione:
		propostaDelibera.commissione = (d.commissione?:(d.tipologia?.commissione)).toDTO()

		// carico gli estremi della delibera, se presente:
		Delibera deli = d.getAtto();
		if (deli != null) {
			estremiDelibera = deli.getEstremiAtto();
			isProtocollato = (deli.numeroProtocollo>0)
		}

		// verifico presenza notifiche
		isNotificaPresente = notificheService.isNotificaPresente(propostaDelibera.domainObject, springSecurityService.currentUser)

		isMotivazionePresente = propostaDelibera.motivazione != null && !propostaDelibera.motivazione.isEmpty()

		//GruppiPolitici
		propostaDelibera.pubblicaCasaDiVetro = d.pubblicaCasaDiVetro
		propostaDelibera.annoProtocolloRichiesta = d.annoProtocolloRichiesta
		propostaDelibera.numeroProtocolloRichiesta = d.numeroProtocolloRichiesta

		// carico la lista di allegati:
		refreshListaAllegati ()

		//carico la lista dei Firmatari Politici
		refreshListaFirmatariGruppoPolitico()

		// carico la lista dei visti:
		refreshListaVisti()

		// carica la lista dei destinatari:
		refreshListaDestinatariInterni()
		refreshListaDestinatariEsterni()
		refreshListaDestinatariPiTre()

		// carica lista delle determine collegate
		refreshListaDocumentiCollegati()

		// carica lista dei modelli testo predefiniti
		caricaListaModelliTestoPredefiniti()

		// aggiorno le note di trasmissioni dello step precedente
		aggiornaNoteTrasmissionePrecedenti()

		// carico lo storico del documento
		caricaStorico()

		// calcolo i vari soggetti della propostaDelibera
		soggetti = caratteristicaTipologiaService.calcolaSoggettiDto(d)

		// controllo se la proposta è firmata o no
		propostaFirmata = (d?.testo?.isModificabile())

		// gestione contabilità
		contabilitaAbilitata = integrazioneContabilita.isAbilitata(d)
        if (contabilitaAbilitata) {
			if (tipoIntegrazioneContabilita == "integrazioneContabilitaAtsPavia") {
				isIntegrazioneContabilitaAtsPavia = true
			}
            conDocumentiContabili = integrazioneContabilita.isConDocumentiContabili(d)
            zulContabilita = integrazioneContabilita.getZul(d)
            aggiornaContabilita(d)

            BindUtils.postNotifyChange(null, null, this, "conDocumentiContabili")
            BindUtils.postNotifyChange(null, null, this, "zulContabilita")
        }
        mostraNote 	  = regolaCampoService.isBloccoVisibile(d, d.tipoOggetto, "NOTE")
        mostraStorico = regolaCampoService.isBloccoVisibile(d, d.tipoOggetto, "STORICO")

		isEstrattoPresente = datiAggiuntiviService.isDatoPresente(d, TipoDatoAggiuntivo.ESTRATTO)

        caricaListaBudget()

		//se ho il testo già memorizzato allora non devo visualizzare la combo di scelta dei modelli testi
		if (d.testo?.id) {
			mostraComboModelloTesti = false
		}
		else {
			mostraComboModelloTesti = true
		}

		abilitaImportoTotale()

        BindUtils.postNotifyChange(null, null, this, "propostaFirmata")
		BindUtils.postNotifyChange(null, null, this, "campiProtetti")
		BindUtils.postNotifyChange(null, null, this, "propostaDelibera")
		BindUtils.postNotifyChange(null, null, this, "competenze")
		BindUtils.postNotifyChange(null, null, this, "posizioneFlusso")
		BindUtils.postNotifyChange(null, null, this, "titolo")
		BindUtils.postNotifyChange(null, null, this, "listaModelliTestoPredefiniti")
		BindUtils.postNotifyChange(null, null, this, "testoLockato")
		BindUtils.postNotifyChange(null, null, this, "soggetti")
		BindUtils.postNotifyChange(null, null, this, "contabilitaAbilitata")
        BindUtils.postNotifyChange(null, null, this, "listaBudget")
		BindUtils.postNotifyChange(null, null, this, "mostraComboModelloTesti")
	}

	@Command onAggiungiFirmatario () {
		long idCaratteristica = -1;
		Map<String, it.finmatica.atti.zk.SoggettoDocumento> firmatari = [:]
		if (propostaDelibera.fuoriSacco) {
			idCaratteristica = propostaDelibera.tipologia.caratteristicaTipologiaFuoriSacco.id
		} else {
			idCaratteristica = propostaDelibera.tipologia.caratteristicaTipologia.id
		}

		Window w = Executions.createComponents ("/atti/documenti/popupSceltaSoggetto.zul", self, [idCaratteristicaTipologia: idCaratteristica
																								  , documento: propostaDelibera
																								  , soggetti: firmatari
																								  , tipoSoggetto: TipoSoggetto.FIRMATARIO
																								  , categoriaSoggetto: TipoSoggetto.CATEGORIA_COMPONENTE])
		w.onClose { event ->
			// se ho annullato la modifica, non faccio niente:
			if (event.data == null)
				return;
			propostaDeliberaDTOService.addSoggettoFirmatario(propostaDelibera, firmatari.FIRMATARIO, soggettiFirmatari.size())
			soggettiFirmatari = propostaDeliberaDTOService.caricaSoggettiFirmatari(propostaDelibera)
			// altrimenti aggiorno i firmatari.
			BindUtils.postNotifyChange(null, null, this, "soggettiFirmatari");
		}
		w.doModal()
	}

	@Command onEliminaFirmatario (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("firmatario") def firmatario) {
		Messagebox.show("Eliminare il firmatario selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							propostaDeliberaDTOService.rimuoviFirmatario(soggettiFirmatari, firmatario)

							BindUtils.postNotifyChange(null, null, PropostaDeliberaViewModel.this, "soggettiFirmatari");
						}
					}
				}
		)
	}

	private void caricaFirmatari () {
		if (propostaDelibera.tipologia?.caratteristicaTipologia?.layoutSoggetti == CaratteristicaTipologiaDettaglioViewModel.listaZulPossibili.find { it.label == "Proposta Delibera con Firmatari" }.url) {
			firmatariAbilitati = true
			soggetti.remove(TipoSoggetto.FIRMATARIO)
			soggettiFirmatari = propostaDeliberaDTOService.caricaSoggettiFirmatari(propostaDelibera)
		}
	}

    /*
     * Metodi per la gestione del budget
     */

    private void caricaListaBudget () {
        if (budgetAbilitato) {
            if (gestioneFondi) {
                listaTipiBudget = TipoBudget.findAllByUnitaSo4AndAttivoAndValido(soggetti[TipoSoggetto.UO_PROPONENTE]?.unita?.domainObject ,true, true, [sort: "titolo", order: "asc"]).toDTO()
            }
            else {
                listaTipiBudget = TipoBudget.findAllByAttivoAndValido(true, true, [sort:'titolo', order:'asc']).toDTO()
            }

            listaBudget = Budget.createCriteria().list() {
                eq("propostaDelibera.id", propostaDelibera.id)
                order("sequenza", "asc")
            }.toDTO(["tipoBudget"])

			listaCe4Conti = Ce4Conto.createCriteria().list() {
				order("descrizione", "asc")
			}

			listaCe4Fornitori = Ce4Fornitore.createCriteria().list() {
				order("ragioneSociale", "asc")
			}

            BindUtils.postNotifyChange(null, null, this, "listaBudget")
			BindUtils.postNotifyChange(null, null, this, "listaCe4Conti")
			BindUtils.postNotifyChange(null, null, this, "listaCe4Fornitori")
        }
    }

	@NotifyChange(["listaCe4Conti", "totalSize"])
	@Command
	void onRicercaCe4Conto(@BindingParam("search") String search) {
		activePage = 0
		filtroRicerca = search
		listaCe4Conti = loadCe4Conti()
		BindUtils.postNotifyChange(null, null, this, "totalSize")
		BindUtils.postNotifyChange(null, null, this, "listaCe4Conti")
	}

	@NotifyChange(["listaCe4Conti", "totalSize"])
	private PagedResultList loadCe4Conti() {
		PagedResultList elencoCe4Conti =  Ce4Conto.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			or {
				ilike("contoEsteso", "%" + filtroRicerca + "%")
				ilike("descrizione", "%" + filtroRicerca + "%")
			}
			order("descrizione", "asc")
		}
		totalSize = elencoCe4Conti.totalCount
		return elencoCe4Conti
	}

	@NotifyChange(["listaCe4Conti", "totalSize"])
	@Command
	void onPaginaCe4Conto() {
		listaCe4Conti = loadCe4Conti().toDTO()
		BindUtils.postNotifyChange(null, null, this, "totalSize")
		BindUtils.postNotifyChange(null, null, this, "listaCe4Conti")
	}

	@Command
	@NotifyChange(["listaBudget","target"])
	void onSelectCe4Conto(
			@ContextParam(ContextType.TRIGGER_EVENT) SelectEvent event, @BindingParam("target") Component target, @BindingParam("ent") def ent, @BindingParam("sel") def sel) {
		// SOLO se ho selezionato un solo item
		if (event.getSelectedItems()?.size() == 1) {
			activePage = 0
			ent.contoEconomico = sel.value.contoEsteso
			//BindUtils.postNotifyChange(null, null, listaBudget, "*")
			BindUtils.postNotifyChange(null, null, this, "listaBudget")
			target?.parent.parent.close()
		}
	}

	@NotifyChange(["listaCe4Fornitori", "totalSize"])
	@Command
	void onRicercaCe4Fornitore(@BindingParam("search") String search) {
		activePage = 0
		filtroRicerca = search
		listaCe4Fornitori = loadCe4Fornitori()
		BindUtils.postNotifyChange(null, null, this, "totalSize")
		BindUtils.postNotifyChange(null, null, this, "listaCe4Fornitori")
	}

	@NotifyChange(["listaCe4Fornitori", "totalSize"])
	private PagedResultList loadCe4Fornitori() {
		PagedResultList elencoCe4Fornitori =  Ce4Fornitore.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			or {
				ilike("contoFornitore", "%" + filtroRicerca + "%")
				ilike("ragioneSociale", "%" + filtroRicerca + "%")
			}
			order("ragioneSociale", "asc")
		}
		totalSize = elencoCe4Fornitori.totalCount
		return elencoCe4Fornitori
	}

	@NotifyChange(["listaCe4Fornitori", "totalSize"])
	@Command
	void onPaginaCe4Fornitore() {
		listaCe4Fornitori = loadCe4Fornitori().toDTO()
		BindUtils.postNotifyChange(null, null, this, "totalSize")
		BindUtils.postNotifyChange(null, null, this, "listaCe4Fornitori")
	}

	@Command
	@NotifyChange(["listaBudget","target"])
	void onSelectCe4Fornitore(
			@ContextParam(ContextType.TRIGGER_EVENT) SelectEvent event, @BindingParam("target") Component target, @BindingParam("ent") def ent, @BindingParam("sel") def sel) {
		// SOLO se ho selezionato un solo item
		if (event.getSelectedItems()?.size() == 1) {
			activePage = 0
			ent.codiceFornitore = sel.value.contoFornitore
			BindUtils.postNotifyChange(null, null, this, "listaBudget")
			target?.parent.parent.close()
		}
	}

    @Command
    onAggiungiBudget(){
		Date now = new Date()
		Date mydate1 = new GregorianCalendar(now.year + 1900, Calendar.JANUARY, 1).time
		Date mydate2 = new GregorianCalendar(now.year + 1900, Calendar.DECEMBER, 31).time
		listaBudget.add(new BudgetDTO(id: -1, dataInizioValidita: mydate1,  dataFineValidita: mydate2, propostaDelibera: propostaDelibera))
		//listaBudget.add(new BudgetDTO(id: -1, propostaDelibera: propostaDelibera))
        BindUtils.postNotifyChange(null, null, this, "listaBudget")
    }

    @Command
    onEliminaBudget (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("budget") BudgetDTO budget) {
        Messagebox.show("Eliminare il budget selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
            new org.zkoss.zk.ui.event.EventListener() {
                void onEvent(Event e) {
                    if (Messagebox.ON_OK.equals(e.getName())) {
                        budgetDTOService.eliminaBudget (budget)
                        propostaDelibera.version = propostaDelibera.domainObject.version
                        PropostaDeliberaViewModel.this.caricaListaBudget()
                    }
                }
            })
    }

	@Command onDownloadStampaUnica () {
		PropostaDelibera d 	= propostaDelibera.domainObject
		attiFileDownloader.downloadFileAllegato(d, d.stampaUnica)
	}


	@Command onChangePriorita (@BindingParam("valore") String valore) {
		if ((propostaDelibera.priorita == null || propostaDelibera.priorita == 0) && Integer.parseInt(valore) > 0) {
			controllaPriorita = true
		}
		propostaDelibera.priorita = Integer.parseInt(valore)
		BindUtils.postNotifyChange(null, null, this, "priorita")
	}

    public def getProposta(){
        return propostaDelibera
    }

	@Command
	public void onExportBudgetExcel() {

		DecimalFormat formatter = new DecimalFormat("#,###.00");
		def exportOptions

		def lista = listaBudget.collect { row ->
			[  atto: 	propostaDelibera.domainObject.estremiAtto
			   , tipoBudget: 	row.tipoBudget?.titolo
			   , importo: 	row.importo ? formatter.format(row.importo): ""
			   , dataInizioValidita: 	row.dataInizioValidita?.format("dd/MM/yyyy") ?: ""
			   , dataFineValidita: 	row.dataFineValidita?.format("dd/MM/yyyy") ?: ""
			   , contoEconomico: 	row.contoEconomico
			   , contoFornitore: 	row.codiceFornitore
			   , codiceProgetto: 	row.codiceProgetto
			   , approvato: 	row.approvato?"Si":"No"]
		}

		if (!gestioneFondi) {
			exportOptions =   [   atto				: [label: 'Atto', 					index: 0, columnType: 'TEXT']
								  , tipoBudget			: [label: 'Budget', 				index: 1, columnType: 'TEXT']
								  , importo 			: [label: 'Importo', 				index: 2, columnType: 'TEXT']
								  , dataInizioValidita	: [label: 'Data Inizio Validità',	index: 3, columnType: 'TEXT']
								  , dataFineValidita	: [label: 'Data Fine Validità',		index: 4, columnType: 'TEXT']
								  , contoEconomico		: [label: 'Conto Economico',		index: 5, columnType: 'TEXT']
								  , contoFornitore		: [label: 'Conto Fornitore',		index: 6, columnType: 'TEXT']
								  , codiceProgetto		: [label: 'Codice Progetto',		index: 7, columnType: 'TEXT']
								  , approvato			: [label: 'Approvato',				index: 8, columnType: 'TEXT']]
		} else {
			exportOptions =   [   atto				: [label: 'Atto', 					index: 0, columnType: 'TEXT']
								  , tipoBudget			: [label: 'Budget', 				index: 1, columnType: 'TEXT']
								  , importo 			: [label: 'Importo', 				index: 2, columnType: 'TEXT']
								  , contoEconomico		: [label: 'Conto Economico',		index: 3, columnType: 'TEXT']
								  , codiceProgetto		: [label: 'Codice Progetto',		index: 4, columnType: 'TEXT']]
		}

		try {
			exportService.downloadExcel(exportOptions, lista)
		} finally {
			// todo
		}
	}

	@NotifyChange(["importo"])
	@Command
	public void aggiornaTotaleImporto() {
		//per ora viene usata solo dall'integrazione con ATS Pavia dove non abbiamo distinzione tra ENTRATA e SPESA
		propostaDelibera.importo = integrazioneContabilita.totaleImporto(propostaDelibera.domainObject, null)
		BindUtils.postNotifyChange(null, null, this, "determina")
	}

	// Integrazione PiTre
	@Command
	void onAggiungiDestinatariPiTre() {
		Window w = Executions.createComponents("/atti/integrazioni/protocollo/popupSceltaDestinatariPiTre.zul", self, [destinatari: listaDestinatariPiTre, documento: propostaDelibera.domainObject])
		w.onClose { event ->
			if (event.data == null) {
				return null
			}

			PropostaDeliberaDTO d = destinatarioNotificaDTOService.aggiungiDestinatarioPiTre(propostaDelibera, event.data)
			propostaDelibera.version = d.version
			refreshListaDestinatariPiTre()
		}
		w.doModal()
	}

	private void refreshListaDestinatariPiTre() {
		listaDestinatariPiTre = destinatarioNotificaDTOService.getListaDestinatariPiTre(propostaDelibera)
		BindUtils.postNotifyChange(null, null, this, "listaDestinatariPiTre")
	}

	private boolean hasDelegaValida() {
		return delegante != null && !so4DelegaService.getDeleghe(springSecurityService.currentUser, delegante).isEmpty()
	}
}
