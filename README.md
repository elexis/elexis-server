# Elexis-Server [![License](http://img.shields.io/badge/license-EPL-blue.svg)](http://www.eclipse.org/legal/epl-v10.html)
Elexis for server operation

The Elexis server implements a part of the FHIR specification. Please consult [HL7-FHIR wik](http://wiki.hl7.org/index.php?title=FHIR) for further details.

## Build

Requires maven 3.3 and java 8. Build can be started by executing `mvn -f releng/es.parent/pom.xml clean verify -DskipTests` 

## Configuration, startup and checking the elexis server

This id documented in the [es.core.product.runtime](products/es.core.product.runtime/Readme.md).