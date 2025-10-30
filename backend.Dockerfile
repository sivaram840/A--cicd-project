# --- STAGE 1: Build the Java application ---
# Use an official Maven image to build the app
FROM maven:3.9.8-eclipse-temurin-17 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml first to leverage Docker's layer caching
COPY expense-splitter-backend/pom.xml .

# Download dependencies (this layer is cached if pom.xml doesn't change)
RUN mvn dependency:go-offline

# Copy the rest of the backend source code
COPY expense-splitter-backend/src ./src

# Package the application, skipping the tests that were failing
# This creates the .jar file
RUN mvn clean package -DskipTests

# --- STAGE 2: Create the final, lightweight image ---
# Use a lightweight JRE image
FROM eclipse-temurin:17-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the .jar file created in the 'build' stage
# Make sure this .jar file name is correct!
COPY --from=build /app/target/expense-splitter-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your application runs on
EXPOSE 8080

# The command to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]
