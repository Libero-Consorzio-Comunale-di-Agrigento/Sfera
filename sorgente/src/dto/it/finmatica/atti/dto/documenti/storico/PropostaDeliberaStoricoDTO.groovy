package it.finmatica.atti.dto.documenti.storico

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.StatoOdg
import it.finmatica.atti.documenti.storico.PropostaDeliberaStorico
import it.finmatica.atti.dto.commons.FileAllegatoStoricoDTO
import it.finmatica.atti.dto.dizionari.CategoriaDTO
import it.finmatica.atti.dto.dizionari.DelegaDTO
import it.finmatica.atti.dto.dizionari.IndirizzoDeliberaDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoDeliberaDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.motore.WkfIterDTO
import it.finmatica.gestioneiter.dto.motore.WkfStepDTO
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class PropostaDeliberaStoricoDTO implements it.finmatica.dto.DTO<PropostaDeliberaStorico> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Integer annoProposta;
    String campiProtetti;
    CategoriaDTO categoria;
    String classificaCodice;
    Date classificaDal;
    String classificaDescrizione;
    String codiciVistiTrattati;
    CommissioneDTO commissione;
    boolean controlloFunzionario;
    Date dataNumeroProposta;
    Date dataProposta;
    Date dateCreated;
    DelegaDTO delega;
    So4AmministrazioneDTO ente;
    Integer fascicoloAnno;
    String fascicoloNumero;
    String fascicoloOggetto;
    boolean fuoriSacco;
    Integer giorniPubblicazione;
    Long idDocumentoEsterno;
    long idPropostaDelibera;
    IndirizzoDeliberaDTO indirizzo;
    WkfIterDTO iter;
    Date lastUpdated;
    GestioneTestiModelloDTO modelloTesto;
    GestioneTestiModelloDTO modelloTestoAnnullamento;
    String note;
    String noteCommissione;
    String noteContabili;
    String noteTrasmissione;
    Integer numeroProposta;
    String oggetto;
    OggettoSedutaDTO oggettoSeduta;
    boolean parereRevisoriConti;
    boolean pubblicaRevoca;
    TipoRegistroDTO registroProposta;
    long revisione;
    boolean riservato;
    FileAllegatoStoricoDTO stampaUnica;
    StatoDocumento stato;
    StatoFirma statoFirma;
    StatoOdg statoOdg;
    WkfStepDTO step;
    FileAllegatoStoricoDTO testo;
    TipoDeliberaDTO tipologia;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Long versioneDocumentoEsterno;
    String xmlDelibereCollegate;
    String xmlSoggetti;
    String motivazione
    Integer priorita
    String xmlDatiAggiuntivi


    public PropostaDeliberaStorico getDomainObject () {
        return PropostaDeliberaStorico.get(this.id)
    }

    public PropostaDeliberaStorico copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
