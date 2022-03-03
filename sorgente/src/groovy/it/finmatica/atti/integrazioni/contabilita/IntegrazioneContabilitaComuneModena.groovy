package it.finmatica.atti.integrazioni.contabilita

import grails.plugin.springsecurity.SpringSecurityService
import org.springframework.transaction.annotation.Transactional
import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.documenti.*
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.lookup.LookupEnti
import it.finmatica.atti.integrazioni.lookup.LookupStatoEsecutivo
import it.finmatica.atti.integrazioni.lookup.LookupTutti
import it.finmatica.atti.integrazioni.lookup.LookupUfficio
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.ModuloIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.atti.integrazioniws.comunemodena.contabilita.*
import it.finmatica.gestionetesti.GestioneTestiService
import org.apache.log4j.Logger
import org.apache.tools.ant.taskdefs.Input
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.zkoss.bind.BindUtils
import org.zkoss.zk.ui.event.EventQueues

import javax.xml.datatype.DatatypeFactory

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW

/**
 *
 * Note per lo sviluppo:
 *
 * la determina di prova su cui anche loro hanno inserito movimenti contabili è la 11/2015
 */
@Component("integrazioneContabilitaComuneModena")
@Lazy
class IntegrazioneContabilitaComuneModena extends AbstractIntegrazioneContabilita {

    public static final Logger log = Logger.getLogger(IntegrazioneContabilitaComuneModena.class)

    @Autowired IntegrazioneContabilitaComuneModenaConfig integrazioneContabilitaComuneModenaConfig
    @Autowired AttiAmministrativi       integrazioneContabilitaComuneModenaWebService
	@Autowired SpringSecurityService    springSecurityService
    @Autowired GestioneTestiService     gestioneTestiService
    @Autowired AllegatoService          allegatoService

	/*
	 * Implementazione AbstractIntegrazioneContabilita
	 */
	@Override
	String getZul(IDocumento documento) {
		return "/atti/integrazioni/contabilita/movimentiCf4.zul"
	}

	@Override
	boolean isConDocumentiContabili(IDocumento documento) {
		IProposta proposta = getProposta(documento)
		return (MovimentoContabile.countByIdDocumentoAndTipoDocumento (proposta.id, proposta.TIPO_OGGETTO) > 0)
	}

	@Override
	boolean isTipiDocumentoAbilitati() {
		return false
	}

	void aggiornaMaschera (IDocumento documento, boolean modifica) {
		BindUtils.postGlobalCommand("movimentiContabiliQueue", EventQueues.DESKTOP, "aggiornaAtto", [atto:documento, competenza:modifica?"W":"R"])
	}

    @Transactional
	void aggiornaMovimentiContabili (IDocumento documento) {
		IProposta proposta = getProposta(documento);
        List<RecordDettagliContabiliAtto> listaRecordDettagliContabilitaAtto = new ArrayList<RecordDettagliContabiliAtto>();
        Integer numeroBloccoInformazioni = 0;
        int caricaAltriBlocchi = 1
        while (caricaAltriBlocchi > 0) {
            ListaRecordDettagliContabilitaAtto result = integrazioneContabilitaComuneModenaWebService.ricercaDettagliContabiliAtto(proposta.id.toString()
                    , null //MappingIntegrazione.getValoreEsternoInt (MAPPING_CATEGORIA, MAPPING_SETTORE, 	proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.codice)
                    , null //MappingIntegrazione.getValoreEsternoInt (MAPPING_CATEGORIA, MAPPING_DIVISIONE,proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.codice)
                    , null //MappingIntegrazione.getValoreEsternoInt (MAPPING_CATEGORIA, MAPPING_UFFICIO, 	proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.codice)
                    , null //proposta.tipologia.codiceEsterno
                    , null //proposta.numeroProposta.toString()
                    , null //proposta.annoProposta
                    , null
                    , integrazioneContabilitaComuneModenaConfig.getCodiceEnte(proposta)
                    , integrazioneContabilitaComuneModenaConfig.getCodiceUtente(proposta)
                    , numeroBloccoInformazioni)

            if (result.getEsito().getEsito() > 0) {
                AttiRuntimeException e = new AttiRuntimeException("Errore (codice: ${result.esito.esito}) in invio movimenti contabili al SIB: ${result.esito.messaggio}")
                log.error(e.message, e)
                throw e
            }

            caricaAltriBlocchi = result.esito.altreInformazioni.intValue()
            numeroBloccoInformazioni++
            listaRecordDettagliContabilitaAtto += result.listaRecordDettagliContabilitaAtto
        }

		// salvo i vari documenti di contabilità:
		upsertDettaglioContabile (proposta, listaRecordDettagliContabilitaAtto);
	}

    @Transactional
	List<?> getMovimentiContabili (IDocumento documento) {
		IProposta proposta = getProposta(documento);

        List<RecordDettagliContabiliAtto> listaRecordDettagliContabilitaAtto = new ArrayList<RecordDettagliContabiliAtto>();
        int numeroBloccoInformazioni = 0;
        int caricaAltriBlocchi = 1
        while (caricaAltriBlocchi > 0) {
            ListaRecordDettagliContabilitaAtto result = integrazioneContabilitaComuneModenaWebService.ricercaDettagliContabiliAtto(proposta.id.toString()
                    , null //MappingIntegrazione.getValoreEsternoInt (MAPPING_CATEGORIA, MAPPING_SETTORE, 	proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.codice)
                    , null //MappingIntegrazione.getValoreEsternoInt (MAPPING_CATEGORIA, MAPPING_DIVISIONE,proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.codice)
                    , null //MappingIntegrazione.getValoreEsternoInt (MAPPING_CATEGORIA, MAPPING_UFFICIO, 	proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.codice)
                    , null //proposta.tipologia.codiceEsterno
                    , null //proposta.numeroProposta.toString()
                    , null //proposta.annoProposta
                    , null
                    , integrazioneContabilitaComuneModenaConfig.getCodiceEnte(proposta)
                    , integrazioneContabilitaComuneModenaConfig.getCodiceUtente(proposta)
                    , numeroBloccoInformazioni)

            if (result.getEsito().getEsito() > 0) {
            AttiRuntimeException e = new AttiRuntimeException ("Errore (codice: ${result.esito.esito}) in recupero movimenti contabili dal SIB: ${result.esito.messaggio}")
                log.error(e.message, e)
                throw e
            }

            caricaAltriBlocchi = result.esito.altreInformazioni.intValue()
            numeroBloccoInformazioni++
            listaRecordDettagliContabilitaAtto += result.listaRecordDettagliContabilitaAtto
        }

        // salvo i vari documenti di contabilità:
        upsertDettaglioContabile (proposta, listaRecordDettagliContabilitaAtto);

		def listaMovimenti = [];
		for (def row : listaRecordDettagliContabilitaAtto) {
			def movimento = [ tipo: 			row.tipo,
							  descrizione: 		row.descrizione,
							  importo: 			row.importo,
							  ragioneSociale: 	row.descrizioneSoggetto,
							  rifBilPeg: 		row.tipoVarDiBil,
							  anno: 			row.annoEsercizio,
							  numero: 			row.numero]
			 listaMovimenti.add(movimento)
		}
		return listaMovimenti;
	}

    @Transactional
	void salvaAtto (IAtto atto) {
		// siccome non posso invocare due volte il metodo "trasferimentoAtto", invio l'atto solo se è una Determina
		if (atto instanceof Delibera) {
			return
		}

		String codiceRegistroEsterno = getCodiceRegistroEsterno (atto.proposta)

        if (codiceRegistroEsterno == null) {
            return
        }

		RecordAtto recordAtto = new RecordAtto (  idEsterno          : atto.proposta.id
												, annoProposta       : atto.proposta.annoProposta
												, attoProposta       : atto.proposta.numeroProposta
												, dataProposta       : DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(time: atto.proposta.dataNumeroProposta))
												, descrizione        : atto.oggetto
												, tipoProposta       : codiceRegistroEsterno
												, annoProvvedimento  : atto.annoAtto
												, attoEsecutivita    : null
												, attoProvvedimento  : atto.numeroAtto
												, dataEsecutivita    : atto.dataEsecutivita ? DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(time: atto.dataEsecutivita)) : null
												, dataProvvedimento  : DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(time: atto.dataAtto))
												, idSib              : null
												, tipoEsecutivita    : null
												, tipoProvvedimento  : codiceRegistroEsterno
												, codiceDivisione    : integrazioneContabilitaComuneModenaConfig.getCodiceDivisione()
												, codiceSettore      : integrazioneContabilitaComuneModenaConfig.getCodiceSettore()
												, codiceUfficio      : integrazioneContabilitaComuneModenaConfig.getCodiceUfficio()
												, codiceUtente       : integrazioneContabilitaComuneModenaConfig.getCodiceUtente (atto.proposta)
												, ente               : integrazioneContabilitaComuneModenaConfig.getCodiceEnte (atto))

        recordAtto.properties.each{ k, v -> log.debug("salvaAtto: ${k}:${v}") }

		ListaRecordAtto result = integrazioneContabilitaComuneModenaWebService.trasferimentoAtto(recordAtto);

		if (result.getEsito().getEsito() > 0) {
			AttiRuntimeException e = new AttiRuntimeException ("Errore (codice: ${result.esito.esito}) in trasferimento atto dal SIB: ${result.esito.messaggio}")

			// se l'errore è che il documento è stato già inviato al sib, lo ignoro
			if (result.esito.messaggio?.startsWith("CHIAVE DUPLICATA IN INSERIMENTO")) {

				log.warn(e.message, e)
				return
			} else {

				// altrimenti do' errore e blocco tutto.
				log.error(e.message, e)
				throw e
			}
		}
	}

    @Transactional
	void salvaProposta (IProposta proposta) {
        // siccome non posso invocare due volte il metodo "trasferimentoAtto", invio l'atto solo se è una proposta di delibera
        if (proposta instanceof Determina) {
            return
        }

        String codiceRegistroEsterno = getCodiceRegistroEsterno (proposta)

        if (codiceRegistroEsterno == null) {
            return
        }

        RecordAtto recordAtto = new RecordAtto (  idEsterno          : proposta.id
                , annoProposta       : proposta.annoProposta
                , attoProposta       : proposta.numeroProposta
                , dataProposta       : DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(time: proposta.dataNumeroProposta))
                , descrizione        : proposta.oggetto
                , tipoProposta       : codiceRegistroEsterno
                , annoProvvedimento  : null
                , attoEsecutivita    : null
                , attoProvvedimento  : null
                , dataEsecutivita    : null
                , dataProvvedimento  : null
                , idSib              : null
                , tipoEsecutivita    : null
                , tipoProvvedimento  : null
                , codiceDivisione    : integrazioneContabilitaComuneModenaConfig.getCodiceDivisione()
                , codiceSettore      : integrazioneContabilitaComuneModenaConfig.getCodiceSettore()
                , codiceUfficio      : integrazioneContabilitaComuneModenaConfig.getCodiceUfficio()
                , codiceUtente       : integrazioneContabilitaComuneModenaConfig.getCodiceUtente (proposta)
                , ente               : integrazioneContabilitaComuneModenaConfig.getCodiceEnte (proposta))

        recordAtto.properties.each{ k, v -> log.debug("salvaProposta: ${k}:${v}") }
        ListaRecordAtto result = integrazioneContabilitaComuneModenaWebService.trasferimentoAtto(recordAtto)

        if (result.getEsito().getEsito() > 0) {
            AttiRuntimeException e = new AttiRuntimeException ("Errore (codice: ${result.esito.esito}) in trasferimento atto dal SIB: ${result.esito.messaggio}")

            // se l'errore è che il documento è stato già inviato al sib, lo ignoro
            if (result.esito.messaggio?.startsWith("CHIAVE DUPLICATA IN INSERIMENTO")) {

                log.warn(e.message, e)
                return
            } else {

                // altrimenti do' errore e blocco tutto.
                log.error(e.message, e)
                throw e
            }
        }
	}

    @Transactional
	void rendiEsecutivoAtto (IAtto atto) {
        if (!atto.tipologiaDocumento.esecutivitaMovimenti) {
            return
        }

		String codiceRegistroEsterno = getCodiceRegistroEsterno (atto.proposta)

        if (codiceRegistroEsterno == null) {
            return
        }

		RecordEsecutivitaAtto recordEsecutivitaAtto = new RecordEsecutivitaAtto (  idEsterno          : atto.proposta.id
												, annoProposta       : atto.proposta.annoProposta
                                                , annoProvvedimento  : atto.annoAtto
												, attoProposta       : atto.proposta.numeroProposta
                                                , attoProvvedimento  : atto.numeroAtto
												, tipoProposta       : codiceRegistroEsterno
												, attoEsecutivita    : atto.numeroAtto
												, dataEsecutivita    : atto.dataEsecutivita ? DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(time: atto.dataEsecutivita)) : null
                                                , dataProvvedimento  : DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(time: atto.dataAtto))
												, idSib              : null
												, flgAnnullaDettagliContabili: null
												, tipoEsecutivita    : integrazioneContabilitaComuneModenaConfig.getStatoEsecutivita()
                                                , codiceDivisione    : integrazioneContabilitaComuneModenaConfig.getCodiceDivisione()
                                                , codiceSettore      : integrazioneContabilitaComuneModenaConfig.getCodiceSettore()
                                                , codiceUfficio      : integrazioneContabilitaComuneModenaConfig.getCodiceUfficio()
                                                , codiceUtente       : integrazioneContabilitaComuneModenaConfig.getCodiceUtente (atto.proposta)
                                                , ente               : integrazioneContabilitaComuneModenaConfig.getCodiceEnte (atto)
                                                , tipoProvvedimento  : codiceRegistroEsterno)


        recordEsecutivitaAtto.properties.each{ k, v -> log.debug("rendiEsecutivoAtto: ${k}:${v}") }

		ListaRecordEsecutivitaAtto result = integrazioneContabilitaComuneModenaWebService.trasferimentoEsecutivitaAtto (recordEsecutivitaAtto);

        if (result.getEsito().getEsito() > 0) {
            AttiRuntimeException e = new AttiRuntimeException ("Errore (codice: ${result.esito.esito}) in trasferimento atto dal SIB: ${result.esito.messaggio}")

            // se l'errore è che il documento è stato già inviato al sib, lo ignoro
            if (result.esito.messaggio?.startsWith("CHIAVE DUPLICATA IN INSERIMENTO")) {

                log.warn(e.message, e)
                return
            } else {

                // altrimenti do' errore e blocco tutto.
                log.error(e.message, e)
                throw e
            }
        }
	}

    @Transactional(readOnly=true)
    String getErroriMovimentiContabili (IProposta proposta) {
        return MovimentoContabile.findAllByIdDocumentoAndStatoInList(proposta.id, [MovimentoContabile.STATO_ERRORE], [sort:"id", order:"asc"])*.descrizione.join("\n")
    }

    @Transactional
    void inviaMovimentiContabili (IProposta proposta) {
        List<MovimentoContabile> movimentiContabili = MovimentoContabile.findAllByIdDocumentoAndStatoInList(proposta.id, [MovimentoContabile.STATO_ERRORE, MovimentoContabile.STATO_DA_INVIARE], [sort:"id", order:"asc"])
        for (MovimentoContabile m : movimentiContabili) {
            inviaMovimentoContabile(m)
            m.refresh()
        }
    }

    @Transactional(propagation=REQUIRES_NEW)
    void inviaMovimentoContabile (MovimentoContabile m) {
        RecordTrasferimentoDettagliContabili dettaglioContabile = new RecordTrasferimentoDettagliContabili()
        dettaglioContabile.aimpr = m.annoEsercizio
        def risultato = integrazioneContabilitaComuneModenaWebService.trasferimentoDettagliContabili(dettaglioContabile)
        if (risultato != null) {
            m.stato = MovimentoContabile.STATO_INVIATO
            m.save()
        } else {
            m.stato = MovimentoContabile.STATO_ERRORE
            m.statoDescrizione = "Si è verificato un errore nell'invio del movimento con id: ${m.id} al webservice."
            m.save()
        }
    }

    InputStream getSchedaContabile (IDocumento documento) {
        TipoAllegato tipoAllegato = allegatoService.getTipoAllegato(Allegato.ALLEGATO_SCHEDA_CONTABILE, documento.tipoOggetto)
        if (tipoAllegato == null) {
            throw new AttiRuntimeException("Non è possibile creare la scheda contabile: è necessario configurare un tipo allegato con codice 'SCHEDA_CONTABILE'")
        }

        if (tipoAllegato.modelloTesto == null) {
            throw new AttiRuntimeException("Non è possibile creare la scheda contabile: è necessario configurare un modello di testo per il tipo allegato '${tipoAllegato.titolo}'.")
        }

        return gestioneTestiService.stampaUnione(tipoAllegato.modelloTesto, [id: documento.id], Impostazioni.FORMATO_DEFAULT.valore)
    }

    /*
     * Metodi di utilità
     */

	private String getCodiceRegistroEsterno (IProposta proposta) {
		if (proposta instanceof Determina) {
			return proposta.tipologia.tipoRegistro?.registroEsterno
		} else if (proposta instanceof PropostaDelibera) {
			return (proposta.tipologia.tipoRegistroDelibera?.registroEsterno)?:(proposta.commissione?.tipoRegistro?.registroEsterno)
		}
	}

	private void upsertDettaglioContabile (IProposta proposta, List<RecordDettagliContabiliAtto> records) {
		// prima svuoto i movimenti contabili
		MovimentoContabile.findAllByIdDocumentoAndTipoDocumento (proposta.id, proposta.TIPO_OGGETTO)*.delete()

		// poi li ricreo
		for (RecordDettagliContabiliAtto d : records) {
			MovimentoContabile m = new MovimentoContabile ()
            m.idDocumento 				= proposta.id
            m.tipoDocumento 			= proposta.tipoOggetto
            m.annoCompetenza 		 	= d.annoCompetenza
            m.annoCrono			 	 	= d.annoCrono
            m.annoEsercizio			 	= d.annoEsercizio
            m.articolo				 	= d.articolo
            m.capitolo				 	= d.capitolo
            m.codiceFinanziamento1	 	= d.codFnz1
            m.codiceFinanziamento2	 	= d.codFnz2
            m.codiceFinanziamento3	 	= d.codFnz3
            m.codiceSoggetto		 	= d.codiceSoggetto
            m.dataDettaglio			 	= d.dataDettaglio?.toGregorianCalendar()?.getTime()
            m.descrizione			 	= d.descrizione
            m.descrizioneCapitolo	 	= d.descrizioneCapitolo
            m.descrizioneFinanziamento1 = d.desFnz1
            m.descrizioneFinanziamento2 = d.desFnz2
            m.descrizioneFinanziamento3 = d.desFnz3
            m.descrizioneInvestimento  	= d.descrizioneInvestimento
            m.descrizioneSoggetto	 	= d.descrizioneSoggetto
            m.disposizioneMandati		= d.dispMandati
            m.importo				 	= d.importo
            m.importoCassa			 	= d.importoCassa
            m.importoFinanziamento1	 	= d.impFnz1
            m.importoFinanziamento2	 	= d.impFnz2
            m.importoFinanziamento3	 	= d.impFnz3
            m.investimento			 	= d.investimento
            m.missione				 	= d.missione
            m.numero				 	= d.numero
            m.numeroCrono			 	= d.numeroCrono
            m.numeroDet			 	 	= d.numeroDet
            m.numeroVariazione	 	 	= d.varNumero
            m.opera				 	 	= d.opera
            m.pdcLiv1			 		= d.pdcLivuno
            m.pdcLiv2			 		= d.pdcLivdue
            m.pdcLiv3			 		= d.pdcLivtre
            m.pdcLiv4			 		= d.pdcLivqua
            m.pdcLiv5			 		= d.pdcLivcin
			if ((d.flagPrenotazione != null) && (d.flagPrenotazione == 'N'))
				m.prenotazione = false
			else
				m.prenotazione = true
            m.progetto				 	= d.progetto
            m.programma			 	 	= d.programma
            m.numeroSub			 	 	= d.subNumero
            m.tipo					 	= d.tipo
            m.tipoDettaglio		 	 	= d.tipoDettaglio
            m.tipoVariazioneDiBilancio 	= d.tipoVarDiBil
			m.save()
		}
	}

    /*
     * Metodi per il ViewModel
     */

	RecordCapitolo getDettagliCapitolo(String codiceEnteInterno, String codiceUoProponente, String movimento, Integer annoEsercizio, String capitolo, String articolo, Integer numero) {
		return integrazioneContabilitaComuneModenaWebService.ricercaCapitoli(
                  integrazioneContabilitaComuneModenaConfig.getCodiceEnte(codiceEnteInterno)  // "ente"
                , Integer.toString(annoEsercizio)   // "annoCompetenza"
                , Integer.toString(annoEsercizio)   // "annoGestione"
                , movimento
                , capitolo
                , null
                , integrazioneContabilitaComuneModenaConfig.getCodiceUtente(codiceUoProponente))
	}

    List<RecordPdc> getListaPianoDeiConti (String codiceEnteInterno, String codiceUoProponente, String movimento, RecordCapitolo capitolo) {
        return integrazioneContabilitaComuneModenaWebService.ricercaPdc (
                  integrazioneContabilitaComuneModenaConfig.getCodiceEnte(codiceEnteInterno)      //"ente"
                , integrazioneContabilitaComuneModenaConfig.getCodiceUtente(codiceUoProponente)   //"codiceUtente"
                , capitolo.esercizio
                , movimento
                , capitolo.liv1Pf
                , capitolo.liv2Pf
                , capitolo.liv3Pf
                , capitolo.liv4Pf
                , capitolo.liv5Pf
                , ""                                    //"flgUtilImpAcc"
        ).getPdc()
    }

    List<RecordVoceEconomica> getListaVociEconomiche(String codiceEnteInterno, String codiceUoProponente, String movimento, RecordCapitolo capitolo) {
        return integrazioneContabilitaComuneModenaWebService.ricercaVociEconomiche(
                  integrazioneContabilitaComuneModenaConfig.getCodiceEnte(codiceEnteInterno)      //"ente"
                , integrazioneContabilitaComuneModenaConfig.getCodiceUtente(codiceUoProponente)   //"codiceUtente"
                , capitolo.esercizio
                , movimento
                , capitolo.titolo
                , capitolo.intervento).getVociEconomiche()
    }


    List<RecordSiope> getListaCodiciSiope (String codiceEnteInterno, String codiceUoProponente, String movimento, RecordCapitolo capitolo, String voceEconomica) {
        return integrazioneContabilitaComuneModenaWebService.ricercaSiope (
                  integrazioneContabilitaComuneModenaConfig.getCodiceEnte(codiceEnteInterno)      //"ente"
                , integrazioneContabilitaComuneModenaConfig.getCodiceUtente(codiceUoProponente)   //"codiceUtente"
                , capitolo.esercizio
                , movimento
                , capitolo.titolo
                , capitolo.intervento
                , voceEconomica).getSiope()
    }

    List<RecordSoggetti> getListaSoggetti (String codiceEnteInterno, String codiceUoProponente, String movimento, String codiceSoggetto, String ragioneSociale, String codiceFiscale, String partitaIVA, String localita) {
        return integrazioneContabilitaComuneModenaWebService.ricercaSoggetti (
                  integrazioneContabilitaComuneModenaConfig.getCodiceEnte(codiceEnteInterno)      //"ente"
                , integrazioneContabilitaComuneModenaConfig.getCodiceUtente(codiceUoProponente)   //"codiceUtente"
                , movimento
                , codiceSoggetto
                , ragioneSociale
                , localita
                , codiceFiscale
                , partitaIVA).getSoggetti()
    }

    RecordSoggetti getSoggetto (String codiceEnteInterno, String codiceUoProponente, String movimento, String codiceSoggetto) {
        List<RecordSoggetti> soggetti = getListaSoggetti (
                  codiceEnteInterno
                , codiceUoProponente
                , movimento
                , codiceSoggetto
                , ""
                , ""
                , ""
                , "")
        if (soggetti.size() > 0) {
            return soggetti.get(0)
        }

        return null
    }

    def getListaCodiciStatistici (String codiceEnteInterno, String codiceUoProponente, String tipoCodiceStatistico) {
        return [[codice:"01", descrizione: "codice statistico 1"]
              , [codice:"02", descrizione: "codice statistico 2"]
              , [codice:"03", descrizione: "codice statistico 3"]
              , [codice:"04", descrizione: "codice statistico 4"]
              , [codice:"05", descrizione: "codice statistico 5"]]
    }
}
