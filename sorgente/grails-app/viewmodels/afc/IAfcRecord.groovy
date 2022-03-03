package afc

import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command

public interface IAfcRecord {

	@Command onChiudi();
	@Command onSalva();
	@Command onSalvaChiudi();
	@Command onSettaValido(@BindingParam("valido") boolean valido);

}
