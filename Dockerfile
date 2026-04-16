FROM eclipse-temurin:17-jdk-jammy AS build

RUN apt-get update && apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && \
    apt-get install -y nodejs

WORKDIR /app

# Instalar dependencias primero para aprovechar caché
COPY package*.json ./
RUN npm install

# Copiar el resto del código
COPY . .

# Dar permiso de ejecución a mvnw
RUN sed -i 's/\r$//' mvnw .mvn/wrapper/maven-wrapper.properties && chmod +x mvnw

# Compilar frontend nativamente indicando a maven que lo omita
RUN npm run webapp:prod
RUN ./mvnw -Pprod package -DskipTests -Dmaven.test.skip=true -Dskip.npm=true -Dskip.installnodenpm=true

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
