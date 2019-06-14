package io.aboutcode.stage.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>A parser for application arguments that are used in an {@link
 * io.aboutcode.stage.application.ApplicationContainer}.</p>
 *
 * <p>The identifier of a parameter consists of a leading double dash ("<code>--</code>") and a
 * combination of characters, numbers and single-dashes ("<code>-</code>"). The values of parameters
 * are separated from the identifier through a whitespace or equals sign ("<code>=</code>"). Values
 * must not contain white spaces unless the whole value is enclosed in quotes. Values can be defined
 * as optional for boolean parameters, in which case the presence of the identifier signifies a
 * <code>true</code> value.</p>
 *
 * <p>If a parameter contains a list of values, the individual values are also separated by
 * whitespaces and values <em>with</em> whitespaces can be wrapped in double quotes. Double quotes
 * within a double quoted string can again be escaped by prepending them with a backslash
 * ("\").</p>
 */
public class ApplicationArgumentParser {
    private static final Pattern EQUALS = Pattern.compile("[^\\\\]=");
    private static final Pattern VALUES = Pattern.compile(
            "[\"](([^\\\\\"]*([\\\\].)*)*)[\"]"); // parse between double quotes, including escaped double quotes
    private static final String TRIGGER = "--";

    /**
     * Parses the specified arguments according to the rules of this class.
     *
     * @param arguments The arguments to parse
     *
     * @return A map of parameter names to the respective value providers.
     *
     * @throws ArgumentParseException Thrown if the arguments do not comply with the parsing rules
     *                                of this classe
     */
    public static Map<String, Supplier<List<String>>> parseArguments(String... arguments)
            throws ArgumentParseException {
        Map<String, Supplier<List<String>>> parsedArguments = new HashMap<>();

        String currentArgumentName = null;
        List<String> currentArgumentValues = new ArrayList<>();
        for (String token : arguments) {
            if (Objects.equals(TRIGGER, token)) {
                throw new ArgumentParseException("Found stray argument trigger (" + TRIGGER
                                                 + ") with no argument name; triggers and argument names must not be separated by whitespace");
            }
            String valuesString = token;
            if (token.startsWith(TRIGGER)) {
                // process previous argument
                if (currentArgumentName != null) {
                    parsedArguments
                            .put(currentArgumentName, new StaticSupplier(currentArgumentValues));
                    currentArgumentValues = new ArrayList<>();
                }

                // let's see if a space or an equals sign comes first
                int equalPosition = token.length();
                Matcher matcher = EQUALS.matcher(token);
                if (matcher.find()) {
                    equalPosition = matcher.end() - 1; // 1 is the length of the split character
                }
                int spacePosition = token.indexOf(' ');
                int splitPosition = Math.min(equalPosition > 0 ? equalPosition : token.length(),
                                             spacePosition > 0 ? spacePosition : token.length());
                currentArgumentName = token.substring(TRIGGER.length(), splitPosition);
                valuesString = token.substring(Math.min(splitPosition + 1, token.length()));
            }
            if (!valuesString.trim().isEmpty() && currentArgumentName != null) {
                currentArgumentValues.add(unescape(valuesString));
            }
        }

        // process last argument
        if (currentArgumentName != null && !currentArgumentName.isEmpty()) {
            parsedArguments.put(currentArgumentName, new StaticSupplier(currentArgumentValues));
        }

        return parsedArguments;
    }

    private static String unescape(String value) {
        Matcher matcher = VALUES.matcher(value);
        if (matcher.matches()) {
            return matcher.group(1);
        }

        return value;
    }

    private static class StaticSupplier implements Supplier<List<String>> {
        private final List<String> value;

        private StaticSupplier(List<String> value) {
            this.value = value;
        }

        @Override
        public List<String> get() {
            return value;
        }
    }
}
