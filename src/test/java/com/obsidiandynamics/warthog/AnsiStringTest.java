package com.obsidiandynamics.warthog;

import static org.fusesource.jansi.Ansi.*;
import static org.junit.Assert.*;

import org.junit.*;

public final class AnsiStringTest {
  @Test
  public void testRetouchMatching() {
    final var string = "hello world";
    final var retouched = new AnsiString(string).retouchPattern("world", ansi().bold());
    assertEquals("hello " + ansi().bold() + "world" + ansi().reset(), retouched.toString());
  }
  @Test
  public void testRetouchMatchingLeadingSpace() {
    final var string = "      Options:";
    final var retouched = new AnsiString(string).retouchPattern("Options:", ansi().bold());
    assertEquals("      " + ansi().bold() + "Options:" + ansi().reset(), retouched.toString());
  }
  
  @Test
  public void testRetouchPatternWhitespace() {
    final var string = "hello world";
    final var retouched = new AnsiString(string).retouchPattern("\\s", ansi().bold());
    assertEquals("hello" + ansi().bold() + " " + ansi().reset() + "world", retouched.toString());
  }
  
  @Test
  public void testRetouchNonMatching() {
    final var string = "hello world";
    final var retouched = new AnsiString(string).retouchPattern("cat", ansi().bold());
    assertEquals(string, retouched.toString());
  }
}
