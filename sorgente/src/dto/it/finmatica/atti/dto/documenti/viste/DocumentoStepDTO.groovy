package it.finmatica.atti.dto.documenti.viste

import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.viste.DocumentoStep
import it.finmatica.atti.dto.documenti.*
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.motore.WkfStepDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class DocumentoStepDTO implements it.finmatica.dto.DTO<DocumentoStep> {
    private static final long serialVersionUID = 1L;

    Integer anno;
    Integer annoProposta;
    CertificatoDTO certificato;
    Date dataAdozione;
    Date dataScadenza;
    DeliberaDTO delibera;
    String descrizioneTipologia;
    DeterminaDTO determina;
    So4AmministrazioneDTO ente;
    Long idDocumento;
    Long idPadre;
    Long idTipologia;
    Integer numero;
    Integer numeroProposta;
    String oggetto;
    PropostaDeliberaDTO propostaDelibera;
    boolean riservato;
    String stato;
    String statoConservazione;
    String statoFirma;
    String statoOdg;
    String statoVistiPareri;
    WkfStepDTO step;
    String stepDescrizione;
    String stepNome;
    Ad4RuoloDTO stepRuolo;
    String stepTitolo;
    So4UnitaPubbDTO stepUnita;
    Ad4UtenteDTO stepUtente;
    String tipoOggetto;
    String tipoRegistro;
    String titoloTipologia;
    String unitaProponente;
    VistoParereDTO vistoParere;
    Integer priorita;
    Date dataOrdinamento;


    public DocumentoStep getDomainObject () {
        return DocumentoStep.get(this.idDocumento)
    }

    public DocumentoStep copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
