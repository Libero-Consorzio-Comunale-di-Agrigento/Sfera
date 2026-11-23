package it.finmatica.atti.zk;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.sys.IdGenerator;

import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StableIdGenerator implements IdGenerator {
    private static final String SESSION_MAP_ATTR = "persistent.semantic.id.map";
    private static final String PREFIX = "tst_";
    private static final int HASH_LEN = 12;

    @Override
    public String nextComponentUuid(Desktop desktop, Component comp, ComponentInfo compInfo) {
        Map<String, String> map = getOrCreateSessionMap(desktop);
        String signature = buildSemanticSignature(comp, compInfo);

        // lookup diretto: se esiste la mapping la riusiamo (verifichiamo ancora che l'id non sia occupato)
        String mapped = map.get(signature);
        if (mapped != null && isUuidAvailable(desktop, mapped)) return mapped;

        // genera base id + prova a registrarlo atomically nella mappa di sessione
        String base = PREFIX + sha256Short(signature, HASH_LEN);
        synchronized (map) {
            // ricontrollo inside lock per evitare race
            mapped = map.get(signature);
            if (mapped != null && isUuidAvailable(desktop, mapped)) return mapped;

            // se base già mappato a signature diversa dobbiamo trovare un suffix unico
            String candidate = base;
            for (int i = 0; i < 1000; i++) {
                if (isUuidAvailable(desktop, candidate)) {
                    // Associa la signature al candidate e ritorna
                    map.put(signature, candidate);
                    return candidate;
                }
                candidate = base + "_" + (i + 1);
            }
            throw new RuntimeException("Unable to generate unique stable id.");
        }
    }

    @Override
    public String nextPageUuid(Page page) {
        String p = page.getId() != null && !page.getId().isEmpty() ? page.getId() : page.getClass().getName();
        return "page_" + sha256Short(p, HASH_LEN);
    }

    @Override
    public String nextDesktopId(Desktop desktop) {
        return "desktop_" + System.identityHashCode(desktop);//dinamico
    }

    private Map<String, String> getOrCreateSessionMap(Desktop desktop) {
        Session zkSess = desktop.getSession();
        HttpSession httpSess = (HttpSession) zkSess.getNativeSession();
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) httpSess.getAttribute(SESSION_MAP_ATTR);
        if (map == null) {
            map = new ConcurrentHashMap<String,String>();
            httpSess.setAttribute(SESSION_MAP_ATTR, map);
        }
        return map;
    }

    private boolean isUuidAvailable(Desktop desktop, String uuid) {
        if (uuid == null) return false;
        Component c = desktop.getComponentByUuidIfAny(uuid);
        return c == null;
    }

    private String buildSemanticSignature(Component comp, ComponentInfo compInfo) {
        StringBuilder sb = new StringBuilder();
        Page page = comp.getPage();
        sb.append("page:");
        sb.append(page != null && page.getId() != null ? page.getId() : (page != null ? page.getClass().getName() : "nopage"));
        sb.append("|def:");
        sb.append(compInfo != null && compInfo.getComponentDefinition() != null
                ? compInfo.getComponentDefinition().getName() : comp.getClass().getName());

        // tenta di estrarre testo/label/value visibile
        String visible = extractVisibleText(comp);
        if (visible != null && !visible.isEmpty()) {
            sb.append("|txt:").append(normalize(visible));
            return sb.toString(); // se trovi testo distintivo, usalo come firma primaria
        }

        // se non c'è testo, prova a includere il path gerarchico semantico
        Deque<String> stack = new ArrayDeque<String>();
        Component cur = comp;
        while (cur != null && !(cur instanceof Page)) {
            StringBuilder lvl = new StringBuilder();
            lvl.append(cur.getDefinition() != null ? cur.getDefinition().getName() : cur.getClass().getSimpleName());
            // indice tra tutti i figli come fallback logico
            lvl.append("#i=").append(indexAmongAllSiblings(cur));
            stack.push(lvl.toString());
            cur = cur.getParent();
        }
        while (!stack.isEmpty()) sb.append("|P:").append(stack.pop());

        return sb.toString();
    }

    private String extractVisibleText(Component comp) {
        try {
            // tentativi comuni: getLabel, getValue, getTooltip, getText, render children text
            Object v;
            // reflection rapido per metodi comuni senza causare errori su classi diverse
            v = tryMethod(comp, "getLabel");
            if (v instanceof String && !((String)v).isEmpty()) return (String)v;
            v = tryMethod(comp, "getValue");
            if (v instanceof String && !((String)v).isEmpty()) return (String)v;
            v = tryMethod(comp, "getTooltip");
            if (v instanceof String && !((String)v).isEmpty()) return (String)v;
            v = tryMethod(comp, "getText");
            if (v instanceof String && !((String)v).isEmpty()) return (String)v;
            // fallback: concatenazione testo figlio
            StringBuilder txt = new StringBuilder();
            for (Object childObj : comp.getChildren()) {
                if (childObj instanceof Component) {
                    String t = extractVisibleText((Component) childObj);
                    if (t != null && !t.isEmpty()) {
                        if (txt.length() > 0) txt.append(" ");
                        txt.append(t);
                    }
                }
            }
            return txt.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private Object tryMethod(Component comp, String methodName) {
        try {
            java.lang.reflect.Method m = comp.getClass().getMethod(methodName);
            return m.invoke(comp);
        } catch (Exception e) {
            return null;
        }
    }

    private int indexAmongAllSiblings(Component comp) {
        Component parent = comp.getParent();
        if (parent == null) return 0;
        List<Component> childs = parent.getChildren();
        int idx = 0;
        for (Component c : childs) {
            if (c == comp) return idx;
            idx++;
        }
        return idx;
    }

    private String normalize(String s) {
        return s.trim().replaceAll("\\s+", " ");
    }

    private String sha256Short(String input, int len) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString().substring(0, Math.min(len, hex.length()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}