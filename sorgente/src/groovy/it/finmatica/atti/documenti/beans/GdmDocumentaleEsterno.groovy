package it.finmatica.atti.documenti.beans

import groovy.sql.Sql
import it.finmatica.atti.*
import it.finmatica.atti.documenti.*
import it.finmatica.atti.dto.documenti.AllegatoDTO
import it.finmatica.atti.dto.documenti.AllegatoDTOService
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdmConfig
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.dmServer.util.Global
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.jdmsutil.data.ProfiloExtend
import it.finmatica.jdmsutil.data.ProfiloVersionExtend
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource
import java.sql.Connection

class GdmDocumentaleEsterno implements IDocumentaleEsterno {

	// logger
	private static final Logger log = Logger.getLogger(GdmDocumentaleEsterno.class)

    public static final String CODICE_DOCUMENTALE_ESTERNO = "GDM"

	// costanti
	public static final String AREA                      = "SEGRETERIA.ATTI.2_0"
	public static final String MODELLO_DETERMINA         = "DETERMINA"
	public static final String MODELLO_VISTO             = "VISTO"
	public static final String MODELLO_ALLEGATO          = "ALLEGATO"
	public static final String MODELLO_PROPOSTA_DELIBERA = "PROPOSTA_DELIBERA"
	public static final String MODELLO_DELIBERA          = "DELIBERA"
	public static final String MODELLO_CERTIFICATO       = "CERTIFICATO"
	public static final String MODELLO_SEDUTA_STAMPA     = "SEDUTA_STAMPA"

	public static final String GDM_USER					= "GDM"
	public static final String GDM_PASSWORD				= null

	public static final String RIFERIMENTO_CERTIFICATI		= "CERT"
	public static final String RIFERIMENTO_ALLEGATI		= "ALLEGATI"
	public static final String RIFERIMENTO_VISTI			= "VISTI"

	// services
	AllegatoDTOService allegatoDTOService

	// connessione al db
	DataSource dataSource_gdm

	// beans
	IGestoreFile gestoreFile
	GrailsApplication grailsApplication

	@Transactional
	void storicizzaDocumento (IDocumentoStoricoEsterno documento) {
		log.debug ("storicizzaDocumento: ${documento.class.name}:${documento.id}")

		Connection conn 	= dataSource_gdm.connection
		log.debug ("Creo il proviloVersion")
        ProfiloVersionExtend profiloDocumentoEsterno = new ProfiloVersionExtend(documento.idDocumentoEsterno, documento.revisione,
                                                                                GdmDocumentaleEsterno.GDM_USER, null, conn)

		log.debug ("Versiono il profilo")
		profiloDocumentoEsterno.versiona()

		documento.versioneDocumentoEsterno = profiloDocumentoEsterno.getVersioneDocumento()
		log.debug ("Ottengo la versione: ${documento.versioneDocumentoEsterno} e committo.")
	}

	@Transactional
	void salvaDocumento (IDocumentoEsterno documento) {
		ProfiloExtend profiloDocumentoEsterno

		Connection conn 	= dataSource_gdm.connection
        profiloDocumentoEsterno = salvaDocumentoGdm (conn, documento)

		if (!profiloDocumentoEsterno.accedi(Global.ACCESS_NO_ATTACH).booleanValue()) {
			throw new AttiRuntimeException ("Errore nella accedi dopo la commit su gdm.", profiloDocumentoEsterno.getLastException())
		}

		documento.idDocumentoEsterno = Long.parseLong (profiloDocumentoEsterno.getDocNumber())

		log.debug ("GdmDocumentaleEsterno.salvaDocumento() idDocumentoEsterno = ${documento.idDocumentoEsterno}")
		documento.save()
		
		// dopo che ho salvato il documento, tento di fascicolarlo:
		// fascicolo il documento solo se sono integrato con il protocollogdm. Non inietto direttamente il bean del protocollo perché incorrerei in dipendenze circolari siccome il bean del protocollo richiede questo stesso bean.
		if (documento instanceof IFascicolabile && "protocolloEsternoGdm".equals(Impostazioni.PROTOCOLLO.valore)) {
			((IProtocolloEsterno) grailsApplication.mainContext.getBean("protocolloEsternoGdm")).fascicola (documento)
		}
	}

	@Override
	@Transactional
	String getUrlDocumento (IDocumentoEsterno documento) {
		String f_get_url_oggetto = "select gdc_utility_pkg.f_get_url_oggetto (:urlServer, '', :idDocumento, 'D', '', '', '', 'R', '', '', '5', 'N') URL_DOCUMENTO from dual"
        String urlDocumento = new Sql(dataSource_gdm).rows(f_get_url_oggetto, [urlServer: Impostazioni.URL_SERVER_GDM.valore, idDocumento: documento.idDocumentoEsterno.toString()])[0].URL_DOCUMENTO

        // se la funzione ritorna un url relativo, di qualche sorta, aggiungo l'url assoluto del server.
        // faccio questo per due ragioni:
        // 1) ha senso che la funzione getUrlDocumento ritorni un url univoco di un certo documento. Per essere "valido" tale url deve essere assoluto.
        // 2) il side-effect di ritornare un url "relativo" (ad es: "../../aspr...." o: "/Protocollo/standalone.zul") è che fa casino con ZK che tenta di aprire la pagina e aggiunge il nome del contesto all'url.
        if (!urlDocumento.startsWith(Impostazioni.URL_SERVER_GDM.valore)) {
			if (urlDocumento.startsWith("/")) {
				return Impostazioni.URL_SERVER_GDM.valore + urlDocumento
			} else {
            	return Impostazioni.URL_SERVER_GDM.valore + "/" + urlDocumento
			}
        }

        return urlDocumento
	}

    @Transactional
    AllegatoDTO importAllegati (AllegatoDTO allegato,List<String> lista) {
        for (String elemento : lista) {
            String[] seq = elemento.split("@")
            allegato = importAllegato(allegato, seq[1], seq[0])
        }
        return allegato
    }

	private java.sql.Date getDateSql (Date d) {
		return (d == null) ? null : new java.sql.Date (d.getTime())
	}

	private Map<String, String> getDocumentiCollegati(IDocumentoEsterno d) {
		Map<String, String> collegamento = [:]

		DocumentoCollegato dc = DocumentoCollegato.createCriteria().get() {
			if (d instanceof Determina) {
				eq ("determinaCollegata.id", d.id)
			} else if (d instanceof PropostaDelibera) {
				eq ("deliberaCollegata.id", d.id)
			}
			ne ("operazione", DocumentoCollegato.OPERAZIONE_COLLEGA)
			maxResults(1)
			order("lastUpdated", "desc")
		}

		if (d instanceof Determina && dc?.determinaPrincipale) {
			collegamento = [DOC_COLLEGATO:dc.determinaPrincipale?.id, TIPO_COLLEGATO:dc.operazione]
			log.debug "Calcolo del documento collegato (determinaCollegata,determinaPrincipale,operazione)::(${d.id},${dc?.determinaPrincipale?.id},${dc?.operazione})"
		} else if (d instanceof PropostaDelibera && dc?.propostaDeliberaPrincipale) {
			collegamento = [DOC_COLLEGATO:dc.propostaDeliberaPrincipale?.id, TIPO_COLLEGATO:dc.operazione]
			log.debug "Calcolo del documento collegato (deliberaCollegata,propostaDeliberaPrincipale,operazione)::(${d.id},${dc?.propostaDeliberaPrincipale?.id},${dc?.operazione})"
		}

		return collegamento
	}

	private Firmatario getUltimoFirmatario (def documento) {
		def f = documento?.firmatari?.findAll { it.firmato }?.sort { it.dataFirma }
		if (f?.size() > 0) {
			return f.last();
		}
		
		return null;
	}

	private ProfiloExtend salvaDocumentoGdm (Connection conn, Determina d) {
		ProfiloExtend deteGdm = getDocumentoGdm(conn, d, MODELLO_DETERMINA)

		salvaDatiComuni(deteGdm, d)
		salvaDatiProtocollo(deteGdm, d)
		salvaDatiPubblicazione(deteGdm, d)

		deteGdm.settaValore("ANNO_DETERMINA", d.annoDetermina)
		deteGdm.settaValore("ANNO_PROPOSTA", d.annoProposta)
		deteGdm.settaValore("DAL_UNITA_PROPONENTE", getDateSql(d.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.dal))
		deteGdm.settaValore("DAL_UNITA_REDATTORE", getDateSql(d.getSoggetto(TipoSoggetto.REDATTORE)?.unitaSo4?.dal))
		deteGdm.settaValore("DATA_ESECUTIVITA", getDateSql(d.dataEsecutivita))
		deteGdm.settaValore("DATA_NUMERO_DETERMINA", getDateSql(d.dataNumeroDetermina))
		deteGdm.settaValore("DATA_NUMERO_PROPOSTA", getDateSql(d.dataNumeroProposta))
		deteGdm.settaValore("DESCRIZIONE_TIPO_DETERMINA", d.tipologia.descrizione ?: "")
		deteGdm.settaValore("DESCR_REGISTRO_DETERMINA", d.registroDetermina?.descrizione ?: "")
		deteGdm.settaValore("DESCR_REGISTRO_PROPOSTA", d.registroProposta?.descrizione ?: "")
		deteGdm.settaValore("DIRIGENTE", d.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.nominativoSoggetto ?: "")
		deteGdm.settaValore("FIRMATARIO", d.getSoggetto(TipoSoggetto.FIRMATARIO)?.utenteAd4?.nominativoSoggetto ?: "")
		deteGdm.settaValore("FUNZIONARIO", d.getSoggetto(TipoSoggetto.FUNZIONARIO)?.utenteAd4?.nominativoSoggetto ?: "")
		deteGdm.settaValore("ID_CONSIP", d.categoria?.id)
		deteGdm.settaValore("DESCRIZIONE_CONSIP", d.categoria?.descrizione ?: "")
		deteGdm.settaValore("ID_REGISTRO_DETERMINA", d.registroDetermina?.codice ?: "")
		deteGdm.settaValore("ID_REGISTRO_PROPOSTA", d.registroProposta?.codice ?: "")
		deteGdm.settaValore("ID_TIPO_DETERMINA", d.tipologia.id ?: "")
		deteGdm.settaValore("NOTE", d.note ?: "")
		deteGdm.settaValore("NOTE_CONTABILI", d.noteContabili ?: "")
		deteGdm.settaValore("NUMERO_DETERMINA", d.numeroDetermina ?: "")
		deteGdm.settaValore("NUMERO_PROPOSTA", d.numeroProposta ?: "")
		deteGdm.settaValore("OGGETTO", d.oggetto ?: "")
		deteGdm.settaValore("PROGR_UNITA_PROPONENTE", d.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.progr ?: "")
		deteGdm.settaValore("PROGR_UNITA_REDATTORE", d.getSoggetto(TipoSoggetto.REDATTORE)?.unitaSo4?.progr ?: "")
		deteGdm.settaValore("REDATTORE", d.getSoggetto(TipoSoggetto.REDATTORE)?.utenteAd4?.nominativoSoggetto ?: "")
		deteGdm.settaValore("UNITA_PROPONENTE", d.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.descrizione ?: "")
		deteGdm.settaValore("UNITA_REDATTORE", d.getSoggetto(TipoSoggetto.REDATTORE)?.unitaSo4?.descrizione ?: "")
		deteGdm.settaValore("UTENTE_AD4_DIRIGENTE", d.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.id ?: "")
		deteGdm.settaValore("UTENTE_AD4_FIRMATARIO", d.getSoggetto(TipoSoggetto.FIRMATARIO)?.utenteAd4?.id ?: "")
		deteGdm.settaValore("UTENTE_AD4_FUNZIONARIO", d.getSoggetto(TipoSoggetto.FUNZIONARIO)?.utenteAd4?.id ?: "")
		deteGdm.settaValore("UTENTE_AD4_REDATTORE", d.getSoggetto(TipoSoggetto.REDATTORE)?.utenteAd4?.id ?: "")
		deteGdm.settaValore("DESCR_UO_DIRIGENTE", d.getSoggetto(TipoSoggetto.DIRIGENTE)?.unitaSo4?.descrizione ?: "")
		deteGdm.settaValore("TIPO_DOCUMENTO", MappingIntegrazione.getValoreEsterno(ProtocolloGdmConfig.MAPPING_CATEGORIA, ProtocolloGdmConfig.MAPPING_CODICE_TIPO_DOCUMENTO, d.tipologia.id.toString(), ""))

		if (Impostazioni.DOCER.abilitato) {
			deteGdm.settaValore("ID_DOCUMENTO_DOCER", d.idDocumentoDocer ?: "")
			log.debug "Gdm service salva determina - Settato eventuale idDocumentoDocer per DOCER ${d.idDocumentoDocer}"
		}

		log.debug "Gdm service salva determina - Setto eventuale documento collegato"
		def collegamento = getDocumentiCollegati(d)

		if (collegamento.size() > 0) {
			deteGdm.settaValore("DOC_COLLEGATO",	collegamento.get('DOC_COLLEGATO'));
			deteGdm.settaValore("TIPO_COLLEGATO",	collegamento.get('TIPO_COLLEGATO'));
		}

		if (d.testo != null && d.idDocumentoEsterno != null) {
			log.debug "Gdm service salva determina testo determina " + gestoreFile
			deteGdm.setFileName(d.testo.nome, gestoreFile.getFile(d, d.testo))
		}

		if (d.stampaUnica != null && d.idDocumentoEsterno != null) {
			log.debug "Gdm service salva determina stampa unica " + gestoreFile
			deteGdm.setFileName(d.stampaUnica.nome, gestoreFile.getFile(d, d.stampaUnica))
		}

		if (!deteGdm.salva().booleanValue()) {
			log.error("deteGdm.getErrorPostSave: ${deteGdm.getErrorPostSave()}");
			log.error("deteGdm.getError: ${deteGdm.getError()}");
			log.error ("errore in salva determina gdm", deteGdm.getLastException())
			throw new AttiRuntimeException ("Errore nel salvare la determina: ${deteGdm.getError()}")
		}

		return deteGdm
	}

	/**
	 * Ritorna il codice dell'ente concatenato alla stringa UPDATE_DA_AGSDE2- in modo tale da poter evitare di far scattare il trigger dalle tabelle gdm.
	 * 
	 * Questo codice è strettamente legato ai trigger su GDM AGSDE2_DELI_TU e AGSDE2_DETE_TU
	 *
	 * @param documento
	 * @return
	 */
	private String getEnte (def documento) {
		return (documento?.ente?.codice?:"");
	}

	private ProfiloExtend salvaDocumentoGdm (Connection conn, VistoParere vp) {
		// per prima cosa salvo il documento principale se non lo è già:
		if (vp.documentoPrincipale.idDocumentoEsterno == null) {
			salvaDocumento (vp.documentoPrincipale);
		}

        ProfiloExtend vistoParereGdm = getDocumentoGdm(conn, vp, MODELLO_VISTO)
        salvaDatiComuni (vistoParereGdm, vp)

		vistoParereGdm.settaPadre(vp.documentoPrincipale.idDocumentoEsterno.toString())
		log.debug "Gdm service salva visto - settato il documento padre " + vp.documentoPrincipale?.idDocumentoEsterno?.toString()

		vistoParereGdm.settaValore("DAL_UNITA_REDAZIONE_VISTO",		vp.unitaSo4?.dal?:"")
		vistoParereGdm.settaValore("DESCRIZIONE_TIPO_VISTOPARERE",	vp.tipologia.titolo)
		vistoParereGdm.settaValore("DIRIGENTE_VISTO",               vp.firmatario?.nominativoSoggetto?:"")
		vistoParereGdm.settaValore("ESITO",                         vp.esito?.toString())
		vistoParereGdm.settaValore("ID_TIPO_VISTOPARERE",           vp.tipologia.id)
		vistoParereGdm.settaValore("NOTE",                          vp.note?:"")
		vistoParereGdm.settaValore("PROGR_UNITA_REDAZIONE_VISTO",   vp.unitaSo4?.progr?:"")
		vistoParereGdm.settaValore("UNITA_REDAZIONE_VISTO",       	vp.unitaSo4?.descrizione?:"")
		vistoParereGdm.settaValore("UTENTE_AD4_DIRIGENTE_VISTO",    vp.firmatario?.id?:"")
		vistoParereGdm.settaValore("ID_DETERMINA_GRAILS",          	vp.determina?.id?:"")
		vistoParereGdm.settaValore("ID_PROPOSTA_DELIBERA_GRAILS",   vp.propostaDelibera?.id?:"")
		vistoParereGdm.settaValore("CONTABILE",						(vp.tipologia.contabile)?'Y':'N')

		if (vp.testo != null && vp.idDocumentoEsterno != null) {
			vistoParereGdm.setFileName(vp.testo.nome, gestoreFile.getFile(vp, vp.testo))
		}

		if (!vistoParereGdm.salva().booleanValue()) {
			throw new AttiRuntimeException ("Errore nel salvare il visto/parere: ${vistoParereGdm.getError()}")
		}

		if (vp.idDocumentoEsterno == null) {
			vistoParereGdm.settaRiferimento(vp.documentoPrincipale.idDocumentoEsterno.toString(), RIFERIMENTO_CERTIFICATI);

			if (!vistoParereGdm.salva().booleanValue()) {
				throw new AttiRuntimeException ("Errore nel salvare l'allegato: ${vistoParereGdm.getError()}")
			}
		}

		return vistoParereGdm
	}

	private ProfiloExtend salvaDocumentoGdm (Connection conn, Allegato a) {

		// per prima cosa salvo il documento principale se non lo è già:
		// siccome posso salvare un allegato senza documento principale (dalla pagina di test della firma), devo saltare tutti i legami con il padre:
		if (a.documentoPrincipale != null && a.documentoPrincipale.idDocumentoEsterno == null) {
			salvaDocumento (a.documentoPrincipale);
		}

        ProfiloExtend allegatoGdm = getDocumentoGdm(conn, a, MODELLO_ALLEGATO)
        salvaDatiComuni (allegatoGdm, a)

		// FIXME: ATTENZIONE: SU GDM UN DOCUMENTO PUO' AVERE UN SOLO PADRE. COME GESTISCO GLI ALLEGATI DELLA DELIBERA?
		// siccome posso salvare un allegato senza documento principale (dalla pagina di test della firma), devo saltare tutti i legami con il padre:
		if (a.documentoPrincipale != null) {
			allegatoGdm.settaPadre(a.documentoPrincipale.idDocumentoEsterno.toString())
		}

		allegatoGdm.settaValore("DESCRIZIONE",					a.descrizione?:"")
		allegatoGdm.settaValore("DESCRIZIONE_TIPO_ALLEGATO",	a.tipoAllegato?.descrizione?:"")
		allegatoGdm.settaValore("ID_DELIBERA_GRAILS"          , a.delibera?.id)
		allegatoGdm.settaValore("ID_DETERMINA_GRAILS"         , a.determina?.id)
		allegatoGdm.settaValore("ID_PROPOSTA_DELIBERA_GRAILS" , a.propostaDelibera?.id)
		allegatoGdm.settaValore("ID_TIPO_ALLEGATO"            , a.tipoAllegato?.id)
		allegatoGdm.settaValore("NUM_PAGINE"                  , a.numPagine)
		allegatoGdm.settaValore("QUANTITA"                    , a.quantita)

		if (!allegatoGdm.salva().booleanValue()) {
			throw new AttiRuntimeException ("Errore nel salvare l'allegato: ${allegatoGdm.getError()}")
		}

		// al primo salvataggio (se ho il documento padre), creo il riferimento.
		// siccome posso salvare un allegato senza documento principale (dalla pagina di test della firma), devo saltare tutti i legami con il padre:
		if (a.documentoPrincipale != null && a.idDocumentoEsterno == null) {
			allegatoGdm.settaRiferimento(a.documentoPrincipale.idDocumentoEsterno.toString(), RIFERIMENTO_ALLEGATI);

			if (!allegatoGdm.salva().booleanValue()) {
				throw new AttiRuntimeException ("Errore nel salvare l'allegato: ${allegatoGdm.getError()}")
			}
		}

		return allegatoGdm
	}


	private ProfiloExtend salvaDocumentoGdm (Connection conn, PropostaDelibera p) {
		ProfiloExtend propDeliberaGdm = getDocumentoGdm(conn, p, MODELLO_PROPOSTA_DELIBERA)

		salvaDatiComuni(propDeliberaGdm, p)
		salvaDatiOdg(propDeliberaGdm, p)
		salvaDatiFlusso(propDeliberaGdm, p)

		propDeliberaGdm.settaValore("ANNO_PROPOSTA", p.annoProposta ?: "")
		propDeliberaGdm.settaValore("DAL_UNITA_PROPONENTE", getDateSql(p.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.dal))
		propDeliberaGdm.settaValore("DAL_UNITA_REDATTORE", getDateSql(p.getSoggetto(TipoSoggetto.REDATTORE)?.unitaSo4?.dal))
		propDeliberaGdm.settaValore("DATA_NUMERO_PROPOSTA", getDateSql(p.dataNumeroProposta))
		propDeliberaGdm.settaValore("DESCRIZIONE_CONSIP", p.categoria?.descrizione ?: "")
		propDeliberaGdm.settaValore("DESCRIZIONE_TIPO_DELIBERA", p.tipologia.descrizione ?: "")
		propDeliberaGdm.settaValore("DESCR_REGISTRO_PROPOSTA", p.registroProposta?.descrizione ?: "")
		propDeliberaGdm.settaValore("DIRIGENTE", p.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.nominativoSoggetto ?: "")
		propDeliberaGdm.settaValore("FUNZIONARIO", p.getSoggetto(TipoSoggetto.FUNZIONARIO)?.utenteAd4?.nominativoSoggetto ?: "")
		propDeliberaGdm.settaValore("ID_CONSIP", p.categoria?.id ?: "")
		propDeliberaGdm.settaValore("ID_REGISTRO_PROPOSTA", p.registroProposta?.codice ?: "")
		propDeliberaGdm.settaValore("ID_TIPO_DELIBERA", p.tipologia.id ?: "")
		propDeliberaGdm.settaValore("NOTE", p.note ?: "")
		propDeliberaGdm.settaValore("NOTE_CONTABILI", p.noteContabili ?: "")
		propDeliberaGdm.settaValore("NUMERO_PROPOSTA", p.numeroProposta ?: "")
		propDeliberaGdm.settaValore("OGGETTO", p.oggetto ?: "")
		propDeliberaGdm.settaValore("PROGR_UNITA_PROPONENTE", p.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.progr ?: "")
		propDeliberaGdm.settaValore("PROGR_UNITA_REDATTORE", p.getSoggetto(TipoSoggetto.REDATTORE)?.unitaSo4?.progr ?: "")
		propDeliberaGdm.settaValore("REDATTORE", p.getSoggetto(TipoSoggetto.REDATTORE)?.utenteAd4?.nominativoSoggetto ?: "")
		propDeliberaGdm.settaValore("UNITA_PROPONENTE", p.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.descrizione ?: "")
		propDeliberaGdm.settaValore("UNITA_REDATTORE", p.getSoggetto(TipoSoggetto.REDATTORE)?.unitaSo4?.descrizione ?: "")
		propDeliberaGdm.settaValore("UTENTE_AD4_DIRIGENTE", p.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.id ?: "")
		propDeliberaGdm.settaValore("UTENTE_AD4_FUNZIONARIO", p.getSoggetto(TipoSoggetto.FUNZIONARIO)?.utenteAd4?.id ?: "")
		propDeliberaGdm.settaValore("UTENTE_AD4_REDATTORE", p.getSoggetto(TipoSoggetto.REDATTORE)?.utenteAd4?.id ?: "")
		propDeliberaGdm.settaValore("ASSESSORE", p.delega?.assessore?.denominazione ?: "")
		propDeliberaGdm.settaValore("UTENTE_AD4_ASSESSORE", p.delega?.assessore?.utenteAd4?.id ?: "")
		propDeliberaGdm.settaValore("DESCRIZIONE_DELEGA", p.delega?.descrizioneAssessorato ?: "")
		propDeliberaGdm.settaValore("TIPO_DOCUMENTO", MappingIntegrazione.getValoreEsterno(ProtocolloGdmConfig.MAPPING_CATEGORIA, ProtocolloGdmConfig.MAPPING_CODICE_TIPO_DOCUMENTO, p.tipologia.id.toString(), ""))

		log.debug "Gdm service salva la proposta di delibera - Setto eventuale documento collegato"
		def collegamento = getDocumentiCollegati(p)

		if(collegamento.size()>0) {
			propDeliberaGdm.settaValore("DOC_COLLEGATO",			collegamento.get('DOC_COLLEGATO'))
			propDeliberaGdm.settaValore("TIPO_COLLEGATO",		collegamento.get('TIPO_COLLEGATO'))
		}

		if (p.testo != null && p.idDocumentoEsterno != null) {
			propDeliberaGdm.setFileName(p.testo.nome, gestoreFile.getFile(p, p.testo))
		}

		if (!propDeliberaGdm.salva().booleanValue()) {
			throw new AttiRuntimeException ("Errore nel salvare la proposta di delibera: ${propDeliberaGdm.getError()}")
		}

		return propDeliberaGdm
	}

	private ProfiloExtend salvaDatiOdg (ProfiloExtend docGdm, Delibera d) {
        docGdm.settaValore("ODG_STATO",          d.propostaDelibera?.statoOdg?.toString()?:"")
        docGdm.settaValore("DESCRIZIONE_DELEGA",	d.oggettoSeduta?.delega?.descrizioneAssessorato?:"")
        docGdm.settaValore("ID_COMMISSIONE",     d.oggettoSeduta?.seduta?.commissione?.id?:"")
        docGdm.settaValore("ID_DELEGA",          d.oggettoSeduta?.delega?.id?:"")
    }

	private ProfiloExtend salvaDatiOdg (ProfiloExtend docGdm, PropostaDelibera d) {
        docGdm.settaValore("ODG_STATO",          d.statoOdg?.toString()?:"")
        docGdm.settaValore("DESCRIZIONE_DELEGA",	d.delega?.descrizioneAssessorato?:"")
        docGdm.settaValore("ID_COMMISSIONE",     d.commissione?.id?:"")
        docGdm.settaValore("ID_DELEGA",          d.delega?.id?:"")
    }

	private ProfiloExtend salvaDocumentoGdm (Connection conn, Delibera d) {
		ProfiloExtend deliGdm = getDocumentoGdm(conn, d, MODELLO_DELIBERA)

		salvaDatiComuni(deliGdm, d)
		salvaDatiProtocollo(deliGdm, d)
		salvaDatiPubblicazione(deliGdm, d)
		salvaDatiFlusso(deliGdm, d)
		salvaDatiOdg(deliGdm, d)

		deliGdm.settaValore("ANNO_DELIBERA", d.annoDelibera ?: "")
		deliGdm.settaValore("ASSESSORE", d.oggettoSeduta?.delega?.assessore?.denominazione ?: "")
		deliGdm.settaValore("UTENTE_AD4_ASSESSORE", d.oggettoSeduta?.delega?.assessore?.utenteAd4?.id ?: "")
		deliGdm.settaValore("DATA_ESECUTIVITA", getDateSql(d.dataEsecutivita))
		deliGdm.settaValore("DATA_ADOZIONE", getDateSql(d.dataAdozione))
		deliGdm.settaValore("DATA_NUMERO_DELIBERA", getDateSql(d.dataNumeroDelibera))
		deliGdm.settaValore("DESCRIZIONE_CONSIP", d.proposta.categoria?.descrizione ?: "")
		deliGdm.settaValore("DESCRIZIONE_TIPO_DELIBERA", d.tipologiaDocumento.descrizione ?: "")
		deliGdm.settaValore("DESCR_REGISTRO_DELIBERA", d.registroDelibera?.descrizione ?: "")
		deliGdm.settaValore("ID_CONSIP", d.proposta.categoria?.id ?: "")
		deliGdm.settaValore("ID_REGISTRO_DELIBERA", d.registroDelibera?.codice ?: "")
		deliGdm.settaValore("ID_TIPO_DELIBERA", d.tipologiaDocumento.id ?: "")
		deliGdm.settaValore("NOTE", d.proposta.note ?: "")
		deliGdm.settaValore("NOTE_CONTABILI", d.proposta.noteContabili ?: "")
		deliGdm.settaValore("NUMERO_DELIBERA", d.numeroDelibera ?: "")
		deliGdm.settaValore("OGGETTO", d.oggetto ?: "")
		deliGdm.settaValore("PRESIDENTE", d.getSoggetto(TipoSoggetto.PRESIDENTE)?.utenteAd4?.nominativoSoggetto ?: "")
		deliGdm.settaValore("SEGRETARIO", d.getSoggetto(TipoSoggetto.SEGRETARIO)?.utenteAd4?.nominativoSoggetto ?: "")
		deliGdm.settaValore("UTENTE_AD4_PRESIDENTE", d.getSoggetto(TipoSoggetto.PRESIDENTE)?.utenteAd4?.id ?: "")
		deliGdm.settaValore("UTENTE_AD4_SEGRETARIO", d.getSoggetto(TipoSoggetto.SEGRETARIO)?.utenteAd4?.id ?: "")
		deliGdm.settaValore("ESEGUIBILITA_IMMEDIATA", d.oggettoSeduta?.eseguibilitaImmediata ? "Y" : "N")
		deliGdm.settaValore("ESITO_DISCUSSIONE", d.oggettoSeduta?.esito?.descrizione ?: "")
		deliGdm.settaValore("ID_PROPOSTA_DELIBERA_GRAILS", d.proposta?.id ?: "")
		deliGdm.settaValore("DESCR_UO_DIRIGENTE", d.getSoggetto(TipoSoggetto.DIRIGENTE)?.unitaSo4?.descrizione ?: "")
		deliGdm.settaValore("TIPO_DOCUMENTO", MappingIntegrazione.getValoreEsterno(ProtocolloGdmConfig.MAPPING_CATEGORIA, ProtocolloGdmConfig.MAPPING_CODICE_TIPO_DOCUMENTO, d.proposta.tipologia.id.toString(), ""))

		if (d.testo != null && d.idDocumentoEsterno != null) {
			deliGdm.setFileName(d.testo.nome, gestoreFile.getFile(d, d.testo))
		}

		if (d.stampaUnica != null && d.idDocumentoEsterno != null) {
			deliGdm.setFileName(d.stampaUnica.nome, gestoreFile.getFile(d, d.stampaUnica))
		}

		if (!deliGdm.salva().booleanValue()) {
			throw new AttiRuntimeException ("Errore nel salvare la delibera: ${deliGdm.getError()}")
		}

		return deliGdm
	}

    private void salvaDatiPubblicazione (ProfiloExtend docGdm, IPubblicabile pubblicabile) {
        docGdm.settaValore("DATA_FINE_PUBBLICAZIONE",	getDateSql(pubblicabile.dataFinePubblicazione))
        docGdm.settaValore("DATA_FINE_PUBBLICAZIONE_2",	getDateSql(pubblicabile.dataFinePubblicazione2))
        docGdm.settaValore("DATA_PUBBLICAZIONE",			getDateSql(pubblicabile.dataPubblicazione))
        docGdm.settaValore("DATA_PUBBLICAZIONE_2",		getDateSql(pubblicabile.dataPubblicazione2))
        docGdm.settaValore("GIORNI_PUBBLICAZIONE",  pubblicabile.giorniPubblicazione?:"")
        docGdm.settaValore("ID_DOCUMENTO_ALBO", 	pubblicabile.idDocumentoAlbo?:"")
        docGdm.settaValore("NUMERO_ALBO", 			pubblicabile.numeroAlbo?:"")
        docGdm.settaValore("ANNO_ALBO", 			pubblicabile.annoAlbo?:"")
    }

    private void salvaDatiFlusso (ProfiloExtend docGdm, IDocumentoIterabile doc) {
        docGdm.settaValore("POSIZIONE_FLUSSO",  doc.iter?.stepCorrente?.cfgStep?.nome?:"")
    }

	private ProfiloExtend salvaDocumentoGdm (Connection conn, Certificato c) {

		// per prima cosa salvo il documento principale se non lo è già:
		if (c.documentoPrincipale.idDocumentoEsterno == null) {
			salvaDocumento (c.documentoPrincipale);
		}

        ProfiloExtend certGdm = getDocumentoGdm(conn, c, MODELLO_CERTIFICATO)

		certGdm.settaPadre(c.documentoPrincipale.idDocumentoEsterno.toString())
		log.debug "Gdm service salva certificato - settato il documento padre " + c.documentoPrincipale?.idDocumentoEsterno?.toString()

        salvaDatiComuni (certGdm, c)
        salvaDatiFlusso (certGdm, c)

		certGdm.settaValore("TIPO",						c.tipo)
		certGdm.settaValore("FIRMATARIO",				c.firmatario?.nominativoSoggetto?:"")
		certGdm.settaValore("UTENTE_AD4_FIRMATARIO",    	c.firmatario?.id?:"")
		certGdm.settaValore("ID_DETERMINA_GRAILS",      c.determina?.id)
		certGdm.settaValore("ID_DELIBERA_GRAILS",       c.delibera?.id)
		certGdm.settaValore("SECONDA_PUBBLICAZIONE",    	c.secondaPubblicazione?"Y":"N")

		if (c.testo != null && c.idDocumentoEsterno != null) {
			certGdm.setFileName(c.testo.nome, gestoreFile.getFile(c, c.testo))
		}

		if (!certGdm.salva().booleanValue()) {
			throw new AttiRuntimeException ("Errore nel salvare il certificato: ${certGdm.getError()}")
		}

		if (c.idDocumentoEsterno == null) {
			certGdm.settaRiferimento(c.documentoPrincipale.idDocumentoEsterno.toString(), RIFERIMENTO_CERTIFICATI);

			if (!certGdm.salva().booleanValue()) {
				throw new AttiRuntimeException ("Errore nel salvare l'allegato: ${certGdm.getError()}")
			}
		}

		return certGdm
	}

	private ProfiloExtend salvaDocumentoGdm (Connection conn, SedutaStampa sedutaStampa) {
		ProfiloExtend sedutaStampaGdm = getDocumentoGdm(conn, sedutaStampa, MODELLO_SEDUTA_STAMPA)

		salvaDatiComuni(sedutaStampaGdm, sedutaStampa)
		salvaDatiProtocollo(sedutaStampaGdm, sedutaStampa)
		salvaDatiPubblicazione(sedutaStampaGdm, sedutaStampa)

		sedutaStampaGdm.settaValore("OGGETTO", sedutaStampa.oggetto ?: "")
		sedutaStampaGdm.settaValore("DESCRIZIONE_COMMISSIONE", sedutaStampa.commissioneStampa.commissione.titolo)
		sedutaStampaGdm.settaValore("NOTE", sedutaStampa.note ?: "")
		sedutaStampaGdm.settaValore("FIRMATARIO", sedutaStampa.getSoggetto(TipoSoggetto.FIRMATARIO)?.utenteAd4?.id)
		sedutaStampaGdm.settaValore("REDATTORE", sedutaStampa.getSoggetto(TipoSoggetto.REDATTORE)?.utenteAd4?.id)
		sedutaStampaGdm.settaValore("TIPO_DOCUMENTO", MappingIntegrazione.getValoreEsterno(ProtocolloGdmConfig.MAPPING_CATEGORIA, ProtocolloGdmConfig.MAPPING_CODICE_TIPO_DOCUMENTO, sedutaStampa.commissioneStampa.id.toString(), ""))

		if (sedutaStampa.testo != null && sedutaStampa.idDocumentoEsterno != null) {
			sedutaStampaGdm.setFileName(sedutaStampa.testo.nome, gestoreFile.getFile(sedutaStampa, sedutaStampa.testo))
		}

		if (!sedutaStampaGdm.salva().booleanValue()) {
			throw new AttiRuntimeException("Errore nel salvare il certificato: ${sedutaStampaGdm.getError()}")
		}

		return sedutaStampaGdm
	}

    private ProfiloExtend getDocumentoGdm (Connection connection, IDocumentoEsterno documentoEsterno, String modelloGdm) {
        if (documentoEsterno.idDocumentoEsterno > 0) {
            return new ProfiloExtend(String.valueOf(documentoEsterno.idDocumentoEsterno), GdmDocumentaleEsterno.GDM_USER, null, connection, false)
        }

        return new ProfiloExtend(modelloGdm, AREA, GdmDocumentaleEsterno.GDM_USER, null, connection, false)
    }

	private AllegatoDTO importAllegato(AllegatoDTO allegato, String idDocumentoEsterno, String idFileEsterno) {
		Connection conn = dataSource_gdm.connection
		ProfiloExtend p = new ProfiloExtend (idDocumentoEsterno, GdmDocumentaleEsterno.GDM_USER, null, conn, false)
		String nomeFile = p.getFileName(Long.parseLong(idFileEsterno))
		String contentType = "application/octet-stream"
		InputStream is = new BufferedInputStream (p.getFileStream(nomeFile))
		if (is != null) {
			allegato = allegatoDTOService.uploadFile(allegato, nomeFile, contentType, is)
		}
		return allegato
	}

    private void salvaDatiComuni (ProfiloExtend profiloGdm, IDocumento documento) {
        profiloGdm.settaValore("ID_DOCUMENTO_GRAILS", 	documento.id)
        profiloGdm.settaValore("UTENTE_INS",            documento.utenteIns.id)
        profiloGdm.settaValore("UTENTE_UPD",            documento.utenteUpd.id)
        profiloGdm.settaValore("DATA_UPD",		getDateSql(documento.lastUpdated))
        profiloGdm.settaValore("DATA_INS",		getDateSql(documento.dateCreated))
        profiloGdm.settaValore("ENTE",			getEnte(documento))
		profiloGdm.settaValore("DATA_FIRMA", 	getDateSql(getUltimoFirmatario(documento)?.dataFirma))
		profiloGdm.settaValore("RISERVATO",     documento.riservato?"Y":"N")
    }

    private void salvaDatiComuni (ProfiloExtend profiloGdm, Allegato documento) {
        profiloGdm.settaValore("ID_DOCUMENTO_GRAILS", 	documento.id)
        profiloGdm.settaValore("UTENTE_INS",    documento.utenteIns.id)
        profiloGdm.settaValore("UTENTE_UPD",    documento.utenteUpd.id)
        profiloGdm.settaValore("DATA_UPD",		getDateSql(documento.lastUpdated))
        profiloGdm.settaValore("DATA_INS",		getDateSql(documento.dateCreated))
        profiloGdm.settaValore("ENTE",			getEnte(documento))
        profiloGdm.settaValore("DATA_FIRMA", 	getDateSql(getUltimoFirmatario(documento.documentoPrincipale)?.dataFirma))
        profiloGdm.settaValore("RISERVATO",     documento.riservato?"Y":"N")
    }

    private void salvaDatiProtocollo (ProfiloExtend profiloGdm, IProtocollabile documento) {
        // se ho il numero di protocollo, lo scrivo sul modello gdm.
        // questo viene fatto per poter gestire le stampe di convocazione e verbale che usano lo stesso modello gdm SEDUTA_STAMPA (issue #22789)
        // che per la convocazione viene protocollato da Sfera, mentre per il verbale viene creato un altro documento con la nuova LETTERA e viene protocollato quello.
        // questo fa si che nella vista PROTO_VIEW del protocollo, ci siano due righe di due documenti diversi con lo stesso numero (perchè il n. di protocollo della lettera verrà riportato
        // sulla ODG_SEDUTA_STAMPA su sfera e infine sul relativo modello gdm)
        if (documento.numeroProtocollo > 0) {
            profiloGdm.settaValore("NUMERO_PROTOCOLLO", documento.numeroProtocollo)
            profiloGdm.settaValore("ANNO_PROTOCOLLO", documento.annoProtocollo)
            profiloGdm.settaValore("DATA_NUMERO_PROTOCOLLO", getDateSql(documento.dataNumeroProtocollo))
            profiloGdm.settaValore("REGISTRO_PROTOCOLLO", 			documento.registroProtocollo?.descrizione?:"")
        }
    }
}