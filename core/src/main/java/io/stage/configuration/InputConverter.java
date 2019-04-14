package io.aboutcode.stage.configuration;

/**
 * <p>Implementations of this convert the string input for parameters to the expected type for that
 * parameter. For multi-value parameters, each separate value is passed to the converter.</p>
 *
 * @param <OutputT> The expected target type of the parameter
 */
public interface InputConverter<OutputT> {
   /**
    * Converts the specified input to the expected type.
    *
    * @param input The input argument to convert
    *
    * @return The resulting, converted value
    */
   OutputT convert(String input);
}
