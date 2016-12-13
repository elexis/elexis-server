
# Elexis-Server

The Elexis-Server is a headless variant of the Elexis desktop application. It is an entire rewrite, based on Equinox and an alternative persistence implementation based on EclipseLink.

Current snapshot binaries are available at [download](http://download.elexis.info/elexis-server/products/master).

## Configuration

While Elexis-Server is can be operated on both Windows, MacOS and Linux, development is heavily focused on usage within the Linux operating system. We additionally focus on operation using Debian 8.

### Required files

For correct configuration the following files are required to be located in `${user.home}/elexis-server`. The directory `elexis-server` will be created on start if not available.

*   `shiro.ini`
*   `elexis-connection.xml` 

You may just copy the bundles/es.core/shiro.ini and bundles/es.core.connector.elexis/rsc/devel/elexis-connection.xml to ~/elexis-server and modify them there.

## Operation

In order to start up elexis-server the enclosed shell script `linux-start.sh` should be used. This creates a local telnet socket on port 7234 bound to the OSGI console.

On Windows use the provided script `win-start.bat`. To install telnet client to access the OSGi console see [Microsoft help](https://technet.microsoft.com/en-us/library/cc771275.aspx).

Be aware that the used Elexis database must contain a row with the param "locale" and a value corresponding to your system locale in the table config. Or you will see in the ~/elexis-server/logs/elexis-server.ERROR.log
a line like

`20:26:59.350 ERROR i.e.s.c.c.e.i.ElexisEntityManager - System locale [de_CH] does not match required database locale [null]`

After you should be able to see the uptime of the elexis server, if you visit with your browser http://localhost:8380/services. It should report something like `Uptime: 0 days, 0 hours, 0 min, 19 sec`

* TODO: Why does http://localhost:8380/services/elexis/connection not work:
* TDOO: Document how a simple fhir should work

## Console commands

Elexis-server bundles partially provide console commands to be executed:


* Core `es (status | launch | system)` 
* P2 `es_p2  (system | repositories | features)` 

Example installing the FHIR REST feature:

* `es_p2 executeUpdate` update with the newest versions from the p2 site
* `es_p2 features install info.elexis.server.fhir.rest.core.feature.feature.group` install the feature
* `es system halt` halt the running elexis server, restart using a start script
