FROM maven:3.8.5-openjdk-17 as build
COPY pom.xml .
COPY settings.xml .
COPY src /src
RUN mvn -s settings.xml -Dmaven.test.skip=true clean package


FROM openjdk:17-slim

WORKDIR /app

COPY --from=build target/*fat.jar app.jar

EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
