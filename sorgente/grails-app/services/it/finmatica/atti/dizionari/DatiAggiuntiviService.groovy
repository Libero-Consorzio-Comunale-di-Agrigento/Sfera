package it.finmatica.atti.dizionari

import groovy.xml.StreamingMarkupBuilder
import it.finmatica.atti.documenti.DatoAggiuntivo
import it.finmatica.atti.dto.dizionari.TipoDatoAggiuntivoValoreDTO
import it.finmatica.atti.dto.documenti.DatoAggiuntivoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.dto.DTO

class DatiAggiuntiviService {

    TipoDatoAggiuntivoValoreDTO salva (TipoDatoAggiuntivoValoreDTO datoAggiuntivoDto) {
        TipoDatoAggiuntivoValore datoAggiuntivo = TipoDatoAggiuntivoValore.get(datoAggiuntivoDto.id) ?: new TipoDatoAggiuntivoValore()
        datoAggiuntivo.codice = datoAggiuntivoDto.codice
        datoAggiuntivo.descrizione = datoAggiuntivoDto.descrizione
        datoAggiuntivo.sequenza = datoAggiuntivoDto.sequenza
        datoAggiuntivo.tipoOggetto = datoAggiuntivoDto.tipoOggetto
        datoAggiuntivo.valido = datoAggiuntivoDto.valido

        if (datoAggiuntivo.version != datoAggiuntivoDto.version) {
            throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
        }
        datoAggiuntivo = datoAggiuntivo.save()

        return datoAggiuntivo.toDTO()
    }

    void elimina (TipoDatoAggiuntivoValoreDTO datoAggiuntivoDto) {
        TipoDatoAggiuntivoValore datoAggiuntivo = TipoDatoAggiuntivoValore.get(datoAggiuntivoDto.id)
        if (datoAggiuntivo.version != datoAggiuntivoDto.version) {
            throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
        }
        datoAggiuntivo.delete(failOnError: true)
    }

    void salvaDatiAggiuntivi (def documento, DTO<?> documentoDto) {
        for (DatoAggiuntivoDTO datoDto : documentoDto.datiAggiuntivi) {
            DatoAggiuntivo dato = documento.datiAggiuntivi.find { it.id == datoDto.id }
            if (dato == null) {
                dato = new DatoAggiuntivo(codice: datoDto.codice)
                documento.addToDatiAggiuntivi(dato)
            }
            dato.valoreTipoDato = datoDto.valoreTipoDato?.domainObject
            dato.valore = datoDto.valore
            dato.save()
            datoDto.id = dato.id
        }

        // per ogni dato aggiuntivo NON presente nel dto ma presente nella domain, elimino quello presente nella domain:
        for (DatoAggiuntivo dato : documento.datiAggiuntivi.collect()) {
            DatoAggiuntivoDTO d = documentoDto.datiAggiuntivi?.find { it.id == dato.id }
            if (d == null) {
                documento.removeFromDatiAggiuntivi(dato)
                dato.delete()
            }
        }
    }

    void copiaDatiAggiuntivi (def documentoSrc, def documentoDest) {
        for (DatoAggiuntivo datoAggiuntivo : documentoSrc.datiAggiuntivi) {
            documentoDest.addToDatiAggiuntivi(new DatoAggiuntivo(codice: datoAggiuntivo.codice, valore: datoAggiuntivo.valore, valoreTipoDato: datoAggiuntivo.valoreTipoDato))
        }
        documentoDest.save()
    }

    boolean isDatoPresente (def documento, String codiceTipoDato) {
        return getDatoAggiuntivo(documento, codiceTipoDato) != null
    }

    DatoAggiuntivo getDatoAggiuntivo (def documento, String codiceTipoDato) {
        if (!documento.hasProperty("datiAggiuntivi")) {
            return null
        }

        return documento.datiAggiuntivi?.find { it.codice == codiceTipoDato }
    }

    /**
     * Ritorna un xml con i dati aggiuntivi della determina:
     * <datiAggiuntivi>
     *     <dato id="" codice="" valore="" idTipoValore="" tipoValore=""/>
     * </datiAggiuntivi>
     */
    String creaXmlDatiAggiuntivi (Collection<DatoAggiuntivo> dati) {
        StreamingMarkupBuilder xml = new StreamingMarkupBuilder()
        return xml.bind { builder ->
            datiAggiuntivi {
                for (DatoAggiuntivo d : dati) {
                    dato(id: d.id
                            , codice: d.codice
                            , valore: d.valore
                            , idTipoValore: d.valoreTipoDato?.id
                            , tipoValore: d.valoreTipoDato?.descrizione
                            , sequenzaTipoValore: d.valoreTipoDato?.sequenza)
                }
            }
        }.toString()
    }
}
