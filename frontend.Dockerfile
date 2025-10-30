# --- STAGE 1: Build the React application ---
# Use an official Node.js image
FROM node:20-alpine AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the package.json and package-lock.json from your 'frontend' folder
COPY frontend/package.json .
COPY frontend/package-lock.json .

# Install dependencies
RUN npm install

# Copy the rest of your 'frontend' source code
COPY frontend/ .

# Build the production-ready static files
RUN npm run build

# --- STAGE 2: Create the final, lightweight image ---
# Use an official Nginx image
FROM nginx:1.27-alpine

# Copy the built static files from the 'build' stage
COPY --from=build /app/dist /usr/share/nginx/html

# We will create this nginx.conf file in the next step
# It tells Nginx how to serve the React app
COPY nginx.conf /etc/nginx/nginx.conf

# Expose port 80 (default for Nginx)
EXPOSE 80