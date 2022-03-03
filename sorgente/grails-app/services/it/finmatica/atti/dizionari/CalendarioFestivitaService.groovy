package it.finmatica.atti.dizionari

import it.finmatica.atti.dto.dizionari.CalendarioFestivitaDTO
import it.finmatica.atti.exceptions.AttiRuntimeException

class CalendarioFestivitaService {

    /**
     * Calcola la data prossima data festiva.
     *
     * @param dataEffettiva la data da utilizzare.
     * @return la data di esecutività effettiva.
     */
    Date getProssimoGiornoFestivo (Date dataEffettiva) {
        // clono la data per non modificare quella in input, mi assicuro di lavorare solo con giorno/mese/anno
        Date data = new Date(dataEffettiva.getTime()).clearTime()

        Calendar workCal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY)
        while (!isFestivo(data)) {
            workCal.setTime(data)
            workCal.add(Calendar.DAY_OF_MONTH, 1)
            data = workCal.getTime()
        }
        return data
    }


    /**
     * Calcola la prossima data feriale.
     *
     * @param dataEffettiva la data da utilizzare.
     * @return la data di esecutività effettiva.
     */
    Date getProssimoGiornoFeriale (Date dataEffettiva) {
        // clono la data per non modificare quella in input, mi assicuro di lavorare solo con giorno/mese/anno
        Date data = new Date(dataEffettiva.getTime()).clearTime()

        Calendar workCal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY)
        while (isFestivo(data)) {
            workCal.setTime(data)
            workCal.add(Calendar.DAY_OF_MONTH, 1)
            data = workCal.getTime()
        }
        return data
    }

    /**
     * Verifica se la data è domenica.
     *
     * @param data la data di input da utilizzare.
     * @return true se è domenica altrimenti false.
     */
    boolean isDomenica (Date data) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY)
        calendar.setTime(data)
        int currentWorkDay = calendar.get(Calendar.DAY_OF_WEEK)
        return (currentWorkDay == Calendar.SUNDAY)
    }

    /**
     * Verifica se la data è un giorno festivo.
     *
     * @param dataInput la data di input da utilizzare.
     * @return true se è festivo altrimenti false.
     */
    boolean isFestivo (Date data) {

        // se è una domenica, esco subito.
        if (isDomenica(data)) {
            return true
        }

        Calendar workCal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY)
        workCal.setTime(new Date())
        // l'anno da confrontare non è l'anno corrente ma l'anno della data di riferimento (in questo modo si gestiscono correttamente i "cambi di anno")
        int annoCorrente = data[Calendar.YEAR]

        List<CalendarioFestivita> listaFestivita = CalendarioFestivita.findAllByValido(true)
        for (CalendarioFestivita festivita : listaFestivita) {
            Date dataFestivita = new Date().clearTime()
            dataFestivita.set(year:festivita.anno ?: annoCorrente, month:festivita.mese-1, date:festivita.giorno)

            if (dataFestivita.compareTo(data) == 0) {
                return true
            }
        }

        return false
    }

    /**
     * Salva un Calendario Festività DTO.
     *
     * @param calendarioFestivitaDto
     * @return
     */
    CalendarioFestivitaDTO salva (CalendarioFestivitaDTO calendarioFestivitaDto) {
        CalendarioFestivita calendarioFestivita = CalendarioFestivita.get(calendarioFestivitaDto.id) ?: new CalendarioFestivita()
        calendarioFestivita.giorno = calendarioFestivitaDto.giorno
        calendarioFestivita.mese = calendarioFestivitaDto.mese
        calendarioFestivita.anno = calendarioFestivitaDto.anno
        calendarioFestivita.descrizione = calendarioFestivitaDto.descrizione
        calendarioFestivita.valido = calendarioFestivitaDto.valido
        if (calendarioFestivita.version != calendarioFestivitaDto.version) {
            throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
        }
        calendarioFestivita = calendarioFestivita.save()

        return calendarioFestivita.toDTO()
    }

    void elimina (CalendarioFestivitaDTO calendarioFestivitaDto) {
        CalendarioFestivita calendarioFestivita = CalendarioFestivita.get(calendarioFestivitaDto.id)
        if (calendarioFestivita.version != calendarioFestivitaDto.version) {
            throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
        }
        calendarioFestivita.delete()
    }
}
