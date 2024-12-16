package com.example.javaotelreferenceapp;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RollController {
  private static final Logger logger =
      JavaOtelReferenceApp.loggerProvider.get(RollController.class.toString());

  public static void log(final Severity severity, final String body) {
    RollController.logger.logRecordBuilder().setBody(body).setSeverity(severity).emit();
  }

  @GetMapping("/rolldice")
  public String index(@RequestParam("player") Optional<String> player) {
    final int result = this.getRandomNumber(1, 6);
    final String playerName = player.orElseGet(() -> "Anonymous");
    final Attributes attributes =
        Attributes.of(
            AttributeKey.stringKey("player"),
            playerName,
            AttributeKey.stringKey("rolled"),
            String.valueOf(result));
    log(Severity.INFO, String.format("{} is rolling the dice: {}", playerName, result));
    JavaOtelReferenceApp.counter.add(1, attributes);
    return Integer.toString(result);
  }

  public int getRandomNumber(final int min, final int max) {
    final Span span = JavaOtelReferenceApp.tracer.spanBuilder("processHelloRequest").startSpan();
    try (final var scope = span.makeCurrent()) {
      log(Severity.INFO, "Allocating random number");
      return ThreadLocalRandom.current().nextInt(min, max + 1);
    } finally {
      span.end();
    }
  }
}
