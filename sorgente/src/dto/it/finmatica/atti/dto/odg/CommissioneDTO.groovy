package it.finmatica.atti.dto.odg

import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.odg.Commissione
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class CommissioneDTO implements it.finmatica.dto.DTO<Commissione> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Set<CommissioneComponenteDTO> componenti;
    boolean controlloFirmatari;
    Date dateCreated;
    String descrizione;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    Long progressivoCfgIter;
    boolean pubblicaWeb;
    boolean ruoliObbligatori;
    Ad4RuoloDTO ruoloCompetenze;
    Ad4RuoloDTO ruoloVisualizza;
    boolean secondaConvocazione;
    boolean sedutaPubblica;
    Set<CommissioneStampaDTO> stampe;
    TipoRegistroDTO tipoRegistro;
    TipoRegistroDTO tipoRegistroSeduta;
    String titolo;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;
    boolean votoPresidente;

    public void addToComponenti (CommissioneComponenteDTO commissioneComponente) {
        if (this.componenti == null)
            this.componenti = new HashSet<CommissioneComponenteDTO>()
        this.componenti.add (commissioneComponente);
        commissioneComponente.commissione = this
    }

    public void removeFromComponenti (CommissioneComponenteDTO commissioneComponente) {
        if (this.componenti == null)
            this.componenti = new HashSet<CommissioneComponenteDTO>()
        this.componenti.remove (commissioneComponente);
        commissioneComponente.commissione = null
    }
    public void addToStampe (CommissioneStampaDTO commissioneStampa) {
        if (this.stampe == null)
            this.stampe = new HashSet<CommissioneStampaDTO>()
        this.stampe.add (commissioneStampa);
        commissioneStampa.commissione = this
    }

    public void removeFromStampe (CommissioneStampaDTO commissioneStampa) {
        if (this.stampe == null)
            this.stampe = new HashSet<CommissioneStampaDTO>()
        this.stampe.remove (commissioneStampa);
        commissioneStampa.commissione = null
    }

    public Commissione getDomainObject () {
        return Commissione.get(this.id)
    }

    public Commissione copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
