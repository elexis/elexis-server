
# Configuration files

elexis-server.properties

# Security

## Realms

A realm is a domain of authentication for the Elexis-Server. There currently exist two realms,
the system realm, backed by Elexis-Server itself, and the Elexis Connector Realm, backed
by an Elexis installation.



### Elexis Server System Realm

The `elexis-server.auth` file contains users for direct system access, and OAuth2 clients.

If an OAuth2 client has a set of defined roles (may not be limited), a user connecting via this client may not
outreach the client rights - he is limited to a subset equal the rights of the client.

### Elexis Connector Realm


## OAuth Authentication



### OAuth H2 Database
