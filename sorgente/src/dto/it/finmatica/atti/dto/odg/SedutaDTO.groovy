package it.finmatica.atti.dto.odg

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dto.odg.dizionari.TipoSedutaDTO
import it.finmatica.atti.odg.Seduta
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class SedutaDTO implements it.finmatica.dto.DTO<Seduta> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Integer anno;
    CommissioneDTO commissione;
    boolean completa;
    Date dataFineSeduta;
    Date dataInizioSeduta;
    Date dataSecondaConvocazione;
    Date dataSeduta;
    Date dateCreated;
    Date lastUpdated;
    String note;
    Integer numero;
    String oraFineSeduta;
    String oraInizioSeduta;
    String oraSecondaConvocazione;
    String oraSeduta;
    Set<SedutaPartecipanteDTO> partecipanti;
    Set<SedutaStampaDTO> stampe;
    boolean pubblica;
    boolean pubblicaWeb;
    boolean secondaConvocazione;
    SedutaDTO secondaSeduta;
    String sede;
    TipoSedutaDTO tipoSeduta;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    boolean votoPresidente;
    String link;

    public void addToStampe (SedutaStampaDTO sedutaStampa) {
        if (this.stampe == null)
            this.stampe = new HashSet<SedutaStampaDTO>()
        this.stampe.add (sedutaStampa);
        sedutaStampa.seduta = this
    }

    public void removeFromStampe (SedutaStampaDTO sedutaStampa) {
        if (this.stampe == null)
            this.stampe = new HashSet<SedutaStampaDTO>()
        this.stampe.remove (sedutaStampa);
        sedutaStampa.seduta = null
    }

    public void addToPartecipanti (SedutaPartecipanteDTO sedutaPartecipante) {
        if (this.partecipanti == null)
            this.partecipanti = new HashSet<SedutaPartecipanteDTO>()
        this.partecipanti.add (sedutaPartecipante);
        sedutaPartecipante.seduta = this
    }

    public void removeFromPartecipanti (SedutaPartecipanteDTO sedutaPartecipante) {
        if (this.partecipanti == null)
            this.partecipanti = new HashSet<SedutaPartecipanteDTO>()
        this.partecipanti.remove (sedutaPartecipante);
        sedutaPartecipante.seduta = null
    }

    public Seduta getDomainObject () {
        return Seduta.get(this.id)
    }

    public Seduta copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    public boolean isIniziata () {
        return (new Date().after(dataSeduta.clearTime()+1))
    }

    public Date getDataOraSeduta () {
        return it.finmatica.atti.commons.AttiUtils.dataOra(dataSeduta, oraSeduta);
    }

    public Date getDataOraInizioSeduta () {
        return it.finmatica.atti.commons.AttiUtils.dataOra(dataInizioSeduta, oraInizioSeduta);
    }

    public Date getDataOraFineSeduta () {
        return it.finmatica.atti.commons.AttiUtils.dataOra(dataFineSeduta, oraFineSeduta);
    }

    public Date getDataOraSecondaConvocazione () {
        return it.finmatica.atti.commons.AttiUtils.dataOra(dataSecondaConvocazione, oraSecondaConvocazione);
    }
}
