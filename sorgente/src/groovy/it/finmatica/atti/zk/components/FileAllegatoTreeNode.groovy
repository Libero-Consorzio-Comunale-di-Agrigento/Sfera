package it.finmatica.atti.zk.components

import groovy.transform.CompileStatic
import org.zkoss.zul.DefaultTreeNode;

@CompileStatic
class FileAllegatoTreeNode<T> extends DefaultTreeNode<T> {
    private static final long serialVersionUID = -8085873079938209759L;

    // Node Control the default open
    private boolean open = true;

    FileAllegatoTreeNode (T data, FileAllegatoTreeNodeCollection<T> children, boolean open) {
        super(data, children);
        this.setOpen(open);
    }

    FileAllegatoTreeNode (T data, FileAllegatoTreeNodeCollection<T> children) {
        super(data, children);
    }

    FileAllegatoTreeNode (T data) {
        super(data);
    }

    boolean isOpen () {
        return open;
    }

    void setOpen (boolean open) {
        this.open = open;
    }
}