FROM maven:3.9.9-eclipse-temurin-21
WORKDIR /workspace
COPY pom.xml testng.xml ./
COPY src ./src
RUN mvn -B -ntp -DskipTests test
CMD ["mvn", "-B", "-ntp", "test"]
