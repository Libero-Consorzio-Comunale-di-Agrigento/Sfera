package it.finmatica.atti.documenti.competenze

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgCompetenza
import it.finmatica.so4.login.So4UserDetail
import it.finmatica.so4.login.detail.UnitaOrganizzativa
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class VistoParereCompetenze {

    VistoParere vistoParere

	boolean lettura			= false
	boolean modifica		= false
	boolean cancellazione	= false

	Ad4Utente utenteAd4
	Ad4Ruolo ruoloAd4
	So4UnitaPubb unitaSo4

	WkfCfgCompetenza cfgCompetenza

	// può tornarci utile mettere una proprietà WkfStep step con lo step che inserisce il record di competenza

	static mapping = {
		table 			'visti_pareri_competenze'
		id				column: 'id_visto_parere_competenza'
		vistoParere		column: 'id_visto_parere', 			index: 'visparcomp_vispar_fk'
		cfgCompetenza   column: 'id_cfg_competenza', 		index: 'visparcomp_wkfcfgcom_fk'
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
		canRead { VistoParere v, So4UserDetail utente ->
			competenze (v, utente)
			eq ("lettura", true)
		}

		canWrite { VistoParere v, So4UserDetail utente ->
			competenze (v, utente)
			eq ("modifica", true)
		}

		canDelete { VistoParere v, So4UserDetail utente ->
			competenze (v, utente)
			eq ("cancellazione", true)
		}

		competenze { VistoParere v, So4UserDetail utente ->
			if (v != null) {
				eq ("visto", v)
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

	public static void verificaCompetenza (VistoParere v, So4UserDetail utente, boolean lettura = true, boolean modifica = false, boolean cancellazione = false) {
		if (cancellazione) {
			int count = VistoParereCompetenze.canDelete (v, utente).count()
			if (count == 0)
				throw new RuntimeException ("L'utente corrente ${utente.nominativoUtente} non ha i permessi di cancellare l'allegato con id ${v.id}")

			return;
		}

		if (modifica) {
			int count = VistoParereCompetenze.canWrite (v, utente).count()
			if (count == 0)
				throw new RuntimeException ("L'utente corrente ${utente.nominativoUtente} non ha i permessi di modificare l'allegato con id ${v.id}")

			return;
		}

		if (lettura) {
			int count = VistoParereCompetenze.canRead (v, utente).count()
			if (count == 0)
				throw new RuntimeException ("L'utente corrente ${utente.nominativoUtente} non ha i permessi di leggere l'allegato con id ${v.id}")

			return;
		}
	}
}
