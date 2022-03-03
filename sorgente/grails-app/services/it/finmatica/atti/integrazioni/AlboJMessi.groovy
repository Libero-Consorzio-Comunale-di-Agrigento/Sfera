package it.finmatica.atti.integrazioni

import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.Sql
import it.finmatica.atti.AbstractAlboEsterno
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.TokenIntegrazione
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.GdmDocumentaleEsterno
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.albo.AlboJMessiConfig
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.jdmsutil.data.ProfiloExtend
import oracle.jdbc.OracleTypes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException

@Component("alboJMessi")
@Lazy
class AlboJMessi extends AbstractAlboEsterno {

    public static final String AREA_MESSI = "MESSI"
    public static final String MODELLO_ALBO = "ALBO"
    public static final String MODELLO_ALLEGATO_ALBO = "M_ALLEGATO_ALBO"
    public static final String ALBO_TIPO_REGISTRO = "ALB"
    public static final String ALBO_CAMPO_NUMERO = "ULTIMO_NUMERO_REG"

    private static final String FORMATO_DATA = "dd/MM/yyyy"

    private static final String Q_NUMERO_ALBO = "SELECT TO_CHAR (ag_albo_sq.NEXTVAL) id_albo FROM DUAL"
    private static
    final String F_NUMERA_ALBO = "{ call ? :=  AG_MES_UTILITY.F_MESSI_NUMERA_ALBO_NOCOMMIT('${AREA_MESSI}','REGISTRO', ?, '${ALBO_TIPO_REGISTRO}', ?, '${AREA_MESSI}', '${MODELLO_ALBO}', '${ALBO_CAMPO_NUMERO}') }"

    @Autowired DataSource dataSource_gdm
    @Autowired DataSource dataSource

    @Autowired AlboJMessiConfig alboJMessiConfig
    @Autowired IDocumentaleEsterno gestoreDocumentaleEsterno
    @Autowired TokenIntegrazioneService tokenIntegrazioneService
    @Autowired SpringSecurityService springSecurityService
    @Autowired StampaUnicaService stampaUnicaService
    @Autowired IGestoreFile gestoreFile

    @Transactional
    void allineaDatePubblicazioni() {
        try {
            new Sql(dataSource).call("{ call integrazione_jmessi_pkg.allinea_date_pubblicazione() }")
        } catch (SQLException e) {
            // trasformo la SQLException in runtime exception in modo da invalidare correttamente la transazione
            throw new AttiRuntimeException(e)
        }
    }

    @Transactional(readOnly = true)
    boolean hasRelata(IPubblicabile atto) {
        def result = [presente: false]
        if (atto.idDocumentoAlbo > 0) {
            // ottengo la connessione
            Sql sql = new Sql(dataSource)
            sql.call("{ call ? := integrazione_jmessi_pkg.has_relata(?) }"
                    , [Sql.out(OracleTypes.NUMBER), atto.idDocumentoAlbo]) { i ->
                result.presente = i >= 1
            }
        }
        return result.presente
    }

    @Transactional(readOnly = true)
    Map getRelata(IPubblicabile atto) {
        def map = [:]
        if (atto.idDocumentoAlbo > 0) {
            // ottengo la connessione
            Sql sql = new Sql(dataSource)
            sql.call("{ call ? := integrazione_jmessi_pkg.get_relata(?) }"
                    , [Sql.out(OracleTypes.VARCHAR), atto.idDocumentoAlbo]) { xmlout ->
                def xml = new XmlSlurper().parseText(xmlout)
                map.relata =
                        [id_documento: xml.id_documento.text(), id_oggetto_file: xml.id_oggetto_file.text(), filename: xml.filename.text(), data: xml.data.text(), numero: xml.numero.text(), anno: xml.anno.text()]
            }
        }
        return map
    }

    @Transactional
    void pubblicaAtto(IPubblicabile atto) {

        // ottengo il lock pessimistico per evitare doppie numerazioni all'albo.
        atto.lock();
        if (atto.numeroAlbo > 0) {
            log.warn("Il documento è già pubblicato all'albo, non lo ripubblico.");
            return;
        }

        // creo il token di numerazione all'albo: se lo trovo ed ha successo, vuol dire che ho già numerato all'albo:
        TokenIntegrazione token = tokenIntegrazioneService.beginTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_ALBO);
        if (token.isStatoSuccesso()) {
            // significa che ho già protocollato: prendo il numero di protocollo, lo assegno al documento ed esco:
            def map = Eval.me(token.dati);
            atto.numeroAlbo = map.numero;
            atto.annoAlbo = map.anno;
            atto.idDocumentoAlbo = map.idDocumentoAlbo;
            atto.save()

            // allineo il documento gdm solo se sono integrato con GDM.
            // Ci sono clienti (ad es: bagno di romagna) che NON hanno GDM.
            if (Impostazioni.GESTORE_FILE.valore == "gdmGestoreFile") {
                gestoreDocumentaleEsterno.salvaDocumento(atto);
            }

            // elimino il token: tutto è andato bene e verrà eliminato solo alla commit sull transaction principale
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_ALBO)
            return;
        }

        // ottengo la connessione
        Connection conn = dataSource_gdm.connection

        try {
            // setto i campi dell'albo e gli allegati
            ProfiloExtend albo = creaAlbo(conn, atto);

            // se sono in prima pubblicazione, setto la data di inizio pubblicazione:
            albo.settaValore("A_DATA_I_PUBB", atto.dataPubblicazione.format(FORMATO_DATA) ?: "");
            albo.settaValore("A_GG_I_PUBB", atto.pubblicaRevoca ? "" : atto.giorniPubblicazione);
            albo.settaValore("A_DATA_FINE_I_PUBB", atto.pubblicaRevoca ? "" : atto.dataFinePubblicazione?.format(FORMATO_DATA) ?: "");
            albo.settaValore("REVOCA", atto.pubblicaRevoca ? "Y" : "N");

            //		albo.settaValore("A_DATA_II_PUBB", 		atto.dataPubblicazione2?.format(FORMATO_DATA)?:"");
            //		albo.settaValore("A_GG_II_PUBB", 		atto.pubblicaRevoca?"":atto.giorniPubblicazione);
            //		albo.settaValore("A_DATA_FINE_II_PUBB", atto.pubblicaRevoca?"":atto.dataFinePubblicazione2?.format(FORMATO_DATA)?:"");

            if (!albo.salva().booleanValue()) {
                throw new AttiRuntimeException("Errore nel salvare l'albo in pubblicazione: ${albo.getError()}")
            }

            // allineo il documento sul documentale esterno
            gestoreDocumentaleEsterno.salvaDocumento(atto)

            // la prima cosa che faccio dopo la creazione dell'albo è salvare il record su db:
            tokenIntegrazioneService.setTokenSuccess("${atto.id}", TokenIntegrazione.TIPO_ALBO,
                    "[numero:${atto.numeroAlbo}, anno:${atto.annoAlbo}, idDocumentoAlbo:${atto.idDocumentoAlbo}]");

        } catch (Throwable e) {
            log.error("Errore in fase di pubblicazione all'albo: ${e.getMessage()}", e);

            // elimino il token e interrompo la transazione del token (così si possono fare più tentativi)
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_ALBO);
            throw new AttiRuntimeException("Errore in fase di pubblicazione all'albo: ${e.getMessage()}.", e);
        }

        // elimino il token: questo avverrà solo se la transazione "normale" di grails andrà a buon fine:
        tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_ALBO)
    }

    @Transactional
    void secondaPubblicazioneAtto(IPubblicabile atto) {
        Connection conn = dataSource_gdm.connection

        // setto i campi dell'albo e gli allegati
        ProfiloExtend albo = new ProfiloExtend(Long.toString(atto.idDocumentoAlbo), GdmDocumentaleEsterno.GDM_USER, null, conn, false);

        albo.settaValore("A_DATA_II_PUBB", atto.dataPubblicazione2?.format(FORMATO_DATA) ?: "");
        albo.settaValore("A_GG_II_PUBB", atto.pubblicaRevoca ? "" : atto.giorniPubblicazione);
        albo.settaValore("A_DATA_FINE_II_PUBB", atto.pubblicaRevoca ? "" : atto.dataFinePubblicazione2?.format(FORMATO_DATA) ?: "");

        if (!albo.salva().booleanValue()) {
            throw new AttiRuntimeException("Errore nel salvare l'albo per la seconda pubblicazione: ${albo.getError()}")
        }
    }

    @Transactional
    void annullaAtto(IPubblicabile atto, IAtto attoPrincipale) {
        if (atto.idDocumentoAlbo > 0) {
            // ottengo la connessione
            Connection conn = dataSource_gdm.connection
            annullaAttoAlbo(conn, atto.idDocumentoAlbo, "Annullato dall'atto ${attoPrincipale.estremiAtto}");
        }
    }

    @Transactional
    void aggiornaDataEsecutivita(IPubblicabile pubblicabile) {

        // questo serve perché non tutti i documenti hanno la data di esecutività (ad esempio la SedutaStampa)
        if (!(pubblicabile instanceof IAtto)) {
            return
        }

        IAtto atto = (IAtto) pubblicabile
        if (atto.idDocumentoAlbo > 0 && atto.dataEsecutivita != null) {
            // ottengo la connessione
            Connection conn = dataSource_gdm.connection
            ProfiloExtend albo = new ProfiloExtend(Long.toString(atto.idDocumentoAlbo), GdmDocumentaleEsterno.GDM_USER, null, conn, false);
            if (albo.getlistaValori()*.key.contains("DATA_ESECUTIVITA")) {
                albo.settaValore("DATA_ESECUTIVITA", atto.dataEsecutivita.format(FORMATO_DATA));

                // salvo il profilo con le date aggiornate
                if (!albo.salva().booleanValue()) {
                    throw new AttiRuntimeException("Errore nel salvare l'albo per inviare la data esecutività: ${albo.getError()}")
                }
            }
        }
    }

    @Transactional
    void terminaPubblicazioneAtto(IPubblicabile atto) {
        Connection conn = dataSource_gdm.connection

        // setto i campi dell'albo e gli allegati
        ProfiloExtend albo = new ProfiloExtend(Long.toString(atto.idDocumentoAlbo), GdmDocumentaleEsterno.GDM_USER, null, conn, false);

        albo.settaValore("A_DATA_FINE_I_PUBB", atto.dataPubblicazione2.format(FORMATO_DATA));

        // salvo il profilo con le date aggiornate
        if (!albo.salva().booleanValue()) {
            throw new AttiRuntimeException("Errore nel salvare l'albo per terminare la pubblicazione: ${albo.getError()}")
        }
    }

    @Transactional
    void terminaSecondaPubblicazioneAtto(IPubblicabile atto) {
        Connection conn = dataSource_gdm.connection

        // setto i campi dell'albo e gli allegati
        ProfiloExtend albo = new ProfiloExtend(Long.toString(atto.idDocumentoAlbo), GdmDocumentaleEsterno.GDM_USER, null, conn, false);

        albo.settaValore("A_DATA_FINE_II_PUBB", atto.dataPubblicazione2.format(FORMATO_DATA));

        // salvo il profilo con le date aggiornate
        if (!albo.salva().booleanValue()) {
            throw new AttiRuntimeException("Errore nel salvare l'albo per terminare la pubblicazione: ${albo.getError()}")
        }
    }

    private void numeraAlbo(Connection conn, ProfiloExtend albo, IPubblicabile atto) {
        int annoCorrente = Calendar.getInstance().get(Calendar.YEAR)
        albo.settaValore("ANNO_REG", annoCorrente)
        albo.settaValore("TIPO_REG", ALBO_TIPO_REGISTRO)
        String numeroAlbo = getNumeroAlbo(conn, annoCorrente, albo.getCodiceRichiesta())
        log.debug("pubblicaDetermina NUMERO: $numeroAlbo")
        albo.settaValore(ALBO_CAMPO_NUMERO, numeroAlbo)
        if (!albo.salva().booleanValue()) {
            throw new AttiRuntimeException("Errore nel salvare l'albo dopo la numerazione: ${albo.getError()}")
        }

        // imposto il numero dell'albo sull'atto
        atto.numeroAlbo = Integer.parseInt(numeroAlbo)
        atto.annoAlbo = annoCorrente
        atto.save()
    }

    private ProfiloExtend creaAlbo(Connection conn, IPubblicabile atto) throws Exception {
        if (atto.idDocumentoAlbo > 0) {
            return new ProfiloExtend(Long.toString(atto.idDocumentoAlbo), GdmDocumentaleEsterno.GDM_USER, null, conn, false);
        }

        // creo il nuovo profilo per l'albo
        ProfiloExtend albo = new ProfiloExtend(MODELLO_ALBO, AREA_MESSI, GdmDocumentaleEsterno.GDM_USER, null, conn, false);
        albo.escludiControlloCompetenze(true);

        // ottengo l'id-rif
        String idRifAlbo = getIdRifAlbo(conn);
        log.debug("creaAlbo - idRifAlbo: $idRifAlbo")
        albo.settaValore("ID_ALBO", idRifAlbo);

        if (atto instanceof Determina) {
            settaValoriDetermina(conn, albo, atto, idRifAlbo)
            creaDocumentiAllegatiAlbo(conn, atto, albo, idRifAlbo)
        } else if (atto instanceof Delibera) {
            settaValoriDelibera(conn, albo, atto, idRifAlbo)
            creaDocumentiAllegatiAlbo(conn, atto, albo, idRifAlbo)
        } else if (atto instanceof SedutaStampa) {
            settaValoriSedutaStampa(conn, albo, atto, idRifAlbo)
        } else {
            throw new AttiRuntimeException("Tipo di atto ${atto.class.name} non riconosciuto.")
        }

        if (!albo.salva().booleanValue()) {
            throw new AttiRuntimeException("Errore nel salvare l'albo: ${albo.getError()}")
        }

        log.debug("alboJMessi: ${albo.getDocNumber()}")
        atto.idDocumentoAlbo = Long.parseLong(albo.getDocNumber())

        // numero l'albo solo se non è già numerato.
        if (!(atto.numeroAlbo > 0)) {
            numeraAlbo(conn, albo, atto)
        }

        return albo
    }

    private void settaValoriCostanti(ProfiloExtend albo, String xml) {
        def campi = new XmlSlurper().parseText(xml);
        campi.campo.each { campo ->
            albo.settaValore(campo.@nome.text(), campo.@valore?.text() ?: "")
        }
    }

    private void settaValoriSedutaStampa(Connection conn, ProfiloExtend albo, SedutaStampa sedutaStampa, String idRifAlbo) {

        albo.settaValore("A_OGGETTO", sedutaStampa.oggetto)
        albo.settaValore("A_DATA_REGISTRAZIONE", new Date().format(FORMATO_DATA))
        albo.settaValore("ALBO_RISERVATO", "N")
        albo.settaValore("A_TIPO_PUBBLICAZIONE", alboJMessiConfig.getTitoloPubblicazione(sedutaStampa.commissioneStampa.id))
        albo.settaValore("A_SETTORE_RICHIEDENTE", sedutaStampa.getUnitaProponente().descrizione)
        albo.settaValore("ANNO_PROT", sedutaStampa.annoProtocollo)
        albo.settaValore("A_NR_PROT_GEN", sedutaStampa.numeroProtocollo)

        albo.setFileName(sedutaStampa.filePrincipale.nome, gestoreFile.getFile(sedutaStampa, sedutaStampa.filePrincipale))
    }

    private void settaValoriDetermina(Connection conn, ProfiloExtend albo, Determina determina, String idRifAlbo) {
        settaValoriCostanti(albo, Impostazioni.ALBO_COSTANTI_DETERMINA.valore);

        // se ho valorizzato il tipo di pubblicazione all'albo nella tipologia, la uso, altrimenti
        // lascio il valore di default come letto dalle impostazioni
        // http://svi-redmine/issues/13728
        if (determina.tipologiaDocumento.tipoPubblicazioneAlbo?.trim()?.length() > 0) {
            albo.settaValore("A_TIPO_PUBBLICAZIONE", determina.tipologia.tipoPubblicazioneAlbo?.trim())
        }

        albo.settaValore("CODICE_UO", determina.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.codice);
        albo.settaValore("A_SETTORE_RICHIEDENTE", determina.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.descrizione);

        albo.settaValore("A_ANNO_DETE", determina.annoDetermina);
        albo.settaValore("A_NR_DETE", determina.numeroDetermina);
        albo.settaValore("A_DATA_DETE", determina.dataNumeroDetermina.format(FORMATO_DATA));
        albo.settaValore("A_COD_REG_DETE", determina.registroDetermina.codice);
        albo.settaValore("A_REGISTRO_DETE", determina.registroDetermina.descrizione);
        albo.settaValore("ID_DOC_DETERMINA", determina.idDocumentoEsterno);
        albo.settaValore("A_SETTORE_DETE", determina.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.descrizione);

        albo.settaValore("A_OGGETTO", determina.oggetto);

        albo.settaValore("N_MESSO_INSERIMENTO_UTENTE", springSecurityService.currentUser.id);
        albo.settaValore("A_DATA_REGISTRAZIONE", new Date().format(FORMATO_DATA));
        albo.settaValore("ALBO_RISERVATO", "N");
        albo.settaValore("VIS_ALLEGATI_RISERVATI", determina.tipologiaDocumento.pubblicaAllegati ? "Y" : "N");
    }

    private void settaValoriDelibera(Connection conn, ProfiloExtend albo, Delibera delibera, String idRifAlbo) {
        settaValoriCostanti(albo, Impostazioni.ALBO_COSTANTI_DELIBERA.valore);

        // se ho valorizzato il tipo di pubblicazione all'albo nella tipologia, la uso, altrimenti
        // lascio il valore di default come letto dalle impostazioni
        // http://svi-redmine/issues/13728
        if (delibera.tipologiaDocumento.tipoPubblicazioneAlbo?.trim()?.length() > 0) {
            albo.settaValore("A_TIPO_PUBBLICAZIONE", delibera.tipologiaDocumento.tipoPubblicazioneAlbo?.trim());
        }

        albo.settaValore("CODICE_UO", delibera.proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.codice);
        albo.settaValore("A_SETTORE_RICHIEDENTE", delibera.proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.descrizione);

        albo.settaValore("A_ANNO_DELI", delibera.annoDelibera);
        albo.settaValore("A_NR_DELI", delibera.numeroDelibera);
        albo.settaValore("A_DATA_ATTO", delibera.dataNumeroDelibera.format(FORMATO_DATA));
        albo.settaValore("A_COD_REG_DELI", delibera.registroDelibera.codice);
        albo.settaValore("A_ORGANO_DELIBERANTE", delibera.registroDelibera.descrizione);
        albo.settaValore("ID_DOC_DELIBERA", delibera.idDocumentoEsterno);

        albo.settaValore("A_OGGETTO", delibera.oggetto);

        albo.settaValore("N_MESSO_INSERIMENTO_UTENTE", springSecurityService.currentUser.id);
        albo.settaValore("A_DATA_REGISTRAZIONE", new Date().format(FORMATO_DATA));
        albo.settaValore("ALBO_RISERVATO", "N");
        albo.settaValore("VIS_ALLEGATI_RISERVATI", delibera.tipologiaDocumento.pubblicaAllegati ? "Y" : "N");
    }

    private creaDocumentiAllegatiAlbo(Connection conn, IAtto atto, ProfiloExtend albo, String idRifAlbo) {
        // pubblico gli allegati se
        // - l'attributo da tipologia vale Y e
        // - l'atto non è riservato oppure

        // se l'impostazione dice che devo pubblicare la stampa unica (nella stampa unica non c'è il testo se riservato), la pubblico, altrimenti pubblico il file principale.
        if ("SU".equals(Impostazioni.FILE_DA_PUBBLICARE.valore) || "All".equals(Impostazioni.FILE_DA_PUBBLICARE.valore)) {

            // se non ho la stampa unica, la creo:
            if (atto.stampaUnica == null) {
                stampaUnicaService.stampaUnica(atto);
            }

            // se comunque non ho la stampa unica (ad es. il documento è riservato e non ho la stampa unica), salto il passaggio
            if (atto.stampaUnica != null) {
                albo.setFileName(atto.stampaUnica.nome, gestoreFile.getFile(atto, atto.stampaUnica))
            }

        }
        if (("T".equals(Impostazioni.FILE_DA_PUBBLICARE.valore) || "All".equals(Impostazioni.FILE_DA_PUBBLICARE.valore)) && atto.testo != null && !atto.riservato) {
            // altrimenti pubblico il file principale
            albo.setFileName(atto.testo.nome, gestoreFile.getFile(atto, atto.testo))
        }

        // pubblico gli allegati (quelli non riservati e quelli su cui c'è scritto che devono essere pubblicati)
        if (atto.tipologiaDocumento.pubblicaAllegati) {
            for (Allegato allegato : atto.allegati.sort { it.sequenza }) {
                // non vanno pubblicati gli allegati non validi
                if (!allegato.valido || !allegato.pubblicaAlbo) {
                    continue
                }

                // non vanno pubblicate le schede contabili degli atti riservati quando è disabilitata l'impostazione PUBBLICAZIONE_SCHEDA_CONTABILE
                if (atto.riservato && Allegato.ALLEGATO_SCHEDA_CONTABILE == allegato.codice && !Impostazioni.PUBBLICAZIONE_SCHEDA_CONTABILE.abilitato) {
                    continue
                }

                creaAllegatoAlbo(conn, allegato, idRifAlbo)
            }
        }

        // pubblico i visti/pareri
        if (Impostazioni.PUBBLICAZIONE_VISTI.abilitato || Impostazioni.PUBBLICAZIONE_ALLEGATI_VISTI.abilitato) {
            // se la delibera ha dei pareri, pubblico solo quelli e non quelli della proposta.
            def vistiPareriAtto = atto.visti.findAll{it.valido == true}
            def vistiPareriProposta = (atto instanceof Delibera) ? atto.proposta.visti.findAll{it.valido == true} : []
            def vistiPareri = (vistiPareriProposta ?: []) + (vistiPareriAtto ?: [])

            for (def vistoParere : vistiPareriProposta){
                if (vistiPareriAtto.findAll{ it.tipologia.codice == vistoParere.tipologia.codice}.size() > 0){
                    vistiPareri.remove(vistoParere)
                }
            }

            for (VistoParere visto : vistiPareri) {
                if (!visto.valido || !visto.tipologia.pubblicazione) {
                    continue
                }

                if (Impostazioni.PUBBLICAZIONE_VISTI.abilitato && visto.testo != null) {
                    creaVistoAlbo(conn, visto, idRifAlbo)
                }

                if (Impostazioni.PUBBLICAZIONE_ALLEGATI_VISTI.abilitato) {
                    for (Allegato allegato : visto.allegati) {

                        // pubblico all'albo solo se l'allegato è valido ed ha il flag di pubblicazione valorizzato.
                        if (!allegato.valido || !allegato.pubblicaAlbo) {
                            continue
                        }

                        creaAllegatoAlbo(conn, allegato, idRifAlbo)
                    }
                }
            }
        }
    }

    private void creaAllegatoAlbo(Connection conn, Allegato allegato, String idRifAlbo) {
        log.debug("creaAllegatoAlbo - idRifAlbo: $idRifAlbo")
        // creo il documento allegato
        ProfiloExtend allegatoAlbo = new ProfiloExtend(MODELLO_ALLEGATO_ALBO, AREA_MESSI, GdmDocumentaleEsterno.GDM_USER, null, conn, false);
        allegatoAlbo.escludiControlloCompetenze(true);

        // setto i vari campi
        allegatoAlbo.settaValore("ALLEGATO_ALBO_QUANTITA", allegato.quantita);
        allegatoAlbo.settaValore("ALLEGATO_ALBO_DESCRIZIONE", allegato.titolo);
        allegatoAlbo.settaValore("ALLEGATO_ALBO_N_PAGINE", allegato.numPagine);
        allegatoAlbo.settaValore("ALLEGATO_ALBO_TIPO_ALLEGATO", allegato.tipoAllegato?.titolo ?: "");
        allegatoAlbo.settaValore("ALLEGATO_RISERVATO", allegato.riservato ? "Y" : "N");
        allegatoAlbo.settaValore("ID_ALBO", idRifAlbo);

		// allego i file
		for (FileAllegato fileAllegato : allegato.fileAllegati.sort { it.id }) {
			allegatoAlbo.setFileName(fileAllegato.nome, gestoreFile.getFile(allegato, fileAllegato));
		}

        // salvo
        if (!allegatoAlbo.salva().booleanValue()) {
            log.error("Impossibile salvare il documento relativo all'allegato dell'albo. " + allegatoAlbo.getError());
            throw new AttiRuntimeException("Impossibile salvare il documento relativo all'allegato dell'albo. " + allegatoAlbo.getError());
        }
    }

    private creaVistoAlbo(Connection conn, VistoParere visto, String idRifAlbo) {
        log.debug("creaAllegatoAlbo - idRifAlbo: $idRifAlbo")
        // creo il documento allegato
        ProfiloExtend allegatoAlbo = new ProfiloExtend(MODELLO_ALLEGATO_ALBO, AREA_MESSI, GdmDocumentaleEsterno.GDM_USER, null, conn, false)
        allegatoAlbo.escludiControlloCompetenze(true)

        // setto i vari campi
        allegatoAlbo.settaValore("ALLEGATO_ALBO_QUANTITA", "1")
        allegatoAlbo.settaValore("ALLEGATO_ALBO_DESCRIZIONE", visto.tipologia.titolo)
        allegatoAlbo.settaValore("ALLEGATO_ALBO_N_PAGINE", "")
        allegatoAlbo.settaValore("ID_ALBO", idRifAlbo)

        // allego i file
        allegatoAlbo.setFileName(visto.testo.nome, gestoreFile.getFile(visto, visto.testo))

        // salvo
        if (!allegatoAlbo.salva().booleanValue()) {
            log.error("Impossibile salvare il documento relativo all'allegato dell'albo. " + allegatoAlbo.getError())
            throw new AttiRuntimeException("Impossibile salvare il documento relativo all'allegato dell'albo. " + allegatoAlbo.getError())
        }
    }

    private void annullaAttoAlbo(Connection conn, Long idDocumentoAlbo, String motivazione) throws Exception {
        Sql sql = new Sql(conn)
        String functionOutput = null;
        String numero = null
//		FUNCTION f_annulla_albo (
//			p_documento    IN   VARCHAR2,
//			p_data_ann     IN   VARCHAR2,
//			p_utente_ann   IN   VARCHAR2,
//			p_motivo_ann   IN   VARCHAR2
//		 ) RETURN VARCHAR2

        sql.call("{ call ? := AG_MES_UTILITY.f_annulla_albo(?, ?, ?, ?) }"
                , [Sql.out(OracleTypes.VARCHAR), Long.toString(idDocumentoAlbo), new Date().format(
                "dd/MM/yyyy"), springSecurityService.currentUser.id, motivazione]) { xmlout ->
            def xml = new XmlSlurper().parseText(xmlout)
            if (xml.RESULT.text() != 'ok') {
                throw new AttiRuntimeException("Errore nell'annullamento dell'albo: ${xml.ERROR.text()}")
            }
        }
    }

    private String getNumeroAlbo(Connection conn, int annoCorrente, String codiceRichiesta) throws Exception {
        Sql sql = new Sql(conn)
        String functionOutput = null;
        String numero = null
        sql.call(F_NUMERA_ALBO, [Sql.out(OracleTypes.VARCHAR), Integer.toString(annoCorrente), codiceRichiesta]) { xmlout ->
            def xml = new XmlSlurper().parseText(xmlout)
            if (xml.RESULT.text() != 'ok') {
                throw new AttiRuntimeException("Errore nella numerazione dell'albo: ${xml.ERROR.text()}")
            }
            numero = xml.DOC."${ALBO_CAMPO_NUMERO}".text()
        }

        return numero
    }

    private String getIdRifAlbo(Connection conn) throws Exception {
        Sql sql = new Sql(conn)
        String idRifAlbo = null
        sql.eachRow(Q_NUMERO_ALBO) { row ->
            idRifAlbo = row.ID_ALBO
        }
        return idRifAlbo
    }

    public boolean controllaDocumentiAlboConErrore(){
        Sql sql = new Sql (dataSource)
        def rows = sql.rows("select * from DOCUMENTI_ERRORE_ALBO")
        if (rows.size() > 0) {
            return true
        }
        return false
    }
}
