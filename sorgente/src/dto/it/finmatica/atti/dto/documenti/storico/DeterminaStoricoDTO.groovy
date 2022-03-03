package it.finmatica.atti.dto.documenti.storico

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.StatoConservazione
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.StatoOdg
import it.finmatica.atti.documenti.storico.DeterminaStorico
import it.finmatica.atti.dto.commons.FileAllegatoStoricoDTO
import it.finmatica.atti.dto.dizionari.CategoriaDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoDeterminaDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDTO
import it.finmatica.gestioneiter.dto.motore.WkfIterDTO
import it.finmatica.gestioneiter.dto.motore.WkfStepDTO
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

@GrailsCompileStatic
class DeterminaStoricoDTO implements it.finmatica.dto.DTO<DeterminaStorico> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Integer annoDetermina;
    Integer annoProposta;
    Integer annoProtocollo;
    String campiProtetti;
    CategoriaDTO categoria;
    String classificaCodice;
    Date classificaDal;
    String classificaDescrizione;
    String codiceGara;
    String codiciVistiTrattati;
    CommissioneDTO commissione;
    boolean controlloFunzionario;
    Date dataEsecutivita;
    Date dataFinePubblicazione;
    Date dataFinePubblicazione2;
    Date dataNumeroDetermina;
    Date dataNumeroProposta;
    Date dataNumeroProtocollo;
    Date dataProposta;
    Date dataPubblicazione;
    Date dataPubblicazione2;
    Date dataScadenza;
    Date dateCreated;
    String xmlDatiAggiuntivi;
    So4AmministrazioneDTO ente;
    Integer fascicoloAnno;
    String fascicoloNumero;
    String fascicoloOggetto;
    Integer giorniPubblicazione;
    long idDetermina;
    Long idDocumentoEsterno;
    WkfIterDTO iter;
    Date lastUpdated;
    GestioneTestiModelloDTO modelloTesto;
    String motivazione;
    String note;
    String noteContabili;
    String noteTrasmissione;
    Integer numeroDetermina;
    Integer numeroProposta;
    Integer numeroProtocollo;
    String oggetto;
    OggettoSedutaDTO oggettoSeduta;
    boolean pubblicaRevoca;
    TipoRegistroDTO registroDetermina;
    TipoRegistroDTO registroProposta;
    TipoRegistroDTO registroProtocollo;
    long revisione;
    boolean riservato;
    FileAllegatoStoricoDTO stampaUnica;
    StatoDocumento stato;
    StatoConservazione statoConservazione;
    StatoFirma statoFirma;
    StatoOdg statoOdg;
    WkfStepDTO step;
    FileAllegatoStoricoDTO testo;
    TipoDeterminaDTO tipologia;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Long versioneDocumentoEsterno;
    String xmlDetermineCollegate;
    String xmlSoggetti;
    Integer priorita


    public DeterminaStorico getDomainObject () {
        return DeterminaStorico.get(this.id)
    }

    public DeterminaStorico copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */
    // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
