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
* As of 2016.12.18 it was impossible to compile the elexis-server inside, therefore
* the last successfull build of the Jenkins build is downloaded and unzipped in the /app directory
* some files are copied to ensure
** /etc/mysql/conf.d/mysql_elexis.cnf forces the mysql-server to use lowercase tablenames and UTF-8
* /app/ contains script to start populate the test database and start the elexis-server
* /home/es/elexis-server/shiro.ini (defined some roles)
* /home/es/elexis-server/elexis-connection.xml (defines the connection to the mysql database)
* docker-compose.yaml defines the mysql server for the elexis-server (environment variables, network, volumes)
* On the first start of the container the test database is initialized and the FHIR-REST services are started

You need docker-engine (tested with 1.12.3) and docker-compose (tested 1.9.0) installed. Earlier version may work.

To start the following snippet should work

    bash
    cd tests
    docker-compose build
    docker-compose up

After some minutes of downloading you should see the line `Created test database. Will start elexis-server now`.
And soon afterward visiting in your browser the url http://localhost:8380/services should give an answer like

    Uptime: 0 days, 0 hours, 0 min, 11 sec

It will take a minute or so to build the test database using the various SQL-scripts used also elsewhere for the unit tests.

Everything went okay if the command `docker-compose logs --no-color  | tail` returns

    es_1        | /home/es/marker created
    es_1        | ls -l /home/es/marker
    es_1        | Will start elexis-server now
    es_1        | /app/start_elexis_server_test: line 30: /home/es/elexis-server//logs/console.log: Permission denied
    es_1        | Going into eternal loop to help you find errors.
    es_1        | 18:10:23.868 INFO  o.e.persistence.logging.connection - 2016-12-20 18:10:23.868--ServerSession(1304343947)--Connection(1337540158)--Thread(Thread[Framework stop,5,main])--disconnect
    es_1        | 18:10:23.868 INFO  o.e.persistence.logging.connection - 2016-12-20 18:10:23.868--ServerSession(1304343947)--Thread(Thread[Framework stop,5,main])--/elexis_nonJtaDataSource=2059052504_url=jdbc:mysql://mysql_es:3306/elexis_server_test_user=es logout successful
    es_1        | 18:10:23.869 INFO  o.e.persistence.logging.connection - 2016-12-20 18:10:23.869--ServerSession(1304343947)--Connection(537135738)--Thread(Thread[Framework stop,5,main])--disconnect
    es_1        | Elexis-server:
    es_1        | GTK+ Version Check

Afterwards point your browser to http://localhost:8380/fhir/Patient?name=TestPatient and you should see the details of two test patients.
