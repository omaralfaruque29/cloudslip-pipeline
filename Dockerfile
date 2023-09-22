FROM openjdk:8-jdk-alpine


RUN apk update && apk add --no-cache libc6-compat

RUN apk update && \
  apk add --no-cache libc6-compat && \
  ln -s /lib/libc.musl-x86_64.so.1 /lib/ld-linux-x86-64.so.2


VOLUME /tmp
ADD target/cloudslip-pipeline-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=prod -jar /app.jar

HEALTHCHECK --interval=5s \
            --timeout=5s \
            CMD curl -f http://127.0.0.1:8082 || exit 1


# tell docker what port to expose
EXPOSE 8082
