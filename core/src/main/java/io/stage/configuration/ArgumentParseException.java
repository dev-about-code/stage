package io.aboutcode.stage.configuration;

/**
 * Thrown if argument parsing fails.
 */
public class ArgumentParseException extends Exception {
   public ArgumentParseException(String message) {
      super(message);
   }
}
