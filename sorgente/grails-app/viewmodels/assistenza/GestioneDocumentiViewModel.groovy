package assistenza

import afc.AfcAbstractGrid
import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.Sql
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.dto.documenti.*
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CaratteristicaTipologiaService
import it.finmatica.atti.impostazioni.OperazioniLogService
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzione
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzioneService
import it.finmatica.gestioneiter.motore.WkfIterService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.media.Media
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

import javax.sql.DataSource

class GestioneDocumentiViewModel {

	// services
	SpringSecurityService springSecurityService
	StampaUnicaService  stampaUnicaService;
	WkfIterService		wkfIterService;
	AttiGestoreCompetenze gestoreCompetenze;
	AttiFileDownloader attiFileDownloader;
	VistoParereService	vistoParereService
	CertificatoService	certificatoService
	AllegatoDTOService	allegatoDTOService
	OperazioniLogService operazioniLogService
	WkfAzioneService	 wkfAzioneService
	CaratteristicaTipologiaService caratteristicaTipologiaService

	// Paginazione e ricerca
	int pageSize 	= AfcAbstractGrid.PAGE_SIZE_DEFAULT
	int activePage 	= 0
	int	totalSize	= 0

	String numeroProposta;
	Integer annoProposta = Calendar.getInstance().get(Calendar.YEAR)
    String numero;
	Integer id;
	// componenti
	Window self;
	@Wire ("#popupDocumento")
	Window popupDocumento;

	// dati
	def listaDocumenti;
	def selectedRecord;
	def doc;

	DataSource dataSource

	// dati del documento selezionato:
	String titoloDocumento;
	String titoloIterDocumento;
	String titoloStepDocumento;
	String codiceOggetto;
	String nomeOggetto;

	def listaCompetenze;
	def listaCfgStep;
	def listaTesti;
	def listaAllegati;
	def listaSoggetti
	def cfgStep;  //nodo selezionato in maschera (per cambiare lo stato del documento)
	def listaAzioni;
	def listaBloccaSblocca;
	def listaCondizioni;
	def azione;
	def condizione;
	def blocco;
	def tipologia;
	def modelloTesto

	IGestoreFile gestoreFile;

	def tipoOggetto
	List tipiOggetto 	= [	[codice: 'DETERMINA'			, nome: "DETERMINA"				, zul: '/atti/documenti/determina.zul'			, classe : Determina		, dto: DeterminaDTO,  zulTipologia : '/dizionari/atti/tipoDeterminaDettaglio.zul']
						  ,	[codice: 'PROPOSTA_DELIBERA'	, nome: "PROPOSTA DI DELIBERA"	, zul: '/atti/documenti/propostaDelibera.zul'	, classe : PropostaDelibera	, dto: PropostaDeliberaDTO,  zulTipologia : '/dizionari/atti/tipoDeliberaDettaglio.zul']
						  ,	[codice: 'DELIBERA'				, nome: "DELIBERA"				, zul: '/atti/documenti/delibera.zul'			, classe : Delibera			, dto: DeliberaDTO, zulTipologia : '/dizionari/atti/tipoDeliberaDettaglio.zul']
						  , [codice: 'VISTO'				, nome: "VISTO"					, zul: '/atti/documenti/visto.zul'				, classe : VistoParere		, dto: VistoParereDTO,  zulTipologia : '/dizionari/atti/tipoVistoParereDettaglio.zul']
						  , [codice: 'PARERE'				, nome: "PARERE"				, zul: '/atti/documenti/parere.zul'				, classe : VistoParere		, dto: VistoParereDTO,  zulTipologia : '/dizionari/atti/tipoVistoParereDettaglio.zul']
						  , [codice: 'CERTIFICATO'			, nome: "CERTIFICATO"			, zul: '/atti/documenti/certificato.zul'		, classe : Certificato		, dto: CertificatoDTO,  zulTipologia : '/dizionari/atti/tipoCertificatoDettaglio.zul']	]

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		activePage= 0
		totalSize = 0
		this.self = w
	}

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
	}

	@Command onApriDocumento (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		selectedRecord 		= event.target.value;
		titoloDocumento = selectedRecord.oggetto;

		codiceOggetto = selectedRecord.tipoOggetto;
		tipoOggetto =  tipiOggetto.find { it.codice == selectedRecord.tipoOggetto};
		nomeOggetto = tipoOggetto.nome;

		doc = tipoOggetto.dto.newInstance();
		doc.id = selectedRecord.idDocumento;

		creaLog("Aperto Documento", "Aperto Documento")

		def atto = doc.getDomainObject();
		titoloIterDocumento = (atto.iter?.cfgIter?.nome?:"ITER NON ANCORA ATTIVATO")
		tipologia = atto.tipologiaDocumento
		modelloTesto = atto.modelloTesto


		titoloStepDocumento = (atto.iter?.stepCorrente?.cfgStep?.titolo?:atto.iter?.stepCorrente?.cfgStep?.nome?:"");
		caricaCompetenze(atto);
		caricaListaCfgStep(atto);
		caricaElencoFile(atto);
		caricaListaAzioni(atto);
		caricaListaCondizioni(atto);
		caricaListaBloccaSblocca(atto);
		caricaSoggetti(atto);

		popupDocumento.doModal();
		BindUtils.postNotifyChange(null, null, this, "documento");
		BindUtils.postNotifyChange(null, null, this, "titoloDocumento");
		BindUtils.postNotifyChange(null, null, this, "titoloIterDocumento");
		BindUtils.postNotifyChange(null, null, this, "titoloStepDocumento");
		BindUtils.postNotifyChange(null, null, this, "codiceOggetto");
		BindUtils.postNotifyChange(null, null, this, "nomeOggetto");
		BindUtils.postNotifyChange(null, null, this, "tipologia");
		BindUtils.postNotifyChange(null, null, this, "modelloTesto");
	}

	@Command onRigeneraStampaUnica () {
		creaLog("Rigenerazione Stampa Unica", "Stampa unica rigenerata")
		// la stampa unica è disponibile solo per certi tipi di documento:
		if ( tipoOggetto.codice == Determina.TIPO_OGGETTO || tipoOggetto.codice == Delibera.TIPO_OGGETTO) {
			def atto = doc.getDomainObject();
			stampaUnicaService.stampaUnica(atto);
			caricaElencoFile(atto);
			Clients.showNotification("Stampa unica rigenerata", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true);
			BindUtils.postNotifyChange(null, null, this, "listaTesti")
		}
	}

	@NotifyChange(["titoloStepDocumento"])
	@Command onCambiaNodo () {
		def atto = doc.getDomainObject();
		creaLog("Modificato Nodo Flusso", "Modificato il nodo del flusso da \"${titoloStepDocumento}\" a \"${cfgStep.domainObject.titolo}\"")

		Determina.withTransaction {
			def docIter = DocumentoFactory.getDocumento(doc.id, atto.TIPO_OGGETTO);
			// si toglie la data fine dell'iter se si gestisce un'atto per cambiare lo step #33135
			atto.iter.dataFine = null;
			wkfIterService.proseguiStep(docIter, atto.iter, cfgStep.domainObject)
		}

		titoloStepDocumento = (atto.iter?.stepCorrente?.cfgStep?.titolo?:atto.iter?.stepCorrente?.cfgStep?.nome?:"");
		caricaCompetenze(atto);

		Clients.showNotification("Nodo cambiato con successo.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 10000, true);
		BindUtils.postNotifyChange(null, null, this, "titoloStepDocumento");
	}

	@Command onChiudi () {
        popupDocumento.setVisible(false);
	}

	@Command
	void onRicerca () {
		ricercaDocumenti()
	}

	private void caricaListaCfgStep (def atto) {
		listaCfgStep = atto.iter?.cfgIter?.getCfgStep()?.toDTO();

		BindUtils.postNotifyChange(null, null, this, "listaCfgStep");
	}

	private void caricaListaAzioni (def atto) {
		listaAzioni = WkfAzione.createCriteria().list {
			eq ("tipo", it.finmatica.gestioneiter.annotations.Action.TipoAzione.AUTOMATICA)
			not{
				like ("nomeMetodo", "abilita%")
				like ("nomeMetodo", "proteggi%")
			}
			eq ("tipoOggetto.codice", codiceOggetto)
			order ("nome", 	"asc")
		}
		BindUtils.postNotifyChange(null, null, this, "listaAzioni");
	}


	private void caricaListaBloccaSblocca (def atto) {
		listaBloccaSblocca = WkfAzione.createCriteria().list {
			eq ("tipo", it.finmatica.gestioneiter.annotations.Action.TipoAzione.AUTOMATICA)
			or {
				like ("nomeMetodo", "abilita%")
				like ("nomeMetodo", "proteggi%")
			}
			eq ("tipoOggetto.codice", codiceOggetto)
			order ("nome", 	"asc")
		}
		BindUtils.postNotifyChange(null, null, this, "listaBloccaSblocca");
	}

	private void caricaListaCondizioni (def atto) {
		listaCondizioni = WkfAzione.createCriteria().list {
			eq ("tipo", it.finmatica.gestioneiter.annotations.Action.TipoAzione.CONDIZIONE)
			eq ("tipoOggetto.codice", codiceOggetto)
			order ("nome", 	"asc")
		}
		BindUtils.postNotifyChange(null, null, this, "listaCondizioni");
	}
	private void caricaCompetenze (def atto) {
		listaCompetenze = gestoreCompetenze.getListaCompetenze(atto);

		BindUtils.postNotifyChange(null, null, this, "listaCompetenze");
	}

	private void caricaSoggetti (def atto) {
		listaSoggetti = caratteristicaTipologiaService.calcolaSoggettiDto(atto)
		BindUtils.postNotifyChange(null, null, this, "listaSoggetti");
	}

	private void ricercaDocumenti () {
		String query = 	 """select tipo_oggetto
                                 , id
                                 , anno_proposta
                                 , numero_proposta
                                 , anno
                                 , numero
                                 , tipologia
                                 , oggetto
								,  stato
                                 , ente
                              from ass_documenti """ +
				((id != null) ? """ where id = :id """ :
                        (numero != null ? (""" where anno = :anno and numero = :numero""")
                                        : (""" where anno_proposta = :anno and numero_proposta = :numero_proposta""")));
	    Sql sql = new Sql (dataSource);

		listaDocumenti = new ArrayList<>();
		def rows = sql.rows(query, [anno: annoProposta, numero_proposta: numeroProposta, id: id, numero: numero]);
		for (def row : rows)
			listaDocumenti.add([idDocumento: 	 row.id
							  , tipoOggetto:	 row.tipo_oggetto
							  , titoloTipologia: row.tipologia
							  , numeroProposta:  row.numero_proposta
							  , annoProposta: 	 row.anno_proposta
							  , numero: 		 row.numero
							  , anno: 			 row.anno
							  , oggetto: 		 row.oggetto
							  , stato:           row.stato]);

		BindUtils.postNotifyChange(null, null, this, "listaDocumenti")
		BindUtils.postNotifyChange(null, null, this, "totalSize")
		BindUtils.postNotifyChange(null, null, this, "activePage")
	}

	private void caricaElencoFile (def atto){
		//creo la lista dei documenti principali (testo atto e stampa unica) da scaricare
		listaTesti = new ArrayList<>()
		if (atto.testo != null)
			listaTesti.add([allegato : atto.testo,	nome:"TESTO"])
		if (atto.testoOdt != null)
			listaTesti.add([allegato : atto.testoOdt,	nome:"TESTO ORIGINALE"])
		if((tipoOggetto.codice == "DETERMINA" || tipoOggetto.codice == "DELIBERA")) {
			if (atto.stampaUnica != null)
				listaTesti.add([allegato: atto.stampaUnica, nome: "STAMPA UNICA"])
		}

		listaAllegati = Allegato.createCriteria().list {
			projections {
				property "titolo"
				property "id"
			}
			or {
				eq("determina.id", doc.id)
				eq("propostaDelibera.id", doc.id)
				eq("delibera.id", doc.id)
			}

			order ("sequenza", "asc")
			order ("titolo",   "asc")
		}.collect { row -> [titolo: row[0], idAllegato: row[1]]};

		BindUtils.postNotifyChange(null, null, this, "listaTesti")
		BindUtils.postNotifyChange(null, null, this, "listaAllegati")
	}

	@Command onDownloadFileTesto (@BindingParam("allegato") def allegato) {
		creaLog("Scaricato file testo", "Scaricato il file allegato ${allegato.id}")
		attiFileDownloader.downloadFileAllegato (doc.getDomainObject(), allegato, false, false)
	}

	@Command onUploadFileTesto (@ContextParam(ContextType.TRIGGER_EVENT) Event event,@BindingParam("allegato") def allegato) {
		FileAllegato file = FileAllegato.get(allegato.id);
		Media media = event.media
		file.nome = media.name + (file.nome.endsWith(FileAllegato.ESTENSIONE_FILE_NASCOSTO) ? FileAllegato.ESTENSIONE_FILE_NASCOSTO : "")
		creaLog("Modificato file testo", "Modificato il file testo ${allegato.id} sostituendo il file ${file.nome}")
		file.contentType = media.contentType
		file.firmato = file.isP7m()
		def d = doc.getDomainObject()
		gestoreFile.addFile(d, file, media.getStreamData())
		file.save()
		caricaElencoFile(d);

		Clients.showNotification("File cambiato con successo.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true);
	}

	@Command onApriAtto () {
		String path = tipoOggetto.zul;
		Window w = Executions.createComponents (path, self, [id: doc.id]);
		w.doModal()
	}

	@Command onAggiungiCompetenza () {
		Window w = Executions.createComponents ("/commons/popupCompetenzaDettaglio.zul", self, [documento: doc, tipoDocumento: codiceOggetto, paginaLog: "ASSISTENZA"])
		w.onClose { caricaCompetenze(doc.getDomainObject()) }
		w.doModal()
	}

	@NotifyChange(["listaCompetenze"])
	@Command onModificaCompetenza (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("competenza") def competenza) {
		Window w = Executions.createComponents ("/commons/popupCompetenzaDettaglio.zul", self, [documento: doc, tipoDocumento: codiceOggetto, id: competenza.id, paginaLog: "ASSISTENZA"])
		w.onClose { caricaCompetenze(doc.getDomainObject()) }
		w.doModal()
	}

	@NotifyChange(["listaCompetenze"])
	@Command onEliminaCompetenza (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("competenza") def competenza) {
		//def doc = documento.getDomainObject()
		Messagebox.show("Eliminare la competenza selezionata?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							creaLog("Cancellazione Competenza", "Cancellata competenza ${doc.id}")
							competenza.delete();
							caricaCompetenze(doc.getDomainObject());
						}
					}
				}
		)

		BindUtils.postNotifyChange(null, null, this, "listaCompetenze");
	}

	@Command onRigeneraVistoParere (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		Messagebox.show("Vuoi Rigenerare il Visto/Parere?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							def vistoParere = VistoParere.get(doc.id);
							if (vistoParere != null) {
								creaLog("Rigenerazione Visto/Parere", "Rigenerato Visto/Parere ${vistoParere.id}")
								vistoParere.valido = false;
								vistoParere.save()
								vistoParereService.creaVistoParere(vistoParere.documentoPrincipale, vistoParere.tipologia, vistoParere.automatico)
								popupDocumento.setVisible(false);
								ricercaDocumenti()
							}
						}
					}
				}
		)
	}


	@Command onRigeneraCertificato (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		Messagebox.show("Vuoi Rigenerare il Certificato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							def certificato = Certificato.get(doc.id);
							if (certificato != null) {
								creaLog("Rigenerazione Certificato", "Rigenerato Certificato ${certificato.id}")
								certificato.valido = false;
								certificato.save()
								certificatoService.creaCertificato(certificato.documentoPrincipale, certificato.tipologia, certificato.tipo, certificato.secondaPubblicazione)
								popupDocumento.setVisible(false);
								ricercaDocumenti()
							}
						}
					}
				}
		)
	}

	@Command onRemoveAllegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("allegato") def value) {
		Messagebox.show("Vuoi eliminare l'allegato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							Allegato allegato = Allegato.get(value.idAllegato)
							creaLog("Eliminazione Allegato", "Eliminato allegato ${allegato.id} \"${allegato.titolo}\"")
							allegatoDTOService.elimina (allegato.toDTO(), doc)
							def atto = doc.getDomainObject();
							caricaElencoFile(atto);
						};
					}
				}
		)
	}


	@Command onModificaAllegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("allegato") def value) {
		Window w = Executions.createComponents("/atti/documenti/allegato.zul", self, [id: value.idAllegato, documento: doc, competenzeLettura: true, paginaLog: "ASSISTENZA"])
		w.onClose {
			def atto = doc.getDomainObject();
			caricaElencoFile(atto);
		}
		w.doModal()
	}

	@Command onAggiungiAllegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		Window w = Executions.createComponents("/atti/documenti/allegato.zul", self, [id: -1, documento: doc, competenzeLettura: true, paginaLog: "ASSISTENZA"])
		w.onClose {
			def atto = doc.getDomainObject();
			caricaElencoFile(atto);
		}
		w.doModal()
	}

	private void creaLog(String operazione, String descrizione){
		operazioniLogService.creaLog(doc.id, doc.getDomainObject().tipoOggetto, "ASSISTENZA", operazione, descrizione)
	}

	@Command onEseguiAzione () {
		if (azione == null) {
			Clients.showNotification("Nessuna Azione Selezionata.", Clients.NOTIFICATION_TYPE_WARNING, null, "top_center", 3000, true);
		}
		else if(azione.automatica) {
			Messagebox.show("Vuoi eseguire l'azione \"${azione.nome}\"?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
					new org.zkoss.zk.ui.event.EventListener() {
						public void onEvent(Event e) {
							if (Messagebox.ON_OK.equals(e.getName())) {
								creaLog("Eseguita azione", "Eseguita l'azione automatica \"${azione.nome}\"")
								def atto = doc.getDomainObject();
								wkfAzioneService.eseguiAzioneAutomatica(azione, atto)
								Clients.showNotification("Azione effettuata con successo.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true);
							}
						}
					}
			);
		}
	}

	@Command onEseguiBloccaSblocca () {
		if (blocco == null) {
			Clients.showNotification("Nessuna Azione Selezionata.", Clients.NOTIFICATION_TYPE_WARNING, null, "top_center", 3000, true);
		}
		else if(blocco.automatica) {
			Messagebox.show("Vuoi eseguire l'azione \"${blocco.nome}\"?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
					new org.zkoss.zk.ui.event.EventListener() {
						public void onEvent(Event e) {
							if (Messagebox.ON_OK.equals(e.getName())) {
								creaLog("Eseguita azione", "Eseguita l'azione automatica \"${blocco.nome}\"")
								def atto = doc.getDomainObject();
								wkfAzioneService.eseguiAzioneAutomatica(blocco, atto)
								Clients.showNotification("Azione effettuata con successo.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true);
							}
						}
					}
			);
		}
	}

	@Command onVerificaCondizione() {
		if (condizione == null) {
			Clients.showNotification("Nessuna Condizione Selezionata.", Clients.NOTIFICATION_TYPE_WARNING, null, "top_center", 3000, true);
		}
		else  {
			creaLog("Verificata condizione", "Effettuata la verifica della condizione \"${condizione.nome}\"");
			def atto = doc.getDomainObject();
			boolean risultato = wkfAzioneService.valutaCondizione(condizione, atto)
			if (risultato){
				Clients.showNotification("Condizione verificata.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true);
			}
			else  {
				Clients.showNotification("Condizione non verificata.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 3000, true);
			}

		}
	}

	@Command onApriTipologia () {
		creaLog("Aperta tipologia", "Apertura pagina tipologia \"${tipologia.titolo}\"");
		Window w = Executions.createComponents (tipoOggetto.zulTipologia, self, [id: tipologia.id]);
		w.onClose {}
		w.doModal()
	}


	@Command onApriModelloTesto () {
		creaLog("Aperta tipologia", "Apertura pagina modello testo \"${modelloTesto.nome}\"");
		Long idGestioneTestiModello = modelloTesto.id
		Window w = Executions.createComponents ("/dizionari/impostazioni/gestioneTestiModelloDettaglio.zul", self, [id: idGestioneTestiModello])
		w.onClose {}
		w.doModal()
	}

	@Command onApriIter () {
		creaLog("Aperta iter", "Apertura pagina iter \"${titoloIterDocumento}\"");
		def atto = doc.getDomainObject();
		Window window = Executions.createComponents("/configuratoreiter/iter/iterDettaglio.zul", self, [id: atto.iter.cfgIter.id]);
		window.onClose {}
		window.doModal();
	}

	@Command
	onSceltaSoggetto (@BindingParam("tipoSoggetto") String tipoSoggetto, @BindingParam("categoriaSoggetto") String categoriaSoggetto) {
        try {
            Window w = Executions.createComponents("/atti/documenti/popupSceltaSoggetto.zul", self,
                    [idCaratteristicaTipologia: ((doc instanceof DeliberaDTO) ? doc.getDomainObject().tipologiaDocumento.caratteristicaTipologiaDelibera.id : doc.getDomainObject().tipologiaDocumento.caratteristicaTipologia.id)
                     , documento              : doc
                     , soggetti               : listaSoggetti
                     , tipoSoggetto           : tipoSoggetto
                     , categoriaSoggetto      : categoriaSoggetto])
            w.onClose { event ->
                // se ho annullato la modifica, non faccio niente:
                if (event.data == null) {
                    return
                };
                creaLog("Aggiornati Soggetti", "Aggiornati i soggetti del documento")
                caratteristicaTipologiaService.salvaSoggettiModificati(doc.getDomainObject(), listaSoggetti)
                // altrimenti aggiorno i soggetti.
                BindUtils.postNotifyChange(null, null, this, "listaSoggetti")
            }
            w.doModal()
        } catch (Exception e){
            log.error(e, e)
            throw new AttiRuntimeException("Non è possibile modificare il soggetto selezionato")
        }
	}

}
