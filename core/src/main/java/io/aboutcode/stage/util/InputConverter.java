package io.aboutcode.stage.util;

/**
 * <p>Implementations of this convert string input values to different types.</p>
 *
 * @param <OutputT> The expected target type of the conversion
 */
public interface InputConverter<OutputT> {
    /**
     * Converts the specified input to the expected type.
     *
     * @param input The input to convert
     *
     * @return The resulting, converted value
     */
    OutputT convert(String input);
}
