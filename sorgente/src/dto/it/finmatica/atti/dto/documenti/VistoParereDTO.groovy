package it.finmatica.atti.dto.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.EsitoVisto
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.StatoMarcatura
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.dto.commons.FileAllegatoDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoVistoParereDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.motore.WkfIterDTO
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class VistoParereDTO implements it.finmatica.dto.DTO<VistoParere> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Set<AllegatoDTO> allegati;
    boolean automatico;
    String campiProtetti;
    Date dataAdozione;
    Date dateCreated;
    DeliberaDTO delibera;
    DeterminaDTO determina;
    So4AmministrazioneDTO ente;
    EsitoVisto esito;
    Set<FirmatarioDTO> firmatari;
    Ad4UtenteDTO firmatario;
    Long idDocumentoEsterno;
    WkfIterDTO iter;
    Date lastUpdated;
    Date dataOrdinamento;
    GestioneTestiModelloDTO modelloTesto;
    String note;
    String noteTrasmissione;
    PropostaDeliberaDTO propostaDelibera;
    StatoDocumento stato;
    StatoFirma statoFirma;
    StatoMarcatura statoMarcatura;
    FileAllegatoDTO testo;
    FileAllegatoDTO testoOdt;
    TipoVistoParereDTO tipologia;
    So4UnitaPubbDTO unitaSo4;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;

    public void addToAllegati (AllegatoDTO allegato) {
        if (this.allegati == null)
            this.allegati = new HashSet<AllegatoDTO>()
        this.allegati.add (allegato);
        allegato.vistoParere = this
    }

    public void removeFromAllegati (AllegatoDTO allegato) {
        if (this.allegati == null)
            this.allegati = new HashSet<AllegatoDTO>()
        this.allegati.remove (allegato);
        allegato.vistoParere = null
    }
    public void addToFirmatari (FirmatarioDTO firmatario) {
        if (this.firmatari == null)
            this.firmatari = new HashSet<FirmatarioDTO>()
        this.firmatari.add (firmatario);
        firmatario.vistoParere = this
    }

    public void removeFromFirmatari (FirmatarioDTO firmatario) {
        if (this.firmatari == null)
            this.firmatari = new HashSet<FirmatarioDTO>()
        this.firmatari.remove (firmatario);
        firmatario.vistoParere = null
    }

    public VistoParere getDomainObject () {
        return VistoParere.get(this.id)
    }

    public VistoParere copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.
	
	// Questa proprietà viene valorizzata nei viewModel per decidere se mostrare o meno il "cestino" nell'interfaccia
	// di proposte/determine/delibere
	boolean competenzeInModifica = false; 

	public void setDocumentoPrincipale (def documentoPrincipale) {
		if (documentoPrincipale instanceof PropostaDeliberaDTO) {
			propostaDelibera = (PropostaDeliberaDTO) documentoPrincipale;
		} else if (documentoPrincipale instanceof DeterminaDTO) {
			determina = (DeterminaDTO) documentoPrincipale;
		} else if (documentoPrincipale instanceof DeliberaDTO) {
			delibera = (DeliberaDTO) documentoPrincipale;
		}
	}

	public def getDocumentoPrincipale () {
		return propostaDelibera?:determina?:delibera;
	}
}
