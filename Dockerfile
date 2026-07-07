FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app
COPY . /app
RUN mvn clean package
CMD ["java", "-jar", "target/argo-server-globale-1.0-SNAPSHOT-jar-with-dependencies.jar"]
