package odg

import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dto.odg.*
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.odg.CommissioneComponente
import it.finmatica.so4.strutturaPubblicazione.So4RuoloComponentePubb
import org.hibernate.FetchMode
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Listbox
import org.zkoss.zul.Listcell
import org.zkoss.zul.Window

class PopupAggiungiPartecipanteViewModel {

	// service
	ConvocatiSedutaDTOService 	convocatiSedutaDTOService

	// componenti
	Window self

	// dati
	SedutaDTO						seduta
	OggettoSedutaDTO				oggetto
	List<As4SoggettoCorrenteDTO>	listaPartecipanti
	List<SedutaPartecipanteDTO> 	selezionati = []
	// Item selezionato
	def selectedRecord

	// stato
	List<CommissioneComponenteDTO> 	listaCommissioneComponenti
	List<As4SoggettoCorrenteDTO> 	listaPartecipantiNonConvocati
	List<As4SoggettoCorrenteDTO> 	partecipanti = []
	String imageSrc
	int sequenza

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("oggetto") OggettoSedutaDTO oggetto, @ExecutionArgParam("elenco") List<OggettoPartecipanteDTO> elenco)  {
		this.self = w
		this.oggetto = oggetto
		this.seduta = oggetto.seduta
		imageSrc = "/images/rosso.png"
		calcolaListaPartecipantiSeduta(elenco)
		sequenza = elenco.size()
		listaPartecipantiNonConvocati = caricaListaNonConvocati()
		listaPartecipanti = listaPartecipantiNonConvocati + calcolaListaPartecipantiRuolo()
	}

	private void calcolaListaPartecipantiSeduta(List<OggettoPartecipanteDTO> elenco) {
		for (OggettoPartecipanteDTO p : elenco) {
			As4SoggettoCorrenteDTO s = (p.sedutaPartecipante.componenteEsterno)?p.sedutaPartecipante.componenteEsterno:p.sedutaPartecipante.commissioneComponente.componente
			partecipanti.add(s)
		}
	}

	private List<As4SoggettoCorrenteDTO> caricaListaNonConvocati() {
		List<As4SoggettoCorrenteDTO> listaSoggetto = []
		if(!partecipanti.empty) {
			listaCommissioneComponenti = CommissioneComponente.createCriteria().list(){
				eq('valido', true)
				eq('commissione.id', seduta.commissione.id)
				componente {
					not {'in'('id',partecipanti*.id)}
				}
				fetchMode("componente", FetchMode.JOIN)
			}?.toDTO().unique()
			listaSoggetto = listaCommissioneComponenti*.componente
		}
		return listaSoggetto
	}

	private List<As4SoggettoCorrenteDTO> calcolaListaPartecipantiRuolo(){
		String impostazione
		List<String> listaRuoli = []
		List<As4SoggettoCorrenteDTO> listaSoggetto = []
		List<String> id_listaNonPartecipanti =  []
		List<String> id_listaPartecipanti = []
		Long vuoto = 0

		listaRuoli = Impostazioni.ODG_RUOLI_CONVOCATI.valori

		if (listaRuoli.size() > 0) {
			id_listaPartecipanti 	= partecipanti*.id
			id_listaNonPartecipanti	= listaPartecipantiNonConvocati*.id

			listaSoggetto = So4RuoloComponentePubb.executeQuery(
				"""select distinct rp.componente.soggetto
					 from So4RuoloComponentePubb rp
					where rp.ruolo.ruolo in (:listaRuoli)
				      and rp.componente.soggetto.id not in (:listaPartecipanti)
					  and rp.componente.soggetto.id not in (:listaNonPartecipanti)
					order by denominazione asc """, [listaRuoli:listaRuoli, listaPartecipanti:id_listaPartecipanti?:[vuoto], listaNonPartecipanti:id_listaNonPartecipanti?:[vuoto]])?.toDTO()
		}

		return listaSoggetto
	}

	private SedutaPartecipanteDTO creaOggettoPartecipante(CommissioneComponenteDTO comp, As4SoggettoCorrenteDTO soggetto){
		SedutaPartecipanteDTO selezionato =  new SedutaPartecipanteDTO(id: -1)
		selezionato.commissioneComponente = comp
		selezionato.convocato = false
		selezionato.presente = false
		selezionato.assenteNonGiustificato = false
		selezionato.seduta = seduta
		selezionato.sequenza = sequenza;
		selezionato.sequenzaPartecipante = sequenza
		selezionato.ruoloPartecipante =  comp?.ruoloPartecipante;
		selezionato.componenteEsterno = (comp!=null) ? null : soggetto
		selezionato = convocatiSedutaDTOService.salva(selezionato)
		return selezionato
	}

	@NotifyChange("imageSrc")
	@Command checkImageSrc(@ContextParam(ContextType.COMPONENT) Listcell lc, @BindingParam("partecipante")  As4SoggettoCorrenteDTO partecipante) {
		List<As4SoggettoCorrenteDTO> lista = listaCommissioneComponenti*.componente
		imageSrc = (partecipante in lista) ? "/images/agsde2/16x16/point_green.png" : "/images/agsde2/16x16/point_red.png"
		lc.setImage(imageSrc)
	}

	@NotifyChange("partecipante")
	@Command onSalva(@BindingParam("listaSoggetti") Listbox listaSoggetti) {
		listaSoggetti.getSelectedItems().each {
			boolean isComponente = false
			As4SoggettoCorrenteDTO s = it.value
			listaCommissioneComponenti.each { c ->
				if (s in c.componente) {
					selezionati.add(creaOggettoPartecipante(c,s))
					isComponente = true
					return
				}
			}
			if (!isComponente)
				selezionati.add(creaOggettoPartecipante(null,s))
		}
		Events.postEvent(Events.ON_CLOSE, self, selezionati)
	}

	@NotifyChange("partecipante")
	@Command onSalvaChiudi(@BindingParam("listaSoggetti") Listbox listaSoggetti) {
		onSalva(listaSoggetti)
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command onChiudi() {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
