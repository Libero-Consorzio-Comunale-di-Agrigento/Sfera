package it.finmatica.zkutils

import groovy.transform.CompileStatic
import org.zkoss.zk.ui.util.Clients

@CompileStatic
class SuccessHandler {

    // Questo lo metto qui per comodità e per non dover creare un altro bean request scoped.
    // potrebbe aver senso cambiare il nome di questa classe. Per ora sticazzi.
    // contiene l'id dell'iter per cui non voglio eseguire il controllo automatico del testo.
    long idIterSaltaControlloTesto

    private List<String> messages

    // Indica se alla fine di tutto i messaggi vanno mostrati o no.
    // ci sono alcuni casi in cui non è necessario mostrarli e anzi è dannoso, ad esempio: http://svi-redmine/issues/10016
    // Quindi per evitare che la casa di vetro venga "nascosta", non mostriamo il messaggio standard "Documento Salvato"
    boolean nascondiMessaggi = false

    // Salta l'invalidate della maschera del documento.
    // Serve per evitare che la casa di vetro venga "nascosta" quando si clicca sul pulsante "Dati Trasparenza".
    boolean saltaInvalidate = false

    SuccessHandler () {
        messages = new ArrayList<String>()
        idIterSaltaControlloTesto = -1
        nascondiMessaggi = false
        saltaInvalidate = false
    }

    void nascondiMessaggi () {
        this.nascondiMessaggi = true
    }

    void saltaInvalidate () {
        this.saltaInvalidate = true
    }

    void showMessages (String messaggioDefault) {
        if (messages == null || messages.size() == 0) {
            messages << messaggioDefault
        }
        if (messages?.size() > 0 && !nascondiMessaggi) {
            showMessage(messages.join("\n"))
            clearMessages()
        }
    }

    void showMessages () {
        if (messages?.size() > 0 && !nascondiMessaggi) {
            showMessage(messages.join("\n"))
            clearMessages()
        }
    }

    void addMessage (String msg) {
        if (messages == null) {
            messages = new ArrayList<String>()
        }
        messages.add(msg)
    }

    void clearMessages () {
        if (messages != null) {
            messages.clear()
        }
    }

    List<String> getMessages () {
        return messages
    }

    void showMessage (String message) {
        Clients.showNotification(message, Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
    }
}