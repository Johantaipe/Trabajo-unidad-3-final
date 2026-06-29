# Imagen oficial moderna de Java 17
FROM eclipse-temurin:17-jdk

# Copiar el jar
COPY target/*.jar app.jar

# Puerto
EXPOSE 8080

# Ejecutar app
ENTRYPOINT ["java","-jar","/app.jar"]