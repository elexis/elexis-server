package es.fhir.rest.core.servlets;

import ch.elexis.core.jaxrs.filter.AbstractCombinedOauthJwtContextSettingFilter;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;

@WebFilter(urlPatterns = { "/fhir/*" }, initParams = { @WebInitParam(name = "skipPattern", value = "/fhir/metadata") })
public class FhirServletCombinedFilter extends AbstractCombinedOauthJwtContextSettingFilter {

}
