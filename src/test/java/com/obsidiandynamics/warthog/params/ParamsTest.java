package com.obsidiandynamics.warthog.params;

import static org.junit.Assert.*;

import org.junit.*;

import com.beust.jcommander.*;

public final class ParamsTest {
  @Test(expected=MissingCommandException.class)
  public void testParseUnknown() {
    Params.parse("--foo-bar");
  }
  
  @Test
  public void testParseHelp() {
    final var params = Params.parse("--help");
    assertTrue(params.getCommon().isHelp());
  }
  
  @Test
  public void testMainUsage() {
    final var usage = Params.parse().usage();
    assertNotNull(usage);
    assertTrue("usage=" + usage, usage.startsWith("Usage: hog"));
  }
  
  @Test
  public void testUpdateUsage() {
    final var usage = Params.parse("update").usage();
    assertNotNull(usage);
    assertTrue("usage=" + usage, usage.startsWith("Locates newer packages"));
  }
  
  @Test
  public void testParseCommonWithoutCommand() {
    final var params = Params.parse("-d", "some/directory");
    assertEquals("some/directory", params.getCommon().getDirectory());
    assertNull(params.getCommand());
  }
  
  @Test
  public void testParseCommonWithUpdate() {
    final var params = Params.parse("-d", "some/directory", "update");
    assertEquals("some/directory", params.getCommon().getDirectory());
    assertEquals("update", params.getCommand());
    assertFalse(params.getUpdate().isSkipBuild());
  }
  
  @Test
  public void testParseCommonWithUpdateAndSkipBuild() {
    final var params = Params.parse("-d", "some/directory", "update", "--skip-build");
    assertEquals("some/directory", params.getCommon().getDirectory());
    assertEquals("update", params.getCommand());
    assertTrue(params.getUpdate().isSkipBuild());
  }
  
  @Test
  public void testParseEmpty() {
    final var params = Params.parse();
    assertNull(params.getCommand());
    assertEquals(".", params.getCommon().getDirectory());
  }
  
  @Test
  public void testParseRelease() {
    final var params = Params.parse("release");
    assertEquals("release", params.getCommand());
    assertFalse(params.getRelease().isSkipTag());
    assertFalse(params.getRelease().isSkipPublish());
    assertEquals("[Warthog] Updated dependencies", params.getRelease().getCommitMessage());
  }
}
