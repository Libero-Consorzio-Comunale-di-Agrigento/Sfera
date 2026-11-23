package atti.integrazioni.protocollo

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.documenti.DestinatarioNotifica
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.dto.dizionari.EmailDTO
import it.finmatica.atti.dto.dizionari.EmailDTOService
import it.finmatica.atti.dto.documenti.DestinatarioNotificaDTO
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.protocollo.ProtocolloPiTre
import it.finmatica.atti.integrazioni.protocollo.ProtocolloPiTreConfig
import it.finmatica.atti.integrazioniws.pitre.classes.Correspondent
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.springframework.beans.factory.annotation.Autowired
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupSceltaDestinatariPiTreViewModel {

    ProtocolloPiTre protocolloPiTre
    @Autowired ProtocolloPiTreConfig protocolloPiTreConfig
    @Autowired StrutturaOrganizzativaService strutturaOrganizzativaService

    EmailDTOService emailDTOService

    String filtroSoggetti
    DestinatarioNotificaDTO destNotifica

    String nome
    String cognome
    String email
    String idSoggettoEsterno
    String tipoSoggettoEsterno
    String tipoCorrispondenteEsterno
    String tipoRubricaEsterna

    CodiceDescrizione rubrica
    List<CodiceDescrizione> listaRubriche = new ArrayList<CodiceDescrizione>()


    def destinatariList
    int pageSize = 10
    int activePage = 0
    int totalSize = 0

    Window self

    So4UnitaPubb unitaIstituzionaleDelRedattore

    String codice
    String denominazione
    //String tipoGruppo = Correspondent.TYPE_GROUP_OFFICES
    String tipo=Correspondent.TYPE_INTERNAL


    boolean tipoGruppoOffices = true
    boolean tipoGruppoRoles = true
    boolean tipoGruppoUsers = false

    boolean visualizzaExternal=false
    boolean visualizzaUtenti=true

    String tipoInternal = Correspondent.TYPE_INTERNAL
    String tipoExternal = Correspondent.TYPE_EXTERNAL
    String tipoGlobal = Correspondent.TYPE_GLOBAL

    int indiceNotifica = 0

    @NotifyChange(["destNotifica", "destinatariList", "totalSize"])
    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window w,
            @ExecutionArgParam("destinatarioEsterno") DestinatarioNotificaDTO input,
            @ExecutionArgParam("documento") IDocumento documento) {
        this.self = w

        // Inizializo le variabili di classe
        activePage = 0
        filtroSoggetti = ""
        if(input!=null){
            destNotifica = input
        } else {
            destNotifica = new DestinatarioNotificaDTO()
        }
        destNotifica.tipoNotifica = DestinatarioNotifica.TIPO_NOTIFICA_COMPETENZA
        //qui deve arrivare l'unita istituzionale del redattore

        if(documento instanceof Determina){
            visualizzaExternal =true
            unitaIstituzionaleDelRedattore=protocolloPiTre.calcoloUnitaServizioIstituzionaleDelRedattore(documento,null)
        }

        if(documento instanceof PropostaDelibera){
            Ad4Utente uteRedattore = documento.getSoggetto(TipoSoggetto.REDATTORE).utenteAd4
            unitaIstituzionaleDelRedattore=protocolloPiTre.calcoloUnitaServizioIstituzionaleDelRedattore(null,uteRedattore)
        }

        if(tipo.equals(tipoInternal)){
            visualizzaUtenti=false
        }

        So4UnitaPubb servizio = strutturaOrganizzativaService.getServizio(unitaIstituzionaleDelRedattore)
        listaRubriche.add(new CodiceDescrizione(protocolloPiTreConfig.getRfeByCodiceUnita(servizio.codice),ProtocolloPiTre.RUBRICA_DEI_SERVIZI))
        if(protocolloPiTreConfig.getRubricaEnte()!=null &&  protocolloPiTreConfig.getRubricaEnte().length()>0)
            listaRubriche.add(new CodiceDescrizione(protocolloPiTreConfig.getRubricaEnte(),ProtocolloPiTre.RUBRICA_ENTE))
        if(protocolloPiTreConfig.getRubricaProvinciale()!=null &&  protocolloPiTreConfig.getRubricaProvinciale().length()>0)
            listaRubriche.add(new CodiceDescrizione(protocolloPiTreConfig.getRubricaProvinciale(),ProtocolloPiTre.RUBRICA_PROVINCIALE))
        if(protocolloPiTreConfig.getRubricaNazionale()!=null &&  protocolloPiTreConfig.getRubricaNazionale().length()>0)
            listaRubriche.add(new CodiceDescrizione(protocolloPiTreConfig.getRubricaNazionale(),ProtocolloPiTre.RUBRICA_NAZIONALE))
        rubrica=listaRubriche.isEmpty() ? null : listaRubriche.get(0);

    }

    @Command
    void onSalva() {
        if(destNotifica.idSoggettoEsterno!=null ) {
            Events.postEvent(Events.ON_CLOSE, self, destNotifica)
        } else {
            Events.postEvent(Events.ON_CLOSE, self, null)
        }

    }

    @Command
    void onCheckUtenti() {
        if(tipo.equals(tipoInternal)){
            visualizzaUtenti=false
            tipoGruppoUsers=false
        } else {
            visualizzaUtenti=true
        }
        BindUtils.postNotifyChange(null, null, this, "visualizzaUtenti")
        BindUtils.postNotifyChange(null, null, this, "tipoGruppoUsers")
    }

    @Command
    void onAnnulla() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }


    @Command onCercaSoggettoPiTre () {
        List<Correspondent> listaCorrispondenti= protocolloPiTre.getListaCorrispondenti(tipo, tipoGruppoOffices,tipoGruppoUsers,tipoGruppoRoles,codice,denominazione,unitaIstituzionaleDelRedattore,rubrica)

        List<DestinatarioNotificaDTO> destinatariResult = new ArrayList<DestinatarioNotificaDTO>()
        for (Correspondent correspondent in listaCorrispondenti){
            DestinatarioNotificaDTO destinatarioNotifica = new DestinatarioNotificaDTO()
            destinatarioNotifica.idSoggettoEsterno=correspondent.Id
            destinatarioNotifica.tipoSoggettoEsterno=correspondent.Type
            destinatarioNotifica.tipoCorrispondenteEsterno = correspondent.CorrespondentType
            destinatarioNotifica.tipoRubricaEsterna = rubrica.descrizione
            if( (correspondent.Name==null || correspondent.Name.length()==0 ) && (correspondent.Surname==null || correspondent.Surname.length()==0) && correspondent.Description!=null && correspondent.Description.length()>0) {
                destinatarioNotifica.nome = correspondent.Code?.toUpperCase()
                destinatarioNotifica.cognome = correspondent.Description?.toUpperCase()
            } else {
                destinatarioNotifica.nome = (correspondent.Name!=null?correspondent.Name.toUpperCase():"")
                destinatarioNotifica.cognome = (correspondent.Surname!=null?correspondent.Surname.toUpperCase():"")
            }
            destinatarioNotifica.indirizzo = (correspondent.Address!=null?correspondent.Address.toUpperCase():"")
            destinatarioNotifica.cap = (correspondent.Cap!=null?correspondent.Cap.toUpperCase():"")
            destinatarioNotifica.comune = (correspondent.City!=null?correspondent.City.toUpperCase():"")
            destinatarioNotifica.provincia=(correspondent.Province!=null?correspondent.Province.toUpperCase():"")
            destinatarioNotifica.codiceFiscale = (correspondent.NationalIdentificationNumber!=null?correspondent.NationalIdentificationNumber:"")
            if(correspondent.Email!=null && correspondent.Email.length()>0) {
                EmailDTO correspondentEmail = new EmailDTO()
                correspondentEmail.indirizzoEmail = correspondent.Email.toUpperCase()
                correspondentEmail.nome = (correspondent.Name!=null?correspondent.Name.toUpperCase():"")
                correspondentEmail.cognome = (correspondent.Surname!=null?correspondent.Surname.toUpperCase():"")
                destinatarioNotifica.email = correspondentEmail
                destinatarioNotifica.indirizzoEmail=correspondent.Email.toUpperCase()
            }
            destinatariResult.add(destinatarioNotifica)
        }

        destinatariList=destinatariResult
        BindUtils.postNotifyChange(null, null, this, "destinatariList")
    }


}
