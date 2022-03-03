package it.finmatica.atti.dto.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.StatoMarcatura
import it.finmatica.atti.dto.commons.FileAllegatoDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoCertificatoDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.motore.WkfIterDTO
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class CertificatoDTO implements it.finmatica.dto.DTO<Certificato> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    DeliberaDTO delibera;
    DeterminaDTO determina;
    So4AmministrazioneDTO ente;
    Set<FirmatarioDTO> firmatari;
    Ad4UtenteDTO firmatario;
    Long idDocumentoEsterno;
    WkfIterDTO iter;
    Date lastUpdated;
    GestioneTestiModelloDTO modelloTesto;
    boolean secondaPubblicazione;
    StatoDocumento stato;
    StatoFirma statoFirma;
    StatoMarcatura statoMarcatura;
    FileAllegatoDTO testo;
    FileAllegatoDTO testoOdt;
    String tipo;
    TipoCertificatoDTO tipologia;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;

    public void addToFirmatari (FirmatarioDTO firmatario) {
        if (this.firmatari == null)
            this.firmatari = new HashSet<FirmatarioDTO>()
        this.firmatari.add (firmatario);
        firmatario.certificato = this
    }

    public void removeFromFirmatari (FirmatarioDTO firmatario) {
        if (this.firmatari == null)
            this.firmatari = new HashSet<FirmatarioDTO>()
        this.firmatari.remove (firmatario);
        firmatario.certificato = null
    }

    public Certificato getDomainObject () {
        return Certificato.get(this.id)
    }

    public Certificato copyToDomainObject () {
        return null
    }

	/* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
	// qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

	public void setDocumentoPrincipale (def documentoPrincipale) {
		if (documentoPrincipale instanceof DeliberaDTO) {
			delibera = (DeliberaDTO) documentoPrincipale;
		} else {
			determina = (DeterminaDTO)documentoPrincipale;
		}
	}

	public def getDocumentoPrincipale () {
		return determina?:delibera;
	}
}
