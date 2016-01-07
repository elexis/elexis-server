package info.elexis.server.core.internal;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name="configurationEntries")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigFile {

	public HashMap<String, String> values = new HashMap<String, String>();
	
	public HashMap<String, String> getValues() {
		return values;
	}
	
	public void setValues(HashMap<String, String> values) {
		this.values = values;
	}
	

	/**
	 * Marshall this object into a storable xml
	 * 
	 * @param os
	 * @throws JAXBException
	 */
	public void marshall(OutputStream os) throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance(ConfigFile.class);
		Marshaller m = jaxbContext.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(this, os);
	}
	
	/**
	 * Unmarshall a DBConnection object created by {@link #marshall()}
	 * 
	 * @param is
	 * @return
	 * @throws JAXBException
	 */
	public static ConfigFile unmarshall(InputStream is) throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance(ConfigFile.class);
		Unmarshaller um = jaxbContext.createUnmarshaller();
		Object o = um.unmarshal(is);
		return (ConfigFile) o;
	}
}
