package info.elexis.jaxrs.service.internal.test;

import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MockElement {

	private String key;
	private String value;

	public MockElement() {
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MockElement other = (MockElement) obj;
		return Objects.equals(key, other.key) && Objects.equals(value, other.value);
	}

}
