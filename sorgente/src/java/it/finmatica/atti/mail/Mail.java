package it.finmatica.atti.mail;

import it.finmatica.cim.*;
import org.apache.log4j.Logger;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import java.util.Iterator;
import java.util.List;

public class Mail {

	static { // add handlers for main MIME types
		MailcapCommandMap mcap = new MailcapCommandMap();
		mcap.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mcap.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mcap.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mcap.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed; x-java-fallback-entry=true");
		mcap.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mcap);
	}

	private static final Logger log = Logger.getLogger(Mail.class);

	/**
	 * Invia la mail con testo e oggetto indicati, alla lista di destinatari passata in ingresso.
	 * L'aliasMail deve coincidere con uno di quelli definiti nel si4cim.cfg
	 *
	 * @param aliasMail				Alias selezionato fra quelli indicati nel si4cim.cfg
	 * @param mittente				Indirizzo e-mail del mittente
	 * @param listaDestinatari		Lista degli indirizzi e-mail dei destinatari
	 * @param testo					Testo della mail
	 * @param oggetto				Oggetto della mail
	 * @param listaAllegati			Lista degli allegati da aggiungere alla mail
	 * @throws Exception
	 */
	public static Throwable invia(String aliasMail, String mittente, List<String> listaDestinatari, String testo, String oggetto, List<Allegato> listaAllegati) {
		try {
			if (listaDestinatari == null || (listaDestinatari != null && listaDestinatari.isEmpty())) {
				log.warn ("Nessun destinatario specificato. Non invio email.");
				return null;
			}

			if (log.isDebugEnabled()) {
				log.debug ( "Invio Email tramite il tag: "+aliasMail+"\n"+
							"DA: "+mittente+"\n"+
							"A: "+listaDestinatari+"\n"+
							"OGGETTO: "+oggetto+
							"TESTO: "+testo);
			}

			GenericMessage gm = Creator.create(aliasMail);

			Sender senderCim = new Sender();

			Contact senderContact = new Contact();
			senderContact.setEmail(mittente);

			senderCim.setContact(senderContact);

			// imposto il codice del Progetto e del Modulo come presenti su AD4
			// per meglio identificare le email una volta che sono sul si4cs.
			gm.setProject("AGSDE2", "AGSDE2", null);
			gm.setSender(senderCim);

			Iterator<String> iteratorDestinatari = listaDestinatari.iterator();
			while (iteratorDestinatari.hasNext()) {
				Contact destContact  = new Contact();
				destContact.setEmail(iteratorDestinatari.next());
				gm.addBcc(destContact);
			}

			if (testo != null) {
				gm.setText(testo);
			}

			if (oggetto != null) {
				gm.setSubject(oggetto);
			}

			if (listaAllegati != null && !listaAllegati.isEmpty()) {
				Iterator<Allegato> iteratorAllegati = listaAllegati.iterator();
				while (iteratorAllegati.hasNext()) {
					Allegato a = iteratorAllegati.next();
					Attachment cimAtt = new Attachment(a.getTesto(), a.getNome());
					gm.addAttachment(cimAtt);
				}
			}

			if (gm.send() != 0) {
				throw new Exception("Si è verificato un errore nella spedizione email");
			}

			log.info("Email inviata.");
			return null;

		} catch (Throwable e) {
			log.error("Si è verificato un errore nella spedizione della email.", e);
			return e;
		}
	}
}