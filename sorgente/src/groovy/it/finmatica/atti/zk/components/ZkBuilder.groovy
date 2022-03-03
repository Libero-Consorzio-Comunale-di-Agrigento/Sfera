package it.finmatica.atti.zk.components

import org.zkoss.zk.ui.Component

/**
 * Created by esasdelli on 29/08/2017.
 */
class ZkBuilder {

    private final Closure builder
    Component context

    ZkBuilder (Closure closure) {
        this.builder = closure
    }

    void renderTo (Component component) {
        component.children.clear()
        context = component
        build (builder)
    }

    private void build (Closure closure) {
        if (closure == null) {
            return
        }

        closure.delegate = this
        closure()
    }

    def methodMissing (String name, args) {
        Class zkComponent = Class.forName("org.zkoss.zul."+name.capitalize())
        Component component = zkComponent.newInstance()
        def properties = args.find { it instanceof Map }
        def nextClosure = args.find { it instanceof Closure }
        for (def property : properties) {
            component."${property.key}" = property.value
        }
        context.appendChild(component)
        Component prevContext = context
        context = component
        build (nextClosure)
        context = prevContext
        return component
    }
}
