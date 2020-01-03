package io.aboutcode.stage.web.util;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Paths {
    private Paths() {}

    /**
     * Concatenates the specified paths without doubling any separators while removing trailing and
     * ending doubling separators. Note that all double separators in the paths themselves are left
     * untouched.
     *
     * @param paths The paths to concatenate
     *
     * @return Optionally, the concatenated paths with trailing and leading separator
     */
    public static Optional<String> concat(String... paths) {
        return Optional.ofNullable(paths)
                       .map(Stream::of)
                       .map(elements -> elements
                               .map(Paths::trimSeparators)
                               .filter(path -> !path.isEmpty())
                               .collect(Collectors.joining("/"))
                       )
                       .map(Paths::prependSeparator);
    }

    private static String trimSeparators(String input) {
        return input == null ? "" : input.replaceAll("^/+|/+$", "");
    }

    private static String prependSeparator(String input) {
        return ('/' + input);
    }
}
