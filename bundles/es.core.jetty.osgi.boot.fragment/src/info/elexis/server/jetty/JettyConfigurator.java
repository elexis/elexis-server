package info.elexis.server.jetty;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.common.util.CoreUtil;

public class JettyConfigurator {

	private static Logger log = LoggerFactory.getLogger(JettyConfigurator.class);

	private static final String KEYSTORE_PASSWORD = "OBF:1vn61uh21wfi1zet1tvn1ym716yn1ym71tvf1zeh1wgg1ugo1vnw";

	public static void configureSslContextFactory(String jettyBase, SslContextFactory sslContextFactory) {

		File elexisServerKeyStore = CoreUtil.getHomeDirectory().resolve("elexis-server.p12").toFile();
		if (!elexisServerKeyStore.exists()) {
			try {
				File defaultKeystore = new File(new URL(jettyBase + "/etc/elexis-server.p12").getFile());
				log.info("Copying default keystore file from [{}] to [{}]", defaultKeystore.getAbsolutePath(),
						elexisServerKeyStore.getAbsolutePath());

				Files.copy(defaultKeystore.toPath(), elexisServerKeyStore.toPath());
			} catch (IOException e) {
				log.error("Error using default keystore file", e);
			}
		}

		sslContextFactory.setKeyStorePath(elexisServerKeyStore.getAbsolutePath());
		sslContextFactory.setTrustStorePath(elexisServerKeyStore.getAbsolutePath());
		sslContextFactory.setKeyStorePassword(KEYSTORE_PASSWORD);
		sslContextFactory.setKeyManagerPassword(KEYSTORE_PASSWORD);
		sslContextFactory.setKeyStoreType("PKCS12");
		sslContextFactory.setTrustStoreType("PKCS12");

		// watch changes to the keystore and reload
		// https://danielflower.github.io/2017/04/08/Lets-Encrypt-Certs-with-embedded-Jetty.html
		Path keystorePath = Paths.get(URI.create(sslContextFactory.getKeyStorePath()));
		try {
			FileWatcher.onFileChange(keystorePath,
					() -> sslContextFactory.reload(scf -> log.info("Reloaded SSL cert")));
		} catch (IOException e) {
			log.error("Error initializing File Watcher on [{}]", keystorePath);
		}
	}

}
