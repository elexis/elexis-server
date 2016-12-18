# Elexis-Server [![License](http://img.shields.io/badge/license-EPL-blue.svg)](http://www.eclipse.org/legal/epl-v10.html)
Elexis for server operation

The Elexis server implements a part of the FHIR specification. Please consult [HL7-FHIR wik](http://wiki.hl7.org/index.php?title=FHIR) for further details.

## Build

Requires maven 3.3 and java 8. Build can be started by executing `mvn -f releng/es.parent/pom.xml clean verify -DskipTests` 

## Configuration, startup and checking the elexis server

This id documented in the [es.core.product.runtime](products/es.core.product.runtime/Readme.md).


## Running elexis-server inside a docker

In order to allow a fast setup and having a common playground for interested developer you find in the tests directory

* Dockerfile: builds a Debian (testing) based container with mysql-client, maven, SUN-JDK and a checkout of the elexis-server
** As of 2016.12.18 it was impossible to compile the elexis-server inside, therefore
** the last successfull build of the Jenkins build is downloaded and unzipped in the /app directory
** some files are copied to ensure
*** /etc/mysql/conf.d/mysql_elexis.cnf forces the mysql-server to use lowercase tablenames and UTF-8
*** /app/ contains script to start populate the test database and start the elexis-server
*** /home/es/elexis-server/shiro.ini (defined some roles)
*** /home/es/elexis-server/elexis-connection.xml (defines the connection to the mysql database)
* docker-compose.yaml defines the mysql server for the elexis-server (environment variables, network, volumes)

You need docker-engine (tested with 1.12.3) and docker-compose (tested 1.9.0) installed. Earlier version may work.

To start the following snippet should work

```bash
cd tests
docker-compose build
docker-compose up
```

After some minutes of downloading you should see the line `Created test database. Will start elexis-server now`.
And soon afterward visiting in your browser the url http://localhost:8380/services should give an answer like
`Uptime: 0 days, 0 hours, 0 min, 11 sec`

TODO: What must be changed to display some meaningful text for http://localhost:8380/fhir/Patient?name=Vorname?

