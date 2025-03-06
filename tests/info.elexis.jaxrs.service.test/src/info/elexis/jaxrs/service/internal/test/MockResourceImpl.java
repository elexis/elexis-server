package info.elexis.jaxrs.service.internal.test;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.jaxrs.JaxrsResource;
import ch.elexis.core.model.IRole;
import ch.elexis.core.services.IContextService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/test")
@Component
public class MockResourceImpl implements MockResource, JaxrsResource {

	@Reference
	private IContextService contextService;

	@GET
	@Override
	public String getContent() {
		return "get";
	}

	@Override
	public String postContent(String content) {
		return "post" + content;
	}

	@Override
	public String putContent(String content) {
		return "put" + content;
	}

	@Override
	public String deleteContent() {
		return "delete";
	}

	@Override
	public MockElement getJson() {
		return getMockElement();
	}

	@Override
	public MockElement getXml() {
		return getMockElement();
	}

	public static MockElement getMockElement() {
		MockElement mockElement = new MockElement();
		mockElement.setKey("key");
		mockElement.setValue("value");
		return mockElement;
	}

	@Override
	public String getUser() {
		return contextService.getActiveUser().map(u -> u.getId()).orElse("no user");
	}

	@Override
	public String getRoles() {
		List<IRole> roles = contextService.getActiveUser().get().getRoles();
		return roles.stream().map(IRole::getId).reduce((a, b) -> a + ", " + b).orElse("no roles");
	}

}
