# Copyright (c) 2016 by Niklaus Giger niklaus.giger@member.fsf.org
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http:#www.eclipse.org/legal/epl-v10.html
#
# Dockerfile for an elexis-server

FROM debian:testing

MAINTAINER Niklaus Giger "niklaus.giger@member.fsf.org"

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    gnupg2 dirmngr unzip procps telnet git ca-certificates ca-certificates-java

RUN echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list && \
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886 && \
    echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    mysql-server mysql-client oracle-java8-installer maven && \
    apt-get clean; rm -rf /var/lib/apt/lists/*

RUN git clone https://github.com/elexis/elexis-server.git /opt/elexis-server

# as the the next commands fails at the moment (2016-12-18)
# RUN cd  /opt/elexis-server &&  mvn --show-version --file releng/es.parent/pom.xml clean verify -Dmaven.test.skip
# we download and install the generated product from the Jenkins
RUN mkdir app && cd /app && wget --quiet  http://download.elexis.info/elexis-server/products/master/info.elexis.server.runtime.product-linux.gtk.x86_64.zip
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends expect
# procs to allow ps for debugging
RUN cd /app && ls -lh && \
    unzip -q info.elexis.server.runtime.product-linux.gtk.x86_64.zip
COPY assets/mysql_elexis.cnf /etc/mysql/conf.d/mysql_elexis.cnf
RUN adduser  --disabled-password --gecos "" es
RUN chown -R es:es /app  /home/es/
USER es
RUN mkdir -p /home/es/elexis-server/logs
COPY assets/shiro.ini assets/elexis-connection.xml /home/es/elexis-server/
COPY assets/start_elexis_server_test assets/wait-for-it.sh assets/create_server_test_db.mysql assets/telnet_test.expect assets/install_fhir.sh /app/
# RUN /app/install_fhir.sh
CMD /app/start_elexis_server_test
WORKDIR /home/es
