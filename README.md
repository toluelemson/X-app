# XchangeApp


## Overview
This project consists of a frontend and a backend service, each running in its own Docker container. The frontend is a web application accessible via port 4200, and the backend is a Java application accessible via port 8080. This README outlines how to set up and run the project using Docker Compose.

## Prerequisites
Before you begin, you need to have the following installed on your system:
- **Docker**: Ensure Docker is installed and running on your machine. For installation instructions, visit [Docker's official site](https://docs.docker.com/get-docker/).
- **Docker Compose**: Ensure Docker Compose is installed. For installation instructions, visit [Docker Compose's official site](https://docs.docker.com/compose/install/).

## Architecture
This Docker Compose setup includes the following services:
- **Frontend**: An Angular application served on port 4200.
- **Backend**: A Java backend application served on port 8080.

## File Structure
- `/client`: Contains the Dockerfile and source code for the frontend.
- `/backend`: Contains the Dockerfile and source code for the backend.
- `docker-compose.yml`: Defines the configuration of the Docker services.

## Setup Instructions

### 1. Clone the Repository
Clone this repository to your local machine:
```bash
git clone [https://github.com/yourusername/yourproject.git](https://github.com/toluelemson/X-app.git)
cd X-app
```

### 2. Build and Run with Docker Compose
From the root directory of the project, run the following command to build and start all services defined in the `docker-compose.yml` file:
```bash
docker-compose up --build
```

This command builds the images for the frontend and backend if they don't exist and starts the containers.

### 3. Accessing the Applications
- **Frontend**: Open your web browser and go to `http://localhost:4200` to access the frontend application.
- **Backend**: The backend API is accessible via `http://localhost:8080`.


## Stopping the Services
To stop and remove the containers, use the following command:
```bash
docker-compose down
```
