package it.finmatica.atti.zk;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.sys.IdGenerator;

public class TestIdGenerator implements IdGenerator {

    private static final String PREFIX = "zk_comp_";
    private static final String INDEX_KEY = "Id_Num";

    public String nextComponentUuid(Desktop desktop, Component comp) {
        int i = Integer.parseInt(desktop.getAttribute(INDEX_KEY).toString());
        i++;
        desktop.setAttribute(INDEX_KEY, String.valueOf(i));

        return PREFIX + i;
    }

    public String nextComponentUuid(Desktop desktop, Component comp, ComponentInfo info) {
        return nextComponentUuid(desktop, comp);
    }

    public String nextDesktopId(Desktop desktop) {
        if (desktop.getAttribute(INDEX_KEY) == null) {
            desktop.setAttribute(INDEX_KEY, "0");
        }
        return null;
    }

    public String nextPageUuid(Page page) {
        return null;
    }
}