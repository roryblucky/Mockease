FROM openjdk:17-alpine

WORKDIR /app

COPY target/*fat.jar app.jar

EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
