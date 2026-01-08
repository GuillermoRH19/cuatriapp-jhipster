# ===========================
# Etapa 1: Build
# ===========================
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Copia todo el proyecto al contenedor
COPY . .

# Instala dependencias Node (para Angular frontend)
RUN npm install --prefix src/main/webapp

# Compila frontend Angular en modo producción
RUN npm run build --prefix src/main/webapp

# Compila backend Spring Boot en modo producción
RUN ./mvnw -Pprod package -DskipTests

# ===========================
# Etapa 2: Runtime
# ===========================
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copia solo el JAR generado en la etapa build
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto donde correrá Spring Boot
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java","-jar","/app/app.jar"]
