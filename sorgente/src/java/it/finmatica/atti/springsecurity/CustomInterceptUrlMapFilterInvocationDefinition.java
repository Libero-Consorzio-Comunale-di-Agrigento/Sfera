package it.finmatica.atti.springsecurity;

import grails.plugin.springsecurity.web.access.intercept.InterceptUrlMapFilterInvocationDefinition;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.util.UrlUtils;

public class CustomInterceptUrlMapFilterInvocationDefinition extends InterceptUrlMapFilterInvocationDefinition {

	@Override
	protected String determineUrl(FilterInvocation filterInvocation) {
		String url = UrlUtils.buildRequestUrl(filterInvocation.getHttpRequest()).toLowerCase();
		return url;
	}

}
