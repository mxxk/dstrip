FROM azul/zulu-openjdk-alpine:11 AS base

# Install Maven.
RUN \
    mkdir /opt/mvn && \
    wget \
        http://ftp.wayne.edu/apache/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.tar.gz \
        -O - \
        | \
        tar -xz --strip 1 -C /opt/mvn

# Generate minimal JRE runtime image from full-block JDK.
RUN \
    jlink \
        --compress 2 \
        --add-modules java.base,java.desktop \
        --output /jlinked

ENV PATH="/opt/mvn/bin:${PATH}"
WORKDIR /app
COPY src ./src
COPY pom.xml .
RUN mvn package

FROM alpine:latest
COPY --from=base /jlinked /opt/jdk
COPY --from=base /app/target/*with-dependencies.jar /main.jar
ENTRYPOINT ["/opt/jdk/bin/java", "-jar", "/main.jar"]
