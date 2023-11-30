
# DEPRECATED

Marked deprecated with 3.12, was bound to `org.eclipse.jetty.osgi.boot`

This required service should also be addable using `org.eclipse.jetty.osgi.httpservice` which
is added as bundle to startlevel.

If no additional usage, remove in 3.13

Removed from MAVEN build


In essence, while org.eclipse.jetty.osgi.boot is about integrating Jetty as a server into OSGi, org.eclipse.jetty.osgi.httpservice is about providing a standard OSGi HTTP service using Jetty. The httpservice bundle allows other OSGi bundles to easily expose web content and services, leveraging Jetty's capabilities under the hood.