
# Elexis-Server

The Elexis-Server is a headless variant of the Elexis desktop application. It is an entire rewrite, based on Equinox and an alternative persistence implementation based on EclipseLink.

## Configuration

While Elexis-Server is can be operated on both Windows, MacOS and Linux, development is heavily focused on usage within the Linux operating system. We additionally focus on operation using Debian 8.

### Required files

For correct configuration the following files are required to be located in `${user.home}/elexis-server`. The directory `elexis-server` will be created on start if not available.

*   `shiro.ini`
*   `elexis-connection.xml` 

## Operation

In order to start up elexis-server the enclosed shell script `linux-start.sh` should be used. This creates a local telnet socket on port 7234 bound to the OSGI console.

## Console commands

Elexis-server bundles partially provide console commands to be executed:


* Core `es (status | launch | system)` 
* P2 `es_p2  (system | repositories)` 