package it.finmatica.atti.documenti.beans

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IFileAllegato
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.TipologieFirma
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
	private String  tipoFirma           = null

	FirmaDigitaleTransazione getTransazioneAttiva () {
		return FirmaDigitaleTransazione.get(idTransazioneFirma)
	}

	void clear () {
		idTransazioneFirma = -1
		tipoFirma           = null
	}

	private void creaTransazione () {
		if (idTransazioneFirma < 0) {
			idTransazioneFirma = firmaDigitaleService.iniziaNuovaTransazione(springSecurityService.currentUserId)
		}
	}

	private void creaTransazione (String pTipoFirma) {
		if (idTransazioneFirma < 0) {
			idTransazioneFirma = firmaDigitaleService.iniziaNuovaTransazione(springSecurityService.currentUserId)
			tipoFirma = pTipoFirma
		}
		else {
			if (!(tipoFirma.equals(pTipoFirma))) {
				throw new AttiRuntimeException("Non è possibile proseguire con la firma perché sono presenti file che richiedono tipologia di firma differente (Pades o Cades).")
			}
		}
	}

	//MIC (11/06/2024): non ho capito chi lo utilizza
	void addFileDaFirmare (String idRiferimento, String filename, InputStream is) {
		creaTransazione()
		filename=calcolaNomeFilePades(filename)
		firmaDigitaleService.aggiungiFileDaFirmare(idTransazioneFirma, idRiferimento, filename, is)
	}

	//MIC (11/06/2024): non ho capito chi lo utilizza
	void addFileDaFirmare (String idRiferimento, IDocumentoEsterno documentoEsterno, IFileAllegato fileAllegato) {
		creaTransazione()
		String nomeFile = calcolaNomeFilePades(fileAllegato.nome)
		firmaDigitaleService.aggiungiFileDaFirmare(idTransazioneFirma, idRiferimento, nomeFile, documentoEsterno.idDocumentoEsterno)
	}

	//aggiunge file alla firma (richiamato da AttiFirmaService e InformazioniUtenetViewModel)
	void addFileDaFirmare (String idRiferimento, IDocumentoEsterno documentoEsterno, IFileAllegato fileAllegato,int numFirma, String tipoFirma) {
		creaTransazione(tipoFirma)
		String nomeFile = calcolaNomeFilePades(fileAllegato.nome)
		firmaDigitaleService.aggiungiFileDaFirmare(idTransazioneFirma, idRiferimento, nomeFile, documentoEsterno.idDocumentoEsterno,numFirma)
	}

	String calcolaNomeFilePades(String filename) {
		//se la FIRMA_PADES è abilitata ed esiste un SUFFISSO e il nomeFile non contiene questo suffisso ( per evitare che scriva due volte -signed)
		try {
			if (TipologieFirma.PDF.equals(tipoFirma) //Impostazioni.FIRMA_PADES.abilitato
					&& Impostazioni.SUFFISSO_FIRMA_PADES.valore != null
					&& Impostazioni.SUFFISSO_FIRMA_PADES.valore.length() > 0
					&& !Impostazioni.SUFFISSO_FIRMA_PADES.valore.equals("-")
					&& filename.indexOf(Impostazioni.SUFFISSO_FIRMA_PADES.valore) < 0
			) {
				String nome = filename.substring(0, filename.lastIndexOf("."))
				String estensione = filename.substring(filename.lastIndexOf("."))
				// concateno nomeFile senza estensione con il suffisso e l'estensione
				// filename = NOMEFILE + SUFF + ESTENSIONE
				filename = nome + Impostazioni.SUFFISSO_FIRMA_PADES.valore + estensione
			}
		}catch(Exception ex){
			log.error("Errore nella creazione del nomeFile per firma Pades: "+ex.getMessage())
		}
		return filename
	}


	/**
	 * Termina la transazione di firma costruendo l'url della popup di firma digitale
	 * @return l'url della popup di firma digitale
	 */
	String finalizzaTransazioneFirma () {

		verificaAlmenoUnFileDaFirmare ()
		String codiceFiscale = calcolaCodiceFiscale ()
		FirmaDigitaleURLConfig configurazione = FirmaDigitaleURLConfig.configuraFirmaDigitale(getUrlFineFirma()).setFirmaHash(Impostazioni.FIRMA_HASH.abilitato).setTimeEnabled(Impostazioni.FIRMA_CON_TIMESTAMP.abilitato).setCodiceFiscale(codiceFiscale)
		//if (Impostazioni.FIRMA_PADES.abilitato) {
		if (TipologieFirma.PDF.equals(tipoFirma)) {
			configurazione.setTipoFirma(FirmaDigitaleURLConfig.TipoFirma.PDF).setFirmaPDFVisibile(Impostazioni.FIRMA_PADES_VISIBILE.abilitato).setFirmaPDFPosizione(FirmaDigitaleURLConfig.PosizioneFirmaPdf.BASSO_DX).setFirmaHash(false)
		}

		this.urlFirma = firmaDigitaleService.getURLFirma(idTransazioneFirma, configurazione)

		log.info ("Url popup di firma: ${urlFirma}")
		return this.urlFirma
	}

	/**
	 * Termina la transazione di firma costruendo l'url della popup di firma remota
	 * @return l'url della popup di firma remota
	 */
	String finalizzaTransazioneFirmaRemota () {
		verificaAlmenoUnFileDaFirmare ()
		String codiceFiscale = calcolaCodiceFiscale ()
		FirmaDigitaleURLConfig configurazione = FirmaDigitaleURLConfig.configuraFirmaDigitale(getUrlFineFirma()).setFirmaRemota(true).setFirmaHash(Impostazioni.FIRMA_HASH.abilitato).setTimeEnabled(Impostazioni.FIRMA_CON_TIMESTAMP.abilitato).setCodiceFiscale(codiceFiscale)

		this.urlFirma = firmaDigitaleService.getURLFirma(idTransazioneFirma, configurazione)
		//if (Impostazioni.FIRMA_PADES.abilitato) {
		if (TipologieFirma.PDF.equals(tipoFirma)) {
			configurazione.setTipoFirma(FirmaDigitaleURLConfig.TipoFirma.PDF).setFirmaPDFVisibile(Impostazioni.FIRMA_PADES_VISIBILE.abilitato).setFirmaPDFPosizione(FirmaDigitaleURLConfig.PosizioneFirmaPdf.BASSO_DX).setFirmaHash(false)
		}
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
