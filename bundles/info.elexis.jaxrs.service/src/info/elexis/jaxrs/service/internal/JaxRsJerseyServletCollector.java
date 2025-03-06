package info.elexis.jaxrs.service.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import ch.elexis.core.jaxrs.JaxrsResource;

@Component(service = {}, immediate = true)
public class JaxRsJerseyServletCollector {

	private static Set<JaxrsResource> jaxrsServletSet = Collections.synchronizedSet(new HashSet<>());

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY)
	public synchronized void bind(JaxrsResource jaxRsServlet) {
		jaxrsServletSet.add(jaxRsServlet);
	}

	public synchronized void unbind(JaxrsResource jaxRsServlet) {
		jaxrsServletSet.remove(jaxRsServlet);
	}

	public static Set<JaxrsResource> getJaxrsServletSet() {
		return jaxrsServletSet;
	}

}
