package it.finmatica.atti.dto.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.OrganoControlloNotifica
import it.finmatica.atti.dto.commons.FileAllegatoDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.odg.dizionari.TipoOrganoControlloDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class OrganoControlloNotificaDTO implements it.finmatica.dto.DTO<OrganoControlloNotifica> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String ambito;
    Integer annoProtocollo;
    Date dataAdozioneAl;
    Date dataAdozioneDal;
    Date dataPubblicazioneAl;
    Date dataPubblicazioneDal;
    Date dateCreated;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    Integer numeroProtocollo;
    String stato;
    FileAllegatoDTO testo;
    TipoOrganoControlloDTO tipoOrganoControllo;
    TipoRegistroDTO tipoRegistro;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public OrganoControlloNotifica getDomainObject () {
        return OrganoControlloNotifica.get(this.id)
    }

    public OrganoControlloNotifica copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
