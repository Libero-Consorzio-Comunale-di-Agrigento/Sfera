# SFERA

Applicativo per la gestione degli atti amministrativi (dalla creazione alla conservazione)

## Descrizione

SFERA è il prodotto che consente di semplificare i processi di adozione degli atti amministrativi e garantire una fattiva dematerializzazione di tutta la documentazione prodotta dall’Ente.

SFERA è realizzato con la finalità di perseguire gli obiettivi e le indicazioni del Codice Amministrazione Digitale (CAD) con particolare attenzione ai processi di dematerializzazione della documentazione, per raggiungere i quali è necessario organizzare il cambiamento. Ricevere e produrre una crescente quantità di documenti originali elettronici, firmati digitalmente e non, comporta ripensare l’organizzazione ed è a questo proposito che SFERA rende disponibili strumenti per agevolare l’introduzione di un nuovo metodo di lavoro finalizzato ad ottenere un sensibile recupero di efficienza ed efficacia.

L’obiettivo è attivare fasi di lavoro per processi e automatizzare l’individuazione delle risorse (utenti, unità organizzative, ruoli, ecc.) coinvolte nelle diversi fasi di gestione dei documenti e degli atti amministrativi al fine di assegnare automaticamente le attività, presentarle sulle singole “scrivanie virtuali” e verificarne in qualsiasi momento lo stato di avanzamento.

SFERA utilizza le componenti architetturali “struttura organizzativa”, “scrivania virtuale” e “gestione documentale” (opzionale) e permette di informatizzare la gestione dei processi di adozione degli atti collegiali e degli atti monocratici avvalendosi, per la produzione dei documenti, di strumenti Open Source, quali Open Office e Libre Office.

SFERA consente la trattazione di tutti gli Atti Amministrativi tramite una gestione informatica degli stessi nella forma di documenti elettronici integrati con la firma digitale. L’applicativo è completamente integrato con la struttura organizzativa dell’Ente ed è predisposto per essere utilizzato da tutte le unità abilitate alla creazione di proposte di delibere e di determinazioni, dai responsabili incaricati di esprimere pareri, dagli amministratori e dall’ufficio preposto alle attività tipiche di segreteria (verbalizzazione, numerazione, gestione pubblicazione e invio comunicazioni). Lo strumento permette di reperire le informazioni sullo stato di avanzamento del processo dal momento della creazione della proposta fino all’adozione dell’atto, e alla relativa esecutività, offrendo i mezzi tecnologici per la costituzione e la consultazione dei relativi archivi.

SFERA si integra con le migliori tecnologie disponibili sul mercato per la conservazione sostitutiva tramite l’utilizzo di web services specifici realizzati per il riversamento ed apposite funzionalità nell’applicativo per la selezione e l’invio dei documenti al sistema di conservazione. Inoltre, è totalmente integrato con il sistema contabile Armonizzato (CFA).

## Struttura del Repository

Il repository è suddiviso nelle seguente cartelle:

 - source contiene il codice sorgente e le risorse statiche incluse nella webapp.
 - scriptDB contiene gli script PL/SQL per la creazione della struttura dello schema database.
 - diagrammiER contiene i diagrammi Entità-Relazione in formato Portable Document Format (PDF)

## Prerequisiti e dipendenze

### Prerequisiti

- AD4: Amministrazione Database di Finmatica S.p.A.
- SO4: Struttura Amministrativa di Finmatica S.p.A.
- AS4: Anagrafe Soggetti di Finmatica S.p.A.
- GDM: Gestione Documentale Modulistica di Finmatica S.p.A.
- Si4CS: Smart Communicator System di Finmatica S.p.A.
- JSign: Firma Digitale di Finmatica S.p.A.
- SmartDesktop: Scrivania Virtuale di Finmatica S.p.A.
- Java JDK versione 7 o superiore
- Apache Tomcat versione 7 o superiore
- Database Oracle versione 10 o superiore

### Dipendenze
- Apache ANT versione 1.6 o superiore per la compilazione dei sorgenti
- Libreria ojdbc.jar driver oracle per Java di Oracle

## Istruzioni per l’installazione:
- Lanciare gli script della cartella scriptDB per generate lo schema e popolare i dati
- Lanciare il comando ant dalla cartella sorgenti per generare la webapp e copiare i file nel contesto di
tomcat.

## Stato del progetto
Stabile

## Amministrazione committente
Libero Consorzio Comunale di Agrigento

## Incaricati del mantenimento del progetto open source
Finmatica S.p.A. Via della Liberazione, 15 40128 Bologna

## Indirizzo e-mail a cui inviare segnalazioni di sicurezza
sicurezza@ads.it
