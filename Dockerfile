FROM eclipse-temurin:21-jre-alpine
LABEL org.opencontainers.image.authors="MEDEVIT <office@medevit.at>"
ARG BRANCH=master
ARG TARGETPLATFORM

# https://www.docker.com/blog/multi-arch-build-and-images-the-simple-way/

RUN apk add --no-cache tzdata bash gcompat cups-client
ENV TZ=Europe/Zurich
ENV LANGUAGE=en_US:en  
ENV LANG=de_CH.UTF-8  
ENV LC_ALL=de_CH.UTF-8
ENV ELEXIS-BRANCH=${BRANCH}
RUN case ${TARGETPLATFORM} in \
         "linux/amd64")  ES_ARCH=x86_64  ;; \
         "linux/arm64")  ES_ARCH=aarch64  ;; \
    esac && \
	addgroup --gid 1001 elexis && adduser -S -u 1001 -G elexis -g "" -h /elexis elexis && \
    mkdir -p /opt/elexis-server && \
    wget https://download.elexis.info/elexis-server/${BRANCH}/products/info.elexis.server.runtime.product-linux.gtk.$ES_ARCH.zip && \
    unzip -d /opt/elexis-server/ info.elexis.server.runtime.product-linux.gtk.$ES_ARCH.zip && \
    rm info.elexis.server.runtime.product-linux.gtk.$ES_ARCH.zip && \
    chown -R elexis:elexis /opt/elexis-server /elexis

COPY releng/docker-assets/elexis-server.sh /

USER elexis
WORKDIR /elexis

EXPOSE 8380
EXPOSE 7234


CMD ["/elexis-server.sh"]
