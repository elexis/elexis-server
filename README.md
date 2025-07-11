# Elexis-Server 
[![Build P2 Site](https://github.com/elexis/elexis-server/actions/workflows/build-p2-site.yaml/badge.svg)](https://github.com/elexis/elexis-server/actions/workflows/build-p2-site.yaml)
[![License](http://img.shields.io/badge/license-EPL-blue.svg)](http://www.eclipse.org/legal/epl-v10.html)

Elexis for server operation

## BREAKING CHANGES


### ES 3.10
* Removal of Shiro
* Security layer is now realized via Keycloak Filter

### ES 3.9
* Change of version handling. It is now merged with elexis-3-core
* DB Connection configuration via Environment Variables

### ES 1.8
* ES will not provide HTTPS services
* OpenID is not integrated in ES anymore
* Only product builds for linux x86_64 are made

If you want to operate ES standalone with authentication/authorization support, [Elexis-Environment](https://github.com/elexis/elexis-environment) is your solution.

## Build

Requires maven 3.8 and java 17. Build can be started by executing `mvn -f releng/es.parent/pom.xml clean verify -DskipTests -Delexis.branch=master`

## Configuration, startup and testing the elexis server

Documented in [es.core.product.runtime](products/es.core.product.runtime/Readme.md).

## Running via docker

You may either run this image using the enclosed `docker-compose.yml` file, or via a direct docker call. 
For an overview of the available versions (tags), see https://hub.docker.com/r/medevit/elexis-server/tags/
Replace `master` with the resp. tag if another version is required.
### Docker-Compose

To get a fresh and clean run of ES perform the following commands in the given order:

- `docker-compose rm -f` Remove an already existing container
- `docker volume rm elexis-server_elexis_home` Remove the volume holding the ES home-directory
- `docker-compose pull` Fetch the newest image of the server
- `docker-compose up` Run the server in foreground mode. Append `-d` to run in background.

### Direct docker call

```bash
docker run -e DEMO_MODE='true' -e TZ=Europe/Zurich -p 8380:8380  -p 7234:7234 medevit/elexis-server:master
```

After initially creating a container out of this image (which is what the `run` command does), note
the ID of the created instance, in order to restart it again afterwards. Repeatedly executing this command, always leaves
you with a newly generated container.

### Parameters to use

- `DISABLE_WEB_SECURITY=true` (DC) or `-e DISABLE_WEB_SECURITY='true'` - disable the web security layer
- `DEMO_MODE=true` (DC) or `-e DEMO_MODE='true'` - download the demo database (only if not already downloaded)
