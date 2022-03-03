package afc

import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.zk.ui.event.Event

public interface IAfcGrid {

	@Command onPagina();
	@Command onRefresh();
	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord);
	@Command onElimina();
	@Command onVisualizzaTutti();
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event);
	@Command onCancelFiltro();

}
