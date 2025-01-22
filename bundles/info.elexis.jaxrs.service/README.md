## Errors

Jetty 12 with EE 10, Jersey 3.1

Unclear HTTP 500 problem

might be swallowed by something like this

com.sun.xml.bind.v2.runtime.IllegalAnnotationsException: 1 counts of IllegalAnnotationExceptions
info.elexis.jaxrs.service.internal.test.MockElement enthält keinen "no-arg"-Standardkonstruktor.
	this problem is related to the following location:
		at info.elexis.jaxrs.service.internal.test.MockElement
		
`org.eclipse.osgi.services` is to be removed, only ee10

Refactor
		
		
## Installation

This Jetty / JaxRs installation works using this settings

<table>
  <tr>
    <th>Library</th>
    <th>Start-Level</th>
    <th>Auto-Start</th>
  </tr>
  <tr>
    <td>org.eclipse.jetty.ee10.annotations</td>
    <td>3</td>
    <td>true</td>
  </tr>
  <tr>
    <td>org.eclipse.jetty.ee10.osgi.boot</td>
    <td>2</td>
    <td>true</td>
  </tr>
  <tr>
    <td>org.eclipse.jetty.osgi</td>
    <td>2</td>
    <td>true</td>
  </tr>
  <tr>
    <td>??? org.glassfish.jersey.containers.jersey-container-servlet</td>
    <td>2</td>
    <td>true</td>
  </tr>
  <tr>
    <td>org.eclipse.equinox.comon</td>
    <td>2</td>
    <td>true</td>
  </tr>
  <tr>
    <td>org.apache.felix.scr</td>
    <td>1</td>
    <td>true</td>
  </tr>
  <tr>
    <td>org.apache.aries.spifly.dynamic.bundle</td>
    <td>1</td>
    <td>true</td>
  </tr>
  <tr>
    <td>ch.qos.logback.classic</td>
    <td>1</td>
    <td>true</td>
  </tr>
</table>

in order to enable a bundle to contribute, `META-INF/MANIFEST.MF` must contain the entries
`Jetty-Environment: ee10` and `Web-ContextPath: /`.

#### Jetty Paramaters

```
-Dorg.osgi.service.http.port=8380
-Djetty.http.port=8380
-Djetty.home.bundle=org.eclipse.jetty.ee10.osgi.boot
-Dorg.eclipse.jetty.LEVEL=DEBUG
```

for more log output look for `logback.xml` and set `<logger name="org.eclipse.jetty.ee10" level="DEBUG" />`