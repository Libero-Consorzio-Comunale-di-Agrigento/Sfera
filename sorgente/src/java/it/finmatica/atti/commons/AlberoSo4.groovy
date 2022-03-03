package it.finmatica.atti.commons

import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.so4.dto.strutturaPubblicazione.So4ComponentePubbDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4RuoloComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.hibernate.FetchMode
import org.zkoss.zul.AbstractTreeModel

class AlberoSo4 extends AbstractTreeModel<Object> {

	private String ente
	private String ottica
	def cache = [:]
	def so4UnitaPubbFake
	String filtro
	boolean consentiCaricamentoCache

	String ruoloComponenti

	public AlberoSo4(String ente, String ottica, So4UnitaPubbDTO root, String filtro ){
		super([elemento: root, tipoElemento: "UO"])
		this.ente = ente
		this.ottica = ottica
		this.filtro = filtro.trim()
		cache = [:]
		consentiCaricamentoCache=true
		ruoloComponenti = Impostazioni.RUOLO_SO4_NOTIFICHE.valore
		if(this.filtro != null && this.filtro != ""){
			// verifico se esistono dei risultati della ricerca
			consentiCaricamentoCache = verificaPresenzaComponentiOppureUo()
			if(consentiCaricamentoCache == true){
				// carico solo l'albero che rispetta le condizioni di filtraggio
				caricaCacheDalFiglio()
				sortCache()
			}
		}
	}

	public boolean isLeaf(Object node) {
		String tipoElemento = node.tipoElemento
		boolean isLeaf = false
		if(consentiCaricamentoCache == false){return isLeaf}

		if(tipoElemento == "UO"){
			long progressivoPadreInCache = caricaCacheDalPadre(node)

			if(progressivoPadreInCache >=0 &&  cache[progressivoPadreInCache].size() == 0){
				isLeaf = true
			}

			if(progressivoPadreInCache < 0){
				isLeaf = false
			}
		}

		if(tipoElemento == "componente"){
			isLeaf = true
		}

		return isLeaf
	}

	public def getChild (Object parent, int index) {
		String tipoElemento = parent.tipoElemento
		if(tipoElemento == "UO"){
			So4UnitaPubbDTO padre = (So4UnitaPubbDTO)parent.elemento
			long progressivoPadreInCache = padre.progr
			String tipoElementoFiglio = "UO"

			// se il progressivoPadreInCache è negativo allora il suo figlio è sicuramente un componente
			if (progressivoPadreInCache < 0){
				tipoElementoFiglio = "componente"
			}

			if ((cache[progressivoPadreInCache]?.size()?:0 )<= index) {
				int conteggioFigli = getChildCount(parent)
				return null
			} else {
				// se invece la grandezza della lista contiene l'indice richiesto lo restituice
				if (cache[progressivoPadreInCache]?.get(index) != null) {
					return [elemento: cache[progressivoPadreInCache].get(index), tipoElemento : tipoElementoFiglio]
				} else
					return null
			}
		}
	}

	public int getChildCount(Object parent) {
		int numeroFigli = 0
		String tipoElemento = parent.tipoElemento
		if(consentiCaricamentoCache == false){return numeroFigli}

		if(tipoElemento == "UO"){
			long progressivoPadreInCache = caricaCacheDalPadre(parent)
			numeroFigli = cache[progressivoPadreInCache].size()
		}

		if(tipoElemento == "componente"){
			// se è un componente non ha figli
			numeroFigli = 0
		}

		return numeroFigli

	}

	private void caricaSo4UnitaPubbFake(Long progressivoPadre){
		so4UnitaPubbFake  = new So4UnitaPubbDTO([progr: -progressivoPadre, descrizione: "Componenti", progrPadre : progressivoPadre])
	}


	public int[] getPath(Object node) {
		boolean radiceRaggiunta = false

		List<Integer> paths = new ArrayList<Integer>();
		String tipoElemento = node.tipoElemento

		Long progrPadreNodo
		def figlio = node.elemento

		// inizializzo leggendo il progressivo del padre del nodo
		if(tipoElemento == "UO"){
			progrPadreNodo = figlio.progrPadre
		}else{
			// il segno negativo denota il fatto che un componente è inserito virtualmente in un oggetto So4UnitaPubb con progr = -progr dell'UO in cui è presente la lista di componenti
			progrPadreNodo = -figlio.progrUnita
		}

		while ( !radiceRaggiunta){
			if(progrPadreNodo == null){
				radiceRaggiunta = true
				progrPadreNodo = 0
			}
			def Children = cache[progrPadreNodo]
			int index = 0
			for(i in Children){
				if(i instanceof So4UnitaPubbDTO){
					// se il figlio è una unita So4
					if(figlio instanceof So4UnitaPubbDTO && figlio.progr == i.progr){
						paths.add(index);
						break;
					}
				}
				if(i instanceof So4ComponentePubbDTO){
					if(figlio instanceof So4ComponentePubbDTO && figlio.id == i.id){
						paths.add(index);
						break;
					}
				}
				index ++
			}
			if(!radiceRaggiunta){
				// cerco il padre nella cache che diventa figlio della prossima iterazione del while

				def listaContenente = cache.find{lista ->
					lista.value.find{ el ->
						el instanceof So4UnitaPubbDTO && el.progr  == progrPadreNodo
					} != null
				}


				figlio = listaContenente.value.find{el -> el instanceof So4UnitaPubbDTO && el.progr  == progrPadreNodo}


				progrPadreNodo = figlio.progrPadre
			}

		}


		int[] p = new int[paths.size()];
		for( int index = 0; index < paths.size(); index++){
			p[index] = paths.get(p.length - 1 - index); // reverse
		}
		return p;
	}


	// Questo metodo carica nella cache le informazioni sul nodo figlio e restituisce il progressivoDelPadre utile per accedere ai figli nella cache (cache[progressivoPadre])
	private long caricaCacheDalPadre(Object parent){
		So4UnitaPubbDTO padre = (So4UnitaPubbDTO)parent.elemento
		long progressivoPadreInCache = padre.progr


		// se non esiste in cache il progressivo del padre, allora si leggono i figli di quel nodo
		if(progressivoPadreInCache >=0 && cache[progressivoPadreInCache] == null){
			List<So4UnitaPubb> lista
			lista = So4UnitaPubb.createCriteria().list(){
				eq("ottica.codice", ottica)
				eq("amministrazione.codice", ente)
				le("dal", org.apache.commons.lang.time.DateUtils.truncate(new Date(), Calendar.DATE))
				or{
					isNull("al")
					ge("al", org.apache.commons.lang.time.DateUtils.truncate(new Date(), Calendar.DATE))
				}
				if(padre.progr != 0) {
					eq("progrPadre", padre.progr)
				} else{
					isNull("progrPadre")
				}

				order("descrizione", "asc")
			}
			List<So4UnitaPubbDTO> listaDTO
			// leggo se sono presenti dei componenti
			List<So4ComponentePubb> componenti  = So4RuoloComponentePubb.allaData(new Date()){
				projections{ property("componente") }
				eq("ruolo.ruolo",ruoloComponenti)
				componente {
					eq("progrUnita", progressivoPadreInCache)
					eq("ottica.codice",ottica)
					and {
						le("dal", org.apache.commons.lang.time.DateUtils.truncate(new Date(), Calendar.DATE))
						or {
							isNull("al")
							ge("al", org.apache.commons.lang.time.DateUtils.truncate(new Date(), Calendar.DATE))
						}
					}
					fetchMode("soggetto", FetchMode.JOIN)
					fetchMode("soggetto.utenteAd4", FetchMode.JOIN)
					order ("nominativoSoggetto", "asc")
				}

			}
			//			So4ComponentePubb.componentiUnitaPubb(progressivoPadreInCache,ottica,new Date()){
			//				fetchMode("soggetto", FetchMode.JOIN)
			//				fetchMode("soggetto.utenteAd4", FetchMode.JOIN)
			//			}
			if(componenti.size() != 0){
				// salvo in cache la lista coi componenti associandola al valore del progressivo negato
				cache[-progressivoPadreInCache] = componenti.toDTO()
				// aggiungo in testa alla lista dei figli il nodo fittizio di tipo So4UnitaPub con progressivo = - progressivo del padre, il metodo getSo4UnitaPub fake gestisce la creazione
				caricaSo4UnitaPubbFake(progressivoPadreInCache)
				listaDTO = new ArrayList<So4UnitaPubbDTO>()
				listaDTO.add(so4UnitaPubbFake)
				listaDTO.addAll(lista.toDTO())
			}else{
				listaDTO = lista.toDTO()
			}
			// salvo in cache
			cache[progressivoPadreInCache] = listaDTO
		}
		if(progressivoPadreInCache < 0 && cache[progressivoPadreInCache] == null){
			List<So4ComponentePubb> componenti = So4ComponentePubb.componentiUnitaPubb(-progressivoPadreInCache,ottica,new Date()).list();
			cache[progressivoPadreInCache] = componenti.toDTO()
		}
		return progressivoPadreInCache
	}


	private void caricaCacheDalFiglio(){
		List<So4ComponentePubb> listaComponenti = So4RuoloComponentePubb.allaData(new Date()){
			projections{ property("componente") }
			eq("ruolo.ruolo",ruoloComponenti)
			componente {
				eq("ottica.codice",ottica)
				and {
					le("dal", org.apache.commons.lang.time.DateUtils.truncate(new Date(), Calendar.DATE))
					or {
						isNull("al")
						ge("al", org.apache.commons.lang.time.DateUtils.truncate(new Date(), Calendar.DATE))
					}
				}
				ilike("nominativoSoggetto","%"+filtro+"%")
				fetchMode("soggetto", FetchMode.JOIN)
				fetchMode("soggetto.utenteAd4", FetchMode.JOIN)
			}

		}


		//		List<So4ComponentePubb> listaComponenti = So4ComponentePubb.allaData(new Date()).perOttica(ottica) {
		//			ilike("nominativoSoggetto","%"+filtro+"%")
		//			fetchMode("soggetto", FetchMode.JOIN)
		//			fetchMode("soggetto.utenteAd4", FetchMode.JOIN)
		//		}

		def cacheComponenti = [:]

		// creo la cache contenente i soli componenti
		cacheComponenti = listaComponenti.groupBy{ -it.progrUnita }

		List<Long> listaProgrPadreComponenti = new ArrayList<Long>()

		// metto nella cache i componenti ed estraggo la lista dei So4UnitaPubb fittizi da creare per gestire i componenti
		cacheComponenti.each{
			cache.put(it.key, it.value.toDTO())
			listaProgrPadreComponenti.add(-it.key)
		}



		// a questo punto creo i contenitori dei componenti e li inserisco in cache
		listaProgrPadreComponenti.each{
			So4UnitaPubbDTO componentContainer = new So4UnitaPubbDTO(progr: -it, progrPadre: it, descrizione:"Componenti")

			if(cache[it] == null){
				// creo la lista
				List<So4UnitaPubbDTO> listaContainer = new ArrayList<So4UnitaPubbDTO>()
				listaContainer.add(componentContainer)
				cache[it] = listaContainer

			}else{
				// lo aggiungo alla lista
				cache[it].add(componentContainer)
			}



		}

		// Una volta creata la cache contenente i componenti, è necessario caricare tutte le UO tali che corrispondono alla ricerca
		// tale query, oltre ad andare a prelevare le UO richieste dal filtro, preleva anche quelle con progr in listaProgrPadreComponenti
		// i dati letti vengono inseriti in un Set

		Set<So4UnitaPubbDTO> insiemeUO = So4UnitaPubb.allaData(new Date()).perOttica(ottica) {
			if(listaProgrPadreComponenti.size() > 0){
				or{
					'in'("progr", listaProgrPadreComponenti)
					ilike("descrizione", "%"+filtro+"%")
				}
			}else{
				ilike("descrizione", "%"+filtro+"%")
			}

		}.toDTO()


		insiemeUO.each{ uo -> getPadre(uo) }
	}

	private void getPadre(So4UnitaPubbDTO uo){
		So4UnitaPubbDTO nodoFiglio = uo
		long progrPadre = nodoFiglio.progrPadre?:(long)0


		while(true){
			// devo inserire il nodoFiglio
			if(cache[progrPadre] == null){
				// creo la lista
				List<So4UnitaPubbDTO> listaContainer = new ArrayList<So4UnitaPubbDTO>()
				listaContainer.add(nodoFiglio)
				cache[progrPadre] = listaContainer
			}else{
				boolean nodoPresente = false
				for(i in cache[progrPadre]){
					if(i.progr == nodoFiglio.progr){
						nodoPresente = true
					}
				}
				if(!nodoPresente){
					cache[progrPadre].add(nodoFiglio)
				}
			}

			if(progrPadre == 0){
				// se il progr padre è 0 (si è già raggiunta la radice) termina il metodo
				break
			}
			// verifico se il padre è già presente nella cache
			def listaContenente = cache.find{lista ->
				lista.value.find{ el ->
					el instanceof So4UnitaPubbDTO && el.progr  == progrPadre
				} != null
			}
			if(listaContenente != null){
				// il padre è già presente e quindi è stata raggiunta la radice o sarà raggiunta in un altra iterazione
				break
			}
			// se invece listaConente è null allora viene letto il nodo padre che diventa il nuovo nodoFiglio nella iterazione successiva
			nodoFiglio = So4UnitaPubb.getUnita(progrPadre, ottica).get().toDTO()
			// calcolo il progrPadre del padre del nuovo nodoFiglio
			progrPadre = nodoFiglio.progrPadre?:(long)0
		}
	}

	private void sortCache(){
		cache.each{ el ->
			if((long)el.key >= 0){
				el.value.sort{
					if(it.progr < 0){
						return "00000000000000000000"
					}else{
						it.descrizione
					}
				}
			}else{
				// ordino le liste contenenti i componenti
				el.value.sort{ it.nominativoSoggetto }
			}
		}
	}

	private boolean verificaPresenzaComponentiOppureUo() {
		boolean result = false
		def rowCountUo
		def rowCountComponente = So4ComponentePubb.allaData(new Date()).perOttica(ottica) {
			projections{ rowCount() }
			ilike("nominativoSoggetto","%"+filtro+"%")
		}

		if (rowCountComponente[0] > 0) {
			result = true
		} else {
			rowCountUo = So4UnitaPubb.allaData(new Date()).perOttica(ottica) {
				projections { rowCount() }
				ilike("descrizione", "%"+filtro+"%")

			}

			if(rowCountUo[0] > 0) {
				result = true
			}
		}

		return result
	}
}
