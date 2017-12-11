
# Configuration files

elexis-server.properties

# Security

## Realms

A realm is a domain of authentication for the Elexis-Server. There currently exist two realms,
the system realm, backed by Elexis-Server itself, and the Elexis Connector Realm, backed
by an Elexis installation.



### Elexis Server System Realm


### Elexis Connector Realm


# Interfaces

## OSGI Console

## REST Interface

### OAuth Authentication

Currently supports two OAuth grant types

*   Client credentials (considers roles mentioned in client file)
*   Resource Owner credentials (considers roles of user)
Both methods required a registered client application to connect. Registered clients are configured
in the file `elexis-server.oauth2.auth`.