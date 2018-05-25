package info.elexis.server.core.jetty.osgi.boot.fragment;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

// to prevent java.lang.ClassNotFoundException:
// info.elexis.server.core.jetty.osgi.boot.fragment.FragmentActivator cannot be
// found by org.eclipse.jetty.osgi.boot
public class FragmentActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {}

	@Override
	public void stop(BundleContext context) throws Exception {}

}
