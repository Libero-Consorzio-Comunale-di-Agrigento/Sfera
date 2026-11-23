package it.finmatica.atti.dto.documenti

import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dizionari.Email
import it.finmatica.atti.documenti.DestinatarioNotifica
import it.finmatica.atti.dto.dizionari.EmailDTO
import it.finmatica.atti.dto.dizionari.EmailDTOService
import it.finmatica.atti.dto.odg.SedutaStampaDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import org.hibernate.FetchMode

class DestinatarioNotificaDTOService {

    EmailDTOService emailDTOService

    void salvaDestinatario (def documentoDto, Email destinatario) {
        def documento = documentoDto.domainObject
        documento.addToDestinatariNotifiche(new DestinatarioNotifica(email: destinatario, tipoDestinatario: DestinatarioNotifica.TIPO_DESTINATARIO_ESTERNO, tipoNotifica: DestinatarioNotifica.TIPO_NOTIFICA_CONOSCENZA))
        documento.save()
        documentoDto.version = documento.version
    }

    void salvaDestinatario (def documentoDto, EmailDTO destinatario) {
        salvaDestinatario(documentoDto, destinatario.domainObject)
    }

    void salvaDestinatario (def documentoDto, As4SoggettoCorrenteDTO destinatario) {
        Email email = emailDTOService.getEmail(destinatario.domainObject)
        if (email == null) {
            throw new AttiRuntimeException("Non è possibile aggiungere un soggetto senza un indirizzo email.")
        }
        salvaDestinatario(documentoDto, email)
    }

    def salvaDestinatariInterni (def documentoDto, List<DestinatarioNotificaDTO> destinatariInterniDTO) {
        def documento = documentoDto.domainObject

        documento.destinatariNotifiche.findAll { it.tipoDestinatario == DestinatarioNotifica.TIPO_DESTINATARIO_INTERNO }.each {
            documento.removeFromDestinatariNotifiche(it)
            it.delete()
        }

        destinatariInterniDTO.each {
            DestinatarioNotifica d = new DestinatarioNotifica()
            d.tipoDestinatario = DestinatarioNotifica.TIPO_DESTINATARIO_INTERNO
            d.tipoNotifica = DestinatarioNotifica.TIPO_NOTIFICA_COMPETENZA
            d.utente = it.utente?.domainObject
            d.unitaSo4 = it.unitaSo4?.domainObject
            documento.addToDestinatariNotifiche(d)
        }

        documento.save()

        return documento.toDTO()
    }

    void eliminaDestinatarioNotifica (DestinatarioNotificaDTO destinatarioDTO) {
        destinatarioDTO.domainObject.delete()
    }

    def aggiungiDestinatarioEsterno (def documentoDto, DestinatarioNotificaDTO destinatarioDTO) {
        def documento = documentoDto.domainObject

        DestinatarioNotifica d = new DestinatarioNotifica()
        d.tipoDestinatario = DestinatarioNotifica.TIPO_DESTINATARIO_ESTERNO
        d.tipoNotifica = destinatarioDTO.tipoNotifica
        if ('PND'.equals(destinatarioDTO.tipoNotifica)) {
            d.soggetto = destinatarioDTO.soggetto.domainObject
            d.importo = destinatarioDTO.importo
            d.dataScadenza = destinatarioDTO.dataScadenza
            d.giorniScadenza = destinatarioDTO.giorniScadenza

            d.idRecapito = destinatarioDTO.idRecapito
            d.idContatto = destinatarioDTO.idContatto
            d.cognome = destinatarioDTO.cognome
            d.nome = destinatarioDTO.nome
            d.codiceFiscale = destinatarioDTO.codiceFiscale
            d.indirizzo = destinatarioDTO.indirizzo
            d.comune = destinatarioDTO.comune
            d.provincia = destinatarioDTO.provincia
            d.cap = destinatarioDTO.cap
            d.indirizzoEmail = destinatarioDTO.indirizzoEmail
        }
        else {
            d.email = destinatarioDTO.email?.id > 0 ? destinatarioDTO.email.getDomainObject() : new Email()
            if (!(destinatarioDTO.email?.id > 0)) {
                d.email.nome = destinatarioDTO.email.nome
                d.email.cognome = destinatarioDTO.email.cognome
                d.email.indirizzoEmail = destinatarioDTO.email.indirizzoEmail
                d.email.ragioneSociale = destinatarioDTO.email.ragioneSociale
                d.email.save()
            }
        }
        documento.addToDestinatariNotifiche(d)
        documento.save()

        return documento.toDTO()
    }

    def aggiungiDestinatarioPiTre (def documentoDto, DestinatarioNotificaDTO destinatarioDTO) {
        def documento = documentoDto.domainObject

        DestinatarioNotifica d = new DestinatarioNotifica()
        d.tipoDestinatario = DestinatarioNotifica.TIPO_DESTINATARIO_PITRE
        d.tipoNotifica = destinatarioDTO.tipoNotifica
        d.idSoggettoEsterno=destinatarioDTO.idSoggettoEsterno
        d.tipoSoggettoEsterno=destinatarioDTO.tipoSoggettoEsterno
        d.tipoCorrispondenteEsterno=destinatarioDTO.tipoCorrispondenteEsterno
        d.tipoRubricaEsterna=destinatarioDTO.tipoRubricaEsterna
        d.nome=destinatarioDTO.nome
        d.cognome=destinatarioDTO.cognome
        d.tipoNotifica=DestinatarioNotifica.TIPO_NOTIFICA_COMPETENZA
        d.indirizzo=destinatarioDTO.indirizzo
        d.email=destinatarioDTO.email?.domainObject
        d.comune=destinatarioDTO.comune
        d.provincia=destinatarioDTO.provincia
        d.cap = destinatarioDTO.cap
        d.indirizzoEmail=destinatarioDTO.indirizzoEmail

        documento.addToDestinatariNotifiche(d)
        documento.save()

        return documento.toDTO()
    }

    List<DestinatarioNotificaDTO> getListaDestinatari (def documento) {
        return (documento.domainObject?.destinatariNotifiche?.toDTO(["utente", "email", "unitaSo4"])?:[] as List)?.sort { it.denominazione }
    }

    def getListaDestinatariInterni (def documento) {
        String nomeCampoDocumentoId
        if (documento instanceof DeterminaDTO) {
            nomeCampoDocumentoId = "determina.id"
        }
        if (documento instanceof PropostaDeliberaDTO) {
            nomeCampoDocumentoId = "propostaDelibera.id"
        }
        if (documento instanceof SedutaStampaDTO) {
            nomeCampoDocumentoId = "sedutaStampa.id"
        }

        return DestinatarioNotifica.createCriteria().list {
            eq(nomeCampoDocumentoId, documento.id)
            eq("tipoDestinatario", DestinatarioNotifica.TIPO_DESTINATARIO_INTERNO)
            fetchMode("utente", FetchMode.JOIN)
            fetchMode("unitaSo4", FetchMode.JOIN)
        }.collect {
            As4SoggettoCorrente soggetto = null
            if (it.utente != null) {
                soggetto = As4SoggettoCorrente.findByUtenteAd4(it.utente)
            }
            [destinatario: it.toDTO(), nome: soggetto?.nome, cognome: soggetto?.cognome, unita: it.unitaSo4?.descrizione, email: soggetto?.indirizzoWeb]
        }
    }

    def getListaDestinatariEsterni (def documento) {
        String nomeCampoDocumentoId
        if (documento instanceof DeterminaDTO) {
            nomeCampoDocumentoId = "determina.id"
        }
        if (documento instanceof PropostaDeliberaDTO) {
            nomeCampoDocumentoId = "propostaDelibera.id"
        }
        if (documento instanceof SedutaStampaDTO) {
            nomeCampoDocumentoId = "sedutaStampa.id"
        }
        return DestinatarioNotifica.createCriteria().list {
            eq(nomeCampoDocumentoId, documento.id)
            eq("tipoDestinatario", DestinatarioNotifica.TIPO_DESTINATARIO_ESTERNO)
            fetchMode("email", FetchMode.JOIN)
            fetchMode("soggetto", FetchMode.JOIN)
        }.toDTO()
    }

    def getListaDestinatariPiTre (def documento) {
        String nomeCampoDocumentoId
        if (documento instanceof DeterminaDTO) {
            nomeCampoDocumentoId = "determina.id"
        }
        if (documento instanceof PropostaDeliberaDTO) {
            nomeCampoDocumentoId = "propostaDelibera.id"
        }
        if (documento instanceof SedutaStampaDTO) {
            nomeCampoDocumentoId = "sedutaStampa.id"
        }
        return DestinatarioNotifica.createCriteria().list {
            eq(nomeCampoDocumentoId, documento.id)
            eq("tipoDestinatario", DestinatarioNotifica.TIPO_DESTINATARIO_PITRE)
        }.toDTO()
    }
}
