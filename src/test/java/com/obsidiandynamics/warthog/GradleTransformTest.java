package com.obsidiandynamics.warthog;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

public final class GradleTransformTest {
  @Test
  public void testUpdateSingleLineWithTrailing() {
    final var updated = GradleTransform.updateSingleLine("    fulcrumVersion = \"0.15.1\" // fulcrum version",
                                                         Collections.singletonMap("fulcrum", "0.17.0"));
    assertEquals("    fulcrumVersion = \"0.17.0\" // fulcrum version", updated.line);
    assertEquals("fulcrum", updated.dependencyName);
    assertEquals("0.15.1", updated.oldVersion);
    assertEquals("0.17.0", updated.newVersion);
  }
  
  @Test
  public void testUpdateSingleLineWithoutTrailing() {
    final var updated = GradleTransform.updateSingleLine("    fulcrumVersion = \"0.15.1\"",
                                                         Collections.singletonMap("fulcrum", "0.17.0"));
    assertEquals("    fulcrumVersion = \"0.17.0\"", updated.line);;
    assertEquals("fulcrum", updated.dependencyName);
    assertEquals("0.15.1", updated.oldVersion);
    assertEquals("0.17.0", updated.newVersion);
  }
  
  @Test
  public void testUpdateSingleLineMultipleVersions() {
    final var namesToVersions = new HashMap<String, String>();
    namesToVersions.put("fulcrum", "0.17.0");
    namesToVersions.put("yconf", "0.15.0");
    final var updated = GradleTransform.updateSingleLine("    fulcrumVersion = \"0.15.1\"",
                                                         Collections.singletonMap("fulcrum", "0.17.0"));
    assertEquals("    fulcrumVersion = \"0.17.0\"", updated.line);;
    assertEquals("fulcrum", updated.dependencyName);
    assertEquals("0.15.1", updated.oldVersion);
    assertEquals("0.17.0", updated.newVersion);
  }
}
