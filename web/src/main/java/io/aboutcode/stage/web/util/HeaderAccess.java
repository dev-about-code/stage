package io.aboutcode.stage.web.util;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class HeaderAccess {
    private static final Pattern ACCESS_HEADER_PATTERN = Pattern.compile(",");
    private static final Pattern ACCESS_SUBHEADER_PATTERN = Pattern.compile(";");

    private HeaderAccess() {
    }

    /**
     * Parses the specified header string into a set of content types
     *
     * @param headerValue The full header value string as specified in the request
     *
     * @return A set of valid content types
     */
    public static Set<String> acceptHeader(String headerValue) {
        if (Objects.isNull(headerValue) || headerValue.isEmpty()) {
            return Collections.emptySet();
        }
        return ACCESS_HEADER_PATTERN.splitAsStream(headerValue)
                                    .filter(entry -> !entry.trim().isEmpty())
                                    .map(entry -> ACCESS_SUBHEADER_PATTERN
                                            .splitAsStream(entry)
                                            .findFirst()
                                            .orElse(null))
                                    .filter(element -> !Objects.isNull(element))
                                    .map(String::trim)
                                    .collect(Collectors.toSet());
    }
}
