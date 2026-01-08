# Etapa de build backend + frontend
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Copia todo el proyecto
COPY . .

# Compila todo en modo producción (backend + frontend)
RUN ./mvnw -Pprod package -DskipTests

# Etapa de runtime
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copia solo el JAR final
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
