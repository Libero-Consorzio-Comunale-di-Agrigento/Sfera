package it.finmatica.atti.dto.odg.dizionari

import groovy.sql.Sql
import it.finmatica.atti.dto.odg.CommissioneComponenteDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.CommissioneStampaDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdmConfig
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.CommissioneComponente
import it.finmatica.atti.odg.CommissioneStampa
import oracle.jdbc.OracleTypes

class CommissioneDTOService {

	def dataSource

    CommissioneDTO salva(CommissioneDTO commissioneDto) {
		Commissione commissione 		= Commissione.get(commissioneDto.id)?:new Commissione()
    	if (commissione.version != commissioneDto.version) {
			throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
		}
		commissione.titolo				= commissioneDto.titolo
		commissione.descrizione			= commissioneDto.descrizione
		commissione.valido				= commissioneDto.valido
		commissione.ruoliObbligatori	= commissioneDto.ruoliObbligatori
		commissione.votoPresidente  	= commissioneDto.votoPresidente
		commissione.progressivoCfgIter	= commissioneDto.progressivoCfgIter > 0 ? commissioneDto.progressivoCfgIter : null
		commissione.secondaConvocazione	= commissioneDto.secondaConvocazione
		commissione.sedutaPubblica		= commissioneDto.sedutaPubblica
		commissione.pubblicaWeb			= commissioneDto.pubblicaWeb
		commissione.tipoRegistro		= commissioneDto.tipoRegistro?.domainObject
		commissione.tipoRegistroSeduta	= commissioneDto.tipoRegistroSeduta?.domainObject
		commissione.ruoloCompetenze		= commissioneDto.ruoloCompetenze?.domainObject
		commissione.ruoloVisualizza		= commissioneDto.ruoloVisualizza?.domainObject
		commissione.controlloFirmatari	= commissioneDto.controlloFirmatari
		commissione.save()

		// prima di salvare, verifico che i requisiti della commissione siano rispettati:
		// verifico che i firmatari abbiano una sequenza corretta:
		List<CommissioneComponente> firmatari = CommissioneComponente.findAllByValidoAndFirmatarioAndCommissione(true, true, commissione, [order:"asc", sort:"sequenzaFirma"])
		int sequenzaFirma = 1
		for (CommissioneComponente firmatario : firmatari) {
			if (firmatario.sequenzaFirma != sequenzaFirma) {
				throw new AttiRuntimeException ("La sequenza di firma del firmatario ${firmatario.componente.denominazione} non è corretta!\nÈ ${firmatario.sequenzaFirma} invece di ${sequenzaFirma}.\nCorreggere la sequenza di firma.")
			}
			sequenzaFirma++
		}

		return commissione.toDTO()
	}

	void elimina(CommissioneDTO commissioneDto) {
		Commissione.get(commissioneDto.id).delete()
	}

	void salvaStampa (CommissioneDTO commissioneDTO, CommissioneStampaDTO commissioneStampaDTO, String tipoDocumentoEsterno) {
		Commissione commissione = commissioneDTO.domainObject
		CommissioneStampa commissioneStampa = commissioneStampaDTO?.domainObject
        if (commissioneStampa == null) {
			if (CommissioneStampa.countByCommissioneAndModelloTestoAndValido(commissione, commissioneStampaDTO.modelloTesto?.domainObject, true) > 0){
				throw new AttiRuntimeException("Esiste già una stampa per questa commissione per il modello di testo: ${commissioneStampaDTO.modelloTesto?.nome}")
			}
            commissioneStampa = new CommissioneStampa()
            commissione.addToStampe(commissioneStampa)
        }
        commissioneStampa.titolo = commissioneStampaDTO.titolo
        commissioneStampa.descrizione = commissioneStampaDTO.descrizione
        commissioneStampa.codice = commissioneStampaDTO.codice
        commissioneStampa.usoNelVisualizzatore = commissioneStampaDTO.usoNelVisualizzatore
        commissioneStampa.progressivoCfgIter = commissioneStampaDTO.progressivoCfgIter
        commissioneStampa.caratteristicaTipologia = commissioneStampaDTO.caratteristicaTipologia?.domainObject
        commissioneStampa.modelloTesto = commissioneStampaDTO.modelloTesto?.domainObject
        commissioneStampa.valido = commissioneStampaDTO.valido
        commissione.save()

        int numeroVerbali = 0
        int numeroConvocazioni = 0
        for (CommissioneStampa stampa : commissione.stampe) {
            if (stampa.valido && stampa.usoNelVisualizzatore) {
                if (stampa.codice == CommissioneStampa.VERBALE) {
                    numeroVerbali++
                } else if (stampa.codice == CommissioneStampa.CONVOCAZIONE) {
                    numeroConvocazioni++
                }
            }
        }

        // verifico che ci sia una sola stampa con uso nel visualizzatore per i codici VERBALE e CONVOCAZIONE
        if (numeroVerbali > 1) {
            throw new AttiRuntimeException ("È possibile selezionare una sola stampa per l'uso 'VERBALE' nel visualizzatore. Togliere il flag 'Uso Nel Visualizzatore'")
        }

        if (numeroConvocazioni > 1) {
            throw new AttiRuntimeException ("È possibile selezionare una sola stampa per l'uso 'CONVOCAZIONE' nel visualizzatore. Togliere il flag 'Uso Nel Visualizzatore'")
        }

		if (tipoDocumentoEsterno?.trim()?.length() > 0) {
			MappingIntegrazione mappingIntegrazione = MappingIntegrazione.findByCategoriaAndCodiceAndValoreInterno(ProtocolloGdmConfig.MAPPING_CATEGORIA, ProtocolloGdmConfig.MAPPING_CODICE_TIPO_DOCUMENTO, commissioneStampa.id.toString())
			if (mappingIntegrazione == null) {
				mappingIntegrazione = new MappingIntegrazione(categoria: ProtocolloGdmConfig.MAPPING_CATEGORIA, codice: ProtocolloGdmConfig.MAPPING_CODICE_TIPO_DOCUMENTO, valoreInterno: commissioneStampa.id.toString())
			}
			mappingIntegrazione.valoreEsterno = tipoDocumentoEsterno
			mappingIntegrazione.save()
		}

        commissioneDTO.version = commissione.version
        commissioneStampaDTO.version = commissioneStampa.version
	}

	CommissioneComponenteDTO salvaComponente (CommissioneComponenteDTO commissioneComponenteDTO) {
		CommissioneComponente commissioneComponente = commissioneComponenteDTO.domainObject?:new CommissioneComponente()
		commissioneComponente.commissione			= commissioneComponenteDTO.commissione.domainObject
		commissioneComponente.componente			= commissioneComponenteDTO.componente.domainObject
		commissioneComponente.ruoloPartecipante 	= commissioneComponenteDTO.ruoloPartecipante?.domainObject
		commissioneComponente.incarico				= commissioneComponenteDTO.incarico?.domainObject

		// aggiungo il componente come ultimo, così dopo posso spostarlo riusando la funzione spostaComponenteCommissione.
		commissioneComponente.sequenza 				= 1 + CommissioneComponente.countByValidoAndCommissione(true, commissioneComponente.commissione)
		commissioneComponente.valido 				= true
		commissioneComponente.firmatario			= commissioneComponenteDTO.firmatario
		commissioneComponente.sequenzaFirma			= 1 + CommissioneComponente.countByValidoAndFirmatarioAndCommissione(true, true, commissioneComponente.commissione)
		commissioneComponente.save()

		// sposto il componente appena aggiunto nella posizione richiesta:
		spostaComponenteCommissione(commissioneComponente, commissioneComponenteDTO.sequenza)
		spostaComponenteCommissioneFirmatario(commissioneComponente, commissioneComponenteDTO.sequenzaFirma)

		return commissioneComponente.toDTO()
	}

	void eliminaComponente (CommissioneComponenteDTO commissioneComponenteDTO, Date data) {
		CommissioneComponente daEliminare = commissioneComponenteDTO.domainObject;
		daEliminare.valido   = false;
		daEliminare.validoAl = data.clearTime();
		daEliminare.save();

		riordinaComponenti(daEliminare.commissione);
	}

	void riordinaComponenti (Commissione commissione) {

		eliminaComponentiNonValidi(commissione)

		// riordino i restanti componenti
		def componenti = CommissioneComponente.findAllByValidoAndCommissione(true, commissione, [order:'asc', sort:'sequenza']);
		int index = 1;
		for (CommissioneComponente componente : componenti) {
			componente.sequenza = (index++);
			componente.save()
		}

		// risistemo anche la sequenza di firma
		componenti = CommissioneComponente.findAllByValidoAndFirmatarioAndCommissione(true, true, commissione, [order:'asc', sort:'sequenzaFirma']);
		index = 1;
		for (CommissioneComponente componente : componenti) {
			componente.sequenzaFirma = (index++);
			componente.save()
		}
	}

	void eliminaComponentiNonValidi (Commissione commissione){
		Sql sql = new Sql(dataSource)
		sql.call("{ call utility_pkg.elimina_componenti_non_validi(?) }"
				, [commissione.id])
	}

	void spostaComponenteSu (CommissioneComponenteDTO componente) {
		spostaComponenteCommissione(componente.domainObject, componente.sequenza-1)
	}

	void spostaComponenteGiu (CommissioneComponenteDTO componente) {
		spostaComponenteCommissione(componente.domainObject, componente.sequenza+1)
	}

	void spostaComponenteCommissione (CommissioneComponente componente, int nuovaSequenza) {
		// ottengo la lista ordinata dei componenti della commissione
		def componenti = CommissioneComponente.findAllByValidoAndCommissione(true, componente.commissione, [order:'asc', sort:'sequenza'])

		if (nuovaSequenza < 1 || nuovaSequenza > componenti.size()) {
			return
		}

		componenti.remove(componente)
		componenti.add(nuovaSequenza - 1, componente)

		int seq = 1;
		for (def c : componenti) {
			c.sequenza = seq
			c.save()
			seq++
		}
	}

	void spostaComponenteCommissioneFirmatario (CommissioneComponente componente, int nuovaSequenza) {
		// ottengo la lista ordinata dei componenti della commissione
		def componenti = CommissioneComponente.findAllByValidoAndFirmatarioAndCommissione(true, true, componente.commissione, [order:'asc', sort:'sequenzaFirma']);

		if (nuovaSequenza < 1 || nuovaSequenza > componenti.size()) {
			return;
		}

		componenti.remove(componente);
		componenti.add(nuovaSequenza - 1, componente);

		int seq = 1;
		for (def c : componenti) {
			c.sequenzaFirma = seq;
			c.save()
			seq++;
		}
	}
}
