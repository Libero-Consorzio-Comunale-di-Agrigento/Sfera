package it.finmatica.atti.config;

import grails.plugin.springsecurity.SpringSecurityService;
import groovy.lang.Closure;
import groovy.transform.CompileStatic;
import it.finmatica.so4.login.So4UserDetail;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Crea un proxy per ogni ENTE configurato nell'ambiente, in modo da poter avere (ad esempio) più bean ciascuno con una
 * configurazione diversa per ente. (ad esempio: client webservice con utenti e password diversi per enti)
 *
 * Created by esasdelli on 21/11/2017.
 */
@CompileStatic
public class MultiEnteProxyFactoryBean<T> extends AbstractFactoryBean<T> implements InvocationHandler {
    private final SpringSecurityService        springSecurityService;
    private final Closure                      beanBuilder;
    private final ConcurrentHashMap<String, T> map;
    private final Class<T>                     targetInterface;
    private T targetBean;

    public MultiEnteProxyFactoryBean (SpringSecurityService springSecurityService, Class<T> targetInterface, Closure beanBuilder) {
        this.map = new ConcurrentHashMap<String, T>();
        this.targetInterface = targetInterface;
        this.springSecurityService = springSecurityService;
        this.beanBuilder = beanBuilder;
        this.targetBean = null;
    }

    @Override
    public Object invoke (Object proxy, Method method, Object[] args) throws Throwable {
        // ottengo l'ente corrente:
        So4UserDetail userDetail = (So4UserDetail) springSecurityService.getPrincipal();
        String        codiceEnte = userDetail.amm().getCodice();

        T storedTargetBean = map.get(codiceEnte);
        if (storedTargetBean == null) {
            storedTargetBean = ((T) (beanBuilder.call()));

            map.put(codiceEnte, storedTargetBean);
        }

        return method.invoke(storedTargetBean, args);
    }

    @Override
    public Class<?> getObjectType () {
        return targetInterface;
    }

    @Override
    protected T createInstance () throws Exception {
        return ((T) (Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{targetInterface}, this)));
    }

    public void invalidateCache () {
        map.clear();
    }
}
