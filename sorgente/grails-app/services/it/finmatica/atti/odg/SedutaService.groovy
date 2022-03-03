package it.finmatica.atti.odg

import grails.plugin.springsecurity.SpringSecurityService
import groovy.xml.StreamingMarkupBuilder
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.dizionari.Email
import it.finmatica.atti.dizionari.RegistroService
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.*
import it.finmatica.atti.dto.dizionari.NotificaDTO
import it.finmatica.atti.dto.odg.CommissioneStampaDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDTO
import it.finmatica.atti.dto.odg.SedutaDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.odg.SedutaConfig
import it.finmatica.atti.odg.dizionari.EsitoStandard
import it.finmatica.atti.odg.dizionari.RuoloPartecipante
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.gestionetesti.GestioneTestiService
import org.apache.commons.io.IOUtils
import org.hibernate.FetchMode
import it.finmatica.gestionetesti.TipoFile

import java.text.SimpleDateFormat

class SedutaService {

    GestioneTestiService  gestioneTestiService
    SpringSecurityService springSecurityService
    StampaUnicaService stampaUnicaService
    NotificheService   notificheService
    RegistroService    registroService
    WkfIterService     wkfIterService
    SedutaConfig       sedutaConfig

    void inviaNotifica (SedutaDTO seduta, OggettoSedutaDTO oggettoSeduta, NotificaDTO notifica, List<String> indirizziEmail, CommissioneStampaDTO stampa) {
        // creo la notifica della seduta:

        SedutaNotifica notificaSeduta = new SedutaNotifica();
        notificaSeduta.dataInvio = new Date()
        notificaSeduta.indirizziEmail = indirizziEmail.join(",");
        notificaSeduta.notifica = notifica.domainObject;
        notificaSeduta.oggettoSeduta = oggettoSeduta?.domainObject;
        notificaSeduta.seduta = seduta.domainObject;
        notificaSeduta.utenteInvio = springSecurityService.currentUser;
        notificaSeduta.save(failOnError: true)

        List<SoggettoNotifica> soggettiNotifica = [];
        for (String indirizzoEmail : indirizziEmail) {
            SoggettoNotifica s = new SoggettoNotifica();
            s.email = indirizzoEmail;
            soggettiNotifica << s
        }

        def allegati =  notifica.allegati?.equals("STAMPA_CONVOCAZIONE") ? [getStampaConvocazione(seduta, stampa)] : null

        notificheService.notifica(notificaSeduta.notifica, notificaSeduta.oggettoSeduta ?: notificaSeduta.seduta, soggettiNotifica, allegati);
    }

    it.finmatica.atti.mail.Allegato getStampaConvocazione (SedutaDTO seduta, CommissioneStampaDTO stampaDTO) {
        CommissioneStampa stampa = stampaDTO.domainObject

        // se non trovo la stampa, esco:
        if (stampa?.modelloTesto == null) {
            return null
        }

        InputStream testo = gestioneTestiService.stampaUnione(stampa.modelloTesto, [id: seduta.id, id_seduta_stampa:-1], TipoFile.PDF.estensione, true)
        return new it.finmatica.atti.mail.Allegato(stampa.modelloTesto.nome+".pdf", new ByteArrayInputStream(IOUtils.toByteArray(testo)))
    }

    List<Commissione> getListaCommissioni () {
        def listaRuoliCompetenze = springSecurityService.principal.uo().ruoli.flatten().codice.unique()

        return Commissione.createCriteria().list() {
            if (!(AttiUtils.isUtenteAmministratore())) {
                ruoloCompetenze {
                    'in'("ruolo", listaRuoliCompetenze)
                }
            }

            fetchMode("ruoloCompetenze", FetchMode.JOIN)
            order("titolo", "asc")
        }
    }

    def cercaSedute (Date date, long idCommissione, boolean seduteDaVerbalizzare) {
        // ottengo il primo giorno del mese:
        Date inizioMese = date.updated(date: 1).clearTime()
        // ottengo il primo giorno del mese successivo (funziona anche con i cambi di anno)
        Date inizioMeseSuccessivo = inizioMese.updated(month: inizioMese[Calendar.MONTH] + 1)

        def listaRuoliCompetenze = springSecurityService.principal.uo().ruoli.flatten().codice.unique()

        def sedute = Seduta.executeQuery("""
					select s
					  from Seduta s
					 where (:id_commissione < 0 or commissione.id = :id_commissione)
					   and commissione.ruoloCompetenze.ruolo in (:ruoli)
					   and s.valido = true
					   and s.dataSeduta between :inizioMese and :inizioMeseSuccessivo
					   and (:daVerbalizzare = false or exists(select o from OggettoSeduta o where o.seduta = s and o.confermaEsito = false) or not exists(select o from OggettoSeduta o where o.seduta = s))
					 order by dataSeduta asc, oraSeduta asc
			""", [id_commissione        : (int) (idCommissione ?: -1)
                  , ruoli               : listaRuoliCompetenze + "AGDATTI"
                  , inizioMese          : inizioMese
                  , inizioMeseSuccessivo: inizioMeseSuccessivo
                  , daVerbalizzare      : seduteDaVerbalizzare]);

        sedute = sedute.toDTO(["commissione", "tipoSeduta"]);

        return sedute.groupBy { it.dataSeduta.clearTime() }.each { k, v -> v.sort { it.oraSeduta } }
    }

    void numeraSeduta (Seduta seduta) {
        // siccome ho già numerato, esco:
        if (seduta.numero > 0) {
            return
        };

        // per prendere il numero della seduta, bisogna fare riferimento alla data della seduta
        registroService.numera(TipoRegistro.findByCodice(seduta.commissione.tipoRegistroSeduta.codice), seduta.dataSeduta,
                               { numero, anno, data, registro ->
                                   seduta.numero = numero
                                   seduta.anno = anno
                               })
    }

    void confermaEsito (OggettoSeduta oggetto) {
        PropostaDelibera proposta = oggetto.propostaDelibera

        // prima di confermare l'esito, faccio alcuni controlli:

        // ho l'esito da confermare?
        if (oggetto.esito == null) {
            throw new AttiRuntimeException("Non è possibile procedere senza aver assegnato un esito.")
        }

        if (oggetto.propostaDelibera.stato == StatoDocumento.ANNULLATO){
            throw new AttiRuntimeException("Impossibile confermare un esito alla proposta ${oggetto.propostaDelibera.numeroProposta}/${oggetto.propostaDelibera.annoProposta}, la proposta è stata ANNULLATA.")
        }

        // sto per creare una delibera su un oggetto seduta non adottabile?
        if (!oggetto.propostaDelibera.tipologia.adottabile && oggetto.esito.esitoStandard.creaDelibera) {
            throw new AttiRuntimeException(
                    "Non è possibile creare la delibera per la proposta n. ${proposta.numeroProposta}: la tipologia indica che la proposta non è adottabile.")
        }

        // http://svi-redmine/issues/12458 -> ODG: Impostare sempre la data di discussione della proposta
        // http://svi-redmine/issues/22189 -> ODG: Impostare sempre anche l'ora di discussione della proposta
        if (oggetto.seduta.dataInizioSeduta == null) {
            oggetto.seduta.dataInizioSeduta = oggetto.seduta.dataSeduta
            oggetto.seduta.oraInizioSeduta = oggetto.seduta.oraSeduta
        }

        if (oggetto.dataDiscussione == null) {
            oggetto.dataDiscussione = oggetto.seduta.dataInizioSeduta
            oggetto.oraDiscussione  = oggetto.seduta.oraInizioSeduta
        }

        if (oggetto.dataDiscussione.before(oggetto.seduta.dataInizioSeduta)) {
            throw new AttiRuntimeException(
                    "Non è possibile proseguire: l'oggetto seduta ${proposta.numeroProposta} / ${proposta.annoProposta} ha la data di discussione precedente alla data di inizio seduta.")
        }

        oggetto.confermaEsito = true
        oggetto.save(failOnError: true)

        // invio subito la notifica (se  necessario) così lo step del flusso è quello corretto
        if (oggetto.esito.notificaVerbalizzazione) {
            notificheService.notifica(TipoNotifica.VERBALIZZAZIONE_PROPOSTA, oggetto.propostaDelibera);
        }

        switch (oggetto.esito.esitoStandard.codice) {
            // la proposta deve essere riproposta per la seduta successiva della stessa commissione
            case EsitoStandard.PARZIALE:
                StatoOdg.mandaInOdg(proposta)
                proposta.oggettoSeduta = null;    // tolgo l'oggettoSeduta così che possa essere rimessa in una nuova seduta.
                proposta.save();
                break;

            // la proposta con esito concluso significa che è stata "approvata": non diventa delibera ma il suo flusso non deve neanche annullata ma deve diventare proposta conclusa.
            case EsitoStandard.CONCLUSO:

                // la proposta viene rinviata all'unità proponente
            case EsitoStandard.RINVIO_UFFICIO:

                // la proposta non viene adottata.
            case EsitoStandard.NON_ADOTTATO:
                StatoOdg.concludiOdg(proposta);
                //				proposta.oggettoSeduta = null;	// non tolgo l'oggetto seduta perché verrà controllato più avanti nel flusso e queste proposte non devono essere reinserite in odg.
                // verrà svuotato dal rigenera proposta.
                proposta.save()
                wkfIterService.sbloccaDocumento(proposta)
                break;

            // la delibera deve essere riproposta per la seduta successiva della commissione scelta
            case EsitoStandard.INVIA_COMMISSIONE:
                StatoOdg.mandaInOdg(proposta);
                proposta.oggettoSeduta =
                        null;    // tolgo l'oggettoSeduta perché così può essere reinserita in una nuova seduta per la nuova commissione.
                proposta.commissione = oggetto.esito.commissioneArrivo
                proposta.save();
                break;

            case EsitoStandard.DA_RATIFICARE: // FIXME: questo caso lo metto qui che non faccia nulla anche se prima o poi andrà gestito.
            case EsitoStandard.ADOTTATO:
                break;
        }
    }

    def creaFileXml (Seduta seduta) {
        String redirectUrl = Impostazioni.URL_SERVER_PUBBLICO.valore;

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy")

        List<SedutaPartecipante> listaPartecipantiSeduta = SedutaPartecipante.createCriteria().list() {
            projections {
                "incarico.titolo"
                "commissioneComponente.incarico.titolo"
            }

            eq("seduta.id", seduta.id)
            or {
                ne("ruoloPartecipante.codice", RuoloPartecipante.CODICE_SEGRETARIO)
                isNull("ruoloPartecipante.codice")
            }

            fetchMode("commissioneComponente", FetchMode.JOIN)
            fetchMode("ruoloPartecipante", FetchMode.JOIN)
            fetchMode("commissioneComponente.componente", FetchMode.JOIN)
            fetchMode("commissioneComponente.incarico", FetchMode.JOIN)
            fetchMode("componenteEsterno", FetchMode.JOIN)
            fetchMode("incarico", FetchMode.JOIN)

            order("sequenzaPartecipante", "asc")
        }

        List<OggettoSeduta> listaOggettiSeduta = Delibera.createCriteria().list {
            projections {
                property("oggettoSeduta")
            }
            oggettoSeduta {
                eq("seduta.id", seduta.id)
                order("sequenzaDiscussione", "asc")
            }
            fetchMode("oggettoSeduta", FetchMode.JOIN)
            fetchMode("propostaDelibera", FetchMode.JOIN)
            fetchMode("propostaDelibera.tipologia", FetchMode.JOIN)
        }

        List<OggettoPartecipante> listaOggettoPartecipanti

        def xml = new StreamingMarkupBuilder().bind { builder ->
            mkp.xmlDeclaration()
            builder.'seduta' {
                builder.data(dateFormatter.format(seduta.dataSeduta))
                builder.ora(seduta.oraSeduta)
                builder.data_inizio(dateFormatter.format(seduta.dataInizioSeduta ?: seduta.dataSeduta))
                builder.ora_inizio(seduta.oraInizioSeduta ?: seduta.oraSeduta)
                if (seduta.dataFineSeduta != null) {
                    builder.data_fine(dateFormatter.format(seduta.dataFineSeduta ?: seduta.dataSeduta))
                }
                builder.ora_fine(seduta.oraFineSeduta)
                builder.commissione(seduta.commissione.titolo)
                builder.partecipanti {
                    for (SedutaPartecipante partecipanteSeduta in listaPartecipantiSeduta) {
                        builder.partecipante {
                            builder.nominativo(
                                    partecipanteSeduta.commissioneComponente != null && partecipanteSeduta.commissioneComponente != "" ? partecipanteSeduta.commissioneComponente?.componente?.denominazione : partecipanteSeduta.componenteEsterno?.denominazione)
                            builder.ruolo(partecipanteSeduta.ruoloPartecipante?.descrizione)
                            builder.incarico(
                                    partecipanteSeduta.incarico != null && partecipanteSeduta.incarico != "" ? partecipanteSeduta.incarico?.titolo : partecipanteSeduta.commissioneComponente?.incarico?.titolo)
                            builder.presenza(partecipanteSeduta.presente ? "SI" : "NO")
                        }
                    }
                }

                builder.proposte {
                    for (OggettoSeduta oggettoSeduta in listaOggettiSeduta) {
                        builder.proposta {
                            builder.numero_proposta(oggettoSeduta.propostaDelibera.numeroProposta)
                            builder.anno_proposta(oggettoSeduta.propostaDelibera.annoProposta)
                            builder.tipo(oggettoSeduta.propostaDelibera.tipologia?.titolo)
                            builder.numero_delibera(oggettoSeduta.delibera.numeroDelibera)
                            builder.anno_delibera(oggettoSeduta.delibera.annoDelibera)
                            builder.oggetto(oggettoSeduta.delibera.oggetto)
                            builder.registro(oggettoSeduta.delibera.registroDelibera?.descrizione)
                            builder.visualizzatore(
                                    redirectUrl + "/AttiVisualizzatore/visualizza/delibera/" + oggettoSeduta.delibera.id.toString())

                            listaOggettoPartecipanti = OggettoPartecipante.findAllByOggettoSeduta(oggettoSeduta).sort { it.sequenza }

                            builder.partecipanti {
                                for (OggettoPartecipante oggettoPartecipante in listaOggettoPartecipanti) {
                                    if (oggettoPartecipante.ruoloPartecipante?.codice == null || oggettoPartecipante.ruoloPartecipante?.codice != RuoloPartecipante.CODICE_SEGRETARIO) {
                                        builder.partecipante {
                                            builder.nominativo(
                                                    oggettoPartecipante.sedutaPartecipante.commissioneComponente != null && oggettoPartecipante.sedutaPartecipante.commissioneComponente != "" ? oggettoPartecipante.sedutaPartecipante.commissioneComponente?.componente?.denominazione : oggettoPartecipante.sedutaPartecipante.componenteEsterno?.denominazione)
                                            builder.ruolo(oggettoPartecipante.ruoloPartecipante?.descrizione)
                                            builder.incarico(
                                                    oggettoPartecipante.sedutaPartecipante.incarico != null && oggettoPartecipante.sedutaPartecipante.incarico != "" ? oggettoPartecipante.sedutaPartecipante.incarico?.titolo : oggettoPartecipante.sedutaPartecipante.commissioneComponente?.incarico?.titolo)
                                            builder.presenza(oggettoPartecipante.presente ? "SI" : "NO")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return xml
    }

    def creaFileCsv (Seduta seduta) {
        String redirectUrl = Impostazioni.URL_SERVER_PUBBLICO.valore;

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

        List<OggettoSeduta> listaOggettiSeduta = PropostaDelibera.createCriteria().list {
            projections {
                property("oggettoSeduta")
            }
            oggettoSeduta {
                eq("seduta.id", seduta.id)
                order("sequenzaDiscussione", "asc")
            }
            fetchMode("oggettoSeduta", FetchMode.JOIN)
            fetchMode("delibera", FetchMode.JOIN)
            fetchMode("propostaDelibera", FetchMode.JOIN)
            fetchMode("propostaDelibera.tipologia", FetchMode.JOIN)
        }

        def map = [[ordine: "ID", categoria : "ID_CATEGORIA", data:"DATA_SEDUTA", oggetto: "OGGETTO", tipologia:"ID_TIPOLOGIA"]]
        for (OggettoSeduta oggettoSeduta in listaOggettiSeduta) {
            map << [ordine:     oggettoSeduta.sequenzaDiscussione,
                    categoria:  "1",
                    data:       dateFormatter.format(seduta.dataSeduta)+" "+seduta.oraSeduta,
                    oggetto:    oggettoSeduta.delibera?.oggetto ?: oggettoSeduta.propostaDelibera.oggetto,
                    tipologia:  sedutaConfig.getTipologia(oggettoSeduta.propostaDelibera.tipologiaDocumento.id) ?: ""]
        }
        return map
    }

    void creaStampaUnica (Seduta seduta) {
        List<OggettoSeduta> listaOggettiSeduta = OggettoSeduta.createCriteria().list {
            eq("seduta.id", seduta.id)
            isNotNull("propostaDelibera")

            fetchMode("seduta", FetchMode.JOIN)
            fetchMode("propostaDelibera", FetchMode.JOIN)
            fetchMode("propostaDelibera.stampaUnica", FetchMode.JOIN)

            order("sequenzaDiscussione", "asc")
        }

        if (listaOggettiSeduta.size() == 0) {
            throw new AttiRuntimeException("Non è possibile creare la stampa unica per la seduta in quanto è prima di proposte.")
        }
        ArrayList<PropostaDelibera> proposte = new ArrayList<PropostaDelibera>();

        for (def oggetto : listaOggettiSeduta) {
            if (oggetto.propostaDelibera?.stampaUnica == null) {
                throw new AttiRuntimeException(
                        "Non è possibile creare la stampa unica per la seduta. Non è presente la stampa unica per la proposta ${oggetto.propostaDelibera.numeroProposta}/${oggetto.propostaDelibera.annoProposta}.")
            }
            proposte.add(oggetto.propostaDelibera)
        }
        stampaUnicaService.stampaUnicaSeduta(proposte)
    }

    boolean isTutteDelibereConEsitoConfermato (Seduta seduta) {
        List<OggettoSeduta> oggettiSeduta = OggettoSeduta.findAllBySeduta(seduta)

        int oggettiConfermati = 0
        for (OggettoSeduta oggettoSeduta : oggettiSeduta) {
            if (oggettoSeduta.confermaEsito == true) {
                oggettiConfermati++
            }
        }

        return (oggettiSeduta.size() > 0 && oggettiConfermati == oggettiSeduta.size())
    }
}

