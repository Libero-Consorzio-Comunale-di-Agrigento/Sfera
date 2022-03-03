package it.finmatica.atti.dto.documenti

import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.AllegatoService
import it.finmatica.atti.documenti.FileFirmatoDettaglioService
import it.finmatica.atti.documenti.StampaUnicaService
import it.finmatica.atti.documenti.competenze.*
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.ricercadocumenti.AllegatoEsterno
import org.zkoss.zk.ui.util.Clients

class AllegatoDTOService {

	IGestoreFile gestoreFile
	StampaUnicaService stampaUnicaService
	AllegatoService allegatoService
	FileFirmatoDettaglioService fileFirmatoDettaglioService

	AllegatoDTO importaAllegatiEsterni (AllegatoDTO allegatoDTO, List<AllegatoEsterno> allegatiEsterni) {

		// per prima cosa salvo l'allegato (serve soprattutto se non l'ho mai salvato prima)
		Allegato allegato = salva (allegatoDTO)

		// Caricamento dei file selezionati
		for (AllegatoEsterno allegatoEsterno : allegatiEsterni) {

			FileAllegato fileAllegato = new FileAllegato()
			fileAllegato.contentType = allegatoEsterno.contentType
			fileAllegato.nome = getNomeFileUnivoco (allegato?.id, allegatoEsterno.nome)
			fileAllegato.dimensione 	= -1
			fileAllegato.modificabile	= false

			allegato.addToFileAllegati(fileAllegato)
			allegato.save()

			gestoreFile.addFile(allegato, fileAllegato, gestoreFile.getFile(allegatoEsterno.getDocumentoEsterno(), allegatoEsterno))

		}

		// verifico la dimensione del file
		controllaDimensioneFilePerStampaUnica (allegato)

		return allegato?.toDTO()
	}

	public AllegatoDTO uploadFile (AllegatoDTO allegatoDTO, String nomeFile, String contentType, InputStream inputStream) {

        // per prima cosa salvo l'allegato (serve soprattutto se non l'ho mai salvato prima)
		Allegato allegato = salva (allegatoDTO)

        // carico il file
		uploadFile(allegato, getNomeFileUnivoco (allegato.id, nomeFile), contentType, inputStream)

        // verifico la dimensione del file
        controllaDimensioneFilePerStampaUnica (allegato)

		return allegato.toDTO()
	}

	public long uploadFile (Allegato allegato, String nomeFile, String contentType, InputStream inputStream) {
		FileAllegato fileAllegato 	= new FileAllegato()
		fileAllegato.nome 			= nomeFile
		fileAllegato.contentType 	= contentType
		fileAllegato.dimensione 	= -1
		fileAllegato.modificabile	= false;

		allegato.addToFileAllegati(fileAllegato)
		allegato.save()

		gestoreFile.addFile(allegato, fileAllegato, inputStream)

        // Verifichiamo che il file sia firmato, nel qual caso aggiungiamo i dettagli delle firme
        if (Impostazioni.ALLEGATO_VERIFICA_FIRMA.abilitato) {
			fileFirmatoDettaglioService.estraiInformazioneFileFirmato(allegato, fileAllegato)
        }
		return fileAllegato.dimensione
	}

	public String getNomeFileUnivoco (long idAllegato, String nomeFile) {
		// ottengo l'estensione del file:
		String estensione 	= ""
		String basename		= ""
		
		// conto i file che hanno il nome richiesto:
		String nome = nomeFile
		int numero = Allegato.numeroFilePerNome(idAllegato, nome).get()
		
		// se esiste già un file con questo nome, rinomino e ne creo un altro:
		int counter = 1
		while (numero > 0) {
			nome = nomeFile.replaceAll(/(\..+)$/, "(${counter})\$1")
			counter++
			numero = Allegato.numeroFilePerNome(idAllegato, nome).get()
		}
		
		return nome
	}

	public Allegato salva (AllegatoDTO allegatoDTO) {
		// per prima cosa salvo l'allegato:
		Allegato allegato = allegatoDTO.getDomainObject()?:new Allegato();
		boolean primoSalvataggio = false
		if (allegato.id <= 0) {
			primoSalvataggio = true
		}

		allegato.determina 	  		= allegatoDTO.determina?.domainObject
		allegato.propostaDelibera 	= allegatoDTO.propostaDelibera?.domainObject
		allegato.delibera			= allegatoDTO.delibera?.domainObject
		allegato.vistoParere		= allegatoDTO.vistoParere?.domainObject
		allegato.tipoAllegato       = allegatoDTO.tipoAllegato?.domainObject

		allegato.titolo                 = allegatoDTO.titolo
		allegato.descrizione            = allegatoDTO.descrizione
		allegato.statoFirma             = allegatoDTO.statoFirma

		allegato.quantita	            = allegatoDTO.quantita
		allegato.numPagine	            = allegatoDTO.numPagine
		allegato.sequenza 	            = allegatoDTO.sequenza

		allegato.stampaUnica            = allegatoDTO.stampaUnica
		allegato.riservato	            = allegatoDTO.riservato
		allegato.pubblicaCasaDiVetro	= allegatoDTO.pubblicaCasaDiVetro
		allegato.pubblicaVisualizzatore = allegatoDTO.pubblicaVisualizzatore
		allegato.pubblicaAlbo	  		= allegatoDTO.pubblicaAlbo

		allegato.ubicazione  = allegatoDTO.ubicazione
		allegato.origine	 = allegatoDTO.origine
		allegato.codice		 = allegatoDTO.codice

		allegato = allegato.save()

		if (primoSalvataggio) {
			// leggo le competenze dal documentocompetenza
			def listaCompetenze

			if (allegatoDTO.determina != null) {
				listaCompetenze = DeterminaCompetenze.createCriteria().list() {
					eq("determina.id", allegatoDTO.determina.id)
				}
			} else if (allegatoDTO.delibera != null) {
				listaCompetenze = DeliberaCompetenze.createCriteria().list() {
					eq("delibera.id", allegatoDTO.delibera.id)
				}
			} else if (allegatoDTO.propostaDelibera != null) {
				listaCompetenze = PropostaDeliberaCompetenze.createCriteria().list() {
					eq("propostaDelibera.id", allegatoDTO.propostaDelibera.id)
				}
			} else if (allegatoDTO.vistoParere != null) {
				listaCompetenze = VistoParereCompetenze.createCriteria().list() {
					eq("vistoParere.id", allegatoDTO.vistoParere.id)
				}
			}

			// le copio in allegato competenza
			for (i in listaCompetenze) {
				AllegatoCompetenze al = new AllegatoCompetenze ()
				al.allegato = allegato
				al.lettura 			= i.lettura
				al.modifica			= i.modifica
				al.cancellazione 	= i.cancellazione
				al.utenteAd4        = i.utenteAd4
				al.ruoloAd4         = i.ruoloAd4
				al.unitaSo4         = i.unitaSo4
				al.cfgCompetenza	= i.cfgCompetenza
				al.save()
			}
		}

        controllaDimensioneFilePerStampaUnica (allegato)

        allegatoService.riordinaAllegati(allegato.documentoPrincipale)

		return allegato
	}

    public void controllaDimensioneFilePerStampaUnica (Allegato allegato) {
        // controllo la dimensione massima di tutti i file caricati:
        long dimensioneFile = stampaUnicaService.sommaDimensioneAllegati(allegato.documentoPrincipale)
        if (allegato.stampaUnica && dimensioneFile > (Impostazioni.STAMPA_UNICA_DIMENSIONE_MASSIMA.valoreInt * 1_000_000)) {
            allegato.stampaUnica = false
            allegato.save()

            String limite = Impostazioni.STAMPA_UNICA_DIMENSIONE_MASSIMA.valoreInt + "MB"
            String dimensione = new Double(dimensioneFile / 1_000_000).round(2) + "MB"
            Clients.showNotification("Allegato rimosso dalla Stampa Unica perché la dimensione totale dei file (${dimensione}) supera il limite consentito (${limite}).", Clients.NOTIFICATION_TYPE_WARNING, null, "middle_center", 5000, true);
        }

        // controllo la dimensione dei singoli file caricati:
        long fileTroppoGrandi = Allegato.createCriteria().count {
            createAlias("fileAllegati", "fa")
            gte ("fa.dimensione", (long)(Impostazioni.STAMPA_UNICA_DIMENSIONE_MASSIMA_ALLEGATI.valoreInt * 1_000_000))
            eq ("id", allegato.id)
        }

        if (allegato.stampaUnica && fileTroppoGrandi > 0) {
            allegato.stampaUnica = false
            allegato.save()

            String limite = Impostazioni.STAMPA_UNICA_DIMENSIONE_MASSIMA_ALLEGATI.valoreInt + "MB"
            Clients.showNotification("Allegato rimosso dalla Stampa Unica perché sono presenti n. ${fileTroppoGrandi} file che superano il limite consentito di ${limite}.", Clients.NOTIFICATION_TYPE_WARNING, null, "middle_center", 5000, true);
        }
    }

	public void elimina (AllegatoDTO allegatoDto, def documentoPrincipaleDto) {
		allegatoService.elimina (allegatoDto.domainObject, documentoPrincipaleDto.domainObject);
	}

	public void eliminaFileAllegato (AllegatoDTO allegatoDTO, long idFileAllegato) {
		Allegato allegato = allegatoDTO.getDomainObject()
		FileAllegato fileAllegato = FileAllegato.get(idFileAllegato)
		allegato.removeFromFileAllegati(fileAllegato)
		gestoreFile.removeFile(allegato, fileAllegato)
	}
}
