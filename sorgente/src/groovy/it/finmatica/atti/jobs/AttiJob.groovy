package it.finmatica.atti.jobs

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.atti.admin.AggiornamentoService
import it.finmatica.atti.dizionari.NotificaErrore
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.beans.NotificheDispatcher
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.mail.Allegato
import it.finmatica.atti.mail.Mail
import it.finmatica.zkutils.SuccessHandler
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled

class AttiJob {

	private static final Logger log = Logger.getLogger(AttiJob.class)
	
	SpringSecurityService	springSecurityService
	AggiornamentoService 	aggiornamentoService
	NotificheDispatcher		notificheDispatcher
	AttiJobExecutor			attiJobExecutor
	SuccessHandler			successHandler

	@Scheduled(cron="\${it.finmatica.atti.jobs.AttiJob.aggiornamentiAutomatici.cron}")
	void aggiornamentiAutomatici () {
		// siccome questo job va fatto su ogni tomcat, non mi preoccupo di gestire l'univocità del lancio:
		aggiornamentoService.runUpdates ()
	}

	@Scheduled(cron="\${it.finmatica.atti.jobs.AttiJob.job.cron}")
	@Async
	void job () {
		log.info ("Eseguo il Job di pulizia / pubblicazione / esecutività / pubblicazione.")
		try {
			// disabilito le notifiche asincrone perché tanto sono nel job e posso permettermi di aspettare,
			// inoltre soprattutto perché apro e chiudo diverse transazioni e le notifiche asincrone possono andare in modifiche concorrenti.
			notificheDispatcher.setAsync(false)

			// eseguo l'autenticazione con l'utente batch (di solito AGSDE2)
			String[] codiciEnti = attiJobExecutor.eseguiAutenticazione (Holders.config.grails.plugins.anagrafesoggetti.utenteBatch)

			for (String codiceEnte : codiciEnti) {
				boolean lockOttenuto = false

				try {
					// come prima cosa ottengo il lock per evitare che due tomcat eseguano questo job in contemporanea:
					lockOttenuto = attiJobExecutor.lock(codiceEnte)

					// se non ho ottenuto il lock, significa che c'è un altro job che sta eseguendo, quindi esco.
					if (lockOttenuto == false) {
						log.warn ("C'è già un token per il job notturno e l'ente: ${codiceEnte}. Non eseguo il job.")
						return
					}

					log.info ("Eseguo il Job per l'ente con codice: ${codiceEnte}")
					executeJob (codiceEnte)

				} finally {
					// rilascio il lock
					if (lockOttenuto) {
						attiJobExecutor.unlock(codiceEnte)
					}
				}
			}

		} finally {
			// ripristino le notifiche asincrone
			notificheDispatcher.setAsync(true)
			successHandler.clearMessages()
		}
		log.info ("Job di pulizia / pubblicazione / esecutività / pubblicazione terminato.")
	}

	@Scheduled(cron="\${it.finmatica.atti.jobs.AttiJob.jobConservazioneAutomatica.cron}")
	@Async
	void jobConservazioneAutomatica () {
		log.info ("Eseguo il Job di conservazione automatica.")
		try {
			// disabilito le notifiche asincrone perché tanto sono nel job e posso permettermi di aspettare,
			// inoltre soprattutto perché apro e chiudo diverse transazioni e le notifiche asincrone possono andare in modifiche concorrenti.
			notificheDispatcher.setAsync(false)

			// eseguo l'autenticazione con l'utente batch (di solito AGSDE2)
			String[] codiciEnti = attiJobExecutor.eseguiAutenticazione (Holders.config.grails.plugins.anagrafesoggetti.utenteBatch)

			for (String codiceEnte : codiciEnti) {
				boolean lockOttenuto = false

				try {
					// come prima cosa ottengo il lock per evitare che due tomcat eseguano questo job in contemporanea:
					lockOttenuto = attiJobExecutor.lock(codiceEnte)

					// se non ho ottenuto il lock, significa che c'è un altro job che sta eseguendo, quindi esco.
					if (lockOttenuto == false) {
						log.warn ("C'è già un token per il job di conservazione automatica e l'ente: ${codiceEnte}. Non eseguo il job.")
						return
					}

					log.info ("Eseguo il Job di conservazione automatica per l'ente con codice: ${codiceEnte}")
					executeConservazioneAutomaticaJob (codiceEnte)

				} finally {
					// rilascio il lock
					if (lockOttenuto) {
						attiJobExecutor.unlock(codiceEnte)
					}
				}
			}

		} finally {
			// ripristino le notifiche asincrone
			notificheDispatcher.setAsync(true)
			successHandler.clearMessages()
		}
		log.info ("Job di conservazione automatica terminato.")
	}

    @Scheduled(cron="\${it.finmatica.atti.jobs.AttiJob.jobAggiornaConservazione.cron}")
    @Async
    void jobAggiornaConservazione () {
        log.info ("Eseguo il Job di aggiornamento degli stati di conservazione.")
        try {
            // disabilito le notifiche asincrone perché tanto sono nel job e posso permettermi di aspettare,
            // inoltre soprattutto perché apro e chiudo diverse transazioni e le notifiche asincrone possono andare in modifiche concorrenti.
            notificheDispatcher.setAsync(false)

            // eseguo l'autenticazione con l'utente batch (di solito AGSDE2)
            String[] codiciEnti = attiJobExecutor.eseguiAutenticazione (Holders.config.grails.plugins.anagrafesoggetti.utenteBatch)

            for (String codiceEnte : codiciEnti) {
                boolean lockOttenuto = false

                try {
                    // come prima cosa ottengo il lock per evitare che due tomcat eseguano questo job in contemporanea:
                    lockOttenuto = attiJobExecutor.lock(codiceEnte)

                    // se non ho ottenuto il lock, significa che c'è un altro job che sta eseguendo, quindi esco.
                    if (lockOttenuto == false) {
                        log.warn ("C'è già un token per il job degli stati di conservazione e l'ente: ${codiceEnte}. Non eseguo il job.")
                        return
                    }

                    log.info ("Eseguo il Job degli stati di conservazione per l'ente con codice: ${codiceEnte}")
                    executeAggiornaConservazioneJob (codiceEnte)

                } finally {
                    // rilascio il lock
                    if (lockOttenuto) {
                        attiJobExecutor.unlock(codiceEnte)
                    }
                }
            }

        } finally {
            // ripristino le notifiche asincrone
            notificheDispatcher.setAsync(true)
            successHandler.clearMessages()
        }
        log.info ("Job di aggiornamento degli stati di conservazione terminato.")
    }


    @Scheduled(cron="\${it.finmatica.atti.jobs.AttiJob.jobInviaNotifiche.cron}")
    @Async
    void jobInviaNotiche () {
        log.info ("Eseguo il Job di invio delle notifiche in errore.")
        try {
            // disabilito le notifiche asincrone perché tanto sono nel job e posso permettermi di aspettare,
            // inoltre soprattutto perché apro e chiudo diverse transazioni e le notifiche asincrone possono andare in modifiche concorrenti.
            notificheDispatcher.setAsync(false)

            // eseguo l'autenticazione con l'utente batch (di solito AGSDE2)
            String[] codiciEnti = attiJobExecutor.eseguiAutenticazione (Holders.config.grails.plugins.anagrafesoggetti.utenteBatch)

            for (String codiceEnte : codiciEnti) {
                boolean lockOttenuto = false

                try {
                    // come prima cosa ottengo il lock per evitare che due tomcat eseguano questo job in contemporanea:
                    lockOttenuto = attiJobExecutor.lock(codiceEnte)

                    // se non ho ottenuto il lock, significa che c'è un altro job che sta eseguendo, quindi esco.
                    if (lockOttenuto == false) {
                        log.warn ("C'è già un token per il job di invio notifiche e l'ente: ${codiceEnte}. Non eseguo il job.")
                        return
                    }

                    log.info ("Eseguo il Job di invio delle notifiche per l'ente con codice: ${codiceEnte}")
                    executeInvioNotificheJob (codiceEnte)

                } finally {
                    // rilascio il lock
                    if (lockOttenuto) {
                        attiJobExecutor.unlock(codiceEnte)
                    }
                }
            }

        } finally {
            // ripristino le notifiche asincrone
            notificheDispatcher.setAsync(true)
            successHandler.clearMessages()
        }
        log.info ("Job di invio delle notifiche terminato.")
    }

	private void executeJob (String codiceEnte) {
        File errorLogFile = File.createTempFile("error", "log")

		try {

			// Aggiorno le classifiche dal protocollo a sfera
			log.info ("Aggiorno le classifiche dal protocollo a Sfera")
			try {
				attiJobExecutor.aggiornaClassificazioni(codiceEnte)
			} catch (Throwable t) {
				log.error ("Errore nell'aggiornamento delle classificazioni per l'ente con codice: ${codiceEnte}", t)
				errorLogFile << "Errore nell'aggiornamento delle classificazioni per l'ente con codice: ${codiceEnte} \n${t.message}:${t.getStackTrace().toString().replace(')', ')\n')}";
			}

			// elimino le transazioni di firma vecchie
			log.info ("Elimino le transazioni di firma vecchie.")
			try {
				attiJobExecutor.eliminaTransazioniFirmaVecchie(codiceEnte)
			} catch (Throwable t) {
				log.error ("Errore nell'eliminare le transazioni di firma vecchie per l'ente con codice: ${codiceEnte}", t)
				errorLogFile << "Errore nell'eliminare le transazioni di firma vecchie per l'ente con codice: ${codiceEnte} \n${t.message}:${t.getStackTrace().toString().replace(')', ')\n')}";
			}
			
			// sblocco i documenti che sono in attesa per vario motivo (pubblicazione che termina, movimenti contabili pagati)
			log.info ("Sblocco documenti in attesa di fine pubblicazione e movimenti contabili pagati.")
			def documenti = attiJobExecutor.getDocumentiDaSbloccare(codiceEnte)
			for (def documento : documenti) {
				try {
					attiJobExecutor.sbloccaDocumento (codiceEnte, documento.class, documento.id)
				} catch (Throwable t) {
					log.error ("Errore nella esecutività del documento ${documento.class} con id ${documento.id} per l'ente con codice: ${codiceEnte}", t)
					errorLogFile << "Errore nella esecutività del documento ${documento.class} con id ${documento.id} per l'ente con codice: ${codiceEnte} \n${t.message}:${t.getStackTrace().toString().replace(')', ')\n')}";
				}
			}

			// esecutività determine
			log.info ("Rendo esecutive le determine con il certificato di esecutività.")
			def determine = attiJobExecutor.getDetermineDaRendereEsecutive(codiceEnte)
			for (def determina : determine) {
				try {
					attiJobExecutor.rendiEsecutivaDetermina(codiceEnte, determina.id)
				} catch (Throwable t) {
					log.error ("Errore nella esecutività della determina con id ${determina.id} per l'ente con codice: ${codiceEnte}", t)
					errorLogFile << "Errore nella esecutività della determina con id ${determina.id} per l'ente con codice: ${codiceEnte} \n${t.message}:${t.getStackTrace().toString().replace(')', ')\n')}";
				}
			}

			// esecutività delibere
			log.info ("Rendo esecutive le delibere con il certificato di esecutività.")
			def delibere = attiJobExecutor.getDelibereDaRendereEsecutive(codiceEnte)
			for (def delibera : delibere) {
				try {
					attiJobExecutor.rendiEsecutivaDelibera(codiceEnte, delibera.id)
				} catch (Throwable t) {
					log.error ("Errore nella esecutività della delibera con id ${delibera.id} per l'ente con codice: ${codiceEnte}", t)
					errorLogFile << "Errore nella esecutività della delibera con id ${delibera.id} per l'ente con codice: ${codiceEnte} \n${t.message}:${t.getStackTrace().toString().replace(')', ')\n')}";
				}
			}

			if (!Impostazioni.INTEGRAZIONE_ALBO.isDisabilitato()) {

				// Aggiorno le date di pubblicazioni dal protocollo a sfera
				log.info("Aggiorno le date di pubblicazione dall'Albo a Sfera")
				try {
					attiJobExecutor.aggiornaDatePubblicazioni(codiceEnte)
				} catch (Throwable t) {
					log.error("Errore nell'aggiornamento delle date di pubblicazione per l'ente con codice: ${codiceEnte}", t)
					errorLogFile << "Errore nell'aggiornamento delle date di pubblicazione per l'ente con codice: ${codiceEnte} \n${t.message}:${t.getStackTrace().toString().replace(')', ')\n')}";
				}

				// Controllo che non esistano documenti albo non presenti su gdm
				log.info("Controllo che non esistano documenti pubblicati all'Albo su Sfera non presenti su GDM")
				try {
					if (attiJobExecutor.controllaDocumentiAlboConErrore(codiceEnte)){
						log.error("Esistono documenti pubblicati all'Albo non presenti su GDM: ${codiceEnte}")
						errorLogFile << "Esistono documenti pubblicati all'Albo non presenti su GDM: ${codiceEnte} \n";
					}
				} catch (Throwable t) {
					log.error("Errore nel controllo dell'esistenza di documenti pubblicati all'Albo non presenti su GDM: ${codiceEnte}", t)
					errorLogFile << "Errore nel controllo dell'esistenza di documenti pubblicati all'Albo non presenti su GDM: ${codiceEnte} \n";
				}
			}

			// pubblicazione determina
			log.info("Pubblicazione di eventuali Determine")
			List<Determina> determineInPubblicazione = attiJobExecutor.getDetermineInPubblicazione(codiceEnte)
			log.info("Ci sono determine da pubblicare?  -->"+determineInPubblicazione.size())
			for (Determina determina : determineInPubblicazione) {
				try {
					log.info("Pubblicazione della Determina -->"+determina.id)
					attiJobExecutor.pubblicaDetermina (codiceEnte, determina.id)
				} catch (Throwable t) {
					log.error ("Errore nella pubblicazione per l'ente con codice: ${codiceEnte} sulla determina con id: ${determina.id}", t)
					errorLogFile << "Errore nella pubblicazione per l'ente con codice: ${codiceEnte} sulla determina con id: ${determina.id}\n${t.message}:${t.getStackTrace().toString().replace(')', ')\n')}";
				}
			}

			// pubblicazione delibera
			log.info("Pubblicazione di eventuali Delibere")
			List<Delibera> delibereInPubblicazione = attiJobExecutor.getDelibereInPubblicazione(codiceEnte)
			log.info("Ci sono delibere da pubblicare?  -->"+determineInPubblicazione.size())
			for (Delibera delibera : delibereInPubblicazione) {
				try {
					log.info("Pubblicazione della Delibera -->"+delibera.id)
					attiJobExecutor.pubblicaDelibera (codiceEnte, delibera.id)
				} catch (Throwable t) {
					log.error ("Errore nella pubblicazione per l'ente con codice: ${codiceEnte} sulla delibera con id: ${delibera.id}", t)
					errorLogFile << "Errore nella pubblicazione per l'ente con codice: ${codiceEnte} sulla delibera con id: ${delibera.id}\n${t.message}:${t.getStackTrace().toString().replace(')', ')\n')}";
				}
			}
			// cancellazione dei lock ai documenti rimasti
			log.info("Rimozione dei lock dei documenti")
			try {
				attiJobExecutor.cancellaLockDocumenti(codiceEnte)
			} catch (Throwable t) {
				log.error ("Errore nella cancellazione dei lock ai documenti per l'ente: ${codiceEnte}", t)
				errorLogFile << "Errore nella cancellazione dei lock ai documenti per l'ente: ${codiceEnte}\n${t.message}:${t.getStackTrace().toString().replace(')', ')\n')}";
			}

			// allineamento dei dati di protocollazione delle sedute-stampe
			log.info("Allineamento dei dati di protocollazione delle sedute-stampe")
			try {
				attiJobExecutor.aggiornaDatiProtocolloSedutaStampa(codiceEnte)
			} catch (Throwable t) {
				log.error ("Errore nell'Allineamento dei dati di protocollazione delle sedute-stampe per l'ente: ${codiceEnte}", t)
				errorLogFile << "Errore nell'Allineamento dei dati di protocollazione delle sedute-stampe per l'ente: ${codiceEnte}\n${t.message}:${t.getStackTrace().toString().replace(')', ')\n')}";
			}

			if (Impostazioni.ALLEGATO_VERIFICA_FIRMA.abilitato) {
				// estrazione delle informazioni di firma
				log.info("Estrazione delle informazioni dei file firmati internamente al sistema")
				try {
					attiJobExecutor.estraiInformazioniFileFirmati(codiceEnte)
				} catch (Throwable t) {
					log.error("Errore nell'estrazione delle informazioni dai file firmati internamente al sistema per l'ente: ${codiceEnte}", t)
					errorLogFile << "Errore nell'estrazione delle informazioni dai file firmati internamente al sistema per l'ente: ${codiceEnte}\n${t.message}:${t.getStackTrace().toString().replace(')', ')\n')}";
				}
			}
		} finally {
			inviaMailErrori (errorLogFile)
            FileUtils.deleteQuietly(errorLogFile)
		}
	}

	private void executeConservazioneAutomaticaJob (String codiceEnte) {
		File errorLogFile = File.createTempFile("error", "log")

		try {

			// invio documenti alla conservazione
			if (Impostazioni.CONSERVAZIONE_AUTOMATICA.abilitato) {
				log.info("Invio automatico dei documenti in conservazione")
				try {
					attiJobExecutor.inviaDocumentiInConservazione(codiceEnte)
				} catch (Throwable t) {
					log.error("Errore nell'invio di nuovi documenti in conservazione per l'ente con codice ${codiceEnte}", t)
					errorLogFile << "Errore nell'invio di nuovi documenti in conservazione per l'ente con codice ${codiceEnte}\n${t.message}:${t.getStackTrace().toString().replace(')', ')\n')}";
				}
			}
		} finally {
			inviaMailErrori (errorLogFile)
			FileUtils.deleteQuietly(errorLogFile)
		}
	}

    private void executeAggiornaConservazioneJob (String codiceEnte) {
        File errorLogFile = File.createTempFile("error", "log")

        try {

            // stati di conservazione
            log.info ("Aggiorno gli stati di conservazione del JCONS.")
            List<Determina> determineInConservazione = attiJobExecutor.getDetermineInConservazione(codiceEnte)
            for (Determina determina : determineInConservazione) {
                try {
                    attiJobExecutor.aggiornaStatoConservazioneDetermina (codiceEnte, determina.id)
                } catch (Throwable t) {
                    log.error ("Errore nell'aggiornamento dello stato di conservazione per l'ente con codice: ${codiceEnte} sulla determina con id: ${determina.id}", t)
                    errorLogFile << "Errore nell'aggiornamento dello stato di conservazione per l'ente con codice: ${codiceEnte} sulla determina con id: ${determina.id}\n${t.message}:${t.getStackTrace().toString().replace(')', ')\n')}";
                }
            }

            List<Delibera> delibereInConservazione = attiJobExecutor.getDelibereInConservazione(codiceEnte)
            for (Delibera delibera : delibereInConservazione) {
                try {
                    attiJobExecutor.aggiornaStatoConservazioneDelibera (codiceEnte, delibera.id)
                } catch (Throwable t) {
                    log.error ("Errore nell'aggiornamento dello stato di conservazione per l'ente con codice: ${codiceEnte} sulla delibera con id: ${delibera.id}", t)
                    errorLogFile << "Errore nell'aggiornamento dello stato di conservazione per l'ente con codice: ${codiceEnte} sulla delibera con id: ${delibera.id}\n${t.message}:${t.getStackTrace().toString().replace(')', ')\n')}";
                }
            }
        } finally {
            inviaMailErrori (errorLogFile)
            FileUtils.deleteQuietly(errorLogFile)
        }
    }


    private void executeInvioNotificheJob (String codiceEnte) {
        // invio notifiche
        log.info ("Invio delle notifiche che hanno dato errore in precedenza.")
        List<NotificaErrore> erroriNotifiche = attiJobExecutor.getErroriNotifiche(codiceEnte)
        for (NotificaErrore errore : erroriNotifiche) {
            try {
                attiJobExecutor.gestisciErroriNotifica(codiceEnte, errore.id)
            } catch (Throwable t) {
                log.error ("Errore nell'invio della notifica per l'ente con codice: ${codiceEnte} sul riferimento: ${errore.idRiferimento}", t)
            }
        }
    }

    private void inviaMailErrori (File errorLogFile) {
		if (Holders.config.atti.emailProblemi == null || Holders.config.atti.emailProblemi instanceof ConfigObject) {
			return
		}
		
		if (errorLogFile.size() == 0) {
			return
		}

		Mail.invia (Impostazioni.ALIAS_INVIO_MAIL.valore, Impostazioni.MITTENTE_INVIO_MAIL.valore, Holders.config.atti.emailProblemi
				, "SFERA: Si sono verificati dei problemi nell'esecuzione del job notturno."
				, "Errore nel job notturno a: ${springSecurityService.principal.amm().descrizione} ${Holders.config.atti.oggettoEmailProblemi}"
				, [new Allegato ("errori.log", new ByteArrayInputStream(errorLogFile.getBytes()))])
	}
}
