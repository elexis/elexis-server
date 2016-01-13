package info.elexis.server.core.security;

import java.security.Principal;

class PrincipalImpl implements Principal {

	public static final PrincipalImpl ADMIN = new PrincipalImpl("admin");

	private String sessionId;

	public PrincipalImpl(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public String getName() {
		return sessionId;
	}
}
