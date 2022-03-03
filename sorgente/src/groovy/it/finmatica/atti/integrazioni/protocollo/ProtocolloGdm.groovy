package it.finmatica.atti.integrazioni.protocollo

import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.Sql
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.AbstractProtocolloEsterno
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IProtocolloEsterno.Classifica
import it.finmatica.atti.IProtocolloEsterno.Fascicolo
import it.finmatica.atti.commons.TokenIntegrazione
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.dizionari.Email
import it.finmatica.atti.documenti.DestinatarioNotifica
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IFascicolabile
import it.finmatica.atti.documenti.IProtocollabile
import it.finmatica.atti.documenti.beans.GdmDocumentaleEsterno
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.dmServer.management.Profilo
import it.finmatica.jdmsutil.data.ProfiloExtend
import it.finmatica.segreteria.common.ParametriSegreteria
import it.finmatica.segreteria.common.struttura.Classificazione
import it.finmatica.segreteria.common.struttura.ParametriProtocollazione
import it.finmatica.segreteria.common.struttura.Titolario
import it.finmatica.segreteria.jprotocollo.struttura.Protocollo
import it.finmatica.segreteria.jprotocollo.struttura.Rapporto
import it.finmatica.segreteria.wkfSupport.ProtocolloUtil
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException

@Conditional(ProtocolloGdmCondition)
@Component("protocolloEsternoGdm")
@Lazy
class ProtocolloGdm extends AbstractProtocolloEsterno {

    private static final Logger log = Logger.getLogger(ProtocolloGdm.class)

    @Autowired ProtocolloGdmConfig protocolloGdmConfig
    @Autowired IDocumentaleEsterno gestoreDocumentaleEsterno
    @Autowired TokenIntegrazioneService tokenIntegrazioneService
    @Autowired SpringSecurityService springSecurityService
    @Autowired @Qualifier("dataSource_gdm") DataSource dataSource_gdm
    @Autowired DataSource dataSource

    private static String QUERY_ALLEGATI_GDM = "SELECT ID_DOCUMENTO FROM SEG_ALLEGATI_PROTOCOLLO WHERE IDRIF = :idrif and descrizione = :descrizione"

    @Override
    @Transactional
    void sincronizzaClassificazioniEFascicoli() {
        try {
            new Sql(dataSource).call("{ call integrazione_agspr_pkg.allinea_class_fasc() }")
        } catch (SQLException e) {
            // trasformo la SQLException in runtime exception in modo da invalidare correttamente la transazione
            throw new AttiRuntimeException(e)
        }
    }

    @Override
    @Transactional
    void fascicola(IFascicolabile atto) {
        try {
            Connection conn = dataSource_gdm.connection

            ParametriSegreteria pg = new ParametriSegreteria(protocolloGdmConfig.getPathGdmProperties(), conn, 0)
            pg.setControlloCompetenzeAttivo(false)

            // ottengo il profilo gdm:
            if (!(atto.idDocumentoEsterno > 0)) {
                return
            }

            ProfiloExtend documentoGdm = new ProfiloExtend(String.valueOf(atto.idDocumentoEsterno), springSecurityService.currentUser.id, null, conn,
                    false)

            // se i dati sono tutti uguali, non devo aggiornare niente ed esco:
            if (atto.classificaCodice?.equals(documentoGdm.getCampo("CLASS_COD")) &&
                    atto.classificaDal?.format("dd/MM/yyyy")?.equals(documentoGdm.getCampo("CLASS_DAL")) &&
                    atto.fascicoloNumero?.equals(documentoGdm.getCampo("FASCICOLO_NUMERO")) &&
                    atto.fascicoloAnno?.toString()?.equals(documentoGdm.getCampo("FASCICOLO_ANNO"))) {
                return
            }

            if (!(atto.classificaCodice?.trim()?.length() > 0 && atto.classificaDal != null)) {
                return
            }

            // tolgo il documento dalla classificazione in cui si trovava prima:
            Titolario titolarioCorrente = getTitolarioCorrente(documentoGdm, pg)

            if (titolarioCorrente != null) {
                titolarioCorrente.togliDocumento(String.valueOf(atto.idDocumentoEsterno))
            }

            // metto il documento nella nuova classificazione/fascicolo
            Titolario nuovoTitolario = getTitolario(atto, pg)
            if (nuovoTitolario != null) {

                documentoGdm.settaValore("FASCICOLO_NUMERO", atto.fascicoloNumero ?: "")
                documentoGdm.settaValore("FASCICOLO_OGGETTO", atto.fascicoloOggetto ?: "")
                documentoGdm.settaValore("FASCICOLO_ANNO", (atto.fascicoloAnno != null) ? Integer.toString(atto.fascicoloAnno) : "")
                documentoGdm.settaValore("CLASS_COD", atto.classificaCodice ?: "")
                documentoGdm.settaValore("CLASS_DAL", (atto.classificaDal != null ? new java.sql.Date(atto.classificaDal.getTime()) : null))
                documentoGdm.settaValore("CLASS_DESCR", atto.classificaDescrizione ?: "")
                if (!documentoGdm.salva().booleanValue()) {
                    throw new AttiRuntimeException("Errore nel salvare la determina: ${documentoGdm.getError()}")
                }

                nuovoTitolario.aggiungiDocumento(String.valueOf(atto.idDocumentoEsterno))
            }
        } catch (Exception e) {
            throw new AttiRuntimeException("Errore nella fascicolazione su PRISMA: ${e.message}", e)
        }
    }

    private Titolario getTitolarioCorrente(Profilo documentoGdm, ParametriSegreteria pg) {
        // ritorno la classifica
        String classCod = documentoGdm.getCampo("CLASS_COD")

        // se non ho neanche il codice classifica, ritorno null
        if (!(classCod?.trim()?.length() > 0)) {
            return null
        }

        java.sql.Date classDal = new java.sql.Date(new Date().parse("dd/MM/yyyy", documentoGdm.getCampo("CLASS_DAL")).getTime())

        // se ho il fascicolo, il documento si trova lì dentro:
        if (documentoGdm.getCampo("FASCICOLO_NUMERO")?.trim()?.length() > 0) {
            Integer annoFascicolo = Integer.parseInt(documentoGdm.getCampo("FASCICOLO_ANNO"))
            String numeroFascicolo = documentoGdm.getCampo("FASCICOLO_NUMERO")

            return it.finmatica.segreteria.common.struttura.Fascicolo.getInstanceFascicolo("FASCICOLO", "SEGRETERIA", annoFascicolo, numeroFascicolo,
                    classCod, classDal, springSecurityService.currentUser.id,
                    null, pg)
        }

        return Classificazione.getInstanceClassificazione("DIZ_CLASSIFICAZIONE", "SEGRETERIA", classCod, classDal,
                springSecurityService.currentUser.id, null, pg)
    }

    private Titolario getTitolario(IFascicolabile fascicolabile, ParametriSegreteria pg) {

        // ritorno la classifica
        String classCod = fascicolabile.classificaCodice

        // se non ho neanche il codice classifica, ritorno null
        if (!(classCod?.trim()?.length() > 0)) {
            return null
        }

        java.sql.Date classDal = new java.sql.Date(fascicolabile.classificaDal.getTime())

        // se ho il fascicolo, il documento si trova lì dentro:
        if (fascicolabile.fascicoloNumero?.trim()?.length() > 0) {
            Integer annoFascicolo = new Integer(fascicolabile.fascicoloAnno)
            String numeroFascicolo = fascicolabile.fascicoloNumero

            return it.finmatica.segreteria.common.struttura.Fascicolo.getInstanceFascicolo("FASCICOLO", "SEGRETERIA", annoFascicolo, numeroFascicolo,
                    classCod, classDal, springSecurityService.currentUser.id,
                    null, pg)
        }

        return Classificazione.getInstanceClassificazione("DIZ_CLASSIFICAZIONE", "SEGRETERIA", classCod, classDal,
                springSecurityService.currentUser.id, null, pg)
    }

    // Purtroppo la funzione ProtocolloUtil fa commit sulla connessione => perdo transazionalità.
    // Quindi tratto la protocollazione GDM come se fosse un Webservice quindi con la gestione del token.
    // Utilizzo la funzione dataSource_gdm.connection per ottenere la connessione a gdm legata alla transazione
    // così non vado in deadlock (siccome anche io scrivo sulle stesse tabelle di gdm)
    // Putroppo, il fatto che la protocollazione faccia commit, mi espone a una serie di problemi:
    // - se la protocollazione va a buon fine, fa commit sulla connessione di gdm, quindi scrive anche gli eventuali altri dati che avevo scritto ma lo fa fuori dalla
    // transazionalità. Per cui, se dopo la protocollazione il mio codice va in errore, verrà fatto rollback solo fino a questa commit, perdendo così la sincronizzazione tra gli atti e gdm.
    //
    @Override
    @Transactional
    void protocolla(IProtocollabile atto) {
        // ottengo il lock pessimistico per evitare doppie protocollazioni.
        atto.lock();

        // controllo che il documento non sia già protocollato
        if (atto.numeroProtocollo > 0) {
            throw new AttiRuntimeException("Il documento è già protocollato!");
        }

        // creo il token di protocollazione: se lo trovo ed ha successo, vuol dire che ho già protocollato:
        TokenIntegrazione token = tokenIntegrazioneService.beginTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
        if (token.isStatoSuccesso()) {
            // significa che ho già protocollato: prendo il numero di protocollo, lo assegno al documento ed esco:
            def map = Eval.me(token.dati);
            atto.numeroProtocollo = map.numero;
            atto.annoProtocollo = map.anno;
            atto.dataNumeroProtocollo = Date.parse("dd/MM/yyyy", map.data);
            atto.save()

            // allineo il documento gdm:
            gestoreDocumentaleEsterno.salvaDocumento(atto);

            // elimino il token: tutto è andato bene e verrà eliminato solo alla commit sull transaction principale
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            return;
        }

        // per prima cosa allineo il documento gdm:
        gestoreDocumentaleEsterno.salvaDocumento(atto);

        String proponente = atto.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.id
        String unita_proponente = atto.getUnitaProponente()?.codice;

        log.debug("Creo ParametriSegreteria");
        // questa connessione è quella della transazione attiva su gdm.
        Connection conn = dataSource_gdm.connection;
        // wrapper della connessione che impedisce di committare al protocollo. NOPE. Ci sono dei problemi con la gestione del rollback. La Protocolla usa un miliardo di connessioni sue che poi non vengono chiuse.
//		conn = new ConnectionNoCommit(conn);

        // lo "0" come terzo parametro significa "connessione oracle". Questa smerdarina è dovuta a causa delle sempiterne merdosissime DbOperationSQL.
        ParametriSegreteria pg = new ParametriSegreteria(protocolloGdmConfig.pathGdmProperties, conn, 0);
        pg.setControlloCompetenzeAttivo(false);
        ProtocolloUtil pu = new ProtocolloUtil(pg);

        ParametriProtocollazione pp = new ParametriProtocollazione();
        pp.setSmistamentiObbligatori(false);
        pp.setSeparaAllegati(false);

        log.debug("Istanzio il protocollo");
        // non posso usare GDM perché potrebbe dare errore in protocollazione. Uso quindi l'utente corrente che deve avere i diritti sul documento.
        pu.istanziaProtocollo(String.valueOf(atto.idDocumentoEsterno), springSecurityService.principal.id, null);
        if (pu.getProtocollo() == null) {
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
            throw new AttiRuntimeException("c'è stato un errore nella creazione del protocollo: " + pu.getMessaggioErrore());
        }

        try {
            pu.getProtocollo().movimento = getMovimento(atto)
            pu.getProtocollo().setUnitaProtocollante(unita_proponente)
            pu.getProtocollo().setCodiceAmministrazione(springSecurityService.principal.amm().codice)
            pu.getProtocollo().setTipoRegistro(protocolloGdmConfig.getCodiceRegistro())
            String tipoDocumento = protocolloGdmConfig.getTipoDocumento(atto.tipologiaDocumento.id)
            pu.getProtocollo().setTipoDocumentoProtocollo(tipoDocumento)
            setIdRif(pu.getProtocollo())

            aggiungiDestinatari(pu.getProtocollo(), pg, atto)

            log.debug("Eseguo la protocollazione del documento ${atto}")
            String codiceModello = pu.getProtocollo().getCodiceModello()
            String area = pu.getProtocollo().getArea()
            String codiceRichiesta = pu.getProtocollo().getCodiceRichiesta()
            pu.protocolla(codiceModello, area, codiceRichiesta, proponente, "", pp)
            if (pu.getProtocollo().getNumero() <= 0) {
                throw new Exception("Si è verificato un errore in protocollazione: " + pu.getMessaggioErrore())
            }

            // la prima cosa che faccio dopo la protocollazione è salvare il record su db:
            tokenIntegrazioneService.setTokenSuccess("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO,
                    "[numero:${pu.getProtocollo().getNumero()}, anno:${pu.getProtocollo().getAnno()}, data:'${pu.getProtocollo().getData().format("dd/MM/yyyy")}']");

            log.info(
                    "Protocollazione GDM effettuata sul documento ${atto.id}: ${pu.getProtocollo().getNumero()}/${pu.getProtocollo().getAnno()} in data ${pu.getProtocollo().getData()}")
        } catch (Exception e) {
            log.error("Errore nella chiamata alla protocollazione gdm: ${e.getMessage()}", e);
            // elimino il token e interrompo la transazione del token (così si possono fare più tentativi)
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
            throw new AttiRuntimeException("Errore in fase di protocollazione via GDM: ${e.getMessage()}.", e);
        }

        atto.numeroProtocollo = pu.getProtocollo().getNumero()
        atto.annoProtocollo = pu.getProtocollo().getAnno()
        atto.dataNumeroProtocollo = pu.getProtocollo().getData()
        atto.save()

        // elimino il token: questo avverrà solo se la transazione "normale" di grails andrà a buon fine:
        tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
    }

    private void setIdRif(Protocollo protocollo) {
        // tutto questo schifo serve perché il protocollo non è proprio affidabile:
        // il suo .getIdrif() può ritornare null/stringa-vuota/-1 in caso di idrif non settato.
        String idrif = protocollo.getIdrif()
        if (idrif == null || idrif.trim() == "") {
            idrif = "-1"
        }

        int id = Integer.parseInt(idrif)
        if (id < 0) {
            id = getIdRif()
        }

        protocollo.setIdrif(Integer.toString(id))
    }

    private int getIdRif() {
        return new Sql(dataSource_gdm).rows("select SEQ_IDRIF.nextval IDRIF from dual")[0].IDRIF
    }

    private void aggiungiDestinatari(Protocollo protocolloGdm, ParametriSegreteria pg, IProtocollabile protocollabile) {
        for (DestinatarioNotifica destinatarioNotifica : protocollabile.destinatari) {
            if (destinatarioNotifica.soggettoCorrente != null) {
                protocolloGdm.addDestinatario(creaDestinatario(destinatarioNotifica.soggettoCorrente, protocolloGdm.getIdrif(), pg))
            } else if (destinatarioNotifica.email != null) {
                protocolloGdm.addDestinatario(creaDestinatario(destinatarioNotifica.email, protocolloGdm.getIdrif(), pg))
            } else if (destinatarioNotifica.unitaSo4 != null) {
                protocolloGdm.addDestinatario(creaDestinatario(destinatarioNotifica.unitaSo4, protocolloGdm.getIdrif(), pg))
            }
        }
    }

    private Rapporto creaDestinatario(As4SoggettoCorrente soggetto, String idrif, ParametriSegreteria pg) {
        Rapporto rapporto = new Rapporto("M_SOGGETTO", "SEGRETERIA", "GDM", null, pg)
        rapporto.setIdrif(idrif)
        rapporto.setNome(soggetto.nome ?: "")
        rapporto.setCognome(((soggetto.cognome) ?: soggetto.denominazione) ?: "")
        rapporto.setIndirizzoTelematico(soggetto.indirizzoWeb ?: "")
        rapporto.setCodiceFiscale(soggetto.codiceFiscale ?: "")
        rapporto.setTipoRapporto("DEST")
        if (!rapporto.salva().booleanValue()) {
            throw new AttiRuntimeException("Errore in salvataggio destinatario GDM: ${rapporto.errorPostSave}")
        }
        return rapporto
    }

    private Rapporto creaDestinatario(Email soggetto, String idrif, ParametriSegreteria pg) {
        Rapporto rapporto = new Rapporto("M_SOGGETTO", "SEGRETERIA", "GDM", null, pg)
        rapporto.setIdrif(idrif)
        rapporto.setNome(soggetto.nome ?: "")
        rapporto.setCognome(((soggetto.cognome) ?: soggetto.ragioneSociale) ?: "")
        rapporto.setIndirizzoTelematico(soggetto.indirizzoEmail ?: "")
        rapporto.setTipoRapporto("DEST")
        if (!rapporto.salva().booleanValue()) {
            throw new AttiRuntimeException("Errore in salvataggio destinatario GDM: ${rapporto.errorPostSave}")
        }
        return rapporto
    }

    private Rapporto creaDestinatario(So4UnitaPubb soggetto, String idrif, ParametriSegreteria pg) {
        Rapporto rapporto = new Rapporto("M_SOGGETTO", "SEGRETERIA", "GDM", null, pg)
        rapporto.setIdrif(idrif)
        rapporto.setDenominazioneUo(soggetto.descrizione ?: "")
        rapporto.setCodiceUo(soggetto.codice ?: "")
        rapporto.setCodiceAoo(soggetto.codiceAoo ?: "")
        rapporto.setCodiceAmministrazione(soggetto.amministrazione.codice ?: "")
        rapporto.setTipoRapporto("DEST")
        if (!rapporto.salva().booleanValue()) {
            throw new AttiRuntimeException("Errore in salvataggio destinatario GDM: ${rapporto.errorPostSave}")
        }
        return rapporto
    }

    String getMovimento(IProtocollabile protocollabile) {
        switch (protocollabile.movimento) {
            case IProtocollabile.Movimento.PARTENZA:
                return "PAR"

            case IProtocollabile.Movimento.ARRIVO:
                return "ARR"

            case IProtocollabile.Movimento.INTERNO:
            default:
                return "INT"
        }
    }

    @Transactional(readOnly = true)
    List<Classifica> getListaClassificazioni(String filtro, String codiceUoProponente) {
        String query = """select * from  
							 (select class_cod 						codice
								 , seg_classificazioni.class_descr 	descrizione
								 , seg_classificazioni.class_dal 	dal
							  from seg_classificazioni
								 , documenti docu_clas
								 , cartelle cart_clas
							 where (upper(seg_classificazioni.nome) like upper(:filtro) or upper(class_cod) like upper(:filtro))
							   and trunc (sysdate) between class_dal and nvl (class_al,  to_date ('01/01/2999', 'dd/mm/yyyy'))
							   and docu_clas.id_documento = seg_classificazioni.id_documento
							   and nvl (docu_clas.stato_documento, 'BO') not in ('CA', 'RE', 'PB')
							   and cart_clas.id_documento_profilo = docu_clas.id_documento
							   and nvl (cart_clas.stato, 'BO') <> 'CA'
                               and seg_classificazioni.contenitore_documenti = 'Y'
							 order by codice, descrizione asc) 
							where rownum < 50""";
        Sql sql = new Sql(dataSource_gdm);
        def rows = sql.rows(query, [filtro: "%" + filtro + "%"]);
        def listaClassifiche = []
        for (def row : rows) {
            listaClassifiche << new Classifica(codice: row.codice, descrizione: row.descrizione, dal: row.dal);
        }
        return listaClassifiche;
    }

    @Transactional(readOnly = true)
    List<Fascicolo> getListaFascicoli(String filtro, String codiceClassifica, Date classificaDal, String codiceUoProponente) {
        String query = """select * from  
							 (select fasc.class_cod 		codice
								, clas.class_descr			descrizione
								, clas.class_dal			dal
								, fasc.fascicolo_anno 		anno
								, fasc.fascicolo_numero 	numero
								, fasc.fascicolo_oggetto 	oggetto
							 FROM seg_fascicoli fasc
								, seg_classificazioni clas
								, documenti docu_clas
								, documenti docu_fasc
								, cartelle cart_clas
								, cartelle cart_fasc
								, view_cartella vica_fasc
							WHERE """ + (classificaDal != null ? "clas.class_dal = :classificaDal and" : "") +
                """ fasc.class_cod = clas.class_cod
							  and fasc.class_dal = clas.class_dal
							  and TRUNC (SYSDATE) BETWEEN fasc.class_dal 
							  and NVL (fasc.class_al, TO_DATE ('01/01/2999', 'dd/mm/yyyy'))
							  and fasc.stato_fascicolo   = 1
							  and docu_clas.id_documento = clas.id_documento
							  and docu_fasc.id_documento = fasc.id_documento
							  and NVL (docu_clas.stato_documento, 'BO') NOT IN ('CA', 'RE', 'PB')
							  and NVL (docu_fasc.stato_documento, 'BO') NOT IN ('CA', 'RE', 'PB')
							  and cart_clas.id_documento_profilo = docu_clas.id_documento
							  and vica_fasc.id_cartella = cart_fasc.id_cartella
							  and NVL (cart_clas.stato, 'BO') <> 'CA'
							  and cart_fasc.id_documento_profilo = docu_fasc.id_documento
							  and NVL (cart_fasc.stato, 'BO') <> 'CA'
							  and (NVL (UPPER (fasc.fascicolo_oggetto), ' ') 	LIKE UPPER (:filtro) OR 
								   UPPER(fasc.class_cod)					 	LIKE UPPER (:filtro) OR 
								   UPPER(to_char(fasc.fascicolo_anno))			LIKE UPPER (:filtro) OR 
								   UPPER(to_char(fasc.fascicolo_numero 	))		LIKE UPPER (:filtro))
							  and fasc.class_cod = nvl(:classifica, fasc.class_cod)
                              and fasc.fascicolo_numero is not null
                              and fasc.fascicolo_anno is not null
						    order by clas.class_cod, fasc.fascicolo_anno desc, ag_fascicolo_utility.get_numero_fasc_ord (fascicolo_numero) asc, fasc.fascicolo_oggetto) 
							where rownum < 100"""

        Sql sql = new Sql(dataSource_gdm);
        def rows = sql.rows(query, [filtro: "%" + filtro + "%", classifica: codiceClassifica, classificaDal: classificaDal ? new java.sql.Date(
                classificaDal.getTime()) : null]);
        def listaFascicoli = []
        for (def row : rows) {
            listaFascicoli << new Fascicolo(classifica: new Classifica(codice: row.codice, descrizione: row.descrizione, dal: row.dal)
                    , anno: row.anno
                    , numero: row.numero
                    , oggetto: row.oggetto);
        }
        return listaFascicoli;
    }

    @Transactional(readOnly = true)
    String getCodiceUnita(IAtto atto) {
        return MappingIntegrazione.getValoreEsterno(ProtocolloGdmConfig.MAPPING_CATEGORIA, ProtocolloGdmConfig.MAPPING_CODICE_TIPO_DOCUMENTO, atto.tipologiaDocumento.id)
    }

    @Override
    @Transactional(readOnly = true)
    Classifica getClassifica(String codice, Date dataValidita) {
        String query = """select class_cod 						codice
								 , seg_classificazioni.class_descr 	descrizione
								 , seg_classificazioni.class_dal 	dal
							  from seg_classificazioni
								 , documenti docu_clas
								 , cartelle cart_clas
							 where upper(class_cod) = upper(:codice)
							   and trunc (:dataValidita) between class_dal and nvl (class_al,  to_date ('01/01/2999', 'dd/mm/yyyy'))
							   and docu_clas.id_documento = seg_classificazioni.id_documento
							   and nvl (docu_clas.stato_documento, 'BO') not in ('CA', 'RE', 'PB')
							   and cart_clas.id_documento_profilo = docu_clas.id_documento
							   and nvl (cart_clas.stato, 'BO') <> 'CA'
                               and seg_classificazioni.contenitore_documenti = 'Y'"""

        if (dataValidita == null) {
            dataValidita = new Date().clearTime()
        }

        Sql sql = new Sql(dataSource_gdm)
        def rows = sql.rows(query, [codice: codice, dataValidita: new java.sql.Date(dataValidita.time)])
        def listaClassifiche = []
        for (def row : rows) {
            listaClassifiche << new Classifica(codice: row.codice, descrizione: row.descrizione, dal: row.dal)
        }

        if (listaClassifiche.size() > 0) {
            return listaClassifiche[0]
        } else {
            return null
        }
    }

    @Override
    Fascicolo getFascicolo(Classifica classifica, String numero, int anno) {
        String query = """select fasc.class_cod 		codice
								, clas.class_descr			descrizione
								, clas.class_dal			dal
								, fasc.fascicolo_anno 		anno
								, fasc.fascicolo_numero 	numero
								, fasc.fascicolo_oggetto 	oggetto
							 FROM seg_fascicoli fasc
								, seg_classificazioni clas
								, documenti docu_clas
								, documenti docu_fasc
								, cartelle cart_clas
								, cartelle cart_fasc
								, view_cartella vica_fasc
							WHERE fasc.stato_fascicolo   = 1
							  and docu_clas.id_documento = clas.id_documento
							  and docu_fasc.id_documento = fasc.id_documento
							  and NVL (docu_clas.stato_documento, 'BO') NOT IN ('CA', 'RE', 'PB')
							  and NVL (docu_fasc.stato_documento, 'BO') NOT IN ('CA', 'RE', 'PB')
							  and cart_clas.id_documento_profilo = docu_clas.id_documento
							  and vica_fasc.id_cartella = cart_fasc.id_cartella
							  and NVL (cart_clas.stato, 'BO') <> 'CA'
							  and cart_fasc.id_documento_profilo = docu_fasc.id_documento
							  and NVL (cart_fasc.stato, 'BO') <> 'CA'
							  and fasc.fascicolo_anno = :anno 
							  and fasc.fascicolo_numero = :numero
							  and TRUNC (:classificaDal) BETWEEN fasc.class_dal and NVL (fasc.class_al, TO_DATE ('01/01/2999', 'dd/mm/yyyy'))
							  and fasc.class_cod = :classificaCodice"""

        Sql sql = new Sql(dataSource_gdm)
        def rows = sql.rows(query, [numero: numero, anno: anno, classificaCodice: classifica.codice, classificaDal: new java.sql.Date(classifica.dal.time)])
        List<Fascicolo> listaFascicoli = []
        for (def row : rows) {
            listaFascicoli << new Fascicolo(classifica: classifica, anno: row.anno, numero: row.numero, oggetto: row.oggetto)
        }

        if (listaFascicoli.size() > 0) {
            return listaFascicoli[0]
        } else {
            return null
        }
    }

    @Override
    @Transactional
    public void creaAllegatoProtocollo(IProtocollabile atto, String descrizione, String nomeFileAllegato, InputStream is){
        Connection conn = dataSource_gdm.connection

        ParametriSegreteria pg = new ParametriSegreteria(protocolloGdmConfig.pathGdmProperties, conn, 0);
        pg.setControlloCompetenzeAttivo(false);
        ProtocolloUtil pu = new ProtocolloUtil(pg);

        pu.istanziaProtocollo(String.valueOf(atto.idDocumentoEsterno), springSecurityService.principal.id, null);

        if (pu.getProtocollo() == null) {
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
            throw new AttiRuntimeException("c'è stato un errore nel caricamento del protocollo: " + pu.getMessaggioErrore());
        }

        Sql sql = new Sql(conn)
        String idDocumento = null
        sql.eachRow(QUERY_ALLEGATI_GDM, [idrif: pu.getProtocollo().getIdrif(), descrizione: descrizione]) { row ->
            idDocumento = row.ID_DOCUMENTO
        }
        if (idDocumento != null){
            return;
        }

        ProfiloExtend p = new ProfiloExtend('M_ALLEGATO_PROTOCOLLO', 'SEGRETERIA', GdmDocumentaleEsterno.GDM_USER, null, conn, false);
        p.escludiControlloCompetenze(true);

        p.settaValore("IDRIF",pu.getProtocollo().getIdrif());
        p.settaValore("DESCRIZIONE",descrizione);
        p.setFileName(nomeFileAllegato, is);
        p.settaPadre(String.valueOf(atto.idDocumentoEsterno))

        if (!p.salva().booleanValue())
        {
            throw new AttiRuntimeException("Errore nel salvare l'allegato: ${p.getError()}")

        }
    }
}
