package info.elexis.server.core.util;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;

public class NetworkUtil {

	/**
	 * Find the IPv4 address applicable to a given interface name
	 * 
	 * @param name
	 *            the name of the interface
	 * @return the IPv4 ({@link Inet4Address}) address defined for the interface
	 *         or <code>null</code> if not found
	 * @throws SocketException
	 */
	public static String getIPByInterfaceName(String name) throws SocketException {
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		Optional<NetworkInterface> device = Collections.list(nets).stream()
				.filter(i -> i.getName().equalsIgnoreCase(name)).findFirst();
		if (!device.isPresent())
			return null;
		return Collections.list(device.get().getInetAddresses()).stream().filter(v -> v instanceof Inet4Address)
				.map(v -> v.getHostAddress()).findFirst().get();
	}

}
