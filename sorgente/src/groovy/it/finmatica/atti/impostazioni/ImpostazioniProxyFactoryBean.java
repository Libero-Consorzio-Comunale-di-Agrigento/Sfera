package it.finmatica.atti.impostazioni;

import com.sun.star.auth.InvalidArgumentException;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ImpostazioniProxyFactoryBean extends AbstractFactoryBean<Object> implements InvocationHandler, GrailsApplicationAware {

	private GrailsApplication grailsApplication;
	private Class<?> targetInterface;
	private String impostazione;

	public ImpostazioniProxyFactoryBean() {
		this.setSingleton(true);
	}

	@Override
	protected Object createInstance() throws Exception {
		return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{targetInterface}, this);
	}

	@Override
	public Class<?> getObjectType() {
		return targetInterface;
	}

	public void setTargetInterface(String targetInterface) throws InvalidArgumentException {
		try {
			this.targetInterface = this.getClass().getClassLoader().loadClass(targetInterface);
		} catch (ClassNotFoundException e) {
			throw new InvalidArgumentException("Non trovo la classe: "+targetInterface, e);
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object targetBean = getTargetBean();
		if (targetBean != null) {
			return method.invoke(targetBean, args);
		}

		// ritorno il default "false", 0, null
		Class<?> returnType = method.getReturnType();

		if (returnType == null) {
			return null;
		}

		if (returnType.isAssignableFrom(boolean.class)) {
			return false;
		}

		if (returnType.isAssignableFrom(int.class)) {
			return -1;
		}

		return null;
	}

	public Object getTargetBean() {
		String nomeBean = Impostazioni.valueOf(getImpostazione()).getValore();
		if ("N".equals(nomeBean)) {
			return null;
		}
		return grailsApplication.getMainContext().getBean(nomeBean);
	}

	@Override
	public void setGrailsApplication(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication;
	}

	public String getImpostazione() {
		return impostazione;
	}

	public void setImpostazione(String impostazione) {
		this.impostazione = impostazione;
	}
}
