package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.*
import it.finmatica.atti.dto.documenti.OrganoControlloNotificaDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.odg.dizionari.OrganoControllo
import it.finmatica.atti.odg.dizionari.TipoOrganoControllo
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import org.apache.commons.io.IOUtils

class OrganoControlloDTOService {

	NotificheService 		notificheService
	GestioneTestiService 	gestioneTestiService

	public OrganoControlloDTO salva(OrganoControlloDTO organoControlloDto) {
		OrganoControllo organoControllo = OrganoControllo.get(organoControlloDto.id)?:new OrganoControllo()
		if(organoControllo.version != organoControlloDto.version) {
			throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
		}

		organoControllo.tipo 			= TipoOrganoControllo.get(organoControlloDto.tipo.codice)
		organoControllo.descrizione 	= organoControlloDto.descrizione
		organoControllo.sequenza 		= organoControlloDto.sequenza
		organoControllo.valido 			= organoControlloDto.valido
		organoControllo.save (failOnError: true)

		organoControlloDto.id 			= organoControllo.id
		organoControlloDto.version		= organoControllo.version

		return organoControlloDto
	}

	public void elimina(OrganoControlloDTO organoControlloDto) {
		OrganoControllo.get(organoControlloDto.id).delete(failError: true)
	}

	public void eliminaNotifica(OrganoControlloNotificaDTO organoControlloNotificaDTO) {
		List notificheDocumenti = OrganoControlloNotificaDocumento.createCriteria().list() {
			eq ("organoControlloNotifica.id", organoControlloNotificaDTO.id)
			organoControlloNotifica {
				eq ("stato", OrganoControlloNotifica.STATO_ANTEPRIMA)
			}
		}

		for (OrganoControlloNotificaDocumento sel : notificheDocumenti) {
			sel.delete(failOnError: true);
		}

		OrganoControlloNotifica o = organoControlloNotificaDTO.getDomainObject();
		FileAllegato file = FileAllegato.get(organoControlloNotificaDTO.testo?.id)

		o.delete(failOnError: true)
		file?.delete(failOnError: true);
	}

	public def getListaDocumentiInviati (OrganoControlloNotificaDTO organoControlloNotificaDto) {
		return OrganoControlloNotificaDocumento.createCriteria().list {
			projections {
				if (organoControlloNotificaDto.ambito.equals(OrganoControlloNotifica.AMBITO_DETERMINA)) {
					property ("determina")
				}

				if (organoControlloNotificaDto.ambito.equals(OrganoControlloNotifica.AMBITO_DELIBERA)) {
					property ("delibera")
				}
			}

			eq ("organoControlloNotifica.id", organoControlloNotificaDto.id)

			if (organoControlloNotificaDto.ambito.equals(OrganoControlloNotifica.AMBITO_DETERMINA)) {
				determina {
					order ("annoDetermina", 	"desc")
					order ("numeroDetermina", 	"asc")
				}
			}

			if (organoControlloNotificaDto.ambito.equals(OrganoControlloNotifica.AMBITO_DELIBERA)) {
				delibera {
					order ("annoDelibera", 		"desc")
					order ("numeroDelibera", 	"asc")
				}
			}
		}
	}

	public def cercaDocumenti (OrganoControlloNotificaDTO organoControlloNotificaDto) {
		if (organoControlloNotificaDto.tipoOrganoControllo == null) {
			return [];
		}

		// cerco tutti i documenti che non sono già stati inviati all'organo scelto:
		def parametri = [   idNotifica: (long)(organoControlloNotificaDto.id?:-1)
						  , codiceTipoOrganoControllo: organoControlloNotificaDto.tipoOrganoControllo.codice
		                  , dataAdozioneDal:organoControlloNotificaDto.dataAdozioneDal
		                  , dataAdozioneAl: organoControlloNotificaDto.dataAdozioneAl
		                  , dataPubblDal: 	organoControlloNotificaDto.dataPubblicazioneDal
		                  , dataPubblAl: 	organoControlloNotificaDto.dataPubblicazioneAl
		                  , tipoRegistro: 	(organoControlloNotificaDto.tipoRegistro?.codice == null ||
							  organoControlloNotificaDto.tipoRegistro.codice.length()==0)?null:organoControlloNotificaDto.tipoRegistro.codice].findAll {it.value != null};

//										 and :idNotifica > 0 and n.organoControlloNotifica.id <> :idNotifica
		if (organoControlloNotificaDto.ambito == OrganoControlloNotifica.AMBITO_DETERMINA) {
			return Determina.executeQuery("""
					select d
					from Determina d
					where (not exists (select n
										from OrganoControlloNotificaDocumento n
									   where n.determina.id = d.id
										 and n.organoControlloNotifica.tipoOrganoControllo.codice = :codiceTipoOrganoControllo)
					or
						exists (select n
										from OrganoControlloNotificaDocumento n
									   where n.determina.id = d.id
										 and n.organoControlloNotifica.tipoOrganoControllo.codice = :codiceTipoOrganoControllo
										 and n.organoControlloNotifica.id = :idNotifica)
					)
					and d.dataPubblicazione  is not null
					and d.dataEsecutivita	 is not null
					and tipologia.notificaOrganiControllo = true"""
					+ ((organoControlloNotificaDto.dataAdozioneDal 		!= null) ? " and TRUNC (d.dataEsecutivita) >= :dataAdozioneDal" : "")
					+ ((organoControlloNotificaDto.dataAdozioneAl  		!= null) ? " and TRUNC (d.dataEsecutivita) <= :dataAdozioneAl"  : "")
					+ ((organoControlloNotificaDto.dataPubblicazioneDal != null) ? " and TRUNC (d.dataPubblicazione) >= :dataPubblDal" 	: "")
					+ ((organoControlloNotificaDto.dataPubblicazioneAl  != null) ? " and TRUNC (d.dataPubblicazione) <= :dataPubblAl" 	: "")
					+ ((organoControlloNotificaDto.tipoRegistro?.codice	!= null &&
						organoControlloNotificaDto.tipoRegistro.codice.length()>0) ? " and d.registroDetermina.codice = :tipoRegistro" : "")
					+ " order by d.annoDetermina desc, d.numeroDetermina asc", parametri);
		}

		if (organoControlloNotificaDto.ambito == OrganoControlloNotifica.AMBITO_DELIBERA) {
			return Delibera.executeQuery("""
					select d
					from Delibera d
					where (not exists (select n
									    from OrganoControlloNotificaDocumento n
									   where n.delibera.id = d.id
										 and n.organoControlloNotifica.tipoOrganoControllo.codice = :codiceTipoOrganoControllo)
					or
						exists (select n
										from OrganoControlloNotificaDocumento n
									   where n.delibera.id = d.id
										 and n.organoControlloNotifica.tipoOrganoControllo.codice = :codiceTipoOrganoControllo
										 and n.organoControlloNotifica.id = :idNotifica)
					)
					and d.dataPubblicazione  	is not null
					and d.dataAdozione  		is not null
					and propostaDelibera.tipologia.notificaOrganiControllo = true"""
					+ ((organoControlloNotificaDto.dataAdozioneDal 		!= null) ? " and TRUNC (d.dataAdozione) >= :dataAdozioneDal" : "")
					+ ((organoControlloNotificaDto.dataAdozioneAl  		!= null) ? " and TRUNC (d.dataAdozione) <= :dataAdozioneAl"  : "")
					+ ((organoControlloNotificaDto.dataPubblicazioneDal != null) ? " and TRUNC (d.dataPubblicazione) >= :dataPubblDal" 	: "")
					+ ((organoControlloNotificaDto.dataPubblicazioneAl  != null) ? " and TRUNC (d.dataPubblicazione) <= :dataPubblAl" 	: "")
					+ ((organoControlloNotificaDto.tipoRegistro?.codice	!= null &&
						organoControlloNotificaDto.tipoRegistro.codice.length()>0) ? " and d.registroDelibera.codice = :tipoRegistro" : "")
					+ " order by d.annoDelibera desc, d.numeroDelibera asc", parametri);
		}

		// se non ho l'ambito...
		return [];
	}

	public void generaTestoNotifica(OrganoControlloNotifica organoControlloNotifica, GestioneTestiModello modello, String formato) {
		InputStream testo = gestioneTestiService.stampaUnione(modello, [id:organoControlloNotifica.id], formato, true)
		if (organoControlloNotifica.testo == null) {
			organoControlloNotifica.testo = new FileAllegato(nome:"notifica.${formato}", contentType: GestioneTestiService.getContentType(formato), allegato: IOUtils.toByteArray(testo))
			organoControlloNotifica.testo.save()
		}
		organoControlloNotifica.save()
	}

	public OrganoControlloNotifica inviaNotifiche (OrganoControlloNotificaDTO organoControlloNotifica) {
		OrganoControlloNotifica o = organoControlloNotifica.domainObject;

		if (o.stato == OrganoControlloNotifica.STATO_INVIATA) {
			throw new AttiRuntimeException ("Notifica già inviata. Non è possibile rimandarla.")
		}

		Notifica selectedNotifica = Notifica.perTipo(TipoNotifica.ORGANI_CONTROLLO).get();

		if (selectedNotifica == null) {
			throw new AttiRuntimeException ("Attenzione: non è possibile inviare la notifica perché non c'è nessuna notifica configurata per gli Organi di Controllo!");
		}

		o.stato = OrganoControlloNotifica.STATO_INVIATA;
		o.save()

		notificheService.notifica (selectedNotifica, o);

		return o;
	}

	public OrganoControlloNotificaDTO preparaNotifiche (OrganoControlloNotificaDTO dto, List documenti, GestioneTestiModelloDTO modelloTesto) {
		if (dto.id > 0) {
			OrganoControlloNotifica o = dto.domainObject;
			// se ho già inviato la notifica, non faccio nulla.
			if (o?.stato == OrganoControlloNotifica.STATO_INVIATA) {
				return dto;
			}
		}

		OrganoControlloNotifica organoControlloNotifica = new OrganoControlloNotifica()
		organoControlloNotifica.valido				= true;
		organoControlloNotifica.ambito 				= dto.ambito
		organoControlloNotifica.tipoOrganoControllo = dto.tipoOrganoControllo.domainObject
		organoControlloNotifica.tipoRegistro		= dto.tipoRegistro?.domainObject
		organoControlloNotifica.dataPubblicazioneDal= dto.dataPubblicazioneDal
		organoControlloNotifica.dataPubblicazioneAl = dto.dataPubblicazioneAl
		organoControlloNotifica.dataAdozioneDal 	= dto.dataAdozioneDal
		organoControlloNotifica.dataAdozioneAl 		= dto.dataAdozioneAl
		organoControlloNotifica.stato 				= OrganoControlloNotifica.STATO_ANTEPRIMA
		organoControlloNotifica.save()

		for (def documento : documenti) {
			OrganoControlloNotificaDocumento organoControlloNotificaDocumento = new OrganoControlloNotificaDocumento()
			organoControlloNotificaDocumento.organoControlloNotifica = organoControlloNotifica

			def doc = documento.domainObject;
			if (doc instanceof Delibera) {
				organoControlloNotificaDocumento.delibera = doc
			} else if (doc instanceof Determina) {
				organoControlloNotificaDocumento.determina = doc
			} else {
				throw new AttiRuntimeException ("Tipo di documento non riconosciuto: ${doc}");
			}

			organoControlloNotificaDocumento.save()
		}

		organoControlloNotifica.save()
		generaTestoNotifica(organoControlloNotifica, modelloTesto.domainObject, Impostazioni.FORMATO_DEFAULT.valore);
		return organoControlloNotifica.toDTO();
	}
}