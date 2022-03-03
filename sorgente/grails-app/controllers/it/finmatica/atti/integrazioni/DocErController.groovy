package it.finmatica.atti.integrazioni

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.impostazioni.Impostazione
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.so4.login.detail.Amministrazione
import it.finmatica.so4.login.detail.Ottica
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.struttura.So4Ottica
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4RuoloComponentePubb

import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element

class DocErController {

	SpringSecurityService 	springSecurityService
	DocErService 			docErService

	def index () {
		redirect action: "registrazioneParticolare", params: params
	}

    def registrazioneParticolare(String user,String xmlInput) {
		String result

		springSecurityService.reauthenticate(user)
		def listaAmministrazioni = springSecurityService.principal.amministrazioni

		// se l'utente non ha amministrazioni
		if (!(listaAmministrazioni?.size() > 1)) {
			String[] codiciEnti = Impostazioni.ENTI_SO4.valori;
			for (String codiceEnte : codiciEnti) {
				So4Amministrazione a = So4Amministrazione.get(codiceEnte);
				Amministrazione amm = new Amministrazione(codice: a.codice, descrizione: a.soggetto.denominazione)

				def ottiche = So4Ottica.findAllByAmministrazione(a)
				amm.ottiche = [];
				for (So4Ottica o : ottiche) {
					amm.ottiche << new Ottica (codice: o.codice, descrizione: o.descrizione);
				}

				listaAmministrazioni << amm;
			}
		}

		for (Amministrazione a : listaAmministrazioni) {
			String codiceOtt = Impostazione.getImpostazione(Impostazioni.OTTICA_SO4.toString(), a.codice).get()?.valore;
			if (codiceOtt == null)
				continue;

			springSecurityService.principal.setAmministrazioneOtticaCorrente (a.codice, codiceOtt);

			if(!springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_FIRMA.valore)){

				result =  getXMLOuput("-1","L'utente "+user+" non ha i diritti di registrazione!","","","","")
				render result
				return
			}
		}

		// Possiede i diritti per effettuare la registrazione
		result = docErService.registrazioneParticolare(xmlInput)

		render result
	}

	private String getXMLOuput (String codice, String descrizione, String data, String numero, String oggetto, String registro) {
		Document xml = DocumentHelper.createDocument()
		xml.setXMLEncoding("iso-8859-1");

		Element esito = xml.addElement("esito")
		esito.addElement("codice").setText(codice)
		esito.addElement("descrizione").setText(descrizione)

		Element dati_registro = esito.addElement("dati_registro")
		dati_registro.addElement ("DataRegistrazione").setText(data?data:"")
		dati_registro.addElement ("NumeroRegistrazione").setText(numero?numero:"")
		dati_registro.addElement ("OggettoRegistrazione").setText(oggetto?oggetto:"")
		dati_registro.addElement ("IDRegistro").setText(registro?registro:"")

		return xml.asXML()
	}
}
