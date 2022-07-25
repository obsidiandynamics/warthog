package com.obsidiandynamics.warthog;

import static org.fusesource.jansi.Ansi.*;

import java.util.regex.*;

import org.fusesource.jansi.*;

public final class AnsiString {
  private final String string;

  public AnsiString(String string) {
    this.string = string;
  }
  
  public AnsiString retouchPattern(String regex, Ansi style) {
    final var matcher = Pattern.compile(regex, Pattern.MULTILINE).matcher(string);
    return new AnsiString(matcher.replaceAll(result -> {
      final var group = result.group(0);
      return style.toString() + group + ansi().reset().toString();
    }));
  }
  
  public AnsiString retouchExact(String text, Ansi style) {
    return new AnsiString(string.replace(text, style.a(text).reset().toString()));
  }
  
  @Override
  public String toString() {
    return string;
  }
}