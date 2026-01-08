FROM gcr.io/distroless/java21-debian12:nonroot
ENV TZ="Europe/Oslo"
COPY /target/app.jar /app/app.jar
EXPOSE 8080
WORKDIR /app
CMD ["app.jar"]
