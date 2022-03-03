package it.finmatica.gestionetesti.ui.dizionari

import it.finmatica.atti.documenti.tipologie.GestioneTestiModelloCompetenza
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO

class GestioneTestiModelloDTOService {


	public GestioneTestiModelloDTO salva(GestioneTestiModelloDTO gestioneTestiModelloDto, byte[] file = null, String nomeFile = null) {
		GestioneTestiModello gestTesti = gestioneTestiModelloDto.getDomainObject()?:new GestioneTestiModello()
		gestTesti.nome        = gestioneTestiModelloDto.nome
		gestTesti.descrizione = gestioneTestiModelloDto.descrizione
		gestTesti.tipoModello = gestioneTestiModelloDto.tipoModello.getDomainObject()
		gestTesti.valido      = gestioneTestiModelloDto.valido

		if (file != null) {
			// vuol dire che è stato fatto l'upload (oppure duplica) e quindi devo inserire il nuovo file nel campo template
			gestTesti.fileTemplate = file
			gestTesti.tipo = nomeFile.substring(nomeFile.lastIndexOf(".")+1);
		}

		gestTesti = gestTesti.save ()

		GestioneTestiModelloDTO gestTestiDto = gestTesti.toDTO()
		gestTestiDto.fileTemplate = null
		return 	gestTestiDto
	}

	public byte[] getFileAllegato (Long id) {
		byte[] result = GestioneTestiModello.createCriteria().get() {
			projections { 
				property("fileTemplate") 
			}
			
			eq("id", id)
		}
		return result
	}

	public void elimina(GestioneTestiModelloDTO gestioneTestiModelloDto) {
		GestioneTestiModello gestTesti = gestioneTestiModelloDto.getDomainObject()
		// controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore
		if (gestTesti.version != gestioneTestiModelloDto.version) {
			throw new RuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		}
		
		// elimino tutte le competenze di questo modello
		GestioneTestiModelloCompetenza.findAllByGestioneTestiModello(gestTesti)*.delete()
		
		// quindi elimino il modello vero e proprio
		gestTesti = gestTesti.delete ()
	}

	public GestioneTestiModelloDTO duplica (GestioneTestiModelloDTO gestioneTestiModelloDTO) {
		GestioneTestiModello GestioneTestiModello = gestioneTestiModelloDTO.domainObject;

		byte[] fileAllegato = getFileAllegato(gestioneTestiModelloDTO.id)
		String nomeFileAllegato = gestioneTestiModelloDTO.nomeFile

		gestioneTestiModelloDTO.id = -1;
		gestioneTestiModelloDTO.version = 0;
		gestioneTestiModelloDTO.nome += " (duplica)";
		GestioneTestiModello duplica = salva(gestioneTestiModelloDTO, fileAllegato, nomeFileAllegato).domainObject;
		return duplica.toDTO();
	}

	public List<GestioneTestiModelloDTO> getListaModelli (def tipiModello, boolean conNessuno = false) {

		tipiModello = ((tipiModello instanceof String)?[tipiModello]:tipiModello)

		def listaModelli = [];
		if (conNessuno) {
			listaModelli << new GestioneTestiModelloDTO(id:-1, nome: "-- nessuno --", descrizione: "-- nessuno --");
		}
		def modelli = GestioneTestiModello.createCriteria().list {
			projections {
				property ("id")
				property ("nome")
				property ("descrizione")
			}
			or {
    			for (String tipoModello : tipiModello) {
    				like ("tipoModello.codice", tipoModello+"%")
    			}
			}
			eq ("valido", true)

			order("nome", "asc")
		}

		for (def row : modelli) {
			listaModelli << new GestioneTestiModelloDTO(id:row[0], nome:row[1], descrizione:row[2]);
		}

		return listaModelli;
	}
}
