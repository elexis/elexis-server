package es.fhir.rest.core.servlets;

import ch.elexis.core.jaxrs.filter.AbstractCombinedOauthJwtContextSettingFilter;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;

@WebFilter(servletNames = { "fhir" }, urlPatterns = { "/*" }, initParams = {
		@WebInitParam(name = "filter-id", value = "fhir"),
		@WebInitParam(name = "skipPattern", value = "/metadata") })
public class FhirServletCombinedFilter extends AbstractCombinedOauthJwtContextSettingFilter {

}
