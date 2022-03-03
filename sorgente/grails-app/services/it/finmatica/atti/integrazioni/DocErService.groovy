package it.finmatica.atti.integrazioni

import atti.actions.determina.DeterminaCondizioniAction
import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.DeterminaService
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.docer.DocErConfig
import it.finmatica.docer.atti.anagrafiche.GestioneAnagrafiche
import it.finmatica.docer.atti.anagrafiche.GestioneRegistrazioneParticolare
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.zkoss.zk.ui.util.Clients

import javax.sql.DataSource
import java.sql.Connection
import java.text.SimpleDateFormat

class DocErService {

    // service
    WkfIterService wkfIterService
    DeterminaCondizioniAction determinaCondizioniAction
    DeterminaService determinaService
    IDocumentaleEsterno gestoreDocumentaleEsterno
    AttiGestoreCompetenze gestoreCompetenze
    IProtocolloEsterno protocolloEsterno
    IGestoreFile gestoreFile
    SpringSecurityService springSecurityService
    DataSource dataSource_gdm

    DocErConfig docErConfig

    public void controllaStatoSincronizzazione(IAtto documento) {
        String msg, url = "", remoteDocument, idDocumento
        List<String> messages = new ArrayList<String>()

        try {
            if (documento.idDocumentoEsterno == null)
                throw new AttiRuntimeException("Non esiste nessun documento nel sistema documentale")
            else
                idDocumento = documento?.idDocumentoEsterno

            url = docErConfig.getUrlStatoSincronizzaizone()
            if (url.equals(""))
                throw new AttiRuntimeException("URL di verifica allo stato di sincronizzazione non valido.")
            else
                url += documento?.idDocumentoEsterno

            def xml = new XmlSlurper().parse(url)

            remoteDocument = xml.remoteDocument.text()

            if (remoteDocument != null && remoteDocument.equals("-1")) {
                messages.add('Il documento non è stato ancora sincronizato con il repository Doc/Er')
            } else {

                String s = xml.lastActionReport.text().toString()

                messages.add("- Numero documento Doc/Er: " + remoteDocument)
                messages.add("- Stato dell'ultima sincronizzazione: ")

                while (s.size() > 0) {

                    if (s.indexOf("\\n") != -1) {
                        messages.add(s.substring(0, s.indexOf("\\n")))
                        s = s.substring(s.indexOf("\\n") + 2, s.size())
                    } else {
                        messages.add(s)
                        break
                    }
                }
            }
        } catch (Exception e) {
            log.error("Errore in fase di sincronizzazione su Doc/Er: ${e.getMessage()}", e)
            throw new AttiRuntimeException("Errore in fase di sincronizzazione su Doc/Er: ${e.getMessage()}.", e)
        }

        if (messages.size() > 0) {
            Clients.showNotification(messages.join("\n"), Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 8000, true)
            messages.clear()
        }
    }

    public String registrazioneParticolare(String xml) {
        log.debug("Doc-Er - Registrazione Particolare ( xml:${xml} )")

        def xmlInput = new XmlSlurper().parseText(xml)
        String xmlOutput, idDocumentoRemoter

        xmlInput = new XmlSlurper().parseText(xml)

        idDocumentoRemoter = xmlInput.Documenti.Documento.@id

        Connection conn = dataSource_gdm.connection
        GestioneRegistrazioneParticolare g = new GestioneRegistrazioneParticolare(conn,
                docErConfig.getUtenteWebservice(), docErConfig.getUrlWsdlAutenticazione(), docErConfig.getUrlWsdlRegistrazione())

        //Controllo sull'esistenza della determina
        def xmlResult = new XmlSlurper().parseText(g.getIDDeterminaRemoteSyste(idDocumentoRemoter))
        String idDocumentoEsterno = xmlResult.ROW.ID_DOCUMENTO_ESTERNO.text()

        Determina determina = Determina.findByIdDocumentoEsterno(idDocumentoEsterno)

        if (determina != null) {
            // Se la proposta determina non è numerata
            if (!determinaCondizioniAction.isPropostaDeterminaNumerata(determina)) {
                determinaService.numeraProposta(determina)
                log.debug("Proposta Determina numerata: ${determina.numeroProposta} / ${determina.annoProposta}")
            }

            // Se la determina non è numerata
            if (!determinaCondizioniAction.isDeterminaNumerata(determina)) {
                determinaService.numeraDetermina(determina)
                log.debug("Determina numerata: ${determina.numeroDetermina} / ${determina.annoDetermina}")

                //Nel caso in cui la determina è stata appena numerata devo riportare le modifiche su GDM con id_documento_docer valorizzato
                determina.idDocumentoDocer = Long.parseLong(idDocumentoRemoter)
                gestoreDocumentaleEsterno.salvaDocumento(determina)
            }

            xmlOutput = getXMLOuput("0",
                    "",
                    determina?.dataNumeroDetermina?.format("yyyy-MM-dd"),
                    determina?.numeroDetermina?.toString(),
                    determina?.oggetto,
                    determina?.registroDetermina?.codice)
        } else {

            GestioneAnagrafiche ga = new GestioneAnagrafiche(conn, docErConfig.getUtenteWebservice(), docErConfig.getUrlWsdlAutenticazione(), docErConfig.getUrlWsdl(), docErConfig.getUrlWsdlFascicolazione(), docErConfig.getUrlWsdlProtocollazione())

            xmlOutput = creaDetermina(xml, idDocumentoRemoter, ga)
        }

        log.debug("Result - Registrazione Particolare ${xmlOutput}")
        return xmlOutput
    }

    private String creaDetermina(String xml, String idDocumentoRemoter, GestioneAnagrafiche g) {
        def xmlInput, xmlOutput

        try {

            xmlInput = new XmlSlurper().parseText(xml)
            Determina determina = new Determina()
            determina.tipologia = TipoDetermina.findByTitoloLike("DOCER")
            determina.statoFirma = getStatoFirma(xmlInput.Intestazione.Flusso.Firma.text())

            String codiceFiscale = xmlInput.Intestazione.Flusso.Firmatario.Persona.@id
            As4SoggettoCorrente soggetto = As4SoggettoCorrente.findByCodiceFiscale(codiceFiscale)

            if (soggetto == null) {
                log.error("Errore in fase di recupero del firmatario: soggetto con codice fiscale non valido o nullo codice fiscale: ${codiceFiscale}", null)
                return getXMLOuput("-1", "Impossibile trovare in anagrafica un Soggetto con codice fiscale: ${codiceFiscale}!", "", "", "", "")
            }

            determina.setSoggetto(TipoSoggetto.DIRIGENTE, soggetto.utenteAd4, null)
            determina.setSoggetto(TipoSoggetto.REDATTORE, soggetto.utenteAd4, null)

            // dati proposta
            determina.oggetto = xmlInput.Intestazione.Oggetto.text()
            determina.dataProposta = new Date()
            determina.idDocumentoDocer = Long.parseLong(idDocumentoRemoter)

            String nomeDocumento, unitaProponente
            String fascicoloAnno, fascicoloNumero, fascicoloOggetto
            String classificaCodice, classificaDescrizione
            String dataPubblicazione, dataEsecutivita, statoBusiness = "0"

            def MetadatiChiave = xmlInput.Documenti.Documento.Metadati.Parametro.@nome.collect()
            def MetadatiValore = xmlInput.Documenti.Documento.Metadati.Parametro.@valore.collect()

            for (int i = 0; i < MetadatiChiave.size(); i++) {

                switch (MetadatiChiave.getAt(i)) {
                    case "DOCNAME":
                        nomeDocumento = MetadatiValore.getAt(i)
                        break

                    case "ANNO_FASCICOLO":
                        fascicoloAnno = MetadatiValore.getAt(i)
                        break

                    case "DES_FASCICOLO":
                        fascicoloOggetto = MetadatiValore.getAt(i)
                        break

                    case "PROGR_FASCICOLO":
                        fascicoloNumero = MetadatiValore.getAt(i)
                        break

                    case "DATA_INIZIO_PUB":
                        dataPubblicazione = MetadatiValore.getAt(i)
                        break

                    case "DATA_ESECUTIVITA":
                        dataEsecutivita = MetadatiValore.getAt(i)
                        break

                    case "UNITA_PROPONENTE":
                        unitaProponente = MetadatiValore.getAt(i)
                        break

                    case "CLASSIFICA":
                        classificaCodice = MetadatiValore.getAt(i)
                        break

                    case "DES_TITOLARIO":
                        classificaDescrizione = MetadatiValore.getAt(i)
                        break

                    case "STATO_BUSINESS":
                        statoBusiness = MetadatiValore.getAt(i)
                        break
                }
            }

            determina.fascicoloAnno = (fascicoloAnno) ? Integer.parseInt(fascicoloAnno) : null
            determina.fascicoloNumero = fascicoloNumero
            determina.fascicoloOggetto = fascicoloOggetto
            determina.classificaCodice = classificaCodice
            determina.classificaDescrizione = classificaDescrizione
            determina.dataPubblicazione = (dataPubblicazione) ? getData(dataPubblicazione) : null

            // Unita Proponente
            So4UnitaPubb unita = So4UnitaPubb.allaData(new Date()).perOttica(springSecurityService.principal.ottica().codice).findByDescrizione(unitaProponente)
            if (unita) {
                determina.setSoggetto(TipoSoggetto.UO_PROPONENTE, null, unita)
            } else {
                log.error("Errore in fase di recupero dell'unita proponente ${unitaProponente}", null)
                return getXMLOuput("-1", "Errore in fase di recupero dell'unita proponente ${unitaProponente} nulla o non valida!", "", "", "", "")
            }

            //Salvataggio della determina
            determina.save()

            //Testo
            InputStream is = protocolloEsterno.downloadFile(idDocumentoRemoter)
            if (is == null) {
                log.error("Errore in fase di recupero del file allegato! ", null)
                return getXMLOuput("-1", "Errore in fase di recupero del file allegato!", "", "", "", "")
            }

            FileAllegato fileAllegato = new FileAllegato()
            fileAllegato.nome = nomeDocumento
            fileAllegato.contentType = getMimeTypeFile(nomeDocumento)
            fileAllegato.dimensione = -1
            fileAllegato.modificabile = false
            determina.testo = fileAllegato
            gestoreFile.addFile(determina, fileAllegato, is)

            //Salvataggio della determina
            determina.save()

            //Assegnazione delle competenze
            def AclChiave = xmlInput.Documenti.Documento.Acl.Parametro.@attore.collect()
            def AclValore = xmlInput.Documenti.Documento.Acl.Parametro.@valore.collect()

            for (int i = 0; i < AclChiave.size(); i++) {

                String codiceUtente = AclChiave.getAt(i)
                String competenza = AclValore.getAt(i)
                String uteDocer = g.retrieveUtenteMapping(codiceUtente)

                log.debug("Recupero dell'utente docer mappato uteDocer:${uteDocer}")

                Ad4Utente utente = Ad4Utente.findByNominativo(uteDocer)
                if (utente == null) {
                    log.error("Errore in fase di assegnazione delle competenze: Il nome utente ${codiceUtente} non è valido! ", null)
                    return getXMLOuput("-1", "Errore in fase di assegnazione delle competenze: Nome utente " + codiceUtente + " non valido!", "", "", "", "")
                }

                Attore a = new Attore()
                a.utenteAd4 = utente

                boolean lettura = false, modifica = false, cancellazione = false

                switch (competenza) {
                    case "0":
                        lettura = modifica = cancellazione = true
                        break
                    case "1":
                        lettura = modifica = true
                        break
                    case "2":
                        lettura = true
                        break
                }

                gestoreCompetenze.assegnaCompetenze(determina, WkfTipoOggetto.get(determina.TIPO_OGGETTO), a, lettura, modifica, cancellazione, null)
            }

            //Numera la proposta
            determinaService.numeraProposta(determina)
            log.debug("Proposta numerata: ${determina.numeroProposta} / ${determina.annoProposta}")

            if (statoBusiness != null && statoBusiness.equals("3")) {

                log.debug("La Determina ${determina.id} è stata inserita sul verticale di registro per procedere alla sua registrazione", null)
                xmlOutput = getXMLOuput("1", "La Determina è stata inserita sul verticale di registro per procedere alla sua registrazione.", "", "", "", "")

            } else {

                //Numerare la determina
                determinaService.numeraDetermina(determina)
                log.debug("Determina numerata: ${determina.numeroDetermina} / ${determina.annoDetermina}")

                // Rende esecutiva la determina
                determinaService.rendiEsecutiva(determina, (dataEsecutivita) ? getData(dataEsecutivita) : new Date())
                log.debug("Determina esecutiva: ${determina.dataEsecutivita.format('dd/MM/yyyy')}")

                xmlOutput = getXMLOuput("0", "",
                        determina?.dataNumeroDetermina?.format("yyyy-MM-dd"),
                        determina?.numeroDetermina?.toString(),
                        determina?.oggetto,
                        determina?.registroDetermina?.codice)
            }

            // salva su GDM con id_documento_docer valorizzato
            gestoreDocumentaleEsterno.salvaDocumento(determina)

            // Instanzia Iter
            WkfCfgIter iterDocer = WkfCfgIter.getIterIstanziabile(determina.tipologiaDocumento.progressivoCfgIter).get()
            wkfIterService.istanziaIter(iterDocer, determina)
            log.debug("Instanziato iter associato alla determina ${determina.id}")

        } catch (Exception e) {
            log.error("Errore nella chiamata alla creazione di una determina: ${e.getMessage()}", e)
            xmlOutput = getXMLOuput("-1", "Errore nella chiamata alla creazione di una determina: ${e.getMessage()}", "", "", "", "")
        }

        return xmlOutput
    }

    private Date getData(String s) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(s)
    }

    private String getMimeTypeFile(String fileName) throws java.io.IOException {
        String type

        try {
            FileNameMap fileNameMap = URLConnection.getFileNameMap()
            type = fileNameMap.getContentTypeFor(fileName)

            if (fileName.toUpperCase().endsWith(".P7M"))
                type = "application/pkcs7-mime";
        }
        catch (Exception e) {
            type = ""
        }

        return type;
    }

    private String getStatoFirma(String stato) {
        String statoFirma
        switch (stato) {
            case null:
            case "":
            case "NF":
                statoFirma = StatoFirma.DA_NON_FIRMARE
                break

            case "FD":
            case "FE":
                statoFirma = StatoFirma.FIRMATO
                break

            case "F":
                statoFirma = StatoFirma.DA_FIRMARE
                break
        }
        return statoFirma
    }

    private String getXMLOuput(String codice, String descrizione, String data, String numero, String oggetto, String registro) {
        Document xml = DocumentHelper.createDocument()
        xml.setXMLEncoding("iso-8859-1");

        Element esito = xml.addElement("esito")
        esito.addElement("codice").setText(codice)
        esito.addElement("descrizione").setText(descrizione)

        Element dati_registro = esito.addElement("dati_registro")
        dati_registro.addElement("DataRegistrazione").setText(data ? data : "")
        dati_registro.addElement("NumeroRegistrazione").setText(numero ? numero : "")
        dati_registro.addElement("OggettoRegistrazione").setText(oggetto ? oggetto : "")
        dati_registro.addElement("IDRegistro").setText(registro ? registro : "")

        return xml.asXML()
    }
}
