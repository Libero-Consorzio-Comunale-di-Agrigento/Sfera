package afc

import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.zk.ui.event.Event


abstract class AfcAbstractGrid implements IAfcGrid {

	public static final int PAGE_SIZE_DEFAULT = 30

	// Paginazione
	int pageSize 	= PAGE_SIZE_DEFAULT
	int activePage 	= 0
	int	totalSize	= 0

	// Filtro in ricerca
	String filtro

	// Item selezionato
	def selectedRecord

	// Filtro su record validi
	boolean visualizzaTutti = false

	@Command abstract onPagina();
	@Command abstract onRefresh();
	@Command abstract onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord);
	@Command abstract onElimina();
	@Command abstract onVisualizzaTutti();
	@Command abstract onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event);
	@Command abstract onCancelFiltro();

}
