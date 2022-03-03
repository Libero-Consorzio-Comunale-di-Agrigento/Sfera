package it.finmatica.atti.dto.documenti.storico

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.EsitoVisto
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.storico.VistoParereStorico
import it.finmatica.atti.dto.commons.FileAllegatoStoricoDTO
import it.finmatica.atti.dto.documenti.DeliberaDTO
import it.finmatica.atti.dto.documenti.DeterminaDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoVistoParereDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.motore.WkfIterDTO
import it.finmatica.gestioneiter.dto.motore.WkfStepDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class VistoParereStoricoDTO implements it.finmatica.dto.DTO<VistoParereStorico> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    boolean automatico;
    String campiProtetti;
    Date dataAdozione;
    Date dateCreated;
    DeterminaDTO determina;
    So4AmministrazioneDTO ente;
    EsitoVisto esito;
    Ad4UtenteDTO firmatario;
    Long idDocumentoEsterno;
    long idVistoParere;
    WkfIterDTO iter;
    Date lastUpdated;
    String note;
    PropostaDeliberaDTO propostaDelibera;
    DeliberaDTO delibera;
    long revisione;
    StatoDocumento stato;
    StatoFirma statoFirma;
    WkfStepDTO step;
    FileAllegatoStoricoDTO testo;
    TipoVistoParereDTO tipologia;
    So4UnitaPubbDTO unitaSo4;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Long versioneDocumentoEsterno;
    String noteTrasmissione;


    public VistoParereStorico getDomainObject () {
        return VistoParereStorico.get(this.id)
    }

    public VistoParereStorico copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
