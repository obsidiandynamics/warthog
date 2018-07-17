package com.obsidiandynamics.warthog.args;

import static org.junit.Assert.*;

import org.junit.*;

import com.beust.jcommander.*;

public final class ArgsTest {
  @Test(expected=MissingCommandException.class)
  public void testParseUnknown() {
    Args.parse("--foo-bar");
  }
  
  @Test
  public void testParseHelp() {
    final var args = Args.parse("--help");
    assertTrue(args.getCommon().isHelp());
  }
  
  @Test
  public void testMainUsage() {
    final var usage = Args.parse().usage();
    assertNotNull(usage);
    assertTrue("usage=" + usage, usage.startsWith("Usage: hog"));
  }
  
  @Test
  public void testUpdateUsage() {
    final var usage = Args.parse("update").usage();
    assertNotNull(usage);
    assertTrue("usage=" + usage, usage.startsWith("Locates newer packages"));
  }
  
  @Test
  public void testParseCommonWithoutCommand() {
    final var args = Args.parse("-d", "some/directory");
    assertEquals("some/directory", args.getCommon().getDirectory());
    assertNull(args.getCommand());
  }
  
  @Test
  public void testParseCommonWithUpdate() {
    final var args = Args.parse("-d", "some/directory", "update");
    assertEquals("some/directory", args.getCommon().getDirectory());
    assertEquals("update", args.getCommand());
    assertFalse(args.getUpdate().isSkipBuild());
  }
  
  @Test
  public void testParseCommonWithUpdateAndSkipBuild() {
    final var args = Args.parse("-d", "some/directory", "update", "--skip-build");
    assertEquals("some/directory", args.getCommon().getDirectory());
    assertEquals("update", args.getCommand());
    assertTrue(args.getUpdate().isSkipBuild());
  }
  
  @Test
  public void testParseEmpty() {
    final var args = Args.parse();
    assertNull(args.getCommand());
    assertEquals(".", args.getCommon().getDirectory());
  }
  
  @Test
  public void testParseRelease() {
    final var args = Args.parse("release");
    assertEquals("release", args.getCommand());
    assertFalse(args.getRelease().isSkipTag());
    assertFalse(args.getRelease().isSkipPublish());
    assertEquals("[Warthog] Updated dependencies", args.getRelease().getCommitMessage());
  }
}
