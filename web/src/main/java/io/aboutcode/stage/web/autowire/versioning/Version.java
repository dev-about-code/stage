package io.aboutcode.stage.web.autowire.versioning;

import com.google.common.base.Preconditions;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instances of this represent a version with major, minor and patch version numbers.
 */
public final class Version implements Comparable<Version> {
    private static final Pattern PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    private final int major;
    private final int minor;
    private final int patch;

    private Version(int major, int minor, int patch) {
        Preconditions.checkArgument(major >= 0);
        Preconditions.checkArgument(minor >= 0);
        Preconditions.checkArgument(patch >= 0);

        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * Creates a new Version instance from the specified information.
     *
     * @param major The major version number, must be &gt; 0
     * @param minor The minor version number, must be &gt; 0
     * @param patch The patch version number, must be &gt; 0
     *
     * @return A new Version instance with the specified information
     */
    public static Version from(int major, int minor, int patch) {
        return new Version(major, minor, patch);
    }

    /**
     * Creates a new Version instance from the specified information
     *
     * @param version A version string that <em>must</em> be in the format <code>/d+/./d+/./d+</code>.
     *                Any other format (or a null value) will cause this method to return an empty
     *                Optional
     *
     * @return Optionally, a new Version instance with the specified information
     */
    public static Optional<Version> from(String version) {
        return Optional.ofNullable(version)
                       .flatMap(versionString -> {
                                    Matcher matcher = PATTERN.matcher(versionString);
                                    if (matcher.matches()) {
                                        return version(matcher);
                                    }
                                    return Optional.empty();
                                }
                       );
    }

    private static Optional<Version> version(Matcher matcher) {
        try {
            return Optional.of(new Version(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3))
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the major version number.
     *
     * @return The major version number
     */
    public int getMajor() {
        return major;
    }


    /**
     * Returns the minor version number.
     *
     * @return The minor version number
     */
    public int getMinor() {
        return minor;
    }


    /**
     * Returns the patch version number.
     *
     * @return The patch version number
     */
    public int getPatch() {
        return patch;
    }

    @Override
    public int compareTo(Version other) {
        return Comparator.comparing(Version::getMajor)
                         .thenComparing(Version::getMinor)
                         .thenComparing(Version::getPatch)
                         .compare(this, other);
    }

    /**
     * Returns whether this version is an earlier version than the specified version.
     *
     * @param version The version to compare to
     *
     * @return True if this version is before the specified version
     */
    public boolean isBefore(Version version) {
        return this.compareTo(version) < 0;
    }

    /**
     * Returns whether this version is a later version than the specified version.
     *
     * @param version The version to compare to
     *
     * @return True if this version is after the specified version
     */
    public boolean isAfter(Version version) {
        return this.compareTo(version) > 0;
    }

    @Override
    public String toString() {
        return String.format("%s.%s.%s", getMajor(), getMinor(), getPatch());
    }
}
