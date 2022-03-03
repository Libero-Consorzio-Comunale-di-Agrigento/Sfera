package it.finmatica.atti.dto.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.DocumentoCollegato
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.dto.DTO
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class DocumentoCollegatoDTO implements it.finmatica.dto.DTO<DocumentoCollegato> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    DeliberaDTO deliberaCollegata;
    DeterminaDTO determinaCollegata;
    DeterminaDTO determinaPrincipale;
    DeliberaDTO deliberaPrincipale;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    String operazione;
    PropostaDeliberaDTO propostaDeliberaPrincipale;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    RiferimentoEsternoDTO riferimentoEsternoCollegato;


    public DocumentoCollegato getDomainObject () {
        return DocumentoCollegato.get(this.id)
    }

    public DocumentoCollegato copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    boolean collegamentoInverso

    String getTipoDocumento () {
        if (collegamentoInverso) {
            if (determinaPrincipale != null) {
                return Determina.TIPO_OGGETTO
            }

            if (propostaDeliberaPrincipale != null) {
                return PropostaDelibera.TIPO_OGGETTO
            }

            if (deliberaPrincipale != null){
                return Delibera.TIPO_OGGETTO
            }
        } else {
            if (deliberaCollegata != null) {
                return Delibera.TIPO_OGGETTO
            }

            if (determinaCollegata != null) {
                return Determina.TIPO_OGGETTO
            }

            if (riferimentoEsternoCollegato != null) {
                return riferimentoEsternoCollegato.tipoDocumento
            }
        }

        return null
    }

    String getOggetto () {
        if (collegamentoInverso) {
            if (propostaDeliberaPrincipale != null) {
                return "${propostaDeliberaPrincipale.estremiAtto} - ${propostaDeliberaPrincipale.oggetto}"
            }

            if (deliberaPrincipale != null) {
                return "${deliberaPrincipale.estremiAtto} - ${deliberaPrincipale.oggetto}"
            }

            if (determinaPrincipale != null) {
                return "${determinaPrincipale.estremiAtto} - ${determinaPrincipale.oggetto}"
            }
        } else {
            if (deliberaCollegata != null) {
                return "${deliberaCollegata.estremiAtto} - ${deliberaCollegata.oggetto}"
            }

            if (determinaCollegata != null) {
                return "${determinaCollegata.estremiAtto} - ${determinaCollegata.oggetto}"
            }

            if (riferimentoEsternoCollegato != null) {
                return riferimentoEsternoCollegato.titolo
            }
        }

        return null
    }

    DTO<?> getDocumento () {
        if (collegamentoInverso) {
            if (propostaDeliberaPrincipale != null) {
                return propostaDeliberaPrincipale
            }

            if (deliberaPrincipale != null) {
                return deliberaPrincipale
            }

            if (determinaPrincipale != null) {
                return determinaPrincipale
            }
        } else {
            if (deliberaCollegata != null) {
                return deliberaCollegata
            }

            if (determinaCollegata != null) {
                return determinaCollegata
            }

            if (riferimentoEsternoCollegato != null) {
                return riferimentoEsternoCollegato
            }
        }

        return null
    }

    boolean isEliminabile () {
        return !collegamentoInverso
    }
}
