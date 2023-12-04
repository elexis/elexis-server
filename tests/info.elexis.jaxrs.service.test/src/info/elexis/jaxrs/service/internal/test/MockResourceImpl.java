package info.elexis.jaxrs.service.internal.test;

import org.osgi.service.component.annotations.Component;

import info.elexis.jaxrs.service.JaxrsResource;

@Component
public class MockResourceImpl implements MockResource, JaxrsResource {

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

}
