package io.aboutcode.stage.util;

import io.aboutcode.stage.dispatch.Dispatcher;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
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
 * <li>ZonedDateTime</li>
 * <li>LocalDateTime</li>
 * <li>LocalDate</li>
 * <li>LocalTime</li>
 * </ul>
 *
 * <p><em>Note</em> that <code>Object</code> types will simply use the input string as-is.</p>
 */
public final class DefaultTypeConverters {
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
                    .with(ZonedDateTime.class, ZonedDateTime::parse)
                    .with(LocalDateTime.class, LocalDateTime::parse)
                    .with(LocalDate.class, LocalDate::parse)
                    .with(LocalTime.class, LocalTime::parse);

    private DefaultTypeConverters() {
    }

    public static <OutputT> Optional<InputConverter<OutputT>> getConverter(Class<OutputT> type) {
        //noinspection unchecked
        return CLASS_TO_CONVERTER.dispatch(type)
                                 .map(converter -> Optional.of((InputConverter<OutputT>) converter))
                                 .orElseGet(() -> enumConverter(type));
    }

    private static <OutputT> Optional<InputConverter<OutputT>> enumConverter(Class<OutputT> type) {
        if (Enum.class.isAssignableFrom(type)) {
            //noinspection unchecked
            return Optional.of(input -> (OutputT) Enum
                    .valueOf((Class<Enum>) type, input));
        }

        return Optional.empty();
    }
}
