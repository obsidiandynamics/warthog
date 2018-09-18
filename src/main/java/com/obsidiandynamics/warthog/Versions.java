package com.obsidiandynamics.warthog;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

import com.obsidiandynamics.concat.*;

/**
 *  Utilities for working with version strings in the form 'a.b.c[...]x.y.z[-SNAPSHOT]'.
 */
public final class Versions {
  private static final String VERSION_REGEX = "^([^\\.]*)(\\d+)\\.(\\d+)\\.?(\\d+)?(\\.?.*)$";

  private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

  private Versions() {}

  public static boolean isSnapshot(String version) {
    return version.endsWith(SNAPSHOT_SUFFIX);
  }

  public static String toSnapshot(String releaseVersion) {
    return releaseVersion + SNAPSHOT_SUFFIX;
  }

  public static String toRelease(String snapshotVersion) {
    return snapshotVersion.substring(0, snapshotVersion.length() - SNAPSHOT_SUFFIX.length());
  }

  public static List<Integer> parse(String version) {
    final var versionFrags = version.split("\\.");
    return Arrays.stream(versionFrags).map(Integer::parseInt).collect(Collectors.toList());
  }

  public static String format(List<Integer> versionSegments) {
    return new Concat().appendArray(".", versionSegments.toArray()).toString();
  }

  static final class InvalidVersionException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    InvalidVersionException(String m) { super(m); }
  }

  private static final class ParsedVersion {
    final String leadingText;
    final String majorVersionText;
    final String minorVersionText;
    final String incrementalVersionText;
    final String trailingText;

    ParsedVersion(String leadingText, 
                  String majorVersionText, 
                  String minorVersionText, 
                  String incrementalVersionText,
                  String trailingText) {
      this.leadingText = leadingText;
      this.majorVersionText = majorVersionText;
      this.minorVersionText = minorVersionText;
      this.incrementalVersionText = incrementalVersionText;
      this.trailingText = trailingText;
    }
  }

  private static ParsedVersion parseVersion(String version) {
    final var pattern = Pattern.compile(VERSION_REGEX);
    final var matcher = pattern.matcher(version);
    if (matcher.matches()) {
      final var leadingText = matcher.group(1);
      final var majorVersionText = matcher.group(2);
      final var minorVersionText = matcher.group(3);
      final var incrementalVersionText = matcher.group(4);
      final var trailingText = matcher.group(5);
      return new ParsedVersion(leadingText, majorVersionText, minorVersionText, incrementalVersionText, trailingText);
    } else {
      throw new InvalidVersionException("Invalid version '" + version + "'"); 
    }
  }

  /**
   *  Increments the major version — the first segment from the left in a conventional Maven-style
   *  version string. The minor version (the second segment) will be reset to zero. If the incremental 
   *  version (the third segment) is present, it will be also be reset to zero.
   *  
   *  @param version The version to roll.
   *  @return The updated version.
   */
  public static String rollMajor(String version) {
    final var parsed = parseVersion(version);
    final var majorVersion = Integer.parseInt(parsed.majorVersionText);
    if (parsed.incrementalVersionText != null) {
      return parsed.leadingText + (majorVersion + 1) + ".0.0" + parsed.trailingText;
    } else {
      return parsed.leadingText + (majorVersion + 1) + ".0" + parsed.trailingText;
    }
  }

  /**
   *  Increments the minor version — the second segment from the left in a conventional Maven-style
   *  version string. If the incremental version (the third segment) is present, it will be
   *  reset to zero.
   *  
   *  @param version The version to roll.
   *  @return The updated version.
   */
  public static String rollMinor(String version) {
    final var parsed = parseVersion(version);
    final var minorVersion = Integer.parseInt(parsed.minorVersionText);
    if (parsed.incrementalVersionText != null) {
      return parsed.leadingText + parsed.majorVersionText + "." + (minorVersion + 1) + ".0" + parsed.trailingText;
    } else {
      return parsed.leadingText + parsed.majorVersionText + "." + (minorVersion + 1) + parsed.trailingText;
    }
  }
}
