package com.obsidiandynamics.warthog;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

import com.obsidiandynamics.concat.*;

/**
 *  Utilities for working with version strings in the form 'a.b.c[...]x.y.z[-SNAPSHOT]'.
 */
public final class Versions {
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
  
  /**
   *  Increments the minor version — the second segment from the left in a conventional Maven-style
   *  version string.
   *  
   *  @param version The version.
   *  @return Incremented version.
   */
  public static String rollMinor(String version) {
    final var pattern = Pattern.compile("^([^\\\\.]+\\.)(\\d+)(\\.?.*)$");
    final var matcher = pattern.matcher(version);
    if (matcher.matches()) {
      final var leadingText = matcher.group(1);
      final var minorVersionText = matcher.group(2);
      final var trailingText = matcher.group(3);
      final var minorVersion = Integer.parseInt(minorVersionText);
      return leadingText + (minorVersion + 1) + trailingText;
    } else {
      throw new IllegalArgumentException("Invalid version '" + version + "'"); 
    }
  }
}
