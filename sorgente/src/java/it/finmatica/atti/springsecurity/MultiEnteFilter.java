package it.finmatica.atti.springsecurity;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Sovrascrive il MultiEnteFilter in modo tale da escludere dalla richiesta dell'ente i path dei webservice.
 * 
 * @author esasdelli
 *
 */
public class MultiEnteFilter extends it.finmatica.so4.filters.MultiEnteFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		AntPathRequestMatcher matcher = new AntPathRequestMatcher("/services/**");
		if (matcher.matches(request)) {
			filterChain.doFilter(request, response);
		} else {
			// eseguo il normale filtro multiEnte
			super.doFilterInternal(request, response, filterChain);
		}
	}
}
