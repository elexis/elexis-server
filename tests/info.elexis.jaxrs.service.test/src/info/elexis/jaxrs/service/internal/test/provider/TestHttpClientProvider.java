package info.elexis.jaxrs.service.internal.test.provider;

import java.io.IOException;
import java.util.Map;

import io.curity.oauth.HttpClientProvider;
import io.curity.oauth.IntrospectionClient;
import io.curity.oauth.WebKeysClient;
import jakarta.servlet.UnavailableException;

/**
 * Enabled via io.curity.oauth.filter.testcert fragment, included here due to
 * class loader issues
 */
public class TestHttpClientProvider extends HttpClientProvider {

	public TestHttpClientProvider() {
		System.out.println("TestHttpClientProvider created");
	}

	@Override
	public IntrospectionClient createIntrospectionClient(Map<String, ?> arg0) throws UnavailableException {
		throw new UnavailableException("Not implemented");
	}

	@Override
	public WebKeysClient createWebKeysClient(Map<String, ?> arg0) throws UnavailableException {
		return new WebKeysClient() {

			@Override
			public String getKeys() throws IOException {
				String publicKey = """
						{"keys": [{
						    "kty": "RSA",
						    "e": "AQAB",
						    "use": "sig",
						    "kid": "Z2PioNPd-xh_CLRAaJcfrgYn-jE8UYANa0wV95GlWOc",
						    "alg": "RS256",
						    "n": "jKkVzuyqkhExEaKh0A4WwH58iXAbrwuZYFyPo3aAm_LHV21hO2vtAWmkqXwvhyUJPnyPcHddHBDeO158Rpl69T-HR4q2hv43bfyX5iq4HDa5Z5ZVNaqKDLjuKpUVe29QoLvvthNwfb3tNIECYPePjEPXaqyFhnWsK9-Uuqwm2xM5CReShiF-2nxDT1x5IUfIDhIkcy4t8HxnjrIuzh37yKfJ1eAX7q_2SRBHzt53r-eXb8N1adzLkI8-W2FsSbUnMuTZTXEokkxKsEgQBSeWk-d-lQB-tI1IPRN_6rJ44_MLUO0BsyrrTOhIvSvAcKKwVZqymPrdrJplhd033X3Q_w"
						}]}""";
				return publicKey;
			}
		};
	}

}
