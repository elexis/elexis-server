/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package info.elexis.server.core.p2.internal.security;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.core.internal.preferences.Base64;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.equinox.security.storage.provider.IPreferencesContainer;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.common.util.CoreUtil;
import info.elexis.server.core.p2.Constants;

@SuppressWarnings("restriction")
public class SecureStoragePasswordProvider extends org.eclipse.equinox.security.storage.provider.PasswordProvider {
	private static Logger log = LoggerFactory.getLogger(SecureStoragePasswordProvider.class);

	private static final String ALGORITHM = "AES/ECB/PKCS5Padding"; //$NON-NLS-1$
	private static final String ENCODING = "UTF-8"; //$NON-NLS-1$
	private static final int BYTE_ARRAY_SIZE = 1024;

	@Override
	public PBEKeySpec getPassword(IPreferencesContainer container, int passwordType) {
		if (CoreUtil.getHomeDirectory().toString() == null)
			return null;

		final boolean newPassword = ((passwordType & CREATE_NEW_PASSWORD) != 0);
		final boolean passwordChange = ((passwordType & PASSWORD_CHANGE) != 0);

		try {
			if (!newPassword && !passwordChange) {
				char[] existing = getPassword();
				if (existing != null && existing.length != 0)
					return new PBEKeySpec(existing);
			}

			String password = UUID.randomUUID().toString();
			if (password == null || password.trim().length() == 0)
				return null;
			writePassword(password);
			return new PBEKeySpec(password.toCharArray());
		} catch (IOException e) {
			log.error("", e);
		}
		return null;
	}

	private void writePassword(String password) throws IOException {
		SecretKeySpec key = getKeySpec();
		byte[] encrypted = encrypt(key, password);
		if (encrypted != null && encrypted.length > 0) {
			byte[] b64 = Base64.encode(encrypted);
			write(b64, new FileOutputStream(getPasswordFile()));
		}
	}

	private char[] getPassword() throws IOException {
		byte[] encrypted = getEncryptedPassword();
		if (encrypted == null)
			return new char[0];
		byte[] bytes = Base64.decode(encrypted);

		if (bytes != null) {
			SecretKeySpec key = getKeySpec();
			byte[] decrypted = decrypt(key, bytes);
			if (decrypted == null || decrypted.length == 0)
				return new char[0];
			return new String(decrypted, ENCODING).toCharArray();
		}
		return new char[0];
	}

	private byte[] getEncryptedPassword() throws IOException {
		File file = getPasswordFile();
		if (!file.exists())
			return null;
		return read(new FileInputStream(file), ENCODING);
	}

	private File getPasswordFile() throws IOException {
		File file = new File(CoreUtil.getHomeDirectory() + File.separator + ".elexis-server-secure", ".store"); //$NON-NLS-1$ //$NON-NLS-2$
		file.getParentFile().mkdirs();
		return file;
	}

	private SecretKeySpec getKeySpec() {
		String ksPref = Platform.getPreferencesService().getString(Constants.PLUGIN_ID, IPreferenceConstants.CACHED_KEY, "", //$NON-NLS-1$
				null);
		byte[] key = null;

		if (!"".equals(ksPref)) //$NON-NLS-1$
		{
			try {
				byte[] bytes = Base64.decode(ksPref.getBytes(ENCODING));
				if (bytes != null) {
					key = bytes;
				}
			} catch (Exception e) {
				log.error(Messages.PasswordProvider_ERR_UnableToDecodeExistingKey, e);
			}
		}

		KeyGenerator kgen;
		if (key == null || key.length == 0) {
			try {
				kgen = KeyGenerator.getInstance("AES"); //$NON-NLS-1$
				kgen.init(128);

				SecretKey skey = kgen.generateKey();
				key = skey.getEncoded();
				byte[] b64 = Base64.encode(skey.getEncoded());
				IEclipsePreferences node = InstanceScope.INSTANCE.getNode(Constants.PLUGIN_ID);
				node.put(IPreferenceConstants.CACHED_KEY, new String(b64));
				node.flush();
			} catch (NoSuchAlgorithmException e) {
				log.error(Messages.PasswordProvider_ERR_NoSuchAlgorithm, e);
				return null;
			} catch (BackingStoreException e) {
				log.error(Messages.PasswordProvider_ERR_UnableToStoreKey, e);
				return null;
			}
		}
		return new SecretKeySpec(key, "AES"); //$NON-NLS-1$
	}

	private byte[] encrypt(SecretKeySpec skeySpec, String password) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			return cipher.doFinal(password.getBytes(ENCODING));
		} catch (NoSuchAlgorithmException e) {
			log.error(Messages.PasswordProvider_ERR_NoSuchAlgorithm, e);
		} catch (NoSuchPaddingException e) {
			log.error(Messages.PasswordProvider_ERR_NoSuchPadding, e);
		} catch (InvalidKeyException e) {
			log.error(Messages.PasswordProvider_ERR_InvalidKey, e);
		} catch (IllegalBlockSizeException e) {
			log.error(Messages.PasswordProvider_ERR_IllegalBlockSize, e);
		} catch (BadPaddingException e) {
			log.warn("", Messages.PasswordProvider_ERR_BadPadding, e);
		} catch (UnsupportedEncodingException e) {
			log.error(Messages.PasswordProvider_ERR_UnsupportedEncoding, e);
		}
		return null;
	}

	private byte[] decrypt(SecretKeySpec skeySpec, byte[] encryptedPassword) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			return cipher.doFinal(encryptedPassword);
		} catch (NoSuchAlgorithmException e) {
			log.error(Messages.PasswordProvider_ERR_NoSuchAlgorithm, e);
		} catch (NoSuchPaddingException e) {
			log.error(Messages.PasswordProvider_ERR_NoSuchPadding, e);
		} catch (InvalidKeyException e) {
			log.error(Messages.PasswordProvider_ERR_InvalidKey, e);
		} catch (IllegalBlockSizeException e) {
			log.error(Messages.PasswordProvider_ERR_IllegalBlockSize, e);
		} catch (BadPaddingException e) {
			log.warn("", Messages.PasswordProvider_ERR_BadPadding, e);
		}
		return null;
	}

	private static byte[] read(InputStream stream, String charset) {
		if (stream == null)
			return null;

		ByteArrayOutputStream out = new ByteArrayOutputStream(BYTE_ARRAY_SIZE);
		try {
			if (!(stream instanceof BufferedInputStream)) {
				stream = new BufferedInputStream(stream);
			}

			// byte[] theBytes = new byte[stream.available()];
			// stream.read(theBytes);

			// TODO Read whatever is available, not in 1024 chunks?
			byte[] buffer = new byte[BYTE_ARRAY_SIZE];
			int len;

			while ((len = stream.read(buffer)) >= 0)
				out.write(buffer, 0, len);
			return out.toByteArray();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// ignore
			}
			try {
				out.close();
			} catch (IOException e) {
				// ignore
			}
		}
		return null;
	}

	private static void write(byte[] bytes, OutputStream stream) {
		try {
			stream.write(bytes);
		} catch (IOException e) {
			log.error("", e);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
}
