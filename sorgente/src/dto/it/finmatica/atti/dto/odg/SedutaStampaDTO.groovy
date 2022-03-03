package it.finmatica.atti.dto.odg

import grails.compiler.GrailsCompileStatic
import grails.util.Holders
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.documenti.DestinatarioNotificaDTO
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestionedocumenti.documenti.DocumentoDTO
import it.finmatica.gestionedocumenti.documenti.FileDocumento
import it.finmatica.gestionedocumenti.documenti.FileDocumentoDTO
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO

@GrailsCompileStatic
class SedutaStampaDTO extends DocumentoDTO implements it.finmatica.dto.DTO<SedutaStampa> {
    private static final long serialVersionUID = 1L

    SedutaDTO seduta
    CommissioneStampaDTO commissioneStampa
    String note

    // dati di fascicolazione
    String classificaCodice
    Date classificaDal
    String classificaDescrizione
    Integer fascicoloAnno
    String fascicoloNumero
    String fascicoloOggetto

    // dati dell'albo:
    Long idDocumentoAlbo
    Integer numeroAlbo
    Integer annoAlbo

    // dati di pubblicazione
    Long idDocumentoLettera
    boolean pubblicaVisualizzatore
    boolean pubblicaRevoca
    boolean daPubblicare
    Integer	giorniPubblicazione
    Date 	dataPubblicazione
    Date 	dataFinePubblicazione
    Date 	dataPubblicazione2
    Date 	dataFinePubblicazione2
    Date    dataMinimaPubblicazione

    // dati di protocollo
    Date            dataNumeroProtocollo
    Integer         numeroProtocollo
    Integer         annoProtocollo
    TipoRegistroDTO registroProtocollo

    // destinatari notifiche
    Set<DestinatarioNotificaDTO> destinatariNotifiche

    SedutaStampa getDomainObject () {
        return SedutaStampa.get(this.id)
    }

    SedutaStampa copyToDomainObject () {
        return null
    }

    public void addToDestinatariNotifiche (DestinatarioNotificaDTO destinatarioNotifica) {
        if (this.destinatariNotifiche == null) {
            this.destinatariNotifiche = new HashSet<DestinatarioNotificaDTO>()
        }
        this.destinatariNotifiche.add(destinatarioNotifica);
        destinatarioNotifica.sedutaStampa = this
    }

    public void removeFromDestinatariNotifiche (DestinatarioNotificaDTO destinatarioNotifica) {
        if (this.destinatariNotifiche == null) {
            this.destinatariNotifiche = new HashSet<DestinatarioNotificaDTO>()
        }
        this.destinatariNotifiche.remove(destinatarioNotifica);
        destinatarioNotifica.sedutaStampa = null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    CommissioneStampaDTO getTipologia () {
        return commissioneStampa
    }

    void setTipologia (CommissioneStampaDTO commissioneStampaDTO) {
        this.commissioneStampa = commissioneStampaDTO
    }

    GestioneTestiModelloDTO getModelloTesto () {
        return getTesto()?.modelloTesto
    }

    FileDocumentoDTO getTesto () {
        return fileDocumenti?.find { it.codice == FileDocumento.CODICE_FILE_PRINCIPALE && it.valido }
    }

    String getDatiProtocollazione () {
        if (numeroProtocollo > 0) {
            return "${numeroProtocollo} / ${annoProtocollo} del ${dataNumeroProtocollo.format("dd/MM/yyyy")}"
        }

        return ""
    }
}
