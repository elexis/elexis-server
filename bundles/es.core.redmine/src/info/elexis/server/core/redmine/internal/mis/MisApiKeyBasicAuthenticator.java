package info.elexis.server.core.redmine.internal.mis;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

class MisApiKeyBasicAuthenticator extends Authenticator {

	private String username;
	private char[] password;

	public MisApiKeyBasicAuthenticator(String username, char[] password){
		this.username = username;
		this.password = password;
	}
	
	@Override
	protected PasswordAuthentication getPasswordAuthentication(){
		if(getRequestingHost().endsWith("medelexis.ch")) {
			return new PasswordAuthentication(username, password);
		}
		return super.getPasswordAuthentication();
	}
	
}
