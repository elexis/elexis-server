package io.curity.oauth;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.UnavailableException;

public class TestHttpClientProvider extends HttpClientProvider {

	@Override
	public IntrospectionClient createIntrospectionClient(Map<String, ?> arg0) throws UnavailableException {
		throw new UnavailableException("Not implemented");
	}

	@Override
	public WebKeysClient createWebKeysClient(Map<String, ?> arg0) throws UnavailableException {
		return new WebKeysClient() {

			@Override
			public String getKeys() throws IOException {
				System.out.println("Johnny the fish");
				return null;
			}
		};
	}

}
