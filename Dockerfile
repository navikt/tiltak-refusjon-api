FROM ghcr.io/navikt/baseimages/temurin:17
COPY /target/app.jar app.jar
EXPOSE 8080
