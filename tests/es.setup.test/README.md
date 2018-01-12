
# Elexis-Server Setup Test

This test performs an initial setup and configuration of an Elexis-Server, by executing the following steps:

1.  Start an Elexis-server without database connection
2.  Set the database connection using the REST interface
3. 	Try to again set the database connection without authentication (Unauthorized)
4.  Verify access to FHIR Rest-Interface not possible (Unauthorized)
5.  Authenticate against the Elexis-Server with right `elexis-server.admin`
6.  Add an Oauth2 Client 