package com.example.javaotelreferenceapp;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JavaOtelReferenceApp {
  private static final String INSTRUMENTATION_NAME = JavaOtelReferenceApp.class.getName();
  private static final OpenTelemetry openTelemetry = OpenTelemetryConfig.initOpenTelemetry();

  static final Tracer tracer = openTelemetry.getTracer(INSTRUMENTATION_NAME);
  static final LongCounter counter =
      openTelemetry.getMeter(INSTRUMENTATION_NAME).counterBuilder("rolls_completed").build();
  static final LoggerProvider loggerProvider = openTelemetry.getLogsBridge();

  public static void main(String[] args) {
    final SpringApplication app = new SpringApplication(JavaOtelReferenceApp.class);
    app.setBannerMode(Banner.Mode.OFF);
    app.run(args);
  }
}
