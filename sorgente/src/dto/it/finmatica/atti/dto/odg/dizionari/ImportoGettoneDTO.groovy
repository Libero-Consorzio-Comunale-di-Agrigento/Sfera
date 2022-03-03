package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.odg.dizionari.ImportoGettone
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class ImportoGettoneDTO implements it.finmatica.dto.DTO<ImportoGettone> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    CommissioneDTO commissione;
    Date dateCreated;
    So4AmministrazioneDTO ente;
    BigDecimal importo;
    Date lastUpdated;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public ImportoGettone getDomainObject () {
        return ImportoGettone.get(this.id)
    }

    public ImportoGettone copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
