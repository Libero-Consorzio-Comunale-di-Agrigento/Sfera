package it.finmatica.atti.integrazioni

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CaratteristicaTipologiaService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.ws.dati.Documento
import it.finmatica.atti.integrazioni.ws.dati.RiferimentoFile
import it.finmatica.atti.zk.SoggettoDocumento
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.gestionetesti.TipoFile
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.apache.commons.io.IOUtils
import org.apache.commons.io.FilenameUtils

/**
 * Questo Service legge i documenti esterni presenti nelle tabelle AG_ACQUISIZIONE_DETERMINE e AG_ACQUISIZIONE_ALLEGATI e crea documenti
 * su Determine e Allegati.
 *
 * @author esasdelli
 */
class DocumentiEsterniService {

    public static final String APPLICATIVO_ESTERNO_WEBSERVICE = "WS"
    public static final String IN_ELABORAZIONE                = "ELABORANDO"
    public static final String DA_ELABORARE                   = "ELABORARE"
    public static final String ELABORATO                      = "ELABORATO"
    public static final String DA_CALCOLARE                   = "DA_CALCOLARE"

    CaratteristicaTipologiaService caratteristicaTipologiaService
    VistoParereService             vistoParereService
    AttiGestoreCompetenze          gestoreCompetenze
    IProtocolloEsterno             protocolloEsterno
    AllegatoService                allegatoService
    WkfIterService                 wkfIterService
    IGestoreFile                   gestoreFile

    PropostaDeliberaEsterna creaPropostaDeliberaEsterna (String codiceApplicativo, Documento proposta) {
        PropostaDeliberaEsterna d = new PropostaDeliberaEsterna()
        d.applicativoEsterno = codiceApplicativo
        d.idDocumentoEsterno = proposta.idRiferimento
        d.oggetto = proposta.oggetto.toUpperCase()
        d.tipologia = proposta.codiceTipologia
        d.utenteInserimento = proposta.redattore?.utenteAd4
        d.utenteDirigente = proposta.dirigente?.utenteAd4 ?: DA_CALCOLARE
        d.unitaEsibente = proposta.unitaProponente?.codice
        d.ente = proposta.unitaProponente?.codiceOttica
        d.dataIns = new Date()
        d.tipoRegistro = proposta.codiceRegistro
        d.annoClassificazione = proposta.classificazione?.getAnnoClassificazione()
        d.numeroClassificazione = proposta.classificazione?.getNumeroClassificazione()
        d.statoAcquisizione = DA_ELABORARE

        if (proposta.classificazione != null) {
            IProtocolloEsterno.Classifica classifica = protocolloEsterno.getClassifica(proposta.classificazione.codice, proposta.classificazione.dataValidita)
            if (classifica != null) {
                d.classificaCodice = classifica.codice
                d.classificaDal = classifica.dal
                d.classificaDescrizione = classifica.descrizione
            } else {
                log.warn ("Creazione Proposta da Webservice: Non ho trovato la classifica con codice ${proposta.classificazione.codice} e data di validità: ${proposta.classificazione.dataValidita}")
            }

            if (classifica == null) {
                classifica = new IProtocolloEsterno.Classifica(codice: proposta.classificazione.codice, dal:proposta.classificazione.dataValidita?:new Date().clearTime())
            }

            if (proposta.fascicolo != null) {
                IProtocolloEsterno.Fascicolo fascicolo = protocolloEsterno.getFascicolo(classifica, proposta.fascicolo.numero, proposta.fascicolo.anno)
                if (fascicolo != null) {
                    d.fascicoloNumero = fascicolo.numero
                    d.fascicoloAnno = fascicolo.anno
                    d.fascicoloOggetto = fascicolo.oggetto
                } else {
                    log.warn("Creazione Proposta da Webservice: Non ho trovato il fascicolo con numero ${proposta.fascicolo.numero} e anno: ${proposta.fascicolo.anno}")
                }
            }
        }

        if (proposta.testo?.file != null) {
            d.fileDocumento = IOUtils.toByteArray(proposta.testo.file.getInputStream())
            d.nomeFile = proposta.testo.nome
            d.formatoFile = TipoFile.getInstanceByEstensione(proposta.testo.nome.substring(proposta.testo.nome.lastIndexOf(".") + 1)).contentType
        }

        d.save()

        for (it.finmatica.atti.integrazioni.ws.dati.Allegato allegato : proposta.allegati) {
            creaAllegatoEsterno(codiceApplicativo, proposta, allegato, null, d)
        }

        return d
    }

    DeterminaEsterna creaDeterminaEsterna (String codiceApplicativo, Documento proposta) {
        DeterminaEsterna d = new DeterminaEsterna()
        d.applicativoEsterno = codiceApplicativo
        d.idDocumentoEsterno = proposta.idRiferimento
        d.oggetto = proposta.oggetto.toUpperCase()
        d.tipologia = proposta.codiceTipologia
        d.utenteInserimento = proposta.redattore?.utenteAd4
        d.utenteDirigente = proposta.dirigente?.utenteAd4 ?: DA_CALCOLARE
        d.unitaEsibente = proposta.unitaProponente?.codice
        d.ente = proposta.unitaProponente?.codiceOttica
        d.dataIns = new Date()
        d.statoAcquisizione = DA_ELABORARE
        d.tipoRegistro = proposta.codiceRegistro
        d.annoClassificazione = proposta.classificazione?.getAnnoClassificazione()
        d.numeroClassificazione = proposta.classificazione?.getNumeroClassificazione()

        if (proposta.classificazione != null) {
            IProtocolloEsterno.Classifica classifica = protocolloEsterno.getClassifica(proposta.classificazione.codice, proposta.classificazione.dataValidita)
            if (classifica != null) {
                d.classificaCodice = classifica.codice
                d.classificaDal = classifica.dal
                d.classificaDescrizione = classifica.descrizione
            } else {
                log.warn ("Creazione Proposta da Webservice: Non ho trovato la classifica con codice ${proposta.classificazione.codice} e data di validità: ${proposta.classificazione.dataValidita}")
            }

            if (classifica == null) {
                classifica = new IProtocolloEsterno.Classifica(codice: proposta.classificazione.codice, dal:proposta.classificazione.dataValidita?:new Date().clearTime())
            }
            if (proposta.fascicolo != null) {
                IProtocolloEsterno.Fascicolo fascicolo = protocolloEsterno.getFascicolo(classifica, proposta.fascicolo.numero, proposta.fascicolo.anno)
                    if (fascicolo != null) {
                    d.fascicoloNumero = fascicolo.numero
                    d.fascicoloAnno = fascicolo.anno
                    d.fascicoloOggetto = fascicolo.oggetto
                } else {
                    log.warn ("Creazione Proposta da Webservice: Non ho trovato il fascicolo con numero ${proposta.fascicolo.numero} e anno: ${proposta.fascicolo.anno}")
                }
            }
        }

        if (proposta.testo?.file != null) {
            d.fileDocumento = IOUtils.toByteArray(proposta.testo.file.getInputStream())
            d.nomeFile = proposta.testo.nome
            d.formatoFile = TipoFile.getInstanceByEstensione(proposta.testo.nome.substring(proposta.testo.nome.lastIndexOf(".") + 1)).contentType
        }

        d.save()

        for (it.finmatica.atti.integrazioni.ws.dati.Allegato allegato : proposta.allegati) {
            creaAllegatoEsterno(codiceApplicativo, proposta, allegato, d, null)
        }

        return d
    }

    AllegatoEsterno creaAllegatoEsterno (String codiceApplicativo, Documento proposta, it.finmatica.atti.integrazioni.ws.dati.Allegato allegato, DeterminaEsterna determinaEsterna, PropostaDeliberaEsterna propostaDeliberaEsterna) {
        for (RiferimentoFile file : allegato.riferimentiFile) {
            AllegatoEsterno allegatoEsterno = new AllegatoEsterno()
            allegatoEsterno.determinaEsterna = determinaEsterna
            allegatoEsterno.propostaDeliberaEsterna = propostaDeliberaEsterna

            allegatoEsterno.applicativoEsterno = codiceApplicativo
            allegatoEsterno.idDocumentoAllegato = allegato.idRiferimento

            allegatoEsterno.idDocumentoEsterno = proposta.idRiferimento
            allegatoEsterno.tipoDocumento = proposta.tipo
            allegatoEsterno.applicativoEsternoDocumento = codiceApplicativo

            allegatoEsterno.tipoAllegato = allegato.tipo
            allegatoEsterno.descrizione = allegato.titolo
            allegatoEsterno.nomeFile = file.nome

            allegatoEsterno.fileDocumento =  IOUtils.toByteArray(file.file.getInputStream())
            allegatoEsterno.save()
        }
    }

    Determina creaDetermina (Documento documento) {
        Determina determina = new Determina()
        determina.oggetto = documento.oggetto
        determina.tipologia = TipoDetermina.findByCodiceEsternoAndValido(documento.codiceTipologia, true)
        if (determina.tipologia == null) {
            throw new AttiRuntimeException("Non ho trovato la tipologia di determina con il codice richiesto: '${documento.codiceTipologia}'.")
        }
        determina.modelloTesto = determina.tipologia.modelloTesto

        if (determina.tipologia.funzionarioObbligatorio) {
            determina.controlloFunzionario = determina.tipologia.funzionarioObbligatorio
        } else {
            determina.controlloFunzionario = Impostazioni.DEFAULT_FUNZIONARIO.abilitato
        }
        determina.giorniPubblicazione = determina.tipologia.giorniPubblicazione
        determina.pubblicaRevoca = determina.tipologia.pubblicazioneFinoARevoca
        determina.diventaEsecutiva = determina.tipologia.diventaEsecutiva

        if (documento.classificazione != null) {
            IProtocolloEsterno.Classifica classifica = protocolloEsterno.getClassifica(documento.classificazione.codice, documento.classificazione.dataValidita)
            if (classifica != null) {
                determina.classificaCodice = classifica.codice
                determina.classificaDal = classifica.dal
                determina.classificaDescrizione = classifica.descrizione
            } else {
                log.warn ("Creazione Proposta da Webservice: Non ho trovato la classifica con codice ${documento.classificazione.codice} e data di validità: ${documento.classificazione.dataValidita}")
            }

            if (classifica == null) {
                classifica = new IProtocolloEsterno.Classifica(codice: documento.classificazione.codice, dal:documento.classificazione.dataValidita?:new Date().clearTime())
            }

            if (documento.fascicolo != null) {
                IProtocolloEsterno.Fascicolo fascicolo = protocolloEsterno.getFascicolo(classifica, documento.fascicolo.numero, documento.fascicolo.anno)
                if (fascicolo != null) {
                    determina.fascicoloNumero = fascicolo.numero
                    determina.fascicoloAnno = fascicolo.anno
                    determina.fascicoloOggetto = fascicolo.oggetto
                } else {
                    log.warn("Creazione Proposta da Webservice: Non ho trovato il fascicolo con numero ${documento.fascicolo.numero} e anno: ${documento.fascicolo.anno}")
                }
            }
        }

        determina.dataProposta = new Date()
        determina.stato = StatoDocumento.PROPOSTA
        determina.save()

        creaSoggetti(documento, determina)

        if (documento.testo?.file != null) {
            FileAllegato fileAllegato = new FileAllegato()
            fileAllegato.nome = documento.testo.nome
            fileAllegato.contentType = TipoFile.getInstanceByEstensione(fileAllegato.estensione).contentType
            fileAllegato.modificabile = !(fileAllegato.isPdf() || fileAllegato.isP7m())
            determina.testo = fileAllegato

            gestoreFile.addFile(determina, fileAllegato, documento.testo.file.inputStream)
        }

        for (it.finmatica.atti.integrazioni.ws.dati.Allegato allegatoEsterno : documento.allegati) {
            Allegato allegato = creaAllegato(determina, allegatoEsterno)
            determina.addToAllegati(allegato)
        }

        determina.save()

        return determina
    }

    PropostaDelibera creaPropostaDelibera (Documento documento) {
        PropostaDelibera propostaDelibera = new PropostaDelibera()
        propostaDelibera.oggetto = documento.oggetto
        propostaDelibera.tipologia = TipoDelibera.findByCodiceEsternoAndValido(documento.codiceTipologia, true)
        if (propostaDelibera.tipologia == null) {
            throw new AttiRuntimeException("Non ho trovato la tipologia di delibera con il codice richiesto: '${documento.codiceTipologia}'.")
        }
        propostaDelibera.modelloTesto = propostaDelibera.tipologia.modelloTesto

        if (propostaDelibera.tipologia.funzionarioObbligatorio) {
            propostaDelibera.controlloFunzionario = propostaDelibera.tipologia.funzionarioObbligatorio
        } else {
            propostaDelibera.controlloFunzionario = Impostazioni.DEFAULT_FUNZIONARIO.abilitato
        }
        propostaDelibera.giorniPubblicazione = propostaDelibera.tipologia.giorniPubblicazione
        propostaDelibera.pubblicaRevoca = propostaDelibera.tipologia.pubblicazioneFinoARevoca

        if (documento.classificazione != null) {
            IProtocolloEsterno.Classifica classifica = protocolloEsterno.getClassifica(documento.classificazione.codice, documento.classificazione.dataValidita)
            if (classifica != null) {
                propostaDelibera.classificaCodice = classifica.codice
                propostaDelibera.classificaDal = classifica.dal
                propostaDelibera.classificaDescrizione = classifica.descrizione
            } else {
                log.warn ("Creazione Proposta da Webservice: Non ho trovato la classifica con codice ${documento.classificazione.codice} e data di validità: ${documento.classificazione.dataValidita}")
            }

            if (classifica == null) {
                classifica = new IProtocolloEsterno.Classifica(codice: documento.classificazione.codice, dal:documento.classificazione.dataValidita?:new Date().clearTime())
            }

            if (documento.fascicolo != null) {
                IProtocolloEsterno.Fascicolo fascicolo = protocolloEsterno.getFascicolo(classifica, documento.fascicolo.numero, documento.fascicolo.anno)
                if (fascicolo != null) {
                    propostaDelibera.fascicoloNumero = fascicolo.numero
                    propostaDelibera.fascicoloAnno = fascicolo.anno
                    propostaDelibera.fascicoloOggetto = fascicolo.oggetto
                } else {
                    log.warn("Creazione Proposta da Webservice: Non ho trovato il fascicolo con numero ${documento.fascicolo.numero} e anno: ${documento.fascicolo.anno}")
                }
            }
        }

        propostaDelibera.dataProposta = new Date()
        propostaDelibera.stato = StatoDocumento.PROPOSTA
        propostaDelibera.save()

        creaSoggetti(documento, propostaDelibera)

        if (documento.testo?.file != null) {
            FileAllegato fileAllegato = new FileAllegato()
            fileAllegato.nome = documento.testo.nome
            fileAllegato.contentType = TipoFile.getInstanceByEstensione(fileAllegato.estensione).contentType
            fileAllegato.modificabile = !(fileAllegato.isPdf() || fileAllegato.isP7m())
            propostaDelibera.testo = fileAllegato

            gestoreFile.addFile(propostaDelibera, fileAllegato, documento.testo.file.inputStream)
        }

        for (it.finmatica.atti.integrazioni.ws.dati.Allegato allegatoEsterno : documento.allegati) {
            Allegato allegato = creaAllegato(propostaDelibera, allegatoEsterno)
            propostaDelibera.addToAllegati(allegato)
        }

        propostaDelibera.save()

        return propostaDelibera
    }

    void creaSoggetti (Documento documentoWebservice, IDocumento nuovoDocumento) {
        Map<String, SoggettoDocumento> soggetti = [:]

        if (documentoWebservice.unitaProponente?.unitaPubb != null) {
            soggetti[TipoSoggetto.UO_PROPONENTE] =
                    new SoggettoDocumento(TipoSoggetto.get(TipoSoggetto.UO_PROPONENTE), documentoWebservice.unitaProponente.unitaPubb)
        }

        if (documentoWebservice.redattore?.utente != null) {
            soggetti[TipoSoggetto.REDATTORE] = new SoggettoDocumento(TipoSoggetto.get(TipoSoggetto.REDATTORE), documentoWebservice.redattore.utente,
                                                                     documentoWebservice.redattore.unita?.unitaPubb)
        }

        if (documentoWebservice.funzionario?.utente != null) {
            soggetti[TipoSoggetto.FUNZIONARIO] =
                    new SoggettoDocumento(TipoSoggetto.get(TipoSoggetto.FUNZIONARIO), documentoWebservice.funzionario.utente,
                                          documentoWebservice.funzionario.unita?.unitaPubb)
        }

        if (documentoWebservice.dirigente?.utente != null) {
            soggetti[TipoSoggetto.DIRIGENTE] = new SoggettoDocumento(TipoSoggetto.get(TipoSoggetto.DIRIGENTE), documentoWebservice.dirigente.utente,
                                                                     documentoWebservice.dirigente.unita?.unitaPubb)
        }

        caratteristicaTipologiaService.calcolaSoggetti(nuovoDocumento, nuovoDocumento.tipologiaDocumento.caratteristicaTipologia, soggetti)
        caratteristicaTipologiaService.salvaSoggettiModificati(nuovoDocumento, soggetti)
    }

    Determina creaDetermina (DeterminaEsterna determinaEsterna) {
        Determina determina = new Determina()
        determina.oggetto = determinaEsterna.oggetto
        determina.tipologia = TipoDetermina.findByCodiceEsternoAndValido(determinaEsterna.tipologia, true)
        if (determina.tipologia == null) {
            throw new AttiRuntimeException("Non ho trovato la tipologia di determina con il codice richiesto: '${determinaEsterna.tipologia}'.")
        }
        determina.modelloTesto = determina.tipologia.modelloTesto

        if (determina.tipologia.funzionarioObbligatorio) {
            determina.controlloFunzionario = determina.tipologia.funzionarioObbligatorio
        } else {
            determina.controlloFunzionario = Impostazioni.DEFAULT_FUNZIONARIO.abilitato
        }
        determina.giorniPubblicazione = determina.tipologia.giorniPubblicazione
        determina.pubblicaRevoca = determina.tipologia.pubblicazioneFinoARevoca
        determina.diventaEsecutiva = determina.tipologia.diventaEsecutiva

        determina.classificaCodice = determinaEsterna.classificaCodice
        determina.classificaDal = determinaEsterna.classificaDal
        determina.classificaDescrizione = determinaEsterna.classificaDescrizione
        determina.fascicoloOggetto = determinaEsterna.fascicoloOggetto
        determina.fascicoloAnno = determinaEsterna.fascicoloAnno
        determina.fascicoloNumero = determinaEsterna.fascicoloNumero

        if (determinaEsterna.tipoRegistro) {
            determina.registroDetermina = TipoRegistro.get(determinaEsterna.tipoRegistro)
        }

        determina.dataProposta = new Date()
        determina.stato = StatoDocumento.PROPOSTA

        So4UnitaPubb unita = (determinaEsterna.unitaEsibente) ? So4UnitaPubb.findByCodice(determinaEsterna.unitaEsibente) : null
        Ad4Utente dirigente = (determinaEsterna.utenteDirigente && !determinaEsterna.utenteDirigente?.equals(
                DA_CALCOLARE)) ? Ad4Utente.findByNominativo(determinaEsterna.utenteDirigente) : null
        Ad4Utente redattore = (determinaEsterna.utenteInserimento) ? Ad4Utente.findByNominativo(determinaEsterna.utenteInserimento) : null
        determina.save()

        Map<String, SoggettoDocumento> soggetti = calcolaSoggetti(determina, unita, dirigente, redattore)
        caratteristicaTipologiaService.salvaSoggettiModificati(determina, soggetti)

        if (determinaEsterna.fileDocumento != null) {
            FileAllegato fileAllegato = new FileAllegato()
            fileAllegato.nome = determinaEsterna.nomeFile
            fileAllegato.contentType = TipoFile.getInstanceByEstensione(
                    determinaEsterna.nomeFile?.substring(determinaEsterna.nomeFile?.lastIndexOf(".") + 1)).contentType
            fileAllegato.dimensione = -1
            fileAllegato.modificabile = !(fileAllegato.isPdf() || fileAllegato.isP7m())
            determina.testo = fileAllegato

            gestoreFile.addFile(determina, fileAllegato, new ByteArrayInputStream(determinaEsterna.fileDocumento))
        }

        wkfIterService.istanziaIter(WkfCfgIter.getIterIstanziabile(determina.tipologia.progressivoCfgIter).get(), determina)

        determina.save()

        determinaEsterna.idDetermina = determina.id
        determinaEsterna.statoAcquisizione = ELABORATO
        determinaEsterna.save()

        // aggiungo i visti automatici presenti nella nuova tipologia
        vistoParereService.creaVistiAutomatici(determina)

        List<AllegatoEsterno> allegati = getAllegatiEsterni(determinaEsterna)
        for (AllegatoEsterno allegatoEsterno : allegati) {
            Allegato allegato = creaAllegato(determina, allegatoEsterno)
            determina.addToAllegati(allegato)
        }
        determina.save()

        return determina
    }

    Allegato creaAllegato (IDocumento documento, it.finmatica.atti.integrazioni.ws.dati.Allegato allegatoEsterno) {
        Allegato allegato = null
        if (allegatoEsterno.tipo == null ){
            allegato = new Allegato()
            //allegato.stampaUnica 	= Impostazioni.ALLEGATO_STAMPA_UNICA_DEFAULT.abilitato
            allegato.numPagine 		= null
            allegato.statoFirma 	= Impostazioni.ALLEGATO_STATO_FIRMA_DEFAULT.valore
            allegato.sequenza       = documento.allegati?.size()?:0 + 1
            allegato.riservato		= Impostazioni.RISERVATO.abilitato && Impostazioni.RISERVATO_DEFAULT.abilitato

            try {
                allegato.pubblicaCasaDiVetro = Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato && documento.tipologiaDocumento.pubblicazione && documento.tipologiaDocumento.pubblicaAllegati
                allegato.pubblicaAlbo = documento.tipologiaDocumento.pubblicazione && documento.tipologiaDocumento.pubblicaAllegati
            } catch (Exception e) {
                log.warn(e)
            }
        }
        else {
        TipoAllegato tipoAllegato = TipoAllegato.findByCodiceEsterno(allegatoEsterno.tipo)
        if (tipoAllegato == null) {
            throw new AttiRuntimeException(
                    "Non è possibile proseguire con la creazione dell'allegato perché il codice esterno '${allegatoEsterno.tipo}' non è riconosciuto.")
        }

            allegato = allegatoService.creaAllegato(documento, tipoAllegato)
        }
        allegato.titolo = allegatoEsterno.titolo
        allegato.quantita = 1

        for (RiferimentoFile riferimentoFile : allegatoEsterno.riferimentiFile) {
            if (riferimentoFile.file == null) {
                continue
            }

            FileAllegato fileAllegato = creaFileAllegato(riferimentoFile)

            if (Impostazioni.ALLEGATO_STAMPA_UNICA_DEFAULT.abilitato && Impostazioni.SU_FORMATI_ESCLUSI.valori.contains(FilenameUtils.getExtension(fileAllegato?.nome).toLowerCase())) {
                allegato.stampaUnica = false
            } else {
                allegato.stampaUnica = Impostazioni.ALLEGATO_STAMPA_UNICA_DEFAULT.abilitato
            }

            allegato.addToFileAllegati(fileAllegato)

            gestoreFile.addFile(allegato, fileAllegato, riferimentoFile.file.inputStream)
        }

        return allegato
    }

    FileAllegato creaFileAllegato (RiferimentoFile riferimentoFile) {
        FileAllegato fileAllegato = new FileAllegato()
        fileAllegato.nome = riferimentoFile.nome
        fileAllegato.contentType = TipoFile.getInstanceByEstensione(fileAllegato.estensione).contentType
        fileAllegato.modificabile = false
        fileAllegato.firmato = false // false perché questo file non è firmato dall'applicativo ma arriva eventualmente già firmato.

        return fileAllegato
    }

    Allegato creaAllegato (IDocumento documento, AllegatoEsterno allegatoEsterno) {
        TipoAllegato tipoAllegato = TipoAllegato.findByCodiceEsterno(allegatoEsterno.tipoAllegato)
        if (tipoAllegato == null) {
            throw new AttiRuntimeException(
                    "Non è possibile proseguire con la creazione dell'allegato perché il codice esterno '${allegatoEsterno.tipoAllegato}' non è riconosciuto.")
        }

        Allegato allegato = allegatoService.creaAllegato(documento, tipoAllegato)
        allegato.titolo = allegatoEsterno.descrizione
        allegato.quantita = 1

        FileAllegato fileAllegato = new FileAllegato()
        fileAllegato.nome = allegatoEsterno.nomeFile
        fileAllegato.contentType = TipoFile.getInstanceByEstensione(fileAllegato.estensione).contentType

        allegato.addToFileAllegati(fileAllegato)
        if (allegatoEsterno.fileDocumento != null) {
            gestoreFile.addFile(allegato, fileAllegato, new ByteArrayInputStream(allegatoEsterno.fileDocumento))
        }

        return allegato
    }

    List<AllegatoEsterno> getAllegatiEsterni (PropostaDeliberaEsterna propostaDeliberaEsterna) {
        if (propostaDeliberaEsterna.applicativoEsterno == APPLICATIVO_ESTERNO_WEBSERVICE) {
            return AllegatoEsterno.findAllByPropostaDeliberaEsterna(propostaDeliberaEsterna)
        }

        return AllegatoEsterno.findAllByApplicativoEsternoAndApplicativoEsternoDocumento(propostaDeliberaEsterna.applicativoEsterno,
                                                                                         propostaDeliberaEsterna.idDocumentoEsterno)
    }

    List<AllegatoEsterno> getAllegatiEsterni (DeterminaEsterna determinaEsterna) {
        if (determinaEsterna.applicativoEsterno == APPLICATIVO_ESTERNO_WEBSERVICE) {
            return AllegatoEsterno.findAllByDeterminaEsterna(determinaEsterna)
        }

        return AllegatoEsterno.findAllByApplicativoEsternoAndApplicativoEsternoDocumento(determinaEsterna.applicativoEsterno,
                                                                                         determinaEsterna.idDocumentoEsterno)
    }

    PropostaDelibera creaPropostaDelibera (PropostaDeliberaEsterna propostaDeliberaEsterna) {
        PropostaDelibera propostaDelibera = new PropostaDelibera()
        propostaDelibera.oggetto = propostaDeliberaEsterna.oggetto
        propostaDelibera.tipologia = TipoDelibera.findByCodiceEsternoAndValido(propostaDeliberaEsterna.tipologia, true)

        if (propostaDelibera.tipologia == null) {
            throw new AttiRuntimeException("Non ho trovato la tipologia di delibera con il codice richiesto: '${propostaDeliberaEsterna.tipologia}'.")
        }

        propostaDelibera.modelloTesto = propostaDelibera.tipologia.modelloTesto

        if (propostaDelibera.tipologia.funzionarioObbligatorio) {
            propostaDelibera.controlloFunzionario = propostaDelibera.tipologia.funzionarioObbligatorio
        } else {
            propostaDelibera.controlloFunzionario = Impostazioni.DEFAULT_FUNZIONARIO.abilitato
        }
        propostaDelibera.giorniPubblicazione = propostaDelibera.tipologia.giorniPubblicazione
        propostaDelibera.pubblicaRevoca = propostaDelibera.tipologia.pubblicazioneFinoARevoca

        propostaDelibera.dataProposta = new Date()
        propostaDelibera.stato = StatoDocumento.PROPOSTA

        if (propostaDeliberaEsterna.tipoRegistro) {
            propostaDelibera.registroProposta = TipoRegistro.get(propostaDeliberaEsterna.tipoRegistro)
        }

        So4UnitaPubb unita = (propostaDeliberaEsterna.unitaEsibente) ? So4UnitaPubb.findByCodice(propostaDeliberaEsterna.unitaEsibente) : null
        Ad4Utente dirigente = (propostaDeliberaEsterna.utenteDirigente && !propostaDeliberaEsterna.utenteDirigente?.equals(
                DA_CALCOLARE)) ? Ad4Utente.findByNominativo(
                propostaDeliberaEsterna.utenteDirigente) : null
        Ad4Utente redattore = (propostaDeliberaEsterna.utenteInserimento) ? Ad4Utente.findByNominativo(
                propostaDeliberaEsterna.utenteInserimento) : null
        Map<String, SoggettoDocumento> soggetti = calcolaSoggetti(propostaDelibera, unita, dirigente, redattore)
        propostaDelibera.setSoggetto(TipoSoggetto.REDATTORE, soggetti[TipoSoggetto.REDATTORE]?.utente?.domainObject,
                                     soggetti[TipoSoggetto.REDATTORE]?.unita?.domainObject)
        propostaDelibera.setSoggetto(TipoSoggetto.DIRIGENTE, soggetti[TipoSoggetto.DIRIGENTE]?.utente?.domainObject,
                                     soggetti[TipoSoggetto.DIRIGENTE]?.unita?.domainObject)
        propostaDelibera.setSoggetto(TipoSoggetto.UO_PROPONENTE, null, soggetti[TipoSoggetto.UO_PROPONENTE]?.unita?.domainObject)

        propostaDelibera.save()

        gestoreCompetenze.assegnaCompetenze(propostaDelibera, WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO),
                                            new Attore(utenteAd4: soggetti[TipoSoggetto.REDATTORE]?.utente?.domainObject), true, true, false, null)
        gestoreCompetenze.assegnaCompetenze(propostaDelibera, WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO),
                                            new Attore(unitaSo4: soggetti[TipoSoggetto.UO_PROPONENTE]?.unita?.domainObject), true, true, false, null);

        if (propostaDeliberaEsterna.fileDocumento != null) {
            FileAllegato fileAllegato = new FileAllegato()
            fileAllegato.nome = propostaDeliberaEsterna.nomeFile
            fileAllegato.contentType = TipoFile.getInstanceByEstensione(
                    propostaDeliberaEsterna.nomeFile?.substring(propostaDeliberaEsterna.nomeFile?.lastIndexOf(".") + 1)).contentType
            fileAllegato.dimensione = -1
            fileAllegato.modificabile = !(fileAllegato.isPdf() || fileAllegato.isP7m())
            propostaDelibera.testo = fileAllegato

            gestoreFile.addFile(propostaDelibera, fileAllegato, new ByteArrayInputStream(propostaDeliberaEsterna.fileDocumento))
        }

        wkfIterService.istanziaIter(WkfCfgIter.getIterIstanziabile(propostaDelibera.tipologia.progressivoCfgIter).get(), propostaDelibera)

        propostaDelibera.save()

        propostaDeliberaEsterna.idPropostaDelibera = propostaDelibera.id
        propostaDeliberaEsterna.statoAcquisizione = ELABORATO
        propostaDeliberaEsterna.utenteDirigente = soggetti[TipoSoggetto.DIRIGENTE]?.utente?.nominativo
        propostaDeliberaEsterna.save()

        // aggiungo i visti automatici presenti nella nuova tipologia
        vistoParereService.creaVistiAutomatici(propostaDelibera)

        List<AllegatoEsterno> allegati = getAllegatiEsterni(propostaDeliberaEsterna)
        int sequenza = 1
        for (AllegatoEsterno allegatoEsterno : allegati) {
            Allegato allegato = creaAllegato(propostaDelibera, allegatoEsterno)
            propostaDelibera.addToAllegati(allegato)
        }
        propostaDelibera.save()

        return propostaDelibera
    }

    private Map<String, SoggettoDocumento> calcolaSoggetti (IDocumento documento, So4UnitaPubb unita, Ad4Utente dirigente, Ad4Utente redattore = null) {
        Map<String, SoggettoDocumento> soggetti = [:]
        if (unita != null) {
            soggetti[TipoSoggetto.UO_PROPONENTE] = new SoggettoDocumento(TipoSoggetto.get(TipoSoggetto.UO_PROPONENTE), unita)
        }

        if (dirigente != null) {
            soggetti[TipoSoggetto.DIRIGENTE] = new SoggettoDocumento(TipoSoggetto.get(TipoSoggetto.DIRIGENTE), dirigente, unita)
        }

        if (redattore != null) {
            soggetti[TipoSoggetto.REDATTORE] = new SoggettoDocumento(TipoSoggetto.get(TipoSoggetto.REDATTORE), redattore, unita)
        }

        if (unita == null || dirigente == null) {
            caratteristicaTipologiaService.calcolaSoggetti(documento, documento.tipologiaDocumento.caratteristicaTipologia, soggetti)
        }
        return soggetti
    }
}
