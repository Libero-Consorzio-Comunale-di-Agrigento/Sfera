package atti.ricerca

import grails.orm.PagedResultList
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.dizionari.Categoria
import it.finmatica.atti.dizionari.Delega
import it.finmatica.atti.dizionari.ImpattoGenerazionale
import it.finmatica.atti.dizionari.StatoIter
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.dizionari.TipoBudget
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.tipologie.TipoCertificato
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.documenti.tipologie.TipoVistoParere
import it.finmatica.atti.documenti.viste.*
import it.finmatica.atti.dto.dizionari.CategoriaDTO
import it.finmatica.atti.dto.dizionari.DelegaDTO
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTO
import it.finmatica.atti.dto.dizionari.TipoAllegatoDTO
import it.finmatica.atti.dto.dizionari.ImpattoGenerazionaleDTO
import it.finmatica.atti.dto.dizionari.TipoBudgetDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoDeliberaDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.odg.Commissione
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import it.finmatica.so4.login.So4UserDetail
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.NotifyChange

import static it.finmatica.zkutils.LabelUtils.getLabel as l

class MascheraRicercaDocumento {

    public static final String NESSUN_VALORE = "-- nessuno --"
    public static final String TUTTI = "TUTTI"

    // configurazione dei tipi di oggetto
    def tipiDocumento = [(Determina.TIPO_OGGETTO)           : [ricercabile              : true
                                                               , domainRicerca          : RicercaDetermina
                                                               , domainTipologia        : TipoDetermina
                                                               , tipoAllegato           : "idTipoAllegatoDetermina"
                                                               , tipoAllegatoVistoParere: "idTipoAllegatoVisto"
                                                               , tipoCategoria          : Categoria.TIPO_OGGETTO_DETERMINA
                                                               , popup                  : "/atti/popupRicercaDocumenti.zul"
                                                               , nome                   : l("tipoOggetto.determine")
                                                               , labelCategoria         : l("label.categoria.determina")
                                                               , zul                    : "/atti/documenti/determina.zul"
                                                               , icona                  : "/images/agsde2/22x22/logo_determina_22.png"]
                         , (Delibera.TIPO_OGGETTO)          : [ricercabile: true, domainRicerca: RicercaDelibera, domainTipologia: TipoDelibera, tipoAllegato: "idTipoAllegatoDelibera", tipoAllegatoVistoParere: "idTipoAllegatoParere",  tipoCategoria: Categoria.TIPO_OGGETTO_PROPOSTA_DELIBERA,
                                                               popup      : "/atti/popupRicercaDocumenti.zul", nome: l(
            "tipoOggetto.delibere"), labelCategoria                       : l(
            "label.categoria.propostaDelibera"), zul                              : "/atti/documenti/delibera.zul", icona: "/images/agsde2/22x22/logo_delibera_22.png"]
                         , (PropostaDelibera.TIPO_OGGETTO)  : [ricercabile: false, domainRicerca: RicercaDelibera, domainTipologia: TipoDelibera, tipoAllegato: "idTipoAllegatoDelibera", tipoAllegatoVistoParere: "idTipoAllegatoParere" ,  tipoCategoria: Categoria.TIPO_OGGETTO_PROPOSTA_DELIBERA,
                                                               popup      : "/atti/popupRicercaDocumenti.zul", nome: l(
            "tipoOggetto.proposteDelibere"), labelCategoria               : l(
            "label.categoria.propostaDelibera"), zul                      : "/atti/documenti/propostaDelibera.zul", icona: "/images/agsde2/22x22/logo_proposta_delibera_22.png"]
                         , (VistoParere.TIPO_OGGETTO)       : [ricercabile: true, domainRicerca: RicercaVisto, domainTipologia: TipoVistoParere, tipoAllegato: null, tipoAllegatoVistoParere: null,   tipoCategoria: "",
                                                               popup      : "/atti/popupRicercaDocumentiCollegati.zul", nome: l(
            "tipoOggetto.visti"), labelCategoria                          : l(
            "label.categoria.determina"), zul                             : "/atti/documenti/visto.zul", icona: "/images/agsde2/22x22/logo_visto_22.png"]
                         , (VistoParere.TIPO_OGGETTO_PARERE): [ricercabile: true, domainRicerca: RicercaParere, domainTipologia: TipoVistoParere, tipoAllegato: null, tipoAllegatoVistoParere: null,  tipoCategoria: "",
                                                               popup      : "/atti/popupRicercaDocumentiCollegati.zul", nome: l(
            "tipoOggetto.pareri"), labelCategoria                         : l(
            "label.categoria.propostaDelibera"), zul                      : "/atti/documenti/parere.zul", icona: "/images/agsde2/22x22/logo_parere_22.png"]
                         , (Certificato.TIPO_OGGETTO)       : [ricercabile: true, domainRicerca: RicercaCertificato, domainTipologia: TipoCertificato, tipoAllegato: null, tipoAllegatoVistoParere: null,  tipoCategoria: "",
                                                               popup      : "/atti/popupRicercaDocumentiCollegati.zul", nome: l(
            "tipoOggetto.certificati"), labelCategoria                    : "", zul: "/atti/documenti/certificato.zul", icona: "/images/agsde2/22x22/logo_certificato_22.png"]]

    def tipiDocumentoRicercabili = getTipiDocumentoRicercabili()

    def getTipiDocumentoRicercabili () {
        return tipiDocumento.findAll { it.value.ricercabile == true }
    }

    def getTipiDocumentoPrincipali () {
        return tipiDocumento.findAll { it.key == Determina.TIPO_OGGETTO || it.key == Delibera.TIPO_OGGETTO }
    }

    /*
     * Vari campi di ricerca
     */
    // dati identificativi del documento
    String tipoDocumento
    Long   idDocumento
    Long   idDocumentoPrincipale

    // dati del documento
    String       oggetto
    int          riservato = 0    // 0: tutti, 1: solo riservati, 2: solo non riservati
    String       stato     = NESSUN_VALORE
    CategoriaDTO categoria

    // dati dei soggetti
    As4SoggettoCorrenteDTO relatore
    So4UnitaPubbDTO        unitaProponente
    So4UnitaPubbDTO        servizio
    Ad4UtenteDTO           redattore
    Ad4UtenteDTO           firmatario
    Ad4UtenteDTO           presidente
    Ad4UtenteDTO           segretario
    Ad4UtenteDTO           incaricato
    Ad4UtenteDTO           funzionario

    // parametri di ricerca "esterni"
    Long numeroAtto
    Long numeroProposta
    Long anno

    // dati dell'atto
    Long   numeroAttoDal
    Long   numeroAttoAl
    Long   annoAtto
    String registroAtto
    Date   dataAdozioneDal
    Date   dataAdozioneAl
    Date   dataEsecutivitaDal
    Date   dataEsecutivitaAl
    Date   dataScadenzaDal
    Date   dataScadenzaAl

    // dati di seconda numerazione, validi solo per la determina: http://svi-redmine/issues/22205
    Long   numeroAtto2Dal
    Long   numeroAtto2Al
    Long   annoAtto2
    String registroAtto2

    // dati della proposta
    String registroProposta
    Long   annoProposta
    Long   numeroPropostaDal
    Long   numeroPropostaAl
    Date   dataPropostaDal
    Date   dataPropostaAl

    // dati del protocollo
    Long   numeroProtocolloDal
    Long   numeroProtocolloAl
    Long   annoProtocollo
    String registroProtocollo
    String classificaCodice
    Integer fascicoloAnno
    String fascicoloNumero

    // dati della tipologia
    def     tipologia            // ATTENZIONE: la tipologia va lasciata "def" perché può essere sia TipoDeliberaDTO che TipoDeterminaDTO.
    boolean conImpegnoSpesa

    // dati di pubblicazione
    Long numeroAlboDal
    Long numeroAlboAl
    Long annoAlbo
    Date dataPubblicazioneDal
    Date dataPubblicazioneAl

    // dati di conservazione
    String logConservazione
    String statoConservazione
    Date   dataConservazioneDal
    Date   dataConservazioneAl

    // gestione della corte dei conti: http://svi-redmine/issues/12267
    String statoInvioCorteConti
    Date   dataInvioCorteContiDal
    Date   dataInvioCorteContiAl

    // specifico per le delibere:
    CommissioneDTO       commissione
    TipoAllegatoDTO      tipoAllegato
    TipoAllegatoDTO      tipoAllegatoVistoParere
    ImpattoGenerazionaleDTO impattoGenerazionale
    OggettoRicorrenteDTO oggettoRicorrente
    TipoBudgetDTO        tipoBudget

    //utilizzati dai ws di ricerca (http://svi-redmine/issues/63219)
    //nelle viste sono valorizzati solo per DELIBERA, PROPOSTA_DELIBERA e DETERMINA
    String 	uoProponenteCodice
    String  codiceEsternoTipologia
    String  tipoPubblicazioneAlbo

    //campi richiesti da integrazione contabilità ats pavia (https://redmine.finmatica.it/issues/69066)
    Integer     inEvidenza = 0   // 0: tutti, 1: in Evidenza, 2: non in Evidenza
    Date 	    dataInEvidenzaDa
    Date 	    dataInEvidenzaA

    // ricerca per importo totale (range)
    BigDecimal  importoTotaleDal
    BigDecimal  importoTotaleAl

    // eventuale filtro aggiuntivo:
    Closure filtroAggiuntivo = null

    /*
     * "Dizionari" per riempire le combobox per i filtri di ricerca
     */
    List<Ad4UtenteDTO> listaRelatori
    List<Ad4UtenteDTO> listaFirmatari
    List<Ad4UtenteDTO> listaRedattori
    List<Ad4UtenteDTO> listaPresidenti
    List<Ad4UtenteDTO> listaSegretari
    List<Ad4UtenteDTO> listaFunzionari
    List<So4UnitaPubbDTO> listaUnita
    List<So4UnitaPubbDTO> listaServizi

    List<String>               listaStatiDocumento
    List<Ad4UtenteDTO>         listaUnitaProponenti
    List<TipoDeliberaDTO>      listaTipologie
    List<TipoRegistroDTO>      listaRegistriAtto
    List<TipoRegistroDTO>      listaTuttiRegistriAtto
    List<CategoriaDTO>         listaCategorie
    List<TipoAllegatoDTO>      listaTipiAllegato
    List<ImpattoGenerazionaleDTO> listaImpattiGenerazionali
    List<OggettoRicorrenteDTO> listaOggettiRicorrenti
    List<TipoBudgetDTO>        listaBudget

    def listaRiservati = ["Tutti", "Solo Riservati", "Solo Non Riservati"]

    // specifico per le delibere:
    List<CommissioneDTO> listaCommissioni

    // specifico per i certificati:
    String tipoCertificato      = TUTTI
    def    listaTipiCertificato = [(TUTTI)                                          : "-- tutti --",
                                   (Certificato.CERTIFICATO_PUBBLICAZIONE)         : "di Pubblicazione",
                                   (Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE): "di Avvenuta Pubblicazione",
                                   (Certificato.CERTIFICATO_ESECUTIVITA)           : "di Esecutività",
                                   (Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA): "di Immediata Esecutività"]

    // specifico per i visti/pareri:
    Ad4UtenteDTO    firmatarioVisto
    So4UnitaPubbDTO unitaVisto
    String          esito      = TUTTI
    def             listaEsiti = [(TUTTI)                                            : "-- tutti --",
	               				    (EsitoVisto.FAVOREVOLE.toString()): 	"Favorevole",
	               				    (EsitoVisto.CONTRARIO.toString()): 		"Contrario",
	               				    (EsitoVisto.NON_APPOSTO.toString()): 	"Non Apposto",
									(EsitoVisto.DA_VALUTARE.toString()): 	"Da Valutare",
									(EsitoVisto.RIMANDA_INDIETRO.toString()): "Respinto",
									(EsitoVisto.FAVOREVOLE_CON_PRESCRIZIONI.toString()): "Favorevole con Prescrizioni"]
	
	def orderMap = [
		'annoAtto':'desc',
		'numeroAtto':'desc',
		'annoProposta':'desc',
		'numeroProposta':'desc'
		]

    int i = 0
    //Attenzione: l'ordine della mappa corrisponde all'ordine delle colonne nel file di excel
	def exportOptions =   [   idDocumento 				  : [label:'ID', 								              index: -1, columnType: 'NUMBER']
							, idDocumentoPrincipale		  : [label:'ID Documento Principale', 			          index: -1, columnType: 'TEXT']
							, tipoDocumento				  : [label:'Tipo Documento', 					              index: -1, columnType: 'TEXT']
							, tipoDocumentoPrincipale 	  : [label:'Tipo documento Principale', 			          index: -1, columnType: 'TEXT']
                            , logConservazione			  : [label:'Conservazione', 						          index: -1, columnType: 'TEXT']
							, numeroAtto2				  : [label:l("feature.secondaNumerazione.numero"),    index:  Impostazioni.SECONDO_NUMERO_DETERMINA.abilitato?i++:-1, columnType: 'NUMBER']
							, annoAtto2					  : [label:l("feature.secondaNumerazione.anno"),      index:  Impostazioni.SECONDO_NUMERO_DETERMINA.abilitato?i++:-1, columnType: 'NUMBER']
                            , numeroAtto				  : [label:'Numero Atto', 						          index:  i++, columnType: 'NUMBER']
                            , annoAtto					  : [label:'Anno Atto', 							          index:  i++, columnType: 'NUMBER']
                            , dataAdozione 				  : [label:'Data Adozione', 						          index:  i++, columnType: 'DATE', formato:'dd/MM/yyyy']
						    , dataEsecutivita			  : [label:l("label.ricerca.dataEsecutivita"),	      index:  i++, columnType: 'DATE', formato:'dd/MM/yyyy']
                            , titoloTipologia			  : [label:'Tipologia', 							          index:  i++, columnType: 'TEXT']
                            , oggetto					  : [label:'Oggetto', 							          index:  i++, columnType: 'TEXT']
                            , numeroProposta			  : [label:l("label.ricerca.numeroProposta"),	      index:  i++, columnType: 'NUMBER']
                            , annoProposta				  : [label:l("label.ricerca.annoProposta"),		      index:  i++, columnType: 'NUMBER']
                            , uoProponenteDescrizione	  : [label:'Unita Proponente', 					          index:  i++, columnType: 'TEXT']
                            , elencoFirmatari             : [label:'Firmatari', 					                  index:  Impostazioni.EXCEL_ELENCO_FIRMATARI.abilitato?i++:-1, columnType: 'TEXT']
							, titoloStep				  : [label:'Stato', 								          index:  -1, columnType: 'TEXT']
                            , stato 					  : [label:'Stato', 								          index:  -1, columnType: 'TEXT']
                            , descrizioneStato			  : [label:'Stato', 								          index:  i++, columnType: 'TEXT']
   						    , dataScadenza				  : [label:Impostazioni.RICHIESTA_ESECUTIVITA_LABEL.valore, index: Impostazioni.RICHIESTA_ESECUTIVITA_COLONNA.abilitato?i++:-1, columnType: 'DATE', formato:'dd/MM/yyyy']
                            , statoConservazione          : [label: 'Stato Conservazione',                          index: -1,   columnType: 'TEXT']
                            , dataConservazione           : [label: 'Data Conservazione',                           index: -1,   columnType: 'DATE']
                            , cup                         : [label: 'CUP',                                          index: -1,   columnType: 'TEXT']
                            , cig                         : [label: 'CIG',                                          index: -1,   columnType: 'TEXT']
                            , titoloImpattoGenerazionale  : [label: l("label.impattoGenerazionale"),           index: Impostazioni.IMPATTO_GENERAZIONALE.abilitato?i++:-1,  columnType: 'TEXT']
                            , note                        : [label: 'NOTE',                                          index: -1,   columnType: 'TEXT']
                            , titoloTipoBudget            : [label: 'Tipo Budget',                                  index: (Impostazioni.CONTABILITA.valore == "integrazioneContabilitaCe4")?i++:-1,  columnType: 'TEXT']
                            , contoEconomico          : [label: 'Conto Economico',                              index: (Impostazioni.CONTABILITA.valore == "integrazioneContabilitaCe4")?i++:-1,  columnType: 'TEXT']
                            , codiceProgetto            : [label: 'Codice Progetto',                              index: (Impostazioni.CONTABILITA.valore == "integrazioneContabilitaCe4")?i++:-1,  columnType: 'TEXT']
                            , importo                      : [label: 'Importo',                                      index: (Impostazioni.CONTABILITA.valore == "integrazioneContabilitaCe4")?i++:-1,  columnType: 'NUMBER']
                            , importoLiquidazione      : [label: 'Importo Liquidazione',                         index: (("integrazioneContabilitaCfa".equals(Impostazioni.CONTABILITA.valore)) && (Impostazioni.CONTABILITA_CFA_EXCEL_IMPORTO.abilitato))?i++:-1,  columnType: 'NUMBER']
                            , dataControlloGestione   : [label: 'Data per controllo di gestione',               index: (TipoDatoAggiuntivo.isAbilitato(TipoDatoAggiuntivo.DATA_CONTROLLO_GESTIONE))?i++:-1, columnType: 'DATE', formato:'dd/MM/yyyy']
                            , numeroPassaggi           : [label: 'Numero passaggi',               index:  Impostazioni.EXCEL_NUMERO_PASSAGGI.abilitato?i++:-1, columnType: 'NUMBER']
                            , ultimoAttoreFlusso        : [label: 'Ultimo attore flusso',           index:  Impostazioni.EXCEL_ULTIMO_ATTORE_FLUSSO.abilitato?i++:-1, columnType: 'TEXT']
			]


    /*
     * Variabili di stato e configurazione
     */
    boolean categoriaAbilitata
    boolean impattoGenerazionaleAbilitato
    boolean corteContiAbilitata
    boolean mostraImportoTotale
    boolean ricercaConservazione = false // indica se si è nella sezione della conservazione
    boolean cercaNelTesto        = false // indica se si deve ricercare il testo dell'oggetto anche nel blob dell'allegato
    boolean abilitaRichiestaEsecutivita
    boolean abilitaRicercaNelTesto
    boolean abilitaIncaricato
    boolean abilitaRicercaServizio
    boolean abilitaBudget
    boolean ricercaMarcatura = false
    boolean daMarcare = false
    boolean attoConcluso = true
    String cup
    String cig
    String contoEconomico
    String codiceProgetto
    String note

    // elenco dei documenti trovati
    def listaDocumenti

    // paginazione
    int activePage = 0
    int pageSize   = 30
    int totalSize

    MascheraRicercaDocumento () {
        corteContiAbilitata = Impostazioni.GESTIONE_CORTE_CONTI.abilitato
        abilitaRichiestaEsecutivita = Impostazioni.RICHIESTA_ESECUTIVITA.abilitato
        abilitaRicercaNelTesto = Impostazioni.RICERCA_NEL_TESTO.abilitato
        abilitaIncaricato = Impostazioni.INCARICATO.abilitato
        abilitaRicercaServizio = Impostazioni.RICERCA_PER_SERVIZIO.abilitato
        mostraImportoTotale = Impostazioni.GESTIONE_IMPORTO_TOTALE.abilitato
        abilitaBudget = Impostazioni.GESTIONE_BUDGET.abilitato
    }

    private def getProp (String prop) {
        return tipiDocumento[tipoDocumento][prop]
    }

    void caricaListe () {
        if (tipoDocumento == Determina.TIPO_OGGETTO) {
            categoriaAbilitata = Impostazioni.CATEGORIA_DETERMINA.abilitato
        } else if (tipoDocumento == Delibera.TIPO_OGGETTO || tipoDocumento == PropostaDelibera.TIPO_OGGETTO) {
            categoriaAbilitata = Impostazioni.CATEGORIA_PROPOSTA_DELIBERA.abilitato
        }

        impattoGenerazionaleAbilitato=Impostazioni.IMPATTO_GENERAZIONALE.abilitato

        caricaListaRegistri()
        caricaListaTipologia()
        caricaListaCommissioni()
        caricaListaCategorie()
        caricaListaOggettiRicorrenti()
        caricaListaBudget()
        listaTipiAllegato()
        caricaImpattiGenerazionali()
        caricaListaStatiDocumento()
        caricaListaRelatori()
        caricaListaServizi()
    }

    void caricaListaRegistri () {
        listaRegistriAtto = TipoRegistro.createCriteria().list() {
            if (tipoDocumento == Determina.TIPO_OGGETTO) {
                eq("determina", true)
            } else if (tipoDocumento == Delibera.TIPO_OGGETTO || tipoDocumento == PropostaDelibera.TIPO_OGGETTO) {
                eq("delibera", true)
            } else {
                or {
                    eq("delibera", true)
                    eq("determina", true)
                }
            }
            eq("valido", true)

            order("descrizione", "asc")
        }.toDTO();
        listaRegistriAtto.add(0, new TipoRegistroDTO(codice: null, descrizione: "-- tutti --"))

        listaTuttiRegistriAtto = TipoRegistro.createCriteria().list() {
            if (tipoDocumento == Determina.TIPO_OGGETTO) {
                eq("determina", true)
            } else if (tipoDocumento == Delibera.TIPO_OGGETTO || tipoDocumento == PropostaDelibera.TIPO_OGGETTO) {
                eq("delibera", true)
            } else {
                or {
                    eq("delibera", true)
                    eq("determina", true)
                }
            }

            order ("descrizione", "asc")
        }.toDTO();
        listaTuttiRegistriAtto.add(0, new TipoRegistroDTO(codice: null, descrizione: "-- tutti --"))
	}
	
	boolean isFiltriAttivi () {
		return (!NESSUN_VALORE.equals(stato)||
			categoria 		        	!= null	||
			relatore 		        	!= null	||
			unitaProponente         	!= null	||
            servizio                    != null ||
			redattore  					!= null ||
			firmatario 			    	!= null || 
			presidente 			    	!= null || 
			segretario 			    	!= null ||
            incaricato                  != null ||
            funzionario                 != null ||
			numeroAttoDal           	!= null || 
			numeroAttoAl            	!= null || 
			annoAtto                	!= null || 
			registroAtto            	!= null ||
			numeroAtto2Dal           	!= null ||
			numeroAtto2Al            	!= null ||
			annoAtto2                	!= null ||
			registroAtto2            	!= null ||
			dataAdozioneDal         	!= null ||
			dataAdozioneAl          	!= null || 
			dataEsecutivitaDal      	!= null || 
			dataEsecutivitaAl       	!= null || 
			dataScadenzaDal 			!= null || 
			dataScadenzaAl  			!= null || 
			registroProposta        	!= null ||
			annoProposta            	!= null ||
			numeroPropostaDal       	!= null ||
			numeroPropostaAl        	!= null ||
			dataPropostaDal         	!= null ||
			dataPropostaAl          	!= null ||
			numeroProtocolloDal     	!= null || 
			numeroProtocolloAl      	!= null ||
			annoProtocollo          	!= null || 
			registroProtocollo      	!= null || 
			tipologia 		        	!= null ||
			conImpegnoSpesa 					||
			numeroAlboDal           	!= null || 
			numeroAlboAl            	!= null ||
			annoAlbo               		!= null || 
			dataPubblicazioneDal    	!= null || 
			dataPubblicazioneAl     	!= null || 
			statoInvioCorteConti    	!= null || 
			dataInvioCorteContiDal  	!= null || 
			dataInvioCorteContiAl   	!= null || 
			commissione             	!= null || 
			tipoAllegato            	!= null || 
			tipoAllegatoVistoParere 	!= null ||
            impattoGenerazionale        != null ||
            oggettoRicorrente           != null ||
            tipoBudget                  != null ||
            contoEconomico              != null ||
            codiceProgetto              != null ||
            note                        != null ||
            esito                       != TUTTI ||
            tipoCertificato             != TUTTI ||
            (ricercaConservazione && !attoConcluso) ||
            cup                         != null ||
            cig                         != null ||
            importoTotaleDal            != null ||
            importoTotaleAl             != null
        )
	}

	String getTooltip () {
		def tooltip = [];
		if (!NESSUN_VALORE.equals(stato)   ) tooltip.add("Stato: ${stato}");
		if (categoria 					!= null) tooltip.add("Categoria: ${categoria.codice}");
		if (relatore 					!= null) tooltip.add("Relatore: ${relatore.denominazione}");
		if (unitaProponente     		!= null) tooltip.add("Unità Proponente: ${unitaProponente.descrizione}");
        if (servizio       		        != null) tooltip.add("Servizio: ${servizio.descrizione}");
		if (redattore 		        	!= null) tooltip.add("Redattore: ${redattore.nominativoSoggetto}");
		if (firmatario		        	!= null) tooltip.add("Firmatario: ${firmatario.nominativoSoggetto}");
		if (presidente		        	!= null) tooltip.add("Presidente: ${presidente.nominativoSoggetto}");
		if (segretario		        	!= null) tooltip.add("Segretario: ${segretario.nominativoSoggetto}");
        if (funzionario		        	!= null) tooltip.add("Funzionario: ${funzionario.nominativoSoggetto}");
		if (numeroAttoDal           	!= null) tooltip.add("Numero Atto Dal: ${numeroAttoDal}");
		if (numeroAttoAl            	!= null) tooltip.add("Numero Atto Al: ${numeroAttoAl}");
		if (annoAtto                	!= null) tooltip.add("Anno Atto: ${annoAtto}");
		if (registroAtto            	!= null) tooltip.add("Registro Atto: ${registroAtto}");
		if (numeroAtto2Dal           	!= null) tooltip.add("Secondo Numero Dal: ${numeroAtto2Dal}");
		if (numeroAtto2Al            	!= null) tooltip.add("Secondo Numero Al: ${numeroAtto2Al}");
		if (annoAtto2                	!= null) tooltip.add("Anno Secondo Numero: ${annoAtto2}");
		if (registroAtto2            	!= null) tooltip.add("Registro Secondo Numero: ${registroAtto2}");
		if (dataAdozioneDal         	!= null) tooltip.add("Data Adozione Dal: ${format(dataAdozioneDal)}");
		if (dataAdozioneAl          	!= null) tooltip.add("Data Adozione Al: ${format(dataAdozioneAl)}");
		if (dataEsecutivitaDal      	!= null) tooltip.add(l("label.ricerca.tooltip.dataEsecutivitaDal", dataEsecutivitaDal));
		if (dataEsecutivitaAl       	!= null) tooltip.add(l("label.ricerca.tooltip.dataEsecutivitaAl", dataEsecutivitaAl));
		if (dataScadenzaDal				!= null) tooltip.add("Data Scadenza Dal: ${format(dataScadenzaDal)}");
		if (dataScadenzaAl  			!= null) tooltip.add("Data Scadenza Al: ${format(dataScadenzaAl)}");
//		if (registroProposta        	!= null) tooltip.add(l("label.ricerca.tooltip.registroProposta", [registroProposta].toArray()));
		if (annoProposta            	!= null) tooltip.add(l("label.ricerca.tooltip.annoProposta", annoProposta));
		if (numeroPropostaDal       	!= null) tooltip.add(l("label.ricerca.tooltip.numeroPropostaDal", numeroPropostaDal));
		if (numeroPropostaAl        	!= null) tooltip.add(l("label.ricerca.tooltip.numeroPropostaAl", numeroPropostaAl));
		if (dataPropostaDal         	!= null) tooltip.add(l("label.ricerca.tooltip.dataPropostaDal", dataPropostaDal));
		if (dataPropostaAl          	!= null) tooltip.add(l("label.ricerca.tooltip.dataPropostaAl", dataPropostaAl));
		if (numeroProtocolloDal     	!= null) tooltip.add("Numero Protocollo Dal: ${numeroProtocolloDal}");
		if (numeroProtocolloAl      	!= null) tooltip.add("Numero Protocollo Al: ${numeroProtocolloAl}");
		if (annoProtocollo          	!= null) tooltip.add("Anno Protocollo: ${annoProtocollo}");
		if (registroProtocollo      	!= null) tooltip.add("Registro Protocollo: ${registroProtocollo}");
		if (tipologia 		        	!= null) tooltip.add("Tipologia: ${tipologia.titolo}");
		if (conImpegnoSpesa 			       ) tooltip.add("Solo con Impegno di Spesa");
		if (numeroAlboDal           	!= null) tooltip.add("Numero Albo Dal: ${numeroAlboDal}");
		if (numeroAlboAl            	!= null) tooltip.add("Numero Albo Al: ${numeroAlboAl}");
		if (annoAlbo                	!= null) tooltip.add("Anno Albo: ${annoAlbo}");
		if (dataPubblicazioneDal    	!= null) tooltip.add("Data Pubblicazione Dal: ${format(dataPubblicazioneDal)}");
		if (dataPubblicazioneAl     	!= null) tooltip.add("Data Pubblicazione Al: ${format(dataPubblicazioneAl)}");
		if (statoInvioCorteConti    	!= null) tooltip.add(l("label.ricerca.tooltip.corteConti.statoInvio", statoInvioCorteConti));
		if (dataInvioCorteContiDal  	!= null) tooltip.add(l("label.ricerca.tooltip.corteConti.dataInvioDal", dataInvioCorteContiDal));
		if (dataInvioCorteContiAl   	!= null) tooltip.add(l("label.ricerca.tooltip.corteConti.dataInvioAl", dataInvioCorteContiAl));
		if (commissione             	!= null) tooltip.add("Organo Deliberante: ${commissione.titolo}");
		if (tipoAllegato            	!= null) tooltip.add("Tipo Allegato: ${tipoAllegato.titolo}");
		if (tipoAllegatoVistoParere 	!= null) tooltip.add("Tipo Allegato Visto/Parere: ${tipoAllegatoVistoParere.titolo}");
        if(impattoGenerazionale         != null) tooltip.add(l("label.impattoGenerazionale")+"${impattoGenerazionale.titolo}");
        if (cup            	            != null) tooltip.add("CUP: ${cup}");
        if (cig            	            != null) tooltip.add("CIG: ${cig}");
		if (esito						!= "TUTTI") tooltip.add("Esito: ${listaEsiti[esito]}");
		if (tipoCertificato				!= "TUTTI") tooltip.add("Tipo Certificato: ${listaTipiCertificato[tipoCertificato]}");
        if (daMarcare	               ) tooltip.add("Da Marcare temporalente");
        if (ricercaConservazione && !attoConcluso) tooltip.add("Atto Concluso: No");

        if (oggettoRicorrente != null) {
            tooltip.add("Oggetto Ricorrente: ${oggettoRicorrente.oggetto}")
        }
        if (tipoBudget != null && tipoBudget.id > 0) {
            tooltip.add("Tipo Budget: ${tipoBudget.titolo}")
        }
        if (contoEconomico != null && !contoEconomico.isEmpty()) {
            tooltip.add("Conto Economico: ${contoEconomico}")
        }
        if (codiceProgetto != null && !codiceProgetto.isEmpty()) {
            tooltip.add("Codice Progetto: ${codiceProgetto}")
        }
        if (note != null && !note.isEmpty()) {
            tooltip.add("Note: ${note}")
        }
        if (incaricato != null) {
            tooltip.add("Incaricato: ${incaricato.nominativoSoggetto}")
        }
        if (dataConservazioneDal != null) tooltip.add("Data conservazione Dal: ${format(dataConservazioneDal)}");
        if (dataConservazioneAl != null)  tooltip.add("Data conservazione Al: ${format(dataConservazioneAl)}");
        if (importoTotaleDal != null) tooltip.add("Importo Totale Dal: ${importoTotaleDal}");
        if (importoTotaleAl != null)  tooltip.add("Importo Totale Al: ${importoTotaleAl}");

        StringBuffer text = new StringBuffer()
		for (String t : tooltip) {
            text.append("- ${t} \n")
		}
        return text.toString()
	}
	
	private String format (Date date) {
		return date.format ("dd/MM/yyyy")
	}
	
	private void caricaListaTipologia() {
		Class DomainTipologia = getProp("domainTipologia")
		listaTipologie = DomainTipologia.list([sort:'titolo', order:'asc']).toDTO()
        listaTipologie.add(0, DomainTipologia.newInstance(id: -1, titolo: '-- Non Valorizzato --').toDTO())
	}

    private void caricaImpattiGenerazionali(){
        listaImpattiGenerazionali = ImpattoGenerazionale.list([sort: 'titolo', order: 'asc']).toDTO()
        listaImpattiGenerazionali.add(0, new ImpattoGenerazionaleDTO(id: -1, titolo: NESSUN_VALORE))
    }

    private void listaTipiAllegato () {
        listaTipiAllegato = TipoAllegato.list([sort: 'titolo', order: 'asc']).toDTO()
        listaTipiAllegato.add(0, new TipoAllegatoDTO(id: -1, titolo: NESSUN_VALORE))
    }

    private void caricaListaStatiDocumento () {
        listaStatiDocumento = [NESSUN_VALORE] + StatoIter.createCriteria().list {
            projections {
                property ("stato")
            }
            eq('tipoDocumento', tipoDocumento)
            order("stato", "asc")
        }
    }

    private void caricaListaRelatori () {
        listaRelatori = []
        listaRelatori.add(0, new As4SoggettoCorrenteDTO(id: -1, denominazione: "-- Non Valorizzato --"))
        List<Long> idSoggetti = []
        idSoggetti.add(-1)
        //leggo le deleghe valide e le ordino per il campo sequenza
        List<Delega> listaDeleghe = Delega.findAllByValido(true, [sort: "sequenza", order: "asc"])
        for (delega in listaDeleghe) {
            //se ho già letto inserito nell'elenco l'ni di un assessore, evito di riaggiungerlo
            if (!(idSoggetti.contains(delega.assessore.id))) {
                listaRelatori.add(new As4SoggettoCorrenteDTO(id: delega.assessore.id, denominazione: delega.assessore.denominazione))
                idSoggetti.add(delega.assessore.id)
            }
        }
    }

    def caricaListaServizi() {
        if (abilitaRicercaServizio && tipoDocumento in (['DETERMINA','DELIBERA'])) {
            // ottengo la domain class che mappa la vista di ricerca
            Class DomainRicercaDocumento = getProp("domainRicerca")

            PagedResultList resultList = DomainRicercaDocumento.createCriteria().list(
                    max: pageSize,
                    offset: pageSize * activePage
            ) {
                eq('tipoSoggetto', TipoSoggetto.UO_SERVIZIO)

                projections {
                    groupProperty('uoServizio')
                    groupProperty('uoServizioDescrizione')
                }

                order('uoServizioDescrizione', 'asc')
            }

            listaServizi = resultList.collect { row -> row[0].toDTO() }

            totalSize = DomainRicercaDocumento.createCriteria().list {
                eq('tipoSoggetto', TipoSoggetto.UO_SERVIZIO)

                projections {
                    groupProperty("uoServizio")
                }
            }.size()
        }
    }

    private void caricaListaCategorie () {
        String tipoCategoria = getProp("tipoCategoria")
        listaCategorie = Categoria.findAllByTipoOggetto(tipoCategoria, [sort: "codice", order: "asc"]).toDTO()
        if (listaCategorie != null) {
            listaCategorie.add(0, new CategoriaDTO(id: -1, codice:  "-- Non Valorizzato --"))
        }
    }

    private void caricaListaOggettiRicorrenti () {
        if (tipoDocumento == Determina.TIPO_OGGETTO) {
            listaOggettiRicorrenti =
                    it.finmatica.atti.dizionari.OggettoRicorrente.findAllByValidoAndDetermina(true, true, [sort: "oggetto", order: "asc"]).toDTO()
        } else if (tipoDocumento == Delibera.TIPO_OGGETTO || tipoDocumento == PropostaDelibera.TIPO_OGGETTO) {
            listaOggettiRicorrenti =
                    it.finmatica.atti.dizionari.OggettoRicorrente.findAllByValidoAndDelibera(true, true, [sort: "oggetto", order: "asc"]).toDTO()
        }

        if (listaOggettiRicorrenti != null) {
            listaOggettiRicorrenti.add(0, new OggettoRicorrenteDTO(id: -1, oggetto: "-- Non Valorizzato --"))
        }
    }

    private void caricaListaBudget() {
        listaBudget = TipoBudget.findAllByValido(true).toDTO()
        listaBudget.add(0, new TipoBudgetDTO(id: -1, titolo: '-- Non Valorizzato --'))
    }

    private void caricaListaCommissioni () {
        listaCommissioni = Commissione.list([sort: "titolo", order: "asc"]).toDTO()
        listaCommissioni.add(0, new CommissioneDTO(id: -1, titolo: "-- Non Valorizzato --"))
    }

    void ricerca (def utente) {
        activePage = 0
        pagina(utente)
    }

    void pagina (So4UserDetail userDetail, boolean tutti = false) {
        //tutti indica se il risultato deve essere paginato o meno (in visualizzazione è paginato, nell'excel no)
        //inoltre con tutti i dati vengono raggruppati per budget (integrazione contabilita CE4). Nell'excel bisogna mostrare un
        //atto più volte (una volta per ogni budget), a video invece l'atto deve essere sempre uno

        // ottengo la domain class che mappa la vista di ricerca
        Class DomainRicercaDocumento = getProp("domainRicerca")

        // totale di righe
        totalSize = DomainRicercaDocumento.createCriteria().get() {
            projections {
                countDistinct("idDocumento")
            }

            // se ho l'utente, controllo le competenze
            if (userDetail != null) {
                controllaCompetenze(delegate)(userDetail)
            }

            // applico i filtri
            controllaFiltri(delegate)()
        }

        if (totalSize>0) {
            // risultato query
            listaDocumenti = DomainRicercaDocumento.createCriteria().list {
                projections {
                    groupProperty ("idDocumento")               // 0
                    groupProperty ("idDocumentoPrincipale")	    // 1
                    groupProperty ("tipoDocumento")	 		    // 2
                    groupProperty ("tipoDocumentoPrincipale")   // 3
                    groupProperty ("annoAtto")                  // 4
                    groupProperty ("numeroAtto")                // 5
                    groupProperty ("annoProposta")              // 6
                    groupProperty ("numeroProposta")            // 7
                    groupProperty ("oggetto")                   // 8
                    groupProperty ("titoloTipologia")           // 9
                    groupProperty ("stato")                 	// 10
                    groupProperty ("uoProponenteDescrizione")   // 11
                    groupProperty ("dataAdozione") 		        // 12
                    groupProperty ("stepNome") 		            // 13
                    groupProperty ("dataEsecutivita") 		    // 14
                    groupProperty ("dataScadenza")				// 15
                    groupProperty ("annoAtto2")                 // 16
                    groupProperty ("numeroAtto2")               // 17
                    groupProperty ("dataNumeroAtto2")           // 18
                    groupProperty ("registroAtto")              // 19
                    groupProperty ("registroAtto2")             // 20
                    groupProperty ("statoMarcatura")            // 21
                    groupProperty ("statoConservazione")        // 22
                    groupProperty ("logConservazione") 		    // 23
                    groupProperty ("cup")                       // 24
                    groupProperty ("cig")                       // 25
                    groupProperty ("dataControlloGestione")     // 26
                    groupProperty ("impattoGenerazionale")      // 27
                    if (note) {
                        groupProperty("note")                   // 28
                    }
                    else {
                        groupProperty ("idDocumento")           // 28
                    }
                    groupProperty ("numeroPassaggi")         // 29
                    groupProperty ("ultimoAttoreFlusso")      // 30
                    if (tutti) {
                        groupProperty ("elencoFirmatari")           // 31
                        groupProperty ("importoLiquidazione")     // 32
                        groupProperty("tipoBudget")                 // 33
                        groupProperty("contoEconomico")         // 34
                        groupProperty("codiceProgetto")          // 35
                        groupProperty("importo")                    // 36

                    }

                }

                // se ho l'utente, controllo le competenze
                if (userDetail != null) {
                    controllaCompetenze(delegate)(userDetail)
                }

                // applico i filtri
                controllaFiltri(delegate)()

                orderMap.each { k, v -> order(k, v) }
                if (! tutti){
                    firstResult (pageSize * activePage)
                    maxResults  (pageSize)

                    //max e' il numero di righe per pagina
                    //pagina e' il numero di pagina e parte da 0
                }
            }.collect { row -> [idDocumento               	: row[0],
                                idDocumentoPrincipale	 	: row[1],
                                tipoDocumento	 		 	: row[2],
                                tipoDocumentoPrincipale   	: row[3],
                                annoAtto                  	: row[4],
                                numeroAtto                	: row[5],
                                annoProposta              	: row[6],
                                numeroProposta            	: row[7],
                                oggetto                   	: row[8],
                                titoloTipologia           	: row[9],
                                stato                 	 	: row[10],
                                uoProponenteDescrizione   	: row[11],
                                dataAdozione 		     	: row[12],
                                titoloStep		 		 	: row[13],
                                dataEsecutivita				: row[14],
                                dataScadenza				: row[15],
                                annoAtto2                  	: row[16],
                                numeroAtto2                	: row[17],
                                dataNumeroAtto2             : row[18],
                                registroAtto                : row[19],
                                registroAtto2               : row[20],
                                statoMarcatura              : row[21],
                                statoConservazione          : getDescrizioneStatoConservazione(row[22]),
                                logConservazione 		 	: row[23],
                                cup                         : row[24],
                                cig                         : row[25],
                                dataControlloGestione       : row[26],
                                titoloImpattoGenerazionale  : getTitoloImpattoGenerazionale(row[27]),
                                note                        : row[28],
                                numeroPassaggi              : row[29],
                                ultimoAttoreFlusso          : row[30],
                                elencoFirmatari             : tutti ? row[31]: null,
                                importoLiquidazione         : tutti ? row[32]: null,
                                //tipoBudget                  : tutti ? row[29]: null,
                                titoloTipoBudget            : tutti ? row[33]?.titolo: null,
                                contoEconomico              : tutti ? row[34]: null,
                                codiceProgetto              : tutti ? row[35]: null,
                                importo                     : tutti ? row[36]: null,


                                descrizioneStato            : getDescrizioneStatoAtto(row[10], row[5]?.toString(), row[14]?.toString())
            ]}
        } else {
            listaDocumenti = []
        }
    }

	private def controllaFiltri (def delegate) {
		
		if (filtroAggiuntivo != null) {
			filtroAggiuntivo.delegate = delegate
		}
		
		def c = {
			
			// campi di ricerca "esterni"
			if (anno > 0) {
				or {
					eq ("annoAtto", 	anno)
					eq ("annoProposta", anno)

                    if (Impostazioni.SECONDO_NUMERO_DETERMINA.abilitato) {
                        eq ("annoAtto2", anno)
                    }
				}
			}

            // ricerca con i campi "esterni" in caso di seconda numerazione abilitata:
            // do' precedenza al secondo numero, se questo non è presente, cerco il primo.
            if (numeroAtto > 0) {
                if (Impostazioni.SECONDO_NUMERO_DETERMINA.abilitato) {
                    or {
                        eq ("numeroAtto2", numeroAtto)
                        and {
                            isNull ("numeroAtto2", )
                            eq ("numeroAtto", numeroAtto)
                        }
                    }
                } else {
				    eq ("numeroAtto", numeroAtto)
                }
            }

			if (numeroProposta) {
				eq ("numeroProposta", numeroProposta)
			}

            // campi di ricerca dell'atto
            if (annoAtto > 0) {
                eq("annoAtto", annoAtto)
            }

            if (numeroAttoDal > 0) {
                ge("numeroAtto", numeroAttoDal)
            }

            if (numeroAttoAl > 0) {
                le("numeroAtto", numeroAttoAl)
            }

			if (registroAtto != null) {
                if (Impostazioni.SECONDO_NUMERO_DETERMINA.abilitato) {
				    or {
                        eq ("registroAtto", registroAtto)
                        eq ("registroAtto2", registroAtto)
                    }
                } else {
				    eq ("registroAtto", registroAtto)
                }
			}

            if (idDocumento != null) {
                eq("idDocumento", idDocumento)
            }

            if (idDocumentoPrincipale != null) {
                eq("idDocumentoPrincipale", idDocumentoPrincipale)
            }

            // campi di ricerca del secondo numero dell'atto: http://svi-redmine/issues/22205
            if (annoAtto2 > 0) {
                eq("annoAtto2", annoAtto2)
            }

            if (numeroAtto2Dal > 0) {
                ge("numeroAtto2", numeroAtto2Dal)
            }

            if (numeroAtto2Al > 0) {
                le("numeroAtto2", numeroAtto2Al)
            }

            if (registroAtto2 != null) {
                eq("registroAtto2", registroAtto2)
            }

            // ricerca della proposta
            if (annoProposta > 0) {
                eq("annoProposta", annoProposta)
            }

            if (numeroPropostaDal > 0) {
                ge("numeroProposta", numeroPropostaDal)
            }

            if (numeroPropostaAl > 0) {
                le("numeroProposta", numeroPropostaAl)
            }

            // dati di protocollo
            if (annoProtocollo > 0) {
                eq("annoProtocollo", annoProtocollo)
            }

            if (numeroProtocolloDal > 0) {
                ge("numeroProtocollo", numeroProtocolloDal)
            }

            if (numeroProtocolloAl > 0) {
                le("numeroProtocollo", numeroProtocolloAl)
            }

            // dati del documento
            if (oggetto?.trim()?.length() > 0) {
                or {
                    ilike("oggetto", "%${oggetto}%")
                    if (cercaNelTesto) {
                        createAlias("allegato", "all", CriteriaSpecification.LEFT_JOIN)

                        or {
                            testo {
                                ilike("testo", "%${oggetto}%")
                            }
                            ilike("all.testo", "%${oggetto}%")
                        }
                    }
                }
            }

            if (ricercaConservazione) {
                and {
                    eq("attoConcluso", attoConcluso)

                    or {
                        if (statoConservazione != null && (statoConservazione.equals("DC") || statoConservazione.equals("ALL") )) {
                            eq("statoConservazione", StatoConservazione.DA_CONSERVARE.toString())
                            and {
                                eq("statoConservazione", StatoConservazione.IN_CONSERVAZIONE.toString())
                                isNotNull("logConservazione")
                            }
                        }
                        if (statoConservazione != null && (statoConservazione.equals("ER") || statoConservazione.equals("ALL") )) {
                            eq("statoConservazione", StatoConservazione.ERRORE.toString())
                            eq("statoConservazione", StatoConservazione.ERRORE_INVIO.toString())
                        }
                    }
                }
            }
            else {
                if (statoConservazione != null && statoConservazione.equals("Y")) {
                    eq("statoConservazione", StatoConservazione.CONSERVATO.toString())
                }
                if (statoConservazione != null && statoConservazione.equals("N")) {
                    or {
                        ne("statoConservazione", StatoConservazione.CONSERVATO.toString())
                        isNull("statoConservazione")
                    }
                }
            }

            if (dataConservazioneDal != null) {
                ge("dataConservazione", dataConservazioneDal)
            }

            if (dataConservazioneAl != null) {
                le("dataConservazione", dataConservazioneAl)
            }

            if (stato != null && stato != NESSUN_VALORE) {
                eq("stato", stato)
            }

            if (dataAdozioneDal != null) {
                ge("dataAdozione", dataAdozioneDal)
            }

            if (dataAdozioneAl != null) {
                le("dataAdozione", dataAdozioneAl)
            }

            if (dataEsecutivitaDal != null) {
                ge("dataEsecutivita", dataEsecutivitaDal)
            }

            if (dataEsecutivitaAl != null) {
                le("dataEsecutivita", dataEsecutivitaAl)
            }

            if (dataScadenzaDal != null) {
                ge("dataScadenza", dataScadenzaDal)
            }

            if (dataScadenzaAl != null) {
                le("dataScadenza", dataScadenzaAl)
            }

            // dati di pubblicazione
            if (dataPubblicazioneDal != null) {
                or {
                    ge("dataPubblicazione", dataPubblicazioneDal)
                    ge("dataFinePubblicazione", dataPubblicazioneDal)
                }

            }

            if (dataPubblicazioneAl != null) {
                or {
                    le("dataPubblicazione", dataPubblicazioneAl)
                    le("dataFinePubblicazione", dataPubblicazioneAl)
                }
            }

            if (annoAlbo > 0) {
                eq("annoAlbo", annoAlbo)
            }

            if (numeroAlboDal > 0) {
                ge("numeroAlbo", numeroAlboDal)
            }

            if (numeroAlboAl > 0) {
                le("numeroAlbo", numeroAlboAl)
            }

            if (tipologia != null && tipologia.id > 0) {
                eq("idTipologia", tipologia.id)
            }

            if (dataPropostaDal != null) {
                ge("dataProposta", dataPropostaDal)
            }

            if (dataPropostaAl != null) {
                le("dataProposta", dataPropostaAl)
            }

            if (commissione != null && commissione.id > 0) {
                eq("commissione.id", commissione.id)
            }

            if (categoriaAbilitata && categoria != null && categoria.id > 0) {
                eq("categoria.id", categoria.id)
            }

            if (impattoGenerazionaleAbilitato && impattoGenerazionale?.id > 0) {
                eq("impattoGenerazionale.id",impattoGenerazionale.id)
            }


            // campo aggiunto per la ricerca dell'oggetto ricorrente: http://svi-redmine/issues/22462
            if (oggettoRicorrente != null) {
                if (oggettoRicorrente.id > 0) {
                    eq("oggettoRicorrente.id", oggettoRicorrente.id)
                } else {
                    isNull("oggettoRicorrente")
                }
            }

            if (tipoBudget != null && tipoBudget.id > 0){
                eq("tipoBudget.id", tipoBudget.id)
            }
            if (contoEconomico != null && !contoEconomico.isEmpty()) {
                eq("contoEconomico", contoEconomico)
            }
            if (codiceProgetto != null && !codiceProgetto.isEmpty()){
                eq("codiceProgetto", codiceProgetto)
            }
            if (note != null && !note.isEmpty()){
                ilike("note", "%${note}%")
            }

            // gestione della corte dei conti: http://svi-redmine/issues/12267
            switch (statoInvioCorteConti) {
                case MovimentoContabile.STATO_DA_INVIARE:
                    eq("daInviareCorteConti", true)
                    break;

                case MovimentoContabile.STATO_DA_NON_INVIARE:
                    eq("daInviareCorteConti", false)
                    break;

                case MovimentoContabile.STATO_INVIATO:
                    isNotNull("dataInvioCorteConti")

                    if (dataInvioCorteContiDal != null) {
                        ge("dataInvioCorteConti", dataInvioCorteContiDal)
                    }

                    if (dataInvioCorteContiAl != null) {
                        le("dataInvioCorteConti", dataInvioCorteContiAl)
                    }

                    break;

                case MovimentoContabile.STATO_NON_INVIATO:
                    eq("daInviareCorteConti", true)
                    isNotNull("dataInvioCorteConti")
                    break;
            }

            if (tipoAllegato?.id > 0) {
                eq(getProp("tipoAllegato"), tipoAllegato?.id)
            }

            if (tipoAllegatoVistoParere?.id > 0) {
                eq(getProp("tipoAllegatoVistoParere"), tipoAllegatoVistoParere?.id)
            }

            if (conImpegnoSpesa) {
                eq("conImpegnoSpesa", true)
            }

            if (tipoCertificato != TUTTI) {
                eq("tipo", tipoCertificato)
            }

            if (esito != TUTTI && esito != null) {
                eq("esito", esito)
            }

            if (filtroAggiuntivo != null) {
                filtroAggiuntivo()
            }

            if (inEvidenza>0) {
                if(inEvidenza==1) {
                    eq("inEvidenza", true)

                    if (dataInEvidenzaDa != null) {
                        ge("dataInEvidenzaDa", dataInEvidenzaDa)
                    }

                    if (dataInEvidenzaA != null) {
                        le("dataInEvidenzaA", dataInEvidenzaA)
                    }
                }
                if(inEvidenza==2) {
                    eq("inEvidenza", false)
                }
            }

            if (importoTotaleDal != null) {
                ge("importoTotale", importoTotaleDal)
            }

            if (importoTotaleAl != null) {
                le("importoTotale", importoTotaleAl)
            }

            or {
                if (relatore != null && relatore.id > -1) {
                    // il campo relatore della vista contiene gli id di tutte le deleghe (principale + aggiuntive) separati da '#'
                    sqlRestriction "'#'||ni_relatore||'#' like '%#${relatore.id}#%'"
                }
                if (redattore != null) {
                    and {
                        eq("utenteSoggetto.id", redattore.id)
                        eq("tipoSoggetto", TipoSoggetto.REDATTORE)
                    }
                }
                if (firmatario != null) {
                    and {
                        eq("utenteSoggetto.id", firmatario.id)
                        eq("tipoSoggetto", TipoSoggetto.DIRIGENTE)
                    }
                }
                if (presidente != null) {
                    and {
                        eq("utenteSoggetto.id", presidente.id)
                        eq("tipoSoggetto", TipoSoggetto.PRESIDENTE)
                    }
                }
                if (segretario != null) {
                    and {
                        eq("utenteSoggetto.id", segretario.id)
                        eq("tipoSoggetto", TipoSoggetto.SEGRETARIO)
                    }
                }
                if (incaricato != null) {
                    and {
                        eq("utenteSoggetto.id", incaricato.id)
                        eq("tipoSoggetto", TipoSoggetto.INCARICATO)
                    }
                }
                if (funzionario != null) {
                    and {
                        eq("controlloFunzionario", true)
                        eq("utenteSoggetto.id", funzionario.id)
                        eq("tipoSoggetto", TipoSoggetto.FUNZIONARIO)
                    }
                }
                if (unitaProponente != null) {
                    and {
                        eq("uoProponente.progr", unitaProponente.progr)
                        eq("uoProponente.ottica.codice", unitaProponente.ottica.codice)
                        //eq("uoProponente.dal", 			unitaProponente.dal) 		// #21542
                        eq("tipoSoggetto", TipoSoggetto.UO_PROPONENTE)
                    }
                }
                if (servizio != null) {
                    and {
                        eq("uoServizio.progr", servizio.progr)
                        eq("uoServizio.ottica.codice", servizio.ottica.codice)
                        eq("tipoSoggetto", TipoSoggetto.UO_SERVIZIO)
                    }
                }

                // specifico di visti, pareri e certificati
                if (firmatarioVisto != null) {
                    eq("utenteFirmatario.id", firmatarioVisto.id)
                }

                if (unitaVisto != null) {
                    and {
                        eq("unitaRedazione.progr", unitaVisto.progr)
                        eq("unitaRedazione.ottica.codice", unitaVisto.ottica.codice)
                        eq("unitaRedazione.dal", unitaVisto.dal)
                    }
                }
            }
            if (ricercaMarcatura) {
                if (annoProtocollo == null) {
                    isNull("annoProtocollo")
                }
                if (numeroProtocolloAl == null && numeroProtocolloDal == null) {
                    isNull("numeroProtocollo")
                }

                if (daMarcare) {
                    eq ("statoMarcatura", StatoMarcatura.DA_MARCARE)
                }
                else {
                    eq ("statoMarcatura", StatoMarcatura.MARCATO)
                }
            }

            if (cup != null){
                ilike("cup", "%${cup}%")
            }

            if (cig != null){
                ilike("cig", "%${cig}%")
            }

            // utilizzati dai ws di ricerca (http://svi-redmine/issues/63219)
            //nelle viste sono valorizzati solo per DELIBERA, PROPOSTA_DELIBERA e DETERMINA
            if (uoProponenteCodice != null) {
                eq("uoProponenteCodice", uoProponenteCodice)
            }

            if (codiceEsternoTipologia != null) {
                eq("codiceEsternoTipologia", codiceEsternoTipologia)
            }

            if (tipoPubblicazioneAlbo != null) {
                eq("tipoPubblicazioneAlbo", tipoPubblicazioneAlbo)
            }

            //campi relativi a classifica e fascicolo usati solo dal ws di ricerca (http://svi-redmine/issues/63219)
            if (classificaCodice != null) {
                eq("classificaCodice", classificaCodice)
            }

            if (fascicoloAnno != null) {
                eq("fascicoloAnno", fascicoloAnno)
            }

            if (fascicoloNumero != null) {
                eq("fascicoloNumero", fascicoloNumero)
            }
        }

        c.delegate = delegate
        return c
    }

    def ricercaSoggetti (String filtro, String tipoSoggetto, int pageSize, int activePage) {
        // ottengo la domain class che mappa la vista di ricerca
        Class DomainRicercaDocumento = getProp("domainRicerca");

        // risultato query
        def listaUtentiAd4 = DomainRicercaDocumento.createCriteria().list {
            projections {
                utenteSoggetto {
                    distinct("id")
                }
            }
            eq("tipoSoggetto", tipoSoggetto)
        }.collect { row -> Ad4Utente.get(row)?.toDTO() }

        int totalSize = listaUtentiAd4.size()
        if (filtro) {
            totalSize = Ad4Utente.createCriteria().get {
                projections {
                    countDistinct("id")
                }
                'in'("id", listaUtentiAd4*.id)
                ilike("nominativoSoggetto", "%${filtro}%")
                order("nominativoSoggetto", "asc")
            }

            listaUtentiAd4 = Ad4Utente.createCriteria().list {
                'in'  ("id", listaUtentiAd4*.id)
                ilike("nominativoSoggetto", "%${filtro}%")
                order("nominativoSoggetto", "asc")

                firstResult(pageSize * activePage)
                maxResults(pageSize)
            }.toDTO()


        }

        return [totalCount: totalSize, lista: listaUtentiAd4]
    }

    def ricercaUoProponente (String filtro, int pageSize, int activePage) {
        // ottengo la domain class che mappa la vista di ricerca
        Class DomainRicercaDocumento = getProp("domainRicerca");

        PagedResultList resultList = DomainRicercaDocumento.createCriteria().list(
                max: pageSize,
                offset: pageSize * activePage
        ) {
            eq('tipoSoggetto', TipoSoggetto.UO_PROPONENTE)

            projections {
                groupProperty("uoProponente")
                groupProperty("uoProponenteDescrizione")
            }

            if (filtro) {
                ilike("uoProponenteDescrizione", "%${filtro}%")
            }
            order("uoProponenteDescrizione", "asc")

        }

        listaUnita = resultList.collect { row -> row[0].toDTO() }

        totalSize = DomainRicercaDocumento.createCriteria().list {
            eq('tipoSoggetto', TipoSoggetto.UO_PROPONENTE)

            projections {
                groupProperty("uoProponente")
            }

            if (filtro) {
                ilike("uoProponenteDescrizione", "%${filtro}%")
            }
        }.size()

        return [totalCount: totalSize, lista: listaUnita]
    }

    /**
     * Criterio di controllo delle competenze (appiattite nella vista documenti competenze):
     *
     *  1) utente indicato pari all'utente loggato (chiamato successivamente #delegate)
     *  2) per ogni uo di #delegate (successivamente indicata come #uoiesima) verifico che
     * 		a) l'unità indicata è #uoiesima e il ruolo è nullo
     * 		b) per ogni ruolo che #delegate ha nella #uoiesima (successivamente chiamato #ruoloiesimo) verifico che
     * 			->  l'unità è nulla o pari a #uoiesima e il ruolo sia pari a #ruoloiesimo
     *
     *  NB1: non è consentito avere uo, ruolo, utente tutti nulli per indicare con competenze a tutti altrimenti la query è lentissima
     * 	NB2: query ottimizzata grazie agli indici!
     *
     * @param delegate
     * @return
     */
    private def controllaCompetenze (def delegate) {
        if (AttiUtils.isUtenteAmministratore()) {
            // se sono utente amministratore, ritorno una closure che non fa niente:
            return { So4UserDetail utente ->
                // non fa niente.
            }
        }

        return AttiGestoreCompetenze.controllaCompetenze(delegate, "compUtente", "compUnita", "compRuolo");
    }

    /**
     * Metodo per la modifica dell'ordinamento delle colonne.
     * Inserisce in testa il campo con l'ordinamento specificato eliminandolo dall'elenco degli ordinamenti se già presente.
     * @param campo
     * @param ordinamento
     */
    void modificaColonnaOrdinamento (String campo, String ordinamento) {
        orderMap.remove(campo)
        orderMap = [(campo): ordinamento] + orderMap
    }

    private String getDescrizioneStatoConservazione(String stato){
        if (stato == null || stato.isEmpty()) {
            return "";
        }
        if (it.finmatica.atti.documenti.StatoConservazione.DA_CONSERVARE.toString() == stato) {
            return "Da Conservare";
        }
        if (it.finmatica.atti.documenti.StatoConservazione.IN_CONSERVAZIONE.toString() == stato) {
            return "Conservazione in Corso";
        }
        if (it.finmatica.atti.documenti.StatoConservazione.ERRORE.toString() == stato) {
            return "Errore in Conservazione";
        }
        if (it.finmatica.atti.documenti.StatoConservazione.ERRORE_INVIO.toString() == stato) {
            return "Errore di invio in Conservazione";
        }
        if (it.finmatica.atti.documenti.StatoConservazione.CONSERVATO.toString() == stato) {
            return "Conservato";
        }
    }

    private String getTitoloImpattoGenerazionale(ImpattoGenerazionale impattoGenerazionale){
        return impattoGenerazionale?.titolo?:""

    }

    private String getDescrizioneStatoAtto(String stato, String numeroAtto, String dataEsecutivita){
        if (stato == null || stato.isEmpty()) {
            return "";
        }

        if ("ANNULLATO".equals(stato) && (numeroAtto == null || numeroAtto.isEmpty())) {
            return l("label.ricerca.propostaAnnullata")
        }

        if ("NON_ESECUTIVO".equals(stato) && (!(numeroAtto == null || numeroAtto.isEmpty())) && (dataEsecutivita == null || dataEsecutivita.isEmpty())) {
            //l'atto è stato numerato ma non è mai diventato esecutivo (visto contabile contrario)
            return l("label.ricerca.attoNonEsecutivo")
        }

        if ("ANNULLATO".equals(stato) && (!(numeroAtto == null || numeroAtto.isEmpty())) && (dataEsecutivita == null || dataEsecutivita.isEmpty())) {
            //l'atto è stato numerato ma non è mai diventato esecutivo (visto contabile contrario) ed è annullato da un altro atto (già esecutivo)
            return l("label.ricerca.attoAnnullato")
        }

        if (("ANNULLATO".equals(stato) || "NON_ESECUTIVO".equals(stato)) && (!(dataEsecutivita == null || dataEsecutivita.isEmpty()))) {
            return l("label.ricerca.attoAnnullato")
        }

        return stato
    }

    void setCategoriaDaCodice(String tipoDocumento, String codiceCategoria) {
        if (tipoDocumento == Determina.TIPO_OGGETTO) {
            categoriaAbilitata = Impostazioni.CATEGORIA_DETERMINA.abilitato
        } else if (tipoDocumento == Delibera.TIPO_OGGETTO || tipoDocumento == PropostaDelibera.TIPO_OGGETTO) {
            categoriaAbilitata = Impostazioni.CATEGORIA_PROPOSTA_DELIBERA.abilitato
        }

        if (categoriaAbilitata) {
            categoria = Categoria.createCriteria().get {
                eq("codice", codiceCategoria)
                eq("tipoOggetto", tipoDocumento)
            }.toDTO()
        }
    }
}