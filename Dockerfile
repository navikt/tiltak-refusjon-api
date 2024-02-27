FROM ghcr.io/navikt/baseimages/temurin:21
COPY /target/app.jar app.jar
EXPOSE 8080
