package it.finmatica.atti.dto.documenti.storico

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.StatoConservazione
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.storico.DeliberaStorico
import it.finmatica.atti.dto.commons.FileAllegatoStoricoDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.motore.WkfIterDTO
import it.finmatica.gestioneiter.dto.motore.WkfStepDTO
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class DeliberaStoricoDTO implements it.finmatica.dto.DTO<DeliberaStorico> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Integer annoDelibera;
    Integer annoProtocollo;
    Date dataAdozione;
    Date dataEsecutivita;
    Date dataFinePubblicazione;
    Date dataFinePubblicazione2;
    Date dataNumeroDelibera;
    Date dataNumeroProtocollo;
    Date dataPubblicazione;
    Date dataPubblicazione2;
    Date dateCreated;
    So4AmministrazioneDTO ente;
    boolean eseguibilitaImmediata;
    Integer giorniPubblicazione;
    long idDelibera;
    Long idDocumentoEsterno;
    WkfIterDTO iter;
    Date lastUpdated;
    GestioneTestiModelloDTO modelloTesto;
    String note;
    String noteTrasmissione;
    Integer numeroDelibera;
    Integer numeroProtocollo;
    String oggetto;
    OggettoSedutaDTO oggettoSeduta;
    PropostaDeliberaDTO proposta;
    boolean pubblicaRevoca;
    TipoRegistroDTO registroDelibera;
    TipoRegistroDTO registroProtocollo;
    long revisione;
    boolean riservato;
    FileAllegatoStoricoDTO stampaUnica;
    StatoDocumento stato;
    StatoConservazione statoConservazione;
    StatoFirma statoFirma;
    WkfStepDTO step;
    FileAllegatoStoricoDTO testo;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Long versioneDocumentoEsterno;
    String xmlSoggetti;
    String motivazioniEseguibilita;
    String xmlDatiAggiuntivi;


    public DeliberaStorico getDomainObject () {
        return DeliberaStorico.get(this.id)
    }

    public DeliberaStorico copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
