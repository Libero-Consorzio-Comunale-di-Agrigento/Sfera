package it.finmatica.atti.dto.dizionari

import groovy.transform.TypeCheckingMode
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class NotificaDTO implements it.finmatica.dto.DTO<Notifica> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String allegati;
    CommissioneDTO commissione;
    Date dateCreated;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    String modalitaInvio;
    Set<NotificaEmailDTO> notificheEmail;
    String oggetti;
    String oggetto;
    OrganoControlloDTO organoControllo;
    String testo;
    String tipoNotifica;
    String titolo;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;

    public void addToNotificheEmail (NotificaEmailDTO notificaEmail) {
        if (this.notificheEmail == null)
            this.notificheEmail = new HashSet<NotificaEmailDTO>()
        this.notificheEmail.add (notificaEmail);
        notificaEmail.notifica = this
    }

    public void removeFromNotificheEmail (NotificaEmailDTO notificaEmail) {
        if (this.notificheEmail == null)
            this.notificheEmail = new HashSet<NotificaEmailDTO>()
        this.notificheEmail.remove (notificaEmail);
        notificaEmail.notifica = null
    }

    public Notifica getDomainObject () {
        return Notifica.get(this.id)
    }

    public Notifica copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

	public TipoNotifica getTipo () {
		return TipoNotifica.lista.find { it.codice == tipoNotifica }
	}

    @GrailsCompileStatic(TypeCheckingMode.SKIP)
	public List<String> getListaAllegati () {
		return allegati?.split("#")?:[];
	}

    @GrailsCompileStatic(TypeCheckingMode.SKIP)
	public List<String> getListaOggetti () {
		return oggetti?.split("#")?:[];
	}

	public String getListaOggettiDescrizioni () {
		return getListaOggetti().join(", ");
	}
}
