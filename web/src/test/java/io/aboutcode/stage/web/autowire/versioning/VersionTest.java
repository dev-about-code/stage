package io.aboutcode.stage.web.autowire.versioning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;

public class VersionTest {

    @Test
    public void testDirectCreation() {
        Version version = Version.from(1, 2, 3);
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(3, version.getPatch());
    }

    @Test
    public void testStringCreation_valid() {
        Optional<Version> optionalVersion = Version.from("1.2.3");
        assertTrue(optionalVersion.isPresent());
        Version version = optionalVersion.get();
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(3, version.getPatch());
    }

    @Test
    public void testStringCreation_big() {
        Optional<Version> optionalVersion = Version.from("111111111.222222222.333333333");
        assertTrue(optionalVersion.isPresent());
        Version version = optionalVersion.get();
        assertEquals(111111111, version.getMajor());
        assertEquals(222222222, version.getMinor());
        assertEquals(333333333, version.getPatch());
    }

    @Test
    public void testStringCreation_tooBig() {
        Optional<Version> optionalVersion = Version.from("1111111111.2222222222.3333333333");
        assertFalse(optionalVersion.isPresent());
    }

    @Test
    public void testStringCreation_alpha() {
        Optional<Version> optionalVersion = Version.from("a.3.4");
        assertFalse(optionalVersion.isPresent());
    }

    @Test
    public void testStringCreation_extra() {
        Optional<Version> optionalVersion = Version.from("2.3.4-pre");
        assertFalse(optionalVersion.isPresent());
    }

    @Test
    public void testStringCreation_negative() {
        Optional<Version> optionalVersion = Version.from("-2.3.4");
        assertFalse(optionalVersion.isPresent());
    }

    @Test
    public void testAfter() {
        Version version = Version.from(2, 3, 4);
        Version majorBefore = Version.from(1, 3, 4);
        Version minorBefore = Version.from(2, 2, 4);
        Version patchBefore = Version.from(2, 3, 3);
        Version majorAfter = Version.from(3, 3, 4);
        Version minorAfter = Version.from(2, 4, 4);
        Version patchAfter = Version.from(2, 3, 5);
        Version match = Version.from(2, 3, 4);

        assertTrue(version.isAfter(majorBefore));
        assertTrue(version.isAfter(minorBefore));
        assertTrue(version.isAfter(patchBefore));
        assertFalse(version.isAfter(majorAfter));
        assertFalse(version.isAfter(minorAfter));
        assertFalse(version.isAfter(patchAfter));
        assertFalse(version.isAfter(match));
    }

    @Test
    public void testBefore() {
        Version version = Version.from(2, 3, 4);
        Version majorBefore = Version.from(1, 3, 4);
        Version minorBefore = Version.from(2, 2, 4);
        Version patchBefore = Version.from(2, 3, 3);
        Version majorAfter = Version.from(3, 3, 4);
        Version minorAfter = Version.from(2, 4, 4);
        Version patchAfter = Version.from(2, 3, 5);
        Version match = Version.from(2, 3, 4);

        assertFalse(version.isBefore(majorBefore));
        assertFalse(version.isBefore(minorBefore));
        assertFalse(version.isBefore(patchBefore));
        assertTrue(version.isBefore(majorAfter));
        assertTrue(version.isBefore(minorAfter));
        assertTrue(version.isBefore(patchAfter));
        assertFalse(version.isBefore(match));
    }

    @Test(expected = IllegalArgumentException.class)
    public void tetNegativeAll() {
        Version.from(-2, -3, -4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tetNegativeMajor() {
        Version.from(-2, 3, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tetNegativeMinor() {
        Version.from(2, -3, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tetNegativePatch() {
        Version.from(2, 3, -4);
    }
}