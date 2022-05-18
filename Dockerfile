FROM maven:3-openjdk-18-slim AS deps
WORKDIR /app
COPY pom.xml /app/
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml dependency:resolve dependency:resolve-plugins

FROM maven:3-openjdk-18-slim AS build
WORKDIR /app
COPY --from=deps /usr/share/maven/ref/repository /usr/share/maven/ref/repository/
COPY pom.xml /app/
COPY src /app/src/
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml package -D skipTests

#FROM amazoncorretto:18 AS gpsbabel-build
#RUN yum install qt5-qtbase-devel unzip gcc
#WORKDIR /gpsbabel
#RUN curl -o gpsbabel.zip 'https://codeload.github.com/GPSBabel/gpsbabel/zip/refs/tags/gpsbabel_1_8_0' && unzip gpsbabel.zip && cd gpsbabel-gpsbabel_1_8_0 &&


#FROM amazoncorretto:18
FROM debian:latest
RUN apt-get update && apt-get -y install openjdk-17-jre-headless gpsbabel && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/backend-*.jar /app.jar
ENV SPRING_PROFILES_ACTIVE=docker
CMD ["java", "-jar", "/app.jar"]