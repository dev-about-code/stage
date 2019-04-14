package io.aboutcode.stage.configuration;

/**
 * Thrown by the configuration mechanism if a component within the configuration procedure
 * misbehaves.
 */
public class ConfigurationException extends RuntimeException {
   public ConfigurationException(final String message) {
      super(message);
   }
}
