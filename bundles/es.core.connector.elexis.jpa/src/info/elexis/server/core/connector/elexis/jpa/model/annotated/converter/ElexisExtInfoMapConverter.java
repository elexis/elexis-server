/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts the Elexis extInfo byte array into the contained Map<Object, Object>. 
 * The method conforms to the getInfoStore.
 *
 * This class requires, for compatibility reasons, the following (enclosed) classes<br> <br>
 * {@link ch.elexis.data.Kontakt}
 * 
 * @author M. Descher, MEDEVIT, Austria
 */
public class ElexisExtInfoMapConverter implements Converter {
	
	private Logger log = LoggerFactory.getLogger(ElexisExtInfoMapConverter.class);
	
	private static final long serialVersionUID = -1939779538183350309L;

	@Override
	public void initialize(DatabaseMapping mapping, Session session){}
	
	@SuppressWarnings("unchecked")
	@Override
	public byte[] convertObjectValueToDataValue(Object objectValue, Session session){
		if(objectValue==null) {
			return null;
		}
		if (objectValue instanceof Hashtable) {
			Hashtable<Object, Object> ov = (Hashtable<Object, Object>) objectValue;
			return flatten(ov);
		}
		log.error("Could not convert into byte array "+objectValue);
		return null;
	}
	
	@Override
	public Map<Object, Object> convertDataValueToObjectValue(Object dataValue, Session session){
		if (dataValue == null) {
			return new Hashtable<Object, Object>();
		}
		Hashtable<Object, Object> ret = fold((byte[]) dataValue);
		if (ret == null) {
			return new Hashtable<Object, Object>();
		}
		return ret;
	}
	
	@Override
	public boolean isMutable(){
		return false;
	}
	
	/**
	 * Convert a Hashtable into a compressed byte array. Note: the resulting array is java-specific,
	 * but stable through jre Versions (serialVersionUID: 1421746759512286392L)
	 * 
	 * @param hash
	 *            the hashtable to store
	 * @return
	 */
	private static byte[] flatten(final Hashtable<Object, Object> hash){
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(hash.size() * 30);
			ZipOutputStream zos = new ZipOutputStream(baos);
			zos.putNextEntry(new ZipEntry("hash"));
			ObjectOutputStream oos = new ObjectOutputStream(zos);
			oos.writeObject(hash);
			zos.close();
			baos.close();
			return baos.toByteArray();
		} catch (Exception ex) {
			// TODO
			System.out.println("flattenException: "+ex);
			return null;
		}
	}
	
	/**
	 * Recreate a Hashtable from a byte array as created by flatten()
	 * 
	 * @param flat
	 *            the byte array
	 * @return the original Hashtable or null if no Hashtable could be created from the array
	 */
	@SuppressWarnings("unchecked")
	private static Hashtable<Object, Object> fold(final byte[] flat){
		Hashtable<Object, Object> res = null;
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(flat);
				ZipInputStream zis = new ZipInputStream(bais);
				zis.getNextEntry();
				ObjectInputStream ois = new ObjectInputStream(zis);
				res = (Hashtable<Object, Object>) ois.readObject();
				ois.close();
				bais.close();
			} catch (IOException e) {
				// TODO
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO
				e.printStackTrace();
			}
			return res;
	}
	
}
