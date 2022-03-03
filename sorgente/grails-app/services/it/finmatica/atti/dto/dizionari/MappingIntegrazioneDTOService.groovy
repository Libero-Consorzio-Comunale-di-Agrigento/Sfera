package it.finmatica.atti.dto.dizionari

import it.finmatica.atti.dto.impostazioni.MappingIntegrazioneDTO
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupTutti
import it.finmatica.atti.integrazioni.parametri.ModuloIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import org.apache.commons.lang.math.RandomUtils

class MappingIntegrazioneDTOService {

    MappingIntegrazioneDTO salva(MappingIntegrazioneDTO mappingIntegrazioneDto) {
        MappingIntegrazione mappingIntegrazione = MappingIntegrazione.get(mappingIntegrazioneDto.id) ?: new MappingIntegrazione()

        // se non ho il valore esterno, non tento neanche di salvare il nuovo mapping
        if (!(mappingIntegrazioneDto.id > 0) && (mappingIntegrazioneDto.valoreEsterno == null || mappingIntegrazioneDto.valoreEsterno.trim().length() == 0)) {
            return null
        }

        if (mappingIntegrazione.id > 0 && (mappingIntegrazioneDto.valoreEsterno == null || mappingIntegrazioneDto.valoreEsterno.trim().length() == 0)) {
            mappingIntegrazione.delete()
            return null
        }

        mappingIntegrazione.categoria = mappingIntegrazioneDto.categoria
        mappingIntegrazione.codice = mappingIntegrazioneDto.codice
        mappingIntegrazione.valoreInterno = mappingIntegrazioneDto.valoreInterno
        mappingIntegrazione.valoreEsterno = mappingIntegrazioneDto.valoreEsterno
        mappingIntegrazione.descrizione = mappingIntegrazioneDto.descrizione
        mappingIntegrazione.save()
        return mappingIntegrazione.toDTO()
    }

    void elimina(MappingIntegrazioneDTO mappingIntegrazioneDto) {
        MappingIntegrazione mappingIntegrazione = MappingIntegrazione.get(mappingIntegrazioneDto.id)
        mappingIntegrazione.delete()
    }

    void salva (List<Map> parametri) {
        for (Map tab : parametri.findAll { !it.parametro?.multiplo }) {
            for (MappingIntegrazioneDTO valore : tab.valori) {
                salva(valore)
            }
        }
    }

    List<Map> getParametriIntegrazioni(ModuloIntegrazione integrazione, String tabSelected) {

        // per l'interfaccia ho bisogno di:
        // - elenco dei parametri di integrazione con eventuali "segnaposto" vuoti per quei parametri con un valore "singolo"
        // - elenco dei parametri di integrazione per i valori "multipli"

        // faccio il "merge" tra i parametri dell'integrazione e quelli effettivamente inseriti su db:
        List<Map> tabs = []
        Map dati = [titolo: "Dati Integrazione", valori: []]
        for (ParametroIntegrazione par : integrazione.listaParametri) {
            if (par.multiplo) {
                Map tab = [titolo: par.titolo, parametro: par, valori: []]
                tabs << tab
                if(par.titolo.equals(tabSelected)) {
                    List<MappingIntegrazioneDTO> parametri = MappingIntegrazione.findAllByCategoriaAndCodice(integrazione.codice, par.codice)?.toDTO()
                    List elenco = par.lookup.valori
                    for (MappingIntegrazioneDTO parametro : parametri){
                        parametro.descrizione = elenco.find { it.codice == parametro.valoreInterno}?.descrizione + " ("+parametro.valoreInterno+")"
                    }

                    tab.valori.addAll(parametri.sort {it.descrizione} )
                tab.valori*.setParametroIntegrazione(par)
                }
            } else if(!par.multiplo) {
                MappingIntegrazioneDTO parametro = MappingIntegrazione.findByCategoriaAndCodice(integrazione.codice, par.codice)?.toDTO()
                if (parametro == null) {
                    dati.valori << new MappingIntegrazioneDTO(id: -RandomUtils.nextInt(), codice: par.codice, categoria: integrazione.codice, parametroIntegrazione: par, valoreInterno: par.lookup.valori[0].codice)
                } else {
                    parametro.parametroIntegrazione = par
                    dati.valori << parametro
                }
            }
        }

        if (dati.valori.size() > 0) {
            tabs.add(0, dati)
        }

        return tabs
    }
}
