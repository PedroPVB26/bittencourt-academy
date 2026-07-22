# Etapa 1 - Build
FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia primeiro o pom para aproveitar cache
COPY pom.xml .

# Baixa dependências
RUN mvn dependency:go-offline

# Copia o restante do projeto
COPY src ./src

# Gera o jar
RUN mvn clean package -DskipTests


# Etapa 2 - Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia apenas o jar gerado
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]