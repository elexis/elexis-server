FROM openjdk:8-jre-alpine
MAINTAINER MEDEVIT <office@medevit.at>
ARG BRANCH=master

RUN apk add --no-cache tzdata openvpn iptables sudo bash libc6-compat certbot openssl bind-tools
ENV TZ=Europe/Zurich
RUN addgroup elexis && adduser -S -u 1000 -G elexis -g "" -h /elexis elexis && \
    mkdir -p /opt/elexis-server && \
    wget http://download.elexis.info/elexis-server/${BRANCH}/products/info.elexis.server.runtime.product-linux.gtk.x86_64.zip && \
    unzip -d /opt/elexis-server/ info.elexis.server.runtime.product-linux.gtk.x86_64.zip && \
    rm info.elexis.server.runtime.product-linux.gtk.x86_64.zip && \
    chown -R elexis:elexis /opt/elexis-server /elexis && \
    ln -s /elexis/letsencrypt /etc/letsencrypt

COPY releng/docker-assets/sudoers /etc/
COPY releng/docker-assets/elexis-server.sh /
COPY releng/docker-assets/startopenvpn.sh /
COPY releng/docker-assets/createESKeystore.sh /

USER elexis
WORKDIR /elexis

USER elexis
EXPOSE 8480
EXPOSE 8380
EXPOSE 7234

VOLUME /elexis

CMD ["/elexis-server.sh"]
