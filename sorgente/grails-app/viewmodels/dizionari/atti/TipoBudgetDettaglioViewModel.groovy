package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.documenti.Budget
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.dto.dizionari.TipoBudgetDTO
import it.finmatica.atti.dto.dizionari.TipoBudgetDTOService
import it.finmatica.atti.dizionari.TipoBudget
import it.finmatica.atti.dto.documenti.BudgetDTO
import it.finmatica.atti.dto.documenti.DocumentoCollegatoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class TipoBudgetDettaglioViewModel extends AfcAbstractRecord {

	TipoBudgetDTO selectedRecord

    boolean modifica = false
    String motivazione
    def listaBudget
    boolean abilitaGestioneFondo
    def listaStorico

	// services
	TipoBudgetDTOService tipoBudgetDTOService

	@NotifyChange(["selectedRecord"])
	@Init
	void init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		if (id != null) {
			selectedRecord = caricaTipoBudgetDto(id)
            listaBudget = tipoBudgetDTOService.listaBudget(selectedRecord)
            listaStorico = tipoBudgetDTOService.listaStorico(selectedRecord)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else {
			selectedRecord = new TipoBudgetDTO(id: -1, tipo:"BUDGET", valido: true, attivo: true, importoIniziale: 0, anno: new Date().format("yyyy").toInteger())
		}
        abilitaGestioneFondo = Impostazioni.GESTIONE_FONDI.abilitato
	}

	private TipoBudgetDTO caricaTipoBudgetDto(Long idTipoBudget) {
		return TipoBudget.get(idTipoBudget).toDTO(["unitaSo4", "utenteAd4"])
	}

	// Estendo i metodi abstract di AfcAbstractRecord
	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica", "modifica", "motivazione"])
	@Command
	def onSalva() {
        if (selectedRecord.anno == null){
            throw new AttiRuntimeException("Anno: campo obbligatorio")
        }
        if (selectedRecord.unitaSo4 == null){
            throw new AttiRuntimeException("Unità: campo obbligatorio")
        }
        if (selectedRecord.utenteAd4 == null){
            throw new AttiRuntimeException("Firmatario: campo obbligatorio")
        }
        if (selectedRecord.tipo == null){
            throw new AttiRuntimeException("Tipo: campo obbligatorio")
        }
        if (selectedRecord.importoIniziale == null || selectedRecord.importoIniziale < 0){
            throw new AttiRuntimeException("Importo Iniziale: campo obbligatorio")
        }
        if (modifica && (motivazione == null || motivazione.isEmpty())){
            throw new AttiRuntimeException("Motivazioni: campo obbligatorio")
        }
        if (Impostazioni.GESTIONE_FONDI.abilitato && "BUDGET".equals(selectedRecord.tipo) && selectedRecord.contoEconomico == null){
            throw new AttiRuntimeException("Conto Economico: campo obbligatorio")
        }

        if (modifica){
            tipoBudgetDTOService.salvaStorico(selectedRecord, motivazione)
            modifica = false
            motivazione = null
        }

		selectedRecord = tipoBudgetDTOService.salva(selectedRecord)
		aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command
	def onSalvaChiudi() {
		onSalva()
		onChiudi()
	}

	@Command
	def onSettaValido(@BindingParam("valido") boolean valido) {
		Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto", [valido ? "valido" : "non valido"].toArray()),
				Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
				Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
					public void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							selectedRecord.valido = valido
							onSalva()
							BindUtils.postNotifyChange(null, null, TipoBudgetDettaglioViewModel.this, "selectedRecord")
							BindUtils.postNotifyChange(null, null, TipoBudgetDettaglioViewModel.this, "datiCreazione")
							BindUtils.postNotifyChange(null, null, TipoBudgetDettaglioViewModel.this, "datiModifica")
						}
					}
				}
		)
	}

    @NotifyChange(["selectedRecord", "datiCreazione", "datiModifica", "modifica", "motivazione"])
    @Command
    def onModifica() {
        modifica = true
    }

    @Command
    void apriDocumento (@BindingParam("budget") Budget budget) {
        if (budget.determina != null) {
            Executions.createComponents("/atti/documenti/determina.zul", null, [id: budget.determina.id]).doModal()
        } else if (budget.propostaDelibera != null) {
            Executions.createComponents("/atti/documenti/propostaDelibera.zul", null, [id: budget.propostaDelibera.id]).doModal()
        }
    }

}