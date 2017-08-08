FROM openjdk:8-jre
MAINTAINER MEDEVIT <office@medevit.at>
ARG branch=1.4

RUN adduser --disabled-password --gecos "" --home /elexis elexis && \
    mkdir /es-prog && \
    wget http://download.elexis.info/elexis-server/products/$branch/info.elexis.server.runtime.product-linux.gtk.x86_64.zip && \
    unzip info.elexis.server.runtime.product-linux.gtk.x86_64.zip -d /opt/elexis-server && \
    rm info.elexis.server.runtime.product-linux.gtk.x86_64.zip && \
    	mkdir -p /elexis/elexis-server/logs && \
    	chown -R elexis:elexis /opt/elexis-server /elexis
    
# Initialize demo database
RUN cd /elexis/ && \
	wget http://download.elexis.info/elexis/demoDB/demoDB_elexis_DBVersion_3.2.7.zip && \
	unzip demoDB_elexis_DBVersion_3.2.7.zip && \
	rm demoDB_elexis_DBVersion_3.2.7.zip && \
	chown -R elexis:elexis /opt/elexis-server /elexis

COPY releng/docker-assets/elexis-connection.xml /elexis/elexis-server/
COPY releng/docker-assets/elexis-server.sh /

WORKDIR /elexis

USER elexis
EXPOSE 8380
EXPOSE 7234

CMD ["/elexis-server.sh"]
