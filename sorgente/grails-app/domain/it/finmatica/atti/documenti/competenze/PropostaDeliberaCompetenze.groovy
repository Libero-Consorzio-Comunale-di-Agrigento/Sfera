package it.finmatica.atti.documenti.competenze

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgCompetenza
import it.finmatica.so4.login.So4UserDetail
import it.finmatica.so4.login.detail.UnitaOrganizzativa
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb;

class PropostaDeliberaCompetenze {

    PropostaDelibera propostaDelibera

	boolean lettura			= false
	boolean modifica		= false
	boolean cancellazione	= false

	Ad4Utente utenteAd4
	Ad4Ruolo ruoloAd4
	So4UnitaPubb unitaSo4

	WkfCfgCompetenza cfgCompetenza

	// può tornarci utile mettere una proprietà WkfStep step con lo step che inserisce il record di competenza

	static mapping = {
		table 				'proposte_delibera_competenze'
		id					column: 'id_proposta_delibera_comp'
		propostaDelibera	column: 'id_proposta_delibera', 	index: 'prodelcom_prodel_fk'
		cfgCompetenza   	column: 'id_cfg_competenza', 		index: 'prodelcom_wkfcfgcom_fk'
		lettura			type: 'yes_no'
		modifica		type: 'yes_no'
		cancellazione	type: 'yes_no'
		utenteAd4		column: 'utente'
		ruoloAd4		column: 'ruolo'
		columns {
			unitaSo4 {
				column name: 'unita_progr'
				column name: 'unita_dal'
				column name: 'unita_ottica'
			}
		}
	}

	static constraints = {
		unitaSo4		nullable: true
		utenteAd4 		nullable: true
		ruoloAd4		nullable: true
		cfgCompetenza	nullable: true // in caso si volesse dare la possibilità di assegnare manualmente le competenze
    }

	static namedQueries = {
		canRead { PropostaDelibera p, So4UserDetail utente ->
			competenze (p, utente)
			eq ("lettura", true)
		}

		canWrite { PropostaDelibera p, So4UserDetail utente ->
			competenze (p, utente)
			eq ("modifica", true)
		}

		canDelete { PropostaDelibera p, So4UserDetail utente ->
			competenze (p, utente)
			eq ("cancellazione", true)
		}

		competenze { PropostaDelibera p, So4UserDetail utente ->
			if (p != null) {
				eq ("propostaDelibera", p)
			}

			or {
				utente.uo().each { UnitaOrganizzativa uo ->
					or {
						and {
							isNull("utenteAd4")
							eq ("utenteAd4.id", utente.id)
						}
						and {
							unitaSo4 {
								eq ("progr", 			uo.id)
								eq ("dal", 				uo.dal)
								eq ("ottica.codice", 	uo.ottica)
							}
							'in'("ruoloAd4.ruolo", uo.ruoli*.codice)
						}
						and {
							isNull("unitaSo4")
							'in'("ruoloAd4.ruolo", uo.ruoli*.codice)
						}
						and {
							unitaSo4 {
								eq ("progr", 			uo.id)
								eq ("dal", 				uo.dal)
								eq ("ottica.codice", 	uo.ottica)
							}
							isNull("ruoloAd4")
						}
						and {
							isNull("unitaSo4")
							isNull("ruoloAd4")
						}
					}
				}
			}
		}
	}

	public static void verificaCompetenza (PropostaDelibera p, So4UserDetail utente, boolean lettura = true, boolean modifica = false, boolean cancellazione = false) {
		if (cancellazione) {
			int count = PropostaDeliberaCompetenze.canDelete (p, utente).count()
			if (count == 0)
				throw new RuntimeException ("L'utente corrente ${utente.nominativoUtente} non ha i permessi di cancellare la determina con id ${p.id}")

			return;
		}

		if (modifica) {
			int count = PropostaDeliberaCompetenze.canWrite (p, utente).count()
			if (count == 0)
				throw new RuntimeException ("L'utente corrente ${utente.nominativoUtente} non ha i permessi di modificare la determina con id ${p.id}")

			return;
		}

		if (lettura) {
			int count = PropostaDeliberaCompetenze.canRead (p, utente).count()
			if (count == 0)
				throw new RuntimeException ("L'utente corrente ${utente.nominativoUtente} non ha i permessi di leggere la determina con id ${p.id}")

			return;
		}
	}
}
