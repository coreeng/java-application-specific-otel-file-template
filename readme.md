# Java Application Specific Instrumentation Reference Template

This guide helps you set up and run a local OpenTelemetry reference app with minimal code changes. Follow these steps to get started.

## Prerequisites

Before you begin, make sure you have the following installed and set up on your machine:

- Docker/Podman: The OpenTelemetry Collector runs in a OCI container, so you'll need Docker installed on your system. [Install Docker](https://docs.docker.com/engine/install/) if you don't have it yet.

- Java (JDK 17 or later): You'll need Java installed to build and run the application. The app is built with Gradle, so ensure that Java is properly configured. [Install Java](https://www.openlogic.com/openjdk-downloads) (JDK 17 or later).

## Steps to Run

### Start the OpenTelemetry Collector

The OpenTelemetry Collector is responsible for collecting and processing telemetry data (like traces and metrics). We are going to store the metrics, spans and logs in a file locally, its going to need broad permissions so that docker can write to it. Lets create it.

```sh
touch logs.json
chmod 666
```

To start the collector, run this command:

```sh
docker run -it \
  -p 0.0.0.0:4317:4317 \
  -p 0.0.0.0:4318:4318 \
  -p 0.0.0.0:55679:55679 \
  -v ./:/app \
  docker.io/otel/opentelemetry-collector-contrib:0.115.1 \
  --config /app/example-configuration.yaml
```

This command does the following:
- Runs the OpenTelemetry Collector in a Docker container.
- Opens necessary ports for communication.
- Mounts the current directory (./) into the container to access the configuration file (example-configuration.yaml).

### Build and Run the Java Application

Next, build the Java application and run it with OpenTelemetry support:

#### Build the app:

```sh
export OTEL_ENDPOINT="http://0.0.0.0:4318"
./gradlew bootRun
```

This will start the app and automatically connect to the OpenTelemetry Collector.

### Call the API
To test the app, call the API by running this command:

```sh
curl http://localhost:8080/rolldice
```

This will trigger the app to roll a dice and generate telemetry data.

### Check the Logs
You can view the app's output logs by running:

```sh
tail -f logs.json
```

This will display the log output in real-time as the app runs.

By following these steps, you'll have the OpenTelemetry Collector running, your Java app instrumented with OpenTelemetry, and be able to observe telemetry data in the logs.
