package io.aboutcode.stage.web.serialization;

/**
 * Implementations of this allow serialization and deserialization of objects to and from a string
 * representation.
 */
public interface WebSerialization {
    /**
     * Deserializes the specified input string to the specified type. Note that this method
     * <em>MUST</em> be symmetric with {@link WebSerialization#serialize(Object)}
     *
     * @param input The input to deserialize
     * @param type  The type to deserialize the input to
     * @param <T>   The generic type of the target type
     *
     * @return The deserialized object
     */
    <T> T deserialize(String input, Class<T> type);

    /**
     * Serializes the specified input into string format.  Note that this method <em>MUST</em> be
     * symmetric with {@link WebSerialization#deserialize(String, Class)}
     *
     * @param input The input to serialize
     *
     * @return The string representation of the specified object
     */
    String serialize(Object input);
}
