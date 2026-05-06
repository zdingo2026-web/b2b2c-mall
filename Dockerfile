FROM maven:3.8-openjdk-8 AS builder
WORKDIR /app
COPY pom.xml .
COPY mall-common/pom.xml mall-common/
COPY mall-model/pom.xml mall-model/
COPY mall-dao/pom.xml mall-dao/
COPY mall-service/pom.xml mall-service/
COPY mall-admin/pom.xml mall-admin/
COPY mall-api/pom.xml mall-api/
RUN mvn dependency:go-offline -B
COPY . .
RUN mvn package -DskipTests -q

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=builder /app/mall-api/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
