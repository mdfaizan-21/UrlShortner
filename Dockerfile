FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/shortner-0.0.1-SNAPSHOT.jar app.jar

# Render uses 10000 by default, but we'll make it dynamic
EXPOSE 10000

# We use the ${PORT} variable which Render provides automatically
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-Dserver.address=0.0.0.0", "-jar", "app.jar"]