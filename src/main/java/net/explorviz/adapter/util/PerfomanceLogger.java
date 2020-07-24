package net.explorviz.adapter.util;

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;

public abstract class PerfomanceLogger {

  // The logger to emmit messages
  protected final Logger baseLogger;

  protected long currentLogs;

  private String message;


  public PerfomanceLogger(final Logger baseLogger, String message) {
    this.message = message;
    this.baseLogger = baseLogger;
    currentLogs = 0;
  }



  public abstract void logOperation();

  protected void outputLog(Duration duration) {
    baseLogger.info(this.message, currentLogs, duration.toMillis());
  }

  public static PerfomanceLogger newOperationPerformanceLogger(
      Logger baseLogger,
      long operationThreshold,
      String message) {
    return new OperationThresholdLogger(baseLogger, operationThreshold, message);
  }

  /**
   * Emits a log once a given amount of operations was executed
   */
  private static class OperationThresholdLogger extends PerfomanceLogger {

    private final long opThreshold;

    private Instant start;

    public OperationThresholdLogger(final Logger baseLogger, final long opThreshold, String message) {
      super(baseLogger, message);
      this.opThreshold = opThreshold;
    }

    @Override
    public void logOperation() {
      if (currentLogs == 0) {
        start = Instant.now();
      }
      currentLogs += 1;
      if (currentLogs == opThreshold) {
        Duration duration = Duration.between(start, Instant.now());
        this.outputLog(duration);
        currentLogs = 0;
      }
    }

  }



}
