package it.finmatica.atti.integrazioni.contabilita

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.contabilita.MovimentoAnagrafica
import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioniws.sicraweb.CmnWSSGatewayService
import it.finmatica.atti.integrazioniws.sicraweb.CmnWSSGatewayServiceLocator
import it.finmatica.atti.integrazioniws.sicraweb.CmnWSSGateway_PortType
import it.finmatica.atti.integrazioniws.sicraweb.classes.Esito
import it.finmatica.atti.integrazioniws.sicraweb.classes.TipoRisultato
import it.finmatica.atti.integrazioniws.sicraweb.classes.aggiornaAnagrafica.*
import it.finmatica.atti.integrazioniws.sicraweb.classes.inserimentoImpegni.*
import it.finmatica.atti.integrazioniws.sicraweb.classes.rendiEsecutiviImpegni.Definitivo
import it.finmatica.atti.integrazioniws.sicraweb.classes.rendiEsecutiviImpegni.Proposta
import it.finmatica.atti.integrazioniws.sicraweb.classes.rendiEsecutiviImpegni.RichiestaRendiEsecutiviImpegni
import it.finmatica.atti.integrazioniws.sicraweb.classes.rendiEsecutiviImpegni.RispostaRendiEsecutiviImpegni
import it.finmatica.atti.integrazioniws.sicraweb.classes.ricercaAnagrafica.Codici
import it.finmatica.atti.integrazioniws.sicraweb.classes.ricercaAnagrafica.RichiestaRicercaAnagrafica
import it.finmatica.atti.integrazioniws.sicraweb.classes.ricercaAnagrafica.RispostaRicercaAnagrafica
import it.finmatica.atti.integrazioniws.sicraweb.classes.ricercaBudgetBilancio.Budget
import it.finmatica.atti.integrazioniws.sicraweb.classes.ricercaBudgetBilancio.RichiestaRicercaBudgetBilancio
import it.finmatica.atti.integrazioniws.sicraweb.classes.ricercaBudgetBilancio.RispostaRicercaBudgetBilancio
import it.finmatica.atti.integrazioniws.sicraweb.classes.ricercaBudgetImpegni.RichiestaRicercaBudgetImpegni
import it.finmatica.atti.integrazioniws.sicraweb.classes.ricercaBudgetImpegni.RispostaRicercaBudgetImpegni
import it.finmatica.atti.integrazioniws.utility.Utility
import it.finmatica.atti.utility.InvioMail
import org.apache.log4j.Logger
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.zkoss.bind.BindUtils
import org.zkoss.zk.ui.event.EventQueues

import javax.xml.bind.JAXBException
import javax.xml.rpc.ServiceException
import java.rmi.RemoteException

@Component("integrazioneContabilitaJSerfin")
@Lazy
class IntegrazioneContabilitaJSerfin extends AbstractIntegrazioneContabilita {

    private static final Logger log = Logger.getLogger(IntegrazioneContabilitaJSerfin.class)

    public static final String APPLICAZIONE = "SFERA"
    public static final String DOMICILIO = "DOMICILIO"
    public static final String RESIDENZA = "RESIDENZA"
    public static final String DATE_FORMAT = "yyyy-MM-dd"
    public static final String DATE_FORMAT_ANAGRAFICA = "dd/MM/yyyy"

    //public static final String endPointService="https://sicrawebtest.comune.parma.it/client/services/CmnWSSGateway"
    //public static final String alias="@sfera_ws_test"

    @Autowired
    IntegrazioneContabilitaJSerfinConfig integrazioneContabilitaJSerfinConfig

    @Autowired
    SpringSecurityService springSecurityService


    @Override
    String getZul(IDocumento documento) {
        return "/atti/integrazioni/contabilita/movimentiContabiliJSerfin.zul"
    }

    void aggiornaMaschera(IDocumento documento, boolean modifica) {
        //all'apertura della pagina passo da qui...anche all'apertura del sacco di soldi
        BindUtils.postGlobalCommand("movimentiContabiliQueue", EventQueues.DESKTOP, "aggiornaAtto", [atto: documento, competenza: modifica ? "W" : "R"])
    }

    List<?> getMovimentiContabili(IDocumento documento) {
        //passo da qui all'apertura dal sacco di soldi
        def documentoPrincipale = getProposta(documento)


        /*Nella tabella MOVIMENTI_CONTABILI ABBIAMO
        - PROGRESSIVO ESTERNO: numero (visibile in maschera) dell'impegno / accertamento / programmazione / subimpengo
        - NUMERO_VARIAZIONE: numero della variazione che si sta facendo (valorizzato per variazione di impegno / prenotazione / accertamento
        - NUMERO_IMPEGNO_PADRE: numero dell'impegno utilizzato solo in caso di subimpengo
        - NUMERO_PROGRAMMAZIONE: numero della programmazione utilizzato solo in caso di impegno su programmazione
         */

        return MovimentoContabile.createCriteria().list {
            eq("idDocumento", documentoPrincipale.id)
            eq("tipoDocumento", documentoPrincipale.TIPO_OGGETTO)

            createAlias("anagrafica", "anagrafica", CriteriaSpecification.LEFT_JOIN)

            projections {
                property("id")
                property("idEsterno")
                property("annoEsercizio")
                property("importo")
                property("idCapitolo")
                property("capitolo")
                property("descrizioneCapitolo")
                property("descrizione")
                property("descrizionePdcLiv5")
                property("pdcLiv5")
                property("descrizioneFinanziamento1")
                property("cig")
                property("cup")
                property("numero")
                property("tipoDettaglio")
                property("opera")
                property("tipoUscita")
                property("idProgetto")
                property("codProgetto")
                property("progetto")
                property("copFpv")
                property("sigCpt")
                property("desCpt")
                property("codcentrocosto")
                property("descentrocosto")
                property("numeroVariazione")
                property("progressivoEsterno")
                property("anagrafica.id")
                property("anagrafica.ragioneSociale")
                property("anagrafica.cognome")
                property("anagrafica.nome")
                property("anagrafica.codiceFiscale")
                property("anagrafica.partitaIva")
                property("anagrafica.tipoIndirizzo")
                property("anagrafica.indirizzo")
                property("anagrafica.idApplicativoEsterno")
                property("numeroProgrammazione")
                property("numeroImpegnoPadre")
                property("codiceGlobale")
            }

            order("annoEsercizio", "asc")
            order("capitolo", "asc")
            order("desCpt", "asc")

        }.collect { row ->
            [id                       : row[0],
             idEsterno                : row[1],
             annoEsercizio            : row[2],
             importo                  : row[3],
             idCapitolo               : row[4],
             capitolo                 : row[5],
             descrizioneCapitolo      : row[6],
             descrizione              : row[7],
             descrizionePdcLiv5       : row[8],
             pdcLiv5                  : row[9],
             descrizioneFinanziamento1: row[10],
             cig                      : row[11],
             cup                      : row[12],
             numero                   : row[13],
             tipoDettaglio            : row[14],
             opera                    : row[15],
             tipoUscita               : row[16],
             iprogetto                : row[17],
             codprogetto              : row[18],
             progetto                 : row[19],
             copfpv                   : row[20],
             sigcpt                   : row[21],
             descpt                   : row[22],
             codcentrocosto           : row[23],
             descentrocosto           : row[24],
             numeroVariazione         : row[25],
             progressivoEsterno       : row[26],
             idAnagrafica             : row[27],
             ragioneSociale           : row[28],
             cognome                  : row[29],
             nome                     : row[30],
             codiceFiscale            : row[31],
             partitaIva               : row[32],
             tipoIndirizzo            : row[33],
             indirizzo                : row[34],
             idAnagraficaEsterno      : row[35],
             numeroProgrammazione     : row[36],
             numeroImpegnoPadre       : row[37],
             codiceGlobale            : row[38]
            ]
        }
    }

    List<Anagrafica> getListaAnagrafica(String filtro) throws Exception {
        /*
        todo
        1. ricerca anagrafica tramite codice fiscale (unico campo di input nella ricerca)
        2. per ogni anagrafica trovata, scorrere l'elenco e creare un istanza della classe New Anagrafica
        3. un'anagrafica può avere più indirizzi collegati...tra questi trovare il domicilio fiscale. Se presente aggiungere una nuova istanza nella classe anagrafica.
        Quindi c'è il caso in cui un'anagrafica restituita dal ws porti 2 occorrenze della nostra classe Anagrafica
        4. per ogni anagrafica restituita dal ws dobbiamo verificare il codice dell'applicativo che l'ha creata. Se siamo noi (codice da definire SFERA ??) allora l'anagrafica è modificabile, altrimenti no
        Nella classe Anagrafica lo gestiamo con il boolean protetta
         */
        List<Anagrafica> ret = new ArrayList<Anagrafica>()
        CmnWSSGateway_PortType port = getPortService(integrazioneContabilitaJSerfinConfig.getUrlWebService())
        RichiestaRicercaAnagrafica richiestaAnagrafica = new RichiestaRicercaAnagrafica()
        if (filtro.length() < 16)
            filtro += "%" //se e' solo parziale inserisco il % per cercare in like la fine della stringa
        // si puo' inserire solo un % preferisco metterlo alla fine
        richiestaAnagrafica.codice_fiscale = filtro.toUpperCase()
        richiestaAnagrafica.escludi_da_bonificare = 1

        RispostaRicercaAnagrafica rispostaRicercaAnagrafica = ricercaAnagrafica(port, richiestaAnagrafica, integrazioneContabilitaJSerfinConfig.getUtenteWebService())
        if (rispostaRicercaAnagrafica.esito.tipo.equals(Esito.OK)) {
            for (it.finmatica.atti.integrazioniws.sicraweb.classes.ricercaAnagrafica.Anagrafica anagrafica in rispostaRicercaAnagrafica.lista_anagrafiche.anagrafica) {
                Anagrafica attiAnagrafica = new Anagrafica()
                attiAnagrafica.pkid = anagrafica.pkid
                attiAnagrafica.nome = anagrafica.nome
                attiAnagrafica.cognome = anagrafica.cognome
                if (anagrafica.domiciliofiscale == null) {
                    attiAnagrafica.ragioneSociale = anagrafica.rag_sociale

                }
                attiAnagrafica.codiceFiscale = anagrafica.cf
                if (!(anagrafica.domiciliofiscale))
                    attiAnagrafica.partitaIva = anagrafica.piva
                if (anagrafica.residenza != null) {
                    String specie = ''
                    if (anagrafica.residenza.specie) {
                        specie = anagrafica.residenza.specie + " "
                    }
                    attiAnagrafica.indirizzo = specie + anagrafica.residenza.area
                    attiAnagrafica.civico = anagrafica.residenza.civico
                    attiAnagrafica.comune = anagrafica.residenza.comune
                    attiAnagrafica.provincia = anagrafica.residenza.sigla_provincia
                    attiAnagrafica.cap = anagrafica.residenza.cap
                    attiAnagrafica.tipoIndirizzo = RESIDENZA
                }
                //se il primo codice è SFERA allora posso modificare l'anagrafica
                def listaCodici = anagrafica.rif_import?.codici
                Codici primoCodice = listaCodici ? listaCodici[0] : null
                attiAnagrafica.protetta = !APPLICAZIONE.equals(primoCodice?.applicazione)

                ret.add(attiAnagrafica)
                if (anagrafica.domiciliofiscale != null) {
                    attiAnagrafica = new Anagrafica()
                    attiAnagrafica.pkid = anagrafica.pkid
                    //       attiAnagrafica.nome = anagrafica.nome
                    //       attiAnagrafica.cognome = anagrafica.cognome
                    attiAnagrafica.codiceFiscale = anagrafica.cf
                    attiAnagrafica.partitaIva = anagrafica.piva
                    attiAnagrafica.ragioneSociale = anagrafica.rag_sociale
                    if (anagrafica.domiciliofiscale != null) {
                        attiAnagrafica.indirizzo = anagrafica.domiciliofiscale.specie + " " + anagrafica.domiciliofiscale.area
                        attiAnagrafica.civico = anagrafica.residenza.civico
                        attiAnagrafica.comune = anagrafica.domiciliofiscale.comune
                        attiAnagrafica.provincia = anagrafica.domiciliofiscale.sigla_provincia
                        attiAnagrafica.cap = anagrafica.domiciliofiscale.cap
                        attiAnagrafica.tipoIndirizzo = DOMICILIO
                    }
                    attiAnagrafica.protetta = protetta
                    ret.add(attiAnagrafica)
                }
            }
        } else {
            throw new Exception(rispostaRicercaAnagrafica.esito.descrizione)
        }
        return ret
    }

    String creaAnagrafica(MovimentoAnagrafica anagraficaAtti) throws Exception {
        RichiestaAggiornaAnagrafica richiestaAggiornaAnagrafica = new RichiestaAggiornaAnagrafica()
        String idNuovaAnagrafica = null
        if (anagraficaAtti != null) {
            CmnWSSGateway_PortType port = getPortService(integrazioneContabilitaJSerfinConfig.getUrlWebService())
            it.finmatica.atti.integrazioniws.sicraweb.classes.aggiornaAnagrafica.Anagrafica anagrafica = new it.finmatica.atti.integrazioniws.sicraweb.classes.aggiornaAnagrafica.Anagrafica()
            if (anagraficaAtti.nome) {
                anagrafica.nome = anagraficaAtti.nome
            } else {
                anagrafica.nome = ' '
            }

            if (anagraficaAtti.cognome) {
                anagrafica.cognome = anagraficaAtti.cognome
            } else {
                anagrafica.cognome = ' '
            }

            anagrafica.codFiscale = (anagraficaAtti.codiceFiscale != null && anagraficaAtti.codiceFiscale.length() > 0 ? anagraficaAtti.codiceFiscale : "")
            anagrafica.versione = "2.0"
            anagrafica.codice = (anagraficaAtti.codiceFiscale != null && anagraficaAtti.codiceFiscale.length() > 0 ? anagraficaAtti.codiceFiscale : anagraficaAtti.partitaIva)
            anagrafica.app = APPLICAZIONE
            anagrafica.dataIni = Utility.getStringFromDate(new Date(), DATE_FORMAT_ANAGRAFICA)
//            Comune comuneNascita = new Comune()
            Italiano it = new Italiano()
            it.desComune = anagraficaAtti.comune
            it.prov = anagraficaAtti.siglaProvincia

            if (anagraficaAtti.ragioneSociale != null && (!(''.equals(anagraficaAtti.ragioneSociale)))) {
                anagrafica.tipo = "G"
            }
            else {
                anagrafica.tipo = "F"
            }
            anagrafica.pIva = (anagraficaAtti.partitaIva != null && anagraficaAtti.partitaIva.length() > 0 ? anagraficaAtti.partitaIva : "")
            anagrafica.ragSociale = (anagraficaAtti.ragioneSociale != null && anagraficaAtti.ragioneSociale.length() > 0 ? anagraficaAtti.ragioneSociale : "")

            Recapito recapito = new Recapito()
            recapito.flgRes = 1
            recapito.des = RESIDENZA
            IndirizzoExtraComune indirizzoExtraComune = new IndirizzoExtraComune()
            Comune comune = new Comune()
            it.codCatasto = ""
            it.desComune = anagraficaAtti.comune
            it.prov = anagraficaAtti.siglaProvincia
            comune.it = it
            indirizzoExtraComune.comune = comune
            indirizzoExtraComune.area = anagraficaAtti.indirizzo
            indirizzoExtraComune.civico = anagraficaAtti.civico?.toInteger()
            indirizzoExtraComune.cap = anagraficaAtti.cap
            Indirizzo indirizzo = new Indirizzo()
            indirizzo.indirizzoExtraComune = indirizzoExtraComune

            Recapiti recapiti = new Recapiti()
            recapiti.recapito = new ArrayList<Recapito>()
            recapito.indirizzo = indirizzo
            recapiti.recapito.add(recapito)

            anagrafica.recapiti = recapiti
            richiestaAggiornaAnagrafica.anagrafica = anagrafica
            richiestaAggiornaAnagrafica.action = (anagraficaAtti.idApplicativoEsterno != null && anagraficaAtti.idApplicativoEsterno.length() > 0 ? "update" : "insert")

            String utenteCorrente = integrazioneContabilitaJSerfinConfig.getUtenteWebService()
            if (integrazioneContabilitaJSerfinConfig.isUtenteSessione()) {
                utenteCorrente = (springSecurityService.currentUser != null ? "@" + springSecurityService.currentUser.nominativo.toLowerCase() : integrazioneContabilitaJSerfinConfig.getUtenteWebService())
            }
            ResponseGeneric ret = inserisciAnagrafica(port, richiestaAggiornaAnagrafica, utenteCorrente)
            if (ret.esitoOk != null) {
                idNuovaAnagrafica = ret.esitoOk.id
            } else if (ret.esitoKo != null) {
                throw new Exception(ret.esitoKo.codErrore + " " + ret.esitoKo.desErrore)
            }
        }

        return idNuovaAnagrafica
    }

    RispostaRicercaBudgetBilancio getCapitoli(Integer numeroCapitolo, Integer annoCapitolo, Integer codLavoro) {
        //devo richiamare il ws fde.ricerca_budget_bilancio

        CmnWSSGateway_PortType port = getPortService(integrazioneContabilitaJSerfinConfig.getUrlWebService())
        RichiestaRicercaBudgetBilancio ricerca = new RichiestaRicercaBudgetBilancio()
        ricerca.annocompetenza = annoCapitolo
        ricerca.numcapitolo = numeroCapitolo
        ricerca.flraggruppacentrocosto = 1
        ricerca.flraggruppacpt = 0
        ricerca.flraggruppapianofin = 0
        ricerca.flraggruppafpv = 1
        ricerca.flraggruppalavoro = 1
        ricerca.flraggruppatipospesa = 1
        ricerca.flraggruppatipofinanz = 1
        ricerca.flraggruppaprogetto = 1
        ricerca.flmostraquintilivelli = 1
        ricerca.aclbilancio = 1  //indica se i capitoli devono essere filtrati in base alla competenza dell'utente
        if (isRicercaCapitoliCodiceLavoro()) {
            ricerca.codlavoro = codLavoro
        }
        String utenteCorrente = (springSecurityService.currentUser != null ? springSecurityService.currentUser.nominativo : integrazioneContabilitaJSerfinConfig.getUtenteWebService())
        RispostaRicercaBudgetBilancio risposta = ricercaBudgetBilanci(port, ricerca, "@" + utenteCorrente.toLowerCase())

        if (!risposta.esito.tipo.equals(Esito.OK)) {
            throw new Exception(risposta.esito.descrizione)
        }
        return risposta
    }

    List<Budget> getPrenotazioni(Integer numeroPrenotazione, Integer annoPrenotazione, Integer annoCompetenza, Long codiceGlobale) {
        //invocare: fde.ricerca_budget_impegno
/* ESEMPIO DI RICERCA PRENOTAZIONI
<richiesta>
    <parte>U</parte>
    <tipo_risultato>
        <nome>XML</nome>
    </tipo_risultato>
    <codannuale>11878</codannuale>
    <anno>2023</anno>
    <annocompetenza>2023</annocompetenza>
    <datavaluta>2023-07-04</datavaluta>
    <prenotazione>1</prenotazione>
    <flraggruppacentrocosto>1</flraggruppacentrocosto>
    <flraggruppacpt>1</flraggruppacpt>
    <flraggruppapianofin>0</flraggruppapianofin>
    <flraggruppalavoro>1</flraggruppalavoro>
    <flraggruppafpv>1</flraggruppafpv>
    <flraggruppatipospesa>1</flraggruppatipospesa>
    <flraggruppatipofinanz>1</flraggruppatipofinanz>
    <flraggruppaprogetto>1</flraggruppaprogetto>
    <aclbilancio>1</aclbilancio>
</richiesta>
 */
        CmnWSSGateway_PortType port = getPortService(integrazioneContabilitaJSerfinConfig.getUrlWebService())
        RichiestaRicercaBudgetImpegni ricerca = new RichiestaRicercaBudgetImpegni()
        ricerca.parte = "U"
        if (codiceGlobale) {
            ricerca.codimpegno = codiceGlobale
        }
        else {
            ricerca.anno = annoPrenotazione
            ricerca.annocompetenza = annoCompetenza
            ricerca.codannuale = numeroPrenotazione
        }
        ricerca.prenotazione = 1
        ricerca.flraggruppacentrocosto = 1
        ricerca.flraggruppacpt = 1
        ricerca.flraggruppapianofin = 1
        ricerca.flraggruppalavoro = 1
        ricerca.flraggruppafpv = 1
        ricerca.flraggruppatipospesa = 1
        ricerca.flraggruppatipofinanz = 1
        ricerca.flraggruppaprogetto = 1
        ricerca.tipo_risultato = new TipoRisultato()
        ricerca.tipo_risultato.nome = "XML"
        ricerca.datavaluta = Utility.getStringFromDate(new Date(), DATE_FORMAT)
        ricerca.aclbilancio = 1  //indica se i capitoli devono essere filtrati in base alla competenza dell'utente

        String utenteCorrente = (springSecurityService.currentUser != null ? springSecurityService.currentUser.nominativo : integrazioneContabilitaJSerfinConfig.getUtenteWebService())
        RispostaRicercaBudgetImpegni risposta = ricercaBudgetImpegni(port, ricerca, "@" + utenteCorrente.toLowerCase())

        if (!risposta.esito.tipo.equals(Esito.OK)) {
            throw new Exception(risposta.esito.descrizione)
        }
        return risposta?.lista_budget?.budget
    }

    /*
    valori per azione
    0 Creazione impegno/accertamento/sub/proposta
    1 Creazione variazione impegno/accertamento/sub/proposta
    2 Modifica testata impegno/accertamento/sub/proposta/variazione
    3 Cancellazione impegno/accertamento/sub/proposta
     */

    RispostaInserimentoImpegni creaImpegno(IDocumento documento, MovimentoContabile movimentoContabile, String azione, String xmlMessaggio) throws Exception {
        String  siglaTipoAtto = ""
        if (documento instanceof Determina)  {
            siglaTipoAtto = integrazioneContabilitaJSerfinConfig.getTipologia(documento.tipologiaDocumento.id)
            if ((siglaTipoAtto == null) || ("".equals(siglaTipoAtto))) {
                siglaTipoAtto = integrazioneContabilitaJSerfinConfig.getCodiceTipoAtto()
            }
        }
        else {
            if (documento instanceof PropostaDelibera) {
                siglaTipoAtto = integrazioneContabilitaJSerfinConfig.getTipologia(documento.tipologiaDocumento.id)
                if ((siglaTipoAtto == null) || ("".equals(siglaTipoAtto))) {
                    siglaTipoAtto = integrazioneContabilitaJSerfinConfig.getCodiceTipoAttoDelibera()
                }
            }
            else {
                siglaTipoAtto = integrazioneContabilitaJSerfinConfig.getTipologia(documento.proposta.tipologiaDocumento.id)
                if ((siglaTipoAtto == null) || ("".equals(siglaTipoAtto))) {
                    siglaTipoAtto = integrazioneContabilitaJSerfinConfig.getCodiceTipoAttoDelibera()
                }
            }
        }
        if (("".equals(siglaTipoAtto)) || (siglaTipoAtto == null)) {
            //Le determine le passo sempre in contabilità. Delibere e proposte di delibera solo in caso di presenza del mapping
        } else {
            CmnWSSGateway_PortType port = getPortService(integrazioneContabilitaJSerfinConfig.getUrlWebService())
            RichiestaInserimentoImpegni inserimento = new RichiestaInserimentoImpegni()
            if (xmlMessaggio != null)
                inserimento.messaggio_xml = xmlMessaggio

            Impegno impegno = new Impegno()
            impegno.progressivo = 1   //valorizzato sempre a 1

            Testata testata = new Testata()
            testata.azione = azione
            if ("2".equals(azione)) {
                testata.codannuale = movimentoContabile.progressivoEsterno
            }
            testata.anno = movimentoContabile.annoEsercizio
            testata.parte = movimentoContabile.tipoUscita // E o U
            testata.dataregistrazione = Utility.getStringFromDate(new Date(), DATE_FORMAT)
            testata.esecutivo = 0
            switch (movimentoContabile.opera) {
                case 'PRENOTAZIONE':
                    testata.prenotazione = 1
                    break
                case 'IMPEGNO':
                    break
                case 'IMPEGNO_PRENOTAZIONE':
                    testata.idprenotazionepartenza = movimentoContabile.idProgrammazione?.toLong()
                    break
                case 'ACCERTAMENTO':
                    break
                case 'PRENOTAZIONE_VARIAZIONE':
                    testata.prenotazione = 1
                    testata.idprenotazionepartenza = movimentoContabile.idProgrammazione?.toLong()
                    break
                case 'IMPEGNO_VARIAZIONE':
                case 'SUBIMPEGNO':
                    testata.idimpegnopadre = movimentoContabile.idImpegno?.toLong()
                    break
                case 'SUBIMPEGNO_VARIAZIONE':
                    testata.idimpegnopadre = movimentoContabile.idImpegno?.toLong()
                    break
                case 'ACCERTAMENTO_VARIAZIONE':
                    testata.idimpegnopadre = movimentoContabile.idImpegno?.toLong()
                    break
            }

            testata.descrizione = movimentoContabile.descrizione

            testata.numatto = documento.numeroProposta
            testata.annoatto = documento.annoProposta
            testata.dataatto = Utility.getStringFromDate(documento.dataNumeroProposta, DATE_FORMAT)

            testata.codSettore = integrazioneContabilitaJSerfinConfig.getCodiceSettore()
            testata.siglaTipoAtto = siglaTipoAtto

            testata.numattoOri = documento.numeroProposta
            testata.annoattoOri = documento.annoProposta
            testata.dataattoOri = Utility.getStringFromDate(documento.dataNumeroProposta, DATE_FORMAT)

            testata.codSettoreOri = integrazioneContabilitaJSerfinConfig.getCodiceSettore()
            testata.siglaTipoAttoOri = siglaTipoAtto

            impegno.testata = testata

            Dettaglio dettaglio = new Dettaglio()
            switch (movimentoContabile.opera) {
                case 'PRENOTAZIONE':
                    break
                case 'IMPEGNO':
                    break
                case 'ACCERTAMENTO':
                    break
                case 'IMPEGNO_PRENOTAZIONE':
                case 'PRENOTAZIONE_VARIAZIONE':
                case 'IMPEGNO_VARIAZIONE':
                case 'SUBIMPEGNO':
                case 'SUBIMPEGNO_VARIAZIONE':
                case 'ACCERTAMENTO_VARIAZIONE':
                    Padre padre = new Padre()
                    padre.idcapitolo = movimentoContabile.idCapitolo?.toLong()
                    padre.annocompetenza = movimentoContabile.annoCompetenza
                    padre.codprogetto = movimentoContabile.codProgetto?.toLong()
                    padre.codcentrocosto = movimentoContabile.codcentrocosto?.toLong()
                    padre.codlavoro = movimentoContabile.codlavoro?.toInteger()
                    padre.codtipofinanz = movimentoContabile.codtipofinanz?.toInteger()
                    padre.siglacpt = movimentoContabile.sigCpt
                    padre.copertofpv = movimentoContabile.copFpv?.toInteger()

                    padre.cig = movimentoContabile.cig
                    padre.cup = movimentoContabile.cup
                    dettaglio.padre = padre
                    break
            }
            dettaglio.descrizione = movimentoContabile.descrizione
            dettaglio.datavaluta = Utility.getStringFromDate(new Date(), DATE_FORMAT)
            dettaglio.idcapitolo = movimentoContabile.idCapitolo?.toLong()

            if (('PRENOTAZIONE_VARIAZIONE'.equals(movimentoContabile.opera)) || ('IMPEGNO_VARIAZIONE'.equals(movimentoContabile.opera)) || ('ACCERTAMENTO_VARIAZIONE'.equals(movimentoContabile.opera)) || ('SUBIMPEGNO_VARIAZIONE'.equals(movimentoContabile.opera))) {
                if (isInVariazioneAnnoCompetenzaUgualeImpegno()) {
                    //Parma: L'anno di competenza della variazione corrisponde all'anno di esercizio dell'impegno originario
                    dettaglio.annocompetenza = movimentoContabile.annoEsercizio
                }
                else {
                    //Trento: l'anno di competenza della variazione è indicato dall'utente. Se non lo indica considero la competenza dell'impegno originario oppure il suo anno di esercizio
                    if (movimentoContabile.annoCompetenzaVariazione) {
                        dettaglio.annocompetenza = movimentoContabile.annoCompetenzaVariazione
                    }
                    else {
                        if (movimentoContabile.annoCompetenza) {
                            dettaglio.annocompetenza = movimentoContabile.annoCompetenzaVariazione
                        }
                        else {
                            dettaglio.annocompetenza = movimentoContabile.annoEsercizio
                        }
                    }
                }
            }
            else {
                dettaglio.annocompetenza = movimentoContabile.annoEsercizio
            }

            dettaglio.codcentrocosto = movimentoContabile.codcentrocosto?.toInteger()
            dettaglio.codprogetto = movimentoContabile.codProgetto?.toInteger()
            dettaglio.codtipofinanz = movimentoContabile.codtipofinanz?.toLong()
            dettaglio.codlavoro = movimentoContabile.codlavoro?.toLong()
            dettaglio.codtipospesa = movimentoContabile.codtipospesa?.toLong()
            dettaglio.siglacpt = movimentoContabile.sigCpt
            dettaglio.cig = movimentoContabile.cig
            dettaglio.cup = movimentoContabile.cup
            dettaglio.siglapianofin = movimentoContabile.pdcLiv5
            //se nel mapping è definito un codice cassa, allora lo riporto
            if (integrazioneContabilitaJSerfinConfig.getCodiceCassa()) {
                dettaglio.cassa = integrazioneContabilitaJSerfinConfig.getCodiceCassa()
            }
            dettaglio.importo = movimentoContabile.importo
            dettaglio.copertofpv = movimentoContabile.copFpv?.toLong()
            if (movimentoContabile.anagrafica) {
                if (movimentoContabile.anagrafica.idApplicativoEsterno)
                    dettaglio.idfornitore = movimentoContabile.anagrafica.idApplicativoEsterno.toLong()
            }
            //se nel mapping è definito un codice credito/debito, allora lo riporto
            if (integrazioneContabilitaJSerfinConfig.getCodiceCreditoDebito()) {
                dettaglio.creditodebitopredefinito = integrazioneContabilitaJSerfinConfig.getCodiceCreditoDebito().toInteger()
            }
            impegno.dettaglio = dettaglio

            inserimento.impegno = new ArrayList<Impegno>()
            inserimento.impegno.add(impegno)


            String utenteCorrente = integrazioneContabilitaJSerfinConfig.getUtenteWebService()
            if (integrazioneContabilitaJSerfinConfig.isUtenteSessione()) {
                utenteCorrente = (springSecurityService.currentUser != null ? "@" + springSecurityService.currentUser.nominativo.toLowerCase() : integrazioneContabilitaJSerfinConfig.getUtenteWebService())
            }
            RispostaInserimentoImpegni risposta = inserimentoImpegni(port, inserimento, utenteCorrente)

            return risposta
        }
    }
/*
<richiesta>
    <impegno>
        <progressivo>1</progressivo>
        <testata>
            <parte>U</parte>
            <azione>3</azione>
            <codannuale>1</codannuale>
            <anno>2023</anno>
        </testata>
    </impegno>
</richiesta>
 */

    void eliminaImpegno(MovimentoContabile movimentoContabile) throws Exception {
        CmnWSSGateway_PortType port = getPortService(integrazioneContabilitaJSerfinConfig.getUrlWebService())
        RichiestaInserimentoImpegni inserimento = new RichiestaInserimentoImpegni()

        Impegno impegno = new Impegno()
        impegno.progressivo = 1   // FISSO

        Testata testata = new Testata()
        testata.azione = "3"
        testata.parte = movimentoContabile.tipoUscita // E o U
        //se è una prenotazione metto il tag
        if (('PRENOTAZIONE'.equals(movimentoContabile.tipoDettaglio)) || ('PRENOTAZIONE_VARIAZIONE'.equals(movimentoContabile.tipoDettaglio)))
            testata.prenotazione = 1
        testata.idimpegno = movimentoContabile.idEsterno.toLong()
        impegno.testata = testata

        inserimento.impegno = new ArrayList<Impegno>()
        inserimento.impegno.add(impegno)

        String utenteCorrente = integrazioneContabilitaJSerfinConfig.getUtenteWebService()
        if (integrazioneContabilitaJSerfinConfig.isUtenteSessione()) {
            utenteCorrente = (springSecurityService.currentUser != null ? "@" + springSecurityService.currentUser.nominativo.toLowerCase() : integrazioneContabilitaJSerfinConfig.getUtenteWebService())
        }

        RispostaInserimentoImpegni risposta = inserimentoImpegni(port, inserimento, utenteCorrente)
        if (!risposta.messaggi.tipo.equals(Esito.OK)) {
            if ("ERR".equals(risposta.messaggi.tipo)) {
                if (risposta.messaggi.descrizione?.length() > 500) {
                    throw new Exception(risposta.messaggi.descrizione.substring(0, 500))
                } else {
                    throw new Exception(risposta.messaggi.descrizione)
                }
            } else {

                if (risposta.messaggi.messaggio_xml != null) {
                    inserimento.messaggio_xml = risposta.messaggi.messaggio_xml
                    risposta = inserimentoImpegni(port, inserimento, utenteCorrente)
                    if (!risposta.messaggi.tipo.equals(Esito.OK)) {
                        if (risposta.messaggi.descrizione?.length() > 500) {
                            throw new Exception(risposta.messaggi.descrizione.substring(0, 500))
                        } else {
                            throw new Exception(risposta.messaggi.descrizione)
                        }
                    }
                } else {
                    //il ws ha restituito errore ma non c'è il dettaglio
                    throw new Exception("Errore generico")
                }
            }
        }
    }

    void annullaProposta(IProposta proposta) {
        boolean daAnnullare = false

        if (proposta instanceof Determina) {
            daAnnullare = true
        }
        else {
            if (proposta instanceof PropostaDelibera) {
                daAnnullare = integrazioneContabilitaJSerfinConfig.isDeliberaIntegrata(proposta.tipologia.id)
            } else {
                if (proposta instanceof Delibera) {
                    daAnnullare = integrazioneContabilitaJSerfinConfig.isDeliberaIntegrata(proposta.tipologia.id)
                }
            }
        }

        if (daAnnullare) {
            def documentoPrincipale = getProposta(proposta)

            List<MovimentoContabile> listaMovimenti = MovimentoContabile.createCriteria().list {
                eq("idDocumento", documentoPrincipale.id)
                eq("tipoDocumento", documentoPrincipale.TIPO_OGGETTO)
            }

            for (MovimentoContabile mc in listaMovimenti) {
                eliminaImpegno(mc)
                //se non ho errori elimino subito il movimento contabile su sfera
                mc.delete(flush: true)
            }

        }
    }

    @Transactional
    void rendiEsecutivoAtto(IAtto documento) {
        boolean daRendereEsecutiva = false

        if (documento instanceof Determina) {
            daRendereEsecutiva = documento.tipologia.esecutivitaMovimenti
        }
        else {
            if (documento instanceof Delibera) {
                daRendereEsecutiva = integrazioneContabilitaJSerfinConfig.isDeliberaIntegrata(documento.proposta.tipologia.id)
                if (daRendereEsecutiva) {
                    daRendereEsecutiva = documento.proposta.tipologia.esecutivitaMovimenti
                }
            }
        }

        if (daRendereEsecutiva) {
            // per ogni movimento devo renderlo esecutivo
            // rendiEsecutivo...
            def documentoPrincipale = getProposta(documento)

//            List<MovimentoContabile> listaMovimenti = MovimentoContabile.createCriteria().list {
//                eq("idDocumento", documentoPrincipale.id)
//                eq("tipoDocumento", documentoPrincipale.TIPO_OGGETTO)
//            }

//            for (MovimentoContabile mc in listaMovimenti) {
//                rendiEsecutivoMovimentoAtto(mc, documento)
//            }
            if (isConDocumentiContabili(documentoPrincipale)) {
                try{
                    rendiEsecutivoMovimentoAtto(documento)
                }catch(Throwable e){
                    InvioMail.inviaMailErrori("Errore in fase di rendi esecutivo l'atto Contabilità Jserfin"
                            ,getDatiAtto(documento,e.getMessage()) + "\n Utente operazione: " + springSecurityService.currentUser?.id
                            ,e.getStackTrace().toString())
                    throw new AttiRuntimeException("Errore in fase di rendi esecutivo l'atto Contabilità Jserfin: ${e.message}", e)
                }

            }
        }
    }

    static String getDatiAtto(def atto, String message){
        String ret
        ret="Id: " + atto.id
        if (atto instanceof Determina) {
            ret+="\n Tipologia: "+ atto.tipologia.titolo
            ret+="\n Anno Proposta: "+ atto.annoProposta
            ret+="\n Numero Proposta: "+ atto.numeroProposta
        } else if (atto instanceof Delibera) {
            ret+="\n Tipologia: "+ atto.propostaDelibera.tipologia.titolo
            ret+="\n Anno Proposta: "+ atto.propostaDelibera.annoProposta
            ret+="\n Numero Proposta: "+ atto.propostaDelibera.numeroProposta
        }
        if(message!=null && message.length()>0){
            ret+="\n Dettaglio Errore: "+ message
        }
        return ret;
    }

//    void rendiEsecutivoMovimentoAtto(MovimentoContabile movimentoContabile, IAtto documento) throws Exception {
      void rendiEsecutivoMovimentoAtto(IAtto documento) throws Exception {
        String  siglaTipoAtto = ""
        if (documento instanceof Determina)  {
            siglaTipoAtto = integrazioneContabilitaJSerfinConfig.getTipologia(documento.tipologiaDocumento.id)
            if ((siglaTipoAtto == null) || ("".equals(siglaTipoAtto))) {
                siglaTipoAtto = integrazioneContabilitaJSerfinConfig.getCodiceTipoAtto()
            }
        }
        else {
            if (documento instanceof PropostaDelibera) {
                siglaTipoAtto = integrazioneContabilitaJSerfinConfig.getTipologia(documento.tipologiaDocumento.id)
                if ((siglaTipoAtto == null) || ("".equals(siglaTipoAtto))) {
                    siglaTipoAtto = integrazioneContabilitaJSerfinConfig.getCodiceTipoAttoDelibera()
                }
            }
            else {
                siglaTipoAtto = integrazioneContabilitaJSerfinConfig.getTipologia(documento.proposta.tipologiaDocumento.id)
                if ((siglaTipoAtto == null) || ("".equals(siglaTipoAtto))) {
                    siglaTipoAtto = integrazioneContabilitaJSerfinConfig.getCodiceTipoAttoDelibera()
                }
            }
        }
        if (!(("".equals(siglaTipoAtto)) || (siglaTipoAtto == null))) {
            rendiEsecutivoDocumento(documento.proposta.getNumeroProposta(), documento.proposta.getAnnoProposta(), documento.proposta.getDataNumeroProposta(), documento.getNumeroAtto(), documento.getAnnoAtto(), documento.getDataAtto(), siglaTipoAtto)
        }
    }

    @Transactional
    void rendiEsecutivaProposta(PropostaDelibera proposta) {
        boolean daRendereEsecutiva =  integrazioneContabilitaJSerfinConfig.isDeliberaIntegrata(proposta.tipologia.id)

        if (daRendereEsecutiva) {
            List<MovimentoContabile> listaMovimenti = MovimentoContabile.createCriteria().list {
                eq("idDocumento", proposta.id)
                eq("tipoDocumento", proposta.TIPO_OGGETTO)
            }

            for (MovimentoContabile mc in listaMovimenti) {
                rendiEsecutivoMovimentoProposta(mc, proposta)
            }
        }
    }

    void rendiEsecutivoMovimentoProposta(MovimentoContabile movimentoContabile, PropostaDelibera proposta) throws Exception {
        String  siglaTipoAtto = integrazioneContabilitaJSerfinConfig.getTipologia(proposta.tipologiaDocumento.id)

        if (!(("".equals(siglaTipoAtto)) || (siglaTipoAtto == null))) {
            rendiEsecutivoDocumento(proposta.getNumeroProposta(), proposta.getAnnoProposta(), proposta.getDataNumeroProposta(), proposta.getNumeroProposta(), proposta.getAnnoProposta(), proposta.getDataNumeroProposta(), siglaTipoAtto)
        }
    }

    private void rendiEsecutivoDocumento(Integer numeroProposta, Integer annoProposta, Date dataProposta,  Integer numeroAtto, Integer annoAtto, Date dataAtto, String siglaTipoAtto) {
        CmnWSSGateway_PortType port = getPortService(integrazioneContabilitaJSerfinConfig.getUrlWebService())
        RichiestaRendiEsecutiviImpegni rendiEsecutivoImpegno = new RichiestaRendiEsecutiviImpegni()

        it.finmatica.atti.integrazioniws.sicraweb.classes.rendiEsecutiviImpegni.Impegno impegno = new it.finmatica.atti.integrazioniws.sicraweb.classes.rendiEsecutiviImpegni.Impegno()
        it.finmatica.atti.integrazioniws.sicraweb.classes.rendiEsecutiviImpegni.Testata testata = new it.finmatica.atti.integrazioniws.sicraweb.classes.rendiEsecutiviImpegni.Testata()
        Proposta proposta = new Proposta()

        proposta.siglaTipoAtto = siglaTipoAtto //integrazioneContabilitaJSerfinConfig.getCodiceTipoAtto()
        //proposta.codSettore = integrazioneContabilitaJSerfinConfig.getCodiceSettore()
        proposta.numatto = numeroProposta
        proposta.annoatto = annoProposta
        proposta.dataatto = Utility.getStringFromDate(dataProposta, DATE_FORMAT)

        // proposta.dataatto = Utility.getStringFromDate(documento.getDataAtto(), DATE_FORMAT)
        testata.proposta = proposta

        Definitivo definitivo = new Definitivo()
        definitivo.siglaTipoAtto = siglaTipoAtto
        definitivo.codSettore = 'DD'
        definitivo.numatto = numeroAtto
        definitivo.annoatto = annoAtto
        definitivo.dataatto = Utility.getStringFromDate(dataAtto, DATE_FORMAT)
        //Utility.getStringFromDate(new Date(), DATE_FORMAT)
        //  definitivo.datavaluta = Utility.getStringFromDate(new Date(), DATE_FORMAT)
        testata.definitivo = definitivo
        impegno.testata = testata
        rendiEsecutivoImpegno.impegno = impegno

        testata.proposta.codSettore = integrazioneContabilitaJSerfinConfig.getCodiceSettore()

        String utenteCorrente = integrazioneContabilitaJSerfinConfig.getUtenteWebService()
        if (integrazioneContabilitaJSerfinConfig.isUtenteSessione()) {
            utenteCorrente = (springSecurityService.currentUser != null ? "@" + springSecurityService.currentUser.nominativo.toLowerCase() : integrazioneContabilitaJSerfinConfig.getUtenteWebService())
        }
        RispostaRendiEsecutiviImpegni risposta = rendiEsecutiviImpegni(port, rendiEsecutivoImpegno, utenteCorrente)
        if (!risposta.messaggi.tipo.equals(Esito.OK)) {
            if ("ERR".equals(risposta.messaggi.tipo)) {
                if (risposta.messaggi.descrizione?.length() > 500) {
                    throw new Exception(risposta.messaggi.descrizione.substring(0, 500))
                } else {
                    throw new Exception(risposta.messaggi.descrizione)
                }
            } else {

                if (risposta.messaggi.messaggio_xml != null) {
                    rendiEsecutivoImpegno.messaggio_xml = risposta.messaggi.messaggio_xml
                    risposta = rendiEsecutiviImpegni(port, rendiEsecutivoImpegno, utenteCorrente)
                    if (!risposta.messaggi.tipo.equals(Esito.OK)) {
                        if (risposta.messaggi.descrizione?.length() > 500) {
                            throw new Exception(risposta.messaggi.descrizione.substring(0, 500))
                        } else {
                            throw new Exception(risposta.messaggi.descrizione)
                        }
                    }
                } else {
                    //il ws ha restituito errore ma non c'è il dettaglio
                    throw new Exception("Errore generico")
                }
            }
        }
    }

    List<Budget> getImpegni(Integer numeroImpegno, Integer annoImpegno, Integer annoCompetenza, Long codiceGlobale) {
        //invocare: fde.ricerca_budget_impegno
        //il ritorno direi che è uguale al getPrenotazioni....valutare se tenere due metodi diversi o uno solo
/* ESEMPIO
<richiesta>
    <parte>U</parte>
    <tipo_risultato>
        <nome>XML</nome>
    </tipo_risultato>
    <codannuale>11898</codannuale>
    <anno>2023</anno>
    <annocompetenza>2023</annocompetenza>
    <datavaluta>2023-07-04</datavaluta>
    <flraggruppacentrocosto>1</flraggruppacentrocosto>
    <flraggruppacpt>1</flraggruppacpt>
    <flraggruppapianofin>0</flraggruppapianofin>
    <flraggruppalavoro>1</flraggruppalavoro>
    <flraggruppafpv>1</flraggruppafpv>
    <flraggruppatipospesa>1</flraggruppatipospesa>
    <flraggruppatipofinanz>1</flraggruppatipofinanz>
    <flraggruppaprogetto>1</flraggruppaprogetto>
    <aclbilancio>1</aclbilancio>
</richiesta>

 */
        CmnWSSGateway_PortType port = getPortService(integrazioneContabilitaJSerfinConfig.getUrlWebService())
        RichiestaRicercaBudgetImpegni ricerca = new RichiestaRicercaBudgetImpegni()
        ricerca.parte = "U"
        if (codiceGlobale) {
            ricerca.codimpegno = codiceGlobale
        }
        else {
            ricerca.anno = annoImpegno
            ricerca.annocompetenza = annoCompetenza
            ricerca.codannuale = numeroImpegno
        }
        ricerca.flraggruppacentrocosto = 1
        ricerca.flraggruppacpt = 1
        ricerca.flraggruppapianofin = 1
        ricerca.flraggruppalavoro = 1
        ricerca.flraggruppafpv = 1
        ricerca.flraggruppatipospesa = 1
        ricerca.flraggruppatipofinanz = 1
        ricerca.flraggruppaprogetto = 1
        ricerca.aclbilancio = 1  //indica se i capitoli devono essere filtrati in base alla competenza dell'utente

        //    ricerca.prenotazione=1
        ricerca.tipo_risultato = new TipoRisultato()
        ricerca.tipo_risultato.nome = "XML"
        ricerca.datavaluta = Utility.getStringFromDate(new Date(), DATE_FORMAT)

        String utenteCorrente = (springSecurityService.currentUser != null ? springSecurityService.currentUser.nominativo : integrazioneContabilitaJSerfinConfig.getUtenteWebService())
        RispostaRicercaBudgetImpegni risposta = ricercaBudgetImpegni(port, ricerca, "@" + utenteCorrente.toLowerCase())

        if (!risposta.esito.tipo.equals(Esito.OK)) {
            throw new Exception(risposta.esito.descrizione)
        }
        return risposta?.lista_budget?.budget
    }

    List<Budget> getSubImpegni(Integer numeroSubImpegno, Integer annoSubImpegno, Integer annoCompetenza, Long codiceGlobale) {
        //invocare: fde.ricerca_budget_impegno
        /* ESEMPI
        <richiesta>
            <parte>U</parte>
            <tipo_risultato>
                <nome>XML</nome>
            </tipo_risultato>
            <codannuale>11898</codannuale>
            <anno>2023</anno>
            <annocompetenza>2023</annocompetenza>
            <datavaluta>2023-07-04</datavaluta>
            <flraggruppacentrocosto>1</flraggruppacentrocosto>
            <flraggruppacpt>1</flraggruppacpt>
            <flraggruppapianofin>0</flraggruppapianofin>
            <flraggruppalavoro>1</flraggruppalavoro>
            <flraggruppafpv>1</flraggruppafpv>
            <flraggruppatipospesa>1</flraggruppatipospesa>
            <flraggruppatipofinanz>1</flraggruppatipofinanz>
            <flraggruppaprogetto>1</flraggruppaprogetto>
            <aclbilancio>1</aclbilancio>
        </richiesta>
         */
        CmnWSSGateway_PortType port = getPortService(integrazioneContabilitaJSerfinConfig.getUrlWebService())
        RichiestaRicercaBudgetImpegni ricerca = new RichiestaRicercaBudgetImpegni()
        ricerca.parte = "U"
        if (codiceGlobale) {
            ricerca.codimpegno = codiceGlobale
        }
        else {
            ricerca.anno = annoSubImpegno
            ricerca.annocompetenza = annoCompetenza
            ricerca.codannuale = numeroSubImpegno
        }
        ricerca.flraggruppacentrocosto = 1
        ricerca.flraggruppacpt = 1
        ricerca.flraggruppapianofin = 1
        ricerca.flraggruppalavoro = 1
        ricerca.flraggruppafpv = 1
        ricerca.flraggruppatipospesa = 1
        ricerca.flraggruppatipofinanz = 1
        ricerca.flraggruppaprogetto = 1
        ricerca.tipo_risultato = new TipoRisultato()
        ricerca.tipo_risultato.nome = "XML"
        ricerca.datavaluta = Utility.getStringFromDate(new Date(), DATE_FORMAT)
        ricerca.aclbilancio = 1  //indica se i capitoli devono essere filtrati in base alla competenza dell'utente

        String utenteCorrente = (springSecurityService.currentUser != null ? springSecurityService.currentUser.nominativo : integrazioneContabilitaJSerfinConfig.getUtenteWebService())
        RispostaRicercaBudgetImpegni risposta = ricercaBudgetImpegni(port, ricerca, "@" + utenteCorrente.toLowerCase())

        if (!risposta.esito.tipo.equals(Esito.OK)) {
            throw new Exception(risposta.esito.descrizione)
        }
        return risposta?.lista_budget?.budget
    }

    List<Budget> getAccertamenti(Integer numeroAccertamento, Integer annoAccertamento, Integer annoCompetenza, Long codiceGlobale) {
        /*
        ESEMPIO
        <richiesta>
            <parte>E</parte>
            <tipo_risultato>
                <nome>XML</nome>
            </tipo_risultato>
            <codannuale>1349</codannuale>
            <anno>2023</anno>
            <annocompetenza>2023</annocompetenza>
            <datavaluta>2023-07-04</datavaluta>
            <flraggruppacentrocosto>1</flraggruppacentrocosto>
            <flraggruppacpt>1</flraggruppacpt>
            <flraggruppapianofin>0</flraggruppapianofin>
            <flraggruppalavoro>1</flraggruppalavoro>
            <flraggruppafpv>1</flraggruppafpv>
            <flraggruppatipospesa>1</flraggruppatipospesa>
            <flraggruppatipofinanz>1</flraggruppatipofinanz>
            <flraggruppaprogetto>1</flraggruppaprogetto>
        <aclbilancio>1</aclbilancio>
         */

        CmnWSSGateway_PortType port = getPortService(integrazioneContabilitaJSerfinConfig.getUrlWebService())
        RichiestaRicercaBudgetImpegni ricerca = new RichiestaRicercaBudgetImpegni()
        ricerca.parte = "E"
        if (codiceGlobale) {
            ricerca.codimpegno = codiceGlobale
        }
        else {
            ricerca.anno = annoAccertamento
            ricerca.annocompetenza = annoCompetenza
            ricerca.codannuale = numeroAccertamento

        }
        //   ricerca.prenotazione = 1
        ricerca.tipo_risultato = new TipoRisultato()
        ricerca.tipo_risultato.nome = "XML"
        ricerca.datavaluta = Utility.getStringFromDate(new Date(), DATE_FORMAT)
        ricerca.aclbilancio = 1  //indica se i capitoli devono essere filtrati in base alla competenza dell'utente
        ricerca.flraggruppacentrocosto = 1
        ricerca.flraggruppacpt = 1
        ricerca.flraggruppapianofin = 1
        ricerca.flraggruppalavoro = 1
        ricerca.flraggruppafpv = 1
        ricerca.flraggruppatipospesa = 1
        ricerca.flraggruppatipofinanz = 1
        ricerca.flraggruppaprogetto = 1

        String utenteCorrente = (springSecurityService.currentUser != null ? springSecurityService.currentUser.nominativo : integrazioneContabilitaJSerfinConfig.getUtenteWebService())
        RispostaRicercaBudgetImpegni risposta = ricercaBudgetImpegni(port, ricerca, "@" + utenteCorrente.toLowerCase())

        if (!risposta.esito.tipo.equals(Esito.OK)) {
            throw new Exception(risposta.esito.descrizione)
        }
        return risposta?.lista_budget?.budget
    }

    @Override
    boolean isConDocumentiContabili(IDocumento documento) {
        def documentoPrincipale = getProposta(documento)
        return (MovimentoContabile.countByIdDocumentoAndTipoDocumento(documentoPrincipale.id, documentoPrincipale.TIPO_OGGETTO) > 0)
    }

    @Override
    boolean isTipiDocumentoAbilitati() {
        return false
    }

    //metodi WS
    public static CmnWSSGateway_PortType getPortService(String endPointService) throws MalformedURLException, ServiceException {
        CmnWSSGateway_PortType ret = null
        CmnWSSGatewayService service = new CmnWSSGatewayServiceLocator()
        ret = service.getCmnWSSGateway(new URL(endPointService))
        return ret
    }

    public static RispostaRicercaAnagrafica ricercaAnagrafica(CmnWSSGateway_PortType port, RichiestaRicercaAnagrafica richiestaRicercaAnagrafica, String aliasUtente) throws MalformedURLException, ServiceException, RemoteException, JAXBException {
        //aliasUtente = TRENTO: "@test_finanziaria" - PARMA: "@p.azzoni"

        RispostaRicercaAnagrafica ret = null
        List<String> datiInput = new ArrayList<String>()
        datiInput.add("semanthic_switch_v2")
        datiInput.add(aliasUtente)
        datiInput.add("fde.ricerca_anagrafica")
        String xmlInput = Utility.getXmlStringFromObject(richiestaRicercaAnagrafica)
        datiInput.add(xmlInput)
        log.info("ricercaAnagrafica:" + xmlInput)
        String response = port.service(toArray(datiInput))
        if (response != null && response.length() > 0)
            ret = (RispostaRicercaAnagrafica) Utility.getObjectFromXml(response, RispostaRicercaAnagrafica.class)
        String xmlOutput = Utility.getXmlStringFromObject(ret)
        log.info("ricercaAnagrafica:" + xmlOutput)
        return ret
    }

    public static ResponseGeneric inserisciAnagrafica(CmnWSSGateway_PortType port, RichiestaAggiornaAnagrafica richiesta, String aliasUtente) throws MalformedURLException, ServiceException, RemoteException, JAXBException {
        //aliasUtente = TRENTO: "@test_finanziaria" - PARMA: "@p.azzoni"

        ResponseGeneric ret = null
        List<String> datiInput = new ArrayList<String>()
        datiInput.add("semanthic_switch_v2")
        datiInput.add(aliasUtente)
        datiInput.add("fde.inserimento_anagrafica")
        String xmlInput = Utility.getXmlStringFromObject(richiesta)
        log.info("inserisciAnagrafica:" + xmlInput)
        datiInput.add(xmlInput)
        String response = port.service(toArray(datiInput))
        if (response != null && response.length() > 0)
            ret = (ResponseGeneric) Utility.getObjectFromXml(response, ResponseGeneric.class)
        String xmlOutput = Utility.getXmlStringFromObject(ret)
        log.info("inserisciAnagrafica:" + xmlOutput)
        return ret
    }


    public static RispostaRicercaBudgetBilancio ricercaBudgetBilanci(CmnWSSGateway_PortType port, RichiestaRicercaBudgetBilancio richiesta, String aliasUtente) throws RemoteException, JAXBException {
        //aliasUtente = TRENTO: "@test_finanziaria" - PARMA: "@p.azzoni"

        RispostaRicercaBudgetBilancio ret = null
        List<String> datiInput = new ArrayList<String>()
        datiInput.add("semanthic_switch_v2")
        datiInput.add(aliasUtente)
        datiInput.add("fde.ricerca_budget_bilancio")
        String xmlInput = Utility.getXmlStringFromObject(richiesta)
        log.info("ricercaBudgetBilanci - utente= " + aliasUtente + ": " + xmlInput)
        datiInput.add(xmlInput)
        String response = port.service(toArray(datiInput))
        if (response != null && response.length() > 0)
            ret = (RispostaRicercaBudgetBilancio) Utility.getObjectFromXml(response, RispostaRicercaBudgetBilancio.class)
        String xmlOutput = Utility.getXmlStringFromObject(ret)
        log.info("ricercaBudgetBilanci:" + xmlOutput)
        return ret
    }


    public static RispostaRendiEsecutiviImpegni rendiEsecutiviImpegni(CmnWSSGateway_PortType port, RichiestaRendiEsecutiviImpegni richiesta, String aliasUtente) throws RemoteException, JAXBException {
        //aliasUtente = TRENTO: "@test_finanziaria" - PARMA: "@p.azzoni"

        RispostaRendiEsecutiviImpegni ret = null
        List<String> datiInput = new ArrayList<String>()
        datiInput.add("semanthic_switch_v2")
        datiInput.add(aliasUtente)
        datiInput.add("fde.rendi_esecutivi_impegni")
        String xmlInput = Utility.getXmlStringFromObject(richiesta)
        log.info("rendiEsecutiviImpegni:" + xmlInput)
        datiInput.add(xmlInput)
        String response = port.service(toArray(datiInput))
        if (response != null && response.length() > 0)
            ret = (RispostaRendiEsecutiviImpegni) Utility.getObjectFromXml(response, RispostaRendiEsecutiviImpegni.class)
        String xmlOutput = Utility.getXmlStringFromObject(ret)
        log.info("rendiEsecutiviImpegni:" + xmlOutput)
        return ret
    }

    public static RispostaInserimentoImpegni inserimentoImpegni(CmnWSSGateway_PortType port, RichiestaInserimentoImpegni richiesta, String aliasUtente) throws RemoteException, JAXBException {
        //aliasUtente = TRENTO: "@test_finanziaria" - PARMA: "@p.azzoni"

        RispostaInserimentoImpegni ret = null
        List<String> datiInput = new ArrayList<String>()
        datiInput.add("semanthic_switch_v2")
        datiInput.add(aliasUtente)
        datiInput.add("fde.inserimento_impegni")
        String xmlInput = Utility.getXmlStringFromObject(richiesta)
        log.info("inserimentoImpegni:" + xmlInput)
        datiInput.add(xmlInput)
        String response = port.service(toArray(datiInput))
        if (response != null && response.length() > 0)
            ret = (RispostaInserimentoImpegni) Utility.getObjectFromXml(response, RispostaInserimentoImpegni.class)
        String xmlOutput = Utility.getXmlStringFromObject(ret)
        log.info("inserimentoImpegni:" + xmlOutput)
        return ret
    }


    public static RispostaRicercaBudgetImpegni ricercaBudgetImpegni(CmnWSSGateway_PortType port, RichiestaRicercaBudgetImpegni richiesta, String aliasUtente) throws RemoteException, JAXBException {
        //aliasUtente = TRENTO: "@test_finanziaria" - PARMA: "@p.azzoni"

        RispostaRicercaBudgetImpegni ret = null
        List<String> datiInput = new ArrayList<String>()
        datiInput.add("semanthic_switch_v2")
        datiInput.add(aliasUtente)
        datiInput.add("fde.ricerca_budget_impegni")
        String xmlInput = Utility.getXmlStringFromObject(richiesta)
        log.info("ricercaBudgetImpegni - utente= " + aliasUtente + ": " + xmlInput)
        datiInput.add(xmlInput)
        String response = port.service(toArray(datiInput))
        if (response != null && response.length() > 0)
            ret = (RispostaRicercaBudgetImpegni) Utility.getObjectFromXml(response, RispostaRicercaBudgetImpegni.class)
        String xmlOutput = Utility.getXmlStringFromObject(ret)
        log.info("ricercaBudgetImpegni:" + xmlOutput)
        return ret
    }

    public static String[] toArray(List<String> input) {
        String[] ret = null
        if (input != null && input.size() > 0) {
            ret = new String[input.size()]
            int i = 0
            for (String dato : input) {
                ret[i] = dato
                i++
            }
        }
        return ret
    }

    boolean isRicercaAnagraficaAbilitata() {
        return integrazioneContabilitaJSerfinConfig.isRicercaAnagraficaAbilitata()
    }

    boolean isVisualizzaTotali() {
        return integrazioneContabilitaJSerfinConfig.isVisualizzaTotali()
    }

    boolean isVisualizzaTipoFinanziamento() {
        return integrazioneContabilitaJSerfinConfig.isVisualizzaTipoFinanziamento()
    }

    boolean isRicercaCapitoliCodiceLavoro() {
        return integrazioneContabilitaJSerfinConfig.isRicercaCapitoliCodiceLavoro()
    }

    boolean isRicercaMovimentiCodiceGlobale() {
        return integrazioneContabilitaJSerfinConfig.isRicercaMovimentiCodiceGlobale()
    }
    boolean isAbilitataVariazioneSubImpegno() {
        return integrazioneContabilitaJSerfinConfig.isAbilitataVariazioneSubImpegno()
    }

    boolean isInVariazioneAnnoCompetenzaUgualeImpegno() {
        return integrazioneContabilitaJSerfinConfig.isInVariazioneAnnoCompetenzaUgualeImpegno()
    }

    boolean isVisualizzareCapitoliFPV() {
        return integrazioneContabilitaJSerfinConfig.isVisualizzareCapitoliFPV()
    }

    boolean isVLivelloObbligatorio() {
        return integrazioneContabilitaJSerfinConfig.isVLivelloObbligatorio()
    }

    boolean isContabilitaIntegrata(IDocumento documento) {
        boolean flag = false
        if (documento instanceof Determina)  {
            Determina dete = Determina.get(documento.id)
            flag = dete.tipologia.scritturaMovimentiContabili
        }
        else {
            if (documento instanceof PropostaDelibera) {
                PropostaDelibera propostaDelibera = PropostaDelibera.get(documento.id)
                if (propostaDelibera.tipologia.scritturaMovimentiContabili) {
                    flag = integrazioneContabilitaJSerfinConfig.isDeliberaIntegrata(propostaDelibera.tipologia.id)
                }
            }
            else {
                if (documento instanceof Delibera) {
                    Delibera delibera = Delibera.get(documento.id)
                    if (delibera.proposta.tipologia.scritturaMovimentiContabili) {
                        flag = integrazioneContabilitaJSerfinConfig.isDeliberaIntegrata(delibera.proposta.tipologia.id)
                    }
                }
            }
        }

        return flag
    }

    @Override
    BigDecimal totaleImporto(IDocumento documento, String tipo) {
        def documentoPrincipale = getProposta(documento)

        return MovimentoContabile.createCriteria().get {
            projections {
                sum("importo")
            }

            and {
                eq("idDocumento", documentoPrincipale.id)
                eq("tipoDocumento", documentoPrincipale.TIPO_OGGETTO)
                eq("opera", tipo)
                isNotNull("importo")
            }
        }
    }
}

class Anagrafica {
    String ragioneSociale
    String cognome
    String nome
    String codiceFiscale
    String partitaIva
    String indirizzo
    String sesso
    String civico
    String stato
    String provincia
    String comune
    String cap
    String email
    String applicativo
    String tipoIndirizzo
    String pkid
    boolean protetta


    public Anagrafica() {
        protetta = true
    }
}