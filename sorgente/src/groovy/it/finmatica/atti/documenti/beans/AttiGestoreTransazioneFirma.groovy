package it.finmatica.atti.documenti.beans

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IFileAllegato
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.atti.integrazioniws.ads.l190.WSPasswordEncrypter
import it.finmatica.grails.firmadigitale.FirmaDigitaleFile
import it.finmatica.grails.firmadigitale.FirmaDigitaleService
import it.finmatica.grails.firmadigitale.FirmaDigitaleTransazione
import it.finmatica.grails.firmadigitale.FirmaDigitaleURLConfig
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

/**
 * Bean con scope Request. Gestisce una transazione di firma.
 *
 * @author esasdelli
 *
 */
class AttiGestoreTransazioneFirma {

	private static final Logger log = Logger.getLogger(AttiGestoreTransazioneFirma.class);

	FirmaDigitaleService	firmaDigitaleService
	LinkGenerator 			grailsLinkGenerator
	SpringSecurityService 	springSecurityService

	private String	urlFirma            = null
	private long 	idTransazioneFirma  = -1

	FirmaDigitaleTransazione getTransazioneAttiva () {
		return FirmaDigitaleTransazione.get(idTransazioneFirma)
	}

	void clear () {
		idTransazioneFirma = -1
	}

	private void creaTransazione () {
		if (idTransazioneFirma < 0) {
			idTransazioneFirma = firmaDigitaleService.iniziaNuovaTransazione(springSecurityService.currentUserId)
		}
	}
	
	void addFileDaFirmare (String idRiferimento, String filename, InputStream is) {
		creaTransazione()
		firmaDigitaleService.aggiungiFileDaFirmare(idTransazioneFirma, idRiferimento, filename, is)
	}

	void addFileDaFirmare (String idRiferimento, IDocumentoEsterno documentoEsterno, IFileAllegato fileAllegato) {
		creaTransazione()
		firmaDigitaleService.aggiungiFileDaFirmare(idTransazioneFirma, idRiferimento, fileAllegato.nome, documentoEsterno.idDocumentoEsterno)
	}

	/**
	 * Termina la transazione di firma costruendo l'url della popup di firma digitale
	 * @return l'url della popup di firma digitale
	 */
	String finalizzaTransazioneFirma () {
		verificaAlmenoUnFileDaFirmare ()
		String codiceFiscale = calcolaCodiceFiscale ()

		this.urlFirma = firmaDigitaleService.getURLFirma(idTransazioneFirma, FirmaDigitaleURLConfig.configuraFirmaDigitale(getUrlFineFirma()).setFirmaHash(Impostazioni.FIRMA_HASH.abilitato).setTimeEnabled(Impostazioni.FIRMA_CON_TIMESTAMP.abilitato).setCodiceFiscale(codiceFiscale))

		log.debug ("Url popup di firma: ${urlFirma}")
		return this.urlFirma
	}

	/**
	 * Termina la transazione di firma costruendo l'url della popup di firma remota
	 * @return l'url della popup di firma remota
	 */
	String finalizzaTransazioneFirmaRemota () {
		verificaAlmenoUnFileDaFirmare ()
		String codiceFiscale = calcolaCodiceFiscale ()

		this.urlFirma = firmaDigitaleService.getURLFirma(idTransazioneFirma, FirmaDigitaleURLConfig.configuraFirmaDigitale(getUrlFineFirma()).setFirmaRemota(true).setFirmaHash(Impostazioni.FIRMA_HASH.abilitato).setTimeEnabled(Impostazioni.FIRMA_CON_TIMESTAMP.abilitato).setCodiceFiscale(codiceFiscale))
		log.debug ("Url popup di firma: ${urlFirma}");

		return this.urlFirma;
	}

	/**
	 * Termina la transazione di firma costruendo l'url della popup di firma remota pdf
	 * @return l'url della popup di firma remota pdf
	 */
	String finalizzaTransazioneFirmaRemotaPdf () {
		verificaAlmenoUnFileDaFirmare ()
		String codiceFiscale = calcolaCodiceFiscale ()

		this.urlFirma = firmaDigitaleService.getURLFirma(idTransazioneFirma, FirmaDigitaleURLConfig.configuraFirmaPDF(getUrlFineFirma(), false, null, -1).setFirmaRemota(true).setCodiceFiscale(codiceFiscale))
		log.debug ("Url popup di firma: ${urlFirma}");

		return this.urlFirma;
	}

	/**
	 * Termina la transazione di firma costruendo l'url della popup di fine firma.
	 * @return l'url della popup di fine firma (non passa dalla popup di firma digitale)
	 */
	String finalizzaTransazioneFirmaAutografa () {
		verificaAlmenoUnFileDaFirmare ()

		FirmaDigitaleTransazione transazione = FirmaDigitaleTransazione.get(idTransazioneFirma)

		this.urlFirma = getUrlFineFirma()+"?idTransazioneFirma="+transazione.id
		log.debug ("Url popup di firma autografa: ${urlFirma}")

		transazione.dataFirma 	= new Date()
		transazione.dateCreated = new Date()
		transazione.lastUpdated = new Date()
		transazione.save()
		// inserito il refresh della transazione per ricaricare il blob degli allegati
		transazione.refresh()
		for (FirmaDigitaleFile file : transazione.firmaFile) {
			file.nomeFirmato = file.nome
			file.save()

            FileAllegato fileAllegato = AttiFirmaService.getFileAllegatoIdRiferimento(file.idRiferimentoFile)
            if (fileAllegato != null){
                fileAllegato.firmato = true;
                fileAllegato.save()
            }
		}

		return this.urlFirma
	}

	String getUrlFirma() {
		return this.urlFirma;
	}

	private String getUrlFineFirma () {
		def securityToken = secureRequestParams (idTransazioneFirma, springSecurityService.currentUser.nominativo, springSecurityService.principal.amministrazione.codice)
		return grailsLinkGenerator.link (absolute:true, controller: 'fineFirma', action: 'index', params:securityToken)
	}
	
	def secureRequestParams (long idTransazioneFirma, String nominativoUtenteAd4, String codiceEnte) {
		return [idTransazioneFirma:idTransazioneFirma, 
				utente:	nominativoUtenteAd4, 
				ente:	codiceEnte, 
				token: 	WSPasswordEncrypter.encrypt("${idTransazioneFirma}+${nominativoUtenteAd4}+${codiceEnte}")]
	}
	
	void autenticaConToken (long idTransazioneFirma, String nominativoUtenteAd4, String codiceEnte, String token) {
		if (!secureRequestParams(idTransazioneFirma, nominativoUtenteAd4, codiceEnte).token.equals(token)) {
			throw new AttiRuntimeException ("Attenzione: non è possibile autenticare l'operazione.")	
		}
		
		AttiUtils.eseguiAutenticazione (nominativoUtenteAd4, codiceEnte)
	}

	void verificaAlmenoUnFileDaFirmare () {
		if (firmaDigitaleService.getFileTransazione (idTransazioneFirma).size() == 0) {
			throw new AttiRuntimeException("Non è possibile proseguire con la firma perché non sono presenti file da firmare.")
		}
	}

	private String calcolaCodiceFiscale() {
		if (Impostazioni.FIRMA_CON_CODICE_FISCALE.abilitato) {
			As4SoggettoCorrente sogg = As4SoggettoCorrente.findByUtenteAd4(springSecurityService.currentUser)
			if (sogg == null || sogg.codiceFiscale == null) {
				throw new AttiRuntimeException("Non è possibile proseguire con la firma perché non è configurato il codice fiscale per l'utente firmatario.")
			}
			return sogg.codiceFiscale?.toUpperCase()
		}
		return null
	}
}
