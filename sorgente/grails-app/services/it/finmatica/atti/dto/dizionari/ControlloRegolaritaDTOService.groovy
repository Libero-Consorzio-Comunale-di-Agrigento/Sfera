package it.finmatica.atti.dto.dizionari

import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.*
import it.finmatica.atti.dto.documenti.ControlloRegolaritaDTO
import it.finmatica.atti.dto.documenti.ControlloRegolaritaDocumentoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.gestionetesti.GestioneTestiService
import org.apache.log4j.Logger
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.zul.Filedownload
import org.zkoss.zul.Messagebox

class ControlloRegolaritaDTOService {

    private static final Logger log = Logger.getLogger(ControlloRegolaritaDTOService.class);

    GestioneTestiService gestioneTestiService
    NotificheService notificheService

    public void elimina(ControlloRegolaritaDTO controlloRegolaritaDto) {
        getListaDocumenti(controlloRegolaritaDto).each { it -> it.delete(failError: true) }
        ControlloRegolarita.get(controlloRegolaritaDto.id).delete(failError: true)
    }


    public def cercaDocumenti(ControlloRegolaritaDTO controlloRegolaritaDto, def map) {
        if (controlloRegolaritaDto.tipoControlloRegolarita == null) {
            return [];
        }

        List id_listaTipologie = map.tipologie*.id
        List id_listaRegistri = map.registri*.id
        List id_listaCategorie = map.categorie*.id
        List id_listaStrutture = map.strutture*.id
        // cerco tutti i documenti che non sono già stati inviati all'organo scelto:
        def parametri = [codiceTipoControlloRegolarita: controlloRegolaritaDto.tipoControlloRegolarita.id
                         , dataEsecutivitaDal         : controlloRegolaritaDto.dataEsecutivitaDal
                         , dataEsecutivitaAl          : controlloRegolaritaDto.dataEsecutivitaAl
                         , listaRegistri              : (id_listaRegistri.size() > 0) ? id_listaRegistri : null
                         , listaTipologie             : (id_listaTipologie.size() > 0) ? id_listaTipologie : null
                         , listaCategorie             : (id_listaCategorie.size() > 0) ? id_listaCategorie : null
                         , listaStrutture             : (id_listaStrutture.size() > 0) ? id_listaStrutture : null
        ].findAll { it.value != null };

        if (controlloRegolaritaDto.ambito == Determina.TIPO_OGGETTO) {
            return Determina.executeQuery("""
					select count (*)
					from Determina d 
					where (not exists (select n
										from ControlloRegolaritaDocumento n
									   where n.determina.id = d.id
										 and n.controlloRegolarita.tipoControlloRegolarita.id = :codiceTipoControlloRegolarita)
					)
					and d.dataEsecutivita is not null and d.valido = true"""
                    + ((controlloRegolaritaDto.dataEsecutivitaDal != null) ? " and TRUNC (d.dataEsecutivita) >= :dataEsecutivitaDal" : "")
                    + ((controlloRegolaritaDto.dataEsecutivitaAl != null) ? " and TRUNC (d.dataEsecutivita) <= :dataEsecutivitaAl" : "")
                    + ((id_listaRegistri.size() > 0) ? " and d.registroDetermina.codice in :listaRegistri" : "")
                    + ((id_listaCategorie.size() > 0) ? " and d.categoria.id in :listaCategorie" : "")
                    + ((id_listaTipologie.size() > 0) ? " and d.tipologia.id in :listaTipologie" : "")
                    + ((id_listaStrutture.size() > 0) ? " and exists (select ps from DeterminaSoggetto ps where ps.determina.id = d.id and ps.tipoSoggetto.codice = 'UO_PROPONENTE' and ps.unitaSo4.progr in (:listaStrutture))" : "")
                    + ((map.impegnoDiSpesa.equals("Si")) ? " and exists (select v from VistoParere v where d.id = v.determina.id and v.tipologia.contabile = true)" : "")
                    + " order by d.annoDetermina desc, d.numeroDetermina asc", parametri)

        }

        if (controlloRegolaritaDto.ambito == Delibera.TIPO_OGGETTO) {
            return Delibera.executeQuery("""
					select count (*)
					from Delibera d 
					where (not exists (select n
									    from ControlloRegolaritaDocumento n
									   where n.delibera.id = d.id
										 and n.controlloRegolarita.tipoControlloRegolarita.id = :codiceTipoControlloRegolarita)
					)
					and d.dataEsecutivita is not null and d.valido = true"""
                    + ((controlloRegolaritaDto.dataEsecutivitaDal != null) ? " and TRUNC (d.dataEsecutivita) >= :dataEsecutivitaDal" : "")
                    + ((controlloRegolaritaDto.dataEsecutivitaAl != null) ? " and TRUNC (d.dataEsecutivita) <= :dataEsecutivitaAl" : "")
                    + ((id_listaRegistri.size() > 0) ? " and d.registroDelibera.codice in :listaRegistri" : "")
                    + ((id_listaCategorie.size() > 0) ? " and d.propostaDelibera.categoria.id in :listaCategorie" : "")
                    + ((id_listaTipologie.size() > 0) ? " and d.propostaDelibera.tipologia.id in :listaTipologie" : "")
                    + ((id_listaStrutture.size() > 0) ? " and exists (select ps from PropostaDeliberaSoggetto ps where ps.propostaDelibera.id = d.propostaDelibera.id and ps.tipoSoggetto.codice = 'UO_PROPONENTE' and ps.unitaSo4.progr in (:listaStrutture))" : "")
                    + " order by d.annoDelibera desc, d.numeroDelibera asc", parametri);
        }

        return [0];
    }

    public def estraiDocumenti(ControlloRegolaritaDTO controlloRegolaritaDto, def map, int size) {
        if (controlloRegolaritaDto.tipoControlloRegolarita == null) {
            return [];
        }

        List id_listaTipologie = map.tipologie*.id
        List id_listaRegistri = map.registri*.id
        List id_listaCategorie = map.categorie*.id
        List id_listaStrutture = map.strutture*.id
        // cerco tutti i documenti che non sono già stati inviati all'organo scelto:
        def parametri = [codiceTipoControlloRegolarita: controlloRegolaritaDto.tipoControlloRegolarita.id
                         , dataEsecutivitaDal         : controlloRegolaritaDto.dataEsecutivitaDal
                         , dataEsecutivitaAl          : controlloRegolaritaDto.dataEsecutivitaAl
                         , listaRegistri              : (id_listaRegistri.size() > 0) ? id_listaRegistri : null
                         , listaTipologie             : (id_listaTipologie.size() > 0) ? id_listaTipologie : null
                         , listaCategorie             : (id_listaCategorie.size() > 0) ? id_listaCategorie : null
                         , listaStrutture             : (id_listaStrutture.size() > 0) ? id_listaStrutture : null
                         , max                        : size
        ].findAll { it.value != null };

        if (controlloRegolaritaDto.ambito == Determina.TIPO_OGGETTO) {
            return Determina.executeQuery("""
					select d
					from Determina d 
					where (not exists (select n
										from ControlloRegolaritaDocumento n
									   where n.determina.id = d.id
										 and n.controlloRegolarita.tipoControlloRegolarita.id = :codiceTipoControlloRegolarita)
					)
					and d.dataEsecutivita is not null and d.valido = true"""
                    + ((controlloRegolaritaDto.dataEsecutivitaDal != null) ? " and TRUNC (d.dataEsecutivita) >= :dataEsecutivitaDal" : "")
                    + ((controlloRegolaritaDto.dataEsecutivitaAl != null) ? " and TRUNC (d.dataEsecutivita) <= :dataEsecutivitaAl" : "")
                    + ((id_listaRegistri.size() > 0) ? " and d.registroDetermina.codice in :listaRegistri" : "")
                    + ((id_listaCategorie.size() > 0) ? " and d.categoria.id in :listaCategorie" : "")
                    + ((id_listaTipologie.size() > 0) ? " and d.tipologia.id in :listaTipologie" : "")
                    + ((id_listaStrutture.size() > 0) ? " and exists (select ps from DeterminaSoggetto ps where ps.determina.id = d.id and ps.tipoSoggetto.codice = 'UO_PROPONENTE' and ps.unitaSo4.progr in (:listaStrutture))" : "")
                    + ((map.impegnoDiSpesa.equals("Si")) ? " and exists (select v from VistoParere v where d.id = v.determina.id and v.tipologia.contabile = true)" : "")
                    + " order by dbms_random.value()", parametri)

        }

        if (controlloRegolaritaDto.ambito == Delibera.TIPO_OGGETTO) {
            return Delibera.executeQuery("""
					select d
					from Delibera d 
					where (not exists (select n
									    from ControlloRegolaritaDocumento n
									   where n.delibera.id = d.id
										 and n.controlloRegolarita.tipoControlloRegolarita.id = :codiceTipoControlloRegolarita)
					)
					and d.dataEsecutivita is not null and d.valido = true"""
                    + ((controlloRegolaritaDto.dataEsecutivitaDal != null) ? " and TRUNC (d.dataEsecutivita) >= :dataEsecutivitaDal" : "")
                    + ((controlloRegolaritaDto.dataEsecutivitaAl != null) ? " and TRUNC (d.dataEsecutivita) <= :dataEsecutivitaAl" : "")
                    + ((id_listaRegistri.size() > 0) ? " and d.registroDelibera.codice in :listaRegistri" : "")
                    + ((id_listaCategorie.size() > 0) ? " and d.propostaDelibera.categoria.id in :listaCategorie" : "")
                    + ((id_listaTipologie.size() > 0) ? " and d.propostaDelibera.tipologia.id in :listaTipologie" : "")
                    + ((id_listaStrutture.size() > 0) ? " and exists (select ps from PropostaDeliberaSoggetto ps where ps.propostaDelibera.id = d.propostaDelibera.id and ps.tipoSoggetto.codice = 'UO_PROPONENTE' and ps.unitaSo4.progr in (:listaStrutture))" : "")
                    + " order by dbms_random.value()", parametri);
        }

        return [];
    }

    public ControlloRegolarita inviaNotifiche(ControlloRegolaritaDTO controlloRegolarita) {
        ControlloRegolarita o = controlloRegolarita.domainObject;

        if (o.stato == ControlloRegolarita.STATO_INVIATO) {
            throw new AttiRuntimeException("Notifica già inviata. Non è possibile rimandarla.")
        }

        Notifica selectedNotifica = Notifica.perTipo(TipoNotifica.CONTROLLO_REGOLARITA).get();

        if (selectedNotifica == null) {
            throw new AttiRuntimeException("Attenzione: non è possibile inviare la notifica perché non c'è nessuna notifica configurata per il Controllo di Regolarità!");
        }

        o.stato = "INVIATA";
        o.save()

        notificheService.notifica(selectedNotifica, o);

        return o;
    }

    public ControlloRegolaritaDTO salva(ControlloRegolaritaDTO dto, List documenti) {
        ControlloRegolarita controlloRegolarita
        if (dto.id > 0) {
            ControlloRegolarita o = dto.domainObject;
            // se ho già inviato la notifica, non faccio nulla.
            if (o?.stato == ControlloRegolarita.STATO_INVIATO) {
                return dto;
            }
            controlloRegolarita = ControlloRegolarita.get(dto.id)
        } else {
            controlloRegolarita = new ControlloRegolarita()
        }

        controlloRegolarita.valido = true;
        controlloRegolarita.ambito = dto.ambito
        controlloRegolarita.tipoControlloRegolarita = dto.tipoControlloRegolarita.domainObject
        controlloRegolarita.tipoRegistro = dto.tipoRegistro?.domainObject
        controlloRegolarita.dataEsecutivitaDal = dto.dataEsecutivitaDal
        controlloRegolarita.dataEsecutivitaAl = dto.dataEsecutivitaAl
        controlloRegolarita.attiDaEstrarre = dto.attiDaEstrarre
        controlloRegolarita.percentuale = dto.percentuale
        controlloRegolarita.stato = dto.stato
        controlloRegolarita.criteriRicerca = dto.criteriRicerca
        controlloRegolarita.totaleAtti = dto.totaleAtti
        controlloRegolarita.modelloTesto = dto.modelloTesto?.domainObject
        controlloRegolarita.dataEstrazione = dto.dataEstrazione
        controlloRegolarita.save()

        for (def documento : documenti) {
            ControlloRegolaritaDocumento controlloRegolaritaDocumento
            if (documento.id > 0) {
                controlloRegolaritaDocumento = ControlloRegolaritaDocumento.get(documento.id)
            } else {
                controlloRegolaritaDocumento = new ControlloRegolaritaDocumento()
            }

            controlloRegolaritaDocumento.determina = documento.determina?.domainObject
            controlloRegolaritaDocumento.delibera = documento.delibera?.domainObject
            controlloRegolaritaDocumento.note = documento.note
            controlloRegolaritaDocumento.notificata = documento.notificata != null ? documento.notificata : false
            controlloRegolaritaDocumento.esitoControlloRegolarita = documento.esitoControlloRegolarita?.domainObject
            controlloRegolaritaDocumento.controlloRegolarita = controlloRegolarita
            controlloRegolaritaDocumento.save()
        }

        controlloRegolarita.save()
        return controlloRegolarita.toDTO();
    }

    private def getListaDocumenti(ControlloRegolaritaDTO controlloRegolaritaDto) {
        return ControlloRegolaritaDocumento.createCriteria().list {
            eq("controlloRegolarita.id", controlloRegolaritaDto.id)
            fetchMode("esitoControlloRegolarita", FetchMode.JOIN)
            if (controlloRegolaritaDto.ambito.equals(Determina.TIPO_OGGETTO)) {
                fetchMode("determina", FetchMode.JOIN)
                determina {
                    order("annoDetermina", "desc")
                    order("numeroDetermina", "asc")
                }
            }

            if (controlloRegolaritaDto.ambito.equals(Delibera.TIPO_OGGETTO)) {
                fetchMode("delibera", FetchMode.JOIN)
                delibera {
                    order("annoDelibera", "desc")
                    order("numeroDelibera", "asc")
                }
            }
        }
    }

    private def getListaDocumentiPerFirmatario(ControlloRegolaritaDTO controlloRegolaritaDto, def firmatario) {
        return ControlloRegolaritaDocumento.createCriteria().list {
            eq("controlloRegolarita.id", controlloRegolaritaDto.id)
            eq("notificata", false)
            isNotNull("esitoControlloRegolarita")
            if (controlloRegolaritaDto.ambito.equals(Determina.TIPO_OGGETTO)) {
                createAlias("determina", "dete", CriteriaSpecification.LEFT_JOIN)
                createAlias("dete.firmatari", "f", CriteriaSpecification.LEFT_JOIN)
                eq("f.firmatario.id", firmatario.id)
            }

            if (controlloRegolaritaDto.ambito.equals(Delibera.TIPO_OGGETTO)) {
                createAlias("delibera", "deli", CriteriaSpecification.LEFT_JOIN)
                createAlias("deli.firmatari", "f", CriteriaSpecification.LEFT_JOIN)
                eq("f.firmatario.id", firmatario.id)
            }
        }
    }


    private def getListaFirmatari(ControlloRegolaritaDTO controlloRegolaritaDto) {
        return ControlloRegolaritaDocumento.createCriteria().list {
            eq("controlloRegolarita.id", controlloRegolaritaDto.id)
            eq("notificata", false)
            fetchMode("esitoControlloRegolarita", FetchMode.JOIN)
            isNotNull("esitoControlloRegolarita")
            if (controlloRegolaritaDto.ambito.equals(Determina.TIPO_OGGETTO)) {
                fetchMode("determina", FetchMode.JOIN)
                fetchMode("determina.firmatari", FetchMode.JOIN)
                fetchMode("determina.firmatari.firmatario", FetchMode.JOIN)
                projections {
                    determina { firmatari { property "firmatario" } }
                }

            }

            if (controlloRegolaritaDto.ambito.equals(Delibera.TIPO_OGGETTO)) {
                fetchMode("delibera", FetchMode.JOIN)
                fetchMode("delibera.firmatari", FetchMode.JOIN)
                fetchMode("delibera.firmatari.firmatario", FetchMode.JOIN)
                projections {
                    delibera { firmatari { property "firmatario" } }
                }
            }
        }.unique { a, b -> a.id <=> b.id }
    }


    public void assegnaEsito(ControlloRegolaritaDocumentoDTO controlloRegolaritaDocumentoDto, EsitoControlloRegolaritaDTO esitoControlloRegolaritaDto, String note) {
        ControlloRegolaritaDocumento controlloRegolaritaDocumento = controlloRegolaritaDocumentoDto.domainObject
        controlloRegolaritaDocumento.esitoControlloRegolarita = esitoControlloRegolaritaDto.domainObject
        controlloRegolaritaDocumento.note = note
        controlloRegolaritaDocumento.notificata = false
        controlloRegolaritaDocumento.save()
    }

    public void chiudiControllo(ControlloRegolaritaDTO controlloRegolaritaDto) {
        ControlloRegolarita controlloRegolarita = controlloRegolaritaDto.domainObject
        controlloRegolarita.stato = ControlloRegolarita.STATO_CHIUSO;
        controlloRegolarita.save()
    }

    public def calcolaListaDocumentiRandom(ControlloRegolaritaDTO controlloRegolarita, def criteriRicerca) {
        def size = cercaDocumenti(controlloRegolarita, criteriRicerca)[0]
        def lista = []
        if (size > 0) {
            controlloRegolarita.totaleAtti = size
            double numero = controlloRegolarita.percentuale ? size * (controlloRegolarita.attiDaEstrarre / 100) : controlloRegolarita.attiDaEstrarre;
            int number = numero.toInteger()
            if (numero % 1 > 0) {
                number++;
            }
            lista = estraiDocumenti(controlloRegolarita, criteriRicerca, number)
            lista = lista.sort { a, b ->
                if (a instanceof Delibera) {
                    a.annoDelibera <=> b.annoDelibera ?: a.numeroDelibera <=> b.numeroDelibera
                } else if (a instanceof Determina) {
                    a.annoDetermina <=> b.annoDetermina ?: a.numeroDetermina <=> b.numeroDetermina
                }
            }.collect {
                if (it instanceof Delibera) {
                    new ControlloRegolaritaDocumentoDTO(delibera: it.toDTO())
                } else if (it instanceof Determina) {
                    new ControlloRegolaritaDocumentoDTO(determina: it.toDTO())
                }
            }
        }
        return lista;
    }


    public void creaStampaRiassuntiva(ControlloRegolaritaDTO controlloRegolarita) {
        if (controlloRegolarita.modelloTesto == null) {
            Messagebox.show("Non è stato selezionato il modello di testo da utilizzare per la stampa riassuntiva del controllo di regolarità", "Attenzione", Messagebox.OK, Messagebox.EXCLAMATION)
            return;
        }

        def mappaParametri = [:]
        mappaParametri["id"] = controlloRegolarita.id

        InputStream testoPdf = gestioneTestiService.stampaUnione(controlloRegolarita.modelloTesto.domainObject, mappaParametri, GestioneTestiService.FORMATO_PDF)
        Filedownload.save(testoPdf, GestioneTestiService.getContentType(GestioneTestiService.FORMATO_PDF), "Stampa Riassuntiva.pdf")
    }


    public def notifica(ControlloRegolaritaDTO controlloRegolarita, Notifica notifica) {
        log.debug("NotificaControlloRegolarita: preparazione notifiche per controllo " + controlloRegolarita?.id)
        boolean erroreInvio = false
        def firmatari = getListaFirmatari(controlloRegolarita)
        String elenco_firmatari_vuoti = "\n"
        for (firmatario in firmatari) {
            def sottolista = getListaDocumentiPerFirmatario(controlloRegolarita, firmatario)
            As4SoggettoCorrente soggetto = As4SoggettoCorrente.findByUtenteAd4(firmatario);

            if (soggetto?.indirizzoWeb?.length() > 0) {
                SoggettoNotifica soggettoNotifica = new SoggettoNotifica(utente: firmatario, email: soggetto?.indirizzoWeb, soggetto: soggetto);
                try {
                    log.debug("NotificaControlloRegolarita: invio della notifica")
                    notificheService.notifica(notifica, sottolista, [soggettoNotifica])
                    for (controlloRegolaritaDocumento in sottolista) {
                        log.debug("NotificaControlloRegolarita: inviata notifica per il documento " + controlloRegolaritaDocumento?.id)
                        controlloRegolaritaDocumento.notificata = true;
                        controlloRegolaritaDocumento.save()
                    }
                } catch (Throwable t) {
                    log.error(t)
                    throw t;
                }
            } else {
                erroreInvio = true
                elenco_firmatari_vuoti += firmatario.nominativoSoggetto + "\n"
                log.warn("NotificaControlloRegolarita: l'utente firmatario " + firmatario?.id + " è privo di email, quindi non è possibile inviare la notifica.")
            }
        }
        return [erroreInvio: erroreInvio, elencoFirmatariVuoti: elenco_firmatari_vuoti]
    }

}