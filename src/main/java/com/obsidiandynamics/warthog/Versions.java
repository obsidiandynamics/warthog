package com.obsidiandynamics.warthog;

import java.util.*;
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
  
  public static String rollSegment(String version, int segmentIndex) {
    final var versionSegments = parse(version);
    versionSegments.set(segmentIndex, versionSegments.get(segmentIndex) + 1);
    return format(versionSegments);
  }
}
