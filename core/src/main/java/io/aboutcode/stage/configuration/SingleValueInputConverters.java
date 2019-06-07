package io.aboutcode.stage.configuration;

import io.aboutcode.stage.dispatch.Dispatcher;
import java.util.Optional;

/**
 * <p>This uses type introspection to guess the correct conversion mechanism for a set of standard
 * types. The following types are supported:</p>
 * <ul>
 * <li>Integer</li>
 * <li>int</li>
 * <li>Long</li>
 * <li>long</li>
 * <li>Double</li>
 * <li>double</li>
 * <li>Float</li>
 * <li>float</li>
 * <li>Byte</li>
 * <li>byte</li>
 * <li>Character</li>
 * <li>char</li>
 * <li>Short</li>
 * <li>short</li>
 * <li>Boolean</li>
 * <li>boolean</li>
 * <li>String</li>
 * <li>Object</li>
 * </ul>
 *
 * <p><em>Note</em> that <code>Object</code> types will simply use the input string as-is.</p>
 */
final class SingleValueInputConverters {
    private static final Dispatcher<Class, InputConverter<?>> CLASS_TO_CONVERTER =
            Dispatcher
                    .<Class, InputConverter<?>>of(Integer.class, Integer::parseInt)
                    .with(int.class, Integer::parseInt)
                    .with(Long.class, Long::parseLong)
                    .with(long.class, Long::parseLong)
                    .with(Double.class, Double::parseDouble)
                    .with(double.class, Double::parseDouble)
                    .with(Float.class, Float::parseFloat)
                    .with(float.class, Float::parseFloat)
                    .with(Byte.class, Byte::parseByte)
                    .with(byte.class, Byte::parseByte)
                    .with(Character.class, input -> input.charAt(0))
                    .with(char.class, input -> input.charAt(0))
                    .with(Short.class, Short::parseShort)
                    .with(short.class, Short::parseShort)
                    .with(Boolean.class, Boolean::parseBoolean)
                    .with(boolean.class, Boolean::parseBoolean)
                    .with(String.class, input -> input)
                    .with(Object.class, input -> input)
            // todo: date types
            ;

    private SingleValueInputConverters() {
    }

    static <OutputT> Optional<InputConverter<OutputT>> getConverter(Class<OutputT> type) {
        //noinspection unchecked
        return CLASS_TO_CONVERTER.dispatch(type).map(result -> (InputConverter<OutputT>) result);
    }
}
