package it.finmatica.atti.documenti

import grails.util.GrailsNameUtils
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.competenze.VistoParereCompetenze
import it.finmatica.atti.documenti.storico.VistoParereStorico
import it.finmatica.atti.documenti.tipologie.ParametroTipologia
import it.finmatica.atti.documenti.tipologie.ParametroTipologiaService
import it.finmatica.atti.documenti.tipologie.TipoVistoParere
import it.finmatica.atti.dto.commons.FileAllegatoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CaratteristicaTipologiaService
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.ws.dati.Soggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.FetchMode

class VistoParereService {

	CaratteristicaTipologiaService 	caratteristicaTipologiaService
	AttiGestoreCompetenze 			gestoreCompetenze
	GrailsApplication 				grailsApplication
	WkfIterService 					wkfIterService
	IGestoreFile 					gestoreFile
    ParametroTipologiaService       parametroTipologiaService

	VistoParere creaVistoParere (def documento, TipoVistoParere tipoVistoParere, boolean automatico, Ad4Utente firmatario=null, So4UnitaPubb unita=null) {
		VistoParere vp 	= new VistoParere()
		// aggiungo il visto al documento
		documento.addToVisti(vp)

		vp.stato 		= StatoDocumento.DA_PROCESSARE
		vp.esito 		= EsitoVisto.DA_VALUTARE
		vp.automatico 	= automatico
		vp.tipologia  	= tipoVistoParere
		vp.modelloTesto	= tipoVistoParere.modelloTesto

		Map<String, it.finmatica.atti.zk.SoggettoDocumento> soggetti = [:]
		if (unita != null) {
			soggetti[TipoSoggetto.UO_DESTINATARIA] = new it.finmatica.atti.zk.SoggettoDocumento(TipoSoggetto.get(TipoSoggetto.UO_DESTINATARIA), unita)
		}
		if (firmatario != null) {
			soggetti[TipoSoggetto.FIRMATARIO] = new it.finmatica.atti.zk.SoggettoDocumento(TipoSoggetto.get(TipoSoggetto.FIRMATARIO), firmatario, null)
		}

		// se mi mancano unità o firmatario, li ricalcolo. Se invece li ho entrambi, non è necessario ricalcolare.
		if (unita == null || firmatario == null) {
			caratteristicaTipologiaService.calcolaSoggetti(vp, vp.tipologia.caratteristicaTipologia, soggetti)
		}

		vp.unitaSo4 	= soggetti[TipoSoggetto.UO_DESTINATARIA]?.unita?.domainObject
		vp.firmatario 	= soggetti[TipoSoggetto.FIRMATARIO]?.utente?.domainObject

		// salvo con flush per poter inserire il file
		vp.save()

		// copio le competenze del documento principale:
		copiaCompetenzeDocumentoPrincipale (vp)

		return vp
	}

	void allineaFirmatarioDocumentoPrincipale (def documentoPrincipale, Ad4Utente utenteDocumentoPrincipale) {
		// cerco quei soggetti di tipoSoggettoDocumentoPrincipale che appartengono a documentoPrincipale e che non sono uguali al firmatario
		// se questo ha come metodo di calcolo il getDirigenteDocumentoPrincipale.
		def visti = VistoParere.createCriteria().list {
			eq (GrailsNameUtils.getPropertyName(GrailsHibernateUtil.unwrapIfProxy(documentoPrincipale).class), documentoPrincipale)
			eq ("valido", true)
			ne ("stato", StatoDocumento.CONCLUSO)
			or {
				isNull("firmatario")
				ne ("firmatario", utenteDocumentoPrincipale)
			}
			tipologia {
				caratteristicaTipologia {
					caratteristicheTipiSoggetto {
						eq ("tipoSoggetto.codice", TipoSoggetto.FIRMATARIO)
						or {
							regolaCalcoloDefault {
								eq ("nomeMetodo", "getDirigenteDocumentoPrincipale")
							}
							regolaCalcoloLista   {
								eq ("nomeMetodo", "getDirigenteDocumentoPrincipale")
							}
						}
					}
				}
			}
		}

		for (VistoParere visto : visti) {
			visto.firmatario = utenteDocumentoPrincipale
			visto.save()
		}
	}

	void allineaUnitaDocumentoPrincipale (def documentoPrincipale, So4UnitaPubb unitaDocumentoPrincipale) {
		// cerco quei soggetti di tipoSoggettoDocumentoPrincipale che appartengonoa documentoPrincipale e che non sono uguali al firmatario
		// se questo ha come metodo di calcolo il getDirigenteDocumentoPrincipale.
		def visti = VistoParere.createCriteria().list {
			eq (GrailsNameUtils.getPropertyName(GrailsHibernateUtil.unwrapIfProxy(documentoPrincipale).class), documentoPrincipale)
			eq ("valido", true)
			ne ("stato", StatoDocumento.CONCLUSO)
			or {
				isNull("unitaSo4.progr")
				ne ("unitaSo4.progr", unitaDocumentoPrincipale.progr)
			}
			tipologia {
				caratteristicaTipologia {
					caratteristicheTipiSoggetto {
						eq ("tipoSoggetto.codice", TipoSoggetto.UO_DESTINATARIA)
						or {
							regolaCalcoloDefault {
								eq ("nomeMetodo", "getUnitaProponenteDocumentoPrincipale")
							}
							regolaCalcoloLista   {
								eq ("nomeMetodo", "getUnitaProponenteDocumentoPrincipale")
							}
						}
					}
				}
			}
		}

		for (VistoParere visto : visti) {
			visto.unitaSo4 = unitaDocumentoPrincipale

			if (visto.tipologia.caratteristicaTipologia.caratteristicheTipiSoggetto.find {it.tipoSoggetto.codice == TipoSoggetto.FIRMATARIO}?.tipoSoggettoPartenza?.codice?.equals(TipoSoggetto.UO_DESTINATARIA)) {
				visto.firmatario = null;

				Map<String, it.finmatica.atti.zk.SoggettoDocumento> soggetti = [:]
				soggetti[TipoSoggetto.UO_DESTINATARIA] = new it.finmatica.atti.zk.SoggettoDocumento(TipoSoggetto.get(TipoSoggetto.UO_DESTINATARIA), unitaDocumentoPrincipale)

				caratteristicaTipologiaService.calcolaSoggetti(visto, visto.tipologia.caratteristicaTipologia, soggetti)

				visto.firmatario = soggetti[TipoSoggetto.FIRMATARIO]?.utente?.domainObject
			}

			visto.save()
		}
	}

	void copiaCompetenzeDocumentoPrincipale (VistoParere vistoParere) {
		gestoreCompetenze.copiaCompetenze(vistoParere.documentoPrincipale, vistoParere, false)
	}

	def creaVistiAutomatici (IDocumento d) {
		// per ogni tipologia visto, creo i suoi visti automatici a meno che non esistano già e siano "validi".
		for (TipoVistoParere tvp : d.tipologiaDocumento.tipiVisto) {

			boolean esisteVistoParere = false

			for (VistoParere vp : d.visti) {
				// controllo se esiste già un visto con stesso codice, automatico e non annullato:
				if (vp.tipologia.codice == tvp.codice &&
					vp.automatico == true &&
					StatoDocumento.ANNULLATO != vp.stato && vp.valido) {
					esisteVistoParere = true
					break
				}
			}

			// se non esiste un visto per questa tipologia, lo creo:
			if (!esisteVistoParere) {
				creaVistoParere (d, tvp, true)
			}
		}

		d.save()

		return d
	}

	void richiediVisti (def d, String codiceVisto) {
		// richiedo ciascun visto con il codice specificato
		for (VistoParere visto : d.visti) {
			if (visto.valido && visto.tipologia.codice == codiceVisto) {
				if (d instanceof Delibera && visto.tipologia.progressivoCfgIterDelibera != null){
					richiediVisto (d, visto, visto.tipologia.progressivoCfgIterDelibera)
				}
				else {
					richiediVisto (d, visto, visto.tipologia.progressivoCfgIter)
				}
			}
		}
	}
	
	void richiediVisto (VistoParere visto) {
		richiediVisto(visto.documentoPrincipale, visto, visto.tipologia.progressivoCfgIter)
	}

	void richiediVisto (def documentoPrincipale, VistoParere visto, long progressivoCfgIter) {
		visto.stato = StatoDocumento.PROCESSATO
		visto.save()

		if (visto.iter == null) {
			wkfIterService.istanziaIter(WkfCfgIter.getIterIstanziabile(progressivoCfgIter).get(), visto)
		} else {
			wkfIterService.sbloccaDocumento(visto)
		}

		visto.save()

		// segno il codice del visto richiesto nella determina:
		documentoPrincipale.addCodiceVistoTrattato(visto.tipologia.codice)
		documentoPrincipale.save()
	}

	Collection<TipoVistoParere> getListaTipologiePossibili (def proposta) {
		// leggo i possibili visti gestiti dall'iter (tramite i parametri della tipologia di determina)
		def codiciVisto = ParametroTipologia.getValoriParametri(proposta.tipologiaDocumento, "CODICE_VISTO")

		// scarto i visti contabili se sono già stati aggiunti
		codiciVisto -= getVistoContabile (proposta)?.tipologia?.codice

		// scarto i visti che sono già stati trattati dal flusso
		codiciVisto -= proposta?.getListaCodiciVistiTrattati()

		if (!(codiciVisto?.size() > 0)) {
			return []
		}

		return TipoVistoParere.createCriteria().list {
			'in' ("codice", codiciVisto)
			order ("descrizione", "asc")
			fetchMode("caratteristicaTipologia", FetchMode.JOIN)
			fetchMode("modelloTesto", 			 FetchMode.JOIN)
		}
	}

	VistoParere getVistoContabile (IProposta proposta) {
		return VistoParere.createCriteria().get {
			eq ((proposta instanceof Determina)?"determina":"propostaDelibera",	proposta)
			eq ("valido", true)
			tipologia {
				eq "contabile", true
			}
		}
	}
	
	VistoParere getVisto (IProposta proposta, String codice) {
		return VistoParere.createCriteria().get {
			eq ((proposta instanceof Determina)?"determina":"propostaDelibera",	proposta)
			eq ("valido", true)
			eq ("automatico", true)
			tipologia {
				eq ("codice", codice)
			}
		}
	}

	boolean esisteAlmenoUnVisto (Collection<VistoParere> visti, String codiceVisto = null, StatoDocumento stato = null, EsitoVisto esito = null, Boolean contabile = null) {
		for (VistoParere visto : visti) {
			if (visto.valido &&
				(contabile 	 == null || visto.tipologia.contabile == contabile)	&&
				(codiceVisto == null || visto.tipologia.codice == codiceVisto)  &&
				(stato 		 == null || visto.stato == stato) 					&&
				(esito 		 == null ||	visto.esito == esito)) {
				return true
			}
		}

		return false
	}

	/**
	 * Ritorna TRUE se tutti i visti con il codice richiesto soddisfano i requisiti. Deve essere presente almeno un visto., FALSE altrimenti.
	 * @param visti
	 * @return
	 */
	boolean tuttiVistiSono (Collection<VistoParere> visti, String codiceVisto = null, StatoDocumento stato = null, def esito = null, Boolean contabile = null) {
		boolean almenoUnoValido = false
		for (VistoParere visto : visti) {
			if (visto.valido &&
				(contabile 	 == null || visto.tipologia.contabile == contabile)	&&
				(codiceVisto == null || visto.tipologia.codice == codiceVisto)) {
				if ((stato 		 == null || visto.stato == stato)	&&
					(esito 		 == null ||	(esito instanceof EsitoVisto && visto.esito == esito) 
										 || (esito instanceof ArrayList && esito.contains(visto.esito)))) {
					almenoUnoValido = true
					continue
				} else {
					return false
				}
			}
		}

		return almenoUnoValido
	}

	void elimina (VistoParere vistoParere) {
		// se il visto ha già un iter, allora lo metto solo come valido "false", altrimenti lo elimino.
		if (vistoParere.iter != null) {
			vistoParere.valido = false
			vistoParere.save()
			
			wkfIterService.terminaIter(vistoParere.iter)
			return
		}
		
		def documento 	= vistoParere.getDocumentoPrincipale()
		VistoParere vp 	= documento.visti.find { it.id == vistoParere.id }
		documento.removeFromVisti(vp)
		VistoParereCompetenze.findAllByVistoParere(vp)*.delete()
		VistoParereStorico.findAllByIdVistoParere(vp.id)*.delete()
		Firmatario.findAllByVistoParere(vp)*.delete()
		vp.delete()
	}

	/**
	 * Verifica la presenza dei firmatari per tutti i visti/pareri validi di un atto
	 * @param d Documento da controllare
	 * @return True se tutti i firmatari sono presenti, false altrimenti
	 */
	boolean verificaFirmatariVistiPareri (IDocumento d) {
		for (VistoParere visto : d.visti) {
			if (visto.valido && visto.firmatario == null) {
				return false
			}
		}
		return true
	}

	/**
	 * Sposta gli allegati da un visto relativo allo stesso documento principale concluso al visto
	 * @param vistoParere Visto/Parere in cui trasferire gli allegati
	 */
	void spostaAllegati (VistoParere vistoParere) {
		def precedenti = VistoParere.createCriteria().list {
			eq ((vistoParere.documentoPrincipale instanceof Determina) ? "determina" : "propostaDelibera", vistoParere.documentoPrincipale)
			eq ("valido", true)
			eq ("stato", StatoDocumento.CONCLUSO)
			not {
				eq("id", vistoParere.id)
			}
		}

		for (VistoParere precedente : precedenti) {
			for (def allegato : precedente.allegati) {
				if (allegato.valido && (Allegato.ALLEGATO_SCHEDA_CONTABILE == allegato.codice || Allegato.ALLEGATO_MODIFICABILE == allegato.codice)) {
					allegato.vistoParere = vistoParere
					allegato.statoFirma  = StatoFirma.DA_FIRMARE
					allegato.save()
				}
			}
		}
	}

	/**
	 * Sposta gli allegati modificabili da un visto al documento principale
	 * @param vistoParere Visto/Parere in cui trasferire gli allegati
	 * @param sposta Indica se spostare un allegato o farne una copia
	 */
	void spostaAllegatiModificabili (VistoParere vistoParere, boolean sposta = true) {

		for (def allegatoModificabile : vistoParere.allegati) {
			if (Allegato.ALLEGATO_MODIFICABILE == allegatoModificabile.codice && allegatoModificabile.valido) {
				Allegato allegato	 = new Allegato()
				allegato.titolo   	 = "note della Ragioneria al testo della Proposta";
				if (sposta) {
					allegatoModificabile.valido = false;
					allegatoModificabile.save()
					allegato.idDocumentoEsterno	= allegatoModificabile.idDocumentoEsterno
				}

				allegato.statoFirma  = StatoFirma.DA_NON_FIRMARE;
				allegato.stampaUnica = false;
				allegato.riservato 	 = false;
				allegato.pubblicaCasaDiVetro = false
				allegato.pubblicaAlbo = false
				allegato.pubblicaVisualizzatore = false
				allegato.sequenza = vistoParere.documentoPrincipale.allegati.findAll({it.valido == true}).size() + 1

				for (def fileAllegato: allegatoModificabile.fileAllegati) {
					if (sposta) {
						allegato.addToFileAllegati(fileAllegato)
					}
					else {
						FileAllegato copia = new FileAllegato (nome: fileAllegato.nome, contentType: fileAllegato.contentType)
						copia.save(failOnError: true)
						gestoreFile.addFile(allegato, copia, gestoreFile.getFile(allegatoModificabile, fileAllegato))
						allegato.addToFileAllegati(copia)
					}
				}

				vistoParere.documentoPrincipale.addToAllegati(allegato);
				allegato.save();
				vistoParere.documentoPrincipale.save();

				// assegno le competenze al nuovo allegato:
				gestoreCompetenze.copiaCompetenze (vistoParere.documentoPrincipale, allegato, true);
			}
		}
	}

	/**
	 * Ritorna TRUE se tutti i visti validi associati al documento sono conclusi.
	 * Ritorna TRUE anche se il documento NON ha visti validi.
	 *
	 * @param documento
	 * @return
	 */
	boolean isTuttiVistiConclusi (def documento) {
		for (VistoParere vp : documento.visti) {
			if (vp.valido && vp.stato != StatoDocumento.CONCLUSO) {
				return false
			}
		}
		return true
	}

    Delibera creaPareriDelibera(Delibera delibera){
        def codiciVisti = parametroTipologiaService.getListaParametri ("tipoDelibera", delibera.tipologiaDocumento.id, delibera.tipologiaDocumento.progressivoCfgIterDelibera).findAll {it.codice == 'CODICE_VISTO' && it.valore != null}.valore

        for (String codiceVisto : codiciVisti){
            // recuper la tipologia collegata
            TipoVistoParere tipologia = TipoVistoParere.findByCodiceAndValido(codiceVisto, true)

            if (tipologia == null) {
                throw new AttiRuntimeException ("Non è possibile proseguire: è necessaria una tipologia di visto/parere valida con codice ${codiceVisto} come richiesto dalla tipologia '${delibera.tipologiaDocumento.titolo}'.")
            }

            def visti = VistoParere.createCriteria().list {
                eq(GrailsNameUtils.getPropertyName(GrailsHibernateUtil.unwrapIfProxy(delibera).class), delibera)
                eq("valido", true)
                eq("tipologia", tipologia)
            }
            if (visti.isEmpty()) {
                // creo il visto/parere
                creaVistoParere(delibera, tipologia, false)
            }
        }

    }
}
