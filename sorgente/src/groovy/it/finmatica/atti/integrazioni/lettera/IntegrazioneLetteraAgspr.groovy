package it.finmatica.atti.integrazioni.lettera

import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.Sql
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.GestoreFileDataSource
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.DestinatarioNotifica
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.beans.GdmDocumentaleEsterno
import it.finmatica.atti.dto.documenti.DeterminaDTO
import it.finmatica.atti.dto.documenti.DocumentoCollegatoDTO
import it.finmatica.atti.dto.documenti.DocumentoCollegatoDTOService
import it.finmatica.atti.dto.documenti.RiferimentoEsternoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioniws.ads.agspr.*
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.dmServer.Riferimento
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.activation.DataHandler
import javax.sql.DataSource
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import java.text.SimpleDateFormat

/**
 * Created by esasdelli on 07/11/2017.
 */
@Component
class IntegrazioneLetteraAgspr {

    @Autowired
    private IntegrazioneLetteraAgsprConfig integrazioneLetteraAgsprConfig

    @Autowired DocumentoCollegatoDTOService documentoCollegatoDTOService

    @Autowired
    @Qualifier("dataSource_gdm")
    private DataSource dataSource_gdm

    @Autowired
    private SpringSecurityService springSecurityService

    @Autowired
    private Protocollo letteraAgsprClient

    @Autowired
    private IGestoreFile gestoreFile

    @Transactional(readOnly = true)
    String getUrlLettera (long idDocumentoLettera) {
        return integrazioneLetteraAgsprConfig.getUrlLettera()+"?operazione=APRI_DOCUMENTO&tipoDocumento=LETTERA&idDoc=${idDocumentoLettera}"
    }

    @Transactional
    void aggiornaDatiProtocolloSedutaStampa () {
        List<SedutaStampa> stampe = SedutaStampa.findAllByNumeroProtocolloIsNullAndValido(true)
        for (SedutaStampa stampa : stampe) {
            aggiornaDatiProtocollo(stampa)
        }
    }

    @Transactional
    void aggiornaDatiProtocollo (SedutaStampa sedutaStampa) {
        Sql sql = new Sql(dataSource_gdm)
        def rows = sql.rows("select ANNO, NUMERO, TIPO_REGISTRO, DATA from proto_view where id_documento = :id_documento and numero > 0", [id_documento: sedutaStampa.idDocumentoLettera])
        if (rows.size() > 0) {
            sedutaStampa.annoProtocollo = rows[0].ANNO
            sedutaStampa.numeroProtocollo = rows[0].NUMERO
            sedutaStampa.dataNumeroProtocollo = rows[0].DATA
            sedutaStampa.registroProtocollo = TipoRegistro.get(rows[0].TIPO_REGISTRO)
            sedutaStampa.save()
        }
    }

    @Transactional
    void creaLettera (SedutaStampa sedutaStampa) {
        long idEnte = integrazioneLetteraAgsprConfig.getCodiceEnte(springSecurityService.principal.amm().codice)
        String tipoProtocollo = integrazioneLetteraAgsprConfig.getCodiceTipologiaProtocollo(sedutaStampa)
        Soggetto soggetto = new Soggetto()
        soggetto.setUtenteAd4(springSecurityService.currentUser.nominativo)
        Protocollo_Type protocollo = new Protocollo_Type()
        protocollo.setOggetto(sedutaStampa.oggetto)
        protocollo.setAnnoFascicolo(sedutaStampa.fascicoloAnno)
        protocollo.setNumeroFascicolo(sedutaStampa.fascicoloNumero)
        protocollo.setClassificazione(sedutaStampa.classificaCodice)
        protocollo.setMovimento(Movimento.PARTENZA)
        protocollo.setRiservato(sedutaStampa.riservato)
        protocollo.setDataRedazione(getDate(sedutaStampa.dateCreated))
        protocollo.setUnitaProtocollante(getUnitaOrganizzativa(sedutaStampa.getUnitaProponente()))
        protocollo.setTipo(tipoProtocollo)
        protocollo.getAllegati().addAll(getAllegati(sedutaStampa))
        protocollo.getCorrispondenti().addAll(getCorrispondenti(sedutaStampa))

        CreaLetteraResponse response = letteraAgsprClient.creaLettera(soggetto, idEnte, protocollo)
        if (response.esito != "OK") {
            throw new AttiRuntimeException("Errore nella invocazione del webservice di creazione lettera: ${response.messaggioErrore}")
        }

        sedutaStampa.idDocumentoLettera = Long.parseLong(response.idDocumentoEsterno)
        sedutaStampa.save()
    }

    public Long creaLettera (IAtto atto) {
        long idEnte = integrazioneLetteraAgsprConfig.getCodiceEnte(springSecurityService.principal.amm().codice)
        String tipoProtocollo = (atto instanceof Delibera) ? integrazioneLetteraAgsprConfig.getCodiceDelibere() : integrazioneLetteraAgsprConfig.getCodiceDetermine()
        Soggetto soggetto = new Soggetto()
        soggetto.setUtenteAd4(springSecurityService.currentUser.nominativo)
        Protocollo_Type protocollo = new Protocollo_Type()
        protocollo.setOggetto(atto.oggetto)
        if (atto.fascicoloAnno > 0 ) {  protocollo.setAnnoFascicolo(atto.fascicoloAnno) }
        if (atto.fascicoloNumero) {     protocollo.setNumeroFascicolo(atto.fascicoloNumero)}
        if (atto.classificaCodice) {    protocollo.setClassificazione(atto.classificaCodice)}
        protocollo.setMovimento(Movimento.PARTENZA)
        protocollo.setRiservato(atto.riservato)
        protocollo.setDataRedazione(getDate(atto.dateCreated))
        protocollo.setUnitaProtocollante(getUnitaOrganizzativa(atto.getUnitaProponente()))
        protocollo.setTipo(tipoProtocollo)
        List<Allegato> allegati = protocollo.getAllegati()
        aggiungiAllegati(allegati, atto)

        CreaLetteraResponse response = letteraAgsprClient.creaLettera(soggetto, idEnte, protocollo)
        if (response.esito != "OK") {
            throw new AttiRuntimeException("Errore nella invocazione del webservice di creazione lettera: ${response.messaggioErrore}")
        }
        it.finmatica.atti.documenti.DocumentoCollegato documentoCollegato = new it.finmatica.atti.documenti.DocumentoCollegato()
        documentoCollegato.operazione = it.finmatica.atti.documenti.DocumentoCollegato.OPERAZIONE_COLLEGA
        documentoCollegato.riferimentoEsternoCollegato = documentoCollegatoDTOService.getRiferimentoEsterno(
                                                                            new RiferimentoEsternoDTO(tipoDocumento: "LETTERA",
                                                                                    idDocumentoEsterno: Long.parseLong(response.idDocumentoEsterno),
                                                                                    titolo:  "Lettera del ${new SimpleDateFormat("dd/MM/yyyy").format(new Date())}, ID ${response.idDocumentoEsterno}",
                                                                                    codiceDocumentaleEsterno: GdmDocumentaleEsterno.CODICE_DOCUMENTALE_ESTERNO))

        documentoCollegatoDTOService.aggiungiDocumentiCollegati (atto.toDTO(), [documentoCollegato.toDTO()])
        return Long.parseLong(response.idDocumentoEsterno);
    }

    private List<Allegato> getAllegati (SedutaStampa sedutaStampa) {
        return [getAllegato(sedutaStampa, sedutaStampa.getTesto())]
    }

    private List<Allegato> aggiungiAllegati (List<Allegato> allegati, IAtto atto) {
        if ( integrazioneLetteraAgsprConfig.getTestoOStampaUnica().equals("STAMPA_UNICA")){
            if (atto.stampaUnica != null) {
                allegati.add(getAllegato(atto, atto.stampaUnica))
            }
        }
        else if (atto.testo != null) {
            allegati.add(getAllegato(atto, atto.getTesto()))
        }

        for (it.finmatica.atti.documenti.Allegato allegato : atto.allegati) {
            if (allegato.valido && !allegato.riservato && (allegato.pubblicaAlbo || integrazioneLetteraAgsprConfig.isAllegatiNonPubblicatiAbilitati())) {
                for (FileAllegato fileAllegato : allegato.fileAllegati) {
                    if (fileAllegato != null) {
                        allegati.add(getAllegato(allegato, fileAllegato))
                    }
                }
            }
        }

        if ( integrazioneLetteraAgsprConfig.isVistiAbilitati()) {
            def vistiPareriAtto = atto.visti.findAll { it.valido == true }
            def vistiPareriProposta = (atto instanceof Delibera) ? atto.proposta.visti.findAll {
                it.valido == true
            } : []
            def vistiPareri = (vistiPareriProposta ?: []) + (vistiPareriAtto ?: [])

            for (def vistoParere : vistiPareriProposta) {
                if (vistiPareriAtto.findAll { it.tipologia.codice == vistoParere.tipologia.codice }.size() > 0) {
                    vistiPareri.remove(vistoParere)
                }
            }

            for (VistoParere visto : vistiPareri) {
                if (!visto.valido) {
                    continue
                }

                if (visto.testo != null) {
                    allegati.add(getAllegato(visto, visto.testo))
                }

                if (integrazioneLetteraAgsprConfig.isAllegatiVistiAbilitati()) {
                    for (it.finmatica.atti.documenti.Allegato allegato : visto.allegati) {
                        if (allegato.valido && !allegato.riservato  && (allegato.pubblicaAlbo || integrazioneLetteraAgsprConfig.isAllegatiNonPubblicatiAbilitati())) {
                            for (FileAllegato fileAllegato : allegato.fileAllegati) {
                                if (fileAllegato != null) {
                                    if (fileAllegato != null) {
                                        allegati.add(getAllegato(allegato, fileAllegato))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if ( integrazioneLetteraAgsprConfig.isCertificatiAbilitati()) {
            for (Certificato certificato : atto.certificati) {
                if (!certificato.valido) {
                    continue
                }

                if (certificato.testo != null) {
                    allegati.add(getAllegato(certificato, certificato.testo))
                }
            }
        }
    }

    private Allegato getAllegato (IDocumentoEsterno documentoEsterno, FileAllegato fileDocumento) {
        Allegato allegato = new Allegato()
        allegato.setIdRiferimento(fileDocumento.id.toString())
        allegato.setContentType(fileDocumento.contentType)
        allegato.setNomeFile(fileDocumento.nome)
        allegato.setFile(getDataHandler(documentoEsterno, fileDocumento))
        return allegato
    }

    private DataHandler getDataHandler (IDocumentoEsterno documentoEsterno, FileAllegato fileDocumento) {
        return new DataHandler(new GestoreFileDataSource(documentoEsterno, fileDocumento))
    }

    private UnitaOrganizzativa getUnitaOrganizzativa (So4UnitaPubb unita) {
        UnitaOrganizzativa uo = new UnitaOrganizzativa()
        uo.setCodice(unita.codice)
        uo.setCodiceOttica(unita.ottica.codice)
        uo.setDal(getDate(unita.dal))
        uo.setProgressivo(unita.progr)
        uo.setDescrizione(unita.descrizione)
        return uo
    }

    private XMLGregorianCalendar getDate (Date date) {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(time: date))
    }

    private List<Corrispondente> getCorrispondenti (SedutaStampa sedutaStampa) {
        List<Corrispondente> corrispondenti = []
        for (DestinatarioNotifica destinatarioNotifica : sedutaStampa.destinatariNotifiche) {
            Corrispondente corrispondente = getCorrispondente(destinatarioNotifica)
            if (corrispondente != null) {
                corrispondenti << corrispondente
            }
        }
        return corrispondenti
    }

    private Corrispondente getCorrispondente (DestinatarioNotifica destinatarioNotifica) {
        As4SoggettoCorrente soggettoCorrente = destinatarioNotifica.soggettoCorrente
        if (soggettoCorrente != null) {
            Corrispondente corrispondente = new Corrispondente()
            corrispondente.nome = soggettoCorrente.nome
            corrispondente.cognome = soggettoCorrente.cognome
            corrispondente.email = soggettoCorrente.indirizzoWeb
            corrispondente.codiceFiscale = soggettoCorrente.codiceFiscale
            return corrispondente
        } else if (destinatarioNotifica.email != null) {
            Corrispondente corrispondente = new Corrispondente()
            corrispondente.email = destinatarioNotifica.email.indirizzoEmail
            corrispondente.nome = destinatarioNotifica.email.nome
            corrispondente.cognome = destinatarioNotifica.email.cognome
            return corrispondente
        }
    }

    public boolean isLetteraPresente(Long idDocumentoLettera) {
        Sql sql = new Sql(dataSource_gdm)
        def rows = sql.rows("select 1 from proto_view p, documenti d where p.id_documento = d.id_documento and d.id_documento = :id_documento and d.stato_documento = 'BO' and stato_pr <> 'AN'", [id_documento: idDocumentoLettera])
        if (rows.size() > 0) {
            return true;
        }
        return false;
    }

}
