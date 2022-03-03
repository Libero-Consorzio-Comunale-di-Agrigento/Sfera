package it.finmatica.atti.integrazioni.contabilita

import grails.compiler.GrailsCompileStatic
import groovy.transform.CompileDynamic
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioni.ContabilitaCe4Service
import it.finmatica.atti.integrazioniws.ads.ce4.Ce4PortType
import it.finmatica.atti.integrazioniws.ads.ce4.Request
import it.finmatica.atti.integrazioniws.ads.ce4.Response
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component("integrazioneContabilitaCe4")
@Lazy
@GrailsCompileStatic
class IntegrazioneContabilitaCe4 extends AbstractIntegrazioneContabilita {

    @Autowired IntegrazioneContabilitaCe4Config integrazioneContabilitaCe4Config
    @Autowired ContabilitaCe4Service contabilitaCe4Service

    @Autowired
    @Qualifier("ce4Client")
    private Ce4PortType ce4PortType

    private static final String CREATOR = "SFERA"
    private static final Logger log = Logger.getLogger(IntegrazioneContabilitaCe4.class);

    String messaggio = "";
    String esito = ""

    // ce4:aggiornaProposta
    void salvaProposta (IProposta proposta) {
        Request request = new Request();
        request.setCreator(CREATOR);

        messaggio = contabilitaCe4Service.getMessaggioAggiornaProposta(proposta)
        request.setMessage(messaggio);
        log.debug("request: " + request.message)

        Response response = ce4PortType.ce4Operation(request);
        log.debug("response: " + response.message)

        try {
            esito =  response.getMessage().split("<Esito>")[1].split("</Esito>")[0];
        } catch (AttiRuntimeException) {
            throw new AttiRuntimeException("Errore durante l'integrazione con la contabilità ce4, metodo ce4:aggiornaProposta: "+ response.getMessage())
        }

        if (!esito.equals("OK")) {
            throw new AttiRuntimeException("Errore durante l'integrazione con la contabilità ce4, metodo ce4:aggiornaProposta: "+ response.getMessage())
        }

    }

    // ce4:rifutaProposta
    void annullaProposta (IProposta proposta) {
        if (proposta instanceof Delibera) {
            annullaProposta(proposta?.atto)
        } else if (proposta instanceof Determina) {
            annullaProposta(proposta?.atto)
        } else {
            // caso Proposta di Delibera
            Request request = new Request();
            request.setCreator(CREATOR);

            messaggio = contabilitaCe4Service.getMessaggioRifiutaProposta(proposta)
            request.setMessage(messaggio);
            log.debug("request: " + request.message)

            Response response = ce4PortType.ce4Operation(request);
            log.debug("response: " + response.message)

            try {
                esito =  response.getMessage().split("<Esito>")[1].split("</Esito>")[0];
            } catch (AttiRuntimeException) {
                throw new AttiRuntimeException("Errore durante l'integrazione con la contabilità ce4, metodo ce4:rifiutaProposta: "+ response.getMessage())
            }

            if (!esito.equals("OK")) {
                throw new AttiRuntimeException("Errore durante l'integrazione con la contabilità ce4, metodo ce4:rifiutaProposta: "+ response.getMessage())
            }
        }
    }

    // ce4:annullaDelibera
    void annullaProposta (IAtto atto) {
        if (atto?.numeroAtto) {
            Request request = new Request();
            request.setCreator(CREATOR);

            messaggio = contabilitaCe4Service.getMessaggioAnnullaDelibera(atto)
            request.setMessage(messaggio);
            log.debug("request: " + request.message)

            Response response = ce4PortType.ce4Operation(request);
            log.debug("response: " + response.message)

            try {
                esito =  response.getMessage().split("<Esito>")[1].split("</Esito>")[0];
            } catch (AttiRuntimeException) {
                throw new AttiRuntimeException("Errore durante l'integrazione con la contabilità ce4, metodo ce4:annullaProposta: "+ response.getMessage())
            }

            if (!esito.equals("OK")) {
                throw new AttiRuntimeException("Errore durante l'integrazione con la contabilità ce4, metodo ce4:annullaProposta: "+ response.getMessage())
            }
        } else if (atto instanceof Determina) {
            // caso proposta di determina
            Request request = new Request();
            request.setCreator(CREATOR);

            messaggio = contabilitaCe4Service.getMessaggioRifiutaProposta(atto?.proposta)
            request.setMessage(messaggio);
            log.debug("request: " + request.message)

            Response response = ce4PortType.ce4Operation(request);
            log.debug("response: " + response.message)

            try {
                esito =  response.getMessage().split("<Esito>")[1].split("</Esito>")[0];
            } catch (AttiRuntimeException) {
                throw new AttiRuntimeException("Errore durante l'integrazione con la contabilità ce4, metodo ce4:rifiutaProposta: "+ response.getMessage())
            }

            if (!esito.equals("OK")) {
                throw new AttiRuntimeException("Errore durante l'integrazione con la contabilità ce4, metodo ce4:rifiutaProposta: "+ response.getMessage())
            }
        }
    }

    // ce4:aggiornaProposta
    void salvaAtto (IAtto atto) {
        Request request = new Request();
        request.setCreator(CREATOR);

        messaggio = contabilitaCe4Service.getMessaggioAggiornaProposta(atto)
        request.setMessage(messaggio);
        log.debug("request: " + request.message)

        Response response = ce4PortType.ce4Operation(request);
        log.debug("response: " + response.message)

        try {
            esito =  response.getMessage().split("<Esito>")[1].split("</Esito>")[0];
        } catch (AttiRuntimeException) {
            throw new AttiRuntimeException("Errore durante l'integrazione con la contabilità ce4, metodo ce4:aggiornaProposta: "+ response.getMessage())
        }

        if (!esito.equals("OK")) {
            throw new AttiRuntimeException("Errore durante l'integrazione con la contabilità ce4, metodo ce4:aggiornaProposta: "+ response.getMessage())
        }
    }

    // ce4:confermaProposta
    @CompileDynamic
    void rendiEsecutivoAtto (IAtto atto) {
        Request request = new Request();
        request.setCreator(CREATOR);

        messaggio = contabilitaCe4Service.getMessaggioConfermaProposta(atto)
        request.setMessage(messaggio);
        log.debug("request: " + request.message)

        Response response = ce4PortType.ce4Operation(request);
        log.debug("response: " + response.message)

        try {
            esito =  response.getMessage().split("<Esito>")[1].split("</Esito>")[0];
        } catch (AttiRuntimeException) {
            throw new AttiRuntimeException("Errore durante l'integrazione con la contabilità ce4, metodo ce4:confermaProposta: "+ response.getMessage())
        }

        if (!esito.equals("OK")) {
            throw new AttiRuntimeException("Errore durante l'integrazione con la contabilità ce4, metodo ce4:confermaProposta: "+ response.getMessage())
        }
    }

}
