package it.finmatica.atti.integrazioni

class SedutaExcel {
    String nominativo
    String commissione
    Date dataSeduta
    String presente
    Date dataEntrata
    Date dataUscita
    String oraEntrata
    String oraUscita

    String getNominativo() {
        return nominativo
    }

    void setNominativo(String nominativo) {
        this.nominativo = nominativo
    }

    String getCommissione() {
        return commissione
    }

    void setCommissione(String commissione) {
        this.commissione = commissione
    }


    String getPresente() {
        return presente
    }

    void setPresente(String presente) {
        this.presente = presente
    }

    Date getDataSeduta() {
        return dataSeduta
    }

    void setDataSeduta(Date dataSeduta) {
        this.dataSeduta = dataSeduta
    }

    Date getDataEntrata() {
        return dataEntrata
    }

    void setDataEntrata(Date dataEntrata) {
        this.dataEntrata = dataEntrata
    }

    Date getDataUscita() {
        return dataUscita
    }

    void setDataUscita(Date dataUscita) {
        this.dataUscita = dataUscita
    }

    String getOraEntrata() {
        return oraEntrata
    }

    void setOraEntrata(String oraEntrata) {
        this.oraEntrata = oraEntrata
    }

    String getOraUscita() {
        return oraUscita
    }

    void setOraUscita(String oraUscita) {
        this.oraUscita = oraUscita
    }

}
