# Elexis-Server [![License](http://img.shields.io/badge/license-EPL-blue.svg)](http://www.eclipse.org/legal/epl-v10.html)
Elexis for server operation

The Elexis server implements a part of the FHIR specification. Please consult [HL7-FHIR wik](http://wiki.hl7.org/index.php?title=FHIR) for further details.

## Build

Requires maven 3.3 and java 8. Build can be started by executing `mvn -f releng/es.parent/pom.xml clean verify -DskipTests` 

## Configuration, startup and checking the elexis server

This id documented in the [es.core.product.runtime](products/es.core.product.runtime/Readme.md).

## Running inside docker

Use the following command to start an elexis-server docker image, omit or replace `:tag` with the required elexis-server executable branch to use. At the moment valid values are `[latest, 1.3]`.

```bash
docker run -p 8380:8380 -p 7234:7234 medevit/elexis-server:tag
```

After initially creating a container out of this image (which is what the `run` command does) you should make a note
off the ID of the created instance, in order to restart it again afterwards. Repeatedly executing this command, always leaves
you with a newly generated container.

After startup point your browser to http://localhost:8380/fhir/Patient?name=TestPatient and you should see the details of two test patients.
