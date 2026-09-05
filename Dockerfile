
# ============================
# Etape 1 : build avec Maven
# ============================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copier d'abord le pom.xml pour profiter du cache Docker sur les dependances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le reste du code source et builder le jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================
# Etape 2 : image d'execution
# ============================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Creer un utilisateur non-root (bonne pratique securite)
RUN useradd -m smartbiz
USER smartbiz

# Recuperer uniquement le jar buildé
COPY --from=build /app/target/*.jar app.jar

# Render fournit la variable PORT dynamiquement
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]