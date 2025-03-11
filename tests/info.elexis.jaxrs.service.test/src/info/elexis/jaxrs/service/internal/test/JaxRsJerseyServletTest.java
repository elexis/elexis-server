package info.elexis.jaxrs.service.internal.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.WebTarget;

public class JaxRsJerseyServletTest {

	private static JWTRequestFilter jwtRequestFilter = new JWTRequestFilter();

	private static IModelService coreModelService;
	private static Client client;
	private static WebTarget target;
	private static MockResource resource;

	@BeforeClass
	public static void beforeClass() {
		client = ClientBuilder.newBuilder().register(jwtRequestFilter).build();
		target = client.target("http://localhost:8380/services");
		resource = WebResourceFactory.newResource(MockResource.class, target);
		coreModelService = OsgiServiceUtil
				.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
	}

	@Before
	public void before() {
		jwtRequestFilter.setJwt(null);
	}

	@Test(expected = NotAuthorizedException.class)
	public void testInvalidJwt() {
		resource.getUser();
	}

	@Test
	public void testValidJwtValidLocalExistingUser() {
		IContact contact = new IContactBuilder.PersonBuilder(coreModelService, "Test", "User", Gender.FEMALE)
				.buildAndSave();
		IUser user = new IUserBuilder(coreModelService, "test-user", contact).buildAndSave();
		jwtRequestFilter.setJwt(generateJWT("test-user", null, "user"));
		assertEquals("test-user", resource.getUser());
		assertEquals("user", resource.getRoles());

		coreModelService.remove(user);
		coreModelService.remove(contact);
	}

	@Test(expected = NotAuthorizedException.class)
	public void testValidJwtNonLocalExistingUser_NoPerformDynamicUserCreation() {
		jwtRequestFilter.setJwt(generateJWT("test-user", null, "user"));
		resource.getUser();
	}

	@Test
	public void testValidJwtValidNonLocalExistingUser_PerformDynamicUserCreation() {
		IContact contact = new IContactBuilder.PersonBuilder(coreModelService, "Test", "User", Gender.FEMALE)
				.buildAndSave();
		IUser user = new IUserBuilder(coreModelService, "test-user-mpa", contact).buildAndSave();
		jwtRequestFilter.setJwt(generateJWT("test-user-mpa", contact.getId(), "user", "mpa"));
		assertEquals("test-user-mpa", resource.getUser());
		assertEquals("user, mpa", resource.getRoles());

		coreModelService.remove(user);
		coreModelService.remove(contact);
	}

	public static class JWTRequestFilter implements ClientRequestFilter {

		private volatile String jwt;

		public void setJwt(String jwt) {
			this.jwt = jwt;
		}

		@Override
		public void filter(ClientRequestContext requestContext) throws IOException {
			if (jwt != null) {
				System.out.println("Authenticated request");
				requestContext.getHeaders().add("Authorization", "Bearer " + jwt);
			}

		}
	}

	public String generateJWT(String username, String elexisContactid, String... roles) {
		try {
			JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
					.keyID("Z2PioNPd-xh_CLRAaJcfrgYn-jE8UYANa0wV95GlWOc").build();
			RSAKey rsaJWK = RSAKey.parse(PublicPrivateKeypair.PUBLIC_PRIVATE_KEYPAIR);
			// issuer and audience must fit
			Map<String, Object> realmAccess = new HashMap<String, Object>();
			realmAccess.put("roles", roles);
			Builder builder = new JWTClaimsSet.Builder();
			builder.issuer("test-jwt-issuer");
			builder.audience("test-jwt-audience");
			builder.jwtID(UUID.randomUUID().toString());
			builder.claim("preferred_username", username);
			builder.claim("email", "test@tester.ch");
			builder.subject(username);
			builder.claim("realm_access", realmAccess);
			builder.expirationTime(new Date(new Date().getTime() + 5 * 1000));
			if (elexisContactid != null) {
				builder.claim("elexisContactId", elexisContactid);
			}
			JWTClaimsSet claimsSet = builder.build();
			SignedJWT signedJWT = new SignedJWT(header, claimsSet);
			JWSSigner signer = new RSASSASigner(rsaJWK);
			signedJWT.sign(signer);
			String jwt = signedJWT.serialize();
			return jwt;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
