# ===========================
# Etapa 1: Build frontend Angular
# ===========================
FROM node:20 AS frontend-build

WORKDIR /app

# Copia solo la carpeta del frontend
COPY src/main/webapp/package*.json ./ 
COPY src/main/webapp/ ./

# Instala dependencias Angular
RUN npm install

# Construye Angular en modo producción
RUN npm run build

# ===========================
# Etapa 2: Build backend Spring Boot
# ===========================
FROM eclipse-temurin:17-jdk-jammy AS backend-build

WORKDIR /app

# Copia todo el proyecto
COPY . .

# Copia los archivos compilados del frontend a su carpeta correspondiente
COPY --from=frontend-build /app/dist ./src/main/webapp/dist

# Compila backend Spring Boot
RUN ./mvnw -Pprod package -DskipTests

# ===========================
# Etapa 3: Runtime
# ===========================
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copia solo el JAR generado
COPY --from=backend-build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
