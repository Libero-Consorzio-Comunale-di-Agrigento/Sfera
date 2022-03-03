package commons

import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.beans.AttiGestoreFile
import it.finmatica.atti.integrazioni.SmartDesktopService
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupVerificaImprontaViewModel {

	IGestoreFile gestoreFile

	Window self

	List lista

	@NotifyChange("lista")
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("documento") IDocumento documento) {
		this.self = w
		lista = new ArrayList();
		if (documento.testo != null) {
			lista.add([file: documento.testo.nome, risultato: gestoreFile.verificaImpronta(documento, documento.testo)])
		}
		if (documento.hasProperty("allegati")){
			for (Allegato allegato : documento.allegati?.findAll { it.valido == true }){
				for (FileAllegato fileAllegato: allegato.fileAllegati?.findAll { it.valido == true }){
					lista.add([file: fileAllegato.nome, risultato: gestoreFile.verificaImpronta(allegato, fileAllegato)])
				}
			}
		}
		if (documento.hasProperty("visti")){
			for (VistoParere visto: documento.visti?.findAll { it.valido == true }) {
				if (visto.testo != null){
					lista.add([file: visto.testo.nome, risultato: gestoreFile.verificaImpronta(visto, visto.testo)])
				}
				for (Allegato allegato : visto.allegati?.findAll { it.valido == true }) {
					for (FileAllegato fileAllegato : allegato.fileAllegati?.findAll { it.valido == true }) {
						lista.add([file: fileAllegato.nome, risultato: gestoreFile.verificaImpronta(allegato, fileAllegato)])
					}
				}
			}
		}
		if (documento instanceof Delibera){
			for (VistoParere visto: documento.proposta.visti?.findAll { it.valido == true }) {
				if (visto.testo != null){
					lista.add([file: visto.testo.nome, risultato: gestoreFile.verificaImpronta(visto, visto.testo)])
				}
				for (Allegato allegato : visto.allegati?.findAll { it.valido == true }) {
					for (FileAllegato fileAllegato : allegato.fileAllegati?.findAll { it.valido == true }) {
						lista.add([file: fileAllegato.nome, risultato: gestoreFile.verificaImpronta(allegato, fileAllegato)])
					}
				}
			}
		}
		if (documento.hasProperty("certificati")){
			for (Certificato certificato: documento.certificati?.findAll { it.valido == true }) {
				if (certificato.testo != null && certificato.stato == StatoDocumento.CONCLUSO) {
					lista.add([file: certificato.testo.nome, risultato: gestoreFile.verificaImpronta(certificato, certificato.testo)])
				}
			}
		}
	}


	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
