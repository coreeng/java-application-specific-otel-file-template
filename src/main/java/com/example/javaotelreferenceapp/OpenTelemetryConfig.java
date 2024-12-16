package com.example.javaotelreferenceapp;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.time.Duration;

/**
 * All SDK management takes place here, away from the instrumentation code, which should only access
 * the OpenTelemetry APIs.
 */
public final class OpenTelemetryConfig {
  private static final String LOG_ENDPOINT = System.getenv("OTEL_ENDPOINT") + "/v1/logs";
  private static final String METRIC_ENDPOINT = System.getenv("OTEL_ENDPOINT") + "/v1/metrics";
  private static final String SPAN_ENDPOINT = System.getenv("OTEL_ENDPOINT") + "/v1/traces";
  private static final Duration TIMEOUT = Duration.ofSeconds(10);
  private static final long METRIC_EXPORT_INTERVAL_MS = 800L;

  public static Resource createDefaultResource() {
    return Resource.getDefault().toBuilder().put("SERVICE_NAME", "my-service").build();
  }

  /**
   * Initializes an OpenTelemetry SDK with a logging, metric, and span exporter.
   *
   * @return A ready-to-use {@link OpenTelemetry} instance.
   */
  public static OpenTelemetry initOpenTelemetry() {
    final LogRecordExporter logRecordExporter =
        OtlpHttpLogRecordExporter.builder().setEndpoint(LOG_ENDPOINT).setTimeout(TIMEOUT).build();

    final SpanExporter spanExporter =
        OtlpHttpSpanExporter.builder().setEndpoint(SPAN_ENDPOINT).setTimeout(TIMEOUT).build();

    final MetricExporter metricExporter =
        OtlpHttpMetricExporter.builder().setEndpoint(METRIC_ENDPOINT).setTimeout(TIMEOUT).build();

    // Create an instance of PeriodicMetricReader and configure it
    // to export via the logging exporter
    final MetricReader periodicReader =
        PeriodicMetricReader.builder(metricExporter)
            .setInterval(Duration.ofMillis(METRIC_EXPORT_INTERVAL_MS))
            .build();

    // This will be used to create instruments
    final SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(periodicReader).build();

    // Tracer provider configured to export spans with SimpleSpanProcessor using
    // the span exporter.
    final SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
            .build();
    // Log provider configured to export logs with the batch BatchLogRecordProcessor using
    // the logging exporter.
    final SdkLoggerProvider logProvider =
        SdkLoggerProvider.builder()
            .setResource(createDefaultResource())
            .addLogRecordProcessor(
                BatchLogRecordProcessor.builder(logRecordExporter)
                    .setExporterTimeout(TIMEOUT)
                    .setScheduleDelay(Duration.ofSeconds(5))
                    .build())
            .build();

    return OpenTelemetrySdk.builder()
        .setLoggerProvider(logProvider)
        .setMeterProvider(meterProvider)
        .setTracerProvider(tracerProvider)
        .buildAndRegisterGlobal();
  }
}
