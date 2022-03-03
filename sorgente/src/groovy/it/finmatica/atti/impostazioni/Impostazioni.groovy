package it.finmatica.atti.impostazioni

import org.zkoss.util.resource.Labels

enum Impostazioni {

	// aggiungere qui i codici delle impostazioni
	AGGIORNAMENTO_IN_CORSO				("Impedisce l'accesso all'applicativo a tutti gli utenti tranne all'utente di amministrazione. Può contenere i valori Y/N oppure una data nel formato dd/MM/yyyy hh:mm.", "AGGIORNAMENTO IN CORSO", "N", null, false),
	ENTI_SO4							("Elenco enti (separati da #) gestiti dall'applicativo per installazione", "ENTI_SO4", "NESSUNO", null, false),
	OTTICA_SO4 							("Ottica di SO4 utilizzata per l'amministrazione", "OTTICA SO4", "IST", null, false),
	DB_CHARSET							("Charset del database", "DB CHARSET", "windows-1252", '<rowset><row label="Caratteri latini senza euro (iso-8859-1)" value="iso-8859-1" /><row label="Caratteri latini con euro (iso-8859-15)" value="iso-8859-15" /><row label="Tutti i caratteri (utf-8)" value="utf-8" /><row label="Caratteri Windows-Compatibili" value="windows-1252" /></rowset>', true),
	GESTORE_FILE						("Indica dove devono stare i files, se su GDM o su AGSDE2.", "GESTIONE FILE", "gdmGestoreFile", '<rowset><row label="AGSDE2" value="attiGestoreFile" /><row label="GDM" value="gdmGestoreFile" /></rowset>', true),
	REGISTRO_PROPOSTE 					("Codice per il registro delle proposte", "RegistroProposte", "PROP", null, true),
	RUOLO_ACCESSO_APPLICATIVO 			("Ruolo di accesso alla applicazione", "RUOLO ACCESSO APPLICATIVO", "AGD", null, true),
	RUOLO_SO4_FIRMATARIO_CERT_PUBB 		("Ruolo che identifica i firmatari dei certificati di pubblicazione e avvenuta pubblicazione", "Ruolo del firmatario dei Certificati", "AGDCERTF", null, true),
	RUOLO_SO4_ODG 						("Ruolo di accesso alla sezione 'Gestione Sedute'", "RUOLO SO4 GESTIONE SEDUTE", "AGDATTI", null, true),
	RUOLO_SO4_CREA_DETERMINA			("Ruolo per l'abilitazione alla creazione di una determina", "RUOLO SO4 CREA DETERMINA", "AGDRED", null, true),
	RUOLO_SO4_CREA_PROPOSTA_DELIBERA	("Ruolo per l'abilitazione alla creazione di una proposta di delibera", "RUOLO SO4 CREA PROPOSTA DELIBERA", "AGDRED", null, true),
	RUOLO_SO4_CONSERVAZIONE				("Ruolo di accesso alla sezione 'Conservazione'", "RUOLO SO4 CONSERVAZIONE", "AGDCONS", null, true),
	RUOLO_SO4_DIZIONARI_ATTI			("Ruolo di accesso alla sezione 'Dizionari:Atti'", "RUOLO SO4 DIZIONARI ATTI", "AGDATTI", null, true),
	RUOLO_SO4_DIZIONARI_ODG				("Ruolo di accesso alla sezione 'Dizionari:Ordine del giorno'", "RUOLO SO4 DIZIONARI ODG", "AGDATTI", null, true),
	RUOLO_SO4_DIZIONARI_IMPOSTAZIONI	("Ruolo di accesso alla sezione 'Dizionari:Impostazioni'", "RUOLO SO4 DIZIONARI IMPOSTAZIONI", "AGDATTI", null, true),
	RUOLO_SO4_NOTIFICHE					("Ruolo per la ricezione delle notifiche", "RUOLO SO4 NOTIFICHE", "AGDNOTI", null, true),
	RUOLO_SO4_ORGANI_CONTROLLO			("Ruolo per l'invio delle notifiche agli organi di controllo", "RUOLO SO4 ORGANI CONTROLLO", "AGDATTI", null, true),
	RUOLO_SO4_CONTROLLO_REGOLARITA		("Ruolo per il controllo di regolarita", "RUOLO SO4 CONTROLLO REGOLARITA", "AGDATTI", null, true),
	RUOLO_SO4_FIRMA						("Ruolo per la firma", "RUOLO SO4 FIRMA", "AGDFIRMA", null, true),
	RUOLO_SO4_FIRMATARIO_DECRETI		("Ruolo per il componente di default per la firma dei decreti", "RUOLO SO4 FIRMA DECRETI", "AGDDECF", null, true),
	RUOLO_SO4_RESPONSABILE				("Ruolo che identifica il responsabile di una UO", "RUOLO RESPONSABILE", "AGDRESP", null, true),
	RUOLO_SO4_FIRMATARIO_CERT_ESEC		("Ruolo che identifica il firmatario default dei certificati di esecutività", "RUOLO CERTIFICATO ESECUTIVITA", "AGDESECF", null, true),
	RUOLO_SO4_RISERVATO_DETE			("Ruolo di accesso alle Determine Riservate", "RUOLO DI ACCESSO DETERMINE RISERVATE", "AGDRISER", null, true),
	RUOLO_SO4_RISERVATO_DELI			("Ruolo di accesso alle Delibere Riservate", "RUOLO DI ACCESSO DELIBERE RISERVATE", "AGDRISER", null, true),
	RUOLO_SO4_CALCOLO_STRUTTURA			("Ruolo di so4 utilizzato da una particolare regola di calcolo", "RUOLO SO4 CALCOLO STRUTTURA", "AGD", null, true),
	RUOLO_SO4_MARCATURA					("Ruolo di accesso alla marcatura temporale degli atti", "RUOLO DI ACCESSO MARCATURA", "AGDMARCA", null, true),
	ODG_RUOLI_CONVOCATI 				("Elenco ruoli (separati da #) per i possibili convocati ad una seduta", "ELENCO RUOLI SO4 - CONVOCATI IN SEDUTA", "AGDATTI#AGDFIRMA#AGP#MESSISUP", null, true),
	ODG_MODIFICA_OGGETTO_PROPOSTA 		("Specifica se il campo oggetto della seduta sia modificabile", "GESTIONE OGGETTO SEDUTA", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	ODG_RUOLI_NOTIFICA_VERBALIZZAZIONE	("Elenco ruoli (separati da #) per i possibili soggetti di una determinata unità", "ELENCO RUOLI SO4 - NOTIFICA VERBALIZZAZIONE", "AGDNOTI", null, true),
	ODG_FORMATO_STAMPE					("Indica il formato delle stampe (ODT, DOC, DOCX o PDF)", "ODG FORMATO STAMPE", "odt", '<rowset><row label="ODT" value="odt" /><row label="DOCX" value="docx" /><row label="DOC" value="doc" /><row label="PDF" value="pdf" /></rowset>', true),
	ODG_CALCOLO_GETTONE_PER_SEDUTA      ("Specifica se il calcolo del gettone di presenza è per seduta o per presenza giornaliera (se ci sono due sedute della stessa commissione nella stessa data se il valore è Y allora vengono calcolati 2 gettoni, altrimenti 1", "ODG_CALCOLO_GETTONE_PER_SEDUTA", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	ODG_MOSTRA_ASSENTI_NON_GIUSTIFICATI ("Indica se mostrare o meno la colonna 'Assenti Non Giustificati' in ODG.", "MOSTRA ASSENTI NON GIUSTIFICATI", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	ODG_MOSTRA_VOTO_PRESIDENTE			("Indica se mostrare o meno il voto del presidente in Seduta.", "MOSTRA VOTO PRESIDENTE", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	ODG_ELENCO_REGISTRI_INFORMATIVE		("Elenco dei registri che contraddistinguono le informative (separati da #)", "ELENCO REGISTRI INFORMATIVE", "-", null, true),
	ODG_GETTONE_PRESENZA_ATTIVO			("Indica se è abilitata la gestione del gettone presenza.", "ODG_GETTONE_PRESENZA_ATTIVO", "Y", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	ODG_STAMPA_UNICA					("Indica se è abilitata la stampa unica delle proposte della seduta.", "ODG STAMPA 	UNICA", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	NOTE_CONTABILI 						("Definisce se devono essere visualizzabili le note contabili oppure no", "NOTE CONTABILI", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	NOTE_TRASMISSIONE_PUBBLICHE			("Definisce se le note di trasmissione devono essere pubbliche oppure no", "NOTE TRASMISSIONE PUBBLICHE", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	ALLEGATO_FORMATI_POSSIBILI 			("Definisce tutti i formati possibili che possono essere allegati, la sintassi è formato1#formato2#formato3", "ALLEGATO FORMATI POSSIBILI", "odt#doc#docx#xls#xlsx#pdf#jpg#jpeg#png#bmp#gif#p7m", null, true),
	ALLEGATO_DIMENSIONE_MASSIMA 		("Definisce la dimensione massima dell allegato espressa in MB", "ALLEGATO DIMENSIONE MASSIMA", "25", null, true),
	ALLEGATO_VERIFICA_FIRMA				("Definisce se è abilitata la verifica della firma dei file allegati", "ALLEGATO VERIFICA FIRMA", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	ALLEGATO_VERIFICA_NOMEFILE			("Definisce se è abilitata la verifica del nome dei file allegati", "ALLEGATO VERIFICA NOME FILE", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	ALLEGATO_CONVERTI_PDF	 			("Definisce se è abilitata la conversione degli allegati in pdf", "ALLEGATO CONVERTI PDF", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	ALLEGATO_CONVERTI_PDF_FORMATO		("Definisce quali formati di allegati vengono convertiti in pdf (odt, xls, doc), la sintassi è formato1#formato2#formato3", "ALLEGATO CONVERTI PDF FORMATO", "odt#doc#xls", null, true),
	STAMPA_UNICA_DIMENSIONE_MASSIMA_ALLEGATI("Definisce la dimensione massima (MB) del file inseribile in stampa unica.", "STAMPA UNICA DIMENSIONE MASSIMA ALLEGATO", "5", null, true),
	STAMPA_UNICA_DIMENSIONE_MASSIMA 		("Definisce la dimensione massima (MB) della somma di tutti i file inseribili in stampa unica.", "STAMPA UNICA DIMENSIONE MASSIMA", "30", null, true),
	PROTOCOLLO 							("Integrazione con il protocollo.", "PROTOCOLLO", "N", '<rowset><row label="Nessuno"  value="N" /><row label="Protocollazione nativa GDM"  value="protocolloEsternoGdm" /><row label="Protocollazione DOCArea GDM" value="protocolloEsternoGdmDocArea" /><row label="Protocollazione DOCArea GS4" value="protocolloEsternoGs4DocArea" /><row label="Protocollazione DOC/ER" value="protocolloEsternoDocer" /><row label="Protocollazione DOCArea via FTP" value="protocolloTreviso" /><row label="Protocollazione DOCArea via FTP (AMBIENTE DI TEST)" value="protocolloTrevisoTest" /><row label="Protocollazione DOCArea (Comune Modena)" value="protocolloComuneModena" /><row label="Protocollazione DOCArea (Da Mapping Integrazioni)" value="protocolloDocAreaDaImpostazioni" /><row label="Protocollazione IRIDE" value="protocolloIrideString" /><row label="Protocollazione SIAV" value="protocolloSiav" /></rowset>', true),
	PROTOCOLLO_ATTIVO					("Indica se è abilitata la protocollazione.", "PROTOCOLLO ATTIVO", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	PROTOCOLLO_MOSTRA_SEZIONE_ARCHIVIAZIONI ("Indica se visualizzare la sezione Archiviazioni del Protocollo", "PROTOCOLLO MOSTRA SEZIONE ARCHIVIAZIONI", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
    PROTOCOLLO_CREA_FASCICOLO			("Indica l'abilitazione a creare un fascicolo", "CREAZIONE FASCICOLO", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	PROTOCOLLO_CLASSIFICA_OBBL 			("Rende la classificazione obbligatoria al salvataggio della determina o della proposta", "CLASSIFICA OBBLIGATORIA PROTOCOLLO", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	PROTOCOLLO_FASCICOLO_OBBL 			("Rende la scelta del fascicolo obbligatoria al salvataggio della determina o della proposta", "FASCICOLO OBBLIGATORIO PROTOCOLLO", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	PROTOCOLLO_SEZIONE 					("Rende visibile o meno il folder protocollo nella determina o nella proposta", "SEZIONE PROTOCOLLO", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),

    EDITOR_DEFAULT						("Indica l'editor di default per i testi", "Editor default", "SOFFICE", '<rowset><row label="Open Office" value="SOFFICE" /><row label="MS Word" value="WORD" /><row label="Open Office (senza controllo chiusura)" value="SOFFICE.NOCHECK" /><row label="MS Word (senza controllo chiusura)" value="WORD.NOCHECK" /></rowset>', true),
	EDITOR_DEFAULT_PATH					("Indica il percorso sul client dove trovare l'eseguibile dell'editor di default. (utile per ambienti citrix)", "Path Editor default", "\"C:\\\\Program Files (x86)\\\\Microsoft Application Virtualization Client\\\\sfttray.exe\" \"OpenOffice.org Writer 3.4.9593.500\"", null, true),
	FILE_REPOSITORY_PATH                ("Indica il percorso dove trovare i file generati", "Path File Repository", "repository", null, true),
	FORMATO_DEFAULT						("Indica il formato di default per i testi", "Formato di default", "odt", '<rowset><row label=".doc" value="doc" /><row label=".docx" value="docx" /><row label=".odt" value="odt" /></rowset>', true),
	VIS_DETERMINE_NON_PUBBLICATE		("Indica se devono essere visualizzate le determine non pubblicate per gli utenti che si siano loggati", "VISUALIZZA DETERMINE NON PUBBLICATE", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	VIS_DELIBERE_NON_PUBBLICATE			("Indica se devono essere visualizzate le delibere non pubblicate per gli utenti che si siano loggati", "VISUALIZZA DELIBERE NON PUBBLICATE", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	VIS_STAMPA_UNICA_DETERMINE			("Indica se consentire il download di una stampa unica per il testo della determina", "VISUALIZZA STAMPA UNICA DETERMINE", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	VIS_STAMPA_UNICA_DELIBERE			("Indica se consentire il download di una stampa unica per il testo della delibera", "VISUALIZZA STAMPA UNICA DELIBERE", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	VIS_TESTO_FIRMATO_DETERMINE			("Indica se consentire il download del testo firmato della determina", "VISUALIZZA TESTO FIRMATO DETERMINE", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	VIS_TESTO_FIRMATO_DELIBERE			("Indica se consentire il download del testo firmato della delibera", "VISUALIZZA TESTO FIRMATO DELIBERE", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	VIS_DETERMINE_PUBBLICATE_DAL        ("Data di inizio intervallo visualizzazione determine pubblicate", "VISUALIZZA DETERMINE PUBBLICATE DAL", " ", null, true),
	VIS_DETERMINE_PUBBLICATE_AL         ("Data di fine intervallo visualizzazione determine pubblicate", "VISUALIZZA DETERMINE PUBBLICATE AL", " ", null, true),
	VIS_DELIBERE_PUBBLICATE_DAL         ("Data di inizio intervallo visualizzazione delibere pubblicate", "VISUALIZZA DELIBERE PUBBLICATE DAL", " ", null, true),
	VIS_DELIBERE_PUBBLICATE_AL          ("Data di fine intervallo visualizzazione delibere pubblicate", "VISUALIZZA DELIBERE PUBBLICATE AL", " ", null, true),
	VIS_PAGINA_UNICA_DETERMINE_PUBBLICATE_DAL("Data di inizio intervallo visualizzazione determine pubblicate nella pagina unica del visualizzatore", "VISUALIZZA PAGINA UNICA DETERMINE PUBBLICATE DAL", " ", null, true),
	VIS_PAGINA_UNICA_DETERMINE_PUBBLICATE_AL ("Data di fine intervallo visualizzazione determine pubblicate nella pagina unica del visualizzatore", "VISUALIZZA PAGINA UNICA DETERMINE PUBBLICATE AL", " ", null, true),
	VIS_PAGINA_UNICA_DELIBERE_PUBBLICATE_DAL ("Data di inizio intervallo visualizzazione delibere pubblicate nella pagina unica del visualizzatore", "VISUALIZZA PAGINA UNICA DELIBERE PUBBLICATE DAL", " ", null, true),
	VIS_PAGINA_UNICA_DELIBERE_PUBBLICATE_AL  ("Data di fine intervallo visualizzazione delibere pubblicate nella pagina unica del visualizzatore", "VISUALIZZA PAGINA UNICA DELIBERE PUBBLICATE AL", " ", null, true),
	VIS_PAGINA_UNICA_ATTIVA 			("Indica se è attiva la pagina Unica del Visualizzatore Atti.", "PAGINA UNICA VISUALIZZATORE ATTI", "N", "<rowset><row label=\"Si\" value=\"Y\" /><row label=\"No\" value=\"N\" /></rowset>", true),
	VIS_MOSTRA_CERTIFICATI				("Indica se devono essere visualizzati i certificati dei documenti.", "MOSTRA CERTIFICATI", "N", "<rowset><row label=\"Si\" value=\"Y\" /><row label=\"No\" value=\"N\" /></rowset>", true),
	VIS_MOSTRA_VISTI_PARERI				("Indica se devono essere visualizzati i visti/pareri dei documenti", "MOSTRA VISTI/PARERI", "N", "<rowset><row label=\"Si\" value=\"Y\" /><row label=\"No\" value=\"N\" /></rowset>", true),
    VIS_GESTIONE_PUBBLCAZIONE_ALLEGATI	("Indica se devono essere visualizzati gli allegati solo nel periodo di pubblizione (da configurare in tipologia)", "GESTIONE PUBBLICAZIONE ALLEGATI ", "N", "<rowset><row label=\"Si\" value=\"Y\" /><row label=\"No\" value=\"N\" /></rowset>", true),
    VIS_FILE_SEDUTA_PUBB				("Indica se devono essere visualizzati i testi della seduta anche ai non autenticati/convocati", "MOSTRA FILE SEDUTA PUBB", "N", "<rowset><row label=\"Si\" value=\"Y\" /><row label=\"No\" value=\"N\" /></rowset>", true),
	JWORKLIST							("Indica l'integrazione della scrivania virtuale", "JWORKLIST", "N", '<rowset><row label="Nessuno" value="N" /><row label="JWorklist" value="JWorklistService" /><row label="Smart Desktop" value="smartDesktopService" /><row label="JWorklist (VIA DB)" value="JWorklistPkgService" /></rowset>', true),

    CONSERVAZIONE_AUTOMATICA			("Definisce se è attiva o meno la conservazione automatica dei documenti", "CONSERVAZIONE AUTOMATICA", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	CONSERVAZIONE_AUTOMATICA_LIMITE		("Numero di atti massimo che viene inviato in conservazione giornalmente.", "Numero Atti Conservazione", "100", null, true),
	INDIRIZZO_DELIBERA 					("Definisce se una delibera può avere o meno degli indirizzi (esempio economico, politico)", "INDIRIZZO DELIBERA", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	STAMPA_UNICA_FRASE_FOOTER			("Frase da aggiungere in fondo ad ogni pagina nella stampa unica", "Stampa Unica - Frase Footer", "copia informatica per consultazione", null, true),
	STAMPA_UNICA_NUMERO_PAGINE			("Indica se aggiungere in fondo ad ogni pagina nella stampa unica il numero di pagina.", "STAMPA UNICA NUMERO PAGINE", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	STAMPA_UNICA_FORMATO_NUMERO_PAGINE	("Formato del numero di pagina nella stampa unica (Es. n° pagina / n° totale).", "STAMPA UNICA FORMATO NUMERO PAGINE", "%s / %s", null, true),
	COPIA_CONFORME_FRASE_FIRMATARI		("Frase da aggiungere dopo i firmatari nella copia conforme", "Copia Conforme - Frase Firmatari", "ai sensi dell'art. 20 e 23 del D.lgs 82/2005", null, true),
    MODELLI_STAMPE_ESEGUIBILITA_DELIBERA("Frase da aggiungere se la delibera è immediatamente eseguibile", "MODELLI_STAMPE_DELIBERA_IMMEDIATAMENTE_ESEGUIBILE", "La presente deliberazione è stata dichiarata immediatamente eseguibile.", null, true),
    MODELLI_STAMPE_NON_ESEGUIBILITA_DELIBERA("Frase da aggiungere se la delibera non è immediatamente eseguibile", "MODELLI_STAMPE_DELIBERA_NON_IMMEDIATAMENTE_ESEGUIBILE", "La presente deliberazione è stata dichiarata non immediatamente eseguibile.", null, true),
    MODELLI_STAMPE_ESEGUIBILITA_DETERMINA("Frase da aggiungere se la determina è immediatamente eseguibile, viene aggiunta in automatico la data di esecutività", "MODELLI_STAMPE_DETERMINA_IMMEDIATAMENTE_ESEGUIBILE", "l'atto diventera' eseguibile dopo 10 giorni dalla data di pubblicazione", null, true),
    MODELLI_STAMPE_NON_ESEGUIBILITA_DETERMINA("Frase da aggiungere se la determina non è immediatamente eseguibile", "MODELLI_STAMPE_DETERMINA_NON_IMMEDIATAMENTE_ESEGUIBILE", "l'atto diventera' esecutivo in data", null, true),
	CATEGORIA_DETERMINA					("Flag che definisce se è presente il dizionario "+ Labels.getLabel("label.categoria.determina")+ " (Y/N)", Labels.getLabel("label.categoria.determina")?.toUpperCase()?:"Categoria Determina", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	CATEGORIA_PROPOSTA_DELIBERA			("Flag che definisce se è presente il dizionario "+ Labels.getLabel("label.categoria.propostaDelibera")+ " (Y/N)", Labels.getLabel("label.categoria.propostaDelibera")?.toUpperCase()?:"Categoria Delibera", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),

	CASA_DI_VETRO_FILE_PRINCIPALE       ("Indica quale file deve essere il principale (se stampa unica, il testo o nessuno)", "File principale in casa di vetro", "TESTO", '<rowset><row label="Testo" value="TESTO" /><row label="Stampa Unica" value="STAMPA_UNICA" /><row label="Nessuno" value="NESSUNO" /></rowset>', true),
	CASA_DI_VETRO_RISERVATI				("Flag che definisce se pubblicare gli allegati riservati nella casa di vetro", "Riservati casa di vetro", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	CASA_DI_VETRO_PUBBLICA_ALLEGATI		("Indica se vanno pubblicati o meno gli allegati alla casa di vetro", "Pubblica gli allegati in casa di vetro", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	CASA_DI_VETRO_PUBBLICA_TESTO_VISTO  ("Indica se va pubblicato o meno il testo del visto alla casa di vetro", "Pubblica il testo del visto in casa di vetro", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	CASA_DI_VETRO_PUBBLICA_ALLEGATI_VISTO("Indica se vanno pubblicati o meno gli allegati del visto alla casa di vetro", "Pubblica gli allegati del visto in casa di vetro", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),

    PUBBLICAZIONE_VIS_PROP_DETERMINA    ("Flag che definisce se è abilitata la pubblicazione per la proposta di determina", "PUBBLICAZIONE VIS PROP DETERMINA", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	PUBBLICAZIONE_VIS_PROP_DELIBERA     ("Flag che definisce se è abilitata la pubblicazione per la proposta di delibera", "PUBBLICAZIONE VIS PROP DELIBERA", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	PUBBLICAZIONE_FINO_REVOCA		    ("Flag che definisce se è possibile abilitare la pubblicazione fino a revoca", "PUBBLICAZIONE FINO A REVOCA", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	PUBBLICAZIONE_GIORNI_ESECUTIVITA 	("Numero di giorni che devono trascorrere prima di creare il certificato di esecutività.", "Giorni Esecutività", "11", null, true),
	PUBBLICAZIONE_VISTI					("Indica se si devono pubblicare i visti e i pareri all'albo", "Visti da pubblicare", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	PUBBLICAZIONE_ALLEGATI_VISTI		("Indica se si devono pubblicare gli allegati dei visti e dei pareri all'albo", "Allegati dei Visti da pubblicare", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	PUBBLICAZIONE_SCHEDA_CONTABILE		("Indica se si deve pubblicare la scheda contabile all'albo anche quando l'atto è riservato", "Scheda contabile da pubblicare per gli atti riservati", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	INIZIO_ESECUTIVITA 					("Da quando bisogna calcolare la data di esecutività: dall'inizio o dalla fine della pubblicazione.", "Inizio Esecutività", "INIZIO", '<rowset><row label="Inizio Pubblicazione" value="INIZIO" /><row label="Fine Pubblicazione" value="FINE" /></rowset>', true),
	RICHIESTA_ESECUTIVITA 				("Visualizza la sezione relativa alla scadenza per le determine", "Attiva gestione scadenza", "GESTIONE SCADENZA", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	RICHIESTA_ESECUTIVITA_LABEL			("Testo della linguetta della sezione relativa alla scadenza per le determine", "Testo tab scadenza", "Priorità", null, true),
	RICHIESTA_ESECUTIVITA_COLONNA		("Indica se visualizzare la data richiesta esecutività nella pagina \"I Miei Documenti\" e \"Ricerca\"", "VISUALIZZA COLONNA RICHIESTA ESECUTIVITA'", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	PRIORITA			 				("Visualizza la sezione relativa alla priorità delle proposte", "Attiva gestione priorita", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	RICHIESTA_SEDUTA	 				("Visualizza la sezione relativa alla richiesta di data seduta prevista", "Attiva richiesta data seduta", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	FILE_DA_PUBBLICARE					("Indica il file da pubblicare all'albo, o la stampa unica o il testo", "File da pubblicare", "SU", '<rowset><row label="Solo Stampa unica" value="SU" /><row label="Solo Testo" value="T" /><row label="Sia testo che Stampa unica" value="All" /><row label="Nessuno" value="N" /></rowset>', true),
	RISERVATO 							("Definisce se è abilitata la riservatezza oppure no", "RISERVATO", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	RISERVATO_DEFAULT					("Definisce il default per il campo riservato degli allegati", "ALLEGATO RISERVATO", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	DESTINATARI_ESTERNI					("Definisce se sono abilitati i destinatari esterni oppure no", "DESTINATARI ESTERNI", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	DESTINATARI_INTERNI					("Definisce se sono abilitati i destinatari interni oppure no", "DESTINATARI INTERNI", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
    DESTINATARI_INTERNI_OBBLIG_DELI		("Definisce se sono abilitati obbligatori i destinatari interni per le proposte di delibera oppure no", "DESTINATARI INTERNI OBBLIGATORI DELIBERE", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	ALIAS_INVIO_MAIL					("Alias selezionato fra quelli indicati nel si4cim.cfg per l'invio di mail NON certicate", "Alias mail NON certificate", "mail", null, true),
	ALIAS_INVIO_MAIL_CERT				("Alias selezionato fra quelli indicati nel si4cim.cfg per l'invio di mail certicate", "Alias mail certificate", "mail_cert", null, true),
	MITTENTE_INVIO_MAIL					("Mittente per l'invio di mail", "Mittente mail", "atti@ads.it", null, true),
	MITTENTE_INVIO_MAIL_CERT			("Mittente per l'invio di mail certicate", "Mittente mail certicate", "istituzionaleads@cert.legalmail.it", null, true),
	INTEGRAZIONE_ALBO					("Integrazione con l'Albo.", "Integrazione Albo", "N", '<rowset><row label="Nessuno" value="N" /><row label="Albo JMessi" value="alboJMessi" /><row label="Albo Pretorio Esterno" value="alboEsterno" /></rowset>', true),
    RELATA_ALBO							("Visualizza la relata di pubblicazione presente sull'Albo Pretorio.", "Visualizza relata albo", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	ALBO_COSTANTI_DELIBERA				("Costanti per la pubblicazione all'albo JMessi delle Delibere." , "Parametri Albo JMessi", "<campi><campo nome=\"C_DESCRIZIONE_ENTE\" valore=\"\"/><campo nome=\"DESCRIZIONE_ENTE\" valore=\"\"/><campo nome=\"A_TIPO_PUBBLICAZIONE\" valore=\"REGISTRO DELLE DELIBERE\" /><campo nome=\"A_TIPO_ATTO\" valore=\"DELI\"/></campi>", null, false),
	ALBO_COSTANTI_DETERMINA				("Costanti per la pubblicazione all'albo JMessi delle Determine.", "Parametri Albo JMessi", "<campi><campo nome=\"C_DESCRIZIONE_ENTE\" valore=\"\"/><campo nome=\"DESCRIZIONE_ENTE\" valore=\"\"/><campo nome=\"A_TIPO_PUBBLICAZIONE\" valore=\"REGISTRO DELLE DETERMINE\"/><campo nome=\"A_TIPO_ATTO\" valore=\"DETE\"/></campi>", null, false),
	INTEGRAZIONE_GDM					("Abilita o meno l'integrazione con il documentale GDM", "Integrazione JDMS", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	INTEGRAZIONE_CASA_DI_VETRO			("Abilita o meno l'integrazione con la Casa di Vetro", "Integrazione Casa di Vetro", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	FIRMA_REMOTA						("Definisce se è abilitata la firma remota", "FIRMA REMOTA", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	FIRMA_HASH							("Abilita la firma tramite hash", "FIRMA HASH", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	SO4_SUDDIVISIONE_AREA				("Codice della suddivisione SO4 che individua l'area (Unità di Primo Livello)", "CODICE AREA SUDDIVISIONE SO4", "AREA", null, true),
	SO4_SUDDIVISIONE_SERVIZIO			("Codice della suddivisione SO4 che individua il servizio (Unità di Secondo Livello)", "CODICE SERVIZIO SUDDIVISIONE SO4", "SERVIZIO", null, true),

    DOCER								("Indica l'integrazione al repository documentale di Doc/Er", "DOCER", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),

    CONTABILITA							("Integrazione con la contabilità finanziaria", "CONTABILITA' FINANZIARIA", "N", '<rowset><row label="Nessuno" value="N" /><row label="Contabilità CFA - ADS" value="integrazioneContabilitaCfa" /><row label="Contabilità CF4 - ADS" value="integrazioneContabilitaCf4" /><row label="Contabilità Comune Modena" value="integrazioneContabilitaComuneModena" /><row label="Contabilità Sfera" value="integrazioneContabilitaSfera" /><row label="Contabilità AscotWeb" value="integrazioneContabilitaAscotWeb" /><row label="Contabilità Ce4" value="integrazioneContabilitaCe4" /></rowset>', true),
	CONTABILITA_SCHEDA_DAFIRMARE		("Indica se la Scheda Contabile è da firmare", "CONTABILITA SCHEDA DA FIRMARE", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	CONTABILITA_SCHEDA_STAMPAUNICA		("Indica se la Scheda Contabile è da inserire nella stampa unica", "CONTABILITA SCHEDA STAMPA UNICA", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	IMPORT_ALLEGATO_GDM					("Indica la funzionalità di import degli allegati di GDM", "IMPORT_ALLEGATO_GDM", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	IMPORT_ALLEGATO_GDM_URL				("Indica l'url alla servlet da invocare per effettuare l'import degli allegati su GDM", "IMPORT_ALLEGATO_GDM_URL", "http://localhost:8080", null, true),
	VIS_MENU							("Voci del menu del Visualizzatore", "MENU VISUALIZZATORE", """<menu login="true"><link sezione="DELIBERE" action="ricercaDelibere">Deliberazioni</link><link sezione="DETERMINE" action="ricercaDetermine">Determinazioni</link><link sezione="ODG" action="ricercaSedute">O. d. G.</link></menu>""", null, false),
	LUNGHEZZA_OGGETTO					("Lunghezza massima dell'oggetto dei documenti determina/proposta delibera/delibera", "Lunghezza massima dell'oggetto dei documenti", "2000", null, true),
	DEFAULT_FUNZIONARIO					("Indica il valore di default della presenza del funzionario per le tipologie di atti che prevedono il funzionario non obbligatorio", "DEFAULT FUNZIONARIO", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	ALLEGATO_STATO_FIRMA_DEFAULT		("Indica il valore di default dello stato della firma in creazione nuovo allegato", "ALLEGATO STATO FIRMA DEFAULT", "DA_FIRMARE", '<rowset><row label="Da Non Firmare" value="DA_NON_FIRMARE" /><row label="Da Firmare" value="DA_FIRMARE" /></rowset>', true),
	ALLEGATO_STAMPA_UNICA_DEFAULT		("Indica il valore di default della stampa unica in creazione nuovo allegato", "ALLEGATO STAMPA UNICA DEFAULT", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	FIRMA_CON_TIMESTAMP					("Indica se va aggiunto il timestamp della data di firma (non la marca temporale) al p7m.", "TIMESTAMP IN FIRMA", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', false),
	FIRMA_CON_CODICE_FISCALE			("Indica se effettuare la verifica tra il codice fiscale dell'utente e quello del certificato di firma.", "CODICE FISCALE IN FIRMA", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', false),
	GESTIONE_CORTE_CONTI				("Indica se va gestito l'invio alla corte dei conti", "GESTIONE CORTE DEI CONTI", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
    ODG_NUMERA_DELIBERE					("Indica se le delibere vengono numerate nella gestione odg.", "ODG NUMERA DELIBERE", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
    DATA_ADOZIONE_DELIBERE				("Indica se la data di adozione della delibera deve coincidere con la data della seduta (se inserita in seduta).", "DATA ADOZIONE DELIBERE", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
	URL_SERVER_PUBBLICO					("Indica l'indirizzo pubblico dell'applicativo (atti e visualizzatore)", "URL SERVER PUBBLICO", " ", '', true),
	DATI_AGGIUNTIVI						("Indica quali dati aggiuntivi sono abilitati (separati da '#').", "Dati Aggiuntivi", "N", null, false),
	ESPORTAZIONE_NUMERO_MASSIMO			("Definisce il numero massimo di documento da esportare", "ESPORTAZIONE NUMERO MASSIMO", "2000", null, true),
	ESEGUIBILITA_IMMEDIATA_ATTIVA		("Indica se è abilitata l'eseguibilità immediata.", "ESEGUIBILITA_IMMEDIATA_ATTIVA", "Y", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	ESEGUIBIILITA_IMMEDIATA_MOTIVAZIONI ("Indica se è obbligatoria la motivazione per l'eseguibilità immediata.", "ESEGUIBILITA_IMMEDIATA_MOTIVAZIONI", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
    ESEGUIBILITA_IMMEDIATA_DETE_ATTIVA	("Indica se è abilitata l'eseguibilità immediata per le determine.", "ESEGUIBILITA_IMMEDIATA_DETE_ATTIVA", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
    ESEGUIBIILITA_IMMEDIATA_DETE_MOTIVAZIONI ("Indica se è obbligatoria la motivazione per l'eseguibilità immediata per le determine.", "ESEGUIBILITA_IMMEDIATA_DETE_MOTIVAZIONI", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	ANNULLA_DELIBERA_COLLEGATA			("Indica se è abilitata l'annullamento di una delibera all'esecutività di un altra delibera.", "ANNULLA_DELIBERA_COLLEGATA", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	URL_SERVER							("Url di accesso all'applicativo Sfera. Utile per la costruzione di link da dare ad integrazioni, ad es. CFA.", "URL SERVER", " ", null, true),
	PARERE_REVISORI_CONTI				("Indica se è abilitata la sezione Parere Revisioni dei Conti per la proposta di delibera.", "PARERE REVISORI CONTI", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	DATI_TESORIERE						("Indica se è abilitata la sezione Dati di Tesoriere per le determine.", "DATI TESORIERE", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	SECONDO_NUMERO_DETERMINA			("Indica se è abilitata la funzionalità di seconda numerazione per le determine.", "SECONDO NUMERO DETERMINA", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	OGGETTI_RICORRENTI_TIPOLOGIE		("Indica se è abilitata la funzionalità degli oggetti ricorrenti nelle tipologie delle determine e delibere.", "TIPOLOGIE OGGETTI RICORRENTI", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	OGGETTI_RICORRENTI_CONTROLLO		("Indica se è abilitato il controllo degli oggetti ricorrenti. (Verranno controllate le parti fisse mentre il formato della parti variabili sarà [...])", "CONTROLLO OGGETTI RICORRENTI", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	ESECUTIVITA_SOLO_GIORNI_FERIALI		("Indica se è possibile dare l'esecutività dei documenti solo nei giorni feriali (Abilita il calendario Festività)", "ESECUTIVITA SOLO NEI GIORNI FERIALI", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	INCARICATO							("Indica se è abilitata la gestione dell'incaricato", "GESTIONE INCARICATO", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	URL_SERVER_GDM						("Url server GDM (se sullo stesso server di sfera è sufficiente il percorso relativo '..')", "URL SERVER GDM", "..", null, true),
	CODICE_AOO							("Codice dell'AOO protocollo", "CODICE AOO", "-", null, true),
	STAMPE_SEDUTA_DOCUMENTI				("Indica se gestire le stampe della seduta come documenti 'reali'", "STAMPE SEDUTA DOCUMENTI", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	CONCORRENZA_ACCESSO					("Indica se è abilitato il controllo dell'accesso in concorrenza ai documenti", "CONCORRENZA ACCESSO", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	DELEGHE								("Indica se è abilitata la gestione delle deleghe", "DELEGHE", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
	RUOLO_SO4_DIRIGENTE		 			("Ruolo dirigenti", "RUOLO SO4 DIRIGENTE", "AGDIR", null, true),
	MODELLI_STAMPE_DIRIGENTE  			("Frase da scrivere nei modelli in caso di firma del dirigente", "MODELLI STAMPE DIRIGENTE", "IL DIRIGENTE", null, true),
	MODELLI_STAMPE_FIRMATARIO  			("Frase da scrivere nei modelli in caso di firma non del dirigente", "MODELLI STAMPE FIRMATARIO", "Il funzionario delegato", null, true),
	MODELLI_STAMPE_DELEGATO  			("Frase da scrivere nei modelli in caso di firma del delegato", "MODELLI STAMPE DELEGATO", "IN SOSTITUZIONE DI", null, true),
	CAMBIO_PASSWORD						("Abilita il cambio password.", "CAMBIO PASSWORD", "Y", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
    SOGGETTI_FORMATO					("Indica il formato di stampa dei soggetti nelle stampe.", "SOGGETTI FORMATO", "COGNOME_NOME", '<rowset><row label="Cognome Nome"  value="COGNOME_NOME" /><row label="Nome Cognome"  value="NOME_COGNOME" /></rowset>', true),
	GESTIONE_DATA_ORDINAMENTO			("Indica se va gestita la data di ordinamento nella pagina I Miei Documenti", "GESTIONE DATA ORDINAMENTO", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
    GESTIONE_BUDGET						("Abilita la gestione del Budget.", "ATTIVA BUDGET", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
    GESTIONE_BUDGET_APPROVAZIONE    	("Abilita l'approvazione dei budget associati alle proposte.", "ATTIVA APPROVAZIONE BUDGET", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
    GESTIONE_FONDI						("Abilita la gestione dei FONDI.", "ATTIVA FONDI", "N", '<rowset><row label="No"  value="N" /><row label="Si"  value="Y" /></rowset>', true),
    RUOLO_SO4_DIZIONARI_BUDGET	        ("Ruolo di accesso alla sezione 'Dizionari:Budget'", "RUOLO SO4 DIZIONARI BUDGET", "AGDATTI", null, true),
    GESTIONE_BUDGET_IMPORTO_MENSILE		("Definisce l'importo mensile massimo utilizzabile da ciascuna UO", "GESTIONE BUDGET IMPORTO MENSILE", "20000", null, true),
    GESTIONE_BUDGET_IMPORTO_MASSIMO		("Definisce l'importo massimo impegnabile con ciascuna proposta", "GESTIONE BUDGET IMPORTO MASSIMO", "5000", null, true),
    NOTIFICHE_DATA_SCADENZA             ("Indica quale data scadenza deve essere inserita nelle attività", "Data Scadenza Attività", "DATA_SCADENZA", '<rowset><row label="Data Scadenza Atto" value="DATA_SCADENZA" /><row label="Data Scadenza Fatture" value="DATA_SCADENZA_FATTURE" /></rowset>', true),
	SU_FORMATI_ESCLUSI					("Definisce quali formati di allegati vengono esclusi dalla Stampa unica (la sintassi è formato1#formato2#formato3)", "ESTENSIONI FILE ESCLUSE DA STAMPA UNICA", "zip#rar#7z#eml", null, true);

	// proprietà
	private final String descrizione;
	private final String etichetta;
	private final String predefinito;
	private final String caratteristiche;
	private final boolean modificabile	= false;

	static final String SEPARATORE = "#";
	static ImpostazioniMap map;

	Impostazioni(String descrizione
					  , String etichetta
					  , String predefinito
					  , String caratteristiche
					  , boolean modificabile) {
		this.descrizione     = descrizione;
		this.etichetta       = etichetta;
		this.predefinito     = predefinito;
		this.caratteristiche = caratteristiche;
		this.modificabile    = modificabile;
	}

    String getEtichetta () {
        return this.etichetta
    }

    String getCaratteristiche () {
        return this.caratteristiche
    }

	String getPredefinito () {
		return this.predefinito
	}

	String getValore (String ente) {
		return map.getValore(this.toString(), predefinito, ente);
	}

	String getValore () {
		return map.getValore(this.toString(), predefinito);
	}

	int getValoreInt () {
		return Integer.parseInt(getValore());
	}

	String[] getValori () {
		return this.getValore()?.split(SEPARATORE)?:[];
	}

	boolean isAbilitato () {
		return "Y".equalsIgnoreCase(this.getValore());
	}

	boolean isDisabilitato () {
		return "N".equalsIgnoreCase(this.getValore());
	}

	byte[] getRisorsa () {
		return map.getRisorsa(this.toString());
	}
}
