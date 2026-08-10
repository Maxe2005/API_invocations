# Contexte de build = racine du repo GatchaApi (voir docker-compose.yaml), pour pouvoir
# construire et installer la lib partagée gatcha-common-security avant ce service (Story 1.2).
FROM maven:3.9.6-amazoncorretto-21 AS common-build
WORKDIR /common
COPY gatcha-common-security/pom.xml .
COPY gatcha-common-security/src ./src
RUN mvn -q install -DskipTests

FROM maven:3.9.6-amazoncorretto-21 AS build
COPY --from=common-build /root/.m2 /root/.m2
WORKDIR /Api_invocations
COPY API_invocations/pom.xml .
COPY API_invocations/src ./src
RUN mvn clean package -Dmaven.test.skip=true

FROM amazoncorretto:21.0.2-alpine3.19
COPY --from=build /Api_invocations/target/*.jar /app.jar
EXPOSE 8080/tcp
ENTRYPOINT ["java", "-jar", "/app.jar"]
