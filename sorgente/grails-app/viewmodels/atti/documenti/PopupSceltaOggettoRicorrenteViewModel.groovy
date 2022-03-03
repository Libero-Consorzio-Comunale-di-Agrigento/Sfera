package atti.documenti

import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTO
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class PopupSceltaOggettoRicorrenteViewModel {
	Window self
	def listaOggettiRicorrenti
    def listaOggettiRicorrentiCompleta
	def selectedRecord
	String filtro = ""
	boolean cancella = false

	@NotifyChange(["listaOggettiRicorrenti"])
	@Init
	init(
			@ContextParam(ContextType.COMPONENT) Window w,
			@ExecutionArgParam("listaOggettiRicorrenti") def listaOggettiRicorrenti, @ExecutionArgParam("cancella") boolean cancella) {
		this.self = w
		this.listaOggettiRicorrenti = listaOggettiRicorrenti.sort { a,b -> a.codice <=> b.codice ?: a.oggetto <=> b.oggetto}
        this.listaOggettiRicorrentiCompleta = listaOggettiRicorrenti.sort { a,b -> a.codice <=> b.codice ?: a.oggetto <=> b.oggetto}
		this.cancella = cancella
	}

	@Command
	onSeleziona() {
		if (selectedRecord != null) {
			Events.postEvent(Events.ON_CLOSE, self, selectedRecord)
		} else {
			Messagebox.show("Selezionare uno degli oggetti ricorrenti nella lista", "Attenzione!", Messagebox.OK, Messagebox.EXCLAMATION)
		}
	}

	@Command
	onCancella() {
		Events.postEvent(Events.ON_CLOSE, self, new OggettoRicorrenteDTO(id: -1))
	}

	@Command
	onChiudi() {
        Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@NotifyChange(["listaOggettiRicorrenti"])
	@Command
	onCerca() {
        if (filtro.length() > 0) {
            listaOggettiRicorrenti = listaOggettiRicorrentiCompleta.findAll {
                it.oggetto.indexOf(filtro.toUpperCase()) > -1 || it.codice?.indexOf(filtro.toUpperCase()) > -1
            }
        }
        else {
            listaOggettiRicorrenti = listaOggettiRicorrentiCompleta
        }
	}
}
