FROM mcr.microsoft.com/devcontainers/java:17-bullseye AS build

RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

WORKDIR /app

COPY . .

# Dar permiso de ejecución a mvnw
RUN sed -i 's/\r$//' mvnw .mvn/wrapper/maven-wrapper.properties && chmod +x mvnw

# Compilar proyecto en producción
RUN ./mvnw -Pprod package -DskipTests -Dmaven.test.skip=true

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
