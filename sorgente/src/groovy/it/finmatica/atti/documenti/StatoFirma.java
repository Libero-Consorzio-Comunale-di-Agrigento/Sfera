package it.finmatica.atti.documenti;

public enum StatoFirma {
	  DA_NON_FIRMARE			// indica che il documento non è da firmare
	, DA_FIRMARE				// indica che il documento è da firmare
	, IN_FIRMA					// indica che il documento è in fase di firma ma questa non è ancora conclusa,
								//  oppure che c'è stato un errore nella firma prima ancora di caricare i file firmati su db
	, FIRMATO_DA_SBLOCCARE		// indica che c'è stato un errore durante l'operazione di firma:
								//  dopo aver già salvato i documenti firmati su db,
								//  ma prima di procedere con l'operazione di sblocco del flusso.
	, FIRMATO					// indica che il documento è stato firmato
//	, FIRMATO_DA_FIRMARE		// indica che il documento è stato firmato ed è da firmare nuovamente.
}
