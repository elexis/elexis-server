
# Elexis-Server

The Elexis-Server is a headless variant of the Elexis desktop application. It is an entire rewrite, based on Equinox and an alternative persistence implementation based on EclipseLink.

Current snapshot binaries are available at [download](http://download.elexis.info/elexis-server/products/).

## Configuration

While Elexis-Server is can be operated on both Windows, MacOS and Linux, development is heavily focused on usage within the Linux operating system. We additionally focus on operation using Debian 8.

### Required files

For correct configuration the following files are required to be located in `${user.home}/elexis-server`. The directory `elexis-server` will be created on start if not available.

*   `shiro.ini`
*   `elexis-connection.xml` 

## Operation

In order to start up elexis-server the enclosed shell script `linux-start.sh` should be used. This creates a local telnet socket on port 7234 bound to the OSGI console.

On Windows use the provided script `win-start.bat`. To install telnet client to access the OSGi console see [Microsoft help](https://technet.microsoft.com/en-us/library/cc771275.aspx).

 

## Console commands

Elexis-server bundles partially provide console commands to be executed:


* Core `es (status | launch | system)` 
* P2 `es_p2  (system | repositories | features)` 

Example installing the FHIR rest feature:

* `es_p2 executeUpdate'` update with the newest versions from the p2 site
* `es_p2 features install info.elexis.server.fhir.rest.core.feature.feature.group` install the feature
* `es system halt` halt the running elexis server, restart using a start script