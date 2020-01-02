package io.aboutcode.stage.web.autowire.versioning;

/**
 * Instances of this represent a range of versions with an (optional) start and (optional) end
 * version.
 */
public final class VersionRange {
    private static final Version START = Version.from(0, 0, 0);
    private static final Version END = Version
            .from(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private final Version start;
    private final Version end;

    private VersionRange(Version start, Version end) {
        this.start = start == null ? START : start;
        this.end = end == null ? END : end;
    }

    /**
     * Returns a version range that spans all versions, without a specified start or end version
     * set.
     *
     * @return A version range that spans all versions
     */
    public static VersionRange wildcard() {
        return new VersionRange(null, null);
    }

    /**
     * Returns a version range that spans the versions between the start and end version inclusively
     * and exclusively, respectively.
     *
     * @param start The version at the start of the range, inclusively
     * @param end   The version at the end of the range, exclusively
     *
     * @return A version range that spans the versions between start and end version inclusively and
     * exclusively, respectively
     */
    public static VersionRange between(Version start, Version end) {
        return new VersionRange(start, end);
    }

    /**
     * Returns a version range that starts at the specified version (inclusively) and includes all
     * versions going forward.
     *
     * @param start The version that starts the range, inclusively
     *
     * @return A version range that starts at the specified version (inclusively) and includes all
     * versions going forward
     */
    public static VersionRange from(Version start) {
        return new VersionRange(start, null);
    }

    /**
     * Returns a version range that ends at the specified version (exclusively) and includes all
     * versions before that up until <code>0.0.0</code>.
     *
     * @param end The version that ends the range, exclusively
     *
     * @return A version range that ends at the specified version (exclusively) and includes all
     * versions before that
     */
    public static VersionRange to(Version end) {
        return new VersionRange(null, end);
    }

    /**
     * Returns whether the specified version falls within this version range.
     *
     * @param version The version to check
     *
     * @return True if the specified version falls within the range; false otherwise
     */
    public boolean allows(Version version) {
        return !start.isAfter(version) && (end == END || end.isAfter(version));
    }

    /**
     * Returns whether this version range overlaps with the specified version range.
     *
     * @param versionRange The version range to check overlap with
     *
     * @return True if the ranges overlap either at the start, end or both; false otherwise.
     */
    public boolean overlaps(VersionRange versionRange) {
        return !(start.isAfter(versionRange.end)) && (end == END || end.isAfter(versionRange.start));
    }
}
