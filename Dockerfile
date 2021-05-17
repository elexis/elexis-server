FROM adoptopenjdk/openjdk11:alpine-slim
MAINTAINER MEDEVIT <office@medevit.at>
ARG BRANCH=1.8

RUN apk add --no-cache tzdata bash
ENV TZ=Europe/Zurich
RUN addgroup elexis && adduser -S -u 1000 -G elexis -g "" -h /elexis elexis && \
    mkdir -p /opt/elexis-server && \
    wget http://download.elexis.info/elexis-server/${BRANCH}/products/info.elexis.server.runtime.product-linux.gtk.x86_64.zip && \
    unzip -d /opt/elexis-server/ info.elexis.server.runtime.product-linux.gtk.x86_64.zip && \
    rm info.elexis.server.runtime.product-linux.gtk.x86_64.zip && \
    chown -R elexis:elexis /opt/elexis-server /elexis

COPY releng/docker-assets/elexis-server.sh /

USER elexis
WORKDIR /elexis

EXPOSE 8380
EXPOSE 7234

VOLUME /elexis

CMD ["/elexis-server.sh"]
