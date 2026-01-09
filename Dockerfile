FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

COPY . .

# Dar permiso de ejecución a mvnw
RUN chmod +x mvnw

# Compilar proyecto en producción
RUN ./mvnw -Pprod package -DskipTests

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
