package it.finmatica.atti.documenti.competenze

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.Allegato
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgCompetenza
import it.finmatica.so4.login.So4UserDetail
import it.finmatica.so4.login.detail.UnitaOrganizzativa
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb;

class AllegatoCompetenze {

    Allegato allegato

	boolean lettura			= false
	boolean modifica		= false
	boolean cancellazione	= false

	Ad4Utente utenteAd4
	Ad4Ruolo ruoloAd4
	So4UnitaPubb unitaSo4

	WkfCfgCompetenza cfgCompetenza

	// può tornarci utile mettere una proprietà WkfStep step con lo step che inserisce il record di competenza

	static mapping = {
		table 			'allegati_competenze'
		id				column: 'id_allegato_competenza'
		allegato		column: 'id_allegato', 				index: 'allcomp_all_fk'
		cfgCompetenza   column: 'id_cfg_competenza', 		index: 'allcomp_wkfcfgcom_fk'

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
		unitaSo4 		nullable: true
		utenteAd4 		nullable: true
		ruoloAd4		nullable: true
		cfgCompetenza	nullable: true // in caso si volesse dare la possibilità di assegnare manualmente le competenze
    }

	static namedQueries = {
		canRead { Allegato a, So4UserDetail utente ->
			competenze (a, utente)
			eq ("lettura", true)
		}

		canWrite { Allegato a, So4UserDetail utente ->
			competenze (a, utente)
			eq ("modifica", true)
		}

		canDelete { Allegato a, So4UserDetail utente ->
			competenze (a, utente)
			eq ("cancellazione", true)
		}

		competenze { Allegato a, So4UserDetail utente ->
			if (a != null) {
				eq ("allegato", a)
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

	public static void verificaCompetenza (Allegato a, So4UserDetail utente, boolean lettura = true, boolean modifica = false, boolean cancellazione = false) {
		if (cancellazione) {
			int count = AllegatoCompetenze.canDelete (a, utente).count()
			if (count == 0)
				throw new RuntimeException ("L'utente corrente ${utente.nominativoUtente} non ha i permessi di cancellare l'allegato con id ${a.id}")

			return;
		}

		if (modifica) {
			int count = AllegatoCompetenze.canWrite (a, utente).count()
			if (count == 0)
				throw new RuntimeException ("L'utente corrente ${utente.nominativoUtente} non ha i permessi di modificare l'allegato con id ${a.id}")

			return;
		}

		if (lettura) {
			int count = AllegatoCompetenze.canRead (a, utente).count()
			if (count == 0)
				throw new RuntimeException ("L'utente corrente ${utente.nominativoUtente} non ha i permessi di leggere l'allegato con id ${a.id}")

			return;
		}
	}
}
