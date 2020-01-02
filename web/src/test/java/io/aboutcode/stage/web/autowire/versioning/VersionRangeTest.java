package io.aboutcode.stage.web.autowire.versioning;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VersionRangeTest {
    private static final Version MIN = Version.from(0, 0, 0);
    private static final Version MAX = Version.from(Integer.MAX_VALUE,
                                                    Integer.MAX_VALUE,
                                                    Integer.MAX_VALUE);

    private static VersionRange range(String start, String end) {
        return VersionRange
                .between(Version.from(start).orElse(null), Version.from(end).orElse(null));
    }

    @Test
    public void testWildcard() {
        Version version = Version.from(2, 3, 4);
        VersionRange range = VersionRange.wildcard();
        assertTrue(range.allows(MIN));
        assertTrue(range.allows(version));
        assertTrue(range.allows(MAX));
    }

    @Test
    public void testNulls() {
        Version version = Version.from(2, 3, 4);
        VersionRange range = VersionRange.between(null, null);
        assertTrue(range.allows(MIN));
        assertTrue(range.allows(version));
        assertTrue(range.allows(MAX));
    }

    @Test
    public void testStart() {
        Version version = Version.from(2, 3, 4);
        VersionRange range = VersionRange.from(version);
        assertFalse(range.allows(MIN));
        assertTrue(range.allows(MAX));
        assertTrue(range.allows(version));

        assertTrue(range.allows(Version.from(3, 3, 4)));
        assertTrue(range.allows(Version.from(2, 4, 4)));
        assertTrue(range.allows(Version.from(2, 3, 5)));

        assertFalse(range.allows(Version.from(1, 3, 4)));
        assertFalse(range.allows(Version.from(2, 2, 4)));
        assertFalse(range.allows(Version.from(2, 3, 3)));
    }

    @Test
    public void testEnd() {
        Version version = Version.from(2, 3, 4);
        VersionRange range = VersionRange.to(version);
        assertTrue(range.allows(MIN));
        assertFalse(range.allows(MAX));
        assertFalse(range.allows(version));

        assertFalse(range.allows(Version.from(3, 3, 4)));
        assertFalse(range.allows(Version.from(2, 4, 4)));
        assertFalse(range.allows(Version.from(2, 3, 5)));

        assertTrue(range.allows(Version.from(1, 3, 4)));
        assertTrue(range.allows(Version.from(2, 2, 4)));
        assertTrue(range.allows(Version.from(2, 3, 3)));
    }

    @Test
    public void testRange() {
        Version start = Version.from(2, 3, 4);
        Version end = Version.from(3, 4, 5);
        VersionRange range = VersionRange.between(start, end);
        assertFalse(range.allows(MIN));
        assertFalse(range.allows(MAX));
        assertTrue(range.allows(start));
        assertFalse(range.allows(end));

        assertTrue(range.allows(Version.from(3, 3, 4)));
        assertTrue(range.allows(Version.from(2, 4, 4)));
        assertTrue(range.allows(Version.from(2, 3, 5)));
        assertTrue(range.allows(Version.from(2, 4, 5)));
        assertTrue(range.allows(Version.from(3, 3, 5)));
        assertTrue(range.allows(Version.from(3, 4, 4)));

        assertFalse(range.allows(Version.from(1, 3, 4)));
        assertFalse(range.allows(Version.from(2, 2, 4)));
        assertFalse(range.allows(Version.from(2, 3, 3)));
    }

    @Test
    public void testOverlap() {
        Version start = Version.from(2, 3, 4);
        Version end = Version.from(3, 4, 5);
        VersionRange range = VersionRange.between(start, end);

        assertTrue(range.overlaps(range(null, null)));
        assertTrue(range.overlaps(range("1.1.1", null)));
        assertTrue(range.overlaps(range(null, "4.4.4")));
        assertTrue(range.overlaps(range("1.1.1", "4.4.4")));

        assertFalse(range.overlaps(range("1.1.1", "2.3.4")));
        assertFalse(range.overlaps(range("3.4.5", "4.4.4")));

        assertTrue(range.overlaps(range("1.1.1", "2.3.5")));
        assertTrue(range.overlaps(range("1.1.1", "3.4.4")));
        assertTrue(range.overlaps(range("2.3.5", "4.4.4")));
        assertTrue(range.overlaps(range("3.4.4", "4.4.4")));

        assertTrue(range.overlaps(range("2.4.0", "3.4.0")));

        assertFalse(range.overlaps(range("1.1.1", "2.2.2")));
        assertFalse(range.overlaps(range("1.1.1", "2.3.3")));
        assertFalse(range.overlaps(range(null, "2.3.3")));
        assertFalse(range.overlaps(range("3.4.5", null)));
        assertFalse(range.overlaps(range("3.4.5", "4.4.4")));
        assertFalse(range.overlaps(range("3.4.6", "4.4.4")));
        assertFalse(range.overlaps(range("3.4.6", null)));
    }
}